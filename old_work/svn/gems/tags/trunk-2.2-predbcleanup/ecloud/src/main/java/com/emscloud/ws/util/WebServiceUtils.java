package com.emscloud.ws.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.apache.log4j.Logger;






/**
 * @author Sameer Surjikar
 * 
 */
public class WebServiceUtils {
	static final Logger logger = Logger.getLogger("EMS_DASHBOARD");
	
	


	/**
	 * @param toChangedate
	 * @return Date in GMT TimeZone
	 */
	public static Date convertDateToGMT(Date toChangedate)
	{
		 Calendar c = Calendar.getInstance();
		 		c.setTime(toChangedate);	   
		    TimeZone z = c.getTimeZone();
		    int offset = z.getRawOffset();
		    if(z.inDaylightTime(new Date())){
		        offset = offset + z.getDSTSavings();
		    }
		    int offsetHrs = offset / 1000 / 60 / 60;
		    int offsetMins = offset / 1000 / 60 % 60;
		    c.add(Calendar.HOUR_OF_DAY, (-offsetHrs));
		    c.add(Calendar.MINUTE, (-offsetMins));
		return c.getTime();
		
	}

	/**
	 * @return Server Time Zone.
	 */
	public static TimeZone getServerTimeZone() {
		final TimeZone timeZone = TimeZone.getDefault();
				return timeZone;
	}
	/**
	 * @return Server Time Zone.
	 */
	public static String getServerTimeZoneOffsetFromGMT() {
		 Calendar c = Calendar.getInstance();
	 		c.setTime(new Date());	   
	    TimeZone z = c.getTimeZone();
	    int rawoffset = z.getRawOffset();
	    if(z.inDaylightTime(new Date())){
	    	rawoffset = rawoffset + z.getDSTSavings();
	    }
	    int offsetHrs = rawoffset / 1000 / 60 / 60;
	    int offsetMins = rawoffset / 1000 / 60 % 60 ;
		  String offset = String.valueOf(offsetHrs) +":"+ String.valueOf(offsetMins) ;
		  return offset ;
	}
	public static void writeToFile(InputStream uploadedInputStream,
			String uploadedFileLocation) {
		OutputStream out = null;
			try {
				 out = new FileOutputStream(new File(
						uploadedFileLocation));
				int read = 0;
				byte[] bytes = new byte[1024];
				while ((read = uploadedInputStream.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
				out.flush();	
			} catch (IOException e) {
				
				e.printStackTrace();
			} finally 
			{
				try {
					out.close() ;
					uploadedInputStream.close() ;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	 
		}
}
