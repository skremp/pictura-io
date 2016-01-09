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

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;

/**
 * Context related servlet output stream to track the number of written bytes.
 *
 * @see ServletOutputStream
 * @see ContextInputStream
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
final class ContextOutputStream extends ServletOutputStream {

    private final ServletRequest ctx;
    private final ServletOutputStream os;

    private long bytesWritten;

    ContextOutputStream(ServletRequest ctx, ServletOutputStream os) {
	this.ctx = ctx;
	this.os = os;
    }

    @Override
    public void write(int b) throws IOException {
	write0(b, -1);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
	if (b == null) {
	    throw new NullPointerException();
	} else if ((off < 0) || (off > b.length) || (len < 0)
		|| ((off + len) > b.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return;
	}
	for (int i = 0; i < len; i++) {
	    write0(b[off + i], len);
	}
    }

    private void write0(int b, int len) throws IOException {
	bytesWritten++;
	os.write(b);

	if (len > 0 && bytesWritten == len) {
	    updateContextStats();
	}
    }

    @Override
    public void close() throws IOException {
	updateContextStats();
	super.close();
    }

    private void updateContextStats() {
	if (ctx != null && bytesWritten > 0) {
	    Object objBytesRead = ctx.getAttribute("io.pictura.servlet.BYTES_WRITTEN");
	    if (objBytesRead instanceof Long) {
		long l = (long) objBytesRead;
		ctx.setAttribute("io.pictura.servlet.BYTES_WRITTEN", (bytesWritten + l));
	    } else {
		ctx.setAttribute("io.pictura.servlet.BYTES_WRITTEN", bytesWritten);
	    }
	    bytesWritten = 0L;
	}
    }

}
