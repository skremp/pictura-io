# `webp-imageio`

Java ImageIO reader and writer for the [Google WebP](https://developers.google.com/speed/webp/) 
image format.

This is a binary redistribution from https://bitbucket.org/luciad/webp-imageio/ 
which is is distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
and is also used in the build test phase from the PicturaIO Servlet.

This distribution contains native libraries for the following platforms:

* Linux i686
* Linux x86_64
* Mac OS X x86_64
* Windows i686
* Windows x86_64

**Usage**

* Add webp-imageio.jar to the classpath of your application.
* Ensure `libwebp-imageio.so` (Linux), `libwebp-imageio.dylib` (Mac OS X) or 
  `webp-imageio.dll` (Windows) is accessible on the Java native library path 
  (`java.library.path` system property).
* The WebP reader and writer can be used like any other Image I/O reader and writer.

## Install

You can't directly use this library because you have to install (copy) the native
library in at least one of the defined Java library path. This is specified with 
the system property `java.library.path` *(list of paths to search when loading 
libraries)*.

Once this is done, import the JAR (`webp-imageio.jar`) in your local Maven 
repository. For this, run from the command line:

```bash
$ mvn install:install-file -Dfile=webp-imageio.jar -DgroupId=com.luciad.imageio 
  -DartifactId=webp -Dversion=0.4 -Dpackaging=jar
```

Now, you can add the dependency

```xml
<dependency>
    <groupId>com.luciad.imagio</groupId>
    <artifactId>webp</artifactId>
    <version>0.4</version>
</dependency>
```

to your Maven project.

### Install at Runtime

Alternatively, you can use the utility class `io.pictura.servlet.LibWebp` from
the test sources to load the native library and the JAR package at runtime.

Usage

```java
// Base path to the native libraries
String libwebp = ...

LibWebp.addLibraryPath(System.getProperty("java.io.tmpdir"));
LibWebp.addLibWebp(libwebp);
```

> **Note:** this works not with all class loaders and you need read and write
> permissions on the used paths.
