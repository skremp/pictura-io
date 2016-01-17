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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the basic implementation of an request processor which will be
 * executed for any {@link PicturaServlet} request.
 *
 * @see Runnable
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public abstract class RequestProcessor implements Runnable, Cacheable {

    private static final Log LOG = Log.getLog(RequestProcessor.class);
    
    // HTTP header field names
    protected static final String HEADER_IFMODSINCE = "If-Modified-Since";
    protected static final String HEADER_LASTMOD = "Last-Modified";
    protected static final String HEADER_IFNONMATCH = "If-None-Match";
    protected static final String HEADER_ETAG = "ETag";
    protected static final String HEADER_DATE = "Date";
    protected static final String HEADER_EXPIRES = "Expires";
    protected static final String HEADER_PRAGMA = "Pragma";
    protected static final String HEADER_CONNECTION = "Connection";
    protected static final String HEADER_CACHECONTROL = "Cache-Control";
    protected static final String HEADER_CONTACCEPT = "Content-Accept";
    protected static final String HEADER_CONTENC = "Content-Encoding";
    protected static final String HEADER_CONTLANG = "Content-Language";
    protected static final String HEADER_CONTLOC = "Content-Location";
    protected static final String HEADER_CONTTYPE = "Content-Type";
    protected static final String HEADER_CONTLEN = "Content-Length";
    protected static final String HEADER_LOCATION = "Location";
    protected static final String HEADER_COOKIE = "Cookie";
    protected static final String HEADER_VARY = "Vary";
    protected static final String HEADER_ACCEPT = "Accept";
    protected static final String HEADER_ACCEPTENC = "Accept-Encoding";
    protected static final String HEADER_ALLOW = "Allow";
    protected static final String HEADER_USERAGENT = "User-Agent";

    // The associated servlet request and response object
    private HttpServletRequest req;
    private HttpServletResponse resp;

    // Pre-process stuff
    private Runnable preProcessor;

    // State flags
    private boolean committed;
    private boolean completed;
    private boolean interrupted;

    // Context if the processor is running async
    private AsyncContext asyncCtx;

    private Pattern[] resPaths;
    private ResourceLocator[] resLocators;
    
    // A calculated "true" cache key for the response
    private String trueCacheKey;

    // Processor timestamps
    private long duration;
    private long runtime;

    private final long timestamp;

    // A unique request ID (for debugging purposes)
    private UUID requestId;

    /**
     * Creates a new <code>RequestProcessor</code>.
     */
    public RequestProcessor() {
	this.duration = -1;
	this.timestamp = System.currentTimeMillis();
    }

    /**
     * Creates a new <code>RequestProcessor</code> based on the specified
     * "origin" request processor.
     *
     * @param rp The base request processor.
     */
    protected RequestProcessor(RequestProcessor rp) {
	if (rp == null) {
	    throw new IllegalArgumentException("Origin request processor must be not null");
	}

	this.req = rp.getRequest();
	this.resp = rp.getResponse();

	// Optional
	this.asyncCtx = rp.getAsyncContext();
	this.resPaths = rp.getResourcePaths();
	this.resLocators = rp.getResourceLocators();
	this.preProcessor = rp.getPreProcessor();

	this.duration = rp.duration;
	this.timestamp = rp.timestamp;
	this.committed = rp.committed;
	this.completed = rp.completed;
	this.interrupted = rp.interrupted;
	this.requestId = rp.requestId;
    }

    Runnable getPreProcessor() {
	return preProcessor;
    }

    void setPreProcessor(Runnable r) {
	if (committed) {
	    throw new IllegalStateException("setPreProcessor() after comitted");
	}
	this.preProcessor = r;
    }

    HttpServletRequest getRequest() {
	return req;
    }

    void setRequest(HttpServletRequest req) {
	if (committed) {
	    throw new IllegalStateException("setPreProcessor() after comitted");
	}
	this.req = req;
    }

    HttpServletResponse getResponse() {
	return resp;
    }

    void setResponse(HttpServletResponse resp) {
	if (committed) {
	    throw new IllegalStateException("setPreProcessor() after comitted");
	}
	this.resp = resp;
    }

    AsyncContext getAsyncContext() {
	return asyncCtx;
    }

    void setAsyncContext(AsyncContext asyncCtx) {
	if (committed) {
	    throw new IllegalStateException("setPreProcessor() after comitted");
	}
	this.asyncCtx = asyncCtx;
	setRequest((HttpServletRequest) asyncCtx.getRequest());
	setResponse((HttpServletResponse) asyncCtx.getResponse());
    }

    Pattern[] getResourcePaths() {
	return resPaths;
    }

    void setResourcePaths(Pattern[] resPaths) {
	if (committed) {
	    throw new IllegalStateException("setPreProcessor() after comitted");
	}
	this.resPaths = resPaths;
    }    

    long getDuration() {
	return duration;
    }

    long getTimestamp() {
	return timestamp;
    }

    /**
     * Tests whether the general debug parameter was set to <code>true</code> or
     * whether the optional debug query parameter (URL) is set to
     * <code>true</code>.
     *
     * @return <code>true</code> if debugging is enabled; otherwise
     * <code>false</code>.
     */
    public final boolean isDebugEnabled() {
	return req != null && req.getParameter("debug") != null
		|| (req.getAttribute("io.pictura.servlet.DEBUG") instanceof Boolean
		&& (boolean) req.getAttribute("io.pictura.servlet.DEBUG"));
    }

    /**
     * Checks if this request processor could execute in an asynchronous way.
     *
     * <p>
     * Asynchronous operation is disabled for this processor if this request
     * (servlet request) is within the scope of a filter or servlet that has not
     * been annotated or flagged in the deployment descriptor as being able to
     * support asynchronous handling.
     *
     * @return <code>true</code> if asynchronous operation is supported;
     * otherwise <code>false</code>.
     */
    public final boolean isAsyncSupported() {
	return asyncCtx != null && req.isAsyncSupported();
    }

    /**
     * Checks if this request processor is already committed.
     *
     * @return <code>true</code> if this request processor is already committed;
     * otherwise <code>false</code>.
     *
     * @see #isCompleted()
     * @see #isInterrupted()
     */
    public final boolean isCommitted() {
	return committed;
    }

    /**
     * Checks if this request processor is already completed.
     *
     * @return <code>true</code> if this request processor is already completed;
     * otherwise <code>false</code>.
     *
     * @see #isCommitted()
     * @see #isInterrupted()
     */
    public final boolean isCompleted() {
	return completed;
    }

    /**
     * Checks if this request processor was interrupted.
     *
     * <p>
     * A request processor is interrupted (normally) after an error was send to
     * the client.
     *
     * @return <code>true</code> if this request processor was interrupted;
     * otherwise <code>false</code>.
     *
     * @see #isCommitted()
     * @see #isCompleted()
     */
    public final boolean isInterrupted() {
	return interrupted;
    }

    /**
     * Handles an interrupt on this request processor and sends an error
     * response to the client using the <code>Internal Server Error</code>
     * status code and clears the buffer (response).
     *
     * <p>
     * If the related HTTP response has already been committed, this method
     * throws an IllegalStateException.
     *
     * @throws IOException If an input or output exception occurs.
     * @throws IllegalStateException If the response was committed before this
     * method call.
     *
     * @see #doInterrupt(int)
     * @see #doInterrupt(int, java.lang.String)
     * @see #doInterrupt(int, java.lang.String, java.lang.Throwable)
     */
    public final void doInterrupt() throws IOException, IllegalStateException {
	this.doInterrupt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
    }

    /**
     * Handles an interrupt on this request processor and sends an error
     * response to the client using the specified status code and clears the
     * buffer (response).
     *
     * <p>
     * If the related HTTP response has already been committed, this method
     * throws an IllegalStateException.
     *
     * @param sc The error status code.
     *
     * @throws IOException If an input or output exception occurs.
     * @throws IllegalStateException If the response was committed before this
     * method call.
     *
     * @see #doInterrupt()
     * @see #doInterrupt(int, java.lang.String)
     * @see #doInterrupt(int, java.lang.String, java.lang.Throwable)
     */
    public final void doInterrupt(int sc) throws IOException, IllegalStateException {
	this.doInterrupt(sc, null);
    }

    /**
     * Handles an interrupt on this request processor and sends an error
     * response to the client using the specified status code and message and
     * clears the buffer (response).
     *
     * <p>
     * If the related HTTP response has already been committed, this method
     * throws an IllegalStateException.
     *
     * @param sc The error status code.
     * @param msg The error message.
     *
     * @throws IOException If an input or output exception occurs.
     * @throws IllegalStateException If the response was committed before this
     * method call.
     *
     * @see #doInterrupt()
     * @see #doInterrupt(int)
     * @see #doInterrupt(int, java.lang.String, java.lang.Throwable)
     */
    public final void doInterrupt(int sc, String msg)
	    throws IOException, IllegalStateException {
	this.doInterrupt(sc, msg, null);
    }

    /**
     * Handles an interrupt on this request processor and sends an error
     * response to the client using the specified status code and message and
     * clears the buffer (response).
     *
     * <p>
     * If the related HTTP response has already been committed, this method
     * throws an IllegalStateException.
     *
     * <p>
     * If the specified cause is an {@link IOException} this method will not
     * send any data back to the client.
     *
     * @param sc The error status code.
     * @param msg The error message.
     * @param e The associated exception which is the cause of this interrupt
     * method call.
     *
     * @throws IOException If an input or output exception occurs.
     * @throws IllegalStateException If the response was committed before this
     * method call.
     *
     * @see #doInterrupt()
     * @see #doInterrupt(int)
     * @see #doInterrupt(int, java.lang.String, java.lang.Throwable)
     */
    public void doInterrupt(int sc, String msg, Throwable e)
	    throws IOException, IllegalStateException {

	// Do nothing if the processor was already interrupted
	if (interrupted) {
	    return;
	}
	interrupted = true;

	// On an IO error it makes no sense to try to send a message to the 
	// client because it is not longer possible to send data to the client.
	if (e instanceof IOException) {
	    return;
	}

	if (isCompleted()) {
	    throw new IllegalStateException("Request already completed.", e);
	}

	HttpServletResponse response = getResponse();
	if (response == null || response.isCommitted()) {
	    throw new IllegalStateException("Response already committed.", e);
	}

	if (sc == HttpServletResponse.SC_NOT_MODIFIED) {
	    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
	    response.flushBuffer();
	    return;
	}

	response.reset();

	if (msg != null) {
	    response.sendError(sc, msg);
	} else {
	    response.sendError(sc);
	}
    }

    /**
     * Returns the full request URI from this request including optional query
     * strings.
     *
     * @return The request URI.
     */
    public final String getRequestURI() {
	if (getRequest() != null) {
	    HttpServletRequest request = getRequest();

	    String ctxPath = request.getContextPath();
	    String uri = request.getRequestURI();

	    if (uri.startsWith("/" + ctxPath)) {
		uri = uri.replaceFirst(ctxPath, "");
	    }

	    String reqUri = uri;
	    String reqQuery = request.getQueryString();

	    StringBuilder sb = new StringBuilder();
	    sb.append(reqUri);
	    if (reqQuery != null) {
		sb.append("?").append(reqQuery);
	    }
	    return sb.toString();
	}
	return null;
    }

    /**
     * Returns all resource locators which are associated with this request. The
     * request processor will use this resource locators to handle all resource
     * lookups in {@link #getResource(java.lang.String)}, too.
     *
     * @return The resource locators for this request.
     *
     * @see ResourceLocator
     */
    public final ResourceLocator[] getResourceLocators() {
	if (resLocators != null) {
	    return resLocators;
	}
	return null;
    }
    
    /**
     * Sets the resource locators are used by this request processor to
     * determine the URL of resources.
     * 
     * @param resLocators The defined resource locators for this processor
     * or <code>null</code> if there are no resource locators are set.
     * 
     * @throws IllegalStateException if the method was called after the
     * request processor was comitted.
     */
    public final void setResourceLocators(ResourceLocator[] resLocators) {
	if (committed) {
	    throw new IllegalStateException("setPreProcessor() after comitted");
	}
	this.resLocators = resLocators;
    }

    /**
     * Tests whether the requested resource path is allowed to request. To test
     * the method will use the defined resource path restrictions from the
     * servlet init parameter.
     *
     * @param path The path to test.
     * @return <code>true</code> if it is allowed to access the specified
     * resource path; otherwise <code>false</code>.
     */
    protected final boolean isAllowedResourcePath(String path) {
	if (resPaths != null && resPaths.length > 0) {
	    Matcher m;
	    for (Pattern p : resPaths) {
		m = p.matcher(path);
		if (m.matches()) {
		    return true;
		}
	    }
	    return false;
	}
	return true;
    }

    /**
     * Returns an URL to the resource that is mapped to the given path.
     * <p>
     * This method returns <tt>null</tt> if no resource is mapped to the
     * pathname or if it is not allowed to access the resource.
     *
     * @param path A <code>String</code> specifying the path to the resource.
     * @return The resource located at the named path, or <tt>null</tt> if there
     * is no resource at that path.
     * @throws MalformedURLException if the pathname is not given in the correct
     * form.
     *
     * @see URL
     * @see #getResourceLocators()
     */
    public URL getResource(String path) throws MalformedURLException {
	if (!isAllowedResourcePath(path)) {
	    return null;
	}
	URL resource = null;
	ResourceLocator[] locators = getResourceLocators();
	if (locators != null) {
	    for (ResourceLocator rl : locators) {
		resource = rl.getResource(path);		
		if (resource != null) {
		    break;
		}
	    }
	}
	return resource;
    }

    /**
     * Gets the first cookie with the specified name from this request if
     * present.
     *
     * @param name The cookie name.
     * @return The first cookie if present; otherwise <code>null</code>.
     *
     * @see Cookie
     */
    public final Cookie getCookie(String name) {
	if (req != null && name != null) {
	    Cookie[] cookies = req.getCookies();
	    if (cookies != null && cookies.length > 0) {
		for (Cookie c : cookies) {
		    if (c == null || !name.equals(c.getName())) {
			continue;
		    }
		    return c;
		}
	    }
	}
	return null;
    }

    /**
     * Tests whether or not the request comes from a mobile device. To make the
     * decision, this method reads the user agent string from the associated
     * servlet request. If there is no user agent string is present, the method
     * returns <code>false</code> as default.
     *
     * @return <code>true</code> if the request is from a mobile device.
     */
    public boolean isMobileDeviceRequest() {
	if (req != null) {
	    final String ua = req.getHeader(HEADER_USERAGENT);
	    return (ua != null && ua.contains("Mobi"));
	}
	return false;
    }

    /**
     * Calculates a weight ETag based on the true cache key for this request.
     *
     * @return The ETag or <code>null</code> if the true cache key from this
     * request is <code>null</code>, too.
     *
     * @see #getETag(byte[])
     */
    public String getETag() {
	String tck = getTrueCacheKey();
	if (tck != null) {
	    return getETag(tck.getBytes());
	}
	return null;
    }

    /**
     * Calculates a weight ETag based on the true cache key and the last
     * modified timestamp of the given file object.
     *
     * @param f The file for which the ETag should calculate.
     * @return The ETag or <code>null</code> if the true cache key or the given
     * file is <code>null</code>.
     *
     * @see #getTrueCacheKey()
     * @see #getETagByDate(long)
     */
    public String getETagByFile(File f) {
	if (f != null) {
	    return getETagByDate(f.lastModified());
	}
	return null;
    }

    /**
     * Calculates a weight ETag based on the true cache key and the given date.
     *
     * @param date The date for which the ETag should calculate.
     * @return The ETag or <code>null</code> if the true cache key is
     * <code>null</code> or the given time less than 1.
     *
     * @see #getTrueCacheKey()
     * @see #getETagByFile(java.io.File)
     */
    public String getETagByDate(long date) {
	if (date > 0) {
	    String tck = getTrueCacheKey();
	    if (tck != null && !tck.isEmpty()) {
		StringBuilder s = new StringBuilder(tck);
		s.append("#").append(date);
		return getETag(s.toString().getBytes());
	    }
	}
	return null;
    }

    /**
     * Calculates a weight ETag based on the specified data array.
     *
     * @param data The source data for which the ETag should be created.
     *
     * @return The ETag or <code>null</code> if it was not possible to calculate
     * an ETag for the specified data array (e.g. if the data array was null).
     */
    protected String getETag(byte[] data) {
	if (data != null) {
	    try {
		StringBuilder sb = new StringBuilder("W/\"");
                byte[] md5 = getMD5(data);
		for (int i = 0; i < md5.length; i++) {
		    sb.append(Integer.toString((md5[i] & 0xff) + 0x100, 16).substring(1));
		}
		sb.append("\"");
		return sb.toString();
	    } catch (NoSuchAlgorithmException ex) {
		// TODO: Is there an alternative required if MD5 is not available?
	    }
	}
	return null;
    }
    
    private byte[] getMD5(byte[] input) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5").digest(input);
    }

    /**
     * Calculates a true cache key based on the requested resource (including
     * all request parameters).
     *
     * @return The true cache key.
     *
     * @see HttpServletRequest
     */
    public String getTrueCacheKey() {
	if (trueCacheKey == null) {
	    trueCacheKey = getRequestURI();
	}
	return trueCacheKey;
    }
    
    /**
     * Returns <code>true</code> if the response which is produced by this
     * request processor is generally cacheable; otherwise <code>false</code>.
     * <p>
     * Generally means it is not a unique selling point and you have to obtain
     * the cache-control headers, too.
     *
     * @return <code>true</code> if the produced response data is cacheable;
     * otherwise <code>false</code>.
     */
    @Override
    public boolean isCacheable() {
	return false;
    }

    /**
     * Returns an unique request ID for this request. The UUID can be used in
     * cases of troubleshooting if the UUID is logged with an associated
     * exception. As default, if {@link #isDebugEnabled()} is <code>true</code>
     * the request ID (UUID) will be automatically added to the response
     * headers.
     *
     * @return A uniqe (anonymized) request ID for this request.
     */
    public UUID getRequestId() {
	if (requestId == null) {
	    if (req == null) {
		throw new IllegalStateException("Request processor not yet initialized");
	    }
	    StringBuilder buf = new StringBuilder();
	    buf.append(timestamp);
	    try {
		buf.append(req.getAttribute("io.pictura.servlet.SERVICE_NANO_TIMESTAMP") instanceof Long
			? (long) req.getAttribute("io.pictura.servlet.SERVICE_NANO_TIMESTAMP") : "")
			.append("Pictura/").append(Version.getVersionString())
			.append(req.getServletContext().getServerInfo())
			.append(req.getLocalAddr()).append(":").append(req.getLocalPort())
			.append(req.getRemoteAddr()).append(":").append(req.getRemotePort())
			.append(req.getRemoteUser() != null ? req.getRemoteUser() : "")
			.append(req.getRequestURL()).append("?")
			.append(req.getQueryString() != null ? req.getQueryString() : "");
	    } catch (RuntimeException ignore) {
	    }
	    requestId = UUID.nameUUIDFromBytes(buf.toString().getBytes());
	}
	return requestId;
    }

    /**
     * Returns the value of the named attribute as an Object, or null if no
     * attribute of the given name exists.
     *
     * @param name a String specifying the name of the attribute
     * @return the named attribute as an Object, or null if no attribute of the
     * given name exists.
     *
     * @throws IllegalStateException if the request processor was not yet
     * initialized (there was no servlet request object set).
     *
     * @see #setAttribute(java.lang.String, java.lang.Object)
     */
    public Object getAttribute(String name) {
	if (req == null) {
	    throw new IllegalStateException("Request processor not yet initialized");
	}
	return req.getAttribute(name);
    }

    /**
     * Stores an attribute in this request processor servelt request.
     *
     * @param name a String specifying the name of the attribute
     * @param o the Object to be stored
     *
     * @throws IllegalStateException if the request processor was not yet
     * initialized (there was no servlet request object set).
     *
     * @see #getAttribute(java.lang.String)
     */
    public void setAttribute(String name, Object o) {
	if (req == null) {
	    throw new IllegalStateException("Request processor not yet initialized");
	}
	req.setAttribute(name, o);
    }

    /**
     * Called by the parent thread pool executor to handle (execute) this
     * request processor in it's own background thread.
     */
    @Override
    public final void run() throws IllegalStateException {
	if (committed) {
	    throw new IllegalStateException("Request processor already committed to process.");
	}
	committed = true;

	long start = System.currentTimeMillis();
	try {
	    HttpServletRequest request = getRequest();
	    if (request == null) {
		throw new IllegalStateException("Servlet request is null.");
	    }

	    HttpServletResponse response = getResponse();
	    if (response == null) {
		throw new IllegalStateException("Servlet response is null.");
	    }

	    req.setAttribute("io.pictura.servlet.REQUEST_ID", getRequestId().toString());
	    if (isDebugEnabled() || (boolean) req.getAttribute("io.pictura.servlet.HEADER_ADD_REQUEST_ID")) {
		response.setHeader("X-Pictura-RequestId", getRequestId().toString());
	    }

	    try {
		// Test if the user is allowed to send this request
		if (req.getAttribute("io.pictura.servlet.IP_ADDRESS_MATCH") != null) {
		    final String[] ipAddrMatch = (String[]) req.getAttribute("io.pictura.servlet.IP_ADDRESS_MATCH");
		    boolean forbidden = true;
		    for (String ipAddrRange : ipAddrMatch) {
			if (new IpAddressMatcher(ipAddrRange.trim()).matches(request.getRemoteAddr())) {
			    forbidden = false;
			    break;
			}
		    }
		    if (forbidden) {
			doInterrupt(HttpServletResponse.SC_FORBIDDEN);
			return;
		    }
		}

		if (!response.isCommitted()) {
		    // Append our true cache key if requested and possible
		    if (isCacheable() && (isDebugEnabled()
			    || (Boolean) req.getAttribute("io.pictura.servlet.HEADER_ADD_TRUE_CACHE_KEY"))) {
			response.setHeader("X-Pictura-TrueCacheKey", getTrueCacheKey());
		    }		
		    if (getPreProcessor() != null) {
			getPreProcessor().run();
		    }
		    doProcess(request, response);
		}
	    } catch (Exception | Error e) {
		if (!response.isCommitted()) {
		    try {
			if (e instanceof IllegalArgumentException) {
			    doInterrupt(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			} else {
			    doInterrupt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				    "The request processor was not able to fullfill the request.", e);
			    throw new RuntimeException(e);
			}
		    } catch (IOException e2) {
			throw new RuntimeException(e2);
		    }
		    return;
		}
		throw new RuntimeException(e);
	    }
	} finally {
	    // Set status to complet
	    duration = System.currentTimeMillis() - start;
	    completed = true;
	    try {
		if (req != null) {
		    req.setAttribute("io.pictura.servlet.PROC_DURATION", duration);
		    req.setAttribute("io.pictura.servlet.PROC_RUNTIME", runtime);
		}
		if (asyncCtx != null) {
		    asyncCtx.complete();
		}
	    } catch (RuntimeException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Unknown runtime exception while finally request thread", e);
                }
	    }
	    runFinalize();
	}
    }
    
    /**
     * Convenience method to clean-up resources after the response was sent.
     */
    protected void runFinalize() {
    }

    /**
     * Called by the {@link PicturaServlet} servlet (via the service method) to
     * handle the request.
     * <p>
     * When implementing this method, read the request data, write the response
     * headers, get the response's writer or output stream object, and finally,
     * write the response data. It's best to include content type and encoding.
     * When using a {@link PrintWriter} object to return the response, set the
     * content type before accessing the {@link PrintWriter} object.
     * <p>
     * Where possible, set the <code>Content-Length</code> header (with the
     * <code>ServletResponse.setContentLength(int)</code> method), to allow the
     * servlet container to use a persistent connection to return its response
     * to the client, improving performance. The content length is automatically
     * set if the entire response fits inside the response buffer.
     * <p>
     * If the request is incorrectly formatted, <code>doProcess</code> should
     * returns an HTTP "Bad Request" message.
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the servlet.
     * @param resp An {@link HttpServletResponse} object that contains the
     * response the servlet sends to the client.
     * @throws ServletException if an input or output error is detected when the
     * servlet handles the GET request.
     * @throws IOException if the request for the GET could not be handled.
     *
     * @see HttpServletRequest
     * @see HttpServletResponse
     */
    protected abstract void doProcess(HttpServletRequest req,
	    HttpServletResponse resp) throws ServletException, IOException;

    /**
     * Writes the given output data to the output stream which is defined by the
     * given servlet response.
     * <p>
     * If the response is text based (not binary), e.g. the content type starts
     * with <code>text/</code> and the data length is larger than 1k bytes and
     * the client accepts <i>gzip</i> or <i>deflate</i>, the response content is
     * automatically compressed and the content length header field will be
     * correct.
     *
     * @param data The encoded image data.
     * @param req The request object.
     * @param resp The respnse object.
     *
     * @return Bytes sent or -1L if the process was already interrupted. Will
     * return "0" in cases of "HEAD" requests.
     *
     * @throws ServletException if an input or output error is detected when the
     * servlet handles the request.
     * @throws IOException if the request for could not be handled.
     *
     * @see #doWrite(byte[], int, int, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected long doWrite(final byte[] data, final HttpServletRequest req,
            final HttpServletResponse resp) throws ServletException, IOException {
        return doWrite(data, 0, data.length, req, resp);
    }
    
    /**
     * Writes the specified range of the given output data to the output stream
     * which is defined by the given servlet response.
     * <p>
     * If the response is text based (not binary), e.g. the content type starts
     * with <code>text/</code> and the data length is larger than 1k bytes and
     * the client accepts <i>gzip</i> or <i>deflate</i>, the response content is
     * automatically compressed and the content length header field will be
     * correct.
     *  
     * @param data The encoded image data.
     * @param off The start offset in the data.
     * @param len The number of bytes to write.
     * @param req The request object.
     * @param resp The respnse object.
     *
     * @return Bytes sent or -1L if the process was already interrupted. Will
     * return "0" in cases of "HEAD" requests.
     *
     * @throws ServletException if an input or output error is detected when the
     * servlet handles the request.
     * @throws IOException if the request for could not be handled.
     * 
     * @since 1.1
     */
    protected long doWrite(final byte[] data, final int off, final int len, 
            final HttpServletRequest req, final HttpServletResponse resp) 
            throws ServletException, IOException {

        final int length = len - off;
        
	if (!isInterrupted()) {
	    if (!isCacheable()) {
		resp.setHeader(HEADER_CACHECONTROL, "no-cache");
		resp.setHeader(HEADER_PRAGMA, "no-cache");
		resp.setHeader(HEADER_EXPIRES, null);
	    }

	    // Do not compress resources <= 1kB
	    if (!"gzip".equalsIgnoreCase(resp.getHeader(HEADER_CONTENC))
		    && !"deflate".equalsIgnoreCase(resp.getHeader(HEADER_CONTENC))
		    && length > ((int) req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE"))
		    && resp.getContentType() != null
		    && isGZipAllowed(resp.getContentType())) {

		String acceptEncoding = req.getHeader("Accept-Encoding");

		FastByteArrayOutputStream bos = null;
		DeflaterOutputStream dos = null;

		if (acceptEncoding != null) {
		    if (acceptEncoding.contains("gzip")) {
			dos = new GZIPOutputStream(bos = new FastByteArrayOutputStream()) {
			    {
				def.setLevel((int) req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_LEVEL"));
			    }
			};
			resp.setHeader(HEADER_CONTENC, "gzip");
		    } else if (acceptEncoding.contains("deflate")) {
			dos = new DeflaterOutputStream(bos = new FastByteArrayOutputStream()) {
			    {
				def.setLevel((int) req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_LEVEL"));
			    }
			};
			resp.setHeader(HEADER_CONTENC, "deflate");
		    }
		}

		if (dos != null && bos != null) {
		    if ("HEAD".equalsIgnoreCase(req.getMethod())) {
			return 0L;
		    }

		    dos.write(data, off, len);
		    dos.finish();

		    resp.setContentLength(bos.size());
                    bos.writeTo(resp.getOutputStream());
		    
		    return bos.size();
		}
	    }

	    resp.setContentLength(length);

	    if ("HEAD".equalsIgnoreCase(req.getMethod())) {
		return 0L;
	    }

	    OutputStream os = new ContextOutputStream(req, resp.getOutputStream());
	    os.write(data, off, len);
	    return length;
	}

	return -1L;
    }

    // Allowed GZIP content types
    private static final String[] GZIP_CONTENT_TYPES = new String[] {
	"text/", 
	"application/json",
	"application/javascript",
	"application/x-javascript",
	"application/xml",
	"application/pdf",
	"image/x-icon",
	"image/svg+xml",
	"image/vnd.microsoft.icon"
    };
    
    private boolean isGZipAllowed(String contentType) {
	if (contentType == null || contentType.isEmpty()) {
	    return false;
	}
	for (String s : GZIP_CONTENT_TYPES) {
	    if (contentType.startsWith(s)) {
		return true;
	    }
	}
	return false;
    }
    
    /**
     * Matches a request based on IP Address or subnet mask matching against the
     * remote address.
     * <p>
     * Both IPv6 and IPv4 addresses are supported, but a matcher which is
     * configured with an IPv4 address will never match a request which returns
     * an IPv6 address, and vice-versa.
     * <p>
     * <i>
     * Derived from org.springframework.security/spring-security-web/
     * 3.1.2.RELEASE/org/springframework/security/web/util/IpAddressMatcher.java.
     * The origin source is licensed under the Apache License, Version 2.0
     */
    private static final class IpAddressMatcher {

	private final int nMaskBits;
	private final InetAddress requiredAddress;

	/**
	 * Takes a specific IP address or a range specified using the IP/Netmask
	 * (e.g. 192.168.1.0/24 or 202.24.0.0/14).
	 *
	 * @param ipAddress the address or range of addresses from which the
	 * request must come.
	 */
	IpAddressMatcher(String ipAddress) {

	    if (ipAddress.indexOf('/') > 0) {
		String[] addressAndMask = ipAddress.split("/");
		ipAddress = addressAndMask[0];
		nMaskBits = Integer.parseInt(addressAndMask[1]);
	    } else {
		nMaskBits = -1;
	    }
	    requiredAddress = parseAddress(ipAddress);
	}

	boolean matches(String address) {
	    InetAddress remoteAddress = parseAddress(address);

	    if (!requiredAddress.getClass().equals(remoteAddress.getClass())) {
		return false;
	    }

	    if (nMaskBits < 0) {
		return remoteAddress.equals(requiredAddress);
	    }

	    byte[] remAddr = remoteAddress.getAddress();
	    byte[] reqAddr = requiredAddress.getAddress();

	    int oddBits = nMaskBits % 8;
	    int nMaskBytes = nMaskBits / 8 + (oddBits == 0 ? 0 : 1);
	    byte[] mask = new byte[nMaskBytes];

	    Arrays.fill(mask, 0, oddBits == 0 ? mask.length : mask.length - 1, (byte) 0xFF);

	    if (oddBits != 0) {
		int finalByte = (1 << oddBits) - 1;
		finalByte <<= 8 - oddBits;
		mask[mask.length - 1] = (byte) finalByte;
	    }

	    for (int i = 0; i < mask.length; i++) {
		if ((remAddr[i] & mask[i]) != (reqAddr[i] & mask[i])) {
		    return false;
		}
	    }

	    return true;
	}

	private InetAddress parseAddress(String address) {
	    try {
		return InetAddress.getByName(address);
	    } catch (UnknownHostException e) {
		throw new IllegalArgumentException("Failed to parse address" + address, e);
	    }
	}

    }   
    
}
