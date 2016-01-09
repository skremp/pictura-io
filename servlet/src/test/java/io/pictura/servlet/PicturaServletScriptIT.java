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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static io.pictura.servlet.PicturaServletIT.getHost;
import static org.junit.Assert.assertFalse;

/**
 * @author Steffen Kremp
 */
public class PicturaServletScriptIT extends PicturaServletIT {

    @Test
    public void testDoGet_JS() throws Exception {
        System.out.println("doGet_JS");

        HttpURLConnection con = null;
        InputStream is = null;
        try {
            con = (HttpURLConnection) new URL("http://" + getHost() + "/js/pictura.js").openConnection();
            con.setRequestMethod("GET");
            con.connect();

            assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
            assertEquals("text/javascript;charset=UTF-8", con.getContentType());
            assertTrue(con.getContentLength() > 1L);
            assertTrue(con.getHeaderField("ETag") != null);

            is = con.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int len;
            byte[] buf = new byte[2048];

            while ((len = is.read(buf)) > -1) {
                bos.write(buf, 0, len);
            }

            String js = bos.toString();
            assertFalse(js.isEmpty());
        } finally {
            if (con != null) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
                con.disconnect();
            }
        }
    }

    @Test
    public void testDoGet_JSMin() throws Exception {
        System.out.println("doGet_JSMin");

        HttpURLConnection con = null;
        InputStream is = null;
        try {
            con = (HttpURLConnection) new URL("http://" + getHost() + "/js/pictura.min.js").openConnection();
            con.setRequestMethod("GET");
            con.connect();

            assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
            assertEquals("text/javascript;charset=UTF-8", con.getContentType());
            assertTrue(con.getContentLength() > 1L);
            assertTrue(con.getHeaderField("ETag") != null);

            is = con.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int len;
            byte[] buf = new byte[2048];

            while ((len = is.read(buf)) > -1) {
                bos.write(buf, 0, len);
            }

            String js = bos.toString();
            assertFalse(js.isEmpty());
        } finally {
            if (con != null) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
                con.disconnect();
            }
        }
    }

    @Test
    public void testDoGet_JS_NotFound() throws Exception {
        System.out.println("doGet_JSM_NotFound");

        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL("http://" + getHost() + "/js/picturafoo.min.js").openConnection();
            con.setRequestMethod("GET");
            con.connect();

            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, con.getResponseCode());

        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    @Test
    public void testDoGet_JS_IfModifiedSince() throws Exception {
        System.out.println("doGet_JS_IfModifiedSince");

        HttpURLConnection con = null;
        InputStream is = null;
        try {
            DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT"));

            con = (HttpURLConnection) new URL("http://" + getHost() + "/js/pictura.js").openConnection();
            con.setRequestProperty("If-Modified-Since", df.format(new Date(System.currentTimeMillis())));
            con.setRequestMethod("GET");
            con.connect();

            assertEquals(HttpURLConnection.HTTP_NOT_MODIFIED, con.getResponseCode());
        } finally {
            if (con != null) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
                con.disconnect();
            }
        }
    }

    @Test
    public void testDoGet_JS_IfNoneMatch() throws Exception {
        System.out.println("doGet_JS_IfNoneMatch");

        HttpURLConnection con = null;
        InputStream is = null;
        try {

            con = (HttpURLConnection) new URL("http://" + getHost() + "/js/pictura.js").openConnection();
            con.setRequestMethod("GET");
            con.connect();

            assertEquals(HttpURLConnection.HTTP_OK, con.getResponseCode());
            String eTag = con.getHeaderField("ETag");

            assertNotNull(eTag);
            con.disconnect();

            con = (HttpURLConnection) new URL("http://" + getHost() + "/js/pictura.js").openConnection();
            con.setRequestProperty("If-None-Match", eTag);
            con.setRequestMethod("GET");
            con.connect();

            assertEquals(HttpURLConnection.HTTP_NOT_MODIFIED, con.getResponseCode());
        } finally {
            if (con != null) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
                con.disconnect();
            }
        }
    }

}
