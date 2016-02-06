# Change Log

## 1.2.0.Final

_TBD_

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
