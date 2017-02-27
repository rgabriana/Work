package com.enlightedinc.events
{
	import flash.events.Event;

	public class DimEvent extends Event
	{
		public static var DIM_FIXTURES:String = "dimFixtures";
		
		private var _dimPercentage:Number;
		private var _minutes:Number;
		
		public function DimEvent(type:String, dimPercentage:Number, minutes:Number)
		{
			super(type, bubbles, cancelable);
			_dimPercentage = dimPercentage;
			_minutes = minutes;
		}
		
		override public function clone():Event
		{
			return new DimEvent(type, dimPercentage, minutes );
		}
		
		public function get dimPercentage():Number
		{
			return _dimPercentage;
		}
		
		public function get minutes():Number
		{
			return _minutes;
		}
	}
}