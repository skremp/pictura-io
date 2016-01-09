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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Describes a factory interface to create different processor instances to
 * handle image requests depending on a given servlet request.
 * <p>
 * An example use case would be to check if a cookie exists to deliver different
 * versions of the same resource. For example:
 *
 * <pre>
 * public class PaywallImageRequestProcessorFactory implements ImageRequestProcessorFactory {
 *
 *    public ImageRequestProcessor createRequestProcessor(HttpServletRequest req)
 *          throws ServletException {
 *
 *       Cookie[] cookies = req.getCookies();
 *       if (cookies != null &amp;&amp; cookies.length &gt; 0) {
 *          for (Cookie c : cookies) {
 *             // Check the condition
 *             if (...) {
 *                return new CustomImageRequestProcessor();
 *             }
 *          }
 *       }
 *
 *       // Use the default
 *       return new ImageRequestProcessor();
 *    }
 *
 *    private static final class CustomImageRequestProcessor extends ImageRequestProcessor {
 *
 *       // Custom code to add a branding, for example
 *
 *    }
 *
 * }
 * </pre>
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public interface ImageRequestProcessorFactory {

    /**
     * Creates a new (clean) image request processor to handle the given user
     * request.
     *
     * @param req The releated request object.
     *
     * @return The request processor to handle the given user request or
     * <code>null</code> if the factory is not able to provide a request
     * processor which can handle the given user request.
     *
     * @throws ServletException if an error occures while creating the new
     * request processor instance.
     *
     * @see ImageRequestProcessor
     * @see HttpServletRequest
     */
    public ImageRequestProcessor createRequestProcessor(HttpServletRequest req)
	    throws ServletException;

}
