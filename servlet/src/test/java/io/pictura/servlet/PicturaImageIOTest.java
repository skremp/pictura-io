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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class PicturaImageIOTest {

    @BeforeClass
    public static void setup() {
	PicturaImageIO.scanForPlugins();
    }

    @Test
    public void testGetRemoteClientAcceptHeader() throws Exception {
	String accept = PicturaImageIO.getRemoteClientAcceptHeader();

	assertTrue(accept.contains("image/*;q=0.5"));
	assertTrue(accept.contains("*/*;q=0.3"));

	// Default internet image formats
	assertTrue(accept.contains("image/jpeg"));
	assertTrue(accept.contains("image/png"));
	assertTrue(accept.contains("image/gif"));
    }

    @Test
    public void testGetImageReaderFormats() throws Exception {
	Map<String, String> irf = PicturaImageIO.getImageReaderFormats();

	assertNotNull(irf);
	assertTrue(!irf.isEmpty());

	assertEquals("image/jpeg", irf.get("jpeg"));
	assertEquals("image/png", irf.get("png"));
	assertEquals("image/gif", irf.get("gif"));
    }

    @Test
    public void testGetImageWriteFormats() throws Exception {
	Map<String, String> irf = PicturaImageIO.getImageWriterFormats();

	assertNotNull(irf);
	assertTrue(!irf.isEmpty());

	assertEquals("image/jpeg", irf.get("jpeg"));
	assertEquals("image/png", irf.get("png"));
	assertEquals("image/gif", irf.get("gif"));
    }

    @Test
    public void testGetMIMETypeFromFile() throws Exception {
	assertEquals("image/jpeg", PicturaImageIO.getMIMETypeFromFile(new File("/tmp/foo.jpg")));
	assertEquals("image/jpeg", PicturaImageIO.getMIMETypeFromFile(new File("/tmp/foo.jpeg")));
	assertEquals("image/png", PicturaImageIO.getMIMETypeFromFile(new File("/tmp/foo.png")));
	assertEquals("image/gif", PicturaImageIO.getMIMETypeFromFile(new File("/tmp/foo.gif")));

	assertNull(PicturaImageIO.getMIMETypeFromFile(new File("/tmp/foo.com")));
	assertNull(PicturaImageIO.getMIMETypeFromFile(null));
    }

    @Test
    public void testCreateBase64EncodedImages() throws Exception {
	assertEquals("data:image/jpeg;UTF-8;base64,MTIzNDU2Nzg5MA==", new String(PicturaImageIO.createBase64EncodedImage("1234567890".getBytes(), "image/jpeg", "UTF-8")));
	assertEquals("data:image/png;UTF-8;base64,MTIzNDU2Nzg5MA==", new String(PicturaImageIO.createBase64EncodedImage("1234567890".getBytes(), "image/png", "UTF-8")));
	assertEquals("data:image/gif;UTF-8;base64,MTIzNDU2Nzg5MA==", new String(PicturaImageIO.createBase64EncodedImage("1234567890".getBytes(), "image/gif", "UTF-8")));
    }

    @Test
    public void testCreateImageInputStream() throws Exception {
	try (InputStream is = PicturaImageIOTest.class.getResourceAsStream("/lenna.jpg")) {
	    ImageInputStream iis = PicturaImageIO.createImageInputStream(is, false, null);
	    assertNotNull(iis);
	} catch (IOException ex) {
	    fail();
	}

	try (InputStream is = PicturaImageIOTest.class.getResourceAsStream("/lenna.png")) {
	    ImageInputStream iis = PicturaImageIO.createImageInputStream(is, false, null);
	    assertNotNull(iis);
	} catch (IOException ex) {
	    fail();
	}

	try (InputStream is = PicturaImageIOTest.class.getResourceAsStream("/lenna.gif")) {
	    ImageInputStream iis = PicturaImageIO.createImageInputStream(is, false, null);
	    assertNotNull(iis);
	} catch (IOException ex) {
	    fail();
	}
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateImageInputStream_IllegalArgumentException() throws Exception {
	PicturaImageIO.createImageInputStream(null, false, null);
    }

    @Test
    public void testCreateImageOutputStream() throws Exception {
        File tmpFile = File.createTempFile("test", ".jpg");
        tmpFile.deleteOnExit();
        
        try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
            ImageOutputStream ios = PicturaImageIO.createImageOutputStream(fos, false, null);
            assertNotNull(ios);
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateImageOutputStream_IllegalArgumentException() throws Exception {
	PicturaImageIO.createImageOutputStream(null, false, null);
    }
    
    @Test
    public void testICOEncoder() throws Exception {
	BufferedImage img = ImageIO.read(PicturaImageIO.class.getResource("/lenna.jpg"));
	img = Pictura.resize(img, Pictura.Method.AUTOMATIC, Pictura.Mode.AUTOMATIC, 64, 64);

	PicturaImageIO.ICOEncoder icoEnc = new PicturaImageIO.ICOEncoder();
	byte[] icoRaw = icoEnc.encode(img);

	assertNotNull(icoRaw);
	assertTrue(icoRaw.length > 0);
    }

}
