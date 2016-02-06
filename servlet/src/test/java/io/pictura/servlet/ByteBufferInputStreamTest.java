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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.FileChannel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class ByteBufferInputStreamTest {

    @Test
    public void testInputStream() throws Exception {
        byte[] exp;
        try (InputStream is = ByteBufferInputStreamTest.class.getResourceAsStream("/lenna.png")) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 8);
            int len;
            byte[] buf = new byte[1024 * 8];
            while ((len = is.read(buf)) > -1) {
                bos.write(buf, 0, len);
            }
            exp = bos.toByteArray();
        }

        assertNotNull(exp);

        URL url = ByteBufferInputStreamTest.class.getResource("/lenna.png");
        File f = new File(url.toURI());

        assertNotNull(f);
        
        byte[] test;
        try (FileChannel ch = new FileInputStream(f).getChannel()) {
            InputStream is = new ByteBufferInputStream(ch.map(FileChannel.MapMode.READ_ONLY, 0, f.length()));

            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 8);
            int len;
            byte[] buf = new byte[1024 * 8];
            while ((len = is.read(buf)) > -1) {
                bos.write(buf, 0, len);
            }
            assertEquals(-1, is.read());
            
            test = bos.toByteArray();            
        }

        assertNotNull(test);
        assertEquals(exp.length, test.length);

        for (int i = 0; i < exp.length; i++) {
            assertTrue(exp[i] == test[i]);
        }
    }

}
