# Change Log

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