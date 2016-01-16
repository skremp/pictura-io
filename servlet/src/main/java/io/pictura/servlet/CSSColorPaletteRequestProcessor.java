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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>CSSColorPaletteRequestProcessor</code> is used to extract 1-32
 * dominant colors from the specified image (after the optional image operations
 * are done). The result is given as <code>text/css</code>.
 * <p>
 * An example response with <code>CC=3</code> (color count request parameter)
 * looks like:
 *
 * <pre>
 * .fg-0{color:#805444;}
 * .bg-0{background-color:#805444;}
 * .fg-1{color:#8c4c38;}
 * .bg-1{background-color:#8c4c38;}
 * .fg-2{color:#944c3c;}
 * .bg-2{background-color:#944c3c;}
 * </pre>
 *
 * @see ImageRequestProcessor
 * @see ImageRequestStrategy
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public class CSSColorPaletteRequestProcessor extends StrategyRequestProcessor {

    /**
     * Parameter name for the requested number of colors to extract from the
     * image.
     */
    protected static final String QPARAM_NAME_COLOR_COUNT = "cc";

    /**
     * Parameter to indicate whether white color shall be ignored.
     */
    protected static final String QPARAM_NAME_IGNORE_WHITE = "iw";

    /**
     * Parameter to define a custom CSS class name prefix.
     */
    protected static final String QPARAM_NAME_PREFIX = "pf";

    /**
     * Parameter to decide whether or not linear gradient background gradient
     * styles shall be generated.
     */
    protected static final String QPARAM_NAME_LINEAR_GRADIENT = "lg";

    @Override
    protected boolean isProxyRequest(HttpServletRequest req) {
	return false;
    }

    /**
     * In this case returns always <code>null</code> because the implemantion is
     * not a real image processor. This processor will always returns content
     * from type <code>text/css</code>, therefore is is not possible to choose
     * between different format types.
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the processor.
     *
     * @return Returns always <code>null</code>.
     */
    @Override
    protected final String getRequestedFormatName(HttpServletRequest req) {
	return null; // use the original file type
    }

    /**
     * Format encoding is not supported by this type of
     * {@link ImageRequestProcessor}. If requested, this method will throws an
     * {@link IllegalArgumentException}.
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the processor.
     *
     * @return <code>null</code> if the parameter was not specified or throws an
     * {@link IllegalArgumentException} if it was specified.
     */
    @Override
    protected final String getRequestedFormatEncoding(HttpServletRequest req) {
	String s = super.getRequestedFormatEncoding(req);
	if (s != null) {
	    throw new IllegalArgumentException("Illegal format encoding: \"" + s
		    + "\" is not supported for the requested format");
	}
	return null;
    }

    /**
     * Format options are not supported by this type of
     * {@link ImageRequestProcessor}. If requested, this method will throws an
     * {@link IllegalArgumentException}.
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the processor.
     *
     * @return <code>null</code> if the parameter was not specified or throws an
     * {@link IllegalArgumentException} if it was specified.
     */
    @Override
    protected final String getRequestedFormatOption(HttpServletRequest req) {
	String s = super.getRequestedFormatOption(req);
	if (s != null) {
	    throw new IllegalArgumentException("Illegal format option: \"" + s
		    + "\" is not supported for the requested format");
	}
	return null;
    }

    /**
     * Upscaling isn't supported by this type of {@link ImageRequestProcessor}.
     * If requested, this method will throws an
     * {@link IllegalArgumentException}.
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the processor.
     *
     * @return <code>null</code> if the parameter was not specified or throws an
     * {@link IllegalArgumentException} if it was specified.
     */
    @Override
    protected final Boolean getRequestedScaleForceUpscale(HttpServletRequest req) {
	boolean b = super.getRequestedScaleForceUpscale(req);
	if (b) {
	    throw new IllegalArgumentException("Illegal scale: force upscale is "
		    + "not supportedf or the requested format");
	}
	return false;
    }

    /**
     * Returns the requested number of colors to extract. Valid values are
     * 1..32. If nothing is defined, the default value is returned (8).
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the processor.
     *
     * @return The number of colors to extract.
     */
    protected Integer getRequestedColorCount(HttpServletRequest req) {
	int cc = tryParseInt(req != null ? getRequestParameter(req,
		QPARAM_NAME_COLOR_COUNT) : null, 8);

	if (cc < 2 || cc > 32) {
	    throw new IllegalArgumentException("Invalid palette color count: \""
		    + cc + "\" (range must be between 2 and 32");
	}

	return cc;
    }

    /**
     * Returns <code>true</code> if white color shall be ignored while the color
     * palette will be extract from the image. If the parameter is not set this
     * method returns <code>true</code> as default.
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the processor.
     *
     * @return <code>true</code> if white is ignored.
     */
    protected Boolean getRequestedIgnoreWhite(HttpServletRequest req) {
	if (getRequestParameter(req, QPARAM_NAME_IGNORE_WHITE) == null) {
	    return true;
	}
	return "1".equalsIgnoreCase(getRequestParameter(req,
		QPARAM_NAME_IGNORE_WHITE));
    }

    /**
     * Returns <code>true</code> if the generated CSS shall be contains linear
     * gardient background styles (CSS3).
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the processor.
     *
     * @return <code>true</code> if the generated CSS shall be contains linear
     * gradient background styles (CSS3).
     */
    protected Boolean getRequestedLinearGradient(HttpServletRequest req) {
	if (getRequestParameter(req, QPARAM_NAME_LINEAR_GRADIENT) == null) {
	    return false;
	}
	return "1".equalsIgnoreCase(getRequestParameter(req,
		QPARAM_NAME_LINEAR_GRADIENT));
    }

    private static final Pattern P_PREFIX = Pattern.compile("^[a-zA-Z0-9]{1,128}$");

    /**
     * A string prefix to use for the CSS class names in the generated palette
     * CSS.
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the processor.
     *
     * @return The CSS class name prefix or <code>null</code> if no prefix is
     * specified.
     */
    protected String getRequestedPrefix(HttpServletRequest req) {
	String pf = getRequestParameter(req, QPARAM_NAME_PREFIX);
	if (pf != null && !pf.isEmpty()) {

	    if (!P_PREFIX.matcher(pf).matches()) {
		throw new IllegalArgumentException("Invalid prefix: \"" + pf + "\"");
	    }
	    pf += "-";
	}
	return (pf != null && pf.isEmpty()) ? null : pf;
    }

    @Override
    protected void doWriteImage(BufferedImage[] img, IIOWriteParam param,
	    HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {

	if (img != null && img.length > 0) {
	    int quality = 10; // default

	    final Quality q = getRequestedQuality(req);
	    if (q != Quality.AUTO) {
		quality = q == Quality.ULTRA_HIGH ? 20 : q == Quality.HIGH ? 15
			: q == Quality.MEDIUM ? 10 : q == Quality.LOW ? 5 : 10;
	    }

	    // The generated css palette data
	    byte[] paletteData = buildCSSColorTable(ColorThief.getPalette(img[0],
		    getRequestedColorCount(req), quality, getRequestedIgnoreWhite(req)),
		    getRequestedPrefix(req), "UTF-8", getRequestedLinearGradient(req));

	    resp.setContentType("text/css");
	    resp.setCharacterEncoding("UTF-8");
	    resp.setContentLength(paletteData.length);

	    doWrite(paletteData, 0, paletteData.length, req, resp);
	} else {
	    doInterrupt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}
    }

    private byte[] buildCSSColorTable(int[][] palette, String prefix, String encoding,
	    boolean linearGradient) throws UnsupportedEncodingException {

	if (palette != null) {
	    String pf = prefix != null ? prefix : "";
	    StringBuilder css = new StringBuilder();

	    ArrayList<String> hexColors = new ArrayList<>();

	    for (int[] p : palette) {
		StringBuilder hexColor = new StringBuilder();
		for (int y = 0; y < p.length; y++) {
		    String s = Integer.toHexString(p[y]);
		    if (s.length() == 1) {
			s = "0" + s;
		    }
		    hexColor.append(s);
		}
		hexColors.add(hexColor.toString());
	    }

	    Collections.sort(hexColors, new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
		    return o1.compareTo(o2) * -1;
		}
	    });

	    int x = 0;

	    for (String hexColor : hexColors) {
		css.append(".").append(pf).append("fg-").append(x).append("{");
		css.append("color:#").append(hexColor).append(";");
		css.append("}\n");

		css.append(".").append(pf).append("bg-").append(x).append("{");
		css.append("background-color:#").append(hexColor).append(";");
		css.append("}\n");

		x++;
	    }

	    if (linearGradient && hexColors.size() > 1) {

		StringBuilder colors = new StringBuilder();
		for (String c : hexColors) {
		    colors.append(",#").append(c);
		}

		// TODO: Append depending on the requested browser engine
		// and handle automatically a content negotiation
		css.append(".").append(pf).append("bg-lg").append("{");
		css.append("background:linear-gradient(").append(colors.toString().substring(1)).append(");");
		css.append("background:-webkit-linear-gradient(").append(colors.toString().substring(1)).append(");");
		css.append("background:-moz-linear-gradient(").append(colors.toString().substring(1)).append(");");
		css.append("background:-o-linear-gradient(").append(colors.toString().substring(1)).append(");");
		css.append("}\n");

		css.append(".").append(pf).append("bg-lg0deg").append("{");
		css.append("background:linear-gradient(0deg").append(colors.toString()).append(");");
		css.append("background:-webkit-linear-gradient(0deg").append(colors.toString()).append(");");
		css.append("background:-moz-linear-gradient(0deg").append(colors.toString()).append(");");
		css.append("background:-o-linear-gradient(0deg").append(colors.toString()).append(");");
		css.append("}\n");

		css.append(".").append(pf).append("bg-lg45deg").append("{");
		css.append("background:linear-gradient(45deg").append(colors.toString()).append(");");
		css.append("background:-webkit-linear-gradient(45deg").append(colors.toString()).append(");");
		css.append("background:-moz-linear-gradient(45deg").append(colors.toString()).append(");");
		css.append("background:-o-linear-gradient(45deg").append(colors.toString()).append(");");
		css.append("}\n");

		css.append(".").append(pf).append("bg-lg90deg").append("{");
		css.append("background:linear-gradient(90deg").append(colors.toString()).append(");");
		css.append("background:-webkit-linear-gradient(90deg").append(colors.toString()).append(");");
		css.append("background:-moz-linear-gradient(90deg").append(colors.toString()).append(");");
		css.append("background:-o-linear-gradient(90deg").append(colors.toString()).append(");");
		css.append("}\n");

		css.append(".").append(pf).append("bg-lg135deg").append("{");
		css.append("background:linear-gradient(135deg").append(colors.toString()).append(");");
		css.append("background:-webkit-linear-gradient(135deg").append(colors.toString()).append(");");
		css.append("background:-moz-linear-gradient(135deg").append(colors.toString()).append(");");
		css.append("background:-o-linear-gradient(135deg").append(colors.toString()).append(");");
		css.append("}\n");

		css.append(".").append(pf).append("bg-lg180deg").append("{");
		css.append("background:linear-gradient(180deg").append(colors.toString()).append(");");
		css.append("background:-webkit-linear-gradient(180deg").append(colors.toString()).append(");");
		css.append("background:-moz-linear-gradient(180deg").append(colors.toString()).append(");");
		css.append("background:-o-linear-gradient(180deg").append(colors.toString()).append(");");
		css.append("}\n");

		css.append(".").append(pf).append("bg-lg225deg").append("{");
		css.append("background:linear-gradient(225deg").append(colors.toString()).append(");");
		css.append("background:-webkit-linear-gradient(225deg").append(colors.toString()).append(");");
		css.append("background:-moz-linear-gradient(225deg").append(colors.toString()).append(");");
		css.append("background:-o-linear-gradient(225deg").append(colors.toString()).append(");");
		css.append("}\n");

		css.append(".").append(pf).append("bg-lg270deg").append("{");
		css.append("background:linear-gradient(270deg").append(colors.toString()).append(");");
		css.append("background:-webkit-linear-gradient(270deg").append(colors.toString()).append(");");
		css.append("background:-moz-linear-gradient(270deg").append(colors.toString()).append(");");
		css.append("background:-o-linear-gradient(270deg").append(colors.toString()).append(");");
		css.append("}\n");

		css.append(".").append(pf).append("bg-lg315deg").append("{");
		css.append("background:linear-gradient(315deg").append(colors.toString()).append(");");
		css.append("background:-webkit-linear-gradient(315deg").append(colors.toString()).append(");");
		css.append("background:-moz-linear-gradient(315deg").append(colors.toString()).append(");");
		css.append("background:-o-linear-gradient(315deg").append(colors.toString()).append(");");
		css.append("}\n");
	    }

	    return css.toString().getBytes(encoding);
	}
	return new byte[0];
    }

    /**
     * Tests whether or not the given request is preferred to process by an
     * <code>CSSColorPaletteRequestProcessor</code>.
     * <p>
     * An request is preferred if the requested format is <code>pcss</code>
     * (Palette CSS). This means the user is asking an auto generated color
     * palette CSS document which contains a number of CSS classes with the
     * color information from the requested image.
     * </p>
     *
     * @param req An {@link HttpServletRequest} object to test.
     *
     * @return <code>true</code> if the request is preferred to process by an
     * <code>CSSColorPaletteRequestProcessor</code>.
     */
    @Override
    public boolean isPreferred(HttpServletRequest req) {
	return "pcss".equals(getBaseRequestProcessor(req)
		.getRequestParameter(req, QPARAM_NAME_FORMAT_NAME));
    }

    @Override
    public ImageRequestProcessor createRequestProcessor() {
	return new CSSColorPaletteRequestProcessor();
    }

    // ColorThief, MMCQ and CMap are derived from:
    // https://github.com/SvenWoltmann/color-thief-java/blob/master/src/main/java/de/androidpit/colorthief
    //
    // The original sources are distributed under the
    // Creative Commons Attribution 2.5 License: http://creativecommons.org/licenses/by/2.5/
    // -- Note: The Creative Commons himself says it is not useful to use any
    // of these licenses for "Software" because it is equals to distribute
    // the Software whithout any license. In other words, a Creative Commons
    // License will have no effects on Software works.
    private static final class ColorThief {

	/**
	 * Use the median cut algorithm to cluster similar colors.
	 *
	 * @param sourceImage the source image
	 * @param colorCount the size of the palette; the number of colors
	 * returned
	 * @param quality 0 is the highest quality settings. 10 is the default.
	 * There is a trade-off between quality and speed. The bigger the
	 * number, the faster the palette generation but the greater the
	 * likelihood that colors will be missed.
	 * @param ignoreWhite if <code>true</code>, white pixels are ignored
	 *
	 * @return the palette as array of RGB arrays
	 */
	private static int[][] getPalette(
		BufferedImage sourceImage,
		int colorCount,
		int quality,
		boolean ignoreWhite) {
	    CMap cmap = getColorMap(sourceImage, colorCount, quality, ignoreWhite);
	    if (cmap == null) {
		return null;
	    }
	    return cmap.palette();
	}

	/**
	 * Use the median cut algorithm to cluster similar colors.
	 *
	 * @param sourceImage the source image
	 * @param colorCount the size of the palette; the number of colors
	 * returned
	 * @param quality 0 is the highest quality settings. 10 is the default.
	 * There is a trade-off between quality and speed. The bigger the
	 * number, the faster the palette generation but the greater the
	 * likelihood that colors will be missed.
	 * @param ignoreWhite if <code>true</code>, white pixels are ignored
	 *
	 * @return the color map
	 */
	private static CMap getColorMap(
		BufferedImage sourceImage,
		int colorCount,
		int quality,
		boolean ignoreWhite) {
	    int[][] pixelArray;

	    switch (sourceImage.getType()) {
		case BufferedImage.TYPE_3BYTE_BGR:
		case BufferedImage.TYPE_4BYTE_ABGR:
		    pixelArray = getPixelsFast(sourceImage, quality, ignoreWhite);
		    break;

		default:
		    pixelArray = getPixelsSlow(sourceImage, quality, ignoreWhite);
	    }

	    // Send array to quantize function which clusters values using median
	    // cut algorithm
	    CMap cmap = MMCQ.quantize(pixelArray, colorCount);
	    return cmap;
	}

	/**
	 * Gets the image's pixels via
	 * BufferedImage.getRaster().getDataBuffer(). Fast, but doesn't work for
	 * all color models.
	 *
	 * @param sourceImage the source image
	 * @param quality 0 is the highest quality settings. 10 is the default.
	 * There is a trade-off between quality and speed. The bigger the
	 * number, the faster the palette generation but the greater the
	 * likelihood that colors will be missed.
	 * @param ignoreWhite if <code>true</code>, white pixels are ignored
	 *
	 * @return an array of pixels (each an RGB int array)
	 */
	private static int[][] getPixelsFast(
		BufferedImage sourceImage,
		int quality,
		boolean ignoreWhite) {
	    DataBufferByte imageData = (DataBufferByte) sourceImage
		    .getRaster()
		    .getDataBuffer();
	    byte[] pixels = imageData.getData();
	    int pixelCount = sourceImage.getWidth() * sourceImage.getHeight();

	    int colorDepth;
	    int type = sourceImage.getType();
	    switch (type) {
		case BufferedImage.TYPE_3BYTE_BGR:
		    colorDepth = 3;
		    break;

		case BufferedImage.TYPE_4BYTE_ABGR:
		    colorDepth = 4;
		    break;

		default:
		    throw new IllegalArgumentException("Unhandled type: " + type);
	    }

	    int expectedDataLength = pixelCount * colorDepth;
	    if (expectedDataLength != pixels.length) {
		throw new IllegalArgumentException("(expectedDataLength = "
			+ expectedDataLength + ") != (pixels.length = "
			+ pixels.length + ")");
	    }

	    // Store the RGB values in an array format suitable for quantize
	    // function
	    // numRegardedPixels must be rounded up to avoid an
	    // ArrayIndexOutOfBoundsException if all pixels are good.
	    int numRegardedPixels = (pixelCount + quality - 1) / quality;

	    int numUsedPixels = 0;
	    int[][] pixelArray = new int[numRegardedPixels][];
	    int offset, r, g, b, a;

	    // Do the switch outside of the loop, that's much faster
	    switch (type) {
		case BufferedImage.TYPE_3BYTE_BGR:
		    for (int i = 0; i < pixelCount; i += quality) {
			offset = i * 3;
			b = pixels[offset] & 0xFF;
			g = pixels[offset + 1] & 0xFF;
			r = pixels[offset + 2] & 0xFF;

			// If pixel is not white
			if (!(ignoreWhite && r > 250 && g > 250 && b > 250)) {
			    pixelArray[numUsedPixels] = new int[]{r, g, b};
			    numUsedPixels++;
			}
		    }
		    break;

		case BufferedImage.TYPE_4BYTE_ABGR:
		    for (int i = 0; i < pixelCount; i += quality) {
			offset = i * 4;
			a = pixels[offset] & 0xFF;
			b = pixels[offset + 1] & 0xFF;
			g = pixels[offset + 2] & 0xFF;
			r = pixels[offset + 3] & 0xFF;

			// If pixel is mostly opaque and not white
			if (a >= 125 && !(ignoreWhite && r > 250 && g > 250 && b > 250)) {
			    pixelArray[numUsedPixels] = new int[]{r, g, b};
			    numUsedPixels++;
			}
		    }
		    break;

		default:
		    throw new IllegalArgumentException("Unhandled type: " + type);
	    }

	    // Remove unused pixels from the array
	    return Arrays.copyOfRange(pixelArray, 0, numUsedPixels);
	}

	/**
	 * Gets the image's pixels via BufferedImage.getRGB(..). Slow, but the
	 * fast method doesn't work for all color models.
	 *
	 * @param sourceImage the source image
	 * @param quality 0 is the highest quality settings. 10 is the default.
	 * There is a trade-off between quality and speed. The bigger the
	 * number, the faster the palette generation but the greater the
	 * likelihood that colors will be missed.
	 * @param ignoreWhite if <code>true</code>, white pixels are ignored
	 *
	 * @return an array of pixels (each an RGB int array)
	 */
	private static int[][] getPixelsSlow(
		BufferedImage sourceImage,
		int quality,
		boolean ignoreWhite) {
	    int width = sourceImage.getWidth();
	    int height = sourceImage.getHeight();

	    int pixelCount = width * height;

	    // numRegardedPixels must be rounded up to avoid an
	    // ArrayIndexOutOfBoundsException if all pixels are good.
	    int numRegardedPixels = (pixelCount + quality - 1) / quality;

	    int numUsedPixels = 0;

	    int[][] res = new int[numRegardedPixels][];
	    int r, g, b;

	    for (int i = 0; i < pixelCount; i += quality) {
		int row = i / width;
		int col = i % width;
		int rgb = sourceImage.getRGB(col, row);

		r = (rgb >> 16) & 0xFF;
		g = (rgb >> 8) & 0xFF;
		b = (rgb) & 0xFF;
		if (!(ignoreWhite && r > 250 && g > 250 && b > 250)) {
		    res[numUsedPixels] = new int[]{r, g, b};
		    numUsedPixels++;
		}
	    }

	    return Arrays.copyOfRange(res, 0, numUsedPixels);
	}

    }

    private static final class MMCQ {

	private static final int SIGBITS = 5;
	private static final int RSHIFT = 8 - SIGBITS;
	private static final int MULT = 1 << RSHIFT;
	private static final int HISTOSIZE = 1 << (3 * SIGBITS);
	private static final int VBOX_LENGTH = 1 << SIGBITS;
	private static final int MAX_ITERATIONS = 1000;

	/**
	 * Get reduced-space color index for a pixel.
	 *
	 * @param r the red value
	 * @param g the green value
	 * @param b the blue value
	 *
	 * @return the color index
	 */
	private static int getColorIndex(int r, int g, int b) {
	    return (r << (2 * SIGBITS)) + (g << SIGBITS) + b;
	}

	/**
	 * 3D color space box.
	 */
	private static final class VBox implements Cloneable {

	    int r1;
	    int r2;
	    int g1;
	    int g2;
	    int b1;
	    int b2;

	    private final int[] histo;

	    private int[] avg;
	    private Integer volume;
	    private Integer count;

	    private VBox(int r1, int r2, int g1, int g2, int b1, int b2, int[] histo) {
		this.r1 = r1;
		this.r2 = r2;
		this.g1 = g1;
		this.g2 = g2;
		this.b1 = b1;
		this.b2 = b2;

		this.histo = histo;
	    }

	    private int volume(boolean force) {
		if (volume == null || force) {
		    volume = ((r2 - r1 + 1) * (g2 - g1 + 1) * (b2 - b1 + 1));
		}

		return volume;
	    }

	    private int count(boolean force) {
		if (count == null || force) {
		    int npix = 0;
		    int i, j, k, index;

		    for (i = r1; i <= r2; i++) {
			for (j = g1; j <= g2; j++) {
			    for (k = b1; k <= b2; k++) {
				index = getColorIndex(i, j, k);
				npix += histo[index];
			    }
			}
		    }

		    count = npix;
		}

		return count;
	    }

	    @Override
	    public VBox clone() {
		return new VBox(r1, r2, g1, g2, b1, b2, histo);
	    }

	    private int[] avg(boolean force) {
		if (avg == null || force) {
		    int ntot = 0;

		    int rsum = 0;
		    int gsum = 0;
		    int bsum = 0;

		    int hval, i, j, k, histoindex;

		    for (i = r1; i <= r2; i++) {
			for (j = g1; j <= g2; j++) {
			    for (k = b1; k <= b2; k++) {
				histoindex = getColorIndex(i, j, k);
				hval = histo[histoindex];
				ntot += hval;
				rsum += (hval * (i + 0.5) * MULT);
				gsum += (hval * (j + 0.5) * MULT);
				bsum += (hval * (k + 0.5) * MULT);
			    }
			}
		    }

		    if (ntot > 0) {
			avg = new int[]{~ ~(rsum / ntot), ~ ~(gsum / ntot),
			    ~ ~(bsum / ntot)};
		    } else {
			avg = new int[]{~ ~(MULT * (r1 + r2 + 1) / 2),
			    ~ ~(MULT * (g1 + g2 + 1) / 2),
			    ~ ~(MULT * (b1 + b2 + 1) / 2)};
		    }
		}

		return avg;
	    }

	}

	/**
	 * Histo (1-d array, giving the number of pixels in each quantized
	 * region of color space), or null on error.
	 */
	private static int[] getHisto(int[][] pixels) {
	    int[] histo = new int[HISTOSIZE];
	    int index, rval, gval, bval;

	    int numPixels = pixels.length;
	    for (int i = 0; i < numPixels; i++) {
		int[] pixel = pixels[i];
		rval = pixel[0] >> RSHIFT;
		gval = pixel[1] >> RSHIFT;
		bval = pixel[2] >> RSHIFT;
		index = getColorIndex(rval, gval, bval);
		histo[index]++;
	    }
	    return histo;
	}

	private static VBox vboxFromPixels(int[][] pixels, int[] histo) {
	    int rmin = 1000000, rmax = 0;
	    int gmin = 1000000, gmax = 0;
	    int bmin = 1000000, bmax = 0;

	    int rval, gval, bval;

	    // find min/max
	    int numPixels = pixels.length;
	    for (int i = 0; i < numPixels; i++) {
		int[] pixel = pixels[i];
		rval = pixel[0] >> RSHIFT;
		gval = pixel[1] >> RSHIFT;
		bval = pixel[2] >> RSHIFT;

		if (rval < rmin) {
		    rmin = rval;
		} else if (rval > rmax) {
		    rmax = rval;
		}

		if (gval < gmin) {
		    gmin = gval;
		} else if (gval > gmax) {
		    gmax = gval;
		}

		if (bval < bmin) {
		    bmin = bval;
		} else if (bval > bmax) {
		    bmax = bval;
		}
	    }

	    return new VBox(rmin, rmax, gmin, gmax, bmin, bmax, histo);
	}

	private static VBox[] medianCutApply(int[] histo, VBox vbox) {
	    if (vbox.count(false) == 0) {
		return new VBox[0];
	    }

	    // only one pixel, no split
	    if (vbox.count(false) == 1) {
		return new VBox[]{vbox.clone(), null};
	    }

	    int rw = vbox.r2 - vbox.r1 + 1;
	    int gw = vbox.g2 - vbox.g1 + 1;
	    int bw = vbox.b2 - vbox.b1 + 1;
	    int maxw = Math.max(Math.max(rw, gw), bw);

	    // Find the partial sum arrays along the selected axis.
	    int total = 0;
	    int[] partialsum = new int[VBOX_LENGTH];
	    Arrays.fill(partialsum, -1); // -1 = not set / 0 = 0
	    int[] lookaheadsum = new int[VBOX_LENGTH];
	    Arrays.fill(lookaheadsum, -1); // -1 = not set / 0 = 0
	    int i, j, k, sum, index;

	    if (maxw == rw) {
		for (i = vbox.r1; i <= vbox.r2; i++) {
		    sum = 0;
		    for (j = vbox.g1; j <= vbox.g2; j++) {
			for (k = vbox.b1; k <= vbox.b2; k++) {
			    index = getColorIndex(i, j, k);
			    sum += histo[index];
			}
		    }
		    total += sum;
		    partialsum[i] = total;
		}
	    } else if (maxw == gw) {
		for (i = vbox.g1; i <= vbox.g2; i++) {
		    sum = 0;
		    for (j = vbox.r1; j <= vbox.r2; j++) {
			for (k = vbox.b1; k <= vbox.b2; k++) {
			    index = getColorIndex(j, i, k);
			    sum += histo[index];
			}
		    }
		    total += sum;
		    partialsum[i] = total;
		}
	    } else /* maxw == bw */ {
		for (i = vbox.b1; i <= vbox.b2; i++) {
		    sum = 0;
		    for (j = vbox.r1; j <= vbox.r2; j++) {
			for (k = vbox.g1; k <= vbox.g2; k++) {
			    index = getColorIndex(j, k, i);
			    sum += histo[index];
			}
		    }
		    total += sum;
		    partialsum[i] = total;
		}
	    }

	    for (i = 0; i < VBOX_LENGTH; i++) {
		if (partialsum[i] != -1) {
		    lookaheadsum[i] = total - partialsum[i];
		}
	    }

	    // determine the cut planes
	    return maxw == rw ? doCut('r', vbox, partialsum, lookaheadsum, total)
		    : maxw == gw ? doCut('g', vbox, partialsum, lookaheadsum, total)
			    : doCut('b', vbox, partialsum, lookaheadsum, total);
	}

	private static VBox[] doCut(
		char color,
		VBox vbox,
		int[] partialsum,
		int[] lookaheadsum,
		int total) {
	    int vbox_dim1;
	    int vbox_dim2;

	    if (color == 'r') {
		vbox_dim1 = vbox.r1;
		vbox_dim2 = vbox.r2;
	    } else if (color == 'g') {
		vbox_dim1 = vbox.g1;
		vbox_dim2 = vbox.g2;
	    } else /* color == 'b' */ {
		vbox_dim1 = vbox.b1;
		vbox_dim2 = vbox.b2;
	    }

	    int left, right;
	    VBox vbox1, vbox2;
	    int d2, count2;

	    for (int i = vbox_dim1; i <= vbox_dim2; i++) {
		if (partialsum[i] > total / 2) {
		    vbox1 = vbox.clone();
		    vbox2 = vbox.clone();

		    left = i - vbox_dim1;
		    right = vbox_dim2 - i;

		    if (left <= right) {
			d2 = Math.min(vbox_dim2 - 1, ~ ~(i + right / 2));
		    } else {
			// 2.0 and cast to int is necessary to have the same
			// behaviour as in JavaScript
			d2 = Math.max(vbox_dim1, ~ ~((int) (i - 1 - left / 2.0)));
		    }

		    // avoid 0-count boxes
		    while (d2 < 0 || partialsum[d2] <= 0) {
			d2++;
		    }
		    count2 = lookaheadsum[d2];
		    while (count2 == 0 && d2 > 0 && partialsum[d2 - 1] > 0) {
			count2 = lookaheadsum[--d2];
		    }

		    // set dimensions
		    if (color == 'r') {
			vbox1.r2 = d2;
			vbox2.r1 = d2 + 1;
		    } else if (color == 'g') {
			vbox1.g2 = d2;
			vbox2.g1 = d2 + 1;
		    } else /* color == 'b' */ {
			vbox1.b2 = d2;
			vbox2.b1 = d2 + 1;
		    }

		    return new VBox[]{vbox1, vbox2};
		}
	    }

	    throw new RuntimeException("VBox can't be cut");
	}

	private static CMap quantize(int[][] pixels, int maxcolors) {
	    // short-circuit
	    if (pixels.length == 0 || maxcolors < 2 || maxcolors > 256) {
		return null;
	    }

	    int[] histo = getHisto(pixels);

	    // get the beginning vbox from the colors
	    VBox vbox = vboxFromPixels(pixels, histo);
	    ArrayList<VBox> pq = new ArrayList<>();
	    pq.add(vbox);

	    // Round up to have the same behaviour as in JavaScript
	    int target = maxcolors;//(int) Math.ceil(FRACT_BY_POPULATION * maxcolors);

	    // first set of colors, sorted by population
	    iter(pq, COMPARATOR_COUNT, target, histo);

	    // Re-sort by the product of pixel occupancy times the size in color
	    // space.
	    Collections.sort(pq, COMPARATOR_PRODUCT);

	    // next set - generate the median cuts using the (npix * vol) sorting.
	    iter(pq, COMPARATOR_PRODUCT, maxcolors - pq.size(), histo);

	    // Reverse to put the highest elements first into the color map
	    Collections.reverse(pq);

	    // calculate the actual colors
	    CMap cmap = new CMap();
	    for (VBox vb : pq) {
		cmap.push(vb);
	    }

	    return cmap;
	}

	/**
	 * Inner function to do the iteration.
	 */
	private static void iter(
		List<VBox> lh,
		Comparator<VBox> comparator,
		int target,
		int[] histo) {
	    int ncolors = 1;
	    int niters = 0;
	    VBox vbox;

	    while (niters < MAX_ITERATIONS) {
		vbox = lh.get(lh.size() - 1);
		if (vbox.count(false) == 0) {
		    Collections.sort(lh, comparator);
		    niters++;
		    continue;
		}
		lh.remove(lh.size() - 1);

		// do the cut
		VBox[] vboxes = medianCutApply(histo, vbox);
		VBox vbox1 = vboxes[0];
		VBox vbox2 = vboxes[1];

		if (vbox1 == null) {
		    throw new RuntimeException(
			    "vbox1 not defined; shouldn't happen!");
		}

		lh.add(vbox1);
		if (vbox2 != null) {
		    lh.add(vbox2);
		    ncolors++;
		}
		Collections.sort(lh, comparator);

		if (ncolors >= target) {
		    return;
		}
		if (niters++ > MAX_ITERATIONS) {
		    return;
		}
	    }
	}

	private static final Comparator<VBox> COMPARATOR_COUNT = new Comparator<VBox>() {
	    @Override
	    public int compare(VBox a, VBox b) {
		return a.count(false) - b.count(false);
	    }
	};

	private static final Comparator<VBox> COMPARATOR_PRODUCT = new Comparator<VBox>() {
	    @Override
	    public int compare(VBox a, VBox b) {
		int aCount = a.count(false);
		int bCount = b.count(false);
		int aVolume = a.volume(false);
		int bVolume = b.volume(false);

		// If count is 0 for both (or the same), sort by volume
		if (aCount == bCount) {
		    return aVolume - bVolume;
		}

		// Otherwise sort by products
		return aCount * aVolume - bCount * bVolume;
	    }
	};

    }

    private static final class CMap {

	private final ArrayList<MMCQ.VBox> vboxes = new ArrayList<>();

	private void push(MMCQ.VBox box) {
	    vboxes.add(box);
	}

	private int[][] palette() {
	    int numVBoxes = vboxes.size();
	    int[][] palette = new int[numVBoxes][];
	    for (int i = 0; i < numVBoxes; i++) {
		palette[i] = vboxes.get(i).avg(false);
	    }
	    return palette;
	}

    }

}
