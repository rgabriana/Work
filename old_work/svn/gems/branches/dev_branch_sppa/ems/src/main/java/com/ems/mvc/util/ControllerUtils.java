package com.ems.mvc.util;

import java.io.File;
import java.io.FilenameFilter;


public class ControllerUtils {
	
	 public static String getTheLicenseFileName(String extension , String folderPath) throws ArrayIndexOutOfBoundsException
		{
		 	String fileName =null;
			GenericExtFilter filter = new GenericExtFilter(extension);
			File dir = new File(folderPath);
			String[] list = dir.list(filter);
			fileName = list[0];
			return fileName ;
		 	
		}
	 public static class GenericExtFilter implements FilenameFilter {
		 
			private String ext;
	 
			public GenericExtFilter(String ext) {
				this.ext = ext;
			}
	 
			public boolean accept(File dir, String name) {
				return (name.endsWith(ext));
			}
}
}
