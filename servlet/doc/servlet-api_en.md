# PicturaIO Image Servlet API Reference

PicturaIO is designed around a dispatcher servlet (`PicturaServlet`) that handles
all the image requests and responses. The image request processing workflow of
the `PicturaServlet` is illustrated in the following diagram:

<div style="text-align: center; margin: 2em;">
    <img src="data:image/png;base64,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" alt=""/>
</div>

After receiving an HTTP request, `PicturaServlet` consults the built-in HTTP
cache (if enabled). If the response for the given request was already rendered
and is still valid, the HTTP cache will directly write out a copy of the cached
image data.

If there is no valid cached instance available, the `PicturaServlet` will 
delegate the HTTP request to the responsible `RequestProcessor` to handle the
request.

In cases of an image request, the image processor will take help from the
available `ResourceLocator`'s *(1..n)* to pickup the origin image to process
for the request.

Once the image process is done, the `PicturaServlet` passes the image data to
the client (e.g. browser).

**Required Configuration**

You need to map image requests that you want the `PicturaServlet` handle, by
using a URL mapping in the `web.xml` file. The following is an example to show 
declaration and mapping for the default `PicturaServlet`:

```xml
<web-app ...>
    ...
    <servlet>
        <servlet-name>PicturaServlet</servlet-name>
        <servlet-class>io.pictura.servlet.PicturaServlet</servlet-class>
        <async-supported>true</async-supported>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>PicturaServlet</servlet-name>
        <url-pattern>/pictura/*</url-pattern>
    </servlet-mapping>
    ...
</web-app>
```

## Table of Contents

  1. [Servlet Types](#servlet-types)
  1. [Servlet Parameters](#servlet-parameters)
  1. [Context Parameters](#context-parameters)
  1. [Servlet Status & Statistics](#servlet-status-statistics)
  1. [ImageIO Support](#imageio-support)
  1. [IIO Registry Context Listener](#iio-registry-context-listener)
  1. [Cache Control Handler](#cache-control-handler)
  1. [Request Processors](#request-processors)
  1. [Request Processor Factory](#request-processor-factory) 
  1. [Resource Locators](#resource-locators)
  1. [URL Connection Factory](#url-connection-factory)
  1. [Params Interceptor](#params-interceptor)
  1. [Image Interceptor](#image-interceptor)
  1. [HTTP Cache](#http-cache)
  1. [Annotations](#annotations)
  1. [Authentication](#authentication)

## Servlet Types
  
The PicturaIO web servlet is available in two different kinds. The default servlet
```io.pictura.servlet.PicturaServlet``` to handle GET requests only and a POST 
variant ```io.pictura.servlet.PicturaPostServlet``` to handle GET and POST 
requests.

In cases of POST requests, the source image is not specified as part of the 
URL (location of the image) and is also not resolved by the server. The image 
to convert is given as POST body data as part of the request.

> POST requests are not cached.

**[\[⬆\]](#table-of-contents)**
  
## Servlet Parameters

The Pictura servlet supports property placeholders for init parameters. You can
use this feature to refer to system properties.

For example, the placeholder ```${http.proxyPort}``` will refer to 
```java.lang.System.getProperty("http.proxyPort")```. You can also define a
default value if the system property is not set with the ```:``` delimeter
```${http.proxyPort:8077}```. In this case the value after the delimeter is
used. 

> To override servlet configuration defaults, specify them in the init-params 
> while configuring the servlet in `web.xml`.

**[\[⬆\]](#table-of-contents)**

### debug

If set to `true`, the servlet enables automatically all available debug
output like additional debug response headers. The default value is `false`.

**[\[⬆\]](#table-of-contents)**

### jmxEnabled

If set to `true`, the servlet registers the available JMX beans for the
pictura servlet implementation. The default value is `false`.

**[\[⬆\]](#table-of-contents)**

### statsEnabled

If set to `true`, enables the internal statistics monitor. The statistics
response (JSON object) could be use to monitor a single servlet instance for
example, by an external tool like Nagios. The default value is `false`.

**[\[⬆\]](#table-of-contents)**

### statsPath

Sets the statistics monitor path, relative to the servlet path. The default
value is `/stats`.

**[\[⬆\]](#table-of-contents)**

### statsIpAddressMatch

Sets the statistics monitor access control. Multiple IP addresses are separated
by comma. The default value is `127.0.0.1,::1`.

**[\[⬆\]](#table-of-contents)**

### corePoolSize

Sets the core executor pool size. Must be a positive integer greater than 0.

**[\[⬆\]](#table-of-contents)**

### maxPoolSize

Sets the maximum allowed number of image processing threads. Must be a positive 
integer greater than 0.

**[\[⬆\]](#table-of-contents)**

### keepAliveTime

Sets the time limit in millis for which image processing threads may remain idle 
before being terminated. Must be a positive integer greater than 0. If there are 
more than the core number of image processing threads currently in the pool, 
after waiting this amount of time without processing a task, excess threads will 
be terminated. The default value is `60000`.

**[\[⬆\]](#table-of-contents)**

### workerQueueSize

Sets the capacity of the worker queue. Must be a positive integer greater than 
0. The default value is `100`.

**[\[⬆\]](#table-of-contents)**

### workerTimeout

Sets the timeout in millis for a image request process. Must be a positive 
integer greater than 0. The default value is `60000`.

**[\[⬆\]](#table-of-contents)**

### resourcePaths

Sets the allowed image resource paths. Multiple paths are separated by comma.
The default value is `/*`.

**[\[⬆\]](#table-of-contents)**

### resourceLocators

Sets the used resource locators (class name). Multiple paths are separated by
comma. The default value is `io.pictura.servlet.FileResourceLocator`.

Default Resource Locators:

  1. `io.pictura.servlet.FileResourceLocator`
  1. `io.pictura.servlet.HttpResourceLocator`
  1. `io.pictura.servlet.EmptyResourceLocator`
 
**Example**

```xml
<web-app ...>
...
    <servlet>
        ...
        <init-param>
            <param-name>resourceLocators</param-name>
            <param-value>
                io.pictura.servlet.FileResourceLocator,
                io.pictura.servlet.HttpResourceLocator
            </param-value>
        </init-param>
    </servlet>
...
</web-app>
```

> See also [Resource Locators](#resource-locators)

**[\[⬆\]](#table-of-contents)**

### requestProcessor

Sets the default image request processor (class name). The default value is
`io.pictura.servlet.ImageRequestProcessor`.

**[\[⬆\]](#table-of-contents)**
 
### requestProcessorFactory

Sets the custom image request processor factory (class name). As default, this
value is not set.

> See also [Request Processor Factory](#request-processor-factory) 

**[\[⬆\]](#table-of-contents)**

### requestProcessorStrategy

Sets the custom image request processor strategy (class names). Multiple
strategies are separated by comma. The strategies are executed in the configured
order. As default, this value is not set.

Default Image Request Strategies:

  1. `io.pictura.servlet.ClientHintRequestProcessor`
  1. `io.pictura.servlet.AutoFormatRequestProcessor`
  1. `io.pictura.servlet.CSSColorPaletteRequestProcessor`
  1. `io.pictura.servlet.BotRequestProcessor`
  1. `io.pictura.servlet.PDFRequestProcessor` 1)
  1. `io.pictura.servlet.MetadataRequestProcessor` 1)

> 1) requires optional dependencies. For more details, please see
> [Request Processors](#request-processors).

**Example**

```xml
<web-app ...>
...
    <servlet>
        ...
        <init-param>
            <param-name>requestProcessorStrategy</param-name>
            <param-value>
                io.pictura.servlet.BotRequestProcessor,
                io.pictura.servlet.CSSColorPaletteRequestProcessor,
                io.pictura.servlet.ClientHintRequestProcessor,
                io.pictura.servlet.AutoFormatRequestProcessor
            </param-value>
        </init-param>
    </servlet>
...
</web-app>
```

**[\[⬆\]](#table-of-contents)**

### maxImageFileSize

Sets the maximum allowed file size in bytes for input images. Must be a positive
integer greater than 0. The default value is `2M`.

**[\[⬆\]](#table-of-contents)**

### maxImagePostContentLength

Sets the maximum allowed content length in bytes for POST requests. Must be
positive integer or `-1` to use the same value of `maxImageFileSize`.

> Required for `io.pictura.servlet.PicturaPostServlet`

**[\[⬆\]](#table-of-contents)**

### maxImageResolution

Sets the maximum allowed image resolution (width x height) in px for input 
images. Must be a positive integer greater than 0. The default value 
is `6000000` (6MP).

**[\[⬆\]](#table-of-contents)**

### maxImageEffects

Sets the maximum allowed number of image effects per request. Must be a positive
integer or `-1` to set to infinite. The default value is `5`.

**[\[⬆\]](#table-of-contents)**

### enabledInputImageFormats

Sets the allowed input image formats. Multiple values are comma separated. As 
default this is not set.

**Example**

```xml
<web-app ...>
...
    <servlet>
        ...
        <init-param>
            <param-name>enabledInputImageFormats</param-name>
            <param-value>JPG,PNG,WEBP,GIF,BMP</param-value>
        </init-param>
    </servlet>
...
</web-app>
```

**[\[⬆\]](#table-of-contents)**

### enabledOutputImageFormats

Sets the allowed output image formats. Multiple values are comma separated. As 
default this is not set.

**Example**

```xml
<web-app ...>
...
    <servlet>
        ...
        <init-param>
            <param-name>enabledInputImageFormats</param-name>
            <param-value>JPG,PNG,WEBP</param-value>
        </init-param>
    </servlet>
...
</web-app>
```

**[\[⬆\]](#table-of-contents)**

### enableBase64ImageEncoding

Enables or disables *Base64* image encoding (output). The default value
is `false`.

**[\[⬆\]](#table-of-contents)**

### imageioSpiFilterInclude

Sets the ImageIO plugin include filter. If set, only listed plugin SPI's
are loaded by the servlet instance. Multiple values are comma separated. As 
default, this value is not set.

```xml
<web-app ...>
...
    <servlet>
        ...
        <init-param>
            <param-name>imageioSpiFilterInclude</param-name>
            <param-value>
                com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi,                
                com.github.jaiimageio.jpeg2000.impl.J2KImageWriterSpi
            </param-value>
        </init-param>
    </servlet>
...
</web-app>
```

**[\[⬆\]](#table-of-contents)**

### imageioSpiFilterInclude

Sets the ImageIO plugin exclude filter. If set, listed plugin SPI's are not
loaded by the servlet instance. Multiple values are comma separated. As default, 
this value is not set.

```xml
<web-app ...>
...
    <servlet>
        ...
        <init-param>
            <param-name>imageioSpiFilterInclude</param-name>
            <param-value>com.github.jaiimageio.jpeg2000.impl.J2KImageWriterSpi</param-value>
        </init-param>
    </servlet>
...
</web-app>
```

**[\[⬆\]](#table-of-contents)**

### imageioUseCache

Sets a flag indicating whether a disk-based cache file should be used when 
creating image input stream's and image output stream's. The default value
is `false`.

**[\[⬆\]](#table-of-contents)**

### imageioCacheDir

Sets the directory where cache files are to be created. The default value is
system depending. If `imageioUseCache` is `false`, this value is ignored.

**[\[⬆\]](#table-of-contents)**

### httpAgent

Overrides the default HTTP client user agent string for external requests.

**[\[⬆\]](#table-of-contents)**

### httpConnectTimeout

Sets a specified timeout value, in milliseconds, to be used when opening a 
communications link to an external resource. Must be a positive integer. A 
timeout of zero is interpreted as an infinite timeout. The default value 
is `5000`.

**[\[⬆\]](#table-of-contents)**

### httpReadTimeout

Sets the read timeout to a specified timeout, in milliseconds. Must be a 
positive integer. A timeout of zero is interpreted as an infinite timeout. The 
default value is `5000`.

**[\[⬆\]](#table-of-contents)**

### httpFollowRedirects

Sets whether HTTP redirects  (requests with response code 3xx) should be 
followed. The default value is `true`.

**[\[⬆\]](#table-of-contents)**

### httpMaxForwards

Sets the maximum number of forwards by proxies or gateways. Must be a integer.
A value of -1 is interpreted as infinite. The default value is `-1`.

**[\[⬆\]](#table-of-contents)**

### httpsDisableCertificateValidation

If set to `true`, the default HTTP client will disable certificate 
validation. The default value is `false`.

**[\[⬆\]](#table-of-contents)**

### httpProxyHost

Sets the optional proxy hostname. As default, this value is not set.

**[\[⬆\]](#table-of-contents)**

### httpProxyPort

Sets the proxy port number if a proxy hostname is set. Must be a positive
integer. As default, this value is not set.

**[\[⬆\]](#table-of-contents)**

### httpsProxyHost

Sets the optional **HTTPS** proxy hostname. As default, this value is not set.

**[\[⬆\]](#table-of-contents)**

### httpsProxyPort

Sets the **HTTPS** proxy port number if a proxy hostname is set. Must be a positive
integer. As default, this value is not set.

**[\[⬆\]](#table-of-contents)**

### urlConnectionFactory

Sets a custom URL connection factory (class name). As default, this value is
not set.

**Example**

```xml
<web-app ...>
...
    <servlet>
        ...
        <init-param>
            <param-name>urlConnectionFactory</param-name>
            <param-value>io.pictura.servlet.examples.CustomConnectionFactory</param-value>
        </init-param>
    </servlet>
...
</web-app>
```

```java
package io.pictura.servlet.examples;

import ...

public class CustomConnectionFactory implements URLConnectionFactory {
    @Override
    public URLConnection newConnection(URL url, Properties props) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }
}
```

> See also [URL Connection Factory](#url-connection-factory)

**[\[⬆\]](#table-of-contents)**

### scriptEnabled

If set to `true` the client site JavaScript library is available by the
servlet instance. The default value is `true`.

**[\[⬆\]](#table-of-contents)**

### scriptPath

Sets the JavaScript library path, relative to the servlet path. The default
value is `/js`.

JavaScript Sources:

  1. `/js/pictura.js`
  1. `/js/pictura.min.js`
  1. `/js/cookie.js`
  1. `/js/cookie.min.js`
 
**[\[⬆\]](#table-of-contents)**

### placeholderProducerEnabled

If set to `true` the image placeholder producer will be enabled. The default
value is `false`.

**[\[⬆\]](#table-of-contents)**

### placeholderProducerPath

Sets the image placeholder producer path, relative to the servlet path. The 
default value is `/ip`.

**[\[⬆\]](#table-of-contents)**

### enableQueryParams

If set to `true`, enables Image API URL query parameters. The default value
is `false`.

**[\[⬆\]](#table-of-contents)**

### enableContentDisposition

If set to `true`, the image processor will append a content-disposition
response header if the request contains the `dl` URL query parameter.
The value of the query parameter must contains a valid filename. The default
servlet parameter value is `false`.

> Note, the filename suffix is automatically computed by the output image format.

**[\[⬆\]](#table-of-contents)**

### headerAddContentLocation

If set to `true`, the servlet appends automatically the origin content
location of remote located resources. The default value is `false`.

**[\[⬆\]](#table-of-contents)**

### headerAddTrueCacheKey

If set to `true`, the servlet appends automatically a true cache key for
each cache key to the responses. The default value is `false`.

**[\[⬆\]](#table-of-contents)**

### headerAddRequestId

If set to `true`, the servlet appends automatically a unique request ID 
each response. The default value is `false`.

**[\[⬆\]](#table-of-contents)**

### cacheControlHandler

Sets a custom cache control handler (class name or file). If a specified cache
control path matches for the request the specified directive is used as cache
control header. As default, this value is not set.

> See also [Cache Control Handler](#cache-control-handler)

**[\[⬆\]](#table-of-contents)**

### deflaterCompressionLevel

Sets the deflater compression level in cases of text resources (JS, CSS, ...).
Valid values are `0 - 9`. The default value is `8`.

**[\[⬆\]](#table-of-contents)**

### deflaterCompressionMinSize

Sets the minimum amount of data (in bytes) before the output is compressed. The 
default value is `1024`.

**[\[⬆\]](#table-of-contents)**

### cacheEnabled

If set to `true`, enables the embedded in-memory HTTP cache for image 
responses. The default value is `false`.

**[\[⬆\]](#table-of-contents)**

### cacheClass

Sets the custom HTTP cache (`io.pictura.servlet.HttpCache`) to use. As
default, this value is not set.

> If HTTP caching is enabled and there is no custom cache class set, the servlet
> will load an internal implementation. Please note, this implementation was
> not designed for production use.
> 
> See also [HTTP Cache](#http-cache)

**[\[⬆\]](#table-of-contents)**

### cacheCapacity

Sets the maximum size of a single cache entry in bytes for the built-in HTTP 
cache.

> Is respected only for the built-in HTTP cache.

**[\[⬆\]](#table-of-contents)**

### cacheMaxEntrySize

Sets the maximum number of possible cache entries in the built-in HTTP cache.

> Is respected only for the built-in HTTP cache.

**[\[⬆\]](#table-of-contents)**

### cacheFile

If set and if caching is enabled, the servlet will persist the current state
of the HTTP cache on destroing and restore on initialization of the servlet
instance. The value must be a valid absolute file path. As default, this value 
is not set.

> The server process requires read and write permissions on the specified path.

**[\[⬆\]](#table-of-contents)**

### configFile

An optional path to an external (XML or Properties) configuration file. As 
default this value is not set.

**Example**

```xml
<web-app ...>
...
    <servlet>
        ...
        <init-param>
            <param-name>configFile</param-name>
            <param-value>pictura.xml</param-value>
        </init-param>
    </servlet>
...
</web-app>
```

*XML Format*

```xml
<pictura>
...
    <debug>true</debug>
    ...
    <resource-locators>
        <class>io.pictura.servlet.FileResourceLocator</class>
        <class>io.pictura.servlet.HttpResourceLocator</class>
    </resource-locators>
...
</pictura>
```

> See ```/META-INF/resources/dtd/pictura-config-1.0.dtd``` for the document type
> definition.

*Properties Format*

```properties
io.pictura.servlet.debug=true
io.pictura.servlet.resourceLocators=io.pictura.servlet.FileResourceLocator,io.pictura.servlet.HttpResourceLocator
```

> A init parameter is specified as ```io.pictura.servlet.{PARAM_NAME}```.

If both configurations contains a servlet init parameter (web.xml and the
external referenced config file), the value from the web.xml will override
the value from the external config file.

**[\[⬆\]](#table-of-contents)**

## Context Parameters

### `io.pictura.servlet.LOG_LEVEL`

Sets the log level for the internal Pictura servlet logger. Valid values are
```TRACE```, ```DEBUG```, ```INFO```, ```WARN```, ```ERROR``` and ```FATAL```.
The default value is ```INFO```.

> The intention of an internal servlet logger is that there maybe problems with 
> different logging frameworks on the running app server. So the internal logger
> copies the style JCL and will automatically try to log to an available JCL or 
> SLF4J. If both fails, the logs will be written to the default servlet context.

**[\[⬆\]](#table-of-contents)**

## `Servlet Status & Statistics`

If enabled (see servlet parameters) the servlet provides status and statistic
values as JSON output. The default path (relativ to servlet path) is ```/stats```.

**Example Output**

```json
{
    id: "mYXmkYVdpIoBGUY5",
    version: "1.0.0.Final",
    servlet: "pictura-demo",
    implClass: "io.pictura.examples.UndertowServletContainer$InterceptableServlet",
    implVersion: "1.0",
    uptime: "01h 29m 15s",
    alive: true,
    async: true,
    contextPath: "/pictura",
    executor: {
        poolSize: 4,
        queueSize: 0,
        activeCount: 0,
        taskCount: 38,
        completedTaskCount: 38,
        rejectedTaskCount: 0,
        instanceHours: 0.019666946
    },
    cache: {
        size: 89,
        hitRate: 0.94521
    },
        network: {
        outbound: 19721106,
        inbound: 4951087,
    },
    throughput: {
        requestsPerSecond: 16.987,
        averageResponseTime: 0.8712895,
        averageResponseSize: 5187
    },
    errorRate: 0.68421054
}
```

**[\[⬆\]](#table-of-contents)**

### Query Parameters

#### `q (Query)`

Specifies the query to execute. Valid values are ```stats```, ```errors```,
```params```, ```imageio``` and ```cache```. The default value (if parameter is
not present) is ```stats```.

**[\[⬆\]](#table-of-contents)**

#### `f (Filter)`

An optional parameter to specify a regular expression filter. Depends on the
query. 

**[\[⬆\]](#table-of-contents)**

#### `a (Action)`

An optional action parameter to specify an action to execute (e.g. delete 
cache entry).

**[\[⬆\]](#table-of-contents)**

### Example Queries

**Example 1**

List the cumulative numbers of all client and server errors since start.

*Request*

 ```/stats?q=errors```

*Response*

```json
{
    http400: 26,
    http415: 1,
    http500: 3
}
```

**Example 2**

List the cumulative numbers of all server errors since start.

*Request*

 ```/stats?q=errors&f=5.*```

*Response*

```json
{
    http500: 3
}
```

**Example 3**

List the servlet init parameters.

*Request*

 ```/stats?q=params```

*Response*

```json
{
    initParams: {
        cacheCapacity: "100",
        cacheControlHandler: "io.pictura.examples.UndertowServletContainer$CacheControl",
        cacheEnabled: "true",
        debug: "true",
        enableBase64ImageEncoding: "true",
        enableContentDisposition: "true",
        enabledOutputImageFormats: "jpg,jp2,webp,png,gif",
        headerAddTrueCacheKey: "true",
        httpAgent: "TEST",
        httpMaxForwards: "2",
        httpsDisableCertificateValidation: "true",
        jmxEnabled: "true",
        maxPoolSize: "4",
        placeholderProducerEnabled: "true",
        requestProcessorStrategy: "io.pictura.servlet.PDFRequestProcessor,io.pictura.servlet.AutoFormatRequestProcessor",
        resourceLocators: "io.pictura.servlet.FileResourceLocator,io.pictura.servlet.HttpResourceLocator",
        statsEnabled: "true",
        workerQueueSize: "100"
    }
}
```

> Note, this lists only the custom parameters however not parameters which
> are initialized with default values.

**Example 4**

List all registered ImageIO plug-ins.

*Request*

 ```/stats?q=imageio```

*Response*

```json
{
    imageio: [
        {
            pluginClassName: "com.luciad.imageio.webp.WebPImageReaderSpi",
            description: "WebP Reader",
            vendorName: "Luciad",
            version: "1.0",
            formatNames: "WebP, webp",
            fileSuffixes: "webp",
            mimeTypes: "image/webp"
        },
        {
            pluginClassName: "com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi",
            description: "Standard JPEG Image Reader",
            vendorName: "Oracle Corporation",
            version: "0.5",
            formatNames: "JPEG, jpeg, JPG, jpg",
            fileSuffixes: "jpg, jpeg",
            mimeTypes: "image/jpeg"
        },
        {
            pluginClassName: "com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi",
            description: "Standard JPEG Image Writer",
            vendorName: "Oracle Corporation",
            version: "0.5",
            formatNames: "JPEG, jpeg, JPG, jpg",
            fileSuffixes: "jpg, jpeg",
            mimeTypes: "image/jpeg"
        },
        {
            pluginClassName: "com.luciad.imageio.webp.WebPImageWriterSpi",
            description: "WebP Writer",
            vendorName: "Luciad",
            version: "1.0",
            formatNames: "WebP, webp",
            fileSuffixes: "webp",
            mimeTypes: "image/webp"
        }
    ]
}
```

**Example 5**

List the current HTTP cache entries.

*Request*

 ```/stats?q=cache```

*Response*

```json
{
    cacheEntries: [
        {
            key: "/pictura/lenna.jpg",
            eTag: "W\/\"5a442a9dff78527bb6f3f0bf951d4980\"",
            hits: 0,
            timestamp: "Sun Dec 06 13:24:03 CET 2015",
            expires: "Sun Dec 06 15:24:03 CET 2015",
            statusCode: 200,
            contentType: "image/jpeg",
            contentLength: 23425,
            producer: "io.pictura.servlet.PicturaPostServlet$PostRequestProcessor"
        }
    ]
}
```

**Example 5**

Delete all HTTP cache entries which contains the string ```jpg``` in the cache key.

*Request*

 ```/stats?q=cache&f=.*jpg.*&a=delete```

*Response*

A list (JSON representation) of the deleted HTTP cache entries.

**[\[⬆\]](#table-of-contents)**

## ImageIO Support

The servlet uses the default Java ImageIO interface to read and write images
*(ImageIO uses a service lookup mechanism, to discover plug-ins at runtime)*.

**WebP**

https://bitbucket.org/luciad/webp-imageio

**JPEG 2000**

```xml
<dependency>
    <groupId>com.github.jai-imageio</groupId>
    <artifactId>jai-imageio-jpeg2000</artifactId>
    <version>1.3.0</version>
</dependency>
```

**JPEG *(CMYK Support)***

```xml
<dependency>
    <groupId>com.twelvemonkeys.imageio</groupId>
    <artifactId>imageio-jpeg</artifactId>
    <version>3.1.1.</version>
</dependency>
```

**PSD *(read-only)***

```xml
<dependency>
    <groupId>com.twelvemonkeys.imageio</groupId>
    <artifactId>imageio-psd</artifactId>
    <version>3.1.1.</version>
</dependency>
```

**TIFF**

```xml
<dependency>
    <groupId>com.twelvemonkeys.imageio</groupId>
    <artifactId>imageio-tiff</artifactId>
    <version>3.1.1.</version>
</dependency>
```

**PCX**

```xml
<dependency>
    <groupId>com.twelvemonkeys.imageio</groupId>
    <artifactId>imageio-pcx</artifactId>
    <version>3.1.1.</version>
</dependency>
```

**ICNS**

```xml
<dependency>
    <groupId>com.twelvemonkeys.imageio</groupId>
    <artifactId>imageio-icns</artifactId>
    <version>3.1.1.</version>
</dependency>
```

**PICT**

```xml
<dependency>
    <groupId>com.twelvemonkeys.imageio</groupId>
    <artifactId>imageio-pict</artifactId>
    <version>3.1.1.</version>
</dependency>
```

**JBIG2**

```xml
<repositories>
    <repository>
        <id>jbig2.googlecode</id>
        <name>JBIG2 ImageIO-Plugin repository at googlecode.com</name>
        <url>http://jbig2-imageio.googlecode.com/svn/maven-repository</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.levigo.jbig2</groupId>
    <artifactId>levigo-jbig2-imageio</artifactId>
    <version>1.6.1</version>
</dependency> 
```

**DICOM**

https://github.com/dcm4che/dcm4che

**[\[⬆\]](#table-of-contents)**

## IIO Registry Context Listener

Because the Java ImageIO plugin registry (the IIORegistry) is **VM global**, 
it doesn't by default work well with servlet contexts. This is especially 
evident if you load plugins from the `WEB-INF/lib` or classes folder. Unless 
you add `ImageIO.scanForPlugins()` somewhere in your code, the plugins might 
never be available at all.

If you restart your application, old classes will by default remain in memory 
forever (because the next time `scanForPlugins` is called, it's another 
`ClassLoader` that scans/loads classes, and thus they will be new instances in 
the registry). If a read is attempted using one of the remaining "old" readers, 
weird exceptions (like `NullPointerException`s when accessing `static final` 
initialized fields or `NoClassDefFoundError`s for uninitialized inner classes) 
may occur.

To work around both the discovery problem and the resource leak, it is **strongly 
recommended** to use the `io.pictura.servlet.IIOProviderContextListener` that 
implements dynamic loading and unloading of Java ImageIO plugins for web 
applications.

```xml
<web-app ...>
...
    <listener>
        <display-name>ImageIO Service Provider Loader/Unloader</display-name>
        <listener-class>io.pictura.servlet.IIOProviderContextListener</listener-class>
    </listener>
...
</web-app>
```

**[\[⬆\]](#table-of-contents)**

## Cache Control Handler

The cache control handler returns the cache control directive (header value) for 
the specified resource path.

You have two options to define a custom cache control handler, use an external
(XML) configuration file which contains the rules or write your own cache
control handler implementation.

**[\[⬆\]](#table-of-contents)**

### Configuration File

The rules from the configuration file are checked in the given order. You can
also use regular expressions and wild-cards to specify the path rules.

```xml
<cache-control>
    <!--
        Cache external resources for 10 min.
    -->
    <rule>
        <path>http*</path>
        <directive>public; max-age=600</directive>
    </rule>
    
    <!--
        For everything else set to 5 min.      
    -->
    <rule>
        <path>/*</path>
        <directive>public; max-age=300</directive>
    </rule>
</cache-control>
```

> See ```/META-INF/resources/cache-control-1.0.dtd``` for the document type
> definition.

```xml
<web-app ...>
...
    <servlet>
        ...
        <init-param>
            <param-name>cacheControlHandler</param-name>
            <param-value>cache-control.xml</param-value>
        </init-param>
    </servlet>
...
</web-app>
```

**[\[⬆\]](#table-of-contents)**

### Custom Implementation

A custom cache control handler implementation must implements the 
interface ```io.pictura.servlet.CacheControlHandler```.

```java
package io.pictura.servlet.examples;

import ...

public class CacheControlHandlerImpl implements CacheControlHandler {

    @Override
    public String getDirective(String path) {
        if (path != null && !path.isEmpty()) {
        
            // Handle external resources
            if (path.startsWidth("http")) {
            
                // This will also override any cache control directive which
                // was set by the origin server where the resource comes from.
                return "public; max-age=600"; // 10 min.
            } 
            
            // Handle internal resources
            else {
                return "public; max-age=300"; // 5 min.
            }
        }
        return null; // no cache control
    }
}
```

```xml
<web-app ...>
...
    <servlet>
        ...
        <init-param>
            <param-name>cacheControlHandler</param-name>
            <param-value>io.pictura.servlet.examples.CacheControlHandlerImpl</param-value>
        </init-param>
    </servlet>
...
</web-app>
```

**[\[⬆\]](#table-of-contents)**

## Request Processors

The API defines a set of predefined image processors as well as strategies. You
can use the request processors to configure the default request processor a
strategy or to reuse in a custom request processor factory. For this, please see
the servlet init parameters `requestProcessor`, `requestProcessorStrategy` and
`requestProcessorFactory`. On each new servlet request the PicturaIO servlet
will lookup a new image request processor in the following order:

 1. Request Processor Factory (`requestProcessorFactory`)
 1. Request Processor Strategy (`requestProcessorStrategy`)
 1. Default Request Processor (`requestProcessor`)

**[\[⬆\]](#table-of-contents)**

### `io.pictura.servlet.ImageRequestProcessor`

This is the default core image request processor. If nothing else is configured
this request processor is used.

> The default image request processor **doesn't** implements the strategy 
> interface ```io.pictura.servlet.ImageRequestStrategy```.

**[\[⬆\]](#table-of-contents)**

### `io.pictura.servlet.AutoFormatRequestProcessor`

An output format content negotiation request processor to automatically decide 
the best image format for the requested client if no output format was set.
You achieved the best results if you keep sure that ImageIO plug-ins are
installed for the following output formats: `JPEG`, `JPEG2000`, `WEBP`, `PNG`
and `GIF`.

**Information about what image formats the browsers support**

| Browser               | JPEG | JPEG 2000 | WEBP | PNG | GIF |
|-----------------------|------|-----------|------|-----|-----|
| Apple Safari OSX      | YES  | YES       |      | YES | YES |
| Apple Safari iOS      | YES  | YES       |      | YES | YES | 
| Google Chrome         | YES  |           | YES  | YES | YES |
| Chromium              | YES  |           | YES  | YES | YES |
| Android (4+)          | YES  |           | YES  | YES | YES |
| Opera                 | YES  |           | YES  | YES | YES |
| Mozilla Firefox       | YES  |           |      | YES | YES |
| Microsoft IE          | YES  |           |      | YES | YES |
| Microsoft Edge        | YES  |           |      | YES | YES |

> Implements the ```io.pictura.servlet.ImageRequestStrategy``` interface.

**[\[⬆\]](#table-of-contents)**

### `io.pictura.servlet.ClientHintRequestProcessor`

Based on the new Client-Hint browser feature or the Pictura Client-Hint Cookie 
Script, the processor will try to automatically negotiate the content.

If the image resource width is known at request time, the user agent can
communicate it to the server to enable selection of an optimized resource.
The client and server can negotiate an optimized asset based on the given
request hints.

For browsers which doesn't support client hint headers, it is possible to
enable this feature by passing values from client to the server via
a small cookie. To enable this feature it is necessary to embedd the
`cookie.js` script into the head of the HTML page.

> Implements the `io.pictura.servlet.ImageRequestStrategy` interface.

**[\[⬆\]](#table-of-contents)**

### `io.pictura.servlet.CSSColorPaletteRequestProcessor`

The CSS color palette processor is used to extract 1 - 32 dominant colors from 
the specified image (after the optional image operations are done). The result 
is given as `text/css`.

Also this type of request processor extends the default parameter set from the
default image request processor.

For more details, please see [Palette CSS API Reference](servlet/doc/pcss-api_en.md).

> Implements the `io.pictura.servlet.ImageRequestStrategy` interface.

**[\[⬆\]](#table-of-contents)**
 
### `io.pictura.servlet.BotRequestProcessor`

This is a special implementation of the default image request processor which 
will handle bot requests different to normal "user" requests.

The difference compared to normal "users" is that a bot will always get the
origin image unless it is a proxy request anyway. To handle this, the bot
request processor will send a moved permanently status code with the location 
to the unmodified image back to the client (bot).

> Implements the ```io.pictura.servlet.ImageRequestStrategy``` interface.

**[\[⬆\]](#table-of-contents)**

### `io.pictura.servlet.MetadataRequestProcessor`

> Requires the optional dependencies ```com.drewnoakes:metadata-extractor``` and 
> ```com.google.code.gson:gson```. For the first use (request), the request 
> processor will check whether the dependencies are available or not.

Produces a non-image, **JSON** file format. This is a data representation of 
the image specified (EXIF values).

The image processor strategy is listening for requested ```EXIF``` output file 
formats (```/F=EXIF/image.jpg```).

**Example Response**

```javascript
{
    Photoshop: {
        MacPrintInfo: "[120 bytes]",
        ResolutionInfo: "100.0x100.0 DPI",
        PrintFlags: "0 0 0 0 0 0 0 0",
        CopyrightFlag: "No",
        PrintFlagsInformation: "0 1 0 0 0 0 0 0 0 2",
        ColorHalftoningInformation: "[72 bytes]",
        ColorTransferFunctions: "[112 bytes]",
        GridandGuidesInformation: "0 0 0 1 0 0 2 64 0 0 2 64 0 0 0 0",
        Photoshop4.0Thumbnail: "JpegRGB, 128x72, Decomp 27648 bytes, 1572865 bpp, 3458 bytes",
        JPEGQuality: "8 (High), Standard format, 3 scans"
    },
    JPEG: {
        CompressionType: "Baseline",
        DataPrecision: "8 bits",
        ImageHeight: "225 pixels",
        ImageWidth: "400 pixels",
        NumberofComponents: "3",
        Component1: "Y component: Quantization table 0, Sampling factors 1 horiz/1 vert",
        Component2: "Cb component: Quantization table 1, Sampling factors 1 horiz/1 vert",
        Component3: "Cr component: Quantization table 1, Sampling factors 1 horiz/1 vert"
    },
    JpegComment: {
        JPEGComment: "File written by Adobe Photoshop 4.0"
    },
    AdobeJPEG: {
        DCTEncodeVersion: "1",
        Flags0: "0",
        Flags1: "0",
        ColorTransform: "YCbCr"
    },
    JFIF: {
        Version: "1.2",
        ResolutionUnits: "inch",
        XResolution: "100 dots",
        YResolution: "100 dots"
    }
}
```

> Implements the ```io.pictura.servlet.ImageRequestStrategy``` interface.

**[\[⬆\]](#table-of-contents)**

### `io.pictura.servlet.PDFRequestProcessor`

> Requires the optional dependency ```org.apache.pdfbox:pdfbox```. For the 
> first use (request), the request processor will check whether the dependency 
> is available or not. 

Produces a non-image, **PDF** file format. The actual image will be part of
the generated PDF document.

The image processor strategy is listening for requested ```PDF``` output file 
formats ```/F=PDF/image.jpg```).

**[\[⬆\]](#table-of-contents)**

## Request Processor Factory

A custom image request processor factory must implements the interface
```io.pictura.servlet.ImageRequestProcessorFactory```. The following example
demonstrates the usage of a custom image request processor factory. If a
request contains the HTTP header ```X-JPEG```, the factory will create a
custom image request processor which creates JPEG output images only, independent
from the specified format parameter.

> If you deal with dynamic client values like cookies, do not forget to 
> override the ```getTrueCacheKey() : String``` method to prevent caching
> errors.

**Example**

```java
package io.pictura.servlet.examples;

import ...

public class ImageRequestProcessorFactoryImpl implements ImageRequestProcessorFactory {
    
    @Override
    public ImageRequestProcessor createRequestProcessor(HttpServletRequest req) throws ServletException {
        if (req.getHeader("X-JPEG") != null) {
            return new JpegOnlyImageRequestProcessor();
        }
        return new ImageRequestProcessor();
    }
    
    public static final JpegOnlyImageRequestProcessor extends ImageRequestProcessor {
    
        @Override
        protected String getRequestedFormatName(HttpServletRequest req) {
            return "jpg";
        }
    }
}
```

Do not forget to set the servlet init parameter *requestProcessorFactory*,
otherwise the factory is not used by the Pictura servlet instance.

```xml
<web-app ...>
...
    <servlet>
        ...
        <init-param>
            <param-name>requestProcessorFactory</param-name>
            <param-value>io.pictura.servlet.examples.ImageRequestProcessorFactoryImpl</param-value>
        </init-param>
    </servlet>
...
</web-app>
```

**[\[⬆\]](#table-of-contents)**

## Resource Locators

A resource locator is used to locate a requested resource (image). In this 
connection it is irrelevant where the resource is located; for example on the 
local disk or on a remote location. In any case, each resource locator returns
a valid URL which will contains the full qualified path to the requested 
resource or ```null``` if the resource can't be located by the resource locator.

As default (if nothing else was configured), the servlet will register a
```io.pictura.servlet.FileResourceLocator``` with the root path of your web
application. To customize the root path, you can implement and register your
own resource locator.

**Example 1**

File resource locator with resource path annotation.

```java
package io.pictura.servlet.examples;

import ...

@ResourcePath("/cms/content/static/images")
public class CustomFileResourceLocator extends FileResourceLocator {
}
```

> The resource path value must be an absolute file path.

**Example 2**

Multiple resource paths.

```java
package io.pictura.servlet.examples;

import ...

public class CustomFileResourceLocator extends FileResourceLocator {

    @Override
    protected String getRootPath() {
        return "/cms/content/static/images-1" 
            + File.pathSeparator 
            + "/cms/content/static/images-2";
    }
}
```

**Example 3**

Check source media type.

```java
package io.pictura.servlet.examples;

import ...

public class CustomFileResourceLocator extends FileResourceLocator {

    @Override
    protected boolean validate(File f) {
        if (super.validate(f)) {
            // Only allow jpeg images
            if (f.getAbsolutePath().endsWidth(".jpg")) {
                return true;
            }
        }
        return false;
    }
}
```

**Example 4**

Protect the source domain of remote located image files.

```java
package io.pictura.servlet.examples;

import ...

public class ProtectedHttpResourceLocator extends HttpResourceLocator {

    @Override
    public URL getResource(String path) throws MalformedURLException {
        if (path != null && path.startsWith("/tblr")) {
            return super.getResource("http://tumblr.com" + path.replaceFirst("/tblr", ""));
        }
        return null;
    }
}
```

**[\[⬆\]](#table-of-contents)**

## URL Connection Factory

You can create customized URL Connections to fetch remote located image 
resources if you register your own URL Connection Factory via the servlet init
parameter ```urlConnectionFactory```.

If `newConnection(URL, Properties)` is called from an image request processor,
the properties list contains the configured HTTP client connection settings as 
described in the following table.

|Property                                                 |Description|
|---------------------------------------------------------|-----------|
|`io.pictura.servlet.HTTP_CONNECT_TIMEOUT`                |A specified timeout value, in milliseconds, to be used when opening a communications link to the resource referenced by the URL object.|
|`io.pictura.servlet.HTTP_READ_TIMEOUT`                   |The read timeout to a specified timeout, in milliseconds.|
|`io.pictura.servlet.HTTP_MAX_FORWARDS`                   |A integer value to set the max-forwards.|
|`io.pictura.servlet.HTTP_FOLLOW_REDIRECTS`               |A boolean value to specifies whether HTTP redirects (requests with response code 3xx) should be automatically followed.|
|`io.pictura.servlet.HTTP_AGENT`                          |Specifies a custom user-agent string.|
|`io.pictura.servlet.HTTPS_DISABLE_CERTIFICATE_VALIDATION`|A boolean value to disable the HTTPS certificate validation.|
|`io.pictura.servlet.HTTP_PROXY_HOST`                     |The hostname, or address, of the proxy server.|
|`io.pictura.servlet.HTTP_PROXY_PORT`                     |The port number of the proxy server.|
|`io.pictura.servlet.HTTPS_PROXY_HOST`                    |The hostname, or address, of the proxy server (used by the HTTPS protocol handler).|
|`io.pictura.servlet.HTTPS_PROXY_PORT`                    |The port number of the proxy server (used by the HTTPS protocol handler).|

**Example 1**

Add a custom request header.

```java
package io.pictura.servlet.examples;

import ...

public class CustomConnectionFactory implements URLConnectionFactory {

    @Override
    public URLConnection newConnection(URL url, Properties props) throws IOException {
        // Use the default factory to create an URLConnection with the default
        // settings (e.g. timeout, certificate validation, etc.)
        URLConnectionFactory factory = URLConnectionFactory.DefaultURLConnectionFactory.getDefault();
        URLConnection connection = factory.newConnection(url, props);
        
        // Add custom request header
        connection.setRequestProperty("X-Header-Name", "Header-Value");
        
        return connection;
    }
}
```

**Example 2**

Basic authentication request.

```java
package io.pictura.servlet.examples;

import ...

public class CustomConnectionFactory implements URLConnectionFactory {

    @Override
    public URLConnection newConnection(URL url, Properties props) throws IOException {
        // Use the default factory to create an URLConnection with the default
        // settings (e.g. timeout, certificate validation, etc.)
        URLConnectionFactory factory = URLConnectionFactory.DefaultURLConnectionFactory.getDefault();
        URLConnection connection = factory.newConnection(url, props);
        
        // Condition
        if (url.toExternalForm().startsWith("http://example.com/")) {
            String userPassword = "username" + ":" + "password";
            connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBase64(userPassword.getBytes()));
        }
        
        return connection;
    }
}
```

**[\[⬆\]](#table-of-contents)**

## Params Interceptor

> Please note, you can set params interceptors only with the corresponding 
> annotation or programmatically if you override the ```doProcess(...)```
> method from the ```PicturaServlet``` class.

Could be used to intercept and modify the request image parameters before the
image will be processed. The following example will remove all defined image
effects and set the pixelate effect depending on a developer defined condition.

**Example**

```java
package io.pictura.servlet.examples;

import ...

@WebServlet(...)
@PicturaParamsInterceptor(PaywallInterceptor.class)
public class InterceptablePicturaServlet extends PicturaServlet {
    
    private static final long serialVersionUID = -1L;
    
    public static final class PaywallInterceptor implements ParamsInterceptor {
        
        @Override
        public String getVaryCacheKey(String trueCacheKey, HttpServletRequest req) {
            if (hasPaid(req)) {
                return trueCacheKey;
            }
            return trueCacheKey + ... ;
        }

        @Override
        public Map<String, String> intercept(Map<String, String> params, HttpServletRequest req) {
            // Add pixelate effect to the response image if the user has not yet paid
            if (!hasPaid(req)) {
                params.put("e", "px");                
            }
            return params;
        }

        private boolean hasPaid(HttpServletRequest req) {
            return ... ;
        }
    }
}
```

**[\[⬆\]](#table-of-contents)**

## Image Interceptor

> Please note, you can set image interceptors only with the corresponding 
> annotation or programmatically if you override the ```doProcess(...)```
> method from the ```PicturaServlet``` class.

Could be used to intercept and modify an image before the image will be encode 
and write out to the response stream. The following example will render a
watermark image to each output image (bottom right corner).

> If you have *1..n* image interceptors, the interceptors are called in the
> specified order.

**Example**

Add a small watermark to the bottom right corner of each image.

```java
package io.pictura.servlet.examples;

import ...

@PicturaImageInterceptor(WatermarkInterceptor.class)
public class WatermarkServlet extends PicturaServlet {
    
    private static final long serialVersionUID = -1L;
    
    public static final class WatermarkInterceptor implements ImageInterceptor {
        
        private BufferedImage watermark;
        
        static {
            watermark = ImageIO.read(...);
        }
        
        @Override
        public String getVaryCacheKey(String trueCacheKey, HttpServletRequest req) {
            return trueCacheKey;
        }
        
        @Override
        public BufferedImage intercept(BufferedImage img, HttpServletRequest req) {
            if (img.getWidth() >= 10 && img.getWidth() >= 10) {
                int w = watermark.getWidth() > watermark.getHeight()
                    ? watermark.getWidth() : watermark.getHeight();			

                float i = Math.max(8, (img.getWidth() / 10f));

                BufferedImage wm = watermark;
                if (w > i) {
                    wm = Pictura.resize(watermark, Pictura.Method.AUTOMATIC,
                            Pictura.Mode.FIT_EXACT, Math.round(i), Math.round(i));
                }

                // Bottom-Right
                int x = img.getWidth() - Math.round(1.2f * wm.getWidth());
                int y = img.getHeight() - Math.round(1.2f * wm.getHeight());

                Graphics2D g = (Graphics2D) img.getGraphics();

                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int rule = AlphaComposite.SRC_OVER;
                Composite comp = AlphaComposite.getInstance(rule, 0.65f);
                g.setComposite(comp);

                g.drawImage(wm, x, y, null);
                g.dispose();
            }
            return img;
        }
    }
}
```

**[\[⬆\]](#table-of-contents)**

## HTTP Cache

The servlet comes with an embedded in-memory HTTP cache to speed up delivery 
time and reduce server side resource consumption. 

Normally, application servers comes with there own in-memory cache API's, e.g. 
JBoss and WildFly uses the **Infinispan** cache. To enable the usage of 
the server default cache implementation, it is necessary to implement and 
register your own wrapper class.

**Example 1** (Infinispan)

```java
package io.pictura.servlet.examples;

import ...

public class InfinispanHttpCache implements HttpCache {

    private static final int CAPACITY = 1000;
    private static final long MAX_ENTRY_SIZE = 1024 * 1024 * 2; // 2MB
    
    private final Cache<String, SoftReference<HttpCacheEntry>> cache;
    
    public InfinispanHttpCache() {
        EmbeddedCacheManager manager = new DefaultCacheManager();
        manager.defineConfiguration("pictura-http-cache", new ConfigurationBuilder()
                .eviction().strategy(LRU).maxEntries(CAPACITY).build());
                
        this.cache = manager.getCache("pictura-http-cache");
        this.cache.start();
    }
    
    public void stop() {
        cache.stop();
    }
    
    @Override
    public HttpCacheEntry get(String key) {
        SoftReference<HttpCacheEntry> ref = cache.get(key);
        return ref != null ? ref.get() : null;
    }
    
    @Override
    public void put(String key, HttpCacheEntry entry) {
        if (MAX_ENTRY_SIZE > 0 && entry != null && entry.getContentLength() > MAX_ENTRY_SIZE) {
            return;
        }
        if (entry == null) {
            remove(key);
        } else {
            cache.put(key, new SoftReference<>(entry));
        }
    }
    
    @Override
    public boolean remove(String key) {
        return cache.remove(key) != null;
    }
    
    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(new ConcurrentSkipListSet<>(cache.keySet()));
    }
}
```

```xml
<web-app ...>
...
    <servlet>
        ...
        <init-param>
            <param-name>cacheEnabled</param-name>
            <param-value>true</param-value>
        </init-param>
        <init-param>
            <param-name>cacheClass</param-name>
            <param-value>io.pictura.servlet.examples.InfinispanHttpCache</param-value>
        </init-param>
    </servlet>
...
</web-app>
```

> Keep sure that responses from external resources contains an ```Expires``` or 
> ```Cache-Control``` header. In cases of local file resources it is necessary 
> to define a custom ```io.pictura.servlet.CacheControlHandler```, too.

**Example 2** *(Shared Cache)*

If you register a `HttpCache` to your PicturaIO servlet instance, you can't
share this cache between different servlet or server instances by default. This
is because the servlet creates always a new (empty) cache instance for each servlet
instance. A shared cache could be helpful if you have *2..n* servlet instances
or server processes with a load balancer in front.

You can solve this, if you register your shared cache instance programmatically
instead of the servlet config:

```java
public class SharedHttpCachePicturaServlet extends PicturaServlet {        
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);            
        // Get or create a instance of your shared cache
        HttpCache sharedCacheInstance = ...            
        // Register the shared cache instance
        setHttpCache(sharedCacheInstance);
    }
}
```

For example, the `sharedCacheInstance` is a wrapped Infinispan `JCache` 
instance.

**[\[⬆\]](#table-of-contents)**

## Annotations

### @PicturaConfigFile

Annotation to set the path, relativ to ```/WEB-INF``` or as absolute path, to
the (optional) external configuration file.

**Example**

```java
package io.pictura.servlet.examples;

import ...

@WebServlet(...)
@PicturaConfigFile("pictura.xml")
public class ExternalConfigPicturaServlet extends PicturaServlet {
    
    private static final long serialVersionUID = -1L;
}
```

> In the example above, the configuration file is located at ```/WEB-INF/pictura.xml```.

**[\[⬆\]](#table-of-contents)**

### @PicturaImageInterceptor

Annotation to intercept the image output (```BufferedImage```) with custom
extensions like watermarks, etc.

**Example**

```java
package io.pictura.servlet.examples;

import ...

@WebServlet(...)
@PicturaImageInterceptor({ImageBorderInterceptor.class, WatermarkInterceptor.class})
public class InterceptablePicturaServlet extends PicturaServlet {
    
    private static final long serialVersionUID = -1L;
    
    public static final class WatermarkInterceptor implements ImageInterceptor {
        ...
    }
    
    public static final class ImageBorderInterceptor implements ImageInterceptor {
        ...
    }
}
```

> The interceptors are processed in the specified order.

**[\[⬆\]](#table-of-contents)**

### @PicturaParamsInterceptor

Annotation to intercept the image request parameters.

```java
package io.pictura.servlet.examples;

import ...

@WebServlet(...)
@PicturaParamsInterceptor(PaywallInterceptor.class)
public class InterceptablePicturaServlet extends PicturaServlet {
    
    private static final long serialVersionUID = -1L;
    
    public static final class PaywallInterceptor implements ParamsInterceptor {
        ...
    }
}
```

**[\[⬆\]](#table-of-contents)**

### @PicturaThreadFactory

Annotation to set a custom thread factory to process image requests.

**Example**

```java
package io.pictura.servlet.examples;

import ...

@WebServlet(...)
@PicturaThreadFactory(ServerThreadFactory.class)
public class ThreadFactoryPicturaServlet extends PicturaServlet {
    
    private static final long serialVersionUID = -1L;
    
    public static final class ServerThreadFactory implements ThreadFactory {
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(...);
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY + 2);
            return t;
        }
    }
}
```

**[\[⬆\]](#table-of-contents)**

### @ResourcePath

Annotation to set the root path for customized ```io.pictura.servlet.FileResourceLocator```'s.

**Example**

```java
package io.pictura.servlet.examples;

import ...

@ResourcePath("/cms/content/static/images")
public class CmsFileResourceLocator extends FileResourceLocator {
    ...
}
```

**[\[⬆\]](#table-of-contents)**

## Authentication

Authentication is done by the servlet-container. If you need it, you have to
add the appropriate sections to the `web.xml`.

**[\[⬆\]](#table-of-contents)**