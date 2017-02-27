package com.enlightedinc.events
{
	import flash.events.Event;
	
	public class CurrentSavingChartUpdate extends Event
	{
		public static var CHART_UPDATE:String = "chartupdate";
		public static var UPDATE_CURRENT_COUNT:String = "updatecurrentcount";
		public static var CHART_RESET:String = "chartreset";
		public function CurrentSavingChartUpdate(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
		override public function clone():Event
		{
			return new CurrentSavingChartUpdate(type);
		}
	}
}