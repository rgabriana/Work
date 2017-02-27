package com.enlightedinc.events
{
	import flash.events.Event;

	public class TimePeriodFilterEvent extends Event
	{
		public static var FILTER_PERIOD_CHANGE:String = "filterperiodchange";
		private var _string:String;
		public function TimePeriodFilterEvent(type:String, str:String)
		{
			super(type, bubbles, cancelable);
			_string = str;
		}
		override public function clone():Event
		{
			return new TimePeriodFilterEvent(type, label);
		}
		public function get label():String
		{
			return _string;
		}
	}
}