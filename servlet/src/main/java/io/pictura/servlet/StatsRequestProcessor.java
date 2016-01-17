/**
 * Copyright 2015, 2016 Steffen Kremp
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

import static io.pictura.servlet.PicturaImageIO.IIO_REGISTRY;
import java.io.IOException;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Special implementation of a request processor to provide some servlet
 * specific real time metrics.
 * <p>
 * The processor supports different status queries via the request parameter
 * <i>q</i>:
 * <ul>
 * <li>status: current status view (default if no query was given)</li>
 * <li>params: lists the user specific init parameters</li>
 * </ul>
 *
 * @see RequestProcessor
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
final class StatsRequestProcessor extends RequestProcessor {

    private static final String CTX_ATTR_T0 = "io.pictura.servlet.STATS_T0";
    private static final String CTX_ATTR_T1 = "io.pictura.servlet.STATS_T1";

    private static final String CTX_ATTR_RT0 = "io.pictura.servlet.STATS_RT0";
    private static final String CTX_ATTR_RT1 = "io.pictura.servlet.STATS_RT1";

    // Context
    private final PicturaServlet servlet;

    private boolean asyncSupported;

    private float throughputRequests;
    private float errorRate;

    /**
     * Creates a new statistics request processor for the specified
     * {@link PicturaServlet}.
     *
     * @param servlet The servlet instance for which the statistics are
     * requested.
     */
    StatsRequestProcessor(PicturaServlet servlet) {
	this.servlet = servlet;
    }

    @Override
    public boolean isCacheable() {
	return false;
    }

    @Override
    protected void doProcess(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {

	final ServletContext ctx = servlet.getServletContext();

	// Do not cache statistics
	resp.setDateHeader(HEADER_DATE, System.currentTimeMillis());
	resp.setDateHeader(HEADER_EXPIRES, 0);
	resp.setHeader(HEADER_CACHECONTROL, "private, max-age=0, no-cache");
	resp.setHeader(HEADER_PRAGMA, "no-cache");

	// Restore throughput from last update	
	long throughputT0 = (ctx.getAttribute(CTX_ATTR_T0 + "-" + servlet.getServletID()) instanceof Long)
		? (long) ctx.getAttribute(CTX_ATTR_T0 + "-" + servlet.getServletID()) : 0L;
	long throughputT1 = (ctx.getAttribute(CTX_ATTR_T1 + "-" + servlet.getServletID()) instanceof Long)
		? (long) ctx.getAttribute(CTX_ATTR_T1 + "-" + servlet.getServletID()) : 0L;
	long throughputRequestsT0 = (ctx.getAttribute(CTX_ATTR_RT0 + "-" + servlet.getServletID()) instanceof Long)
		? (long) ctx.getAttribute(CTX_ATTR_RT0 + "-" + servlet.getServletID()) : 0L;
	long throughputRequestsT1 = (ctx.getAttribute(CTX_ATTR_RT1 + "-" + servlet.getServletID()) instanceof Long)
		? (long) ctx.getAttribute(CTX_ATTR_RT1 + "-" + servlet.getServletID()) : 0L;

	// Calculate the throughput
	if (throughputT0 == 0L) {
	    throughputT0 = System.currentTimeMillis();
	    throughputRequestsT0 = servlet.getCompletedTaskCount();
	} else {
	    throughputT1 = System.currentTimeMillis();
	    throughputRequestsT1 = servlet.getCompletedTaskCount();

	    final long deltaT = (throughputT1 - throughputT0) / 1000;
	    final long deltaReq = throughputRequestsT1 - throughputRequestsT0;

	    throughputRequests = (float) deltaReq / (deltaT > 0 ? deltaT : 1);
	    if (throughputRequests < 0.01f) {
		throughputRequests = 0f;
	    }

	    throughputT0 = throughputT1;
	    throughputRequestsT0 = throughputRequestsT1;
	}

	ctx.setAttribute(CTX_ATTR_T0 + "-" + servlet.getServletID(), throughputT0);
	ctx.setAttribute(CTX_ATTR_T1 + "-" + servlet.getServletID(), throughputT1);
	ctx.setAttribute(CTX_ATTR_RT0 + "-" + servlet.getServletID(), throughputRequestsT0);
	ctx.setAttribute(CTX_ATTR_RT1 + "-" + servlet.getServletID(), throughputRequestsT1);

	// Ensure servlet API 3.0+ is available
	try {
	    asyncSupported = getRequest().isAsyncSupported();
	} catch (Exception | Error e) {
	}

	// Calculate the error rates
	Map<Integer, Long> errors = servlet.getCumulativeErrors();
	if (!errors.isEmpty() && servlet.getCompletedTaskCount() > 0) {
	    long err4xx = 0L, err5xx = 0L;

	    Iterator<Integer> iterErrors = errors.keySet().iterator();
	    while (iterErrors.hasNext()) {
		Integer key = iterErrors.next();
		Long val = errors.get(key);

		if (key > 499) {
		    err5xx += val;
		} else {
		    err4xx += val;
		}
	    }
	    errorRate = ((float) err4xx / (float) servlet.getCompletedTaskCount()) + 
		    ((float) err5xx / (float) servlet.getCompletedTaskCount());
	} else {
	    errorRate = 0f;
	}

	String query = req.getParameter("q") != null ? req.getParameter("q") : "status";
	String json;

	switch (query) {
	    case "status":
		json = buildCoreStatusJsonResponse();
		break;

	    case "errors":
		json = buildErrorStatsJsonResponse(req.getParameter("f"));
		break;
		
	    case "params":
		json = buildInitParamsJsonResponse();
		break;

	    case "imageio":
		json = buildImageIORegistryJsonResponse();
		break;
		
	    case "cache":
		String filter = req.getParameter("f") != null ? req.getParameter("f") : null;
		String action = req.getParameter("a") != null ? req.getParameter("a") : null;
		
		if ("delete".equals(action) && (filter == null || filter.isEmpty())) {
		    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
			    "Cache delete request without filter");
		    return;
		}
		
		json = buildHttpCacheStatsResponse(filter, action);
		break;
		
	    default:
		resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
			"Invalid stats query \"" + query + "\"");
		return;
	}

	// We will use alwasy UTF-8 as encoding  
	byte[] data = json.getBytes("UTF-8");

	resp.setStatus(HttpServletResponse.SC_OK);
	resp.setContentLength(data.length);
	resp.setContentType("application/json");
	resp.setCharacterEncoding("UTF-8");

	doWrite(data, req, resp);
    }

    private String buildCoreStatusJsonResponse() {
	StringBuilder json = new StringBuilder();
	json.append("{").append("\n");	
	
	json.append("\t").append("\"id\": \"").append(escapeString(servlet.getServletID())).append("\",\n");
	json.append("\t").append("\"version\": \"").append(escapeString(Version.getVersionString())).append("\",\n");
	json.append("\t").append("\"servlet\": \"").append(escapeString(servlet.getServletName())).append("\",\n");
	json.append("\t").append("\"implClass\": \"").append(escapeString(servlet.getClass().getName())).append("\",\n");
	if (servlet.getServletVendor() != null && !servlet.getServletVendor().isEmpty()) {
	    json.append("\t").append("\"implVendor\": \"").append(escapeString(servlet.getServletVendor())).append("\",\n");
	}
	json.append("\t").append("\"implVersion\": \"").append(escapeString(servlet.getServletVersion())).append("\",\n");
	json.append("\t").append("\"uptime\": \"").append(formatUptime(servlet.getUptime())).append("\",\n");
	json.append("\t").append("\"alive\": ").append(servlet.isAlive()).append(",\n");
	json.append("\t").append("\"async\": ").append(asyncSupported).append(",\n");
	json.append("\t").append("\"contextPath\": \"").append(escapeString(servlet.getServletContext().getContextPath())).append("\",\n");
        
        if (!servlet.useContainerPool()) {
            json.append("\t").append("\"executor\": ").append("{").append("\n");
            json.append("\t\t").append("\"poolSize\": ").append(servlet.getPoolSize()).append(",\n");
            json.append("\t\t").append("\"queueSize\": ").append(servlet.getQueueSize()).append(",\n");
            json.append("\t\t").append("\"activeCount\": ").append(servlet.getActiveCount()).append(",\n");
            json.append("\t\t").append("\"taskCount\": ").append(servlet.getTaskCount()).append(",\n");
            json.append("\t\t").append("\"completedTaskCount\": ").append(servlet.getCompletedTaskCount()).append(",\n");
            json.append("\t\t").append("\"rejectedTaskCount\": ").append(servlet.getRejectedTaskCount()).append(",\n");
            json.append("\t\t").append("\"instanceHours\": ").append(servlet.getInstanceHours()).append("\n");
            json.append("\t}").append(",\n");
        }
	
	if (servlet.getHttpCache() != null) {	    
	    json.append("\t").append("\"cache\": ").append("{").append("\n");
	    json.append("\t\t").append("\"size\": ").append(servlet.getHttpCacheSize()).append(",\n");
	    json.append("\t\t").append("\"hitRate\": ").append(servlet.getHttpCacheHitRate()).append("\n");
	    json.append("\t}").append(",\n");
	}
	
	json.append("\t").append("\"network\": ").append("{").append("\n");
	json.append("\t\t").append("\"outbound\": ").append(servlet.getOutgoingBandwidth()).append(",\n");
	json.append("\t\t").append("\"inbound\": ").append(servlet.getIncomingBandwidth()).append("\n");
	json.append("\t}").append(",\n");

	json.append("\t").append("\"throughput\": ").append("{").append("\n");
	json.append("\t\t").append("\"requestsPerSecond\": ").append(throughputRequests).append(",\n");
	json.append("\t\t").append("\"averageResponseTime\": ").append(servlet.getAverageResponseTime()).append(",\n");
	json.append("\t\t").append("\"averageResponseSize\": ").append(servlet.getAverageResponseSize()).append("\n");
	json.append("\t}").append(",\n");
	
	json.append("\t").append("\"errorRate\": ").append(errorRate).append("\n");

	json.append("}");
	return json.toString();
    }

    private String buildErrorStatsJsonResponse(String filter) {
	StringBuilder json = new StringBuilder();
	json.append("{").append("\n");	
	
	Map<Integer, Long> ce = servlet.getCumulativeErrors();
	
	ArrayList<Integer> keys = new ArrayList<>(ce.keySet());
	Collections.sort(keys);
	
	Pattern p = null;
	if (filter != null) {
	    p = Pattern.compile(filter, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	}
	
	String lineBreak = "";
	for (Integer sc : keys) {
	    if (sc > 399) {
		
		if (p != null) {
		    Matcher m = p.matcher(String.valueOf(sc));
		    if (!m.matches()) {
			continue;
		    }
		}
		
		json.append(lineBreak).append("\t").append("\"http").append(sc).append("\": ").append(ce.get(sc));
		lineBreak = ",\n";
	    }
	}
		
	json.append("\n}");
	return json.toString();
    }
    
    private String buildInitParamsJsonResponse() {
	StringBuilder json = new StringBuilder();
	json.append("{").append("\n");

	json.append("\t").append("\"initParams\": ").append("{").append("\n");
	
	Enumeration<String> initParamNames = servlet.getServletConfig().getInitParameterNames();
	ArrayList<String> orderedInitParamNames = new ArrayList<>();
	
	while (initParamNames.hasMoreElements()) {
	    orderedInitParamNames.add(initParamNames.nextElement());
	}
	Collections.sort(orderedInitParamNames);
	
	String sep = "";
	
	for (String name : orderedInitParamNames) {
	    String value = servlet.getServletConfig().getInitParameter(name);
	    json.append(sep);
	    json.append("\t\t").append("\"").append(name).append("\": ").append("\"").append(escapeString(value)).append("\"");
	    sep = (",\n");
	}
	json.append("\n");
	json.append("\t}").append("\n");

	json.append("}");
	return json.toString();
    }

    private String buildHttpCacheStatsResponse(String filter, String action) {
	StringBuilder json = new StringBuilder();
	json.append("{").append("\n");

	final HttpCache hc = servlet.getHttpCache();
	if (hc != null) {
	
	    json.append("\t").append("\"cacheEntries\": [\n");
	    
	    String sep = "";
	    Pattern p = null;
	    if (filter != null) {
		p = Pattern.compile(filter, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	    }
	    
	    final Collection<String> cacheKeys = hc.keySet();
	    
	    for (String key : cacheKeys) {
		
		final HttpCacheEntry entry = hc.get(key);		
		if (entry != null) {		    
		    
		    if (p != null) {
			Matcher m = p.matcher(key);
			if (!m.matches()) {
			    continue;
			}
		    }		  
		    
		    if ("delete".equals(action)) {
			if (!hc.remove(key)) {
			    continue;
			}
		    }
		    
		    json.append(sep);
		    json.append("\t\t{\n");
		    json.append("\t\t\t\"key\": \"").append(escapeString(entry.getKey())).append("\",\n");
		    if (entry.getHeader(HEADER_ETAG) != null) {
			json.append("\t\t\t\"eTag\": \"").append(escapeString(entry.getHeader(HEADER_ETAG))).append("\",\n");		    
		    }
		    json.append("\t\t\t\"hits\": ").append(entry.getHitCount()).append(",\n");
		    json.append("\t\t\t\"timestamp\": \"").append(escapeString(new Date(entry.getTimestamp()).toString())).append("\",\n");
		    json.append("\t\t\t\"expires\": \"").append(escapeString(new Date(entry.getExpires()).toString())).append("\",\n");
		    json.append("\t\t\t\"statusCode\": ").append(entry.getStatus()).append(",\n");
		    json.append("\t\t\t\"contentType\": \"").append(entry.getContentType()).append("\",\n");
		    if (entry.getContentEncoding() != null) {
			json.append("\t\t\t\"contentEncoding\": \"").append(entry.getContentEncoding()).append("\",\n");
		    }
		    json.append("\t\t\t\"contentLength\": ").append(entry.getContentLength());		    
		    
		    // Additional information
		    if (isDebugEnabled()) {			
			if (entry.getUserProperty("__producer") != null) {
			    json.append(",\n\t\t\t\"producer\": \"").append(escapeString(entry.getUserProperty("__producer"))).append("\""); 
			}
		    }
		    
		    json.append("\n\t\t}");
		    sep = ",\n";
		}
	    }
	    json.append("\n\t]\n");
	} else {
	    json.append("\t\"message\": \"").append(escapeString("No cache active")).append("\"");
	}

	json.append("}");
	return json.toString();
    }
    
    private String buildImageIORegistryJsonResponse() {
	StringBuilder json = new StringBuilder();
	json.append("{").append("\n");
	json.append("\t\"imageio\": [\n");

	HashSet<ImageReaderWriterSpi> listed = new HashSet<>();

	Iterator<ImageReaderSpi> iterIrs;
	try { // Ensure category is present
	    iterIrs = IIO_REGISTRY.getServiceProviders(ImageReaderSpi.class, true);
	    String sep = "";
	    while (iterIrs != null && iterIrs.hasNext()) {
		ImageReaderSpi spi = iterIrs.next();

		if (listed.contains(spi)) {
		    continue;
		}

		json.append(sep);
		appendImageSpi(json, spi);
		sep = ",\n";

		listed.add(spi);
	    }
	} catch (IllegalArgumentException e) {
	}

	json.append(",\n");

	Iterator<ImageWriterSpi> iterIws;
	try { // Ensure category is present
	    iterIws = IIO_REGISTRY.getServiceProviders(ImageWriterSpi.class, true);
	    String sep = "";
	    while (iterIws != null && iterIws.hasNext()) {
		ImageWriterSpi spi = iterIws.next();

		if (listed.contains(spi)) {
		    continue;
		}

		json.append(sep);
		appendImageSpi(json, spi);
		sep = ",\n";

		listed.add(spi);
	    }
	} catch (IllegalArgumentException e) {
	}

	json.append("\t]\n");
	json.append("}");
	return json.toString();
    }        
    
    private void appendImageSpi(StringBuilder json, ImageReaderWriterSpi spi) {
	json.append("\t\t{\n");
	json.append("\t\t\t\"pluginClassName\": \"").append(escapeString(spi.getClass().getName())).append("\",\n");
	json.append("\t\t\t\"description\": \"").append(escapeString(spi.getDescription(Locale.getDefault()))).append("\",\n");
	json.append("\t\t\t\"vendorName\": \"").append(escapeString(spi.getVendorName())).append("\",\n");
	json.append("\t\t\t\"version\": \"").append(escapeString(spi.getVersion())).append("\",\n");

	String formatName = Arrays.toString(spi.getFormatNames()).replace("[", "").replace("]", "").trim();
	json.append("\t\t\t\"formatNames\": \"").append(escapeString(formatName)).append("\",\n");

	String fileSuffixes = Arrays.toString(spi.getFileSuffixes()).replace("[", "").replace("]", "").trim();
	json.append("\t\t\t\"fileSuffixes\": \"").append(escapeString(fileSuffixes)).append("\",\n");

	String mimeTypes = Arrays.toString(spi.getMIMETypes()).replace("[", "").replace("]", "").trim();
	json.append("\t\t\t\"mimeTypes\": \"").append(escapeString(mimeTypes)).append("\"\n");

	json.append("\t\t}");
    }

    private static String formatUptime(long millis) {
	return String.format("%02dh %02dm %02ds", TimeUnit.MILLISECONDS.toHours(millis),
		TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
		TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private static String escapeString(String s) {
	final StringBuilder result = new StringBuilder();
	StringCharacterIterator iterator = new StringCharacterIterator(s);
	char character = iterator.current();
	while (character != StringCharacterIterator.DONE) {
	    switch (character) {
	    	case '\"':
		    result.append("\\\"");
		    break;
	    	case '\\':
		    result.append("\\\\");
		    break;
	    	case '/':
		    result.append("\\/");
		    break;
	    	case '\b':
		    result.append("\\b");
		    break;
	    	case '\f':
		    result.append("\\f");
		    break;
	    	case '\n':
		    result.append("\\n");
		    break;
	    	case '\r':
		    result.append("\\r");
		    break;
	    	case '\t':
		    result.append("\\t");
		    break;
	    	default:
		    //the char is not a special one
		    //add it to the result as is
		    result.append(character);
		    break;
	    }
	    character = iterator.next();
	}
	return result.toString();
    }    
    
}
