package com.ems.ws.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.springframework.context.i18n.LocaleContextHolder;

import com.ems.model.DashboardRecord;
import com.ems.service.EnergyConsumptionManager;

/**
 * @author Sameer Surjikar
 * 
 */
public class WebServiceUtils {
	static final Logger logger = Logger.getLogger(WebServiceUtils.class
			.getName());

	/**
	 * @param Takes
	 *            in oRecords (DashboardRecord ) with local timezone.
	 * @return List of Records ( DashboardRecord ) with GMT timezone
	 */
	@SuppressWarnings("unused")
	public static List<DashboardRecord> convertToGMT(
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
	 * @return Server Time Zone.
	 */
	public static TimeZone getServerTimeZone() {
		final TimeZone timeZone = TimeZone.getDefault();
		// We need to do this steps for taking into consideration the daylight
		// saving . If the administrator changes the time
		// on the server, this change won't be reflected in subsequent calls.
		// What you need to do instead, is to find out is the
		// daylight savings is on right now. The to step below give us proper
		// display name even in the senario where some timezones change their
		// names during daylight saving.
		final boolean daylight = timeZone.inDaylightTime(new Date());
		final Locale locale = LocaleContextHolder.getLocale();
		timeZone.getDisplayName(daylight, TimeZone.LONG, locale);
		return timeZone;
	}
}
