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

/**
 * Annotation used to set the path to the {@link PicturaServlet} configuration
 * file.
 * 
 * <p>Usage:</p>
 * <pre>
 * &#64;WebServlet(...)
 * &#64;PicturaConfigFile("image-servlet-config.xml")
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
public @interface PicturaConfigFile {
    
    /**
     * Returns the path to the external {@link PicturaServlet} configuration
     * file.
     * 
     * @return The path to the external Pictura configuration file.
     */
    String value();
    
}
