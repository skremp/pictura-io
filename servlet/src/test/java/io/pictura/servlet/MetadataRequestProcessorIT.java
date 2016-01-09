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

import com.google.gson.Gson;
import static io.pictura.servlet.PicturaServletIT.getHost;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.annotation.WebInitParam;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
@WebInitParam(name = PicturaServlet.IPARAM_REQUEST_PROCESSOR_STRATEGY,
	value = "io.pictura.servlet.MetadataRequestProcessor")
public class MetadataRequestProcessorIT extends PicturaServletIT {
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMetadataResponse() throws Exception {	
	HttpURLConnection con = null;
	try {
	    con = doGetResponse(new URL("http://" + getHost() + "/f=exif/lenna.jpg"), 
		HttpURLConnection.HTTP_OK, "application/json");
	
	    try (InputStream is = con.getInputStream()) {
		
		LinkedHashMap<String, LinkedHashMap<String, String>> tree = load(is, LinkedHashMap.class);
		
		Set<String> keySet = tree.keySet();		
		assertTrue(keySet.contains("JFIF"));
		assertTrue(keySet.contains("JPEG"));
		
		Map<String, String> jfif = tree.get("JFIF");
				
		assertEquals("1.2", jfif.get("Version"));
	    }
	} finally {
	    if (con != null) {
		con.disconnect();
	    }
	}
    }
    
    public static <T> T load(final InputStream is, final Class<T> clazz) {
	return (new Gson()).fromJson(new BufferedReader(new InputStreamReader(is)), clazz);
    }
    
}
