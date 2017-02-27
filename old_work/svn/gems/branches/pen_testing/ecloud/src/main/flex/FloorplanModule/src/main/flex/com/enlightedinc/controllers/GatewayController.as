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

	public class GatewayController
	{
		public var fpModel:FPModel;
		
		public var gatewayHttpService:HTTPService = new HTTPService();
		
		private var TXN_ID:Number = 0;
		
		public function GatewayController()
		{
			
			gatewayHttpService.method = "GET";
			gatewayHttpService.addEventListener(ResultEvent.RESULT,gatewayHttpService_resultHandler);
			gatewayHttpService.addEventListener(FaultEvent.FAULT,gatewayHttpService_faultHandler);
			gatewayHttpService.showBusyCursor = true;
			gatewayHttpService.useProxy = false;
			gatewayHttpService.resultFormat = "e4x";
		}
		
		public function getFloorPlanGatewayData(m_propertyMode:String , m_propertyType:String , m_propertyId:String) : void 
		{
			TXN_ID = new Date().getTime();
			var txnObj:Object = new Object();
			txnObj.transactionId = TXN_ID;
			txnObj.propertyMode = m_propertyMode;
			txnObj.propertyType = m_propertyType;
			
			
			if(m_propertyMode == Constants.FLOORPLAN)
			{
				gatewayHttpService.url = Constants.serverurl + "org/gateway/list/" + m_propertyType + "/" + m_propertyId + "?ts=" + new Date().time;
				gatewayHttpService.send(txnObj);
			}
		}
		
		private function gatewayHttpService_resultHandler(event:ResultEvent):void
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
			
			fpModel.commissionedGatewayData.disableAutoUpdate();
			fpModel.commissionedGatewayData.removeAll();
			
			fpModel.gatewayData.disableAutoUpdate();
			fpModel.gatewayData.removeAll();
			
			var xml:XMLDocument = new XMLDocument(event.result.toString());				
			var decoder:SimpleXMLDecoder = new SimpleXMLDecoder(true);				
			var resultObj:Object = decoder.decodeXML(xml);
			
						
			var tempData:ArrayCollection = new ArrayCollection();
			(resultObj.gateways.gateway is ArrayCollection) ? (tempData = resultObj.gateways.gateway) : (tempData.addItem(resultObj.gateways.gateway));
			
			for each(var gatewayObj:Object in tempData)
			{
				(gatewayObj.commissioned) ? (fpModel.commissionedGatewayData.addItem(gatewayObj)) : (fpModel.gatewayData.addItem(gatewayObj));
				gatewayObj.type = Constants.GATEWAY;
				if(gatewayObj.upgradestatus == null)
					gatewayObj.upgradestatus = "";
			}
			
			fpModel.commissionedGatewayData.enableAutoUpdate();
			fpModel.gatewayData.enableAutoUpdate();
			
			fpModel.searchData = new ArrayCollection(fpModel.searchData.toArray().concat(fpModel.commissionedGatewayData.toArray()));
			
			fpModel.gatewayDataReceived = true;
			
			if(fpModel.fixtureDataReceived && fpModel.gatewayDataReceived && fpModel.wdsDataReceived)
			{
				GlobalUtils.sortData(fpModel.searchData, "name");
			}
			
		}
		
		
		private function gatewayHttpService_faultHandler(event:FaultEvent):void
		{
			fpModel.commissionedGatewayData = new ArrayCollection();
			fpModel.fixtureData = new ArrayCollection();
			trace("fault : unable to get gateway list");
		}
	}
}