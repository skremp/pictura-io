# PicturaIO Palette CSS API Reference

Extract dominant colors from any image. The result is given as **CSS**.

```css
.fg-0{color:#805444;}
.bg-0{background-color:#805444;}
.fg-1{color:#8c4c38;}
.bg-1{background-color:#8c4c38;}
.fg-2{color:#944c3c;}
.bg-2{background-color:#944c3c;}
```

**Preconditions**

1. Keep sure the ```io.pictura.servlet.CSSColorPaletteRequestProcessor``` is
   configured in the affected servlet instance.
2. Set the format name parameter to ```/F=PCSS``` for each request of this type.

## Table of Contents

  1. [Color Count](#color-count)
  1. [Ignore White](#ignore-white)
  1. [Prefix](#prefix)
  2. [Linear Gradient](#linear-gradient)

## Color Count

 ```CC={value}```
 
Specifies the number of colors to extract. Must be positive integer. Valid 
values are ```1 - 32```. The default value is ```8```.

**Example**

 ```/F=PCSS/CC=12/image.jpg```

**[\[⬆\]](#table-of-contents)**

## Ignore White

 ```IW={value}```
 
Specified whether white color shall be ignored while the color palette will be 
extract from the image. Valid values are ```1``` (true) and ```0``` (false). The 
default value is ```1```.

**Example**

 ```/F=PCSS/IW=0/image.jpg```

**[\[⬆\]](#table-of-contents)**

## Prefix

 ```PF={value}```
 
Specifies a string prefix to use for the CSS class names in the generated 
palette. As default this value is not set.

**Example**

 ```/F=PCSS/PF=foo/image.jpg```

**[\[⬆\]](#table-of-contents)**

## Linear Gradient

 ```LG={value}```
 
Specified whether the generated CSS shall be contains linear gardient background 
styles (CSS3). Valid values are ```1``` (true) and ```0``` (false). The default 
value is ```0```.

**Example**

 ```/F=PCSS/LG=1/image.jpg```

**[\[⬆\]](#table-of-contents)**