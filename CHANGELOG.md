# Change Log

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

* Added missing webp-imageio lib (required to run intefration tests).
* Removed unnecessary whitespace in cache control header.
* Fixed broken links in documentation.

## 1.0.1.Final

_2016-01-14_

* Fixed missing filename suffix (content-disposition) in cases of ICO, PDF, CSS 
  JS or JSON mime types if the download query param is present.
* Fixed missing cache control header at placeholder images.

## 1.0.0.Final

_2016-01-09_

* Initial version.