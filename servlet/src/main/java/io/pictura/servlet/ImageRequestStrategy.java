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

import javax.servlet.http.HttpServletRequest;

/**
 * A image request strategy could use to make a decision if two or more types of
 * image request processors are in use.
 * 
 * <p>Example strategy to handle a custom image request processor depending
 * on the specified format operation:</p>
 * 
 * <pre>
 * public class ExifImageRequestProcessor extends ImageRequestProcessor implements
 *         ImageRequestStrategy {
 * 
 *   public boolean isPreferred(HttpServletRequest req) {
 *      ImageRequestProcessor irp = new ImageRequestProcessor();
 *      return "exif".equals(irp.getRequestParameter(req, QPARAM_NAME_FORMAT_NAME));
 *   }
 * 
 *   public ImageRequestProcessor createRequestProcessor() {
 *      return new ExifImageRequestProcessor();
 *   }
 * 
 *   // TODO: Override/implement your custom code
 * 
 * }
 * </pre>
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public interface ImageRequestStrategy {

    /**
     * Tests whether or not the implementation of this image request strategy is
     * preferred for the given request object.
     *
     * @param req The related request object.
     *
     * @return <code>true</code> if this image strategy is preferred to handle
     * the image request from the given request object; otherwise
     * <code>false</code>.
     */
    public boolean isPreferred(HttpServletRequest req);

    /**
     * Creates a new {@link ImageRequestProcessor} which will match the image
     * request strategy for this implementation.
     *
     * @return A new (clean) request processor to handle a image request for
     * "this" strategy.
     */
    public ImageRequestProcessor createRequestProcessor();

}
