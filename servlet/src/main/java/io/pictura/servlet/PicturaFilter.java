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
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Filter for use as alternative to the normal servlet approach. This will work
 * equals to the default {@link PicturaServlet}. The filter just loads an
 * instance of an Pictura servlet and delegates requests to the servlet instance
 * for service.
 * <p>
 * With the init parameter {@link #IPARAM_SERVLET_CLASS} it is possible to
 * specify an explicit servlet implementation of an {@link PicturaServlet} which
 * is used to handle the servlet requests. If nothins is specified the default
 * {@link PicturaServlet} is used.
 * <p>
 * Note: Does not supports async operation.
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public class PicturaFilter implements Filter {

    private static final Log LOG = Log.getLog(PicturaFilter.class);
    
    /**
     * Init parameter name to specify an {@link PicturaServlet} class to be use
     * to create the service instance to handle requests by a
     * {@link PicturaFilter} delegate.
     */
    @InitParam
    public static final String IPARAM_SERVLET_CLASS = "servletClass";

    // Our service instance
    private PicturaServlet servlet;

    @Override
    public void init(FilterConfig config) throws ServletException {
	LOG.info("Run pictura servlet wrapped by servlet filter");

	servlet = createServlet(config);
	servlet.init(new FilterConfigWrapper(config));
    }

    @Override
    public void destroy() {
	servlet.destroy();
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
	    final FilterChain chain) throws IOException, ServletException {

	if (request.isAsyncSupported() && !request.isAsyncStarted()) {
	    request.startAsync();
	}
	service(request, response);
    }

    /**
     * Dispatches client requests to the protected <code>service</code> method.
     * Normally, there's no need to override this method.
     *
     * @param request The {@link ServletRequest} object that contains the
     * request the client made of the servlet.
     *
     * @param response The {@link ServletResponse} object that contains the
     * response the servlet returns to the client
     *
     * @throws IOException if an input or output error occurs while the servlet
     * service is handling the HTTP request.
     * @throws ServletException if the HTTP request cannot be handled.
     */
    protected void service(ServletRequest request, ServletResponse response)
	    throws IOException, ServletException {
	servlet.service(request, response);
    }

    /**
     * Creates a new {@link PicturaServlet} instance based on the given filter
     * configuration. All requests handled by this filter instance are delegated
     * to the servlet instance created by this method.
     *
     * @param config The associated filter config for this instance.
     *
     * @return The new Pictura servlet instance.
     *
     * @throws ServletException if something goes wrong or it was not possible
     * to create a new Pictura servlet instance.
     */
    protected PicturaServlet createServlet(FilterConfig config)
	    throws ServletException {

	String servletClazz = config.getInitParameter(IPARAM_SERVLET_CLASS);
	if (servletClazz != null && !servletClazz.isEmpty()) {
	    servletClazz = servletClazz.trim();

	    try {
		Class<?> clazz = Class.forName(servletClazz, true,
			Thread.currentThread().getContextClassLoader());
		Object obj = clazz.newInstance();
		if (obj instanceof PicturaServlet) {
		    return (PicturaServlet) obj;
		} else {
		    throw new ServletException();
		}
	    } catch (ClassNotFoundException | IllegalAccessException |
		    InstantiationException ex) {
		throw new ServletException(ex);
	    }
	}

	return new PicturaServlet();
    }

    // Wrapper class to make a filter config usable in servlets
    private static final class FilterConfigWrapper implements ServletConfig {

	private final FilterConfig config;

	private FilterConfigWrapper(FilterConfig config) {
	    this.config = config;
	}

	@Override
	public String getServletName() {
	    return config.getFilterName();
	}

	@Override
	public ServletContext getServletContext() {
	    return config.getServletContext();
	}

	@Override
	public String getInitParameter(String name) {
	    return config.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
	    return config.getInitParameterNames();
	}

    }

}
