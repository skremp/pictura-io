# PicturaIO URL Builder API Reference

A Java client library for generating URLs with PicturaIO.

To begin creating PicturaIO URLs programmatically, simply add a dependency to
your project POM if you have a Maven project or add the jar to your project's 
classpath and import the PicturaIO Builder library.

## Table of Contents

  1. [Basic Usage](#basic-usage)
  1. [Domain Sharding](#domain-sharding)
  1. [Reuse Builders](#reuse-builders)
  1. [CDI](#cdi)

## Basic Usage

```java
import io.pictura.builder.URLBuilder;
import ...

public class Example {

    public static void main(String... args) throws Exception {
        URL url = new URLBuilder("http://localhost:8084/")
                .setImagePath("lenna.png")
                .setImageParameter("s", "w320")
                .joinImageParameter("s", "dpr2")
                .toURL();

        HttpURLConnection con = url.openConnection();
        con.connect();
        
        try (InputStream is = con.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
        } finally {
            con.disconnect();
        }
    }
}
```

For HTTPS support, simply change the endpoint protocol

```java
import io.pictura.builder.URLBuilder;
import ...

public class Example {

    public static void main(String... args) throws Exception {
        URL url = new URLBuilder("https://localhost:8084/")
                .setImagePath("lenna.png")
                .setImageParameter("s", "w320")
                .joinImageParameter("s", "dpr2")
                .toURL();
    }
}
```

**[\[⬆\]](#table-of-contents)**

## Domain Sharding

Domain sharding enables you to spread image requests across multiple domains.
This allows you to bypass the requests-per-host limits of browsers.

```java
import io.pictura.builder.URLBuilder;
import ...

public class Example {

    public static void main(String... args) throws Exception {
        URLBuilder builder = new URLBuilder("http://localhost:8084/img1", "http://localhost:9084/img2")
                .setImagePath("lenna.png")
                .setImageParameter("s", "w320")
                .joinImageParameter("s", "dpr2");

        System.out.println(builder.toString());
        System.out.println(builder.toString());
        System.out.println(builder.toString());
    }
}
```

The example above prints out:

```
http://localhost:8084/img1/s=w320,dpr2/lenna.png
http://localhost:9084/img2/s=w320,dpr2/lenna.png
http://localhost:8084/img1/s=w320,dpr2/lenna.png
```

**[\[⬆\]](#table-of-contents)**

## Reuse Builders

You can also reuse a builder instance to create different resource URLs for
the same endpoint.

```java
import io.pictura.builder.URLBuilder;
import ...

public class Example {

    public static void main(String... args) throws Exception {
        URLBuilder builder = new URLBuilder("http://localhost:8084/img1", "http://localhost:9084/img2")
                .setImagePath("lenna.png")
                .setImageParameter("s", "w320")
                .joinImageParameter("s", "dpr2");

        URL url1 = builder.toURL();
        System.out.println(url1.toExternalForm());

        builder.reset();
        builder.setImagePath("lenna.gif")
            .setImageParameter("f", "webp");

        URL url2 = builder.toURL();
        System.out.println(url2.toExternalForm());
    }
}
```

The example above prints out:

```
http://localhost:8084/img1/s=w320,dpr2/lenna.png
http://localhost:9084/img2/f=webp/lenna.gif
```

**[\[⬆\]](#table-of-contents)**

## CDI

The PicturaIO Builder library also provides an annotation (`@PicturaURL`) to
inject components into an application in a typesafe way.

**Inject URLs**

```java
@Inject
@PicturaURL(endpoint = "http://localhost:8084/",
        imagePath = "lenna.png",
        imageParams = {
            @ImageParam(name = "s", value = "w320"),
            @ImageParam(name = "s", value = "dpr3.5"),
            @ImageParam(name = "e", value = "AE")})
private URL imageURL;
```

**Inject URIs**

```java
@Inject
@PicturaURL(endpoint = "/img",
        imagePath = "lenna.png",
        imageParams = {
            @ImageParam(name = "s", value = "w320"),
            @ImageParam(name = "s", value = "dpr3.5"),
            @ImageParam(name = "e", value = "AE")})
private URI imageURI;
```

**Inject Strings**

```java
@Inject
@PicturaURL(endpoint = "https://localhost:8084/",
        imagePath = "lenna.png",
        imageParams = {
            @ImageParam(name = "s", value = "w320"),
            @ImageParam(name = "s", value = "dpr3.5"),
            @ImageParam(name = "e", value = "AE")})
private String image;
```

**Inject Images**

```java
@Inject
@PicturaURL(endpoint = "https://localhost:8084/",
        imagePath = "lenna.png",
        imageParams = {
            @ImageParam(name = "s", value = "w320"),
            @ImageParam(name = "s", value = "dpr3.5"),
            @ImageParam(name = "e", value = "AE")})
private Image image;
```

**Inject Images (via Proxy-Server)**

```java
@Inject
@InetProxy("proxy.foobar.com", 8088)
@PicturaURL(endpoint = "https://localhost:8084/",
        imagePath = "lenna.png",
        imageParams = {
            @ImageParam(name = "s", value = "w320"),
            @ImageParam(name = "s", value = "dpr3.5"),
            @ImageParam(name = "e", value = "AE")})
private Image image;
```

**[\[⬆\]](#table-of-contents)**