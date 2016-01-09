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
package io.pictura.servlet.annotation;

import io.pictura.servlet.PicturaServlet;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import org.junit.Test;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Steffen Kremp
 */
public class PicturaConfigFileTest {

    @Test
    public void testAnnotation() throws Exception {
	
	URL cfgURL = PicturaConfigFileTest.class.getResource("/pictura-config-annotation-test.xml");
	File cfgFile = new File(cfgURL.toURI());
	
	final ServletContext context = mock(ServletContext.class);	
	
	when(context.getInitParameter("io.pictura.servlet.LOG_LEVEL")).thenReturn("INFO");
	when(context.getRealPath("/WEB-INF/pictura-config-annotation-test.xml")).thenReturn(cfgFile.getAbsolutePath());
	when(context.getResource("/WEB-INF/pictura-config-annotation-test.xml")).thenReturn(cfgURL);
		
	ServletMock servlet = new ServletMock(context);
		
	servlet.init(new ServletConfigMock(context));	
	
	assertEquals("true", servlet.getInitParameter(PicturaServlet.IPARAM_DEBUG));
	assertEquals("false", servlet.getInitParameter(PicturaServlet.IPARAM_JMX_ENABLED));
	assertEquals("true", servlet.getInitParameter(PicturaServlet.IPARAM_ENABLE_CONTENT_DISPOSITION));
    }
        
    @PicturaConfigFile("pictura-config-annotation-test.xml")
    static class ServletMock extends PicturaServlet {
	
	private static final long serialVersionUID = 1961509223672032499L;
    
	private final ServletContext context;
	
	ServletMock(ServletContext context) {
	    this.context = context;
	}

	@Override
	public ServletContext getServletContext() {
	    return context;
	}		
	
    }
    
    static class ServletConfigMock implements ServletConfig {

	private final ServletContext context;
		
	ServletConfigMock(ServletContext context) {
	    this.context = context;
	}
	
	@Override
	public String getServletName() {
	    return "";
	}

	@Override
	public ServletContext getServletContext() {
	    return context;
	}

	@Override
	public String getInitParameter(String name) {
	    return null;
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
	    return Collections.<String>emptyEnumeration();
	}
	
    }
    
}
