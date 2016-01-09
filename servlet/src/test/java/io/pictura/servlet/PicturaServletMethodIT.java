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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class PicturaServletMethodIT extends PicturaServletIT {

    @Test
    public void testDoPost() throws Exception {
	System.out.println("doPost");

	HttpURLConnection con = null;
	try {
	    con = (HttpURLConnection) new URL("http://" + getHost() + "/lenna.jpg").openConnection();
	    con.setRequestMethod("POST");
	    con.connect();

	    assertEquals(HttpURLConnection.HTTP_BAD_METHOD, con.getResponseCode());
	} finally {
	    if (con != null) {
		con.disconnect();
	    }
	}
    }

    @Test
    public void testDoHead() throws Exception {
	System.out.println("doHead");

	HttpURLConnection con = null;
	try {
	    con = (HttpURLConnection) new URL("http://" + getHost() + "/lenna.jpg").openConnection();
	    con.setRequestMethod("HEAD");
	    con.connect();

	    assertEquals(HttpURLConnection.HTTP_BAD_METHOD, con.getResponseCode());
	} finally {
	    if (con != null) {
		con.disconnect();
	    }
	}
    }

    @Test
    public void testDoOptions() throws Exception {
	System.out.println("doOptions");

	HttpURLConnection con = null;
	try {
	    con = (HttpURLConnection) new URL("http://" + getHost() + "/lenna.jpg").openConnection();
	    con.setRequestMethod("OPTIONS");
	    con.connect();

	    assertEquals(HttpURLConnection.HTTP_BAD_METHOD, con.getResponseCode());
	} finally {
	    if (con != null) {
		con.disconnect();
	    }
	}
    }

    @Test
    public void testDoPut() throws Exception {
	System.out.println("doPut");

	HttpURLConnection con = null;
	try {
	    con = (HttpURLConnection) new URL("http://" + getHost() + "/lenna.jpg").openConnection();
	    con.setRequestMethod("PUT");
	    con.connect();

	    assertEquals(HttpURLConnection.HTTP_BAD_METHOD, con.getResponseCode());
	} finally {
	    if (con != null) {
		con.disconnect();
	    }
	}
    }

    @Test
    public void testDoDelete() throws Exception {
	System.out.println("doDelete");

	HttpURLConnection con = null;
	try {
	    con = (HttpURLConnection) new URL("http://" + getHost() + "/lenna.jpg").openConnection();
	    con.setRequestMethod("DELETE");
	    con.connect();

	    assertEquals(HttpURLConnection.HTTP_BAD_METHOD, con.getResponseCode());
	} finally {
	    if (con != null) {
		con.disconnect();
	    }
	}
    }

    @Test
    public void testDoTrace() throws Exception {
	System.out.println("doTrace");

	HttpURLConnection con = null;
	try {
	    con = (HttpURLConnection) new URL("http://" + getHost() + "/lenna.jpg").openConnection();
	    con.setRequestMethod("TRACE");
	    con.connect();

	    assertEquals(HttpURLConnection.HTTP_BAD_METHOD, con.getResponseCode());
	} finally {
	    if (con != null) {
		con.disconnect();
	    }
	}
    }

    @Test
    public void testDoGet() throws Exception {
	System.out.println("doGet");

	HttpURLConnection con = null;
	try {
	    con = (HttpURLConnection) new URL("http://" + getHost() + "/lenna.jpg").openConnection();
	    con.setRequestMethod("GET");
	    con.connect();

	    assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
	    assertTrue(con.getContentLength() > 1);
	    assertEquals("image/jpeg", con.getContentType());
	} finally {
	    if (con != null) {
		con.disconnect();
	    }
	}
    }

}
