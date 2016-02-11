# PicturaIO Image API Reference

Once your servlet is configured and deployed, you can begin making requests.
All PicturaIO image requests have the same basic structure:

<p align="center">
    <img src="misc/url_schema.png" alt=""/>
</p>

A image processor request path parameter `{PARAM}` is always a key-value-pair
`{NAME}={VALUE}` where `{NAME}` is the operation and `{VALUE}` are the arguments 
for the requested operation.

A parameter can also have *1..n* values. Multiple values are comma-separated

    {NAME}={VALUE-1},{VALUE-2},...,{VALUE-n}

As an alternative to the comma-separated notation, it is also possible to 
separate multiple values by reusing the path notation

    {NAME}={VALUE-1}/{NAME}={VALUE-2}/.../{NAME}={VALUE-n}

A full qualified request path is defined as

    /{CONTEXT-PATH}/{SERVLET-PATH}/{PARAM-1}/{PARAM-2}/.../{PARAM-n}/{IMAGE-PATH}

or with an optional query string as

    /{CONTEXT-PATH}/{SERVLET-PATH}/{PARAM-1}/{PARAM-2}/.../{PARAM-n}/{IMAGE-PATH}?{QUERY-STRING}

> Note, in cases if the image path contains query parameters (e.g. remote
> located image resources), the image path must be URL encoded.

## Table of Contents

  1. [Format](#format)
  1. [Quality](#quality)
  1. [Compression](#compression)
  1. [Resize](#resize)
  1. [Crop](#crop)
  1. [Trim](#trim)
  1. [Rotation](#rotation)
  1. [Effects](#effects)
  1. [Background](#background)

## Format

 `F={value}`

The output image format to convert the image to. Valid values are depending on
the installed Java ImageIO plugins. As default, the Java runtime environment
comes with support for JPEG, PNG, GIF, BMP and WBMP. For other output formats
like WebP or JPEG 2000 please install 3rd party plug-ins.

You can also specify whether the returned image should be returned with
baseline or progressive optimization by appending (comma separated) `B` for
baseline or `P` for progressive to the value. As default, progressive is
used if the output format supports this mode.

If the output image is required as Base 64 encoded image you can append (comma
separated) `B64` to the value. In this case, the response content type is
always `text/plain` instead of the underlying image mime type.

**Examples**

 `/F=JPG/image.png`

 `/F=JPG,B/image.png`

 `/F=JPG,B64/image.png`
 
 `/F=JPG,B,B64/image.png`

**[\[⬆\]](#table-of-contents)**

## Quality

 `Q={value}`
 
Controls the output image quality. Valid values are `A` auto, `UH` ultra
high, `H` high, `M` medium or `L` low. 

**Examples**

 `/Q=M/image.jpg`

|Ultra High|High|Medium|Low|
|----------|----|------|---|
|![A](misc/lenna_quality_uh.jpg)|![A](misc/lenna_quality_h.jpg)|![A](misc/lenna_quality_m.jpg)|![A](misc/lenna_quality_l.jpg)|

**[\[⬆\]](#table-of-contents)**

## Compression

`O={value}`

Controls the output compression ratio (quality) of lossy file formats. Valid
values are in the range `0 - 100`. The default value is depending on the
output file format, the used image request processor and the overall quality
parameter. If the image output file format supports lossless compression you
can set the value to `100` to enable lossless compression. For example,
WebP supports lossless image compression.

**Examples**

 `/O=75/image.jpg`
 
 `/F=WEBP/O=100/image.jpg`

|Source|Ratio 80%|Ratio 60%|Ratio 40%|
|------|---------|---------|---------|
|![A](misc/lenna.jpg)|![A](misc/lenna_compression_80.jpg)|![A](misc/lenna_compression_60.jpg)|![A](misc/lenna_compression_40.jpg)|

**[\[⬆\]](#table-of-contents)**

## Resize

 `S={params}`

Control the output dimension of your image.

**[\[⬆\]](#table-of-contents)**

### Image Width

 `W{value}`
 
The width of the output image. Must be a positive integer greater than 0. The 
value is interpreted as a pixel width. The resulting image will be `W`
pixels tall.

 `W{value}P`
 
The width of the output image interpreted as a ratio in relation to the source 
image size. For example, a `W` value of 50 will result in an output image 
half the width of the input image.

If only one dimension is specified, the other dimension will be calculated 
according to the aspect ratio of the input image. If both width and height are 
omitted, then the input image's dimensions are used.

**Examples**

 `/S=W320/image.jpg`
 
 `/S=50P/image.jpg`

**[\[⬆\]](#table-of-contents)**

### Image Height

 `H{value}`
 
The height of the output image. Must be a positive integer greater than 0. The 
value is interpreted as a pixel height. The resulting image will be `H`
pixels wide.

 `H{value}P`

The height of the output image interpreted as a ratio in relation to the source 
image size. For example, a `H` value of 50 will result in an output image 
half the height of the input image.

If only one dimension is specified, the other dimension will be calculated 
according to the aspect ratio of the input image. If both width and height are 
omitted, then the input image's dimensions are used.

**Examples**

 `/S=H480/image.jpg`
 
 `/S=50P/image.jpg`

**[\[⬆\]](#table-of-contents)**

### Pixel Density

 `DPR{value}`
 
Control the output density of your image. The device pixel ratio is used to 
easily convert between CSS pixels and device pixels. This makes it possible to 
display images at the correct pixel density on a variety of devices.

You must specify either a width, a height, or both for this parameter to work.

The default value is 1.

**Examples**

 `/S=W320,DPR1.5/image.jpg`
 
 `/S=W320,H480,DPR2/image.jpg`

**[\[⬆\]](#table-of-contents)**

### Resize Method

 `Q{value}`
 
Control the scaling algorithm. Valid values are `0 - 4`. The default value
is `0`, which will automatically use the algorithm in order to get the best
looking scaled image in the least amount of time.

`1` (balanced) is used to indicate that the scaling implementation should 
use a scaling operation balanced between spped and quality.

`2` (speed) is used to indicate that the scaling implementation should 
scale as fast as possible and return a result. For smaller images (800px in 
size) this can result in noticeable aliasing.
 
`3` (quality) is used to indicate that the scaling implementation should 
do everything it can to create as nice of a result as possible. This approach is
most important for smaller pictures (800px or smaller) and less important for 
larger images.

`4` (ultra quality) is used to make the image look exceptionally good at 
the cost of more processing and response time.

You must specify either a width, a height, or both for this parameter to work.

**Examples**

 `/S=W320,Q2/image.jpg`
 
 `/S=W320,DPR2,Q4/image.jpg`

**[\[⬆\]](#table-of-contents)**

### Resize Mode

 `M{value}`

Control the resizing mode. Valid values are `0 - 4`.

`0` (automatic) is used to automatically calculate dimensions for the 
resultant image by looking at the image's orientation and generating 
proportional dimensions that best fit into the target width and height given.

`1` (best fit both) is used to calculate dimensions for the largest image 
that fit within the bounding box, without cropping or distortion, retaining the 
original proportions.

`2` (fit exact) is used to fit the image to the exact dimensions given 
regardless of the image's proportions. If the dimensions are not proportionally
correct, this will introduce vertical or horizontal stretching to the image.

`3` (fit to width) is used to calculate dimensions for the resultant image 
that best-fit within the given width, regardless of the orientation of the 
image.

`4` (fit to height) is used to calculate dimensions for the resultant image 
that best-fit within the given height, regardless of the orientation of the 
image.

`5` (crop) is used to indicate that the scaling implementation should calculate
dimensions for the resultant image that best-fit within the given height and 
width, and then crop it to fit within the bounding box.

You must specify either a width, a height, or both for this parameter to work.

**Examples**

 `/S=W320,M2/image.jpg`
 
 `/S=W320,H480,Q2,M4/image.jpg`

**[\[⬆\]](#table-of-contents)**

## Crop

 `C={params}`
 
Controls how the input image is aligned. 

**[\[⬆\]](#table-of-contents)**

### X,Y Coordinates

Select a region of the image using the X,Y coordinate of the top left corner and 
the width & height of the region. This is useful for cropping images when you 
know the exact required pixel dimensions.

 `X{value}`

Sets the X-Axis position of the top left corner of the crop. To be used in 
conjunction with `Y`, `W` and `H`.
 
 `Y{value}`
 
Sets the Y-Axis position of the top left corner of the crop. To be used in 
conjunction with `X`, `W` and `H`.
 
 `W{value}`
 
Sets the width of the crop. To be used in conjunction with `X`, `Y` and `H`.
 
 `H{value}`
 
Sets the height of the crop. To be used in conjunction with `X`, `Y` and `W`.

**Examples**

 `/C=X10,Y20/image.jpg`
 
 `/C=X10,Y20,W200/image.jpg`
 
 `/C=X10,Y20,W200,H100/image.jpg`

**[\[⬆\]](#table-of-contents)**
 
### Edge Mode

Specifies top, right, bottom, left values to trim the edges of an image. This is
useful for removing unwanted borders or whitespace.

 `T{value}`
 
Top, to be used in conjunction with `L`, `R`, and `B` to allow cropping in from 
the edges of the image.
 
 `L{value}`

Left, to be used in conjunction with `L`, `T`, and `R` to allow cropping in from 
the edges of the image.
 
 `B{value}`

Bottom, to be used in conjunction with `T`, `R`, and `B` to allow cropping in 
from the edges of the image.
 
 `R{value}`

Right, to be used in conjunction with `L`, `T`, and `B` to allow cropping in from 
the edges of the image.

**Examples**

 `/C=T10/image.jpg`
 
 `/C=T10,L10/image.jpg`
 
 `/C=T10,L10,B20/image.jpg`
 
 `/C=T10,L10,B20,R30/image.jpg`

**[\[⬆\]](#table-of-contents)**
 
### Square Crop

 `SQ`
 
Select the largest square region from the center of the image. This sets the 
crop to a maximum size square area from the center of the image.

**Example**

 `/C=SQ/image.jpg`

|Source|Square Crop|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_crop_sq.jpg)|

**[\[⬆\]](#table-of-contents)**

### Aspect Ratio Crop

 `AR{width}X{height}`

Select the largest area of given aspect ratio from the image. This sets the 
proportions of the area to be selected from the center of the image.

**Examples**

 `/C=AR4X3/image.jpg`
 
 `/C=AR16X9/image.jpg`

|Source|AR 4x3|
|------|------|
|![A](misc/lenna.jpg)|![A](misc/lenna_crop_ar4x3.jpg)|

**[\[⬆\]](#table-of-contents)**

## Trim

 `T={value}`
 
Trimming an image removes a uniform border around the image. The value will
specify the tolerance of the color around the edge of the image. Valid values
are in the range `0 - 10`.

> The operation will change the size of the source image.

**Examples**

 `/T=5/image.jpg`

|Source|Trim Tolerance 5|
|------|----------------|
|![A](misc/lenna_trim.jpg)|![A](misc/lenna_trim_t5.jpg)|

**[\[⬆\]](#table-of-contents)**

## Rotation

 `R={value}`
 
Rotate and flip your image. Valid values, to flip the image horizontally or
vertically are `H` or `V`. To change the orientation you can set the value to 
`L` to rotate the image 90 degrees clockwise, `LR` or `RL` to rotate the image 
180 degrees clockwise or `R` to rotate the image 270 degrees clockwise.

**Examples**

 `/R=H/image.jpg`
 
 `/R=V/image.jpg`
 
 `/R=L/image.jpg`
 
 `/R=LR/image.jpg`
 
 `/R=R/image.jpg`

**[\[⬆\]](#table-of-contents)**
 
## Effects

 `E={params}`
 
Apply styles and effects to your image.

**[\[⬆\]](#table-of-contents)**

### Antialias (Blur)

 `A`
 
Applies a very light gaussian style blur to your output image that acts like an 
anti-aliasing filter (softens the image a bit).

**Example**

 `/E=A/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_a.jpg)|

**[\[⬆\]](#table-of-contents)**

### Brightness

 `B` or `B({value})`

Adjusts the brightness of the image. Must be a positive integer greater than 0.
The default value is `10`.

**Examples**

 `/E=B/image.jpg`
 
 `/E=B(5)/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_b.jpg)|

**[\[⬆\]](#table-of-contents)**

### Darkness

 `D` or `D({value})`

Adjusts the darkness of the image. Must be a positive integer greater than 0.
The default value is `10`.

**Examples**

 `/E=D/image.jpg`
 
 `/E=D(5)/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_d.jpg)|

**[\[⬆\]](#table-of-contents)**

### Sharpen

 `S`
 
Sharpens the image details using a convolution filter.
 
**Example**

 `/E=S/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_s.jpg)|

**[\[⬆\]](#table-of-contents)**

### Grayscale

 `G`
 
Applies a grayscale color conversion.
 
**Example**

 `/E=G/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_g.jpg)|

**[\[⬆\]](#table-of-contents)**

### Grayscale Luminosity

 `GL`
 
Applies a grayscale-luminosity color conversion. The luminosity method is a 
more sophisticated version of the average method. It also averages the values, 
but it forms a weighted average to account for human perception. We’re more 
sensitive to green than other colors, so green is weighted most heavily. 
 
**Example**

 `/E=GL/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_gl.jpg)|

**[\[⬆\]](#table-of-contents)**

### Sepia Tone

 `SP`
 
Applies a sepia toning effect to the image.
 
**Example**

 `/E=SP/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_sp.jpg)|

**[\[⬆\]](#table-of-contents)**
 
### Sunset Tone

 `SS`
 
Applies a sunset toning effect to the image.
 
**Example**

 `/E=SS/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_ss.jpg)|

**[\[⬆\]](#table-of-contents)**

### Invert

 `I`
 
Inverts all the pixel colors and brightness values within the image producing a 
negative of the image.
 
**Example**

 `/E=I/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_i.jpg)|

**[\[⬆\]](#table-of-contents)**

### Threshold

 `T({value})`
 
Valid values are in the range `0 - 255`. The default value is `127`.

**Examples**

 `/E=T/image.jpg`
 
 `/E=T(100)/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_t.jpg)|

**[\[⬆\]](#table-of-contents)**

### Posterize

 `P`

**Example**

 `/E=P/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_p.jpg)|

**[\[⬆\]](#table-of-contents)**

### Pixelate

 `PX({value})`
 
Applies a pixellation effect to the image. Valid values are in the 
range `10 - 100`. The default value is `10`.
 
**Examples**

 `/E=PX/image.jpg`
 
 `/E=PX(15)/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_px.jpg)|

**[\[⬆\]](#table-of-contents)**

### Median

 `M`

Applies a median (noise reduction) effect to the image.

**Example**

 `/E=M/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_m.jpg)|

**[\[⬆\]](#table-of-contents)**

### Noise

 `N`

Applies a noise effect to the image.

**Example**

 `/E=N/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_n.jpg)|

**[\[⬆\]](#table-of-contents)**

### Gamma

 `GAM({value})`
 
Adjusts gamma/midtone brightness. Valid values are in the range `-100`
to `100`. The default value is `0` which leaves the image unchanged.

**Example**

 `/E=GAM(25)/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_gam25.jpg)|

**[\[⬆\]](#table-of-contents)**

### Saturation

 `SAT({value})`
 
Adjusts the saturation of the image. Valid values are in the range `-100`
to `100`. The default value is `0` which leaves the image unchanged. A value of 
`-100` will convert the image to grayscale.

**Example**

 `/E=SAT(60)/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_sat60.jpg)|

**[\[⬆\]](#table-of-contents)**

### Auto Contrast

 `AC`

**Example**

 `/E=AC/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_ac.jpg)|

**[\[⬆\]](#table-of-contents)**

### Auto Level

 `AL`

**Example**

 `/E=AL/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_al.jpg)|

**[\[⬆\]](#table-of-contents)**

### Auto Color

 `AS`

**Example**

 `/E=AS/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_as.jpg)|

**[\[⬆\]](#table-of-contents)**

### Auto Enhancement

 `AE`

Applies all three auto filters `AC`, `AL` and `AS` which is equals to 
`/E=AC,AL,AS/image.jpg`.

**Example**

 `/E=AE/image.jpg`

|Source|Destination|
|------|-----------|
|![A](misc/lenna.jpg)|![A](misc/lenna_effect_ae.jpg)|

**[\[⬆\]](#table-of-contents)**

## Background
 
  `BG={value}`
  
Control the background color of your image. The background color to use when 
transparency is encountered. Valid values are 6-value (rgb) hexadecimal colors.
The default value is `FFFFFF`.

**Example**

 `/F=JPG/BG=336699/image.png`

**[\[⬆\]](#table-of-contents)**