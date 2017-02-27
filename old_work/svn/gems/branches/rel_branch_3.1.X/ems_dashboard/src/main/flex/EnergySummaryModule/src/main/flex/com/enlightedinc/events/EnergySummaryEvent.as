package com.enlightedinc.events
{
	import flash.events.Event;

	public class EnergySummaryEvent extends Event
	{
		public static var OPENURL:String = "openurl";
		public static var REFRESH:String = "refresh";
		
		public function EnergySummaryEvent(type:String)
		{
			super(type, true, false);
		}
		
		override public function clone():Event
		{
			return new EnergySummaryEvent(type);
		}
	}
}