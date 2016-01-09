/**
 * Copyright 2016 Steffen Kremp
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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LoggingMXBean;
import javax.servlet.ServletContext;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Steffen Kremp
 */
public class LogTest {

    @Test
    public void testGetLog() throws Exception {
        Log l1 = Log.getLog(TestClass1.class);
        assertNotNull(l1);
        Log l2 = Log.getLog(TestClass2.class);
        assertNotNull(l2);
        
        assertNotSame(l1, l2);
    }
    
    @Test
    public void testSetConfiguration() throws Exception {
        Log log = Log.getLog(TestClass1.class);
                
        // Check default log level
        assertFalse(log.isTraceEnabled());
        assertFalse(log.isDebugEnabled());
        assertTrue(log.isInfoEnabled());
        assertTrue(log.isWarnEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isFatalEnabled());
        
        ServletContext ctx = mock(ServletContext.class);
        when(ctx.getInitParameter("io.pictura.servlet.LOG_LEVEL")).thenReturn("TRACE");
        
        Log.setConfiguration(ctx);
        
        LoggingMXBean loggingMXBean = LogManager.getLoggingMXBean();
        List<String> loggerNames = loggingMXBean.getLoggerNames();
        for (String loggerName : loggerNames) {
            if (loggerName.startsWith("io.pictura.servlet")) {
                loggingMXBean.setLoggerLevel(loggerName, Level.FINEST.getName());
            }
        }
        
        assertTrue(log.isTraceEnabled());
        assertTrue(log.isDebugEnabled());
        assertTrue(log.isInfoEnabled());
        assertTrue(log.isWarnEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isFatalEnabled());
        
        when(ctx.getInitParameter("io.pictura.servlet.LOG_LEVEL")).thenReturn("WARN");
        Log.setConfiguration(ctx);
        
        assertFalse(log.isTraceEnabled());
        assertFalse(log.isDebugEnabled());
        assertFalse(log.isInfoEnabled());
        assertTrue(log.isWarnEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isFatalEnabled());
    }
    
    private static final class TestClass1 { }
    
    private static final class TestClass2 { }
    
}
