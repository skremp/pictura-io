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

/**
 * Specifies whether a request should be cached.
 * 
 * @author Steffen Kremp
 * 
 * @since 1.0
 */
public interface Cacheable {

    /**
     * Returns <code>true</code> if the response is cacheable; otherwise 
     * <code>false</code>.
     * <p>
     * Generally means it is not a unique selling point and you have to obtain
     * the cache-control headers, too.
     *
     * @return <code>true</code> if the produced response data is cacheable;
     * otherwise <code>false</code>.
     */
    public boolean isCacheable();
    
}
