package com.enlightedinc.controllers
{
	import com.enlightedinc.components.Constants;
	import com.enlightedinc.models.FPModel;
	import com.enlightedinc.utils.GlobalUtils;
	
	import flash.events.ContextMenuEvent;
	import flash.external.ExternalInterface;
	import flash.geom.Point;
	import flash.ui.ContextMenu;
	import flash.ui.ContextMenuItem;
	
	import mx.charts.PlotChart;
	import mx.charts.series.items.PlotSeriesItem;
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.core.FlexGlobals;
	import mx.rpc.mxml.Concurrency;

	public class FloorPlanContextMenuController
	{
		public var fpModel:FPModel;
		
		public var floorPlanChart:PlotChart;
		
		public function FloorPlanContextMenuController()
		{
			
		}
		
		public function manageFloorPlanContextMenu() : void
		{
			if(fpModel.m_propertyType == Constants.FLOOR)
			{
				fpModel.floorPlanContextMenu.customItems[0].visible = true;
				fpModel.floorPlanContextMenu.customItems[1].visible = true;
								
				fpModel.floorPlanContextMenu.customItems[0].enabled = (fpModel.selectedFixtures.length > 0) ? true : false;
				fpModel.floorPlanContextMenu.customItems[1].enabled = (fpModel.selectedFixtures.length > 0) ? true : false;
				
			}
		}
		
		public function createFloorPlanContextMenu() : void
		{
			fpModel.floorPlanContextMenu = new ContextMenu();
			
			for (var i:uint = 0; i<fpModel.floorPlanContextMenuItems.length; i++) 
			{
				var menuItem:ContextMenuItem = new ContextMenuItem(fpModel.floorPlanContextMenuItems[i]);
				if(menuItem.caption == Constants.ASSIGN_PROFILE)
				{
					menuItem.separatorBefore = true;
				}
				fpModel.floorPlanContextMenu.customItems.push(menuItem);
				menuItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, handleFloorPlanContextMenu);
			}
			
			fpModel.floorPlanContextMenu.hideBuiltInItems();
			floorPlanChart.contextMenu = fpModel.floorPlanContextMenu;
			
			fpModel.floorPlanContextMenu.addEventListener(ContextMenuEvent.MENU_SELECT, onOpenContextMenu);
			
			manageFloorPlanContextMenu();
		}
		
		private function onOpenContextMenu(event:ContextMenuEvent) : void
		{
			if(fpModel.mouseOverFixture)
			{
				// If we are right clicking on the device that is already selected then do not do anything with the current selection
				if(isSelected(fpModel.mouseOverFixture) == false)
				{					
					FlexGlobals.topLevelApplication.plotChartView.removeSelection();
					fpModel.selectedItems.push(fpModel.mouseOverFixture);
					fpModel.selectedFixtures.addItem(fpModel.mouseOverFixture);
					FlexGlobals.topLevelApplication.plotChartView.applyFilters(fpModel.mouseOverFixture, "glowFilter");
				}
				manageDeviceContextMenu();
			}
			else if(fpModel.mouseOverGateway)
			{
				// If we are right clicking on the device that is already selected then do not do anything with the current selection
				if(isSelected(fpModel.mouseOverGateway) == false)
				{					
					FlexGlobals.topLevelApplication.plotChartView.removeSelection();
					fpModel.selectedItems.push(fpModel.mouseOverGateway);
					fpModel.selectedGateways.addItem(fpModel.mouseOverGateway);
					FlexGlobals.topLevelApplication.plotChartView.applyFilters(fpModel.mouseOverGateway, "glowFilter");
				}
				manageDeviceContextMenu();
			}
			else
			{
				manageFloorPlanContextMenu();
			}
		}
		
		private function manageDeviceContextMenu() : void
		{
			if(!fpModel.floorPlanContextMenu)
				return;
			
			fpModel.floorPlanContextMenu.customItems[0].enabled = (fpModel.mouseOverFixture) ? true : false;
			fpModel.floorPlanContextMenu.customItems[1].enabled = (fpModel.mouseOverFixture) ? true : false;
			
		}
		
		private function handleFloorPlanContextMenu(event:ContextMenuEvent):void 
		{
			var selFixtures:String = "[";
			var sep:String = "";
			var fixtureItem:Object
			
			switch(ContextMenuItem(event.target).caption)
			{
				case Constants.ASSIGN_PROFILE : 
					if(ExternalInterface.available)
					{
						for(var i:int=0; i<fpModel.selectedFixtures.length; i++)
						{
							if(fpModel.selectedFixtures[i] is PlotSeriesItem)
							{
								selFixtures += sep + "{id: " + fpModel.selectedFixtures[i].item.id + ", currentprofile: \"" + fpModel.selectedFixtures[i].item.currentprofile + "\"}";
							}
							else
							{
								selFixtures += sep + "{id: " + fpModel.selectedFixtures[i].id + ", currentprofile: \"" + fpModel.selectedFixtures[i].currentprofile + "\"}";
							}
							sep = ",";
						}
						selFixtures += "]";  
						ExternalInterface.call("assignProfileToFixtures", selFixtures,fpModel.m_propertyId);
					}
					break;
				
				case Constants.AUTO_LIGHT_LEVEL : 
					onAutoSelect();
					break;
			}
		}
		
		// This function iterates through the selectedItems list and returns true
		// if the searchItem is found in the list
		private function isSelected(searchItem:Object) : Boolean
		{
			return GlobalUtils.isSelected(searchItem,fpModel.selectedItems);
		}
		
		private function onAutoSelect() : void
		{
			FlexGlobals.topLevelApplication.plotChartView.onAutoSelect();
		}
	}
}