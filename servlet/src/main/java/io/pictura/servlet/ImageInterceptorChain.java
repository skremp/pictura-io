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

/**
 * Implementation of a chain to handle 1..n {@link ImageInterceptor}'s for the
 * same request. The chain himself is also a {@link ImageInterceptor} and
 * delegates the specified interceptors in a loop.
 * 
 * @see ImageInterceptor
 * 
 * @author Steffen Kremp
 * 
 * @since 1.0
 */
final class ImageInterceptorChain implements ImageInterceptor {

    private final ImageInterceptor[] chain;
                
    /**
     * Constructs a new interceptor chain for the specified 1..n image interceptors.
     * @param chain The image interceptors in the order to process.
     */
    ImageInterceptorChain(ImageInterceptor... chain) {
	this.chain = chain;
    }
    
    @Override
    public String getVaryCacheKey(String trueCacheKey, HttpServletRequest req) {
	String varyCacheKey = trueCacheKey;
        for (ImageInterceptor ii : chain) {
	    varyCacheKey = ii.getVaryCacheKey(varyCacheKey, req);
	}
	return varyCacheKey;
    }

    /**
     * Performs a multiple interception operation for the specified image
     * interceptors on this chain on the specified source image. The interception
     * order is the same as specified in the constructor of this instance.
     * 
     * @param img The image to intercept.
     * @param req The associated servlet request.
     * 
     * @return The intercepted image.
     */
    @Override
    public BufferedImage intercept(BufferedImage img, HttpServletRequest req) {
	BufferedImage img0 = img;
	for (ImageInterceptor ii : chain) {
	    img0 = ii.intercept(img, req);
	}
	return img0;
    }
    
}
