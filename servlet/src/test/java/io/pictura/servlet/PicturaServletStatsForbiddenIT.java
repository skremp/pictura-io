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

/**
 * @author Steffen Kremp
 */
@PicturaServletIT.WebInitParams({
    @WebInitParam(name = PicturaServlet.IPARAM_STATS_ENABLED, value = "true"),
    @WebInitParam(name = PicturaServlet.IPARAM_STATS_IP_ADDRESS_MATCH, value = "127.0.0.2, 8.10.0.64/8")
})
public class PicturaServletStatsForbiddenIT extends PicturaServletIT {

    @Test
    public void testForbidden() throws Exception {
	System.out.println("forbidden");

	HttpURLConnection con = null;
	try {
	    // Default (JSON)
	    con = (HttpURLConnection) new URL("http://" + getHost() + "/stats").openConnection();
	    con.setRequestMethod("GET");
	    con.connect();

	    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, con.getResponseCode());
	} finally {
	    if (con != null) {
		con.disconnect();
	    }
	}
    }

}
