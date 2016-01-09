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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class RequestProcessorTest {

    private static String rootPath;

    @BeforeClass
    public static void setUpClass() {
	rootPath = System.getProperty("java.io.tmpdir") + File.separator + "pictura";

	InputStream is = null;
	OutputStream os = null;
	try {
	    File path = new File(rootPath);
	    if (!path.exists()) {
		if (!path.mkdirs()) {
		    fail("Can't create temporary root path \"" + path.getAbsolutePath() + "\".");
		    return;
		}
	    }

	    File tmp = new File(path.getAbsolutePath() + File.separator + "lenna.jpg");
	    if (!tmp.exists()) {
		if (!tmp.createNewFile()) {
		    fail("Can't create temporary resource file \"" + tmp.getAbsolutePath() + "\".");
		    return;
		}
	    }

	    is = FileResourceLocatorTest.class.getResourceAsStream("/lenna.jpg");
	    os = new FileOutputStream(tmp);

	    int read;
	    byte[] bytes = new byte[1024];

	    while ((read = is.read(bytes)) != -1) {
		os.write(bytes, 0, read);
	    }

	    tmp.deleteOnExit();
	    path.deleteOnExit();
	} catch (IOException ex) {
	    fail("Can't create temporary root path and test resource.");
	} finally {
	    if (is != null) {
		try {
		    is.close();
		} catch (IOException ex) {
		}
	    }
	    if (os != null) {
		try {
		    os.close();
		} catch (IOException ex) {
		}
	    }
	}
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_RequestProcessor() throws Exception {
	new RequestProcessor(null) {

	    @Override
	    protected void doProcess(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    }
	};
    }    

    @Test
    public void testGetSetPreProcessor() {
	RequestProcessor rp = new RequestProcessor() {
	    @Override
	    protected void doProcess(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    }
	};

	assertNull(rp.getPreProcessor());

	final Runnable r = new Runnable() {

	    @Override
	    public void run() {
	    }
	};

	rp.setPreProcessor(r);

	assertSame(r, rp.getPreProcessor());
    }

    @Test
    public void testIsMobileDeviceRequest() {
	RequestProcessor rp = new RequestProcessor() {
	    @Override
	    protected void doProcess(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    }
	};

	HttpServletRequest req = mock(HttpServletRequest.class);
	rp.setRequest(req);

	when(req.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
	assertFalse(rp.isMobileDeviceRequest());

	when(req.getHeader("User-Agent")).thenReturn("Mobile-Mozilla/5.0");
	assertTrue(rp.isMobileDeviceRequest());
    }

    @Test
    public void testGetRequest() {
	System.out.println("getRequest");

	HttpServletRequest req = mock(HttpServletRequest.class);
	RequestProcessor rp = new RequestProcessorImpl();
	rp.setRequest(req);

	assertEquals(req, rp.getRequest());
    }

    @Test
    public void testSetRequest() {
	System.out.println("setRequest");

	HttpServletRequest req = mock(HttpServletRequest.class);
	RequestProcessor rp = new RequestProcessorImpl();
	rp.setRequest(req);

	assertEquals(req, rp.getRequest());
    }

    @Test
    public void testGetResponse() {
	System.out.println("getResponse");

	HttpServletResponse resp = mock(HttpServletResponse.class);
	RequestProcessor rp = new RequestProcessorImpl();
	rp.setResponse(resp);

	assertEquals(resp, rp.getResponse());
    }

    @Test
    public void testSetResponse() {
	System.out.println("setResponse");

	HttpServletResponse resp = mock(HttpServletResponse.class);
	RequestProcessor rp = new RequestProcessorImpl();
	rp.setResponse(resp);

	assertEquals(resp, rp.getResponse());
    }

    @Test
    public void testGetAsyncContext() {
	System.out.println("getAsyncContext");

	AsyncContext ac = mock(AsyncContext.class);
	RequestProcessor rp = new RequestProcessorImpl();
	rp.setAsyncContext(ac);

	assertEquals(ac, rp.getAsyncContext());
    }

    @Test
    public void testSetAsyncContext() {
	System.out.println("setAsyncContext");

	AsyncContext ac = mock(AsyncContext.class);
	RequestProcessor rp = new RequestProcessorImpl();
	rp.setAsyncContext(ac);

	assertEquals(ac, rp.getAsyncContext());
    }

    @Test
    public void testIsAsyncSupported() {
	System.out.println("isAsyncSupported");

	AsyncContext ac = mock(AsyncContext.class);

	HttpServletRequest req = mock(HttpServletRequest.class);
	when(req.isAsyncSupported()).thenReturn(Boolean.TRUE);

	when(ac.getRequest()).thenReturn(req);
	when(ac.getResponse()).thenReturn(mock(HttpServletResponse.class));

	RequestProcessor rp = new RequestProcessorImpl();
	rp.setRequest(req);
	rp.setAsyncContext(ac);

	assertTrue(rp.isAsyncSupported());
    }

    @Test
    public void testIsCommitted() throws InterruptedException {
	System.out.println("isCommitted");

	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	HttpServletResponse resp = mock(HttpServletResponse.class);
	rp.setRequest(req);
	rp.setResponse(resp);

	assertFalse(rp.isCommitted());

	Thread t = new Thread(rp);
	t.start();
	t.join();

	assertTrue(rp.isCommitted());
    }

    @Test
    public void testIsCompleted() throws InterruptedException {
	System.out.println("isCompleted");

	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	HttpServletResponse resp = mock(HttpServletResponse.class);
	rp.setRequest(req);
	rp.setResponse(resp);

	assertFalse(rp.isCompleted());

	Thread t = new Thread(rp);
	t.start();
	t.join();

	assertTrue(rp.isCompleted());
    }

    @Test
    public void testIsInterrupted() throws IOException {
	System.out.println("isInterrupted");

	RequestProcessor rp = new RequestProcessorImpl();
	assertFalse(rp.isInterrupted());

	HttpServletRequest req = mock(HttpServletRequest.class);
	HttpServletResponse resp = mock(HttpServletResponse.class);
	rp.setRequest(req);
	rp.setResponse(resp);

	rp.doInterrupt();
	assertTrue(rp.isInterrupted());
    }

    @Test
    public void testDoInterrupt_0args() throws Exception {
	System.out.println("doInterrupt_0args");

	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	HttpServletResponse resp = mock(HttpServletResponse.class);
	rp.setRequest(req);
	rp.setResponse(resp);

	rp.doInterrupt();
	assertTrue(rp.isInterrupted());
    }

    @Test
    public void testDoInterrupt_int() throws Exception {
	System.out.println("doInterrupt_int");

	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	HttpServletResponse resp = mock(HttpServletResponse.class);
	rp.setRequest(req);
	rp.setResponse(resp);

	rp.doInterrupt(HttpServletResponse.SC_NOT_FOUND);
	assertTrue(rp.isInterrupted());
    }

    @Test
    public void testDoInterrupt_int_String() throws Exception {
	System.out.println("doInterrupt_int_String");

	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	HttpServletResponse resp = mock(HttpServletResponse.class);
	rp.setRequest(req);
	rp.setResponse(resp);

	rp.doInterrupt(HttpServletResponse.SC_NOT_FOUND, "File not found.");
	assertTrue(rp.isInterrupted());
    }

    @Test(expected = IllegalStateException.class)
    public void testDoInterrupt_int_String_IllegalStateException() throws Exception {
	System.out.println("doInterrupt_int_String_IllegalStateException");

	RequestProcessor rp = new RequestProcessorImpl();

	Thread t = new Thread(rp);
	t.start();
	t.join();

	rp.doInterrupt(HttpServletResponse.SC_NOT_FOUND, "File not found.");
    }

    @Test
    public void testRun() throws InterruptedException {
	System.out.println("run");

	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	HttpServletResponse resp = mock(HttpServletResponse.class);
	rp.setRequest(req);
	rp.setResponse(resp);

	assertFalse(rp.isInterrupted());
	assertFalse(rp.isCommitted());
	assertFalse(rp.isCompleted());

	Thread t = new Thread(rp);
	t.start();
	t.join();

	assertFalse(rp.isInterrupted());
	assertTrue(rp.isCommitted());
	assertTrue(rp.isCompleted());
    }

    @Test
    public void testRun_Async() throws InterruptedException {
	System.out.println("run_Async");

	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	HttpServletResponse resp = mock(HttpServletResponse.class);
	rp.setRequest(req);
	rp.setResponse(resp);

	AsyncContext ac = mock(AsyncContext.class);
	rp.setAsyncContext(ac);

	assertFalse(rp.isInterrupted());
	assertFalse(rp.isCommitted());
	assertFalse(rp.isCompleted());

	Thread t = new Thread(rp);
	t.start();
	t.join();

	assertFalse(rp.isInterrupted());
	assertTrue(rp.isCommitted());
	assertTrue(rp.isCompleted());
    }

    @Test()
    public void testRun_IllegalStateException0() throws InterruptedException {
	System.out.println("run_IllegalStateException0");

	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	HttpServletResponse resp = mock(HttpServletResponse.class);
	rp.setRequest(req);
	rp.setResponse(resp);

	assertFalse(rp.isInterrupted());
	assertFalse(rp.isCommitted());
	assertFalse(rp.isCompleted());

	Thread t0 = new Thread(rp);
	t0.start();
	t0.join();

	Thread t1 = new Thread(rp);
	t1.start();
	t1.join();

	assertFalse(rp.isInterrupted());
	assertTrue(rp.isCommitted());
	assertTrue(rp.isCompleted());
    }

    @Test()
    public void testRun_IllegalStateException1() throws InterruptedException {
	System.out.println("run_IllegalStateException1");

	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	rp.setRequest(req);

	assertFalse(rp.isInterrupted());
	assertFalse(rp.isCommitted());
	assertFalse(rp.isCompleted());

	Thread t = new Thread(rp);
	t.start();
	t.join();

	assertFalse(rp.isInterrupted());
	assertTrue(rp.isCommitted());
	assertTrue(rp.isCompleted());
    }

    @Test()
    public void testRun_IllegalStateException2() throws InterruptedException {
	System.out.println("run_IllegalStateException2");

	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletResponse resp = mock(HttpServletResponse.class);
	rp.setResponse(resp);

	assertFalse(rp.isInterrupted());
	assertFalse(rp.isCommitted());
	assertFalse(rp.isCompleted());

	Thread t = new Thread(rp);
	t.start();
	t.join();

	assertFalse(rp.isInterrupted());
	assertTrue(rp.isCommitted());
	assertTrue(rp.isCompleted());
    }

    @Test
    public void testGetResourceLocators() {
	System.out.println("getResourceLocators");

	RequestProcessor rp = new RequestProcessorImpl();
	assertNull(rp.getResourceLocators());

	rp.setResourceLocators(new ResourceLocator[]{new FileResourceLocator()});
	assertNotNull(rp.getResourceLocators());
	assertEquals(1, rp.getResourceLocators().length);
    }

    @Test
    public void testIsAllowedResourcePath() {
	System.out.println("isAllowedResourcePath");

	RequestProcessor rp = new RequestProcessorImpl();
	ServletContext ctx = mock(ServletContext.class);
	rp.setResourceLocators(new ResourceLocator[]{new FileResourceLocator(ctx)});

	assertTrue(rp.isAllowedResourcePath("/lenna.jpg"));
	assertTrue(rp.isAllowedResourcePath("lenna.jpg"));
	assertTrue(rp.isAllowedResourcePath("foobar.png"));
	assertTrue(rp.isAllowedResourcePath("/subdir/lenna.jpg"));
	assertTrue(rp.isAllowedResourcePath("http://lenna.org/lenna.jpg"));
	assertTrue(rp.isAllowedResourcePath("https://lenna.org/lenna.jpg"));

	rp.setResourcePaths(new Pattern[]{Pattern.compile("/.{0,}")});
	assertTrue(rp.isAllowedResourcePath("/lenna.jpg"));
	assertFalse(rp.isAllowedResourcePath("lenna.jpg"));
	assertFalse(rp.isAllowedResourcePath("http://lenna.org/lenna.jpg"));
	assertFalse(rp.isAllowedResourcePath("https://lenna.org/lenna.jpg"));
	assertNotNull(rp.getResourcePaths());
	assertEquals(1, rp.getResourcePaths().length);
	assertEquals("/.{0,}", rp.getResourcePaths()[0].toString());

	rp.setResourcePaths(new Pattern[]{Pattern.compile("/images/.{0,}")});
	assertTrue(rp.isAllowedResourcePath("/images/lenna.jpg"));
	assertFalse(rp.isAllowedResourcePath("/lenna.jpg"));
	assertFalse(rp.isAllowedResourcePath("lenna.jpg"));
	assertNotNull(rp.getResourcePaths());
	assertEquals(1, rp.getResourcePaths().length);
	assertEquals("/images/.{0,}", rp.getResourcePaths()[0].toString());

	rp.setResourcePaths(new Pattern[]{Pattern.compile("http://lenna.org/images/.{0,}")});
	assertTrue(rp.isAllowedResourcePath("http://lenna.org/images/lenna.jpg"));
	assertFalse(rp.isAllowedResourcePath("https://lenna.org/images/lenna.jpg"));
	assertFalse(rp.isAllowedResourcePath("/images/lenna.jpg"));
	assertFalse(rp.isAllowedResourcePath("/lenna.jpg"));
	assertFalse(rp.isAllowedResourcePath("lenna.jpg"));
	assertNotNull(rp.getResourcePaths());
	assertEquals(1, rp.getResourcePaths().length);
	assertEquals("http://lenna.org/images/.{0,}", rp.getResourcePaths()[0].toString());
    }

    @Test
    public void testGetResource() throws MalformedURLException {
	System.out.println("getResource");

	RequestProcessor rp = new RequestProcessorImpl();

	ServletContext ctx = mock(ServletContext.class);
	when(ctx.getRealPath("/")).thenReturn(rootPath);
	when(ctx.getRealPath("lenna.jpg")).thenReturn(rootPath + File.separator + "lenna.jpg");

	FileResourceLocator locator = new FileResourceLocator(ctx);
	rp.setResourceLocators(new ResourceLocator[]{locator});

	assertNotNull(rp.getResource("lenna.jpg"));
	assertTrue(rp.getResource("lenna.jpg").toString().startsWith("file:/"));
	assertTrue(rp.getResource("lenna.jpg").toString().endsWith("lenna.jpg"));

	rp.setResourcePaths(new Pattern[]{Pattern.compile("/.{0,}")});
	assertNotNull(rp.getResource("/lenna.jpg"));
	assertNull(rp.getResource("lenna.jpg"));
    }

    @Test
    public void testGetRequestURI() {
	System.out.println("getRequestURI");

	RequestProcessor rp = new RequestProcessorErrorImpl();
	assertNull(rp.getRequestURI());

	HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("test/");
	when(req.getRequestURI()).thenReturn("test/lenna.jpg");
	when(req.getQueryString()).thenReturn("f=png&e=g");

	rp.setRequest(req);

	assertNotNull(rp.getRequestURI());
	assertEquals("test/lenna.jpg?f=png&e=g", rp.getRequestURI());
    }

    @Test
    public void testIsCacheable() {
	System.out.println("isCacheable");

	RequestProcessor rp = new RequestProcessorImpl();
	assertFalse(rp.isCacheable());
    }

    @Test
    public void testGetTimestamp() {
	System.out.println("getTimestamp");

	RequestProcessor rp = new RequestProcessorImpl();
	assertTrue(rp.getTimestamp() > System.currentTimeMillis() - 100);
    } 

    @Test
    public void testGetETag() {
	System.out.println("getETag");

	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/q=foo/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);

	rp.setRequest(req);

	assertEquals("W/\"5ee17e22d1a001932dc0f526d0aa599c\"", rp.getETag());
    }

    @Test
    public void testGetETagByFile() throws Exception {
	System.out.println("getETagByFile");

	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/q=foo/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);

	rp.setRequest(req);

	File f = File.createTempFile("test", ".etag");
	String eTag = rp.getETagByFile(f);

	assertNotNull(eTag);
	assertTrue(eTag.length() > 3);
	assertTrue(eTag.startsWith("W/\""));
    }

    @Test
    public void testGetETag_Null() {
	System.out.println("getETag_Null");

	RequestProcessor rp = new RequestProcessorImpl();
	assertNull(rp.getETag());
    }

    @Test
    public void testGetETag_Bytes() {
	System.out.println("getETag_Bytes");

	RequestProcessor rp = new RequestProcessorImpl();
	assertNull(rp.getETag(null));
	assertNotNull(rp.getETag("foobar".getBytes()));
    }

    @Test
    public void testGetTrueCacheKey_Null() {
	System.out.println("getTrueCacheKey_Null");

	RequestProcessor rp = new RequestProcessorImpl();
	assertNull(rp.getTrueCacheKey());
    }

    @Test
    public void testGetRequestId() throws Exception {
	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	when(req.getContextPath()).thenReturn("/pictura-web");
	when(req.getServletPath()).thenReturn("/images");
	when(req.getRequestURI()).thenReturn("/pictura-web/images/q=foo/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);

	rp.setRequest(req);

	UUID id = rp.getRequestId();
	assertNotNull(id);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetRequestId_IllegalStateException() throws Exception {
	RequestProcessor rp = new RequestProcessorImpl();
	rp.getRequestId();
    }

    @Test
    public void testGetCookie_Null() {
	System.out.println("getCookie_Null");

	RequestProcessor rp = new RequestProcessorImpl();
	assertNull(rp.getCookie(null));
	assertNull(rp.getCookie(""));
	assertNull(rp.getCookie("foo"));
    }

    @Test
    public void testGetCookie() {
	System.out.println("getCookie");

	RequestProcessor rp = new RequestProcessorImpl();

	Cookie c1 = new Cookie("test1", "value1");
	Cookie c2 = new Cookie("test2", "value2");

	Cookie[] cookies = new Cookie[]{c1, c2, null};

	HttpServletRequest req = mock(HttpServletRequest.class);
	when(req.getCookies()).thenReturn(cookies);

	HttpServletResponse resp = mock(HttpServletResponse.class);

	rp.setRequest(req);
	rp.setResponse(resp);

	assertSame(c2, rp.getCookie("test2"));
	assertSame(c1, rp.getCookie("test1"));
    }

    @Test
    public void testGetAttribute() {
	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	when(req.getAttribute("test1")).thenReturn(Boolean.TRUE);
	when(req.getAttribute("test2")).thenReturn(null);

	rp.setRequest(req);

	assertEquals(rp.getAttribute("test1"), Boolean.TRUE);
	assertNull(rp.getAttribute("test2"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetAttribute_IllegalStateException() {
	RequestProcessor rp = new RequestProcessorImpl();
	rp.getAttribute("test");
    }

    @Test
    public void testSetAttribute() {
	RequestProcessor rp = new RequestProcessorImpl();

	HttpServletRequest req = mock(HttpServletRequest.class);
	rp.setRequest(req);

	req.setAttribute("test", "value");
	verify(req).setAttribute("test", "value");
    }

    @Test(expected = IllegalStateException.class)
    public void testSetAttribute_IllegalStateException() {
	RequestProcessor rp = new RequestProcessorImpl();
	rp.setAttribute("test", "value");
    }
    
    @Test(expected = IllegalStateException.class)
    public void testSetPreProcessor_IllegalStateException() {
        RequestProcessor rp = new RequestProcessorImpl();
        rp.run();
        rp.setPreProcessor(new Runnable() {
            @Override
            public void run() {
            }
        });
    }
    
    @Test(expected = IllegalStateException.class)
    public void testSetRequest_IllegalStateException() {
        RequestProcessor rp = new RequestProcessorImpl();
        rp.run();
        rp.setRequest(mock(HttpServletRequest.class));
    }
    
    @Test(expected = IllegalStateException.class)
    public void testSetResponse_IllegalStateException() {
        RequestProcessor rp = new RequestProcessorImpl();
        rp.run();
        rp.setResponse(mock(HttpServletResponse.class));
    }
    
    @Test(expected = IllegalStateException.class)
    public void testAsyncContext_IllegalStateException() {
        RequestProcessor rp = new RequestProcessorImpl();
        rp.run();
        rp.setAsyncContext(mock(AsyncContext.class));
    }
    
    @Test(expected = IllegalStateException.class)
    public void testResourcePaths_IllegalStateException() {
        RequestProcessor rp = new RequestProcessorImpl();
        rp.run();
        rp.setResourcePaths(null);
    }

    private final class RequestProcessorImpl extends RequestProcessor {

	@Override
	public void doProcess(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
	}

    }

    private final class RequestProcessorErrorImpl extends RequestProcessor {

	@Override
	public void doProcess(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
	    throw new Error();
	}

    }

}
