/**
 * Copyright 2015 Steffen Kremp
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

import static io.pictura.servlet.PicturaImageIO.IIO_REGISTRY;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry.Filter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Extends the {@link RequestProcessor} with basic ImageIO support depending
 * on the object that contains the request the client has made of the servlet.
 *
 * @see RequestProcessor
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public abstract class IIORequestProcessor extends RequestProcessor {    
    
    private static final Log LOG = Log.getLog(IIORequestProcessor.class);
    
    /**
     * Creates a new basic Image I/O request processor.
     */
    public IIORequestProcessor() {
    }

    /**
     * Returns a fallback image format name if the specified format name is not
     * supported as output image format. It will returns the given format name
     * if there is no fallback format necessary.
     *
     * <p>
     * If the result is not <code>null</code>, the support of the returned
     * output format is guaranteed.
     *
     * @param formatName The output format name to test.
     *
     * @return A supported fallback format name or <code>null</code> if no
     * fallback image format is available.
     */
    @SuppressWarnings("fallthrough")
    public String getFallbackFormatName(String formatName) {
	if (!canWriteFormat(formatName)) {
	    String selected;
	    switch (formatName) {
		case "svg":
		case "icns":
		case "ico":
		case "gif":
		case "pcx":
		case "wmf":
		    selected = "png";
		    break;

		case "wbmp":
		    selected = "gif";
		    break;

		case "jp2":
		case "jpeg2000":
		case "j2k":
		case "webp":
		case "psd":
		case "tif":
		case "tiff":
		case "hdr":
		case "raw":
		    selected = "jpg";
		    break;

		case "jpeg":
		    if (canWriteFormat("jpg")) {
			return "jpg";
		    }

		case "jpg":
		    if (canWriteFormat("jpeg")) {
			return "jpeg";
		    }

		default:
		    selected = "png";
	    }
	    return canWriteFormat(selected) ? selected : null;
	}
	return formatName;
    }

    /**
     * Tests whether this image processor is able to read images which are
     * encoded in the specified format.
     *
     * @param format The image format to test.
     * @return <code>true</code> if this image processor is able to read images
     * in the given format; otherwise <code>false</code>.
     *
     * @see #canReadMimeType(java.lang.String)
     */
    protected final boolean canReadFormat(String format) {
	if (format == null || format.isEmpty()) {
	    return false;
	}
	final boolean tmp = PicturaImageIO.getImageReaderFormats().containsKey(format);
	if (tmp) {
	    HttpServletRequest req = getRequest();
	    if (req != null) {
		if (req.getAttribute("io.pictura.servlet.ENABLED_INPUT_IMAGE_FORMATS") instanceof String[]) {
		    String[] enabledFormats = (String[]) req.getAttribute("io.pictura.servlet.ENABLED_INPUT_IMAGE_FORMATS");
		    if (enabledFormats.length > 0) {
			for (String fmt : enabledFormats) {
			    if (fmt != null && fmt.equals(format)) {
				return true;
			    }
			}
			return false;
		    }
		}
	    }
	}
	return tmp;
    }

    /**
     * Tests whether this image processor is able to read resources from the
     * specified mime type.
     *
     * @param mimeType The mime type to test.
     * @return <code>true</code> if this image processor is able to read
     * resources from the given mime type; otherwise <code>false</code>.
     *
     * @see #canReadFormat(java.lang.String)
     */
    protected final boolean canReadMimeType(String mimeType) {
	if (mimeType == null || mimeType.isEmpty()) {
	    return false;
	}

	String formatName = PicturaImageIO.getImageReaderWriterMIMETypes().get(mimeType);
	if (formatName != null) {
	    return PicturaImageIO.getImageReaderFormats().get(formatName) != null;
	}
	return false;
    }

    /**
     * Tests whether this image processor is able to write images in the
     * specified format.
     *
     * @param format The image format to test.
     * @return <code>true</code> if this image processor is able to write images
     * in the given format; otherwise <code>false</code>.
     *
     * @see #canWriteMimeType(java.lang.String)
     */
    protected final boolean canWriteFormat(String format) {
	if (format == null || format.isEmpty()) {
	    return false;
	}
	final boolean tmp = PicturaImageIO.getImageWriterFormats().containsKey(format);
	if (tmp) {
	    HttpServletRequest req = getRequest();
	    if (req != null) {
		if (req.getAttribute("io.pictura.servlet.ENABLED_OUTPUT_IMAGE_FORMATS") instanceof String[]) {
		    String[] enabledFormats = (String[]) req.getAttribute("io.pictura.servlet.ENABLED_OUTPUT_IMAGE_FORMATS");
		    if (enabledFormats.length > 0) {
			for (String fmt : enabledFormats) {
			    if (fmt != null && fmt.equals(format)) {
				return true;
			    }
			}
			return false;
		    }
		}
	    }
	}
	return tmp;
    }

    /**
     * Tests whether this image processor is able to write resources with the
     * specified mime type.
     *
     * @param mimeType The mime type to test.
     * @return <code>true</code> if this image processor is able to write the
     * specified image format by the given mime type; otherwise
     * <code>false</code>.
     *
     * @see #canWriteFormat(java.lang.String)
     */
    protected final boolean canWriteMimeType(String mimeType) {
	if (mimeType == null || mimeType.isEmpty()) {
	    return false;
	}
	Iterator<String> iterFormats = PicturaImageIO.getImageWriterFormats().keySet().iterator();
	while (iterFormats.hasNext()) {
	    if (mimeType.equalsIgnoreCase(PicturaImageIO.getImageWriterFormats().get(iterFormats.next()))) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Creates a new image input stream for the given source.
     *
     * @param is The source image input.
     * @return An image input stream instance.
     *
     * @throws IOException If a cache file is needed but cannot be created.
     */
    protected ImageInputStream createImageInputStream(InputStream is) throws IOException {
	// ImageIO Caching
	final boolean useCache = getRequest().getAttribute("io.pictura.servlet.IMAGEIO_USE_CACHE") != null
		? (Boolean) getRequest().getAttribute("io.pictura.servlet.IMAGEIO_USE_CACHE") : ImageIO.getUseCache();
	final File cacheDirectory = getRequest().getAttribute("io.pictura.servlet.IMAGEIO_CACHE_DIR") != null
		? (File) getRequest().getAttribute("io.pictura.servlet.IMAGEIO_CACHE_DIR") : ImageIO.getCacheDirectory();

	return PicturaImageIO.createImageInputStream(is, getIIOFilter(getRequest()), useCache, cacheDirectory);
    }

    /**
     * Creates a new image reader instance for the specified source.
     *
     * @param iis The image input stream for which the reader should be created.
     * @return An image reader instance.
     *
     * @throws IOException If an error occurs during loading, or initialization
     * of the reader class, or during instantiation or initialization of the
     * reader object.
     */
    protected ImageReader createImageReader(ImageInputStream iis)
	    throws IOException {

	if (iis == null) {
	    throw new IllegalArgumentException("The image input stream cannot be null.");
	}

	Iterator<ImageReaderSpi> iter = null;
	// Ensure category is present
	try {
	    iter = IIO_REGISTRY.getServiceProviders(ImageReaderSpi.class, true);
	} catch (IllegalArgumentException e) {
	    // nothing to do here
	}

	if (iter != null) {
	    while (iter.hasNext()) {
		ImageReaderSpi spi = iter.next();
		if (spi.canDecodeInput(iis)) {
		    if (canReadFormat(spi.getFormatNames()[0])
			    || canReadMimeType(spi.getMIMETypes()[0])) {			
			return spi.createReaderInstance();
		    }
		}
	    }
	}

	return null;
    }

    /**
     * Creates a new image output stream for the given source.
     *
     * @param os The output stream to which the image should be written.
     * @return An image output stream instance.
     *
     * @throws IOException If a cache file is needed but cannot be created.
     */
    protected ImageOutputStream createImageOutputStream(OutputStream os) throws IOException {
	// ImageIO Caching
	final boolean useCache = getRequest().getAttribute("io.pictura.servlet.IMAGEIO_USE_CACHE") != null
		? (Boolean) getRequest().getAttribute("io.pictura.servlet.IMAGEIO_USE_CACHE") : ImageIO.getUseCache();
	final File cacheDirectory = getRequest().getAttribute("io.pictura.servlet.IMAGEIO_CACHE_DIR") != null
		? (File) getRequest().getAttribute("io.pictura.servlet.IMAGEIO_CACHE_DIR") : ImageIO.getCacheDirectory();

	return PicturaImageIO.createImageOutputStream(os, getIIOFilter(getRequest()), useCache, cacheDirectory);
    }

    /**
     * Creates a new image writer instance for the given image and image format.
     *
     * @param img The image to be write.
     * @param format The image format name.
     *
     * @return An image writer instance.
     *
     * @throws IOException If an error occurs during loading, or initialization
     * of the writer class, or during instantiation or initialization of the
     * writer object.
     */
    protected ImageWriter createImageWriter(BufferedImage img, String format)
	    throws IOException {

	if (format == null) {
	    throw new IllegalArgumentException("The image format cannot be null.");
	}

	Iterator<ImageWriterSpi> iter;
	// Ensure category is present
	try {
	    iter = IIO_REGISTRY.getServiceProviders(ImageWriterSpi.class, true);
	} catch (IllegalArgumentException e) {
	    return null;
	}

	ImageWriterSpi spi = null;
	while (iter.hasNext()) {
	    spi = iter.next();
	    if ((Arrays.asList(spi.getFormatNames()).contains(format)
		    || Arrays.asList(spi.getFileSuffixes()).contains(format))
		    && spi.canEncodeImage(img)) {
		break;
	    } else {
		spi = null;
	    }
	}

	if (spi != null) {
	    return spi.createWriterInstance();
	}

	return null;
    }

    private ImageWriteParam createImageWriteParam(final ImageWriter writer,
	    final float compressionQuality, final boolean progressive) {

	final ImageWriteParam iwp = writer.getDefaultWriteParam();

	// Set compression mode and quality
	if (iwp.canWriteCompressed()) {
	    try {
		if (iwp instanceof JPEGImageWriteParam) {
		    jpegImageWriteParam(iwp, compressionQuality);
		} else {
		    iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

		    boolean sct = true;
		    String iwClazzName = writer.getClass().getName();

		    switch (iwClazzName) {
			case "com.sun.imageio.plugins.gif.GIFImageWriter":
			    iwp.setCompressionType("LZW");
			    break;

			case "com.luciad.imageio.webp.WebPWriter":
			    webpImageWriteParam(iwp, compressionQuality);
			    break;

			case "com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriter":
			case "com.github.jaiimageio.jpeg2000.impl.J2KImageWriter":
			    jp2ImageWriteParam(iwp, compressionQuality);
			    break;

			case "com.sun.imageio.plugins.bmp.BMPImageWriter":
			default:
			    sct = false;
			    break;
		    }

		    if (sct) {
			iwp.setCompressionQuality(compressionQuality);
		    } else {
			iwp.setCompressionMode(ImageWriteParam.MODE_DEFAULT);
		    }

		}
	    } catch (IllegalStateException | UnsupportedOperationException ex) {
		throw new IllegalArgumentException(ex);
	    }
	}

	// Set progressive or baseline
	if (iwp.canWriteProgressive()) {
	    iwp.setProgressiveMode(progressive ? ImageWriteParam.MODE_DEFAULT
		    : ImageWriteParam.MODE_DISABLED);
	}

	return iwp;
    }

    private void jpegImageWriteParam(final ImageWriteParam iwp,
	    final float compressionQuality) {

	((JPEGImageWriteParam) iwp).setOptimizeHuffmanTables(true);
	iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	iwp.setCompressionQuality(compressionQuality);
	if (compressionQuality >= 0.999f) {
	    iwp.setSourceSubsampling(1, 1, 0, 0);
	}
	iwp.setCompressionQuality(compressionQuality);
    }

    private void jp2ImageWriteParam(final ImageWriteParam iwp,
	    final float compressionQuality) {

	/**
	 * Use reflections to setup the write parameters because the origin Sun
	 * J2K implementation doesn't work with the interface method
	 * setCompressionQuality. We have to set 3 different fields in cases of
	 * lossy compression.
	 */
	if (compressionQuality < 0.999f) {
	    AccessController.doPrivileged(new PrivilegedAction<Object>() {

		@Override
		public Object run() {
		    try {
			iwp.setCompressionType("JPEG2000");

			Field lossless = iwp.getClass().getDeclaredField("lossless");
			lossless.setAccessible(true);
			lossless.setBoolean(iwp, false);

			Field filter = iwp.getClass().getDeclaredField("filter");
			filter.setAccessible(true);
			filter.set(iwp, "w9x7");

			Field encodingRate = iwp.getClass().getDeclaredField("encodingRate");
			encodingRate.setAccessible(true);
			encodingRate.setDouble(iwp, 0.1d + compressionQuality);
		    } catch (NoSuchFieldException | SecurityException |
			    IllegalArgumentException | IllegalAccessException ex) {

			throw new RuntimeException(ex);
		    }
		    return null;
		}
	    });
	}
    }

    private void webpImageWriteParam(final ImageWriteParam iwp,
	    final float compressionQuality) {

	if (compressionQuality >= 0.999f) {
	    iwp.setCompressionType("Lossless");
	} else {
	    iwp.setCompressionType("Lossy");
	}
    }

    /**
     * Writes the single frame image with the specified write params to the the
     * output stream of the servlet response.
     *
     * @param img Image to write out.
     * @param param Write parameters.
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the servlet.
     * @param resp An {@link HttpServletResponse} object that contains the
     * response the servlet sends to the client
     *
     * @throws ServletException if an input or output error is detected when the
     * servlet handles the request.
     * @throws IOException if the request for could not be handled.
     *
     * @see #doWriteImage(java.awt.image.BufferedImage[],
     * io.pictura.servlet.IIORequestProcessor.IIOWriteParam,
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     *
     * @see BufferedImage
     * @see IIOWriteParam
     */
    protected void doWriteImage(BufferedImage img, IIOWriteParam param,
	    HttpServletRequest req, HttpServletResponse resp) throws
	    ServletException, IOException {

	doWriteImage(new BufferedImage[]{img}, param, req, resp);
    }

    /**
     * Writes the image sequence with the specified write params to the output
     * stream of the servlet response.
     * <p>
     * If the image writer does not supports to write sequences, only the first
     * frame of the given sequence will be written to the output.</p>
     * <p>
     * If there is an {@link ImageInterceptor} is defined in the request
     * scope (attribute <code>io.pictura.servlet.IIO_IMAGE_INTERCEPTOR</code>),
     * the interceptor is called for each image frame, before the result will be
     * written to the response output stream.</p>
     *
     * @param img Image to write out.
     * @param param Write parameters.
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the servlet.
     * @param resp An {@link HttpServletResponse} object that contains the
     * response the servlet sends to the client
     *
     * @throws ServletException if an input or output error is detected when the
     * servlet handles the request.
     * @throws IOException if the request for could not be handled.
     *
     * @see #doWriteImage(java.awt.image.BufferedImage,
     * io.pictura.servlet.IIORequestProcessor.IIOWriteParam,
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     *
     * @see BufferedImage
     * @see IIOWriteParam
     */
    protected void doWriteImage(BufferedImage[] img, IIOWriteParam param,
	    HttpServletRequest req, HttpServletResponse resp) throws
	    ServletException, IOException {

	doIntercept(img, getImageInterceptor());
        
        // TODO: Replace with a fast byte array output stream which is also
        // not synchronized. This should also provide a direct access to the
        // byte array without copy them.
	ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 256);

	ImageWriter iw = null;
	ImageOutputStream ios = null;

	try {
            long startEncodeImage = -1L;
            if (LOG.isTraceEnabled()) {
                startEncodeImage = System.currentTimeMillis();
            }
            
	    if ("ico".equals(param.formatName)) {
		bos.write(new PicturaImageIO.ICOEncoder().encode(img[0]));
	    } else {
		iw = createImageWriter(img[0], param.formatName);
		iw.setOutput(ios = createImageOutputStream(bos));

		// At first, let us check if we can write the sequence with
		// this image writer. If not, we will continue as usual.
		if (img.length > 1 && "gif".equals(param.formatName)
			&& iw.canWriteSequence()) {

		    GIFSequenceWriter writer = new GIFSequenceWriter(iw, img[0].getType(),
			    param.animationDelayTime, true);

		    // Write frame 1 till n
		    for (BufferedImage frame : img) {
			writer.writeToSequence(frame);
			frame.flush();
		    }
		    writer.close();
		} else {
		    ImageWriteParam writeParam = createImageWriteParam(
			    iw, param.compressionQuality, param.progressive);

		    IIOMetadata metadata = null;

		    if (param.appendMetadata) {
			ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier
				.createFromBufferedImageType(img[0].getType());
			metadata = iw.getDefaultImageMetadata(typeSpecifier, writeParam);
		    }

		    iw.write(null, PicturaImageIO.optimizedIIOImage(
			    img[0], param.formatName, metadata), writeParam);

		    img[0].flush();
		}
	    }
            
            if (LOG.isTraceEnabled()) {
                LOG.trace("Image encoded in " + (System.currentTimeMillis() - startEncodeImage) + "ms");
            }
	} finally {
	    if (iw != null) {
		iw.dispose();
	    }
	    if (ios != null) {
		try {
		    ios.flush();
		} catch (IOException ex) {
		}
	    }
	}
        
	doWriteImage0(bos.toByteArray(), param, req, resp);
    }

    private void doWriteImage0(byte[] data, IIOWriteParam param,
	    HttpServletRequest req, HttpServletResponse resp) throws
	    ServletException, IOException {

	if (param.isBase64Encoded()) {
	    // The client has asked us to encode the image response as
	    // base64. For this, we will use our own embedded Base64
	    // encoder and return the "image" as plain text.
            long startBase64Encoding = -1L;
            if (LOG.isTraceEnabled()) {
                startBase64Encoding = System.currentTimeMillis();
            }
            
	    data = PicturaImageIO.createBase64EncodedImage(data,
		    PicturaImageIO.getImageWriterFormats().get(param.formatName), "UTF-8");

            if (LOG.isTraceEnabled()) {
                LOG.trace("Binary image to base64 converted in " + (System.currentTimeMillis() - startBase64Encoding) + "ms");
            }
            
	    resp.setContentType("text/plain");
	    resp.setCharacterEncoding("utf-8");
	} else {
	    resp.setContentType(PicturaImageIO.getImageWriterFormats().get(param.formatName));
	}

	req.setAttribute("io.pictura.servlet.DST_IMAGE_SIZE", (long) data.length);
	doWrite(data, req, resp);
    }

    /**
     * @return <code>true</code> if a image interceptor is defined for this
     * request processor; otherwise <code>false</code>.
     */
    protected final boolean hasImageInterceptor() {
	return (getRequest() != null
		&& getAttribute("io.pictura.servlet.IIO_IMAGE_INTERCEPTOR") instanceof ImageInterceptor);
    }

    /**
     * @return The defined image interceptor for this request processor or
     * <code>null</code> if there was no interceptor set.
     */
    protected ImageInterceptor getImageInterceptor() {
	if (hasImageInterceptor()) {
	    return (ImageInterceptor) getAttribute("io.pictura.servlet.IIO_IMAGE_INTERCEPTOR");
	}
	return null;
    }

    /**
     * Sets the specified (optional) image interceptor for this request
     * processor instance.
     *
     * @param interceptor The image interceptor for this instance.
     */
    public void setImageInterceptor(ImageInterceptor interceptor) {
	setAttribute("io.pictura.servlet.IIO_IMAGE_INTERCEPTOR", interceptor);
    }

    /**
     * @return <code>true</code> if a params interceptor is defined for this
     * request processor; otherwise <code>false</code>.
     */
    protected final boolean hasParamsInterceptor() {
	return (getRequest() != null
		&& getAttribute("io.pictura.servlet.IIO_PARAMS_INTERCEPTOR") instanceof ParamsInterceptor);
    }
    
    /**
     * @return The defined params interceptor for this request processor or
     * <code>null</code> if there was no interceptor set.
     */
    protected ParamsInterceptor getParamsInterceptor() {
	if (hasParamsInterceptor()) {
	    return (ParamsInterceptor) getAttribute("io.pictura.servlet.IIO_PARAMS_INTERCEPTOR");
	}
	return null;
    }
    
    /**
     * Sets the specified (optional) params interceptor for this request
     * processor instance.
     *
     * @param interceptor The params interceptor for this instance.
     */
    public void setParamsInterceptor(ParamsInterceptor interceptor) {
	setAttribute("io.pictura.servlet.IIO_PARAMS_INTERCEPTOR", interceptor);
    }
    
    private void doIntercept(BufferedImage[] img, ImageInterceptor interceptor) {	
	if (img != null && img.length > 0 && interceptor != null) {
	    for (int i = 0; i < img.length; i++) {
		final BufferedImage img0 = interceptor.intercept(img[i], getRequest());
		if (img0 != null) {
		    img[i] = img0;
		}
	    }
	}
    }

    @Override
    public String getTrueCacheKey() {
	String trueCacheKey = super.getTrueCacheKey();
	if (hasImageInterceptor()) {
	    return getImageInterceptor().getVaryCacheKey(trueCacheKey, getRequest());
	}
	return trueCacheKey;
    }

    private Filter getIIOFilter(HttpServletRequest req) {
	String[] include = null, exclude = null;
	Object oIncludeFilter = req.getAttribute("io.pictura.servlet.IMAGEIO_SPI_FILTER_INCLUDE");
	if (oIncludeFilter instanceof String[]) {
	    include = (String[]) oIncludeFilter;
	}
	Object oExcludeFilter = req.getAttribute("io.pictura.servlet.IMAGEIO_SPI_FILTER_EXCLUDE");
	if (oExcludeFilter instanceof String[]) {
	    exclude = (String[]) oExcludeFilter;
	}
	return (include == null && exclude == null) ? null
		: PicturaImageIO.createServiceProviderFilter(include, exclude);
    }

    /**
     * A class describing how a image to be encoded.
     */
    public static final class IIOWriteParam {

	private String formatName;

	private float compressionQuality;

	private boolean progressive;
	private boolean appendMetadata;
	private boolean base64Encoded;

	private int animationDelayTime;

	/**
	 * Constructs an empty <code>IIOWriteParam</code>.
	 */
	public IIOWriteParam() {

	}

	/**
	 * Returns the current format name setting.
	 *
	 * @return The format name.
	 */
	public String getFormatName() {
	    return formatName;
	}

	/**
	 * Returns the current format name setting.
	 *
	 * @param formatName The format name.
	 */
	public void setFormatName(String formatName) {
	    this.formatName = formatName;
	}

	/**
	 * Returns the current compression quality setting.
	 *
	 * @return A <code>float</code> between <code>0</code> and
	 * <code>1</code> indicating the desired quality level.
	 */
	public float getCompressionQuality() {
	    return compressionQuality;
	}

	/**
	 * Sets the compression quality to a value between <code>0</code> and
	 * <code>1</code>.
	 *
	 * @param compressionQuality A <code>float</code> between <code>0</code>
	 * and <code>1</code> indicating the desired quality level.
	 */
	public void setCompressionQuality(float compressionQuality) {
	    this.compressionQuality = compressionQuality;
	}

	/**
	 * Returns the current mode for writing the stream in a progressive
	 * manner.
	 *
	 * @return The current mode for progressive encoding.
	 */
	public boolean isProgressive() {
	    return progressive;
	}

	/**
	 * Sets the current mode for writing the stream in a progressive manner.
	 *
	 * @param progressive The current mode for progressive encoding.
	 */
	public void setProgressive(boolean progressive) {
	    this.progressive = progressive;
	}

	/**
	 * Returns the current state for writing basic metadata.
	 *
	 * @return The current state for writing basic metadata.
	 */
	public boolean isAppendMetadata() {
	    return appendMetadata;
	}

	/**
	 * Sets the current state for writing basic metadata.
	 *
	 * @param appendMetadata The current state for writing basic metadata.
	 */
	public void setAppendMetadata(boolean appendMetadata) {
	    this.appendMetadata = appendMetadata;
	}

	/**
	 * Returns the current mode for writing the stream Base 64 encoded.
	 *
	 * @return The current mode for writing the stream Base 64 encoded.
	 */
	public boolean isBase64Encoded() {
	    return base64Encoded;
	}

	/**
	 * Sets the current mode for writing the stream Base 64 encoded.
	 *
	 * @param base64Encoded The current mode for writing the stream Base 64
	 * encoded.
	 */
	public void setBase64Encoded(boolean base64Encoded) {
	    this.base64Encoded = base64Encoded;
	}

	/**
	 * Returns the current delay time for animated sequences between two
	 * image frames.
	 *
	 * @return The current delay time for animated sequences between two
	 * image frames.
	 */
	public int getAnimationDelayTime() {
	    return animationDelayTime;
	}

	/**
	 * Sets the current delay time for animated sequences between two image
	 * frames.
	 *
	 * @param animationDelayTime The current delay time for animated
	 * sequences between two image frames.
	 */
	public void setAnimationDelayTime(int animationDelayTime) {
	    this.animationDelayTime = animationDelayTime;
	}

    }

}
