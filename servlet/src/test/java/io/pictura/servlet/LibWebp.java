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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Helper class to load the native libwebp library and JAR package at runtime.
 * 
 * @author Steffen Kremp
 */
public final class LibWebp {

    public static void addLibraryPath(String libraryPath) throws IOException {
	if (libraryPath != null && !libraryPath.isEmpty()) {
	    final File fLibraryPath = new File(libraryPath);

	    boolean exists = fLibraryPath.exists();
	    if (!fLibraryPath.exists()) {
		exists = fLibraryPath.mkdirs();
	    }

	    if (exists && fLibraryPath.isDirectory() && fLibraryPath.canWrite()
		    && fLibraryPath.canRead() && !fLibraryPath.isHidden()) {

		if (System.getProperty("java.library.path").contains(
			fLibraryPath.getAbsolutePath())) {
		    // Already set; nothing to do
		    return;
		}

		// Append the given library path to the existing library path
		System.setProperty("java.library.path",
			System.getProperty("java.library.path")
			+ File.pathSeparator + fLibraryPath.getAbsolutePath());

		AccessController.doPrivileged(new PrivilegedAction<Object>() {

		    @Override
		    public Object run() {
			try {
			    Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			    fieldSysPath.setAccessible(true);
			    fieldSysPath.set(null, null);
			} catch (IllegalAccessException | IllegalArgumentException |
				NoSuchFieldException | SecurityException ex) {
			    throw new RuntimeException("Unable to modify JNI library path", ex);
			}
			return null;
		    }
		});
	    } else {
		throw new IOException("Can't create dynamic java library path for \""
			+ libraryPath + "\"");
	    }
	}
    }

    public static void addLibWebp(String basedir) throws UnsupportedOperationException, 
	    IOException, UnsatisfiedLinkError {
	
	if (basedir == null || basedir.isEmpty()) {
	    throw new IllegalArgumentException("Base dir must be not null and not empty");
	}
	
	if (!(Thread.currentThread().getContextClassLoader() instanceof URLClassLoader)) {
	    throw new UnsupportedOperationException(
		    "Can't load embedded WebP Image I/O plugin (unsupported classloader)");
	}

	ClassLoader classLoader = ClassLoader.getSystemClassLoader();

	boolean jniBridgeAvailable = false;

	// We need to know on which platform we are         
	final String osName = System.getProperty("os.name");
	final String osDataModel = System.getProperty("sun.arch.data.model");

	String jniFile = null;

	// WINDOWS OS
	if (osName.toLowerCase().contains("windows")) {
	    if ("32".equals(osDataModel)) { // 32 bit
		jniFile = basedir + "/lib/win-i686/libwebp.dll";
	    } else if ("64".equals(osDataModel)) { // 64 bit
		jniFile = basedir + "/lib/win-x86_64/libwebp.dll";
	    } else { // Unknwon
		throw new UnsupportedOperationException("The system data model architecture is "
			+ "not supported or could not be determined.");
	    }
	} // LINUX OS
	else if (osName.toLowerCase().contains("linux")) {
	    if ("32".equals(osDataModel)) { // 32 bit
		jniFile = basedir + "/lib/linux-i686/libwebp.so";
	    } else if ("64".equals(osDataModel)) { // 64 bit
		jniFile = basedir + "/lib/linux-x86_64/libwebp.so";
	    } else { // Unknwon
		throw new UnsupportedOperationException("The system data model architecture is "
			+ "not supported or could not be determined.");
	    }
	} // MAX OS X; Universal Dynamic Lib
	else if (osName.toLowerCase().contains("mac os x")) {
	    jniFile = basedir + "/lib/macosx-x86_64/libwebp.dylib";
	}

	// Check if we can support WebP
	if (jniFile != null) {

	    // Get the current java library path from the system
	    String javaLibPath = System.getProperty("java.library.path");

	    if (javaLibPath != null) {

		String[] javaLibPath2 = javaLibPath.split(javaLibPath.contains(";") ? ";" : ":");
		if (javaLibPath2.length > 0) {

		    String pathToUse = null;

		    // Try to find a directory where we can write
		    //for (String path : javaLibPath2) {
		    for (int i = javaLibPath2.length - 1; i >= 0; i--) {
			String path = javaLibPath2[i];
			if (path.length() > 1) {
			    final File tmp = new File(path);
			    if (tmp.exists() && tmp.isDirectory() && tmp.canWrite()) {
				pathToUse = path;
				break;
			    }
			}
		    }

		    if (pathToUse == null) {
			throw new IOException("Can't write to Java native library path.");
		    }

		    // We need write access to the library path and we must be
		    // sure that the library path is a directory and not a file
		    File libPath = new File(pathToUse);
		    if (libPath.exists() && libPath.isDirectory() && libPath.canRead() && libPath.canWrite()) {

			// The native library file object
			final File lib = new File(libPath.getAbsolutePath()
				+ (libPath.getAbsolutePath().endsWith(File.separator)
					? "" : File.separator) + System.mapLibraryName("webp-imageio"));

			if (!lib.exists()) {
			    try {
				if (lib.createNewFile()) {
				    try (InputStream is = new FileInputStream(jniFile);
					    OutputStream os = new FileOutputStream(lib)) {

					int len;
					byte[] buf = new byte[1024 * 32];

					while ((len = is.read(buf)) > -1) {
					    os.write(buf, 0, len);
					}

				    }
				} else {
				    throw new IOException("Could not create file \""
					    + lib.getAbsolutePath() + "\".");
				}

				// The JNI bridge is available, we can load the JAR part too
				// and handle a rescan on ImageIO plug-ins.
				jniBridgeAvailable = true;
			    } catch (UnsatisfiedLinkError ex) {
				lib.delete();
				throw ex;
			    }
			} else {
			    jniBridgeAvailable = true;
			}
		    } else {
			throw new IOException("Can not access the Java library path on \""
				+ pathToUse + "\".");
		    }
		} else {
		    throw new IOException("Could not determine the Java library path "
			    + "(system property is empty).");
		}
	    } else {
		throw new IOException("Could not determine the Java library path.");
	    }
	} else {
	    throw new UnsupportedOperationException("The embedded Pictura WebP Image I/O is not "
		    + "supported on the current running platform. "
		    + "Please use an external Image I/O library to enable WebP support "
		    + "or install manually.");
	}

	if (jniBridgeAvailable && !isClassAvailable("com.luciad.imageio.webp.WebP", classLoader)) {
	    final String jarFile = basedir + "/webp-imageio.jar";

	    File tmpJarFile = File.createTempFile("webp-imageio", ".jar");

	    try (InputStream is = new FileInputStream(jarFile);
		    OutputStream os = new FileOutputStream(tmpJarFile)) {

		int len;
		byte[] buf = new byte[1024 * 32];

		while ((len = is.read(buf)) > -1) {
		    os.write(buf, 0, len);
		}
	    }

	    try {
		final URL jar = tmpJarFile.toURI().toURL();

		if (classLoader instanceof URLClassLoader) {
		    final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;

		    // Test whether the JAR is already loaded
		    for (URL url : urlClassLoader.getURLs()) {
			if (url != null && url.toExternalForm().equalsIgnoreCase(jar.toExternalForm())) {
			    return;
			}
		    }

		    // Add the JAR url to the class path                    
		    Method addURL = URLClassLoader.class.getDeclaredMethod("addURL",
			    new Class<?>[]{URL.class});

		    addURL.setAccessible(true);
		    addURL.invoke(urlClassLoader, new Object[]{jar});
		}

		// Verify		    
		Class.forName("com.luciad.imageio.webp.WebP", true, classLoader);

	    } catch (IllegalAccessException | InvocationTargetException |
		    IllegalArgumentException | NoSuchMethodException |
		    MalformedURLException e) {
		throw new IOException(e);
	    } catch (ClassNotFoundException | NoClassDefFoundError ex) {
		throw new IOException("Could not load embedded WebP Image I/O plugin", ex);
	    }

	}
    }

    private static boolean isClassAvailable(String className, ClassLoader cl) {
	try {
	    Class<?> c = Class.forName(className, true, cl);
	    if (c != null) {
		return true;
	    }
	} catch (ClassNotFoundException ex) {
	    // do nothing
	}
	return false;
    }
    
    // Prevent instantiation
    private LibWebp() {
    }

}
