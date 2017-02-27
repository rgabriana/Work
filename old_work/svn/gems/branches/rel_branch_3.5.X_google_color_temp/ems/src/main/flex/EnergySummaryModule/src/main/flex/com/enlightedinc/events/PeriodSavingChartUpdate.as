package com.enlightedinc.events
{
	import flash.events.Event;

	public class PeriodSavingChartUpdate extends Event
	{
		public static var PERIOD_CHART_UPDATE:String = "periodchartupdate";
		public static var PERIOD_CHART_RESET:String = "periodchartreset";
		public function PeriodSavingChartUpdate(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
		override public function clone():Event
		{
			return new PeriodSavingChartUpdate(type);
		}
	}
}