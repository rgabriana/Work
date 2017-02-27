package com.emscloud.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class CommonUtils {
	
	public static Logger logger = Logger.getLogger(CommonUtils.class.getName());
	
	public static int getRandomPort()
	{
		 int port = 0 ;
		 try {
		ServerSocket server =
			    new ServerSocket(0);
			   port = server.getLocalPort();
			  server.close();
		 }catch(Exception e)
		 {
			 logger.info(e.getMessage());
			 port =0 ;
		 }
			  
			  return port ;
	}
	
	public static String getHostName()
	{
		String hostName = null ;
		 Runtime run = Runtime.getRuntime();
			try {
				Process pr = run.exec("hostname");
				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				String line = "";
				while ((line=buf.readLine())!=null) {
					hostName = line ;
				}
			    pr.waitFor();
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			}catch (Exception e)
			{
				logger.error(e.getMessage());
			}
			
			return hostName ;
	}
	
	public static void reloadApache()
	{
		 Runtime run = Runtime.getRuntime();
			try {
				Process pr = run.exec("sudo /etc/init.d/apache2 reload");
				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				String line = "";
				while ((line=buf.readLine())!=null) {
					System.out.println(line);
					logger.info(line) ;
				}
			    pr.waitFor();
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			}catch (Exception e)
			{
				logger.error(e.getMessage());
			}
			
	}
	/**
	 * Convenience method to get the application's URL based on request
	 * variables.
	 */
	public static String getAppURL(HttpServletRequest request) {
		StringBuffer url = new StringBuffer();
		int port = request.getServerPort();
		if (port < 0) {
			port = 80; // Work around java.net.URL bug
		}
		String scheme = request.getScheme();
		url.append(scheme);
		url.append("://");
		url.append(request.getServerName());
		if ((scheme.equals("http") && (port != 80)) || (scheme.equals("https") && (port != 443))) {
			url.append(':');
			url.append(port);
		}
		url.append(request.getContextPath());
		return url.toString();
	}

}
