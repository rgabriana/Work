package com.enlightedinc.components
{
	import flash.events.MouseEvent;
	
	import spark.components.VSlider;
	
	public class CustomVSlider extends VSlider
	{
		public function CustomVSlider()
		{
			super();
		}
		
		/*
		 * This function is overriden to stop the mouse wheel event on VSlider
		*/
		override protected function system_mouseWheelHandler(event:MouseEvent):void
		{
			
		}
	}
}