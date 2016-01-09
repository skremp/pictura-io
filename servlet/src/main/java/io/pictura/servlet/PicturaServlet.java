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

import io.pictura.servlet.PicturaConfig.ConfigParam;
import static io.pictura.servlet.RequestProcessor.HEADER_ALLOW;
import static io.pictura.servlet.RequestProcessor.HEADER_CACHECONTROL;
import static io.pictura.servlet.RequestProcessor.HEADER_CONTTYPE;
import static io.pictura.servlet.RequestProcessor.HEADER_DATE;
import static io.pictura.servlet.RequestProcessor.HEADER_EXPIRES;
import static io.pictura.servlet.RequestProcessor.HEADER_PRAGMA;
import io.pictura.servlet.jmx.HttpCacheMXBean;
import io.pictura.servlet.jmx.PicturaServletMXBean;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.imageio.ImageIO;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import io.pictura.servlet.annotation.PicturaImageInterceptor;
import io.pictura.servlet.annotation.PicturaConfigFile;
import io.pictura.servlet.annotation.PicturaParamsInterceptor;
import io.pictura.servlet.annotation.PicturaThreadFactory;
import java.util.Locale;
import java.util.concurrent.ThreadFactory;

/**
 * Provides an implementation of an {@link HttpServlet} which is specializes in
 * delivering and modifying images.
 * <p>
 * The <code>PicturaServlet</code> supports async servlet execution according to
 * the Servlet API 3.0+ specification. For a high throughput it is strictly
 * recommended to enable async.</p>
 * <p>
 * <b>Important:</b> The servlet will use his own thread executor. This means
 * you have to use the servlet init parameters to configure the pool size as
 * well as the workers. You can access this thread pool (executor) via <code>
 * getServletContext().getAttribute("io.pictura.servlet.SERVLET_NAME.threadPool")
 * </code>, where <code>SERVLET_NAME</code> is the name of the servlet from
 * which you want to access the thread pool.
 * </p>
 *
 * @see HttpServlet
 * @see PicturaConfigFile
 * @see PicturaImageInterceptor
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public class PicturaServlet extends HttpCacheServlet {

    private static final long serialVersionUID = -4347115757605509782L;

    private static final Log LOG = Log.getLog(PicturaServlet.class);

    /**
     * Indicates that a local static variable is a {@link PicturaServlet} init
     * parameter name.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({FIELD})
    @Documented
    public @interface InitParam {
    }

    /**
     * The default pictura servlet info string.
     */
    static final String PICTURA_INFO = "PicturaIO/1";

    /**
     * Init parameter to set the (optional) external servlet configuration file.
     * If an external configuration file was set, the file is used to set the
     * configured servlet properties. However, if a property is configured twice
     * (via servlet init parameter and the external configuration file), the
     * servlet init parameter value will override the value which is configured
     * by the external configuration file.
     */
    @InitParam
    public static final String IPARAM_CONFIG_FILE = "configFile";

    /**
     * The global (servlet instance) debug parameter. If this is set to
     * <code>true</code>, the servlet enables automatically all available debug
     * output like additional debug response headers. The default value is
     * <code>false</code>. <b>Note</b>: On production systems, the debug options
     * should be always disabled.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/debug")
    public static final String IPARAM_DEBUG = "debug";

    /**
     * Servlet parameter to enable/disable the MXBean for the
     * <code>PicturaServlet</code> instance.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/jmx/enabled")
    public static final String IPARAM_JMX_ENABLED = "jmxEnabled";

    /**
     * Servlet parameter to set the core executor pool size.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/executor/core-pool-size")
    public static final String IPARAM_CORE_POOL_SIZE = "corePoolSize";

    /**
     * Servlet parameter to set the maximum executor pool size.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/executor/max-pool-size")
    public static final String IPARAM_MAX_POOL_SIZE = "maxPoolSize";

    /**
     * Servlet parameter to set the keep alive time in millis.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/executor/keep-alive-time")
    public static final String IPARAM_KEEP_ALIVE_TIME = "keepAliveTime";

    /**
     * Servlet parameter to set the worker queue size.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/executor/worker-queue-size")
    public static final String IPARAM_WORKER_QUEUE_SIZE = "workerQueueSize";

    /**
     * Servlet parameter to set the worker timeout in millis.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/executor/worker-timeout")
    public static final String IPARAM_WORKER_TIMEOUT = "workerTimeout";

    /**
     * Servlet parameter to set the allowed resource paths. It is possible to
     * define 1..n resource paths as comma separated list.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/resource-paths/path", repeated = true)
    public static final String IPARAM_RESOURCE_PATHS = "resourcePaths";

    /**
     * Servlet parameter to set the resource locators used by the servlet
     * instance. It is possible to define 1..n resource locators as comma
     * separated list. Any resource locator is defined by his full qualified
     * class name.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/resource-locators/class", repeated = true)
    public static final String IPARAM_RESOURCE_LOCATORS = "resourceLocators";

    /**
     * Servlet parameter to overwrite the default image request processor. A
     * user defined image request processor is defined by his full qualified
     * class name.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/request-processor/class")
    public static final String IPARAM_REQUEST_PROCESSOR = "requestProcessor";

    /**
     * Servlet parameter to define a custom request processor factory.
     *
     * @see ImageRequestProcessorFactory
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/request-processor/factory/class")
    public static final String IPARAM_REQUEST_PROCESSOR_FACTORY = "requestProcessorFactory";

    /**
     * Servlet parameter to define a custom request processor strategy.
     *
     * @see ImageRequestStrategy
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/request-processor/strategy/class", repeated = true)
    public static final String IPARAM_REQUEST_PROCESSOR_STRATEGY = "requestProcessorStrategy";

    /**
     * Servlet parameter to set the maximum allowed source image file size per
     * request.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/imageio/max-file-size")
    public static final String IPARAM_MAX_IMAGE_FILE_SIZE = "maxImageFileSize";

    /**
     * Servlet parameter to set the maximum allowed source image resolution per
     * request.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/imageio/max-resolution")
    public static final String IPARAM_MAX_IMAGE_RESOLUTION = "maxImageResolution";

    /**
     * Servlet parameter to set the maximum allowed image effects per request.
     * To disable image effects in genral, this value must be set to "0".
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/imageio/max-effects")
    public static final String IPARAM_MAX_IMAGE_EFFECTS = "maxImageEffects";

    /**
     * Servlet parameter to set the allowed image input formats. The value is a
     * comma separated list of image format names. Use this parameter to limit
     * the server side support by reading images.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/imageio/enabled-input-formats/format-name", repeated = true)
    public static final String IPARAM_ENABLED_INPUT_IMAGE_FORMATS = "enabledInputImageFormats";

    /**
     * Servlet parameter to set the allowed image output formats. The value is a
     * comma separated list of image format names. Use this parameter to limit
     * the server side support by writing images.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/imageio/enabled-output-formats/format-name", repeated = true)
    public static final String IPARAM_ENABLED_OUTPUT_IMAGE_FORMATS = "enabledOutputImageFormats";

    /**
     * Servlet parameter to enable or disable Base64 support. If this parameter
     * is set to <code>true</code> the servlet allows to write the output image
     * as Base64 encoded (UTF-8) string.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/imageio/enable-base64-image-encoding")
    public static final String IPARAM_ENABLE_BASE64_IMAGE_ENCODING = "enableBase64ImageEncoding";

    /**
     * An comma separated list of included Image IO service provider interfaces.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/imageio/spi-filter/include/class", repeated = true)
    public static final String IPARAM_IMAGEIO_SPI_FILTER_INCLUDE = "imageioSpiFilterInclude";

    /**
     * An comma separated list of excluded Image IO service provider interfaces.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/imageio/spi-filter/exclude/class", repeated = true)
    public static final String IPARAM_IMAGEIO_SPI_FILTER_EXCLUDE = "imageioSpiFilterExclude";

    /**
     * Servlet parameter to overwrite the default HTTP client user agent string.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/http/agent")
    public static final String IPARAM_HTTP_AGENT = "httpAgent";

    /**
     * Servlet parameter to overwrite the default HTTP client connect timeout.
     * The default connect timeout is set to 5.000 ms.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/http/connect-timeout")
    public static final String IPARAM_HTTP_CONNECT_TIMEOUT = "httpConnectTimeout";

    /**
     * Servlet parameter to overwrite the default HTTP client read timeout. The
     * default read timeout is set to 5.000 ms.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/http/read-timeout")
    public static final String IPARAM_HTTP_READ_TIMEOUT = "httpReadTimeout";

    /**
     * Servlet parameter to overwrite the default HTTP client follow redirects
     * behaviour. As default, the HTTP client will not follow redirects. To
     * disable this behaviour you need to set this to <code>true</code>.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/http/follow-redirects")
    public static final String IPARAM_HTTP_FOLLOW_REDIRECTS = "httpFollowRedirects";

    /**
     * The maximum number of possible forwards by proxies and/or gateways.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/http/max-forwards")
    public static final String IPARAM_HTTP_MAX_FORWARDS = "httpMaxForwards";

    /**
     * Servlet parameter to disable the HTTP client certificate validation. This
     * could be helpful in development environments. As default this is set to
     * <code>false</code>.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/https/disable-certificate-validation")
    public static final String IPARAM_HTTPS_DISABLE_CERTIFICATE_VALIDATION = "httpsDisableCertificateValidation";

    /**
     * Servlet parameter to set an optional proxy hostname which is to use if an
     * external resource is requested via an {@link URLConnection}.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/http/proxy/host")
    public static final String IPARAM_HTTP_PROXY_HOST = "httpProxyHost";

    /**
     * Servlet parameter to set an optional proxy port number which is to use if
     * an external resource is requested via an {@link URLConnection}.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/http/proxy/port")
    public static final String IPARAM_HTTP_PROXY_PORT = "httpProxyPort";

    /**
     * Servlet parameter to set an optional HTTPS proxy hostname which is to use if an
     * external resource is requested via an {@link URLConnection}.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/https/proxy/host")
    public static final String IPARAM_HTTPS_PROXY_HOST = "httpsProxyHost";

    /**
     * Servlet parameter to set an optional HTTPS proxy port number which is to use if
     * an external resource is requested via an {@link URLConnection}.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/https/proxy/port")
    public static final String IPARAM_HTTPS_PROXY_PORT = "httpsProxyPort";
    
    /**
     * Servlet parameter to set an optional factory class to create customized
     * {@link URLConnection}'s to fetch external resources. The implementation
     * of an {@link URLConnectionFactory} gets always the default HTTP init
     * parameters (if configured) as default {@link Properties} map.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/url/connection-factory/class")
    public static final String IPARAM_URL_CONNECTION_FACTORY = "urlConnectionFactory";

    /**
     * Servlet parameter to enable or disable statistic requests. The statistics
     * response could be use to monitor a single <code>PicturaServlet</code>
     * instance. As default, this value is set to <code>false</code>.
     *
     * @see #IPARAM_STATS_IP_ADDRESS_MATCH
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/stats/enabled")
    public static final String IPARAM_STATS_ENABLED = "statsEnabled";

    /**
     * Servlet parameter to set the path where the statistic requests are
     * available. The default statistics request path is <code>/stats</code>.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/stats/path")
    public static final String IPARAM_STATS_PATH = "statsPath";

    /**
     * Servlet parameter to define the IP address ranges (both IPv4 and IPv6 are
     * possible) who are allowed to send statistic requests to the servlet. The
     * value of the parameter is a comma separated list of IP addresses and or
     * IP ranges. As default, this is set to <code>127.0.0.1,::1</code>, to only
     * allow requests from <code>localhost</code>.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/stats/ip-address-match/address", repeated = true)
    public static final String IPARAM_STATS_IP_ADDRESS_MATCH = "statsIpAddressMatch";

    /**
     * Servlet parameter to enable or disable the embedded client side
     * JavaScript. As default this is set to <code>true</code>.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/script/enabled")
    public static final String IPARAM_SCRIPT_ENABLED = "scriptEnabled";

    /**
     * Servlet parameter to overwrite the default embedded JavaScript path. The
     * default path is set to <code>/js</code> and is relative to the servlet
     * path.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/script/path")
    public static final String IPARAM_SCRIPT_PATH = "scriptPath";

    /**
     * Servlet parameter to enable or disable the embedded image placeholder
     * producer. As default this is set to <code>false</code>.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/placeholder-producer/enabled")
    public static final String IPARAM_PLACEHOLDER_PRODUCER_ENABLED = "placeholderProducerEnabled";

    /**
     * Servlet parameter to overwrite the default embedded image placeholder
     * producer path. The default path is set to <code>/ip</code> and is
     * relative to the servlet path.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/placeholder-producer/path")
    public static final String IPARAM_PLACEHOLDER_PRODUCER_PATH = "placeholderProducerPath";

    /**
     * Servlet parameter to enable URL query parameters. If this parameter is
     * set to <code>true</code>, the underlying request processor will also
     * process all URL query parameters. As default this is set to
     * <code>false</code>.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/query-params/enable")
    public static final String IPARAM_ENABLE_QUERY_PARAMS = "enableQueryParams";

    /**
     * Servlet parameter to enable the optional content disposition response
     * header if the request was send with the <code>dl</code> URL query
     * parameter and an valid content filename. The default value is
     * <code>false</code>.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/content-disposition/enable")
    public static final String IPARAM_ENABLE_CONTENT_DISPOSITION = "enableContentDisposition";

    /**
     * Servlet parameter to overwrite the default used
     * {@link ImageIO#getUseCache()}.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/imageio/use-cache")
    public static final String IPARAM_IMAGEIO_USE_CACHE = "imageioUseCache";

    /**
     * Servlet parameter to overwrite the default used
     * {@link ImageIO#getCacheDirectory()}.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/imageio/cache-dir")
    public static final String IPARAM_IMAGEIO_CACHE_DIR = "imageioCacheDir";

    /**
     * Servlet parameter to append the origin content location in cases if the
     * origin image is a remote hosted image. As default this is set to
     * <code>false</code> to reduce the size of the response head.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/header/add-content-location")
    public static final String IPARAM_HEADER_ADD_CONTENT_LOCATION = "headerAddContentLocation";

    /**
     * Servlet parameter to append the calculated true cache key to the response
     * if the response is cacheable. As default this is set to
     * <code>false</code> to reduce the size of the response head. In cases if
     * the response is cacheable and this parameter is set to <code>true</code>,
     * the response head will contain the header with the name
     * <code>X-Pictura-TrueCacheKey</code>.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/header/add-true-cache-key")
    public static final String IPARAM_HEADER_ADD_TRUE_CACHE_KEY = "headerAddTrueCacheKey";

    /**
     * Servlet parameter to add always the unique Pictura request ID which can
     * be used for troubleshooting. As default this is set to
     * <code>false</code>.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/header/add-request-id")
    public static final String IPARAM_HEADER_ADD_REQUEST_ID = "headerAddRequestId";

    /**
     * Servlet parameter to define a cache control handler to set or overwrite
     * the default cache control response headers depending on the requested
     * resource path. The value could be a filename to a local cache control
     * configuration file or the class name of an custom handler which should be
     * use. If a filename is given and the file name is not an absolute path to
     * the configuration file, the servlet will look relativ to the WEB-INF
     * directory. If a custom implementation of the {@link CacheControlHandler}
     * interface is given, the implementation must implement the default class
     * constructor.
     *
     * @see CacheControlHandler
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/cache-control-handler/class")
    public static final String IPARAM_CACHE_CONTROL_HANDLER = "cacheControlHandler";

    /**
     * Servlet parameter to override the default deflater compression level in
     * cases of text responses (JS, CSS, ...).
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/deflater/compression-level")
    public static final String IPARAM_DEFLATER_COMPRESSION_LEVEL = "deflaterCompressionLevel";

    /**
     * Servlet parameter to specify the minimum amount of data before the output
     * is compressed. The default value is "1024".
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/deflater/compression-min-size")
    public static final String IPARAM_DEFLATER_COMPRESSION_MIN_SIZE = "deflaterCompressionMinSize";

    /**
     * Servlet parameter to enable HTTP caching. The default value is
     * <code>false</code>.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/cache/enabled")
    public static final String IPARAM_CACHE_ENABLED = "cacheEnabled";

    /**
     * Servlet parameter to specifiy a custom {@link HttpCache} implementation.
     * If not specified and {@link #IPARAM_CACHE_ENABLED} was set to
     * <code>true</code>, a default (embedded) LRU cache implementation will
     * use.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/cache/class")
    public static final String IPARAM_CACHE_CLASS = "cacheClass";

    /**
     * Servlet parameter to specify the cache capacity in cases if the default
     * {@link HttpCache} is used. The default value is <code>250</code>.
     * <p>
     * <b>Note:</b> this value is {@link HttpCache} dependent and this parameter
     * is only effected by the built-in cache implementation.
     * </p>
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/cache/capacity")
    public static final String IPARAM_CACHE_CAPACITY = "cacheCapacity";

    /**
     * Servlet parameter to specify the maximum size of a single cache entry in
     * cases if the default {@link HttpCache} is used. The default value is
     * <code>1048576</code> (1MB).
     * <p>
     * <b>Note:</b> this value is {@link HttpCache} dependent and this parameter
     * is only effected by the built-in cache implementation.
     * </p>
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/cache/max-entry-size")
    public static final String IPARAM_CACHE_MAX_ENTRY_SIZE = "cacheMaxEntrySize";

    /**
     * Servlet parameter to specify a local filename to persist and restore
     * caches in cases if the default {@link HttpCache} is used. If not set, the
     * cache will not persist and restore. As default there is no cache file
     * set.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/cache/file")
    public static final String IPARAM_CACHE_FILE = "cacheFile";

    /**
     * Servlet parameter to disable the <i>X-Powered-By</i> response header. Set
     * it's value to <code>false</code> to remove the header from any response.
     * The default value is <code>true</code>.
     */
    @InitParam
    @ConfigParam(xpath = "/pictura/x-powered-by")
    public static final String IPARAM_X_POWERED_BY = "xPoweredBy";

    /**
     * The default used thread pool core size if nothing else is defined. This
     * value is platform dependent. The core pool size is is calculated
     * according to <tt>Math.max(Runtime.getRuntime().availableProcessors(),
     * 2)</tt>.
     */
    protected static final int DEFAULT_CORE_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 2);

    /**
     * The default used thread pool maximum size if nothing else is defined.
     * This value is platform dependent. The maximum pool size is calculated
     * according to <tt>DEFAULT_CORE_POOL_SIZE * 2</tt>.
     */
    protected static final int DEFAULT_MAXIMUM_POOL_SIZE = DEFAULT_CORE_POOL_SIZE * 2;

    /**
     * The default used keep alive in millis if nothing else is defined.
     */
    protected static final int DEFAULT_KEEP_ALIVE_TIME = 60000;

    /**
     * The default used worker timeout in millis if nothing else is defined.
     */
    protected static final int DEFAULT_WORKER_TIMEOUT = 60000;

    /**
     * The default used worker queue size if nothing else is defined.
     */
    protected static final int DEFAULT_WORKER_QUEUE_SIZE = 100;

    /**
     * The default JavaScript path (if JavaScript is enabled).
     */
    protected static final String DEFAULT_SCRIPT_PATH = "/js";

    /**
     * The default statistics path (if statistics are enabled).
     */
    protected static final String DEFAULT_STATS_PATH = "/stats";

    /**
     * The default image placeholder producer path (if enabled).
     */
    protected static final String DEFAULT_PLACEHOLDER_PRODUCER_PATH = "/ip";

    /**
     * The default deflater compression min size in bytes.
     */
    protected static final int DEFAULT_DEFLATER_COMPRESSION_MIN_SIZE = 1024;

    /**
     * The default used HTTP user-agent. The default agent is normally equals to
     * the value of {@link #getServletInfo()}.
     */
    protected static final String DEFAULT_HTTP_AGENT = PICTURA_INFO;

    /**
     * The default allowed maximum source image file sitze.
     */
    protected static final int DEFAULT_MAX_IMAGE_FILE_SIZE = 1024 * 1024 * 2;

    /**
     * The default number of allwoed image effects per request.
     */
    protected static final int DEFAULT_MAX_IMAGE_EFFECTS = 5;
    
    /**
     * The default maximum source image resolution (px).
     */
    protected static final long DEFAULT_MAX_IMAGE_RESOLUTION = 6000000L;
    
    /**
     * The default maximum cache entry size in bytes.
     */
    protected static final int DEFAULT_CACHE_MAX_ENTRY_SIZE = 1024 * 1024;
    
    /**
     * The default cache capacity.
     */
    protected static final int DEFAULT_CACHE_CAPACITY = 250;
    
    /**
     * The default HTTP client maximum number of forwards.
     */
    protected static final int DEFAULT_HTTP_MAX_FORWARDS = -1;
    
    /**
     * The default HTTP client read timeout in millis.
     */
    protected static final int DEFAULT_HTTP_READ_TIMEOUT = 5000;
    
    /**
     * The default HTTP client connect timeout in millis.
     */
    protected static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5000;
    
    /**
     * Localhost IP address matches (IPv4 & IPv6).
     */
    private static final String[] LOCALHOST_IP_ADDRESSES = new String[]{
	"127.0.0.1", "::1"
    };

    // Internal servlet id
    private final String servletId;

    // Servlet start (init) timestamp
    private long startTime;

    // Flag which indicates whether the servlet is destroyed or not
    private volatile boolean alive;

    // Global debug flag
    private boolean debug;

    // X-Powered-By response header
    private boolean xPoweredBy;

    // Statistics
    private boolean statsEnabled;
    private String statsPath;
    private String[] statsIpAddressMatch;

    // Embedded JavaScript Resource
    private boolean scriptEnabled;
    private String scriptPath;

    // Embedded Image Placeholder Producer
    private boolean placeholderProducerEnabled;
    private String placeholderProducerPath;

    // True worker timeout in millis
    private long workerTimeout;

    // Handler to set the HTTP response cache control header
    private CacheControlHandler cacheControlHandler;

    // Executor services to handle the requests in our own thread pool
    // independent from the sever thread pool
    private ThreadPoolExecutor coreExecutor;
    private ThreadPoolExecutor statsExecutor;

    // The number of all rejected tasks since servlet start
    private volatile long rejectedTaskCount;

    // Rejected execution handler for this servlet instance 
    private final RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler() {

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
	    rejectedTaskCount++;
	}

    };

    // Resource locations
    private Pattern[] resPaths;
    private ResourceLocator[] resLocators;

    // Custom request processor types
    private String requestProcessorClass;
    private ImageRequestProcessorFactory requestProcessorFactory;
    private ImageRequestProcessorFactory requestProcessorStrategy;

    // The maxium image file size which the processor should provide to
    // process during a single request.
    private long maxImageFileSize;
    private long maxImageResolution;
    private int maxImageEffects;

    // Image related restrictions
    private String[] enabledInputImageFormats = new String[0];
    private String[] enabledOutputImageFormats = new String[0];
    private boolean enableBase64ImageEncoding;

    // The HTTP client (agent, timeouts, etc.)
    private String httpAgent;
    private int httpConnectTimeout;
    private int httpReadTimeout;
    private int httpMaxForwards;
    private boolean httpFollowRedirects;

    // Additional HTTPS client settings
    private boolean httpsDisableCertificateValidation;

    // HTTP proxy
    private String httpProxyHost;
    private int httpProxyPort;
    
    // HTTPS proxy
    private String httpsProxyHost;
    private int httpsProxyPort;

    // Flag to enable/disable query parameters in a processor
    private boolean enableQueryParams;

    // Flag to enable/disable the optional content disposition
    private boolean contentDisposition;

    // ImageIO
    private boolean imageioUseCache;
    private File imageioCacheDir;
    private String[] imageioSpiFilterInclude;
    private String[] imageioSpiFilterExclude;

    // Deflater
    private int deflaterCompressionLevel;
    private int deflaterCompressionMinSize;

    // Additional headers
    private boolean headerAddContentLocation;
    private boolean headerAddTrueCacheKey;
    private boolean headerAddRequestId;

    // Additional statistics
    private volatile long instanceMillis;
    private volatile long responseMillis;
    private volatile long outgoingBandwidth;
    private volatile long incomingBandwidth;

    // Counter map for response errors
    private ConcurrentHashMap<Integer, AtomicLong> errorCounter;

    // URL connection factory to fetch external resources
    private URLConnectionFactory urlConnectionFactory;

    // MXBean registration
    private ObjectName mxBeanServletObjName;
    private ObjectName mxBeanCacheObjName;

    /**
     * Creates a new <code>PicturaServlet</code> instance.
     */
    public PicturaServlet() {
	servletId = randomString(new Random(System.currentTimeMillis()),
		"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", 16);
    }

    @Override
    public String getServletInfo() {
	return PICTURA_INFO;
    }

    /**
     * @return A string indicates the implementation vendor.
     */
    public String getServletVendor() {
	return "";
    }

    /**
     * @return The servlet implementation version string.
     */
    public String getServletVersion() {
	return "1.0";
    }

    /**
     * @return The internal servlet ID from this instance.
     */
    public String getServletID() {
	return servletId;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
	alive = false;
	Log.setConfiguration(config.getServletContext());

	// Wrapp the given config
	try {
	    super.init(config = new PicturaServletConfigWrapper(this, config));
	} catch (IOException ex) {
	    throw new ServletException(ex);
	}		

	final ServletContext ctx = getServletContext();

        if (LOG.isInfoEnabled()) {
            LOG.info("PicturaIO Servlet Implementation Version " + Version.getVersionString());
        }
	
	debug = Boolean.parseBoolean(config.getInitParameter(IPARAM_DEBUG));
	xPoweredBy = tryParseBoolean(config.getInitParameter(IPARAM_X_POWERED_BY), true);

	// Check whether query parameters are enabled or disabled. As default
	// query parameters are disabled
	enableQueryParams = Boolean.parseBoolean(
		config.getInitParameter(IPARAM_ENABLE_QUERY_PARAMS));

	// Check whether JavaScript resources are enabled (default) or
	// disabled.
	final String initJsEnabled = config.getInitParameter(IPARAM_SCRIPT_ENABLED);
	if (scriptEnabled = (initJsEnabled == null || Boolean.parseBoolean(initJsEnabled))) {
	    scriptPath = config.getInitParameter(IPARAM_SCRIPT_PATH);
	    if (scriptPath == null) {
		scriptPath = DEFAULT_SCRIPT_PATH;
	    } else if (!scriptPath.startsWith("/")) {
		throw new ServletException("The JavaScript resource path must starts with \"/\".");
	    }
	}

	// Embedded image placeholder producer
	placeholderProducerEnabled = Boolean.parseBoolean(config.getInitParameter(
		IPARAM_PLACEHOLDER_PRODUCER_ENABLED));
	if (placeholderProducerEnabled) {
	    placeholderProducerPath = config.getInitParameter(IPARAM_PLACEHOLDER_PRODUCER_PATH);
	    if (placeholderProducerPath == null) {
		placeholderProducerPath = DEFAULT_PLACEHOLDER_PRODUCER_PATH;
	    } else if (!placeholderProducerPath.startsWith("/")) {
		throw new ServletException("The image placeholder producer path must starts with \"/\".");
	    }
	}

	// Read the configured worker timeout
	workerTimeout = tryParseLong(config.getInitParameter(IPARAM_WORKER_TIMEOUT),
		DEFAULT_WORKER_TIMEOUT);

	// Create the patterns for the allowed resource paths
	String strResPathConfig = config.getInitParameter(IPARAM_RESOURCE_PATHS);
	if (strResPathConfig != null && !strResPathConfig.isEmpty()) {
	    String[] paths = strResPathConfig.split(",");
	    resPaths = new Pattern[paths.length];
	    try {
		for (int i = 0; i < paths.length; i++) {
		    resPaths[i] = Pattern.compile(paths[i].replace("*", ".{0,}"));
		}
	    } catch (PatternSyntaxException ex) {
		throw new ServletException(ex);
	    }
	}

	// Initialize all supported and configured resource locators    
	String strResLocatorClazzes = config.getInitParameter(IPARAM_RESOURCE_LOCATORS);
	if (strResLocatorClazzes != null && !strResLocatorClazzes.isEmpty()) {
	    String[] clazzNames = strResLocatorClazzes.split(",");
	    ArrayList<ResourceLocator> rlArray = new ArrayList<>(clazzNames.length);
	    for (String clazzName : clazzNames) {
		final Object obj = createObjectInstance(clazzName);
		if (obj instanceof ResourceLocator) {
		    ResourceLocator rl = (ResourceLocator) obj;
		    if (rl instanceof FileResourceLocator) {
			((FileResourceLocator) rl).setServletContext(ctx);
		    }
		    rlArray.add(rl);
		} else {
		    throw new ServletException(
			    "The specified resource locator '" + clazzName
			    + "' is not a instance of "
			    + ResourceLocator.class.getName());
		}
	    }
	    resLocators = rlArray.toArray(new ResourceLocator[rlArray.size()]);
	} else {
	    // Secure by default. If nothing else is defined, we will only
	    // allow access to the local web application resources.
	    resLocators = new ResourceLocator[]{
		new FileResourceLocator(ctx)
	    };
	}

	// Read the configured max file size we should support to process
	String strMaxImageFileSize = config.getInitParameter(IPARAM_MAX_IMAGE_FILE_SIZE);
	if (strMaxImageFileSize != null && !strMaxImageFileSize.isEmpty()) {
	    strMaxImageFileSize = strMaxImageFileSize.toLowerCase(Locale.ENGLISH).trim();
	    if (strMaxImageFileSize.endsWith("k")) {
		strMaxImageFileSize = strMaxImageFileSize.substring(0, strMaxImageFileSize.length() - 1).trim();
		maxImageFileSize = tryParseLong(strMaxImageFileSize, -1L);
		maxImageFileSize = maxImageFileSize * 1024;
	    } else if (strMaxImageFileSize.endsWith("m")) {
		strMaxImageFileSize = strMaxImageFileSize.substring(0, strMaxImageFileSize.length() - 1).trim();
		maxImageFileSize = tryParseLong(strMaxImageFileSize, -1L);
		maxImageFileSize = maxImageFileSize * 1024 * 1024;
	    } else {
		maxImageFileSize = tryParseLong(strMaxImageFileSize, -1L);
	    }

	    // Check whether we have a valid value
	    if (maxImageFileSize < 1) {
		throw new ServletException("The max file size must be greather than 0 and a valid number.");
	    }
	} else {
	    maxImageFileSize = DEFAULT_MAX_IMAGE_FILE_SIZE; // Default 2MB
	}

	// Read the configured max image resolution (width x height)
	String strMaxImageResolution = config.getInitParameter(IPARAM_MAX_IMAGE_RESOLUTION);
	if (strMaxImageResolution != null && !strMaxImageResolution.isEmpty()) {
	    strMaxImageResolution = strMaxImageResolution.toLowerCase(Locale.ENGLISH).trim();
	    maxImageResolution = tryParseLong(strMaxImageResolution, 6000000L);
	} else {
	    maxImageResolution = DEFAULT_MAX_IMAGE_RESOLUTION; // Set default to 6MP if nothing else is defined
	}

	// Read the configured max image effects we would allow
	String strMaxImageEffects = config.getInitParameter(IPARAM_MAX_IMAGE_EFFECTS);
	if (strMaxImageEffects != null && !strMaxImageEffects.isEmpty()) {
	    strMaxImageEffects = strMaxImageEffects.toLowerCase(Locale.ENGLISH).trim();
	    maxImageEffects = tryParseInt(strMaxImageEffects, 5);
	} else {
	    maxImageEffects = DEFAULT_MAX_IMAGE_EFFECTS; // Set default to 5 if nothing else is defined
	}

	// Read the enabled image formats
	String strEnabledInputImageFormats = config.getInitParameter(IPARAM_ENABLED_INPUT_IMAGE_FORMATS);
	if (strEnabledInputImageFormats != null && !strEnabledInputImageFormats.isEmpty()) {
	    enabledInputImageFormats = strEnabledInputImageFormats.toLowerCase(Locale.ENGLISH).split(",");
	}
	String strEnabledOutputImageFormats = config.getInitParameter(IPARAM_ENABLED_OUTPUT_IMAGE_FORMATS);
	if (strEnabledOutputImageFormats != null && !strEnabledOutputImageFormats.isEmpty()) {
	    enabledOutputImageFormats = strEnabledOutputImageFormats.toLowerCase(Locale.ENGLISH).split(",");
	}
	String strEnableBase64ImageEncoding = config.getInitParameter(IPARAM_ENABLE_BASE64_IMAGE_ENCODING);
	if (strEnableBase64ImageEncoding != null) {
	    enableBase64ImageEncoding = Boolean.parseBoolean(strEnableBase64ImageEncoding);
	} else {
	    enableBase64ImageEncoding = true; // Default
	}

	// Read the spi filter config
	String strSpiFilterInclude = config.getInitParameter(IPARAM_IMAGEIO_SPI_FILTER_INCLUDE);
	if (strSpiFilterInclude != null && !strSpiFilterInclude.isEmpty()) {
	    imageioSpiFilterInclude = strSpiFilterInclude.split(",");
	}
	String strSpiFilterExclude = config.getInitParameter(IPARAM_IMAGEIO_SPI_FILTER_EXCLUDE);
	if (strSpiFilterExclude != null && !strSpiFilterExclude.isEmpty()) {
	    imageioSpiFilterExclude = strSpiFilterExclude.split(",");
	}

	// Get (optional) the user specified request processor class
	requestProcessorClass = config.getInitParameter(IPARAM_REQUEST_PROCESSOR);
	if (requestProcessorClass != null) {
	    requestProcessorClass = requestProcessorClass.trim();
	}

	// Get the optional request processor factory class
	final String requestProcessorFactoryClass = config.getInitParameter(IPARAM_REQUEST_PROCESSOR_FACTORY);
	if (requestProcessorFactoryClass != null) {
	    final Object obj = createObjectInstance(requestProcessorFactoryClass.trim());
	    if (obj instanceof ImageRequestProcessorFactory) {
		requestProcessorFactory = (ImageRequestProcessorFactory) obj;
	    } else {
		throw new ServletException(
			"The specified request processor factory is not a instance of "
			+ ImageRequestProcessorFactory.class.getName());
	    }
	}

	// Get the optional request processor strategy
	final String requestProcessorStrategyClasses = config.getInitParameter(IPARAM_REQUEST_PROCESSOR_STRATEGY);
	if (requestProcessorStrategyClasses != null && !requestProcessorStrategyClasses.isEmpty()) {
	    initImageStrategy(requestProcessorStrategyClasses);
	}

	// Get the optional URL connection factory
	final String urlConnectionFactoryClass = config.getInitParameter(IPARAM_URL_CONNECTION_FACTORY);
	if (urlConnectionFactoryClass != null) {
	    final Object obj = createObjectInstance(urlConnectionFactoryClass.trim());
	    if (obj instanceof URLConnectionFactory) {
		urlConnectionFactory = (URLConnectionFactory) obj;
	    } else {
		throw new ServletException(
			"The specified URL connection factory is not a instance of "
			+ URLConnectionFactory.class.getName());
	    }
	}

	// Read the HTTP agent for remote handling
	httpAgent = config.getInitParameter(IPARAM_HTTP_AGENT);
	if (httpAgent == null || httpAgent.isEmpty()) {
	    httpAgent = DEFAULT_HTTP_AGENT;
	}

	// Read the HTTP client timeout settings
	httpConnectTimeout = tryParseInt(config.getInitParameter(IPARAM_HTTP_CONNECT_TIMEOUT), DEFAULT_HTTP_CONNECT_TIMEOUT);
	httpReadTimeout = tryParseInt(config.getInitParameter(IPARAM_HTTP_READ_TIMEOUT), DEFAULT_HTTP_READ_TIMEOUT);

	// Max proxy/gateway forwards
	httpMaxForwards = tryParseInt(config.getInitParameter(IPARAM_HTTP_MAX_FORWARDS), DEFAULT_HTTP_MAX_FORWARDS);

	// HTTP client follow redirects
	httpFollowRedirects = Boolean.parseBoolean(config.getInitParameter(IPARAM_HTTP_FOLLOW_REDIRECTS));

	// HTTPS certificate validation
	httpsDisableCertificateValidation = Boolean.parseBoolean(config.getInitParameter(IPARAM_HTTPS_DISABLE_CERTIFICATE_VALIDATION));
        if (httpsDisableCertificateValidation) {
            LOG.warn("HTTPS certificate validation disabled");
        }
        
	// HTTP Proxy
	httpProxyHost = config.getInitParameter(IPARAM_HTTP_PROXY_HOST);
	httpProxyPort = tryParseInt(config.getInitParameter(IPARAM_HTTP_PROXY_PORT), -1);

	// HTTPS Proxy
	httpsProxyHost = config.getInitParameter(IPARAM_HTTPS_PROXY_HOST);
	httpsProxyPort = tryParseInt(config.getInitParameter(IPARAM_HTTPS_PROXY_PORT), -1);
	
	// ImageIO Cache
	imageioUseCache = Boolean.parseBoolean(config.getInitParameter(IPARAM_IMAGEIO_USE_CACHE));
	if (imageioUseCache) {
	    String strImageioCacheDir = config.getInitParameter(IPARAM_IMAGEIO_CACHE_DIR);
	    if (strImageioCacheDir != null) {
		imageioCacheDir = new File(strImageioCacheDir);
		if (!imageioCacheDir.exists()) {
		    throw new ServletException("The specified ImageIO cache directory does not exists.");
		}
		if (!imageioCacheDir.isDirectory()) {
		    throw new ServletException("The specified ImageIO cache directory must be a directory.");
		}
		if (!imageioCacheDir.canRead() || !imageioCacheDir.canWrite()) {
		    throw new ServletException("The servlet process needs rw permissons on the specified ImageIO cache directory.");
		}
	    }
	}

	// Additional response headers behaviour
	headerAddContentLocation = Boolean.parseBoolean(config.getInitParameter(IPARAM_HEADER_ADD_CONTENT_LOCATION));
	headerAddTrueCacheKey = Boolean.parseBoolean(config.getInitParameter(IPARAM_HEADER_ADD_TRUE_CACHE_KEY));
	headerAddRequestId = Boolean.parseBoolean(config.getInitParameter(IPARAM_HEADER_ADD_REQUEST_ID));

	// Optional content disposition option via query param
	contentDisposition = Boolean.parseBoolean(config.getInitParameter(IPARAM_ENABLE_CONTENT_DISPOSITION));

	// Deflater
	deflaterCompressionLevel = tryParseInt(config.getInitParameter(IPARAM_DEFLATER_COMPRESSION_LEVEL), -1);
	deflaterCompressionMinSize = tryParseInt(config.getInitParameter(IPARAM_DEFLATER_COMPRESSION_MIN_SIZE),
		DEFAULT_DEFLATER_COMPRESSION_MIN_SIZE);

	// Cache conrol handler
	initCacheControlHandler(config.getInitParameter(IPARAM_CACHE_CONTROL_HANDLER));

	// Create a new error counter for this servlet instance
	errorCounter = new ConcurrentHashMap<>();

	// Check whether statistics are enabled. If enabled we need to 
	// create an own thread pool executor for statistic requests only, to
	// separate statistic requests from normal image processing requests.
	if (statsEnabled = Boolean.parseBoolean(
		config.getInitParameter(IPARAM_STATS_ENABLED))) {

	    statsPath = config.getInitParameter(IPARAM_STATS_PATH);
	    if (statsPath == null) {
		statsPath = DEFAULT_STATS_PATH;
	    } else if (!statsPath.startsWith("/")) {
		throw new ServletException("Statistics path must starts with \"/\".");
	    }

	    String userStatsIpMatch = config.getInitParameter(IPARAM_STATS_IP_ADDRESS_MATCH);
	    if (userStatsIpMatch != null && !userStatsIpMatch.isEmpty()) {
		statsIpAddressMatch = userStatsIpMatch.split(",");
	    } else {
		statsIpAddressMatch = LOCALHOST_IP_ADDRESSES;
	    }

	    statsExecutor = new ThreadPoolExecutor(1, 2, 60, TimeUnit.SECONDS,
		    new ArrayBlockingQueue<Runnable>(10),
		    new ServerThreadFactory(Thread.MIN_PRIORITY + 1));
	}

	// Enable and initialize the HTTP cache if specified
	if (Boolean.parseBoolean(config.getInitParameter(IPARAM_CACHE_ENABLED))) {
	    initHttpCache(config.getInitParameter(IPARAM_CACHE_CLASS),
		    tryParseInt(config.getInitParameter(IPARAM_CACHE_CAPACITY), DEFAULT_CACHE_CAPACITY),
		    tryParseInt(config.getInitParameter(IPARAM_CACHE_MAX_ENTRY_SIZE), DEFAULT_CACHE_MAX_ENTRY_SIZE),
		    config.getInitParameter(IPARAM_CACHE_FILE));
	}

	// Create the image processing executor for this pictura servlet
	// instance.
	try {
	    int corePoolSize = tryParseInt(config.getInitParameter(
		    IPARAM_CORE_POOL_SIZE), DEFAULT_CORE_POOL_SIZE);
	    int maxPoolSize = tryParseInt(config.getInitParameter(
		    IPARAM_MAX_POOL_SIZE), DEFAULT_MAXIMUM_POOL_SIZE);
	    int keepAlive = tryParseInt(config.getInitParameter(
		    IPARAM_KEEP_ALIVE_TIME), DEFAULT_KEEP_ALIVE_TIME);

	    BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(
		    tryParseInt(config.getInitParameter(IPARAM_WORKER_QUEUE_SIZE),
			    DEFAULT_WORKER_QUEUE_SIZE));

	    coreExecutor = new ThreadPoolExecutor(
		    corePoolSize <= maxPoolSize ? corePoolSize : maxPoolSize,
		    maxPoolSize, keepAlive, TimeUnit.MILLISECONDS,
		    queue, createThreadFactory(), rejectedExecutionHandler);

	    coreExecutor.prestartCoreThread();

	    // Share the thread pool executor in the current context
	    getServletContext().setAttribute("io.pictura.servlet."
		    + getServletName() + ".threadPool", coreExecutor);
	} catch (IllegalArgumentException | NullPointerException ex) {
	    throw new ServletException(ex);
	}

	// Rescan for ImageIO plugins for "internal" mappings
	PicturaImageIO.scanForPlugins();

	// TODO: Define a parameter for general IO buffer sizes or calculate
	// the buffer size automatically, depending on the current max memory.
	// Initialize and register the MXBean for this servlet instance
	if (config.getInitParameter(IPARAM_JMX_ENABLED) == null
		|| Boolean.parseBoolean(config.getInitParameter(IPARAM_JMX_ENABLED))) {
	    initMXBeans();
	}

	startTime = System.currentTimeMillis();
	alive = true;
	
	// Do some validation checks
	if (getHttpCache() != null && cacheControlHandler == null) {
	    for (ResourceLocator rl : resLocators) {
		if (rl instanceof FileResourceLocator) {
		    LOG.warn("There was no cache-control handler found but HTTP " 
			    + "cache is enabled and at least one file resource locator is in use");
		    break;
		}
	    }
	}
        
        if (debug) {
            LOG.warn("PicturaIO is running in DEBUG MODE (to disable set init param \"debug\" to \"false\"!)");
        }
    }    

    private void initImageStrategy(String strategy) throws ServletException {
	final String[] clazzes = strategy.split(",");
	final ImageRequestStrategy[] strategies = new ImageRequestStrategy[clazzes.length];

	for (int i = 0; i < clazzes.length; i++) {
	    Object obj = createObjectInstance(clazzes[i].trim());
	    if (obj instanceof ImageRequestStrategy) {
		strategies[i] = (ImageRequestStrategy) obj;
	    } else {
		throw new ServletException("The specified image strategy \"" + clazzes[i]
			+ "\" is not a instance of " + ImageRequestStrategy.class.getName());
	    }
	}

	this.requestProcessorStrategy = new ImageRequestProcessorFactory() {

	    @Override
	    public ImageRequestProcessor createRequestProcessor(HttpServletRequest req)
		    throws ServletException {

		for (ImageRequestStrategy irs : strategies) {
		    if (irs.isPreferred(req)) {
			return irs.createRequestProcessor();
		    }
		}
		return null;
	    }
	};
    }

    private void initCacheControlHandler(String cacheControl) throws ServletException {
	if (cacheControl == null || cacheControl.isEmpty()) {
	    return; // do nothing if nothing is specified
	}

	// Case 1: XML based config file
	if (cacheControl.toLowerCase(Locale.ENGLISH).endsWith(".xml")) {
	    try {
		URL confPath = getServletContext().getResource("/WEB-INF/" + cacheControl);
		// attempt to retrieve from location other than local WEB-INF
		if (confPath == null) {
		    confPath = ClassLoader.getSystemResource(cacheControl);
		}
		if (confPath == null) {
		    File f = new File(cacheControl);
		    if (f.exists()) {
			confPath = f.toURI().toURL();
		    }
		}
		if (confPath != null) {
		    cacheControlHandler = new XMLCacheControlHandler(
			    new File(confPath.toURI()).getAbsolutePath());
		} else if ((new File(cacheControl)).exists()) {
		    cacheControlHandler = new XMLCacheControlHandler(cacheControl);
		} else {
		    throw new ServletException("Unable to find cache control config file at " + cacheControl);
		}
	    } catch (IOException | URISyntaxException e) {
		throw new ServletException(e);
	    }
	} // Case 2: Custom handler implementation
	else {
	    Object obj = createObjectInstance(cacheControl);
	    if (obj instanceof CacheControlHandler) {
		cacheControlHandler = (CacheControlHandler) obj;
	    } else {
		throw new ServletException("The specified cache control handler \"" + cacheControl
			+ "\" is not a instance of " + CacheControlHandler.class.getName());
	    }
	}
    }

    private void initHttpCache(String className, int capacity, int maxEntrySize,
	    String filename) throws ServletException {

	HttpCache cache;
	if (className != null) {
	    Object objHttpCache = createObjectInstance(className);
	    if (!(objHttpCache instanceof HttpCache)) {
		throw new ServletException("The specified HTTP cache \"" + className
			+ "\" is not a instance of " + HttpCache.class.getName());
	    }
	    cache = (HttpCache) objHttpCache;            
	} else {
	    cache = createDefaultHttpCache(capacity, maxEntrySize);
	}

	setHttpCache(cache);	

	if (filename != null) {
	    final File f = new File(filename);
	    if (f.exists()) {
		try {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Loading serialized cache from file \"" + f.getAbsolutePath() + "\"");
                    }
		    loadHttpCacheFromFile(f, cache);
		} catch (IOException ex) {
		    throw new ServletException(ex);
		}
	    }
	}
    }

    private void initMXBeans() throws ServletException {
	try {
	    ManagementFactory.getPlatformMBeanServer().registerMBean(new PicturaServletMXBean() {

		@Override
		public String getVersion() {
		    return Version.getVersionString();
		}

		@Override
		public String getServletName() {
		    return PicturaServlet.this.getServletName();
		}

		@Override
		public String getServletInfo() {
		    return PicturaServlet.this.getServletInfo();
		}

		@Override
		public boolean isDebugEnabled() {
		    return PicturaServlet.this.isDebugEnabled();
		}

		@Override
		public long getUptime() {
		    return PicturaServlet.this.getUptime();
		}

		@Override
		public boolean isAlive() {
		    return PicturaServlet.this.isAlive();
		}

		@Override
		public void setAlive(boolean alive) {
		    PicturaServlet.this.setAlive(alive);
		}

		@Override
		public int getActiveCount() {
		    return PicturaServlet.this.getActiveCount();
		}

		@Override
		public int getPoolSize() {
		    return PicturaServlet.this.getPoolSize();
		}

		@Override
		public long getTaskCount() {
		    return PicturaServlet.this.getTaskCount();
		}

		@Override
		public long getCompletedTaskCount() {
		    return PicturaServlet.this.getCompletedTaskCount();
		}

		@Override
		public long getRejecedTaskCount() {
		    return PicturaServlet.this.getRejectedTaskCount();
		}

		@Override
		public float getInstanceHours() {
		    return PicturaServlet.this.getInstanceHours();
		}

		@Override
		public float getAverageResponseTime() {
		    return PicturaServlet.this.getAverageResponseTime();
		}

		@Override
		public float getAverageResponseSize() {
		    return PicturaServlet.this.getAverageResponseSize();
		}

		@Override
		public long getOutboundTraffic() {
		    return PicturaServlet.this.getOutgoingBandwidth();
		}

		@Override
		public long getInboundTraffic() {
		    return PicturaServlet.this.getIncomingBandwidth();
		}

		@Override
		public float getErrorRate() {
		    Map<Integer, Long> err = PicturaServlet.this.getCumulativeErrors();
		    if (err.isEmpty()) {
			return .0f;
		    }

		    long sumErrors = 0L;
		    Iterator<Integer> iterErrors = err.keySet().iterator();

		    while (iterErrors.hasNext()) {
			sumErrors += err.get(iterErrors.next());
		    }

		    return (float) sumErrors / (float) PicturaServlet.this.getCompletedTaskCount();
		}

	    }, mxBeanServletObjName = new ObjectName(
		    "io.pictura.servlet.servlet:type=" + this.getClass().getSimpleName() + ",name="
		    + getServletName()));
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Registered new MBean " + mxBeanServletObjName.getCanonicalName());
            }

	    ManagementFactory.getPlatformMBeanServer().registerMBean(new HttpCacheMXBean() {

		@Override
		public int getSize() {
		    return getHttpCacheSize();
		}

		@Override
		public float getHitRate() {
		    return getHttpCacheHitRate();
		}

	    }, mxBeanCacheObjName = new ObjectName(
		    "io.pictura.servlet.servlet:type=" + HttpCache.class.getSimpleName() + ",name="
		    + getServletName()));
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Registered new MBean " + mxBeanCacheObjName.getCanonicalName());
            }

	} catch (MalformedObjectNameException | InstanceAlreadyExistsException |
		MBeanRegistrationException | NotCompliantMBeanException ex) {
	    throw new RuntimeException(ex);
	}
    }

    @Override
    public void destroy() {
	super.destroy();

	ObjectName[] mBeans = new ObjectName[]{
	    mxBeanServletObjName, mxBeanCacheObjName
	};

	for (ObjectName objName : mBeans) {
	    if (objName != null) {
		try {
		    ManagementFactory.getPlatformMBeanServer().unregisterMBean(objName);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Unregistered MBean " + objName.getCanonicalName());
                    }
		} catch (InstanceNotFoundException | MBeanRegistrationException ignore) {
		}
	    }
	}

	setAlive(false);

	// Shutdown the statistics executor
	if (statsExecutor != null && !statsExecutor.isShutdown()
		&& !statsExecutor.isTerminated() && !statsExecutor.isTerminating()) {
	    statsExecutor.shutdownNow();
	}

	// Shutdown the core image processor request executor from this
	// pictura servlet instance.
	if (coreExecutor != null && !coreExecutor.isShutdown()
		&& !coreExecutor.isTerminated() && !coreExecutor.isTerminating()) {
	    
	    LOG.info("Shutdown PicturaIO request executor");
	    coreExecutor.shutdownNow();
	}

	// Shutdown file system watcher service
	if (cacheControlHandler instanceof XMLCacheControlHandler) {
	    try {
		((XMLCacheControlHandler) cacheControlHandler).destroy();
	    } catch (IOException ex) {
		LOG.error("Destroy cache control handler failed. "
			+ "See nested exception for more details:", ex);
	    }
	}

	// If a cache is in use, persist
	if (getHttpCache() != null) {
	    String cacheFilename = getServletConfig().getInitParameter(IPARAM_CACHE_FILE);
	    if (cacheFilename != null) {
		try {
		    saveHttpCacheToFile(new File(cacheFilename), getHttpCache());
		} catch (IOException ex) {
		    LOG.error("Unable to store the current cache instance. "
			    + "See nested exception for more details", ex);
		}
	    }
	}

	// Clean-up resource lookup
	resPaths = null;
	resLocators = null;
	cacheControlHandler = null;
    }

    /**
     * Tests whether the global (for this instance) debug option is enabled.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>.
     */
    public final boolean isDebugEnabled() {
	return debug;
    }

    /**
     * Returns the complete uptime (include non alive times) from this servlet
     * instance in millis.
     *
     * @return The complete uptime in millis since start.
     */
    public final long getUptime() {
	return System.currentTimeMillis() - startTime;
    }

    /**
     * Tests if this servlet instance is alive. The servlet instance is alive if
     * it has been successfully initialized and started and has not yet
     * destroyed.
     *
     * @return <code>true</code> if this servlet instance is alive; otherwise
     * <tt>false</tt>.
     *
     * @see #setAlive(boolean)
     */
    public final boolean isAlive() {
	return alive;
    }

    /**
     * Sets the current servlet alive status to the specified value. If the
     * status is set to <code>false</code> the servlet will answer all requests
     * with an HTTP 503 error.
     * <p>
     * If the new alive status is <code>false</code> the method will also remove
     * all tasks from the core executor queue.
     * </p>
     *
     * @param alive The new servlet alive status.
     *
     * @see #isAlive()
     */
    public final synchronized void setAlive(boolean alive) {
	this.alive = alive;
	if (!alive) {
	    final BlockingQueue<Runnable> queue = coreExecutor.getQueue();
	    for (Runnable r : queue) {
		if (r instanceof RequestProcessor) {
		    try {
			RequestProcessor rp = (RequestProcessor) r;
			rp.doInterrupt(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		    } catch (IOException ignore) {
		    }
		}
		coreExecutor.remove(r);
	    }
	}
        if (LOG.isDebugEnabled()) {
            LOG.debug("Servlet status changed [" + (this.alive ? "alive" : "hold") + "]");
        }
    }

    /**
     * Returns the approximate number of threads that are actively executing
     * tasks.
     *
     * @return The number of threads.
     */
    public final int getActiveCount() {
	return coreExecutor != null ? coreExecutor.getActiveCount() : -1;
    }

    /**
     * Returns the current number of threads in the core pool.
     *
     * @return The number of threads.
     */
    public final int getPoolSize() {
	return coreExecutor != null ? coreExecutor.getPoolSize() : -1;
    }

    /**
     * Returns the current number of tasks in the queue. Retrieving the numbers
     * of tasks does not prevent queued tasks from executing.
     *
     * @return The number of tasks.
     */
    public final int getQueueSize() {
	return coreExecutor != null ? coreExecutor.getQueue().size() : -1;
    }

    /**
     * Returns the task queue used by this servlet. Access to the task queue is
     * intended primarily for debugging and monitoring. This queue may be in
     * active use. Retrieving the task queue does not prevent queued tasks from
     * executing.
     *
     * @return The task queue.
     *
     * @see Queue
     * @see Runnable
     */
    public final Queue<Runnable> getTaskQueue() {
	if (isAlive() && coreExecutor != null) {
	    return coreExecutor.getQueue();
	}
	return null;
    }

    /**
     * Returns the approximate total number of tasks that have ever been
     * scheduled for execution. Because the states of tasks and threads may
     * change dynamically during computation, the returned value is only an
     * approximation.
     *
     * @return The number of tasks.
     */
    public final long getTaskCount() {
	return coreExecutor != null ? coreExecutor.getTaskCount() : -1;
    }

    /**
     * Returns the approximate total number of tasks that have completed
     * execution by this instance. Because the states of tasks and threads may
     * change dynamically during computation, the returned value is only an
     * approximation, but one that does not ever decrease across successive
     * calls.
     *
     * @return The number of tasks.
     */
    public final long getCompletedTaskCount() {
	return coreExecutor.getCompletedTaskCount();
    }

    /**
     * Returns the total number of rejected tasks from this servlet instance. A
     * task will be reject if the "server" is temporarily overloaded and it was
     * not possible to create a new thread to process the request. In this case
     * the servlet will automatically answer with an 503 status code.
     *
     * @return The number of rejected tasks.
     */
    public final long getRejectedTaskCount() {
	return rejectedTaskCount;
    }

    /**
     * Returns the approximate process time of all tasks that have completed
     * execution by this instance. Because the states of tasks and threads may
     * change dynamically during computation, the returned value is only an
     * approximation, but one that does not ever decrease across successive
     * calls.
     *
     * @return The complete process time in hours.
     */
    public final float getInstanceHours() {
	return (instanceMillis / 1000f) / 3600f;
    }

    /**
     * Returns the approximate average response time of all tasks that have
     * completed execution by this instance. Because the states of tasks and
     * threads may change dynamically during computation, the returned value is
     * only an approximation, but one that does not ever decrease across
     * successive calls.
     *
     * @return The average response time in seconds.
     */
    public final float getAverageResponseTime() {
	return getCompletedTaskCount() > 0 ? ((responseMillis / 1000f) / getCompletedTaskCount()) : 0f;
    }

    /**
     * Returns the approximate average response size of all tasks that have
     * completed execution by this instance. Because the states of tasks and
     * threads may change dynamically during computation, the returned value is
     * only an approximation, but one that does not ever decrease across
     * successive calls.
     *
     * @return The average response size in bytes.
     */
    public final long getAverageResponseSize() {
	return getCompletedTaskCount() > 0 ? outgoingBandwidth / getCompletedTaskCount() : 0L;
    }

    /**
     * Returns the approximate outgoing bandwidth in bytes of all tasks that
     * have completed execution by this instance. Because the states of tasks
     * and threads may change dynamically during computation, the returned value
     * is only an approximation, but one that does not ever decrease across
     * successive calls.
     * <p>
     * Also the outgoing bandwidth is only the value of the body content how was
     * send to the clients. It does not contains the number of bytes are present
     * in the head.</p>
     *
     * @return The approximate outgoing bandwidth in bytes.
     */
    public final long getOutgoingBandwidth() {
	return outgoingBandwidth;
    }

    /**
     * Returns the approximate incoming bandwidth in bytes of all tasks that
     * have completed execution by this instance. Because the states of tasks
     * and threads may change dynamically during computation, the returned value
     * is only an approximation, but one that does not ever decrease across
     * successive calls.
     * <p>
     * Also the incoming bandwidth is only the value of the body content how was
     * received from external servers. It does not contains the number of bytes
     * are present in the head.</p>
     *
     * @return The approximate incoming bandwidth in bytes.
     */
    public long getIncomingBandwidth() {
	return incomingBandwidth;
    }

    /**
     * Returns the cumulative number of client and server errors grouped by the
     * error code (HTTP response status code) during the servlet was started.
     * <p>
     * If there was no error until now present, the method will return an empty
     * map.</p>
     *
     * @return The cumulative number of all errors by this servlet in an
     * unmodifiable map.
     */
    public final Map<Integer, Long> getCumulativeErrors() {
	Map<Integer, Long> copy = new HashMap<>();
	Enumeration<Integer> eec = errorCounter.keys();
	while (eec.hasMoreElements()) {
	    Integer key = eec.nextElement();
	    copy.put(key, errorCounter.get(key).get());
	}
	return Collections.unmodifiableMap(copy);
    }

    /**
     * Creates and returns a new <code>RequestProcessor</code> instance to
     * handle the request for the given servlet request.
     *
     * @param req The servlet request for which the new request processor should
     * be created.
     * @return A new request processor instance or <code>null</code> if there
     * was no possible processor found to create for the specified servlet
     * request.
     *
     * @throws ServletException If it was not able to create a new instance of a
     * custom defined request processor.
     *
     * @see RequestProcessor
     * @see HttpServletResponse
     */
    protected RequestProcessor createRequestProcessor(HttpServletRequest req)
	    throws ServletException {

	if (req != null) {

	    RequestProcessor internal = createInternalRequestProcessor(req);
	    if (internal != null) {
		return internal;
	    }

	    // If a request processor factory is defined, we will ask the
	    // factory at first to create a processor to handle the current 
	    // request. If the result of the factory is "null" we will continue
	    // as usual with the standard approach.
	    if (requestProcessorFactory != null) {
		final RequestProcessor rp = requestProcessorFactory.createRequestProcessor(req);
		if (rp != null) {
		    return rp;
		}
	    }

	    // Maybe we have a configured strategy
	    if (requestProcessorStrategy != null) {
		final RequestProcessor rp = requestProcessorStrategy.createRequestProcessor(req);
		if (rp != null) {
		    return rp;
		}
	    }

	    // The user has specified a custom request processor. We will try
	    // to create a new instance of this processor.
	    if (requestProcessorClass != null) {
		final Object obj = createObjectInstance(requestProcessorClass);
		if (obj instanceof ImageRequestProcessor) {
		    return (RequestProcessor) obj;
		} else {
		    throw new ServletException(
			    "The specified request processor is not a instance of "
			    + ImageRequestProcessor.class.getName());
		}
	    }

	    // There was no custom request processor defined; therefore we will
	    // use the default implementation of our image request processor.
	    return new ImageRequestProcessor(); // Use the default impl

	}
	return null;
    }

    // Tests the given request and if necessary creates an internal non
    // image request processor instance; otherwise returns "null".
    private RequestProcessor createInternalRequestProcessor(HttpServletRequest req)
	    throws ServletException {

	final String path = getRelativeRequestPath(req);

	// Special (internal only) request processor paths
	if (path != null) {
	    if (statsEnabled && path.startsWith(statsPath)) {
		// In cases of a statistics request we need to create a special
		// request processor which is able to fullfill this type of
		// request, but only if statistics are explicitly enabled by the 
		// user.
		req.setAttribute("io.pictura.servlet.IP_ADDRESS_MATCH", statsIpAddressMatch);
		return new StatsRequestProcessor(this);
	    }

	    if (scriptEnabled && path.startsWith(scriptPath)) {
		final String script = path.replaceFirst(scriptPath, "");
		return new ScriptRequestProcessor(script.startsWith("/")
			? script.substring(1) : script);
	    }

	    if (placeholderProducerEnabled && path.startsWith(placeholderProducerPath)) {
		return new PlaceholderRequestProcessor();
	    }
	}

	return null;
    }

    /**
     * Indicates whether the given request method is supported by this servlet
     * or not.
     *
     * @param method The method to test.
     *
     * @return <code>true</code> if the request method is supported; otherwise
     * <code>false</code>.
     */
    protected boolean isMethodSupported(String method) {
	return "GET".equalsIgnoreCase(method);
    }
    
    /**
     * Called by the servlet to handle a request. The method creates the needed
     * {@link RequestProcessor} which will handle the specified servlet request.
     * Also all statistics are measured and collected by this method. Therefore
     * this method should normally not override.
     *
     * @param method The request method (GET, POST, PUT, DELETE, HEAD, OPTIONS,
     * TRACE).
     * @param req An {@link HttpServletRequest} object that contains the request
     * the client has made of the servlet.
     * @param resp An {@link HttpServletResponse} object that contains the
     * response the servlet sends to the client
     *
     * @throws ServletException if an input or output error is detected when the
     * servlet handles the request.
     * @throws IOException if the request for could not be handled.
     */
    protected void doProcess(final String method, final HttpServletRequest req,
	    final HttpServletResponse resp) throws ServletException, IOException {

	final long serviceNanoTimestamp = System.nanoTime();

	// Create our own wrapped servlet request/response wrapper
	final PicturaServletRequest pReq = new PicturaServletRequest(req);
	final PicturaServletResponse pResp = new PicturaServletResponse(req, resp);

	// If the servlet is not yet alive send an error
	if (!isAlive()) {
	    throw new UnavailableException("Service temporarily not available", 60);
	}

	// Test if we support the request method
	if (!isMethodSupported(method)) {
	    pResp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	    return;
	}

	// Create a new processor for this request and execute it
	try {
	    final RequestProcessor rp = createRequestProcessor(pReq);

	    if (rp == null) {
		pResp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		return;
	    }

	    // Set the required request and response references to the processor
	    rp.setRequest(pReq);
	    rp.setResponse(pResp);

	    // Set other servlet instance related information to the processor
	    rp.setResourcePaths(resPaths);
	    if (rp.getResourceLocators() == null) {
		rp.setResourceLocators(resLocators);
	    }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing \"" + pReq.getMethod() + " " + rp.getRequestURI() 
                        + " " + pReq.getProtocol() + "\"");
            }
	    
	    // Set all necessary request specific attributes
	    if (rp instanceof ImageRequestProcessor) {

		// TRY TO PREVENT AN OUT OF MEMORY ERROR
		// An image processor needs a lot of free memory to fullfill the
		// request. Therefore we will check whether we have enough free
		// memory available to fullfill this request. If not, we reject
		// the request to prevent an out of memory error. But we can do
		// this only if the maximum image resolution is given. NOTE: This 
		// mechanism can bend forward it but not 100% prevent it. 
		if (maxImageResolution > 0) {
		    final long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		    final long freeMemory = Runtime.getRuntime().maxMemory() - usedMemory;

		    // At least, an ARGB image needs 4 bytes (32 bit) per pixel in his 
		    // image raster map. For example; a 4 MPixel raster image would
		    // need at least 16 Mbytes free main memory to process it.
		    // Additionally multiplied with an offset of 1.1.
		    final long minFreeMemory = (long) (1.1f * 4 * maxImageResolution);

		    if (freeMemory < minFreeMemory) {
			// It's better to send a 503 to "some" clients as to run
			// into an OOM error which means a 503 for ALL clients.
			rejectedTaskCount++;
			pResp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

			LOG.warn("Request rejected because of low memory detection. "
				+ "Increase heap space or reduce the amount of parallel image processors"
				+ " to prevent request rejections.");
			return;
		    }
		}

		// General attributes
		pReq.setAttribute("io.pictura.servlet.MAX_IMAGE_FILE_SIZE", maxImageFileSize);
		pReq.setAttribute("io.pictura.servlet.MAX_IMAGE_RESOLUTION", maxImageResolution);
		pReq.setAttribute("io.pictura.servlet.MAX_IMAGE_EFFECTS", maxImageEffects);
		pReq.setAttribute("io.pictura.servlet.ENABLED_INPUT_IMAGE_FORMATS", enabledInputImageFormats);
		pReq.setAttribute("io.pictura.servlet.ENABLED_OUTPUT_IMAGE_FORMATS", enabledOutputImageFormats);
		pReq.setAttribute("io.pictura.servlet.ENABLE_BASE64_IMAGE_ENCODING", enableBase64ImageEncoding);

		// HTTP client attributes
		pReq.setAttribute("io.pictura.servlet.HTTP_AGENT", httpAgent);
		pReq.setAttribute("io.pictura.servlet.HTTP_CONNECT_TIMEOUT", httpConnectTimeout);
		pReq.setAttribute("io.pictura.servlet.HTTP_READ_TIMEOUT", httpReadTimeout);
		pReq.setAttribute("io.pictura.servlet.HTTP_MAX_FORWARDS", httpMaxForwards);
		pReq.setAttribute("io.pictura.servlet.HTTP_FOLLOW_REDIRECTS", httpFollowRedirects);

		// HTTPS client attributes
		pReq.setAttribute("io.pictura.servlet.HTTPS_DISABLE_CERTIFICATE_VALIDATION", httpsDisableCertificateValidation);

		// HTTP Proxy
		if (httpProxyHost != null) {
		    pReq.setAttribute("io.pictura.servlet.HTTP_PROXY_HOST", httpProxyHost);
		    pReq.setAttribute("io.pictura.servlet.HTTP_PROXY_PORT", httpProxyPort);
		}
		
		// HTTPS Proxy
		if (httpsProxyHost != null) {
		    pReq.setAttribute("io.pictura.servlet.HTTPS_PROXY_HOST", httpsProxyHost);
		    pReq.setAttribute("io.pictura.servlet.HTTPS_PROXY_PORT", httpsProxyPort);
		}

		// ImageIO
		pReq.setAttribute("io.pictura.servlet.IMAGEIO_USE_CACHE", imageioUseCache);
		pReq.setAttribute("io.pictura.servlet.IMAGEIO_CACHE_DIR", imageioCacheDir);
		pReq.setAttribute("io.pictura.servlet.IMAGEIO_SPI_FILTER_INCLUDE", imageioSpiFilterInclude);
		pReq.setAttribute("io.pictura.servlet.IMAGEIO_SPI_FILTER_EXCLUDE", imageioSpiFilterExclude);

		// Optional attributes
		pReq.setAttribute("io.pictura.servlet.ENABLE_QUERY_PARAMS", enableQueryParams);

		// Optional params interceptor
		PicturaParamsInterceptor paramsInterceptor = getClass().getAnnotation(PicturaParamsInterceptor.class);
		if (paramsInterceptor != null) {
		    Class<? extends ParamsInterceptor> clazzes = paramsInterceptor.value();
		    ((IIORequestProcessor) rp).setParamsInterceptor((ParamsInterceptor) createObjectInstance(clazzes.getName()));
		}
		
		// Optional image interceptor
		PicturaImageInterceptor imageInterceptor = getClass().getAnnotation(PicturaImageInterceptor.class);
		if (imageInterceptor != null) {
		    Class<? extends ImageInterceptor>[] clazzes = imageInterceptor.value();
		    if (clazzes.length > 1) {
			ImageInterceptor[] chainInstances = new ImageInterceptor[clazzes.length];
			for (int i = 0; i < clazzes.length; i++) {
			    chainInstances[i] = (ImageInterceptor) createObjectInstance(clazzes[i].getName());
			}
			((IIORequestProcessor) rp).setImageInterceptor(new ImageInterceptorChain(chainInstances));
		    } else {
			((IIORequestProcessor) rp).setImageInterceptor((ImageInterceptor) createObjectInstance(clazzes[0].getName()));
		    }
		}
	    }

	    // Additional response headers behaviour which is valid for all
	    // types of processors	
	    pReq.setAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_LEVEL", deflaterCompressionLevel);
	    pReq.setAttribute("io.pictura.servlet.DEFLATER_COMPRESSION_MIN_SIZE", deflaterCompressionMinSize);
	    pReq.setAttribute("io.pictura.servlet.SERVICE_NANO_TIMESTAMP", serviceNanoTimestamp);
	    pReq.setAttribute("io.pictura.servlet.HEADER_ADD_CONTENT_LOCATION", headerAddContentLocation);
	    pReq.setAttribute("io.pictura.servlet.HEADER_ADD_TRUE_CACHE_KEY", headerAddTrueCacheKey);
	    pReq.setAttribute("io.pictura.servlet.HEADER_ADD_REQUEST_ID", headerAddRequestId);
	    pReq.setAttribute("io.pictura.servlet.DEBUG", debug);
	    pReq.setAttribute("io.pictura.servlet.URL_CONNECTION_FACTORY", urlConnectionFactory);

	    pResp.setHeader(HEADER_ALLOW, "GET");
	    if (pResp.getContentType() != null) {
		pResp.setHeader("X-Content-Type-Options", "nosniff");
	    }
	    if (xPoweredBy) {
		String xpb = pResp.getHeader("X-Powered-By");
		pResp.setHeader("X-Powered-By", (xpb != null && !xpb.isEmpty())
			? xpb + ", " + getServletInfo() : getServletInfo());
	    }

	    // Set some additional pre-processing
	    if (cacheControlHandler != null) {
		rp.setPreProcessor(new Runnable() {
		    @Override
		    public void run() {
			// Test if the user has specified his own cache control directive
			final String ccDirective = cacheControlHandler.getDirective(
				(rp instanceof ImageRequestProcessor)
					? ((ImageRequestProcessor) rp).getRequestedImage(pReq)
					: getRelativeRequestPath(pReq));
			pResp.setHeader(HEADER_CACHECONTROL, ccDirective);
		    }
		});
	    }

	    // Execute
	    doProcess(rp);
	} catch (IllegalArgumentException e) {
	    doHandleError(resp, e);
	}
    }

    /**
     * Executes the given request processor to handle the user request which is
     * specified by the processor.
     *
     * @param rp The request processor to execute.
     *
     * @throws ServletException if an input or output error is detected when the
     * servlet handles the request.
     * @throws IOException if the request for could not be handled.
     */
    protected void doProcess(final RequestProcessor rp) throws ServletException, IOException {

	final RequestProcessor crp = (getHttpCache() == null
		|| rp instanceof StatsRequestProcessor)
			? rp : createCacheRequestProcessor(rp);

	// Execute the processor for this request and wait for the result
	try {
	    if (crp.getRequest() == null || crp.getResponse() == null) {
		throw new IllegalStateException();
	    }

	    final ThreadPoolExecutor exec = getExecutor(crp);
	    if (exec == null || exec.isShutdown()) {
		crp.getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		throw new ServletException("Thread pool executor not initialized or shutdown.");
	    }

	    // If async servlet execution is supported by the servlet container
	    // and it is enabled in the deployment descriptior we will execute
	    // the image process completely async.                        
	    boolean asyncSupported = false;

	    // Ensure servlet API 3.0+ is available
	    if (getServletContext().getMajorVersion() >= 3) {
		try {
		    asyncSupported = crp.getRequest().isAsyncSupported();
		} catch (Exception | Error e) {
		}
	    }

	    // Only if async is supported we will execute this request async.
	    // NOTE: Not each server supports async servlet execution and
	    // per specification all associated filters must be support async,
	    // too.
	    if (asyncSupported) {
		AsyncContext asyncCtx = crp.getRequest().isAsyncStarted()
			? crp.getRequest().getAsyncContext() : crp.getRequest().startAsync();
		asyncCtx.setTimeout(workerTimeout);

		if (exec != statsExecutor) {
		    asyncCtx.addListener(new AsyncListener() {

			@Override
			public void onComplete(AsyncEvent event) throws IOException {
			    instanceMillis += (crp.getDuration() > -1 ? crp.getDuration() : 0);
			    responseMillis += System.currentTimeMillis() - crp.getTimestamp();
			    if (crp.getRequest().getAttribute("io.pictura.servlet.BYTES_READ") instanceof Long) {
				incomingBandwidth += (long) crp.getRequest().getAttribute("io.pictura.servlet.BYTES_READ");
			    }
			    if (crp.getRequest().getAttribute("io.pictura.servlet.BYTES_WRITTEN") instanceof Long) {
				outgoingBandwidth += (long) crp.getRequest().getAttribute("io.pictura.servlet.BYTES_WRITTEN");
			    }
			}

			@Override
			public void onTimeout(AsyncEvent event) throws IOException {
			}

			@Override
			public void onError(AsyncEvent event) throws IOException {
			}

			@Override
			public void onStartAsync(AsyncEvent event) throws IOException {
			}
		    });
		}

		crp.setAsyncContext(asyncCtx);

		exec.execute(crp);
	    } // If no async is supported or enabled we will execute the image
	    // processor concurrent (blocking mode).
	    else {
		Future<?> f = exec.submit(crp);
		f.get(workerTimeout, TimeUnit.MILLISECONDS);
		if (exec != statsExecutor) {
		    instanceMillis += (crp.getDuration() > -1 ? crp.getDuration() : 0);
		    responseMillis += System.currentTimeMillis() - crp.getTimestamp();
		    if (crp.getRequest().getAttribute("io.pictura.servlet.BYTES_READ") instanceof Long) {
			incomingBandwidth += (long) crp.getRequest().getAttribute("io.pictura.servlet.BYTES_READ");
		    }
		    if (crp.getRequest().getAttribute("io.pictura.servlet.BYTES_WRITTEN") instanceof Long) {
			outgoingBandwidth += (long) crp.getRequest().getAttribute("io.pictura.servlet.BYTES_WRITTEN");
		    }
		}
	    }

	} catch (RejectedExecutionException | ExecutionException |
		InterruptedException | TimeoutException | UnavailableException |
		IllegalStateException e) {
	    doHandleError(crp.getResponse(), e);
	} catch (RuntimeException | OutOfMemoryError e) {
	    doHandleError(crp.getResponse(), e);
	}
    }

    private void doHandleError(HttpServletResponse resp, Throwable e)
	    throws ServletException, IOException {

	String msg = null;

	// If not yet committed, we will send an error code depending
	// on the exception type.            
	int sc = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	if (e instanceof RejectedExecutionException) {
	    sc = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
	} else if (e instanceof TimeoutException || e instanceof InterruptedException) {
	    sc = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
	} else if (e instanceof IllegalArgumentException) {
	    sc = HttpServletResponse.SC_BAD_REQUEST;
	    msg = e.getMessage();
	}

	addError(sc);

	if (!resp.isCommitted()) {
	    resp.reset();
	    if (e instanceof RejectedExecutionException
		    || e instanceof TimeoutException
		    || e instanceof InterruptedException) {
		resp.setIntHeader("Retry-After", 30);
	    } else if (e instanceof UnavailableException) {
		UnavailableException ue = (UnavailableException) e;
		int s = ue.getUnavailableSeconds();
		if (s > 0) {
		    resp.setIntHeader("Retry-After", s);
		    resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		    return;
		}
	    }
	    if (msg != null) {
		resp.sendError(sc, msg);
	    } else {
		resp.sendError(sc);
	    }
	} else {
	    LOG.error("Service error while processing request", e);
	    throw new ServletException(e);
	}
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	doProcess("GET", req, resp);
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	doProcess("POST", req, resp);
    }

    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	doProcess("PUT", req, resp);
    }

    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	doProcess("DELETE", req, resp);
    }

    @Override
    protected final void doHead(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	doProcess("HEAD", req, resp);
    }

    @Override
    protected final void doOptions(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	doProcess("OPTIONS", req, resp);
    }

    @Override
    protected final void doTrace(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	doProcess("TRACE", req, resp);
    }

    /**
     * Returns a thread pool who is responsible to execute the specified request
     * processor.
     *
     * @param rp Request processor for which is the thread pool needed.
     *
     * @return The thread pool for the specified request processor.
     *
     * @see RequestProcessor
     * @see ThreadPoolExecutor
     */
    private ThreadPoolExecutor getExecutor(RequestProcessor rp) {
	if (rp instanceof StatsRequestProcessor) {
	    return statsExecutor;
	}
	return coreExecutor;
    }

    private ThreadFactory createThreadFactory() throws ServletException {
	PicturaThreadFactory ptf = getClass().getAnnotation(PicturaThreadFactory.class);
	if (ptf != null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Create new thread factory \"" + ptf.value().getName() + "\" "
                        + "for servlet with id[" + getServletID() + "]");
            }
	    return (ThreadFactory) createObjectInstance(ptf.value().getName());
	}
	return new ServerThreadFactory();
    }

    private void addError(int sc) {
	// Errors starts with 400 (client error bad request). If unknown (status
	// code is -1, we append the error as internal server error.
	if (sc > 399 || sc == -1) {
	    if (errorCounter.containsKey(sc == -1 ? 500 : sc)) {
		errorCounter.get(sc == -1 ? 500 : sc).incrementAndGet();
	    } else {
		errorCounter.put(sc == -1 ? 500 : sc, new AtomicLong(1));
	    }
	}
    }

    static Object createObjectInstance(String className) throws ServletException {
	try {
	    Class<?> clazz = Class.forName(className.trim(), true,
		    Thread.currentThread().getContextClassLoader());
	    Constructor<?> ctor = clazz.getConstructor();
	    return ctor.newInstance();
	} catch (ClassNotFoundException | NoSuchMethodException |
		InstantiationException | IllegalAccessException |
		InvocationTargetException ex) {
	    throw new ServletException("Unable to create a new instance of '"
		    + className + "'. See nested exception for more details:", ex);
	}
    }

    static String getRelativeRequestPath(HttpServletRequest req) {
	final String ctxPath = req.getContextPath();
	final String servlet = (ctxPath.equals("/") ? "" : ctxPath) + req.getServletPath();

	String path = req.getRequestURI();
	if (!servlet.isEmpty() && !servlet.equals("/") && !path.equals(servlet)
		&& path.startsWith(servlet)) {
	    path = path.replaceFirst(servlet, "");
	}

	return path;
    }

    protected static int tryParseInt(String s, int defaultValue) {
	if (s != null && !s.isEmpty()) {
	    try {
		return Integer.parseInt(s);
	    } catch (NumberFormatException e) {
		return defaultValue;
	    }
	}
	return defaultValue;
    }

    protected static long tryParseLong(String s, long defaultValue) {
	if (s != null && !s.isEmpty()) {
	    try {
		return Long.parseLong(s);
	    } catch (NumberFormatException e) {
		return defaultValue;
	    }
	}
	return defaultValue;
    }

    protected static float tryParseFloat(String s, float defaultValue) {
	if (s != null && !s.isEmpty()) {
	    try {
		return Float.parseFloat(s);
	    } catch (NumberFormatException ex) {
		return defaultValue;
	    }
	}
	return defaultValue;
    }

    protected static boolean tryParseBoolean(String s, boolean defaultValue) {
	if (s != null && !s.isEmpty()) {
	    return Boolean.parseBoolean(s);
	}
	return defaultValue;
    }

    private static String randomString(Random rnd, String characters, int length) {
	char[] text = new char[length];
	for (int i = 0; i < length; i++) {
	    text[i] = characters.charAt(rnd.nextInt(characters.length()));
	}
	return new String(text);
    }

    // Special URL query parameter names
    private static final String QUERY_PARAM_DEBUG = "debug";
    private static final String QUERY_PARAM_DOWNLOAD = "dl";
    private static final String QUERY_PARAM_DO_NOT_CACHE = "dnc";

    private static final Pattern P_CONTENT_DISPOSITION_VALUE = Pattern.compile("^[a-zA-Z0-9\\-_\\.]{1,128}$");

    // Response wrapper to inject error handling and cache control
    private final class PicturaServletResponse extends HttpServletResponseWrapper {

	private final HttpServletRequest request;

	private boolean error; // state error
	private boolean dnc; // do not track

	private String requestId; // the generated request ID

	private PicturaServletResponse(final HttpServletRequest request,
		final HttpServletResponse response) {

	    super(response);

	    this.request = request;
	    this.error = false;

	    setDoNotCache();
	}

	private void setContentDisposition() {
	    if (request.getParameter(QUERY_PARAM_DOWNLOAD) != null) {
		String filename = request.getParameter(QUERY_PARAM_DOWNLOAD);
                
                // Use the correct file name suffix for the current mime type
                String fns = PicturaImageIO.getImageReaderWriterMIMETypes().get(getHeader(HEADER_CONTTYPE));
                if (fns != null && !fns.isEmpty()) {
                    filename += "." + fns;
                }
                
		if (!filename.isEmpty() && P_CONTENT_DISPOSITION_VALUE.matcher(filename).matches()) {
		    setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		} else {
		    if (request.getParameter(QUERY_PARAM_DEBUG) != null || debug) {
			setHeader("X-Pictura-ContentDispositionErr", "Invalid attachment filename");
		    }
		}
	    }
	}

	private void setDoNotCache() {
	    if (isDoNotCacheRequest(request)) {
		super.setDateHeader(HEADER_EXPIRES, 0);
		super.setHeader(HEADER_CACHECONTROL, "private, max-age=0, no-cache");
		super.setHeader(HEADER_PRAGMA, "no-cache");
		dnc = true;
	    }
	}

	private boolean isDoNotCacheRequest(HttpServletRequest req) {
	    String dncValue = req.getParameter(QUERY_PARAM_DO_NOT_CACHE);
	    return (dncValue != null && !dncValue.equals("0") && !dncValue.equalsIgnoreCase("false"));
	}

	@Override
	public void setHeader(String name, String value) {
	    if (!error) {
		if (name.equals("X-Pictura-RequestId")) {
		    requestId = value;
		} else if (dnc || cacheControlHandler != null) {
		    if ((HEADER_CACHECONTROL.equalsIgnoreCase(name)
			    || HEADER_EXPIRES.equalsIgnoreCase(name))
			    && super.getHeader(HEADER_CACHECONTROL) != null) {
			return;
		    }

		    // Calculate and set the expires header before the cache control
		    // header will be set
		    if (HEADER_CACHECONTROL.equalsIgnoreCase(name) && value != null
			    && value.toLowerCase(Locale.ENGLISH).contains("max-age=")) {

			final long now = System.currentTimeMillis();

			String[] directives = value.split(",");
			for (String s : directives) {
			    if (s.trim().toLowerCase(Locale.ENGLISH).startsWith("max-age=")) {
				String[] maxAge = s.split("=");
				if (maxAge.length == 2) {
				    long maxAgeSeconds = tryParseLong(maxAge[1], -1);
				    if (maxAgeSeconds > -1) {
					setDateHeader(HEADER_DATE, now);
					setDateHeader(HEADER_EXPIRES, (now + (maxAgeSeconds * 1000)));
				    }
				}
				break;
			    }
			}
		    }
		}
	    }
	    super.setHeader(name, value);
	}

	@Override
	public void setDateHeader(String name, long date) {
	    if (cacheControlHandler != null && HEADER_EXPIRES.equalsIgnoreCase(name)
		    && super.getHeader(HEADER_CACHECONTROL) != null) {
		return;
	    }
	    super.setDateHeader(name, date);
	}

        @Override
        public void setContentType(String type) {
            super.setContentType(type);
            
            if (PicturaServlet.this.contentDisposition) {
                setContentDisposition();
            }
        }       
        
	@Override
	public void sendError(int sc) throws IOException {
	    sendError(sc, null);
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
	    // Clear all headers
	    error = true;
	    
	    if (sc != HttpServletResponse.SC_NOT_MODIFIED) {
                if (!isCommitted()) {
		    reset();

		    if (requestId != null) {
			setHeader("X-Pictura-RequestId", requestId);
		    }

		    // Append the reason if knowing
		    if (msg != null && !msg.isEmpty() && debug) {
			setHeader("X-Pictura-Err", msg);
		    }

		    setDateHeader(HEADER_DATE, System.currentTimeMillis());

		    if (xPoweredBy) {
			setHeader("X-Powered-By", getServletInfo());
		    }

		    if (sc == HttpServletResponse.SC_SERVICE_UNAVAILABLE) {
			setIntHeader("Retry-After", 30);
		    }                    
		}
                addError(sc);
	    }
	    super.sendError(sc, msg);
	}        
    }

    // Request wrapper to handle some user attributes
    private static final class PicturaServletRequest extends HttpServletRequestWrapper {

	private PicturaServletRequest(final HttpServletRequest request) {
	    super(request);
	}

	@Override
	public void setAttribute(String name, Object o) {
	    if (name.startsWith("io.pictura.servlet.") && getAttribute(name) == null) {
		super.setAttribute(name, o);
	    } else if (!name.startsWith("io.pictura.servlet.")) {
		super.setAttribute(name, o);
	    }
	}

    }

    // Wrapper to inject the servlet init parameters. The intention is to allow
    // to use system property templates.  
    // A template is defined as <code>${PROPERTY_NAME}</code>. All templates will
    // be replaced by the real system property value before a init parameter is
    // returned to the caller.
    private static final class PicturaServletConfigWrapper implements ServletConfig {

	private static final Pattern SYSPROP_PLACEHOLDER = Pattern.compile("\\$\\{([^$\\{\\}]){1,}\\}");

	private final ServletConfig config;
	private final PicturaConfig extConfig;

	PicturaServletConfigWrapper(Servlet servlet, ServletConfig config) throws IOException {
	    this.config = config;

	    PicturaConfigFile aConf = servlet.getClass().getAnnotation(PicturaConfigFile.class);

	    String cfgFile = aConf != null ? aConf.value() : getInitParameter1(IPARAM_CONFIG_FILE);
	    extConfig = cfgFile != null ? new PicturaConfig(servlet,
		    toFilePath(merge(cfgFile))) : null;
	}

	@Override
	public String getServletName() {
	    return config.getServletName();
	}

	@Override
	public ServletContext getServletContext() {
	    return config.getServletContext();
	}

	@Override
	public String getInitParameter(String name) {
	    String value = getInitParameter1(name);
	    if (value == null) {
		value = getInitParameter2(name);
	    }
	    return value != null ? merge(value) : null;
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
	    return config.getInitParameterNames();
	}

	private String getInitParameter1(String name) {
	    return config.getInitParameter(name);
	}

	private String getInitParameter2(String name) {
	    return extConfig != null ? extConfig.getConfigParam(name) : null;
	}

	private String merge(String value) {
	    if (value != null) {
		Matcher m = SYSPROP_PLACEHOLDER.matcher(value);

		int p = 0;
		while (m.find(p)) {
		    p = m.end();

		    String pn = value.substring(m.start() + 2, m.end() - 1);
		    String ph = "\\$\\{" + pn + "\\}";
		    String pd = pn.contains(":") ? pn.split(":")[1] : "";
		    String sp = System.getProperty(pn);

		    value = value.replaceAll(ph, sp != null ? sp : pd);
		}
	    }
	    return value;
	}

	private String toFilePath(String configFile) throws IOException {
	    URL confPath;
	    if (new File(configFile).exists()) {
		confPath = new File(configFile).toURI().toURL();
	    } else {
		File f = new File(getServletContext().getRealPath("/WEB-INF/" + configFile));
		confPath = f.exists() ? f.toURI().toURL()
			: getServletContext().getResource("/WEB-INF/" + configFile);
	    }
	    // attempt to retrieve from location other than local WEB-INF
	    if (confPath == null) {
		confPath = ClassLoader.getSystemResource(configFile);
	    }
	    if (confPath != null) {
		try {
		    return new File(confPath.toURI()).getAbsolutePath();
		} catch (URISyntaxException ex) {
		    throw new IOException(ex);
		}
	    }
	    throw new IOException("External config file at \"" + configFile + "\" not found");
	}
    }
    
}
