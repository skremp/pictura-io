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
 * A <code>ImageInterceptor</code> could be use to intercept and modify an
 * image before the image will write out to the response stream.
 * 
 * @author Steffen Kremp
 * 
 * @since 1.0
 */
public interface ImageInterceptor extends Interceptor {
    
    /**
     * Performs a single interception operation on the given image.
     * 
     * @param img The image to intercept.
     * @param req The associated servlet request.
     * 
     * @return The intercepted image.
     * 
     * @see BufferedImage
     * @see HttpServletRequest
     */
    public BufferedImage intercept(BufferedImage img, HttpServletRequest req);
    
}
