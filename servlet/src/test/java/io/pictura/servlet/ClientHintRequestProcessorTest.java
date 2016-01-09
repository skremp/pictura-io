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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class ClientHintRequestProcessorTest {

    @Test
    public void testGetRequestedScaleWidth_1() throws Exception {
	System.out.println("getRequestedScaleWidth_1");

	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getHeader("Width")).thenReturn("230");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertEquals(new Integer(230), rp.getRequestedScaleWidth(req));
    }

    @Test
    public void testGetRequestedScaleWidth_2() throws Exception {
	System.out.println("getRequestedScaleWidth_2");

	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getHeader("Viewport-Width")).thenReturn("320");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertEquals(new Integer(320), rp.getRequestedScaleWidth(req));
    }

    @Test
    public void testGetRequestedScaleWidth_3() throws Exception {
	System.out.println("getRequestedScaleWidth_3");

	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getHeader("Width")).thenReturn("230");
	when(req.getHeader("Viewport-Width")).thenReturn("320");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertEquals(new Integer(230), rp.getRequestedScaleWidth(req));
    }

    @Test
    public void testGetRequestedScaleWidth_4() throws Exception {
	System.out.println("getRequestedScaleWidth_4");

	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getHeader("Width")).thenReturn("230");
	when(req.getHeader("Viewport-Width")).thenReturn("320");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/s=w222/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertEquals(new Integer(222), rp.getRequestedScaleWidth(req));
    }
    
    @Test
    public void testGetRequestedScaleWidth_5() throws Exception {
	System.out.println("getRequestedScaleWidth_5");

	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

        when(req.getCookies()).thenReturn(new Cookie[] {
	    new Cookie("__picturaio__", "dpr=1.2,dw=1920,dh=925")
	});
        
        rp.setRequest(req);
        
	assertEquals(new Integer(1920), rp.getRequestedScaleWidth(req));
    }

    @Test
    public void testGetRequestedScalePixelRatio_1() throws Exception {
	System.out.println("getRequestedScalePixelRatio_1");

	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getHeader("DPR")).thenReturn("1.25");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/s=w222/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertEquals(new Float(1.25f), rp.getRequestedScalePixelRatio(req));
    }

    @Test
    public void testGetRequestedScalePixelRatio_2() throws Exception {
	System.out.println("getRequestedScalePixelRatio_2");

	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getHeader("DPR")).thenReturn("1.2T5");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/s=w222/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertEquals(new Float(1f), rp.getRequestedScalePixelRatio(req));
    }

    @Test
    public void testGetRequestedScalePixelRatio_3() throws Exception {
	System.out.println("getRequestedScalePixelRatio_3");

	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getHeader("DPR")).thenReturn("1.25");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/s=w222,dpr1.5/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertEquals(new Float(1.5f), rp.getRequestedScalePixelRatio(req));
    }

    @Test
    public void testIsPreferred_1() throws Exception {
	System.out.println("isPreferred_1");

	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getHeader("DPR")).thenReturn("1.25");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/s=w222,dpr1.5/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertTrue(rp.isPreferred(req));
    }

    @Test
    public void testIsPreferred_2() throws Exception {
	System.out.println("isPreferred_2");

	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getHeader("Viewport-Width")).thenReturn("1.25");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/s=w222,dpr1.5/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertTrue(rp.isPreferred(req));
    }

    @Test
    public void testIsPreferred_3() throws Exception {
	System.out.println("isPreferred_3");

	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getHeader("Width")).thenReturn("1.25");
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/s=w222,dpr1.5/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertTrue(rp.isPreferred(req));
    }

    @Test
    public void testIsPreferred_4() throws Exception {
	System.out.println("isPreferred_4");

	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/s=w222,dpr1.5/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertFalse(rp.isPreferred(req));
    }

    @Test
    public void testCreateRequestProcessor() {
	ImageRequestStrategy s = new ClientHintRequestProcessor();
	assertNotNull(s.createRequestProcessor());
	assertTrue(s.createRequestProcessor() instanceof ClientHintRequestProcessor);
    }
    
    @Test
    public void testClientHintCookie_0() throws Exception {	
	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);		
	
        rp.setRequest(req);
        
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));
	when(req.getCookies()).thenReturn(new Cookie[] {
	    new Cookie("__picturaio__", "dpr=1.2,dw=1920,dh=925,webp=0,jp2=1")
	});
	
                
	assertTrue(rp.isPreferred(req));
	
        Cookie c = rp.getClientHintCookie(req);
        assertNotNull(c);
        
        assertEquals(null, c.getComment());
        assertEquals(null, c.getDomain());        
        assertEquals(null, c.getPath());
        
        assertEquals(-1, c.getMaxAge());
        assertEquals("__picturaio__", c.getName());
        assertEquals("dpr=1.2,dw=1920,dh=925,webp=0,jp2=1", c.getValue());
        assertEquals(0, c.getVersion());
        
        assertFalse(c.getSecure());
        assertFalse(c.isHttpOnly());
        
        c.setSecure(true);
        assertTrue(c.getSecure());
        
        c.setHttpOnly(true);
        assertTrue(c.isHttpOnly());
        
        c.setComment("Test");
        assertEquals("Test", c.getComment());
        
        c.setVersion(1);
        assertEquals(1, c.getVersion());
        
        c.setPath("/");
        assertEquals("/", c.getPath());
        
        c.setValue("");
        assertEquals("", c.getValue());
    }
    
    @Test
    public void testClientHintCookie_1() throws Exception {	
	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);		
	
	rp.setRequest(req);
	
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/s=w222/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));
	when(req.getCookies()).thenReturn(new Cookie[] {
	    new Cookie("__picturaio__", "dpr=1.2,dw=1920,dh=925")
	});
	
	assertTrue(rp.isPreferred(req));
	assertEquals(1.2f, rp.getRequestedScalePixelRatio(req), 0.01f);
	assertEquals(new Integer(222), rp.getRequestedScaleWidth(req));                
    }

    @Test
    public void testClientHintCookie_2() throws Exception {	
	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);		
	
	rp.setRequest(req);
	
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/s=w222/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));
	when(req.getCookies()).thenReturn(new Cookie[] {
	    new Cookie("__picturaio__", "dpr=1.2,dw=1920,dh=925,jp2=1,webp=0")
	});
	
	assertTrue(rp.clientSupportsJP2(req));
        assertFalse(rp.clientSupportsWebP(req));
    }
    
    @Test
    public void testGetTrueCacheKey() throws Exception {
	System.out.println("getTrueCacheKey");

	ClientHintRequestProcessor rp = new ClientHintRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

        when(req.getCookies()).thenReturn(new Cookie[] {
	    new Cookie("__picturaio__", "dpr=1.2,dw=1920,dh=925")
	});
        
        rp.setRequest(req);
        
	assertEquals("/pictura-web/images/lenna.jpg#sw=1920;dpr=1.2", rp.getTrueCacheKey());
    }
    
}
