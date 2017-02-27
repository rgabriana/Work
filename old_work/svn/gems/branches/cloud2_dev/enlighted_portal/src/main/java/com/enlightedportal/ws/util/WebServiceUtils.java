package com.enlightedportal.ws.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
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
	
	public static void copyfile(String srFile, String dtFile){
		  try{
		  File f1 = new File(srFile);
		  File f2 = new File(dtFile);
		  InputStream in = new FileInputStream(f1);
		
		  //For Overwrite the file.
		  OutputStream out = new FileOutputStream(f2);

		  byte[] buf = new byte[1024];
		  int len;
		  while ((len = in.read(buf)) > 0){
		  out.write(buf, 0, len);
		  }
		  in.close();
		  out.close();
		 
		  }
		  catch(FileNotFoundException ex){
		 
		  System.exit(0);
		  }
		  catch(IOException e){
	
		  }
		  }
}
