/*! 
 * PicturaIO 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

/**
 * Copyright 2015 Steffen Kremp
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

if (!window['__PicturaIO_Cookie__']) {

    // Prevent double script running on same page. Also note, we NEED to use 
    // string as closure compiler would otherwise compile this statement badly.

    "use strict";

    (function (window, document) {

        /**
         * The current PicturaIO client side Cookie JavaScript implementation version.
         */
        PicturaIO.Cookie.version = "${script.version}";

        var con = '__picturaio__',
            pfx = ';expires=0;path=/',
            val = 'dpr=' + (window.devicePixelRatio || 1) +
                  ',dw=' + Math.max(document.documentElement.clientWidth, window.innerWidth || 0) +
                  ',dh=' + Math.max(document.documentElement.clientHeight, window.innerHeight || 0);

        document.cookie = con + '=' + val + pfx;

        function canReadImage(fmt, fn) {
            var img = new Image();
            img.onload = function () {
                fn();
            };
            img.src = fmt;
        }

        var fmtList = {
            jp2: 'data:image/jp2;base64,/0//UQAyAAAAAAABAAAAAgAAAAAAAAAAAAAABAAAAAQAAAAAAAAAAAAEBwEBBwEBBwEBBwEB/1IADAAAAAEAAAQEAAH/XAAEQED/ZAAlAAFDcmVhdGVkIGJ5IE9wZW5KUEVHIHZlcnNpb24gMi4wLjD/kAAKAAAAAABYAAH/UwAJAQAABAQAAf9dAAUBQED/UwAJAgAABAQAAf9dAAUCQED/UwAJAwAABAQAAf9dAAUDQED/k8+kEAGvz6QQAa/PpBABr994EAk//9k=',
            webp: 'data:image/webp;base64,UklGRhoAAABXRUJQVlA4TA0AAAAvAAAAEAcQERGIiP4HAA=='
        };

        canReadImage(fmtList.jp2, function () {
            val += ',jp2=1';
            document.cookie = con + '=' + val + pfx;
        });
        canReadImage(fmtList.webp, function () {
            val += ',webp=1';
            document.cookie = con + '=' + val + pfx;
        });

        window['__PicturaIO_Cookie__'] = val;

    })(this, document);

}