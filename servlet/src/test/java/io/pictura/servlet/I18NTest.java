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

import java.util.Locale;
import java.util.Properties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class I18NTest {

    @Test
    public void testGetString() throws Exception {
        Properties p = new Properties();
        p.load(I18N.class.getResourceAsStream("/io/pictura/servlet/i18n.properties"));
        for (Object key : p.keySet()) {
            assertEquals(p.getProperty(key.toString()), I18N.getString(key.toString()));
            assertFalse(I18N.getString(key.toString()).isEmpty());
        }
        
        assertEquals("Bad Request", I18N.getString("http.400", 123, "test", true));
        
        assertEquals("?", I18N.getString("foobar"));
        assertEquals("?", I18N.getString("foobar", 123));
        assertEquals("?", I18N.getString(""));
        assertEquals("?", I18N.getString(null));
        assertEquals("?", I18N.getString("foobar", Locale.ENGLISH));
        assertEquals("?", I18N.getString("foobar", Locale.GERMAN));
        assertEquals("?", I18N.getString("foobar", Locale.ENGLISH, "123"));
    }    
    
}
