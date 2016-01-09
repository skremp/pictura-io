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
public class PicturaServletRotateIT extends PicturaServletIT {

    @Test
    public void testRotate_L() throws Exception {
	System.out.println("rotate_L");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/r=l/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(225, image.getWidth());
	assertEquals(400, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/r=90/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(225, image.getWidth());
	assertEquals(400, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/r=cw90/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(225, image.getWidth());
	assertEquals(400, image.getHeight());
    }

    @Test
    public void testRotate_LR() throws Exception {
	System.out.println("rotate_LR");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/r=lr/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/r=rl/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/r=180/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/r=cw180/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testRotate_R() throws Exception {
	System.out.println("rotate_R");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/r=r/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(225, image.getWidth());
	assertEquals(400, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/r=270/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(225, image.getWidth());
	assertEquals(400, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/r=cw270/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(225, image.getWidth());
	assertEquals(400, image.getHeight());
    }

    @Test
    public void testRotate_H() throws Exception {
	System.out.println("rotate_H");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/r=h/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testRotate_V() throws Exception {
	System.out.println("rotate_V");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/r=v/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testRotate_UnknownArgument() throws Exception {
	System.out.println("rotate_UnknownArgument");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/r=z/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

}
