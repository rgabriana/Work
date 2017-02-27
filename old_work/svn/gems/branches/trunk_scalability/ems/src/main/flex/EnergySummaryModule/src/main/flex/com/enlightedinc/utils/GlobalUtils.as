package com.enlightedinc.utils
{
	import com.enlightedinc.components.Constants;
	
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	import flash.utils.ByteArray;
	
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
		
		/**
		 * Converts Energy Value into order of magnitude and returns string value.
		 */		
		public static function getCustomEnergyLabel(labelValue:Object):String
		{
			if (Number(labelValue) >= 1000000)
				return (Number(labelValue)/1000000).toFixed(2) +" MWh";				
			if (Number(labelValue) >= 1000)
				return (Number(labelValue)/1000).toFixed(2) +" kWh";
			else
				return Number(labelValue).toFixed(2) +" Wh";
		}
		/**
		 * Converts Carbon Value into order of magnitude and returns string value.
		 */		
		public static function getCarbonUnitEnergyLabel(labelValue:Object):String
		{
			if (Number(labelValue)>= 1000000)
				return (Number(labelValue)/1000000).toFixed(2) +" mtons";				
			else if (Number(labelValue) >= 1000)
				return (Number(labelValue)/1000).toFixed(2) +" ktons";
			else if(Number(labelValue) < 1000 && Number(labelValue) >= 1)
				return Number(labelValue).toFixed(2) +" tons";
			else
			{
				var carbon:Number= Number(labelValue) * Constants.METRIC_POUND_CONVERSION_FACTOR;
				return carbon.toFixed(2) +" lb";
			}
		}
		/**
		 * Converts Money Value into 2 digit decimal precision.
		 */		
		public static function getCustomMoneyLabel(labelValue:Object):String{
			return "$ "+  Number(labelValue).toFixed(2);
		}
		
		/**
		 * Method to Checks for Not a Number
		 * @Returns Number
		 */
		public static function checkNaN(value:*):Number
		{
			if(isNaN(value)){
				return 0;
			}
			return value;
		}
		public static function clone( source:Object ) : *
		{
			var byteArray:ByteArray = new ByteArray();
			byteArray.writeObject( source );
			byteArray.position = 0;
			return byteArray.readObject();
		}
	}
}