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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class ScriptRequestProcessorTest {

    @Test
    public void testIsCacheable() {
	assertTrue(new ScriptRequestProcessor("test.js").isCacheable());
    }

    @Test
    public void testDoProcess() throws Exception {
	final ByteArrayOutputStream bos = new ByteArrayOutputStream();
	final ServletOutputStream sos = new ServletOutputStream() {

	    @Override
	    public void write(int b) throws IOException {
		bos.write(b);
	    }

	    public boolean isReady() {
		return true;
	    }

	    public void setWriteListener(WriteListener arg0) {
	    }
	};

	HttpServletRequest req = mock(HttpServletRequest.class);
	when(req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE")).thenReturn(1024);

	HttpServletResponse resp = mock(HttpServletResponse.class);
	when(resp.getOutputStream()).thenReturn(sos);

	ScriptRequestProcessor rp = new ScriptRequestProcessor("test.js");
	rp.setRequest(req);
	rp.setResponse(resp);

	rp.doProcess(req, resp);

	assertEquals("var x = 0;", bos.toString());
    }

}
