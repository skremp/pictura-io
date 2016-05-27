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

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Request processor to produce a PDF document from a requested image.
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public class PDFRequestProcessor extends StrategyRequestProcessor {

    private static final Log LOG = Log.getLog(PDFRequestProcessor.class);

    private static final String PDF_PRODUCER = "PicturaIO";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    @Override
    public String getRequestParameter(HttpServletRequest req, String name) {
        if (QPARAM_NAME_FORMAT_NAME.equals(name)) {
            return "jpg";
        }
        return super.getRequestParameter(req, name);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected long doWrite(byte[] data, HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
        return doWrite(data, 0, data.length, req, resp);
    }

    @Override
    protected long doWrite(byte[] data, int off, int len, HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
        if (!isPreferred(req)) {
            doInterrupt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return -1L;
        }
        return new PDFConverter(this).doConvert(data, off, len, req, resp);
    }
    
    private long doWrite0(byte[] data, int off, int len, HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {       
        return super.doWrite(data, off, len, req, resp);
    }

    /**
     * Tests whether or not the implementation of this image request strategy is
     * preferred for the given request object.
     *
     * @param req The related request object.
     *
     * @return <code>true</code> if this image strategy is preferred to handle
     * the image request from the given request object; otherwise
     * <code>false</code>. <b>Returns also <code>false</code> if the required
     * optional dependencies {@code org.apache.pdfbox:pdfbox} is not
     * available.</b>
     */
    @Override
    public boolean isPreferred(HttpServletRequest req) {
        if ("pdf".equals(getBaseRequestProcessor(req).getRequestParameter(req, QPARAM_NAME_FORMAT_NAME))) {
            if (classForName(req.getServletContext(), "org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage") == null
                    || classForName(req.getServletContext(), "org.apache.pdfbox.pdmodel.PDDocument") == null) {
                LOG.warn("Can not handle request because of missing optional dependency \"org.apache.pdfbox:pdfbox\"");
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public ImageRequestProcessor createRequestProcessor() {
        return new PDFRequestProcessor();
    }

    // Encapsulate the optional dependencies
    private static final class PDFConverter {

        private final PDFRequestProcessor rp;

        PDFConverter(PDFRequestProcessor rp) {
            this.rp = rp;
        }

        private long doConvert(byte[] data, int off, int len, HttpServletRequest req,
                HttpServletResponse resp) throws ServletException, IOException {

            long outLen = -1L;

            try (org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument()) {
                org.apache.pdfbox.pdmodel.PDDocumentInformation info = new org.apache.pdfbox.pdmodel.PDDocumentInformation();
                info.setProducer(PDF_PRODUCER);
                info.setTitle(req.getParameter("dl"));
                document.setDocumentInformation(info);

                org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg img = new org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg(
                        document, new FastByteArrayInputStream(data, off, len));
                BufferedImage bimg = img.getRGBImage();

                org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(
                        new org.apache.pdfbox.pdmodel.common.PDRectangle(bimg.getWidth(), bimg.getHeight()));
                document.addPage(page);

                try (org.apache.pdfbox.pdmodel.edit.PDPageContentStream stream
                        = new org.apache.pdfbox.pdmodel.edit.PDPageContentStream(document, page)) {
                    stream.drawImage(img, 0, 0);
                }

                FastByteArrayOutputStream bos = new FastByteArrayOutputStream(len + 2048);
                document.save(bos);

                resp.setContentType(PDF_CONTENT_TYPE);
                outLen = rp.doWrite0(bos.buf, 0, bos.count, req, resp);
            } catch (Throwable t) {
                throw new ServletException(t);
            }
            return outLen;
        }
    }

}