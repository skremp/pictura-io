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
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author Steffen Kremp
 */
public class XMLCacheControlHandlerTest {

    @Test
    public void testGetDirective() throws Exception {
	System.out.println("getDirective");

	URL testFile = XMLCacheControlHandlerTest.class.getResource("/pictura-cache-control-test.xml");
	CacheControlHandler handler = new XMLCacheControlHandler(new File(testFile.toURI()).getAbsolutePath());

	assertEquals("public, max-age=60", handler.getDirective("/foobar"));

	assertEquals("private, no-cache", handler.getDirective("/private/myImage.jpg"));

	assertEquals("public, max-age=1200", handler.getDirective("/public/id-1/myImage.jpg"));
	assertEquals("public, max-age=1200", handler.getDirective("/public/id-12/myImage.jpg"));
	assertEquals("public, max-age=1200", handler.getDirective("/public/id-123/myImage.jpg"));
	assertEquals("public, max-age=1200", handler.getDirective("/public/id-1234/myImage.jpg"));

	assertEquals("public, max-age=60", handler.getDirective("/public/id-12345/myImage.jpg"));
	assertEquals("public, max-age=1200", handler.getDirective("/public/id-1234/id-1/myImage.jpg"));
    }

    @Test
    public void testDestroy() throws Exception {
	System.out.println("destroy");

	URL testFile = XMLCacheControlHandlerTest.class.getResource("/pictura-cache-control-test.xml");
	XMLCacheControlHandler handler = new XMLCacheControlHandler(new File(testFile.toURI()).getAbsolutePath());

	assertEquals("public, max-age=60", handler.getDirective("/foobar"));

	handler.destroy();

	assertNull(handler.getDirective("/foobar"));
    }

    @Test(expected = IOException.class)
    public void testWrongPattern() throws Exception {
	// Create temp. test file
	String xml = "<cache-control><rule><path>{/x</path><directive>no-cache</directive></rule></cache-control>";

	File tmp = File.createTempFile("cache-control", ".xml");
	tmp.deleteOnExit();

	try (OutputStream os = new FileOutputStream(tmp)) {
	    os.write(xml.getBytes());
	    os.flush();
	}

	XMLCacheControlHandler handler = new XMLCacheControlHandler(tmp.getAbsolutePath());
    }

    @Test(expected = IOException.class)
    public void testIllegalXml() throws Exception {
	// Create temp. test file
	String xml = "<cache-control><rule><path>/*</path><directive>no-cache</directive></ru--le></cache-control>";

	File tmp = File.createTempFile("cache-control", ".xml");
	tmp.deleteOnExit();

	try (OutputStream os = new FileOutputStream(tmp)) {
	    os.write(xml.getBytes());
	    os.flush();
	}

	XMLCacheControlHandler handler = new XMLCacheControlHandler(tmp.getAbsolutePath());
    }

    @Test
    public void testNullPath() throws Exception {
	// Create temp. test file
	String xml = "<cache-control><rule><path></path><directive>no-cache</directive></rule></cache-control>";

	File tmp = File.createTempFile("cache-control", ".xml");
	tmp.deleteOnExit();

	try (OutputStream os = new FileOutputStream(tmp)) {
	    os.write(xml.getBytes());
	    os.flush();
	}

	XMLCacheControlHandler handler = new XMLCacheControlHandler(tmp.getAbsolutePath());
	assertEquals("no-cache", handler.getDirective("/public/id-1/myImage.jpg"));
	assertEquals("no-cache", handler.getDirective("public/id-1/myImage.jpg"));

	handler.destroy();
	tmp.delete();
    }

    @Test
    public void testConfigFileChange() throws Exception {
	String xml = "<cache-control><rule><path></path><directive>no-cache</directive></rule></cache-control>";

	String currentWorkingDir = System.getProperty("user.dir");
	File tmp = new File(currentWorkingDir + File.separator + "target/tmp/cache-control.xml");

	tmp.deleteOnExit();
	tmp.getParentFile().mkdirs();
	tmp.createNewFile();

	assertTrue(tmp.exists());

	try (OutputStream os = new FileOutputStream(tmp)) {
	    os.write(xml.getBytes());
	    os.flush();
	}
        System.out.println("last modified cache control: " + tmp.lastModified());

	XMLCacheControlHandler handler = new XMLCacheControlHandler(tmp.getAbsolutePath());
	assertEquals("no-cache", handler.getDirective("/public/id-1/myImage.jpg"));
	assertEquals("no-cache", handler.getDirective("public/id-1/myImage.jpg"));

        TimeUnit.SECONDS.sleep(1);
        
	xml = "<cache-control><rule><path></path><directive>max-age=20</directive></rule></cache-control>";

	try (OutputStream os = new FileOutputStream(tmp)) {
	    os.write(xml.getBytes());
	    os.flush();
	}

        System.out.println("last modified cache control: " + tmp.lastModified());
        
	// automaically fail after 30 seconds
	for (int i = 0; i < 30; i++) {
	    String directive = handler.getDirective("/public/id-1/myImage.jpg");
	    if (!directive.equals("no-cache")) {
		break;
	    }
	    TimeUnit.SECONDS.sleep(1);
	}

	assertEquals("max-age=20", handler.getDirective("/public/id-1/myImage.jpg"));
	assertEquals("max-age=20", handler.getDirective("public/id-1/myImage.jpg"));

	handler.destroy();
	tmp.delete();
    }

}
