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

import static io.pictura.servlet.PicturaServletIT.doGetImage;
import static io.pictura.servlet.PicturaServletIT.getHost;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class PicturaServletTrimIT extends PicturaServletIT {

    @Test
    public void testTrim() throws Exception {
	System.out.println("rotate_L");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/t=2/lenna-trim.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(399, image.getWidth());
	assertEquals(224, image.getHeight());
    }
    
}
