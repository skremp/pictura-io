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

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry.Filter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * Convenience class to provide methods to scan the classpath for new ImageIO
 * plugins and create image input and output streams. It also provides embedded
 * (non standard) image encoders.
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
final class PicturaImageIO {

    // Supported I/O image formats as mapping from format name to mime type
    private static final Map<String, String> IMAGE_READER_FORMATS = new HashMap<String, String>() {

	private static final long serialVersionUID = 5528888241670256798L;

	@Override
	public String put(String key, String value) {
	    if ("".equals(key) || "".equals(value)) {
		return null;
	    }
	    return super.put(key, value);
	}
    };

    private static final Map<String, String> IMAGE_WRITER_FORMATS = new HashMap<String, String>() {

	private static final long serialVersionUID = -6425788761264392130L;

	@Override
	public String put(String key, String value) {
	    if ("".equals(key) || "".equals(value)) {
		return null;
	    }
	    return super.put(key, value);
	}
    };
    
    private static final Map<String, String> MIME_MAPPING = new HashMap<String, String>() {
	
	private static final long serialVersionUID = 3109256773218160485L;
	
	@Override
	public String put(String key, String value) {
	    if ("".equals(key) || "".equals(value)) {
		return null;
	    }
	    return super.put(key, value);
	}
    };

    // TODO: build a dynamic accept header string
    private static final String REMOTE_CLIENT_ACCEPT_HEADER = "image/jpeg, image/png, image/gif, image/*;q=0.5, */*;q=0.3";

    /**
     * The default instance (used in the context of the
     * <code>PicturaServlet</code>) of the JDK Image I/O registry.
     */
    static final IIORegistry IIO_REGISTRY = IIORegistry.getDefaultInstance();

    static String getRemoteClientAcceptHeader() {
	return REMOTE_CLIENT_ACCEPT_HEADER;
    }

    static Map<String, String> getImageReaderFormats() {
	return IMAGE_READER_FORMATS;
    }

    static Map<String, String> getImageWriterFormats() {
	return IMAGE_WRITER_FORMATS;
    }
    
    static Map<String, String> getImageReaderWriterMIMETypes() {
	return MIME_MAPPING;
    }

    /**
     * Gets the image MIME-type for a given local file.
     *
     * @param f The file object.
     * @return The corresponding image MIME-type, based on the filename
     * extension of the given file object or <code>null</code> if the MIME-type
     * could not be determined.
     *
     * @see File
     */
    static String getMIMETypeFromFile(File f) {
	// Determine the source mime type
	if (f != null) {
	    String filename = f.getAbsolutePath().toLowerCase(Locale.ENGLISH);
	    int is = filename.lastIndexOf('.');
	    if (is > -1) {
		String formatName = filename.substring(is + 1);
		return IMAGE_READER_FORMATS.containsKey(formatName)
			? IMAGE_READER_FORMATS.get(formatName)
			: IMAGE_WRITER_FORMATS.get(formatName);
	    }
	}
	return null;
    }

    static ImageInputStream createImageInputStream(InputStream is, boolean useCache,
	    File cacheDirectory) throws IOException {

	return createImageInputStream(is, null, useCache, cacheDirectory);
    }

    static ImageInputStream createImageInputStream(InputStream is, Filter filter,
	    boolean useCache, File cacheDirectory) throws IOException {

	if (is == null) {
	    throw new IllegalArgumentException("The image input cannot be null.");
	}

	Iterator<ImageInputStreamSpi> iter;
	// Ensure category is present
	try {
	    iter = (filter == null)
		    ? IIO_REGISTRY.getServiceProviders(ImageInputStreamSpi.class, true)
		    : IIO_REGISTRY.getServiceProviders(ImageInputStreamSpi.class, filter, true);
	} catch (IllegalArgumentException e) {
	    return null;
	}

	while (iter.hasNext()) {
	    ImageInputStreamSpi spi = iter.next();
	    if (spi.getInputClass().isInstance(is)) {
		try {
		    return spi.createInputStreamInstance(is, useCache,
			    cacheDirectory);
		} catch (IOException e) {
		    throw new IIOException("Can't create cache file.", e);
		}
	    }
	}

	return null;
    }

    static ImageOutputStream createImageOutputStream(OutputStream os, boolean useCache,
	    File cacheDirectory) throws IOException {

	return createImageOutputStream(os, null, useCache, cacheDirectory);
    }

    static ImageOutputStream createImageOutputStream(OutputStream os, Filter filter,
	    boolean useCache, File cacheDirectory) throws IOException {

	if (os == null) {
	    throw new IllegalArgumentException("The image output cannot be null.");
	}

	Iterator<ImageOutputStreamSpi> iter;
	// Ensure category is present
	try {
	    iter = (filter == null)
		    ? IIO_REGISTRY.getServiceProviders(ImageOutputStreamSpi.class, true)
		    : IIO_REGISTRY.getServiceProviders(ImageOutputStreamSpi.class, filter, true);
	} catch (IllegalArgumentException e) {
	    return null;
	}

	while (iter.hasNext()) {
	    ImageOutputStreamSpi spi = iter.next();
	    if (spi.getOutputClass().isInstance(os)) {
		try {
		    return spi.createOutputStreamInstance(os, useCache,
			    cacheDirectory);
		} catch (IOException e) {
		    throw new IIOException("Can't create cache file!", e);
		}
	    }
	}

	return null;
    }

    // Helper method to convert the image bytes to base64
    static byte[] createBase64EncodedImage(byte[] data, String mimeType,
	    String encoding) throws UnsupportedEncodingException {

	StringBuilder b64 = new StringBuilder("data:");
	b64.append(mimeType);
	b64.append(";UTF-8");
	b64.append(";base64,");
	b64.append(Base64Encoder.encode(data));
	return b64.toString().getBytes(encoding);
    }
    
    /**
     * Scans for plug-ins on the application class path, loads their service
     * provider classes, and registers a service provider instance for each one
     * found with the {@link IIORegistry}.
     */
    static void scanForPlugins() {
	// Clean all
	IMAGE_READER_FORMATS.clear();
	IMAGE_WRITER_FORMATS.clear();
	
	// Register all MIME types
	registerMIMETypes(IIO_REGISTRY.getServiceProviders(ImageReaderSpi.class, true));	
	registerMIMETypes(IIO_REGISTRY.getServiceProviders(ImageWriterSpi.class, true));

	// Add build in image support
	IMAGE_WRITER_FORMATS.put("ico", "image/x-icon");
        MIME_MAPPING.put("image/x-icon", "ico");
    }   
    
    private static final class ServiceProviderFilter implements Filter {

	private final List<String> includeProviderNames;
	private final List<String> excludeProviderNames;
	
	private ServiceProviderFilter(List<String> include, List<String> exclude) {
	    this.includeProviderNames = include;
	    this.excludeProviderNames = exclude;
	}
	
	@Override
	public boolean filter(Object provider) {
	    if (!(provider instanceof ImageReaderWriterSpi) 
		    || ((includeProviderNames == null || includeProviderNames.isEmpty()) &&
			    (excludeProviderNames == null || excludeProviderNames.isEmpty()))) {
		return true;
	    }	    
	    if (provider instanceof ImageReaderWriterSpi) {
		String spiClazzName = ((ImageReaderWriterSpi) provider).getClass().getName();
		if (includeProviderNames != null && !includeProviderNames.isEmpty()) {
		    for (String s : includeProviderNames) {
			if (spiClazzName.equals(s.trim())) {
			    return true;
			}
		    }
		    return false;
		}
		if (excludeProviderNames != null && !excludeProviderNames.isEmpty()) {
		    for (String s : excludeProviderNames) {
			if (spiClazzName.equals(s.trim())) {
			    return false;
			}
		    }
		    return true;
		}
		return true;
	    }
	    return false;
	}
	
    }            
    
    static Filter createServiceProviderFilter(String[] include, String[] exclude) {
	return new ServiceProviderFilter(include != null ? Arrays.asList(include) : null, 
		exclude != null ? Arrays.asList(exclude) : null);
    }
    
    private static void registerMIMETypes(Iterator<? extends ImageReaderWriterSpi> spiIter) {
	while (spiIter != null && spiIter.hasNext()) {
	    ImageReaderWriterSpi spi = spiIter.next();
	    String[] fmArr = concatAll(spi.getFormatNames(), spi.getFileSuffixes());
	    for (String fm : fmArr) {
		String fm0 = fm.toLowerCase(Locale.ENGLISH);
		if (!fm0.contains(" ")) {
		    if (spi instanceof ImageWriterSpi) {
			if (!IMAGE_WRITER_FORMATS.containsKey(fm0)) {
			    IMAGE_WRITER_FORMATS.put(fm0, spi.getMIMETypes()[0]);
			}
		    } else if (spi instanceof ImageReaderSpi) {
			if (!IMAGE_READER_FORMATS.containsKey(fm0)) {
			    IMAGE_READER_FORMATS.put(fm0, spi.getMIMETypes()[0]);
			}
		    }
		    
		    
		}
	    }
	    
	    String[] mimeTypes = spi.getMIMETypes();
	    for (String mime : mimeTypes) {
		if (!MIME_MAPPING.containsKey(mime)) {
		    MIME_MAPPING.put(mime, spi.getFileSuffixes()[0].toLowerCase(Locale.ENGLISH));
		}
	    }
	}
    }

    @SafeVarargs
    private static <T> T[] concatAll(T[] first, T[]... rest) {
	int totalLength = first.length;
	for (T[] array : rest) {
	    totalLength += array.length;
	}
	T[] result = Arrays.copyOf(first, totalLength);
	int offset = first.length;
	for (T[] array : rest) {
	    System.arraycopy(array, 0, result, offset, array.length);
	    offset += array.length;
	}
	return result;
    }

    private static final long MAX_COLORS_TYPE_8INDEX = Math.round(Math.pow(2, 8));
    private static final long MAX_COLORS_TYPE_RGB555 = Math.round(Math.pow(2, 15));
    private static final long MAX_COLORS_TYPE_RGB565 = Math.round(Math.pow(2, 16));

    /**
     * Creates an optimized {@link IIOImage} to write with an
     * {@link ImageWriter}.
     *
     * @param img The image to write out.
     * @param formatName The target image format.
     *
     * @return An {@link IIOImage} to write with an {@link ImageWriter}.
     */
    static IIOImage optimizedIIOImage(BufferedImage img, String formatName) {
	return optimizedIIOImage(img, formatName, null);
    }

    /**
     * Creates an optimized {@link IIOImage} to write with an
     * {@link ImageWriter}.
     *
     * @param img The image to write out.
     * @param formatName The target image format.
     * @param appendMetadata If <code>true</code> appends default metadata to
     * the output image.
     * @param metadata The image metadata to append to the {@link IIOImage}.
     *
     * @return An {@link IIOImage} to write with an {@link ImageWriter}.
     */
    static IIOImage optimizedIIOImage(BufferedImage img, String formatName,
	    IIOMetadata metadata) {

	if (formatName != null) {

	    switch (formatName) {

		// Reduce the color space if possible to reduce the target
		// image file size in cases of PNG images.
		case "png":
		case "bmp":
		    if (!img.getColorModel().hasAlpha()) {
			int numColors = Pictura.colorTable(img).size();
			if (numColors <= MAX_COLORS_TYPE_RGB565) {
			    BufferedImage out = new BufferedImage(img.getWidth(),
				    img.getHeight(),
				    numColors <= MAX_COLORS_TYPE_8INDEX
					    ? BufferedImage.TYPE_BYTE_INDEXED
					    : numColors <= MAX_COLORS_TYPE_RGB555
						    ? BufferedImage.TYPE_USHORT_555_RGB
						    : BufferedImage.TYPE_USHORT_565_RGB);

			    ColorConvertOp op = new ColorConvertOp(
				    img.getColorModel().getColorSpace(),
				    out.getColorModel().getColorSpace(), null);

			    op.filter(img, out);

			    return new IIOImage(out, null, null);
			}
		    }
	    }
	}

	return new IIOImage(img, null, metadata);
    }

    // Build in ICO encoder
    // http://stackoverflow.com/questions/18521470/writing-ico-files-java
    static class ICOEncoder {

	ICOEncoder() {
	}

	byte[] encode(BufferedImage src) throws IOException {
	    byte[] imgBytes = getImgBytes(src);
	    int fileSize = imgBytes.length + 22;

	    ByteBuffer bytes = ByteBuffer.allocate(fileSize);
	    bytes.order(ByteOrder.LITTLE_ENDIAN);

	    bytes.putShort((short) 0);
	    bytes.putShort((short) 1);
	    bytes.putShort((short) 1);
	    bytes.put((byte) src.getWidth());
	    bytes.put((byte) src.getHeight()); //no need to multiply
	    bytes.put((byte) src.getColorModel().getNumColorComponents()); //the pallet size
	    bytes.put((byte) 0);
	    bytes.putShort((short) 1); //should be 1
	    bytes.putShort((short) src.getColorModel().getPixelSize()); //bits per pixel
	    bytes.putInt(imgBytes.length);
	    bytes.putInt(22);
	    bytes.put(imgBytes);

	    return bytes.array();
	}

	private byte[] getImgBytes(BufferedImage img) throws IOException {
	    // create a new image, with 2x the original height.
	    BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight() * 2, BufferedImage.TYPE_INT_RGB);

	    // copy paste the pixels, but move them half the height.
	    Raster sourceRaster = img.getRaster();
	    WritableRaster destinationRaster = img2.getRaster();
	    destinationRaster.setRect(0, img.getHeight(), sourceRaster);

	    // save the new image to BMP format. 
	    FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
	    ImageIO.write(img2, "bmp", bos);

	    // strip the first 14 bytes (contains the bitmap-file-header)
	    // the next 40 bytes contains the DIB header which we still need.
	    // the pixel data follows until the end of the file.
	    return Arrays.copyOfRange(bos.buf, 14, bos.count);
	}

    }

    // Build in Base64 encoder
    static class Base64Encoder {

	private Base64Encoder() {
	}

	static String encode(byte[] a) {
	    int aLen = a.length;
	    int numFullGroups = aLen / 3;
	    int numBytesInPartialGroup = aLen - 3 * numFullGroups;
	    int resultLen = 4 * ((aLen + 2) / 3);
	    StringBuilder result = new StringBuilder(resultLen);
	    char[] intToAlpha = INT_TO_BASE64;

	    // Translate all full groups from byte array elements to Base64
	    int inCursor = 0;
	    for (int i = 0; i < numFullGroups; i++) {
		int byte0 = a[inCursor++] & 0xff;
		int byte1 = a[inCursor++] & 0xff;
		int byte2 = a[inCursor++] & 0xff;
		result.append(intToAlpha[byte0 >> 2]);
		result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
		result.append(intToAlpha[(byte1 << 2) & 0x3f | (byte2 >> 6)]);
		result.append(intToAlpha[byte2 & 0x3f]);
	    }

	    // Translate partial group if present
	    if (numBytesInPartialGroup != 0) {
		int byte0 = a[inCursor++] & 0xff;
		result.append(intToAlpha[byte0 >> 2]);
		if (numBytesInPartialGroup == 1) {
		    result.append(intToAlpha[(byte0 << 4) & 0x3f]);
		    result.append("==");
		} else {
		    // assert numBytesInPartialGroup == 2;
		    int byte1 = a[inCursor++] & 0xff;
		    result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
		    result.append(intToAlpha[(byte1 << 2) & 0x3f]);
		    result.append('=');
		}
	    }
	    return result.toString();
	}

	private static final char INT_TO_BASE64[] = {
	    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
	    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
	    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
	    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
	    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
	};

    }

    // Prevent instantiation
    private PicturaImageIO() {
    }

}
