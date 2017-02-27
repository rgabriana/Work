#ifndef __ELF_GEMS_API_H__
#define __ELF_GEMS_API_H__

#include "elf_std.h"
#include "genAna.h"
#include "genBin.h"
#include "elf_db.h"


#define	POST_BACNET_EVENT			"https://%s/ems/api/events/addbacnetevent"

#ifdef UEM

#define GET_FLOOR_LIST_API 		    "https://%s/uem/services/org/facility/list/floors"
#define GET_AREA_LIST_API  		    "https://%s/uem/services/hvac/list/zones/floor/%d"
#define GET_HVAC_STATS_BY_ZONE_API	"https://%s/uem/services/hvac/stats/zone/%d"

#endif

#ifdef EM

// documented here: https://enlightedinc.atlassian.net/wiki/pages/viewpage.action?spaceKey=EM&title=API+Extensions

// by site (EM)
#define GET_FLOOR_LIST_API	"https://%s/ems/api/org/floor/v1/list"
#define GET_EM_CONSUMPTION	"https://%s/ems/api/org/em/v1/energy"
#define GET_EM_ADR_LEVEL	"https://%s/ems/api/org/dr/listdr"
#define SET_EM_EMERGENCY	"https://%s/ems/api/org/em/v1/setEmergency?time=60"
#define SET_EM_ADR_LEVEL	"https://%s/ems/api/org/dr/scheduledr"

// by floor
#define GET_AREA_LIST_API						"https://%s/ems/api/org/area/list/%d"
#define GET_FLOOR_SENSORS_ENERGY_API			"https://%s/ems/api/org/sensor/v3/lastSensorEnergyStats/15min/floor/%d"
#define GET_FLOOR_SWITCH_GROUPS					"https://%s/ems/api/org/switchgroups/list/floor/%d"

// switch groups
												// floor id, switch name, dim level
#define SET_SWITCH_DIM_LEVEL_API				"https://%s/ems/api/org/switch/v1/op/dimGroup/%d/%s/%d?time=60"
#define GET_SCENES_BY_FLOOR_SWITCH_NAME			"https://%s/ems/api/org/switch/v1/getSwitchScenes/%d/%s"
#define GET_LIGHTLEVELS_FOR_SCENE				"https://%s/ems/api/org/scene/v1/list/getSceneLevels/%d"
#define GET_PLUGLEVELS_FOR_SCENE				"https://%s/ems/api/org/scene/list/plugloadscenelevel/sid/%d"


// by area

#define GET_FIXTURE_LOC_BY_AREA_API				"https://%s/ems/api/org/fixture/v1/location/list/area/%d"
// todo 4 - what  is the difference here?? (I have asked Sachin to check in with Suman)
#define GET_FLOOR_AREA_OCC_STATE_API            "https://%s/ems/api/org/facility/v1/getOccupancyStateOfFloorAreas/%d"
// #define GET_AREA_OCC_STATE_API					"https://%s/ems/api/org/area/v1/occ/%d"

// todo 4 - this does not allow multiple 'area 0'
#define GET_AREA_FIXTURE_OUTAGES_API            "https://%s/ems/api/org/area/v1/out/%d"

// aditya's doc and 3.5 show power, but confluence docs show energy. Need to resolve.
// https ://enlightedinc.atlassian.net/browse/EM-418
#define GET_AREA_PLUGLOAD_CONSUMPTION_API       "https://%s/ems/api/org/plugload/v1/energy/area/%d"
#define GET_AREA_EMERGENCY_STATUS		        "https://%s/ems/api/org/area/v1/getEmergencyMode/%d"
	
#define GET_AREA_PLUGLOAD_CONFIGURATION_API		"https://%s/ems/api/org/plugload/v1/location/list/area/%d/1"
#define SET_AREA_EMERGENCY_API					"https://%s/ems/api/org/area/v1/setEmergency/%d?time=60"	


#define SET_FIXTURE_DIM_LEVEL_API			"https://%s/ems/api/org/sensor/op/dim/%s/%d/%d"
#define SET_SWITCH_SCENE_API				"https://%s/ems/api/org/switch/v1/op/applyScene/%d/%d?time=60"

// by plugload

#define GET_INDIVIDUAL_PLUGLOAD_CONSUMPTION_API	"https://%s/ems/api/org/plugload/v1/energy/%d"
#define SET_PLUGLOAD_STATUS_API					"https://%s/ems/api/org/area/v1/setPlugloadStatus/%d/%d"

#endif

#define TMP_FILES_PATH2    "/tmp"
#define TMP_FILE_PREFIX    "xml"

#define FLOORS_XML						"get_config_floors"
#define POST_BACNET_EVENT_IN_XML		"post_event_in"
#define POST_BACNET_EVENT_OUT_XML		"post_event_out"

#ifdef UEM
#define GET_CONFIG_AND_DATA_SECTORS_XML		"get_config_and_data_zone_per_floor"
#define FILENAME_HVAC_STATS_ZONE_XML		"get_data_hvac_per_zone"
#endif

#ifdef EM
#define GET_CONFIG_AND_DATA_SECTORS_XML	"get_config_and_data_area_per_floor"
#define SET_EM_EMERGENCY_XML			"set_em_emergency"
#define GET_EM_ENERGY_XML				"get_config_em_energy"
#define GET_EM_ADR_LEVEL_XML			"get_data_em_adr_level"
#define SENSORS_LOC_BY_AREA_XML        "get_config_sensors_loc_by_area"
#define GET_SCENE_BY_FLOOR_SWITCH_NAME  "get_config_scenes_per_floor_and_switchgroup_name"
#define GET_LIGHTLEVEL_FOR_SCENE_XML    "get_config_lightlevel_for_scene"
#define GET_PLUGLEVEL_FOR_SCENE_XML     "get_config_pluglevel_for_scene"
//#define GET_FIXTURE_DIM_XML			"get_data_fixture_dim_level"
#define GET_FLOOR_SENSORS_ENERGY_XML	"get_data_floor_sensors_energy"

// todo 4 - what  is the difference here?? (I have asked Sachin to check in with Suman)
#define GET_FLOOR_AREA_OCC_XML			"get_floor_area_occ"
// #define GET_AREA_OCC_XML				"get_area_occ"

#define GET_FLOOR_SWITCH_GROUPS_XML			"get_config_floor_switch_groups"
#define SET_AREA_EMERGENCY_XML				"set_area_emergency"
#define AREA_FIXTURE_OUTAGES_XML			"get_data_area_fixture_outages"
#define GET_AREA_EMERGENCY_STATUS_XML		"get_data_area_emergency_status"
#define GET_AREA_PLUGLOAD_CONFIGURATION_XML	"get_config_area_plugload"

#define GET_AREA_PLUGLOAD_CONSUMPTION_XML			"get_data_area_plugload_consumption"
#define GET_INDIVIDUAL_PLUGLOAD_DATA_XML			"get_data_individual_plugload_consumption_for_area"
#define GET_INDIVIDUAL_PLUGLOAD_CONSUMPTION_XML		"get_data_individual_plugload"

#define SET_FIXTURE_DIM_XML					"set_fixture_dim_level"
#define SET_FIXTURE_DIM_OUT_XML				"set_fixture_dim_level_out"
#define SET_SWITCH_DIM_LEVEL_XML			"set_switch_dim_level"
#define SET_SWITCH_SCENE_XML				"set_switch_scene"
#define SET_PLUGLOAD_STATUS_XML				"set_plugload_status"
#define SET_DEMANDRESPONSE_IN_XML			"set_demand_response_in"
#define SET_DEMANDRESPONSE_OUT_XML			"set_demand_response_out"

#endif

#define MX_NAME     512

#ifdef EM

// 2015.12.09 Moving all fixture records to EM level so that data, info can be shared by EM based objects or Area based objects.

typedef struct fixture
{
    uint	fixtureId;
	uint	areaId;
	
    char name[MX_NAME];
    unsigned char mac_address[3];		// we use only last 3 bytes
	AnalogObjectTypeDescriptor         fixture_base_energy;
	AnalogObjectTypeDescriptor         fixture_used_energy;
	AnalogObjectTypeDescriptor         fixture_saved_energy;
	AnalogObjectTypeDescriptor         occ_saved_energy;
	AnalogObjectTypeDescriptor         amb_saved_energy2;
	AnalogObjectTypeDescriptor         task_saved_energy;
	AnalogObjectTypeDescriptor         manual_saved_energy;
	AnalogObjectTypeDescriptor         ambient_light;

	BinaryObjectTypeDescriptor         occupancy;
	BinaryObjectTypeDescriptor         fixtureOutage;
	
    AnalogCommandableTypeDescriptor		dim_level2;
	

} s_fixture_t2 ;


typedef struct
{
	unsigned int	id;
	char			name[MX_NAME];
	unsigned char	mac_address[3];		// we use only last 3 bytes
	
	AnalogObjectTypeDescriptor			plugManagedConsumption;
	AnalogObjectTypeDescriptor			plugUnManagedConsumption;
	AnalogObjectTypeDescriptor			plugTotalConsumption;
	BinaryCommandableTypeDescriptor     plugLoadStatus;
} s_plugload_t ;

#endif // EM


typedef struct sector
{
    // never used unsigned int   	bacnet_id;
    uint   				id;
    char  				za_name[MX_NAME];
	// todo 3 e_device_state_t	state;

    struct floor		*floorPtr ;

#ifdef UEM

    /* From: https://enlightedinc.atlassian.net/wiki/display/HA/API+-+Get+HVAC+Stats+for+a+Zone
     *
         Write/Read
 "bmsSetPoint": null,
 "bmsSetPointTimestamp": null,
 "bmsSetPointLow": null,
 "bmsPointLowTimestamp": null,
 "bmsSetPointHigh": null,
 "bmsPointHighTimestamp": null,
 "bmsTemperature": null,
 "bmsTemperatureTimestamp": null,
         Read Only
 "avgTemp": 72,
 "maxTemp": 72,
 "minTemp": 72,
 "setback": 0,
 "zonefailure": 0,
 "tempSetPointChange": 0,
 "airflowRecommendation": 0
     */
        // RW
    AnalogCommandableTypeDescriptor   bmsSetPoint ;
    AnalogCommandableTypeDescriptor   bmsSetPointLow ;
    AnalogCommandableTypeDescriptor   bmsSetPointHigh ;
    AnalogCommandableTypeDescriptor   bmsTemperature ;

        // RO
    AnalogObjectTypeDescriptor	maxTemp;
    AnalogObjectTypeDescriptor	minTemp;
    AnalogObjectTypeDescriptor	avgTemp;
    AnalogObjectTypeDescriptor	setback;
    AnalogObjectTypeDescriptor  zoneFailure;
    AnalogObjectTypeDescriptor	tempSetpointChange;
    AnalogObjectTypeDescriptor 	airflowRecommendation;
#endif

#ifdef EM
    uint	num_fixtures_in_area;
	uint	num_plugloads;
    AnalogObjectTypeDescriptor          area_base_energy;
    AnalogObjectTypeDescriptor          area_consumed_lighting_energy;
    AnalogObjectTypeDescriptor          area_saved_lighting_energy;
    AnalogObjectTypeDescriptor          occ_saved_energy;
    AnalogObjectTypeDescriptor          amb_saved_energy;
    AnalogObjectTypeDescriptor          task_saved_energy;
    AnalogObjectTypeDescriptor          manual_saved_energy;
    AnalogObjectTypeDescriptor          avg_dim_level;
    AnalogObjectTypeDescriptor          outageCount ;
    AnalogObjectTypeDescriptor          areaPlugManagedConsumption ;
	AnalogObjectTypeDescriptor          areaPlugUnManagedConsumption;
	AnalogObjectTypeDescriptor          areaPlugTotalConsumption;


    BinaryObjectTypeDescriptor          occupancy;

    BinaryCommandableTypeDescriptor     areaEmergency;

    // s_fixture_t		*s_sensor;
	s_plugload_t	*plugLoads;

    // s_bacObjIDlistForBACtype_t       *fixtureOidlists;
	// s_bacObjIDlistForBACtype_t       *plugloadOidlists;
#endif

} s_sector_t;		// was (lighting) area, now combining with HVAC zone



#ifdef EM

typedef struct
{
	uint	id;
	uint	switchId;
	uint	sceneId;
	uint	fixtureId;
	uint	lightLevel;
} s_scenelightlevel_t ;

typedef struct
{
	uint	id;
	uint	switchId;
	uint	sceneId;
	uint	plugloadId;
	uint	plugLevel;
} s_pluglevel_t ;

typedef struct 
{
	uint	sceneId;
	uint	switchId;
	char	sceneName[MX_NAME];
	uint	num_lightlevels;
	uint	num_pluglevels;
	
	s_scenelightlevel_t	*s_lightlevels;
	s_pluglevel_t		*s_pluglevels;
	
} s_scene_t ;

typedef struct
{
    uint		    id;
	uint			switchId;					// todo 4 - Warning !! this parameter is saved when configuring _scenes_ to be used when setting a _scene_. We need to resolve the reason why each scene has a
												// an identical 'switch ID', and these identical switch IDs are _different_ to the switchgroup ID.
    char            switch_name[MX_NAME];
    struct floor    *floorPtr ;
    // bool            valid ;
	uint		num_scenes;
	s_scene_t	*s_scenes;

    AnalogCommandableTypeDescriptor dimLevel ;
    AnalogCommandableTypeDescriptor scene ;

} s_switchgroup_t;

#endif // EM

typedef struct floor
{
    uint		   	id;
	char  			company_name[MX_NAME];
	char  			campus_name[MX_NAME];
	char  			bldg_name[MX_NAME];
	char  			floor_name[MX_NAME];
    uint 		   	num_sectors;
    s_sector_t      *s_sector ;              // A Sector is an Area (for lighting) or a Zone (for HVAC)

#ifdef EM
    uint				num_switches;
    s_switchgroup_t     *s_switch ;
#endif

} s_floor_t;


typedef struct
{
    char            em_name[MX_NAME];
	
#ifdef EM	
    AnalogObjectTypeDescriptor      energyLighting ;
    AnalogObjectTypeDescriptor      energyPlugload ;

    BinaryCommandableTypeDescriptor emergencyStatus ;

    AnalogCommandableTypeDescriptor adrLevel ;
	uint			num_fixtures;
	s_fixture_t2	*fixtures;
#endif
	
	uint			num_floors;
	s_floor_t       *floors;
	
} s_energy_manager_t;


bool elf_gen_ana_put_present_value_commandable(BACNET_OBJECT_TYPE objectType, BACNET_WRITE_PROPERTY_DATA *wp_data, float value);
int   elf_gen_ana_relinquish(BACNET_OBJECT_TYPE objectType, BACNET_WRITE_PROPERTY_DATA *wp_data);

BACNET_BINARY_PV elf_gen_bin_get_present_value( BACNET_OBJECT_TYPE objectType, uint32_t object_instance) ;
bool   elf_gen_bin_put_present_value_commandable(BACNET_OBJECT_TYPE objectType, BACNET_WRITE_PROPERTY_DATA *wp_data, BACNET_BINARY_PV value);
int   elf_gen_bin_relinquish(BACNET_OBJECT_TYPE objectType, BACNET_WRITE_PROPERTY_DATA *wp_data);
bool CheckLicenseAndFault(ObjectTypeDescriptor *bacnetObject, int value);
void StoreAnalogPresentValue(AnalogObjectTypeDescriptor *anaObject, float newPresentValue);
void StoreAnalogPresentValueConditionalInt(AnalogObjectTypeDescriptor *anaObject, int newPresentValue);

#ifdef EM
void StoreBinaryPresentValue(BinaryObjectTypeDescriptor *binObject, bool newPresentValue);
void StoreBinaryPresentValuePV(BinaryObjectTypeDescriptor *binObject, BACNET_BINARY_PV newPresentValue);
void StoreBinaryPresentValueConditionalInt(BinaryObjectTypeDescriptor *binObject, int value);
#endif // EM

BACNET_RELIABILITY GetReliability(ObjectTypeDescriptor *bacnetObject);

#endif /* __ELF_GEMS_API_H__ */
