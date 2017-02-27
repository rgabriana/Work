package com.enlightedinc.components.renderers
{
	import com.enlightedinc.assets.images.Images;
	import com.enlightedinc.components.Constants;
	import com.enlightedinc.utils.GlobalUtils;
	
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
	public class PlugloadIconRenderer extends UIComponent implements IDataRenderer
	{
		/*** Embed fixture icons ***/
		[Bindable]
		public static var plrendererType:String = "";
		private var _chartItem:Object;
		private var value:Number;
		
		/*** Embed plugload icons ***/
		private var bChanged:Boolean = false;
		private var _image:Image;
		private var _label:Label;
		public function PlugloadIconRenderer() 
		{
			super();
			name = Constants.PLUGLOAD_RENDERER;
			this.addEventListener(MouseEvent.DOUBLE_CLICK, showPludloadDetails);
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
		private function updatePlugloadStatus() : void
		{
			// value here indicate lastconnectivityat
			var lastConnectivity:Date = getDateFromString(_chartItem.item.lastConnectivityAt);
			var serverTime:Date = parentDocument.getServerTime();		// Call PloChartView's getServerTime function
			value = ((serverTime.time - lastConnectivity.time)/1000)/60;
			if(value <= 15) // less than 15 mins
			{
				_image.source = Images.PlugloadConnectedON ;
			}
			else if(value > 15 && value <= 10080 ) // between 15 minutes and 7 days
			{
				_image.source = Images.PlugloadConnectedOFF ;
			}
			else if(value > 10080) // greater than 7 days
			{
				_image.source = Images.PlugloadConnectedDisconnected ;
			}
		}
		private function getDateFromString(str:String) : Date
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
		override protected function commitProperties():void {
			super.commitProperties();
			if (bChanged) {
				if(_chartItem != null)
				{
					if (PlotSeriesItem(_chartItem).item != null)
					{
						_label.text ="";
						if (_image != null) {
							trace("commit " + plrendererType  + " _chartItem.item.snapaddress " + _chartItem.item.snapaddress);
							updatePlugloadStatus();
						}
						if(plrendererType == Constants.PLUDLOAD_STATUS)
						{
							var strMacAddress:String = GlobalUtils.getMacAddress(_chartItem.item.macaddress);
							_label.text = strMacAddress;
						}else if(plrendererType == Constants.IMAGE_UPGRADE)
						{
							_label.text = _chartItem.item.imageUpgradeStatus;
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

		private function showPludloadDetails(event:MouseEvent) : void
		{
			if((ExternalInterface.available) && (FlexGlobals.topLevelApplication.m_propertyMode == Constants.FLOORPLAN))
			{
				ExternalInterface.call("showPlugloadForm", data.item.id, "");
				FlexGlobals.topLevelApplication.showAlertMarquee("Please wait. Opening Plugload details", true);
			}
		}
	}
}
