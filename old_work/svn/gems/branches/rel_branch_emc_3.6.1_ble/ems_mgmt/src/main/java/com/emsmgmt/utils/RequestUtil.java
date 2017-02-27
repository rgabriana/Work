package com.emsmgmt.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Convenience class for setting and retrieving cookies.
 */
public class RequestUtil {
	private transient static Log log = LogFactory.getLog(RequestUtil.class);

	/**
	 * Convenience method to set a cookie
	 *
	 * @param response
	 * @param name
	 * @param value
	 * @param path
	 */
	public static void setCookie(HttpServletResponse response, String name,
			String value, String path,int age) {
		if (log.isDebugEnabled()) {
			log.debug("Setting cookie '" + name + "' on path '" + path + "'");
		}

		Cookie cookie = new Cookie(name, value);
		cookie.setSecure(false);
		cookie.setPath(path);
		cookie.setMaxAge(age);

		response.addCookie(cookie);
	}

	/**
	 * Convenience method to get a cookie by name
	 *
	 * @param request the current request
	 * @param name the name of the cookie to find
	 *
	 * @return the cookie (if found), null if not found
	 */
	public static Cookie getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		Cookie returnCookie = null;

		if (cookies == null) {
			return returnCookie;
		}

		for (int i = 0; i < cookies.length; i++) {
			Cookie thisCookie = cookies[i];

			if (thisCookie.getName().equals(name)) {
				// cookies with no value do me no good!
				if (!thisCookie.getValue().equals("")) {
					returnCookie = thisCookie;

					break;
				}
			}
		}

		return returnCookie;
	}

	/**
	 * Convenience method for deleting a cookie by name
	 *
	 * @param response the current web response
	 * @param cookie the cookie to delete
	 * @param path the path on which the cookie was set (i.e. /appfuse)
	 */
	public static void deleteCookie(HttpServletResponse response,
			Cookie cookie, String path) {
		if (cookie != null) {
			// Delete the cookie by setting its maximum age to zero
			cookie.setMaxAge(0);
			cookie.setPath(path);
			response.addCookie(cookie);
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
	
	/**
	 * Convenience method to get the application's URL based on request
	 * variables. 
	 * @return a complete url include host port and context based on the current request. 
	 * e.g. http://www.amprice.de/webapp, http://www.amprice.com/webapp 
	 */
/*	public static String getURLFromRequest() {
		HttpServletRequest request = ServletActionContext.getRequest();
		String requestUrl=request.getRequestURL().toString();
		StringBuffer url=new StringBuffer();
		url.append(requestUrl.substring(0,requestUrl.indexOf("//")+2));
		requestUrl=requestUrl.substring(requestUrl.indexOf("//")+2);
		url.append(requestUrl.substring(0,requestUrl.indexOf('/')));
		url.append(request.getContextPath());
		log.info(url);
		return url.toString();
	}
	*/
	
	/**
	 * Convenience method to get the server session id
	 * @return String
	 * @throws UnsupportedEncodingException
	 */
	public static String getServerSessionId() throws UnsupportedEncodingException{
		   String uid = new java.rmi.server.UID().toString();
		      String sessionID =  URLEncoder.encode(uid,"UTF-8");
		      return sessionID;
		 }
	/**
	 * Convenience method to get the server domain 
	 * @param request
	 * @return String
	 */
	public static String getDomain(HttpServletRequest request){
		String domainName=request.getServerName();
		return domainName;
	}
	/**
	 * Convenience method to get the application's external URL based on request
	 * @param request
	 * @return String
	 *//*
	public static String getAppExternalUrl(HttpServletRequest request){
		String url="";
	    try{
			 String externalp=AppContext.getProperty("externalIp"); 
			 url=externalp+request.getContextPath();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return url;
		
	}*/
	
/*	public static int getPageNo(HttpServletRequest request, String tableId) {
		String paramName = new ParamEncoder(tableId).encodeParameterName(TableTagParameters.PARAMETER_PAGE);
		String paramValue = request.getParameter(paramName);
		if(!ArgumentUtils.isNullOrEmpty(paramValue)){
			return Integer.parseInt(paramValue);
		}
		return 1;
	}
	*/
	public static byte[] getBytesFromFile(File file) throws IOException { 
		InputStream is = new FileInputStream(file); 
		long length = file.length(); 
		byte[] bytes = new byte[(int)length]; 
		int offset = 0; 
		int numRead = 0; 
		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) { 
			offset += numRead; 
		} 
		if (offset < bytes.length) { 
			throw new IOException("Could not completely read file "+file.getName()); 
		} 
		is.close(); 
		return bytes; 
	}
	
	public static void zipFolder(File inFolder, File outFolder){
		try{
			ZipOutputStream out = new ZipOutputStream(new 
					BufferedOutputStream(new FileOutputStream(outFolder)));
			BufferedInputStream in = null;
			byte[] data = new byte[1000];
			String files[] = inFolder.list();
			for (int i=0; i<files.length; i++){
				in = new BufferedInputStream(new FileInputStream
						(inFolder.getPath() + "/" + files[i]), 1000);                  
				out.putNextEntry(new ZipEntry(files[i])); 
				int count;
				while((count = in.read(data,0,1000)) != -1)
				{
					out.write(data, 0, count);
				}
				out.closeEntry();
			}
			out.flush();
			out.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static String getTomcatBasePath(HttpServletRequest request){
		String tomcatBasePath = request.getSession().getServletContext().getRealPath(".."+File.separator+".."+File.separator);
		return tomcatBasePath;
	}
}
