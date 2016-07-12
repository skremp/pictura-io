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

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ServiceRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Takes care of registering and de-registering local ImageIO plugins (service
 * providers) for the servlet context.
 * <p>
 * De-registers all plugins which have the {@link Thread#getContextClassLoader()
 * current thread's context class loader} as its class loader on
 * {@code contextDestroyed} event, to avoid class/resource leak.</p>
 * 
 * <p>To register this context listener, append</p>
 * 
 * <pre>
 * &lt;web-app ...&gt;
 *   ...
 *   &lt;listener&gt;
 *     &lt;listener-class&gt;io.pictura.servlet.IIOProviderContextListener&lt;/listener-class&gt;
 *   &lt;/listener&gt;
 *   ...
 * &lt;/web-app&gt;
 * </pre>
 * 
 * to the web.xml.
 * 
 * 
 * @author Steffen Kremp
 *
 * @see ServletContextListener
 *
 * @since 1.0
 */
public class IIOProviderContextListener implements ServletContextListener {

    private static final Log LOG = Log.getLog(IIOProviderContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
	Log.setConfiguration(sce.getServletContext());
        
	LOG.info("Scan classpath for Image I/O plugins");
	ImageIO.scanForPlugins();
        
        if (LOG.isDebugEnabled()) {
            Iterator<ImageReaderSpi> readers = IIORegistry.getDefaultInstance()
                    .getServiceProviders(ImageReaderSpi.class, true);
            while (readers.hasNext()) {
                logImageReaderWriterSpi(readers.next());
            }
            Iterator<ImageReaderSpi> writers = IIORegistry.getDefaultInstance()
                    .getServiceProviders(ImageReaderSpi.class, true);
            while (writers.hasNext()) {
                logImageReaderWriterSpi(writers.next());
            }
        }
    }
    
    private void logImageReaderWriterSpi(ImageReaderWriterSpi spi) {
        LOG.debug("Found Image I/O service provider \"" 
                + spi.getPluginClassName() + ":" 
                + spi.getVersion()+ ":" 
                + spi.getVendorName() + "\" on class path to handle " 
                + Arrays.toString(spi.getMIMETypes()));
    }

    // https://github.com/haraldk/TwelveMonkeys/blob/master/servlet/src/main/java/com/twelvemonkeys/servlet/image/IIOProviderContextListener.java
    @Override
    public void contextDestroyed(ServletContextEvent sce) {

	// De-register any locally registered IIO plugins. Relies on each web 
	// app having its own context class loader.
	final IIORegistry registry = IIORegistry.getDefaultInstance();

	// scanForPlugins uses context class loader
	final LocalFilter localFilter = new LocalFilter(Thread.currentThread().getContextClassLoader());

	Iterator<Class<?>> categories = registry.getCategories();

	while (categories.hasNext()) {
	    Class<?> category = categories.next();
	    Iterator<?> providers = registry.getServiceProviders(category, localFilter, false);

	    // Copy the providers, as de-registering while iterating over providers 
	    // will lead to ConcurrentModificationExceptions.
	    List<Object> providersCopy = new ArrayList<>();
	    while (providers.hasNext()) {
		providersCopy.add(providers.next());
	    }

	    for (Object provider : providersCopy) {
		registry.deregisterServiceProvider(provider);
		LOG.info("Unregistered locally installed provider class: "
			+ provider.getClass().getName());
	    }
	}
    }    

    // https://github.com/haraldk/TwelveMonkeys/blob/master/servlet/src/main/java/com/twelvemonkeys/servlet/image/IIOProviderContextListener.java
    private static class LocalFilter implements ServiceRegistry.Filter {

	private final ClassLoader loader;

	public LocalFilter(ClassLoader loader) {
	    this.loader = loader;
	}

	@Override
	public boolean filter(Object provider) {
	    return provider.getClass().getClassLoader() == loader;
	}
    }

}
