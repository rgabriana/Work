package com.enlightedinc.events
{
	import flash.events.Event;

	public class EnergySummaryDockEvent extends Event
	{
		public static var CLICK_EVENT:String = "dockClick";
		
		public static var REFRESH_SCREEN:String = "refreshScreen";
		
		public static var EXPORT:String = "export";
		
		private var _string:String;
		
		public function EnergySummaryDockEvent(type:String, str:String)
		{
			super(type, bubbles, cancelable);
			_string = str;
		}
		
		override public function clone():Event
		{
			return new EnergySummaryDockEvent(type, label);
		}
		
		public function get label():String
		{
			return _string;
		}
	}
}