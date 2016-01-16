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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Servlet which extends the default {@link HttpServlet} with the possibility to
 * cache request responses.
 * <p>
 * Note: This servlet implementation will only cache HTTP responses with status
 * code <code>200</code>. Other status codes are ignored and not cached.
 *
 * @author Steffen Kremp
 *
 * @see HttpServlet
 *
 * @since 1.0
 */
public abstract class HttpCacheServlet extends HttpServlet {

    private static final long serialVersionUID = 611795151208000921L;

    private static final Log LOG = Log.getLog(HttpCacheServlet.class);

    // Cache instance to store (buffer) responses; e.g. in-memory LRU cache
    private volatile HttpCache cache;

    // Cache statistics
    private volatile long cacheHitCount;
    private volatile long cacheMissCount;
    
    /**
     * Sets the response cache for this servlet instance. Normally, the cache is
     * set while servlet's initialization ({@link #init()} or
     * {@link #init(javax.servlet.ServletConfig)}).
     *
     * @param cache The cache instance to use by this servlet to cache servlet
     * responses if the response is cacheable.
     *
     * @see #createDefaultHttpCache(int, int)
     * @see HttpCache
     */
    public synchronized void setHttpCache(HttpCache cache) {
	this.cache = cache;
	if (cache instanceof DefaultHttpCache) {
	    LOG.warn("Using PicturaIO built-in HttpCache (not for production use!)");
            DefaultHttpCache dhc = (DefaultHttpCache) cache;
            
            if (dhc.maxEntrySize > 0) {
                long maxHeap = Runtime.getRuntime().maxMemory();
                long maxSize = (long) dhc.capacity * (long) dhc.maxEntrySize;
                
                if (LOG.isWarnEnabled() && (maxSize > (maxHeap / 2))) {
                    LOG.warn("Estimated cache size (" + (maxSize/1024/1024) 
                            + "m) is greater than the half heap size (Xmx" 
                            + (maxHeap/1024/1024) + "m)");
                }
            }
	}
    }

    /**
     * Gets the current used cache from this servlet instance.
     *
     * @return The current used cache.
     */
    public synchronized HttpCache getHttpCache() {
	return this.cache;
    }

    /**
     * Returns the approximate cache hit rate over all requests or
     * <code>-1f</code> if caching is not enabled.
     *
     * @return Cache hit rate or <code>-1</code> if caching is not enabled.
     */
    public synchronized float getHttpCacheHitRate() {
	if (cache == null) {
	    return -1f;
	}
	final long sum = cacheHitCount + cacheMissCount;
	return sum > 0 ? ((100f / sum) * cacheHitCount) / 100f : 0f;
    }

    /**
     * Returns the total number of cache entries or <code>-1</code> if there is
     * currently no cache active.
     *
     * @return Number of cache entries or <code>-1</code> if there is currently
     * no cache active.
     */
    public synchronized int getHttpCacheSize() {
	return getHttpCache() != null ? getHttpCache().keySet().size() : -1;
    }

    /**
     * Creates a new request processor based on the given who is able to handle
     * cacheable requests. This returns the same request processor as the given
     * if the origin request is not cacheable or there is currently (at the time
     * the method is called) no cache available.
     *
     * @param rp Origin request processor to wrap.
     *
     * @return A wrapped request processor instance or the given request
     * processor if the request is not cacheable or there is currently no cache
     * to store the response data available.
     *
     * @see RequestProcessor
     */
    protected RequestProcessor createCacheRequestProcessor(RequestProcessor rp) {
	if (rp == null || cache == null || !rp.isCacheable()
		|| rp.getTrueCacheKey() == null || rp.getTrueCacheKey().isEmpty()) {

	    if (rp != null) {
		cacheMissCount++;
	    }
	    return rp;
	}
	return new CacheRequestProcessor(rp);
    }

    // Request processor wrapper
    private final class CacheRequestProcessor extends RequestProcessor {

	private final RequestProcessor rp;
	private final CacheServletResponse cResp;

	private final Runnable pp;
	
	private final long timestamp;

	private CacheRequestProcessor(RequestProcessor rp) {
	    super(rp);

	    this.rp = rp;
	    this.pp = rp.getPreProcessor();

	    this.cResp = new CacheServletResponse(super.getResponse());
	    injectResponse();
	    
	    timestamp = System.currentTimeMillis();
	}
	
	private void injectResponse() {
	    setResponse(cResp);
	    rp.setResponse(cResp);
	}

	@Override
	Runnable getPreProcessor() {
	    return null;
	}

	@Override
	public boolean isCacheable() {
	    return true;
	}

	@Override
	public String getTrueCacheKey() {
	    return rp.getTrueCacheKey();
	}

	@Override
	protected void doProcess(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
	    
	    HttpCache hc = getHttpCache();
	    String cacheKey = rp.getTrueCacheKey();

	    if (hc != null) {
		HttpCacheEntry cacheEntry = hc.get(cacheKey);

		if ("DELETE".equalsIgnoreCase(req.getMethod())) {
		    doDelete(cacheEntry, resp);
		    return;
		}

		if (cacheEntry != null) {
                    // TODO: Maybe reuse expired entry in cases of origin host errors?
		    if (!cacheEntry.isExpired()) {			
			doSend(cacheEntry, req, cResp);
			return;
		    } else {
			doRemove(cacheEntry);
		    }
		}
	    }

	    if (pp != null) {
		pp.run();
	    }

	    cacheMissCount++;
	    cResp.setHeader("X-Pictura-Cache", "Miss");

	    rp.doProcess(req, cResp);
	    if (cResp != null && cResp.getStatus() == HttpServletResponse.SC_OK) {
		if ("GET".equalsIgnoreCase(req.getMethod())) {
		    doCache(new HttpCacheEntry(cacheKey, cResp.getCopy(), req, cResp));
		}
	    }
	}	
	
	private void doSend(HttpCacheEntry entry, HttpServletRequest req,
		HttpServletResponse resp) throws ServletException, IOException {
	    
	    cacheHitCount++;
	    entry.hitCount++;
	    resp.setHeader("X-Pictura-Cache", "Hit");

	    Collection<String> headerNames = entry.getHeaderNames();
	    for (String name : headerNames) {
		switch (name) {

		    // Update the date header
		    case HEADER_DATE:
			resp.setDateHeader(HEADER_DATE, System.currentTimeMillis());
			break;

		    // Calculate a new max-age based on the old and the current
		    // server time
		    case HEADER_CACHECONTROL:
			String[] cacheControl = entry.getHeader(HEADER_CACHECONTROL).split(",");
			StringBuilder newCacheControl = new StringBuilder();

			String sep = "";
			for (String s : cacheControl) {
			    newCacheControl.append(sep);
			    if (s.toLowerCase(Locale.ENGLISH).trim().startsWith("max-age=")) {
				newCacheControl.append("max-age=")
					.append((entry.getExpires()
						- System.currentTimeMillis()) / 1000);
			    } else {
				newCacheControl.append(" ").append(s);
			    }
			    sep = ", ";
			}

			resp.setHeader(HEADER_CACHECONTROL, newCacheControl.toString());
			break;

		    // Do not set previouse values
		    case HEADER_PRAGMA:
		    case HEADER_CONTLEN:
		    case HEADER_CONNECTION:
		    case HEADER_COOKIE:
		    case "X-Pictura-Cache":
		    case "X-Pictura-Lookup":
			break;

		    case "X-Pictura-RequestId":
			resp.setHeader("X-Pictura-RequestId", rp.getRequestId().toString());
			break;

		    default:
			resp.setHeader(name, entry.getHeader(name));
		}
	    }

	    if (isDebugEnabled()) {
		resp.setHeader("X-Pictura-CacheLookup", 
			String.valueOf(System.currentTimeMillis() - timestamp) + "ms");
	    }
	    
	    if (req.getHeader(HEADER_IFMODSINCE) != null
		    || req.getHeader(HEADER_IFNONMATCH) != null) {

		doInterrupt(HttpServletResponse.SC_NOT_MODIFIED);
		return;
	    }

	    resp.setStatus(entry.getStatus());
	    resp.setContentType(entry.getContentType());

	    String acceptEncoding = req.getHeader(HEADER_ACCEPTENC);
	    acceptEncoding = acceptEncoding != null ? acceptEncoding.toLowerCase(Locale.ENGLISH) : "";

	    if (("gzip".equalsIgnoreCase(entry.getContentEncoding())
		    && !acceptEncoding.contains("gzip"))
		    || ("deflate".equalsIgnoreCase(entry.getContentEncoding())
		    && !acceptEncoding.contains("deflate"))) {

		byte[] tmp = getInflaterContent(entry.getContent(), entry.getContentEncoding());

		resp.setHeader(HEADER_CONTENC, null);
		resp.setContentLength(tmp.length);
		doWrite(tmp, getRequest(), resp);
		return;
	    }

	    resp.setContentLength(entry.getContentLength());
	    doWrite(entry.getContent(), getRequest(), resp);
	}

	private void doDelete(HttpCacheEntry entry, HttpServletResponse resp)
		throws ServletException, IOException {

	    HttpCache hc = getHttpCache();
	    if (hc != null && entry != null && hc.remove(entry.getKey())) {
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		return;
	    }
	    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private void doCache(HttpCacheEntry entry) {
	    if (entry != null) {
		HttpCache hc = getHttpCache();
		if (hc != null && !entry.isExpired()) {
		    entry.setUserProperty("__producer", rp.getClass().getName());
		    hc.put(rp.getTrueCacheKey(), entry);
		}
	    }
	}

	private void doRemove(HttpCacheEntry entry) {
	    if (entry != null) {
		HttpCache hc = getHttpCache();
		if (hc != null) {
		    hc.remove(entry.getKey());
		}
	    }
	}

	private byte[] getInflaterContent(byte[] content, String contentEncoding) throws IOException {
	    if ("gzip".equalsIgnoreCase(contentEncoding)
		    || "deflate".equalsIgnoreCase(contentEncoding)) {

		InflaterInputStream iis = "gzip".equalsIgnoreCase(contentEncoding)
			? new GZIPInputStream(new FastByteArrayInputStream(content))
			: new InflaterInputStream(new FastByteArrayInputStream(content));

		FastByteArrayOutputStream bos = new FastByteArrayOutputStream(1024 * 16);

		int len;
		byte[] buf = new byte[1024 * 16];

		while ((len = iis.read(buf)) > -1) {
		    bos.write(buf, 0, len);
		}

		return bos.toByteArray();
	    }
	    return content;
	}

    }

    // Helper class to wrap the servlet response
    private static final class CacheServletResponse extends HttpServletResponseWrapper {

	private ServletOutputStream outputStream;
	private PrintWriter writer;
	private ServletOutputStreamCopier copier;

	private CacheServletResponse(HttpServletResponse response) {
	    super(response);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
	    if (writer != null) {
		throw new IllegalStateException("getWriter() has already been called on this response.");
	    }

	    if (outputStream == null) {
		outputStream = getResponse().getOutputStream();
		copier = new ServletOutputStreamCopier(outputStream);
	    }

	    return copier;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
	    if (outputStream != null) {
		throw new IllegalStateException("getOutputStream() has already been called on this response.");
	    }

	    if (writer == null) {
		copier = new ServletOutputStreamCopier(getResponse().getOutputStream());
		writer = new PrintWriter(new OutputStreamWriter(copier, getResponse().getCharacterEncoding()), true);
	    }

	    return writer;
	}

	@Override
	public void flushBuffer() throws IOException {
	    if (writer != null) {
		writer.flush();
	    } else if (outputStream != null) {
		copier.flush();
	    }
	}

	byte[] getCopy() {
	    if (copier != null) {
		return copier.getCopy();
	    } else {
		return new byte[0];
	    }
	}

    }

    // Helper class to get a copy of the data written to the output stream 
    private static final class ServletOutputStreamCopier extends ServletOutputStream {

	private final OutputStream outputStream;
	private final FastByteArrayOutputStream copy;

	private ServletOutputStreamCopier(OutputStream outputStream) {
	    this.outputStream = outputStream;
	    this.copy = new FastByteArrayOutputStream(64 * 1024);
	}

	@Override
	public void write(int b) throws IOException {
	    outputStream.write(b);
	    copy.write(b);
	}

	byte[] getCopy() {
	    return copy.toByteArray();
	}

    }

    /**
     * Creates a new {@link HttpCache} instance which uses a default (built-in)
     * implementation.
     *
     * @param capacity Maximum number of allowed elements in the cache.
     * @param maxEntrySize The number of the maximum content size per entry.
     *
     * @return A new cache instance with the specified cache settings.
     */
    public static HttpCache createDefaultHttpCache(int capacity, int maxEntrySize)
	    throws IllegalArgumentException {

	return new DefaultHttpCache(capacity, 1f, maxEntrySize);
    }

    /**
     * Saves the current state of the given cache (entries) to the specified
     * target file on the filesystem.
     *
     * @param file File to store the cache entries to.
     * @param cache Cache instance to store.
     *
     * @throws IllegalArgumentException if the given cache is <code>null</code>.
     * @throws IOException if an I/O error occurs while writing stream header
     */
    public static void saveHttpCacheToFile(File file, HttpCache cache)
	    throws IllegalArgumentException, IOException {

	if (cache == null) {
	    throw new IllegalArgumentException("Cache must be not null");
	}

	if (file == null) {
	    throw new IllegalArgumentException("Cache file must be not null");
	}

        if (LOG.isDebugEnabled()) {
            LOG.debug("Store cache entries to \"" + file.getAbsolutePath() + "\"");
        }

	if (!file.exists()) {
	    File dir = file.getParentFile();
	    if (!dir.exists()) {
		dir.mkdirs();
	    }
	    if (!file.createNewFile()) {
		throw new IOException("Can't create file at \"" + file.getAbsolutePath() + "\"");
	    }
	}

	ArrayList<HttpCacheEntry> entryList = new ArrayList<>();

	Collection<String> cacheKeys = cache.keySet();
	for (String key : cacheKeys) {
	    HttpCacheEntry entry = cache.get(key);
	    if (entry != null && !entry.isExpired()) {
		entryList.add(entry);
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Submitted cache entry with key[" + entry.getKey() + "]");
                }
	    }
	}

	try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
	    oos.writeObject(entryList);
	}
    }

    /**
     * Loads a previousely saved cache instance from the specified location.
     *
     * @param file File where the cache entries are stored.
     * @param cache Cache instance to put the loaded entries to.
     *
     * @throws IllegalArgumentException if the given cache instance is
     * <code>null</code>.
     * @throws IOException if an I/O error occurs while reading stream header or
     * the file is corrupt.
     */
    @SuppressWarnings("unchecked")
    public static void loadHttpCacheFromFile(File file, HttpCache cache)
	    throws IllegalArgumentException, IOException {

	if (cache == null) {
	    throw new IllegalArgumentException("Destination cache to restore persisted entries must be not null");
	}

	if (file == null) {
	    throw new IllegalArgumentException("Cache file must be not null");
	}

        if (LOG.isDebugEnabled()) {
            LOG.debug("Load cache entries from \"" + file.getAbsolutePath() + "\"");
        }

	if (file.length() > 0) {
	    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
		Object o = ois.readObject();
		if (!(o instanceof ArrayList)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Invalid cache format (serialized array list expected)");
                    }
		    throw new IOException("Corrupted cache persistence file");
		}

		ArrayList<HttpCacheEntry> entryList = (ArrayList<HttpCacheEntry>) o;

		for (HttpCacheEntry entry : entryList) {
		    if (entry.isExpired()) {
			continue;
		    }
		    cache.put(entry.getKey(), entry);
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Restored cache entry with key[" + entry.getKey() + "]");
                    }
		}

	    } catch (ClassNotFoundException ex) {
		throw new IOException("Invalid cache data file", ex);
	    }
	}
    }
    
    // Default (built-in) LRU in-memory cache implementation
    private static final class DefaultHttpCache implements HttpCache {

	private final int maxEntrySize;
        private final int capacity;
	private final Map<String, SoftReference<HttpCacheEntry>> cache;

	private DefaultHttpCache(final int capacity, final float loadFactor,
		final int maxEntrySize) throws IllegalArgumentException {

	    this.maxEntrySize = maxEntrySize;
            this.capacity = capacity;
	    this.cache = Collections.synchronizedMap(new LinkedHashMap<String, SoftReference<HttpCacheEntry>>(
		    capacity, loadFactor, true) {

			private static final long serialVersionUID = 3421111110521406377L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<String, SoftReference<HttpCacheEntry>> eldest) {
			    return size() > capacity;
			}
		    });
	}

	@Override
	public HttpCacheEntry get(String key) {
	    SoftReference<HttpCacheEntry> ref = cache.get(key);
	    return ref != null ? ref.get() : null;
	}

	@Override
	public void put(String key, HttpCacheEntry entry) {
	    // Do not cache if the entry content size is larger than the maximum
	    // allowed content length per entry
	    if (maxEntrySize > 0 && entry != null && entry.getContentLength() > maxEntrySize) {
		return;
	    }

	    // Remove if the value is null
	    if (entry == null) {
		remove(key);
	    } else {
		cache.put(key, new SoftReference<>(entry));
	    }
	}

	@Override
	public boolean remove(String key) {
	    return cache.remove(key) != null;
	}

	@Override
	public Set<String> keySet() {
	    return Collections.unmodifiableSet(new ConcurrentSkipListSet<>(cache.keySet()));
	}
    }

}
