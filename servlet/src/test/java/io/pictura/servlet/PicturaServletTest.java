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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class PicturaServletTest {

    @Test
    public void testGetServletInfo() {
	System.out.println("getServletInfo");

	PicturaServlet servlet = new PicturaServlet();
	assertEquals(PicturaServlet.PICTURA_INFO, servlet.getServletInfo());
    }

    @Test
    public void testServletVersion() {
	assertEquals(Version.getVersionString(), (new PicturaServlet()).getServletVersion());
    }

    @Test
    public void testServletVendor() {
	assertEquals("", (new PicturaServlet()).getServletVendor());
    }

    @Test
    public void testIsRequestMethodSupported_GET() {
	assertTrue((new PicturaServlet()).isMethodSupported("GET"));
    }

    @Test
    public void testIsRequestMethodSupported_POST() {
	assertFalse((new PicturaServlet()).isMethodSupported("POST"));
    }

    @Test
    public void testIsRequestMethodSupported_HEAD() {
	assertFalse((new PicturaServlet()).isMethodSupported("HEAD"));
    }

    @Test
    public void testIsRequestMethodSupported_DELETE() {
	assertFalse((new PicturaServlet()).isMethodSupported("DELETE"));
    }
    
    @Test
    public void testIsRequestMethodSupported_DELETE2() {
        HttpCache cache = mock(HttpCache.class);
        PicturaServlet servlet = new PicturaServlet();
        servlet.setHttpCache(cache);
        
	assertTrue(servlet.isMethodSupported("DELETE"));
    }

    @Test
    public void testIsRequestMethodSupported_PUT() {
	assertFalse((new PicturaServlet()).isMethodSupported("PUT"));
    }

    @Test
    public void testIsRequestMethodSupported_TRACE() {
	assertFalse((new PicturaServlet()).isMethodSupported("TRACE"));
    }

    @Test
    public void testInit() throws ServletException {
	System.out.println("init");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	// Init with default values
	when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
	when(config.getInitParameter(PicturaServlet.IPARAM_CORE_POOL_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_KEEP_ALIVE_TIME)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_MAX_POOL_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_QUEUE_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_TIMEOUT)).thenReturn(null);

	servlet.init(config);

	assertTrue(servlet.isAlive());

	assertEquals(0, servlet.getCompletedTaskCount());
	assertEquals(0, servlet.getRejectedTaskCount());
    }

    @Test
    public void testInit_1() throws ServletException {
	System.out.println("init_1");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	// Init with default values
	when(config.getInitParameter(PicturaServlet.IPARAM_CORE_POOL_SIZE)).thenReturn("10");
	when(config.getInitParameter(PicturaServlet.IPARAM_KEEP_ALIVE_TIME)).thenReturn("5250");
	when(config.getInitParameter(PicturaServlet.IPARAM_MAX_POOL_SIZE)).thenReturn("20");
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_QUEUE_SIZE)).thenReturn("110");
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_TIMEOUT)).thenReturn("10111");
	when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");

	servlet.init(config);

	assertTrue(servlet.isAlive());

	assertEquals(0, servlet.getCompletedTaskCount());
	assertEquals(0, servlet.getRejectedTaskCount());
    }

    @Test
    public void testInit_2() throws ServletException {
	System.out.println("init_2");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);
	when(config.getInitParameter(PicturaServlet.IPARAM_RESOURCE_LOCATORS))
		.thenReturn("io.pictura.servlet.HttpResourceLocator");
	when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");

	servlet.init(config);
	assertTrue(servlet.isAlive());

	assertEquals(0, servlet.getCompletedTaskCount());
	assertEquals(0, servlet.getRejectedTaskCount());
    }
    
    @Test
    public void testInit_3() throws ServletException {
	System.out.println("init_3");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);
	when(config.getInitParameter(PicturaServlet.IPARAM_RESOURCE_LOCATORS))
		.thenReturn("io.pictura.servlet.HttpResourceLocator");
        when(config.getInitParameter(PicturaServlet.IPARAM_USE_CONTAINER_POOL)).thenReturn("true");

	servlet.init(config);
	assertTrue(servlet.isAlive());

	assertEquals(0, servlet.getCompletedTaskCount());
	assertEquals(0, servlet.getRejectedTaskCount());
        
        assertEquals(-1, servlet.getQueueSize());
        assertEquals(-1, servlet.getActiveCount());
        assertEquals(-1, servlet.getPoolSize());
        
        assertTrue(servlet.useContainerPool());
    }

    @Test
    public void testInit_FileResourceLocator() throws ServletException {
	System.out.println("init_FileResourceLocator");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);
	when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
	when(config.getInitParameter(PicturaServlet.IPARAM_RESOURCE_LOCATORS))
		.thenReturn("io.pictura.servlet.FileResourceLocator");

	servlet.init(config);
	assertTrue(servlet.isAlive());

	assertEquals(0, servlet.getCompletedTaskCount());
	assertEquals(0, servlet.getRejectedTaskCount());
    }

    @Test(expected = ServletException.class)
    public void testInit_ServletException_1() throws ServletException {
	System.out.println("init_ServletException_1");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	// Init with default values
	when(config.getInitParameter(PicturaServlet.IPARAM_CORE_POOL_SIZE)).thenReturn("-1");
	when(config.getInitParameter(PicturaServlet.IPARAM_KEEP_ALIVE_TIME)).thenReturn("5250");
	when(config.getInitParameter(PicturaServlet.IPARAM_MAX_POOL_SIZE)).thenReturn("-2");
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_QUEUE_SIZE)).thenReturn("110");
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_TIMEOUT)).thenReturn("10111");

	servlet.init(config);
    }

    @Test(expected = ServletException.class)
    public void testInit_ServletException_2() throws ServletException {
	System.out.println("init_ServletException_2");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	// Init with default values
	when(config.getInitParameter(PicturaServlet.IPARAM_CORE_POOL_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_KEEP_ALIVE_TIME)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_MAX_POOL_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_QUEUE_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_TIMEOUT)).thenReturn(null);

	when(config.getInitParameter(PicturaServlet.IPARAM_RESOURCE_PATHS)).thenReturn("/blub-<{");

	servlet.init(config);
    }

    @Test(expected = ServletException.class)
    public void testInit_ServletException_3() throws ServletException {
	System.out.println("init_ServletException_3");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	// Init with default values
	when(config.getInitParameter(PicturaServlet.IPARAM_CORE_POOL_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_KEEP_ALIVE_TIME)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_MAX_POOL_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_QUEUE_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_TIMEOUT)).thenReturn(null);

	when(config.getInitParameter(PicturaServlet.IPARAM_RESOURCE_LOCATORS)).thenReturn("com.foobar.MyClass");

	servlet.init(config);
    }

    @Test(expected = ServletException.class)
    public void testInit_ServletException_4() throws ServletException {
	System.out.println("init_ServletException_4");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	// Init with default values
	when(config.getInitParameter(PicturaServlet.IPARAM_CORE_POOL_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_KEEP_ALIVE_TIME)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_MAX_POOL_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_QUEUE_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_TIMEOUT)).thenReturn(null);

	when(config.getInitParameter(PicturaServlet.IPARAM_RESOURCE_LOCATORS)).thenReturn("io.pictura.servlet.ImageRequestProcessor");

	servlet.init(config);
    }

    @Test(expected = ServletException.class)
    public void testInit_ServletException_InvalidStatsPath() throws ServletException {
	System.out.println("init_ServletException_InvalidStatsPath");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	when(config.getInitParameter(PicturaServlet.IPARAM_STATS_ENABLED)).thenReturn("true");
	when(config.getInitParameter(PicturaServlet.IPARAM_STATS_PATH)).thenReturn("stats");

	servlet.init(config);
    }

    @Test(expected = ServletException.class)
    public void testInit_ServletException_InvalidScriptPath() throws ServletException {
	System.out.println("init_ServletException_InvalidScriptPath");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	when(config.getInitParameter(PicturaServlet.IPARAM_SCRIPT_ENABLED)).thenReturn("true");
	when(config.getInitParameter(PicturaServlet.IPARAM_SCRIPT_PATH)).thenReturn("my/js");

	servlet.init(config);
    }

    @Test(expected = ServletException.class)
    public void testInit_ServletException_InvalidPlaceholderPath() throws ServletException {
	System.out.println("init_ServletException_InvalidPlaceholderPath");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	when(config.getInitParameter(PicturaServlet.IPARAM_PLACEHOLDER_PRODUCER_ENABLED)).thenReturn("true");
	when(config.getInitParameter(PicturaServlet.IPARAM_PLACEHOLDER_PRODUCER_PATH)).thenReturn("ip");

	servlet.init(config);
    }

    @Test(expected = ServletException.class)
    public void testInit_ServletException_InvalidResourceLocator() throws ServletException {
	System.out.println("init_ServletException_InvalidResourceLocator");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	when(config.getInitParameter(PicturaServlet.IPARAM_RESOURCE_LOCATORS)).thenReturn("io.pictura.servlet.BotRequestProcessor");

	servlet.init(config);
    }

    @Test(expected = ServletException.class)
    public void testInit_ServletException_InvalidRequestProcessorFactory() throws ServletException {
	System.out.println("init_ServletException_InvalidRequestProcessorFactory");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	when(config.getInitParameter(PicturaServlet.IPARAM_REQUEST_PROCESSOR_FACTORY)).thenReturn("io.pictura.servlet.BotRequestProcessor");

	servlet.init(config);
    }

    @Test(expected = ServletException.class)
    public void testInit_ServletException_InvalidRequestStrategy() throws ServletException {
	System.out.println("init_ServletException_InvalidRequestStrategy");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	when(config.getInitParameter(PicturaServlet.IPARAM_REQUEST_PROCESSOR_STRATEGY)).thenReturn("io.pictura.servlet.ImageRequestProcessor");

	servlet.init(config);
    }

    @Test(expected = ServletException.class)
    public void testInit_ServletException_InvalidResourcePaths() throws ServletException {
	System.out.println("init_ServletException_InvalidResourcePaths");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	when(config.getInitParameter(PicturaServlet.IPARAM_RESOURCE_PATHS)).thenReturn("**{1,/}");

	servlet.init(config);
    }

    @Test(expected = ServletException.class)
    public void testInit_ServletException_InvalidImageIOCacheDir() throws ServletException {
	System.out.println("init_ServletException_InvalidImageIOCacheDir");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	when(config.getInitParameter(PicturaServlet.IPARAM_IMAGEIO_USE_CACHE)).thenReturn("true");
	when(config.getInitParameter(PicturaServlet.IPARAM_IMAGEIO_CACHE_DIR)).thenReturn("/foo");

	servlet.init(config);
    }

    @Test(expected = ServletException.class)
    public void testInit_ServletException_InvalidMaxImageFileSize() throws ServletException {
	System.out.println("init_ServletException_InvalidMaxImageFileSize");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	when(config.getInitParameter(PicturaServlet.IPARAM_MAX_IMAGE_FILE_SIZE)).thenReturn("-2");

	servlet.init(config);
    }

    @Test
    public void testDestroy() throws ServletException {
	System.out.println("destroy");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);

	// Init with default values
	when(config.getInitParameter(PicturaServlet.IPARAM_CORE_POOL_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_KEEP_ALIVE_TIME)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_MAX_POOL_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_QUEUE_SIZE)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_TIMEOUT)).thenReturn(null);
	when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");

	servlet.init(config);
	servlet.destroy();

	assertFalse(servlet.isAlive());
    }

    @Test
    public void testIsAlive() throws ServletException {
	System.out.println("destroy");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);
	when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");

	assertFalse(servlet.isAlive());

	servlet.init(config);
	assertTrue(servlet.isAlive());

	servlet.destroy();
	assertFalse(servlet.isAlive());
    }

    @Test
    public void testCreateRequestProcessor() throws ServletException {
	System.out.println("createRequestProcessor");

	PicturaServlet servlet = new PicturaServlet();
	assertNull(servlet.createRequestProcessor(null));

	HttpServletRequest req = mock(HttpServletRequest.class);
	when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("");
	assertNotNull(servlet.createRequestProcessor(req));
	assertTrue(servlet.createRequestProcessor(req) instanceof ImageRequestProcessor);
    }

    @Test
    public void testCreateRequestProcessor_Custom() throws ServletException {
	System.out.println("createRequestProcessor_Custom");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);
	when(config.getInitParameter(PicturaServlet.IPARAM_REQUEST_PROCESSOR)).thenReturn(
		CustomRequestProcessor.class.getName());
	when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");

	servlet.init(config);

	assertNull(servlet.createRequestProcessor(null));

	HttpServletRequest req = mock(HttpServletRequest.class);
	when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("");
	RequestProcessor rp = servlet.createRequestProcessor(req);

	assertNotNull(rp);
	assertTrue(rp instanceof CustomRequestProcessor);
    }
    
    @Test
    public void testImageRequestProcessorFactory() throws ServletException {
	System.out.println("requestImageProcessorFactory");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);
	when(config.getInitParameter(PicturaServlet.IPARAM_REQUEST_PROCESSOR_FACTORY)).thenReturn(
		CustomRequestProcessorFactory.class.getName());
	when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");

	servlet.init(config);

	assertNull(servlet.createRequestProcessor(null));

	HttpServletRequest req = mock(HttpServletRequest.class);
	when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("");
	RequestProcessor rp = servlet.createRequestProcessor(req);

	assertNotNull(rp);
	assertTrue(rp instanceof CustomRequestProcessorByFactory);
    }
    
    @Test(expected = ServletException.class)
    public void testErrorHandler_Failure() throws ServletException {
	System.out.println("errorHandler_Failure");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
	when(config.getInitParameter(PicturaServlet.IPARAM_ERROR_HANDLER)).thenReturn(
		CustomRequestProcessorFactory.class.getName());

	servlet.init(config);
    }
    
    @Test(expected = ServletException.class)
    public void testURLConnectionFactory_Failure() throws ServletException {
	System.out.println("urlConnectionFactory_Failure");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
	when(config.getInitParameter(PicturaServlet.IPARAM_URL_CONNECTION_FACTORY)).thenReturn(
		CustomRequestProcessorFactory.class.getName());

	servlet.init(config);
    }
    
    @Test(expected = ServletException.class)
    public void testCacheControlHandler_Failure_1() throws ServletException {
	System.out.println("cacheControlHandler_Failure_1");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
	when(config.getInitParameter(PicturaServlet.IPARAM_CACHE_CONTROL_HANDLER)).thenReturn(
		CustomRequestProcessorFactory.class.getName());

	servlet.init(config);
    }
    
    @Test(expected = ServletException.class)
    public void testCacheControlHandler_Failure_2() throws ServletException {
	System.out.println("cacheControlHandler_Failure_2");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);

	when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
	when(config.getInitParameter(PicturaServlet.IPARAM_CACHE_CONTROL_HANDLER)).thenReturn("/foo.xml");

	servlet.init(config);
    }
    
    @Test(expected = ServletException.class)
    public void testImageIOCacheDir_Failure() throws ServletException, IOException {
	System.out.println("imageIOCacheDir_Failure");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);
        
        File f = File.createTempFile("test", ".tmp");
        if (!f.exists()) {
            assertTrue(f.createNewFile());
        }
        f.deleteOnExit();
        
	when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        when(config.getInitParameter(PicturaServlet.IPARAM_IMAGEIO_USE_CACHE)).thenReturn(
		"true");
	when(config.getInitParameter(PicturaServlet.IPARAM_IMAGEIO_CACHE_DIR)).thenReturn(
		f.getAbsolutePath());

	servlet.init(config);
    }
    
    @Test(expected = ServletException.class)
    public void testInitEmbeddedHttpCache() throws ServletException {
        System.out.println("initEmbeddedHttpCache");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        when(config.getInitParameter(PicturaServlet.IPARAM_CACHE_ENABLED)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_CACHE_CLASS)).thenReturn(CustomRequestProcessorFactory.class.getName());
        when(config.getInitParameter(PicturaServlet.IPARAM_CACHE_CAPACITY)).thenReturn(null);
        when(config.getInitParameter(PicturaServlet.IPARAM_CACHE_MAX_ENTRY_SIZE)).thenReturn(null);
        when(config.getInitParameter(PicturaServlet.IPARAM_CACHE_FILE)).thenReturn(null);
        
        servlet.init(config);        
    }       
    
    @Test
    public void testIsDebugEnabled() throws ServletException {
	System.out.println("isDebugEnabled");

	PicturaServlet servlet = new PicturaServlet();
	ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_DEBUG)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        
        servlet.init(config);        
        assertTrue(servlet.isDebugEnabled());        
        servlet.destroy();
    }
    
    @Test
    public void testDoPost() throws Exception {
        PicturaServlet servlet = new PicturaServlet();
        ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_DEBUG)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        
        servlet.init(config);     
        assertTrue(servlet.isAlive());
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        
        servlet.doPost(req, resp);        
        verify(resp).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, null);        
        servlet.destroy();
    }
    
    @Test
    public void testDoPut() throws Exception {
        PicturaServlet servlet = new PicturaServlet();
        ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_DEBUG)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        
        servlet.init(config);     
        assertTrue(servlet.isAlive());
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        
        servlet.doPut(req, resp);        
        verify(resp).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, null);        
        servlet.destroy();
    }
    
    @Test
    public void testDoDelete() throws Exception {
        PicturaServlet servlet = new PicturaServlet();
        ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_DEBUG)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        
        servlet.init(config);     
        assertTrue(servlet.isAlive());
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        
        servlet.doDelete(req, resp);        
        verify(resp).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, null);        
        servlet.destroy();
    }
    
    @Test
    public void testDoHead() throws Exception {
        PicturaServlet servlet = new PicturaServlet();
        ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_DEBUG)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        
        servlet.init(config);     
        assertTrue(servlet.isAlive());
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        
        servlet.doHead(req, resp);        
        verify(resp).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, null);        
        servlet.destroy();
    }
    
    @Test
    public void testDoOptions() throws Exception {
        PicturaServlet servlet = new PicturaServlet();
        ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_DEBUG)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        
        servlet.init(config);     
        assertTrue(servlet.isAlive());
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        
        servlet.doOptions(req, resp);        
        verify(resp).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, null);        
        servlet.destroy();
    }
    
    @Test
    public void testDoTrace() throws Exception {
        PicturaServlet servlet = new PicturaServlet();
        ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_DEBUG)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        
        servlet.init(config);     
        assertTrue(servlet.isAlive());
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        
        servlet.doTrace(req, resp);        
        verify(resp).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, null);        
        servlet.destroy();
    }
    
    @Test(expected = UnavailableException.class)
    public void testDoProcessWhenNotAlive() throws ServletException, IOException {
        PicturaServlet servlet = new PicturaServlet();
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        
        servlet.doGet(req, resp);
    }
    
    @Test
    public void testGetTaskQueue() throws Exception {
        PicturaServlet servlet = new PicturaServlet();
        assertNull(servlet.getTaskQueue());
        
        ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        
        servlet.init(config);     
        assertTrue(servlet.isAlive());
        
        Queue<Runnable> q = servlet.getTaskQueue();
        assertNotNull(q);
        assertEquals(0, q.size());
        
        servlet.destroy();
    }

    @Test
    public void testTryParseInt() {
	System.out.println("tryParseInt");

	assertEquals(0, PicturaServlet.tryParseInt("0", -1));
	assertEquals(-1, PicturaServlet.tryParseInt("-1", 0));
	assertEquals(Integer.MIN_VALUE, PicturaServlet.tryParseInt(String.valueOf(Integer.MIN_VALUE), -1));
	assertEquals(Integer.MAX_VALUE, PicturaServlet.tryParseInt(String.valueOf(Integer.MAX_VALUE), -1));

	assertEquals(-1, PicturaServlet.tryParseInt("", -1));
	assertEquals(-1, PicturaServlet.tryParseInt(".", -1));
    }

    @Test
    public void testTryParseLong() {
	System.out.println("tryParseLong");

	assertEquals(0L, PicturaServlet.tryParseLong("0", -1L));
	assertEquals(-1L, PicturaServlet.tryParseLong("-1", 0L));
	assertEquals(Long.MIN_VALUE, PicturaServlet.tryParseLong(String.valueOf(Long.MIN_VALUE), -1L));
	assertEquals(Long.MAX_VALUE, PicturaServlet.tryParseLong(String.valueOf(Long.MAX_VALUE), -1L));
	assertEquals(Integer.MIN_VALUE, PicturaServlet.tryParseLong(String.valueOf(Integer.MIN_VALUE), -1L));
	assertEquals(Integer.MAX_VALUE, PicturaServlet.tryParseLong(String.valueOf(Integer.MAX_VALUE), -1L));

	assertEquals(-1L, PicturaServlet.tryParseLong("", -1L));
	assertEquals(-1L, PicturaServlet.tryParseLong(".", -1L));
    }

    @Test
    public void testTryParseFloat() {
	System.out.println("tryParseFloat");

	assertEquals(0.f, PicturaServlet.tryParseFloat("0", -1f), 0f);
	assertEquals(-1f, PicturaServlet.tryParseFloat("-1", 0f), 0f);
	assertEquals(1.001f, PicturaServlet.tryParseFloat("1.001", 0f), 0f);

	assertEquals(-1f, PicturaServlet.tryParseFloat("", -1f), 0f);
	assertEquals(-1f, PicturaServlet.tryParseFloat(".", -1f), 0f);
    }
    
    public static class CustomRequestProcessor extends ImageRequestProcessor {

	public CustomRequestProcessor() {
	}

    }        
    
    public static class CustomRequestProcessorFactory implements ImageRequestProcessorFactory {

        @Override
        public ImageRequestProcessor createRequestProcessor(HttpServletRequest req) throws ServletException {
            return new CustomRequestProcessorByFactory();
        }
        
    }
    
    public static class CustomRequestProcessorByFactory extends ImageRequestProcessor {
        
        public CustomRequestProcessorByFactory() {
        }
        
    }    
    
    @Test
    public void testDoProcessGET_BadRequest() throws Exception {
        PicturaServlet servlet = new PicturaServlet();
        ServletConfig config = mock(ServletConfig.class);
	ServletContext context = mock(ServletContext.class);       
        
        when(config.getServletContext()).thenReturn(context);
        when(config.getInitParameter(PicturaServlet.IPARAM_DEBUG)).thenReturn("true");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");
        
        servlet.init(config);     
        assertTrue(servlet.isAlive());
        
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        
        when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("");
        when(req.getRequestURI()).thenReturn("/f=jpg/s=w320");
        when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));        
        when(req.getMethod()).thenReturn("GET");
        when(req.getContentLength()).thenReturn(-1);
        when(req.getAttribute("io.pictura.servlet.DEBUG")).thenReturn(Boolean.FALSE);
        when(req.getAttribute("io.pictura.servlet.HEADER_ADD_TRUE_CACHE_KEY")).thenReturn(Boolean.FALSE);
        when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_FILE_SIZE")).thenReturn(1024L * 1024L);
	when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_RESOLUTION")).thenReturn(1000L * 1000L);
	when(req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE")).thenReturn(1024 * 100);
        when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_POST_CONTENT_LEGTH")).thenReturn(1024 * 1024);
        
        servlet.doGet(req, resp);        
        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "The requested image path cannot be not null or empty.");
        servlet.destroy();
    }
    
}
