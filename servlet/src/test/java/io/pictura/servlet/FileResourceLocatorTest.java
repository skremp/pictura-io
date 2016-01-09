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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletContext;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Steffen Kremp
 */
public class FileResourceLocatorTest {

    private static String rootPath;

    @BeforeClass
    public static void setUpClass() {
	rootPath = System.getProperty("java.io.tmpdir") + File.separator + "pictura";

	InputStream is = null;
	OutputStream os = null, os2 = null;

	try {
	    File path = new File(rootPath);
	    if (!path.exists()) {
		if (!path.mkdirs()) {
		    fail("Can't create temporary root path \"" + path.getAbsolutePath() + "\".");
		    return;
		}
	    }

	    File tmp = new File(path.getAbsolutePath() + File.separator + "lenna.jpg");
	    if (!tmp.exists()) {
		if (!tmp.createNewFile()) {
		    fail("Can't create temporary resource file \"" + tmp.getAbsolutePath() + "\".");
		    return;
		}
	    }

	    File tmp2 = new File(path.getParentFile().getAbsolutePath() + File.separator + "lenna.jpg");
	    if (!tmp2.exists()) {
		if (!tmp2.createNewFile()) {
		    fail("Can't create temporary resource file \"" + tmp2.getAbsolutePath() + "\".");
		    return;
		}
	    }

	    is = FileResourceLocatorTest.class.getResourceAsStream("/lenna.jpg");
	    os = new FileOutputStream(tmp);
	    os2 = new FileOutputStream(tmp2);

	    int read;
	    byte[] bytes = new byte[1024];

	    while ((read = is.read(bytes)) != -1) {
		os.write(bytes, 0, read);
		os2.write(bytes, 0, read);
	    }

	    tmp.deleteOnExit();
	    tmp2.deleteOnExit();
	    path.deleteOnExit();
	} catch (IOException ex) {
	    fail("Can't create temporary root path and test resource.");
	} finally {
	    if (is != null) {
		try {
		    is.close();
		} catch (IOException ex) {
		}
	    }
	    if (os != null) {
		try {
		    os.close();
		} catch (IOException ex) {
		}
	    }
	    if (os2 != null) {
		try {
		    os2.close();
		} catch (IOException ex) {
		}
	    }
	}
    }

    @Test
    public void testGetResource() throws MalformedURLException {
	System.out.println("getResource");

	ServletContext ctx = mock(ServletContext.class);
	when(ctx.getRealPath("/")).thenReturn(rootPath);
	when(ctx.getRealPath("lenna.jpg")).thenReturn(rootPath + File.separator + "lenna.jpg");

	FileResourceLocator locator0 = new FileResourceLocator(ctx);
	FileResourceLocator locator1 = new FileResourceLocator();
	locator1.setServletContext(ctx);

	assertNull(locator0.getResource(null));
	assertNull(locator1.getResource(null));

	assertNull(locator0.getResource(""));
	assertNull(locator1.getResource(""));

	assertNull(locator0.getResource("foobar.jpg"));
	assertNull(locator1.getResource("foobar.jpg"));

	assertNotNull(locator0.getResource("lenna.jpg"));
	assertNotNull(locator1.getResource("lenna.jpg"));

	URL url;

	url = locator0.getResource("lenna.jpg");
	assertTrue(url.toString().startsWith("file:/"));

	url = locator1.getResource("lenna.jpg");
	assertTrue(url.toString().startsWith("file:/"));

	url = locator0.getResource("lenna.jpg");
	assertTrue(url.toString().endsWith("lenna.jpg"));

	url = locator1.getResource("lenna.jpg");
	assertTrue(url.toString().endsWith("lenna.jpg"));

	when(ctx.getRealPath("/")).thenReturn(null);
	FileResourceLocator locator2 = new FileResourceLocator(ctx);
	assertNull(locator2.getResource("lenna.jpg"));

	when(ctx.getRealPath("/")).thenReturn("");
	FileResourceLocator locator3 = new FileResourceLocator(ctx);
	assertNull(locator3.getResource("lenna.jpg"));
    }

    @Test
    public void testGetResource_WithQuery() throws MalformedURLException {
	System.out.println("getResource_WithQuery");

	ServletContext ctx = mock(ServletContext.class);
	when(ctx.getRealPath("/")).thenReturn(rootPath);
	when(ctx.getRealPath("lenna.jpg")).thenReturn(rootPath + File.separator + "lenna.jpg");

	FileResourceLocator locator0 = new FileResourceLocator(ctx);
	locator0.setServletContext(ctx);

	URL url = locator0.getResource("lenna.jpg?t=0");
	assertTrue(url.toExternalForm().endsWith("/lenna.jpg"));
    }

    @Test
    public void testGetResource_HTTP() throws MalformedURLException {
	System.out.println("getResource_HTTP");

	ServletContext ctx = mock(ServletContext.class);
	when(ctx.getRealPath("/")).thenReturn(rootPath);
	when(ctx.getRealPath("lenna.jpg")).thenReturn(rootPath + File.separator + "lenna.jpg");

	FileResourceLocator locator0 = new FileResourceLocator(ctx);
	FileResourceLocator locator1 = new FileResourceLocator();
	locator1.setServletContext(ctx);

	assertNull(locator0.getResource("http://foobar.com/foo.jpg"));
	assertNull(locator1.getResource("http://foobar.com/foo.jpg"));
    }

    @Test
    public void testGetResource_FTP() throws MalformedURLException {
	System.out.println("getResource_HTTP");

	ServletContext ctx = mock(ServletContext.class);
	when(ctx.getRealPath("/")).thenReturn(rootPath);
	when(ctx.getRealPath("lenna.jpg")).thenReturn(rootPath + File.separator + "lenna.jpg");

	FileResourceLocator locator0 = new FileResourceLocator(ctx);
	FileResourceLocator locator1 = new FileResourceLocator();
	locator1.setServletContext(ctx);

	assertNull(locator0.getResource("ftp://foobar.com/foo.jpg"));
	assertNull(locator1.getResource("http://foobar.com/foo.jpg"));
    }

    @Test
    public void testGetResource_ForbiddenFile() throws MalformedURLException {
	System.out.println("getResource_ForbiddenFile");

	ServletContext ctx = mock(ServletContext.class);
	when(ctx.getRealPath("/")).thenReturn(rootPath);

	FileResourceLocator loc = new FileResourceLocator(ctx);

	assertNull(loc.getResource("../lenna.jpg"));
    }

    @Test
    public void testGetURL_String() throws MalformedURLException {
	System.out.println("getURL_String");

	FileResourceLocator locator = new FileResourceLocator();
	URL url = locator.getURL(rootPath + File.separator + "lenna.jpg");

	assertNotNull(url);
	assertTrue(url.toString().startsWith("file:/"));
	assertTrue(url.toString().endsWith("lenna.jpg"));
    }

    @Test
    public void testGetURL_File() throws MalformedURLException {
	System.out.println("getURL_File");

	FileResourceLocator locator = new FileResourceLocator();
	URL url = locator.getURL(new File(rootPath + File.separator + "lenna.jpg"));

	assertNotNull(url);
	assertTrue(url.toString().startsWith("file:/"));
	assertTrue(url.toString().endsWith("lenna.jpg"));
    }

}
