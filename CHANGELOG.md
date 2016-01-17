### 1.1.0.Final (TBD)

* Added new saturation effect filter.
* Added new servlet parameter `useContainerPool`.
* Improved I/O performance.
* Fixed issue with CH (client hint) width if the requested scale height is
  given explicitly.

### 1.0.2.Final (2016-01-16)

* Added missing webp-imageio lib (required to run integration tests).
* Removed unnecessary whitespace in cache control header.
* Fixed broken links in documentation.

### 1.0.1.Final (2016-01-14)

* Fixed missing filename suffix (content-disposition) in cases of ICO, PDF, CSS 
  JS or JSON mime types if the download query param is present.
* Fixed missing cache control header at placeholder images.

### 1.0.0.Final (2016-01-09)

* Initial version.
