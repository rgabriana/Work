package com.emsdashboard.ws.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.emsdashboard.model.DashboardRecord;




/**
 * @author Sameer Surjikar
 * 
 */
public class WebServiceUtils {
	static final Logger logger = Logger.getLogger("EMS_DASHBOARD");
	
	/**
	 * @param Takes
	 *            in oRecords (DashboardRecord ) with local timezone.
	 * @return List of Records ( DashboardRecord ) with GMT timezone
	 */
	@SuppressWarnings("unused")
	public static List<DashboardRecord> convertDashboardRecordToGMT(
			List<DashboardRecord> oRecords) {
		if ((!oRecords.isEmpty()) || (oRecords != null)) {
			List<DashboardRecord> gmtRecords = new ArrayList<DashboardRecord>();
			TimeZone localTimeZone = getServerTimeZone();
			Calendar oldCal = Calendar.getInstance(getServerTimeZone()) ;		
			Calendar newCal = Calendar.getInstance(new SimpleTimeZone(0, "GMT")) ;
			DashboardRecord record = null;
			Date recordDate = null;
			Iterator<DashboardRecord> itr = oRecords.iterator();
			while (itr.hasNext()) {
				record = itr.next();
				recordDate = record.getCaptureOn();			
				oldCal.setTime(recordDate) ;
				newCal.setTimeInMillis(oldCal.getTimeInMillis() + TimeZone.getTimeZone("GMT").getOffset(oldCal.getTimeInMillis()) - localTimeZone.getOffset(oldCal.getTimeInMillis())) ;
				record.setCaptureOn(newCal.getTime());
				gmtRecords.add(record);

			}

			return gmtRecords;
		} else {
			return oRecords;
		}
	}


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
}
