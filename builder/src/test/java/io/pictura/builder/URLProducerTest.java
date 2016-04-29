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
package io.pictura.builder;

import io.pictura.builder.annotation.ImageParam;
import io.pictura.builder.annotation.PicturaURL;
import io.pictura.builder.annotation.QueryParam;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.enterprise.inject.InjectionException;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(URLProducer.class)
public class URLProducerTest {

    @Inject
    @PicturaURL(endpoint = "http://localhost:8084/",
            imagePath = "lenna.png",
            imageParams = {
                @ImageParam(name = "s", value = "w320"),
                @ImageParam(name = "s", value = "dpr3.5"),
                @ImageParam(name = "e", value = "AE")})
    private URL testURL;

    @Inject
    @PicturaURL(endpoint = "/img",
            imagePath = "lenna.png",
            imageParams = {
                @ImageParam(name = "s", value = "w320"),
                @ImageParam(name = "s", value = "dpr3.5"),
                @ImageParam(name = "e", value = "AE"),
                @ImageParam(name = "e", value = "gl", join = false)})
    private URI testURLAsURI;

    @Inject
    @PicturaURL(endpoint = "https://localhost:8084/",
            imagePath = "lenna.png",
            imageParams = {
                @ImageParam(name = "s", value = "w320"),
                @ImageParam(name = "s", value = "dpr3.5"),
                @ImageParam(name = "e", value = "AE"),
                @ImageParam(name = "e", value = "i")},
            queryParams = {
                @QueryParam(name = "foo", value = "bar")
            })
    private String testURLAsString;   

    @Inject
    @PicturaURL
    private URL testNullURL;

    @Test
    public void testInjectionPoints() throws Exception {
        assertNotNull(testURL);
        assertEquals("http://localhost:8084/s=w320,dpr3.5/e=ae/lenna.png", testURL.toExternalForm());

        assertNotNull(testURLAsURI);
        assertEquals("/img/s=w320,dpr3.5/e=gl/lenna.png", testURLAsURI.toString());

        assertNotNull(testURLAsString);
        assertEquals("https://localhost:8084/s=w320,dpr3.5/e=ae,i/lenna.png?foo=bar", testURLAsString);

        assertNull(testNullURL);
    }

    @Test(expected = InjectionException.class)
    public void testInjectPicturaURL() throws Exception {
        URLProducer p = new URLProducer();

        Annotated a = mock(Annotated.class);
        when(a.getAnnotation(PicturaURL.class)).thenReturn(new PicturaURL() {

            @Override
            public String endpoint() {
                return "/img";
            }

            @Override
            public String imagePath() {
                return "lenna.jpg";
            }

            @Override
            public ImageParam[] imageParams() {
                return new ImageParam[]{
                    new ImageParam() {
                        @Override
                        public String name() {
                            return "s";
                        }

                        @Override
                        public String value() {
                            return "w320";
                        }

                        @Override
                        public boolean join() {
                            return false;
                        }

                        @Override
                        public Class<? extends Annotation> annotationType() {
                            return null;
                        }
                    }
                };
            }

            @Override
            public QueryParam[] queryParams() {
                return new QueryParam[0];
            }

            @Override
            public String encoding() {
                return "UTF-8";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }
        });

        InjectionPoint ip = mock(InjectionPoint.class);
        when(ip.getAnnotated()).thenReturn(a);
        
        p.injectPicturaURL(ip);
    }    

}
