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

import io.pictura.servlet.annotation.ResourcePath;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletContext;

/**
 * A {@link ResourceLocator} implementation to lookup resources on the system's
 * file system where the default root path ({@link #getRootPath()} is relative
 * to the servlet resource path which is specified by
 * {@link ServletContext#getRealPath(java.lang.String)}.
 * <p>
 *
 * A customized implementation could be use to handle resources outside from the
 * web application root path, for example:
 *
 * <pre><code>
 * public class CustomFileResourceLocator extends FileResourceLocator {
 *
 *   protected String getRootPath() {
 *      return "/etc/images";
 *   }
 *
 * }
 * </code></pre>
 *
 * To exclude some files from the root path of an
 * <code>FileResourceLocator</code>, it is possible to override the
 * #validate(java.io.File) method, for example:
 *
 * <pre><code>
 * public class CustomFileResourceLocator extends FileResourceLocator {
 *
 *    protected String getRootPath() {
 *       return "/etc/images";
 *    }
 *
 *    protected boolean validate(File f) {
 *       if (super.validate(f)) {
 *          // here, check if it is allowed to consume the resource file
 *          return true;
 *       }
 *       return false;
 *    }
 *
 * }
 * </code></pre>
 *
 * @see ResourceLocator
 * @see ResourcePath
 *
 * @author Steffen Kremp
 *
 * @since 1.0
 */
public class FileResourceLocator implements ResourceLocator {

    private static final Log LOG = Log.getLog(FileResourceLocator.class);

    private String rootPath;

    /**
     * Creates a new file resource locator.
     */
    public FileResourceLocator() {
	ResourcePath rp = getClass().getAnnotation(ResourcePath.class);	
	if (rp != null) {
	    rootPath = rp.value();
	    File f = new File(rootPath);
            if (LOG.isWarnEnabled()) {
                if (!f.exists()) {
                    LOG.warn("Declared resource path @ResourcePath(\"" + rootPath + "\") does not exists");
                } else if (!f.isDirectory()) {
                    LOG.warn("Declared resource path @ResourcePath(\"" + rootPath + "\") is not a directory");
                } else if (!f.canRead() || f.isHidden()) {
                    LOG.warn("Declared resource path @ResourcePath(\"" + rootPath + "\") is not accessable");
                }
            }
	}
    }

    /**
     * Creates a new <code>FileResourceLocator</code> for local resource files
     * are located in the servlet context.
     *
     * @param ctx The servlet context which is used to lookup local resources.
     */
    FileResourceLocator(ServletContext ctx) {
	setServletContext(ctx);
    }

    final void setServletContext(ServletContext ctx) {
	if (rootPath == null && getClass().getAnnotation(ResourcePath.class) == null) {
	    rootPath = ctx.getRealPath("/");
	}
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
	if (path == null || path.isEmpty()
		|| path.contains("./") || path.contains("://")
		|| path.contains("/WEB-INF") || path.contains("/META-INF")) {
	    return null;
	}

	String rp = getRootPath();	

	if (rp != null && !rp.isEmpty()) {

	    String[] rpl = rp.contains(File.pathSeparator) ?
		    rp.split(File.pathSeparator) : new String[]{rp};
	    	    
	    for (String s : rpl) {	    		
		String rp2 = s.trim();
		
		StringBuilder rpp = new StringBuilder(rp2);
		if (!rp2.endsWith(File.separator)) {
		    rpp.append(File.separator);
		}

		rpp.append(path.startsWith("/") ? path.substring(1) : path);
		String filename = rpp.toString().replace("/", File.separator);

		File f = new File(filename.contains("?")
			? filename.substring(0, filename.indexOf('?')) : filename);

		return validate(f) ? getURL(f) : null;
	    }
	}
	return null;
    }

    /**
     * Returns a URL to the resource that is mapped to the given filename.
     *
     * @param filename A String specifying the path to the resource.
     * @return The resource located at the named path.
     *
     * @throws MalformedURLException if the filename is not given in the correct
     * form.
     *
     * @see #getURL(java.io.File)
     */
    protected URL getURL(String filename) throws MalformedURLException {
	return getURL(new File(filename));
    }

    /**
     * Returns a URL that represents the abstract pathname from the given file.
     *
     * @param f A file specifying the path to the resource.
     * @return The resource located at the filesystem.
     *
     * @throws MalformedURLException if a protocol handler for the URL could not
     * be found, or if some other error occurred while constructing the URL.
     *
     * @see #getURL(java.lang.String)
     */
    protected URL getURL(File f) throws MalformedURLException {
	return f.toURI().toURL();
    }

    /**
     * Gets the root entry path which is used by this
     * <code>FileResourceLocator</code> to locate resource files.
     *
     * @return The root path or <code>null</code> if there is no root path
     * specified.
     */
    protected String getRootPath() {
	return rootPath;
    }

    /**
     * Method invoked prior to return the URL to the given file.
     * <p>
     * Note: To properly nest multiple overridings, subclasses should generally
     * invoke {@code super.validate()} at the end of this method.
     * </p>
     *
     * @param f The file resource to validate.
     *
     * @return <code>true</code> if the specified file is a valid (exists, can
     * read, have permissions to read, ...) non hidden resource file; otherwise
     * <code>false</code>
     */
    protected boolean validate(File f) {
	return f.isFile() && !f.isHidden() && f.canRead();
    }

}
