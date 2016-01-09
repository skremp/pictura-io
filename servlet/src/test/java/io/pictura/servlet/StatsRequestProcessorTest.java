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
import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class StatsRequestProcessorTest {

    @Test
    public void testIsCacheable() {
        assertFalse(new StatsRequestProcessor(null).isCacheable());
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

        PicturaServlet servlet = new PicturaPostServlet() {

            private static final long serialVersionUID = 3109256773218160485L;

            @Override
            public String getServletVendor() {
                return "pictura-io-impl";
            }

        };

        ServletConfig config = mock(ServletConfig.class);
        ServletContext context = mock(ServletContext.class);

        when(context.getContextPath()).thenReturn("/");

        when(config.getServletContext()).thenReturn(context);
        when(config.getServletName()).thenReturn("pictura-test");
        when(config.getInitParameter(PicturaServlet.IPARAM_CORE_POOL_SIZE)).thenReturn("10");
        when(config.getInitParameter(PicturaServlet.IPARAM_KEEP_ALIVE_TIME)).thenReturn("2000");
        when(config.getInitParameter(PicturaServlet.IPARAM_MAX_POOL_SIZE)).thenReturn("10");
        when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_QUEUE_SIZE)).thenReturn("20");
        when(config.getInitParameter(PicturaServlet.IPARAM_WORKER_TIMEOUT)).thenReturn("2500");
        when(config.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED)).thenReturn("false");

        servlet.init(config);

        assertTrue(servlet.isAlive());

        StatsRequestProcessor rp = new StatsRequestProcessor(servlet);

        HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getContextPath()).thenReturn("/");
        when(req.getServletPath()).thenReturn("");
        when(req.getRequestURI()).thenReturn("/stats");
        when(req.getQueryString()).thenReturn(null);
        when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));
        when(req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE")).thenReturn(1024 * 100);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(resp.getOutputStream()).thenReturn(sos);

        rp.setRequest(req);
        rp.setResponse(resp);

        rp.doProcess(req, resp);

        String json = bos.toString();

        assertNotNull(json);
        assertTrue(json.contains(servlet.getServletID()));
        assertTrue(json.contains("pictura-test"));
        assertTrue(json.contains("pictura-io-impl"));
        assertFalse(json.contains("cache"));
    }

}
