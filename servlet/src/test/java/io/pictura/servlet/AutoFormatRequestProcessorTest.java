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

import java.util.ArrayList;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class AutoFormatRequestProcessorTest {

    @BeforeClass
    public static void setUp() {
	ImageIO.scanForPlugins();
	PicturaImageIO.scanForPlugins();
    }

    @Test
    public void testIsBypassRequest() throws Exception {
	HttpServletRequest req = mock(HttpServletRequest.class);
	AutoFormatRequestProcessor rp = new AutoFormatRequestProcessor();

	when(req.getParameter("bypass")).thenReturn(null);
	assertFalse(rp.isBypassRequest(req));

	when(req.getParameter("bypass")).thenReturn("");
	assertTrue(rp.isBypassRequest(req));

	when(req.getParameter("bypass")).thenReturn("1");
	assertTrue(rp.isBypassRequest(req));

	when(req.getParameter("bypass")).thenReturn("true");
	assertTrue(rp.isBypassRequest(req));

	when(req.getParameter("bypass")).thenReturn("TRUE");
	assertTrue(rp.isBypassRequest(req));

	when(req.getParameter("bypass")).thenReturn("0");
	assertFalse(rp.isBypassRequest(req));

	when(req.getParameter("bypass")).thenReturn("false");
	assertFalse(rp.isBypassRequest(req));

	when(req.getParameter("bypass")).thenReturn("FALSE");
	assertFalse(rp.isBypassRequest(req));

	when(req.getParameter("bypass")).thenReturn("foo");
	assertFalse(rp.isBypassRequest(req));
    }

    @Test
    public void testGetRequestedFormatName_0() throws Exception {
	HttpServletRequest req = mock(HttpServletRequest.class);
	AutoFormatRequestProcessor rp = new AutoFormatRequestProcessor();

	when(req.getHeader("User-Agent")).thenReturn("Safari");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/f=jpg/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertEquals("jpg", rp.getRequestedFormatName(req));
    }

    @Test
    public void testGetRequestedFormatName_1() throws Exception {
	HttpServletRequest req = mock(HttpServletRequest.class);
	AutoFormatRequestProcessor rp = new AutoFormatRequestProcessor();

	when(req.getHeader("User-Agent")).thenReturn("Safari");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertEquals("jp2", rp.getRequestedFormatName(req));
    }

    @Test
    public void testGetRequestedFormatName_2() throws Exception {
	HttpServletRequest req = mock(HttpServletRequest.class);
	AutoFormatRequestProcessor rp = new AutoFormatRequestProcessor();

	when(req.getHeader("User-Agent")).thenReturn("Safari/Chrome");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertNull(rp.getRequestedFormatName(req));
    }

    @Test
    public void testGetRequestedCompressionQuality_0() throws Exception {
	HttpServletRequest req = mock(HttpServletRequest.class);
	AutoFormatRequestProcessor rp = new AutoFormatRequestProcessor();

	when(req.getHeader("User-Agent")).thenReturn("Safari");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/f=jpg/o=57/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertEquals((0.57f * 0.9f), rp.getRequestedCompressionQuality(req), 0.05f);
    }

    @Test
    public void testGetRequestedCompressionQuality_1() throws Exception {
	HttpServletRequest req = mock(HttpServletRequest.class);
	AutoFormatRequestProcessor rp = new AutoFormatRequestProcessor();

	when(req.getHeader("User-Agent")).thenReturn("Safari");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/o=57/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertEquals((0.57f * 0.9f), rp.getRequestedCompressionQuality(req), 0.05f);
    }

    @Test
    public void testCreateRequestProcessor() {
	AutoFormatRequestProcessor rp = new AutoFormatRequestProcessor();
	ImageRequestProcessor rp2 = rp.createRequestProcessor();

	assertNotNull(rp2);
	assertTrue(rp2 instanceof AutoFormatRequestProcessor);
	assertNotSame(rp, rp2);
    }

    @Test
    public void testIsPreferred_0() {
	HttpServletRequest req = mock(HttpServletRequest.class);
	AutoFormatRequestProcessor rp = new AutoFormatRequestProcessor();

	when(req.getHeader("User-Agent")).thenReturn("Safari");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/o=57/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertTrue(rp.isPreferred(req));
    }

    @Test
    public void testIsPreferred_1() {
	HttpServletRequest req = mock(HttpServletRequest.class);
	AutoFormatRequestProcessor rp = new AutoFormatRequestProcessor();

	when(req.getHeader("User-Agent")).thenReturn("Safari");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/f=jpg/o=57/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertFalse(rp.isPreferred(req));
    }

    @Test
    public void testGetTrueCacheKey() {
        HttpServletRequest req = mock(HttpServletRequest.class);
	AutoFormatRequestProcessor rp = new AutoFormatRequestProcessor();       
        
	when(req.getHeader("User-Agent")).thenReturn("Safari");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/o=57/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
        when(req.getParameter("bypass")).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));
        
        rp.setRequest(req);
        
        assertNotNull(rp.getTrueCacheKey());
        assertEquals("/pictura-web/images/o=57/lenna.jpg#o=51;f=jp2", rp.getTrueCacheKey());
    }
    
    @Test
    public void testClientSupportsJP2() {
        HttpServletRequest req = mock(HttpServletRequest.class);
	AutoFormatRequestProcessor rp = new AutoFormatRequestProcessor();       
        
        when(req.getHeader("User-Agent")).thenReturn(null);
	when(req.getHeader("Accept")).thenReturn("image/png; image/jp2");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/o=57/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
        when(req.getParameter("bypass")).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));
        
        rp.setRequest(req);
        
        assertTrue(rp.clientSupportsJP2(req));
        assertFalse(rp.clientSupportsWebP(req));
    }
    
    @Test
    public void testClientSupportsWebP() {
        HttpServletRequest req = mock(HttpServletRequest.class);
	AutoFormatRequestProcessor rp = new AutoFormatRequestProcessor();       
        
        when(req.getHeader("User-Agent")).thenReturn(null);
	when(req.getHeader("Accept")).thenReturn("image/png; image/webp");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/o=57/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
        when(req.getParameter("bypass")).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));
        
        rp.setRequest(req);
        
        assertFalse(rp.clientSupportsJP2(req));
        assertTrue(rp.clientSupportsWebP(req));
    }
    
}
