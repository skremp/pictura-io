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
public class PicturaServletCropIT extends PicturaServletIT {

    @Test
    public void testCrop_SQ() throws Exception {
	System.out.println("crop_sq");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=sq/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(image.getHeight(), image.getWidth());
    }

    @Test
    public void testCrop_AR_4_3() throws Exception {
	System.out.println("crop_ar_4_3");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=ar4,3/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals((4.d / 3.d), ((double) image.getWidth() / (double) image.getHeight()), 0.1);
    }

    @Test
    public void testCrop_AR_16_9() throws Exception {
	System.out.println("crop_ar_16_9");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=ar16,9/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals((16.d / 9.d), ((double) image.getWidth() / (double) image.getHeight()), 0.1);
    }

    @Test
    public void testCrop_AR_0_3() throws Exception {
	System.out.println("crop_0_3");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=ar0,3/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

    @Test
    public void testCrop_AR_4_0() throws Exception {
	System.out.println("crop_4_0");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=ar4,0/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

    @Test
    public void testCrop_T() throws Exception {
	System.out.println("crop_T");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=t5/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(220, image.getHeight());
    }

    @Test
    public void testCrop_L() throws Exception {
	System.out.println("crop_L");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=l7/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(393, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testCrop_B() throws Exception {
	System.out.println("crop_B");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=b10/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(215, image.getHeight());
    }

    @Test
    public void testCrop_R() throws Exception {
	System.out.println("crop_R");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=r5/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(395, image.getWidth());
	assertEquals(225, image.getHeight());
    }

    @Test
    public void testCrop_TL() throws Exception {
	System.out.println("crop_TL");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=t5,l5/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(395, image.getWidth());
	assertEquals(220, image.getHeight());
    }

    @Test
    public void testCrop_TLBR() throws Exception {
	System.out.println("crop_TLBR");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=t5,l5,b5,r5/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(390, image.getWidth());
	assertEquals(215, image.getHeight());
    }

    @Test
    public void testCrop_XY() throws Exception {
	System.out.println("crop_XY");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=x5,y5/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(395, image.getWidth());
	assertEquals(220, image.getHeight());
    }

    @Test
    public void testCrop_Short_XY() throws Exception {
	System.out.println("crop_Short_XY");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=5,5/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(395, image.getWidth());
	assertEquals(220, image.getHeight());
    }

    @Test
    public void testCrop_WH() throws Exception {
	System.out.println("crop_WH");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=w50,h25/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(50, image.getWidth());
	assertEquals(25, image.getHeight());
    }

    @Test
    public void testCrop_XYWH() throws Exception {
	System.out.println("crop_XYWH");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=x5,y5,w100,h50/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(100, image.getWidth());
	assertEquals(50, image.getHeight());
    }

    @Test
    public void testCrop_UnknownArgument() throws Exception {
	System.out.println("crop_UnknownArgument");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=q10/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

    @Test
    public void testCrop_TooManyArgument() throws Exception {
	System.out.println("crop_TooManyArgument");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=t5,l3,b12,r7,x1/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

    @Test
    public void testCrop_SQ_TooManyArgument() throws Exception {
	System.out.println("crop_SQ_TooManyArgument");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/c=sq,t5/lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

}
