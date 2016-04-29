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
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * Annotation used to inject an PicturaIO image URL's. The producer is able
 * to inject {@code URL}'s, {@code URI}'s and {@code String}'s.
 * <p>
 * This annotation is processed by the CDI container at runtime.
 * </p>
 *
 * <p>Usage:</p>
 * <pre>
 * &#64;Inject
 * &#64;PicturaURL(endpoint = "http://localhost:8084/",
 *           imagePath = "lenna.png",
 *           imageParams = {
 *               &#64;ImageParam(name = "s", value = "w320"),
 *               &#64;ImageParam(name = "s", value = "dpr1.5"),
 *               &#64;ImageParam(name = "e", value = "AE")},
 *           queryParams = {
 *               &#64;QueryParam(name = "foo", value = "bar")
 *           })
 * private URL imageURL;
 * </pre>
 *
 * @author Steffen Kremp
 * 
 * @since 1.0
 *
 * @see ImageParam
 * @see QueryParam
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, METHOD})
@Documented
public @interface PicturaURL {

    /**
     * Specifies the service endpoint addresse. A valid non-empty address must
     * be given to produce a valid image URL.
     *
     * @return The service endpoint addresses.
     */
    @Nonbinding
    String endpoint() default "";

    /**
     * Specifies the origin image resource path. A valid non-empty image path
     * must be given to produce a valid image URL.
     *
     * @return The origin image resource path.
     */
    @Nonbinding
    String imagePath() default "";

    /**
     * Specifies the image request parameters.
     *
     * @return The image request parameters.
     *
     * @see ImageParam
     */
    @Nonbinding
    ImageParam[] imageParams() default {};

    /**
     * Specifies the image request query parameters.
     *
     * @return The image request query parameters.
     *
     * @see QueryParam
     */
    @Nonbinding
    QueryParam[] queryParams() default {};

    /**
     * Specifies the encoding to encode unsafe characters.
     *
     * @return The encoding to encode unsafe characters.
     */
    @Nonbinding
    String encoding() default "UTF-8";

}
