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
import org.junit.Test;
import static io.pictura.servlet.PicturaServletIT.getHost;

@WebInitParam(name = PicturaServlet.IPARAM_CACHE_CONTROL_HANDLER,
	value = "io.pictura.servlet.PicturaServletCacheControlIT$CacheControlHandlerImpl")
public class PicturaServletCacheControlIT extends PicturaServletIT {

    @Test
    public void testCacheControl() throws Exception {
	System.out.println("cacheControl");

	HttpURLConnection con;

	con = createConnectionAndConnect(new URL("http://" + getHost() + "/lenna.jpg"));
	assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
	assertEquals("public, max-age=65", con.getHeaderField("Cache-Control"));
	con.disconnect();
    }

    private HttpURLConnection createConnectionAndConnect(URL url) throws Exception {

	HttpURLConnection con = (HttpURLConnection) url.openConnection();
	con.setRequestMethod("GET");
	con.connect();
	return con;
    }

    public static class CacheControlHandlerImpl implements CacheControlHandler {

	@Override
	public String getDirective(String path) {
	    if (path.startsWith("lenna")) {
		return "public, max-age=65";
	    }
	    return null;
	}

    }

}
