package com.enlightedinc.events
{
	import flash.events.Event;

	public class DockEvent extends Event
	{
		public static var AUTO_CLICK:String = "autoClick";
		
		public static var REALTIME_CLICK:String = "realtimeClick";
		
		public static var REFRESH_SCREEN:String = "refreshScreen";
		
		public static var SHOW_FLOOR_PLAN:String = "showFloorPlan";
		
		public static var SHOW_REPORTS:String = "showReports";
		
		private var _string:String;
		
		public function DockEvent(type:String, str:String)
		{
			super(type, bubbles, cancelable);
			_string = str;
		}
		
		override public function clone():Event
		{
			return new DockEvent(type, label);
		}
		
		public function get label():String
		{
			return _string;
		}
	}
}