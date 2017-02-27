package com.ems.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.ems.server.ServerMain;
import com.ems.server.util.ServerUtil;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Utility Class used to Detect USB Drive.
 * @author Sharad M
 *
 */

public class AdminUtil {
	
	//private transient static Log log = LogFactory.getLog(AdminUtil.class);
	//private static final Logger logger_apache = Logger.getLogger(AdminUtil.class);
	private static Logger syslog = Logger.getLogger("SysLog");
	
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
				syslog.info("getMountedUsbSticks(): Usb mounts--> "+output);
				listOfUsbs.add(output);
			}			
			String stringOfErrors = null;
			
			while((stringOfErrors = errorStream.readLine())!= null)
			{
				syslog.info("Error:"+stringOfErrors);
			}			
			
		} catch (IOException ioe) {
			syslog.info("getMountedUsbSticks(): "+ioe);
		}		
		return listOfUsbs;
	}
	
	 public static void readStreamOfProcess(final Process pr){
		 readStreamOfProcess(pr, false);
	 }
	 
	 public static void readStreamOfProcess(final Process pr, boolean flagReadStream){
		 readStreamOfProcess(pr, flagReadStream, false);
	 }
	 public static void readStreamOfProcess(final Process pr, boolean flagReadStream, final boolean isLogAsInfo){
		 readStreamInThread(pr.getErrorStream(),true, isLogAsInfo);
		 readStreamInThread(pr.getInputStream(),flagReadStream, isLogAsInfo);
	 }
	 
	 
	 public static void executeLinuxCommand(final String[] cmdArr) throws Exception{
		syslog.info("cmdArr command is "+ Arrays.deepToString(cmdArr) );
		final Process cmdArrPr = Runtime.getRuntime().exec(cmdArr);
		AdminUtil.readStreamOfProcess(cmdArrPr, false, true);
		final int cmdArrStatus = cmdArrPr.waitFor();
		final boolean cmdArrSuccess = cmdArrStatus == 0;
		if(!cmdArrSuccess){
			final String str = "********************************ERROR Occured while executing cmd::"+Arrays.deepToString(cmdArr)+":: Please contact admin.********************************************";
			syslog.error(str);
			throw new Exception(str);
		}
	 }
	 public static void readStreamInThread(final InputStream stream, final boolean isErrorStream){
		 readStreamInThread(stream, isErrorStream, false);
	 }

	 /**
	  * The info should be logged as info and not as log in the EMS_SYS.log based on the flag isLogAsInfo
	  * @param stream
	  * @param isErrorStream
	  * @param isLogAsError
	  */
	 public static void readStreamInThread(final InputStream stream, final boolean isErrorStream, final boolean isLogAsInfo) {

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
	                        	if (isErrorStream){
	                        		syslog.error("WARNING: Error Observed in the Process Error Stream: "+ line);
	                        	}else if(isLogAsInfo){
	                        		syslog.info("Output from command: "+ line);
	                        	}
	                        }
	                    }
	                } catch (Exception e) {
	                	syslog.error("ERROR: Reading inputstream",e);
	                } finally {
	                    if (br != null) {
	                        try {
	                            br.close();
	                        } catch (Exception e) {
	                        	syslog.error("ERROR: During closing BufferedReader:",e);
	                        }
	                    }
	                }
	            }
	        }.start();

	    } // end of method readErrorStream
	 
	 
	 public static String callNetworkInterfacesScript(){
		 Runtime rt = Runtime.getRuntime();
			Process proc;
			String[] cmdArr = {
					"php",
					ServerMain.getInstance().getTomcatLocation()
							+ "adminscripts/network_interfaces_info.php" };
			try {
				proc = rt.exec(cmdArr);
				AdminUtil.readStreamInThread(proc.getErrorStream(), true);
				BufferedReader outputStream = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				String output = null;
				final StringBuffer buf = new StringBuffer();
				while ((output = outputStream.readLine()) != null) {				
					buf.append(output);			
				}
				return buf.toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;		
			
	 }
	 
	 /**
	  * USAGE:
	  * 
	  * 	To open the port 1234 on eth0 please use -
	  *   		enableDisablePort (1234, 0, eth0, null)
	  *   
	  *   	To remove a port 5678 on say eth1 -
	  *   		enableDisablePort (0, 5678, null, eth1)
	  *   
	  *   	To open the 12302 port on eth0 and at the same time wants to remove that port from eth1 - 
	  * 		enableDisablePort (12302, 12302, eth0, eth1)
	  * 
	  * @param portToOpen
	  * @param portToClose
	  * @param interfaceToOpenPort
	  * @param interfaceToClosePort
	  */
	 public static void enableDisablePort(final int portToOpen, final int portToClose, final String interfaceToOpenPort, final String interfaceToClosePort){
		    try {
		    	String interfaceToOpenPorttemp = interfaceToOpenPort;
		    	if (StringUtils.isEmpty(interfaceToOpenPort)){
		    		interfaceToOpenPorttemp = "null";
		    	}
		    	String interfaceToClosePorttemp = interfaceToClosePort;
		    	if (StringUtils.isEmpty(interfaceToClosePort)){
		    		interfaceToClosePorttemp = "null";
		    	}
				String[] cmdArr = {
						"bash",
						ServerMain.getInstance().getTomcatLocation()
								+ "adminscripts/enablePort.sh",
						String.valueOf(portToOpen), String.valueOf(portToClose),
						interfaceToOpenPorttemp, interfaceToClosePorttemp };
				syslog.info("command to enableDisablePort is "
						+ Arrays.deepToString(cmdArr));
				final Process pr = Runtime.getRuntime().exec(cmdArr);
				AdminUtil.readStreamOfProcess(pr);
				final int status = pr.waitFor();
				final boolean success = status == 0;
				if (!success) {
					syslog.error("********************************ERROR:: Port not getting opened/closed . Please contact admin.********************************************");
				}
			} catch (Exception e) {
				syslog.error("Error occured while enableDisablePort ",e);
			} 
	 }
	 
	public static void generatePdfFromHtmlFilePath(final String htmlFilePath, final String pdfFileNameWithPath, final String header, final String footer) throws Exception{
		final String[] cmdArr = {
				"sh",ServerMain.getInstance().getTomcatLocation()+ "adminscripts/yahppdf.sh",
				htmlFilePath, pdfFileNameWithPath, header, footer };
		
		AdminUtil.executeLinuxCommand(cmdArr);
	 }
	
	public final static String replaceAPatternInString(final String pattern, final String replacement, final String input){
		if (StringUtils.isEmpty(pattern) || replacement == null || StringUtils.isEmpty(input)){
			return null;
		}
		final Pattern COMPILED_PATTERN = Pattern.compile(pattern);
		final Matcher matcher = COMPILED_PATTERN.matcher(input);
        final String rStr = matcher.replaceAll(replacement);
        return rStr;
		
	}
}
