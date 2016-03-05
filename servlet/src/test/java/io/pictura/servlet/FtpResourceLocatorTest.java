/**
 * Copyright 2016 Steffen Kremp
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

import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class FtpResourceLocatorTest {
    
    @Test
    public void testGetResource() throws Exception {
	System.out.println("getResource");

	FtpResourceLocator locator = new FtpResourceLocator();

	assertNull(locator.getResource(null));
	assertNull(locator.getResource("http://foo.com/path/bar.jpg"));
	assertNull(locator.getResource("itunes://foo.com/path/bar.jpg"));
	assertNull(locator.getResource("file://C:/bar.jpg"));

	URL urlLoc1 = locator.getResource("ftp://foo.com/bar.jpg");
        assertEquals("ftp://foo.com/bar.jpg", urlLoc1.toExternalForm());
    }
    
}
