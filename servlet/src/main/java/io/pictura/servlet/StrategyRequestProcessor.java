/**
 * Copyright 2015 Steffen Kremp
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.pictura.servlet;

import javax.servlet.http.HttpServletRequest;

/**
 * Basic image request strategy.
 * 
 * @see ImageRequestProcessor
 * @see ImageRequestStrategy
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
abstract class StrategyRequestProcessor extends ImageRequestProcessor
	implements ImageRequestStrategy {
    
    protected ImageRequestProcessor getBaseRequestProcessor(HttpServletRequest req) {
	ImageRequestProcessor srp;
	if (!(req.getAttribute("io.pictura.servlet.BASE_REQUEST_PROCESSOR") instanceof ImageRequestProcessor)) {
	    srp = new ImageRequestProcessor();
	    req.setAttribute("io.pictura.servlet.BASE_REQUEST_PROCESSOR", srp);
	} else {
	    srp = (ImageRequestProcessor) req.getAttribute("io.pictura.servlet.BASE_REQUEST_PROCESSOR");
	}
	return srp;
    }

    @Override
    protected void runFinalize() {	
	super.runFinalize();
	getRequest().removeAttribute("io.pictura.servlet.BASE_REQUEST_PROCESSOR");
    }        
    
}
