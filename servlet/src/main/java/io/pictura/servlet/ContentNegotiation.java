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
 * Request processors who implements this interface are able to negotiate the
 * content depending on one or more request headers.
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public interface ContentNegotiation {

    /**
     * Tests whether the user has set the "bypass" query parameter. Usually this
     * parameter is used for debugging purposes to temporarily disable the
     * content negotiation for the specified request.
     *
     * @param req The servlet request.
     * @return <code>true</code> if the bypass query parameter is set; otherwise
     * <code>false</code>.
     */
    public boolean isBypassRequest(HttpServletRequest req);

}
