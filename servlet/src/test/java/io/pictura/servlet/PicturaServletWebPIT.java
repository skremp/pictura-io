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
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static io.pictura.servlet.PicturaServletIT.getHost;

/**
 * @author Steffen Kremp
 */
public class PicturaServletWebPIT extends PicturaServletIT {

    @Test
    public void testGetImageWebPEncoded() throws Exception {
	System.out.println("getImageWebPEncoded");

	// Recall the plugin scanner in ImageRequestProcessor
	PicturaImageIO.scanForPlugins();

	HttpURLConnection con = null;
	InputStream is = null;
	try {
	    con = (HttpURLConnection) new URL("http://" + getHost() + "/f=webp/lenna.jpg").openConnection();
	    con.setRequestMethod("GET");
	    con.connect();

	    assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
	    assertTrue(con.getContentLength() > 1);
	    assertEquals("image/webp", con.getContentType());

	    BufferedImage image = ImageIO.read(is = con.getInputStream());
	    assertNotNull(image);

	    assertEquals(400, image.getWidth());
	    assertEquals(225, image.getHeight());

	} finally {
	    if (con != null) {
		if (is != null) {
		    try {
			is.close();
		    } catch (IOException ex) {
		    }
		}
		con.disconnect();
	    }
	}
    }

}
