package io.pictura.servlet;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpCacheEntryTest {
    
    private static HttpCacheEntry entry;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
	
	HashMap<String, String> headers = new HashMap<>();
	headers.put("Cache-Control", "max-age=60");
	headers.put("Content-Type", "text/plain");
	
	HttpServletRequest req = mock(HttpServletRequest.class);
	HttpServletResponse resp = mock(HttpServletResponse.class);
	
	when(resp.getStatus()).thenReturn(200);
	when(resp.getHeaderNames()).thenReturn(headers.keySet());
	when(resp.getHeader("Cache-Control")).thenReturn(headers.get("Cache-Control"));
	when(resp.getHeader("Content-Type")).thenReturn(headers.get("Content-Type"));        
	
	entry = new HttpCacheEntry("/testkey", new byte[1024], req, resp);
    }
    
    @Test
    public void testGetTimestamp() {
	assertTrue(entry.getTimestamp() <= System.currentTimeMillis());
    }
    
    @Test
    public void testGetHitCount() {
	assertEquals(0, entry.getHitCount());
    }
    
    @Test
    public void testGetKey() {
	assertEquals("/testkey", entry.getKey());
    }
    
    @Test
    public void testGetStatus() {
	assertEquals(200, entry.getStatus());
    }
    
    @Test
    public void testGetExpires() {
	assertTrue(entry.getExpires() > System.currentTimeMillis() &&
		entry.getExpires() < (System.currentTimeMillis() + 65000));
    }
    
    @Test
    public void testIsExpires() {
	assertFalse(entry.isExpired());
    }
    
    @Test
    public void testGetContentLength() {
	assertEquals(1024, entry.getContentLength());
    }
    
    @Test
    public void testGetContentType() {
	assertEquals("text/plain", entry.getContentType());
    }
    
    @Test
    public void testGetContentEncoding() {
	assertNull(entry.getContentEncoding());
    }
    
    @Test
    public void testGetContent() {
	assertNotNull(entry.getContent());
    }
    
    @Test
    public void testGetHeaderNames() {
	assertNotNull(entry.getHeaderNames());
	
	Collection<String> names = entry.getHeaderNames();
	for (String s : names) {
	    assertNotNull(entry.getHeader(s));
	}
    }
    
    @Test
    public void testUserProperty() {
        assertNull(entry.getUserProperty("test"));
        entry.setUserProperty("test", "junit");
        assertEquals("junit", entry.getUserProperty("test"));
    }
    
    @Test
    public void testDatePattern() {
        Date d = new Date(System.currentTimeMillis() + 60000);
        
        long droppedMillis = 1000 * (d.getTime()/ 1000);    
                
        String rfc1036 = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US).format(d);
        System.out.println("RFC1036: " + rfc1036);
        
        String rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).format(d);
        System.out.println("RFC1123: " + rfc1123);
        
        HashMap<String, String> headers = new HashMap<>();
	headers.put("Expires", rfc1036);
	headers.put("Content-Type", "text/plain");
	
	HttpServletRequest req = mock(HttpServletRequest.class);
	HttpServletResponse resp = mock(HttpServletResponse.class);
	
	when(resp.getStatus()).thenReturn(200);
	when(resp.getHeaderNames()).thenReturn(headers.keySet());
	when(resp.getHeader("Expires")).thenReturn(headers.get("Expires"));
	when(resp.getHeader("Content-Type")).thenReturn(headers.get("Content-Type"));        
	
	HttpCacheEntry e = new HttpCacheEntry("/testkey", new byte[1024], req, resp);
        assertEquals(droppedMillis, e.getExpires());
                
        headers.put("Expires", rfc1123);
        e = new HttpCacheEntry("/testkey", new byte[1024], req, resp);
        assertEquals(droppedMillis, e.getExpires());
    }
    
}
