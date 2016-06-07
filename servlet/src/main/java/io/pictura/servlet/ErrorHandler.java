/**
 * Copyright 2016 Steffen Kremp
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface to handle customized error responses for any HTTP status code
 * designated as an error condition - that is, any 4xx or 5xx status.
 *
 * <p>Example:</p>
 *
 * <pre><code>
 * public class MyErrorHandler implements ErrorHandler {
 *
 *   public doHandle(HttpServletRequest req, HttpServletResponse resp,
 *          int sc, String msg) throws IOException {
 *
 *     if (sc == 404) {
 *       resp.setContentType("text/plain");
 *       resp.getWriter().write("Resource not found - 404");
 *       return true;
 *     }
 *     return false;
 *   }
 *
 * }
 * </code></pre>
 *
 * @author Steffen Kremp
 *
 * @since 1.2
 */
public interface ErrorHandler {

    /**
     * Called from the servlet service in cases of errors.
     *
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the servlet.
     * @param resp An {@link HttpServletResponse} object that contains the
     * response the servlet sends to the client.
     * @param sc The error status code (<code>-1</code> if unknown).
     * @param msg The descriptive message (could be empty or <code>null</code>).
     *
     * @return <code>true</code> if this error handler has handled the given
     * error; otherwise <code>false</code>. In cases of <code>false</code>,
     * {@link HttpServletResponse#sendError(int, java.lang.String)} is called.
     *
     * @throws IOException in cases of I/O errors.
     */
    public boolean doHandle(HttpServletRequest req, HttpServletResponse resp,
            int sc, String msg) throws IOException;

}
