
package com.ems.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;

import com.ems.mvc.util.ControllerUtils;
import com.ems.server.ServerMain;
import com.ems.util.HardwareInfoUtils;
import com.enlightedinc.keyutil.EnlightedKeyGenerator;

/**
 * @author SAMEER SURJIKAR
 * 
 */
public class ExternalRestApiValidationFilter implements Filter {
	


	public void init(FilterConfig filterConfig) throws ServletException {
	}
	
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		String url = request.getServletPath();
		boolean allowedRequest = false;
		 String FILE_TEXT_EXT = ".enlighted";
		// generate session if one doesn't exist
		request.getSession();
		try {
			String filePath =  ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/" ;
			 String fileName = ControllerUtils.getTheLicenseFileName(FILE_TEXT_EXT , filePath) ;
			 String dest = filePath+fileName ;
			 byte[] key = readFromFile(dest);
			if (validated(key)) {
				if(request.getRequestedSessionId() != null && request.isRequestedSessionIdValid())
				 {
					filterChain.doFilter(request, response);
					
				 }
				else
				{
					 response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				}
			 } else
				response.getWriter().println("Key is not valid or The Api Key service time is over. Please contact Enlighted for an upgrade.");
		} catch (IllegalArgumentException lae) {
			response.getWriter().println("Illegal Arguments");
		} catch (AuthenticationException ae) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}catch (ArrayIndexOutOfBoundsException e) 
		{
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not authorized to access this Rest Api.");
		}
		catch (Exception ae) {
			response.getWriter().println("Unknown Exception");
		}

	}
	
	 public static boolean validated(byte[] key)
	    {
	  
	    	boolean valid = false ;
	    	
	    	EnlightedKeyGenerator keyGenerator =  EnlightedKeyGenerator.getInstance() ;	
	    	 keyGenerator.setEncrptedApiKey(key) ;
	    	 byte[] mac = HardwareInfoUtils.getMacAddressForIp(ServerMain.getInstance().getIpAddress("eth0"));
	    	 String macString = HardwareInfoUtils.macBytetoString(':',mac);	
	    	 try {
	    		 	macString =macString.toLowerCase();
	    		 	byte[] salt = macString.getBytes("UTF-8") ;
	    		 	keyGenerator.setSeceretKey(salt);
	    	 	} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
	    	 	}
	    	 String decryptedString = keyGenerator.doDecryption() ;
	    	 String keyMac[] = decryptedString.split("\\|") ;
	    	
	    	 if(keyMac[0].equalsIgnoreCase(macString)&&checkDateValidation(keyMac))
	    	 {
	    		
	    		 valid=true ; 
	    	 }
			
	    	return valid ;
	    }
	 public static boolean checkDateValidation(String[] key)
	 {
		 boolean valid=false ;
		 try{
		 String endDate =key[key.length-1]  ;
		 String startDate= key[key.length-2] ;
		 Calendar start = Calendar.getInstance() ;
		 String temp[] = startDate.split("/") ;
		 start.set(Integer.valueOf(temp[2]).intValue(), Integer.valueOf(temp[0]).intValue()-1,Integer.valueOf(temp[1] ).intValue()) ;
		 Calendar end = Calendar.getInstance() ;
		 temp = endDate.split("/") ;
		 end.set(Integer.valueOf(temp[2]).intValue(), Integer.valueOf(temp[0]).intValue()-1,Integer.valueOf(temp[1] ).intValue()) ;
		 Calendar today = Calendar.getInstance() ;
		 if(start.getTimeInMillis()<=today.getTimeInMillis()&&today.getTimeInMillis()<=end.getTimeInMillis())
		 {
			 valid=true ;
		 }
		
		 return valid;
		 }
		 catch (Exception ex)
		 {
			 return false ;
		 }
		 
		 
	 }
	 public static  byte[] readFromFile(String filePath)
		{
			byte[] fileContent = null ;
			File file = new File(filePath) ;
			try 
			{
				FileInputStream fin = new FileInputStream(file);
				fileContent = new byte[(int)file.length()];
				fin.read(fileContent) ;
				return fileContent ;
				
			}catch(FileNotFoundException e)
		    {
			      System.out.println("File not found" + e);
			    }
			    catch(IOException ioe)
			    {
			      System.out.println("Exception while reading the file " + ioe);
			    }
			return fileContent ;
		}
	public void destroy() {
	}
}

