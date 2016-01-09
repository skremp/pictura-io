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

import static io.pictura.servlet.PicturaServletIT.getResourceManager;
import io.undertow.Undertow;
import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.ListenerInfo;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import javax.servlet.DispatcherType;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static io.pictura.servlet.PicturaServletIT.doGetImage;

/**
 * @author Steffen Kremp
 */
public class PicturaFilterIT {

    // The test server host (binding)
    private static String host;
    // The test server port (binding)
    private static int port;

    // Test server instance
    private static Undertow undertow;
    private static DeploymentManager manager;

    @BeforeClass
    public static void setUpRuntimeProperties() throws Exception {
	Properties props = new Properties();
	props.load(PicturaServletIT.class.getResourceAsStream("/properties/test-runtime.properties"));

	host = props.getProperty("it.host");
	port = Integer.parseInt(props.getProperty("it.port"));
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
	    
	    FilterInfo fi = new FilterInfo("pictura-filter", PicturaFilter.class);
	    fi.setAsyncSupported(true);

	    fi.addInitParam(PicturaFilter.IPARAM_SERVLET_CLASS, "io.pictura.servlet.PicturaPostServlet");
	    fi.addInitParam(PicturaServlet.IPARAM_MAX_POOL_SIZE, "2");
	    fi.addInitParam(PicturaServlet.IPARAM_WORKER_QUEUE_SIZE, "10");
	    fi.addInitParam(PicturaServlet.IPARAM_STATS_ENABLED, "true");
	    fi.addInitParam(PicturaServlet.IPARAM_PLACEHOLDER_PRODUCER_ENABLED, "true");
	    fi.addInitParam(PicturaServlet.IPARAM_HTTP_MAX_FORWARDS, "1");
	    fi.addInitParam(PicturaServlet.IPARAM_HTTPS_DISABLE_CERTIFICATE_VALIDATION, "true");
	    fi.addInitParam(PicturaServlet.IPARAM_ENABLE_CONTENT_DISPOSITION, "true");
	    fi.addInitParam(PicturaServlet.IPARAM_ENABLE_BASE64_IMAGE_ENCODING, "true");
	    fi.addInitParam(PicturaServlet.IPARAM_HTTP_AGENT, "");
	    fi.addInitParam(PicturaServlet.IPARAM_RESOURCE_LOCATORS, "io.pictura.servlet.FileResourceLocator");

	    DeploymentInfo deploymentInfo = deployment()
		    .setClassLoader(PicturaServletIT.class.getClassLoader())
		    .setDeploymentName("pictura.war")
		    .setContextPath("/pictura")
		    .setResourceManager(getResourceManager())
		    .setUrlEncoding("UTF-8")
		    .addListener(new ListenerInfo(IIOProviderContextListener.class))
		    .addFilter(fi)
		    .addFilterUrlMapping("pictura-filter", "/*", DispatcherType.REQUEST);
		    
	    manager = defaultContainer().addDeployment(deploymentInfo);
	    manager.deploy();

	    undertow = Undertow.builder()
		    .addHttpListener(port, host)
		    .setHandler(manager.start())
		    .build();
	    undertow.start();
	}
    }
    
    private static String getHost() {
	return host + ":" + port;
    }
    
    @Test
    public void testFilterInstance() throws Exception {
	BufferedImage image;
	
	image = doGetImage(new URL("http://" + getHost() + "/"), HttpURLConnection.HTTP_BAD_REQUEST, null);
	assertNull(image);
	
	image = doGetImage(new URL("http://" + getHost() + "/f=jpg/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
	
	image = doGetImage(new URL("http://" + getHost() + "/s=w100,dpr1.5/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(150, image.getWidth());
	
	image = doGetImage(new URL("http://" + getHost() + "/r=r/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(225, image.getWidth());
	assertEquals(400, image.getHeight());
	
	image = doGetImage(new URL("http://" + getHost() + "/e=ac/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");
	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }
       
}
