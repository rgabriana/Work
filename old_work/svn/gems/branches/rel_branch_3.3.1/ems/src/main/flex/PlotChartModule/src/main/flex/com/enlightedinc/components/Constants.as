package com.enlightedinc.components
{
	import mx.collections.ArrayCollection;

	public class Constants
	{
		public function Constants()
		{
		}
		
		public static const serverurl:String = "/ems/services/org/";
		
		public static const INSTALL_FIXTURE:String = "Install Fixture";
		public static const INSTALL_GATEWAY:String = "Install Gateway";
		public static const FLOOR_PLAN:String = "Floor Plan";
		
		public static const PIN_TOOLS_WINDOW:String = "Pin Tools Window";
		public static const UNPIN_TOOLS_WINDOW:String = "Unpin Tools Window";
		public static const ENABLE_PANNING:String = "Enable Panning";
		public static const ENABLE_MULTIPLE_SELECTION:String = "Enable Multiple Selection";
		public static const FIT_TO_SCREEN:String = "Fit to screen";
		public static const FULL_SCREEN:String = "Full screen";
		public static const HALIGN:String = "Horizontal align";
		public static const VALIGN:String = "Vertical align";
		
		
		public static const REFRESH:String = "Refresh";
		public static const REAL_TIME:String = "Real Time";
		
		public static const ALL_FIXTURES:String = "All Fixtures";
		public static const ALMOST_DEAD:String = "Almost Dead";
		public static const MIDLIFE_CRISIS:String = "Midlife Crisis";
		
		public static const FLOORPLAN:String = "FLOORPLAN";
		public static const COMMISSION:String = "COMMISSION";
		public static const FIXTURE_COMMISSION:String = "FIXTURE_COMMISSION";
		public static const GATEWAY_COMMISSION:String = "GATEWAY_COMMISSION";
		public static const PLUGLOAD_COMMISSION:String="PLUGLOAD_COMMISSION";
		public static const PLACED_FIXTURE_COMMISSION:String = "PLACED_FIXTURE_COMMISSION";
		public static const OUTAGE:String = "OUTAGE";
		public static const REPORT:String = "REPORT";
		public static const IMAGE_UPGRADE:String = "IMAGE_UPGRADE";
		public static const SCHEDULE_IMAGE_UPGRADE:String = "SCHEDULE_IMAGE_UPGRADE";
		public static const SCHEDULE_IMAGE_UPGRADE_GATEWAY:String = "SCHEDULE_IMAGE_UPGRADE_GATEWAY";
		public static const SCHEDULE_IMAGE_UPGRADE_FIXTURE:String = "SCHEDULE_IMAGE_UPGRADE_FIXTURE";
		public static const SCHEDULE_IMAGE_UPGRADE_ERC:String = "SCHEDULE_IMAGE_UPGRADE_ERC";
		public static const SWITCH_MODE:String = "SWITCH_MODE";
		public static const GROUP_MODE:String = "GROUP_MODE";
		public static const MOTION_BITS_GROUP_MODE:String = "MOTION_BITS_GROUP_MODE";

		public static const BULB:String = "BULB";
		public static const AREA:String = "area";
		public static const FLOOR:String = "floor";
		public static const BUILDING:String = "building";
		public static const PROFILE:String = "profile";
		
		public static const DISCOVERED:String = "DISCOVERED";
		public static const COMMISSIONED:String = "COMMISSIONED";
		
		public static const NO_FIXTURE_SELECTED_ERROR:String = "Please select fixtures or plugloads for the operation to take place";
		
		public static const ASSIGN_PROFILE:String = "Assign Profile";
		public static const CREATE_OTHER_DEVICES:String = "Add Other Devices";
		public static const ASSIGN_SUITE:String = "Assign Area";
		public static const DEFINE_GROUP_BEHAVIOUR:String = "Create Motion Group";
		public static const DEFINE_SWITCH_BEHAVIOUR:String = "Create Switch Group";
		public static const DEFINE_BULK_GROUP_BEHAVIOUR:String = "Configure Groups";
		public static const DEFINE_MOTION_BITS_BEHAVIOUR:String = "Define Motion Bits Behavior";
		public static const UNASSIGN_GROUPS:String = "Unassign Groups";
		public static const UNASSIGN_AREA:String = "Unassign Area";
		public static const ASSIGN_USERS:String = "Assign Users";
		public static const COMMISSION_AND_PLACE:String = "Commission and Place";
		public static const COMMISSION_STRING:String = "Commission";
		public static const RMA:String = "RMA";
		public static const SET_SWITCH_POSITION:String = "Set Switch Position";
		
		public static const FIXTURE_RENDERER:String = "FixtureRenderer";
		public static const HEATMAP_RENDERER:String = "HeatmapRenderer";
		public static const GATEWAY_RENDERER:String = "GatewayRenderer";
		public static const LOCATOR_DEVICE_RENDERER:String = "LocatorDeviceRenderer";
		public static const SWITCH_RENDERER:String = "SwitchRenderer";
		public static const WDS_RENDERER:String = "WDSRenderer";
		public static const PLACED_FIXTURE_RENDERER:String = "PlacedFixtureRenderer";
		public static const PLUGLOAD_RENDERER:String = "PlugloadRenderer";
				
		public static const FIXTURE_STATUS:String = "Fixture Status";
		public static const LIGHT_LEVEL:String = "Light Level";
		public static const AMBIENT_STATUS:String = "Ambient Status";
		public static const OCCUPANCY_STATUS:String = "Occupancy Status";
		public static const TEMPERATURE_STATUS:String = "Temperature Status";
		public static const BULB_STATUS:String = "Bulb Status";
		public static const FIXTURE_NAME:String = "Fixture Name";
		public static const FIXTURE_MAC:String = "Fixture MAC";
		public static const FIXTURE_AREA:String = "Fixture Area";
		public static const FIXTURE_ID:String = "Fixture Id";
		public static const FIXTURE_GROUP:String = "Fixture Group";
		public static const FIXTURE_PROFILE:String = "Fixture Profile";
		public static const MOTION_GROUP:String = "Motion Group";
		public static const SWITCH_GROUP:String = "Switch Group";
		public static const FIXTURE_LAMP_STATUS:String ="Fixture Lamp Status";
		public static const GATEWAY_STATUS:String = "Gateway Status";
		public static const ERC_BATTERY:String = "ERC Battery Level";
		public static const ERC_BATTERY_NORMAL:String = "Normal";
		public static const ERC_BATTERY_LOW:String = "Low";
		public static const ERC_BATTERY_CRITICAL:String = "Critical";
		public static const ERC_BATTERY_UNKNOWN:String = "Unknown";
		public static const PLUDLOAD_STATUS:String = "Plug Load Status";
		
		public static const FULL_ON:String = "FULL ON";
		public static const OFF:String = "OFF";
		public static const ON:String = "ON";
		public static const NA:String = "NA";
		
		public static const FIXTURE:String = "fixture";
		public static const GATEWAY:String = "gateway";
		public static const SWITCH:String = "switch";
		public static const WDS:String = "wds";
		public static const LOCATORDEVICE:String = "locatorDevice";
		public static const PLACED_FIXTURE:String = "placedFixture";
		public static const PLUGLOAD:String = "Plugload";
				
		public static const LOCATORDEVICE_ENLIGHTED_MANAGER:String = "Energy_manager";
		public static const LOCATORDEVICE_NON_ENLIGHTED_FIXTURE:String = "Unmanaged_fixture";
		public static const LOCATORDEVICE_EMERGENCY_FIXTURE:String = "Unmanaged_emergency_fixture";
		
		public static const VIEW:String = "View:";
		public static const FIND:String = "Find:";
		public static const OVERRIDE_LIGHT_LEVEL:String = "Override Light Level:";
		public static const AUTO_LIGHT_LEVEL:String = "Auto Light Level";
		public static const SAVE_AS_PDF:String = "Save as PDF";
		public static const ZOOM:String = "-        Zoom        +";
		public static const ICONZOOM:String = "-    Resize Icons    +";
		
		public static const EMPLOYEE:String = "employee";
		public static const ADMIN:String = "admin";
		
		public static const SESSION_TIME_OUT:Number =401;
		
		public static const SWITCH_TYPE_REAL:String = "Real";
		
		public static const LOCATION_TAB:String = "Location";
		
		public static const FIXTURE_TAB:String = "Device";
		
		public static const SCENE_TAB:String = "Scene";
		
		public static const WDS_TAB:String = "ERC";
		
		public static const FILTER:String = "Filter:";
		
		public static const IMG_UP_STATUS_SCHEDULED:String = "Scheduled";
		public static const IMG_UP_STATUS_INPROGRESS:String = "In Progress";
		public static const IMG_UP_STATUS_SUCCESS:String = "Success";
		public static const IMG_UP_STATUS_FAIL:String = "Fail";
		public static const IMG_UP_STATUS_PARTIAL:String = "Partial";
		public static const IMG_UP_STATUS_NOT_PENDING:String = "Not Pending";
		
		public static const ENABLE_HOPPER:String = "Enable Hopper";
		public static const DISABLE_HOPPER:String = "Disable Hopper";
		public static const CALIBRATE_FIXTURES:String = "Initiate Power Usage Characterization";
		public static const FETCH_BASELINE:String = "Retrieve Power Usage Characterization";
		public static const UPDATE_FIXTURE_CURVE:String = "View Power Usage Characterization";

		public static const ENABLE_EMERGENCY_FIXTURE:String = "Enable Emergency Fixture";
		public static const DISABLE_EMERGENCY_FIXTURE:String = "Disable Emergency Fixture";
		public static const EMERGENCY_FIXTURE:String = "Emergency Fixture";

		public static const LAMP_OUT:String = "Lamp Out";
		public static const FIXTURE_OUT:String = "Fixture Out";
		public static const CALIBRATED:String = "Characterized Fixture";
		public static const NONCALIBRATED:String = "Uncharacterized";
		public static const ALL_ISSUES:String = "All Issues";
		public static const CALIBRATION_STATUS_SCHEDULED:String = "Characterization Scheduled";
		
		public static const DETERMINE_DAY_LIGHT_HARVESTING_TARGET:String = "Determine Daylight Harvesting Target";
		public static const SET_AMBIENT_THRESHOLD:String = "Set Daylight Harvesting Target Value";
		public static const FIXTURE_TYPE:String = "Fixture Type";
		public static const ASSIGN_FIXTURE_TYPE:String = "Assign Fixture Type";
		public static const SHOW_NETWORK:String = "Show Network";
		
		//Default 28 Charting colors 
		/*public static const DEFAULT_COLORS:Array =
			[
				0xE48701
				,0xA5BC4E
				,0x1B95D9
				,0xCACA9E
				,0x6693B0
				,0xF05E27
				,0x86D1E4
				,0xE4F9A0
				,0xFFD512
				,0x75B000
				,0x0662B0
				,0xEDE8C6
				,0xCC3300
				,0xD1DFE7
				,0x52D4CA
				,0xC5E05D
				,0xE7C174
				,0xFFF797
				,0xC5F68F
				,0xBDF1E6
				,0x9E987D
				,0xEB988D
				,0x91C9E5
				,0x93DC4A
				,0xFFB900
				,0x9EBBCD
				,0x009797
				,0x0DB2C2
			];*/
		//Colors in sequence : 0.Red, 1.Blue, 2.Green, 3.Yellow, 4.Maroon, 5.Olive, 6.Lime, 7.Aqua, 8.Teal, 9.Fuchsia, 10.Purple, 11.Gray
		public static const DEFAULT_COLORS:Array =
			[
				0xFF0000 
				,0x0000FF
				,0x008000
				,0xFFFF00
				,0x800000
				,0x808000
				,0x00FF00
				,0x00FFFF
				,0x008080
				,0xFF00FF
				,0x800080
				,0x808080 
			];
	}
}
