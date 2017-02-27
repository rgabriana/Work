package com.enlightedinc.components.renderers
{
	import com.enlightedinc.assets.images.Images;
	import com.enlightedinc.components.Constants;
	
	import flash.display.Shape;
	import flash.events.MouseEvent;
	import flash.external.ExternalInterface;
	import flash.geom.Rectangle;
	
	import mx.charts.series.items.PlotSeriesItem;
	import mx.controls.Image;
	import mx.controls.Label;
	import mx.core.FlexGlobals;
	import mx.core.IDataRenderer;
	import mx.core.UIComponent;
		
	/**
	 *  Chart itemRenderer implementation 
	 */
	public class WdsIconRenderer extends UIComponent implements IDataRenderer
	{
		private var _chartItem:Object;
		private var value:Number;
		private var batteryLevel:String;
		
		/*** Embed WDS icons ***/
		[Bindable]
		public static var wdsRendererType:String = "";
		[Bindable]
		public static var wdsImageStatus:String = "";

		private var bChanged:Boolean = false;
		private var _image:Image;
		private var _label:Label;
		public function WdsIconRenderer() 
		{
			super();
			name = Constants.WDS_RENDERER;
			this.addEventListener(MouseEvent.DOUBLE_CLICK, showSwitchDetails);
		}		
		public function dispose():void {}
		public function get data():Object
		{
			return _chartItem;
		}		
		public function set data(value:Object):void
		{
			if (_chartItem == value)
				return;	
			_chartItem = value;
			bChanged = true;
			//trace("set");
			invalidateProperties();
		}
		private function updateWdsNetworkStatus():void
		{
			var secGwId:Number = _chartItem.item.gatewayid;
			var color:uint = parentDocument.getColorDictionary(secGwId);
			var rect:Shape = new Shape();
			var borderCol:uint=color;
			var borderSize:int=1.5;
			rect.name="showWdsRect";
			rect.graphics.beginFill(0xFFFFFF);
			rect.graphics.lineStyle(borderSize, borderCol, 1.0);
			rect.graphics.drawRect(_image.x, _image.y, 16,20);
			rect.graphics.endFill();
			
			rect.graphics.lineStyle(borderSize, color, 1.0);
			rect.graphics.moveTo(8,7);
			rect.graphics.lineTo(16,7);
			
			rect.graphics.lineStyle(borderSize, color, 1.0);
			rect.graphics.moveTo(8,13);
			rect.graphics.lineTo(16,13);
			
			_image.source=rect;
		}
		private function updateERCLevel() : void
		{
			batteryLevel = _chartItem.item.batteryLevel;
			if(batteryLevel == Constants.ERC_BATTERY_NORMAL)
			{
				_image.source = Images.ERCHEALTHY;
			}
			else if(batteryLevel == Constants.ERC_BATTERY_LOW)
			{
				_image.source = Images.ERCLOW;
			}
			else if(batteryLevel == Constants.ERC_BATTERY_CRITICAL)
			{
				_image.source = Images.ERCCRITICAL;
			}else {
				_image.source = Images.ERCNA;
			}
		}
		override protected function createChildren():void
		{
			super.createChildren();            
			_image = new Image();
			_image.alpha = 1.0;
			_image.width = 7;
			_image.height = 7;
			_image.buttonMode = true;
			addChild(_image);
			_label = new Label();
			_label.setStyle("fontWeight","normal");
			addChild(_label);
			//trace("create");
		}		 
		override protected function commitProperties():void {
			super.commitProperties();
			if (bChanged) {
				if(_chartItem != null)
				{
					if (PlotSeriesItem(_chartItem).item != null)
					{
						if (_image != null) {
							//_image.source = Images.WDS;
							updateERCLevel();
						}
						if(_label && wdsRendererType == Constants.IMAGE_UPGRADE)
						{
							_label.text = "";
							_label.text = _chartItem.item.imageUpgradeStatus;
						}else if(wdsRendererType == Constants.SHOW_NETWORK)
						{
							updateWdsNetworkStatus();
						}
					} 
				}
				bChanged = false;
			}
		}
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
		{
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			
			if (_image != null)
			{
				var x:int = unscaledWidth/2 - _image.getExplicitOrMeasuredHeight()/2;
				_image.setActualSize(_image.getExplicitOrMeasuredWidth()+60,_image.getExplicitOrMeasuredHeight());
				_image.move(unscaledWidth/2 - _image.getExplicitOrMeasuredHeight()/2 ,0);
				
				x = x + _image.getExplicitOrMeasuredWidth() / 2 - _label.getExplicitOrMeasuredWidth() / 2;
				_label.setActualSize(_label.getExplicitOrMeasuredWidth(),_label.getExplicitOrMeasuredHeight());
				_label.move(x ,-15);
			}
		}

		private function showSwitchDetails(event:MouseEvent) : void
		{
			if((ExternalInterface.available) && (FlexGlobals.topLevelApplication.m_propertyMode == Constants.FLOORPLAN))
			{
				ExternalInterface.call("showWdsEdit", data.item.id, "");
				FlexGlobals.topLevelApplication.showAlertMarquee("Please wait. Opening Switch details", true);
			}
		}
	}
}
