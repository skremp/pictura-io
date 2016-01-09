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

import java.awt.Color;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class ImageRequestProcessorTest {

    @BeforeClass
    public static void setUpClass() {
	PicturaImageIO.scanForPlugins();
    }

    @Test
    public void testTryParseInt() {
	System.out.println("tryParseInt");

	assertEquals(new Integer(0), ImageRequestProcessor.tryParseInt("0", -1));
	assertEquals(new Integer(-1), ImageRequestProcessor.tryParseInt("-1", 0));
	assertEquals(new Integer(Integer.MIN_VALUE), ImageRequestProcessor.tryParseInt(String.valueOf(Integer.MIN_VALUE), -1));
	assertEquals(new Integer(Integer.MAX_VALUE), ImageRequestProcessor.tryParseInt(String.valueOf(Integer.MAX_VALUE), -1));

	assertEquals(new Integer(-1), ImageRequestProcessor.tryParseInt("", -1));
	assertEquals(new Integer(-1), ImageRequestProcessor.tryParseInt(".", -1));
    }

    @Test
    public void testTryParseFloat() {
	System.out.println("tryParseFloat");

	assertEquals(new Float(0.25), ImageRequestProcessor.tryParseFloat("0.25", -1f));
	assertEquals(new Float(-1.1), ImageRequestProcessor.tryParseFloat("-1.1", 0f));
	assertEquals(new Float(Float.POSITIVE_INFINITY), ImageRequestProcessor.tryParseFloat(String.valueOf(Double.MAX_VALUE), Float.NaN));

	assertEquals(new Float(0), ImageRequestProcessor.tryParseFloat("0", -1f));
	assertEquals(new Float(-1), ImageRequestProcessor.tryParseFloat("-1", 0f));
	assertEquals(new Float(Float.MIN_VALUE), ImageRequestProcessor.tryParseFloat(String.valueOf(Float.MIN_VALUE), -1f));
	assertEquals(new Float(Float.MAX_VALUE), ImageRequestProcessor.tryParseFloat(String.valueOf(Float.MAX_VALUE), -1f));

	assertEquals(new Float(-1), ImageRequestProcessor.tryParseFloat("", -1f));
	assertEquals(new Float(-1), ImageRequestProcessor.tryParseFloat(".", -1f));
	assertEquals(new Float(Float.NaN), ImageRequestProcessor.tryParseFloat("foobar", Float.NaN));
    }

    @Test
    public void testCanReadFormat() {
	System.out.println("canReadFormat");

	ImageRequestProcessor irp = new ImageRequestProcessor();

	assertTrue(irp.canReadFormat("jpg"));
	assertTrue(irp.canReadFormat("jpeg"));
	assertTrue(irp.canReadFormat("gif"));
	assertTrue(irp.canReadFormat("png"));

	assertFalse(irp.canReadFormat("foobar"));
    }

    @Test
    public void testCanReadMimeType() {
	System.out.println("canReadMimeType");

	ImageRequestProcessor irp = new ImageRequestProcessor();

	assertTrue(irp.canReadMimeType("image/jpeg"));
	assertTrue(irp.canReadMimeType("image/gif"));
	assertTrue(irp.canReadMimeType("image/png"));

	assertFalse(irp.canReadMimeType("image/foobar"));
    }

    @Test
    public void testCanWriteFormat() {
	System.out.println("canWriteFormat");

	ImageRequestProcessor irp = new ImageRequestProcessor();

	assertTrue(irp.canWriteFormat("jpg"));
	assertTrue(irp.canWriteFormat("jpeg"));
	assertTrue(irp.canWriteFormat("gif"));
	assertTrue(irp.canWriteFormat("png"));

	assertFalse(irp.canWriteFormat("foobar"));
    }

    @Test
    public void testIsCacheable() {
	System.out.println("isCacheable");

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertTrue(irp.isCacheable());
    }

    @Test
    public void testCanWriteMimeType() {
	System.out.println("canWriteMimeType");

	ImageRequestProcessor irp = new ImageRequestProcessor();

	assertTrue(irp.canWriteMimeType("image/jpeg"));
	assertTrue(irp.canWriteMimeType("image/gif"));
	assertTrue(irp.canWriteMimeType("image/png"));

	assertFalse(irp.canWriteMimeType("image/foobar"));
    }

    @Test
    public void testGetRequestParameter_FromQuery() {
	System.out.println("getRequestParameter_FromQuery");

	ArrayList<String> params = new ArrayList<>();
	params.add("f");
	params.add("o");
	params.add("s");
	params.add("e");
	params.add("t");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getAttribute("io.pictura.servlet.ENABLE_QUERY_PARAMS")).thenReturn(true);
	when(req.getContextPath()).thenReturn("");
	when(req.getRequestURI()).thenReturn("lenna.jpg");
	when(req.getQueryString()).thenReturn("f=jpg&o=80&s=w100&e=g&t=1234567890");
	when(req.getServletPath()).thenReturn("/image/");
	when(req.getParameterNames()).thenReturn(Collections.enumeration(params));
	when(req.getParameter("f")).thenReturn("jpg");
	when(req.getParameter("o")).thenReturn("80");
	when(req.getParameter("s")).thenReturn("w100");
	when(req.getParameter("e")).thenReturn("g");
	when(req.getParameter("t")).thenReturn("1234567890");

	ImageRequestProcessor irp = new ImageRequestProcessor();

	assertEquals("jpg", irp.getRequestParameter(req, "f"));
	assertEquals("80", irp.getRequestParameter(req, "o"));
	assertEquals("w100", irp.getRequestParameter(req, "s"));
	assertEquals("g", irp.getRequestParameter(req, "e"));
	assertEquals("1234567890", irp.getRequestParameter(req, "t"));

	assertNull(irp.getRequestParameter(req, "x"));
	assertNull(irp.getRequestParameter(req, "y"));
	assertNull(irp.getRequestParameter(req, "c"));
    }

    @Test
    public void testGetRequestParameter_FromPath() {
	System.out.println("getRequestParameter_FromPath");

	ArrayList<String> params = new ArrayList<>();
	params.add("t");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getAttribute("io.pictura.servlet.ENABLE_QUERY_PARAMS")).thenReturn(true);
	when(req.getContextPath()).thenReturn("");
	when(req.getServletPath()).thenReturn("/image/");
	when(req.getRequestURI()).thenReturn("/f=jpg/o=80/s=w100,h60/e=g/lenna.jpg");
	when(req.getQueryString()).thenReturn("t=1234567890");
	when(req.getParameterNames()).thenReturn(Collections.enumeration(params));
	when(req.getParameter("t")).thenReturn("1234567890");

	ImageRequestProcessor irp = new ImageRequestProcessor();

	assertEquals("jpg", irp.getRequestParameter(req, "f"));
	assertEquals("80", irp.getRequestParameter(req, "o"));
	assertEquals("w100,h60", irp.getRequestParameter(req, "s"));
	assertEquals("g", irp.getRequestParameter(req, "e"));
	assertEquals("1234567890", irp.getRequestParameter(req, "t"));

	assertNull(irp.getRequestParameter(req, "x"));
	assertNull(irp.getRequestParameter(req, "y"));
	assertNull(irp.getRequestParameter(req, "c"));
    }

    @Test
    public void testGetRequestParameter_Null() {
	System.out.println("getRequestParameter_Null");

	ArrayList<String> params = new ArrayList<>();
	params.add("t");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("");
	when(req.getServletPath()).thenReturn("/image/");
	when(req.getRequestURI()).thenReturn("/f=jpg/o=80/s=w100,h60/e=g/lenna.jpg");
	when(req.getQueryString()).thenReturn("t=1234567890");
	when(req.getParameterNames()).thenReturn(Collections.enumeration(params));
	when(req.getParameter("t")).thenReturn("1234567890");

	ImageRequestProcessor irp = new ImageRequestProcessor();

	assertNull(irp.getRequestParameter(req, null));
	assertNull(irp.getRequestParameter(null, "f"));
	assertNull(irp.getRequestParameter(null, null));
    }

    @Test
    public void testIsProxyRequest() {
	System.out.println("isProxyRequest");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals("lenna.jpg", irp.getRequestedImage(req));

	assertNull(irp.getRequestedFormatName(req));
	assertNull(irp.getRequestedCompressionQuality(req));
	assertNull(irp.getRequestedScaleWidth(req));
	assertNull(irp.getRequestedScaleHeight(req));
	assertNull(irp.getRequestedCropX(req));
	assertNull(irp.getRequestedCropY(req));
	assertNull(irp.getRequestedCropWidth(req));
	assertNull(irp.getRequestedCropHeight(req));
	assertNull(irp.getRequestedEffects(req));

	assertTrue(irp.isProxyRequest(req));
    }

    @Test
    public void testGetRequestedImage() {
	System.out.println("getRequestedImage");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals("lenna.jpg", irp.getRequestedImage(req));
    }

    @Test
    public void testGetRequestedImage_Null() {
	System.out.println("getRequestedImage_Null");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertNull(irp.getRequestedImage(req));
    }

    @Test
    public void testGetRequestedQuality_Ultra() {
	System.out.println("getRequestedQuality_Ultra");

	ImageRequestProcessor irp = new ImageRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	when(req.getRequestURI()).thenReturn("/pictura-web/images/q=u/lenna.jpg");
	assertEquals(ImageRequestProcessor.Quality.ULTRA_HIGH, irp.getRequestedQuality(req));
	assertEquals(0.9f, irp.getRequestedCompressionQuality(req), 0.01f);
    }

    @Test
    public void testGetRequestedQuality_Heigh() {
	System.out.println("getRequestedQuality_Heigh");

	ImageRequestProcessor irp = new ImageRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	when(req.getRequestURI()).thenReturn("/pictura-web/images/q=h/lenna.jpg");
	assertEquals(ImageRequestProcessor.Quality.HIGH, irp.getRequestedQuality(req));
	assertEquals(0.8f, irp.getRequestedCompressionQuality(req), 0.01f);
    }

    @Test
    public void testGetRequestedQuality_Medium() {
	System.out.println("getRequestedQuality_Medium");

	ImageRequestProcessor irp = new ImageRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	when(req.getRequestURI()).thenReturn("/pictura-web/images/q=m/lenna.jpg");
	assertEquals(ImageRequestProcessor.Quality.MEDIUM, irp.getRequestedQuality(req));
	assertEquals(0.75f, irp.getRequestedCompressionQuality(req), 0.01f);
    }

    @Test
    public void testGetRequestedQuality_Low() {
	System.out.println("getRequestedQuality_Ultra");

	ImageRequestProcessor irp = new ImageRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	when(req.getRequestURI()).thenReturn("/pictura-web/images/q=l/lenna.jpg");
	assertEquals(ImageRequestProcessor.Quality.LOW, irp.getRequestedQuality(req));
	assertEquals(0.65f, irp.getRequestedCompressionQuality(req), 0.01f);
    }

    @Test
    public void testGetRequestedQuality_Auto() {
	System.out.println("getRequestedQuality_Auto");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(ImageRequestProcessor.Quality.AUTO, irp.getRequestedQuality(req));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedQuality_IllegalArgumentException() {
	System.out.println("getRequestedQuality_Auto");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/q=foo/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedQuality(req);
    }

    @Test
    public void testGetRequestedFormatName() {
	System.out.println("getRequestedFormatName");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/f=jpg/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals("jpg", irp.getRequestedFormatName(req));
    }

    @Test
    public void testGetRequestedFormatName_Null() {
	System.out.println("getRequestedFormatName_Null");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertNull(irp.getRequestedFormatName(req));
    }

    @Test
    public void testGetRequestedFormatOption() {
	System.out.println("getRequestedFormatOption");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/f=jpg,p/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals("p", irp.getRequestedFormatOption(req));
    }

    @Test
    public void testGetRequestedFormatOption_PJPG() {
	System.out.println("getRequestedFormatOption_PJPG");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/f=pjpg/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals("p", irp.getRequestedFormatOption(req));
    }

    @Test
    public void testGetRequestedFormatOption_BJPG() {
	System.out.println("getRequestedFormatOption_BJPG");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/f=bjpg/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals("b", irp.getRequestedFormatOption(req));
    }

    @Test
    public void testGetRequestedFormatOption_Null() {
	System.out.println("getRequestedFormatOption_Null");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertNull(irp.getRequestedFormatOption(req));
    }

    @Test
    public void testGetRequestedFormatEncoding_1() {
	System.out.println("getRequestedFormatEncoding_1");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/f=jpg,p,b64/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals("b64", irp.getRequestedFormatEncoding(req));
    }

    @Test
    public void testGetRequestedFormatEncoding_2() {
	System.out.println("getRequestedFormatEncoding_2");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/f=jpg,b64/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals("b64", irp.getRequestedFormatEncoding(req));
    }

    @Test
    public void testGetRequestedFormatEncoding_3() {
	System.out.println("getRequestedFormatEncoding_3");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/f=b64/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals("b64", irp.getRequestedFormatEncoding(req));
    }

    @Test
    public void testGetRequestedFormatEncoding_Null() {
	System.out.println("getRequestedFormatEncoding_Null");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/f=jpg,p/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertNull(irp.getRequestedFormatEncoding(req));
    }

    @Test
    public void testGetRequestedCompressionQuality() {
	System.out.println("getRequestedCompressionQuality");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/o=90/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Float(0.9f), irp.getRequestedCompressionQuality(req));
    }

    @Test
    public void testGetRequestedCompressionQuality_1() {
	System.out.println("getRequestedCompressionQuality_1");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/o=80/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Float(0.8f), irp.getRequestedCompressionQuality(req));
    }

    @Test
    public void testGetRequestedCompressionQuality_2() {
	System.out.println("getRequestedCompressionQuality_2");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/o=85/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Float(0.85f), irp.getRequestedCompressionQuality(req));
    }

    @Test
    public void testGetRequestedCompressionQuality_3() {
	System.out.println("getRequestedCompressionQuality_3");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/o=70/q=m/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(0.7f * 0.955f, irp.getRequestedCompressionQuality(req), 0.01f);
    }

    @Test
    public void testGetRequestedCompressionQuality_4() {
	System.out.println("getRequestedCompressionQuality_4");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/o=71/q=l/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(0.71f * 0.855f, irp.getRequestedCompressionQuality(req), 0.01f);
    }

    @Test
    public void testGetRequestedCompressionQuality_Null() {
	System.out.println("getRequestedCompressionQuality_Null");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertNull(irp.getRequestedCompressionQuality(req));
    }

    @Test
    public void testGetRequestedScaleWidth() {
	System.out.println("getRequestedScaleWidth");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60,u/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(120), irp.getRequestedScaleWidth(req));
    }

    @Test
    public void testGetRequestedScaleWidth_Null() {
	System.out.println("getRequestedScaleWidth_Null");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=h60,u/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertNull(irp.getRequestedScaleWidth(req));
    }

    @Test
    public void testGetRequestedScaleWidthPercentage() {
	System.out.println("getRequestedScaleWidthPercentage");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w90p,h60,u/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(90), irp.getRequestedScaleWidthPercentage(req));
    }

    @Test
    public void testGetRequestedScaleHeight() {
	System.out.println("getRequestedScaleHeight");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60,u/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(60), irp.getRequestedScaleHeight(req));
    }

    @Test
    public void testGetRequestedScaleHeight_Null() {
	System.out.println("getRequestedScaleHeight_Null");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,u/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertNull(irp.getRequestedScaleHeight(req));
    }

    @Test
    public void testGetRequestedScaleHeightPercentage() {
	System.out.println("getRequestedScaleHeightPercentage");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h20p,u/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(20), irp.getRequestedScaleHeightPercentage(req));
    }

    @Test
    public void testGetRequestedScalePixelRatio() {
	System.out.println("getRequestedScalePixelRatio");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60,u,dpr1.5/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Float(1.5f), irp.getRequestedScalePixelRatio(req));
    }

    @Test
    public void testGetRequestedScalePixelRatio_Null() {
	System.out.println("getRequestedScalePixelRatio_Null");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60,u/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertNull(irp.getRequestedScalePixelRatio(req));
    }

    @Test
    public void testGetRequestedScaleForceUpscale() {
	System.out.println("getRequestedScaleForceUpscale");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60,u,dpr1.5/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertTrue(irp.getRequestedScaleForceUpscale(req));
    }

    @Test
    public void testGetRequestedScaleForceUpscale_Null() {
	System.out.println("getRequestedScaleForceUpscale_Null");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60,dpr1.5/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertFalse(irp.getRequestedScaleForceUpscale(req));
    }

    @Test
    public void testGetRequestedRotation_L() {
	System.out.println("getRequestedRotation_L");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/r=l/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(90), irp.getRequestedRoatation(req));
    }

    @Test
    public void testGetRequestedRotation_R() {
	System.out.println("getRequestedRotation_R");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/r=r/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(270), irp.getRequestedRoatation(req));
    }

    @Test
    public void testGetRequestedRotation_LR() {
	System.out.println("getRequestedRotation_LR");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/r=lr/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(180), irp.getRequestedRoatation(req));
    }

    @Test
    public void testGetRequestedRotation_RL() {
	System.out.println("getRequestedRotation_RL");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/r=rl/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(180), irp.getRequestedRoatation(req));
    }

    @Test
    public void testGetRequestedFlip_H() {
	System.out.println("getRequestedFlip_H");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/r=h/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(0), irp.getRequestedFlip(req));
    }

    @Test
    public void testGetRequestedFlip_V() {
	System.out.println("getRequestedFlip_V");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/r=v/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(1), irp.getRequestedFlip(req));
    }

    @Test
    public void testGetRequestedTrimTolerance() {
	System.out.println("getRequestedTrimTolerance");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/t=2.1/s=w120,h60/c=x5,y15,w100,h50/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Float(2.1f), irp.getRequestedTrimTolerance(req));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedTrimTolerance_IllegalArgumentException_1() {
	System.out.println("getRequestedTrimTolerance_IllegalArgumentException_1");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/t=25/s=w120,h60/c=x5,y15,w100,h50/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedTrimTolerance(req);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedTrimTolerance_IllegalArgumentException_2() {
	System.out.println("getRequestedTrimTolerance_IllegalArgumentException_2");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/t=-1/s=w120,h60/c=x5,y15,w100,h50/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedTrimTolerance(req);
    }
    
    @Test
    public void testGetRequestedCropX() {
	System.out.println("getRequestedCropX");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=x5,y15,w100,h50/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(5), irp.getRequestedCropX(req));
    }

    @Test
    public void testGetRequestedCropY() {
	System.out.println("getRequestedCropY");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=x5,y15,w100,h50/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(15), irp.getRequestedCropY(req));
    }

    @Test
    public void testGetRequestedCropWidth() {
	System.out.println("getRequestedCropWidth");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=x5,y15,w100,h50/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(100), irp.getRequestedCropWidth(req));
    }

    @Test
    public void testGetRequestedCropHeight() {
	System.out.println("getRequestedCropHeight");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=x5,y15,w100,h52/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(52), irp.getRequestedCropHeight(req));
    }

    @Test
    public void testGetRequestedCropX_2() {
	System.out.println("getRequestedCropX_2");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=3,12,96,34/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(3), irp.getRequestedCropX(req));
    }

    @Test
    public void testGetRequestedCropY_2() {
	System.out.println("getRequestedCropY_2");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=3,12,96,34/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(12), irp.getRequestedCropY(req));
    }

    @Test
    public void testGetRequestedCropWidth_2() {
	System.out.println("getRequestedCropWidth_2");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=3,12,96,34/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(96), irp.getRequestedCropWidth(req));
    }

    @Test
    public void testGetRequestedCropHeight_2() {
	System.out.println("getRequestedCropHeight_2");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=3,12,96,34/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(34), irp.getRequestedCropHeight(req));
    }

    @Test
    public void testGetRequestedCropTop() {
	System.out.println("getRequestedCropTop");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=t20,l30,b12,r10/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(20), irp.getRequestedCropTop(req));
    }

    @Test
    public void testGetRequestedCropLeft() {
	System.out.println("getRequestedCropLeft");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=t20,l30,b12,r10/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(30), irp.getRequestedCropLeft(req));
    }

    @Test
    public void testGetRequestedCropBottom() {
	System.out.println("getRequestedCropBottom");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=t20,l30,b12,r10/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(12), irp.getRequestedCropBottom(req));
    }

    @Test
    public void testGetRequestedCropRight() {
	System.out.println("getRequestedCropRight");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=t20,l30,b12,r10/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(10), irp.getRequestedCropRight(req));
    }

    @Test
    public void testGetRequestedCropSq() {
	System.out.println("getRequestedCropSq");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=sq/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(Boolean.TRUE, irp.getRequestedCropSquare(req));
    }

    @Test
    public void testGetRequestedCropAR() {
	System.out.println("getRequestedCropAR");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=ar16x9/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(16), irp.getRequestedCropAspectRatioX(req));
	assertEquals(new Integer(9), irp.getRequestedCropAspectRatioY(req));
    }

    @Test
    public void testGetRequestedCropAR_2() {
	System.out.println("getRequestedCropAR_2");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=ar4,3/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(new Integer(4), irp.getRequestedCropAspectRatioX(req));
	assertEquals(new Integer(3), irp.getRequestedCropAspectRatioY(req));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedCrop_IllegalArgumentException_1() {
	System.out.println("getRequestedCrop_IllegalArgumentException_1");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=12,96,34/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedCropWidth(req);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedCrop_IllegalArgumentException_2() {
	System.out.println("getRequestedCrop_IllegalArgumentException_2");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=3,7,12,96,34/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedCropWidth(req);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedCrop_IllegalArgumentException_4() {
	System.out.println("getRequestedCrop_IllegalArgumentException_4");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=x0,y7,w100,h40,z7/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedCropWidth(req);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedCrop_IllegalArgumentException_5() {
	System.out.println("getRequestedCrop_IllegalArgumentException_5");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/c=ar16|9/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedCropAspectRatioX(req);
    }

    @Test
    public void testGetRequestedEffects_AS() {
	System.out.println("getRequestedEffects_AS");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=as/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_AUTO_COLOR, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_AC() {
	System.out.println("getRequestedEffects_AC");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=ac/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_AUTO_CONTRAST, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_AL() {
	System.out.println("getRequestedEffects_AL");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=al/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_AUTO_LEVEL, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_AE() {
	System.out.println("getRequestedEffects_AE");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=ae/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(3, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_AUTO_COLOR, irp.getRequestedEffects(req)[0]);
	assertSame(Pictura.OP_AUTO_CONTRAST, irp.getRequestedEffects(req)[1]);
	assertSame(Pictura.OP_AUTO_LEVEL, irp.getRequestedEffects(req)[2]);
    }

    @Test
    public void testGetRequestedEffects_T() {
	System.out.println("getRequestedEffects_T");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=t/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_THRESHOLD, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_T_99() {
	System.out.println("getRequestedEffects_T_99");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=t(99)/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertTrue(irp.getRequestedEffects(req)[0] instanceof BufferedImageOp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedEffects_T_IllegalArgumentException_1() {
	System.out.println("getRequestedEffects_T_IllegalArgumentException_1");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=t(-1)/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedEffects(req);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedEffects_T_IllegalArgumentException_2() {
	System.out.println("getRequestedEffects_T_IllegalArgumentException_2");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=t(256)/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedEffects(req);
    }

    @Test
    public void testGetRequestedEffects_N() {
	System.out.println("getRequestedEffects_N");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=n/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_NOISE, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_I() {
	System.out.println("getRequestedEffects_I");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=i/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_INVERT, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_SP() {
	System.out.println("getRequestedEffects_SP");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=sp/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_SEPIA, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_SS() {
	System.out.println("getRequestedEffects_SS");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=ss/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_SUNSET, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_PX() {
	System.out.println("getRequestedEffects_PX");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=px/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_PIXELATE, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_P() {
	System.out.println("getRequestedEffects_P");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=p/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_POSTERIZE, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_G() {
	System.out.println("getRequestedEffects_G");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=g/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_GRAYSCALE, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_GL() {
	System.out.println("getRequestedEffects_GL");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=gl/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_GRAYSCALE_LUMINOSITY, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_GAM_50() {
	System.out.println("getRequestedEffects_GL");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=gam(50)/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertTrue(irp.getRequestedEffects(req)[0] instanceof LookupOp);
    }

    @Test
    public void testGetRequestedEffects_S() {
	System.out.println("getRequestedEffects_S");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=s/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_SHARPEN, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_A() {
	System.out.println("getRequestedEffects_A");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=a/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_ANTIALIAS, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_B() {
	System.out.println("getRequestedEffects_B");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=b/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_BRIGHTER, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_B_12() {
	System.out.println("getRequestedEffects_B_12");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=b(12)/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertNotNull(irp.getRequestedEffects(req)[0]);
	assertTrue(irp.getRequestedEffects(req)[0] instanceof RescaleOp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedEffects_B_IllegalArgumentException_1() {
	System.out.println("getRequestedEffects_B_IllegalArgumentException_1");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=b(x202)/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedEffects(req);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedEffects_B_IllegalArgumentException_2() {
	System.out.println("getRequestedEffects_B_IllegalArgumentException_2");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=b(-1)/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedEffects(req);
    }

    @Test
    public void testGetRequestedEffects_D() {
	System.out.println("getRequestedEffects_D");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=d/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_DARKER, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_D_25() {
	System.out.println("getRequestedEffects_D_25");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=d(25)/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(1, irp.getRequestedEffects(req).length);
	assertNotNull(irp.getRequestedEffects(req)[0]);
	assertTrue(irp.getRequestedEffects(req)[0] instanceof RescaleOp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedEffects_D_IllegalArgumentException_1() {
	System.out.println("getRequestedEffects_D_IllegalArgumentException_1");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=d(1212)/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedEffects(req);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedEffects_D_IllegalArgumentException_2() {
	System.out.println("getRequestedEffects_D_IllegalArgumentException_2");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=d(-0.001)/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedEffects(req);
    }

    @Test
    public void testGetRequestedEffects_GS() {
	System.out.println("getRequestedEffects_GS");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=g,s/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(2, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_GRAYSCALE, irp.getRequestedEffects(req)[0]);
	assertSame(Pictura.OP_SHARPEN, irp.getRequestedEffects(req)[1]);
    }

    @Test
    public void testGetRequestedEffects_SG() {
	System.out.println("getRequestedEffects_SG");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=s,g/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(2, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_GRAYSCALE, irp.getRequestedEffects(req)[1]);
	assertSame(Pictura.OP_SHARPEN, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_DGS() {
	System.out.println("getRequestedEffects_DGS");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=d,g,s/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(3, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_DARKER, irp.getRequestedEffects(req)[0]);
	assertSame(Pictura.OP_GRAYSCALE, irp.getRequestedEffects(req)[1]);
	assertSame(Pictura.OP_SHARPEN, irp.getRequestedEffects(req)[2]);
    }

    @Test
    public void testGetRequestedEffects_SGD() {
	System.out.println("getRequestedEffects_SGD");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=s,g,d/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(3, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_DARKER, irp.getRequestedEffects(req)[2]);
	assertSame(Pictura.OP_GRAYSCALE, irp.getRequestedEffects(req)[1]);
	assertSame(Pictura.OP_SHARPEN, irp.getRequestedEffects(req)[0]);
    }

    @Test
    public void testGetRequestedEffects_SG_D_21() {
	System.out.println("getRequestedEffects_SG_D_21");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=s,g,d(21)/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertEquals(3, irp.getRequestedEffects(req).length);
	assertSame(Pictura.OP_GRAYSCALE, irp.getRequestedEffects(req)[1]);
	assertSame(Pictura.OP_SHARPEN, irp.getRequestedEffects(req)[0]);
	assertTrue(irp.getRequestedEffects(req)[2] instanceof RescaleOp);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedEffects_IllegalArgumentException() {
	System.out.println("getRequestedEffects_IllegalArgumentException");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w120,h60/e=z/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedEffects(req);
    }

    @Test
    public void testGetRequestedBackgroundColor_B000000() {
	System.out.println("getRequestedBackgroundColor_B000000");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/bg=000000/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	Color c = irp.getRequestedBackgroundColor(req);

	assertNotNull(c);
	assertEquals(0, c.getRed());
	assertEquals(0, c.getGreen());
	assertEquals(0, c.getBlue());
    }

    @Test
    public void testGetRequestedBackgroundColor_Null() {
	System.out.println("testGetRequestedBackgroundColor_Null");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/s=w200/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	Color c = irp.getRequestedBackgroundColor(req);

	assertNull(c);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedBackgroundColor_IllegalArgumentException() {
	System.out.println("testGetRequestedBackgroundColor_IllegalArgumentException");

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/bg=00000Z/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	ImageRequestProcessor irp = new ImageRequestProcessor();
	irp.getRequestedBackgroundColor(req);
    }

    @Test
    public void testCanReadFormat_Null() throws Exception {
	System.out.println("canReadFormat_Null");

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertFalse(irp.canReadFormat(null));
	assertFalse(irp.canReadFormat(""));
    }

    @Test
    public void testCanReadMimeType_Null() throws Exception {
	System.out.println("canReadMimeType_Null");

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertFalse(irp.canReadMimeType(null));
	assertFalse(irp.canReadMimeType(""));
    }

    @Test
    public void testCanWriteFormat_Null() throws Exception {
	System.out.println("canWriteFormat_Null");

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertFalse(irp.canWriteFormat(null));
	assertFalse(irp.canWriteFormat(""));
    }

    @Test
    public void testCanWriteMimeType_Null() throws Exception {
	System.out.println("canWriteMimeType_Null");

	ImageRequestProcessor irp = new ImageRequestProcessor();
	assertFalse(irp.canWriteMimeType(null));
	assertFalse(irp.canWriteMimeType(""));
    }

    @Test
    public void testIgnoreSourceContentType() throws Exception {
        assertFalse(new ImageRequestProcessor().ignoreSourceContentType());
    }
    
}
