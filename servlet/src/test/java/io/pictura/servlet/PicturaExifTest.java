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

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author Steffen kremp
 */
public class PicturaExifTest {

    @Test
    public void testGetOrientation1() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(PicturaExifTest.class.getResourceAsStream("/orientation_1.jpg")));
        assertEquals(1, PicturaExif.getOrientation(reader, 0));
    }

    @Test
    public void testGetOrientation2() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(PicturaExifTest.class.getResourceAsStream("/orientation_2.jpg")));
        assertEquals(2, PicturaExif.getOrientation(reader, 0));
    }
    
    @Test
    public void testGetOrientation3() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(PicturaExifTest.class.getResourceAsStream("/orientation_3.jpg")));
        assertEquals(3, PicturaExif.getOrientation(reader, 0));
    }
    
    @Test
    public void testGetOrientation4() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(PicturaExifTest.class.getResourceAsStream("/orientation_4.jpg")));
        assertEquals(4, PicturaExif.getOrientation(reader, 0));
    }
    
    @Test
    public void testGetOrientation5() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(PicturaExifTest.class.getResourceAsStream("/orientation_5.jpg")));
        assertEquals(5, PicturaExif.getOrientation(reader, 0));
    }
    
    @Test
    public void testGetOrientation6() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(PicturaExifTest.class.getResourceAsStream("/orientation_6.jpg")));
        assertEquals(6, PicturaExif.getOrientation(reader, 0));
    }
    
    @Test
    public void testGetOrientation7() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(PicturaExifTest.class.getResourceAsStream("/orientation_7.jpg")));
        assertEquals(7, PicturaExif.getOrientation(reader, 0));
    }
    
    @Test
    public void testGetOrientation8() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpg").next();
        reader.setInput(ImageIO.createImageInputStream(PicturaExifTest.class.getResourceAsStream("/orientation_8.jpg")));
        assertEquals(8, PicturaExif.getOrientation(reader, 0));
    }

}
