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

import static io.pictura.servlet.RequestProcessor.HEADER_EXPIRES;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class HttpCacheServletTest {

    @Test
    public void testSetGetHttpCache() throws Exception {
        HttpCacheServletMock s = new HttpCacheServletMock();
        HttpCache c = HttpCacheServletMock.createDefaultHttpCache(100, 1024 * 1024 * 2);

        s.setHttpCache(c);
        assertSame(c, s.getHttpCache());
    }

    @Test
    public void testCreateCacheRequestProcessor() throws Exception {
        HttpCacheServletMock s = new HttpCacheServletMock();
        s.setHttpCache(HttpCacheServletMock.createDefaultHttpCache(100, 1024 * 1024 * 2));

        final RequestProcessor rpNotCacheable = new RequestProcessor() {
            @Override
            public boolean isCacheable() {
                return false;
            }

            @Override
            protected void doProcess(HttpServletRequest req, HttpServletResponse resp)
                    throws ServletException, IOException {
            }
        };
        RequestProcessor crp = s.createCacheRequestProcessor(rpNotCacheable);
        assertSame(rpNotCacheable, crp);

        final HttpServletRequest req = mock(HttpServletRequest.class);
        final HttpServletResponse resp = mock(HttpServletResponse.class);

        final RequestProcessor rpCacheable = new RequestProcessor() {

            @Override
            HttpServletRequest getRequest() {
                return req;
            }

            @Override
            HttpServletResponse getResponse() {
                return resp;
            }

            @Override
            public boolean isCacheable() {
                return true;
            }

            @Override
            public String getTrueCacheKey() {
                return "test";
            }

            @Override
            protected void doProcess(HttpServletRequest req, HttpServletResponse resp)
                    throws ServletException, IOException {
            }
        };
        crp = s.createCacheRequestProcessor(rpCacheable);
        assertNotNull(crp);
        assertNotSame(rpNotCacheable, crp);

        assertTrue(crp.isCacheable());
        assertNull(crp.getPreProcessor());
    }

    @Test
    public void testInitialHttpCacheSize() throws Exception {
        HttpCacheServlet servlet = new HttpCacheServletMock();
        assertEquals(-1, servlet.getHttpCacheSize());

        servlet.setHttpCache(HttpCacheServletMock.createDefaultHttpCache(100, 1024));
        assertEquals(0, servlet.getHttpCacheSize());
    }

    @Test
    public void testInitialHttpCacheHitRate() throws Exception {
        HttpCacheServlet servlet = new HttpCacheServletMock();
        assertEquals(-1f, servlet.getHttpCacheHitRate(), 0f);

        servlet.setHttpCache(HttpCacheServletMock.createDefaultHttpCache(100, 1024));
        assertEquals(0f, servlet.getHttpCacheHitRate(), 0f);
    }

    @Test
    public void testCreateDefaultHttpCache() throws Exception {
        HttpCache c = HttpCacheServletMock.createDefaultHttpCache(100, 1024);
        assertNotNull(c);

        assertNull(c.get("foo"));
        assertTrue(c.keySet().isEmpty());

        HttpCacheEntry e = new HttpCacheEntry("foo", new byte[512], null, null);
        c.put("foo", e);

        assertSame(e, c.get("foo"));
        assertTrue(c.keySet().size() == 1);
        assertTrue(c.keySet().contains("foo"));

        assertTrue(c.remove("foo"));
        assertTrue(c.keySet().isEmpty());

        assertFalse(c.remove("foo"));

        for (int i = 0; i < 110; i++) {
            c.put("foo" + i, new HttpCacheEntry("foo" + 1, new byte[512], null, null));

            if (i < 100) {
                assertEquals((i + 1), c.keySet().size());
            } else {
                assertEquals(100, c.keySet().size());
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveHttpCacheToStreamStreamNull() throws Exception {
        HttpCacheServlet.saveHttpCacheToStream(null, HttpCacheServlet.createDefaultHttpCache(1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveHttpCacheToStreamCacheNull() throws Exception {
        HttpCacheServlet.saveHttpCacheToStream(new ByteArrayOutputStream(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveHttpCacheToFileFileNull() throws Exception {
        HttpCacheServlet.saveHttpCacheToFile(null, HttpCacheServlet.createDefaultHttpCache(1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveHttpCacheToFileCacheNull() throws Exception {
        File f = File.createTempFile("test", "dat");
        f.deleteOnExit();
        HttpCacheServlet.saveHttpCacheToFile(f, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadHttpCacheFromStreamStreamNull() throws Exception {
        HttpCacheServlet.loadHttpCacheFromStream(null, HttpCacheServlet.createDefaultHttpCache(1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadHttpCacheFromStreamCacheNull() throws Exception {
        HttpCacheServlet.loadHttpCacheFromStream(new ByteArrayInputStream(new byte[1]), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadHttpCacheFromFileFileNull() throws Exception {
        HttpCacheServlet.loadHttpCacheFromFile(null, HttpCacheServlet.createDefaultHttpCache(1, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadHttpCacheFromFileCacheNull() throws Exception {
        File f = File.createTempFile("test", "dat");
        f.deleteOnExit();
        HttpCacheServlet.loadHttpCacheFromFile(f, null);
    }

    @Test
    @Ignore
    public void testSaveLoadHttpCacheToStream() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024 * 64);        
        HttpCacheServlet.saveHttpCacheToStream(os, createDummyCache());
        assertTrue(os.toByteArray().length > (10 * 1024));
        
        HttpCache newCache = HttpCacheServlet.createDefaultHttpCache(100, 1024 * 32);
        assertEquals(0, newCache.keySet().size());
        
        HttpCacheServlet.loadHttpCacheFromStream(new ByteArrayInputStream(os.toByteArray()), newCache);
        assertEquals(10, newCache.keySet().size());
        
        Thread.sleep(1010);
        
        HttpCache newCache2 = HttpCacheServlet.createDefaultHttpCache(100, 1024 * 32);
        assertEquals(0, newCache2.keySet().size());
        
        HttpCacheServlet.loadHttpCacheFromStream(new ByteArrayInputStream(os.toByteArray()), newCache2);
        assertEquals(0, newCache2.keySet().size());
    }    
    
    public HttpCache createDummyCache() {
        HttpCache cache = HttpCacheServlet.createDefaultHttpCache(10, 1024 * 32);
        for (int i = 0; i < 10; i++) {
            cache.put("key-" + i, createDummyCacheEntry("key-" + i, new Date(System.currentTimeMillis() + 1000)));
        }
        return cache;
    }
    
    public HttpCacheEntry createDummyCacheEntry(String key, Date expires) {

        HttpServletResponse resp = mock(HttpServletResponse.class);

        HashMap<String, String> headers = new HashMap<>();
        headers.put(HEADER_EXPIRES, new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US).format(expires));

        Collection<String> headerNames = headers.keySet();

        when(resp.getStatus()).thenReturn(200);
        when(resp.getHeaderNames()).thenReturn(headerNames);
        when(resp.getHeader(HEADER_EXPIRES)).thenReturn(headers.get(HEADER_EXPIRES));

        return new HttpCacheEntry(key, new byte[1024], null, resp);
    }

    public static final class HttpCacheServletMock extends HttpCacheServlet {

        private static final long serialVersionUID = -802317978849685708L;

    }

}
