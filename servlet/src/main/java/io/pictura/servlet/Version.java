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

import java.util.Properties;

/**
 * Convenience class to provide a general API version info.
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public final class Version {

    private static final String VERSION_STRING;

    static {
	String version = "Unknown";
	try {
	    Properties props = new Properties();
	    props.load(Version.class.getResourceAsStream("version.properties"));
	    version = props.getProperty("version");
	} catch (Exception e) {
	    // noting to do
	}
	VERSION_STRING = version;
    }

    /**
     * @return Implementation version string.
     */
    public static String getVersionString() {
	return VERSION_STRING;
    }

    private Version() {
    }

}
