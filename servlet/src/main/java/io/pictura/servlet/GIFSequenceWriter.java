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

import java.awt.image.RenderedImage;
import java.io.IOException;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

/**
 * Derived from
 * http://elliot.kroo.net/software/java/GifSequenceWriter/GifSequenceWriter.java
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
final class GIFSequenceWriter {

    private final ImageWriter imageWriter;
    private final ImageWriteParam imageWriteParam;
    private final IIOMetadata imageMetaData;

    GIFSequenceWriter(ImageWriter writer, int imageType, int delayTime,
	    boolean loopContinuously) throws IIOException, IOException {

	imageWriter = writer;
	imageWriteParam = imageWriter.getDefaultWriteParam();

	ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);

	imageMetaData = imageWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);
	String metaFormatName = imageMetaData.getNativeMetadataFormatName();

	IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);
	IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");

	graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
	graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
	graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
	graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delayTime));
	graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

	IIOMetadataNode appEntensionsNode = getNode(root, "ApplicationExtensions");
	IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");

	child.setAttribute("applicationID", "NETSCAPE");
	child.setAttribute("authenticationCode", "2.0");

	int loop = loopContinuously ? 0 : 1;

	child.setUserObject(new byte[]{0x1, (byte) (loop & 0xFF), (byte) ((loop >> 8) & 0xFF)});
	appEntensionsNode.appendChild(child);

	imageMetaData.setFromTree(metaFormatName, root);
	imageWriter.prepareWriteSequence(null);
    }

    void writeToSequence(RenderedImage img) throws IOException {
	imageWriter.writeToSequence(new IIOImage(img, null, imageMetaData),
		imageWriteParam);
    }

    void close() throws IOException {
	imageWriter.endWriteSequence();
    }

    private IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
	int nNodes = rootNode.getLength();
	for (int i = 0; i < nNodes; i++) {
	    if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
		return ((IIOMetadataNode) rootNode.item(i));
	    }
	}
	IIOMetadataNode node = new IIOMetadataNode(nodeName);
	rootNode.appendChild(node);
	return (node);
    }
}
