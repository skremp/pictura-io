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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class IIORequestProcessorTest {

    @Test
    public void testGetFallbackFormatName() {
	PicturaImageIO.scanForPlugins();

	IIORequestProcessor rp = new IIORequestProcessorMock();
	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getAttribute("io.pictura.servlet.ENABLED_OUTPUT_IMAGE_FORMATS")).thenReturn(new String[]{"jpg", "png", "gif"});

	rp.setRequest(req);
	assertFalse(rp.canWriteFormat("jp2"));
	assertEquals("jpg", rp.getFallbackFormatName("jp2"));
	assertEquals("png", rp.getFallbackFormatName("icns"));
	assertEquals("png", rp.getFallbackFormatName("xxx"));
	assertEquals("gif", rp.getFallbackFormatName("wbmp"));
        assertEquals("jpg", rp.getFallbackFormatName("jpeg"));
        
        when(req.getAttribute("io.pictura.servlet.ENABLED_OUTPUT_IMAGE_FORMATS")).thenReturn(new String[]{"jpeg", "png", "gif"});
        assertEquals("jpeg", rp.getFallbackFormatName("jpg"));

	when(req.getAttribute("io.pictura.servlet.ENABLED_OUTPUT_IMAGE_FORMATS")).thenReturn(new String[]{"png", "gif"});
	assertNull(rp.getFallbackFormatName("jp2"));
        
        assertEquals("png", rp.getFallbackFormatName("png"));
    }

    @Test
    public void testCanReadFormat() {
	PicturaImageIO.scanForPlugins();

	IIORequestProcessor rp = new IIORequestProcessorMock();
	HttpServletRequest req = mock(HttpServletRequest.class);
	rp.setRequest(req);

	when(req.getAttribute("io.pictura.servlet.ENABLED_INPUT_IMAGE_FORMATS")).thenReturn(new String[]{"jpg", "png", "gif"});

	assertFalse(rp.canReadFormat("jp2"));
	assertTrue(rp.canReadFormat("jpg"));
	assertTrue(rp.canReadFormat("gif"));
	assertTrue(rp.canReadFormat("png"));
    }        
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateImageReader_IllegalArgumentException() throws Exception {
        IIORequestProcessor rp = new IIORequestProcessorMock();
        rp.createImageReader(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateImageWriter_IllegalArgumentException() throws Exception {
        IIORequestProcessor rp = new IIORequestProcessorMock();
        rp.createImageWriter(null, null);
    }
    
    @Test
    public void testInterceptors() throws Exception {
        IIORequestProcessor rp = new IIORequestProcessorMock();
        assertFalse(rp.hasImageInterceptor());
        assertFalse(rp.hasParamsInterceptor());
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);                
        
        rp.setRequest(req);
        rp.setResponse(resp);
        
        ParamsInterceptor pi = mock(ParamsInterceptor.class);        
        when(req.getAttribute("io.pictura.servlet.IIO_PARAMS_INTERCEPTOR")).thenReturn(pi);
        assertTrue(rp.hasParamsInterceptor());
        assertSame(pi, rp.getParamsInterceptor());
        
        ImageInterceptor ii = mock(ImageInterceptor.class);        
        when(req.getAttribute("io.pictura.servlet.IIO_IMAGE_INTERCEPTOR")).thenReturn(ii);
        assertTrue(rp.hasImageInterceptor());
        assertSame(ii, rp.getImageInterceptor());
    }
    
    @Test
    public void testIIOWriteParam() throws Exception {
        IIORequestProcessor.IIOWriteParam wp = new IIORequestProcessorMock.IIOWriteParam();
        
        assertNull(wp.getFormatName());
        wp.setFormatName("foo");
        assertEquals("foo", wp.getFormatName());
        
        assertEquals(0f, wp.getCompressionQuality(), 0f);
        wp.setCompressionQuality(0.5f);
        assertEquals(0.5f, wp.getCompressionQuality(), 0.5f);
        
        assertEquals(0, wp.getAnimationDelayTime());
        wp.setAnimationDelayTime(105);
        assertEquals(105, wp.getAnimationDelayTime());
        
        assertFalse(wp.isProgressive());
        wp.setProgressive(true);
        assertTrue(wp.isProgressive());
        
        assertFalse(wp.isAppendMetadata());
        wp.setAppendMetadata(true);
        assertTrue(wp.isAppendMetadata());
        
        assertFalse(wp.isBase64Encoded());
        wp.setBase64Encoded(true);
        assertTrue(wp.isBase64Encoded());
    }

    public static class IIORequestProcessorMock extends IIORequestProcessor {

	public IIORequestProcessorMock() {
	}
	
	@Override
	protected void doProcess(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
	}

    }

}
