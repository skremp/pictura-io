/**
 * Originally written by Chris Kroells (https://github.com/coobird/thumbnailator)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.pictura.servlet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An utility class used to obtain the orientation information from a given
 * Exif metadata.
 * 
 * @author Steffen Kremp
 * 
 * @since 1.2
 */
final class PicturaExif {

    private static final String EXIF_MAGIC_STRING = "Exif";

    // Prevent instantiation
    private PicturaExif() {
    }

    /**
     * Returns the orientation obtained from the image EXIF metadata.
     *
     * @param reader An {@link ImageReader} which is reading the target image.
     * @param imageIndex The index of the image from which the EXIF metadata
     * should be read from.
     * 
     * @return	The orientation information obtained from the EXIF metadata.
     * 
     * @throws IOException When an error occurs during reading.
     * @throws IllegalArgumentException	If the {@link ImageReader} does not have
     * the target image set, or if the reader does not have a JPEG open.
     */
    public static int getOrientation(ImageReader reader, int imageIndex) throws IOException {
        IIOMetadata metadata = reader.getImageMetadata(imageIndex);
        Node rootNode = metadata.getAsTree("javax_imageio_jpeg_image_1.0");

        NodeList childNodes = rootNode.getChildNodes();

        // Look for the APP1 containing Exif data, and retrieve it.
        for (int i = 0; i < childNodes.getLength(); i++) {
            if ("markerSequence".equals(childNodes.item(i).getNodeName())) {
                NodeList markerSequenceChildren = childNodes.item(i).getChildNodes();

                for (int j = 0; j < markerSequenceChildren.getLength(); j++) {
                    IIOMetadataNode metadataNode = (IIOMetadataNode) (markerSequenceChildren.item(j));

                    byte[] bytes = (byte[]) metadataNode.getUserObject();
                    if (bytes == null) {
                        continue;
                    }

                    byte[] magicNumber = new byte[4];
                    ByteBuffer.wrap(bytes).get(magicNumber);

                    if (EXIF_MAGIC_STRING.equals(new String(magicNumber))) {
                        return getOrientationFromExif(bytes);
                    }
                }
            }
        }

        return -1;
    }

    private static int getOrientationFromExif(byte[] exifData) {

        // Needed to make byte-wise reading easier.
        ByteBuffer buffer = ByteBuffer.wrap(exifData);

        byte[] exifId = new byte[4];
        buffer.get(exifId);

        if (!EXIF_MAGIC_STRING.equals(new String(exifId))) {
            return -1;
        }

        // read the \0 after the Exif
        buffer.get();
        // read the padding byte
        buffer.get();

        byte[] tiffHeader = new byte[8];
        buffer.get(tiffHeader);

        /*
         * The first 2 bytes of the TIFF header contains either:
         *   "II" for Intel byte alignment (little endian), or
         *   "MM" for Motorola byte alignment (big endian)
         */
        ByteOrder bo = (tiffHeader[0] == 'I' && tiffHeader[1] == 'I') 
                ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

        byte[] numFields = new byte[2];
        buffer.get(numFields);

        int nFields = ByteBuffer.wrap(numFields).order(bo).getShort();

        byte[] ifd = new byte[12];
        for (int i = 0; i < nFields; i++) {
            buffer.get(ifd);
            IfdStructure ifdStructure = readIfd(ifd, bo);

            // Return the orientation from the orientation IFD
            if (ifdStructure.getTag() == 0x0112) {
                return ifdStructure.getOffsetValue();
            }
        }

        return -1;
    }

    private static IfdStructure readIfd(byte[] ifd, ByteOrder bo) {
        ByteBuffer buffer = ByteBuffer.wrap(ifd).order(bo);

        short tag = buffer.getShort();
        short type = buffer.getShort();
        int count = buffer.getInt();

        IfdType ifdType = IfdType.typeOf(type);
        int offsetValue = 0;

        /*
         * Per section 4.6.2 of the Exif Spec, if value is smaller than
         * 4 bytes, it will exist in the earlier byte.
         */
        int byteSize = count * ifdType.size();

        if (byteSize <= 4) {
            if (ifdType == IfdType.SHORT) {
                for (int i = 0; i < count; i++) {
                    offsetValue = (int) buffer.getShort();
                }
            } else if (ifdType == IfdType.BYTE || ifdType == IfdType.ASCII || ifdType == IfdType.UNDEFINED) {
                for (int i = 0; i < count; i++) {
                    offsetValue = (int) buffer.get();
                }
            } else {
                offsetValue = buffer.getInt();
            }
        } else {
            offsetValue = buffer.getInt();
        }

        return new IfdStructure(tag, type, count, offsetValue);
    }    

    private static enum IfdType {
        
        /**
         * An 8-bit unsigned integer value.
         */
        BYTE(1, 1),
        
        /**
         * An 8-bit value containing a single 7-bit ASCII character. The final
         * byte is NULL-terminated.
         */
        ASCII(2, 1),
        
        /**
         * A 16-bit unsigned integer value.
         */
        SHORT(3, 2),
        
        /**
         * An 8-bit value which can be value as defined elsewhere.
         */
        UNDEFINED(7, 1);
        
        private final int value;
        private final int size;

        private IfdType(int value, int size) {
            this.value = value;
            this.size = size;
        }

        /**
         * Returns the size in bytes for this IFD type.
         *
         * @return Size in bytes for this IFD type.
         */
        public int size() {
            return size;
        }

        /**
         * Returns the IFD type as a type value.
         *
         * @return IFD type as a type value.
         */
        public int value() {
            return value;
        }

        /**
         * Returns the {@link IfdType} corresponding to the given IFD type
         * value.
         *
         * @param value	The IFD type value.
         * 
         * @return {@link IfdType} corresponding to the IDF type value. Return
         * {@code null} if the given value does not correspond to a valid
         * {@link IfdType}.
         */
        public static IfdType typeOf(int value) {
            for (IfdType type : IfdType.values()) {
                if (type.value == value) {
                    return type;
                }
            }

            return null;
        }
    }

    private static class IfdStructure {

        private final int tag;        
        private final int offsetValue;       

        /**
         * Instantiates a IFD with the given attributes.
         *
         * @param tag The tag element.
         * @param type The type element.
         * @param count	The count of values.
         * @param offsetValue The offset or value.
         */
        public IfdStructure(int tag, int type, int count, int offsetValue) {
            super();
            this.tag = tag;
            this.offsetValue = offsetValue;
        }

        /**
         * Returns the tag element in the IFD structure.
         *
         * @return An integer representation of the tag element. Should be a
         * value between 0x00 to 0xFF.
         */
        public int getTag() {
            return tag;
        }

        /**
         * Returns either the offset or value of the IFD.
         *
         * @return Either the offset or value. The type of the returned value
         * can be determined by the return of the {@link #isOffset()} or
         * {@link #isValue()} method.
         */
        public int getOffsetValue() {
            return offsetValue;
        }

    }

}
