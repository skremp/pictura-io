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

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The class <code>MetadataRequestProcessor</code> extracts the image metadata
 * (EXIF) from the requested source image and returns the values as JSON
 * formatted response.
 * <p>
 * <b>Note</b>, this request processor requires the optional dependencies
 * {@code com.drewnoakes:metadata-extractor} and {@code com.google.code.gson:gson}.
 * </p>
 * @author Steffen Kremp
 *
 * @see ImageRequestProcessor
 * @see ImageRequestStrategy
 * 
 * @since 1.0
 */
public class MetadataRequestProcessor extends StrategyRequestProcessor {    
    
    private static final Log LOG = Log.getLog(MetadataRequestProcessor.class);
    
    // Optional dependency check flag
    private static int dependency;
    
    @Override
    public String getRequestParameter(HttpServletRequest req, String name) {
        if (QPARAM_NAME_FORMAT_NAME.equals(name)
                || QPARAM_NAME_IMAGE.equals(name)) {
            return super.getRequestParameter(req, name);
        }
        return null;
    }
    
    @Override
    protected void doProcessImage(InputStream is, HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {

	if (!isPreferred(req)) {
	    doInterrupt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    return;
	}
	
        try {
            // Get the metadata from the source image stream
            com.drew.metadata.Metadata metadata = com.drew.imaging.ImageMetadataReader.readMetadata(is);

            // Memory tree
            LinkedHashMap<String, LinkedHashMap<String, String>> tree = new LinkedHashMap<>();

            // Itterate over all meta tag directories
            for (com.drew.metadata.Directory directory : metadata.getDirectories()) {  
		
		String name = directory.getName().replace(" ", "");
		
                LinkedHashMap<String, String> dir = tree.containsKey(name)
                        ? tree.get(name) : new LinkedHashMap<String, String>();

                for (com.drew.metadata.Tag tag : directory.getTags()) {
                    if (tag.getDescription() != null && tag.hasTagName()) {
                        dir.put(tag.getTagName().replace(" ", ""), tag.getDescription());
                    }
                }
                tree.put(name, dir);
            }

            // Convert the memory tree to JSON
            com.google.gson.Gson gson = new com.google.gson.Gson();
            byte[] json = gson.toJson(tree).getBytes();

            resp.setContentType("application/json");
            doWrite(json, 0, json.length, req, resp);

        } catch (com.drew.imaging.ImageProcessingException e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Tests whether or not the implementation of this image request strategy is
     * preferred for the given request object.
     *
     * @param req The related request object.
     *
     * @return <code>true</code> if this image strategy is preferred to handle
     * the image request from the given request object; otherwise
     * <code>false</code>. <b>Returns also <code>false</code> if one of the required
     * optional dependencies {@code com.drewnoakes:metadata-extractor} and 
     * {@code com.google.code.gson:gson} is not available.</b>
     */
    @Override
    public boolean isPreferred(HttpServletRequest req) {
	if (dependency == 0) {
	    if (classForName(req.getServletContext(), "com.google.gson.Gson") == null) {
		LOG.warn("Missing optional dependency \"com.google.gson.Gson\"");
		dependency = 2;
	    }
	    if (classForName(req.getServletContext(), "com.drew.imaging.ImageMetadataReader") == null) {
		LOG.warn("Missing optional dependency \"com.drew.imaging.ImageMetadataReader\"");
		dependency = 2;
	    }
	    if (classForName(req.getServletContext(), "com.drew.metadata.Metadata") == null) {
		LOG.warn("Missing optional dependency \"com.drew.metadata.Metadata\"");
		dependency = 2;
	    }	    
	    dependency = (dependency == 0) ? 1 : 2;
	}	
	
        return dependency == 1 && "exif".equals(getBaseRequestProcessor(req)
		.getRequestParameter(req, QPARAM_NAME_FORMAT_NAME));
    }

    @Override
    public ImageRequestProcessor createRequestProcessor() {
	return new MetadataRequestProcessor();
    }
    
}
