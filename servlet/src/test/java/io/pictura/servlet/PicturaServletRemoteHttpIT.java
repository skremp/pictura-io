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
import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static io.pictura.servlet.PicturaServletIT.getResourceManager;
import javax.servlet.annotation.WebInitParam;

/**
 * @author Steffen Kremp
 */
@PicturaServletIT.WebInitParams({
    @WebInitParam(name = PicturaServlet.IPARAM_HEADER_ADD_CONTENT_LOCATION, value = "true"),
    @WebInitParam(name = PicturaServlet.IPARAM_HEADER_ADD_TRUE_CACHE_KEY, value = "true"),
    @WebInitParam(name = PicturaServlet.IPARAM_RESOURCE_LOCATORS, value = "io.pictura.servlet.FileResourceLocator,io.pictura.servlet.HttpResourceLocator")
})
public class PicturaServletRemoteHttpIT extends PicturaServletIT {

    // The test server host (binding)
    private static final String HOST = "localhost";
    // The test server port (binding)
    private static final int PORT = 8194;

    // Test server instance
    private static Undertow remoteUndertow;
    private static DeploymentManager manager;

    @AfterClass
    public static void tearDownRemoteServer() throws Exception {
	if (remoteUndertow != null) {
	    if (manager.getState() == DeploymentManager.State.STARTED) {
		manager.stop();
	    }
	    if (manager.getState() == DeploymentManager.State.DEPLOYED) {
		manager.undeploy();
	    }
	    remoteUndertow.stop();
	    remoteUndertow = null;
	}
    }

    @Before
    public void setUpRemoteServer() throws Exception {
	if (remoteUndertow == null) {
	    ServletInfo servletInfo = new ServletInfo("pictura-remote", PicturaServlet.class);
	    servletInfo.setAsyncSupported(getAsyncSupported());
	    servletInfo.setLoadOnStartup(1);
	    servletInfo.addInitParam(PicturaServlet.IPARAM_RESOURCE_LOCATORS,
		    "io.pictura.servlet.FileResourceLocator");
	    servletInfo.addMapping("/*");

	    DeploymentInfo deploymentInfo = deployment()
		    .setClassLoader(PicturaServletMethodIT.class.getClassLoader())
		    .setDeploymentName("pictura.war")
		    .setContextPath("/pictura")
		    .setResourceManager(getResourceManager())
		    .setUrlEncoding("UTF-8")
		    .addServlet(servletInfo);

	    manager = defaultContainer().addDeployment(deploymentInfo);
	    manager.deploy();

	    remoteUndertow = Undertow.builder()
		    .addHttpListener(PORT, HOST)
		    .setHandler(manager.start())
		    .build();
	    remoteUndertow.start();
	}
    }

    @Test
    public void testRemoteLocatedResource() throws Exception {
	System.out.println("remoteLocatedResource");

	BufferedImage image = doGetImage(new URL("http://" + getHost() + "/f=jpg/http://localhost:8194/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");

	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());

	// proxy
	image = doGetImage(new URL("http://" + getHost() + "/http://localhost:8194/lenna.jpg"), HttpURLConnection.HTTP_OK, "image/jpeg");

	assertEquals(400, image.getWidth());
	assertEquals(225, image.getHeight());
    }

}
