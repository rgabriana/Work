package com.enlightedinc.utils
{
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	
	import mx.core.FlexGlobals;

	public class GlobalUtils
	{
		public static function LogOut():void
		{
			// Once Session timeout - Moved to logout page
			var contentRoot:String = FlexGlobals.topLevelApplication.parameters.contextRoot;
			var logOutURL:String = contentRoot + "/logout.jsp";
			flash.net.navigateToURL(new URLRequest(logOutURL), "_self");
		}
		
		/**
		 * compares two dates, returning -1 if the first date is before the second,
		 * 0 if the dates are equal, 
		 * or 1 if the first date is after the second:
		 */
		public static function dateCompare (date1 : Date, date2 : Date) : Number
		{
			date1.setHours(0);
			date1.setMinutes(0);
			date1.setSeconds(0);
			
			var date1Timestamp : Number = date1.getTime ();
			
			date2.setHours(0);
			date2.setMinutes(0);
			date2.setSeconds(0);
			
			var date2Timestamp : Number = date2.getTime ();
			var result : Number = -1;
			if (date1Timestamp == date2Timestamp)
			{
				result = 0;
			}
			else if (date1Timestamp > date2Timestamp)
			{
				result = 1;
			}
			return result;
		} 
		
		/**
		 * Returns the Server time offset where the EM Application is running
		 */		
		public static function getServerTime() : Date
		{
			var date:Date = new Date();
			
			// Get offset from GMT in milliseconds
			var offsetMilliseconds:Number = date.getTimezoneOffset() * 60 * 1000;
			var serverOffsetMilliseconds:Number =  FlexGlobals.topLevelApplication.serverGMTOffset * 60 * 1000;
			
			// Convert the date to server time by adding the offsets from GMT
			date.setTime(date.getTime() + offsetMilliseconds + serverOffsetMilliseconds);
			
			return date;
		}
	}
}