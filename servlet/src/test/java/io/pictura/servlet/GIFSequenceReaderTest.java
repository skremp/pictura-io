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

import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class GIFSequenceReaderTest {

    @Test
    public void testReadAllFrames() throws Exception {
	try (InputStream is = GIFSequenceReaderTest.class.getResourceAsStream("/loader.gif")) {
	    PicturaImageIO.scanForPlugins();

	    ImageRequestProcessor irp = new ImageRequestProcessor();
	    ImageRequestProcessor irpSpy = spy(irp);

	    HttpServletRequest req = mock(HttpServletRequest.class);
	    when(req.getAttribute("io.pictura.servlet.IMAGEIO_USE_CACHE")).thenReturn(null);
	    when(req.getAttribute("io.pictura.servlet.IMAGEIO_CACHE_DIR")).thenReturn(null);

	    when(irpSpy.getRequest()).thenReturn(req);

	    ImageInputStream iis;

	    ImageReader ir = irpSpy.createImageReader(iis = irpSpy.createImageInputStream(is));
	    ir.setInput(iis);

	    GIFSequenceReader reader = new GIFSequenceReader(ir);
	    BufferedImage[] frames = reader.readAllFrames();

	    assertEquals(8, frames.length);
	    assertEquals(6, reader.getDelayTime());
	}
    }

}
