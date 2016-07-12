# Servlet Request Attributes

As default, each servlet request is handled with the same init parameter (e.g.
maximum allowed image resolution or the used resource locators). To use 
different parameters depending on the request path without registering different
servlet instances it is possible to handle this with a customized servlet filter.

**Example Servlet Filter**

```java
package servlets;

import ...

public class PicturaServletFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void destroy() { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;

        // Determine the request URI (including query string)
        String uri = httpReq.getRequestURI();
        if (httpReq.getQueryString() != null) {
            uri += "?" + httpReq.getQueryString();
        }
        
        // Set different parameters for each request on "/special/*"
        if (uri.startsWith("/special") {

            httpReq.setAttribute("io.pictura.servlet.HEADER_ADD_REQUEST_ID", false);
            httpReq.setAttribute("io.pictura.servlet.MAX_IMAGE_EFFECTS", 10);
            httpReq.setAttribute("io.pictura.servlet.RESOURCE_PATHS", new Pattern[] {
                Pattern.compile("lenna\\.gif.{0,}")
            });

            // Dispatch the request with the modifed servlet parameters to the
            // default PicturaIO servlet, by removing the path prefix "/special"
            request.getRequestDispatcher(uri.replaceFirst("/special", ""))
                .forward(request, response);
            return;
        }

        // Default; use the origin servlet init parameters 
        chain.doFilter(request, response);
    }

}
```

Next, configuring the servlet filter in `web.xml`:

```xml
<filter>
    <filter-name>PicturaServletFilter</filter-name>
    <filter-class>servlets.PicturaServletFilter</filter-class>
</filter>

<filter-mapping>
    <filter-name>PicturaServletFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

## Supported Attributes

| Attribute | Type |
| :-------- | :--- |
| io.pictura.servlet.DEBUG | `java.lang.Boolean` |
| io.pictura.servlet.MAX_IMAGE_FILE_SIZE | `java.lang.Long` |
| io.pictura.servlet.MAX_IMAGE_RESOLUTION | `java.lang.Long` |
| io.pictura.servlet.MAX_IMAGE_EFFECTS | `java.lang.Integer` |
| io.pictura.servlet.ENABLED_INPUT_IMAGE_FORMATS | `java.lang.String` |
| io.pictura.servlet.ENABLED_OUTPUT_IMAGE_FORMATS | `java.lang.String` |
| io.pictura.servlet.ENABLE_BASE64_IMAGE_ENCODING | `java.lang.Boolean` |
| io.pictura.servlet.HTTP_AGENT | `java.lang.String` |
| io.pictura.servlet.HTTP_CONNECT_TIMEOUT | `java.lang.Integer` |
| io.pictura.servlet.HTTP_READ_TIMEOUT | `java.lang.Integer` |
| io.pictura.servlet.HTTP_MAX_FORWARDS | `java.lang.Integer` |
| io.pictura.servlet.HTTP_FOLLOW_REDIRECTS | `java.lang.Integer` |
| io.pictura.servlet.HTTPS_DISABLE_CERTIFICATE_VALIDATION | `java.lang.Boolean` |
| io.pictura.servlet.HTTP_PROXY_HOST | `java.lang.String` |
| io.pictura.servlet.HTTP_PROXY_PORT | `java.lang.Integer` |
| io.pictura.servlet.HTTPS_PROXY_HOST | `java.lang.String` |
| io.pictura.servlet.HTTPS_PROXY_PORT | `java.lang.Integer` |
| io.pictura.servlet.IMAGEIO_USE_CACHE | `java.lang.String` |
| io.pictura.servlet.IMAGEIO_CACHE_DIR | `java.lang.String` |
| io.pictura.servlet.IMAGEIO_SPI_FILTER_INCLUDE | `java.lang.String` |
| io.pictura.servlet.IMAGEIO_SPI_FILTER_EXCLUDE | `java.lang.String` |
| io.pictura.servlet.ENABLE_QUERY_PARAMS | `java.lang.Boolean` |
| io.pictura.servlet.DEFLATER_COMPRESSION_LEVEL | `java.lang.Integer` |
| io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE | `java.lang.Integer` |
| io.pictura.servlet.HEADER_ADD_CONTENT_LOCATION | `java.lang.Boolean` |
| io.pictura.servlet.HEADER_ADD_TRUE_CACHE_KEY | `java.lang.Boolean` |
| io.pictura.servlet.HEADER_ADD_REQUEST_ID | `java.lang.Boolean` |
| io.pictura.servlet.HEADER_ADD_NORMALIZED_PARAMS | `java.lang.Boolean` |
| io.pictura.servlet.URL_CONNECTION_FACTORY | `io.pictura.servlet.URLConnectionFactory` |
| io.pictura.servlet.RESOURCE_LOCATORS | `io.pictura.servlet.ResourceLocator[]` |
| io.pictura.servlet.RESOURCE_PATHS | `java.util.regex.Pattern[]` |


