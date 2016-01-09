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

import java.net.HttpURLConnection;
import javax.servlet.http.HttpServletResponse;

/**
 * A handler to provide cache control directives for the value of the HTTP
 * response header.
 * <p>
 * For example, to set a general max-age of 60 seconds, the implementation looks
 * like:
 *
 * <pre>
 * public class DefaultCacheControlHandler implements CacheControlHandler {
 *
 *    public String getDirective(String path) {
 *       if (path != null) {
 *          return "public, max-age=60";
 *       }
 *       return null;
 *    }
 *
 * }
 * </pre>
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public interface CacheControlHandler {

    /**
     * Gets the cache control directive for the specified resource path. If
     * there is no directive available for the requested resource, the method
     * returns <code>null</code>. Also a directive will only set in cases of an
     * {@link HttpServletResponse#SC_OK} status code.
     * <p>
     * If the resource is located on a remote server and the used
     * {@link RequestProcessor} uses an {@link HttpURLConnection} to fetch the
     * resource the origin cache control header will be overwritten with the
     * directive by this implementation, however not if there was no custom
     * directive found.
     * </p>
     * <p>
     * If the directive contains a <code>max-age</code>, the value of the
     * maximum age is also used to calculate and set the <code>Expires</code>
     * header, where the value of the expiration date is <code>System.currentTimeMillis() +
     * (max-age * 1000)</code>.
     * </p>
     *
     * @param path The resource path.
     *
     * @return The cache control directive or <code>null</code> if there was no
     * directive found for the given resource path.
     */
    public String getDirective(String path);

}
