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
public class PicturaServletScaleIT extends PicturaServletIT {

    @Test
    public void testScale_W() throws Exception {
	System.out.println("scale_W");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w150/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(150, image.getWidth());
    }

    @Test
    public void testScale_W0() throws Exception {
	System.out.println("scale_W0");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w0/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

    @Test
    public void testScale_H() throws Exception {
	System.out.println("scale_H");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=h150/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(150, image.getHeight());
    }

    @Test
    public void testScale_H0() throws Exception {
	System.out.println("scale_H0");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=h0/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

    @Test
    public void testScale_U() throws Exception {
	System.out.println("scale_U");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w600,u/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(600, image.getWidth());
    }

    @Test
    public void testScale_DPR() throws Exception {
	System.out.println("scale_DPR");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w100,dpr2/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(200, image.getWidth());
    }

    @Test
    public void testScale_DPRDot() throws Exception {
	System.out.println("scale_DPRDot");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w100,dpr1.5/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(150, image.getWidth());
    }

    @Test
    public void testScale_DPRDotD() throws Exception {
	System.out.println("scale_DPRDotD");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w100,dpr1d5/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(150, image.getWidth());
    }

    @Test
    public void testScale_Q0() throws Exception {
	System.out.println("scale_Q0");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w200,q0/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(200, image.getWidth());
    }

    @Test
    public void testScale_Q1() throws Exception {
	System.out.println("scale_Q1");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w200,q1/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(200, image.getWidth());
    }

    @Test
    public void testScale_Q2() throws Exception {
	System.out.println("scale_Q2");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w200,q2/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(200, image.getWidth());
    }

    @Test
    public void testScale_Q3() throws Exception {
	System.out.println("scale_Q3");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w200,q3/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(200, image.getWidth());
    }

    @Test
    public void testScale_Q4() throws Exception {
	System.out.println("scale_Q4");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w200,q4/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(200, image.getWidth());
    }

    @Test
    public void testScale_M0() throws Exception {
	System.out.println("scale_M0");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w200,m0/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(200, image.getWidth());
    }

    @Test
    public void testScale_M1() throws Exception {
	System.out.println("scale_M1");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w200,m1/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(200, image.getWidth(), 5);
    }

    @Test
    public void testScale_M2() throws Exception {
	System.out.println("scale_M2");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w200,m2/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(200, image.getWidth(), 5);
    }

    @Test
    public void testScale_M3() throws Exception {
	System.out.println("scale_M3");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w200,m3/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(200, image.getWidth());
    }

    @Test
    public void testScale_M4() throws Exception {
	System.out.println("scale_M4");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w200,m4/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(200, image.getWidth(), 5);
    }

    @Test
    public void testScale_EmptyParam() throws Exception {
	System.out.println("scale_EmptyParam");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=w100,h/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

    @Test
    public void testScale_UnknownArgument() throws Exception {
	System.out.println("scale_UnknownArgument");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/s=h100,z5/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

}
