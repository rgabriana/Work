package com.ems.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility Class used to Detect USB Drive.
 * @author Sharad M
 *
 */

public class AdminUtil {
	
	private transient static Log log = LogFactory.getLog(AdminUtil.class);
	
	public static String TOMCAT_INSTALLATION_PATH = null;
	
	/**
	 * Method to get list of USB connected
	 *
	 * @param request the current request
	 *
	 * @return the list of USB Mounted (if found), null if not found
	 */
	public static ArrayList<String> getMountedUsbSticks()
	{
		ArrayList<String> listOfUsbs = new ArrayList<String>();
		
		String command = "ls -tr1 /media";
		Runtime rt = Runtime.getRuntime();
		Process proc;
		try {
			proc = rt.exec(command);
			BufferedReader outputStream 	= 	new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader errorStream	 	=	new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			
			String output = "";
			while((output = outputStream.readLine())!= null)
			{
				log.info("getMountedUsbSticks(): Usb mounts--> "+output);
				listOfUsbs.add(output);
			}			
			String stringOfErrors = null;
			
			while((stringOfErrors = errorStream.readLine())!= null)
			{
				log.info("Error:"+stringOfErrors);
			}			
			
		} catch (IOException ioe) {
			log.info("getMountedUsbSticks(): "+ioe);
		}		
		return listOfUsbs;
	}
}
