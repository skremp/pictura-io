# PicturaIO Placeholder Image API Reference

The placeholder API is a quick and simple placeholder image generator embedded
into the default PicturaIO servlet. You can also use all available request
parameters, specified by the Pictura IO Image API, to apply custom image
operations like rotation or cropping.

<div style="text-align: center; margin: 2em;">
    <img src="data:image/gif;UTF-8;base64,R0lGODlhLAE8APUAAAAAAGFhYWJiYmRkZGVl
         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
         mGYA6zmMdM5jKb+cxoTrOa18zmNksrASAAOw==" alt=""/>
    <p style="font-size: 0.9em; margin-top: 2px;"><i>Example placeholder image</i></p>
</div>

> The placeholder image producer is disabled by default. To enable this feature,
> you have to set the servlet init parameter `placeholderProducerEnabled` to
> `true`. For more details, please see the PicturaIO Image Servlet API Reference.

## Table of Contents

  1. [Dimension](#dimension)
  1. [Type Face](#type-face)
  1. [Background Color](#background-color)
  1. [Format](#format)
 
## Dimension

 ```D={value}```

Controls the size (dimension) of the generated placeholder image. The value
must given in the form ```{width}X{height}``` in pixels.

**Example**

 ```/ip/D=320x480```

**[\[⬆\]](#table-of-contents)**

## Type Face

Sets the font name to print the placeholder dimension. Valid values are depending
on the local graphics environment.

 ```TF={value}```
 
 **Example**
 
  ```/ip/TF=mono```

**[\[⬆\]](#table-of-contents)**

## Background Color

Extends the default behaviour of the background color parameter with named
(predefined) colors. Additionally to the default hexadecimal color values you
can also use the following color names:

 * red
 * pink
 * purple
 * deeppurple
 * indigo
 * blue
 * lightblue
 * cyan
 * teal
 * green
 * lightgreen
 * lime
 * yellow
 * amber
 * orange
 * deeporange
 * brown
 * grey
 * bluegrey

**Examples**

 ```/ip/D=100x100/BG=RED/```
 
 ```/ip/D=100x100/BG=BROWN/```

**[\[⬆\]](#table-of-contents)**

## Format

The default output format is `GIF`. To override this, you can use the Image API
format parameter `F`.

**Example**

 ```/ip/D=100x100/BG=RED/F=PNG```

**[\[⬆\]](#table-of-contents)**