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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class ContextInputStreamTest {
    
    @Test
    public void testRead() throws Exception {
	ServletRequest req = new ServletRequestMock();
	
	final int expLen = 16532;
	
	byte[] data = new byte[expLen];
	Arrays.fill(data, (byte)0);
	
	ContextInputStream is = new ContextInputStream(req, 
		new ByteArrayInputStream(data));
	
	while (is.read() > -1) {
	}
	
	assertEquals(expLen, (long) req.getAttribute("io.pictura.servlet.BYTES_READ"));
    }
    
    @Test
    public void testReadBlock() throws Exception {
	ServletRequest req = new ServletRequestMock();
	
	final int expLen = 16532;
	
	byte[] data = new byte[expLen];
	Arrays.fill(data, (byte)0);
	
	ContextInputStream is = new ContextInputStream(req, 
		new ByteArrayInputStream(data));
	
	byte[] buf = new byte[256];
	while (is.read(buf) > -1) {
	}
	
	assertEquals(expLen, (long) req.getAttribute("io.pictura.servlet.BYTES_READ"));
    }
    
    @SuppressWarnings("deprecation")
    private final static class ServletRequestMock implements ServletRequest {

	private final Map<String, Object> attributes = new HashMap<>();
	
	@Override
	public Object getAttribute(String name) {
	    return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public String getCharacterEncoding() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public int getContentLength() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}
	
	public long getContentLengthLong() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public String getContentType() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public String getParameter(String name) {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public Enumeration<String> getParameterNames() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public String[] getParameterValues(String name) {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public Map<String, String[]> getParameterMap() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public String getProtocol() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public String getScheme() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public String getServerName() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public int getServerPort() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public BufferedReader getReader() throws IOException {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public String getRemoteAddr() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public String getRemoteHost() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public void setAttribute(String name, Object o) {
	    attributes.put(name, o);
	}

	@Override
	public void removeAttribute(String name) {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public Locale getLocale() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public Enumeration<Locale> getLocales() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public boolean isSecure() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override	
	@SuppressWarnings("deprecation")
	public String getRealPath(String path) {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public int getRemotePort() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public String getLocalName() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public String getLocalAddr() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public int getLocalPort() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public ServletContext getServletContext() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public boolean isAsyncStarted() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public boolean isAsyncSupported() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public AsyncContext getAsyncContext() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}

	@Override
	public DispatcherType getDispatcherType() {
	    throw new UnsupportedOperationException("Not supported yet."); 
	}
	
    }
    
}
