package com.ems.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;

public class DateUtil {

	public static final String DEFAULT_FORMAT = "dd/MM/yyyy";
	public static final String SQL_FORMAT = "yyyy-MM-dd";
	public static final String INTERNATIONAL_FORMAT = "yyyy-MM-dd : hh:mm:ss a";
	public static final long MILLISECS_PER_MINUTE = 60*1000;
	public static final long MILLISECS_PER_HOUR   = 60*MILLISECS_PER_MINUTE;
	protected static final long MILLISECS_PER_DAY = 24*MILLISECS_PER_HOUR;
	static final Logger logger = Logger.getLogger(DateUtil.class.getName());

	public static boolean isValidDate(String dateString,String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			sdf.parse(dateString);
		} catch (ParseException e) {
			return false;
		}
		return true;
	}

	public static Date parseString(String str,String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			return sdf.parse(str);
		} catch (ParseException e) {
			return null;
		}
	}

	public static Date parseString(String str) {
		return parseString(str,DEFAULT_FORMAT);
	}

	public static String formatDate(Date date,String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	public static String formatDate(Date date){
		return formatDate(date, DEFAULT_FORMAT);
	}

	public static List<String> getYearList() {

		Calendar calendar = new GregorianCalendar();
		int startYear = calendar.get(Calendar.YEAR);
		int endYear = Integer.parseInt(AppContext.getProperty("date.min.year"));
		List<String> yearList = new ArrayList<String>();
		int index = 0;
		yearList.add(index, "Year");
		for (int i = startYear; i >= endYear; i--) {
			index = (startYear + 1) - i;
			yearList.add(index, Integer.toString(i));
		}
		return yearList;
	}

	public static Date addDays(Date date, int days) {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DATE, days);
			return calendar.getTime();
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean checkDate(String day, String month, String year) {
		int iDay = Integer.parseInt(day);
		int iMonth = Integer.parseInt(month);
		int iYear = Integer.parseInt(year);
		int monthDays[] = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		int temp = iYear;
		if(temp%100==0){
			temp=temp/100;
		}
		if(temp%4==0){
			monthDays[2]=monthDays[2]+1;
		}
		if(iDay>monthDays[iMonth])
			return false;
		return true;
	}

	public static boolean isEqualDate(Date date,Date date2){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date d1 = df.parse(formatDate(date, "yyyy-MM-dd"));
			Date d2 = df.parse(formatDate(date2, "yyyy-MM-dd"));
			if (d1.equals(d2))
				return true;
			else return false;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * this function will convert string date from MM/dd/yyyy to yyyy-MM-dd
	 * @return
	 */
	public static String formatDate(String date){
		String[]arr=date.split("/");
		if(arr==null || arr.length<2){
			logger.error("Invalid date format >>>>> "+date);
			return null;
		}
		return arr[2]+"-"+arr[0]+"-"+arr[1];
	}

	private static int convertTimeToInt(String time){
		int hour = 0;
		int min = 0;
		boolean isMorning = true;
		if(!ArgumentUtils.isNullOrEmpty(time)){
			hour = Integer.valueOf(time.substring(0, time.indexOf(":")));
			min = Integer.valueOf(time.substring(time.indexOf(":")+1, time.indexOf(" ")));
			isMorning = (time.substring(time.indexOf(" ")+1, time.length())).equals("AM");
		}
		if((!isMorning && hour!=12) || (isMorning && hour==12)){
			hour = (hour+12)%24;
		}
		return ((hour*100)+min);
	}
	
	public static void setTimeInCalendar(String time, Calendar calendar){
		int hour = 0;
		int min = 0;
		int timeAsInt = convertTimeToInt(time);
		if(timeAsInt > 0){
			hour = timeAsInt/100;
			min = timeAsInt - (hour*100);
		}
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, min);
	}
	
	/**
	 * Sets time in HH:MM a format into a specified java.util.Date date
	 * 
	 * @param time
	 * @param date
	 * @return date after setting the time in date
	 */
	public static Date setTimeInDate(String time, Date date){
		Calendar calendar = Calendar.getInstance();
		if(date==null){
			date = new Date();
		}
		calendar.setTime(date);
		setTimeInCalendar(time, calendar);
		return calendar.getTime();
	}

	public static long diffDayPeriods(Date from,Date to) {
		Calendar end = Calendar.getInstance();
		end.setTime(to);
		Calendar start = Calendar.getInstance();
		start.setTime(from);
		long endL   =  end.getTimeInMillis() +  end.getTimeZone().getOffset(  end.getTimeInMillis() );
		long startL = start.getTimeInMillis() + start.getTimeZone().getOffset( start.getTimeInMillis() );
		System.out.println("ssss"+(endL - startL) / MILLISECS_PER_DAY);
		return (endL - startL) / MILLISECS_PER_DAY;
	}
	
	public static boolean isWithinDateRange(Date dateToSearch, Date startdate, Date enddate) {
		  Calendar calstart = Calendar.getInstance();
		  calstart.setTime(startdate);
		  calstart.set(Calendar.HOUR, 0);
		  calstart.set(Calendar.MINUTE, 0);
		  calstart.set(Calendar.SECOND, 0);
		 
		 
		  Calendar calend = Calendar.getInstance();
		  calend.setTime(enddate);
		  calend.set(Calendar.HOUR, 0);
		  calend.set(Calendar.MINUTE, 0);
		  calend.set(Calendar.SECOND, 0);
		 
		 
		  Calendar calsearch = Calendar.getInstance();
		  calsearch.setTime(dateToSearch);
		  calsearch.set(Calendar.HOUR, 0);
		  calsearch.set(Calendar.MINUTE, 0);
		  calsearch.set(Calendar.SECOND, 0);
		 System.out.println(calstart.getTime());
		 System.out.println(calend.getTime());
		 System.out.println(calsearch.getTime());
		 System.out.println(isDateEqual(calstart.getTime(), calsearch.getTime())
				  +"::"+ (calstart.getTime().before(calsearch.getTime()))
				  +"::"+(isDateEqual(calend.getTime(), calsearch.getTime())
						  +"::"+ (calend.getTime().after(calsearch.getTime()))));
		  if (((
				  isDateEqual(calstart.getTime(), calsearch.getTime())) 
				  || (calstart.getTime().before(calsearch.getTime()))) 
				  &&((
						  isDateEqual(calend.getTime(), calsearch.getTime())) 
						  || (calend.getTime().after(calsearch.getTime())))){
			  return true;
		  }else{
			  return false;
		  }
		}
		 
		 
		public static boolean isDateEqual(Date date1, Date date2) {
		  Calendar cal1 = Calendar.getInstance();
		  Calendar cal2 = Calendar.getInstance();
		  cal1.setTime(date1);
		  cal2.setTime(date2);
		  if ((cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) && (cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE)) && (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))) return true;
		  else return false;
		}
		
	public static Date truncateToPrevious15min(Date truncateDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(truncateDate);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		int unroundedMinutes = calendar.get(Calendar.MINUTE);
		int mod = unroundedMinutes % 15;
		calendar.add(Calendar.MINUTE,  -mod);
		return calendar.getTime();
	}
	public static Date truncateToFuture15min(Date truncateDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(truncateDate);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		int unroundedMinutes = calendar.get(Calendar.MINUTE);
		int mod = unroundedMinutes % 15;
		calendar.add(Calendar.MINUTE,  (15-mod));
		return calendar.getTime();
	}
	
	public static long getDateDiffInMinutes(Date beforeDate, Date afterDate) {
		 if( beforeDate == null || afterDate == null ) return 0;
		    return (int)((afterDate.getTime()/60000) - (beforeDate.getTime()/60000));
	}

		
		public static void main(String[] args) {
			Date enddate = new Date();
			Date startdate = DateUtil.addDays(enddate, -30);
			Date dateToSearch = DateUtil.addDays(enddate, -30);
			System.out.println(DateUtil.isWithinDateRange(dateToSearch, startdate, enddate));
		}
}
