package com.emscloud.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class UTCConverter {
	
	
	public static String getUTCTimestamp(Date timestamp,String timeZone) throws ParseException {
		
		TimeZone tz = Calendar.getInstance().getTimeZone();
		String dateString = "";
		if(timestamp != null ) {
			Date newdate = new Date(timestamp.getTime()- tz.getRawOffset());
			DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			dateString = dateFormat1.format(newdate);
		}
		
		return dateString;
		

}
	
	public static String getUTCTime(Date timestamp) throws ParseException {
		
		TimeZone tz = Calendar.getInstance().getTimeZone();
		String dateString = "";
		if(timestamp != null ) {
			Date newdate = new Date(timestamp.getTime()- tz.getRawOffset());
			DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			dateString = dateFormat1.format(newdate);
		}
		
		return dateString;
	}
	
}
