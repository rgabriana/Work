package com.enlightedinc.events
{
	import flash.events.Event;
	
	public class AllPlugloadChartUpdate extends Event
	{
		public static var ALL_PLUGLOAD_CHART_UPDATE:String = "allplugloadchartupdate";
		public static var CHART_RESET:String = "chartreset";
		public function AllPlugloadChartUpdate(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
		override public function clone():Event
		{
			return new AllPlugloadChartUpdate(type);
		}
	}
}