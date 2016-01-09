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

import static io.pictura.servlet.RequestProcessor.HEADER_ACCEPT;
import static io.pictura.servlet.RequestProcessor.HEADER_ACCEPTENC;
import static io.pictura.servlet.RequestProcessor.HEADER_IFMODSINCE;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * <p>
 * An <code>URLConnectionFactory</code> instance can be used to create
 * {@link URLConnection} objects.</p>
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public interface URLConnectionFactory {

    /**
     * Creates a new {@link URLConnection} with the specified connection
     * properties.
     *
     * @param url The target URL to connect to.
     * @param props The default connection properties.
     *
     * @return The new URL connection object.
     *
     * @throws IOException if an I/O exception occurs.
     * @throws IllegalArgumentException if the specified arguments are not
     * value; e.g. the default connection properties are <code>null</code>.
     *
     * @see URL
     * @see Properties
     */
    public URLConnection newConnection(URL url, Properties props) throws IOException;

    /**
     * Default implementation of the factory interface. Used is nothing else was
     * specified in the servlet context.
     *
     * @see URLConnectionFactory
     */
    public static final class DefaultURLConnectionFactory implements URLConnectionFactory {
        
	private static final URLConnectionFactory FACTORY = new DefaultURLConnectionFactory();

	/**
	 * @return Default implementation of the context URL connection factory.
	 * @see URLConnectionFactory
	 */
	public static URLConnectionFactory getDefault() {
	    return FACTORY;
	}

	// Prevent instantiation
	private DefaultURLConnectionFactory() {
	}

	@Override
	public URLConnection newConnection(URL url, Properties props) throws IOException {
	    if (props == null) {
		throw new IllegalArgumentException("URL connection properties must be not null");
	    }

	    // Optional proxy settings
	    Proxy proxy = getProxy(url, props);

	    URLConnection con;
	    if (url.getProtocol().startsWith("http")) {
		con = (HttpURLConnection) (proxy != null ? url.openConnection(proxy) : url.openConnection());
		con.setAllowUserInteraction(false);

		HttpURLConnection httpCon = (HttpURLConnection) con;
		if (httpCon instanceof HttpsURLConnection
			&& props.get("io.pictura.servlet.HTTPS_DISABLE_CERTIFICATE_VALIDATION") instanceof Boolean
			&& (Boolean) props.get("io.pictura.servlet.HTTPS_DISABLE_CERTIFICATE_VALIDATION")) {

		    final HttpsURLConnection httpsCon = (HttpsURLConnection) httpCon;

		    SSLContext sslCtx;
		    try {
			sslCtx = SSLContext.getInstance("SSL");
			sslCtx.init(null, new TrustManager[]{new X509NoValidationTrustManager()}, new SecureRandom());
		    } catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new RuntimeException("Internal server error", e);
		    }

		    httpsCon.setSSLSocketFactory(sslCtx.getSocketFactory());
		    httpsCon.setHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String string, SSLSession ssls) {
			    return true; // all hosts valid
			}
		    });
		}
		httpCon.setRequestMethod("GET");
		if (props.get("io.pictura.servlet.HTTP_FOLLOW_REDIRECTS") instanceof Boolean) {
		    httpCon.setInstanceFollowRedirects((Boolean) props.get("io.pictura.servlet.HTTP_FOLLOW_REDIRECTS"));
		}
	    } else {
		con = (proxy != null ? url.openConnection(proxy) : url.openConnection());
	    }

	    // Generic properties
	    if (props.get("io.pictura.servlet.HTTP_CONNECT_TIMEOUT") instanceof Integer) {
		con.setConnectTimeout((Integer) props.get("io.pictura.servlet.HTTP_CONNECT_TIMEOUT"));
	    }
	    if (props.get("io.pictura.servlet.HTTP_READ_TIMEOUT") instanceof Integer) {
		con.setReadTimeout((Integer) props.get("io.pictura.servlet.HTTP_READ_TIMEOUT"));
	    }
	    con.setUseCaches(true);

	    // Override the default Java user agent string
	    if (props.get("io.pictura.servlet.HTTP_AGENT") instanceof String) {
		con.setRequestProperty(RequestProcessor.HEADER_USERAGENT,
			(String) props.get("io.pictura.servlet.HTTP_AGENT"));
	    }

	    // Maximum possible proxy/gateway forwards
	    if ((props.get("io.pictura.servlet.HTTP_MAX_FORWARDS") instanceof Integer)
		    && (int) props.get("io.pictura.servlet.HTTP_MAX_FORWARDS") > -1) {
		con.setRequestProperty("Max-Forwards", String.valueOf(props.get(
			"io.pictura.servlet.HTTP_MAX_FORWARDS")));
	    }

	    // For images it makes no sense to accept gzip or deflate :)
	    con.setRequestProperty(HEADER_ACCEPTENC, "identity");

	    // Tell the remote server which image format we accept
	    con.setRequestProperty(HEADER_ACCEPT,
		    PicturaImageIO.getRemoteClientAcceptHeader());

	    // Append if-modified-since header if present
	    String ifModifiedSince = props.getProperty(HEADER_IFMODSINCE);
	    if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
		con.setRequestProperty(HEADER_IFMODSINCE, ifModifiedSince);
	    }

	    return con;
	}

	/**
	 * Returns a new proxy instance if a proxy is configured.
	 * <p>
	 * If there is an HTTP proxy is defined but no explicit HTTPS proxy, the
	 * HTTP proxy settings are also used to create a new proxy instance to
	 * connect to an HTTPS url.
	 * </p>
	 *
	 * @param url The URL to connect to.
	 * @param props The properties map.
	 *
	 * @return A new proxy instance if defined; otherwise <code>null</code>.
	 *
	 * @see PicturaServlet#IPARAM_HTTP_PROXY_HOST
	 * @see PicturaServlet#IPARAM_HTTP_PROXY_PORT
	 * @see PicturaServlet#IPARAM_HTTPS_PROXY_HOST
	 * @see PicturaServlet#IPARAM_HTTPS_PROXY_PORT
	 *
	 * @see Proxy
	 */
	public Proxy getProxy(URL url, Properties props) {

	    final String httpProxyHost = getHttpProxyHost(props);
	    final String httpsProxyHost = getHttpsProxyHost(props);

	    Proxy proxy = null;
	    if (httpProxyHost != null || httpsProxyHost != null) {

		final int httpProxyPort = getHttpProxyPort(props);
		final int httpsProxyPort = getHttpsProxyPort(props);

		if (("https".equals(url.getProtocol()) || "ftps".equals(url.getProtocol()))
			&& httpsProxyPort > -1) {

		    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
			    httpsProxyHost, httpsProxyPort));
		    
		} else if (httpProxyHost != null && httpProxyPort > -1) {

		    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
			    httpProxyHost, httpProxyPort));
		}
	    }
	    return proxy;
	}

	private String getHttpProxyHost(Properties props) {
	    Object o = getPropertyValue("io.pictura.servlet.HTTP_PROXY_HOST", 
		    "http.proxyHost", props);
	    return (o instanceof String) ? (String) o : null;
	}

	private int getHttpProxyPort(Properties props) {
	    Object o = getPropertyValue("io.pictura.servlet.HTTP_PROXY_PORT", 
		    "http.proxyPort", props);
	    return (o instanceof Integer) ? (int) o : -1;
	}

	private String getHttpsProxyHost(Properties props) {
	    Object o = getPropertyValue("io.pictura.servlet.HTTPS_PROXY_HOST", 
		    "https.proxyHost", props);
	    return (o instanceof String) ? (String) o : null;
	}

	private int getHttpsProxyPort(Properties props) {
	    Object o = getPropertyValue("io.pictura.servlet.HTTPS_PROXY_PORT", 
		    "https.proxyPort", props);
	    return (o instanceof Integer) ? (int) o : -1;
	}

	private Object getPropertyValue(String key, String fallbackKey, Properties props) {
	    if (props.get(key) != null) {
		return props.getProperty(key);
	    }
	    // Fallback
	    return System.getProperty(fallbackKey) != null
		    ? System.getProperty(fallbackKey)
		    : System.getenv(fallbackKey);
	}

	private static final class X509NoValidationTrustManager implements X509TrustManager {

	    @Override
	    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	    }

	    @Override
	    public void checkClientTrusted(X509Certificate[] certs, String authType) {
	    }

	    @Override
	    public void checkServerTrusted(X509Certificate[] certs, String authType) {
	    }
	}

    }

}
