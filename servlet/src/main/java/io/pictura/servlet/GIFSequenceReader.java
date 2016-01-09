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

import static io.pictura.servlet.ImageRequestProcessor.tryParseInt;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class to read a GIF sequence (animated GIF).
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
final class GIFSequenceReader {

    private final ImageReader imageReader;
    private int delayTime;

    GIFSequenceReader(ImageReader reader) throws IIOException, IOException {
	imageReader = reader;
    }

    BufferedImage[] readAllFrames() throws IOException {
	int noi = imageReader.getNumImages(true);
	BufferedImage[] sequences = new BufferedImage[noi];

	int masterWidth = 0, masterHeight = 0, masterType = 0;

	for (int i = 0; i < noi; i++) {
	    BufferedImage image = imageReader.read(i);
	    IIOMetadata metadata = imageReader.getImageMetadata(i);

	    Node tree = metadata.getAsTree("javax_imageio_gif_image_1.0");
	    NodeList children = tree.getChildNodes();

	    for (int j = 0; j < children.getLength(); j++) {
		Node nodeItem = children.item(j);
		switch (nodeItem.getNodeName()) {
		    case "ImageDescriptor": {
			Map<String, Integer> imageAttr = new HashMap<>();
			for (String ia : new String[]{
			    "imageLeftPosition", "imageTopPosition",
			    "imageWidth", "imageHeight"}) {
			    NamedNodeMap attr = nodeItem.getAttributes();
			    Node attnode = attr.getNamedItem(ia);
			    imageAttr.put(ia, Integer.valueOf(attnode.getNodeValue()));
			}

			// On the first frame (master frame) we need
			// to read some meta data which we need later
			// if we produce the output image
			if (i == 0) {
			    masterWidth = imageAttr.get("imageWidth");
			    masterHeight = imageAttr.get("imageHeight");
			    masterType = BufferedImage.TYPE_INT_ARGB;
			}

			// Render each frame
			BufferedImage frame = new BufferedImage(
				masterWidth, masterHeight, masterType);
			frame.getGraphics().drawImage(image,
				imageAttr.get("imageLeftPosition"),
				imageAttr.get("imageTopPosition"),
				null);

			sequences[i] = frame;
			break;
		    }
		    case "GraphicControlExtension": {
			if (i == 0) {
			    Map<String, Integer> imageAttr = new HashMap<>();
			    for (String ia : new String[]{"delayTime"}) {
				NamedNodeMap attr = nodeItem.getAttributes();
				Node attnode = attr.getNamedItem(ia);
				imageAttr.put(ia, tryParseInt(attnode.getNodeValue(), 0));
			    }
			    delayTime = imageAttr.get("delayTime");
			}
			break;
		    }
		}
	    }
	}
	return sequences;
    }

    int getDelayTime() {
	return delayTime;
    }

}
