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
import io.pictura.builder.annotation.InetProxy;
import io.pictura.builder.annotation.PicturaURL;
import java.awt.Image;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Producer implementation to handle {@link PicturaURL} injection points.
 * 
 * @author Steffen Kremp
 *
 * @since 1.0
 */
final class URLProducer {

    /**
     * Creates a new {@link URL} object with the specified values given by the
     * injection point annotations.
     *
     * @param ip The injection point.
     *
     * @return A new <code>URL</code> object with the specified parameters or
     * <code>null</code> if the precondition fails.
     *
     * @throws InjectionException if an error occurs.
     *
     * @see PicturaURL
     * @see ImageParam
     */
    @Produces
    @PicturaURL
    public URL injectPicturaURL(InjectionPoint ip) throws InjectionException {
        URL url = null;
        try {
            final URLBuilder builder = newBuilder(ip.getAnnotated().getAnnotation(PicturaURL.class));
            if (builder != null) {
                url = builder.toURL();
            }
        } catch (Throwable t) {
            throw new InjectionException(t);
        }
        return url;
    }

    @Produces
    @PicturaURL
    public URI injectPicturaURLAsURI(InjectionPoint ip) throws InjectionException {
        URI url = null;
        try {
            final URLBuilder builder = newBuilder(ip.getAnnotated().getAnnotation(PicturaURL.class));
            if (builder != null) {
                url = builder.toURI();
            }
        } catch (Throwable t) {
            throw new InjectionException(t);
        }
        return url;
    }

    @Produces
    @PicturaURL
    public String injectPicturaURLAsString(InjectionPoint ip) throws InjectionException {
        String url = null;
        try {
            final URLBuilder builder = newBuilder(ip.getAnnotated().getAnnotation(PicturaURL.class));
            if (builder != null) {
                url = builder.toString();
            }
        } catch (Throwable t) {
            throw new InjectionException(t);
        }
        return url;
    }
    
    @Produces
    @PicturaURL
    public Image injectPicturaURLAsImage(InjectionPoint ip) throws InjectionException {
        Image img = null;
        try {
            final URLBuilder builder = newBuilder(ip.getAnnotated().getAnnotation(PicturaURL.class));
            if (builder != null) {
                final InetProxy proxy = ip.getAnnotated().getAnnotation(InetProxy.class);
                img = builder.toImage(proxy != null 
                        ? new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.hostname(), proxy.port())) 
                        : null);
            }
        } catch (Throwable t) {
            throw new InjectionException(t);
        }
        return img;
    }

    // Helper method to create a new URLBuilder instance from the given 
    // PicturaURL annotation.
    private static URLBuilder newBuilder(PicturaURL purl) {
        if (purl != null && purl.endpoint() != null && !purl.endpoint().isEmpty()
                && purl.imagePath() != null && !purl.imagePath().isEmpty()) {

            return new URLBuilder(purl.endpoint())
                    .setEncoding(purl.encoding())
                    .setImagePath(purl.imagePath())
                    .setImageParameters(purl.imageParams())
                    .setQueryParameters(purl.queryParams());
        }
        return null;
    }

}
