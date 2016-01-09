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
import javax.servlet.annotation.WebInitParam;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import static io.pictura.servlet.PicturaServletIT.getHost;

/**
 * @author Steffen Kremp
 */
@WebInitParam(name = PicturaServlet.IPARAM_MAX_IMAGE_EFFECTS, value = "2")
public class PicturaServletMaxImageEffectsIT extends PicturaServletIT {

    @Test
    public void testMaxImageEffects() throws Exception {
	System.out.println("maxImageEffects");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/e=g/lenna.jpg"),
		HttpURLConnection.HTTP_OK, null);
	assertNotNull(image);
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/e=g,a/lenna.jpg"),
		HttpURLConnection.HTTP_OK, null);
	assertNotNull(image);
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());

	image = doGetImage(new URL("http://" + getHost() + "/e=g,a,i/lenna.jpg"),
		HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);

	image = doGetImage(new URL("http://" + getHost() + "/e=g,a,i,s/lenna.jpg"),
		HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
    }

}
