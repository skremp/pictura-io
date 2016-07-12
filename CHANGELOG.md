# Change Log

## 1.3.0.Final

_TBD_

* TODO: Added interface and a default implementation to securing image URLs to
  preventing unauthorized parties from changing the parameters on image URLs.
* Added new annotation *PicturaServletConfig* to configure any *PicturaServlet*
  directly with a single annotation (PicturaIO related parameters).
* Added a default error handler implementation for client errors (4xx) and
  server errors (5xx).

## 1.2.0.Final

_TBD_

* Added new image parameter *P* and *B* to apply padding and border around the 
  edges of an image.
* Added new image parameter *N* to extract a specified image frame from a
  sequence of *1..n* image frames (e.g. Animated-GIFs).
* Added resize mode *CROP*.
* Added new vibrance effect filter.
* Added new FTP resource locator.
* Added support for auto image rotation depending on the source image
  orientation provided by Exif metadata.
* Added support for the upcoming Client Hint header *Save-Data*.
* Added convenience methods to load and save the current http image cache.
  Among the existing methods, now it is also possible to load and save cache 
  entries within I/O streams.
* Added the possibility to define a custom error handler in case of HTTP error
  responses (status code 4xx and 5xx).
* Added optional response header *X-Pictura-NormalizedParams*.
* Added support for JEE 7 JSON API in cases of EXIF metadata requests.
* Improved color value handling in image URL parameters. Now, 3-, 4-, 6- and
  8-digit values, to handle RGB and ARGB color values, are supported.
* Fixed unobserved maximum image resolution in case of up-scaling images.
* Fixed *resourcePaths* in the base *RequestProcessor*. If the value was
  overridden (by method) the new value was not used while the requested path
  was checked by the *RequestProcessor* himself.

## 1.1.5.Final

_2016-06-07_

* Fixed wrong error messages.
* Fixed an error while the internal servlet ID is calculated.
* Fixed broken *onResize* handling in client side JS.

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