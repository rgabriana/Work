package com.enlightedinc.utils
{
	import flash.external.ExternalInterface;
	
	import mx.core.FlexGlobals;
	
	public class CookieUtil
	{
		public function CookieUtil()
		{
		}
		public static function setCookie(name:String, value:Object, days:int):void{
			ExternalInterface.call("setFlashCookies", name, value, days);
		}
		
		public static function getCookie(name:String):Object{
			return ExternalInterface.call("getFlashCookies", name);
		}
		
		public static function deleteCookie(name:String):void{
			ExternalInterface.call("deleteFlashCookies", name);
		}
	}
}