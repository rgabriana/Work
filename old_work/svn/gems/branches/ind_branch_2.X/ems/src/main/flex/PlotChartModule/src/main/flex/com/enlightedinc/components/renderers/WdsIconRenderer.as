package com.enlightedinc.components.renderers
{
	import com.enlightedinc.assets.images.Images;
	import com.enlightedinc.components.Constants;
	
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
	public class WdsIconRenderer extends UIComponent implements IDataRenderer
	{
		private var _chartItem:Object;
		private var value:Number;
		
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
						if (_image != null) {
							_image.source = Images.WDS;
						}
						if(_label && wdsRendererType == Constants.IMAGE_UPGRADE)
						{
							_label.text = "";
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
