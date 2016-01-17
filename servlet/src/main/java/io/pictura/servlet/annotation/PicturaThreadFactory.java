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

import io.pictura.servlet.PicturaServlet;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ThreadFactory;

/**
 * Annotation used to set a custom {@link ThreadFactory} for a 
 * {@link PicturaServlet} instance.
 * <p>
 * <b>Note</b>: this is ignored if the servlet is configured to use the
 * container pool to execute requests.
 * </p>
 * 
 * <p>Usage:</p>
 * <pre>
 * &#64;WebServlet(...)
 * &#64;PicturaThreadFactory(...)
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
public @interface PicturaThreadFactory {
    
    /**
     * Returns the implementation class of a {@link ThreadFactory} which is
     * used to create new threads by the {@link PicturaServlet} servlet.
     * <p>
     * <b>NOTE:</b> The implementation class must provides a standard constructor.
     * </p>
     * 
     * @return The custom {@link ThreadFactory} to use.
     */
    Class<? extends ThreadFactory> value();
    
}
