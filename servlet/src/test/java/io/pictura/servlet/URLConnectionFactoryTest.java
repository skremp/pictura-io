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

import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class URLConnectionFactoryTest {
    
    @Test
    public void testDefaultURLConnection() throws Exception {
	Properties props = new Properties();
	props.put("io.pictura.servlet.HTTP_CONNECT_TIMEOUT", 2000);
	props.put("io.pictura.servlet.HTTP_READ_TIMEOUT", 1500);
	props.put("io.pictura.servlet.HTTP_MAX_FORWARDS", 9);
		
	URLConnectionFactory factory = URLConnectionFactory.DefaultURLConnectionFactory.getDefault();
	URLConnection con = factory.newConnection(new URL("http://localhost:8080/foo/s=w100/image.jpg"), props);
	
	assertEquals(2000, con.getConnectTimeout());
	assertEquals(1500, con.getReadTimeout());
	assertEquals("9", con.getRequestProperty("Max-Forwards"));
	
	assertEquals("http://localhost:8080/foo/s=w100/image.jpg", con.getURL().toExternalForm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPropsNull() throws Exception {
        URLConnectionFactory.DefaultURLConnectionFactory.getDefault()
                .newConnection(new URL("http://localhost:8080/foo/s=w100/image.jpg"), null);
    }
    
}
