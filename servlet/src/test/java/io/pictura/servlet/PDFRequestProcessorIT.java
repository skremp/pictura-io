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

import static io.pictura.servlet.PicturaServletIT.doGetResponse;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.annotation.WebInitParam;
import org.apache.pdfbox.pdfparser.PDFParser;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
@WebInitParam(name = PicturaServlet.IPARAM_REQUEST_PROCESSOR_STRATEGY,
	value = "io.pictura.servlet.PDFRequestProcessor")
public class PDFRequestProcessorIT extends PicturaServletIT {
    
    @Test
    public void testMetadataResponse() throws Exception {	
	HttpURLConnection con = null;
	try {
	    con = doGetResponse(new URL("http://" + getHost() + "/f=pdf/lenna.jpg"), 
		HttpURLConnection.HTTP_OK, "application/pdf");
	
	    try (InputStream is = con.getInputStream()) {		
		PDFParser p = new PDFParser(is);
		p.parse();
		assertNotNull(p.getDocument());
                p.clearResources();
	    }
	} finally {
	    if (con != null) {
		con.disconnect();
	    }
	}
    }
    
}
