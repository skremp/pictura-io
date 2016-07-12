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

import io.pictura.servlet.PicturaServlet.InitParam;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.servlet.Servlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Pictura servlet configuration (external configuration via XML file).
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public final class PicturaConfig {

    /**
     * Specifies a new configuration parameter.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({FIELD})
    @Documented
    public @interface ConfigParam {

        /**
         * @return XPath to the configuration value.
         */
        String xpath();

        /**
         * @return <code>true</code> if repeated configuration value (1..n);
         * otherwise <code>false</code>.
         */
        boolean repeated() default false;
    }

    private final Servlet servlet;
    private final String filename;

    private Map<String, String> paramMap;

    /**
     * Constructs a new configuration instance for the specified servlet and the
     * given external configuration file.
     *
     * @param servlet Servlet for which the configuration shall be read.
     * @param filename The external configuration file.
     *
     * @throws IOException if something goes wrong while reading and parsing the
     * external configuration file.
     */
    PicturaConfig(Servlet servlet, String filename) throws IOException {
        if (servlet == null) {
            throw new IllegalArgumentException("Servlet must be not null");
        }
        this.servlet = servlet;

        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Config filename must be not null nor empty");
        }
        this.filename = filename;

        if (filename.toLowerCase(Locale.ENGLISH).endsWith(".properties")) {
            readPropertiesConfig();
        } else if (filename.toLowerCase(Locale.ENGLISH).endsWith(".xml")) {
            readXmlConfig();
        } else {
            throw new IOException("Unsupported Pictura configuration file format");
        }
    }

    /**
     * Gets the configuration parameter value for the equivalent servlet init
     * parameter name ({@link InitParam}).
     *
     * @param paramName The configuration parameter name.
     *
     * @return The associated parameter value or <code>null</code> if the
     * parameter is not present.
     */
    public String getConfigParam(String paramName) {
        return (paramMap != null && paramMap.get(paramName) != null)
                ? paramMap.get(paramName) : null;
    }

    /**
     * Gets a set of parameter names by this instance.
     * 
     * @return A set of all available parameter names listed by this
     * configuration.
     *
     * @since 1.3
     */
    public Set<String> getConfigParamNames() {
        return paramMap != null ? paramMap.keySet() : Collections.<String>emptySet();
    }

    private void readXmlConfig() throws IOException {
        try (InputStream is = new FileInputStream(filename)) {
            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
            Document xmlDocument = xmlBuilder.parse(is);

            XPathFactory xpFactory = XPathFactory.newInstance();
            XPath xp = xpFactory.newXPath();

            HashMap<String, String> map = new HashMap<>();

            Field[] psf = getDeclaredFields(servlet.getClass(), true);
            for (Field f : psf) {
                InitParam ip = f.getAnnotation(InitParam.class);
                if (ip != null) {
                    ConfigParam cp = f.getAnnotation(ConfigParam.class);
                    if (cp != null && cp.xpath() != null && !cp.xpath().isEmpty()) {

                        Object val = xp.evaluate(cp.xpath(), xmlDocument,
                                cp.repeated() ? XPathConstants.NODESET : XPathConstants.NODE);

                        String tVal = null;

                        if (val instanceof Node) {
                            tVal = ((Node) val).getTextContent().trim();
                        } else if (val instanceof NodeList) {
                            StringBuilder sb = new StringBuilder();
                            String sep = "";

                            NodeList nl = (NodeList) val;
                            for (int i = 0; i < nl.getLength(); i++) {
                                sb.append(sep);
                                sb.append(nl.item(i).getTextContent().trim());
                                sep = ",";
                            }

                            tVal = sb.toString();
                        }

                        Object initParamName = f.get(null);
                        if (initParamName instanceof String) {
                            map.put((String) initParamName, tVal);
                        }
                    }
                }
            }

            paramMap = Collections.unmodifiableMap(map);
        } catch (ParserConfigurationException | SAXException |
                XPathExpressionException | IllegalAccessException ex) {
            throw new IOException(ex);
        }
    }

    private void readPropertiesConfig() throws IOException {
        try (InputStream is = new FileInputStream(filename)) {
            final Properties props = new Properties();
            props.load(is);

            final HashMap<String, String> map = new HashMap<>();

            Field[] psf = getDeclaredFields(servlet.getClass(), true);

            for (final Field f : psf) {
                InitParam ip = f.getAnnotation(InitParam.class);
                if (ip != null) {

                    AccessController.doPrivileged(new PrivilegedAction<Object>() {

                        @Override
                        public Object run() {
                            try {
                                String shortName = (String) f.get(servlet);
                                String val = props.getProperty("io.pictura.servlet." + shortName);
                                if (val != null) {
                                    map.put(shortName, val);
                                }
                            } catch (IllegalAccessException | IllegalArgumentException ex) {
                                // TODO
                            }
                            return null;
                        }
                    });

                }
            }

            paramMap = Collections.unmodifiableMap(map);
        }
    }

    /**
     * Retrieving fields list of specified class. If recursively is true,
     * retrieving fields from all class hierarchy
     *
     * @param clazz where fields are searching
     * @param recursively param
     * @return list of fields
     */
    private static Field[] getDeclaredFields(Class<?> clazz, boolean recursively) {
        List<Field> fields = new LinkedList<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        Collections.addAll(fields, declaredFields);

        Class<?> superClass = clazz.getSuperclass();

        if (superClass != null && recursively) {
            Field[] declaredFieldsOfSuper = getDeclaredFields(superClass, recursively);
            if (declaredFieldsOfSuper.length > 0) {
                Collections.addAll(fields, declaredFieldsOfSuper);
            }
        }

        return fields.toArray(new Field[fields.size()]);
    }

}
