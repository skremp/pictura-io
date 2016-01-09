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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A <code>ResourceLocator</code> is used to locate a requested resource
 * (lookup). In this connection it is irrelevant where the resource is located;
 * for example on the local disk or on a remote location. The implementation of
 * this interface will (if the resource could be located) return a valid URL
 * object which will contains the full qualified path to the requested resource.
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public interface ResourceLocator {

    /**
     * Returns an URL to the resource that is mapped to the given path.
     * <p>
     * This method returns <tt>null</tt> if no resource was found.
     * 
     * @param path A <code>String</code> specifying the path to the resource.
     * @return The resource located at the named path, or <tt>null</tt> if there
     * is no resource at that path.
     * @throws MalformedURLException if the pathname is not given in the correct
     * form.
     *
     * @see URL
     */
    public URL getResource(String path) throws MalformedURLException;

}
