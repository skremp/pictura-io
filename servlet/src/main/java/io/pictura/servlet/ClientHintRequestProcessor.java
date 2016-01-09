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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementation of an Client-Hint Image Request Processor.
 * <p>
 * If the image resource width is known at request time, the user agent can
 * communicate it to the server to enable selection of an optimized resource.
 * The client and server can negotiate an optimized asset based on the given
 * request hints.
 * </p>
 * <p>
 * For browsers which doesn't support client hint headers, it is possible to
 * enable this feature by passing values from client to the server via
 * a small cookie. To enable this feature it is necessary to embedd the
 * <code>cookie.js</code> script into the head of the HTML page.
 * </p>
 *
 * @author Steffen Kremp
 *
 * @see AutoFormatRequestProcessor
 *
 * @since 1.0
 */
public class ClientHintRequestProcessor extends AutoFormatRequestProcessor {

    private static final Log LOG = Log.getLog(ClientHintRequestProcessor.class);
    
    // Device Pixel Ratio
    private static final String CH_DPR = "DPR";

    // Resource Width
    private static final String CH_RW = "Width";

    // Device Width
    private static final String CH_DW = "Viewport-Width";
    
    private static final String CLIENT_HINT_COOKIE_NAME = "__picturaio__";
    
    // Lazy cache key
    private String trueCacheKey;
    
    // Lazy client hint cookie
    private boolean varyClientHintCookie;
    private boolean lazyClientHintCookie;    
    private ClientHintCookie clientHintCookie;

    /**
     * Constructs a new <code>ClientHintRequestProcessor</code> to handle client
     * hints.
     */
    public ClientHintRequestProcessor() {
    }

    @Override
    boolean clientSupportsWebP(HttpServletRequest req) {
	boolean b = super.clientSupportsWebP(req);
	if (!b) {
	    Cookie c = getClientHintCookie(req);
	    if (c instanceof ClientHintCookie) {
		if (((ClientHintCookie) c).isImageFormatWebPSupported()) {
		    varyClientHintCookie = true;
		    return true;
		}
	    }
	}
	return b;
    }

    @Override
    boolean clientSupportsJP2(HttpServletRequest req) {
	boolean b = super.clientSupportsJP2(req);
	if (!b) {
	    Cookie c = getClientHintCookie(req);
	    if (c instanceof ClientHintCookie) {
		if (((ClientHintCookie) c).isImageFormatJP2Supported()) {
		    varyClientHintCookie = true;
		    return true;
		}
	    }
	}
	return b;
    }
    
    /**
     * Returns the (optional) client hint cookie if present in the given servlet 
     * request.
     * <p>
     * The cookie is set on client side via JavaScript if cookies are enabled
     * on browser side and the script was running before the request of this
     * processor was made.
     * </p>
     * 
     * @param req The request object.
     * @return The client hint cookie if present; otherwise <code>null</code>.
     */
    protected Cookie getClientHintCookie(HttpServletRequest req) {
	if (!lazyClientHintCookie) {
	    if (hasClientHintCookie(req)) {
		clientHintCookie = new ClientHintCookie(getCookie(CLIENT_HINT_COOKIE_NAME));
	    }
	    lazyClientHintCookie = true;
	}
	return clientHintCookie;
    }
    
    /**
     * Sets automatically an requested width if nothing else was specified by
     * the user and the client hint resource width and/or device width header is
     * present as request header.
     *
     * @param req The request object.
     *
     * @return The requested resource width or <code>null</code> if not
     * specified by the request object.
     */
    @Override
    protected Integer getRequestedScaleWidth(HttpServletRequest req) {
	Integer originWidth = super.getRequestedScaleWidth(req);
	if (!isBypassRequest(req) && originWidth == null) {
	    if (req.getHeader(CH_RW) != null) {
		Integer w = tryParseInt(req.getHeader(CH_RW), -1);
		return w > -1 ? w : originWidth;
	    } else if (req.getHeader(CH_DW) != null) {
		Integer w = tryParseInt(req.getHeader(CH_DW), -1);
		return w > -1 ? w : originWidth;
	    }	    	    
	    
	    Cookie c = getClientHintCookie(req);
	    if (c instanceof ClientHintCookie) {
		ClientHintCookie chc = (ClientHintCookie) c;		
		if (chc.getViewportWidth() > 0) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("DW by client hint cookie; dw[" + chc.getViewportWidth() + "]");
                    }
		    varyClientHintCookie = true;
		    return chc.getViewportWidth();
		}
	    }
	}
	return originWidth;
    }

    /**
     * Sets automatically an requested pixel ratio if nothing else was specified
     * by the user and the client hint device pixel ratio header is present as
     * request header.
     *
     * @param req The request object.
     *
     * @return The requested pixel ratio or <code>null</code> if not specified
     * by the request object.
     */
    @Override
    protected Float getRequestedScalePixelRatio(HttpServletRequest req) {
	final Float originDpr = super.getRequestedScalePixelRatio(req);
	if (!isBypassRequest(req) && originDpr == null) {
	    if (req.getHeader(CH_DPR) != null) {
		return tryParseFloat(req.getHeader(CH_DPR), 1F);
	    } else if (getClientHintCookie(req) instanceof ClientHintCookie) {
		ClientHintCookie chc = (ClientHintCookie) getClientHintCookie(req);
		if (chc.getDevicePixelRatio() > 0f) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("DPR by client hint cookie; dpr[" + chc.getDevicePixelRatio() + "]");
                    }
		    varyClientHintCookie = true;		    
		    return chc.getDevicePixelRatio();
		}
	    }
	}
	return originDpr;
    }   
    
    @Override
    public String getTrueCacheKey() {
	if (trueCacheKey == null) {
	    String oldTrueCacheKey = super.getTrueCacheKey();
	    trueCacheKey = oldTrueCacheKey;
		    
	    HttpServletRequest req = getRequest();
	    
	    if (!isBypassRequest(req) && isPreferred(req)) {
		
		String sep = "";
		
		StringBuilder newTrueCacheKey = new StringBuilder(oldTrueCacheKey);
		if (!oldTrueCacheKey.contains("#")) {
		    newTrueCacheKey.append("#");	    
		} else {
		    sep = ";";
		}
		
		Integer sw = getRequestedScaleWidth(req);
		if (sw != null) {
		    newTrueCacheKey.append(sep).append("sw=").append(sw);
		    sep = ";";
		}
		
		Float dpr = getRequestedScalePixelRatio(req);
		if (dpr != null) {
		    newTrueCacheKey.append(sep).append("dpr=").append(dpr);
		}
		
		trueCacheKey = newTrueCacheKey.toString();
	    }
	}
	return trueCacheKey;	
    }       
    
    @Override
    protected void doProcess(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {

	if (req.getHeader(CH_DPR) != null) {
	    resp.addHeader(HEADER_VARY, CH_DPR);
	    resp.setHeader("Content-" + CH_DPR, req.getHeader(CH_DPR));
	}

	if (req.getHeader(CH_RW) != null) {
	    resp.addHeader(HEADER_VARY, CH_RW);
	    resp.setHeader("Content-" + CH_RW, req.getHeader(CH_RW));
	} else if (req.getHeader(CH_DW) != null) {
	    resp.addHeader(HEADER_VARY, CH_DW);
	    resp.setHeader("Content-" + CH_DW, req.getHeader(CH_DW));
	}		

	if (varyClientHintCookie) {
	    resp.addHeader(HEADER_VARY, "Cookie");
	}
	
	super.doProcess(req, resp);
    }

    @Override
    public boolean isPreferred(HttpServletRequest req) {
	return req.getHeader(CH_DPR) != null
		|| req.getHeader(CH_RW) != null
		|| req.getHeader(CH_DW) != null
		|| hasClientHintCookie(req);
    }

    @Override
    public ImageRequestProcessor createRequestProcessor() {
	return new ClientHintRequestProcessor();
    }    
    
    private boolean hasClientHintCookie(HttpServletRequest req) {
	if (req != null && req.getCookies() != null) {
	    Cookie[] cookies = req.getCookies();
	    for (Cookie c : cookies) {
		if (CLIENT_HINT_COOKIE_NAME.equals(c.getName())) {
		    return true;
		}
	    }
	}
	return false;
    }
    
    // Wrapper class to get direct access to client hint cookie values
    private static final class ClientHintCookie extends Cookie {

	private static final long serialVersionUID = 5466958693847149073L;

	private final Cookie cookie;

	private float dpr = -1f;
	private int dw = -1;
	private int dh = -1;
	
	private boolean webp = false;
	private boolean jp2 = false;

	public ClientHintCookie(Cookie c) {
	    super(c.getName(), c.getValue());
	    this.cookie = c;
	    
	    if (c.getValue() != null) {
		String[] val = c.getValue().split(",");
		
		for (String v : val) {
		    String[] kv = v.split("=");
		    
		    if (kv.length == 2) {
			switch (kv[0]) {
			    case "dpr":
				this.dpr = PicturaServlet.tryParseFloat(kv[1], 1f);
				break;
			    case "dw":
				this.dw = PicturaServlet.tryParseInt(kv[1], -1);
				break;
			    case "dh":
				this.dh = PicturaServlet.tryParseInt(kv[1], -1);
				break;
			    case "jp2":
				this.jp2 = "1".equals(kv[1]);
				break;
			    case "webp":
				this.webp = "1".equals(kv[1]);
				break;
			}
		    }
		}
	    }
	}

	float getDevicePixelRatio() {
	    return dpr;
	}

	int getViewportWidth() {
	    return dw;
	}

	int getViewportHeight() {
	    return dh;
	}
	
	boolean isImageFormatWebPSupported() {
	    return webp;
	}

	boolean isImageFormatJP2Supported() {
	    return jp2;
	}
	
	@Override
	public void setComment(String purpose) {
	    cookie.setComment(purpose);
	}

	@Override
	public String getComment() {
	    return cookie.getComment();
	}

	@Override
	public void setDomain(String domain) {
	    cookie.setDomain(domain);
	}

	@Override
	public String getDomain() {
	    return cookie.getDomain();
	}

	@Override
	public void setMaxAge(int expiry) {
	    cookie.setMaxAge(expiry);
	}

	@Override
	public int getMaxAge() {
	    return cookie.getMaxAge();
	}

	@Override
	public void setPath(String uri) {
	    cookie.setPath(uri);
	}

	@Override
	public String getPath() {
	    return cookie.getPath();
	}

	@Override
	public void setSecure(boolean flag) {
	    cookie.setSecure(flag);
	}

	@Override
	public boolean getSecure() {
	    return cookie.getSecure();
	}

	@Override
	public String getName() {
	    return cookie.getName();
	}

	@Override
	public void setValue(String newValue) {
	    cookie.setValue(newValue);
	}

	@Override
	public String getValue() {
	    return cookie.getValue();
	}

	@Override
	public int getVersion() {
	    return cookie.getVersion();
	}

	@Override
	public void setVersion(int v) {
	    cookie.setVersion(v);
	}

	@Override
	public void setHttpOnly(boolean isHttpOnly) {
	    cookie.setHttpOnly(isHttpOnly);
	}

	@Override
	public boolean isHttpOnly() {
	    return cookie.isHttpOnly();
	}

    }
    
}
