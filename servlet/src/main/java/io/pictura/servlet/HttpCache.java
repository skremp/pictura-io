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

import java.util.Set;

/**
 * Defines a basic HTTP I/O cache interface. An implementation of this 
 * interface must guarantee that the cache is thread safe.
 * 
 * @author Steffen Kremp
 * 
 * @see HttpCacheEntry
 * @see HttpCacheServlet 
 * 
 * @since 1.0
 */
public interface HttpCache {

    /**
     * Returns the cache entry with the associated cache key or <code>null</code>
     * if the cache does not contains an entry for the key.
     * 
     * @param key The associated key for the requested entry.
     * 
     * @return The cache entry or <code>null</code> if the cache does not 
     * contains an entry for the key.
     */
    public HttpCacheEntry get(String key);
    
    /**
     * Adds the specified cache entry to this cache instance. If the cache
     * already contains a cache entry with the specified key, the current
     * entry will replaced by the new one.
     * 
     * @param key Cache key to associate with the cache entry.
     * 
     * @param entry Cache entry to put to the cache.
     */    
    public void put(String key, HttpCacheEntry entry);
        
    /**
     * Removes the cache entry with the specified unique key.
     * 
     * @param key The key of the cache entry to remove.
     * 
     * @return <code>true</code> if the cache entry was a member of this cache
     * and was successfully removed; otherwise <code>false</code>.
     */
    public boolean remove(String key);
    
    /**
     * Returns a set of all cache keys.
     * @return All cache keys.
     */
    public Set<String> keySet();
        
}
