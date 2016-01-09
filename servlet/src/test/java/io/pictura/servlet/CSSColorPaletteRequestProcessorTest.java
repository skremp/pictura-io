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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class CSSColorPaletteRequestProcessorTest {

    @Test
    public void testIsPreferred() throws Exception {
	System.out.println("isPreferred");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertTrue(rp.isPreferred(req));
    }

    @Test
    public void testCreateRequestProcessor() throws Exception {
	System.out.println("createRequestProcessor");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	assertTrue(rp.createRequestProcessor() instanceof CSSColorPaletteRequestProcessor);
    }

    @Test
    public void testGetRequestedPrefix() throws Exception {
	System.out.println("getRequestedPrefix");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/pf=foo/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertEquals("foo-", rp.getRequestedPrefix(req));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedPrefix_IllegalArgumentException() throws Exception {
	System.out.println("getRequestedPrefix_IllegalArgumentException");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/pf=foo@/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	rp.getRequestedPrefix(req);
    }

    @Test
    public void testGetRequestedLinearGradient() throws Exception {
	System.out.println("getRequestedLinearGradient");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/pf=foo/lg=1/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertTrue(rp.getRequestedLinearGradient(req));
    }

    @Test
    public void testGetRequestedLinearGradient_Default() throws Exception {
	System.out.println("getRequestedLinearGradient_Default");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/pf=foo/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertFalse(rp.getRequestedLinearGradient(req));
    }

    @Test
    public void testGetRequestedIgnoreWhite() throws Exception {
	System.out.println("getRequestedIgnoreWhite");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/pf=foo/lg=1/iw=0/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertFalse(rp.getRequestedIgnoreWhite(req));
    }

    @Test
    public void testGetRequestedIgnoreWhite_Default() throws Exception {
	System.out.println("getRequestedIgnoreWhite");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/pf=foo/lg=1/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertTrue(rp.getRequestedIgnoreWhite(req));
    }

    @Test
    public void testGetRequestedColorCount() throws Exception {
	System.out.println("getRequestedColorCount");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/pf=foo/lg=1/iw=1/cc=5/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertSame(5, rp.getRequestedColorCount(req));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedColorCount_IllegalArgument1() throws Exception {
	System.out.println("getRequestedColorCount_IllegalArgument1");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/pf=foo/lg=1/iw=1/cc=1/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	rp.getRequestedColorCount(req);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedColorCount_IllegalArgument2() throws Exception {
	System.out.println("getRequestedColorCount_IllegalArgument2");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/pf=foo/lg=1/iw=1/cc=33/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	rp.getRequestedColorCount(req);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedScaleForceUpscale_IllegalArgument() throws Exception {
	System.out.println("getRequestedScaleForceUpscale_IllegalArgument");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/pf=foo/lg=1/iw=1/cc=33/s=w222,u/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	rp.getRequestedScaleForceUpscale(req);
    }

    @Test
    public void testGetRequestedScaleForceUpscale() throws Exception {
	System.out.println("getRequestedScaleForceUpscale");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/pf=foo/lg=1/iw=1/cc=33/s=w222/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	rp.getRequestedScaleForceUpscale(req);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedFormatOption_IllegalArgument() throws Exception {
	System.out.println("getRequestedFormatOption_IllegalArgument");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss,p/pf=foo/lg=1/iw=1/cc=33/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	rp.getRequestedFormatOption(req);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRequestedFormatEncoding_IllegalArgument() throws Exception {
	System.out.println("getRequestedFormatEncoding_IllegalArgument");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/pf=foo/lg=1/iw=1/cc=33/s=w222,u/fe=b64/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	rp.getRequestedFormatEncoding(req);
    }

    @Test
    public void testGetRequestedFormatEncoding() throws Exception {
	System.out.println("getRequestedFormatEncoding");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/lg=1/iw=1/cc=33/s=w222,u/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertNull(rp.getRequestedFormatEncoding(req));
    }

    @Test
    public void testGetRequestedFormatName() throws Exception {
	System.out.println("getRequestedFormatName");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=pcss/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertNull(rp.getRequestedFormatName(req));
    }

    @Test
    public void testIsProxyRequest() throws Exception {
	System.out.println("isProxyRequest");

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertFalse(rp.isProxyRequest(req));
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

	FileResourceLocator frl = new FileResourceLocator() {

	    @Override
	    protected String getRootPath() {
		try {
		    URL url = CSSColorPaletteRequestProcessorTest.class.getResource("/lenna.jpg");
		    File f = new File(url.toURI());
		    return f.getParentFile().getAbsolutePath();
		} catch (Throwable t) {
		    fail();
		}
		return null;
	    }

	};

	CSSColorPaletteRequestProcessor rp = new CSSColorPaletteRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("");
	when(req.getRequestURI()).thenReturn("/f=pcss/lg=1/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));
	when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_FILE_SIZE")).thenReturn(1024L * 1024L);
	when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_RESOLUTION")).thenReturn(1000L * 1000L);
	when(req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE")).thenReturn(1024 * 100);

	HttpServletResponse resp = mock(HttpServletResponse.class);

	when(resp.getOutputStream()).thenReturn(sos);

	rp.setRequest(req);
	rp.setResponse(resp);
	rp.setResourceLocators(new ResourceLocator[]{frl});

	rp.doProcess(req, resp);

	String css = bos.toString();

	assertTrue(css.contains(".fg-0{color:#e8b499;}"));
	assertTrue(css.contains(".bg-0{background-color:#e8b499;}"));
	assertTrue(css.contains(".bg-lg{background:linear-gradient(#e8b499,#d68d6f,#ca9f9f,#bb907f,#985643,#957179,#78241a,#523030,#311f21);background:-webkit-linear-gradient(#e8b499,#d68d6f,#ca9f9f,#bb907f,#985643,#957179,#78241a,#523030,#311f21);background:-moz-linear-gradient(#e8b499,#d68d6f,#ca9f9f,#bb907f,#985643,#957179,#78241a,#523030,#311f21);background:-o-linear-gradient(#e8b499,#d68d6f,#ca9f9f,#bb907f,#985643,#957179,#78241a,#523030,#311f21);}"));
    }

}
