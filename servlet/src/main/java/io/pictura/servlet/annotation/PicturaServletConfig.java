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
package io.pictura.servlet.annotation;

import io.pictura.servlet.PicturaServlet;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.servlet.Servlet;
import javax.servlet.annotation.WebInitParam;

/**
 * Annotation used to configure a {@link PicturaServlet}.
 *
 * <p>
 * This annotation is processed by a {@link PicturaServlet} at init time when
 * the {@link Servlet#init(javax.servlet.ServletConfig)} methode is called for
 * the first time.</p>
 *
 * <p>
 * The order to lookup init parameters:
 * <ol>
 * <li>{@link WebInitParam} or <code>web.xml</code></li>
 * <li>External configuration file (see also {@link PicturaConfigFile})</li>
 * <li><b>{@link PicturaServletConfig}</b> annotation</li>
 * </ol>
 * </p>
 *
 * <p>
 * Usage:</p>
 * <pre>
 * &#64;WebServlet(...)
 * &#64;PicturaServletConfig(
 *     debug = true,
 *     resourceLocators = {"io.pictura.servlet.FileResourceLocator", "io.pictura.servlet.HttpResourceLocator"}
 * )
 * public class ImageServlet extends PicturaServlet {
 * }
 * </pre>
 *
 * @since 1.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface PicturaServletConfig {

    boolean debug() default false;

    boolean jmxEnabled() default true;

    boolean useContainerPool() default false;

    int corePoolSize() default 2;

    int maxPoolSize() default 4;

    int keepAliveTime() default 60000;

    int workerQueueSize() default 100;

    long workerTimeout() default 60000;

    String resourcePaths() default "";

    String[] resourceLocators() default {"io.pictura.servlet.FileResourceLocator"};

    String requestProcessor() default "";

    String requestProcessorFactory() default "";

    String[] requestProcessorStrategy() default {};

    long maxImageFileSize() default 1024 * 1024 * 2;

    long maxImageResolution() default 1024 * 1024 * 6;

    int maxImageEffects() default 5;

    String[] enabledInputImageFormats() default {};

    String[] enabledOutputImageFormats() default {};

    boolean enableBase64ImageEncoding() default false;

    String[] imageioSpiFilterInclude() default {};

    String[] imageioSpiFilterExclude() default {};

    String httpAgent() default "";

    int httpConnectTimeout() default 60000;

    int httpReadTimeout() default 60000;

    boolean httpFollowRedirects() default true;

    int httpMaxForwards() default -1;

    boolean httpsDisableCertificateValidation() default false;

    String httpProxyHost() default "";

    int httpProxyPort() default -1;

    String httpsProxyHost() default "";

    int httpsProxyPort() default -1;

    String urlConnectionFactory() default "";

    boolean statsEnabled() default false;

    String statsPath() default "/stats";

    String[] statsIpAddressMatch() default {"127.0.0.1", "::1"};

    boolean scriptEnabled() default true;

    String scriptPath() default "/js";

    boolean placeholderProducerEnabled() default false;

    String placeholderProducerPath() default "/ip";

    boolean enableQueryParams() default false;

    boolean enableContentDisposition() default false;

    boolean imageioUseCache() default false;

    String imageioCacheDir() default "";

    boolean headerAddContentLocation() default false;

    boolean headerAddTrueCacheKey() default false;

    boolean headerAddRequestId() default false;

    boolean headerAddNormalizedParams() default false;

    String cacheControlHandler() default "";

    int deflaterCompressionLevel() default 7;

    int deflaterCompressionMinSize() default 1024;

    boolean cacheEnabled() default false;

    String cacheClass() default "";

    int cacheCapacity() default 100;

    int cacheMaxEntrySize() default 1024 * 512;

    String cacheFile() default "";

    String errorHandler() default "";

}
