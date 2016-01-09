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
 * A resource locator who will always return <code>null</code> as resource,
 * independent from the given resource path.
 * <p>
 * There is currently only one use case to set this resource locator if the
 * {@link PicturaServlet} is used only as an image placeholder producer. In this
 * case if it is not desired to handle normal image requests, you can set the
 * <code>EmptyResourceLocator</code> to block all other requests than image
 * placeholder requests.
 *
 * @author Steffen Kremp
 *
 * @see ResourceLocator
 * @see PicturaServlet#IPARAM_RESOURCE_LOCATORS
 * @see PicturaServlet#IPARAM_PLACEHOLDER_PRODUCER_ENABLED
 *
 * @since 1.0
 */
public final class EmptyResourceLocator implements ResourceLocator {

    /**
     * Returns always <code>null</code>. See class documentation for more
     * details.
     *
     * @param path The requested resource path.
     * @return <code>null</code> for all paths.
     *
     * @throws MalformedURLException <b>Will never thrown by this
     * implementation.</b>
     */
    @Override
    public URL getResource(String path) throws MalformedURLException {
	return null;
    }

}
