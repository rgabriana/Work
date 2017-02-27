package com.enlightedinc.events
{
	import flash.events.Event;
	
	// Event that handles Snap to Grid for PlotChart

	public class SearchEvent extends Event
	{
		public static var SEARCH_COMPLETE:String = "searchComplete";
		
		private var _obj:Object;
		
		public function SearchEvent(type:String, obj:Object)
		{
			super(type, bubbles, cancelable);
			_obj = obj;
		}
		
		override public function clone():Event
		{
			return new SearchEvent(type, searchObject);
		}
		
		public function get searchObject():Object
		{
			return _obj;
		}
	}
}