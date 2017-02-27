package com.enlightedinc.controllers
{
	import com.enlightedinc.components.Constants;
	import com.enlightedinc.models.FPModel;
	import com.enlightedinc.utils.GlobalUtils;
	
	import flash.events.Event;
	import flash.xml.XMLDocument;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.rpc.xml.SimpleXMLDecoder;

	public class WdsController
	{
		public var fpModel:FPModel;
		
		public var wdsHttpService:HTTPService = new HTTPService();
		
		private var TXN_ID:Number = 0;
		
		public function WdsController()
		{
			
			wdsHttpService.method = "GET";
			wdsHttpService.addEventListener(ResultEvent.RESULT,wdsHttpService_resultHandler);
			wdsHttpService.addEventListener(FaultEvent.FAULT,wdsHttpService_faultHandler);
			wdsHttpService.showBusyCursor = true;
			wdsHttpService.useProxy = false;
			wdsHttpService.resultFormat = "e4x";
		}
		
		public function getFloorPlanWdsData(m_propertyMode:String , m_propertyType:String , m_propertyId:String) : void 
		{
			TXN_ID = new Date().getTime();
			var txnObj:Object = new Object();
			txnObj.transactionId = TXN_ID;
			txnObj.propertyMode = m_propertyMode;
			txnObj.propertyType = m_propertyType;
			
			
			if(m_propertyMode == Constants.FLOORPLAN)
			{
				wdsHttpService.url = Constants.serverurl + "org/wds/list/" + m_propertyType + "/" + m_propertyId + "?ts=" + new Date().time;
				wdsHttpService.send(txnObj);
			}
		}
		
		private function wdsHttpService_resultHandler(event:ResultEvent):void
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
			
			fpModel.wdsData.disableAutoUpdate();
			fpModel.wdsData.removeAll();
			
			var xml:XMLDocument = new XMLDocument(event.result.toString());				
			var decoder:SimpleXMLDecoder = new SimpleXMLDecoder(true);				
			var resultObj:Object = decoder.decodeXML(xml);
			
						
			var tempData:ArrayCollection = new ArrayCollection();
			(resultObj.wdss.wds is ArrayCollection) ? (tempData = resultObj.wdss.wds) : (tempData.addItem(resultObj.wdss.wds));
			
			for each(var wdsObj:Object in tempData)
			{
				fpModel.wdsData.addItem(wdsObj);
				
				wdsObj.type = Constants.WDS;
				
				if(wdsObj.batteryLevel == Constants.NA)
				{
					wdsObj.batteryLevel = Constants.ERC_BATTERY_UNKNOWN;
				}
				if(wdsObj.upgradestatus == null)
					wdsObj.upgradestatus = "";
				
			}
			
			fpModel.wdsData.enableAutoUpdate();
			
			//fpModel.searchData = new ArrayCollection(fpModel.searchData.toArray().concat(fpModel.wdsData.toArray()));
			
			fpModel.wdsDataReceived = true;
			
			if(fpModel.fixtureDataReceived && fpModel.gatewayDataReceived && fpModel.wdsDataReceived)
			{
				GlobalUtils.sortData(fpModel.searchData, "name");
			}
			
		}
		
		
		private function wdsHttpService_faultHandler(event:FaultEvent):void
		{
			fpModel.wdsData = new ArrayCollection();
			trace("fault : unable to get ERC list");
		}
	}
}