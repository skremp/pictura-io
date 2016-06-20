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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * I18N helper class.
 * 
 * @author Steffen Kremp
 *
 * @since 1.3
 */
final class I18N {

    private static final Log LOG = Log.getLog(I18N.class);
    
    private static final String BASE_NAME = "io.pictura.servlet.i18n";

    /**
     * Gets a string for the given key.
     * 
     * @param key The key for the desired string.
     *
     * @return The string for the given key.
     */
    public static String getString(String key) {
        return getString(key, Locale.getDefault());
    }

    /**
     * Gets an formatted string for the given key.
     * 
     * @param key The key for the desired string.
     * @param arguments The arguments to format the pattern.
     *
     * @return The string for the given key.
     */
    public static String getString(String key, Object... arguments) {
        return getString(key, Locale.getDefault(), arguments);
    }
    
    /**
     * Gets a string for the given key.
     * 
     * @param key The key for the desired string.
     * @param locale The locale for which the key is desired.
     *
     * @return The string for the given key.
     */
    public static String getString(String key, Locale locale) {
        // Resource bundle is already cached by the JDK implementation
        String value = "?";
        if (key != null && !key.isEmpty()) {
            try {
                value = locale != null 
                        ? ResourceBundle.getBundle(BASE_NAME, locale).getString(key) 
                        : ResourceBundle.getBundle(BASE_NAME).getString(key);
            } catch (MissingResourceException ex) {
                LOG.warn("Missing I18N resource for key \"" + key + "\"");
            } catch (NullPointerException ex) {
                LOG.error("NullPointerException for I18N resource", ex);
            }
        } else {
            LOG.warn("I18N resource for empty key requested");
        }
        return value;
    }

    /**
     * Gets an formatted string for the given key.
     * 
     * @param key The key for the desired string.
     * @param locale The locale for which the key is desired.
     * @param arguments The arguments to format the pattern.
     *
     * @return The string for the given key.
     */
    public static String getString(String key, Locale locale, Object... arguments) {
        try {
            return MessageFormat.format(getString(key, locale), arguments);
        } catch (IllegalArgumentException ex) {
            LOG.warn("Unable for format I18N resource for key \"" + key + "\"", ex);
        }
        return "?";
    }
    
    // Prevent instantiation
    private I18N() {
    }

}
