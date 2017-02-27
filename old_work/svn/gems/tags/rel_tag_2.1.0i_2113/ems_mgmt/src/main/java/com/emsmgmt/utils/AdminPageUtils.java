package com.emsmgmt.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

public class AdminPageUtils {

	static final Logger logger 								= Logger.getLogger("GemsUpgrade");
	
	
	
public static String compatibleVersion(File child, String path, ServletContext context, String flowoperation) {
	
		int tarVersion 		=	-1;
		int versionOnSystem = 	-1;
		String versionComparisonResult = "-1";
		String versionOutputString = "";
		
		String METHOD = "compatibleVersion():";	
		String filename = child.getName();
		String scriptPath = context.getRealPath("/")+"adminscripts/checkversion.sh";
		
		String TOMCAT_INSTALLATION_PATH = context.getRealPath("/").substring(0,context.getRealPath("/").lastIndexOf(context.getContextPath()));
		String scriptExecCommand = "/bin/bash "+scriptPath+" "+filename+" "+path+" "+TOMCAT_INSTALLATION_PATH + " "+flowoperation;
		
		logger.info(METHOD+"Command being executed:"+scriptExecCommand);
		
		Runtime rt = Runtime.getRuntime();
		Process proc;
		try {
			proc = rt.exec(scriptExecCommand);
			
			BufferedReader outputStream 	= 	new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader errorStream	 	=	new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			String outputLine = null;
			while((outputLine = outputStream.readLine()) != null)
			{
				versionOutputString = outputLine.trim();
				if(versionOutputString.startsWith("tarversion"))
				{
					String[] arr = versionOutputString.split("##");
					logger.info(Arrays.toString(arr));
					tarVersion = Integer.parseInt(arr[1]);
				}
				else if(versionOutputString.startsWith("systemversion"))
				{
					String[] arr = versionOutputString.split("##");
					logger.info(Arrays.toString(arr));
					versionOnSystem = Integer.parseInt(arr[1]);
				}
			}
			
			String errorLine = null;
			while((errorLine = errorStream.readLine()) != null)
			{
				logger.info(METHOD+errorLine);
			}			
			proc.waitFor();
			outputStream.close();
			
			/**
			 * Define successful conditions for the version check to succeed.
			 * Moved the comparison logic from shell script to java since integer calculations are better done here.
			 * Initialise it to -1. 
			 */
			
			if(flowoperation.equalsIgnoreCase("backuprestore"))
			{
				//tarversion and versiononsystem wont get new values for invalid archives.
				if(tarVersion == versionOnSystem)
					versionComparisonResult = Integer.toString(tarVersion);
			}
			else if(flowoperation.equalsIgnoreCase("upgrade"))
			{
				if (tarVersion >= versionOnSystem)
					versionComparisonResult = Integer.toString(tarVersion);
			}
		}		
		catch(IOException ioe)
		{
			logger.error("compatibleVersion(): Exception:"+ioe);
		}
		catch(InterruptedException ie)
		{
			logger.error("compatibleVersion(): Exception:"+ie);
		}
		catch (ArrayIndexOutOfBoundsException aiobe) { //This will be thrown by split.
			logger.error("compatibleVersion(): Exception:"+aiobe);
			versionComparisonResult = "-1";
		}
		
		logger.info(METHOD+"version result: "+versionComparisonResult);
		return versionComparisonResult;
	}
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
				logger.info("getMountedUsbSticks(): Usb mounts--> "+output);
				listOfUsbs.add(output);
			}			
			String stringOfErrors = null;
			
			while((stringOfErrors = errorStream.readLine())!= null)
			{
				logger.info("Error:"+stringOfErrors);
			}			
			
		} catch (IOException ioe) {
			logger.info("getMountedUsbSticks(): "+ioe);
		}		
		return listOfUsbs;
	}
}

