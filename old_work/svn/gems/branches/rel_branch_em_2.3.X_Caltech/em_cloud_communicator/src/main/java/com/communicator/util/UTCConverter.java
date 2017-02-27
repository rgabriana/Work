package com.communicator.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UTCConverter {
	
	
	public static Timestamp getUTCTimestamp(Timestamp timestamp,TimeZone tz) throws ParseException {
		Date date = new Date(timestamp.getTime());
		DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormat1.setTimeZone(tz);
		
		
		
		DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormat2.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date newdate = dateFormat2.parse(dateFormat1.format(date));
		long time = newdate.getTime();
		
		return new Timestamp(time);
		

}
}	
