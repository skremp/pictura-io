# JavaScript Client Side API Reference

pictura.js is a dependency free JavaScript library that allows you to easily
use the PicturaIO API to make images on your web site or app responsive to
device size and pixel density. pictura.js is also directly bundeled with the 
core PicturaIO Java library.

## Table of Contents

  1. [Usage](#usage)
  1. [Style Images Using CSS](#style-images-using-css)
  1. [Handling Non-JavaScript Users](#handling-non-javascript-users)
  1. [Inject Images](#inject-images)
  1. [Script Parameters](#script-parameters) 
  1. [Script Functions](#script-functions) 
  1. [Data Attributes](#data-attributes)
  1. [Client Hint Cookie ](#client-hint-cookie)
 
## Usage

```html
<html>
    <head>
        ...
        <style type="text/css">
            .pictura { 
                ...
   `         }
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

**[\[⬆\]](#table-of-contents)**

## Style Images Using CSS

pictura.js respects the styles applied to each image element. It does not force 
you to specify any custom style attributes, instead it picks up the style you
have already specified using normal CSS or image attributes.

```html			
<style>
    .pictura {
        max-width: 100%;
    }
</style>
```
		
Styles are calculated by the browser in the following priority:

  1. Inline style attribute on the image element
  1. External stylesheets and inline style tags
  1. Image element width and/or height attributes

> **Note**: if you do not specify an image style, the responsive image will be 
> displayed in it's natural full size - just like a normal HTML image.

**[\[⬆\]](#table-of-contents)**

## `Handling Non-JavaScript Users`

Almost all browsers have JavaScript enabled but for accessibility & SEO use the 
following approach as a fallback for non-JavaScript browsers:

```javascript		
<img class="pictura" data-src="pictura/image.jpg" alt="Responsive image"/>
<noscript>
     <img src="image.jpg" alt="Fallback image">
</noscript>
```

**[\[⬆\]](#table-of-contents)**

## Inject Images

pictura.js only loads images present in the markup at the time the script is
executed.

If you inject image elements after page load using JavaScript, you have to tell 
pictura.js about it using the ```reload()``` method:

```html
<script>
    // create image element and append to the document
    var img = document.createElement('img');
    img.setAttribute('data-src', 'image.jpg');
    document.documentElement.appendChild(img); 
    // load image using responsive.io    
    PicturaIO.reload(img);
</script>
```

**[\[⬆\]](#table-of-contents)**

## Script Parameters

### debug

If set to ```true```, the script will automatically creats debug URL's instead
of default image URL's (debug URL's contains an additional ```debug``` request
parameter). The default value is ```false```.

**[\[⬆\]](#table-of-contents)**

### server

> Required parameter

Defines the service endpoint (URL or relative path), where the servlet is reachable.
Optionally, could be an array of *1..n* endpoints.

**Example 1**

```javascript
// Servlet path
PicturaIO = {
    init: {
        server: 'pictura/',
        ...
    }
};
```

**Example 2**

```javascript
// Absolute service address
PicturaIO = {
    init: {
        server: 'http://my-image-server.de/pictura/',
        ...
    }
};
```

**Example 3**

> Domain sharding is a technique to accelerate page load times by tricking 
> browsers into opening more simultaneous connections than are normally allowed. 
> It's a widely-used optimization tactic that enables browsers to make better 
> use of high-bandwidth internet connections.

```javascript
// Domain sharding
PicturaIO = {
    init: {
        server: ['http://my-image-server-1.de/pictura/', 'http://my-image-server-1.de/pictura/'],
        ...
    }
};
```

**[\[⬆\]](#table-of-contents)**

### delay

Defines a delay in millis before the script will start. Must be a positive 
integer. The default value is ```0```.

**[\[⬆\]](#table-of-contents)**

### imageClass

Defines the CSS image class name. Must be a string. If the value is **not**
 ```null```, only ```img``` tags with the specified CSS class name are 
considered by the script. As default, this value is not set.

**[\[⬆\]](#table-of-contents)**

### imageQuality

Sets the default image quality. Must be a string or ```null``` to unset. As
default, this value is not set.

**[\[⬆\]](#table-of-contents)**

### imageCompressionQuality

Sets the default image compression quality. Valid values are in the range 
```0 - 100```. As default, this value is not set.

**[\[⬆\]](#table-of-contents)**

### imageOnError

Callback function to notify for ```onError``` events. As default, there is no 
callback function set. If a callback function is specified and the callback
returns an alternative image source the script tries to load this one.

**Example 1**

```javascript
PicturaIO = {
    init: {
        imageOnError: function(event) {
            console.log('on image error: ' + event);
            return '/alternative-image.jpg';
        }
    }
};
```

**Example 2**

```javascript
PicturaIO.param('imageOnError', function(event) {
    console.log('on image error: ' + event);
    return '/alternative-image.jpg';
});
```

**[\[⬆\]](#table-of-contents)**

### imageOnLoad

Callback function to notify for ```onLoad``` events. As default, there is no 
callback function set.

**Example 1**

```javascript
PicturaIO = {
    init: {
        imageOnLoad: function(event) {
            console.log('on image load: ' + event);
        }
    }
};
```

**Example 2**

```javascript
PicturaIO.param('imageOnLoad', function(event) {
    console.log('on image load: ' + event);
});
```

**[\[⬆\]](#table-of-contents)**

### imageOnChange

Callback function to notify for ```onChange``` events. As default, there is no 
callback function set.

**Example 1**

```javascript
PicturaIO = {
    init: {
        imageOnChange: function(event) {
            console.log('on image change: ' + event);
        }
    }
};
```

**Example 2**

```javascript
PicturaIO.param('imageOnChange', function(event) {
    console.log('on image change: ' + event);
});
```

**[\[⬆\]](#table-of-contents)**

### imageResolver

Callback function to resolving custom client site image URL's. As default, there
is no callback function set.

**Example 1**

```javascript
PicturaIO = {
    init: {
        imageResolver: function(url, params) {
            return src;
        }
    }
};
```

**Example 2**

```javascript
PicturaIO.param('imageResolver', function(src, params) {
    return src;
});
```

**[\[⬆\]](#table-of-contents)**

### lazyLoad

Enables/disables lazy loading of images. The default value is ```false```.

**[\[⬆\]](#table-of-contents)**

### lazyLoadOffset

Vertical offset in px used for preloading images while scrolling. Must be a
positive integer or ```-1``` for auto calculation. The default value is ```-1```.

> The auto calculated value results to ```Viewport Height / 6```.

**[\[⬆\]](#table-of-contents)**

### reloadOnResize

If ```true``` resizes images on browser resize.

**[\[⬆\]](#table-of-contents)**

### reloadOnResizeDown

If ```true``` resizes images when scaled down in size.

**[\[⬆\]](#table-of-contents)**

### reloadOnOrientationChange

If ```true``` resizes images on device rotation (orientation change).

**[\[⬆\]](#table-of-contents)**

### reloadThreshold

Reload images only if the size changed is larger than the specified delta in px.
Must be positive integers. The default value is ```10```. 

**[\[⬆\]](#table-of-contents)**

## Script Functions

The following methods can be called once the JavaScript has loaded.

**[\[⬆\]](#table-of-contents)**

### param

Gets or sets a script init parameter.

**Example 1**

```javascript
var imageClass = PicturaIO.param('imageClass');
```

**Example 2**

```javascript
PicturaIO.param('imageCompressionQuality', 90);
```

**[\[⬆\]](#table-of-contents)**

### pcssURL

Converts the given image URL to a valid palette CSS URL.

**Example**

```javascript
var css = PicturaIO.pcssURL('/image.jpg');
```

> Requires a registered ```io.pictura.servlet.CSSColorPaletteRequestProcessor```
> request processor at the requested service endpoint.

**[\[⬆\]](#table-of-contents)**

### imgURL

Creates a Pictura image URL based on the source image URL.

**Example 1**

```javascript
var img = PicturaIO.imgURL('/image.jpg');
```

**Example 2**

```javascript
var img = PicturaIO.imgURL('/image.jpg', '/e=ae,gl/f=png,b64');
```

**[\[⬆\]](#table-of-contents)**

### serverAddr

Gets the configured service endpoint address.

**Example**

```javascript
var url = PicturaIO.serverAddr();
```

**[\[⬆\]](#table-of-contents)**

### reload

Reloads the specified image. If there is no image is specified or the image is 
```null```, this function triggers a reload of all images with the current 
global and image specific (data-params) parameters.

**Example 1**

```javascript
// Reload all images
PicturaIO.reload();
```

**Example 2**

```javascript
// Reload a specific image
PicturaIO.reload(document.getElementWithId(...));
```

**Example 3**

```javascript
// Reload a specific image with a delay of 500 milliseconds
PicturaIO.reload(document.getElementWithId(...), 500);
```

**[\[⬆\]](#table-of-contents)**

## Data Attributes

### `data-src`

Image path or URL.

**[\[⬆\]](#table-of-contents)**

### `data-srcset`

Image path or URL as ```srcset```. On clients without ```srcset``` support, the 
value of the ```src``` attribute will be used as the image source.

**Example**

```html
<img src="/1x1.gif" data-src="/image.jpg" data-srcset="/image-1x.jpg 1x, /image-2x.jpg 2x">
```

**[\[⬆\]](#table-of-contents)**

### `data-params`

Specifies the optional API parameters.

**Example**

```html
<img src="/1x1.gif" data-src="/image.jpg" data-params="/E=G/F=PNG/R=L">
```

**[\[⬆\]](#table-of-contents)**

### `data-lazyload`

If present and set, the attribute will override the global ```lazyLoad```
parameter with the specified value. Valid values are ```true``` or ```false```.

**Example**

```html
<img src="/1x1.gif" data-src="/image.jpg" data-lazyload="false">
```

**[\[⬆\]](#table-of-contents)**

### `data-skip`

If present and set to true, the script will ignore the affected image tag.
Valid values are ```true``` or ```false```.

**Example**

```html
<img src="/1x1.gif" data-src="/image.jpg" data-skip="true">
```

**[\[⬆\]](#table-of-contents)**

## Client Hint Cookie

### Usage

> Keep sure, the ```io.pictura.servlet.ClientHintRequestProcessor``` is used
> otherwise the cookie is ignored at server site.

```html
<html>
    <head>
        ...
        <!-- Load and run the client hint cookie script -->
        <script src="pictura/js/cookie.min.js" type="text/javascript"></script>
    </head>
    <body>
        ...
        <img src="/image.jpg" alt="">        
        ...        
    </body>
</html>
```

Now, the script will automatically set a client site cookie with alternative
client hint values (device pixel ratio, device width, device height, WebP
and JPEG 2000 support).

The optional cookie script could also set in combination with the default
script as fallback.

```html
<html>
    <head>
        ...
        <style type="text/css">
            .pictura { }
        </style>
        ...
        <!-- Load and run the client hint cookie script -->
        <script src="pictura/js/cookie.min.js" type="text/javascript"></script>
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

**[\[⬆\]](#table-of-contents)**