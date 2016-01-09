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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class PicturaConfigTest {

    @Test
    public void testGetPropertiesConfigParam() throws Exception {
	URL testFile = PicturaConfigTest.class.getResource("/pictura-config-test.properties");
	PicturaConfig pc = new PicturaConfig(new PicturaServlet(), new File(testFile.toURI()).getAbsolutePath());
	
	assertEquals(null, pc.getConfigParam("foobar"));
	
	assertEquals("true", pc.getConfigParam(PicturaServlet.IPARAM_DEBUG));
	assertEquals("/javascript/lib", pc.getConfigParam(PicturaServlet.IPARAM_SCRIPT_PATH));
    }
    
    @Test
    public void testGetXmlConfigParam() throws Exception {
	URL testFile = PicturaConfigTest.class.getResource("/pictura-config-test.xml");
	PicturaConfig pc = new PicturaConfig(new PicturaServlet(), new File(testFile.toURI()).getAbsolutePath());

	assertEquals(null, pc.getConfigParam("foobar"));

	assertEquals("false", pc.getConfigParam(PicturaServlet.IPARAM_DEBUG));
	assertEquals("true", pc.getConfigParam(PicturaServlet.IPARAM_JMX_ENABLED));
	assertEquals("2", pc.getConfigParam(PicturaServlet.IPARAM_CORE_POOL_SIZE));
	assertEquals("4", pc.getConfigParam(PicturaServlet.IPARAM_MAX_POOL_SIZE));
	assertEquals("25500", pc.getConfigParam(PicturaServlet.IPARAM_KEEP_ALIVE_TIME));
	assertEquals("100", pc.getConfigParam(PicturaServlet.IPARAM_WORKER_QUEUE_SIZE));
	assertEquals("15000", pc.getConfigParam(PicturaServlet.IPARAM_WORKER_TIMEOUT));
	assertEquals("/images/*,http://www.google.de/*", pc.getConfigParam(PicturaServlet.IPARAM_RESOURCE_PATHS));
	assertEquals("io.pictura.servlet.FileResourceLocator,io.pictura.servlet.HttpResourceLocator", pc.getConfigParam(PicturaServlet.IPARAM_RESOURCE_LOCATORS));
	assertEquals("io.pictura.servlet.ImageRequestProcessor", pc.getConfigParam(PicturaServlet.IPARAM_REQUEST_PROCESSOR));
	assertEquals("io.pictura.servlet.ImageRequestProcessorFactory", pc.getConfigParam(PicturaServlet.IPARAM_REQUEST_PROCESSOR_FACTORY));
	assertEquals("io.pictura.servlet.BotRequestProcessor,io.pictura.servlet.WebPRequestProcessor", pc.getConfigParam(PicturaServlet.IPARAM_REQUEST_PROCESSOR_STRATEGY));
	assertEquals("2000000", pc.getConfigParam(PicturaServlet.IPARAM_MAX_IMAGE_FILE_SIZE));
	assertEquals("30000000", pc.getConfigParam(PicturaServlet.IPARAM_MAX_IMAGE_RESOLUTION));
	assertEquals("3", pc.getConfigParam(PicturaServlet.IPARAM_MAX_IMAGE_EFFECTS));
	assertEquals("jpg,gif,png", pc.getConfigParam(PicturaServlet.IPARAM_ENABLED_INPUT_IMAGE_FORMATS));
	assertEquals("jpg,webp", pc.getConfigParam(PicturaServlet.IPARAM_ENABLED_OUTPUT_IMAGE_FORMATS));
	assertEquals("true", pc.getConfigParam(PicturaServlet.IPARAM_ENABLE_BASE64_IMAGE_ENCODING));
	assertEquals("Pictura-Image-Processor", pc.getConfigParam(PicturaServlet.IPARAM_HTTP_AGENT));
	assertEquals("3500", pc.getConfigParam(PicturaServlet.IPARAM_HTTP_CONNECT_TIMEOUT));
	assertEquals("6000", pc.getConfigParam(PicturaServlet.IPARAM_HTTP_READ_TIMEOUT));
	assertEquals("false", pc.getConfigParam(PicturaServlet.IPARAM_HTTP_FOLLOW_REDIRECTS));
	assertEquals("0", pc.getConfigParam(PicturaServlet.IPARAM_HTTP_MAX_FORWARDS));
	assertEquals("true", pc.getConfigParam(PicturaServlet.IPARAM_HTTPS_DISABLE_CERTIFICATE_VALIDATION));
	assertEquals("localhost", pc.getConfigParam(PicturaServlet.IPARAM_HTTP_PROXY_HOST));
	assertEquals("7775", pc.getConfigParam(PicturaServlet.IPARAM_HTTP_PROXY_PORT));
	assertEquals("true", pc.getConfigParam(PicturaServlet.IPARAM_STATS_ENABLED));
	assertEquals("myStats", pc.getConfigParam(PicturaServlet.IPARAM_STATS_PATH));
	assertEquals("localhost,127.0.0.1/12", pc.getConfigParam(PicturaServlet.IPARAM_STATS_IP_ADDRESS_MATCH));
	assertEquals("false", pc.getConfigParam(PicturaServlet.IPARAM_SCRIPT_ENABLED));
	assertEquals("/javascript", pc.getConfigParam(PicturaServlet.IPARAM_SCRIPT_PATH));
	assertEquals("true", pc.getConfigParam(PicturaServlet.IPARAM_PLACEHOLDER_PRODUCER_ENABLED));
	assertEquals("/pp", pc.getConfigParam(PicturaServlet.IPARAM_PLACEHOLDER_PRODUCER_PATH));
	assertEquals("true", pc.getConfigParam(PicturaServlet.IPARAM_ENABLE_QUERY_PARAMS));
	assertEquals("true", pc.getConfigParam(PicturaServlet.IPARAM_ENABLE_CONTENT_DISPOSITION));
	assertEquals("true", pc.getConfigParam(PicturaServlet.IPARAM_IMAGEIO_USE_CACHE));
	assertEquals("/foo", pc.getConfigParam(PicturaServlet.IPARAM_IMAGEIO_CACHE_DIR));
	assertEquals("false", pc.getConfigParam(PicturaServlet.IPARAM_HEADER_ADD_CONTENT_LOCATION));
	assertEquals("true", pc.getConfigParam(PicturaServlet.IPARAM_HEADER_ADD_TRUE_CACHE_KEY));
	assertEquals("true", pc.getConfigParam(PicturaServlet.IPARAM_HEADER_ADD_REQUEST_ID));
	assertEquals("io.pictura.servlet.CacheControlHandler", pc.getConfigParam(PicturaServlet.IPARAM_CACHE_CONTROL_HANDLER));
	assertEquals("7", pc.getConfigParam(PicturaServlet.IPARAM_DEFLATER_COMPRESSION_LEVEL));
	assertEquals("com.mycompany.MyURLConnectionFactory", pc.getConfigParam(PicturaServlet.IPARAM_URL_CONNECTION_FACTORY));
    }

    @Test(expected = IOException.class)
    public void testConstructor_IOException() throws Exception {
	PicturaConfig pc = new PicturaConfig(new PicturaServlet(), "foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_IllegalArgumentException_1() throws Exception {
	URL testFile = PicturaConfigTest.class.getResource("/pictura-config-test.xml");
	PicturaConfig pc = new PicturaConfig(null, new File(testFile.toURI()).getAbsolutePath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_IllegalArgumentException_2() throws Exception {
	PicturaConfig pc = new PicturaConfig(new PicturaServlet(), null);
    }

    @Test(expected = IOException.class)
    public void testParseException() throws Exception {
	String xml = "<pictura><debug>true</debu></pictura>";
	File tmp = File.createTempFile("test", "xml");
	tmp.deleteOnExit();

	try (FileOutputStream fos = new FileOutputStream(tmp)) {
	    fos.write(xml.getBytes());
	    fos.flush();
	} catch (IOException ex) {
	    fail("Can't create temporary test file");
	}

	PicturaConfig pc = new PicturaConfig(new PicturaServlet(), tmp.getAbsolutePath());
    }

}
