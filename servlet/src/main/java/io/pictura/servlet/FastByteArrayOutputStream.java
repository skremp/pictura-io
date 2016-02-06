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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * A fast <code>ByteArrayOutputStream</code> with direct access to the stored
 * data and without any synchonization.
 *
 * @author Steffen Kremp
 *
 * @see FastByteArrayInputStream
 *
 * @since 1.1
 */
final class FastByteArrayOutputStream extends OutputStream {

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private static int DEFAULT_BUFFER_SIZE ;

    static {
        final long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory < 64 * 1024 * 1024) {
            DEFAULT_BUFFER_SIZE = 1024 * 4;
        } else if (maxMemory < 128 * 1024 * 1024) {
            DEFAULT_BUFFER_SIZE = 1024 * 8;
        } else if (maxMemory < 512 * 1024 * 1024) {
            DEFAULT_BUFFER_SIZE = 1024 * 16;
        } else if (maxMemory < 1024 * 1024 * 1024) {
            DEFAULT_BUFFER_SIZE = 1024 * 64;
        } else {
            DEFAULT_BUFFER_SIZE = 1024 * 128;
        }
    }

    byte buf[];
    int count;

    public FastByteArrayOutputStream() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public FastByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        buf = new byte[size];
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity - buf.length > 0) {
            int oldCap = buf.length;
            int newCap = oldCap << 1;
            if (newCap - minCapacity < 0) {
                newCap = minCapacity;
            }
            if (newCap - MAX_ARRAY_SIZE > 0) {
                if (minCapacity < 0) { // overflow
                    throw new OutOfMemoryError();
                }
                newCap = (minCapacity > MAX_ARRAY_SIZE)
                        ? Integer.MAX_VALUE
                        : MAX_ARRAY_SIZE;
            }
            buf = Arrays.copyOf(buf, newCap);
        }
    }

    @Override
    public void write(int b) {
        ensureCapacity(count + 1);
        buf[count] = (byte) b;
        count += 1;
    }

    @Override
    public void write(byte b[], int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0)
                || ((off + len) - b.length > 0)) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(count + len);
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    public void reset() {
        count = 0;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(buf, count);
    }

    public int size() {
        return count;
    }

}
