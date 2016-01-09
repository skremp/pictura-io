/**
 * Copyright 2016 Steffen Kremp
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

import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class StrategyRequestProcessorTest {
    
    @Test
    public void testStrategyRequestProcessor() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);

	when(req.getContextPath()).thenReturn("/");
	when(req.getServletPath()).thenReturn("");
	when(req.getRequestURI()).thenReturn("/f=exif/lenna.jpg");
	when(req.getQueryString()).thenReturn(null);
	when(req.getParameterNames()).thenReturn(Collections.enumeration(new ArrayList<String>(0)));
	when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_FILE_SIZE")).thenReturn(1024L * 1024L);
	when(req.getAttribute("io.pictura.servlet.MAX_IMAGE_RESOLUTION")).thenReturn(1000L * 1000L);
	when(req.getAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE")).thenReturn(1024 * 100);
        
        final StrategyRequestProcessor rp = new StrategyRequestProcessor() {
            @Override
            public boolean isPreferred(HttpServletRequest req) {
                return false;
            }

            @Override
            public ImageRequestProcessor createRequestProcessor() {
                return null;
            }
        };
        
        rp.setRequest(req);
        
        ImageRequestProcessor irp = rp.getBaseRequestProcessor(req);
        assertNotNull(irp);
        assertNotSame(rp, irp);
        
        rp.runFinalize();
        assertNotSame(irp, rp.getBaseRequestProcessor(req));
    }
    
}
