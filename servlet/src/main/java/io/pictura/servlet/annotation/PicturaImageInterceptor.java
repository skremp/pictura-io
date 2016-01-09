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
package io.pictura.servlet.annotation;

import io.pictura.servlet.IIORequestProcessor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import io.pictura.servlet.ImageInterceptor;

/**
 * Annotation used to set a default {@link ImageInterceptor} to intercept outbound
 * images with custom operations.
 * 
 * <p>Usage:</p>
 * <pre>
 * &#64;WebServlet(...)
 * &#64;PicturaImageInterceptor(...)
 * public class ImageServlet extends PicturaServlet {
 * }
 * </pre>
 * 
 * @author Steffen Kremp
 * 
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface PicturaImageInterceptor {
    
    /**
     * Returns the implementation class of an {@link ImageInterceptor} to 
     * intercept outbound images. If set, this will also include proxy requests.
     * <p>
     * For each {@link IIORequestProcessor} which is equals to a single user
     * request, a new interceptor will create.
     * </p>
     * <p>
     * <b>NOTE:</b> The implementation class must provides a standard constructor.
     * </p>
     * 
     * @return The outbound image interceptor(s).
     */
    Class<? extends ImageInterceptor>[] value();
    
}
