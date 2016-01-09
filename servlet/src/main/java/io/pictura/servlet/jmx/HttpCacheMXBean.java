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
package io.pictura.servlet.jmx;

import io.pictura.servlet.HttpCache;
import javax.management.MXBean;

/**
 * JMX descriptor for the {@link HttpCache} class.
 *
 * @see HttpCache
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
@MXBean
public interface HttpCacheMXBean {
    
    /**
     * @return The current number of entries in the cache instance or -1 if
     * there is currently no cache in use.
     */
    public int getSize();
    
    /**
     * @return The current cache hit rate from 0 till 1.0f (0..100%) or -1f if
     * there is currently no cache in use.
     */
    public float getHitRate();
    
}
