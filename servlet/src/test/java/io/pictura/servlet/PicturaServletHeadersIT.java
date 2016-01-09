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
import static io.pictura.servlet.PicturaServletIT.getHost;

/**
 * @author Steffen Kremp
 */
@PicturaServletIT.WebInitParams({
    @WebInitParam(name = PicturaServlet.IPARAM_HEADER_ADD_CONTENT_LOCATION, value = "true"),
    @WebInitParam(name = PicturaServlet.IPARAM_HEADER_ADD_TRUE_CACHE_KEY, value = "true"),
    @WebInitParam(name = PicturaServlet.IPARAM_RESOURCE_LOCATORS, value = "io.pictura.servlet.FileResourceLocator,io.pictura.servlet.HttpResourceLocator")
})
public class PicturaServletHeadersIT extends PicturaServletRemoteHttpIT {

    @Test
    public void testContentLocationHeader() throws Exception {
	System.out.println("contentLocationHeader");

	HttpURLConnection con = doGetResponse(new URL("http://" + getHost() + "/f=jpg/http://localhost:8194/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals("http://localhost:8194/lenna.jpg", con.getHeaderField("Content-Location"));
	assertNotNull(con.getHeaderField("X-Pictura-TrueCacheKey"));
	con.disconnect();
    }

    @Test
    public void testTrueCacheKeyHeader() throws Exception {
	System.out.println("trueCacheKeyHeader");

	HttpURLConnection con = doGetResponse(new URL("http://" + getHost() + "/f=jpg/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertNotNull(con.getHeaderField("X-Pictura-TrueCacheKey"));
	con.disconnect();
    }

    @Test
    public void testDebugHeaders() throws Exception {
	System.out.println("debugHeaders");

	HttpURLConnection con = doGetResponse(new URL("http://" + getHost() + "/f=jpg/http://localhost:8194/lenna.jpg?debug"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertNotNull(con.getHeaderField("X-Pictura-Lookup"));
	con.disconnect();

	con = doGetResponse(new URL("http://" + getHost() + "/f=jpg/lenna.jpg?debug"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertNotNull(con.getHeaderField("X-Pictura-Lookup"));
	con.disconnect();
    }

    @Override
    public void testRemoteLocatedResource() throws Exception {
    }

}
