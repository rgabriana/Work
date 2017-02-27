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
			navigateToURL(new URLRequest(logOutURL), "_self");
		}
		
		public static function getMacAddress(macAddress:String):String{
			var strMacAddress:String = "";
			var macSplitArray:Array;
			macSplitArray = macAddress.split(':');
			for (var i:int = 0; i < macSplitArray.length; i++) {
				if(macSplitArray[i].length == 1){
					macSplitArray[i] = "0"+macSplitArray[i];
				}
				if(i == 0){
					strMacAddress = macSplitArray[i];
				}else{
					strMacAddress = strMacAddress + ":" + macSplitArray[i];
				}
			}
			return strMacAddress;
		}
	}
}