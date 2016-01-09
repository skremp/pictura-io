/**
 * Copyright 2015 Steffen Kremp
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package io.pictura.servlet;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class PicturaFilterTest {

    @Test
    public void testCreateServlet() throws Exception {
        FilterConfig fc = mock(FilterConfig.class);
        when(fc.getInitParameter(PicturaFilter.IPARAM_SERVLET_CLASS)).thenReturn(PicturaPostServlet.class.getName());

        PicturaFilter pf = new PicturaFilter();
        PicturaServlet ps = pf.createServlet(fc);

        assertNotNull(ps);
        assertTrue(ps instanceof PicturaPostServlet);

        when(fc.getInitParameter(PicturaFilter.IPARAM_SERVLET_CLASS)).thenReturn("");
        ps = pf.createServlet(fc);

        assertNotNull(ps);
        assertFalse(ps instanceof PicturaPostServlet);
    }

    @Test(expected = ServletException.class)
    public void testCreateServlet_ServletException_1() throws Exception {
        FilterConfig fc = mock(FilterConfig.class);
        when(fc.getInitParameter(PicturaFilter.IPARAM_SERVLET_CLASS)).thenReturn(FileResourceLocator.class.getName());

        PicturaFilter pf = new PicturaFilter();
        pf.createServlet(fc);
    }

    @Test(expected = ServletException.class)
    public void testCreateServlet_ServletException_2() throws Exception {
        FilterConfig fc = mock(FilterConfig.class);
        when(fc.getInitParameter(PicturaFilter.IPARAM_SERVLET_CLASS)).thenReturn("io.pictura.servlet.FoobarClass");

        PicturaFilter pf = new PicturaFilter();
        pf.createServlet(fc);
    }

}
