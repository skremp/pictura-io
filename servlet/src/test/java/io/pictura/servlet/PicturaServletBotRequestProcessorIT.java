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
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
@WebInitParam(name = PicturaServlet.IPARAM_REQUEST_PROCESSOR, value = "io.pictura.servlet.BotRequestProcessor")
public class PicturaServletBotRequestProcessorIT extends PicturaServletIT {

    @Test
    public void testBotProcessor() throws Exception {
	System.out.println("botProcessor");

	HttpURLConnection con;

	con = createConnectionAndConnect(new URL("http://" + getHost() + "/f=jpg/o=50/lenna.jpg"),
		null, null);
	assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
	con.disconnect();

	con = createConnectionAndConnect(new URL("http://" + getHost() + "/f=jpg/o=50/lenna.jpg"),
		"Mozilla", null);
	assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
	con.disconnect();

	con = createConnectionAndConnect(new URL("http://" + getHost() + "/f=jpg/o=50/lenna.jpg"),
		null, "de-DE,de;q=0.8,en-US;q=0.6,en;q=0.4");
	assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
	con.disconnect();

	// GoogleBot
	con = createConnectionAndConnect(new URL("http://" + getHost() + "/f=jpg/o=50/lenna.jpg"),
		"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
		null);
	assertEquals(HttpURLConnection.HTTP_MOVED_PERM, con.getResponseCode());
	assertNotNull(con.getHeaderField("Location"));
	assertEquals("/pictura/lenna.jpg", con.getHeaderField("Location"));
	con.disconnect();

	// GoogleBot Proxy
	con = createConnectionAndConnect(new URL("http://" + getHost() + "/lenna.jpg"),
		"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
		null);
	assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
	con.disconnect();
    }

    private HttpURLConnection createConnectionAndConnect(URL url, String userAgent,
	    String acceptLanguage) throws Exception {

	HttpURLConnection con = (HttpURLConnection) url.openConnection();
	con.setRequestProperty("User-Agent", userAgent);
	con.setRequestProperty("Accept-Language", acceptLanguage);
	con.setRequestMethod("GET");
	con.setInstanceFollowRedirects(false);
	con.connect();
	return con;
    }

}
