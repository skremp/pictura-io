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

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LoggingMXBean;
import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple embedded unterdow servlet container to deploy and run the pictura
 * servlet as standalone service on a specified port at localhost.
 *
 * @author Steffen Kremp
 */
public final class UndertowPicturaServer {

    private static final Log LOG = Log.getLog(UndertowPicturaServer.class);

    private static final String HOST = "localhost";
    private static final int PORT = 8084;

    private static final Level LOG_LEVEL = Level.FINER;

    private static Undertow undertow;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-7s [%2$s] %5$s%6$s%n");

        Locale.setDefault(Locale.ENGLISH);

        ResourceManager rm = PicturaServletIT.getResourceManager();

        if (LOG.isInfoEnabled()) {
            LOG.info("Xmx" + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "m");
            LOG.info("Set resource base to \"" + PicturaServletIT.getResourceBase() + "\"");
        }

        Properties props = new Properties();
        props.load(PicturaServletIT.class.getResourceAsStream("/properties/test-runtime.properties"));

        LibWebp.addLibraryPath(System.getProperty("java.io.tmpdir"));
        LibWebp.addLibWebp(props.getProperty("it.libwebp"));

        ServletInfo servletInfo = new ServletInfo("pictura-dev", InterceptableServlet.class);
        servletInfo.setAsyncSupported(true);
        servletInfo.setLoadOnStartup(1);

        servletInfo.addInitParam(PicturaServlet.IPARAM_DEBUG, "true");
        servletInfo.addInitParam(PicturaServlet.IPARAM_JMX_ENABLED, "true");
        servletInfo.addInitParam(PicturaServlet.IPARAM_HEADER_ADD_TRUE_CACHE_KEY, "true");
        servletInfo.addInitParam(PicturaServlet.IPARAM_USE_CONTAINER_POOL, "true");
        servletInfo.addInitParam(PicturaServlet.IPARAM_STATS_ENABLED, "true");
        servletInfo.addInitParam(PicturaServlet.IPARAM_PLACEHOLDER_PRODUCER_ENABLED, "true");
        servletInfo.addInitParam(PicturaServlet.IPARAM_HTTP_MAX_FORWARDS, "2");
        servletInfo.addInitParam(PicturaServlet.IPARAM_HTTPS_DISABLE_CERTIFICATE_VALIDATION, "true");
        servletInfo.addInitParam(PicturaServlet.IPARAM_ENABLE_CONTENT_DISPOSITION, "true");
        servletInfo.addInitParam(PicturaServlet.IPARAM_ENABLE_BASE64_IMAGE_ENCODING, "true");
        servletInfo.addInitParam(PicturaServlet.IPARAM_HTTP_AGENT, "TEST");
        servletInfo.addInitParam(PicturaServlet.IPARAM_IMAGEIO_SPI_FILTER_EXCLUDE,
                "com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi");
        servletInfo.addInitParam(PicturaServlet.IPARAM_RESOURCE_LOCATORS,
                "io.pictura.servlet.FileResourceLocator,"
                + "io.pictura.servlet.HttpResourceLocator");
        servletInfo.addInitParam(PicturaServlet.IPARAM_REQUEST_PROCESSOR_STRATEGY,
                "io.pictura.servlet.MetadataRequestProcessor,"
                + "io.pictura.servlet.PDFRequestProcessor,"
                + "io.pictura.servlet.CSSColorPaletteRequestProcessor,"
                + "io.pictura.servlet.ClientHintRequestProcessor,"
                + "io.pictura.servlet.AutoFormatRequestProcessor");
        servletInfo.addInitParam(PicturaServlet.IPARAM_ENABLED_OUTPUT_IMAGE_FORMATS,
                "jpg,jp2,webp,png,gif");
        servletInfo.addInitParam(PicturaServlet.IPARAM_CACHE_CONTROL_HANDLER,
                "io.pictura.servlet.UndertowPicturaServer$CacheControl");
        servletInfo.addInitParam(PicturaServlet.IPARAM_CACHE_ENABLED, "true");
        servletInfo.addInitParam(PicturaServlet.IPARAM_CACHE_CAPACITY, "50");
        servletInfo.addMapping("/*");

        DeploymentInfo deploymentInfo = deployment()
                .setClassLoader(UndertowPicturaServer.class.getClassLoader())
                .setDeploymentName("pictura.war")
                .setContextPath("/")
                .setUrlEncoding("UTF-8")
                .setResourceManager(rm)
                .addListener(new ListenerInfo(IIOProviderContextListener.class))
                .addInitParameter("io.pictura.servlet.LOG_LEVEL", "DEBUG")
                .addServlet(servletInfo);

        DeploymentManager deploymentManager = defaultContainer().addDeployment(deploymentInfo);
        deploymentManager.deploy();

        PathHandler path = Handlers.path(Handlers.redirect("/"))
                .addPrefixPath("/", deploymentManager.start());

        undertow = Undertow.builder()
                .addHttpListener(PORT, HOST)
                .setHandler(path)
                .setIoThreads(10)
                .setWorkerThreads(100)
                .build();
        undertow.start();

        LoggingMXBean loggingMXBean = LogManager.getLoggingMXBean();
        List<String> loggerNames = loggingMXBean.getLoggerNames();
        for (String loggerName : loggerNames) {
            if (loggerName.startsWith("io.pictura.servlet")) {
                loggingMXBean.setLoggerLevel(loggerName, LOG_LEVEL.getName());
            }
        }

        Handler[] logHandler = LogManager.getLogManager().getLogger("").getHandlers();
        for (Handler handler : logHandler) {
            if (handler instanceof ConsoleHandler) {
                ((ConsoleHandler) handler).setLevel(LOG_LEVEL);
            }
        }
    }

    public static class InterceptableServlet extends PicturaPostServlet {

        private static final long serialVersionUID = 1981623660260122632L;

        private BufferedImage watermark;

        @Override
        public void init(ServletConfig config) throws ServletException {
            super.init(config);

            try {
                watermark = ImageIO.read(UndertowPicturaServer.class.getResourceAsStream("/watermark.png"));
            } catch (IOException e) {
                throw new ServletException(e);
            }
        }

        @Override
        protected RequestProcessor createRequestProcessor(HttpServletRequest req) throws ServletException {
            String path = getRelativeRequestPath(req);
            if (path.endsWith("demo.html")) {
                return new DemoPageRequestProcessor();
            }
            return super.createRequestProcessor(req);
        }

        @Override
        protected void doProcess(RequestProcessor rp) throws ServletException, IOException {
            if (rp instanceof IIORequestProcessor) {
                ((IIORequestProcessor) rp).setParamsInterceptor(new ParamsInterceptor() {

                    @Override
                    public Map<String, String> intercept(Map<String, String> params, HttpServletRequest req) {
                        if (req.getParameter("tpi") != null) {
                            String e = params.get("e");
                            if (e == null) {
                                params.put("e", "px");
                            } else if (!e.contains("px")) {
                                params.put("e", e.isEmpty() ? "px" : e + ",px");
                            }
                        }
                        return params;
                    }

                    @Override
                    public String getVaryCacheKey(String trueCacheKey, HttpServletRequest req) {
                        return trueCacheKey;
                    }
                });

                ((IIORequestProcessor) rp).setImageInterceptor(new ImageInterceptorChain(
                        new ImageInterceptor() {

                    @Override
                    public String getVaryCacheKey(String trueCacheKey, HttpServletRequest req) {
                        return trueCacheKey;
                    }

                    @Override
                    public BufferedImage intercept(BufferedImage img, HttpServletRequest req) {
                        if (img.getWidth() >= 10 && img.getWidth() >= 10) {
                            int w = watermark.getWidth() > watermark.getHeight()
                                    ? watermark.getWidth() : watermark.getHeight();

                            float i = Math.max(8, (img.getWidth() / 10f));

                            BufferedImage wm = watermark;
                            if (w > i) {
                                wm = Pictura.resize(watermark, Pictura.Method.AUTOMATIC,
                                        Pictura.Mode.FIT_EXACT, Math.round(i), Math.round(i));
                            }

                            // Bottom-Right
                            int x = img.getWidth() - Math.round(1.2f * wm.getWidth());
                            int y = img.getHeight() - Math.round(1.2f * wm.getHeight());

                            Graphics2D g = (Graphics2D) img.getGraphics();

                            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                            int rule = AlphaComposite.SRC_OVER;
                            Composite comp = AlphaComposite.getInstance(rule, 0.65f);
                            g.setComposite(comp);

                            g.drawImage(wm, x, y, null);
                            g.dispose();
                        }
                        return img;
                    }
                }, new ImageInterceptor() {

                    @Override
                    public String getVaryCacheKey(String trueCacheKey, HttpServletRequest req) {
                        return trueCacheKey;
                    }

                    @Override
                    public BufferedImage intercept(BufferedImage img, HttpServletRequest req) {
                        Graphics2D g = (Graphics2D) img.getGraphics();

                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

                        String bc = req.getParameter("bc");

                        g.setColor("1".equals(bc) ? Color.red : Color.lightGray);
                        g.drawRect(0, 0, img.getWidth() - 1, img.getHeight() - 1);
                        g.dispose();

                        return img;
                    }
                }));
            }
            super.doProcess(rp);
        }

    }

    public static class CacheControl implements CacheControlHandler {

        @Override
        public String getDirective(String path) {
            if (path != null && !path.startsWith("http")) {
                return "public, max-age=300";
            }
            return null;
        }

    }

    private static class DemoPageRequestProcessor extends RequestProcessor {

        @Override
        public boolean isCacheable() {
            return false;
        }

        @Override
        protected void doProcess(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {

            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024 * 5);

            int len;
            byte[] buf = new byte[1024 * 5];

            try (InputStream is = UndertowPicturaServer.class.getResourceAsStream("/demo.html")) {
                while ((len = is.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
            }

            resp.setContentType("text/html");
            resp.setContentLength(bos.size());

            bos.writeTo(resp.getOutputStream());
        }

    }

}
