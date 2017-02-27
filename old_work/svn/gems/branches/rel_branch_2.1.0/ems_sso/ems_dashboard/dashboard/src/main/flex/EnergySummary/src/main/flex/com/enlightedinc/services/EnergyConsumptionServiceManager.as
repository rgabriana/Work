package com.enlightedinc.services
{	
	import com.enlightedinc.components.Constants;
	
	import mx.messaging.Channel;
	import mx.messaging.ChannelSet;
	import mx.messaging.channels.AMFChannel;
	import mx.messaging.channels.SecureAMFChannel;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.InvokeEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;

	public class EnergyConsumptionServiceManager 
	{
		private static var instance : EnergyConsumptionServiceManager; 
		private static var url:String;
		public const name:String = "my-amf";
		public const sname:String = "my-secure-amf";
		private var destination:String = "energyConsumptionService";
		private var ro:RemoteObject;
		
		public static function getUrl():String
		{
			return url;
		}
		
		public static function setUrl(currUrl:String):void
		{
			url = currUrl;
		}
		
		public static function getInstance():EnergyConsumptionServiceManager  
		{
			if ( instance == null ) 
			{
				instance = new EnergyConsumptionServiceManager();
			}
			return instance;
		}
		
		public function EnergyConsumptionServiceManager() 
		{
			if ( instance != null )
			{
				throw new Error("Cant Instantiate EnergyConsumptionServiceManager directly. " + 
					"Call EnergyConsumptionServiceManager.getInstance()");
				return;
			}
		}
		
		private function createRemoteObject():void
		{
			var channelSet:ChannelSet = new ChannelSet();
			//var secureChannel:Channel = new SecureAMFChannel(sname,url);
			var simpleChannel:Channel = new AMFChannel(name,url);
			channelSet.addChannel(simpleChannel);    
			
			ro = new RemoteObject(destination);
			ro.channelSet = channelSet;
			
			ro.addEventListener(ResultEvent.RESULT,resultHandler);
			ro.addEventListener(FaultEvent.FAULT,flistener);
			ro.addEventListener(InvokeEvent.INVOKE,Ilistener) ;
		}
		
		public function getMeterDataWithRange(dataObject:Object, resultReceiver:Function, faultHandler:Function=null): void 
		{
			this.createRemoteObject();
			var token:AsyncToken;
			token = ro.loadMeterDataWithDateRange("company_id", dataObject.id, dataObject.fromDate, dataObject.toDate);
			
			token.addResponder(new AsyncResponder(
				function(data:Object, token:Object):void 
				{
					if(!data || !data.result) 
					{
						return;
					}
					resultReceiver(data.result as Object);
				},
				function(info:Object, token:Object):void 
				{
					if(faultHandler != null) 
					{
						faultHandler(info);					
					}
				},
				token ));
		}
		
		
		public function loadAreaReportPieChartData(dataObject:Object, fromDate:Date, toDate:Date, resultReceiver:Function, fault:Function=null): void 
		{
			this.createRemoteObject();
			var token:AsyncToken;
			token = ro.loadAreaReportPieChartData("company", dataObject.id, fromDate, toDate);
			
			token.addResponder(new AsyncResponder(
				function(data:Object, token:Object):void 
				{
					if(!data || !data.result) 
					{						
						resultReceiver();						
					}
					resultReceiver(dataObject.name, data.result as Object);
				},
				function(info:Object, token:Object):void 
				{
					if(fault != null) fault(info);
				},
				token ));
		}
		
		public function loadEnergyConsumption(dataObject:Object, resultReceiver:Function, fault:Function=null) : void
		{
			this.createRemoteObject();
			var token:AsyncToken;
			var propertyName:String = "company";
			
			switch(dataObject.period)
			{
				case Constants.DAY:
				{
					token = ro.loadDayEnergyConsumption(dataObject.id, propertyName, dataObject.toDate);
					break;
				}
				case Constants.WEEK:
				{
					token = ro.loadWeekEnergyConsumption(dataObject.id, propertyName, dataObject.toDate);
					break;
				}
				case Constants.MONTH:
				{
					token = ro.loadMonthEnergyConsumption(dataObject.id, propertyName, dataObject.toDate);
					break;
				}
				case Constants.YEAR:
				{
					token = ro.loadYearEnergyConsumption(dataObject.id, propertyName, dataObject.toDate);
					break;
				}
			}
			token.addResponder(new AsyncResponder(
			function(data:Object, token:Object):void 
			{
				if(!data) 
				{
					return;
				}
				resultReceiver(dataObject.period, data.result as Object);
			},
			function(info:Object, token:Object):void 
			{
				if(fault != null) 
				{
					fault(info);					
				}
			},
			token ));
		}
		
		private function resultHandler(e:ResultEvent) : void 
		{
		}
		
		private function flistener(e:FaultEvent) : void 
		{
		}
		
		private function Ilistener(e:InvokeEvent) : void 
		{
		}
	}
}