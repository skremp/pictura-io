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
package io.pictura.builder;

import io.pictura.servlet.PicturaServletIT;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class URLBuilderIT extends PicturaServletIT {
    
    @Test
    public void testToImage() throws Exception {
        Image img = new URLBuilder("http://" + getHost())
                .setImagePath("/lenna.jpg")
                .setImageParameter("s", "w320")
                .setImageParameter("f", "png")
                .toImage();
        
        assertTrue(img instanceof BufferedImage);
        
        final BufferedImage bi = (BufferedImage) img;        
        
        assertEquals(320, bi.getWidth());
        assertEquals(180, bi.getHeight());
        
        assertNotEquals(Color.BLACK.getRGB(), bi.getRGB(0, 0));
        assertNotEquals(Color.WHITE.getRGB(), bi.getRGB(0, 0));
    }
    
}
