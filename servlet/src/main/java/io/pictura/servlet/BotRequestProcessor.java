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
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is a special implementation of the default {@link ImageRequestProcessor}
 * which will handle bot requests different to normal "user" requests.
 * <p>
 * The difference compared to normal "users" is that a bot will always get the
 * origin image unless it is a proxy request anyway. To handle this, the
 * <code>BotRequestProcessor</code> will send a moved permanently status code
 * with the location to the unmodified image back to the bot client.
 *
 * @author Steffen Kremp
 *
 * @see ImageRequestProcessor
 * @see ImageRequestStrategy
 *
 * @since 1.0
 */
public class BotRequestProcessor extends ImageRequestProcessor implements
	ImageRequestStrategy {

    //  Another solution to handle bot requests could be to check whether
    //  the request comes from a bot or not and in cases if the request
    //  comes from a bot overwrite the isProxyRequest method and return
    //  true. But this means we have to add the vary header to the response so
    //  a proxy or cache knows about this.
    
    /**
     * RegEx to match a Bot/Crawler from the user agent header. Note, this will
     * not match all bots but the most available.
     */
    private static final Pattern P_BOT = Pattern.compile(
	    "^.*(bot|googlebot|crawler|spider|slurp|robot|crawling).*$",
	    Pattern.CASE_INSENSITIVE);
    
    /**
     * Tests whether the given request comes from a bot or crawler.
     *
     * @param req The related request object.
     * @return <code>true</code> if the request comes from a bot; otherwise
     * <code>false</code>.
     */
    protected boolean isBotRequest(HttpServletRequest req) {
	return req.getHeader(HEADER_USERAGENT) != null
		&& P_BOT.matcher(req.getHeader(HEADER_USERAGENT)).matches();
    }

    /**
     * Calculates a redirect URL to the origin (unmodified) image location.
     *
     * @param req The related request object.
     * @return The relative redirect URL.
     */
    protected String getRedirectURL(HttpServletRequest req) {
	final String ctxPath = req.getContextPath();
	final String srvPath = req.getServletPath();
	final String servlet = (ctxPath.equals("/") ? "" : ctxPath) + srvPath;
	return new StringBuilder(servlet)
		.append(!servlet.endsWith("/") ? "/" : "")
		.append(getRequestedImage(req))
		.toString();
    }

    // Do not cache redirects
    @Override
    public boolean isCacheable() {
	return false;
    }
    
    /**
     * Tests whether the request comes from a bot or crawler and in cases if the
     * client is a bot or crawler the method will send a permanently redirect to
     * the unmodified source image back to the client.
     * <p>
     * In all other cases the processor will continue with the default 
     * {@link ImageRequestProcessor#doProcess(javax.servlet.http.HttpServletRequest, 
     * javax.servlet.http.HttpServletResponse)} processing.
     * <p>
     * Note: If the request is onyl a proxy request it will not send a redirect
     * back to the client. In this case the processor will also continue as
     * usual.
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
     * @see
     * ImageRequestProcessor#doProcess(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doProcess(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {

	if (isBotRequest(req) && !isProxyRequest(req)) {
	    resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
	    resp.setHeader(HEADER_LOCATION, getRedirectURL(req));
	    resp.setContentLength(0);
	    return;
	}

	super.doProcess(req, resp);
    }

    @Override
    public boolean isPreferred(HttpServletRequest req) {
	return isBotRequest(req);
    }

    @Override
    public ImageRequestProcessor createRequestProcessor() {
	return new BotRequestProcessor();
    }

}
