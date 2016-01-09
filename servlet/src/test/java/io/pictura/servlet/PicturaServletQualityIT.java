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
import org.junit.Test;
import static io.pictura.servlet.PicturaServletIT.getHost;

/**
 * @author Steffen Kremp
 */
public class PicturaServletQualityIT extends PicturaServletIT {

    @Test
    public void testQuality_U() throws Exception {
	System.out.println("quality_U");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/q=u/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/q=ultrahigh/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testQuality_H() throws Exception {
	System.out.println("quality_H");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/q=h/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/q=high/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testQuality_M() throws Exception {
	System.out.println("quality_M");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/q=m/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/q=medium/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testQuality_L() throws Exception {
	System.out.println("quality_L");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/q=l/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/q=low/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testQuality_A() throws Exception {
	System.out.println("quality_A");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/q=a/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/q=auto/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testQuality_Unknown() throws Exception {
	System.out.println("quality_Unknown");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/q=unknown/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

}
