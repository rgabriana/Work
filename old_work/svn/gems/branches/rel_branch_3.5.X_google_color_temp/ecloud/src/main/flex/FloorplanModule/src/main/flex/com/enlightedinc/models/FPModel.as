package com.enlightedinc.models
{
	import com.enlightedinc.components.Constants;
	import com.enlightedinc.utils.GlobalUtils;
	
	import flash.ui.ContextMenu;
	
	import mx.charts.series.items.PlotSeriesItem;
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;

	public class FPModel
	{
		public function FPModel()
		{
		}
		
		[Bindable]
		public var commissionedFixtureData:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var commissionedGatewayData:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var fixtureData:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var gatewayData:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var wdsData:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var searchData:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var profileData:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var showSaveAsPdfButton:Boolean=false;
		
		public var fixtureDataReceived:Boolean = false;
		public var gatewayDataReceived:Boolean = false;
		public var wdsDataReceived:Boolean = false;
		
		public var selectedFixtures:ArrayCollection = new ArrayCollection();
		public var selectedGateways:ArrayCollection = new ArrayCollection();
		public var selectedWdses:ArrayCollection = new ArrayCollection();
		
		public var serverGMTOffset:Number = 0;
		
		public var mouseOverGateway:Object;
		public var mouseOverFixture:Object;
		public var mouseOverWds:Object;
		
		public var m_propertyId:String;
		public var m_propertyType:String;
		public var m_propertyMode:String;
		public var m_propertyModeId:String;
		
		public var floorPlanContextMenu:ContextMenu;
		public var floorPlanContextMenuItems:Array = [Constants.AUTO_LIGHT_LEVEL, Constants.ASSIGN_PROFILE];
		
		[Bindable]
		public var selectedItems:Array = new Array;
		
		public var layersDataGroup:ArrayCollection = new ArrayCollection([
			{label:"Light Level"},
			{label:"Fixture Status"},
			{label:"Ambient Status"},
			{label:"Occupancy Status"},
			{label:"Temperature Status"},
			{label:"Fixture Name"},
			{label:"Fixture MAC"},
			{label:"Fixture Profile"},
			{label:"Gateway Status"},
			{label:"ERC Battery Level"}
		]);
		
		public var batteryLevelList:ArrayCollection = new ArrayCollection([
			{name:"Select All"},
			{name:"Normal"},
			{name:"Low"},
			{name:"Critical"},
			{name:"Unknown"}
		]);
		
	}
}