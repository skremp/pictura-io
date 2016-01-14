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

import java.util.Properties;
import javax.imageio.ImageIO;
import javax.servlet.ServletContextEvent;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;

/**
 * @author Steffen Kremp
 */
public class IIOProviderContextListenerTest {
    
    // Set to @Ignore if there is already a WebP plugin installed
    @Test
    public void testListener() throws Exception {
        assertFalse(ImageIO.getImageReadersByMIMEType("image/webp").hasNext());
        
        Properties props = new Properties();
	props.load(PicturaServletIT.class.getResourceAsStream("/properties/test-runtime.properties"));
        
        LibWebp.addLibraryPath(System.getProperty("java.io.tmpdir"));
	LibWebp.addLibWebp(props.getProperty("it.libwebp"));
        
        ServletContextEvent evt = mock(ServletContextEvent.class);
        IIOProviderContextListener listener = new IIOProviderContextListener();
        
        listener.contextInitialized(evt);        
        assertTrue(ImageIO.getImageReadersByMIMEType("image/webp").hasNext());
        
        listener.contextDestroyed(evt);
        assertFalse(ImageIO.getImageReadersByMIMEType("image/webp").hasNext());
    }
    
}
