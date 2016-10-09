package org.seleniumxml.test.commons;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ResourcesLocator {
	
	Pattern underscorePatter = Pattern.compile(".+_.+");
	
	public Pattern getUnderscorePatter() {
		return underscorePatter;
	}

	public ResourcesLocator() {
		super();
	}

	protected List<String> getPropertyResources(final Pattern pattern) {
		final ArrayList<String> retval = new ArrayList<String>();
		final String classPath = System.getProperty("java.class.path", ".");
		final String[] classPathElements = classPath.split(System.getProperty("path.separator"));
		for (final String element : classPathElements) {
			retval.addAll(getPropertyResources(element, pattern));
		}
		return retval;
	}

	protected List<String> getPropertyResources(final String element, final Pattern pattern) {
		final ArrayList<String> retval = new ArrayList<String>();
		final File file = new File(element);
		if (file.isDirectory()) {
			retval.addAll(getPropertyResourcesFromDirectory(file, pattern));
		} else {
			//retval.addAll(getPropertyResourcesFromJarFile(file, pattern));
		}
		return retval;
	}

//	private List<String> getPropertyResourcesFromJarFile(final File file, final Pattern pattern) {
//		final ArrayList<String> retval = new ArrayList<String>();
//		ZipFile zf;
//		try {
//			zf = new ZipFile(file);
//		} catch (final ZipException e) {
//			throw new Error(e);
//		} catch (final IOException e) {
//			throw new Error(e);
//		}
//		final Enumeration e = zf.entries();
//		while (e.hasMoreElements()) {
//			final ZipEntry ze = (ZipEntry) e.nextElement();
//			final String fileName = ze.getName();
//			final boolean accept = pattern.matcher(fileName).matches();
//			if (accept) {
//				retval.add(fileName);
//			}
//		}
//		try {
//			zf.close();
//		} catch (final IOException e1) {
//			throw new Error(e1);
//		}
//		return retval;
//	}

	private List<String> getPropertyResourcesFromDirectory(final File directory, final Pattern pattern) {
		final ArrayList<String> retval = new ArrayList<String>();
		final File[] fileList = directory.listFiles();
		for (final File file : fileList) {
			if (file.isDirectory()) {
				//retval.addAll(getResourcesFromDirectory(file, pattern));
			} else {
				try {
					String fileName = file.getCanonicalPath();
					final boolean accept = pattern.matcher(fileName).matches();
					fileName = fileName.substring(fileName.lastIndexOf(file.separator)+1);
					fileName = fileName.substring(0, fileName.lastIndexOf("."));
					final boolean doesnaccept = underscorePatter.matcher(fileName).matches();
					if (accept) {
						if(!doesnaccept){
							retval.add(fileName);
						}
					}
				} catch (final IOException e) {
					throw new Error(e);
				}
			}
		}
		return retval;
	}

}