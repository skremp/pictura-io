/**
 * Copyright 2015 Steffen Kremp
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package io.pictura.servlet;

import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.annotation.WebInitParam;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
@WebInitParam(name = PicturaServlet.IPARAM_STATS_ENABLED, value = "true")
public class PicturaServletStatsIT extends PicturaServletIT {

    private boolean hasErrorCase;

    @Before
    public void setUpErrorCases() throws Exception {
        if (!hasErrorCase) {
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) new URL("http://" + getHost() + "/foobar.jpg").openConnection();
                con.setRequestMethod("GET");
                con.connect();

                if (HttpURLConnection.HTTP_OK == con.getResponseCode()) {
                    fail("Set up dummy error cases failed.");
                }

            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
            hasErrorCase = true;
        }
    }

    @Test
    public void testDoGet_Stats() throws Exception {
        System.out.println("doGet_Stats");

        HttpURLConnection con = null;
        try {
            // Default (JSON)
            con = (HttpURLConnection) new URL("http://" + getHost() + "/stats").openConnection();
            con.setRequestMethod("GET");
            con.connect();

            assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
            assertTrue(con.getContentType().startsWith("application/json"));
            assertTrue(con.getContentLength() > 1);

        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

}
