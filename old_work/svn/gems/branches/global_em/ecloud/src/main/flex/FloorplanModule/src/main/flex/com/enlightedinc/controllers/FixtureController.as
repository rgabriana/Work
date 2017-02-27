package com.enlightedinc.controllers
{
	import com.enlightedinc.components.Constants;
	import com.enlightedinc.components.FPView;
	import com.enlightedinc.models.FPModel;
	import com.enlightedinc.utils.GlobalUtils;
	
	import flash.events.Event;
	import flash.xml.XMLDocument;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.rpc.xml.SimpleXMLDecoder;
	
	public class FixtureController
	{
		
		public var fpModel:FPModel;
		
		public var fixtureHttpService:HTTPService = new HTTPService();
		
		private var TXN_ID:Number = 0;
		
		public function FixtureController()
		{
			fixtureHttpService.method = "GET";
			fixtureHttpService.addEventListener(ResultEvent.RESULT,fixtureHttpService_resultHandler);
			fixtureHttpService.addEventListener(FaultEvent.FAULT,fixtureHttpService_faultHandler);
			fixtureHttpService.showBusyCursor = true;
			fixtureHttpService.useProxy = false;
			fixtureHttpService.resultFormat = "e4x";
		}
		
		public function getFloorPlanFixtureData(m_propertyMode:String , m_propertyType:String , m_propertyId:String) : void 
		{
			TXN_ID = new Date().getTime();
			var txnObj:Object = new Object();
			txnObj.transactionId = TXN_ID;
			txnObj.propertyMode = m_propertyMode;
			txnObj.propertyType = m_propertyType;
			
			
			if(m_propertyMode == Constants.FLOORPLAN)
			{
				fixtureHttpService.url = Constants.serverurl + "org/fixture/list/" + m_propertyType + "/" + m_propertyId + "/?ts=" + new Date().time;
				fixtureHttpService.send(txnObj);
			}
		}
		
		private function fixtureHttpService_resultHandler(event:ResultEvent):void
		{
			
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
			
			if(event.result == "")
				return;
			
			var asyncTxnObj:Object = event.token.message.body;
			
			if(asyncTxnObj && asyncTxnObj.transactionId != TXN_ID) {
				trace("Response discarded: " + asyncTxnObj.transactionId + ", " + TXN_ID);
				return;
			}
			
			fpModel.commissionedFixtureData.disableAutoUpdate();
			fpModel.commissionedFixtureData.removeAll();
			
			fpModel.fixtureData.disableAutoUpdate();
			fpModel.fixtureData.removeAll();
			
			var xml:XMLDocument = new XMLDocument(event.result.toString());				
			var decoder:SimpleXMLDecoder = new SimpleXMLDecoder(true);				
			var resultObj:Object = decoder.decodeXML(xml);
			
			var tempData:ArrayCollection = new ArrayCollection();
			(resultObj.fixtures.fixture is ArrayCollection) ? (tempData = resultObj.fixtures.fixture) :  (tempData.addItem(resultObj.fixtures.fixture));
			
			
			for each(var fixtureObj:Object in tempData)
			{
				var versionNumber:Number;
				
				if(fixtureObj.version == null)
					fixtureObj.version = "";
				
				fixtureObj.type = Constants.FIXTURE;
				
				(fixtureObj.state == Constants.COMMISSIONED) ? (fpModel.commissionedFixtureData.addItem(fixtureObj)) : (fpModel.fixtureData.addItem(fixtureObj));
				
				if(fixtureObj.upgradestatus == null)
					fixtureObj.upgradestatus = "";
				
			}
			
			fpModel.commissionedFixtureData.enableAutoUpdate();
			fpModel.fixtureData.enableAutoUpdate();
			
			fpModel.searchData = new ArrayCollection(fpModel.searchData.toArray().concat(fpModel.commissionedFixtureData.toArray()));
			
			fpModel.fixtureDataReceived = true;
			
			if(fpModel.fixtureDataReceived && fpModel.gatewayDataReceived)
			{
				GlobalUtils.sortData(fpModel.searchData, "name");
			}
			
		}
		
		
		private function fixtureHttpService_faultHandler(event:FaultEvent):void
		{
			fpModel.commissionedFixtureData = new ArrayCollection();
			fpModel.fixtureData = new ArrayCollection();
			trace("fault : unable to get fixture list");
		}
		
	}
}