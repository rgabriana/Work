package com.enlightedinc.controllers
{
	import com.enlightedinc.components.Constants;
	import com.enlightedinc.components.PanComponent;
	import com.enlightedinc.components.renderers.DataTipRenderer;
	import com.enlightedinc.models.FPModel;
	import com.enlightedinc.utils.GlobalUtils;
	
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.ui.Mouse;
	import flash.utils.Timer;
	import flash.xml.XMLDocument;
	
	import mx.charts.events.ChartItemEvent;
	import mx.core.FlexGlobals;
	import mx.events.CollectionEvent;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.rpc.xml.SimpleXMLDecoder;


	public class RealTimeStatsController
	{
		public var fpModel:FPModel;
		
		public var panCanvas:PanComponent;
		
		private var fixtureDetailsTimer:Timer;
		private var gatewayDetailsTimer:Timer;
		
		private var dataTipTimer:Timer;
		private var mouseOverTimer:Timer;
		private var dataTipRenderer:DataTipRenderer;
		
		private var toolTipColor:String = "0xF8FBA9";
		
		private var fixuteRealtimeHttpService:HTTPService;
		
		private var gatewayRealtimeHttpService:HTTPService;
		
		private var getFixtureByIdHttpService:HTTPService;
		
		private var getGatewayByIdHttpService:HTTPService;
		
		public function RealTimeStatsController()
		{
			
		}
		
		private function fixuteRealtimeHttpService_resultHandler(event:ResultEvent):void
		{
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
			if(!fpModel.mouseOverFixture)
				return;
			
			if (fixtureDetailsTimer != null) 
			{
				fixtureDetailsTimer.stop();
				fixtureDetailsTimer = null;
			}
			
			fixtureDetailsTimer = new Timer(2000, 3);
			fixtureDetailsTimer.addEventListener(TimerEvent.TIMER, fixtureDetailsTimerHandler);
			fixtureDetailsTimer.start();
		}
		
		private function fixuteRealtimeHttpService_faultHandler(event:FaultEvent):void
		{
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
			else
				FlexGlobals.topLevelApplication.showAlertMarquee("Realtime command failed. Please retry.", true);
		}
		
		private function fixtureDetailsTimerHandler(event:TimerEvent) : void
		{
			if(!fpModel.mouseOverFixture)
			{
				fixtureDetailsTimer.stop();
				fixtureDetailsTimer = null;
				return;
			}
			
			getFixtureByIdHttpService = new HTTPService();
			getFixtureByIdHttpService.method = "GET";
			getFixtureByIdHttpService.addEventListener(ResultEvent.RESULT,getFixtureByIdHttpService_resultHandler);
			getFixtureByIdHttpService.addEventListener(FaultEvent.FAULT,getFixtureByIdHttpService_faultHandler);
			getFixtureByIdHttpService.showBusyCursor = true;
			getFixtureByIdHttpService.useProxy = false;
			getFixtureByIdHttpService.resultFormat = "e4x";
			getFixtureByIdHttpService.url= Constants.serverurl + "org/fixture/details/" + fpModel.mouseOverFixture.id + "/"+ fpModel.m_propertyId +"?ts=" + new Date().time;
			getFixtureByIdHttpService.send();
		}
		
		private function getFixtureByIdHttpService_resultHandler(event:ResultEvent):void
		{
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
			if(!fpModel.mouseOverFixture)
				return;
			
			if(event.result == "")
				return;
			
			var xml:XMLDocument = new XMLDocument(event.result.toString());				
			var decoder:SimpleXMLDecoder = new SimpleXMLDecoder(true);				
			var resultObj:Object = decoder.decodeXML(xml);
			
			var serverTime:Date;
			var lastConnectivityTime:Date;
			
			if(fpModel.mouseOverFixture.lastconnectivityat != resultObj.fixture.lastconnectivityat)
			{
				if(fixtureDetailsTimer)
				{
					fixtureDetailsTimer.stop();
					fixtureDetailsTimer = null;
				}
				
				serverTime = GlobalUtils.getServerTime(fpModel.serverGMTOffset);
				serverTime.setMinutes(serverTime.getMinutes() - 15);
				
				lastConnectivityTime = GlobalUtils.createDate(resultObj.fixture.lastconnectivityat);
				
				if(lastConnectivityTime <= serverTime)
				{
					toolTipColor = "0xFDCCBB";
				}
				else if(lastConnectivityTime > serverTime)
				{
					toolTipColor = "0x9FF97B"; // Green
				}
				fpModel.mouseOverFixture.name = resultObj.fixture.name;
				if(resultObj.fixture.ishopper=='1' && (fpModel.mouseOverFixture.name.indexOf("(H)")==-1))
				{
					fpModel.mouseOverFixture.name+= "(H)";
				}
				fpModel.mouseOverFixture.lightlevel = resultObj.fixture.lightlevel;
				fpModel.mouseOverFixture.wattage = resultObj.fixture.wattage;
				fpModel.mouseOverFixture.avgtemperature = resultObj.fixture.avgtemperature;
				fpModel.mouseOverFixture.ambientlight = resultObj.fixture.ambientlight;
				fpModel.mouseOverFixture.lastoccupancyseen = resultObj.fixture.lastoccupancyseen;
				fpModel.mouseOverFixture.currentprofile = resultObj.fixture.currentprofile;
				fpModel.mouseOverFixture.state = resultObj.fixture.state;
				fpModel.mouseOverFixture.lastconnectivityat = resultObj.fixture.lastconnectivityat;
				fpModel.mouseOverFixture.bulblife = resultObj.fixture.bulblife;
				fpModel.mouseOverFixture.currentstate = resultObj.fixture.currentstate;
				
				//var profileObj:Object = getProfileDataById(resultObj.fixture.groupid);
				//If default profile is there, then rename the default profiles with suffix "_default"
				//if(profileObj!=null && profileObj.defaultProfile==true && profileObj.profileNo>0)
				//{
				//	fpModel.mouseOverFixture.currentprofile = GlobalUtils.setDefaultProfileName(fpModel.mouseOverFixture.currentprofile);
				//}
				
				fpModel.commissionedFixtureData.dispatchEvent(new CollectionEvent(CollectionEvent.COLLECTION_CHANGE));
				
				if(dataTipRenderer)
				{
					dataTipRenderer.setBackgroundColor(toolTipColor);
					buildDataTip(fpModel.mouseOverFixture);
					dataTipRenderer.updateData();
				}
			}
			
			if(fixtureDetailsTimer && fixtureDetailsTimer.currentCount == 3)
			{
				
				fixtureDetailsTimer.stop();
				fixtureDetailsTimer = null;
				
				serverTime = GlobalUtils.getServerTime(fpModel.serverGMTOffset);
				serverTime.setMinutes(serverTime.getMinutes() - 15);
				
				lastConnectivityTime = GlobalUtils.createDate(resultObj.fixture.lastconnectivityat);
				
				if(lastConnectivityTime <= serverTime)
				{
					toolTipColor = "0xFDCCBB";
				}
				else if(lastConnectivityTime > serverTime)
				{
					toolTipColor = "0xF8FBA9";
				}
				
				if(dataTipRenderer)
				{
					dataTipRenderer.setBackgroundColor(toolTipColor);
					buildDataTip(fpModel.mouseOverFixture);
					dataTipRenderer.updateData();
				}
				return;
			}
		} 
		
		private function getFixtureByIdHttpService_faultHandler(event:FaultEvent):void
		{
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
			else
				FlexGlobals.topLevelApplication.showAlertMarquee("Get fixture realtime command failed. Please retry.", true);
		}
		
		private function gatewayRealtimeHttpService_resultHandler(event:ResultEvent):void
		{
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
			if(!fpModel.mouseOverGateway)
				return;
			
			if (gatewayDetailsTimer != null) 
			{
				gatewayDetailsTimer.stop();
				gatewayDetailsTimer = null;
			}
			
			gatewayDetailsTimer = new Timer(2000, 3);
			gatewayDetailsTimer.addEventListener(TimerEvent.TIMER, gatewayDetailsTimerHandler);
			gatewayDetailsTimer.start();
		}
		
		private function gatewayRealtimeHttpService_faultHandler(event:FaultEvent):void
		{
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
			else
				FlexGlobals.topLevelApplication.showAlertMarquee("Realtime command failed. Please retry.", true);
		}
		
		private function gatewayDetailsTimerHandler(event:TimerEvent) : void
		{
			if(!fpModel.mouseOverGateway)
			{
				gatewayDetailsTimer.stop();
				gatewayDetailsTimer = null;
				return;
			}
			
			getGatewayByIdHttpService = new HTTPService()
			getGatewayByIdHttpService.method = "GET";
			getGatewayByIdHttpService.addEventListener(ResultEvent.RESULT,getGatewayByIdHttpService_resultHandler);
			getGatewayByIdHttpService.addEventListener(FaultEvent.FAULT,getGatewayByIdHttpService_faultHandler);
			getGatewayByIdHttpService.showBusyCursor = true;
			getGatewayByIdHttpService.useProxy = false;
			getGatewayByIdHttpService.resultFormat = "e4x";
			getGatewayByIdHttpService.url= Constants.serverurl + "org/gateway/details/" + fpModel.mouseOverGateway.id + "/" + fpModel.m_propertyId + "?ts=" + new Date().time;
			getGatewayByIdHttpService.send();
		}
		
		private function getGatewayByIdHttpService_resultHandler(event:ResultEvent):void
		{
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
			if(!fpModel.mouseOverGateway)
				return;
			
			if(event.result == "")
				return;
			
			var xml:XMLDocument = new XMLDocument(event.result.toString());				
			var decoder:SimpleXMLDecoder = new SimpleXMLDecoder(true);				
			var resultObj:Object = decoder.decodeXML(xml);
			
			var serverTime:Date;
			var lastConnectivityTime:Date;
			
			if(fpModel.mouseOverGateway.lastconnectivityat != resultObj.gateway.lastconnectivityat)
			{
				if(gatewayDetailsTimer)
				{
					gatewayDetailsTimer.stop();
					gatewayDetailsTimer = null;
				}
				
				serverTime = GlobalUtils.getServerTime(fpModel.serverGMTOffset);
				serverTime.setMinutes(serverTime.getMinutes() - 15);
				
				lastConnectivityTime = GlobalUtils.createDate(resultObj.gateway.lastconnectivityat);
				
				if(lastConnectivityTime <= serverTime)
				{
					toolTipColor = "0xFDCCBB";
				}
				else if(lastConnectivityTime > serverTime)
				{
					toolTipColor = "0x9FF97B"; // Green
				}
				
				fpModel.mouseOverGateway.lastconnectivityat = resultObj.gateway.lastconnectivityat;
				fpModel.mouseOverGateway.commissioned = resultObj.gateway.commissioned;
				fpModel.mouseOverGateway.name = resultObj.gateway.name;
				fpModel.mouseOverGateway.noofsensors = resultObj.gateway.noofsensors;
				fpModel.mouseOverGateway.noofactivesensors = resultObj.gateway.noofactivesensors;
				
				fpModel.commissionedGatewayData.dispatchEvent(new CollectionEvent(CollectionEvent.COLLECTION_CHANGE));
				
				if(dataTipRenderer)
				{
					dataTipRenderer.setBackgroundColor(toolTipColor);
					buildDataTip(fpModel.mouseOverGateway);
					dataTipRenderer.updateData();
				}
			}
			
			if(gatewayDetailsTimer && gatewayDetailsTimer.currentCount == 3)
			{
				gatewayDetailsTimer.stop();
				gatewayDetailsTimer = null;
				
				serverTime = GlobalUtils.getServerTime(fpModel.serverGMTOffset);
				serverTime.setMinutes(serverTime.getMinutes() - 15);
				
				lastConnectivityTime = GlobalUtils.createDate(resultObj.gateway.lastconnectivityat);
				
				if(lastConnectivityTime <= serverTime)
				{
					toolTipColor = "0xFDCCBB";
				}
				else if(lastConnectivityTime > serverTime)
				{
					toolTipColor = "0xF8FBA9";
				}
				
				if(dataTipRenderer)
				{
					dataTipRenderer.setBackgroundColor(toolTipColor);
					buildDataTip(fpModel.mouseOverGateway);
					dataTipRenderer.updateData();
				}
				return;
			}
		} 
		
		private function getGatewayByIdHttpService_faultHandler(event:FaultEvent):void
		{
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
			else
				FlexGlobals.topLevelApplication.showAlertMarquee("Get gateway realtime command failed. Please retry.", true);
		}
		
		private function showDataTip() : void
		{
			dataTipRenderer = new DataTipRenderer();
			var obj:Object;
			if(fpModel.mouseOverFixture)
			{
				obj = fpModel.mouseOverFixture;
			}
			else if(fpModel.mouseOverGateway)
			{
				obj = fpModel.mouseOverGateway;
			}
			else return;
			
			buildDataTip(obj);
			
			panCanvas.addChild(dataTipRenderer);
		}
		
		private function hideDataTip() : void
		{
			if(dataTipTimer)
			{
				dataTipTimer.stop();
				dataTipTimer = null;
			}
			if(dataTipRenderer && panCanvas.contains(dataTipRenderer))
			{
				panCanvas.removeChild(dataTipRenderer);
				dataTipRenderer = null;
			}
		}
		
		private function buildDataTip(obj:Object) : void
		{
			dataTipRenderer.obj = obj;
			dataTipRenderer.setBackgroundColor(toolTipColor);
			
			if(fpModel.m_propertyMode != Constants.FLOORPLAN)
			{
				positionDataTip();
				return;
			}
			
			if(obj.lastconnectivityat)
			{
				var serverTime:Date = GlobalUtils.getServerTime(fpModel.serverGMTOffset);
				serverTime.setMinutes(serverTime.getMinutes() - 15);
				
				var lastConnectivityTime:Date = GlobalUtils.createDate(obj.lastconnectivityat);
				
				if(lastConnectivityTime <= serverTime)
				{
					dataTipRenderer.connectivityDelay = true;
					positionDataTip();
					panCanvas.addChild(dataTipRenderer);
					return;
				}
			}
			
			positionDataTip();
		}
		
		private function positionDataTip() : void
		{
			//if(panCanvas.width - mouseX >= dataTipRenderer.width)
			if(panCanvas.width >= dataTipRenderer.width)
			{
				dataTipRenderer.x = panCanvas.contentMouseX;
			}
			else
			{
				dataTipRenderer.x = panCanvas.contentMouseX - dataTipRenderer.width;
			}
			
			//if(panCanvas.height - mouseY >= dataTipRenderer.height)
			if(panCanvas.height >= dataTipRenderer.height)
			{
				dataTipRenderer.y = panCanvas.contentMouseY;
			}
			else
			{
				dataTipRenderer.y = panCanvas.contentMouseY - dataTipRenderer.height;
			}
		}
		
		private function onDataTipTimerComplete(event:TimerEvent) : void
		{
			if(dataTipTimer)
			{
				dataTipTimer.stop();
				dataTipTimer = null;
			}
			showDataTip();
		}
		
		private function onMouseOverTimerComplete(event:TimerEvent):void
		{
			mouseOverTimer = null;
			if(fpModel.mouseOverFixture)
			{
				fixuteRealtimeHttpService = new HTTPService();
				fixuteRealtimeHttpService.method = "POST";
				fixuteRealtimeHttpService.addEventListener(ResultEvent.RESULT,fixuteRealtimeHttpService_resultHandler);
				fixuteRealtimeHttpService.addEventListener(FaultEvent.FAULT,fixuteRealtimeHttpService_faultHandler);
				fixuteRealtimeHttpService.contentType = "application/xml";
				fixuteRealtimeHttpService.showBusyCursor = true;
				fixuteRealtimeHttpService.useProxy = false;
				fixuteRealtimeHttpService.resultFormat = "xml";
				fixuteRealtimeHttpService.url = Constants.serverurl + "org/fixture/op/realtime/" + fpModel.m_propertyId + "?ts=" + new Date().time;
				var fixtureXmlList:XML = XML("<fixtures><fixture><id>" + fpModel.mouseOverFixture.id + "</id><name>" + fpModel.mouseOverFixture.name + "</name></fixture></fixtures>");
				fixuteRealtimeHttpService.request = fixtureXmlList;
				fixuteRealtimeHttpService.send();
			}
			else if(fpModel.mouseOverGateway)
			{
				gatewayRealtimeHttpService = new HTTPService()
				gatewayRealtimeHttpService.method = "POST";
				gatewayRealtimeHttpService.addEventListener(ResultEvent.RESULT,gatewayRealtimeHttpService_resultHandler);
				gatewayRealtimeHttpService.addEventListener(FaultEvent.FAULT,gatewayRealtimeHttpService_faultHandler);
				gatewayRealtimeHttpService.contentType = "application/xml";
				gatewayRealtimeHttpService.showBusyCursor = true;
				gatewayRealtimeHttpService.useProxy = false;
				gatewayRealtimeHttpService.resultFormat = "xml";
				gatewayRealtimeHttpService.url = Constants.serverurl + "org/gateway/op/realtime/" + fpModel.m_propertyId + "?ts=" + new Date().time;
				var gatewayXmlList:XML = XML("<gateways><gateway><id>" + fpModel.mouseOverGateway.id + "</id></gateway></gateways>");
				gatewayRealtimeHttpService.request = gatewayXmlList;
				gatewayRealtimeHttpService.send();
			}
		}

		
		public function onItemRollOver(event:ChartItemEvent) : void
		{
			// When the fixtures are placed too close to each other then itemRollOver might come before itemRollOut resulting in
			// dangling tool tips. so clear the tool tip in case this happens.
			hideDataTip();
			fpModel.mouseOverFixture = null;
			fpModel.mouseOverGateway = null;
			
			
			dataTipTimer = new Timer(1000, 1);
			dataTipTimer.addEventListener(TimerEvent.TIMER_COMPLETE, onDataTipTimerComplete);
			dataTipTimer.start();
			
			if(event.hitData.chartItem.itemRenderer.name == Constants.FIXTURE_RENDERER)
			{
				fpModel.mouseOverFixture = event.hitData.item;
				
				if(fpModel.m_propertyMode != Constants.FLOORPLAN)
					return;
				
				if (mouseOverTimer != null) 
				{
					mouseOverTimer.stop();
					mouseOverTimer = null;
				}
				mouseOverTimer = new Timer(3000, 1);
				mouseOverTimer.addEventListener(TimerEvent.TIMER_COMPLETE, onMouseOverTimerComplete);
				mouseOverTimer.start();
			}
			else if(event.hitData.chartItem.itemRenderer.name == Constants.GATEWAY_RENDERER)
			{
				fpModel.mouseOverGateway = event.hitData.item;
				
				if(fpModel.m_propertyMode != Constants.FLOORPLAN)
					return;
				
				if (mouseOverTimer != null) 
				{
					mouseOverTimer.stop();
					mouseOverTimer = null;
				}
				mouseOverTimer = new Timer(3000, 1);
				mouseOverTimer.addEventListener(TimerEvent.TIMER_COMPLETE, onMouseOverTimerComplete);
				mouseOverTimer.start();
			}
			
		}
		
		public function onItemRollOut(event:MouseEvent) : void
		{
			hideDataTip();
			
			if(mouseOverTimer)
			{
				mouseOverTimer.stop();
				mouseOverTimer = null;
			}
			
			if(fixtureDetailsTimer)
			{
				fixtureDetailsTimer.stop();
				fixtureDetailsTimer = null;
			}
			
			fpModel.mouseOverFixture = null;
			fpModel.mouseOverGateway = null;
			
			toolTipColor = "0xF8FBA9";
		}
	}
}