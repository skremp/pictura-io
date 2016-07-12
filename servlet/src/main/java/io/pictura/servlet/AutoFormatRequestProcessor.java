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

/**
 * An output format content negotiation request processor to automatically
 * decide the best image format for the requested client if no output format was
 * set by the client or user.
 * <p>
 * Note: If you have a CDN in front of your service, this approach can have a
 * negative impact on your hit rate because the request processor appends the
 * <code>Vary</code> header to each response for which the image format was
 * automatically overwritten. <b>If this is not desired, you should not use this
 * type of image processor.</b>
 *
 * @see ImageRequestProcessor
 * @see ImageRequestStrategy
 * @see ContentNegotiation
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public class AutoFormatRequestProcessor extends ImageRequestProcessor
	implements ImageRequestStrategy, ContentNegotiation {

    private static final Log LOG = Log.getLog(AutoFormatRequestProcessor.class);
    
    // The default compression ratio by this image processor
    private static final float DEFAULT_AUTO_COMPRESSION_QUALITY = 0.8f; // 80%

    private static final float DEFAULT_COMPRESSION_FACTOR = 0.9f;
    private static final float MOBILE_COMPRESSION_FACTOR = 0.85f;
    
    private String varyHeader;
    private String contAcceptHeader;

    private String trueCacheKey;
    
    /**
     * Creates a new auto format request processor.
     */
    public AutoFormatRequestProcessor() {
    }
    
    @Override
    public boolean isBypassRequest(HttpServletRequest req) {
	return req.getParameter("bypass") != null
		&& (req.getParameter("bypass").isEmpty()
		|| "true".equalsIgnoreCase(req.getParameter("bypass"))
		|| "1".equalsIgnoreCase(req.getParameter("bypass")));
    }

    /**
     * Returns the user secified request format name if the format name was set
     * or the auto calculated format name, based on the analyzed request
     * headers, which is given by
     * {@link #getAutoFormatName(javax.servlet.http.HttpServletRequest)}.
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the servlet.
     *
     * @return The user specified format name or an auto calculated format name.
     */
    @Override
    protected String getRequestedFormatName(HttpServletRequest req) {
	String format = super.getRequestedFormatName(req);
	if (format == null && !isBypassRequest(req)) {
	    String fn = getAutoFormatName(req);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Detect format \"" + fn + "\" [" + getRequestURI() + "]");
            }
            return fn;
	}
	return format;
    }

    /**
     * Returns the automatic calculated output format name depending on the
     * available image writers and the client side (browser) image format
     * support if no format was set by the client/user.
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the servlet.
     *
     * @return The optimal output format name.
     */
    protected String getAutoFormatName(HttpServletRequest req) {
	if (clientSupportsWebP(req) && canWriteFormat("webp")) {
	    return "webp";
	} else if (clientSupportsJP2(req)) {
	    if (canWriteFormat("jp2")) {
		return "jp2";
	    } else if (canWriteFormat("jpeg2000")) {
		return "jpeg2000";
	    }
	}        
	return null;
    }

    /**
     * Gets the compression quality.
     * <p>
     * This will overrite the "default" compression quality if the image
     * processor can produce an WebP or JP2 output image and the client is able
     * to decode WebP or JP2 images. The "new" compression quality is calculated
     * by <code>o = o' * 0.9f</code>. If no compression quality was set, a
     * default value of 0.8f is used. However, only if the requested image
     * quality was set to <code>AUTO</code> (which is also the default value).
     * In cases of mobile clients, the factor is reduced to <code>0.85f</code>,
     * to save the limited bandwidth.
     * </p>
     *
     * @param req The related request object.
     *
     * @return The compression quality depending on the output image format and
     * the choosed image quality ({@link #getRequestedQuality(
     *   javax.servlet.http.HttpServletRequest)}).
     */
    @Override
    protected Float getRequestedCompressionQuality(HttpServletRequest req) {
	Float o = super.getRequestedCompressionQuality(req);

	String fn;
	if (!isBypassRequest(req)
		&& getRequestedQuality(req) == Quality.AUTO
		&& (fn = getAutoFormatName(req)) != null) {

	    if (o == null) {
		o = DEFAULT_AUTO_COMPRESSION_QUALITY;
	    }

	    switch (fn) {
		case "webp":
		case "jp2":
		case "jpeg2000":
		    o *= isMobileDeviceRequest() ? MOBILE_COMPRESSION_FACTOR :
			    DEFAULT_COMPRESSION_FACTOR;
		    break;
	    }
	}
	return o;
    }

    @Override
    public String getTrueCacheKey() {
	if (trueCacheKey == null) {
	    String oldTrueCacheKey = super.getTrueCacheKey();
	    trueCacheKey = oldTrueCacheKey;
	    
	    HttpServletRequest req = getRequest();
	    
	    if (!isBypassRequest(req) && super.getRequestedFormatName(req) == null
		    && getAutoFormatName(req) != null) {
		
		StringBuilder newTrueCacheKey = new StringBuilder(oldTrueCacheKey);
		if (!oldTrueCacheKey.contains("#")) {
		    newTrueCacheKey.append("#");	    
		} else {
		    newTrueCacheKey.append(";");
		}
		
		newTrueCacheKey.append("o=").append((int) (100 * getRequestedCompressionQuality(req)));
		
		String f = getAutoFormatName(req);
		if (f != null) {
		    newTrueCacheKey.append(";f=").append(getAutoFormatName(req));
		}
		
		trueCacheKey = newTrueCacheKey.toString();
	    }
	}
	return trueCacheKey;	
    }        

    @Override
    protected void doProcess(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {

	if (!isBypassRequest(req) && super.getRequestedFormatName(req) == null
		&& getAutoFormatName(req) != null) {

	    resp.addHeader(HEADER_VARY, varyHeader);
	    resp.setHeader(HEADER_CONTACCEPT, contAcceptHeader);

	}
	super.doProcess(req, resp);
    }

    // Only Apple's Safari (Mac OS X and iOS) browsers currently supports JPEG 2000.
    boolean clientSupportsJP2(HttpServletRequest req) {
	if (req != null) {
	    if (req.getHeader(HEADER_USERAGENT) != null) {
		String ua = req.getHeader(HEADER_USERAGENT);
		boolean b = ua.contains("Safari") && !ua.contains("Chrome");
		if (b) {
		    varyHeader = HEADER_USERAGENT;
		    contAcceptHeader = ua;
		}
		return b;
	    } else if (req.getHeader(HEADER_ACCEPT) != null
			&& req.getHeader(HEADER_ACCEPT).contains("image/jp2")) {		
		varyHeader = HEADER_ACCEPT;
		contAcceptHeader = "image/jp2";
                return true;
	    }
	} 
	return false;
    }

    // Chrome and Opera will inform about webp image support by the
    // request accept header.
    boolean clientSupportsWebP(HttpServletRequest req) {
	boolean b = req.getHeader(HEADER_ACCEPT) != null
		&& req.getHeader(HEADER_ACCEPT).contains("image/webp");
	if (b) {
	    varyHeader = HEADER_ACCEPT;
	    contAcceptHeader = "image/webp";
	}
	return b;
    }    
    
    @Override
    public boolean isPreferred(HttpServletRequest req) {
	return req != null && super.getRequestedFormatName(req) == null
		&& getAutoFormatName(req) != null;
    }

    @Override
    public ImageRequestProcessor createRequestProcessor() {
	return new AutoFormatRequestProcessor();
    }

}
