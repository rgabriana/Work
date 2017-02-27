package com.enlightedinc.events
{
	import flash.events.Event;

	public class EnergySummaryUnitBarEvent extends Event
	{
		public static var UNIT_CHANGE:String = "unitChange";
		private var _string:String;
		public function EnergySummaryUnitBarEvent(type:String, str:String)
		{
			super(type, bubbles, cancelable);
			_string = str;
		}
		override public function clone():Event
		{
			return new EnergySummaryUnitBarEvent(type,label);
		}
		public function get label():String
		{
			return _string;
		}
	}
}