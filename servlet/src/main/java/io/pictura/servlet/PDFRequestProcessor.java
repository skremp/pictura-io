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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    
    // Optional dependency check flag
    private static int dependency;
    
    @Override
    public String getRequestParameter(HttpServletRequest req, String name) {
        if (QPARAM_NAME_FORMAT_NAME.equals(name)) {
            return "jpg";
        }
        return super.getRequestParameter(req, name);
    }
    
    @Override
    protected long doWrite(byte[] data, HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
	
	if (!isPreferred(req)) {
	    doInterrupt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    return -1L;
	}
	
	org.apache.pdfbox.pdmodel.PDDocument document = null;
	
        long len;        
        try {          
            document = new org.apache.pdfbox.pdmodel.PDDocument();
                        
            org.apache.pdfbox.pdmodel.PDDocumentInformation info = new org.apache.pdfbox.pdmodel.PDDocumentInformation();
            info.setProducer(PDF_PRODUCER);
            info.setTitle(req.getParameter("dl"));
            document.setDocumentInformation(info);
            
            org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg img = new org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg(
		    document, new ByteArrayInputStream(data));            
            BufferedImage bimg = img.getRGBImage();
                        
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(
		    new org.apache.pdfbox.pdmodel.common.PDRectangle(bimg.getWidth(), bimg.getHeight()));
            document.addPage(page);
            
            try (org.apache.pdfbox.pdmodel.edit.PDPageContentStream stream = 
		    new org.apache.pdfbox.pdmodel.edit.PDPageContentStream(document, page)) {
                stream.drawImage(img, 0, 0);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            document.save(bos);
                        
            resp.setContentType(PDF_CONTENT_TYPE);
            
            len = super.doWrite(bos.toByteArray(), req, resp);
            
        } catch (org.apache.pdfbox.exceptions.COSVisitorException e) {
            throw new ServletException(e);
        } finally {
	    if (document != null) {
		document.close();
	    }
	}
        return len;
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
     * optional dependencies {@code org.apache.pdfbox:pdfbox} is not available.</b>
     */
    @Override
    public boolean isPreferred(HttpServletRequest req) {
        if (dependency == 0) {
	    if (classForName(req.getServletContext(), "org.apache.pdfbox.pdmodel.PDDocument") == null) {
		LOG.warn("Missing optional dependency \"org.apache.pdfbox.pdmodel.PDDocument\"");
		dependency = 2;
	    }	    
	    dependency = (dependency == 0) ? 1 : 2;
	}
	
        return dependency == 1 && "pdf".equals(getBaseRequestProcessor(req)
		.getRequestParameter(req, QPARAM_NAME_FORMAT_NAME));
    }

    @Override
    public ImageRequestProcessor createRequestProcessor() {
        return new PDFRequestProcessor();
    }

}
