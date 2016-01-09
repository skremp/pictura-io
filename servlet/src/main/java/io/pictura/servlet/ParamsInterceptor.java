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

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * A <code>ParamsInterceptor</code> could be use to intercept and modify the
 * the request image parameters (depending on the associated request object) 
 * made by the client.
 * 
 * @author Steffen Kremp
 * 
 * @since 1.0
 */
public interface ParamsInterceptor extends Interceptor {
        
    /**
     * Returns a map with the modified image request parameters.
     * 
     * @param params The origin (non intercepted) parameters.
     * @param req The associated servlet request.
     * 
     * @return The intercepted parameters.
     */
    public Map<String, String> intercept(Map<String, String> params, HttpServletRequest req);
    
}
