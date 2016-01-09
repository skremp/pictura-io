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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class GIFSequenceWriterTest {

    @Test
    public void testWriteAnimation() throws Exception {

	PicturaImageIO.scanForPlugins();

	ImageRequestProcessor irp = new ImageRequestProcessor();
	ImageRequestProcessor irpSpy = spy(irp);

	HttpServletRequest req = mock(HttpServletRequest.class);
	when(req.getAttribute("io.pictura.servlet.IMAGEIO_USE_CACHE")).thenReturn(null);
	when(req.getAttribute("io.pictura.servlet.IMAGEIO_CACHE_DIR")).thenReturn(null);

	when(irpSpy.getRequest()).thenReturn(req);

	BufferedImage src = ImageIO.read(GIFSequenceWriterTest.class.getResource("/lenna.png"));

	BufferedImage[] frames = new BufferedImage[]{
	    Pictura.apply(src, Pictura.OP_INVERT),
	    Pictura.apply(src, Pictura.OP_SEPIA),
	    Pictura.apply(src, Pictura.OP_GRAYSCALE)
	};

	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	try (ImageOutputStream ios = irpSpy.createImageOutputStream(bos)) {
	    ImageWriter iw = irpSpy.createImageWriter(frames[0], "gif");
	    iw.setOutput(ios);

	    GIFSequenceWriter gsw = new GIFSequenceWriter(iw, frames[0].getType(), 15, true);
	    for (BufferedImage bi : frames) {
		gsw.writeToSequence(bi);
	    }
	    gsw.close();
	}

	assertTrue(bos.size() > 0);

	ImageInputStream iis;

	ImageReader ir = irpSpy.createImageReader(iis = irpSpy.createImageInputStream(
		new ByteArrayInputStream(bos.toByteArray())));

	ir.setInput(iis);

	GIFSequenceReader reader = new GIFSequenceReader(ir);
	BufferedImage[] framesVerify = reader.readAllFrames();

	assertEquals(3, framesVerify.length);
	assertEquals(15, reader.getDelayTime());
    }

}
