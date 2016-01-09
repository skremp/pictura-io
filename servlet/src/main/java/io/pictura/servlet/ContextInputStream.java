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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletRequest;

/**
 * Context related buffered input stream to track the number of bytes read.
 *
 * @see BufferedInputStream
 * @see ContextOutputStream
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
final class ContextInputStream extends BufferedInputStream {

    private final ServletRequest ctx;
    private long bytesRead;

    ContextInputStream(ServletRequest ctx, InputStream in) {
	super(in);
	this.ctx = ctx;
    }   

    @Override
    public synchronized int read() throws IOException {
	int b = super.read();	
	if (b > -1) {
	    bytesRead++;
	} else {
	    updateContext();
	}
	return b;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
	int n = super.read(b, off, len);
	if (n > -1) {
	    bytesRead += n;	    
	} else {
	    updateContext();
	}
	return n;
    }    
    
    private void updateContext() {
	if (ctx != null && bytesRead > 0) {
	    Object objBytesRead = ctx.getAttribute("io.pictura.servlet.BYTES_READ");
	    if (objBytesRead instanceof Long) {
		long l = (long) objBytesRead;
		ctx.setAttribute("io.pictura.servlet.BYTES_READ", (bytesRead + l));
	    } else {
		ctx.setAttribute("io.pictura.servlet.BYTES_READ", bytesRead);
	    }
	}
	bytesRead = 0;
    }
    
}
