# PicturaIO Placeholder Image API Reference

The placeholder API is a quick and simple placeholder image generator embedded
into the default PicturaIO servlet. You can also use all available request
parameters, specified by the Pictura IO Image API, to apply custom image
operations like rotation or cropping.

<p align="center">
    <img src="misc/placeholder_300x60.gif" alt=""/>
    <p align="center"><i>Example placeholder image</i></p>
</p>

> The placeholder image producer is disabled by default. To enable this feature,
> you have to set the servlet init parameter `placeholderProducerEnabled` to
> `true`. For more details, please see the PicturaIO Image Servlet API Reference.

## Table of Contents

  1. [Dimension](#dimension)
  1. [Type Face](#type-face)
  1. [Background Color](#background-color)
  1. [Format](#format)
 
## Dimension

 `D={value}`

Controls the size (dimension) of the generated placeholder image. The value
must given in the form `{width}X{height}` in pixels.

**Example**

 `/ip/D=320x480`

**[\[⬆\]](#table-of-contents)**

## Type Face

Sets the font name to print the placeholder dimension. Valid values are depending
on the local graphics environment.

 `TF={value}`
 
 **Example**
 
  `/ip/TF=mono`

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

 `/ip/D=100x100/BG=RED/`
 
 `/ip/D=100x100/BG=BROWN/`

**[\[⬆\]](#table-of-contents)**

## Format

The default output format is `GIF`. To override this, you can use the Image API
format parameter `F`.

**Example**

 `/ip/D=100x100/BG=RED/F=PNG`

**[\[⬆\]](#table-of-contents)**