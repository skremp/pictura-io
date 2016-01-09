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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * A file based cache control handler implementation where the rules are
 * configured by a simple XML file structure.
 *
 * @author Steffen Kremp
 *
 * @see CacheControlHandler
 *
 * @since 1.0
 */
final class XMLCacheControlHandler implements CacheControlHandler {

    private static final Log LOG = Log.getLog(XMLCacheControlHandler.class);
    
    private final String filename; // XML file which contains the rules
    private Collection<CacheControlRule> rules; // rules

    private WatchServiceHandler watchService;
    private volatile boolean destroyed;

    /**
     * Constructs a new cache control handler from the given XML configuration
     * file.
     *
     * @param filename The file with the cache control rules.
     * @throws IOException if an I/O error occures or the configuration is not
     * valid.
     */
    XMLCacheControlHandler(String filename) throws IOException {
	this.filename = filename;
	this.destroyed = false;

	initRules();
	initWatchService();
    }

    void destroy() throws IOException {
	this.destroyed = true;
	rules.clear();
    }

    // Helper method to parse the cache control rules
    private void initRules() throws IOException {

	ArrayList<CacheControlRule> tmpRules = new ArrayList<>();

	try (InputStream is = new FileInputStream(new File(filename))) {
	    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	    XMLEventReader eventReader = inputFactory.createXMLEventReader(is);

	    CacheControlRule rule = new CacheControlRule();

	    while (eventReader.hasNext()) {
		XMLEvent event = eventReader.nextEvent();

		if (event.isStartElement()) {
		    StartElement startElement = event.asStartElement();

		    if ("rule".equals(startElement.getName().getLocalPart())) {
			rule = new CacheControlRule();
		    }
		}

		if (event.isStartElement()) {
		    if ("path".equals(event.asStartElement().getName().getLocalPart())) {
			event = eventReader.nextEvent();
			rule.setPath(event.isCharacters() ? event.asCharacters().getData() : null);
			continue;
		    }

		    if ("directive".equals(event.asStartElement().getName().getLocalPart())) {
			event = eventReader.nextEvent();
			rule.setDirective(event.asCharacters().getData());
			continue;
		    }
		}

		if (event.isEndElement()) {
		    EndElement endElement = event.asEndElement();
		    if ("rule".equals(endElement.getName().getLocalPart())) {
			tmpRules.add(rule);
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("rule \"" + rule.getPattern().toString() 
                                    + "\" to \"" + rule.getDirective() + "\" added");
                        }
		    }
		}
	    }
	} catch (XMLStreamException | IllegalArgumentException e) {
	    throw new IOException(e);
	}

	this.rules = tmpRules;
	
	if (rules.isEmpty()) {
	    LOG.warn("There are no cache control rules defined");
	}
    }

    // Helper method to create a watch service for the config file
    private void initWatchService() {
	watchService = new WatchServiceHandler(new File(filename).getParentFile().toPath());

	Thread wst = new Thread(watchService);
	wst.setPriority(Thread.MIN_PRIORITY);
	wst.setDaemon(true);
	wst.start();
    }

    @Override
    public String getDirective(String path) {
	if (path != null && !path.isEmpty() && rules != null && !rules.isEmpty()) {

	    // A rule starts with an initial "/" but the path is given
	    // as relative path
	    if (!path.startsWith("/")) {
		path = "/" + path;
	    }

	    Iterator<CacheControlRule> iter = rules.iterator();
	    while (iter.hasNext()) {
		CacheControlRule r = iter.next();
		if (r.getPattern().matcher(path).matches()) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("rule \"" + r.getPattern().toString() + "\" matched");
                    }
		    return r.getDirective();
		}
	    }
	}
	return null;
    }

    // Helper class to hold the rules
    private static class CacheControlRule {

	private Pattern pattern; // path
	private String directive;

	private CacheControlRule() {
	}

	private void setPath(String path) throws PatternSyntaxException {
	    if (path == null || path.isEmpty()) {
		path = "/*";
	    }
	    try {
		this.pattern = Pattern.compile(path.replace("*", ".{0,}"));
	    } catch (PatternSyntaxException ex) {
		throw new IllegalArgumentException("Invalid cache control rule '"
			+ path + "'. See nested exception for more details:", ex);
	    }
	}

	private void setDirective(String directive) {
	    this.directive = (directive != null && directive.isEmpty()) ? null : directive;
	}

	private Pattern getPattern() {
	    return pattern;
	}

	private String getDirective() {
	    return directive;
	}

    }

    // Helper class to handle cache control config reloads (on file change)
    private class WatchServiceHandler implements Runnable {

	private final Path file;
	private WatchService watcher;

	private WatchServiceHandler(Path file) {
	    this.file = file;
	}

	@Override
	public void run() {
	    try {
		watcher = FileSystems.getDefault().newWatchService();
		file.register(watcher, ENTRY_DELETE, ENTRY_MODIFY);

		while (!destroyed) {
		    WatchKey key;
		    try {
			key = watcher.take();
		    } catch (InterruptedException ex) {
			return;
		    }

		    for (WatchEvent<?> event : key.pollEvents()) {
			WatchEvent.Kind<?> kind = event.kind();

			@SuppressWarnings("unchecked")
			WatchEvent<Path> ev = (WatchEvent<Path>) event;
			Path path = ev.context();

			if (kind == ENTRY_MODIFY) {
			    File f = new File(filename);
			    if (f.getAbsolutePath().endsWith(path.toString())) {
                                if (LOG.isInfoEnabled()) {
                                    LOG.info("Reloading cache control rules from \"" + f.getAbsolutePath() + "\"");
                                }
				initRules();
			    }
			}

			boolean valid = key.reset();
			if (!valid) {
			    break;
			}
		    }
		}

	    } catch (IOException ex) {
		LOG.error("Exception in file system watcher for cache control configuration file", ex);
	    } finally {
		if (watcher != null) {
		    try {
			watcher.close();
		    } catch (IOException ex) {
			LOG.error("Exception while closing file system watcher for cache control configuration file", ex);
		    }
		}
	    }
	}

    }

}
