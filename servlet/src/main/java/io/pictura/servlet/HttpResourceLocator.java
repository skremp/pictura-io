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
import java.util.Locale;

/**
 * An {@link ResourceLocator} implementation to lookup remote (http, https)
 * located resources.
 *
 * @see ResourceLocator
 * @see FileResourceLocator
 * @see FtpResourceLocator
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public class HttpResourceLocator implements ResourceLocator {

    @Override
    public URL getResource(String path) throws MalformedURLException {
	if (path != null && !path.isEmpty()) {
	    String str = path.toLowerCase(Locale.ENGLISH);
	    if (str.startsWith("http://") || str.startsWith("https://")) {
		return new URL(path);
	    }
	}
	return null;
    }

}
