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
 * A basic interface to describe a interceptor. Normally, a interceptor will
 * change the content of the intercepted response. Therefore it is necessary
 * to negotiate the true cache key if present. This interface provides a 
 * convenience methot to calculate a vary cache key depending on the origin
 * true cache key.
 * 
 * @author Steffen Kremp
 * 
 * @since 1.0
 */
public interface Interceptor {
    
    /**
     * Returns a new true cache key, based on the interceptor and the associated
     * servlet request.
     * 
     * @param trueCacheKey The origin (unmodified) true cache key.
     * @param req The associated servlet request.
     * 
     * @return A new true cache key based on this interceptor if necessary;
     * otherwise returns the unmodified true cache key.
     * 
     * @see HttpServletRequest
     */
    public String getVaryCacheKey(String trueCacheKey, HttpServletRequest req);
    
}
