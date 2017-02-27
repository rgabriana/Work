package com.enlightedinc.controllers
{
	import com.enlightedinc.components.Constants;
	import com.enlightedinc.models.FPModel;
	import com.enlightedinc.utils.GlobalUtils;
	
	import flash.xml.XMLDocument;
	
	import mx.charts.series.items.PlotSeriesItem;
	import mx.collections.ArrayCollection;
	import mx.core.FlexGlobals;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.rpc.xml.SimpleXMLDecoder;

	public class DimfixtureController
	{
		public var fpModel:FPModel;
		
		public var dimFixtureHttpService:HTTPService = new HTTPService();
		
		public var fixuteModeHttpService:HTTPService = new HTTPService();
		
		private var TXN_ID:Number = 0;
		
		public function DimfixtureController()
		{
			dimFixtureHttpService.method = "POST";
			dimFixtureHttpService.contentType = "application/xml";
			dimFixtureHttpService.showBusyCursor = true;
			dimFixtureHttpService.useProxy = false;
			dimFixtureHttpService.resultFormat = "xml";
			
			fixuteModeHttpService.method = "POST";
			fixuteModeHttpService.contentType = "application/xml";
			fixuteModeHttpService.showBusyCursor = true;
			fixuteModeHttpService.useProxy = false;
			fixuteModeHttpService.resultFormat = "xml";
		}
		
		
		public function manageDimmerControl(m_propertyId:String,dimmerValue:String, dimmingType:String) : void 
		{
			dimFixtureHttpService.url = Constants.serverurl + "org/fixture/op/dim/" + dimmingType + "/" + dimmerValue + "/60" + "/" + m_propertyId +"/?ts=" + new Date().time;
			dimFixtureHttpService.request = createFixturesRequestXml();
			dimFixtureHttpService.addEventListener(ResultEvent.RESULT,dimFixtureHttpService_resultHandler);
			dimFixtureHttpService.addEventListener(FaultEvent.FAULT,dimFixtureHttpService_faultHandler);
			dimFixtureHttpService.send();
		}
		
		public function onAutoSelect(m_propertyId:String) : void
		{
			fixuteModeHttpService.url = Constants.serverurl + "org/fixture/op/mode/AUTO" + "/" + m_propertyId +"/?ts=" + new Date().time;
			fixuteModeHttpService.request = createFixturesRequestXml();
			fixuteModeHttpService.addEventListener(ResultEvent.RESULT,fixuteModeHttpService_resultHandler);
			fixuteModeHttpService.addEventListener(FaultEvent.FAULT,fixuteModeHttpService_faultHandler);
			fixuteModeHttpService.send();
		}
		
		private function createFixturesRequestXml() : XML
		{
			var requestXML:XML = <fixtures></fixtures>;
			var id:String;
			var name:String;
			
			for(var i:int=0; i<fpModel.selectedFixtures.length; i++)
			{
				if(fpModel.selectedFixtures[i] is PlotSeriesItem)
				{
					id = fpModel.selectedFixtures[i].item.id;
					name = fpModel.selectedFixtures[i].item.name;
				}
				else
				{
					id = fpModel.selectedFixtures[i].id;
					name = fpModel.selectedFixtures[i].name;
				}
				var xmlList:XMLList = XMLList("<fixture><id>" + id + "</id><name>" + name + "</name></fixture>");
				requestXML.appendChild(xmlList);
			}
			return requestXML;
		}
		
		private function dimFixtureHttpService_resultHandler(event:ResultEvent):void
		{
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
		}
		
		
		private function dimFixtureHttpService_faultHandler(event:FaultEvent):void
		{
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
			else
				FlexGlobals.topLevelApplication.showAlertMarquee("Dimming command failed. Please retry.", true);
		}
		
		private function fixuteModeHttpService_resultHandler(event:ResultEvent):void
		{
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
		}
		
		
		private function fixuteModeHttpService_faultHandler(event:FaultEvent):void
		{
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
			else
				FlexGlobals.topLevelApplication.showAlertMarquee("Auto command failed. Please retry.", true);
		}
		
	}
}