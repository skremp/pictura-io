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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.annotation.WebInitParam;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import static io.pictura.servlet.PicturaServletIT.getHost;

/**
 * @author Steffen Kremp
 */
@WebInitParam(name = PicturaServlet.IPARAM_PLACEHOLDER_PRODUCER_ENABLED, value = "true")
public class PicturaServletPlaceholderRequestProcessorIT extends PicturaServletIT {

    @Test
    public void testPlaceholder_100x100() throws Exception {
        System.out.println("placeholder_100x100");

        BufferedImage image = doGetImage(new URL("http://" + getHost() + "/ip/d=100x100"), HttpURLConnection.HTTP_OK, "image/gif");

        assertNotNull(image);
        assertEquals(100, image.getWidth());
        assertEquals(100, image.getHeight());

        // Again; from placeholder cache
        image = doGetImage(new URL("http://" + getHost() + "/ip/d=100x100"), HttpURLConnection.HTTP_OK, "image/gif");

        assertNotNull(image);
        assertEquals(100, image.getWidth());
        assertEquals(100, image.getHeight());
    }

    @Test
    public void testPlaceholder_100x100_IfModifiedSince() throws Exception {
        System.out.println("placeholder_100x100_IfModifiedSince");

        HttpURLConnection con = null;
        InputStream is = null;
        try {
            DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("GMT"));

            con = (HttpURLConnection) (new URL("http://" + getHost() + "/ip/d=100x100")).openConnection();
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
    public void testPlaceholder_1x1() throws Exception {
        System.out.println("placeholder_1x1");

        BufferedImage image = doGetImage(new URL("http://" + getHost() + "/ip/d=1x1"), HttpURLConnection.HTTP_OK, "image/gif");

        assertNotNull(image);
        assertEquals(1, image.getWidth());
        assertEquals(1, image.getHeight());
    }

    @Test
    public void testPlaceholder_0x0() throws Exception {
        System.out.println("placeholder_0x0");

        BufferedImage image = doGetImage(new URL("http://" + getHost() + "/ip/f=png/d=0x0"), HttpURLConnection.HTTP_BAD_REQUEST, null);

        assertNull(image);
    }

    @Test
    public void testPlaceholder_0x1() throws Exception {
        System.out.println("placeholder_0x1");

        BufferedImage image = doGetImage(new URL("http://" + getHost() + "/ip/f=png/d=0x1"), HttpURLConnection.HTTP_BAD_REQUEST, null);

        assertNull(image);
    }

    @Test
    public void testPlaceholder_1x0() throws Exception {
        System.out.println("placeholder_1x0");

        BufferedImage image = doGetImage(new URL("http://" + getHost() + "/ip/f=png/d=1x0"), HttpURLConnection.HTTP_BAD_REQUEST, null);

        assertNull(image);
    }

    @Test
    public void testPlaceholder__1x0() throws Exception {
        System.out.println("placeholder_-1x1");

        BufferedImage image = doGetImage(new URL("http://" + getHost() + "/ip/f=png/d=-1x0"), HttpURLConnection.HTTP_BAD_REQUEST, null);

        assertNull(image);
    }

    @Test
    public void testPlaceholder_IllegalArgumentFormat() throws Exception {
        System.out.println("placeholder_IllegalArgumentFormat");

        BufferedImage image = doGetImage(new URL("http://" + getHost() + "/ip/f=png/d=12T22"), HttpURLConnection.HTTP_BAD_REQUEST, null);
        assertNull(image);

        image = doGetImage(new URL("http://" + getHost() + "/ip/f=png/v=12T22"), HttpURLConnection.HTTP_BAD_REQUEST, null);
        assertNull(image);

        image = doGetImage(new URL("http://" + getHost() + "/ip/f=png/d="), HttpURLConnection.HTTP_BAD_REQUEST, null);
        assertNull(image);

        image = doGetImage(new URL("http://" + getHost() + "/ip/f=png/d=120"), HttpURLConnection.HTTP_BAD_REQUEST, null);
        assertNull(image);
    }

    @Test
    public void testPlaceholder_300x100_Png() throws Exception {
        System.out.println("placeholder_300x100_Png");

        BufferedImage image = doGetImage(new URL("http://" + getHost() + "/ip/f=png/d=300x100"), HttpURLConnection.HTTP_OK, "image/png");

        assertNotNull(image);
        assertEquals(300, image.getWidth());
        assertEquals(100, image.getHeight());

        // Again; from placeholder cache
        image = doGetImage(new URL("http://" + getHost() + "/ip/f=png/d=300x100"), HttpURLConnection.HTTP_OK, "image/png");

        assertNotNull(image);
        assertEquals(300, image.getWidth());
        assertEquals(100, image.getHeight());
    }

}
