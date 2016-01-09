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

import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import static io.pictura.servlet.PicturaServletIT.getHost;

/**
 * @author Steffen Kremp
 */
public class PicturaServletPathIT extends PicturaServletIT {

    @Test
    public void testEmptyPath() throws Exception {
	System.out.println("emptyPath");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/"), HttpURLConnection.HTTP_BAD_REQUEST, null);

	assertNull(image);
    }

    @Test
    public void testFileProtocol() throws Exception {
	System.out.println("fileProtocol");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/file:///lenna.jpg"), HttpURLConnection.HTTP_BAD_REQUEST, null);

	assertNull(image);
    }

    @Test
    public void testProxyFileRequest() throws Exception {
	System.out.println("proxyFileRequest");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");

	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

}
