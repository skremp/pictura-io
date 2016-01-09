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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A request processor implementation to provide the embedded PicturaJS client
 * side JavaScript.
 *
 * @see RequestProcessor
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
final class ScriptRequestProcessor extends RequestProcessor {

    // The response mime type
    private static final String JS_MIME_TYPE = "text/javascript";

    private final String script;

    ScriptRequestProcessor(String script) {
	this.script = script;
    }

    @Override
    public boolean isCacheable() {
	return true;
    }

    @Override
    protected void doProcess(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {

	final long now = System.currentTimeMillis();
	resp.setDateHeader(HEADER_DATE, now);

	final URL resURL = ScriptRequestProcessor.class.getResource("/io/pictura/js/" + script);
	if (resURL == null) {
	    doInterrupt(HttpServletResponse.SC_NOT_FOUND);
	    return;
	}

	final long lastModified = new Date(resURL.openConnection().getLastModified()).getTime();
	final long ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
	if (ifModifiedSince > -1L) {
	    if (lastModified <= ifModifiedSince) {
		doInterrupt(HttpServletResponse.SC_NOT_MODIFIED);
		return;
	    }
	}

	final byte[] content = getContent(resURL);

	final String eTag = getETag(content);
	final String ifNoneMatch = req.getHeader(HEADER_IFNONMATCH);
	if (eTag.equalsIgnoreCase(ifNoneMatch)) {
	    doInterrupt(HttpServletResponse.SC_NOT_MODIFIED);
	    return;
	}

	resp.setContentType(JS_MIME_TYPE);
	resp.setContentLength(content.length);
	resp.setCharacterEncoding("UTF-8");
	resp.setHeader(HEADER_ETAG, eTag);
	resp.setDateHeader(HEADER_LASTMOD, lastModified);

	doWrite(content, req, resp);
    }

    private byte[] getContent(URL u) throws IOException {
	try (InputStream is = u.openStream()) {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 16);

	    int len;
	    byte[] buf = new byte[1024 * 16];
	    while ((len = is.read(buf)) != -1) {
		bos.write(buf, 0, len);
	    }

	    return bos.toByteArray();
	}
    }

}
