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
import io.pictura.builder.annotation.QueryParam;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * A builder pattern for generating URLs with <b>PicturaIO</b>.
 *
 * <p>
 * Usage:</p>
 * <pre>
 * URLBuilder builder = new URLBuilder("http", "localhost", 8080, "/pictura")
 * .setEncoding("ISO-8859-1")
 * .setImagePath("/lenna.png")
 * .setImageParameter("s", "w80p")
 * .joinImageParameter("s", "dpr1.5")
 * .setQueryParameter("rnd", "123456789");
 *
 * URL url = builder.toURL();
 * URI uri = builder.toURI();
 * String s = builder.toString();
 * </pre>
 *
 * <p>
 * Shorthand constructor:</p>
 * <pre>
 * ... = new URLBuilder("http", "localhost", 8080, "/pictura");
 * </pre> is equals to
 * <pre>
 * ... = new URLBuilder("http://localhost:8080/pictura");
 * </pre>
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 *
 * @see Cloneable
 */
public class URLBuilder implements Cloneable {

    // The default URL encoding if nothing else was specified
    private static final String DEFAULT_ENCODING = "UTF-8";

    static {
        // Scan for available Image I/O plugins to handle
        // #toImage(...) convenience methods
        ImageIO.scanForPlugins();
    }    
        
    // Service endpoint(s)
    private final String[] shardEndpoints;
    private int shardCycleNextIndex = -1;

    // The path to the origin image resource
    private String imagePath;

    // The image request parameters
    private Map<String, String> imageParams;
    private Map<String, String> queryParams;

    // URL encoding
    private String encoding = DEFAULT_ENCODING;

    /**
     * Constructs a new <code>URLBuilder</code> instance with the specified
     * service endpoint address.
     *
     * @param endpoint The service endpoint address (e.h.
     * http://localhost:8084/img)
     *
     * @throws IllegalArgumentException if the given endpoint is
     * <code>null</code> or <code>empty</code>.
     */
    public URLBuilder(String endpoint) throws IllegalArgumentException {
        this(new String[]{endpoint});
    }

    /**
     * Constructs a new <code>URLBuilder</code> instance with the specified
     * service endpoint addresses.
     * <p>
     * If the shard endpoints contains more than 1 address, the addresses are
     * use in a cycle rotation for each {@link #build()} call.
     * </p>
     *
     * @param shardEndpoints The service endpoint addresses.
     *
     * @throws IllegalArgumentException if the given endpoint is
     * <code>null</code> or <code>empty</code>.
     */
    public URLBuilder(String... shardEndpoints) throws IllegalArgumentException {
        for (int i = 0; i < shardEndpoints.length; i++) {
            if (shardEndpoints[i] == null || shardEndpoints[i].isEmpty()) {
                throw new IllegalArgumentException("Service endpoint at index "
                        + i + " must be not null nor empty");
            }
        }
        this.shardEndpoints = shardEndpoints;
    }

    /**
     * Constructs a new <code>URLBuilder</code> instance with the specified host
     * (including protocol and path) as service endpoint address.
     *
     * @param protocol The name of the protocol to use.
     * @param host The name of the host.
     * @param path The path (e.g. context path) on the host.
     *
     * @throws IllegalArgumentException if an unknown protocol is specified.
     *
     * @see #URLBuilder(java.lang.String, java.lang.String, int,
     * java.lang.String)
     */
    public URLBuilder(String protocol, String host, String path)
            throws IllegalArgumentException {

        this(protocol, host, -1, path);
    }

    /**
     * Constructs a new <code>URLBuilder</code> instance with the specified host
     * (including protocol, port number and path) as service endpoint address.
     *
     * @param protocol The name of the protocol to use.
     * @param host The name of the host.
     * @param port The port number on the host.
     * @param path The path (e.g. context path) on the host.
     *
     * @throws IllegalArgumentException if an unknown protocol is specified.
     *
     * @see #URLBuilder(java.lang.String, java.lang.String, java.lang.String)
     */
    public URLBuilder(String protocol, String host, int port, String path)
            throws IllegalArgumentException {

        try {
            this.shardEndpoints = new String[]{
                port > -1
                ? new URL(protocol, host, port, path).toString()
                : new URL(protocol, host, path).toString()
            };
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // Copy constructor to clone the given instance
    URLBuilder(URLBuilder builder) {
        this.encoding = builder.encoding;
        this.imagePath = builder.imagePath;

        this.shardEndpoints = new String[builder.shardEndpoints.length];
        System.arraycopy(builder.shardEndpoints, 0, this.shardEndpoints, 0, this.shardEndpoints.length);

        this.imageParams = cloneParamsMap(builder.imageParams);
        this.queryParams = cloneParamsMap(builder.queryParams);
    }

    // Helper method to clone a given <String,String> map
    private Map<String, String> cloneParamsMap(Map<String, String> m) {
        if (m != null) {
            LinkedHashMap<String, String> newMap = new LinkedHashMap<>(m.size());
            Iterator<String> iterKeys = m.keySet().iterator();

            while (iterKeys.hasNext()) {
                String k = iterKeys.next();
                String v = m.get(k);

                newMap.put(k, v);
            }

            return newMap;
        }
        return null;
    }
    
    /**
     * Sets the URL encoding to use to encode the unsafe characters. If the
     * specified encoding is <code>null</code> the default encoding (UTF-8) is
     * use.
     *
     * @param encoding The encoding to use.
     *
     * @return This builder instance.
     *
     * @throws IllegalArgumentException If the given encoding is not supported
     */
    public URLBuilder setEncoding(String encoding) throws IllegalArgumentException {
        if (encoding != null) {
            if (encoding.isEmpty()) {
                throw new IllegalArgumentException("Encoding name must be not empty");
            } else if (!Charset.isSupported(encoding)) {
                throw new IllegalArgumentException("Unsupported encoding \"" + encoding + "\"");
            }
        }
        this.encoding = encoding == null ? DEFAULT_ENCODING : encoding;
        return this;
    }

    /**
     * Sets the image path. This could be a relative path to the specified
     * endpoint or an absolute url (e.g. an external located image resource).
     *
     * @param path Path to the origin image resource.
     *
     * @return This builder instance.
     *
     * @throws IllegalArgumentException if the path is not null but is an empty
     * string.
     */
    public URLBuilder setImagePath(String path) throws IllegalArgumentException {
        if (path != null) {
            if (path.isEmpty()) {
                throw new IllegalArgumentException("Image path must be not empty");
            }
        }
        this.imagePath = path;
        return this;
    }

    URLBuilder setImageParameters(ImageParam... params) throws IllegalArgumentException {
        if (params != null && params.length > 0) {
            for (ImageParam ip : params) {
                putImageParameter(ip.name(), ip.value(), ip.join());
            }
        }
        return this;
    }

    /**
     * Associates the specified value with the specified parameter name in the
     * <code>URLBuilder</code> path parameter map. If the value is
     * <code>null</code> this removes the mapping for the parameter name from
     * this <code>URLBuilder</code> path parameter map if it is present.
     *
     * @param name Name with which the specified value is to be associated
     * @param value Value to be associated with the specified name
     *
     * @return This builder instance.
     *
     * @throws IllegalArgumentException if the name is <code>null</code> or
     * <code>empty</code> or the name contains an illegal character.
     *
     * @see #joinImageParameter(java.lang.String, java.lang.String)
     */
    public URLBuilder setImageParameter(String name, String value) throws IllegalArgumentException {
        return putImageParameter(name, value, false);
    }

    /**
     * Associates the specified value with the specified parameter name in the
     * <code>URLBuilder</code> path parameter map. If the parameter is already
     * present, this joins the parameter value with the existing value.
     *
     * @param name Name with which the specified value is to be associated
     * @param value Value to be associated with the specified name
     *
     * @return This builder instance.
     *
     * @throws IllegalArgumentException if the name is <code>null</code> or
     * <code>empty</code> or the name contains an illegal character or the value
     * is <code>null</code>.
     *
     * @see #setImageParameter(java.lang.String, java.lang.String)
     */
    public URLBuilder joinImageParameter(String name, String value) throws IllegalArgumentException {
        return putImageParameter(name, value, true);
    }

    /**
     * Associates the specified value with the specified parameter name in the
     * <code>URLBuilder</code> path parameter map. If the value is
     * <code>null</code> and <code>join</code> is <code>false</code> this
     * removes the mapping for the parameter name from this
     * <code>URLBuilder</code> path parameter map if it is present.
     *
     * @param name Name with which the specified value is to be associated
     * @param value Value to be associated with the specified name
     * @param join If <code>true</code> this joins the value to an existing
     * value for the specified name.
     *
     * @return This builder instance.
     *
     * @throws IllegalArgumentException if the name is <code>null</code> or
     * <code>empty</code> or the name contains an illegal character or the value
     * is <code>null</code> but join was set to <code>true</code>.
     *
     * @see #setImageParameter(java.lang.String, java.lang.String)
     * @see #joinImageParameter(java.lang.String, java.lang.String)
     */
    protected URLBuilder putImageParameter(String name, String value, boolean join) throws IllegalArgumentException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Image parameter name must be not blank");
        } else if (!name.matches("[a-zA-Z0-9]{1,}")) {
            throw new IllegalArgumentException("Image parameter \"" + name + "\" is not a valid parameter name");
        } else if ((value != null && value.isEmpty()) || (join && value == null)) {
            throw new IllegalArgumentException("Image parameter value to join must be not blank");
        } else if ("i".equalsIgnoreCase(name)) {
            throw new IllegalArgumentException("Use io.pictura.builder.URLBuilder#setImagePath to set the path to the image to process");
        }

        // Create a new map if necessary
        if (imageParams == null) {
            imageParams = new LinkedHashMap<>();
        }

        // A "null" value means remove the specified parameter
        if (value == null && !join) {
            imageParams.remove(name);
        } else if (join) {
            final String oldValue = imageParams.get(name);
            imageParams.put(name, oldValue != null ? oldValue + "," + value : value);
        } else {
            imageParams.put(name, value);
        }

        return this;
    }

    URLBuilder setQueryParameters(QueryParam... params) throws IllegalArgumentException {
        if (params != null && params.length > 0) {
            for (QueryParam qp : params) {
                setQueryParameter(qp.name(), qp.value());
            }
        }
        return this;
    }

    /**
     * Associates the specified value with the specified query parameter name in
     * the <code>URLBuilder</code> query parameter map. If the value is
     * <code>null</code> this removes the mapping for the parameter name from
     * this <code>URLBuilder</code> path parameter map if it is present.
     *
     * @param name Name with which the specified value is to be associated
     * @param value Value to be associated with the specified name
     *
     * @return This builder instance.
     *
     * @throws IllegalArgumentException if the name is <code>null</code> or
     * <code>empty</code> or the name contains an illegal character.
     */
    public URLBuilder setQueryParameter(String name, String value) throws IllegalArgumentException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Parameter name must be not blank");
        } else if (!name.matches("[a-zA-Z0-9]{1,}")) {
            throw new IllegalArgumentException("Parameter \"" + name + "\" is not a valid parameter name");
        }

        if (queryParams == null) {
            queryParams = new LinkedHashMap<>();
        }

        if (value == null) {
            queryParams.remove(name);
        } else {
            queryParams.put(name, value);
        }

        return this;
    }    
    
    /**
     * Resets this URL builder instance. This will clears all image parameters
     * and optional query parameters as well as the image path however, not
     * the service endpoint address and other settings like the URL encoding.
     */
    public void reset() {
        this.imagePath = null;        

        // Reset the image processing request parameters
        if (imageParams != null) {
            imageParams.clear();
        }

        // Reset the optional URL query parameters
        if (queryParams != null) {
            queryParams.clear();
        }
    }

    /**
     * @return A new (cloned) <code>URLBuilder</code> instance.
     * 
     * @throws CloneNotSupportedException if it is not possible to clone this
     * instance or this type.
     */
    @Override
    public URLBuilder clone() throws CloneNotSupportedException {
        return new URLBuilder(this);
    }

    /**
     * Constructs a {@link URL} representation of this builder.
     *
     * @return An URL representation of this object.
     */
    public URL build() {
        URL url;
        try {
            url = toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    /**
     * Constructs an {@link URL} representation of this URL builder instance.
     * This method calls the {@link #toString()} method to construct the new URL
     * instance.
     *
     * @return A new {@link URL} representation of this object.
     *
     * @throws MalformedURLException if the used service endpoint who was
     * specified by creating this builder instance has no protocol specified, or
     * an unknown protocol is found. For example, if the specified endpoint is a
     * relative path without protocol and domain.
     *
     * @see #toURI()
     */
    public URL toURL() throws MalformedURLException {
        return new URL(toString());
    }

    /**
     * Constructs an {@link URI} representation of this URL builder instance.
     * This method calls the {@link #toString()} method to construct the new URI
     * instance.
     *
     * @return A new {@link URI} representation of this object.
     *
     * @throws URISyntaxException if the given string violates RFC&nbsp;2396, as
     * augmented by the above deviations
     *
     * @see #toURL()
     */
    public URI toURI() throws URISyntaxException {
        return new URI(toString());
    }
    
    /**
     * Convenience method to fetch the specified image by this builder.
     * <p>
     * The builder uses a default {@link URLConnection} to fetch the resource
     * with the default {@link URLConnection} settings.
     * </p>
     * 
     * @return A new {@link Image} instance.
     * 
     * @throws MalformedURLException if the used service endpoint who was
     * specified by creating this builder instance has no protocol specified, or
     * an unknown protocol is found. For example, if the specified endpoint is a
     * relative path without protocol and domain.
     * @throws IOException if an error occurs during reading the image resource.
     * 
     * @see #toImage(java.net.Proxy) 
     */
    public Image toImage() throws MalformedURLException, IOException {
        return toImage(null);
    }
    
    /**
     * Convenience method to fetch the specified image by this builder.
     * <p>
     * The builder uses a default {@link URLConnection} to fetch the resource
     * with the default {@link URLConnection} settings.
     * </p>
     * 
     * @param proxy The {@link Proxy} through which this connection will be made. 
     * If direct connection is desired, {@link Proxy#NO_PROXY} should be specified.
     * 
     * @return A new {@link Image} instance.
     * 
     * @throws MalformedURLException if the used service endpoint who was
     * specified by creating this builder instance has no protocol specified, or
     * an unknown protocol is found. For example, if the specified endpoint is a
     * relative path without protocol and domain.
     * @throws IOException if an error occurs during reading the image resource.
     * 
     * @see #toImage() 
     */
    public Image toImage(Proxy proxy) throws MalformedURLException, IOException {
        try (InputStream is = (proxy != null 
                ? build().openConnection(proxy).getInputStream() 
                : build().openStream())) {
            return ImageIO.read(is);
        }
    }

    /**
     * Constructs a string representation of this URL builder instance.
     *
     * @return A string representation of this object.
     */
    @Override
    public String toString() {
        shardCycleNextIndex = shardEndpoints.length == 1 ? 0
                : (shardCycleNextIndex + 1) % shardEndpoints.length;

        String endpoint = shardEndpoints[shardCycleNextIndex];

        StringBuilder url = new StringBuilder(endpoint.endsWith("/")
                ? endpoint.substring(0, endpoint.length() - 1)
                : endpoint);

        if (imageParams != null && !imageParams.isEmpty()) {
            Iterator<String> iterImageParams = imageParams.keySet().iterator();
            while (iterImageParams.hasNext()) {
                String k = iterImageParams.next();
                String v = imageParams.get(k);

                if (!k.startsWith("/")) {
                    url.append("/");
                }

                url.append(k.toLowerCase(Locale.ENGLISH)).append("=")
                        .append(encodeParamComponent(v.toLowerCase(Locale.ENGLISH), encoding));
            }
        }

        if (imagePath != null && !imagePath.startsWith("/")) {
            url.append("/");
        }

        String imagePath2 = imagePath == null ? "/" : imagePath;
        if (imagePath2.endsWith("#")) {
            imagePath2 = imagePath2.substring(0, imagePath2.length() - 1);
        }
        if (imagePath2.endsWith("?")) {
            imagePath2 = imagePath2.substring(0, imagePath2.length() - 1);
        }

        url.append(imagePath2.contains("://") ? encodeImageComponent(imagePath2, encoding) : imagePath2);

        // Add the optional query parameters
        if (queryParams != null && !queryParams.isEmpty()) {
            Iterator<String> iterQueryParams = queryParams.keySet().iterator();
            
            String sep = "?";
            while (iterQueryParams.hasNext()) {
                String k = iterQueryParams.next();
                String v = queryParams.get(k);
                url.append(sep).append(k);
                if (!v.isEmpty()) {
                    url.append("=").append(encodeParamComponent(v, encoding));
                }
                sep = "&";
            }
        }

        return url.toString();
    }

    // Helper method to encode the image path component
    private static String encodeImageComponent(String s, String encoding) {
        String path = s;
        try {
            path = URLEncoder.encode(s, encoding);
        } catch (UnsupportedEncodingException ignore) {
            // ignore
        }
        return path;
    }

    // Helper method to encode a parameter (path parameter) component 
    private static String encodeParamComponent(String s, String encoding) {
        String param = s;
        try {
            param = URLEncoder.encode(s, encoding)
                    .replaceAll("\\%2C", ",")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException ignore) {
            // ignore
        }
        return param;
    }

}
