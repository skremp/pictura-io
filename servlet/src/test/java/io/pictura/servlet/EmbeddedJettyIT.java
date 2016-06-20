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

import java.awt.image.BufferedImage;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Steffen Kremp
 */
public class EmbeddedJettyIT {
    
    // The test server host (binding)
    private static String host;
    // The test server port (binding)
    private static int port;
    
    private static Server server;
    
    @BeforeClass
    public static void setUpRuntimeProperties() throws Exception {
	Properties props = new Properties();
	props.load(PicturaServletIT.class.getResourceAsStream("/properties/test-runtime.properties"));

	host = props.getProperty("it.host");
	port = Integer.parseInt(props.getProperty("it.port")) + 100;
	
	LibWebp.addLibraryPath(System.getProperty("java.io.tmpdir"));
	LibWebp.addLibWebp(props.getProperty("it.libwebp"));
		
	FileResourceManager rm = (FileResourceManager) PicturaServletIT.getResourceManager();	
	
	ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        context.setResourceBase(rm.getBase().getAbsolutePath());
        
	server = new Server(new java.net.InetSocketAddress(host, port));	
	server.setHandler(context);
 
        // Add servlet
        ServletHolder sh = context.addServlet(PicturaServlet.class, "/*");
        sh.setInitOrder(1);
        sh.setAsyncSupported(true);
        sh.setInitParameter(PicturaServlet.IPARAM_DEBUG, "true");
	
        server.start();
	System.out.println("Embedded Jetty started");
    }
    
    @AfterClass
    public static void tearDownServletContainer() throws Exception {
	if (server != null) {
	    server.stop();
	    System.out.println("Embedded Jetty stopped");
	}
    }
    
    @Test
    public void testServer() throws Exception {
	BufferedImage img = PicturaServletIT.doGetImage(new java.net.URL("http://" + host + ":" + port + "/lenna.jpg"), 200, "image/jpeg");
	assertNotNull(img);
	assertTrue(img.getWidth() > 0);
	assertTrue(img.getHeight() > 0);
	
	img = PicturaServletIT.doGetImage(new java.net.URL("http://" + host + ":" + port + "/s=w201/lenna.jpg"), 200, "image/jpeg");
	assertNotNull(img);
	assertTrue(img.getWidth() == 201);
	assertTrue(img.getHeight() > 0);
    }
    
}
