package com.enlightedinc.components.renderers
{
	import com.enlightedinc.assets.images.Images;
	import com.enlightedinc.components.Constants;
	
	import flash.display.Shape;
	import flash.events.MouseEvent;
	import flash.external.ExternalInterface;
	
	import mx.charts.series.items.PlotSeriesItem;
	import mx.controls.Image;
	import mx.controls.Label;
	import mx.core.FlexGlobals;
	import mx.core.IDataRenderer;
	import mx.core.UIComponent;

		
	/**
	 *  Chart itemRenderer implementation 
	 */
	public class GatewayIconRenderer extends UIComponent implements IDataRenderer
	{
		private var _chartItem:Object;
		private var value:Number;
		
		/*** Embed Gateway icons ***/
		[Bindable]
		public static var gwRendererType:String = "";
		[Bindable]
		public static var gwImageStatus:String = "";

		private var bChanged:Boolean = false;
		private var _image:Image;
		private var _label:Label;
		public function GatewayIconRenderer() 
		{
			super();
			name = Constants.GATEWAY_RENDERER;
			this.addEventListener(MouseEvent.DOUBLE_CLICK, showGatewayDetails);
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
		private function createDate(str:String) : Date
		{
			var date:Date = new Date();
			date.setFullYear(str.slice(0,4));
			date.setMonth(str.slice(5,7));
			date.setMonth(date.getMonth() - 1);
			date.setDate(str.slice(8,10));
			date.setHours(str.slice(11,13));
			date.setMinutes(str.slice(14,16));
			date.setSeconds(str.slice(17,19));
			return date;
		}
		
		private function updateGatewayNetworkStatus():void
		{
			var secGwId:Number = _chartItem.item.id;
			var color:uint = parentDocument.getColorDictionary(secGwId);
			var circle:Shape = new Shape();
			var borderCol:uint=color;
			var borderSize:int=2;
			circle.name="showGwcircle";
			circle.graphics.beginFill(color);
			circle.graphics.lineStyle(borderSize, borderCol, 1.0);
			circle.graphics.drawCircle(12,12, _image.height+4);
			circle.graphics.endFill();
			_image.width = _image.width+4;
			_image.height = _image.height+4;
			_image.source=circle;
		}
		override protected function createChildren():void
		{
			super.createChildren();            
			_image = new Image();
			_image.alpha = 1.0;
			_image.width = 16;
			_image.height = 16;
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
						_image.width = 16;
						_image.height = 16;
						if (_image != null) {
							// value here indicate lastconnectivityat
							var lastConnectivity:Date = createDate(data.item.lastconnectivityat);
							var serverTime:Date = parentDocument.getServerTime();			// Call plotChartView's getServerTime
							var value:Number = ((serverTime.time - lastConnectivity.time)/1000)/60;
							
							if(value <= 15) // less than 15 mins
							{
								_image.source = Images.GatewayConnected ;
							}
							else if(value > 15 && value <= 10080) // less than 7 days and greater than 15 mins
							{
								_image.source = Images.GatewayIdle ;
							}
							else if(value > 10080) // less than 10 mins
							{
								_image.source = Images.GatewayNotConnected ;
							}
						}
						if(_label && gwRendererType == Constants.IMAGE_UPGRADE)
						{
							_label.text = "";
							_label.text = _chartItem.item.imageUpgradeStatus;
						}else if(gwRendererType == Constants.SHOW_NETWORK)
						{
							updateGatewayNetworkStatus();
						}else if(_label && gwRendererType == Constants.GATEWAY_NAME){
							_label.text = "";
							_label.text = _chartItem.item.name;
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

		private function showGatewayDetails(event:MouseEvent) : void
		{
			if((ExternalInterface.available) && (FlexGlobals.topLevelApplication.m_propertyMode == Constants.FLOORPLAN))
			{
				ExternalInterface.call("showGateWayForm", data.item.id);
				FlexGlobals.topLevelApplication.showAlertMarquee("Please wait. Opening Gateway details", true);
			}
		}
	}
}
