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
import javax.servlet.http.HttpServletRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class ImageInterceptorChainTest {
    
    @Test
    public void testChain() throws Exception {        
        ImageInterceptorChain emptyChain = new ImageInterceptorChain();        
        assertEquals("test", emptyChain.getVaryCacheKey("test", null));
        
        ImageInterceptorChain chain = new ImageInterceptorChain(new ImageInterceptor0(), new ImageInterceptor1());
        assertEquals("default-interceptor0-interceptor1", chain.getVaryCacheKey("default", null));
        
        BufferedImage bimg = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        assertNotEquals(bimg, chain.intercept(bimg, null));
        assertEquals(110, chain.intercept(bimg, null).getWidth());
        assertEquals(110, chain.intercept(bimg, null).getHeight());
        assertEquals(bimg.getType(), chain.intercept(bimg, null).getType());
    }
    
    public static class ImageInterceptor0 implements ImageInterceptor {

        @Override
        public BufferedImage intercept(BufferedImage img, HttpServletRequest req) {
            return img;
        }

        @Override
        public String getVaryCacheKey(String trueCacheKey, HttpServletRequest req) {
            return trueCacheKey + "-interceptor0";
        }
        
    }
    
    public static class ImageInterceptor1 implements ImageInterceptor {

        @Override
        public BufferedImage intercept(BufferedImage img, HttpServletRequest req) {
            return new BufferedImage(img.getWidth() + 10, img.getHeight() + 10, img.getType());
        }

        @Override
        public String getVaryCacheKey(String trueCacheKey, HttpServletRequest req) {
            return trueCacheKey + "-interceptor1";
        }
        
    }
    
}
