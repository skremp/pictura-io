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

import io.pictura.servlet.PicturaConfig.ConfigParam;
import static io.pictura.servlet.RequestProcessor.HEADER_ALLOW;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is an extension of the {@link PicturaServlet} to support POST
 * requests. In this case the source image is not given by a path reference but
 * as POST data (input stream from the request instance).
 *
 * @see PicturaServlet
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public class PicturaPostServlet extends PicturaServlet {

    private static final long serialVersionUID = -2374622764393048002L;
    
    private static final Log LOG = Log.getLog(PicturaPostServlet.class);
    
    /**
     * Init parameter to specify the max allowed content length in cases of a
     * POST request. If not specified, the value of the
     * {@link #IPARAM_MAX_IMAGE_FILE_SIZE} is used as max content length.
     * <p>
     * A value of -1 also means the default max allowed image file size is to
     * use. A value of 0 means there is no limit to the allowed content length.
     * In production environments you should not set this to 0 to prevent the
     * server from OOM errors.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/imageio/max-post-content-length")
    public static final String IPARAM_MAX_IMAGE_POST_CONTENT_LENGTH = "maxImagePostContentLength";

    // The max allowed content length
    private int maxImagePostContentLength = -1;    

    @Override
    public void init(ServletConfig config) throws ServletException {
	super.init(config);
	setAlive(false);

	// Get the optional max content length for POST requests
	String sMaxImagePostContentLength = getInitParameter(IPARAM_MAX_IMAGE_POST_CONTENT_LENGTH);
	if (sMaxImagePostContentLength != null && !sMaxImagePostContentLength.isEmpty()) {
	    maxImagePostContentLength = tryParseInt(sMaxImagePostContentLength, -1);
	}
	
	if (!isMethodSupported("POST")) {
	    LOG.warn("Method POST on PicturaPostServlet is disabled by user or super class");
	}
	
	setAlive(true);
    }

    /**
     * Overrides the default behaviour to allow <code>POST</code> requests, too.
     *
     * @param method The request method.
     *
     * @return <code>true</code> if the request method is allowed; otherwise
     * <code>false</code>.
     */
    @Override
    protected boolean isMethodSupported(String method) {
	return "POST".equalsIgnoreCase(method) || super.isMethodSupported(method);
    }

    /**
     * Converts the given (origin) request processor to a POST request processor
     * and executes the process.
     *
     * @param rp The request processor to execute as POST request processor.
     *
     * @throws ServletException if an input or output error is detected when the
     * servlet handles the request.
     * @throws IOException if the request for could not be handled.
     */
    @Override
    protected void doProcess(RequestProcessor rp)
	    throws ServletException, IOException {

	// Override the default allow header
	rp.getResponse().setHeader(HEADER_ALLOW, "GET, POST");

	// Set additional request attributes for this type of servlet
	rp.getRequest().setAttribute("io.pictura.servlet.MAX_IMAGE_POST_CONTENT_LEGTH",
		maxImagePostContentLength);

	if (rp instanceof ScriptRequestProcessor
		|| rp instanceof StatsRequestProcessor
		|| rp instanceof PlaceholderRequestProcessor) {

	    super.doProcess(rp);
	    return;
	}

	super.doProcess(new PostRequestProcessor(rp));
    }

    // POST request processor to wrap and execute the origin request processor
    private static final class PostRequestProcessor extends RequestProcessor {

	private final RequestProcessor rp;

	public PostRequestProcessor(RequestProcessor rp) {
	    super(rp);
	    this.rp = rp;
	}

	@Override
	public String getTrueCacheKey() {
	    if ("POST".equalsIgnoreCase(getRequest().getMethod())) {
		return null;
	    }
	    return rp.getTrueCacheKey();
	}

	@Override
	public boolean isCacheable() {
	    // Responses from POST requests are not cacheable
	    if ("POST".equalsIgnoreCase(getRequest().getMethod())) {
		return false;
	    }
	    return rp.isCacheable();
	}	

	@Override
	protected void doProcess(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
	    		    
	    if ("POST".equalsIgnoreCase(req.getMethod())
		    && rp instanceof ImageRequestProcessor) {

		// Get the origin processor
		ImageRequestProcessor irp = (ImageRequestProcessor) rp;

		// We have to set this properties now
		irp.maxImageFileSize = (Long) req.getAttribute("io.pictura.servlet.MAX_IMAGE_FILE_SIZE");
		irp.maxImageResolution = (Long) req.getAttribute("io.pictura.servlet.MAX_IMAGE_RESOLUTION");

		int maxPCL = (Integer) req.getAttribute("io.pictura.servlet.MAX_IMAGE_POST_CONTENT_LEGTH");

		// Check if the content length was given (mandatory by this
		// implementation) and the length is less than the maximum
		// allowed
		int reqCL = req.getContentLength();
		if (reqCL == -1) {
		    irp.doInterrupt(HttpServletResponse.SC_LENGTH_REQUIRED);
		    return;
		}

		// Content length too large
		if ((maxPCL > 0 && reqCL > maxPCL)
			|| (irp.maxImageFileSize > 0 && reqCL > irp.maxImageFileSize)) {

		    irp.doInterrupt(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
		    return;
		}

		// Only accept image media types and images we can process (read)
		if (req.getContentType() == null
			|| !req.getContentType().toLowerCase(Locale.ENGLISH).startsWith("image/")
			|| !irp.canReadMimeType(req.getContentType())) {

		    irp.doInterrupt(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
			    "Unsupported media or content type was not set");
		    return;
		}

		// Read the raw image data from the POST request
		FastByteArrayOutputStream bos = new FastByteArrayOutputStream(reqCL);
		try (InputStream is = new ContextInputStream(req, req.getInputStream())) {
		    long received = 0L;

		    int len;
		    byte[] buf = new byte[1024 * 16]; // 16k bytes

		    while ((len = is.read(buf)) > -1) {
			bos.write(buf, 0, len);
			received += len;

			// OK, the client tells us a content length smaller
			// than the real file size. This is not nice; let us
			// say this with an error response.
			if (received > reqCL) {
			    irp.doInterrupt(HttpServletResponse.SC_BAD_REQUEST,
				    "The received data does not match the sepcified content length");
			    return;
			}
		    }
		}

		// Maybe it makes no sence to append an etag header to the
		// response. The only way we can calculate an unique etag is
		// to use the given data and combine it with the requested
		// transformation rules (request parameters).
		resp.setHeader(HEADER_ETAG, getETag(bos.toByteArray(), getRequestURI()));

		// Process the image
		irp.doProcessImage(new FastByteArrayInputStream(bos), req, resp);
		return;
	    } // Do not allow POST requests on other request processors than on
	    // ImageRequestProcessor's
	    else if ("POST".equalsIgnoreCase(req.getMethod())) {
		doInterrupt(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		return;
	    }

	    // Continue as usual (GET request)
	    rp.doProcess(req, resp);
	}

	// Calculates an etag based on the given data array and the optional
	// meta string value (in this conjunction normally the image request
	// parameters.
	private String getETag(byte[] data, String meta) {
	    byte[] buf = new byte[data.length + meta.length()];

	    System.arraycopy(data, 0, buf, 0, data.length);
	    System.arraycopy(meta.getBytes(), 0, buf, data.length, meta.length());

	    return getETag(buf);
	}

    }

}
