package com.enlightedinc.events
{
	import flash.events.Event;

	public class ScreenChangeEvent extends Event
	{		
		public static var SCREEN_CHANGE:String = "screenChange";
		
		private var _string:String;
		
		public function ScreenChangeEvent(type:String, str:String)
		{
			super(type, bubbles, cancelable);
			_string = str;
		}
		
		override public function clone():Event
		{
			return new ScreenChangeEvent(type, label);
		}
		
		public function get label():String
		{
			return _string;
		}
	}
}