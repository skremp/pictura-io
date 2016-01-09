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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class PlaceholderRequestProcessorTest {

    @Test
    public void testGetRequestedImage() throws Exception {
	System.out.println("getRequestedImage");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();
	assertNull(rp.getRequestedImage(null));

	PlaceholderRequestProcessor rp2 = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=556655/test.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertNull(rp2.getRequestedImage(req));
    }

    @Test
    public void testGetBackgroundColor_556655() throws Exception {
	System.out.println("getBackgroundColor_556655");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=556655");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0x55, c.getRed());
	assertEquals(0x66, c.getGreen());
	assertEquals(0x55, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_red() throws Exception {
	System.out.println("getBackgroundColor_red");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=red/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0xf4, c.getRed());
	assertEquals(0x43, c.getGreen());
	assertEquals(0x36, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_pink() throws Exception {
	System.out.println("getBackgroundColor_pink");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=pink/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0xe9, c.getRed());
	assertEquals(0x1e, c.getGreen());
	assertEquals(0x63, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_purple() throws Exception {
	System.out.println("getBackgroundColor_red");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=purple/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0x9c, c.getRed());
	assertEquals(0x27, c.getGreen());
	assertEquals(0xb0, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_deeppurple() throws Exception {
	System.out.println("getBackgroundColor_deeppurple");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=deeppurple/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0x67, c.getRed());
	assertEquals(0x3a, c.getGreen());
	assertEquals(0xb7, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_indigo() throws Exception {
	System.out.println("getBackgroundColor_indigo");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=indigo/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0x3f, c.getRed());
	assertEquals(0x51, c.getGreen());
	assertEquals(0xb5, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_blue() throws Exception {
	System.out.println("getBackgroundColor_blue");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=blue/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0x21, c.getRed());
	assertEquals(0x96, c.getGreen());
	assertEquals(0xf3, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_lightblue() throws Exception {
	System.out.println("getBackgroundColor_lightblue");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=lightblue/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0x03, c.getRed());
	assertEquals(0xa9, c.getGreen());
	assertEquals(0xf4, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_cyan() throws Exception {
	System.out.println("getBackgroundColor_cyan");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=cyan/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0x00, c.getRed());
	assertEquals(0xbc, c.getGreen());
	assertEquals(0xd4, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_teal() throws Exception {
	System.out.println("getBackgroundColor_teal");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=teal/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0x00, c.getRed());
	assertEquals(0x96, c.getGreen());
	assertEquals(0x88, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_green() throws Exception {
	System.out.println("getBackgroundColor_green");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=green/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0x4c, c.getRed());
	assertEquals(0xaf, c.getGreen());
	assertEquals(0x50, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_lightgreen() throws Exception {
	System.out.println("getBackgroundColor_lightgreen");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=lightgreen/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0x8b, c.getRed());
	assertEquals(0xc3, c.getGreen());
	assertEquals(0x4a, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_lime() throws Exception {
	System.out.println("getBackgroundColor_lime");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=lime/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0xcd, c.getRed());
	assertEquals(0xdc, c.getGreen());
	assertEquals(0x39, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_yellow() throws Exception {
	System.out.println("getBackgroundColor_yellow");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=yellow/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0xff, c.getRed());
	assertEquals(0xeb, c.getGreen());
	assertEquals(0x3b, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_amber() throws Exception {
	System.out.println("getBackgroundColor_amber");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=amber/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0xff, c.getRed());
	assertEquals(0xc1, c.getGreen());
	assertEquals(0x07, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_orange() throws Exception {
	System.out.println("getBackgroundColor_orange");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=orange/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0xff, c.getRed());
	assertEquals(0x98, c.getGreen());
	assertEquals(0x00, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_deeporange() throws Exception {
	System.out.println("getBackgroundColor_deeporange");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=deeporange/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0xff, c.getRed());
	assertEquals(0x57, c.getGreen());
	assertEquals(0x22, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_brown() throws Exception {
	System.out.println("getBackgroundColor_brown");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=brown/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0x79, c.getRed());
	assertEquals(0x55, c.getGreen());
	assertEquals(0x48, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_grey() throws Exception {
	System.out.println("getBackgroundColor_grey");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=grey/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0x9e, c.getRed());
	assertEquals(0x9e, c.getGreen());
	assertEquals(0x9e, c.getBlue());
    }

    @Test
    public void testGetBackgroundColor_bluegrey() throws Exception {
	System.out.println("getBackgroundColor_bluegrey");

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/ip/bg=bluegrey/");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	Color c = rp.getRequestedBackgroundColor(req);
	assertEquals(0x60, c.getRed());
	assertEquals(0x7d, c.getGreen());
	assertEquals(0x8b, c.getBlue());
    }

    @Test
    public void testDoProcess() throws Exception {
	System.out.println("doProcess");

	PicturaImageIO.scanForPlugins();

	final ByteArrayOutputStream bos = new ByteArrayOutputStream();
	final ServletOutputStream sos = new ServletOutputStream() {

	    @Override
	    public void write(int b) throws IOException {
		bos.write(b);
	    }

	    public boolean isReady() {
		return true;
	    }

	    public void setWriteListener(WriteListener arg0) {
	    }
	};

	PlaceholderRequestProcessor rp = new PlaceholderRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("");
	when(req.getRequestURI()).thenReturn("/ip/d=100x100");
	when(req.getQueryString()).thenReturn(null);
	when(req.getDateHeader("If-Modified-Since")).thenReturn(-1L);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));
	when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_FILE_SIZE")).thenReturn(1024L * 1024L);
	when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_RESOLUTION")).thenReturn(1000L * 1000L);
	when(req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE")).thenReturn(1024 * 100);

	HttpServletResponse resp = mock(HttpServletResponse.class);

	when(resp.getOutputStream()).thenReturn(sos);

	rp.setRequest(req);
	rp.setResponse(resp);

	rp.doProcess(req, resp);

	byte[] image = bos.toByteArray();

	BufferedImage bi = ImageIO.read(new ByteArrayInputStream(image));

	assertEquals(100, bi.getWidth());
	assertEquals(100, bi.getHeight());
    }

}
