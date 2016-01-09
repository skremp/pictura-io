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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class MetadataRequestProcessorTest {
    
    @Test
    public void testIsPreferred() throws Exception {
	System.out.println("isPreferred");

	MetadataRequestProcessor rp = new MetadataRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=exif/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

	assertTrue(rp.isPreferred(req));
    }
    
    @Test
    public void testCreateRequestProcessor() throws Exception {
	System.out.println("createRequestProcessor");

	MetadataRequestProcessor rp = new MetadataRequestProcessor();
	assertTrue(rp.createRequestProcessor() instanceof MetadataRequestProcessor);
	assertNotSame(rp, rp.createRequestProcessor());
    }
    
    @Test
    public void testGetRequestParameter() throws Exception {
	System.out.println("getRequestParameter");

	MetadataRequestProcessor rp = new MetadataRequestProcessor();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/f=exif/e=g/images/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));

        rp.setRequest(req);
        
	assertNotNull(rp.getRequestParameter(req, ImageRequestProcessor.QPARAM_NAME_FORMAT_NAME));
        assertNotNull(rp.getRequestParameter(req, ImageRequestProcessor.QPARAM_NAME_IMAGE));
        assertNull(rp.getRequestParameter(req, ImageRequestProcessor.QPARAM_NAME_EFFECT));
    }
    
    @Test
    public void testDoProcess() throws Exception {
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
		    URL url = MetadataRequestProcessorTest.class.getResource("/lenna.jpg");
		    File f = new File(url.toURI());
		    return f.getParentFile().getAbsolutePath();
		} catch (Throwable t) {
		    fail();
		}
		return null;
	    }

	};
        
        MetadataRequestProcessor rp = new MetadataRequestProcessor();

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("");
	when(req.getRequestURI()).thenReturn("/f=exif/lenna.jpg");
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

	String json = bos.toString();
        
        assertNotNull(json);
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
        assertTrue(json.contains("\"ImageWidth\":\"400 pixels\""));
        assertTrue(json.contains("\"ImageHeight\":\"225 pixels\""));
    }
    
}
