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

import io.undertow.Undertow;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.servlet.Servlet;
import javax.servlet.annotation.WebInitParam;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author Steffen Kremp
 */
public abstract class PicturaServletIT {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface WebInitParams {

	WebInitParam[] value();
    }

    static Properties props;
    
    // The test server host (binding)
    private static String host;
    // The test server port (binding)
    private static int port;

    // Location of the test images
    private static String resourceBase;

    // Test server instance
    private static Undertow undertow;
    private static DeploymentManager manager;

    protected Class<? extends Servlet> servletImplClazz;

    protected static String getResourceBase() {
	return resourceBase;
    }   
    
    @BeforeClass
    public static void setUpRuntimeProperties() throws Exception {
	props = new Properties();
	props.load(PicturaServletIT.class.getResourceAsStream("/properties/test-runtime.properties"));

	host = props.getProperty("it.host");
	port = Integer.parseInt(props.getProperty("it.port"));
	
	LibWebp.addLibraryPath(System.getProperty("java.io.tmpdir"));
	LibWebp.addLibWebp(props.getProperty("it.libwebp"));
    }

    @AfterClass
    public static void tearDownServletContainer() throws Exception {
	if (undertow != null) {
	    if (manager.getState() == DeploymentManager.State.STARTED) {
		manager.stop();
	    }
	    if (manager.getState() == DeploymentManager.State.DEPLOYED) {
		manager.undeploy();
	    }
	    undertow.stop();
	    undertow = null;
	}
    }

    @Before
    public void setUpServletContainer() throws Exception {
	if (undertow == null) {
	    ServletInfo servletInfo = new ServletInfo("pictura-test",
		    servletImplClazz != null ? servletImplClazz : PicturaServlet.class);
	    servletInfo.setAsyncSupported(getAsyncSupported());
	    servletInfo.setLoadOnStartup(1);
	    servletInfo.addMapping("/*");

	    final Annotation[] annotations = getClass().getAnnotations();
	    if (annotations != null && annotations.length > 0) {
		for (Annotation a : annotations) {
		    if (a instanceof WebInitParam) {
			WebInitParam wip = (WebInitParam) a;
			servletInfo.addInitParam(wip.name(), wip.value());
		    } else if (a instanceof WebInitParams) {
			WebInitParams wips = (WebInitParams) a;
			for (WebInitParam wip : wips.value()) {
			    servletInfo.addInitParam(wip.name(), wip.value());
			}
		    }
		}
	    }

	    DeploymentInfo deploymentInfo = deployment()
		    .setClassLoader(PicturaServletIT.class.getClassLoader())
		    .setDeploymentName("pictura.war")
		    .setContextPath("/pictura")
		    .setResourceManager(getResourceManager())
		    .setUrlEncoding("UTF-8")
		    .addListener(new ListenerInfo(IIOProviderContextListener.class))
		    .addServlet(servletInfo);

	    manager = defaultContainer().addDeployment(deploymentInfo);
	    manager.deploy();

	    undertow = Undertow.builder()
		    .addHttpListener(port, host)
		    .setHandler(manager.start())
		    .build();
	    undertow.start();
	}
    }

    protected boolean getAsyncSupported() {
	return true;
    }

    protected static HttpURLConnection doGetResponse(URL url, int expectedStatusCode,
	    String expectedContentType) throws Exception {

	HttpURLConnection con = (HttpURLConnection) url.openConnection();
	con.setRequestMethod("GET");
	con.connect();

        if (con.getResponseCode() != 200) {
            System.out.println("NOT 200: " + url.toExternalForm());
        }
        
	assertEquals(expectedStatusCode, con.getResponseCode());

	if (expectedContentType != null) {
	    assertEquals(expectedContentType, con.getContentType());
	}

	return con;
    }

    protected static byte[] doGetContent(URL url, int expectedStatusCode,
	    String expectedContentType) throws Exception {

	return doGetContent(url, expectedStatusCode, expectedContentType, null);
    }

    protected static byte[] doGetContent(URL url, int expectedStatusCode,
	    String expectedContentType, Map<String, String> headers) throws Exception {

	byte[] content = null;

	HttpURLConnection con = null;
	InputStream is = null;
	try {
	    con = (HttpURLConnection) url.openConnection();
	    con.setRequestMethod("GET");

	    if (headers != null && !headers.isEmpty()) {
		Set<String> headerNames = headers.keySet();
		for (String name : headerNames) {
		    con.setRequestProperty(name, headers.get(name));
		}
	    }

	    con.connect();

	    assertEquals(expectedStatusCode, con.getResponseCode());

	    if (expectedContentType != null) {
		assertEquals(expectedContentType, con.getContentType());
	    }

	    if (con.getResponseCode() == HttpURLConnection.HTTP_OK
		    && con.getContentType().startsWith("image/")
		    && !con.getContentType().startsWith("image/x-")) {

		assertTrue(con.getContentLength() > 1);

		is = con.getInputStream();

		int len;
		byte[] buf = new byte[1024 * 16];

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		while ((len = is.read(buf)) > -1) {
		    bos.write(buf, 0, len);
		}

		content = bos.toByteArray();
	    }

	} finally {
	    if (con != null) {
		if (is != null) {
		    try {
			is.close();
		    } catch (IOException ex) {
		    }
		}
		con.disconnect();
	    }
	}

	return content;
    }

    protected static BufferedImage doGetImage(URL url, int expectedStatusCode,
	    String expectedContentType) throws Exception {

	return doGetImage(url, expectedStatusCode, expectedContentType, null);
    }

    protected static BufferedImage doGetImage(URL url, int expectedStatusCode,
	    String expectedContentType, Map<String, String> headers) throws Exception {

	byte[] content = doGetContent(url, expectedStatusCode, expectedContentType);
	return content != null ? ImageIO.read(new ByteArrayInputStream(content)) : null;
    }

    protected static String getHost() {
	return host + ":" + port;
    }

    static ResourceManager getResourceManager() {
	resourceBase = System.getProperty("java.io.tmpdir");
	if (!resourceBase.endsWith(File.separator)) {
	    resourceBase += File.separator;
	}
	resourceBase += "pictura" + File.separator;

	try {
	    File path = new File(resourceBase);
	    if (!path.exists()) {
		if (!path.mkdirs()) {
		    fail("Can't create temporary root path \"" + path.getAbsolutePath() + "\".");
		}
	    }

	    File tmp[] = new File[]{
		new File(path.getAbsolutePath() + File.separator + "lenna.jpg"),
		new File(path.getAbsolutePath() + File.separator + "lenna.png"),
		new File(path.getAbsolutePath() + File.separator + "lenna.gif"),
		new File(path.getAbsolutePath() + File.separator + "lenna.psd"),
		new File(path.getAbsolutePath() + File.separator + "loader.gif"),
		new File(path.getAbsolutePath() + File.separator + "lenna-trim.jpg"),
		new File(path.getAbsolutePath() + File.separator + "cmyk-jpeg.jpg")
	    };
	    for (File f : tmp) {
		if (!f.exists()) {
		    if (!f.createNewFile()) {
			fail("Can't create temporary resource file \"" + f.getAbsolutePath() + "\".");
		    }
		}

		try (InputStream is = FileResourceLocatorTest.class.getResourceAsStream("/" + f.getName());
			OutputStream os = new FileOutputStream(f)) {

		    int read;
		    byte[] bytes = new byte[1024];

		    while ((read = is.read(bytes)) != -1) {
			os.write(bytes, 0, read);
		    }

		    f.deleteOnExit();
		    path.deleteOnExit();
		}
	    }

	} catch (IOException ex) {
	    fail("Can't create temporary root path and test resource.");
	}

	return new FileResourceManager(new File(resourceBase), 1L);
    }

}
