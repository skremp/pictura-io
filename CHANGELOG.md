# Change Log

## 1.2.0.Final

_TBD_

* Added support for auto image rotation depending on the source image
  orientation provided by Exif metadata.
* Added new image parameter *P* and *B* to apply padding and border around the 
  edges of an image.
* Added new image parameter *N* to extract a specified image frame from a
  sequence of *1..n* image frames (e.g. Animated-GIFs).
* Added new resize mode *CROP*.
* Added new convenience methods to load and save the current http image cache.
  Among the existing methods, now it is also possible to load and save cache 
  entries within I/O streams.
* Added new FTP resource locator.
* Added support for the upcoming Client Hint header *Save-Data*.
* Added new vibrance effect filter.
* Added the possibility to define a custom error handler in case of HTTP error
  responses (status code 4xx and 5xx).
* Introduced the URL Builder API (new module).
* Improved color value handling in image URL parameters. Now, 3-, 4-, 6- and
  8-digit values, to handle RGB and ARGB color values, are supported.
* Fixed unobserved maximum image resolution in cases of up-scaling.
* Fixed wrong palette CSS source URL computation in pictura.js if the format
  *PCSS* is already set in the origin source URL.
* Fixed browser viewport width and height update in pictura.js in case of
  window resizes.

## 1.1.4.Final

_2016-05-27_

* Fixed missing DPR check in cases of ICO output files.
* Fixed failed bounce check for effect filter arguments in cases of saturation 
  and gamma effects.
* Fixed missing HTTP response content type in cases of cached responses on 
  Apache Tomcat application servers.
* Fixed servlet startup failure in cases of missing optional dependencies where 
  the affected request processor was configured but the dependency is missing on
  the application class path.
* Fixed wrong interrupt message in placeholder request processor.

## 1.1.3.Final

_2016-05-20_

* Fixed missing support for *DELETE* requests if a cache instance is defined.

## 1.1.2.Final

_2016-04-29_

* Fixed limited support for various protocols of external resources. See also
  [Amazon S3 Resource Locator Example](https://github.com/skremp/pictura-io/wiki/Amazon-S3-Resource-Locator-Example).

## 1.1.1.Final

_2016-04-26_

* Fixed possible servlet ID collision.

## 1.1.0.Final

_2016-02-06_

* Added new saturation and median effect filters.
* Added new servlet parameter `useContainerPool` for throughput optimization
  if there is no dedicated thread pool executor is required. By default, this
  option is not automatically enabled.
* Improved I/O performance by replacing `FileInputStream`'s with `MappedByteBuffer`'s
  (NIO) and replacing the default JDK `ByteArrayInputStream` and `ByteArrayOutputStream`
  with a more efficiently and memory optimized variant.

## 1.0.3.Final

_2016-01-19_

* Fixed issue with client hint width if an explicitly height is requested via
  the default path parameter.

## 1.0.2.Final

_2016-01-16_

* Removed unnecessary whitespace in cache control header.

* Added missing webp-imageio lib (required to run integration tests).
* Fixed broken links in documentation.

## 1.0.1.Final

_2016-01-14_

* Fixed missing filename suffix (content-disposition) in cases of ICO, PDF, CSS 
  JS or JSON mime types if the download query param is present.
* Fixed missing cache control header at placeholder images.

## 1.0.0.Final

_2016-01-09_

* Initial version.