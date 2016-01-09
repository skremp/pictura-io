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
public class PicturaServletEffectIT extends PicturaServletIT {

    @Test
    public void testEffect_AC() throws Exception {
	System.out.println("effect_AC");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=ac/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_AL() throws Exception {
	System.out.println("effect_AL");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=al/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_A() throws Exception {
	System.out.println("effect_A");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=a/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_SS() throws Exception {
	System.out.println("effect_SS");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=ss/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_B() throws Exception {
	System.out.println("effect_B");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=b/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_D() throws Exception {
	System.out.println("effect_D");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=d/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_G() throws Exception {
	System.out.println("effect_G");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=g/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_GL() throws Exception {
	System.out.println("effect_GL");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=gl/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_I() throws Exception {
	System.out.println("effect_I");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=i/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_S() throws Exception {
	System.out.println("effect_S");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=s/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_SP() throws Exception {
	System.out.println("effect_SP");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=sp/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_GAM_50() throws Exception {
	System.out.println("effect_GAM_50");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=gam(50)/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_GAM_InvalidNumber() throws Exception {
	System.out.println("effect_GAM_InvalidNumber");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=gam(1a)/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

    @Test
    public void testEffect_B_50() throws Exception {
	System.out.println("effect_B_50");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=b(50)/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_B_InvalidNumber() throws Exception {
	System.out.println("effect_B_InvalidNumber");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=b(X2)/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

    @Test
    public void testEffect_D_50() throws Exception {
	System.out.println("effect_D_50");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=d(50)/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testEffect_D_InvalidNumber() throws Exception {
	System.out.println("effect_D_InvalidNumber");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=d(a)/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

    @Test
    public void testEffect_UnknownArgument() throws Exception {
	System.out.println("effect_UnknownArgument");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=foo/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

    @Test
    public void testEffect_OverLimit() throws Exception {
	System.out.println("effect_OverLimit");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=a,b,d,g,gl,i/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

}
