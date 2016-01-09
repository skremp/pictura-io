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

import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.annotation.WebInitParam;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import static io.pictura.servlet.PicturaServletIT.getHost;

/**
 * @author Steffen Kremp
 */
@PicturaServletIT.WebInitParams({
    @WebInitParam(name = PicturaServlet.IPARAM_REQUEST_PROCESSOR_STRATEGY,
	    value = "io.pictura.servlet.BotRequestProcessor, io.pictura.servlet.AutoFormatRequestProcessor")
})
public class PicturaServletImageStrategyIT extends PicturaServletIT {

    @Before
    public void imageIOReScan() throws Exception {
	PicturaImageIO.scanForPlugins();
    }

    @Test
    public void testImageStrategy() throws Exception {
	System.out.println("imageStrategy");

	HttpURLConnection con;

	con = createConnectionAndConnect(new URL("http://" + getHost() + "/o=50/lenna.jpg"),
		null, null);
	assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
	assertEquals("image/jpeg", con.getContentType());
	assertNull(con.getHeaderField("Vary"));
	con.disconnect();

	con = createConnectionAndConnect(new URL("http://" + getHost() + "/o=50/lenna.jpg"),
		"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)", null);
	assertEquals(HttpURLConnection.HTTP_MOVED_PERM, con.getResponseCode());
	assertNotNull(con.getHeaderField("Location"));
	assertEquals("/pictura/lenna.jpg", con.getHeaderField("Location"));
	assertNull(con.getHeaderField("Vary"));
	con.disconnect();

	con = createConnectionAndConnect(new URL("http://" + getHost() + "/o=50/lenna.jpg"),
		"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
		"image/webp");
	assertEquals(HttpURLConnection.HTTP_MOVED_PERM, con.getResponseCode());
	assertNotNull(con.getHeaderField("Location"));
	assertEquals("/pictura/lenna.jpg", con.getHeaderField("Location"));
	assertNull(con.getHeaderField("Vary"));
	con.disconnect();

// Works only on systems with WebP support
//	con = createConnectionAndConnect(new URL("http://" + getHost() + "/o=50/lenna.jpg"),
//		"Mozilla/5.0", "image/webp");
//	assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
//	assertEquals("image/webp", con.getContentType());
//	assertEquals("Accept", con.getHeaderField("Vary"));
//	con.disconnect();
//
//	con = createConnectionAndConnect(new URL("http://" + getHost() + "/o=50/lenna.jpg?bypass"),
//		"Mozilla/5.0", "image/webp");
//	assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
//	assertEquals("image/jpeg", con.getContentType());
//	assertNull(con.getHeaderField("Vary"));
//	con.disconnect();
//
//	con = createConnectionAndConnect(new URL("http://" + getHost() + "/f=jpg/o=50/lenna.jpg"),
//		"Mozilla/5.0", "image/webp");
//	assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
//	assertEquals("image/jpeg", con.getContentType());
//	assertNull(con.getHeaderField("Vary"));
//	con.disconnect();
    }

    private HttpURLConnection createConnectionAndConnect(URL url, String userAgent,
	    String accept) throws Exception {

	HttpURLConnection con = (HttpURLConnection) url.openConnection();
	con.setRequestProperty("User-Agent", userAgent);
	con.setRequestProperty("Accept", accept);
	con.setRequestMethod("GET");
	con.setInstanceFollowRedirects(false);
	con.connect();
	return con;
    }

}
