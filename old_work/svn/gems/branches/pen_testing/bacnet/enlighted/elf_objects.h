#ifndef __ELF_OBJECTS_H__
#define __ELF_OBJECTS_H__

#include <stdint.h>
#include "bacenum.h"

#define MAX_BACNET_TYPES_PER_ELF_DEVICE     (4) // AI, BI, AV, BV

#define MAX_OBJECTS            (100)

extern uint ux_template_objs;

#ifdef EM
#define	SUBCAT_FIXTURE	1
#define	SUBCAT_PLUGLOAD	2

#define SUBCAT_POSN	1000000

// todo 4 - make these functions with bounds checks.
// todo 3 - need to make space for 25000 fixtures??
#define GET_ADDER_FROM_INSTANCE(oi)			(oi % 100)
#define GET_FIXTURE_ID_FROM_INSTANCE(oi)    ((oi % SUBCAT_POSN) / 100)	
#define GET_PLUGLOAD_ID_FROM_INSTANCE(oi)   ((oi % SUBCAT_POSN) / 100)	
#define GET_SUBCAT_FROM_INSTANCE(oi)		((oi/SUBCAT_POSN)%10)

#define SET_FIXTURE_INSTANCE(fid,objectIndex)     ((unsigned int) ((unsigned int) SUBCAT_FIXTURE * SUBCAT_POSN + (fid * 100 ) + (objectIndex)))
#define SET_PLUGLOAD_INSTANCE(plid,objectIndex)    ((unsigned int) ((unsigned int) SUBCAT_PLUGLOAD * SUBCAT_POSN + (plid * 100 ) + (objectIndex)))

#define OID_OFFSET_SCENE		10000
#define OID_OFFSET_SGPL_TYPE	1000 

#define GET_SCENE_SCENE_ID_FROM_INSTANCE(oi)		(oi/OID_OFFSET_SCENE) 
#define GET_SCENE_SUBCAT_FROM_INSTANCE(oi)			((oi/OID_OFFSET_SGPL_TYPE)%10)
#define SET_SCENE_PLUGLOAD_INSTANCE(scid,plid)		((uint32_t) scid * OID_OFFSET_SCENE + SUBCAT_PLUGLOAD * OID_OFFSET_SGPL_TYPE + plid )
#define GET_SCENE_PLUGLOAD_ID_FROM_INSTANCE(oi)		(oi%OID_OFFSET_SGPL_TYPE)

#define SET_SCENE_FIXTURE_INSTANCE(scid,fid)		((uint32_t) scid * OID_OFFSET_SCENE + SUBCAT_FIXTURE * OID_OFFSET_SGPL_TYPE + fid )
#define GET_SCENE_FIXTURE_ID_FROM_INSTANCE(oi)		(oi%OID_OFFSET_SGPL_TYPE)

#endif // EM

typedef struct
{
	int value;
	const char *text;
} INT_TO_TEXT;


typedef enum
{

#ifdef UEM
    // RW
	ELF_BMS_SETPOINT                            = 4441,
	ELF_BMS_SETPOINT_HIGH,
	ELF_BMS_SETPOINT_LOW,
	ELF_BMS_TEMPERATURE,
	// RO
	ELF_AI_ZONE_MIN_TEMP,
	ELF_AI_ZONE_MAX_TEMP,
	ELF_AI_ZONE_AVG_TEMP,
	ELF_ZONE_SETBACK,
	ELF_ZONE_FAILURE,
	ELF_AI_ZONE_TEMP_SP_CHANGE,
	ELF_AI_ZONE_AIRFLOW_RECOMMENDATION,
#endif

#ifdef EM
	ELF_AREA_BASE_ENERGY_OBJECT                 = 3331,
	ELF_AREA_CONSUMED_ENERGY_LIGHTING_OBJECT,
	ELF_AREA_SAVED_ENERGY_OBJECT,
	ELF_AREA_OCC_SAVED_ENERGY_OBJECT,
	ELF_AREA_DAYLIGHT_SAVED_ENERGY_OBJECT,
	ELF_AREA_TASK_TUNED_ENERGY_OBJECT,
	ELF_AREA_MANUAL_SAVED_ENERGY_OBJECT,
	ELF_AREA_DIM_LEVEL_OBJECT,
	ELF_AREA_OCCUPANCY,
	ELF_AREA_OUTAGE_COUNT,
	ELF_AREA_PLUG_CONSUMPTION_TOTAL,
	ELF_AREA_PLUG_CONSUMPTION_MANAGED,
	ELF_AREA_PLUG_CONSUMPTION_UNMANAGED,
	ELF_AREA_EMERGENCY,

	ELF_FIXTURE_BASE_ENERGY_OBJECT              = 4441,
	ELF_FIXTURE_CONSUMED_ENERGY_LIGHTING_OBJECT,
	ELF_FIXTURE_SAVED_ENERGY_OBJECT,
	ELF_FIXTURE_OCC_SAVED_ENERGY_OBJECT,
	ELF_FIXTURE_DAYLIGHT_SAVED_ENERGY_OBJECT,
	ELF_FIXTURE_TASK_TUNED_ENERGY_OBJECT,
	ELF_FIXTURE_MANUAL_SAVED_ENERGY_OBJECT,
	ELF_FIXTURE_AMBIENT_LIGHT,
	ELF_FIXTURE_DIM_LEVEL_OBJECT,
	ELF_FIXTURE_OUTAGE,
	ELF_FIXTURE_OCCUPANCY,

	ELF_PLUG_CONSUMPTION_TOTAL 					= 7771,
	ELF_PLUG_CONSUMPTION_MANAGED,
	ELF_PLUG_CONSUMPTION_UNMANAGED,
	ELF_PLUG_STATUS,
	
	ELF_SWITCH_SCENE                            = 5551,
	ELF_SWITCH_DIM,
	ELF_SWITCH_SCENE_DIM,
	ELF_SWITCH_SCENE_PLUGLOAD,
	
	ELF_EM_ENERGY_LIGHTING                      = 6661,
	ELF_EM_ENERGY_PLUGLOAD,
	ELF_EM_EMERGENCY,
	ELF_EM_ADR_LEVEL

		
#endif

} ELF_DATA_TYPE;


typedef enum
{
	CATEGORY_EM = 10,
#ifdef EM
	CATEGORY_SWITCH,
	CATEGORY_SWITCH_SCENE_FIXTURE,
	CATEGORY_SWITCH_SCENE_PLUGLOAD,
	CATEGORY_AREA,				
	CATEGORY_FIXTURE,			
	CATEGORY_AREA_PLUGLOAD,		
#else
	CATEGORY_AREA,
#endif // EM/UEM
	CATEGORY_UNKNOWN
} e_elf_category_t;

typedef enum
{
	ELF_DEVICE_EM = 20,
	ELF_DEVICE_AREA,		// todo 4 rename to sector
#ifdef EM	
	ELF_DEVICE_SWITCH,
#endif // EM
} e_elf_device_t;


typedef struct elf_objects
{
	BACNET_OBJECT_TYPE bacnet_obj_type;
	char description[128];
	BACNET_ENGINEERING_UNITS units;

	uint8_t				objectInstance;
	ELF_DATA_TYPE		elfDataType;
	bool				readOnly;
	e_elf_category_t	elfCategory;
	// todonext4 - replace once visualgdb sorted }__attribute ((__packed__)) ts_elf_template_object_t;
} ts_elf_template_object_t;

#endif /* __ELF_OBJECTS_H__ */


