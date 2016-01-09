/**
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
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.imageio.ImageIO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class PicturaTest {

    private static BufferedImage img, imgTrim;

    @BeforeClass
    public static void init() throws Exception {
	img = ImageIO.read(PicturaTest.class.getResource("/lenna.jpg"));
	imgTrim = ImageIO.read(PicturaTest.class.getResource("/lenna-trim.jpg"));
    }

    @Test
    public void testResize() {
	BufferedImage img2 = Pictura.resize(img, Pictura.Method.AUTOMATIC, Pictura.Mode.AUTOMATIC, 100, 60);
	assertEquals(100, img2.getWidth());

	img2 = Pictura.resize(img, Pictura.Method.AUTOMATIC, Pictura.Mode.BEST_FIT_BOTH, 100, 60);
	assertEquals(100, img2.getWidth());
	assertEquals(56, img2.getHeight());

	img2 = Pictura.resize(img, Pictura.Method.AUTOMATIC, Pictura.Mode.FIT_EXACT, 100, 60);
	assertEquals(100, img2.getWidth());
	assertEquals(60, img2.getHeight());

	img2 = Pictura.resize(img, Pictura.Method.AUTOMATIC, Pictura.Mode.FIT_TO_WIDTH, 100, 60);
	assertEquals(100, img2.getWidth());

	img2 = Pictura.resize(img, Pictura.Method.AUTOMATIC, Pictura.Mode.FIT_TO_HEIGHT, 100, 60);
	assertEquals(60, img2.getHeight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResize_IllegalArgumentException0() throws Exception {
	Pictura.resize(null, Pictura.Method.AUTOMATIC, Pictura.Mode.AUTOMATIC, 50, 40);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResize_IllegalArgumentException1() throws Exception {
	Pictura.resize(img, null, Pictura.Mode.AUTOMATIC, 50, 40);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResize_IllegalArgumentException2() throws Exception {
	Pictura.resize(img, Pictura.Method.AUTOMATIC, null, 50, 40);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResize_IllegalArgumentException3() throws Exception {
	Pictura.resize(img, Pictura.Method.AUTOMATIC, Pictura.Mode.AUTOMATIC, -1, 40);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResize_IllegalArgumentException4() throws Exception {
	Pictura.resize(img, Pictura.Method.AUTOMATIC, Pictura.Mode.AUTOMATIC, 50, -1);
    }

    @Test
    public void testRotate() {
	BufferedImage img2 = Pictura.rotate(img, Pictura.Rotation.CW_90);
	assertEquals(img.getWidth(), img2.getHeight());
	assertEquals(img.getHeight(), img2.getWidth());

	img2 = Pictura.rotate(img, Pictura.Rotation.CW_180);
	assertEquals(img.getWidth(), img2.getWidth());
	assertEquals(img.getHeight(), img2.getHeight());

	img2 = Pictura.rotate(img, Pictura.Rotation.CW_270);
	assertEquals(img.getWidth(), img2.getHeight());
	assertEquals(img.getHeight(), img2.getWidth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRotate_IllegalArgumentException0() throws Exception {
	Pictura.rotate(img, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRotate_IllegalArgumentException1() throws Exception {
	Pictura.rotate(null, Pictura.Rotation.CW_90);
    }

    @Test
    public void testTrim() {
	BufferedImage img2 = Pictura.trim(imgTrim, 0.2f);
	assertEquals(399, img2.getWidth());
	assertEquals(224, img2.getHeight());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testTrim_IllegalArgumentException0() throws Exception {
	Pictura.trim(null, 0.2f);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testTrim_IllegalArgumentException1() throws Exception {
	Pictura.trim(imgTrim, -0.1f);
    }
    
    @Test
    public void testCrop() {
	BufferedImage img2 = Pictura.crop(img, 5, 5, 55, 25);
	assertEquals(55, img2.getWidth());
	assertEquals(25, img2.getHeight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCrop_IllegalArgumentException0() throws Exception {
	Pictura.crop(img, -1, 0, 10, 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCrop_IllegalArgumentException1() throws Exception {
	Pictura.crop(img, 0, -1, 10, 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCrop_IllegalArgumentException2() throws Exception {
	Pictura.crop(img, 0, 0, 1000, 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCrop_IllegalArgumentException3() throws Exception {
	Pictura.crop(img, 0, 0, 100, 2000);
    }

    @Test
    public void testConvertToBinaryImage() {
	BufferedImage bi = Pictura.convertToBinaryImage(img);
	assertEquals(BufferedImage.TYPE_BYTE_BINARY, bi.getType());
    }

    @Test
    public void testColorTable() throws Exception {
	try (InputStream is = PicturaImageIOTest.class.getResourceAsStream("/lenna.jpg")) {
	    Set<Integer> s = Pictura.colorTable(ImageIO.read(is));
	    assertEquals(32122, s.size());
	} catch (IOException ex) {
	    fail();
	}
    }

    @Test
    public void testCreate() {
	BufferedImage bi = Pictura.create(100, 200, Color.yellow);

	assertEquals(100, bi.getWidth());
	assertEquals(200, bi.getHeight());

	assertEquals(Color.yellow.getRGB(), bi.getRGB(0, 0));
	assertEquals(Color.yellow.getRGB(), bi.getRGB(99, 199));

	bi = Pictura.create(100, 200, null);

	assertEquals(100, bi.getWidth());
	assertEquals(200, bi.getHeight());

	assertEquals(Color.white.getRGB(), bi.getRGB(0, 0));
	assertEquals(Color.white.getRGB(), bi.getRGB(99, 199));
    }

    @Test
    public void testConvertToRGB() {
	BufferedImage src = new BufferedImage(102, 204, BufferedImage.TYPE_BYTE_INDEXED);
	BufferedImage dst = Pictura.convertToRGBImage(src, Color.WHITE);

	assertNotSame(src, dst);
	assertEquals(BufferedImage.TYPE_INT_RGB, dst.getType());
	assertEquals(102, dst.getWidth());
	assertEquals(204, dst.getHeight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertToRGB_IllegalArgumentException() {
	Pictura.convertToRGBImage(null, Color.WHITE);
    }

    @Test
    public void testStaticFilterInstances() {
	assertTrue(Pictura.OP_ANTIALIAS instanceof ConvolveOp);
	assertTrue(Pictura.OP_SHARPEN instanceof ConvolveOp);
	assertTrue(Pictura.OP_INVERT instanceof RescaleOp);
	assertTrue(Pictura.OP_DARKER instanceof RescaleOp);
	assertTrue(Pictura.OP_BRIGHTER instanceof RescaleOp);
	assertTrue(Pictura.OP_GRAYSCALE instanceof ColorConvertOp);
    }

    @Test
    public void testGetOpRescale() {
	BufferedImageOp op = Pictura.getOpRescale(1f);
	assertNotNull(op);
    }

    @Test
    public void testGetOpGamma() {
	BufferedImageOp op = Pictura.getOpGamma(2f);
	assertNotNull(op);
    }

    @Test
    public void testGetOpThreshold() {
	BufferedImageOp op = Pictura.getOpThreshold(20);
	assertNotNull(op);
    }

    @Test
    public void testAntialiasFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_ANTIALIAS);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testSharpenFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_SHARPEN);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testInvertFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_INVERT);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testDarkerFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_DARKER);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testBrighterFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_BRIGHTER);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testGrayscaleFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_GRAYSCALE);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testGrayscaleLuminosityFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_GRAYSCALE_LUMINOSITY);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testNoiseFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_NOISE);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testPixelateFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_PIXELATE);
	assertNotEquals(img.getRGB(121, 121), bi.getRGB(120, 120));
    }

    @Test
    public void testPosterizeFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_POSTERIZE);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testSepiaFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_SEPIA);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testSunsetFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_SUNSET);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testThresholdFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_THRESHOLD);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testAutoLevelFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_AUTO_LEVEL);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testAutoContrastFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_AUTO_CONTRAST);
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testAutoColorFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.OP_AUTO_COLOR);
	assertEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

    @Test
    public void testGammaFilter() {
	BufferedImage bi = Pictura.apply(img, Pictura.getOpGamma(1.5f));
	assertNotEquals(img.getRGB(120, 120), bi.getRGB(120, 120));
    }

}
