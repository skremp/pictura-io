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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class PicturaPostServletTest {

    @Test
    public void testServletVersion() {
	assertEquals(Version.getVersionString(), (new PicturaPostServlet()).getServletVersion());
    }

    @Test
    public void testServletVendor() {
	assertEquals("", (new PicturaPostServlet()).getServletVendor());
    }

    @Test
    public void testIsRequestMethodSupported_POST() {
	assertTrue((new PicturaPostServlet()).isMethodSupported("POST"));
    }
    
    @Test
    public void testContentLengthRequired() throws Exception {
        PicturaPostServlet servlet = new PicturaPostServlet();
        ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);       
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_DEBUG)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        when(config.getInitParameter(PicturaServlet.IPARAM_ERROR_HANDLER)).thenReturn(DummyErrorHandler.class.getName());
        
        servlet.init(config);     
        assertTrue(servlet.isAlive());
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        
        when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("");
        when(req.getRequestURI()).thenReturn("/f=jpg/s=w320");
        when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));        
        when(req.getMethod()).thenReturn("POST");
        when(req.getContentLength()).thenReturn(-1);
        when(req.getAttribute("io.pictura.servlet.DEBUG")).thenReturn(Boolean.FALSE);
        when(req.getAttribute("io.pictura.servlet.HEADER_ADD_TRUE_CACHE_KEY")).thenReturn(Boolean.FALSE);
        when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_FILE_SIZE")).thenReturn(1024L * 1024L);
	when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_RESOLUTION")).thenReturn(1000L * 1000L);
	when(req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE")).thenReturn(1024 * 100);
        when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_POST_CONTENT_LEGTH")).thenReturn(1024 * 1024);
        
        servlet.doPost(req, resp);        
        verify(resp).sendError(HttpServletResponse.SC_LENGTH_REQUIRED, null);
        servlet.destroy();
    }
        
    @Test
    public void testRequestEntityTooLarge() throws Exception {
        PicturaPostServlet servlet = new PicturaPostServlet();
        ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);       
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_DEBUG)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        when(config.getInitParameter(PicturaServlet.IPARAM_ERROR_HANDLER)).thenReturn(DummyErrorHandler.class.getName());
        
        servlet.init(config);     
        assertTrue(servlet.isAlive());
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        
        when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("");
        when(req.getRequestURI()).thenReturn("/f=jpg/s=w320");
        when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));        
        when(req.getMethod()).thenReturn("POST");
        when(req.getContentLength()).thenReturn(1024 * 1024 + 1);
        when(req.getAttribute("io.pictura.servlet.DEBUG")).thenReturn(Boolean.FALSE);
        when(req.getAttribute("io.pictura.servlet.HEADER_ADD_TRUE_CACHE_KEY")).thenReturn(Boolean.FALSE);
        when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_FILE_SIZE")).thenReturn(1024L * 1024L);
	when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_RESOLUTION")).thenReturn(1000L * 1000L);
	when(req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE")).thenReturn(1024 * 100);
        when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_POST_CONTENT_LEGTH")).thenReturn(1024 * 1024);
        
        servlet.doPost(req, resp);        
        verify(resp).sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, null);
        servlet.destroy();
    }
    
    @Test
    public void testUnsupportedMediaType_1() throws Exception {
        PicturaPostServlet servlet = new PicturaPostServlet();
        ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);       
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_DEBUG)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        when(config.getInitParameter(PicturaServlet.IPARAM_ERROR_HANDLER)).thenReturn(DummyErrorHandler.class.getName());
        
        servlet.init(config);     
        assertTrue(servlet.isAlive());
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        
        when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("");
        when(req.getRequestURI()).thenReturn("/f=jpg/s=w320");
        when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));        
        when(req.getMethod()).thenReturn("POST");
        when(req.getContentLength()).thenReturn(1024);
        when(req.getContentType()).thenReturn("text/css");
        when(req.getAttribute("io.pictura.servlet.DEBUG")).thenReturn(Boolean.FALSE);
        when(req.getAttribute("io.pictura.servlet.HEADER_ADD_TRUE_CACHE_KEY")).thenReturn(Boolean.FALSE);
        when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_FILE_SIZE")).thenReturn(1024L * 1024L);
	when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_RESOLUTION")).thenReturn(1000L * 1000L);
	when(req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE")).thenReturn(1024 * 100);
        when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_POST_CONTENT_LEGTH")).thenReturn(1024 * 1024);
        
        servlet.doPost(req, resp);        
        verify(resp).sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported media or content type was not set");
        servlet.destroy();
    }
    
    @Test
    public void testUnsupportedMediaType_2() throws Exception {
        PicturaPostServlet servlet = new PicturaPostServlet();
        ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);       
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_DEBUG)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        when(config.getInitParameter(PicturaServlet.IPARAM_ERROR_HANDLER)).thenReturn(DummyErrorHandler.class.getName());
        
        servlet.init(config);     
        assertTrue(servlet.isAlive());
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        
        when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("");
        when(req.getRequestURI()).thenReturn("/f=jpg/s=w320");
        when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));        
        when(req.getMethod()).thenReturn("POST");
        when(req.getContentLength()).thenReturn(1024);
        when(req.getContentType()).thenReturn("image/webp2");
        when(req.getAttribute("io.pictura.servlet.DEBUG")).thenReturn(Boolean.FALSE);
        when(req.getAttribute("io.pictura.servlet.HEADER_ADD_TRUE_CACHE_KEY")).thenReturn(Boolean.FALSE);
        when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_FILE_SIZE")).thenReturn(1024L * 1024L);
	when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_RESOLUTION")).thenReturn(1000L * 1000L);
	when(req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE")).thenReturn(1024 * 100);
        when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_POST_CONTENT_LEGTH")).thenReturn(1024 * 1024);
        
        servlet.doPost(req, resp);        
        verify(resp).sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported media or content type was not set");
        servlet.destroy();
    }
    
    @Test
    public void testDoProcessPOST() throws Exception {
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
        
        final InputStream content = PicturaPostServletTest.class.getResourceAsStream("/lenna.png");
        final ServletInputStream sis = new ServletInputStream() {
                       
            @Override
            public int read() throws IOException {
                return content.read();
            }
        };
        
        PicturaPostServlet servlet = new PicturaPostServlet();
        ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);       
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_DEBUG)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        when(config.getInitParameter(PicturaServlet.IPARAM_ERROR_HANDLER)).thenReturn(DummyErrorHandler.class.getName());
        
        servlet.init(config);     
        assertTrue(servlet.isAlive());
        
        HttpServletRequest req = mock(HttpServletRequest.class);              
        
        when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("");
        when(req.getRequestURI()).thenReturn("/f=jpg/s=w320");
        when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));        
        when(req.getMethod()).thenReturn("POST");
        when(req.getContentLength()).thenReturn(165029);
        when(req.getContentType()).thenReturn("image/png");
        when(req.getInputStream()).thenReturn(sis);
        when(req.getAttribute("io.pictura.servlet.DEBUG")).thenReturn(Boolean.FALSE);
        when(req.getAttribute("io.pictura.servlet.HEADER_ADD_TRUE_CACHE_KEY")).thenReturn(Boolean.FALSE);
        when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_FILE_SIZE")).thenReturn(1024L * 1024L);
	when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_RESOLUTION")).thenReturn(1000L * 1000L);
	when(req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE")).thenReturn(1024 * 100);
        when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_POST_CONTENT_LEGTH")).thenReturn(1024 * 1024);
        
        HttpServletResponse resp = mock(HttpServletResponse.class);  
        when(resp.getOutputStream()).thenReturn(sos);
        
        servlet.doPost(req, resp);        
        
        byte[] img = bos.toByteArray();
        assertNotNull(img);
        
        BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(img));
        assertNotNull(bimg);
        assertEquals(320, bimg.getWidth());
        
        servlet.destroy();
    }
    
    public static class DummyErrorHandler implements ErrorHandler {

        @Override
        public boolean doHandle(HttpServletRequest req, HttpServletResponse resp, 
                int sc, String msg) throws IOException {
            return false;
        }
        
    }
    
}
