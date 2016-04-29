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

import java.awt.Image;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used on a {@link PicturaURL} annotation to specify a
 * proxy server in cases of remote located {@link Image} injections.
 * 
 * @author Steffen Kremp
 *
 * @since 1.0
 * 
 * @see PicturaURL
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD})
@Documented
public @interface InetProxy {

    /**
     * A socket address host name.
     * @return The host name.
     */
    String hostname();
    
    /**
     * A socket address port number.
     * @return The port number.
     */
    int port();
    
}
