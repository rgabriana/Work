package com.ems.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.ems.server.util.ServerUtil;

/**
 * Utility Class used to Detect USB Drive.
 * @author Sharad M
 *
 */

public class AdminUtil {
	
	private transient static Log log = LogFactory.getLog(AdminUtil.class);
	private static final Logger logger_apache = Logger.getLogger(AdminUtil.class);
	
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
	
	 public static void readStreamOfProcess(final Process pr){
		 readStreamInThread(pr.getErrorStream(),true);
		 readStreamInThread(pr.getInputStream(),false);
	 }
	 public static void readStreamInThread(final InputStream stream, final boolean isErrorStream) {

	        new Thread() {
	            public void run() {
	                BufferedReader br = null;
	                try {
	                    ServerUtil.sleep(1);
	                    br = new BufferedReader(new InputStreamReader(stream));
	                    String line = "";
	                    StringTokenizer st = null;
	                    while (true) {
	                        line = br.readLine();
	                        if (line == null) {
	                            break;
	                        }else{
	                        	if(isErrorStream){
	                        		logger_apache.error("WARNING: Error Observed in the Process Error Stream: "+ line);
	                        	}
	                        }
	                    }
	                } catch (Exception e) {
	                	logger_apache.error("ERROR: Reading inputstream",e);
	                } finally {
	                    if (br != null) {
	                        try {
	                            br.close();
	                        } catch (Exception e) {
	                        	logger_apache.error("ERROR: During closing BufferedReader:",e);
	                        }
	                    }
	                }
	            }
	        }.start();

	    } // end of method readErrorStream
}