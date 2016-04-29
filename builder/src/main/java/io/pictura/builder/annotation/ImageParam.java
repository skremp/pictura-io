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
package io.pictura.builder.annotation;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used on a {@link PicturaURL} annotation to specify an
 * image parameter.
 * 
 * @author Steffen Kremp
 *
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD})
@Documented
public @interface ImageParam {
    
    /**
     * @return The image parameter name.
     */
    String name();

    /**
     * @return The image parameter value.
     */
    String value();
    
    /**
     * @return <code>true</code> if the value should join with an existing
     * value for the same parameter (name).
     */
    boolean join() default true;

}
