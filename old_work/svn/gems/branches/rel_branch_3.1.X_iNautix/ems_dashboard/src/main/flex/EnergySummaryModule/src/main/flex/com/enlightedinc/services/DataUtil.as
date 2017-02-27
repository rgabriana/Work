package com.enlightedinc.services
{
	
	public class DataUtil
	{
		public static var allEvents:ArrayCollection;
		public static var appURL:String;
		public static var userData:User;		
		[Bindable]public static var powerData:Array = null;
		[Bindable]public static var groupData:ArrayCollection; 
		
		public function DataUtil(){}		
		public static function setProfiles(groups:ArrayCollection):void
		{
			groupData = new ArrayCollection();			
			for each (var group:Groups in groups)
			{
				groupData.addItem(group);
			}
		}		
		public static function setUrl():void
		{
			var httpService:HTTPService = new HTTPService();
			httpService.rootURL="@ContextRoot()";
			
			var contextPos:int = Application.application.url.search(httpService.rootURL);
        	var currUrl:String = Application.application.url.substr(0, contextPos);
			appURL = Application.application.url.substr(0, contextPos);
        	
        	currUrl = currUrl + httpService.rootURL +"/messagebroker/amf";
        	TreeServiceManager.setUrl(currUrl);
        	FixtureServiceManager.setUrl(currUrl);
        	GatewayServiceManager.setUrl(currUrl);
        	EnergyConsumptionServiceManager.setUrl(currUrl);
        	EventsAndFaultServiceManager.setUrl(currUrl);
		}		
        public static function getEventsAndFaults():void
        {
			EventsAndFaultServiceManager.getInstance().getAllEventsAndFaults(true, EFRPCResultHandler, EFRPCFaultHandler);
        }        
        private static function EFRPCResultHandler(eventsAndFaults:ArrayCollection):void
        {
        	DataUtil.allEvents = eventsAndFaults;
        }        
        private static function EFRPCFaultHandler(fault:FaultEvent):void{}
		public static function formatMacAddress(macAddress:String):String
		{
			var formattedMacAddress:String = macAddress;
			try
			{				
				if((macAddress != "") && (macAddress.indexOf(":") > 0))
				{			
					formattedMacAddress = "";
					var Arr:Array = macAddress.split(":");
					for (var i:int = 0; i < Arr.length; i++) 
					{
						if(Arr[i].toString().length < 2)
							formattedMacAddress += "0" + Arr[i].toString();
						else
							formattedMacAddress += Arr[i].toString();
					}				
				}
			}
			catch(error:Error){}
			return formattedMacAddress
		}
		public static function IsFixtureNameExists(strName:String, fData:ArrayCollection):Boolean
		{
			var isExist:Boolean = false;
			for each(var fixture:Fixture in fData)
			{
				if(fixture.fixtureName.toUpperCase() == strName.toUpperCase())
				{
					isExist = true;
					break;
				}
			}
			return isExist;
		}
		public static function IsGatewayNameExists(strName:String, gData:ArrayCollection):Boolean
		{
			var isExist:Boolean = false;
			for each(var gateway:Gateway in gData)
			{
				if(gateway.gatewayName.toUpperCase() == strName.toUpperCase())
				{
					isExist = true;
					break;
				}
			}
			return isExist;
		}
		public static function ShowAlert(text:String = "", title:String = "",
										 flags:uint = 0x4, 
										 parent:Sprite = null, 
										 closeHandler:Function = null, 
										 iconClass:Class = null, 
										 defaultButtonFlag:uint = 0x4):void
		{
			var isDefined:Boolean = false;
			var screenHeight:Number = 0;
			var screenWidth:Number = 0;
			var scrollHeight:Number = 0;	
			
			var result:* = ExternalInterface.call("eval", "window.innerHeight");
			if (result == undefined)
			{
				result = ExternalInterface.call("eval", "document.documentElement.clientHeight");
				if (result == undefined)
				{
					result = ExternalInterface.call("eval", "document.getElementsByTagName('body')[0].clientHeight");//document.body.clientHeight
					if (result != undefined)
					{
						isDefined = true;
						screenHeight = Number(result);
						screenWidth = ExternalInterface.call("eval", "document.getElementsByTagName('body')[0].clientWidth");//document.body.clientWidth
						scrollHeight = ExternalInterface.call("eval", "document.getElementsByTagName('body')[0].scrollTop");//document.body.scrollTop
					}
				}
				else
				{
					isDefined = true;
					screenHeight = Number(result);
					screenWidth = ExternalInterface.call("eval", "document.documentElement.clientWidth");
					scrollHeight = ExternalInterface.call("eval", "document.documentElement.scrollTop");
				}
			}
			else 
			{
				isDefined = true;
				screenHeight = Number(result);
				screenWidth = ExternalInterface.call("eval", "window.innerWidth");
				scrollHeight = ExternalInterface.call("eval", "window.pageYOffset");
			}
			
			var alert:Alert = Alert.show(text, title, flags, parent, closeHandler, iconClass, defaultButtonFlag);
			PopUpManager.centerPopUp (alert);
			
			if(isDefined)
			{
				var newX:Number = screenWidth / 2 - alert.width / 2;
				var newY:Number = scrollHeight + screenHeight / 2 - alert.height;
				alert.move (newX, newY);
			}
		}
	}
}