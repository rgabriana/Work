package com.enlightedinc.components.renderers
{
	import com.enlightedinc.assets.images.Images;
	import com.enlightedinc.components.Constants;
	import com.enlightedinc.utils.GlobalUtils;
	
	import flash.display.Graphics;
	import flash.display.Shape;
	import flash.events.MouseEvent;
	import flash.external.ExternalInterface;
	import flash.filters.BitmapFilterQuality;
	import flash.filters.GlowFilter;
	import flash.geom.Rectangle;
	import flash.utils.Dictionary;
	
	import mx.charts.ChartItem;
	import mx.charts.chartClasses.GraphicsUtilities;
	import mx.charts.series.items.PlotSeriesItem;
	import mx.collections.ArrayCollection;
	import mx.collections.ICollectionView;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.controls.Alert;
	import mx.controls.Image;
	import mx.controls.Label;
	import mx.core.FlexGlobals;
	import mx.core.IDataRenderer;
	import mx.core.UIComponent;
	import mx.effects.easing.Back;
	import mx.events.CollectionEvent;
	import mx.events.ToolTipEvent;
	import mx.formatters.NumberFormatter;
	import mx.graphics.IFill;
	import mx.graphics.SolidColor;
	import mx.graphics.Stroke;
	import mx.managers.ToolTipManager;
	import mx.styles.StyleManager;

		
	/**
	 *  Chart itemRenderer implementation 
	 *  that fills an elliptical area with gradient based on the Light Level of the fixture that is set as the chart item
	 *  It renders its area on screen using the <code>fill</code> and <code>stroke</code> styles
	 *  of its associated series.
	 */
	public class FixtureIconRenderer extends UIComponent implements IDataRenderer
	{
		//private static var rcFill:Rectangle = new Rectangle();
		private var _chartItem:Object;
		private var value:Number;
		
		/*** Embed fixture icons ***/
		[Bindable]
		public static var rendererType:String = "";
		[Bindable]
		public static var imageStatus:String = "";

		[Bindable]
		public static var scaleFactor:Number = 1;

		private var bChanged:Boolean = false;
		private var _image:Image;
		private var _label:Label;
		public function FixtureIconRenderer() 
		{
			super();
			name = Constants.FIXTURE_RENDERER;
			this.addEventListener(MouseEvent.DOUBLE_CLICK, showFixtureDetails);
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
		
		private function updateFixtureStatus() : void
		{
			// value here indicate lastconnectivityat
			var lastConnectivity:Date = getDateFromString(_chartItem.item.lastconnectivityat);
			var serverTime:Date = parentDocument.getServerTime();		// Call PloChartView's getServerTime function
			value = ((serverTime.time - lastConnectivity.time)/1000)/60;
			
			if(value <= 15) // less than 15 mins
			{
				if(_chartItem.item.ishopper == 1)
					_image.source = Images.ConnectivityHealthyHopper ;
				else
					_image.source = Images.ConnectivityHealthy ;
			}
			else if(value > 15 && value <= 10080 ) // between 15 minutes and 7 days
			{
				if(_chartItem.item.ishopper == 1)
					_image.source = Images.ConnectivityPowerOffHopper;
				else
					_image.source = Images.ConnectivityPowerOff;
			}
			else if(value > 10080) // greater than 7 days
			{
				if(_chartItem.item.ishopper == 1)
					_image.source = Images.ConnectivityProblemHopper;
				else
					_image.source = Images.ConnectivityProblem;
			}
		}
		
		private function updateLightLevel() : void
		{
			// value here indicate lightlevel
			value = _chartItem.item.lightlevel;
			
			if(!value) // This is a hack to show light off for outage report
			{
				_image.source = Images.LightOff;
				return;
			}
			
			if(value == 0)
			{
				_image.source = Images.LightOff;
			}
			else if(value > 0 && value < 25)
			{
				_image.source = Images.Light10On;
			}
			else if(value >= 25 && value < 50)
			{
				_image.source = Images.Light25On;
			}
			else if(value >= 50 && value < 75)
			{
				_image.source = Images.Light50On;
			}
			else if(value >= 75 && value < 100)
			{
				_image.source = Images.Light75On;
			}
			else if(value == 100)
			{
				_image.source = Images.LightFullOn;
			}else {
				_image.source = Images.LightOff;
			}
		}
		
		private function updateAmbientStatus() : void
		{
			// value here indicate ambientlight
			value = _chartItem.item.ambientlight;
			
			if(!value) 
			{
				_image.source = Images.AmbientDark;
				return;
			}
			
			if(value < 10)
			{
				_image.source = Images.AmbientDark;
			}
			else if(value >= 10 && value < 30)
			{
				_image.source = Images.AmbientDim;
			}
			else if(value >= 30 && value < 65)
			{
				_image.source = Images.AmbientNormal;
			}
			else if(value >= 65)
			{
				_image.source = Images.AmbientBright;
			}
		}
		
		private function updateOccupancyStatus() : void
		{
			// value here indicate lastoccupancyseen
			value = _chartItem.item.lastoccupancyseen;
			
			if(!value) 
			{
				_image.source = Images.OccupancyOccupied;
				return;
			}
			
			if(value <= 30)
			{
				_image.source = Images.OccupancyOccupied;
			}
			else if(value > 30 &&  value< 300)
			{
				_image.source = Images.OccupancyJustVacated; 
			}
			else
			{
				_image.source = Images.OccupancyVacated;
			}
		}
		
		private function updateTemperatureStatus() : void
		{
			// value here indicate avgtemperature
			value = _chartItem.item.avgtemperature;
			
			if(!value) 
			{
				_image.source = Images.TemperatureNormal;
				return;
			}
			
			if(value > 82)
			{
				_image.source = Images.TemperatureHot;
			}
			else if(value > 75 && value <= 82)
			{
				_image.source = Images.TemperatureWarm;
			}
			else if(value > 68 && value <= 75)
			{
				_image.source = Images.TemperatureNormal;
			}
			else if(value > 65 && value <= 68)
			{
				_image.source = Images.TemperatureCool;
			}
			else if(value <= 65)
			{
				_image.source = Images.TemperatureCold;
			}
		}
		
		private function updateBulbStatus() : void
		{
			// value here indicate bulblife
			value = _chartItem.item.bulblife;
			
			if(!value) 
			{
				_image.source = Images.BulbNormalLevel;
				return;
			}
			if (value < 25)
			{
				_image.source = Images.BulbLowLevel;
			}						
			else if(value < 75 && value >= 25)
			{
				_image.source = Images.BulbNormalLevel;
			}
			else
			{
				_image.source = Images.BulbGoodLevel;
			}
		}
		private function updateFixtureLampStatus() : void
		{
			var isLampOut:Boolean = _chartItem.item.lampOut;
			var isFixtureOut:Boolean = _chartItem.item.fixtureOut;
			var isCalibrated:Number = _chartItem.item.calibrated;
			var curveType:Number = _chartItem.item.curvetype;
			var calibrationInProcess:String = _chartItem.item.calibrationUpgradeStatus;
			var rectangle:Shape = new Shape();
			var color:uint = 0xFFFFFF;
			var borderCol:uint=0x000000;
			var borderSize:int=2;
			//Lets Define color 
			// GREEN - Calibrated
			// RED - Fixture Out
			// Yellow - Lamp Out
			// Grey - Non Calibrated
			//Border Color : 
			//Blue : 0x1313FE
			//Brown: #FE940A
			rectangle.name="bulbBg";
			//FX CURVE
			if(curveType==1)
			{
				borderCol=0xFE940A;
			}else if(curveType==2) // BALLAST CURVE
			{
				borderCol=0x1313FE;
			}
			if(isLampOut==true)
			{
				_label.text = Constants.LAMP_OUT;
				color = 0xF8FB33;
			}else if(isFixtureOut==true)
			{
				_label.text = Constants.FIXTURE_OUT;
				color = 0xFF0000;
			}else if(isCalibrated==1)
			{
				color = 0x00FF00;
			}else
			{
				if(isCalibrated==2)
				_label.text= Constants.NONCALIBRATED;
				borderSize =1;
				color = 0xEFEDED;
			}
			if(calibrationInProcess!=null)
			{
				_label.text= Constants.CALIBRATION_STATUS_SCHEDULED;
			}
			rectangle.graphics.beginFill(color);
			rectangle.graphics.lineStyle(borderSize, borderCol, 1.0);
			rectangle.graphics.drawRect(0, 0, 16,16);
			rectangle.graphics.endFill();
			_image.source=rectangle;
			
			
		}
		private function updateEmergencyFixtureStatus():void
		{
			var isCalibrated:Number = _chartItem.item.calibrated;
			var calibrationInProcess:String = _chartItem.item.calibrationUpgradeStatus;
			
			if(isCalibrated==1)
			{
				_label.text= Constants.CALIBRATED;
			}else if(isCalibrated==2)
			{
				_label.text= Constants.NONCALIBRATED;
			}
			if(calibrationInProcess!=null)
			{
				_label.text= Constants.CALIBRATION_STATUS_SCHEDULED;
			}
			updateLightLevel();
		}
		private function updateLabelRenderer( label:String ) : void
		{
			if(imageStatus == Constants.FIXTURE_STATUS)
			{
				updateFixtureStatus();
			}
			else if(imageStatus == Constants.LIGHT_LEVEL)
			{
				updateLightLevel();
			}
			else if(imageStatus == Constants.AMBIENT_STATUS)
			{
				updateAmbientStatus();
			}
			else if(imageStatus == Constants.OCCUPANCY_STATUS)
			{
				updateOccupancyStatus();
			}
			else if(imageStatus == Constants.TEMPERATURE_STATUS)
			{
				updateTemperatureStatus();
			}
			else if(imageStatus == Constants.BULB_STATUS)
			{
				updateBulbStatus();
			}
			else if(imageStatus == Constants.FIXTURE_LAMP_STATUS)
			{
				updateFixtureLampStatus();
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
		
		private function updateFixtureNetworkStatus():void
		{
			var secGwId:Number = _chartItem.item.secgwid;
			var color:uint = parentDocument.getColorDictionary(secGwId);
			var rect:Shape = new Shape();
			var borderCol:uint=color;
			var borderSize:int=2;
			rect.name="showfixtureRect";
			rect.graphics.beginFill(color);
			rect.graphics.lineStyle(borderSize, borderCol, 1.0);
			
			var isHopper:Number = _chartItem.item.ishopper;
			var measure:Number = 16;
			if(isHopper==0)
			{
				rect.graphics.drawRect(_image.x, _image.y, measure,measure);
			}else
			{
				rect = drawTriangle(_image.x, _image.y, measure, color);
			}
			rect.graphics.endFill();
			_image.source=rect;
			//_image.scaleX=scaleFactor;
			//_image.scaleY=scaleFactor;
		}
		private function drawTriangle(x:Number, y:Number, height:Number, color:uint):Shape{
			var triangle:Shape = new Shape; 
			triangle.graphics.beginFill(color);
			triangle.graphics.moveTo(height/2, y);
			triangle.graphics.lineTo(height, height+y);
			triangle.graphics.lineTo(x, height+y);
			triangle.graphics.lineTo(height/2, y);
			triangle.graphics.endFill();
			return triangle;
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
							trace("commit " + rendererType  + " imageStatus  :  "+ imageStatus + " _chartItem.item.snapaddress " + _chartItem.item.snapaddress);
							_label.text = "";
							if(rendererType == Constants.FIXTURE_STATUS)
							{
								updateFixtureStatus();
							}
							else if(rendererType == Constants.LIGHT_LEVEL)
							{
								updateLightLevel();
							}
							else if(rendererType == Constants.AMBIENT_STATUS)
							{
								updateAmbientStatus();
							}
							else if(rendererType == Constants.OCCUPANCY_STATUS)
							{
								updateOccupancyStatus();
							}
							else if(rendererType == Constants.TEMPERATURE_STATUS)
							{
								updateTemperatureStatus();
							}
							else if(rendererType == Constants.BULB_STATUS)
							{
								updateBulbStatus();
							}
							else if(rendererType == Constants.FIXTURE_LAMP_STATUS)
							{
								updateFixtureLampStatus();
							}
							else if(rendererType == Constants.FIXTURE_NAME)
							{
								var fixtureName:String = _chartItem.item.name;
								if(_chartItem.item.ishopper=='1' && (fixtureName.indexOf("(H)")==-1))
								{
									fixtureName+= "(H)";
								}
								_label.text = fixtureName;
								updateLabelRenderer(_label.text);
							}
							else if(rendererType == Constants.FIXTURE_MAC)
							{
								var strMacAddress:String = GlobalUtils.getMacAddress(_chartItem.item.snapaddress);
								_label.text = strMacAddress;
								updateLabelRenderer(strMacAddress);
							}
							else if(rendererType == Constants.FIXTURE_ID)
							{
								var fixtureId:String = _chartItem.item.id;
								_label.text = fixtureId;
								updateLabelRenderer(fixtureId);
							}
							else if(rendererType == Constants.FIXTURE_AREA)
							{
								if(_chartItem.item.area)
								{
									_label.text = _chartItem.item.area.name;
									updateLabelRenderer(_label.text);
								}
							}
							else if(rendererType == Constants.MOTION_GROUP || rendererType == Constants.SWITCH_GROUP)
							{
								_label.text = _chartItem.item.name;
								updateLabelRenderer(_label.text);
							}
							else if(rendererType == Constants.FIXTURE_PROFILE)
							{
								_label.text = _chartItem.item.currentprofile;
								updateLabelRenderer(_label.text);
							}else if(rendererType == Constants.IMAGE_UPGRADE)
							{
								_label.text = _chartItem.item.imageUpgradeStatus;
								updateLabelRenderer(_label.text);
							}else if(rendererType == Constants.FIXTURE_TYPE)
							{
								_label.text = _chartItem.item.fixtureTypeName;
								updateLabelRenderer(_label.text);
							}else if(rendererType == Constants.EMERGENCY_FIXTURE)
							{
								updateEmergencyFixtureStatus();
							}else if(rendererType == Constants.SHOW_NETWORK)
							{
								updateFixtureNetworkStatus();
							}else
							{
								updateLightLevel();
							}
						}
					} 
				}
				bChanged = false;
			}
		}
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
		{
			super.updateDisplayList(unscaledWidth, unscaledHeight);
//			var fill:IFill;
//			var state:String = "";
//			
//			if(_chartItem is ChartItem && _chartItem.hasOwnProperty('fill'))
//			{
//				fill = _chartItem.fill;
//				state = _chartItem.currentState;
//			}
//			else
//				fill = GraphicsUtilities.fillFromStyle(getStyle('fill'));			 
			
//			var glow:GlowFilter = new GlowFilter(0x0000FF, 1.0, 15, 15, 2, BitmapFilterQuality.MEDIUM);
//			glow.color = 0x0000FF;
//			glow.alpha = 1;
//			glow.blurX = 10;
//			glow.blurY = 10;
//			glow.quality = BitmapFilterQuality.MEDIUM;
			
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

		private function showFixtureDetails(event:MouseEvent) : void
		{
			if((FlexGlobals.topLevelApplication.m_propertyMode == Constants.FLOORPLAN) && ExternalInterface.available)
			{
				FlexGlobals.topLevelApplication.showAlertMarquee("Please wait. Opening Fixture details", true);
				//If the call is successful; update the fixture image as hopper property might change
				ExternalInterface.call("showFixtureForm", _chartItem.item.id);
			}
		}
	}
}
