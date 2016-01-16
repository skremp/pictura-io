/**
 * Copyright 2016 Steffen Kremp
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

import java.io.InputStream;

/**
 * A fast <code>ByteArrayInputStream</code> without synchronization.
 * 
 * @author Steffen Kremp
 *
 * @see FastByteArrayOutputStream
 * 
 * @since 1.1
 */
final class FastByteArrayInputStream extends InputStream {
    
    byte buf[];
    
    int pos;
    int mark;
    int count;

    public FastByteArrayInputStream(byte buf[]) {
        this.buf = buf;
        this.pos = 0;
        this.mark = 0;
        this.count = buf.length;
    }
    
    public FastByteArrayInputStream(byte buf[], int off, int len) {
        this.buf = buf;
        this.pos = off;
        this.mark = off;
        this.count = len;
    }
    
    public FastByteArrayInputStream(FastByteArrayOutputStream os) {
        this.buf = os.buf;
        this.pos = 0;
        this.mark = 0;
        this.count = os.count;
    }

    @Override
    public int read() {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }
    
    @Override
    public int read(byte b[], int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        if (pos >= count) {
            return -1;
        }

        int avail = count - pos;
        if (len > avail) {
            len = avail;
        }
        if (len <= 0) {
            return 0;
        }
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    @Override
    public long skip(long n) {
        long k = count - pos;
        if (n < k) {
            k = n < 0 ? 0 : n;
        }
        pos += k;
        return k;
    }

    @Override
    public int available() {
        return count - pos;
    }

    @Override
    public void reset() {
        pos = mark;
    }
    
}
