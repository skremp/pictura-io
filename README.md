![License Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)

# PicturaIO Image Servlet

PicturaIO is a full configurable, extandable and dependency free (core features) 
Java Servlet, based on the Servlet API 3.0, to make delivering responsive images 
for your web applications or apps easy and performant. It helps you dynamically 
resize, crop, trim, rotate, compress, apply effetcs and convert images for the 
ever increasing number of different screen sizes and displays with different 
pixel density.

<p align="center">
    <img src="readme.png" alt=""/>
</p>

Essentially, the servlet works like a proxy that lies between image assets and 
the browser or application requesting them. The servlet API is accessed entirely 
via placing a prefix before the original image URL. This prefix gives you 
declarative access to all of the different types of transformation that the
servlet can perform.

**Getting started**

Let's start with a quick example to see how it works. Let us say a PNG image 
with his natural size of 1024x768 px is required as JPEG image with a maximum 
width of 320 px, you have only to replace the original source image:

```html
<img src="/{CONTEXT-PATH}/{SERVLET-PATH}/image.png" alt="">
```

with the servlet path followed by the transformation rules and the location of
the source image:

```html
<img src="/{CONTEXT-PATH}/{SERVLET-PATH}/f=jpg/s=w320/image.png" alt="">
```

That's all!

## Build

Download the project (using [Git](http://git-scm.com/downloads)):

    $ git clone git@github.com:skremp/pictura-io.git

This should create a folder named `pictura-io` in your current directory. Change 
directory to the `pictura-io` folder, and issue the command below to build.

To build the project (using [Maven](http://maven.apache.org/download.cgi)):

    $ mvn package -Prelease

from the main project path. Optionally, you can also build the project without
running unit and integration-tests:

    $ mvn package -DskipTests=true -DskipITs=true -Prelease

The output JAR will be in the `servlet/target/` directory.

Currently, the recommended JDK for making a build is 
[Oracle JDK](http://www.oracle.com/technetwork/java/javase/overview/index.html) 
7.x or 8.x. It's also possible to build using [OpenJDK](http://openjdk.java.net/).

Optionally, you can install the project in your local Maven repository using:

    $ mvn install -Prelease

> **Note**: this requires Maven 3.0.3 or newer be installed

## Usage

To use the servlet in your web application project, include the `pictura-servlet-{VERSION}.jar`
in your web-application's classpath (`WEB-INF/lib`) or if your project is a 
Maven based project, append the dependency

```xml
<dependency>
    <groupId>io.pictura</groupId>
    <artifactId>pictura-servlet</artifactId>
    <verion>{VERSION}</verion>
</dependency>
```

to the project's `pom.xml`.

With Gradle append

```groovy
compile 'io.pictura:pictura-servlet:{VERSION}'
```

to the `build.gradle`.

Next, to `WEB-INF/web.xml` add

```xml
<web-app ...>
...
    <listener>
        <display-name>ImageIO Service Provider Loader/Unloader</display-name>
        <listener-class>io.pictura.servlet.IIOProviderContextListener</listener-class>
    </listener>
    ...
    <servlet>
        <servlet-name>PicturaServlet</servlet-name>
        <servlet-class>io.pictura.servlet.PicturaServlet</servlet-class>
        <init-param>
            <param-name>statsEnabled</param-name>
            <param-value>true</param-value>
        </init-param>
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

> The web application descriptor (`web.xml`) is not required with Servlet API 
> 3.0. You can also define servlets with the `@WebServlet` annotation.

After restarting the web application server you can visit 
```http://127.0.0.1:8080/pictura/stats``` (or whatever the address of your local
webapp and context) to see the current servlet status and statistics.

> **Note**: as default the status page is only viewable from localhost.

## Client Side JS

Once your PicturaIO servlet is registered then simply add the client side
dependency free pictura.js JavaScript library (only 4kB minified and compressed)
to your page and replace the image tag `src` attributes with `data-src` 
to enable the client side features:

```html
<html>
    <head>
        ...
        <style type="text/css">
            .pictura { }
        </style>
    </head>
    <body>
        ...
        <!-- Replace src attribute with the new data-src attribute -->
        <img class="pictura" data-src="IMAGE-PATH-OR-URL" alt="">
        ...        
        <script>
            <!-- Pictura client side configuration -->
            PicturaIO = {
                init: {
                    server: 'pictura/', 
                    delay: 500,
                    imageClass: 'pictura',
                    imageCompressionQuality: 80,
                    reloadOnResize: true,
                    reloadOnResizeDown: true,
                    lazyLoad: true
                }
            };
        </script>
        <!-- Load and run the client side script -->
        <script src="pictura/js/pictura.min.js" type="text/javascript"></script>
    </body>
</html>
```

After reloading the page, the images (marked with style class `.pictura` and the
`data-src` attribute instead of `src`) are automatically lazy loaded within the
correct image container size and device pixel ratio dimension.

> The service endpoint address (`PicturaIO.init.server`) could be relative or
> absolute.

### Asynchronous Method

To avoid http blocking you can also loading the file asynchronously:

```javascript
<!-- Load the script asynchronous -->
<script>
    (function () {
        var p = document.createElement("script");
        p.src = "pictura/js/pictura.min.js";
        p.type = "text/javascript";
        p.async = "true";
        var s = document.getElementsByTagName("script")[0];
        s.parentNode.insertBefore(p, s);
    })();
</script>
```

## Embedded

You can use PicturaIO in an embeddable servlet container, to embedd it in an
application or to run as standalone service.

**Example 1** *([Undertow](http://undertow.io/))*

```java
package io.pictura.servlet.examples;

import ...

public class UndertowPicturaServer {
    
    public static void main(String[] args) throws Exception {
        ServletInfo servletInfo = new ServletInfo("pictura", PicturaServlet.class)
            .setAsyncSupported(true);
            .setLoadOnStartup(1)
            .addInitParam(PicturaServlet.IPARAM_REQUEST_PROCESSOR_STRATEGY, 
                "io.pictura.servlet.CSSColorPaletteRequestProcessor,"
                + "io.pictura.servlet.ClientHintRequestProcessor,"
                + "io.pictura.servlet.AutoFormatRequestProcessor")
            .addInitParam(PicturaServlet.IPARAM_CACHE_ENABLED, "true")
            .addInitParam(PicturaServlet.IPARAM_RESOURCE_LOCATORS,
		"io.pictura.servlet.FileResourceLocator,"
		+ "io.pictura.servlet.HttpResourceLocator")
            .addMapping("/*");
        
        ResourceManager resourceManager = ...

        DeploymentInfo deploymentInfo = deployment()
		.setClassLoader(UndertowServletContainerTest.class.getClassLoader())
		.setDeploymentName("pictura.war")
		.setContextPath("/pictura")
		.setUrlEncoding("UTF-8")
                .setResourceManager(resourceManager)
		.addListener(new ListenerInfo(IIOProviderContextListener.class))
		.addServlet(servletInfo);

        DeploymentManager deploymentManager = defaultContainer().addDeployment(deploymentInfo);
	deploymentManager.deploy();
        
        PathHandler path = Handlers.path(Handlers.redirect("/pictura"))
		.addPrefixPath("/pictura", deploymentManager.start());

        Undertow undertow = Undertow.builder()
		.addHttpListener(8080, "localhost")
		.setHandler(path)
		.build();
	undertow.start();
    }
}
```

**Example 2** *([Jetty](http://www.eclipse.org/jetty/))*

```java
package io.pictura.servlet.examples;

import ...

public class JettyPicturaServer {
    
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        // Base path to the local resource files (images)
        context.setResourceBase(...);

        server.setHandler(context);
        context.addServlet(PicturaServlet.class, "/*");

        server.start();
        server.join();
    }
}
```

## Run & Play

The test package contains a simple embedded [Undertow](http://undertow.io/) 
server (`io.pictura.servlet.UndertowPicturaServer`) which deploys the PicturaIO 
servlet on localhost port 8084.

If you run this class from your IDE or command line (with Maven), the service is 
available at `http://localhost:8084/`. For example:

 * `http://localhost:8084/lenna.psd`
 * `http://localhost:8084/F=JPG/E=GL,A/lenna.psd`
 * `http://localhost:8084/F=WEBP/O=70/S=W320/lenna.jpg`
 * `http://localhost:8084/F=EXIF/cmyk-jpeg.jpg`
 * `http://localhost:8084/F=PDF/lenna.png`
 * `http://localhost:8084/F=PCSS/lenna.png`
 * `http://localhost:8084/ip/D=320x480/`
 * `http://localhost:8084/ip/D=320x480/BG=ORANGE/R=L`
 * `http://localhost:8084/js/pictura.js`

## Related Documentation

For a more complete set of documentation, go to

 1. [Servlet API Reference](servlet/doc/servlet-api_en.md)
 1. [Image API Reference](servlet/doc/image-api_en.md)
 1. [JS Client Side API Reference](servlet/doc/js-api_en.md)
 1. [Palette CSS API Reference](servlet/doc/pcss-api_en.md)
 1. [Placeholder Image API Reference](servlet/doc/placeholder-api_en.md)

## FAQ

Here is a list of frequently asked questions.

 1. **What does PicturaIO?**
    
    PicturaIO is an on-demand dependency free (core features) Java Servlet to 
    fetch (local or remote hosted) and transform image resources on the fly.

 1. **What is pictura.js?**

    pictura.js is the client side dependency free JavaScript library that allows 
    you to easily use the PicturaIO API to make images on your web site or app 
    responsive to device size and pixel density. The JavaScript library is part
    of PicturaIO.

 1. **How does PicturaIO work?**

    The PicturaIO servlet acts as a proxy that lies between image assets and 
    the browser or applications requesting them. In this connection it is 
    insignificant where your images are hosted, locally or remotely.

 1. **What are the core features?**

    * Converting image formats
    * Scaling, cropping, compressing and rotating images
    * Applying image effects
    * Automatic resize to fit any screen size
    * Optimizations to make images smaller
    * Embedded cache to deliver images in milliseconds
    * Custom interceptors
    * HTTP client hints

 1. **What image formats are supported?**

    Basically, the servlet uses the Java [ImageIO](http://docs.oracle.com/javase/7/docs/api/javax/imageio/package-summary.html)
    service provider interface. Therefore all image formats for which a plugin
    is available and installed are supported. The default implementations of 
    `javax.imageio` provide the following standard image format plug-ins:

    * JPEG, Progressive JPEG (r/w)
    * PNG (r/w)
    * BMP (r/w)
    * WBMP (r/w)
    * GIF (r/w)

    The servlet implementation also supports the following additional formats
    out of the box (embedded support):

    * Animated-GIF (r/w)
    * ICO (w)

    With additional 3rd party plug-ins you can extend the support for reading
    and/or writing other image formats like:

    * [WEBP, Lossless WEBP](https://bitbucket.org/luciad/webp-imageio) (r/w)
    * [JPEG 2000](https://github.com/jai-imageio/jai-imageio-jpeg2000) (r/w)
    * [PSD](https://github.com/haraldk/TwelveMonkeys) (r)
    * [TIFF](https://github.com/haraldk/TwelveMonkeys) (r/w)
    * [PCX](https://github.com/haraldk/TwelveMonkeys) (r/w)
    * [ICNS](https://github.com/haraldk/TwelveMonkeys) (r)
    * [PICT](https://github.com/haraldk/TwelveMonkeys) (r/w)
    * [JBIG2](https://github.com/levigo/jbig2-imageio) (r)
    * [DICOM](https://github.com/dcm4che/dcm4che) (r)

 1. **Can I control how long my images remain in a cache?**

    Yes. You can define your own cache control rules. The PicturaIO servlet
    will automatically set all cache control headers depending on your rule
    set. For more details, please see the related documentation above.

 1. **Can I use it as standalone service?**

    Yes, it's simple. Create a new web application, setup and configure the 
    `PicturaServlet`, compile and deploy the application to a servlet 
    container or application server your choice. Or simply use the servlet
    in an embedded servlet container your choice.

 1. **Can I extend or customize the servlet?**

    Yes. Please see the source code and Javadoc for more details.

 1. **What is the preferred operation mode?**

    As PicturaIO is based on the Servlet API 3.0, the preferred and optimized 
    operation mode is asynchronous.

 1. **What are the minimum requirements?**

    * Java 7 or later
    * Servlet API 3.0 or later
    * Maven 3.0.3 or later (to build)

 1. **What license is PicturaIO under?**

    PicturaIO is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Known Issues

 1. **CMYK Color Profile in JPEG's**

    Unfortunately, the JDK JPEG ImageIO plug-in does not support CMYK color
    profiles. If you require support for CMYK color profiles in JPEG images,
    you have to install a 3rd party ImageIO JPEG plug-in with support for CMYK
    color profiles, e.g. `com.twelvemonkeys.imageio:imageio-jpeg`, to solve
    this until the JDK embedded JPEG image reader does not support CMYK color
    profiles.

    To replace the default JDK JPEG ImageIO plug-in, add a dependency to
    a CMYK compatible JPEG ImageIO plug-in to Maven project's `POM` like

    ```xml
    <dependency>
        <groupId>com.twelvemonkeys.imageio</groupId>
        <artifactId>imageio-jpeg</artifactId>
        <version>3.2</version>
    </dependency>
    ```

    Next, exclude the default JDK JPEG ImageIO plug-in from your PicturaIO
    servlet instance by adding the init parameter `imageioSpiFilterExclude`

    ```xml
    <servlet>
        <servlet-name>PicturaServlet</servlet-name>
        <servlet-class>io.pictura.servlet.PicturaServlet</servlet-class>
        <init-param>
            <param-name>imageioSpiFilterExclude</param-name>
            <param-value>
                com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi,
                com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi
            </param-value>
        </init-param>
        ...
    </servlet>
    ```

 1. **Missing JEPG XR**

    Currently, there is no known JPEG XR Java ImageIO plug-in available.

## License

The project is distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0):

    Copyright (c) 2015, 2016, Steffen Kremp
    All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
