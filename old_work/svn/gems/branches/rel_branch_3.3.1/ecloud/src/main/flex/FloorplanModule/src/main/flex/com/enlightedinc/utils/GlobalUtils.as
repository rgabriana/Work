package com.enlightedinc.utils
{
	import com.enlightedinc.components.Constants;
	
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	import flash.utils.ByteArray;
	
	import mx.charts.series.items.PlotSeriesItem;
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.core.FlexGlobals;
	
	public class GlobalUtils
	{
		public static function LogOut():void
		{
			// Once Session timeout - Moved to logout page
			var contentRoot:String = FlexGlobals.topLevelApplication.parameters.contextRoot;
			var logOutURL:String = contentRoot + "/logout.jsp";
			navigateToURL(new URLRequest(logOutURL), "_self");
		}
		
		public static function globalFaultHandler(statusCode:Number):Boolean{
			var flag:Boolean =false;
			// Check for session timeout
			if(statusCode == Constants.SESSION_TIME_OUT){
				flag = true;
				// Session timedout; show login page
				LogOut();
			}
			return flag;
		}
		
		public static function setDefaultProfileName(currentProfileName:String):String{
			var currProfile :String = currentProfileName;
			var defaultStr :String = "Default";
			if(currProfile.toUpperCase()!= defaultStr.toUpperCase() && (currProfile.indexOf("_Default")==-1))
			{
				currentProfileName=currProfile+"_Default";
			}
			return currentProfileName;
		}
		
		public static function createDate(str:String) : Date
		{
			var date:Date = new Date();
			date.setFullYear(str.slice(0,4));
			date.setMonth(str.slice(5,7));
			date.setMonth(date.getMonth() - 1);
			date.setDate(str.slice(8,10));
			date.setHours(str.slice(11,13));
			date.setMinutes(str.slice(14,16));
			date.setSeconds(str.slice(17,19));
			return date;
		}
		
		public static function sortData( data:ArrayCollection, sortString:String) : ArrayCollection
		{
			var sortField:SortField  = new SortField();
			sortField.name = sortString;
			
			var sort:Sort = new Sort();
			sort.fields = [sortField];
			
			data.sort = sort;
			data.refresh();
			
			return data;
		}
		
		public static function getServerTime(serverGMTOffset:Number) : Date
		{
			var date:Date = new Date();
			
			// Get offset from GMT in milliseconds
			var offsetMilliseconds:Number = date.getTimezoneOffset() * 60 * 1000;
			var serverOffsetMilliseconds:Number = serverGMTOffset * 60 * 1000;
			
			// Convert the date to server time by adding the offsets from GMT
			date.setTime(date.getTime() + offsetMilliseconds + serverOffsetMilliseconds);
			
			return date;
		}
		
		public static function GetFactorValue(height:Number, width:Number):Number
		{	
			var idealWidth:Number = 275;
			var idealHeight:Number = 430;
			var factor:Number = 1;
			var tempWidth:Number = width;
			var tempHeight:Number = height;
			
			if(height <= idealHeight && width <= idealWidth)
			{
				return factor;
			}
			while(tempHeight > idealHeight || tempWidth > idealWidth)
			{
				tempWidth = width;
				tempHeight = height;
				factor = factor + 0.5;
				tempHeight = tempHeight / factor;
				tempWidth = tempWidth / factor;
			}
			return factor;
		}
		
		public static  function roundValue( roundTo:Number, value:int ) : Number
		{
			return (Math.round(value/roundTo) * roundTo);
		}
		
		public static function clone( source:Object ) : *
		{
			var byteArray:ByteArray = new ByteArray();
			byteArray.writeObject( source );
			byteArray.position = 0;
			return byteArray.readObject();
		}
		
		// This function iterates through a selectedItems list and returns true
		// if the searchItem is found in the list
		public static function isSelected(searchItem:Object,selectedItems:Array) : Boolean
		{
			var arrayLength:int = selectedItems.length;
			
			for (var j:int=0; j<arrayLength; j++)
			{
				if(selectedItems[j] is PlotSeriesItem)
				{
					if(selectedItems[j].item.id == searchItem.id)
					{
						return true;
					}
				}
				else
				{
					if(selectedItems[j].id == searchItem.id)
					{
						return true;
					}
				}
			}
			return false;
		}
		
	}
}