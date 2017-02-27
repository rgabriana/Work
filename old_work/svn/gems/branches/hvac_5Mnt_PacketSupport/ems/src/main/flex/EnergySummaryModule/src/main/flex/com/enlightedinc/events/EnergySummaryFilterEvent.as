package com.enlightedinc.events
{
	import flash.events.Event;

	public class EnergySummaryFilterEvent extends Event
	{
		public static var FILTER_ENERGY_SUMMARY:String = "filterEnergySummary";
		
		private var _fromDate:Date;
		private var _toDate:Date;
		
		public function EnergySummaryFilterEvent(type:String, fromDate:Date, toDate:Date)
		{
			super(type, bubbles, cancelable);
			_fromDate = fromDate;
			_toDate = toDate;
		}
		
		override public function clone():Event
		{
			return new EnergySummaryFilterEvent(type, fromDate, toDate);
		}
		
		public function get fromDate():Date
		{
			return _fromDate;
		}
		
		public function get toDate():Date
		{
			return _toDate;
		}
	}
}