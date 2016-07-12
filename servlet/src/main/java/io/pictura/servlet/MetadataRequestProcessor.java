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

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The class <code>MetadataRequestProcessor</code> extracts the image metadata
 * (EXIF) from the requested source image and returns the values as JSON
 * formatted response.
 * <p>
 * <b>Note</b>, this request processor requires the optional dependencies
 * {@code com.drewnoakes:metadata-extractor} and
 * {@code com.google.code.gson:gson}.
 * </p>
 *
 * @author Steffen Kremp
 *
 * @see ImageRequestProcessor
 * @see ImageRequestStrategy
 *
 * @since 1.0
 */
public class MetadataRequestProcessor extends StrategyRequestProcessor {

    private static final Log LOG = Log.getLog(MetadataRequestProcessor.class);

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
            byte[] json = toJson(tree, req.getServletContext()).getBytes();

            resp.setContentType("application/json");
            doWrite(json, 0, json.length, req, resp);
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }

    private String toJson(LinkedHashMap<String, LinkedHashMap<String, String>> src,
            ServletContext ctx) {
        
        // Try the default javax.json package available since JEE 7 
        if (classForName(ctx, "javax.json.Json") != null) {
            // javax.json is available as API and provider. If there is the API
            // available it is not guaranteed that a provider is also available.
            String json = null;
            try {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Found javax.json.Json to build EXIF metadata output");
                }
                javax.json.JsonObjectBuilder builder = javax.json.Json.createObjectBuilder();
                for (String k : src.keySet()) {
                    javax.json.JsonObjectBuilder j = javax.json.Json.createObjectBuilder();
                    Map<String, String> v = src.get(k);
                    for (String vk : v.keySet()) {
                        j.add(vk, v.get(vk));
                    }
                    builder.add(k, j.build());
                }
                json = builder.build().toString();
            } catch (Throwable t) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(t);
                }
            }
            if (json != null) {
                return json;
            }
        }
        
        // Fallback Gson
        if (classForName(ctx, "com.google.gson.Gson") != null) {
            return new com.google.gson.Gson().toJson(src);
        }
        
        // Should normally never happen
        return null;
    }

    /**
     * Tests whether or not the implementation of this image request strategy is
     * preferred for the given request object.
     *
     * @param req The related request object.
     *
     * @return <code>true</code> if this image strategy is preferred to handle
     * the image request from the given request object; otherwise
     * <code>false</code>. <b>Returns also <code>false</code> if one of the
     * required optional dependencies {@code com.drewnoakes:metadata-extractor}
     * and {@code com.google.code.gson:gson} is not available.</b>
     */
    @Override
    public boolean isPreferred(HttpServletRequest req) {
        if ("exif".equals(getBaseRequestProcessor(req).getRequestParameter(req, QPARAM_NAME_FORMAT_NAME))) {
            if (classForName(req.getServletContext(), "javax.json.Json") == null
                    && classForName(req.getServletContext(), "com.google.gson.Gson") == null) {
                LOG.warn("Can not handle request because of missing optional dependency \"com.google.code.gson:gson\"");
                return false;
            }
            if (classForName(req.getServletContext(), "com.drew.metadata.Metadata") == null) {
                LOG.warn("Can not handle request because of missing optional dependency \"com.drewnoakes:metadata-extractor\"");
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public ImageRequestProcessor createRequestProcessor() {
        return new MetadataRequestProcessor();
    }

}