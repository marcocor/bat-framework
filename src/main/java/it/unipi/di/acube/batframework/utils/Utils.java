package it.unipi.di.acube.batframework.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Utils {
	public static Map<String, InputStream> getResourceListing(ClassLoader classloader, String path, String pattern) throws URISyntaxException, UnsupportedEncodingException, IOException {
		if (!path.endsWith("/"))
			path += "/";
		Map<String, InputStream> result = new HashMap<>();
		URL dirURL = classloader.getResource(path);
		if (dirURL == null)
			throw new RuntimeException("Path " + path + " does not exist.");
		
		if (dirURL.getProtocol().equals("file")) {
			for (String filename : new File(dirURL.toURI()).list()){
				boolean match = (pattern == null) ? true : filename.toLowerCase().matches(pattern);
				if (match)
					result.put(filename, new FileInputStream(dirURL.getPath() + filename));
			}
		} else if (dirURL.getProtocol().equals("jar")) {
			String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); // strip
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				if (name.startsWith(path) && name.length() > path.length()) {
					String filename = name.substring(path.length());
					boolean isSubDir = (filename.indexOf("/", 1) != -1);
					boolean match = (pattern == null) ? true : filename.toLowerCase().matches(pattern);
					if (!isSubDir && match)
						result.put(filename, classloader.getResourceAsStream(name));
				}
			}
			jar.close();
		}
		else
			throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
		
		return result;
	}
}
