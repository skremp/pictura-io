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

import static io.pictura.servlet.RequestProcessor.HEADER_CACHECONTROL;
import static io.pictura.servlet.RequestProcessor.HEADER_EXPIRES;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Defines a basic HTTP cache entry.
 * 
 * @author Steffen Kremp
 * 
 * @see Serializable
 * @see HttpCache
 * 
 * @since 1.0
 */
public class HttpCacheEntry implements Serializable {
    
    private static final long serialVersionUID = 3761385199872993848L;
    
    private static final Pattern MAX_AGE = Pattern.compile(".*(max-age=[0-9]{1,}).*");

    private final long timestamp;
    private final String key;    
    
    private final long expires; // when the content expires
    private final byte[] content; // response content body
    
    private final int status;
    
    // Copy of the origin response headers
    private final HashMap<String, String> headers;
    
    // Internal user properties to hold additional information
    private final HashMap<String, String> userProperties;
    
    volatile long hitCount;
    
    HttpCacheEntry(String key, byte[] content, HttpServletRequest req, HttpServletResponse resp) {
	this.timestamp = System.currentTimeMillis();
	this.key = key;
	this.content = content != null ? content : new byte[0];
	this.status = resp != null ? resp.getStatus() : -1;
	
	this.headers = new HashMap<>();
        Date expiresDate = null;
        
        if (resp != null) {
            Collection<String> headerNames = resp.getHeaderNames();
            for (String name : headerNames) {
                headers.put(name, resp.getHeader(name));
            }

            String expiresString = resp.getHeader(HEADER_EXPIRES);
            if (expiresString != null) {
                try {
                    expiresDate = parseDate(expiresString);
                } catch (IllegalArgumentException ex) {
                    // Unable to parse the date
                }
            }

            // Let's try to calculate by max-age
            if (expiresDate == null) {
                String cacheControl = resp.getHeader(HEADER_CACHECONTROL);
                if (cacheControl != null) {
                    Matcher m = MAX_AGE.matcher(cacheControl.toLowerCase(Locale.ENGLISH));
                    if (m.matches()) {
                        String[] maxAge = m.group().split("=");
                        expiresDate = new Date(System.currentTimeMillis() + 
                                (Long.parseLong(maxAge[1]) * 1000));
                    }
                }
            }
        }
	this.expires = expiresDate == null ? -1 : expiresDate.getTime();
	this.userProperties = new HashMap<>();
    }
    
    void setUserProperty(String name, String value) {
	userProperties.put(name, value);
    }
    
    String getUserProperty(String name) {
	return userProperties.get(name);
    }
        
    /**
     * Returnst the timestamp in millis when this entry was created.
     * @return Creation timestamp in millis.
     */
    public long getTimestamp() {
	return timestamp;
    }    
    
    /**
     * Returns the uniqe cache key which is associated with this cache entry.
     * @return Cache key.
     */
    public String getKey() {
	return key;
    }
    
    /**
     * Returns the approximated hit count since the cache entry was created.
     * @return Hit count since the entry was created.
     */
    public long getHitCount() {
	return hitCount;
    }
    
    /**
     * A boolean indicating whether or not the cache entry is expired.
     * @return <code>true</code> if this cache entry is expired; otherwise
     * <code>false</code>.
     */
    public boolean isExpired() {
	return System.currentTimeMillis() > getExpires();
    }
    
    /**
     * Returns the expiration time of this cache entry.
     * @return Expiration time.
     */
    public long getExpires() {
	return expires;
    }
    
    /**
     * Gets the status code of the associated HTTP response.
     * @return Status code of the response.
     */
    public int getStatus() {
	return status;
    }        
    
    /**
     * Gets the raw content of the associated HTTP response.
     * @return Content of the response.
     */
    public byte[] getContent() {
	return content;
    }    

    /**
     * Gets the content length in bytes of the associated HTTP response.
     * @return Content length of the response.
     */
    public int getContentLength() {
	return getContent().length;
    }
    
    /**
     * Gets the content type (mime type) of the associated HTTP response.
     * @return Content type of the response.
     */
    public String getContentType() {
	return headers.get(RequestProcessor.HEADER_CONTTYPE);
    }
    
    /**
     * Gets the content encoding (e.g. gzip) of the associated HTTP response.
     * @return Content encoding of the response.
     */
    public String getContentEncoding() {
	return headers.get(RequestProcessor.HEADER_CONTENC);
    }
    
    /**
     * Returns an enumeration of all the header names the response contains. 
     * If the response has no headers, this method returns an empty enumeration.
     *
     * @return Enumeration of all the header names.
     */
    public Collection<String> getHeaderNames() {
	return headers.keySet();
    }
    
    /**
     * Returns the value of the specified response header
     * as a <code>String</code>. If the request did not include a header
     * of the specified name, this method returns <code>null</code>.
     * 
     * @param name A <code>String</code> specifying the header name.
     * 
     * @return <code>String</code> containing the value of the requested
     * header, or <code>null</code> if the request does not have a header of 
     * that name.    
     */
    public String getHeader(String name) {
	return headers.get(name);
    }

    @Override
    public String toString() {
	return "HttpCacheEntry ["
		+ "key=" + key
		+ ", age=" + (System.currentTimeMillis() - timestamp)
		+ ", expires=" + expires
		+ ", status=" + status
		+ ", type=" + getContentType() != null ? getContentType() : ""
		+ ", length=" + getContentLength()
		+ ", encoding=" + getContentEncoding() != null ? getContentEncoding() : ""
		+ "]";
    }        
    
    // org.apache.commons.httpclient.util.DateUtil
    
    /**
     * Date format pattern used to parse HTTP date headers in RFC 1123 format.
     */
    private static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /**
     * Date format pattern used to parse HTTP date headers in RFC 1036 format.
     */
    private static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";

    /**
     * Date format pattern used to parse HTTP date headers in ANSI C
     * <code>asctime()</code> format.
     */
    private static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";

    private static final Collection<String> DEFAULT_PATTERNS = Arrays.asList(
	    new String[]{PATTERN_ASCTIME, PATTERN_RFC1036, PATTERN_RFC1123});

    private static final Date DEFAULT_TWO_DIGIT_YEAR_START;

    static {
	Calendar calendar = Calendar.getInstance();
	calendar.set(2000, Calendar.JANUARY, 1, 0, 0);
	DEFAULT_TWO_DIGIT_YEAR_START = calendar.getTime();
    }

    /**
     * Parses a date value. The formats used for parsing the date value are
     * retrieved from the default http params.
     *
     * @param dateValue the date value to parse
     *
     * @return the parsed date
     *
     * @throws IllegalArgumentException if the value could not be parsed using
     * any of the supported date formats
     */
    private static Date parseDate(String dateValue) throws IllegalArgumentException {
	return parseDate(dateValue, null, null);
    }

    /**
     * Parses the date value using the given date formats.
     *
     * @param dateValue the date value to parse
     * @param dateFormats the date formats to use
     * @param startDate During parsing, two digit years will be placed in the
     * range <code>startDate</code> to <code>startDate + 100 years</code>. This
     * value may be <code>null</code>. When <code>null</code> is given as a
     * parameter, year <code>2000</code> will be used.
     *
     * @return the parsed date
     *
     * @throws IllegalArgumentException if none of the dataFormats could parse
     * the dateValue
     */
    private static Date parseDate(String dateValue, Collection<String> dateFormats,
	    Date startDate) throws IllegalArgumentException {

	if (dateValue == null) {
	    throw new IllegalArgumentException("dateValue is null");
	}
	if (dateFormats == null) {
	    dateFormats = DEFAULT_PATTERNS;
	}
	if (startDate == null) {
	    startDate = DEFAULT_TWO_DIGIT_YEAR_START;
	}
	// trim single quotes around date if present
	// see issue #5279
	if (dateValue.length() > 1
		&& dateValue.startsWith("'")
		&& dateValue.endsWith("'")) {
	    dateValue = dateValue.substring(1, dateValue.length() - 1);
	}

	SimpleDateFormat dateParser = null;
	Iterator<String> formatIter = dateFormats.iterator();

	while (formatIter.hasNext()) {
	    String format = formatIter.next();
	    if (dateParser == null) {
		dateParser = new SimpleDateFormat(format, Locale.US);
		dateParser.setTimeZone(TimeZone.getTimeZone("GMT"));
		dateParser.set2DigitYearStart(startDate);
	    } else {
		dateParser.applyPattern(format);
	    }
	    try {
		return dateParser.parse(dateValue);
	    } catch (ParseException pe) {
		// ignore this exception, we will try the next format
	    }
	}

	// we were unable to parse the date
	throw new IllegalArgumentException("Unable to parse the date " + dateValue);
    }

}
