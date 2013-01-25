ImageViewer usage:

1. copy gwt-image-loader-1.1.1.jar to your web project under WEB-INF/lib
2. copy ImageViewer-0.4.0/ImageViewer-0.4.0.jar to your web project 

under WEB-INF/lib
3. Recompile the widgetset

For now there are no detailed instructions for use;
however, the JavaDocs in ImageViewer.java should be enough to get you started.

As a general tip you should probably keep the size of the images at a maximum of 300kB
since the visible images are loaded to the browser and resized using the browser's 
resize functionality.

GWT Image Loader used under the Apache License 2.0.
Project homepage: http://code.google.com/p/gwt-image-loader/

ImageViewer is licensed under the Apache License 2.0.

Browser tested to provide full functionality:
* Firefox 3
* Firefox 4
* IE 6
* IE 7
* IE 8
* IE 9
* Opera 10
* Opera 11
* Chrome

Known issues:
* Safari 5: Works, but with animation glitches
* Chrome: Works, but there is a minor random animation glitch when expanding an image
* IE6-9: Works, but animations are noticeably slower than with other browsers

The sample photos provided with the demo application are by Bruno Monginoux 
and they are CC-by-NC-ND licensed from www.Landscape-Photo.net. 