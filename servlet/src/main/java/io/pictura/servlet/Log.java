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

import javax.servlet.ServletContext;

/**
 * The intention of this class is that there maybe problems with different
 * logging frameworks on the running app server. So this class copies the style
 * of JCL and will automatically try to log to an available JCL or SLF4J. If
 * both fails, the logs will be written to the default servlet context.
 * <p>
 * It is also possible to set a context wide log level only for this library. To
 * handle this you can set a context init parameter.
 * </p>
 */
final class Log {

    /**
     * The default log level.
     */
    public static final String DEFAULT_LEVEL = "INFO";

    private enum LogLevel {

	TRACE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4), FATAL(5);

	private final int level;

	private LogLevel(int level) {
	    this.level = level;
	}

	int value() {
	    return level;
	}
    }

    private static ServletContext context;
    private static LogLevel level;

    static {
	level = LogLevel.INFO;
    }

    // Supported logging facades
    private org.slf4j.Logger slf4jLog;
    private org.apache.commons.logging.Log jclLog;

    private final Class<?> clazz;

    private Log(Class<?> clazz) {
	this.clazz = clazz;

	// Testing if there is any logging API (facade) available
	if (jclLog == null && classForName("org.apache.commons.logging.Log") != null) {
	    jclLog = org.apache.commons.logging.LogFactory.getLog(this.clazz);
	    return;
	}
	
	if (slf4jLog == null && classForName("org.slf4j.LoggerFactory") != null) {
	    slf4jLog = org.slf4j.LoggerFactory.getLogger(this.clazz);
	}
    }
    
    /**
     * <p> Is trace logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than trace. </p>
     *
     * @return true if trace is enabled in the underlying logger.
     */
    public boolean isTraceEnabled() {
        return isLogLevelEnabled(LogLevel.TRACE);
    }
    
    /**
     * <p> Is debug logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than debug. </p>
     *
     * @return true if debug is enabled in the underlying logger.
     */
    public boolean isDebugEnabled() {
        return isLogLevelEnabled(LogLevel.DEBUG);
    }
    
    /**
     * <p> Is info logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than info. </p>
     *
     * @return true if info is enabled in the underlying logger.
     */
    public boolean isInfoEnabled() {
        return isLogLevelEnabled(LogLevel.INFO);
    }
    
    /**
     * <p> Is warn logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than warn. </p>
     *
     * @return true if warn is enabled in the underlying logger.
     */
    public boolean isWarnEnabled() {
        return isLogLevelEnabled(LogLevel.WARN);
    }
    
    /**
     * <p> Is error logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than error. </p>
     *
     * @return true if error is enabled in the underlying logger.
     */
    public boolean isErrorEnabled() {
        return isLogLevelEnabled(LogLevel.ERROR);
    }
    
    /**
     * <p> Is fatal logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than fatal. </p>
     *
     * @return true if fatal is enabled in the underlying logger.
     */
    public boolean isFatalEnabled() {
        return isLogLevelEnabled(LogLevel.FATAL);
    }

    /**
     * Log a message with trace log level.
     *
     * @param o log this message
     */
    public void trace(Object o) {
	trace(o, null);
    }

    /**
     * Log an error with trace log level.
     *
     * @param o log this message
     * @param t log this cause
     */
    public void trace(Object o, Throwable t) {
	write(LogLevel.TRACE, o, t);
    }

    /**
     * Log a message with debug log level.
     *
     * @param o log this message
     */
    public void debug(Object o) {
	debug(o, null);
    }

    /**
     * Log an error with debug log level.
     *
     * @param o log this message
     * @param t log this cause
     */
    public void debug(Object o, Throwable t) {
	write(LogLevel.DEBUG, o, t);
    }

    /**
     * Log a message with info log level.
     *
     * @param o log this message
     */
    public void info(Object o) {
	info(o, null);
    }

    /**
     * Log an error with info log level.
     *
     * @param o log this message
     * @param t log this cause
     */
    public void info(Object o, Throwable t) {
	write(LogLevel.INFO, o, t);
    }

    /**
     * Log a message with warn log level.
     *
     * @param o log this message
     */
    public void warn(Object o) {
	warn(o, null);
    }

    /**
     * Log an error with warn log level.
     *
     * @param o log this message
     * @param t log this cause
     */
    public void warn(Object o, Throwable t) {
	write(LogLevel.WARN, o, t);
    }

    /**
     * Log a message with error log level.
     *
     * @param o log this message
     */
    public void error(Object o) {
	error(o, null);
    }

    /**
     * Log an error with error log level.
     *
     * @param o log this message
     * @param t log this cause
     */
    public void error(Object o, Throwable t) {
	write(LogLevel.ERROR, o, t);
    }

    /**
     * Log a message with fatal log level.
     *
     * @param o log this message
     */
    public void fatal(Object o) {
	fatal(o, null);
    }

    /**
     * Log an error with fatal log level.
     *
     * @param o log this message
     * @param t log this cause
     */
    public void fatal(Object o, Throwable t) {
	write(LogLevel.FATAL, o, t);
    }

    private void write(LogLevel l, Object o, Throwable t) {
	if (isLogLevelEnabled(l) && o != null) {
	    if (jclLog != null) {
		writeJCL(l, o, t);
	    } else if (slf4jLog != null) {
		writeSLF4J(l, o, t);
	    } else if (context != null) {
		writeCTX(l, o, t);
	    } else {
		// As fallback write to stdout
		if (t == null && l.value() < LogLevel.WARN.value()) {
		    System.out.println(getMessage(l, o));
		} else {
		    System.err.println(getMessage(l, o));
		    if (t != null) {
			t.printStackTrace(System.err);
		    }
		}
	    }
	}
    }

    private void writeJCL(LogLevel l, Object o, Throwable t) {
	switch (l) {
	    case TRACE:		
                jclLog.trace(o, t);
		return;

	    case DEBUG:		
                jclLog.debug(o, t);
		return;

	    case INFO:		
                jclLog.info(o, t);
		return;

	    case WARN:		
                jclLog.warn(o, t);
		return;

	    case ERROR:
                jclLog.error(o, t);
		return;

	    case FATAL:
                jclLog.fatal(o, t);
	}
    }

    private void writeSLF4J(LogLevel l, Object o, Throwable t) {
	switch (l) {
	    case TRACE:		
                slf4jLog.trace(String.valueOf(o), t);
		return;

	    case DEBUG:
                slf4jLog.debug(String.valueOf(o), t);
		return;

	    case INFO:
                slf4jLog.info(String.valueOf(o), t);
		return;

	    case WARN:		
                slf4jLog.warn(String.valueOf(o), t);
		return;

	    case ERROR:
	    case FATAL:
		slf4jLog.error(String.valueOf(o), t);
	}
    }

    private void writeCTX(LogLevel l, Object o, Throwable t) {
	if (t != null) {
	    context.log(getMessage(l, o), t);
	} else {
	    context.log(getMessage(l, o));
	}
    }
    
    private boolean isLogLevelEnabled(LogLevel l) {
        if (l.value() >= Log.level.value()) {
            if (jclLog != null) {
                return isJCLLogLevelEnabled(l);
            } else if (slf4jLog != null) {
		return isSLF4JLogLevelEnabled(l);
	    }
            return true;
        }
        return false;
    }
    
    private boolean isJCLLogLevelEnabled(LogLevel l) {
        switch (l) {
	    case TRACE:
		return jclLog.isTraceEnabled();

	    case DEBUG:
		return jclLog.isDebugEnabled();

	    case INFO:
		return jclLog.isInfoEnabled();

	    case WARN:
		return jclLog.isWarnEnabled();

	    case ERROR:
		return jclLog.isErrorEnabled();

	    case FATAL:
		return jclLog.isFatalEnabled();
                
            default:
                return false;
	}
    }
    
    private boolean isSLF4JLogLevelEnabled(LogLevel l) {
        switch (l) {
	    case TRACE:
		return slf4jLog.isTraceEnabled();

	    case DEBUG:
		return slf4jLog.isDebugEnabled();

	    case INFO:
		return slf4jLog.isInfoEnabled();

	    case WARN:
		return slf4jLog.isWarnEnabled();

	    case ERROR:
            case FATAL:
		return slf4jLog.isErrorEnabled();
                
            default:
                return false;
	}
    }

    private String getMessage(LogLevel l, Object o) {
	StringBuilder msg = new StringBuilder();
	msg.append(clazz != null ? clazz.getName() : "null");
	msg.append(" ");
	msg.append(l.toString());
	msg.append(": ");
	msg.append(o.toString());
	return msg.toString();
    }

    /**
     * Return a logger named corresponding to the class passed as parameter.
     *
     * @param clazz The returned logger will be named after clazz.
     * @return The logger.
     */
    public static Log getLog(Class<?> clazz) {
	return new Log(clazz);
    }

    /**
     * Sets the context wide log configuration for this component.
     *
     * @param context The servlet configuration.
     */
    static void setConfiguration(final ServletContext context) {
	Log.context = context;

	String contextLogLevel = context != null 
		? context.getInitParameter("io.pictura.servlet.LOG_LEVEL") 
		: DEFAULT_LEVEL;
	
	switch (contextLogLevel != null 
		? contextLogLevel.trim().toUpperCase() : DEFAULT_LEVEL) {
	    
	    case "TRACE":
		Log.level = LogLevel.TRACE;
		break;

	    case "DEBUG":
		Log.level = LogLevel.DEBUG;
		break;

	    case "WARN":
		Log.level = LogLevel.WARN;
		break;

	    case "ERROR":
		Log.level = LogLevel.ERROR;
		break;

	    case "FATAL":
		Log.level = LogLevel.FATAL;
		break;

	    case DEFAULT_LEVEL: // INFO
	    default:
		Log.level = LogLevel.INFO;
	}
    }

    private static Class<?> classForName(String className) {
	Class<?> c = null;
	try {
	    c = Class.forName(className, false, context != null
		    ? context.getClassLoader() : Thread.currentThread().getContextClassLoader());
	} catch (ClassNotFoundException ex) {
	    // do nothing!
	}
	return c;
    }
}