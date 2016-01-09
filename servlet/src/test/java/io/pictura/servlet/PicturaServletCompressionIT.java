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
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static io.pictura.servlet.PicturaServletIT.getHost;

/**
 * @author Steffen Kremp
 */
public class PicturaServletCompressionIT extends PicturaServletIT {

    private int baseJpgImageSize = -1;

    @Before
    public void setUpComparableImage() throws Exception {
	if (baseJpgImageSize == -1) {
	    HttpURLConnection conJpg = doGetResponse(new URL("http://" + getHost() + "/f=jpg/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	    conJpg.disconnect();
	    baseJpgImageSize = conJpg.getContentLength();
	    assertTrue(baseJpgImageSize > 0);
	}
    }

    @Test
    public void testCompression_JPG_O_80() throws Exception {
	System.out.println("compression_JPG_O_80");

	HttpURLConnection con = doGetResponse(new URL("http://" + getHost() + "/f=jpg/o=80/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	con.disconnect();
	assertTrue(con.getContentLength() > 0 && con.getContentLength() < baseJpgImageSize);
    }

    @Test
    public void testCompression_JPG_O_50() throws Exception {
	System.out.println("compression_JPG_O_50");

	HttpURLConnection con = doGetResponse(new URL("http://" + getHost() + "/f=jpg/o=50/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	con.disconnect();
	assertTrue(con.getContentLength() > 0 && con.getContentLength() < (baseJpgImageSize / 2));
    }

}
