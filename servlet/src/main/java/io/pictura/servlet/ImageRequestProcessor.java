/**
 * Copyright 2015, 2016 Steffen Kremp
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.pictura.servlet;

import io.pictura.servlet.URLConnectionFactory.DefaultURLConnectionFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>ImageRequestProcessor</code> is the default implementation of any
 * image based request processor.
 *
 * <p>
 * This processor implements the following image operations:
 * <ul>
 * <li>Scaling</li>
 * <li>Cropping</li>
 * <li>Rotation</li>
 * <li>Trim</li>
 * <li>Apply Border</li>
 * <li>Apply Padding</li>
 * </ul>
 * <p>
 * and the following I/O operations:
 * </p>
 * <ul>
 * <li>Reading</li>
 * <li>Writing</li>
 * <li>Compression (Lossy/Lossless; depending on the available image writers)
 * </ul>
 *
 * <p>
 * To apply image effects (filter), the processor is able to apply
 * {@link BufferedImageOp}'s during the image is processing. As default the
 * following image effects are supported by this implementation:
 * <ul>
 * <li>Antialias</li>
 * <li>Brightness</li>
 * <li>Darkness</li>
 * <li>Sharpen</li>
 * <li>Grayscale</li>
 * <li>Grayscale-Luminosity</li>
 * <li>Color-Inversion</li>
 * <li>Sepia</li>
 * <li>Sun-Set</li>
 * <li>Gamma-Correction</li>
 * <li>Saturation</li>
 * <li>Median</li>
 * <li>Noise</li>
 * <li>Invert</li>
 * <li>Pixelate</li>
 * <li>Posterize</li>
 * <li>Threshold</li>
 * <li>Auto Color</li>
 * <li>Auto Level</li>
 * <li>Auto Contrast</li>
 * </ul>
 *
 * <p>
 * The supported image formats are depending on the runtime environment.
 * Normally, the following image formats are available within this processor on
 * the HotSpot JavaVM:
 * <ul>
 * <li>JPEG</li>
 * <li>GIF</li>
 * <li>Animated GIF</li>
 * <li>PNG</li>
 * <li>BMP</li>
 * <li>ICO (write only)</li>
 * </ul>
 *
 * <p>
 * The parameters for the different operations are given by the URL path the
 * client used to make the request. A single parameter is specified by
 * <code>NAME={VALUE}</code>. During the process, the image processor will parse
 * all path segments to extract the given parameters from the URL. Optionally,
 * it is also possible to pass the parameters in the URL query string or to
 * combine both options. However, it is recommended to use the option to pass
 * the parameters within the URL path because this is normally more SEO and CDN
 * friendly than the option with the query string.
 * <p>
 * A full qualified image processor URL is defined as:
 * <p>
 * <code>
 * /CONTEXT-PATH/PARAM-1={VALUE-1}/PARAM-2={VALUE-2}/../PARAM-n={VALUE-n}/PATH-TO-THE-SOURCE-IMAGE
 * </code>
 * <p>
 * or<p>
 * <code>
 * /CONTEXT-PATH?PARAM-1=(VALUE-1)&amp;PARAM-2=(VALUE-2)&amp;..&amp;PARAM-n=(VALUE-n)
 * </code>
 * <p>
 * where<p>
 * <code>{VALUE}</code> could be a multi parameter value and
 * <code>(value)</code> is always a single parameter value. A multi parameter
 * value is a comma separated combination of one or more values.
 *
 * <p>
 * For example, to rescale the source image "lenna.jpg" on the context path
 * "/pictura" to a width of 320px for a device with the device pixel ratio of
 * 1.5 and to convert the image to the WebP format and to transform the source
 * image into a 16:9 aspect ratio you need to create the following URL:
 *
 * <p>
 * <code>
 * /pictura/f=webp/s=w320,dpr1d5/c=ar16,9/lenna.jpg
 * </code>
 * <p>
 * To rotate the same image additionally 90 clock-wise:
 * <p>
 * <code>
 * /pictura/f=webp/s=w320,dpr1d5/c=ar16,9/r=90/lenna.jpg
 * </code>
 * <p>
 * And to get this image as base 64 encoded string:
 * <p>
 * <code>
 * /pictura/f=webp,b64/s=w320,dpr1d5/c=ar16,9/r=90/lenna.jpg
 * </code>
 *
 * <p>
 * See the detailed documentation to get more informaion about the image
 * control.
 *
 * @see IIORequestProcessor
 * @see RequestProcessor
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public class ImageRequestProcessor extends IIORequestProcessor {

    private static final Log LOG = Log.getLog(ImageRequestProcessor.class);

    /**
     * The image source location parameter name. The image source is required in
     * each request which should be handled by an
     * <code>ImageRequestProcessor</code>. Also this parameter is an virtual
     * parameter in cases if the image resource path is part of the origin URL
     * path. Only in cases if the image source if given via an URL query
     * parameter, the parameter name would also be a part of the URL.
     *
     * @see #getRequestedImage(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_IMAGE = "i";

    /**
     * The image frame index parameter name. 
     */
    protected static final String QPARAM_NAME_PAGE = "n";
    
    /**
     * The image format parameter name. This parameter is a multi-part
     * parameter. With the image format it is possible to specify the format
     * name different format options and an optional encoding if supported.
     *
     * @see #getRequestedFormatName(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedFormatOption(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedFormatEncoding(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_FORMAT = "f";

    /**
     * The image format name parameter name.
     *
     * @see #getRequestedFormatName(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_FORMAT_NAME = "fn";

    /**
     * The image format option parameter name.
     *
     * @see #getRequestedFormatOption(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_FORMAT_OPTION = "fo";

    /**
     * The image format encoding parameter name.
     *
     * @see #getRequestedFormatEncoding(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_FORMAT_ENCODING = "fe";

    /**
     * The image compression parameter name.
     *
     * @see
     * #getRequestedCompressionQuality(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_COMPRESSION_QUALITY = "o";

    /**
     * The image scale parameter name. This parameter is a multi-part parameter.
     *
     * @see #getRequestedScaleWidth(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedScaleHeight(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedScalePixelRatio(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedScaleMethod(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedScaleMode(javax.servlet.http.HttpServletRequest)
     * @see
     * #getRequestedScaleForceUpscale(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_SCALE = "s";

    /**
     * The image scale width parameter name.
     *
     * @see #getRequestedScaleWidth(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_SCALE_WIDTH = "sw";

    /**
     * The image scale height parameter name.
     *
     * @see #getRequestedScaleHeight(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_SCALE_HEIGHT = "sh";

    /**
     * The image scale width percentage parmeter name.
     */
    protected static final String QPARAM_NAME_SCALE_WIDTH_PERCENTAGE = "swp";

    /**
     * The image scale height percentage parmeter name.
     */
    protected static final String QPARAM_NAME_SCALE_HEIGHT_PERCENTAGE = "shp";

    /**
     * The image scale method parameter name.
     *
     * @see #getRequestedScaleMethod(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_SCALE_METHOD = "sq";

    /**
     * The image scale mode parameter name.
     *
     * @see #getRequestedScaleMode(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_SCALE_MODE = "sm";

    /**
     * The image scale force up-scale parameter name.
     *
     * @see
     * #getRequestedScaleForceUpscale(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_SCALE_FORCE_UPSCALE = "su";

    /**
     * The image scale pixel ratio parameter name.
     *
     * @see #getRequestedScalePixelRatio(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_SCALE_PIXEL_RATIO = "sr";

    /**
     * The image crop parameter name. This parameter is a multi-part parameter.
     *
     * @see #getRequestedCropAspectRatioX(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropAspectRatioY(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropBottom(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropHeight(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropLeft(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropRight(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropSquare(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropTop(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropWidth(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropX(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropY(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_CROP = "c";

    /**
     * The image crop x position parameter name.
     *
     * @see #getRequestedCropX(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_CROP_X = "cx";

    /**
     * The image crop y position parameter name.
     *
     * @see #getRequestedCropY(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_CROP_Y = "cy";

    /**
     * The image crop width parameter name.
     *
     * @see #getRequestedCropWidth(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_CROP_WIDTH = "cw";

    /**
     * The image crop height parameter name.
     *
     * @see #getRequestedCropHeight(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_CROP_HEIGHT = "ch";

    /**
     * The image crop top position parameter name.
     *
     * @see #getRequestedCropTop(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_CROP_TOP = "ct";

    /**
     * The image crop left position parameter name.
     *
     * @see #getRequestedCropLeft(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_CROP_LEFT = "cl";

    /**
     * The image crop bottom position parameter name.
     *
     * @see #getRequestedCropBottom(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_CROP_BOTTOM = "cb";

    /**
     * The image crop right position parameter name.
     *
     * @see #getRequestedCropRight(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_CROP_RIGHT = "cr";

    /**
     * The image crop square option parameter name.
     *
     * @see #getRequestedCropSquare(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_CROP_SQUARE = "csq";

    /**
     * The image crop aspect ratio x parameter name.
     *
     * @see #getRequestedCropAspectRatioX(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_CROP_ASPECT_RATIO_X = "carx";

    /**
     * The image crop aspect ratio y parameter name.
     *
     * @see #getRequestedCropAspectRatioY(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_CROP_ASPECT_RATIO_Y = "cary";

    /**
     * The image trim tolerance (distance between two color values) paarameter
     * name to detect the border coords of an image.
     */
    protected static final String QPARAM_NAME_TRIM = "t";

    /**
     * The image rotation parameter name.
     *
     * @see #getRequestedRoatation(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedFlip(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_ROTATION = "r";

    /**
     * The image effects parameter name.
     *
     * @see #getRequestedEffects(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_EFFECT = "e";

    /**
     * The image background color parameter name. Used if the source and
     * destination image are using different color spaces. E.g. if a source
     * image with transparency is converting in a image format which will not
     * support color transparency.
     *
     * @see #getRequestedBackgroundColor(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_BGCOLOR = "bg";

    /**
     * The image padding parameter name.
     *
     * @see #getRequestedPaddingSize(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedPaddingColor(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_PAD = "p";

    /**
     * The image padding size parameter name.
     *
     * @see #getRequestedPaddingSize(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_PAD_COLOR = "pc";

    /**
     * The image padding color parameter name.
     *
     * @see #getRequestedPaddingColor(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_PAD_SIZE = "ps";
    
    /**
     * The image border parameter name.
     *
     * @see #getRequestedBorderSize(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedBorderColor(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_BORDER = "b";
    
    /**
     * The image border size parameter name.
     *
     * @see #getRequestedBorderSize(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_BORDER_COLOR = "bc";
    
    /**
     * The image border color parameter name.
     *
     * @see #getRequestedBorderColor(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_BORDER_SIZE = "bs";
    
    /**
     * The name of the image quality parameter. This is an optional parameter to
     * request an image in an pre-defined image quality. As quality the
     * <code>ImageRequestProcessor</code> supports 5 different modes.
     * <ul>
     * <li><tt>AUTO</tt>: Calculates the compression ratio, scaling method and
     * mode depending on the source image and the other image parameters,
     * automatically.</li>
     * <li><tt>ULTRA_HIGH</tt>: Uses best compression ratio and if possible an
     * lose-less compression mode as well as the scaling method and mode which
     * will create the best result. However you should note, that this mode
     * needs the longest processing time to transform an image.</li>
     * <li><tt>HIGH</tt>: Uses a better compression ratio than the medium
     * quality.</li>
     * <li><tt>MEDIUM</tt>: Uses a compression ratio and scaling method and mode
     * which will fit in most of all cases with the result of an acceptable file
     * size.</li>
     * <li><tt>LOW</tt>: Uses a high compression ratio and a speed optimized
     * scale method.</li>
     * </ul>
     *
     * @see #getRequestedQuality(javax.servlet.http.HttpServletRequest)
     */
    protected static final String QPARAM_NAME_QUALITY = "q";   

    /**
     * Defines the default compression quality if nothing else is defined.
     */
    private static final float DEFAULT_COMPRESSION_QUALITY = 0.85f;

    /**
     * Specifies the quality.
     *
     * @see #getRequestedQuality(javax.servlet.http.HttpServletRequest)
     */
    protected static enum Quality {

        /**
         * Calculates the compression ratio, scaling method and mode depending
         * on the source image and the other image parameters, automatically.
         */
        AUTO,
        /**
         * Uses best compression ratio and if possible an loseless compression
         * mode as well as the scaling method and mode which will create the
         * best result. However you should note, that this mode needs the
         * longest processing time to transform an image
         */
        ULTRA_HIGH,
        /**
         * Uses a better compression ratio than the medium quality.
         */
        HIGH,
        /**
         * Uses a compression ratio and scaling method and mode which will fit
         * in most of all cases with the result of an acceptable file size.
         */
        MEDIUM,
        /**
         * Uses a high compression ratio and a speed optimized scale method.
         */
        LOW;
    }
    
    // Limitations
    long maxImageFileSize;
    long maxImageResolution;

    // The parsed request parameters (lazy load)
    private Map<String, String> parameterMap;

    // The list of requested image effects
    private BufferedImageOp[] effects;

    // The requested quality step
    private Quality quality;

    /**
     * Creates a new image request processor instance.
     */
    public ImageRequestProcessor() {
        super();

        maxImageFileSize = -1L;
        maxImageResolution = -1L;
    }

    /**
     * Status flag to indicate whether the source data reader will ignore the
     * content type of the origin source or not.
     *
     * @return <code>true</code> if the source data reader will ignore the
     * content type of the origin source (e.g. HTTP header in cases of remote
     * located images); otherwise <code>false</code>. The default implementation
     * will return <code>false</code>.
     */
    public boolean ignoreSourceContentType() {
        return false;
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    @Override
    public String getTrueCacheKey() {
        String trueCacheKey = super.getTrueCacheKey();
        if (hasParamsInterceptor()) {
            return getParamsInterceptor().getVaryCacheKey(trueCacheKey, getRequest());
        }
        return trueCacheKey;
    }

    /**
     * Tests whether the related HTTP request is equals to an proxy request or
     * not. Normally a proxy request does not contains any image transformation
     * commands specified by the query.
     *
     * @param req The related request object.
     * @return <code>true</code> if the request is equals to a simple proxy
     * request (without any transformation commands); otherwise
     * <code>false</code>.
     *
     * @see HttpServletRequest
     */
    protected boolean isProxyRequest(HttpServletRequest req) {
        return !hasImageInterceptor()
                && !hasParamsInterceptor()
                && getRequestedFormatName(req) == null
                && getRequestedFormatEncoding(req) == null
                && getRequestedCompressionQuality(req) == null
                && getRequestedScaleWidth(req) == null
                && getRequestedScaleHeight(req) == null
                && getRequestedScaleWidthPercentage(req) == null
                && getRequestedScaleHeightPercentage(req) == null
                && (getRequestedScalePixelRatio(req) == null
                || getRequestedScalePixelRatio(req) == 1.0f)
                && getRequestedTrimTolerance(req) == null
                && getRequestedPaddingSize(req) == null
                && getRequestedBorderSize(req) == null
                && getRequestedCropX(req) == null
                && getRequestedCropY(req) == null
                && getRequestedCropWidth(req) == null
                && getRequestedCropHeight(req) == null
                && getRequestedCropAspectRatioX(req) == null
                && getRequestedCropAspectRatioY(req) == null
                && (getRequestedCropSquare(req) == null
                || !getRequestedCropSquare(req))
                && getRequestedCropTop(req) == null
                && getRequestedCropLeft(req) == null
                && getRequestedCropBottom(req) == null
                && getRequestedCropRight(req) == null
                && getRequestedRotation0(req) == null
                && (getRequestedEffects(req) == null
                || getRequestedEffects(req).length == 0)
                && (getRequestedQuality(req) == null
                || getRequestedQuality(req) == Quality.AUTO)
                && getRequestedPage(req) == null;
    }

    /**
     * Gets the value of the specified request parameter from the related
     * request object. If there was no parameter given in the origin URL it will
     * return <code>null</code>.
     * <p>
     * The <code>ImageRequestProcessor</code> supports two ways to provide
     * requests parameters. The first one (which is the default way) is via part
     * of the URL path, separated by the default URL path delimeter
     * <code>"/"</code>. If enabled (via request attribute)
     * <code>io.pictura.ENABLE_QUERY_PARAMS</code>, the image processor will
     * also keep parameters from the URL query.
     * </p>
     * <p>
     * If URL query parameters are enabled, the image processor will also check
     * whether parameters are duplicated. In this case (if an parameter is
     * duplicated), the image processor will handle an bad request exception.
     * </p>
     *
     * @param req The related request object.
     * @param name The parameter name.
     *
     * @return The value of the asked parameter if present or <code>null</code>
     * if the parameter is not present in the URL.
     *
     * @see #QPARAM_NAME_IMAGE
     * @see #QPARAM_NAME_FORMAT
     * @see #QPARAM_NAME_COMPRESSION_QUALITY
     * @see #QPARAM_NAME_EFFECT
     * @see #QPARAM_NAME_QUALITY
     * @see #QPARAM_NAME_ROTATION
     * @see #QPARAM_NAME_SCALE
     * @see #QPARAM_NAME_CROP
     * @see #QPARAM_NAME_PAD
     * @see #QPARAM_NAME_BORDER
     * @see #QPARAM_NAME_BGCOLOR
     *
     * @see HttpServletRequest
     */
    public String getRequestParameter(HttpServletRequest req, String name) {
        return (req == null || name == null || name.isEmpty()) ? null 
                : getRequestParameters(req).get(name);
    }
        
    /**
     * Gets the requested image source location.
     *
     * @param req The related request object.
     * @return The image source location or <code>null</code> if the request
     * parameter for this is not given or empty.
     */
    protected String getRequestedImage(HttpServletRequest req) {
        String str = req != null ? getRequestParameter(req,
                QPARAM_NAME_IMAGE) : null;
        if (str != null && !str.isEmpty()) {
            return str;
        }
        return null;
    }
    
    /**
     * Gets the requested image frame index (page).
     * 
     * @param req The related request object.
     * @return The image frame index or <code>null</code> if the request
     * parameter for this is not given or empty.
     */
    protected Integer getRequestedPage(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_PAGE) : null, null);
    }

    /**
     * Gets the requested image target format.
     *
     * @param req The related request object.
     * @return The image target format or <code>null</code> if there is no
     * target format is specified.
     */
    protected String getRequestedFormatName(HttpServletRequest req) {
        String str = req != null ? getRequestParameter(req,
                QPARAM_NAME_FORMAT_NAME) : null;
        if (str != null && !str.isEmpty()) {
            return ("pjpg".equals(str) || "bjpg".equals(str)) ? "jpg" : str;
        }
        return null;
    }

    /**
     * Gets the requested image target format options (optional).
     *
     * @param req The related request object.
     * @return The image target format options if specified; otherwise
     * <code>null</code>.
     */
    protected String getRequestedFormatOption(HttpServletRequest req) {
        String str = req != null ? getRequestParameter(req,
                QPARAM_NAME_FORMAT_OPTION) : null;
        if (str != null && !str.isEmpty()) {
            return str;
        } else {
            String fmt = req != null ? getRequestParameter(req,
                    QPARAM_NAME_FORMAT_NAME) : null;
            if (fmt != null) {
                return "pjpg".equals(fmt) ? "p" : "bjpg".equals(fmt) ? "b" : null;
            }
        }
        return null;
    }

    /**
     * Gets the requested image target format encoding (optional).
     *
     * @param req The related request object.
     * @return The image target format encoding if specified; otherwise
     * <code>null</code>.
     */
    protected String getRequestedFormatEncoding(HttpServletRequest req) {
        String str = req != null ? getRequestParameter(req,
                QPARAM_NAME_FORMAT_ENCODING) : null;
        if (str != null && !str.isEmpty()) {
            return str;
        }
        return null;
    }

    /**
     * Gets the requested compression quality. The compression quality is also
     * depending on the requested image quality if a quality was given. In
     * details, if a compression quality is given and a image quality as well,
     * the compression quality will modified by the following formular (only for
     * {@link Quality#MEDIUM} and {@link Quality#LOW}:
     * <p>
     * <code>COMPRESSION QUALITY' = COMPRESSION QUALITY * 0.955F</code> for a
     * specified image quality {@link Quality#MEDIUM} and <code>COMPRESSION
     *  QUALITY' = COMPRESSION QUALITY * 0.855F</code> for a specified image
     * quality {@link Quality#LOW}
     * </p>
     * <p>
     * If a image quality
     * {@link #getRequestedQuality(javax.servlet.http.HttpServletRequest)} with
     * another value as of the above described values are requested, the
     * compression quality is not changed if the compression quality is also
     * given by the request. However if there is no compression quality
     * specified by the request but the image quality is specified, then the
     * follwing compression qualities are used:
     * </p>
     * <ul>
     * <li>{@link Quality#ULTRA_HIGH}: 0.90F</li>
     * <li>{@link Quality#HIGH}: 0.80F</li>
     * <li>{@link Quality#MEDIUM}: 0.75F</li>
     * <li>{@link Quality#LOW}: 0.65F</li>
     * </ul>
     * <p>
     * Note: the compression quality in combination with the image quality
     * {@link Quality#AUTO} is depending on the implementation.
     * </p>
     *
     * @param req The related request object.
     * @return The compression quality in dependence of the requested image
     * quality or <code>null</code> if no compression quality and no image
     * quality was defined.
     *
     * @see #getRequestedQuality(javax.servlet.http.HttpServletRequest)
     */
    protected Float getRequestedCompressionQuality(HttpServletRequest req) {
        Float f = tryParseFloat(req != null ? getRequestParameter(req,
                QPARAM_NAME_COMPRESSION_QUALITY) : null, null);

        if (f != null) {
            if (f > 0) {
                f = f / 100;
            }

            switch (getRequestedQuality(req)) {
                case MEDIUM:
                    return f * 0.955f;
                case LOW:
                    return f * 0.855f;
            }

            return f;
        } else {

            // If there is no user defined compression ratio is given, we will
            // calculate the compression ratio based on the given quality
            // level (if not auto).
            switch (getRequestedQuality(req)) {
                case ULTRA_HIGH:
                    return 0.90f;
                case HIGH:
                    return 0.80f;
                case MEDIUM:
                    return 0.75f;
                case LOW:
                    return 0.65f;
            }
        }

        return null;
    }

    /**
     * Gets the requested image width in px.
     *
     * @param req The related request object.
     * @return The image width in px or <code>null</code> if there was no image
     * width requested.
     *
     * @see #getRequestedScaleHeight(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedScaleWidth(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_SCALE_WIDTH) : null, null);
    }

    /**
     * Gets the requested image height in px.
     *
     * @param req The related request object.
     * @return The image height in px or <code>null</code> if there was no image
     * width requested.
     *
     * @see #getRequestedScaleWidth(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedScaleHeight(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_SCALE_HEIGHT) : null, null);
    }

    /**
     * Gets the requested image width in % from the source image.
     *
     * @param req The related request object.
     * @return The image width in % or <code>null</code> if there was no image
     * width requested.
     *
     * @see
     * #getRequestedScaleHeightPercentage(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedScaleWidthPercentage(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_SCALE_WIDTH_PERCENTAGE) : null, null);
    }

    /**
     * Gets the requested image height in % from the source image.
     *
     * @param req The related request object.
     * @return The image height in % or <code>null</code> if there was no image
     * width requested.
     *
     * @see
     * #getRequestedScaleWidthPercentage(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedScaleHeightPercentage(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_SCALE_HEIGHT_PERCENTAGE) : null, null);
    }

    /**
     * Gets the requested pixel ratio.
     *
     * @param req The related request object.
     * @return The pixel ratio or <code>null</code> if there was no pixel ratio
     * requested.
     */
    protected Float getRequestedScalePixelRatio(HttpServletRequest req) {
        return tryParseFloat(req != null ? getRequestParameter(req,
                QPARAM_NAME_SCALE_PIXEL_RATIO) : null, null);
    }

    /**
     * Indicates whether upscaling is allowed if the requested image dimension
     * is larger than the origin image dimension.
     *
     * @param req The related request object.
     * @return <code>true</code> if upscaling is allowed; otherwise
     * <code>false</code>.
     */
    protected Boolean getRequestedScaleForceUpscale(HttpServletRequest req) {
        return "u".equalsIgnoreCase(getRequestParameter(req,
                QPARAM_NAME_SCALE_FORCE_UPSCALE));
    }

    /**
     * Gets the X-Axis position of the top left corner of the crop.
     *
     * @param req The related request object.
     * @return The X-Axis position of the top left corner of the crop or
     * <code>null</code> if not requested.
     *
     * @see #getRequestedCropY(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropWidth(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropHeight(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedCropX(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_CROP_X) : null, null);
    }

    /**
     * Gets the Y-Axis position of the top left corner of the crop.
     *
     * @param req The related request object.
     * @return The Y-Axis position of the top left corner of the crop or
     * <code>null</code> if not requested.
     *
     * @see #getRequestedCropX(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropWidth(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropHeight(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedCropY(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_CROP_Y) : null, null);
    }

    /**
     * Gets the width in px of the crop.
     *
     * @param req The related request object.
     * @return The width in px of the the crop or <code>null</code> if not
     * requested.
     *
     * @see #getRequestedCropX(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropY(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropHeight(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedCropWidth(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_CROP_WIDTH) : null, null);
    }

    /**
     * Gets the height in px of the crop.
     *
     * @param req The related request object.
     * @return The height in px of the the crop or <code>null</code> if not
     * requested.
     *
     * @see #getRequestedCropX(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropY(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropWidth(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedCropHeight(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_CROP_HEIGHT) : null, null);
    }

    /**
     * Gets the top cropping in px.
     *
     * @param req The related request object.
     * @return The top cropping in px or <code>null</code> if not requested.
     *
     * @see #getRequestedCropLeft(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropBottom(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropRight(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedCropTop(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_CROP_TOP) : null, null);
    }

    /**
     * Gets the left cropping in px.
     *
     * @param req The related request object.
     * @return The left cropping in px or <code>null</code> if not requested.
     *
     * @see #getRequestedCropTop(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropBottom(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropRight(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedCropLeft(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_CROP_LEFT) : null, null);
    }

    /**
     * Gets the bottom cropping in px.
     *
     * @param req The related request object.
     * @return The bottom cropping in px or <code>null</code> if not requested.
     *
     * @see #getRequestedCropTop(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropLeft(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropRight(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedCropBottom(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_CROP_BOTTOM) : null, null);
    }

    /**
     * Gets the right cropping in px.
     *
     * @param req The related request object.
     * @return The right cropping in px or <code>null</code> if not requested.
     *
     * @see #getRequestedCropTop(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropLeft(javax.servlet.http.HttpServletRequest)
     * @see #getRequestedCropBottom(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedCropRight(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_CROP_RIGHT) : null, null);
    }

    /**
     * Gets the crop to a maximum size square area from the center of the source
     * image.
     *
     * @param req The related request object.
     * @return The crop to a maximum size square area from the center of the
     * source image or <code>null</code> if not requested.
     *
     * @see #getRequestedCropSquare(javax.servlet.http.HttpServletRequest)
     */
    protected Boolean getRequestedCropSquare(HttpServletRequest req) {
        return Boolean.parseBoolean(getRequestParameter(req, QPARAM_NAME_CROP_SQUARE));
    }

    /**
     * Gets the proportion on the X-Axis of the area to be selected from the
     * center of the image.
     *
     * @param req The related request object.
     * @return The proportion on the X-Axis of the area to be selected from the
     * center of the image or <code>null</code> if not requested.
     *
     * @see #getRequestedCropAspectRatioY(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedCropAspectRatioX(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_CROP_ASPECT_RATIO_X) : null, null);
    }

    /**
     * Gets the proportion on the Y-Axis of the area to be selected from the
     * center of the image.
     *
     * @param req The related request object.
     * @return The proportion on the Y-Axis of the area to be selected from the
     * center of the image or <code>null</code> if not requested.
     *
     * @see #getRequestedCropAspectRatioX(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedCropAspectRatioY(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_CROP_ASPECT_RATIO_Y) : null, null);
    }

    /**
     * Gets the trim tolerance (max. distance between two colors) to
     * automatically trim (crop) images with borders/paddings.
     *
     * @param req The related request object.
     * @return The tirm tolerance (distance) level.
     */
    protected Float getRequestedTrimTolerance(HttpServletRequest req) {
        Float f = tryParseFloat(req != null ? getRequestParameter(req,
                QPARAM_NAME_TRIM) : null, null);
        if (f != null && (f < 0 || f > 10)) {
            throw new IllegalArgumentException(
                    "Invalid trim level: value must be between 0 and 10");
        }
        return f;
    }

    /**
     * Gets the padding color.
     *
     * @param req The related request object.
     * @return The padding color or <code>null</code> if there was no padding
     * color specified by the request.
     */
    protected Color getRequestedPaddingColor(HttpServletRequest req) {
        String str = req != null ? getRequestParameter(req,
                QPARAM_NAME_PAD_COLOR) : null;
        if (str != null && !str.isEmpty()) {
            final Color res = tryParseColor(str, null);
            if (res == null) {
                throw new IllegalArgumentException("Invalid padding color: " + str);
            }
            return res;
        }        
        return null;
    }

    /**
     * Gets the padding size in px.
     *
     * @param req The related request object.
     * @return The padding size in px or <code>null</code> if there was no
     * padding size specified by the request.
     */
    protected Integer getRequestedPaddingSize(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_PAD_SIZE) : null, null);
    }

    /**
     * Gets the border color.
     *
     * @param req The related request object.
     * @return The border color or <code>null</code> if there was no padding
     * color specified by the request.
     */
    protected Color getRequestedBorderColor(HttpServletRequest req) {
        String str = req != null ? getRequestParameter(req,
                QPARAM_NAME_BORDER_COLOR) : null;        
        if (str != null && !str.isEmpty()) {
            final Color res = tryParseColor(str, null);
            if (res == null) {
                throw new IllegalArgumentException("Invalid border color: " + str);
            }
            return res;
        }        
        return null;
    }
    
    /**
     * Gets the border size in px.
     *
     * @param req The related request object.
     * @return The border size in px or <code>null</code> if there was no
     * padding size specified by the request.
     */
    protected Integer getRequestedBorderSize(HttpServletRequest req) {
        return tryParseInt(req != null ? getRequestParameter(req,
                QPARAM_NAME_BORDER_SIZE) : null, null);
    }    
    
    // Precompiled effect parameter patterns
    private static final Pattern P_EFFECT_BDT = Pattern.compile("^[bdt]{1,1}\\([0-9]{1,3}\\)$");
    private static final Pattern P_EFFECT_GAM_SAT_VIB = Pattern.compile("^(gam|sat|vib){1,1}\\(\\-?[0-9]{1,3}\\)$");
    private static final Pattern P_EFFECT_PX = Pattern.compile("^(px){1,1}\\(\\-?[0-9]{1,3}\\)$");
    private static final Pattern P_EFFECT_BDT_NB = Pattern.compile("^[bdt]{1,1}[0-9]{1,3}$"); // no brackets style
    private static final Pattern P_EFFECT_GAM_SAT_VIB_NB = Pattern.compile("^(gam|sat|vib){1,1}\\-?[0-9]{1,3}$"); // no brackets style
    private static final Pattern P_EFFECT_PX_NB = Pattern.compile("^(px){1,1}\\-?[0-9]{1,3}$"); // no brackets style

    /**
     * Gets the requested image effects which should perform on the source
     * image.
     *
     * @param req The related request object.
     * @return The image effects or <code>null</code> if there are no image
     * effects are requested.
     *
     * @see BufferedImageOp
     */
    protected BufferedImageOp[] getRequestedEffects(HttpServletRequest req) {
        String str = req != null ? getRequestParameter(req,
                QPARAM_NAME_EFFECT) : null;

        if (str != null && !str.isEmpty()) {
            if (effects != null) {
                return effects;
            }
            String[] op = str.split(",");
            if (op.length > 0) {
                ArrayList<BufferedImageOp> l = new ArrayList<>();
                for (String o : op) {
                    switch (o) {
                        case "":
                            break; // ignor this
                        case "a":
                        case "bl": // blur == antialias
                            l.add(Pictura.OP_ANTIALIAS);
                            break;
                        case "b":
                            l.add(Pictura.OP_BRIGHTER);
                            break;
                        case "d":
                            l.add(Pictura.OP_DARKER);
                            break;
                        case "s":
                            l.add(Pictura.OP_SHARPEN);
                            break;
                        case "g":
                            l.add(Pictura.OP_GRAYSCALE);
                            break;
                        case "gl":
                            l.add(Pictura.OP_GRAYSCALE_LUMINOSITY);
                            break;
                        case "sp":
                            l.add(Pictura.OP_SEPIA);
                            break;
                        case "ss":
                            l.add(Pictura.OP_SUNSET);
                            break;
                        case "i":
                            l.add(Pictura.OP_INVERT);
                            break;
                        case "t":
                            l.add(Pictura.OP_THRESHOLD);
                            break;
                        case "p":
                            l.add(Pictura.OP_POSTERIZE);
                            break;
                        case "px":
                            l.add(Pictura.OP_PIXELATE);
                            break;
                        case "m":
                            l.add(Pictura.OP_MEDIAN);
                            break;
                        case "n":
                            l.add(Pictura.OP_NOISE);
                            break;
                        case "ac":
                            l.add(Pictura.OP_AUTO_CONTRAST);
                            break;
                        case "al":
                            l.add(Pictura.OP_AUTO_LEVEL);
                            break;
                        case "as":
                            l.add(Pictura.OP_AUTO_COLOR);
                            break;
                        case "ae": // auto enhancement
                            l.add(Pictura.OP_AUTO_COLOR);
                            l.add(Pictura.OP_AUTO_CONTRAST);
                            l.add(Pictura.OP_AUTO_LEVEL);
                            break;
                        default:
                            boolean noBrackets = false;

                            // Brightness/Darkness/Treshold
                            if (P_EFFECT_BDT.matcher(o).matches()
                                    || (noBrackets = P_EFFECT_BDT_NB.matcher(o).matches())) {

                                float f = tryParseFloat(noBrackets ? o.substring(1, o.length())
                                        : o.substring(o.indexOf('(') + 1, o.length() - 1), Float.NaN);

                                if (!Float.isNaN(f)) {
                                    if (o.startsWith("t")) {
                                        if (f < 0 || f > 255) {
                                            throw new IllegalArgumentException("Invalid effect: argument of \"t\" must be between 0 and 255");
                                        }
                                        l.add(Pictura.getOpThreshold((int) f));
                                    } else {
                                        f = f / 100;
                                        if (o.startsWith("b")) {
                                            f = 1 + f;
                                        } else {
                                            f = 1 - f;
                                        }
                                        if (f < 0) {
                                            throw new IllegalArgumentException("Invalid effect: argument must be > 0");
                                        } else if (f != 0) {
                                            l.add(Pictura.getOpRescale(f));
                                        }
                                    }
                                } else {
                                    throw new IllegalArgumentException("Invalid effect: argument must be a valid number");
                                }
                            } 

                            // Pixelate
                            else if (P_EFFECT_PX.matcher(o).matches()
                                    || (noBrackets = P_EFFECT_PX_NB.matcher(o).matches())) {

                                int s = tryParseInt(noBrackets ? o.substring(2, o.length())
                                        : o.substring(o.indexOf('(') + 1, o.length() - 1), -1);

                                if (s < 10 || s > 100) {
                                    throw new IllegalArgumentException("Invalid effect: argument of \"px\" must be between 10 and 100");
                                }
                                l.add(Pictura.getOpPixelate(s));
                            } 

                            // Gamma, Saturation, Vibrance
                            else if (P_EFFECT_GAM_SAT_VIB.matcher(o).matches()
                                    || (noBrackets = P_EFFECT_GAM_SAT_VIB_NB.matcher(o).matches())) {

                                float f = tryParseFloat(noBrackets ? o.substring(3, o.length())
                                        : o.substring(o.indexOf('(') + 1, o.length() - 1), Float.NaN);

                                if (!Float.isNaN(f)) {
                                    if (o.startsWith("gam")) {
                                        if (f < -100 || f > 100) {
                                            throw new IllegalArgumentException("Invalid effect: gamma correction must be between -100 and 100");
                                        }
                                        l.add(Pictura.getOpGamma(((f + 100) / 100)));
                                    } else if (o.startsWith("sat")) {
                                        if (f < -100 || f > 100) {
                                            throw new IllegalArgumentException("Invalid effect: saturation must be between -100 and 100");
                                        }
                                        l.add(Pictura.getOpSaturation(((f + 100) / 100)));
                                    } else if (o.startsWith("vib")) {
                                        if (f < -100 || f > 100) {
                                            throw new IllegalArgumentException("Invalid effect: vibrance must be between -100 and 100");
                                        }
                                        l.add(Pictura.getOpVibrance(f));
                                    }
                                } else {
                                    throw new IllegalArgumentException("Invalid effect: the effect argument must be a valid number");
                                }
                            } 

                            // Bad parameters
                            else {
                                throw new IllegalArgumentException("Invalid effect: unknown effect type or wrong arguments \"" + o + "\"");
                            }
                    }
                }
                return (effects = l.toArray(new BufferedImageOp[l.size()]));
            }
        }
        return null;
    }

    /**
     * Gets the image background color which should set in cases if the origin
     * image contains an alpha channel but the requested output image does't
     * support the alpha channel.
     *
     * @param req The related request object.
     * @return The background color or <code>null</code> if no background color
     * was requested.
     */
    protected Color getRequestedBackgroundColor(HttpServletRequest req) {
        String str = req != null ? getRequestParameter(req,
                QPARAM_NAME_BGCOLOR) : null;
        if (str != null && !str.isEmpty()) {
            final Color res = tryParseColor(str, null);
            if (res == null) {
                throw new IllegalArgumentException("Invalid background color: " + str);
            }
            return res;
        }
        return null;
    }

    /**
     * Returns the requested rotation clock wise in degrees.
     *
     * @param req The related request object.
     * @return The requested rotation clock wise in degrees.
     *
     * @see #getRequestedFlip(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedRoatation(HttpServletRequest req) {
        Pictura.Rotation rot = getRequestedRotation0(req);
        if (rot != null) {
            return rot == Pictura.Rotation.CW_90 ? 90
                    : rot == Pictura.Rotation.CW_180 ? 180
                            : rot == Pictura.Rotation.CW_270 ? 270 : null;
        }
        return null;
    }

    /**
     * Returns the requested flip mode.
     *
     * @param req The related request object.
     * @return <code>0</code> if horizontal flip is requested or <code>1</code>
     * if vertical filp is requested.
     *
     * @see #getRequestedRoatation(javax.servlet.http.HttpServletRequest)
     */
    protected Integer getRequestedFlip(HttpServletRequest req) {
        Pictura.Rotation rot = getRequestedRotation0(req);
        if (rot != null) {
            return rot == Pictura.Rotation.FLIP_HORZ ? 0
                    : rot == Pictura.Rotation.FLIP_VERT ? 1 : null;
        }
        return null;
    }

    private Pictura.Rotation getRequestedRotation0(HttpServletRequest req) {
        String str = req != null ? getRequestParameter(req,
                QPARAM_NAME_ROTATION) : null;
        if (str != null && !str.isEmpty()) {
            str = str.toLowerCase(Locale.ENGLISH);
            switch (str) {
                case "l":
                case "90":
                case "cw90":
                    return Pictura.Rotation.CW_90;
                case "lr":
                case "rl":
                case "180":
                case "cw180":
                    return Pictura.Rotation.CW_180;
                case "r":
                case "270":
                case "cw270":
                    return Pictura.Rotation.CW_270;
                case "h":
                    return Pictura.Rotation.FLIP_HORZ;
                case "v":
                    return Pictura.Rotation.FLIP_VERT;
                case "a":                    
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Invalid rotation: \"" + str + "\"");
            }
        }
        return null;
    }

    // Helper method to determine the required image rotation based on
    // the provided Exif metadata (if present). If there is no Exif metadata
    // available we can not calculate any required image rotation.
    private Pictura.Rotation getRequestedAutoRotation(HttpServletRequest req,
            ImageReader ir) throws IOException {

        String str = req != null ? getRequestParameter(req,
                QPARAM_NAME_ROTATION) : null;
        
        if (str != null && "a".equalsIgnoreCase(str)) {
            int orientation = PicturaExif.getOrientation(ir, 0);
            switch (orientation) {
                case 1:
                    break;
                case 2: 
                    // Flip X
                    return Pictura.Rotation.FLIP_HORZ;
                case 3: 
                    // PI rotation
                    return Pictura.Rotation.CW_180;
                case 4: 
                    // Flip Y
                    return Pictura.Rotation.FLIP_VERT;
                case 5: 
                    // -PI/2 and Flip X
                    // not supported
                    break;
                case 6: 
                    // -PI/2 and -width
                    return Pictura.Rotation.CW_90;
                case 7: 
                    // PI/2 and Flip
                    // not supported
                    break;
                case 8: 
                    // PI/2
                    return Pictura.Rotation.CW_270;
                default:
                    break;
            }
        }
        return null;
    }

    /**
     * Gets the requested image quality. This affects the compression quality
     * and the used scaling method.
     *
     * @param req The related request object.
     * @return The image quality or {@link Quality#AUTO} if no other quality
     * level was requested.
     *
     * @see
     * #getRequestedCompressionQuality(javax.servlet.http.HttpServletRequest)
     */
    protected Quality getRequestedQuality(HttpServletRequest req) {
        if (quality == null) {
            String str = req != null ? getRequestParameter(req,
                    QPARAM_NAME_QUALITY) : null;
            if (str != null && !str.isEmpty()) {
                switch (str) {
                    case "u":
                    case "uh":
                    case "ultrahigh":
                        quality = Quality.ULTRA_HIGH;
                        break;
                    case "h":
                    case "high":
                        quality = Quality.HIGH;
                        break;
                    case "m":
                    case "medium":
                        quality = Quality.MEDIUM;
                        break;
                    case "l":
                    case "low":
                        quality = Quality.LOW;
                        break;
                    case "a":
                    case "auto":
                        quality = Quality.AUTO;
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Invalid quality: \"" + str + "\"");
                }
            } else {
                quality = Quality.AUTO;
            }
        }
        return quality;
    }

    private Pictura.Method getRequestedScaleMethod(HttpServletRequest req) {
        int m = tryParseInt(getRequestParameter(req, QPARAM_NAME_SCALE_METHOD), -1);
        if (m > -1) {
            switch (m) {
                case 0:
                    return Pictura.Method.AUTOMATIC;
                case 1:
                    return Pictura.Method.BALANCED;
                case 2:
                    return Pictura.Method.SPEED;
                case 3:
                    return Pictura.Method.QUALITY;
                case 4:
                    return Pictura.Method.ULTRA_QUALITY;
                default:
                    throw new IllegalArgumentException(
                            "Invalid scale method: \"" + m + "\"");
            }
        } else {
            switch (getRequestedQuality(req)) {
                case ULTRA_HIGH:
                    return Pictura.Method.ULTRA_QUALITY;
                case HIGH:
                    return Pictura.Method.QUALITY;
                case MEDIUM:
                    return Pictura.Method.BALANCED;
                case LOW:
                    return Pictura.Method.SPEED;
            }
        }
        return null;
    }

    private Pictura.Mode getRequestedScaleMode(HttpServletRequest req) {
        String s = getRequestParameter(req, QPARAM_NAME_SCALE_MODE);
        if (s != null && !s.isEmpty()) {
            switch (s) {
                case "0":
                case "a":
                    return Pictura.Mode.AUTOMATIC;
                case "1":
                case "b":
                    return Pictura.Mode.BEST_FIT_BOTH;
                case "2":
                case "e":
                    return Pictura.Mode.FIT_EXACT;
                case "3":
                case "w":
                    return Pictura.Mode.FIT_TO_WIDTH;
                case "4":
                case "h":
                    return Pictura.Mode.FIT_TO_HEIGHT;
                case "5":
                case "c":
                    return Pictura.Mode.CROP;
                default:
                    throw new IllegalArgumentException(
                            "Invalid scale mode: \"" + s + "\"");
            }
        }
        return null;
    }

    // Precompiled regex to identify an URL path parameter
    private static final Pattern P_PATH_PARAMETER = Pattern.compile("^[a-zA-Z]{1}[^=/]{0,}=[^=/]*");

    // Parses the request URI from the given servlet request and creates
    // a key value map with all request parameters.
    private Map<String, String> getRequestParameters(HttpServletRequest req) {
        if (parameterMap != null) {
            return parameterMap;
        }       

        Map<String, String> map = new HashMap<>();

        String ctxPath = req.getContextPath();
        String reqUri = req.getRequestURI();
        if (!"/".equals(ctxPath) && ctxPath != null) {
            reqUri = reqUri.replaceFirst(ctxPath, "");
        }

        String reqPath = reqUri;
        String reqSourcePath = null;

        boolean urlEnc = false;
        if (reqPath.contains("://") || (urlEnc = reqPath.contains("%3A%2F%2F"))) {
            
            int pos = reqPath.indexOf(urlEnc ? "%3A%2F%2F" : "://");
            if (pos > 0) {
                
                int i = pos;
                char[] reqPathArr = reqPath.toCharArray();
                                
                while (i > 0) {
                    if (reqPathArr[i] == '/') {
                        break;
                    }
                    i--;
                }
                
                reqSourcePath = reqPath.substring(++i);
                if (urlEnc) {
                    try {
                        reqSourcePath = URLDecoder.decode(reqSourcePath, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                reqPath = reqPath.substring(0, i);
                if (reqPath.endsWith("/")) {
                    reqPath = reqPath.substring(0, reqPath.length() - 1);
                }
            }
        }
        
        String imgSourcePath = null;

        // Remove the servlet path only if the calculated request path
        // is not equals to the servlet path (char by char)
        if (!req.getServletPath().equals(reqPath)) {
            reqPath = reqPath.replaceFirst(req.getServletPath(), "");
        }

        // A valid request starts always with an "/"
        if (reqPath.startsWith("/")) {
            reqPath = reqPath.substring(1);

            String[] params = reqPath.split("/");
            ArrayList<String> sourceParts = new ArrayList<>();

            for (String param : params) {
                if (param == null || param.isEmpty()) {
                    continue;
                }

                if (P_PATH_PARAMETER.matcher(param).matches()) {

                    String[] nameValue = param.split("=");

                    // Always the first param wins if there are multiple
                    // parameters from the same name present.
                    if (nameValue.length > 0) {
                        if (!map.containsKey(nameValue[0].toLowerCase(Locale.ENGLISH))) {
                            map.put(nameValue[0].toLowerCase(Locale.ENGLISH), nameValue.length > 1
                                    ? nameValue[1].toLowerCase(Locale.ENGLISH) : null);
                        } else {
                            // Try to concat
                            final String val = map.get(nameValue[0].toLowerCase(Locale.ENGLISH));
                            if (val == null) {
                                map.put(nameValue[0].toLowerCase(Locale.ENGLISH),
                                        nameValue.length > 1
                                                ? nameValue[1].toLowerCase(Locale.ENGLISH) : null);
                            } else if (nameValue[1].length() > 1) {
                                // FIXME: Does not work with effect parameter
                                if (!val.contains(nameValue[1].toLowerCase(Locale.ENGLISH))) {
                                    map.put(nameValue[0].toLowerCase(Locale.ENGLISH),
                                            val + "," + nameValue[1].toLowerCase(Locale.ENGLISH));
                                } else {
                                    throw new IllegalArgumentException(
                                            "Duplicated parameter: " + nameValue[0]);
                                }
                            }
                        }
                    }
                } else {
                    sourceParts.add(param);
                }
            }

            // Rebuild the requested image source path from the splitted
            // URL parts.
            if (reqSourcePath == null) {
                StringBuilder source = new StringBuilder();
                for (int j = 0; j < sourceParts.size(); j++) {
                    source.append(sourceParts.get(j));
                    if (j < sourceParts.size() - 1) {
                        source.append("/");
                    }
                }
                imgSourcePath = source.toString();
                if (imgSourcePath.contains("%3F")) { // --> ?
                    try {
                        imgSourcePath = URLDecoder.decode(imgSourcePath, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } else {
                imgSourcePath = reqSourcePath;
            }
        }

        map.put(QPARAM_NAME_IMAGE, (imgSourcePath != null
                ? imgSourcePath : reqSourcePath));

        if (req.getAttribute("io.pictura.servlet.ENABLE_QUERY_PARAMS") instanceof Boolean
                && (Boolean) req.getAttribute("io.pictura.servlet.ENABLE_QUERY_PARAMS")) {
            // Test whether the request contains query parameters. If yes, we will
            // add the parameters to the parameter map for this request but only
            // parameters which are not already present in the map.
            Enumeration<String> enumNames = req.getParameterNames();
            while (enumNames.hasMoreElements()) {
                String name = enumNames.nextElement();
                if (map.get(name) == null) {
                    String value = req.getParameter(name);
                    map.put(name, value);
                } else {
                    throw new IllegalArgumentException("Duplicate parameter: " + name);
                }
            }
        }

        // Parse combined parameters (if present)
        parseRequestParamFormat(map);
        parseRequestParamCompression(map);
        parseRequestParamScale(map);
        parseRequestParamCrop(map);
        parseRequestParamPad(map);
        parseRequestParamBorder(map);
        
        return (parameterMap = !hasParamsInterceptor() ? map
                : getParamsInterceptor().intercept(map, req));
    }

    // Precompiled format parameter patterns
    private static final Pattern P_FORMAT_NAME = Pattern.compile("^(jpg|pjpg|bjpg|jpeg|jp2|j2k|jpeg2000|webp|png|gif|bmp|wbmp|tif|tiff|pcx|[a-z]{3,}|[a-z]{2,2}[0-9]{1,1}|[a-z0-9]{4,})$");
    private static final Pattern P_FORMAT_OPTION = Pattern.compile("^[pb]{1,1}$");
    private static final Pattern P_FORMAT_ENCODING = Pattern.compile("^(b64){1,1}$");

    private void parseRequestParamFormat(Map<String, String> map) {
        String f = map.get(QPARAM_NAME_FORMAT);
        if (f != null && !f.isEmpty()) {
            f = f.toLowerCase(Locale.ENGLISH);
            String[] fnfo = f.split(",");
            switch (fnfo.length) {
                case 1:
                    if (P_FORMAT_ENCODING.matcher(fnfo[0]).matches()) {
                        map.put(QPARAM_NAME_FORMAT_ENCODING, fnfo[0]);
                    } else if (P_FORMAT_NAME.matcher(fnfo[0]).matches()) {
                        map.put(QPARAM_NAME_FORMAT_NAME, fnfo[0]);
                    } else if (P_FORMAT_OPTION.matcher(fnfo[0]).matches()) {
                        map.put(QPARAM_NAME_FORMAT_OPTION, fnfo[0]);
                    } else {
                        throw new IllegalArgumentException("Invalid format: "
                                + "unknown argument \"" + f + "\"");
                    }
                    break;
                case 2:
                case 3:
                    Pattern[] fp = new Pattern[]{P_FORMAT_NAME, P_FORMAT_OPTION, P_FORMAT_ENCODING};
                    for (String s : fnfo) {
                        for (int i = 0; i < fp.length; i++) {
                            if (fp[i].matcher(s).matches()) {
                                map.put(i == 0 ? QPARAM_NAME_FORMAT_NAME
                                        : i == 1 ? QPARAM_NAME_FORMAT_OPTION
                                                : QPARAM_NAME_FORMAT_ENCODING, s);
                            }
                        }
                    }
                    int fc = 0;
                    if (map.get(QPARAM_NAME_FORMAT_NAME) != null) {
                        fc++;
                    }
                    if (map.get(QPARAM_NAME_FORMAT_OPTION) != null) {
                        fc++;
                    }
                    if (map.get(QPARAM_NAME_FORMAT_ENCODING) != null) {
                        fc++;
                    }
                    if (fc != fnfo.length) {
                        throw new IllegalArgumentException("Invalid format: "
                                + "unknown argument(s) \"" + f + "\"");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid format: "
                            + (fnfo.length == 0 ? "empty parameter"
                                    : fnfo.length > 3 ? "too many arguments"
                                            : "unknown argument \"" + f + "\""));
            }
        }
    }

    // Precompiled compression parameter patterns
    private static final Pattern P_COMPRESSION_QUALITY = Pattern.compile("^[0-9]{1,3}");

    private void parseRequestParamCompression(Map<String, String> map) {
        String c = map.get(QPARAM_NAME_COMPRESSION_QUALITY);
        if (c != null && !c.isEmpty()) {
            c = c.toLowerCase(Locale.ENGLISH);
            if (!P_COMPRESSION_QUALITY.matcher(c).matches()) {
                throw new IllegalArgumentException(
                        "Invalid compression: invalid argument \"" + c + "\"");
            }
            map.put(QPARAM_NAME_COMPRESSION_QUALITY, c);
        }
    }

    // Precompiled scale parameter patterns
    private static final Pattern P_SCALE_WH = Pattern.compile("^(w|h)[0-9]{1,5}$");
    private static final Pattern P_SCALE_WHP = Pattern.compile("^(w|h)[0-9]{1,3}p$");
    private static final Pattern P_SCALE_DPR = Pattern.compile("^dpr[0-9]{1,1}((\\.|d)[0-9]{1,2})?$");
    private static final Pattern P_SCALE_U = Pattern.compile("^u$");
    private static final Pattern P_SCALE_Q = Pattern.compile("^q[0-4]{1,1}");
    private static final Pattern P_SCALE_M = Pattern.compile("^m[0-5]{1,1}");

    private void parseRequestParamScale(Map<String, String> map) {
        String s = map.get(QPARAM_NAME_SCALE);
        if (s != null && !s.isEmpty()) {
            String[] swsh = s.split(",");
            if (swsh.length > 0) {
                for (String so : swsh) {
                    if (P_SCALE_WH.matcher(so).matches()) {
                        if (so.startsWith("w")) {
                            map.put(QPARAM_NAME_SCALE_WIDTH, so.substring(1));
                        } else if (so.startsWith("h")) {
                            map.put(QPARAM_NAME_SCALE_HEIGHT, so.substring(1));
                        }
                    } else if (P_SCALE_DPR.matcher(so).matches()) {
                        String pd = so.substring(3);
                        if (pd.contains("d")) {
                            pd = pd.replace("d", ".");
                        }
                        map.put(QPARAM_NAME_SCALE_PIXEL_RATIO, pd);
                    } else if (P_SCALE_WHP.matcher(so).matches()) {
                        if (so.startsWith("w")) {
                            map.put(QPARAM_NAME_SCALE_WIDTH_PERCENTAGE, so.substring(1, so.length() - 1));
                        } else if (so.startsWith("h")) {
                            map.put(QPARAM_NAME_SCALE_HEIGHT_PERCENTAGE, so.substring(1, so.length() - 1));
                        }
                    } else if (P_SCALE_U.matcher(so).matches()) {
                        map.put(QPARAM_NAME_SCALE_FORCE_UPSCALE, so);
                    } else if (P_SCALE_Q.matcher(so).matches()) {
                        map.put(QPARAM_NAME_SCALE_METHOD, so.substring(1));
                    } else if (P_SCALE_M.matcher(so).matches()) {
                        map.put(QPARAM_NAME_SCALE_MODE, so.substring(1));
                    } else {
                        throw new IllegalArgumentException(
                                "Invalid scale: unknown argument \"" + so + "\"");
                    }
                }
            } else {
                throw new IllegalArgumentException(
                        "Invalid scale: empty parameter");
            }
        }
    }

    // Precompiled crop parameter patterns
    private static final Pattern P_CROP_X = Pattern.compile("^x[0-9]{1,}$");
    private static final Pattern P_CROP_Y = Pattern.compile("^y[0-9]{1,}$");
    private static final Pattern P_CROP_W = Pattern.compile("^w[0-9]{1,}$");
    private static final Pattern P_CROP_H = Pattern.compile("^h[0-9]{1,}$");
    private static final Pattern P_CROP_T = Pattern.compile("^t[0-9]{1,}$");
    private static final Pattern P_CROP_L = Pattern.compile("^l[0-9]{1,}$");
    private static final Pattern P_CROP_B = Pattern.compile("^b[0-9]{1,}$");
    private static final Pattern P_CROP_R = Pattern.compile("^r[0-9]{1,}$");
    private static final Pattern P_CROP_INT = Pattern.compile("^[0-9]{1,}$");

    private void parseRequestParamCrop(Map<String, String> map) {
        String d = map.get(QPARAM_NAME_CROP);
        if (d != null && !d.isEmpty()) {
            d = d.toLowerCase(Locale.ENGLISH);

            // test for aspect ratio crop request
            if (d.startsWith("ar")) {
                String[] xy = d.replaceFirst("ar", "").split(d.contains("x") ? "x" : ",");
                if (xy.length == 2 && P_CROP_INT.matcher(xy[0]).matches()
                        && P_CROP_INT.matcher(xy[1]).matches()) {
                    map.put(QPARAM_NAME_CROP_ASPECT_RATIO_X, xy[0]);
                    map.put(QPARAM_NAME_CROP_ASPECT_RATIO_Y, xy[1]);
                    return;
                } else {
                    throw new IllegalArgumentException("Invalid crop: wrong aspect ratio arguments");
                }
            }

            String[] cxywh = d.split(",");

            if (cxywh.length == 1 && "sq".equals(cxywh[0])) {
                map.put(QPARAM_NAME_CROP_SQUARE, "true");
                return;
            } else if (d.contains("sq")) {
                throw new IllegalArgumentException("Invalid crop: too many arguments");
            }

            if (cxywh.length > 4 || cxywh.length < 1) {
                throw new IllegalArgumentException("Invalid crop: "
                        + (cxywh.length < 1 ? "incomplete crop coords"
                                : "too many arguments"));
            }

            if (d.contains("x") || d.contains("y") || d.contains("w") || d.contains("h")) {
                final Pattern[] p = new Pattern[]{P_CROP_X, P_CROP_Y, P_CROP_W, P_CROP_H};
                for (String c : cxywh) {
                    for (int j = 0; j < p.length; j++) {
                        if (p[j].matcher(c).matches()) {
                            map.put(j == 0 ? QPARAM_NAME_CROP_X
                                    : j == 1 ? QPARAM_NAME_CROP_Y
                                            : j == 2 ? QPARAM_NAME_CROP_WIDTH
                                                    : QPARAM_NAME_CROP_HEIGHT, c.substring(1));
                        }
                    }
                }
            } else if (d.contains("t") || d.contains("l") || d.contains("b") || d.contains("r")) {
                final Pattern[] p = new Pattern[]{P_CROP_T, P_CROP_L, P_CROP_B, P_CROP_R};
                for (String c : cxywh) {
                    for (int j = 0; j < p.length; j++) {
                        if (p[j].matcher(c).matches()) {
                            map.put(j == 0 ? QPARAM_NAME_CROP_TOP
                                    : j == 1 ? QPARAM_NAME_CROP_LEFT
                                            : j == 2 ? QPARAM_NAME_CROP_BOTTOM
                                                    : QPARAM_NAME_CROP_RIGHT, c.substring(1));
                        }
                    }
                }
            } else if (cxywh.length == 2) {
                map.put(QPARAM_NAME_CROP_X, cxywh[0]);
                map.put(QPARAM_NAME_CROP_Y, cxywh[1]);
            } else if (cxywh.length == 4) {
                map.put(QPARAM_NAME_CROP_X, cxywh[0]);
                map.put(QPARAM_NAME_CROP_Y, cxywh[1]);
                map.put(QPARAM_NAME_CROP_WIDTH, cxywh[2]);
                map.put(QPARAM_NAME_CROP_HEIGHT, cxywh[3]);
            } else {
                throw new IllegalArgumentException("Invalid crop: incomplete crop coords");
            }
        }
    }

    // Precompiled pad parameter patterns
    private static final Pattern P_PAD_SIZE_COLOR = Pattern.compile("^[0-9]{1,2}\\,[0-9a-f]{3,8}$");

    private void parseRequestParamPad(Map<String, String> map) {
        String s = map.get(QPARAM_NAME_PAD);
        if (s != null && !s.isEmpty()) {
            if (P_PAD_SIZE_COLOR.matcher(s).matches()) {
                String[] sizeColor = s.split(",");
                int size = tryParseInt(sizeColor[0], -1);
                if (size < 1 || size > 99) {
                    throw new IllegalArgumentException("Inavlid padding: size must be between 1 and 99");
                }
                map.put(QPARAM_NAME_PAD_SIZE, sizeColor[0]);

                if (sizeColor[1].length() == 6 || sizeColor[1].length() == 8) {
                    map.put(QPARAM_NAME_PAD_COLOR, sizeColor[1]);
                } else if (sizeColor[1].length() == 3 || sizeColor[1].length() == 4) {
                    String shortColor = sizeColor[1];
                    String newColor = "#";
                    for (char c : shortColor.toCharArray()) {
                        newColor += Character.toString(c) + Character.toString(c);
                    }
                    map.put(QPARAM_NAME_PAD_COLOR, newColor);
                } else {
                    throw new IllegalArgumentException(
                            "Invalid padding: wrong color value \"" + sizeColor[1] + "\"");
                }
            } else {
                throw new IllegalArgumentException(
                        "Invalid padding: unknown or wrong argument \"" + s + "\"");
            }
        }
    }
    
    // Precompiled border parameter patterns
    private static final Pattern P_BORDER_SIZE_COLOR = Pattern.compile("^[0-9]{1,2}\\,[0-9a-f]{3,8}$");

    private void parseRequestParamBorder(Map<String, String> map) {
        String s = map.get(QPARAM_NAME_BORDER);
        if (s != null && !s.isEmpty()) {
            if (P_BORDER_SIZE_COLOR.matcher(s).matches()) {
                String[] sizeColor = s.split(",");
                int size = tryParseInt(sizeColor[0], -1);
                if (size < 1 || size > 99) {
                    throw new IllegalArgumentException("Inavlid border: size must be between 1 and 99");
                }
                map.put(QPARAM_NAME_BORDER_SIZE, sizeColor[0]);

                if (sizeColor[1].length() == 6 || sizeColor[1].length() == 8) {
                    map.put(QPARAM_NAME_BORDER_COLOR, sizeColor[1]);
                } else if (sizeColor[1].length() == 3 || sizeColor[1].length() == 4) {
                    String shortColor = sizeColor[1];
                    String newColor = "#";
                    for (char c : shortColor.toCharArray()) {
                        newColor += Character.toString(c) + Character.toString(c);
                    }
                    map.put(QPARAM_NAME_BORDER_COLOR, newColor);
                } else {
                    throw new IllegalArgumentException(
                            "Invalid border: wrong color value \"" + sizeColor[1] + "\"");
                }
            } else {
                throw new IllegalArgumentException(
                        "Invalid border: unknown or wrong argument \"" + s + "\"");
            }
        }
    }
    
    @Override
    protected void doProcess(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String srcPath;
        // This is the first call of the get parameters method. If something
        // is wrong with the permitted request arguments we will get an
        // illegal argument exception from the affected parameter call.
        try {
            srcPath = getRequestedImage(req);
        } catch (IllegalArgumentException ex) {
            doInterrupt(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            return;
        }

        URL srcUrl;
        // Depending on the registered resource locators we are able to get
        // a valid URL object to the requested source or not. In this case
        // if the URL object is null it means the requested image was not
        // found. Or if there is no source path to an image defined (in this 
        // request) we definately know that the request arguments are bad.
        if (srcPath == null || srcPath.isEmpty()) {
            doInterrupt(HttpServletResponse.SC_BAD_REQUEST,
                    "The requested image path cannot be not null or empty.");
            return;
        } else if (srcPath.startsWith("file:/")) {
            doInterrupt(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } else if ((srcUrl = getResource(srcPath)) == null) {
            doInterrupt(HttpServletResponse.SC_NOT_FOUND,
                    "There was no image found on the requested path.");
            return;
        }

        // Now, we have to differentiate between local files and remote files.
        // In Java locale files (described as an URL) can have a "file" or
        // "jndi" protocol. In both cases we have to transform the URL object
        // to a valid file object. In cases of a remote requested image source
        // we can continue with the already given URL object.
        try {
            maxImageFileSize = (Long) req.getAttribute("io.pictura.servlet.MAX_IMAGE_FILE_SIZE");
            maxImageResolution = (Long) req.getAttribute("io.pictura.servlet.MAX_IMAGE_RESOLUTION");

            if (srcUrl.getHost() != null && "".equals(srcUrl.getHost())) {
                if ("file".equalsIgnoreCase(srcUrl.getProtocol())) {
                    doProcessFile(new File(srcUrl.toURI()), req, resp);
                } else if ("jndi".equalsIgnoreCase(srcUrl.getProtocol())) {
                    doProcessFile(new File(req.getServletContext().getRealPath(
                            srcPath)), req, resp);
                } else {
                    doProcessURL(srcUrl, req, resp);
                }
            } else {
                doProcessURL(srcUrl, req, resp);
            }
        } catch (IllegalArgumentException ex) {
            doInterrupt(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (URISyntaxException ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Called from the processor instance to handle the specified image resource
     * file object.
     *
     * @param f The resource file (image) to process.
     * @param req The related request object.
     * @param resp The related response object.
     *
     * @throws ServletException if an input or output error is detected when the
     * servlet handles the request.
     * @throws IOException if the request for could not be handled.
     */
    protected void doProcessFile(File f, HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {

        // Check if the resource is available
        if (f == null || !f.exists()) {
            doInterrupt(HttpServletResponse.SC_NOT_FOUND,
                    "The requested image resource can not be resolved.");
            return;
        }

        // Test if the requested resource is readable and a valid file object
        if (f.isDirectory()) {
            doInterrupt(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (!f.canRead()) {
            doInterrupt(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (f.length() > maxImageFileSize) {
            doInterrupt(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }

        String eTag = getETagByFile(f);
        String ifNoneMatch = req.getHeader(HEADER_IFNONMATCH);

        if (eTag.equalsIgnoreCase(ifNoneMatch)) {
            doInterrupt(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        // Set the ETag and Date for this request
        resp.setHeader(HEADER_ETAG, eTag);
        resp.setDateHeader(HEADER_DATE, System.currentTimeMillis());
        resp.setDateHeader(HEADER_LASTMOD, f.lastModified());

        if (isDebugEnabled()) {
            resp.setHeader("X-Pictura-Lookup", System.currentTimeMillis() - getTimestamp() + "ms");
        }

        // TODO: Check content type
        if (isProxyRequest(req)) {
            doProcessFileProxy(f, req, resp);
        } else {
            // Use NIO byte buffer
            try (FileChannel ch = new FileInputStream(f).getChannel()) {
                long length = f.length();
                req.setAttribute("io.pictura.servlet.SRC_IMAGE_SIZE", length);
                doProcessImage(new ByteBufferInputStream(ch.map(
                        FileChannel.MapMode.READ_ONLY, 0, length)), req, resp);
            }
        }
    }

    private void doProcessFileProxy(File f, HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {

        // On a proxy request we can check the if modified since header
        // because the response would be the same for all clients.
        long ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
        if (ifModifiedSince > -1L) {
            if (f.lastModified() <= ifModifiedSince) {
                doInterrupt(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }

        String mimeType = PicturaImageIO.getMIMETypeFromFile(f);
        if (mimeType != null) {
            // TODO: Check if this works correct!
            resp.setContentType(mimeType);
        }
        resp.setContentLength((int) f.length());

        OutputStream os = new ContextOutputStream(req, resp.getOutputStream());

        // Use NIO byte buffer
        try (FileChannel ch = new FileInputStream(f).getChannel()) {

            InputStream is = new ByteBufferInputStream(ch.map(
                    FileChannel.MapMode.READ_ONLY, 0, f.length()));

            int len;
            byte[] buf = new byte[1024 * 16];

            while ((len = is.read(buf)) > -1) {
                os.write(buf, 0, len);
            }
        }
    }

    /**
     * Called from the processor instance to handle the specified image resource
     * URL object.
     *
     * @param url The resource URL (image) to process.
     * @param req The related request object.
     * @param resp The related response object.
     *
     * @throws ServletException if an input or output error is detected when the
     * servlet handles the request.
     * @throws IOException if the request for could not be handled.
     */
    protected void doProcessURL(URL url, HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {

        if (url == null) {
            doInterrupt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The requested image resource can not be resolved.");
            return;
        } else if (url.getProtocol() == null) {
            doInterrupt(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Tell the client the location of the original resource
        if ((Boolean) req.getAttribute("io.pictura.servlet.HEADER_ADD_CONTENT_LOCATION")) {
            resp.setHeader(HEADER_CONTLOC, url.toExternalForm());
        }

        URLConnection con = null;
        try {
            con = getURLConnection(url, req);
            if (isDebugEnabled()) {
                con.setRequestProperty("X-Pictura-RequestId", getRequestId().toString());
            }
            doProcessURLConnection(con, req, resp);
        } catch (RuntimeException e) {
            doInterrupt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    e.getMessage());
        } catch (IOException ex) {
            doInterrupt(HttpServletResponse.SC_BAD_GATEWAY,
                    "Gateway error: " + ex.getMessage());
        } finally {
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Closed (disconnected) connection from url["
                            + con.getURL().toExternalForm() + "]");
                }
            }
        }
    }

    private void doProcessURLConnection(URLConnection con, HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {

        if (con == null) {
            doInterrupt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        final boolean isHttp = con instanceof HttpURLConnection;

        // Connect to the remote server, now.
        con.connect();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Opened connection to url[" + con.getURL().toExternalForm() + "]");
        }

        // Add last modified if it's present in the response from
        // the origin.
        long lastModified = con.getLastModified();
        if (lastModified > 0L) {
            resp.setDateHeader(HEADER_LASTMOD, lastModified);
        }

        // Set an ETag based as true cache key from this.            
        resp.setHeader(HEADER_ETAG, getETagByDate(lastModified));

        // If the remote server tells us something about caching,
        // we will force the values to the client.
        if (con.getHeaderField(HEADER_CACHECONTROL) != null) {
            resp.setHeader(HEADER_CACHECONTROL, con.getHeaderField(HEADER_CACHECONTROL));
        }
        if (con.getHeaderField(HEADER_EXPIRES) != null) {
            resp.setHeader(HEADER_EXPIRES, con.getHeaderField(HEADER_EXPIRES));
        }
        if (con.getHeaderField(HEADER_PRAGMA) != null) {
            resp.setHeader(HEADER_PRAGMA, con.getHeaderField(HEADER_PRAGMA));
        }

        // Ok, now we have to check the date header too because
        // we (maybe) have a different date as the origin server.
        // If the origin response contains a date header we will
        // use this value in our response, too.
        resp.setDateHeader(HEADER_DATE, con.getHeaderFieldDate(HEADER_DATE,
                System.currentTimeMillis()));

        final int sc = isHttp ? ((HttpURLConnection) con).getResponseCode() : HttpURLConnection.HTTP_OK;
        if (sc == HttpURLConnection.HTTP_OK) {

            if (isDebugEnabled()) {
                resp.setHeader("X-Pictura-Lookup", System.currentTimeMillis() - getTimestamp() + "ms");
            }

            // Additional check. We will accept only image/* mimetypes.
            if (!ignoreSourceContentType()) {
                String ct = con.getContentType();
                if (ct == null || !ct.toLowerCase(Locale.ENGLISH).startsWith("image/")) {
                    doInterrupt(HttpServletResponse.SC_BAD_GATEWAY,
                            ct == null ? "There was no mime type given by the origin resource."
                                    : "The origin resource content type was \"" + ct
                                    + "\" but \"image/*\" was expected.");
                    return;
                }
            }

            InputStream is = null; // Body data stream

            req.setAttribute("io.pictura.servlet.SRC_IMAGE_SIZE", con.getContentLengthLong());

            if (con.getContentLengthLong() > maxImageFileSize) {
                doInterrupt(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                return;
            } else if (con.getContentLengthLong() < 0) {
                // The server will not tell us how lare the resource
                // is. We need to calculate this on our side.
                long bytesRead = 0L;
                int len;

                byte[] buf = new byte[1024 * 16]; // read in 16kB blocks
                is = new BufferedInputStream(con.getInputStream());

                FastByteArrayOutputStream bos = new FastByteArrayOutputStream();

                while ((len = is.read(buf)) > -1) {
                    bos.write(buf, 0, len);
                    bytesRead += len;

                    if (bytesRead > maxImageFileSize) {
                        req.setAttribute("io.pictura.servlet.BYTES_READ", bytesRead);
                        doInterrupt(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                        is.close();
                        return;
                    }
                }

                is.close();
                is = new FastByteArrayInputStream(bos);

                req.setAttribute("io.pictura.servlet.SRC_IMAGE_SIZE", bytesRead);
            }

            // Check if content language is available
            if (con.getHeaderField(HEADER_CONTLANG) != null) {
                resp.setHeader(HEADER_CONTLANG, con.getHeaderField(HEADER_CONTLANG));
            }

            if (isProxyRequest(req)) {
                doProcessURLProxy(is == null ? con : is, req, resp);
            } else {
                // The input and output streams returned by an URLConnection 
                // are not buffered. Therefore we will wrap it to increase 
                // the performance.
                doProcessImage(is != null ? is : new ContextInputStream(
                        req, con.getInputStream()), req, resp);
            }
        } else if (sc == HttpServletResponse.SC_NOT_MODIFIED) {
            resp.setContentLength(0);
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else if ((sc == HttpServletResponse.SC_MOVED_TEMPORARILY
                || sc == HttpServletResponse.SC_MOVED_PERMANENTLY)
                && con.getHeaderField(HEADER_LOCATION) != null) {

            StringBuffer bufURL = req.getRequestURL();
            if (req.getQueryString() != null && !req.getQueryString().isEmpty()) {
                bufURL.append("?").append(req.getQueryString());
            }

            String newURL = bufURL.toString();
            newURL = newURL.replace(con.getURL().toString(), con.getHeaderField(HEADER_LOCATION));

            resp.setContentLength(0);
            resp.setStatus(sc);
            resp.setHeader(HEADER_LOCATION, newURL);
        } else if (isHttp) {
            doInterrupt(sc, ((HttpURLConnection) con).getResponseMessage());
        } else {
            doInterrupt(HttpServletResponse.SC_BAD_GATEWAY);
        }
    }

    private void doProcessURLProxy(Object source, HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {

        InputStream is = null;
        try {
            if (source instanceof URLConnection) {
                URLConnection con = (URLConnection) source;

                resp.setContentLength(con.getContentLength());
                resp.setContentType(con.getContentType());

                // The input and output streams returned by an URLConnection are 
                // not buffered. Therefore we will wrap it to increase the
                // performance.
                is = new ContextInputStream(req, con.getInputStream());
            } else if (source instanceof InputStream) {
                is = (InputStream) source;
            } else {
                doInterrupt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            OutputStream os = new ContextOutputStream(req, resp.getOutputStream());

            int len;
            byte[] buf = new byte[1024 * 16];

            while ((len = is.read(buf)) > -1) {
                os.write(buf, 0, len);
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private URLConnection getURLConnection(URL url, HttpServletRequest req)
            throws IOException {

        if (req.getAttribute("io.pictura.servlet.URL_CONNECTION_FACTORY") instanceof URLConnectionFactory) {
            return ((URLConnectionFactory) req.getAttribute("io.pictura.servlet.URL_CONNECTION_FACTORY"))
                    .newConnection(url, getURLConnectionProperties(req));
        }
        return DefaultURLConnectionFactory.getDefault().newConnection(
                url, getURLConnectionProperties(req));
    }

    private Properties getURLConnectionProperties(HttpServletRequest req) {
        final Properties props = new Properties() {

            private static final long serialVersionUID = 4976479401693155004L;

            @Override
            public synchronized Object put(Object key, Object value) {
                if (key == null) {
                    return null;
                }
                if (value == null) {
                    return super.remove(key);
                }
                return super.put(key, value);
            }

        };

        props.put("io.pictura.servlet.HTTP_AGENT", req.getAttribute("io.pictura.servlet.HTTP_AGENT"));
        props.put("io.pictura.servlet.HTTP_CONNECT_TIMEOUT", req.getAttribute("io.pictura.servlet.HTTP_CONNECT_TIMEOUT"));
        props.put("io.pictura.servlet.HTTP_READ_TIMEOUT", req.getAttribute("io.pictura.servlet.HTTP_READ_TIMEOUT"));
        props.put("io.pictura.servlet.HTTP_MAX_FORWARDS", req.getAttribute("io.pictura.servlet.HTTP_MAX_FORWARDS"));
        props.put("io.pictura.servlet.HTTP_FOLLOW_REDIRECTS", req.getAttribute("io.pictura.servlet.HTTP_FOLLOW_REDIRECTS"));

        props.put("io.pictura.servlet.HTTPS_DISABLE_CERTIFICATE_VALIDATION", req.getAttribute("io.pictura.servlet.HTTPS_DISABLE_CERTIFICATE_VALIDATION"));

        if (req.getAttribute("io.pictura.servlet.HTTP_PROXY_HOST") != null) {
            props.put("io.pictura.servlet.HTTP_PROXY_HOST", req.getAttribute("io.pictura.servlet.HTTP_PROXY_HOST"));
            props.put("io.pictura.servlet.HTTP_PROXY_PORT", req.getAttribute("io.pictura.servlet.HTTP_PROXY_PORT"));
        }

        if (req.getAttribute("io.pictura.servlet.HTTPS_PROXY_HOST") != null) {
            props.put("io.pictura.servlet.HTTPS_PROXY_HOST", req.getAttribute("io.pictura.servlet.HTTPS_PROXY_HOST"));
            props.put("io.pictura.servlet.HTTPS_PROXY_PORT", req.getAttribute("io.pictura.servlet.HTTPS_PROXY_PORT"));
        }

        props.put(HEADER_IFMODSINCE, req.getHeader(HEADER_IFMODSINCE));

        return props;
    }

    /**
     * Called to process the image from the given stream. This method reads the
     * source image from the stream and will process (rescale, rotate, crop and
     * append effects) the image and produce the target image for the give
     * parameters which are defined by this request processor.
     * <p>
     * If the data from the given stream is not a valid image or the image
     * format could not be handeld by this image processor, the method will
     * interrupt the process and send an error to the client.
     *
     * @param is The source image input stream.
     * @param req The related request object.
     * @param resp The related response object.
     *
     * @throws ServletException if an input or output error is detected when the
     * servlet handles the request.
     * @throws IOException if the request for could not be handled.
     */
    protected void doProcessImage(InputStream is, HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {

        if (is == null || req == null || resp == null) {
            doInterrupt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        BufferedImage src = null; // The source input image
        BufferedImage out = null; // The converted output image (master frame)                              

        BufferedImage[] srcSequence = null;

        try {
            // Test the format parameter. In this case if the parameter is present
            // but the value is empty we have a bad request. In the case if the
            // parameter is not present we will use the original source image
            // format as output format, too (see below).
            String formatName = getRequestedFormatName(req);
            if (formatName != null) {
                if (formatName.isEmpty()) {
                    doInterrupt(HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid format: the format parameter is present but "
                            + "there was no format specified");
                    return;
                } else if (!canWriteFormat(formatName)) {
                    doInterrupt(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                            "Invalid format: the requested image output format \""
                            + formatName + "\" is not supported by this server.");
                    return;
                }
            }

            // As default we enable progressive writing. In cases if progressive
            // is disabled, when the flag "B" is set, we will set the flag to 
            // false (e.g. use baseline instead of progressive).
            boolean progressive = true;
            String formatOpt = getRequestedFormatOption(req);
            if (formatOpt != null && "b".equals(formatOpt.toLowerCase(Locale.ENGLISH))) {
                progressive = false;
            }

            // The client can choose between "default" and "base64" encoded
            // image response. As default "base64" is "false".
            boolean base64 = false;
            String formatEnc = getRequestedFormatEncoding(req);
            if (formatEnc != null && "b64".equals(formatEnc.toLowerCase(Locale.ENGLISH))) {
                base64 = true;
                if (base64 && req.getAttribute("io.pictura.servlet.ENABLE_BASE64_IMAGE_ENCODING") instanceof Boolean
                        && !(Boolean) req.getAttribute("io.pictura.servlet.ENABLE_BASE64_IMAGE_ENCODING")) {
                    doInterrupt(HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid format encoding: the server does not support base 64 encoded output images");
                    return;
                }
            }

            // Get compression quality if present
            Float compressionQuality = getRequestedCompressionQuality(req);
            if ((compressionQuality != null && (compressionQuality == Float.NaN
                    || compressionQuality < 0f || compressionQuality > 1f))) {

                // Bounds check failed (e.g. compression ration less than zero
                // or greather than 100 or the specified compression ratio
                // is not a valid number).
                doInterrupt(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid compression: the compression quality must be "
                        + "between 0 and 100");
                return;
            } else if (compressionQuality == null) {
                compressionQuality = DEFAULT_COMPRESSION_QUALITY;
            }

            // Get scale options if present
            Integer scaleWidth = getRequestedScaleWidth(req);
            Integer scaleHeight = getRequestedScaleHeight(req);
            Float scalePixelRatio = getRequestedScalePixelRatio(req);
            Pictura.Method scaleMethod = getRequestedScaleMethod(req);
            Pictura.Mode scaleMode = getRequestedScaleMode(req);
            if (scaleWidth != null && scaleWidth < 1
                    || scaleHeight != null && scaleHeight < 1
                    || scalePixelRatio != null && scalePixelRatio <= 0f) {

                // Bounds check failed (e.g. the user tries to request
                // an image with negative dimensions)
                doInterrupt(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid scale: the image width, height and pixel "
                        + "ratio must be > 1");
                return;
            }
            Boolean scaleForceUpscale = getRequestedScaleForceUpscale(req);

            // Get the tolerance color for trim white spaces
            Float trimWhiteSpaces = getRequestedTrimTolerance(req);
            if (trimWhiteSpaces == null) {
                trimWhiteSpaces = -1f;
            } else {
                if (trimWhiteSpaces < 0f || trimWhiteSpaces > 10f) {
                    doInterrupt(HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid trim: the tolerance must be between 0 and 10");
                    return;
                }
                trimWhiteSpaces /= 100;
            }

            // Check if a squared image crop is requested
            Boolean cropSquare = getRequestedCropSquare(req);

            Integer cropArX = !cropSquare ? getRequestedCropAspectRatioX(req) : null;
            Integer cropArY = cropArX != null ? getRequestedCropAspectRatioY(req) : null;
            if (cropArX != null) {
                if (cropArX == 0) {
                    doInterrupt(HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid crop: aspect ratio value must be > 0 [x=0]");
                    return;
                }
                if (cropArY == 0) {
                    doInterrupt(HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid crop: aspect ratio value must be > 0 [y=0]");
                    return;
                }
            }

            // Get the (basic) crop coords if present
            Integer cropX = (!cropSquare && cropArX == null) ? getRequestedCropX(req) : null;
            Integer cropY = (!cropSquare && cropArX == null) ? getRequestedCropY(req) : null;
            Integer cropWidth = (!cropSquare && cropArX == null) ? getRequestedCropWidth(req) : null;
            Integer cropHeight = (!cropSquare && cropArX == null) ? getRequestedCropHeight(req) : null;

            // Get the (advanced - directors cut) crop coords if present
            Integer cropTop = (!cropSquare && cropArX == null) ? getRequestedCropTop(req) : null;
            Integer cropLeft = (!cropSquare && cropArX == null) ? getRequestedCropLeft(req) : null;
            Integer cropBottom = (!cropSquare && cropArX == null) ? getRequestedCropBottom(req) : null;
            Integer cropRight = (!cropSquare && cropArX == null) ? getRequestedCropRight(req) : null;

            // Get rotation direction if present
            Pictura.Rotation rotation = getRequestedRotation0(req);

            // Get effects if present
            BufferedImageOp[] ops = getRequestedEffects(req);
            if (ops != null && ops.length > 0) {
                int maxImageEffects = -1;
                if (req.getAttribute("io.pictura.servlet.MAX_IMAGE_EFFECTS") != null) {
                    maxImageEffects = (int) req.getAttribute("io.pictura.servlet.MAX_IMAGE_EFFECTS");
                }
                if (maxImageEffects > -1 && ops.length > maxImageEffects) {
                    doInterrupt(HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid effect: too many effects requested "
                            + "[max: " + maxImageEffects + "]");
                    return;
                }
            }

            // Our image reader objects with which we will try to read and
            // decode the specified source image object
            ImageInputStream iis = null;
            ImageReader ir = null;
            
            int srcW = 1;
            int srcH = 1;
            
            // The requested image frame index
            Integer index = getRequestedPage(req);
            Dimension scaleTargetSize = null;

            // We will try to read the image based on a new image input stream.
            // The image reader instance is detected by the service provider
            // interface of the Java ImageIO API.
            int srcSequenceDelayTime = 0;
            try {
                long startDecodeImage = -1L;
                if (LOG.isTraceEnabled()) {
                    startDecodeImage = System.currentTimeMillis();
                }

                if ((ir = createImageReader(iis = createImageInputStream(is))) != null) {

                    boolean gif = false;

                    if (index != null && "gif".equalsIgnoreCase(ir.getFormatName())
                            && (formatName == null || "gif".equalsIgnoreCase(formatName))) {
                        ir.setInput(iis);
                        gif = true;
                    } else {
                        ir.setInput(iis, true, true);
                        
                        if (rotation == null) {
                            rotation = getRequestedAutoRotation(req, ir);
                        }
                    }

                    final int numImages = ir.getNumImages(!ir.isSeekForwardOnly());
                    
                    if (index != null && (index < ir.getMinIndex() || 
                            (numImages > -1 && index > numImages -1))) {                        
                        doInterrupt(HttpServletResponse.SC_BAD_REQUEST, 
                                "Requested frame index out of bounds");
                        return;
                    } else if (index == null) {
                        index = 0;
                    }
                    
                    // The source image dimension in px
                    srcW = ir.getWidth(index);
                    srcH = ir.getHeight(index);
                    
                    final long dim = srcW * srcH;

                    // Check whether we are able to process the image
                    if (maxImageResolution > -1L && (dim > maxImageResolution)) {
                        doInterrupt(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                                "The source image raw resolution (width x height) is too large "
                                + "[max: " + maxImageResolution + "]");
                        return;
                    }
                    
                    // Scale percentage
                    Integer scaleWidthP = getRequestedScaleWidthPercentage(req);
                    if (scaleWidth == null && scaleWidthP != null) {
                        scaleWidth = (int) (srcW * (scaleWidthP / 100.f));
                    }
                    Integer scaleHeightP = getRequestedScaleHeightPercentage(req);
                    if (scaleHeight == null && scaleHeightP != null) {
                        scaleHeight = (int) (srcH * (scaleHeightP / 100.f));
                    }

                    // Bounds check for ICO output format
                    if ("ico".equals(formatName)) {
                        if ((scaleWidth != null && scaleWidth > 256)
                                || (scaleHeight != null && scaleHeight > 256)) {
                            
                            doInterrupt(HttpServletResponse.SC_BAD_REQUEST,
                                    "Invalid scale: the max image width and height "
                                    + "for the requested format are 256 px");
                            return;
                            
                        } else if (scaleWidth == null && scaleHeight == null
                                && (srcW > 256 || srcH > 256)) {
                            if (srcW > srcH) {
                                scaleWidth = 256;
                            } else {
                                scaleHeight = 256;
                            }                            
                        }
                        
                        // Bounds check in combination with DPR
                        if (scalePixelRatio != null && scalePixelRatio > 1f) {
                            if (scaleWidth != null && (scaleWidth * scalePixelRatio) > 256) {
                                scaleWidth = 256;
                            }
                            if (scaleHeight != null && (scaleHeight * scalePixelRatio) > 256) {
                                scaleHeight = 256;
                            }
                        }
                    }
                    
                    scaleTargetSize = calculateTargetDimension(srcW, srcH, 
                            scaleWidth != null ? scaleWidth : -1, 
                            scaleHeight != null ? scaleHeight : -1, 
                            scalePixelRatio != null ? scalePixelRatio : 1f, 
                            scaleForceUpscale != null ? scaleForceUpscale : false);
                    
                    // Prevent us from DoS by requesting big image dimensions in cases
                    // of force upscale
                    if (scaleForceUpscale && scaleTargetSize != null && 
                            ((long)scaleTargetSize.width * (long)scaleTargetSize.height > maxImageResolution)) {
                        
                        doInterrupt(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                                "The target image resolution (width x height) is too large "
                                + "[max: " + maxImageResolution + "]");
                        return;
                    }

                    final long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                    // Prevent OutOfMemoryError (ARGB -> 32 bit == 4 bytes per px)
                    if ((dim * 4 * 1.1f) > (Runtime.getRuntime().maxMemory() - usedMemory)) {
                        LOG.warn("Image processing aborted because of not enough free memory");
                        throw new UnavailableException("Not enough free resources to process the image", 30);
                    }

                    // Read GIF sequences only if the requested output format
                    // is also GIF; otherwise it makes no sense to read and
                    // decode each frame from the animation.
                    if (gif) {
                        GIFSequenceReader reader = new GIFSequenceReader(ir);
                        BufferedImage[] frames = reader.readAllFrames();
                        if (frames.length > 0) {
                            src = frames[0];
                            if (maxImageResolution < 0 || (src.getWidth() * src.getHeight() < maxImageResolution)) {
                                srcSequence = new BufferedImage[frames.length - 1];
                                for (int i = 1; i < frames.length; i++) {
                                    srcSequence[i - 1] = frames[i];
                                }
                            }
                        }
                        srcSequenceDelayTime = reader.getDelayTime();
                    } else {
                        src = ir.read(index, ir.getDefaultReadParam());
                    }

                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Source image decoded in " + (System.currentTimeMillis() - startDecodeImage) 
                                + "ms [" + getRequestURI() + "]");
                    }

                    // TODO: Prevent image meta data (IIOMetadata) if the user
                    // has configured this?!
                }
            } catch (IOException | RuntimeException ex) {
                if (ex instanceof SocketException
                        || ex instanceof SocketTimeoutException) {
                    doInterrupt(HttpServletResponse.SC_GATEWAY_TIMEOUT);
                    return;
                } else if (ex instanceof IndexOutOfBoundsException) {
                    doInterrupt(HttpServletResponse.SC_BAD_REQUEST, 
                            "Requested frame index out of bounds");
                    return;
                } else {
                    LOG.error("Unexpected exception while try to decode the source image from \""
                            + getRequestedImage(req) + "\"", ex);
                    ir = null; // unsupported media
                }
            } finally {
                if (ir != null) {
                    // Dispose reader to avoid memory leaks
                    ir.dispose();
                }
                if (iis != null) {
                    try {
                        iis.close();
                    } catch (IOException ex) {
                    }
                }
            }

            // If we have no valid image instance from the source image or
            // we can't read the image because of missing decoders. In this
            // case it is not possible to process the image.
            if (src == null || ir == null) {
                doInterrupt(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                        "The server was not able to decode the source image "
                        + "or the image format is not supported by the server.");
                return;
            }

            // Set output format equals to the input format if not specified
            // by the user
            if (formatName == null) {
                formatName = ir.getFormatName().toLowerCase(Locale.ENGLISH);
                if (!canWriteFormat(formatName)
                        && (formatName = getFallbackFormatName(formatName)) == null) {
                    doInterrupt(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                            "The server is not able to encode the image.");
                    return;
                }
            }

            // Calculate the absolute crop coords if crop SQUARE is requested            
            if (cropSquare) {
                final int cropBand = cropWidth = cropHeight = srcW > srcH ? srcH : srcW;
                final int cropHalfBand = cropBand / 2;

                cropX = srcW / 2 - cropHalfBand;
                cropY = srcH / 2 - cropHalfBand;
            } // Calculate the aspect ratio crop
            else if (cropArX != null) {
                float r0 = (float) srcW / (float) srcH;
                float r1 = (float) cropArX / (float) cropArY;

                if (r0 != r1) {
                    int arw, arh; // new image size                    

                    if (Objects.equals(cropArX, cropArY)) {
                        arw = srcH < srcW ? srcH : srcW;
                        arh = arw;
                    } else {
                        arw = srcW;

                        // if w is given --> h = w/X * Y
                        arh = (int) ((srcW / (float) cropArX) * cropArY);

                        if (arh > srcH) {
                            arh = srcH;
                            arw = (int) ((srcH / (float) cropArY) * cropArX);
                        }
                    }

                    // calcualte the crop coords
                    if (arw < srcW) {
                        cropX = (srcW - arw) / 2;
                        cropWidth = srcW - (cropX * 2);
                    }
                    if (arh < srcH) {
                        cropY = (srcH - arh) / 2;
                        cropHeight = srcH - (cropY * 2);
                    }
                }
            } // Calculate the absolute crop coords if crop T,L,B,R is requested
            else if (cropTop != null || cropLeft != null
                    || cropBottom != null || cropRight != null) {

                cropX = cropLeft != null ? cropLeft : 0;
                cropY = cropTop != null ? cropTop : 0;
                cropWidth = srcW - (cropRight != null ? cropRight : 0) - (cropLeft != null ? cropLeft : 0);
                cropHeight = srcH - (cropBottom != null ? cropBottom : 0) - (cropTop != null ? cropTop : 0);
            } // Calculate the absolute crop coords if crop W,H is requested
            else if (cropX == null && cropY == null
                    && cropWidth != null && cropHeight != null) {

                cropX = srcW / 2 - cropWidth / 2;
                cropY = srcH / 2 - cropHeight / 2;
            }

            // Bounds check
            if (cropX != null || cropY != null
                    || cropWidth != null || cropHeight != null) {

                if (cropX == null) {
                    cropX = 0;
                }

                if (cropY == null) {
                    cropY = 0;
                }

                if (cropWidth == null) {
                    cropWidth = srcW - cropX;
                }

                if (cropHeight == null) {
                    cropHeight = srcH - cropY;
                }
            }

            // Padding
            Integer padSize = getRequestedPaddingSize(req);
            Color padColor = null;
            if (padSize != null && padSize > 0) {
                padColor = getRequestedPaddingColor(req);
                if (padColor == null) {
                    padColor = getRequestedBackgroundColor(req);
                    if (padColor == null) {
                        padColor = Color.WHITE;
                    }
                }
            }
            
            // Border
            Integer borderSize = getRequestedBorderSize(req);
            Color borderColor = borderSize != null ? getRequestedBorderColor(req) : null;
            
            // Process the image as specified by the client
            long startProcessImageFrames = -1L;
            if (LOG.isTraceEnabled()) {
                startProcessImageFrames = System.currentTimeMillis();
            }

            BufferedImage[] tmp = doProcessImageFrames(new BufferedImage[]{src},
                    trimWhiteSpaces, cropX, cropY, cropWidth, cropHeight,
                    scaleTargetSize, scaleMethod, scaleMode, rotation, padSize, 
                    padColor, borderSize, borderColor, ops);

            out = tmp.length > 0 ? tmp[0] : null;

            // Something goes wrong! We have no master frame to send back to
            // the client.
            if (out == null) {
                doInterrupt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "The server was not able to process the image transformation.");
                return;
            }

            // We need to transfrom the color model
            if (out.getColorModel().hasAlpha() && ("jpg".equals(formatName)
                    || "jpeg".equals(formatName) || "ico".equals(formatName))) {
                final Color bg = getRequestedBackgroundColor(req);
                out = Pictura.convertToRGBImage(out, bg != null ? bg : Color.WHITE);
            } else if ("wbmp".equals(formatName)) {
                out = Pictura.convertToBinaryImage(src);
            }

            IIOWriteParam writeParams = new IIOWriteParam();
            writeParams.setFormatName(formatName);
            writeParams.setProgressive(progressive);
            writeParams.setCompressionQuality(compressionQuality);
            writeParams.setBase64Encoded(base64);
            writeParams.setAppendMetadata(false);
            writeParams.setAnimationDelayTime(srcSequenceDelayTime);

            if (srcSequence != null && srcSequence.length > 0 && "gif".equals(formatName)) {
                ArrayList<BufferedImage> outFrames = new ArrayList<>();
                outFrames.add(out);

                BufferedImage[] outSequence = doProcessImageFrames(srcSequence,
                        trimWhiteSpaces, cropX, cropY, cropWidth, cropHeight,
                        scaleTargetSize, scaleMethod, scaleMode, rotation, padSize,
                        padColor, borderSize, borderColor, ops);

                outFrames.addAll(Arrays.asList(outSequence));
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Image processed in " + (System.currentTimeMillis() - startProcessImageFrames) 
                            + "ms [" + getRequestURI() + "]");
                }
                doWriteImage(outFrames.toArray(new BufferedImage[outFrames.size()]), writeParams, req, resp);
            } else {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Image processed in " + (System.currentTimeMillis() - startProcessImageFrames) 
                            + "ms [" + getRequestURI() + "]");
                }
                doWriteImage(out, writeParams, req, resp);
            }
        } catch (IllegalArgumentException e) {
            doInterrupt(HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), e);
        } catch (IOException e) {
            doInterrupt(HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (Exception | Error e) {
            doInterrupt(HttpServletResponse.SC_CONFLICT,
                    "The server was not able to process the image.", e);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                // nothing to do here!
            }
            if (src != null) {
                src.flush();
            }
            if (out != null) {
                out.flush();
            }
        }
    }
    
    private BufferedImage[] doProcessImageFrames(BufferedImage[] frames,
            Float trim, Integer cropX, Integer cropY, Integer cropWidth,
            Integer cropHeight, Dimension scaleTargetSize, 
            Pictura.Method scaleMethod, Pictura.Mode scaleMode, 
            Pictura.Rotation rotation, Integer padSize, Color padColor,
            Integer borderSize, Color borderColor, BufferedImageOp[] effects) {

        BufferedImage[] outS = new BufferedImage[frames.length];

        try {
            for (int i = 0; i < frames.length; i++) {

                // The current frame
                BufferedImage srcS = frames[i];

                // Trim white spaces
                BufferedImage srcSTrim = (trim >= 0f && trim <= 1f)
                        ? trimImage(srcS, trim) : srcS;
                srcS.flush();

                // Crop the current frame
                BufferedImage srcSCropped = (cropX != null && cropY != null
                        && cropWidth != null && cropHeight != null) ? cropImage(
                                        srcS, cropX, cropY, cropWidth, cropHeight) : srcSTrim;
                srcSTrim.flush();                

                // Scale the current frame
                BufferedImage srcSScaled = scaleImage(srcSCropped, scaleTargetSize,
                        scaleMethod != null ? scaleMethod : Pictura.Method.AUTOMATIC,
                        scaleMode != null ? scaleMode : Pictura.Mode.AUTOMATIC,
                        padSize != null ? padSize : -1);
                srcSCropped.flush();

                // Rotate the current frame
                BufferedImage srcSRotated = rotateImage(srcSScaled, rotation);
                srcSScaled.flush();
                
                // Append effects to the current frame
                BufferedImage srcSEffects = filterImage(srcSRotated, effects);
                srcSRotated.flush();
                
                // Add a colorized padding to the current frame
                BufferedImage srcSPadded = padImage(srcSEffects, padSize != null ? padSize : -1, padColor);
                srcSEffects.flush();
                
                // Add colorized border to the current frame
                BufferedImage srcSBorder = borderImage(srcSPadded, borderSize != null ? borderSize : -1, borderColor);
                srcSPadded.flush();                
                
                // Store the result in the output sequence buffer
                outS[i] = srcSBorder;
            }
        } catch (Exception ex) {
            LOG.error("Exception in process image. See nested exception for more details", ex);
            throw new RuntimeException(ex);
        }
        return outS;
    }

    private BufferedImage cropImage(BufferedImage src, int x, int y,
            int width, int height) {
        if (x > -1 && y > -1 && width > -1 && height > -1) {
            return Pictura.crop(src, x, y, width, height);
        }
        return src;
    }

    private BufferedImage trimImage(BufferedImage src, float trim) {
        if (trim >= 0f && trim <= 100f) {
            return Pictura.trim(src, trim);
        }
        return src;
    }

    private BufferedImage scaleImage(BufferedImage src, Dimension dim,
            Pictura.Method method, Pictura.Mode mode, int padding) {
        
        if (dim != null) {
            int targetWidth = dim.width;
            int targetHeight = dim.height;

            if (padding > 0) {
                int padSize = 2 * padding;
                targetWidth -= padSize;
                targetHeight -= padSize;
            }
            
            return Pictura.resize(src, method != null ? method : Pictura.Method.AUTOMATIC,
                    mode != null ? mode : Pictura.Mode.AUTOMATIC, targetWidth, targetHeight);
        }
        return src;
    }
    
    private BufferedImage rotateImage(BufferedImage src, Pictura.Rotation rotation) {
        if (rotation != null) {
            return Pictura.rotate(src, rotation);
        }
        return src;
    }

    private BufferedImage padImage(BufferedImage src, int padding, Color color) {
        if (padding > 0 && padding < 100 && color != null) {
            return Pictura.pad(src, padding, color);
        }
        return src;
    }
    
    private BufferedImage borderImage(BufferedImage src, int border, Color color) {
        if (border > 0 && border < 100 && color != null) {
            return Pictura.border(src, border, color);
        }
        return src;
    }

    private BufferedImage filterImage(BufferedImage src, BufferedImageOp... ops) {
        if (ops != null && ops.length > 0) {
            return Pictura.apply(src, ops);
        }
        return src;
    }

    private static Dimension calculateTargetDimension(int srcWidth, int srcHeight, 
            int width, int height, float pixelRatio, boolean forceUpscale) {
        
        if (width > -1 || height > -1 || forceUpscale
                || (pixelRatio > 0.f && (width > -1 || height > -1))) {

            int targetWidth = width > -1 ? width : srcWidth;
            int targetHeight = height > -1 ? height : srcHeight;

            if (width > 0 && height == -1) {
                float scal = (float) targetWidth / (float) srcWidth;
                targetHeight = (int) (scal * srcHeight);
            } else if (width == -1 && height > 0) {
                float scal = (float) targetHeight / (float) srcHeight;
                targetWidth = (int) (scal * srcWidth);
            } else if (width > 0 && height > 0) {
                targetWidth = width;
                targetHeight = height;
            }

            if (pixelRatio > 0.f) {
                targetWidth = (int) (targetWidth * pixelRatio);
                targetHeight = (int) (targetHeight * pixelRatio);
            }

            if (!forceUpscale) {
                if (targetWidth > srcWidth) {
                    targetWidth = srcWidth;
                }
                if (targetHeight > srcHeight) {
                    targetHeight = srcHeight;
                }
            }

            return new Dimension(targetWidth, targetHeight);
        }        
        return null;
    }
    
    /**
     * Returns the given string as integer or if the string is not a valid
     * integer returns the given default value.
     *
     * @param s The string to parse.
     * @param defaultValue The default fallback value in cases of an error.
     *
     * @return The parsed string as integer or the default fallback value in any
     * case of an error.
     */
    protected static Integer tryParseInt(String s, Integer defaultValue) {
        if (s != null && !s.isEmpty()) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Returns the given string as float or if the string is not a valid integer
     * returns the given default value.
     *
     * @param s The string to parse.
     * @param defaultValue The default fallback value in cases of an error.
     *
     * @return The parsed string as float or the default fallback value in any
     * case of an error.
     */
    protected static Float tryParseFloat(String s, Float defaultValue) {
        if (s != null && !s.isEmpty()) {
            try {
                return Float.parseFloat(s);
            } catch (NumberFormatException ex) {
                return defaultValue;
            }
        }
        return defaultValue;
    }   

    /**
     * Parses the specified color value.
     * 
     * @param s RGB or ARGB color value to parse.
     * @param defaultValue A default value.
     * 
     * @return The parsed string as {@link Color} value or the default fallback
     * value in any case of an error.
     * 
     * @since 1.2
     */
    protected static Color tryParseColor(String s, Color defaultValue) {
        if (s != null && !s.isEmpty()) {
            
            String ts = s.startsWith("#") ? s.substring(1) : s;                        
            if (ts.length() == 3 || ts.length() == 4) {
                String ns = "";
                for (char c : ts.toCharArray()) {
                    ns += Character.toString(c) + Character.toString(c);
                }
                ts = ns;
            }
            
            if (ts.length() == 6) {
                return Color.decode("#" + ts);
            } else if (ts.length() == 8) {
                try {
                    Color c = Color.decode("#" + ts.substring(2));
                    int a = Integer.parseInt(ts.substring(0, 2), 16);
                    return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }            
        }
        return defaultValue;
    }
    
    static Class<?> classForName(ServletContext context, String className) {
        Class<?> c = null;
        try {
            c = Class.forName(className, false, context != null
                    ? context.getClassLoader() : Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException ex) {
            // do nothing!
        }
        return c;
    }

}
