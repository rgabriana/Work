/**
 * 
 */
package com.ems.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;

/**
 * @author yogesh
 * 
 */
public final class VersionUtil {
    private static String EMS_VERSION_STR = "2.1";

	public VersionUtil() {

	}

	public static String getAppVersion(ServletContext sContext) {
		String appServerHome = sContext.getRealPath("/");
		File manifestFile = new File(appServerHome, "META-INF/MANIFEST.MF");
		Manifest mf = new Manifest();

		try {
			mf.read(new FileInputStream(manifestFile));
			Attributes atts = mf.getMainAttributes();
			return atts.getValue("Implementation-Version") + "."
					+ atts.getValue("Implementation-Build");
		} catch (FileNotFoundException e) {
			return EMS_VERSION_STR;
		} catch (IOException e) {
			return EMS_VERSION_STR;
		}
	}

}
