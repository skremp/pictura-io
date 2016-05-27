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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>PlaceholderRequestProcessor</code> is a simple "Lorem Ipsum" image
 * generator, to produce placeholder images.
 * <p>
 * The processor supports the same operations as the default {@link
 * ImageRequestProcessor} with the expection that the image source parameter
 * will be ignored.
 *
 * @author Steffen Kremp
 *
 * @see ImageRequestProcessor
 *
 * @since 1.0
 */
final class PlaceholderRequestProcessor extends ImageRequestProcessor {

    /**
     * The image placeholder dimension parameter name.
     */
    private static final String QPARAM_NAME_DIMENSION = "d";

    /**
     * The image placeholder font name to print the placeholder dimension.
     */
    private static final String QPARAM_NAME_TYPE_FACE = "tf";

    /**
     * The default placeholder image format.
     */
    private static final String DEFAULT_FORMAT_NAME = "GIF";

    /**
     * Specifies an image placeholder dimension in px.
     */
    private interface Dimension {

	/**
	 * @return The placeholder width in px.
	 */
	int getWidth();

	/**
	 * @return The placeholder height in px.
	 */
	int getHeight();

    }

    /**
     * List of named colors.
     */
    private enum BackgroundColor {

	RED_500("red", 0xF4, 0x43, 0x36),
	PINK_500("pink", 0xE9, 0x1E, 0x63),
	PURPLE_500("purple", 0x9C, 0x27, 0xB0),
	DEEP_PURPLE_500("deeppurple", 0x67, 0x3A, 0xB7),
	INDIGO_500("indigo", 0x3F, 0x51, 0xB5),
	BLUE_500("blue", 0x21, 0x96, 0xF3),
	LIGHT_BLUE_500("lightblue", 0x03, 0xA9, 0xF4),
	CYAN_500("cyan", 0x00, 0xBC, 0xD4),
	TEAL_500("teal", 0x00, 0x96, 0x88),
	GREEN_500("green", 0x4C, 0xAF, 0x50),
	LIGHT_GREEN_500("lightgreen", 0x8B, 0xC3, 0x4A),
	LIME_500("lime", 0xCD, 0xDC, 0x39),
	YELLOW_500("yellow", 0xFF, 0xEB, 0x3B),
	AMBER_500("amber", 0xFF, 0xC1, 0x07),
	ORANGE_500("orange", 0xFF, 0x98, 0x00),
	DEEP_ORGANGE_500("deeporange", 0xFF, 0x57, 0x22),
	BROWN_500("brown", 0x79, 0x55, 0x48),
	GREY_500("grey", 0x9E, 0x9E, 0x9E),
	BLUE_GREY_500("bluegrey", 0x60, 0x7D, 0x8B);

	private final String name;
	private final Color color;

	private BackgroundColor(String name, int red, int green, int blue) {
	    this.name = name;
	    this.color = new Color(red, green, blue);
	}

	static Color forName(String name) {
	    for (BackgroundColor bg : BackgroundColor.values()) {
		if (bg.name.equals(name)) {
		    return bg.color;
		}
	    }
	    return null;
	}

    }

    @Override
    protected String getRequestedImage(HttpServletRequest req) {
	// Placeholder images are dynamic images without any real image reference
	// Therefore we will always return null but we will not thrown any
	// exception because a "fake" image name could be necessary into a
	// client application.
	return null;
    }

    @Override
    protected Color getRequestedBackgroundColor(HttpServletRequest req) {
	String str = req != null ? getRequestParameter(req,
		QPARAM_NAME_BGCOLOR) : null;

	if (str != null && !str.isEmpty()) {
	    Color named = BackgroundColor.forName(str);
	    return named != null ? named : super.getRequestedBackgroundColor(req);
	}

	return null;
    }

    // Gets the requested placeholder dimension
    private Dimension getRequestedDimension(HttpServletRequest req) {
	final String dim = getRequestParameter(req, QPARAM_NAME_DIMENSION);
	if (dim != null && !dim.isEmpty()) {
	    final String[] dimParams = dim.split("x");
	    if (dimParams.length == 2) {
		final Integer w = tryParseInt(dimParams[0], null);
		final Integer h = tryParseInt(dimParams[1], null);
		if (w != null && h != null) {
		    if (w < 1 || h < 1) {
			throw new IllegalArgumentException("Invalid placeholder dimension: "
				+ "The minimum placeholder dimension is 1x1 px");
		    }
		    return new Dimension() {
			@Override
			public int getWidth() {
			    return w;
			}

			@Override
			public int getHeight() {
			    return h;
			}
		    };
		}
	    } else {
		throw new IllegalArgumentException("Invalid placeholder dimension: "
			+ "The placeholder dimension must be given in the form [width x height]");
	    }
	}
	return null;
    }

    // Gets the requested type face (font)
    private String getRequestedTypeFace(HttpServletRequest req) {
	final String tf = getRequestParameter(req, QPARAM_NAME_TYPE_FACE);
	if (tf != null && !tf.isEmpty()) {
	    return tf;
	}
	return null;
    }

    @Override
    protected void doProcess(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {

	// A placeholder image will be always the same image
	final long ifModifiedSince = req.getDateHeader("If-Modified-Since");
	if (ifModifiedSince > -1L) {
	    doInterrupt(HttpServletResponse.SC_NOT_MODIFIED);
	    return;
	}

	final Dimension dim = getRequestedDimension(req);
	if (dim == null) {
	    doInterrupt(HttpServletResponse.SC_BAD_REQUEST,
		    "Invalid placeholder dimension: Missing required dimension parameter");
	    return;
	}

	final String typeFace = getRequestedTypeFace(req);
	if (typeFace != null) {
	    String[] ffn = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

	    boolean typeFaceSupported = false;
	    for (String fn : ffn) {
		if (fn.toLowerCase(Locale.ENGLISH).equals(typeFace)) {
		    typeFaceSupported = true;
		    break;
		}
	    }

	    if (!typeFaceSupported) {
		doInterrupt(HttpServletResponse.SC_BAD_REQUEST,
			"Invalid type face: Unsupported "
			+ "type face (font family name) \"" + typeFace + "\"");
		return;
	    }
	}

	// The requested background color or if not specified the default
	// background color
	Color bgColor = getRequestedBackgroundColor(req);
	if (bgColor == null) {
	    bgColor = BackgroundColor.GREY_500.color;
	}

	// Create a new placeholder image with the specified dimension
	final BufferedImage img = Pictura.create(dim.getWidth(), dim.getHeight(), bgColor);

	// Write the origin dimension in the inverse background color to 
	// the placeholder
	final Graphics2D g = (Graphics2D) img.getGraphics();
	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	g.setColor(getForegroundColor(bgColor));

	final String txt = dim.getWidth() + " x " + dim.getHeight();

	// Set the initial font to paint the dimension string
	g.setFont(new Font(typeFace != null ? typeFace : Font.SANS_SERIF, Font.PLAIN, 20));

	// Scale the font to fit in the placeholder image
	Font scaledFont = scaleFont(txt, new Rectangle(0, 0,
		dim.getWidth() - (dim.getWidth() / 4),
		dim.getHeight() - (dim.getHeight() / 4)), g);
	g.setFont(scaledFont);

	// The font metrics to calculate the coords
	final FontMetrics fm = g.getFontMetrics();

	// Calculate the center position and draw the text
	final Rectangle2D strBounds = fm.getStringBounds(txt, g);

	// Only append the dimension string to the produced image if
	// the image dimension is larger as the string bounds
	if (dim.getWidth() > strBounds.getWidth()
		&& dim.getHeight() > strBounds.getHeight()) {
	    final double x = (dim.getWidth() - strBounds.getWidth()) / 2d;
	    final double y = (dim.getHeight() - strBounds.getHeight()) / 2d;
	    g.drawString(txt, (int) x, (int) (y + fm.getAscent()));
	}

	g.dispose();

	// Create a valid image data
	FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
	ImageIO.write(img, DEFAULT_FORMAT_NAME, bos);

	// Release resources
	img.flush();

	// Continue with the image processing (scaling, cropping, ...) if
	// also requested or if not write the response to the client
	doProcessImage(new FastByteArrayInputStream(bos), req, resp);
    }

    private Color getForegroundColor(Color bgColor) {
	return new Color(255 - bgColor.getRed(),
		255 - bgColor.getGreen(),
		255 - bgColor.getBlue(),
		255);
    }

    // http://stackoverflow.com/questions/876234/need-a-way-to-scale-a-font-to-fit-a-rectangle
    private Font scaleFont(String text, Rectangle rect, Graphics g) {
	float fontSize = 20.0f;

	Font font = g.getFont().deriveFont(fontSize);
	int width = g.getFontMetrics(font).stringWidth(text);

	fontSize = ((float) rect.width / width) * fontSize;
	return g.getFont().deriveFont(fontSize);
    }

}
