package com.enlightedinc.events
{
	import flash.events.Event;

	public class LocationServiceEvent extends Event
	{
		public static var LOCATION_CHANGE:String = "locationChange";
		private var _location:String;
		
		public function LocationServiceEvent(type:String, str:String)
		{
			super(type, bubbles, cancelable);
			_location = str;
		}

		public function get location():String
		{
			return _location;
		}
		override public function clone():Event
		{
			return new LocationServiceEvent(type,location);
		}
	}
}