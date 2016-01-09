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
import java.net.HttpURLConnection;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static io.pictura.servlet.PicturaServletIT.getHost;

/**
 * @author Steffen Kremp
 */
public class PicturaServletFormatIT extends PicturaServletIT {

    @Test
    public void testFormat_JPG() throws Exception {
	System.out.println("format_JPG");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/f=jpg/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");

	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testFormat_JPG_B() throws Exception {
	System.out.println("format_JPG_B");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/f=jpg,b/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");

	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testFormat_JPG_P() throws Exception {
	System.out.println("format_JPG_P");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/f=jpg,p/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");

	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testFormat_JPG_Base64() throws Exception {
	System.out.println("format_JPG_Base64");

	HttpURLConnection con = doGetResponse(new URL("http://" + getHost() + "/f=jpg,b64/lenna.jpg"), HttpURLConnection.HTTP_OK, null);
	con.disconnect();

	assertTrue(con.getContentType().startsWith("text/plain"));
	assertTrue(con.getContentLength() > 0);
    }

    @Test
    public void testFormat_JPEG() throws Exception {
	System.out.println("format_jpeg");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/f=jpeg/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");

	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testFormat_PNG() throws Exception {
	System.out.println("format_PNG");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/f=png/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/png");

	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testFormat_GIF() throws Exception {
	System.out.println("format_GIF");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/f=gif/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/gif");

	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testFormat_ICO() throws Exception {
	System.out.println("format_ICO");

	doGetImage(new URL("http://" + getHost() + "/f=ico/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/x-icon");
	// TODO: decode and validate the icon
    }

    @Test
    public void testFormat_UnknownFormat() throws Exception {
	System.out.println("format_UnknownFormat");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/f=foo/lenna.jpg"), HttpURLConnection.HTTP_UNSUPPORTED_TYPE, null);
	assertNull(image);
    }

    @Test
    public void testFormat_UnknownArgument() throws Exception {
	System.out.println("format_UnknownArgument");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/f=jpg,z/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

    @Test
    public void testFormat_TooManyArguments() throws Exception {
	System.out.println("format_TooManyArguments");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/f=jpg,b,b64,p/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

}
