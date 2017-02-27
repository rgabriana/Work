package com.enlightedinc.components.renderers
{
	import com.enlightedinc.components.Constants;
	
	import mx.controls.Alert;
	import mx.controls.Label;
	import mx.core.FlexGlobals;

	public class WidgetFixtureLabelRenderer extends Label
	{
		private const BLACK_COLOR:uint = 0x000000; // Black
		private const GREY_COLOR:uint = 0x7C7C7C; // Grey
		[Bindable]
		public var application:Object = FlexGlobals.topLevelApplication;
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			
			var floorId:Number = application.plotChartView.m_propertyId;
			var PropertyType:String = application.plotChartView.m_propertyType;
			var color:Object;
			if(PropertyType!=Constants.FLOOR)
			{
				color = GREY_COLOR;
			}else
			{	
				if(data.floorId == floorId)
					color = BLACK_COLOR;
				else
					color = GREY_COLOR;
			}
			setStyle("color", color);
		}
	}
}
