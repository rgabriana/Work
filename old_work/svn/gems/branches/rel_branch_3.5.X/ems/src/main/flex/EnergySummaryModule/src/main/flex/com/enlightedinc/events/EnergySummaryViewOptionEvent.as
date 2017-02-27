package com.enlightedinc.events
{
	import flash.events.Event;

	public class EnergySummaryViewOptionEvent extends Event
	{
		public static var VIEW_CHANGE:String = "viewOption";
		private var _string:String;
		public function EnergySummaryViewOptionEvent(type:String, str:String)
		{
			super(type, bubbles, cancelable);
			_string = str;
		}
		override public function clone():Event
		{
			return new EnergySummaryViewOptionEvent(type,label);
		}
		public function get label():String
		{
			return _string;
		}
	}
}