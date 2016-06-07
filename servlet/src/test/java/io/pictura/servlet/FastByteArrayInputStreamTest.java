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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class FastByteArrayInputStreamTest {
    
    @Test
    public void testConstructor() {
        byte[] b = new byte[10];
        
        FastByteArrayInputStream fis = new FastByteArrayInputStream(b);
        
        assertSame(b, fis.buf);        
        assertEquals(0, fis.pos);
        assertEquals(0, fis.mark);
        assertEquals(b.length, fis.count);
                
        assertEquals(10, fis.available());
        
        b[2] = 0x21;
        
        assertEquals(0x00, fis.read());
        assertEquals(9, fis.available());
        assertEquals(0x00, fis.read());
        assertEquals(8, fis.available());
        assertEquals(0x21, fis.read());
        assertEquals(7, fis.available());
        
        fis.skip(2);
        assertEquals(5, fis.available());
        
        fis.reset();
        assertEquals(10, fis.available());
    }
    
    @Test(expected = NullPointerException.class)
    public void testRead_NullPointerException() throws Exception {
        FastByteArrayInputStream fis = new FastByteArrayInputStream(new byte[10]);
        fis.read(null, 0, 3);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void testRead_IndexOutOfBoundsException() throws Exception {
        FastByteArrayInputStream fis = new FastByteArrayInputStream(new byte[10]);
        fis.read(new byte[5], 2, 12);
    }
    
}
