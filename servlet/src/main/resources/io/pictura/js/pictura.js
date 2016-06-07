/*! 
 * PicturaIO 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

/**
 * Copyright 2015, 2016 Steffen Kremp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Usage:
 * 
 * 1. Add and configure the client side JavaScript file to the HTML page:
 * 
 * <pre>
 *   <!-- PicturaIO Client Side Init Params -->
 *   PicturaIO = {
 *     server: 'service/',
 *     imageClass: 'pictura',
 *     reloadOnResize: true,
 *     reloadOnResizeDown: true,
 *     lazyLoad: true
 *   }
 *   
 *   <!-- PicturaIO Client Side JavaScript -->
 *   &lt;script type="text/javascript" src="service/js/pictura.js"&gt;&lt;/script&gt;
 * </pre>
 * 
 * 2. Replace the src attribute with data-src and add a class attribute of pictura:
 * 
 * <pre>
 *   &ltimg data-src="http://www.your-site.com/image.jpg" class="pictura"/&gt;
 * </pre>
 */

if (!window['__PicturaIO__']) {

    // Prevent double script running on same page. Also note, we NEED to use 
    // string as closure compiler would otherwise compile this statement badly.

    "use strict";

    (function (window, document) {

        // User options
        var options = {};

        // Client side config
        if (typeof (PicturaIO) === 'object') {
            if (typeof (PicturaIO.init) !== undefined) {
                options = PicturaIO.init;
            }
        } else {
            PicturaIO = {
                init: {}
            };
        }

        /**
         * The current PicturaIO client side JavaScript implementation version.
         */
        PicturaIO.version = "${script.version}";

        /**
         * Reloads the specified image. If there is no image is specified or
         * is <code>null</code>, this function triggers a reload of all images
         * with the current global and image specific (data-params) parameters.
         * 
         * @param {Image} img The image element (node) to reload.
         * @param {Integer} delay An optional delay in millis to wait before the
         *   image will be reloaded.
         */
        PicturaIO.reload = function (img, delay) {
            var i = img;
            if (delay && delay > 0) {
                window.setTimeout(function () {
                    if (i === null) {
                        imgs = [];
                        findImages();
                    } else {
                        showImage(i);
                    }
                }, delay);
            } else {
                if (i === null || !i) {
                    imgs = [];
                    findImages();
                }
                showImage(i);
            }
        };

        /**
         * Gets, sets or overrides the specified parameter. The current set of 
         * parameters includes:
         * 
         * <table>
         * <tr>
         * <td><i>debug</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>server</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>delay</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>imageClass</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>imageQuality</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>imageCompressionQuality</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>imageOnError</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>imageOnLoad</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>imageOnChange</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>imageResolver</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>lazyLoad</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>lazyLoadOffset</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>reloadOnResize</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>reloadOnResizeDown</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>reloadOnOrientationChange</i></td>
         * <td></td>
         * </tr>
         * <tr>
         * <td><i>reloadThreshold</i></td>
         * <td></td>
         * </tr>         
         * </table>
         * 
         * @param {String} name Parameter name.
         * @param {Object} value The new parameter value.
         * 
         * @returns {Object} The old value of the specified parameter or in
         *   cases of read parameter the current value.
         */
        PicturaIO.param = function (name, value) {
            if (value === undefined) {
                return P[name];
            } else if (value !== undefined && name !== undefined) {
                var oldValue = P[name];
                P[name] = value;
                return oldValue;
            }
        };

        /**
         * Converts the given source image URL to a palette CSS URL.
         * <p>
         * Note: This function only creates the URL to request the CSS
         * resource. Keep sure, the palette CSS request processor is reachable
         * on the configured remote server endpoint.
         * 
         * @param {String} src URL of the source image.
         * @param {String} opt The optional data options.
         * 
         * @returns {String} URL to get the palette CSS for the specified
         *   source image with the optional data options.
         */
        PicturaIO.pcssURL = function (src, opt) {
            if (src && src.toLowerCase().indexOf("/f=pcss") === -1) {
                return buildSrcURL(getServer(), src, opt += '/F=PCSS');
            }
            return src;
        };

        PicturaIO.imgURL = function (src, opt) {
            return buildSrcURL(getServer(), src, opt);
        };

        /**
         * Returns the server address (endpoint) where the Pictura service is
         * running. In cases of multiple address URLs, the funtion will 
         * automatically rotate the addresses for each function call.
         * 
         * @returns Address of the remote service endpoint.
         */
        PicturaIO.serverAddr = function () {
            return getServer();
        };

        // Responsive image as a service options
        var P = {
            // Debug flag to automatically append the debug URL query parameter
            debug: options.debug || false,
            // Image I/O server (service) endpoint
            server: options.server || null,
            // An optional delay to wait after domready before the script
            // will search for images
            delay: options.delay || 0,
            // CSS class name for image tags as marker
            imageClass: options.imageClass || null,
            // Default image quality
            imageQuality: options.imageQuality || null,
            // Default image compression quality
            imageCompressionQuality: options.imageCompressionQuality || null,
            // Custom error handler
            imageOnError: options.imageOnError || null,
            // Custom on load callback
            imageOnLoad: options.imageOnLoad || null,
            // Custom on change callback
            imageOnChange: options.imageOnChange || null,
            // Custom image source resolver
            imageResolver: options.imageResolver || null,
            // Lazyload
            lazyLoad: options.lazyLoad || false,
            // Vertical offset in px. used for preloading images while scrolling
            lazyLoadOffset: options.lazyLoadOffset || -1,
            // Reload on resize
            reloadOnResize: options.reloadOnResize || true,
            // Reload on resize down
            reloadOnResizeDown: options.reloadOnResizeDown || false,
            // Reload on orientation change
            reloadOnOrientationChange: options.reloadOnOrientationChange || true,
            // Reload only if the size changed is larger than the specified delta in px (default is 10px)
            reloadThreshold: options.reloadThreshold || 10,
            // The attribute, which should be transformed to src
            srcAttr: 'data-src',
            // The attribute, which should be transformed to srcset
            srcsetAttr: 'data-srcset',
            // The attribute, used to set the optional image processing parameters
            paramsAttr: 'data-params'
        };

        var width = viewportW();
        var height = viewportH();

        var
        // Vertical offset in px. used for preloading images while scrolling
        offset = P.lazyLoadOffset > -1 ? P.lazyLoadOffset : height / 6,
        // Where to get real src            
        dataSrcAttr = P.srcAttr,
        // Where to get real srcset (optional)
        dataSrcsetAttr = P.srcsetAttr,
        // Where to get additional image params (optional)
        dataParamsAttr = P.paramsAttr,
        // Where to get custom layzyload (optional)
        dataLazyLoadAttr = 'data-lazyload',
        // If set to true, the script will ignore the related image tag
        dataSkipAttr = 'data-skip',       
        // Window width
        winW = width,
        // Window height
        winH = height,
        // Current consumed reload delta
        reloadThreshold = 0,
        // Client pixel ratio
        dpr = (window.devicePixelRatio !== undefined ? Math.round(window.devicePixelRatio * 100) / 100 : 1),
        // Self-populated page images array, we do not getElementsByTagName
        imgs = [],
        pageHasLoaded = false,
        unsubscribed = false,
        // Throttled functions, so that we do not call them too much
        saveViewportT = throttle(viewportH, 20),
        showImagesT = throttle(showImages, 20),
        // Resize timer to prevent polling
        resizeTimer = 0,
        // Flag if media print matches
        mediaPrint = false,
        // Server addr array index
        serverIndex = -1,
        // RegEx to detect raster image format names
        rasterImageFormatRegExp = /(\.(jpg|jpeg|JPG|JPEG)($|#.*|\?.*)|\.(png|PNG)($|#.*|\?.*)|\.(gif|GIF)($|#.*|\?.*)|\.(webp|WEBP)($|#.*|\?.*)|\.(bmp|BMP)($|#.*|\?.*)|\.(jp2|jpeg2000|JP2|JPEG2000)($|#.*|\?.*)|\.(ico)($|#.*|\?.*))/;

        // Override image element .getAttribute globally so that we give the real src
        // does not works for ie < 8: http://perfectionkills.com/whats-wrong-with-extending-the-dom/
        // Internet Explorer 7 (and below) [...] does not expose global Node, Element, 
        // HTMLElement, HTMLParagraphElement
        window['HTMLImageElement'] && overrideGetAttribute();

        // Called from every lazy <img> onload event
        window['__PicturaIO__'] = onDataSrcImgLoad;

        // init       
        if (P.delay && P.delay > 0) {
            window.setTimeout(function () {
                domready(findImages);
            }, P.delay);
        } else {
            domready(findImages);
        }
        addEvent(window, 'load', onLoad);
        addEvent(window, 'resize', onResize);

        // Bind events
        subscribe();

        // Force image loading if 'print' was called
        if (window.matchMedia) {
            window.matchMedia('print').addListener(function (e) {
                mediaPrint = true;
                showImages();
            });
        }

        // domain sharding
        function getServer() {
            return P.server === null ? null : (typeof (P.server) === 'string' ?
                    P.server : (typeof (P.server) === 'object' ? (P.server[P.server.length >
                            (serverIndex + 1) ? ++serverIndex : (serverIndex = 0)]) : P.server));
        }

        // called by img onload= or onerror= for IE6/7
        function onDataSrcImgLoad(img) {
            // if image is not already in the imgs array
            // it can already be in it if domready was fast and img onload slow
            if (inArray(img, imgs) === -1) {
                // this case happens when the page had loaded but we inserted more lazyload images with
                // javascript (ajax). We need to re-watch scroll/resize
                if (unsubscribed) {
                    subscribe();
                }
                showIfVisible(img, imgs.push(img) - 1);
            }
        }

        // find and merge images on domready with possibly already present onload= onerror= imgs
        function findImages() {
            var domreadyImgs = document.getElementsByTagName('img'),
                    domreadyDivs = P.imageClass ? document.getElementsByClassName(P.imageClass) : [],
                    currentImg;

            // remove imgs from the "div" list
            var buf = [];
            for (var i = 0; i < domreadyDivs.length; i++) {
                if (domreadyDivs[i].tagName.toLowerCase() === 'div') {
                    buf.push(domreadyDivs[i]);
                }
            }
            domreadyDivs = buf;

            // Concat the two node lists
            var domreadyEls = Array.prototype.slice.call(domreadyImgs).concat(
                    Array.prototype.slice.call(domreadyDivs));

            // merge them with already self onload registered imgs
            for (var imgIndex = 0, max = domreadyEls.length; imgIndex < max; imgIndex += 1) {
                currentImg = domreadyEls[imgIndex];
                if (currentImg.getAttribute(dataSrcAttr) && inArray(currentImg, imgs) === -1) {
                    imgs.push(currentImg);
                }
            }

            showImages();
            setTimeout(showImagesT, 25);
        }

        function onLoad() {
            pageHasLoaded = true;
            // if page height changes (hiding elements at start)
            // we should recheck for new in viewport images that need to be shown
            // see onload test
            showImagesT();
            // we could be the first to be notified about onload, so let others event handlers
            // pass and then try again
            // because they could change things on images
            setTimeout(showImagesT, 25);
        }

        function throttle(fn, minDelay) {
            var lastCall = 0;
            return function () {
                var now = +new Date();
                if (now - lastCall < minDelay) {
                    return;
                }
                lastCall = now;
                // we do not return anything as
                // https://github.com/documentcloud/underscore/issues/387
                fn.apply(this, arguments);
            };
        }

        // X-browser
        function addEvent(el, type, fn) {
            if (el.attachEvent) {
                el.attachEvent && el.attachEvent('on' + type, fn);
            } else {
                el.addEventListener(type, fn, false);
            }
        }

        // X-browser
        function removeEvent(el, type, fn) {
            if (el.detachEvent) {
                el.detachEvent && el.detachEvent('on' + type, fn);
            } else {
                el.removeEventListener(type, fn, false);
            }
        }

        // custom domready function
        // ripped from https://github.com/dperini/ContentLoaded/blob/master/src/contentloaded.js
        // http://javascript.nwbox.com/ContentLoaded/
        // http://javascript.nwbox.com/ContentLoaded/MIT-LICENSE
        // kept the inner logic, merged with our helpers/variables
        function domready(callback) {
            var done = false, top = true;

            function init(e) {
                if (e.type === 'readystatechange' && document.readyState !== 'complete') {
                    return;
                }
                removeEvent((e.type === 'load' ? window : document), e.type, init);
                if (!done) {
                    done = true;
                    callback();
                }
            }

            function poll() {
                try {
                    document.documentElement.doScroll('left');
                } catch (e) {
                    setTimeout(poll, 50);
                    return;
                }
                init('poll');
            }

            if (document.readyState === 'complete') {
                callback();
            } else {
                if (document.createEventObject && document.documentElement.doScroll) {
                    try {
                        top = !window.frameElement;
                    } catch (e) {
                    }
                    if (top) {
                        poll();
                    }
                }
                addEvent(document, 'DOMContentLoaded', init);
                addEvent(document, 'readystatechange', init);
                addEvent(window, 'load', init);
            }

        }

        function onResize() {
            winH = viewportH();
            winW = viewportW();
            offset = P.lazyLoadOffset > -1 ? P.lazyLoadOffset : height / 6;
        }

        function onResizeReload() {
            if (resizeTimer) {
                clearTimeout(resizeTimer);
            }
            resizeTimer = setTimeout(reloadIfViewportChanged, 500);
        }

        function onErrorImg(e) {
            if (e && e.target && e.target.dataset) {
                var alt = typeof (P.imageOnError) === 'function' ? P.imageOnError(e) : P.imageOnError;
                if (alt !== undefined && alt !== null && alt !== '') {
                    if (startsWith(alt, 'data:')) {
                        e.target['src'] = alt;
                    } else {
                        e.target['src'] = '';
                        e.target.dataset['src'] = alt;
                        showImage(e.target, true);
                    }
                }
            }
        }

        /**
         * Reload images if the new viewport width is larger than the
         * initial viewport as we have loaded the current displayed images.
         */
        function reloadIfViewportChanged() {
            var vw = viewportW();
            if (winW < vw || (imgs.length > 0 && P.reloadOnResizeDown)) {
                reloadThreshold += (winW - vw);
                if (P.reloadThreshold && P.reloadThreshold > 0) {
                    if ((reloadThreshold < 0 && P.reloadOnResizeDown && reloadThreshold > P.reloadThreshold) ||
                            (reloadThreshold > 0 && reloadThreshold < P.reloadThreshold)) {
                        return;
                    }
                }
                reloadThreshold = 0;
                winW = vw;
                imgs = [];
                findImages();
            }
        }

        // img = dom element
        // index = imgs array index
        function showIfVisible(img, index) {
            // We have to check that the current node is in the DOM
            // It could be a detached() dom node
            // http://bugs.jquery.com/ticket/4996                                    
            if (contains(document.documentElement, img) &&
                    !isSkipLoading(img) && (mediaPrint || !isLazyLoad(img) ||
                    img.getBoundingClientRect().top < winH + offset)) {

                // To avoid onload loop calls
                // removeAttribute on IE is not enough to prevent the event to fire
                img.onload = null;
                img.removeAttribute('onload');

                // on IE < 8 we get an onerror event instead of an onload event
                img.onerror = null;
                img.removeAttribute('onerror');

                showImage(img);
                imgs[index] = null;

                return true; // img shown
            }
            return false; // img to be shown
        }

        function isLazyLoad(img) {
            var lazyLoad = img.getAttribute(dataLazyLoadAttr);
            return (lazyLoad && lazyLoad === '1') ? true :
                    (P.lazyLoad ? ((lazyLoad && lazyLoad === '0') ? false : true) :
                            false);
        }

        function isSkipLoading(img) {
            var skip = img.getAttribute(dataSkipAttr);
            return skip && (skip === '1' || skip === 'true');
        }

        function buildSrcURL(server, src, opt, width, height) {

            // If the source image is not a raster image do not use Pictura
            if (src.match(rasterImageFormatRegExp) === null) {
                return;
            }

            // remove the server address if specified by the user
            src = src.replace(server, '');
            if (startsWith(src, 'http') && src.indexOf('?') > -1) {
                src = encodeURIComponent(src);
            }

            // append the initial "/" at the beginning of the relative path
            src = (startsWith(src, '/') ? '' : '/') + src;

            // get the optional image parameters
            if (opt !== null) {
                opt = (startsWith(opt, '/') ? '' : '/') + opt;
                opt = opt.toLowerCase();
            }

            // bounds check; if the image has no information about the
            // current client width, we will try to get this info 
            // directly from the parents element
            if (opt === null || (opt !== null && opt.indexOf('/s=w') === -1 &&
                    opt.indexOf('/s=h') === -1) && !(src.indexOf('/s=')) > -1) {
                if (width !== undefined) {
                    src = '/s=w' + width + (height > 0 ? ',h' + height : '') + ',dpr' + dpr + src;
                }
            }

            // if there is no custom quality is specified we will use
            // the global default specified quality.
            if (P.imageQuality !== null && !(opt !== null && opt.indexOf('/q=') > -1) &&
                    !(src.indexOf('/q=') > -1)) {
                src = '/q=' + P.imageQuality + src;
            }

            // set the global defined compression quality if the
            // global value is defined and the origin source image
            // does not defines anything else.
            if (P.imageCompressionQuality !== null && !(opt !== null && opt.indexOf('/o=') > -1) &&
                    !(src.indexOf('/o=') > -1)) {
                src = '/o=' + P.imageCompressionQuality + src;
            }

            // append the user image parameters
            if (opt !== null) {
                src = opt + src;
            }

            if (endsWith(server, '/') && startsWith(src, '/')) {
                src = src.substring(1);
            }

            // the new image source link (if debug enabled this will also
            // append the debug query parameter)
            return server + src + (P.debug === true ? '?debug' : '');
        }        

        function showImage(img, err) {
            
            if (typeof (P.imageOnChange) === 'function') {
                P.imageOnChange(img);
            }

            var src = img.getAttribute(dataSrcAttr),
                    srcset = img.getAttribute(dataSrcsetAttr) || null,
                    params = img.getAttribute(dataParamsAttr) || null;

            if ((!err || err === false) && typeof (P.imageResolver) === 'function') {
                src = P.imageResolver(src, params);
                srcset = null; // reset
            }

            var server = getServer();

            // calcualte the scale width only if the image is marked as
            // responsive image             
            if ((src || srcset) && server !== null &&
                    (P.imageClass === null ||
                            ((' ' + img.className + ' ').indexOf(' ' + P.imageClass + ' ') > -1))) {

                // Get the required image width and the device pixel ratio.
                // Also skip tracking pixels or placeholder pixels with a size
                // of 1x1 px.
                var w = (img.width !== undefined && img.width > 0) ? img.width
                        : (img.clientWidth > 1 && img.clientHeight > 1 &&
                                img.width !== img.clientWidth) ? img.clientWidth : 0,
                        h = -1;

                if ((w === undefined || w === 0) && img.parentElement !== undefined) {

                    var n = img.parentElement;
                    while (n.clientWidth < 1) {
                        n = n.parentElement;
                    }

                    w = n.clientWidth;
                    h = n.clientHeight;

                    if (w !== undefined && w > 0 && n.offsetWidth) {
                        if (w < n.offsetWidth) {
                            w = n.offsetWidth;
                            h = n.offsetHeight;
                        }
                    } else if (w === undefined || w === 0) {
                        w = winW;
                    }
                }

                // Test if we need to use an image from the srcset attribute
                var srcsetArr = srcset ? parseSrcset(srcset) : null;
                if (srcsetArr !== null) {
                    for (var i = 0; i < srcsetArr.length; i++) {
                        var srci = srcsetArr[i];
                        if (srci && srci['url']) {
                            var srcj = buildSrcURL(server, srci['url'], params, w);
                            if (srci['w']) {
                                srcj += ' ' + srci['w'] + 'w';
                            }
                            if (srci['h']) {
                                srcj += ' ' + srci['h'] + 'h';
                            }
                            if (srci['x']) {
                                srcj += ' ' + srci['x'] + 'x';
                            }
                            srcset = (i === 0 ? srcj : srcset + ', ' + srcj);
                        }
                    }
                }

                src = buildSrcURL(server, src, params, w, h);
            }

            if (typeof (P.imageOnLoad) === 'function' && !img.eventOnLoadAdded) {
                addEvent(img, 'load', function (e) {
                    P.imageOnLoad(e);
                    img.eventOnLoadAdded = true;
                });
            }

            // if the user has defined an onError fallback image we have
            // to register an onError event listener to this image.
            if (P.imageOnError !== null) {
                if (err !== undefined && err === true) {
                    // prevent endless loops in cases if the alternative
                    // image throws an error, too
                    removeEvent(img, 'error', onErrorImg);
                    img.eventOnErrorAdded = false;
                } else {
                    if (!img.eventOnErrorAdded) {
                        addEvent(img, 'error', onErrorImg);
                        img.eventOnErrorAdded = true;
                    }
                }
            }

            if (img.tagName.toLowerCase() === 'div') {
                var url = 'url(' + src + ')';
                img.style.backgroundImage = url;
                img.style.backgroundPosition = 'center center';
                img.style.backgroundRepeat = 'no-repeat';
            } else {
                if (srcset) {
                    img.srcset = srcset;
                }
                img.src = src;
            }
        }

        /**
         * Tests whether the given string starts with the specified prefix 
         * or not.
         * 
         * @param {String} str String to test.
         * @param {String} prefix The prefix.
         * 
         * @returns {Boolean} <code>true</code> if the given string starts
         *   with the given prefix.
         */
        function startsWith(str, prefix) {
            return str.length > 0 && str.slice(0, 1) === prefix;
        }

        function endsWith(str, suffix) {
            return str.indexOf(suffix, str.length - suffix.length) !== -1;
        }

        /**
         * Calculates the browser viewport width in px.
         * @returns {Number} Viewport width.
         */
        function viewportW() {
            return document.documentElement.clientWidth >= 0 ? document.documentElement.clientWidth :
                    (document.body && document.body.clientWidth >= 0) ? document.body.clientWidth :
                    window.innerWidth >= 0 ? window.innerWidth : 0;
        }

        /**
         * Calculates the browser viewport hight in px.
         * @returns {Number} Viewport height.
         */
        function viewportH() {
            return document.documentElement.clientHeight >= 0 ? document.documentElement.clientHeight :
                    (document.body && document.body.clientHeight >= 0) ? document.body.clientHeight :
                    window.innerHeight >= 0 ? window.innerHeight : 0;
        }

        /**
         * Loop through images array to find to-be-shown images.
         */
        function showImages() {
            var last = imgs.length, current, allImagesDone = true;

            for (current = 0; current < last; current++) {
                var img = imgs[current];
                // if showIfVisible is false, it means we have some waiting images to be
                // shown
                if (img !== null && !showIfVisible(img, current)) {
                    allImagesDone = false;
                }
            }

            if (allImagesDone && pageHasLoaded) {
                unsubscribe();
            }
        }

        function unsubscribe() {
            unsubscribed = true;
            removeEvent(window, 'load', onLoad);
            removeEvent(window, 'resize', onResize);
        }

        function subscribe() {
            unsubscribed = false;
            addEvent(window, 'resize', saveViewportT);
            addEvent(window, 'scroll', showImagesT);
            if (P.reloadOnResize) {
                addEvent(window, 'resize', onResizeReload);
            }
            if (P.reloadOnOrientationChange) {
                addEvent(window, 'orientationchange', reloadIfViewportChanged());
            }
        }

        function overrideGetAttribute() {
            var original = window['HTMLImageElement'].prototype.getAttribute;
            window['HTMLImageElement'].prototype.getAttribute = function (name) {
                // if name !== 'src' our own lazyloader will go through theses 
                // lines because we use getAttribute(lazyAttr)
                return name === 'src' ? (original.call(this, dataSrcAttr) ||
                        original.call(this, name)) : original.call(this, name);
            };
        }

        // https://github.com/jquery/sizzle/blob/3136f48b90e3edc84cbaaa6f6f7734ef03775a07/sizzle.js#L708
        var contains = document.documentElement.compareDocumentPosition ?
                function (a, b) {
                    return !!(a.compareDocumentPosition(b) & 16);
                } :
                document.documentElement.contains ?
                function (a, b) {
                    return a !== b && (a.contains ? a.contains(b) : false);
                } :
                function (a, b) {
                    var b0 = b.parentNode;
                    while ((b === b0)) {
                        if (b === a) {
                            return true;
                        }
                        b0 = b.parentNode;
                    }
                    return false;
                };

        // https://github.com/jquery/jquery/blob/f3515b735e4ee00bb686922b2e1565934da845f8/src/core.js#L610
        // We cannot use Array.prototype.indexOf because it's not always available
        function inArray(elem, array, i) {
            if (array) {
                if (Array.prototype.indexOf) {
                    return Array.prototype.indexOf.call(array, elem, i);
                }

                var len = array.length;
                i = i ? i < 0 ? Math.max(0, len + i) : i : 0;

                for (; i < len; i++) {
                    // Skip accessing in sparse arrays
                    if (i in array && array[i] === elem) {
                        return i;
                    }
                }
            }

            return -1;
        }

        // https://github.com/baloneysandwiches/parse-srcset/blob/master/parse-srcset.js
        function parseSrcset(input) {
            var inputLength = input.length;

            // (Don't use \s, to avoid matching non-breaking space)
            var regexLeadingSpaces = /^[ \t\n\r\u000c]+/;
            var regexLeadingCommasOrSpaces = /^[, \t\n\r\u000c]+/;
            var regexLeadingNotSpaces = /^[^ \t\n\r\u000c]+/;
            var regexTrailingCommas = /[,]+$/;
            var regexInteger = /^[-+]?\d+$/;

            // ( Positive or negative or unsigned integers or decimals, without or without exponents.
            // Must include at least one digit.
            // According to spec tests any decimal point must be followed by a digit.
            // No leading plus sign is allowed.)
            // https://html.spec.whatwg.org/multipage/infrastructure.html#valid-floating-point-number 
            var regexFloatingPoint = /^-?(?:[0-9]+|[0-9]*\.[0-9]+)(?:[eE][+-]?[0-9]+)?$/;

            var url;
            var descriptors;
            var currentDescriptor;
            var state;
            var c;

            // 2. Let position be a pointer into input, initially pointing at the start
            //    of the string.
            var pos = 0;

            // 3. Let candidates be an initially empty source set.
            var candidates = [];

            // SOME UTILITY FUNCTIONS

            // Manual is faster than RegEx
            // http://bjorn.tipling.com/state-and-regular-expressions-in-javascript
            // http://jsperf.com/whitespace-character/5
            function isSpace(c) {
                return (c === '\u0020' || // space
                        c === '\u0009' || // horizontal tab
                        c === '\u000A' || // new line
                        c === '\u000C' || // form feed
                        c === '\u000D');  // carriage return
            }

            function collectCharacters(regEx) {
                var chars;
                var match = regEx.exec(input.substring(pos));
                if (match) {
                    chars = match[0];
                    pos += chars.length;
                    return chars;
                }
            }

            // 4. Splitting loop: Collect a sequence of characters that are space
            //    characters or U+002C COMMA characters. If any U+002C COMMA characters
            //    were collected, that is a parse error.	
            while (true) {
                collectCharacters(regexLeadingCommasOrSpaces);

                // 5. If position is past the end of input, return candidates and abort these steps.
                if (pos >= inputLength) {
                    return candidates; // (we're done, this is the sole return path)
                }

                // 6. Collect a sequence of characters that are not space characters,
                //    and let that be url.
                url = collectCharacters(regexLeadingNotSpaces);

                // 7. Let descriptors be a new empty list.
                descriptors = [];

                // 8. If url ends with a U+002C COMMA character (,), follow these substeps:
                //		(1). Remove all trailing U+002C COMMA characters from url. If this removed
                //         more than one character, that is a parse error.
                if (url.slice(-1) === ",") {
                    url = url.replace(regexTrailingCommas, "");
                    // (Jump ahead to step 9 to skip tokenization and just push the candidate).
                    parseDescriptors();

                    //	Otherwise, follow these substeps:
                } else {
                    tokenize();
                } // (close else of step 8)

                // 16. Return to the step labeled splitting loop.
            } // (close of big while loop)		

            /**
             * Tokenizes descriptor properties prior to parsing
             * Returns undefined.
             */
            function tokenize() {

                // 8.1. Descriptor tokeniser: Skip whitespace
                collectCharacters(regexLeadingSpaces);

                // 8.2. Let current descriptor be the empty string.
                currentDescriptor = "";

                // 8.3. Let state be in descriptor.
                state = "in descriptor";

                while (true) {

                    // 8.4. Let c be the character at position.
                    c = input.charAt(pos);

                    //  Do the following depending on the value of state.
                    //  For the purpose of this step, "EOF" is a special character representing
                    //  that position is past the end of input.

                    // In descriptor
                    if (state === "in descriptor") {
                        // Do the following, depending on the value of c:

                        // Space character
                        // If current descriptor is not empty, append current descriptor to
                        // descriptors and let current descriptor be the empty string.
                        // Set state to after descriptor.
                        if (isSpace(c)) {
                            if (currentDescriptor) {
                                descriptors.push(currentDescriptor);
                                currentDescriptor = "";
                                state = "after descriptor";
                            }
                        }

                        // U+002C COMMA (,)
                        // Advance position to the next character in input. If current descriptor
                        // is not empty, append current descriptor to descriptors. Jump to the step
                        // labeled descriptor parser.
                        else if (c === ",") {
                            pos += 1;
                            if (currentDescriptor) {
                                descriptors.push(currentDescriptor);
                            }
                            parseDescriptors();
                            return;
                        }

                        // U+0028 LEFT PARENTHESIS (()
                        // Append c to current descriptor. Set state to in parens.
                        else if (c === '\u0028') {
                            currentDescriptor = currentDescriptor + c;
                            state = "in parens";
                        }

                        // EOF
                        // If current descriptor is not empty, append current descriptor to
                        // descriptors. Jump to the step labeled descriptor parser.
                        else if (c === "") {
                            if (currentDescriptor) {
                                descriptors.push(currentDescriptor);
                            }
                            parseDescriptors();
                            return;
                        }
                        // Anything else
                        // Append c to current descriptor.
                        else {
                            currentDescriptor = currentDescriptor + c;
                        }
                    } // (end "in descriptor"

                    // In parens
                    else if (state === "in parens") {

                        // U+0029 RIGHT PARENTHESIS ())
                        // Append c to current descriptor. Set state to in descriptor.
                        if (c === ")") {
                            currentDescriptor = currentDescriptor + c;
                            state = "in descriptor";

                            // EOF
                            // Append current descriptor to descriptors. Jump to the step labeled
                            // descriptor parser.
                        } else if (c === "") {
                            descriptors.push(currentDescriptor);
                            parseDescriptors();
                            return;

                            // Anything else
                            // Append c to current descriptor.
                        } else {
                            currentDescriptor = currentDescriptor + c;
                        }

                        // After descriptor
                    } else if (state === "after descriptor") {

                        // Do the following, depending on the value of c:
                        // Space character: Stay in this state.
                        if (isSpace(c)) {
                        }

                        // EOF: Jump to the step labeled descriptor parser.
                        else if (c === "") {
                            parseDescriptors();
                            return;
                        }
                        // Anything else
                        // Set state to in descriptor. Set position to the previous character in input.
                        else {
                            state = "in descriptor";
                            pos -= 1;

                        }
                    }

                    // Advance position to the next character in input.
                    pos += 1;

                    // Repeat this step.
                } // (close while true loop)
            }


            /**
             * Adds descriptor properties to a candidate, pushes to the candidates array
             * @return undefined
             */
            // Declared outside of the while loop so that it's only created once.
            function parseDescriptors() {

                // 9. Descriptor parser: Let error be no.
                var pError = false;

                // 10. Let width be absent.			
                // 11. Let density be absent.			
                // 12. Let future-compat-h be absent. (We're implementing it now as h)
                var w, x, h;

                var i;
                var candidate = {};
                var desc, lastChar, value, intVal, floatVal;

                // 13. For each descriptor in descriptors, run the appropriate set of steps
                // from the following list:
                for (i = 0; i < descriptors.length; i++) {
                    desc = descriptors[i];

                    lastChar = desc[desc.length - 1];
                    value = desc.substring(0, desc.length - 1);
                    intVal = parseInt(value, 10);
                    floatVal = parseFloat(value);

                    // If the descriptor consists of a valid non-negative integer followed by
                    // a U+0077 LATIN SMALL LETTER W character
                    if (value.match(regexInteger) && (intVal >= 0) && (lastChar === 'w')) {

                        // If width and density are not both absent, then let error be yes.
                        if (w || x) {
                            pError = true;
                        }

                        // Apply the rules for parsing non-negative integers to the descriptor.
                        // If the result is zero, let error be yes.
                        // Otherwise, let width be the result.
                        if (intVal === 0) {
                            pError = true;
                        } else {
                            w = intVal;
                        }
                    }

                    // If the descriptor consists of a valid floating-point number followed by
                    // a U+0078 LATIN SMALL LETTER X character
                    else if (value.match(regexFloatingPoint) && (lastChar === 'x')) {

                        // If width, density and future-compat-h are not all absent, then let error
                        // be yes.				
                        if (w || x || h) {
                            pError = true;
                        }

                        // Apply the rules for parsing floating-point number values to the descriptor.
                        // If the result is less than zero, let error be yes. Otherwise, let density
                        // be the result.
                        if (floatVal < 0) {
                            pError = true;
                        } else {
                            x = floatVal;
                        }
                    }

                    // If the descriptor consists of a valid non-negative integer followed by
                    // a U+0068 LATIN SMALL LETTER H character
                    else if (value.match(regexInteger) && (intVal >= 0) && (lastChar === "h")) {

                        // If height and density are not both absent, then let error be yes.
                        if (h || x) {
                            pError = true;
                        }

                        // Apply the rules for parsing non-negative integers to the descriptor.
                        // If the result is zero, let error be yes. Otherwise, let future-compat-h
                        // be the result.
                        if (intVal === 0) {
                            pError = true;
                        } else {
                            h = intVal;
                        }
                    }

                    // Anything else, Let error be yes.
                    else {
                        pError = true;
                    }
                } // (close step 13 for loop)

                // 15. If error is still no, then append a new image source to candidates whose
                // URL is url, associated with a width width if not absent and a pixel
                // density density if not absent. Otherwise, there is a parse error.
                if (!pError) {
                    candidate.url = url;
                    if (w) {
                        candidate.w = w;
                    }
                    if (x) {
                        candidate.x = x;
                    }
                    if (h) {
                        candidate.h = h;
                    }
                    candidates.push(candidate);
                }
            } // (close parseDescriptors fn)

        }

    })(this, document);

}