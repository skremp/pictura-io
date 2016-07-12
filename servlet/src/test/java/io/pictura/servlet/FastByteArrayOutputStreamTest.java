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

import java.io.ByteArrayOutputStream;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class FastByteArrayOutputStreamTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_IllegalArgumentException() throws Exception {
        new FastByteArrayOutputStream(-1);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void testWrite_IndexOutOfBoundsException() {
        FastByteArrayOutputStream fos = new FastByteArrayOutputStream(10);
        fos.write(new byte[5], 1, 5);
    }
    
    @Test
    public void testWriteTo() throws Exception {                
        FastByteArrayOutputStream fos = new FastByteArrayOutputStream(10);
        
        fos.write(0x00);
        fos.write(0x01);
        fos.write(0x02);
        fos.write(0x03);
        fos.write(0x04);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        fos.writeTo(bos);
        
        byte[] b = bos.toByteArray();
        assertEquals(5, b.length);
        assertEquals(0x00, b[0]);
        assertEquals(0x01, b[1]);
        assertEquals(0x02, b[2]);
        assertEquals(0x03, b[3]);
        assertEquals(0x04, b[4]);
        
        assertEquals(5, fos.count);
        assertEquals(5, fos.size());
        fos.reset();
        assertEquals(0, fos.count);
        assertEquals(0, fos.size());
        
        for (int i=0; i<12; i++) {
            fos.write(0x07);
        }
        
        b = fos.toByteArray();
        
        assertEquals(12, fos.count);
        assertEquals(12, fos.size());
        assertEquals(0x07, b[fos.count - 1]);        
    }
    
}
