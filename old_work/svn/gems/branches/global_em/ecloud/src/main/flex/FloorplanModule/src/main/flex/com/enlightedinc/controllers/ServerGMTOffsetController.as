package com.enlightedinc.controllers
{
	import com.enlightedinc.components.Constants;
	import com.enlightedinc.models.FPModel;
	import com.enlightedinc.utils.GlobalUtils;
	
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;

	public class ServerGMTOffsetController
	{
		public var fpModel:FPModel;
		
		private var getServerGMTOffsetHttpService:HTTPService;
		
		public function ServerGMTOffsetController()
		{
			getServerGMTOffsetHttpService = new HTTPService();
			getServerGMTOffsetHttpService.method = "GET";
			getServerGMTOffsetHttpService.addEventListener(ResultEvent.RESULT,getServerGMTOffsetHttpService_resultHandler);
			getServerGMTOffsetHttpService.addEventListener(FaultEvent.FAULT,getServerGMTOffsetHttpService_faultHandler);
			getServerGMTOffsetHttpService.showBusyCursor = true;
			getServerGMTOffsetHttpService.useProxy = false;
			getServerGMTOffsetHttpService.resultFormat = "e4x";
			
		}
		
		public function getServerGMTOffset(): void{
			getServerGMTOffsetHttpService.url = Constants.serverurl + "org/facility/getEMInstanceServerTimeOffsetFromGMT/" + fpModel.m_propertyId + "?ts=" + new Date().time;
			getServerGMTOffsetHttpService.send();
		}
		
		private function getServerGMTOffsetHttpService_resultHandler(event:ResultEvent) : void
		{
			if(GlobalUtils.globalFaultHandler(event.statusCode)){
				return;
			}
			fpModel.serverGMTOffset = event.result.valueOf();
		}
		
		private function getServerGMTOffsetHttpService_faultHandler(event:FaultEvent) : void
		{
			GlobalUtils.globalFaultHandler(event.statusCode);
		}
		
	}
}