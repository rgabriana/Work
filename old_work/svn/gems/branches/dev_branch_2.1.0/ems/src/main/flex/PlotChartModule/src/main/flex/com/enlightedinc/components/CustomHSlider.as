package com.enlightedinc.components
{
	import flash.events.MouseEvent;
	
	import spark.components.HSlider;
	
	public class CustomHSlider extends HSlider
	{
		public function CustomHSlider()
		{
			super();
		}
		
		/*
		* This function is overriden to stop the mouse wheel event on HSlider
		*/
		override protected function system_mouseWheelHandler(event:MouseEvent):void
		{
			
		}
	}
}