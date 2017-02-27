#include <ctype.h>
#include <syslog.h>
#include <stdlib.h>
#include "elf.h"
#include "advdebug.h"
#include "bacenum.h"
#include "elf_config.h"

#ifdef EM
extern elf_config_t elf_bacnet_config;
#endif

ts_elf_template_object_t *elf_template_objs;
uint ux_template_objs = 0;

// fixture

// note to self, need to add extern to consts to free from being trapped in this linkage: http://goo.gl/GJJJZu

INT_TO_TEXT objectTypeText[] = {
{
	OBJECT_ANALOG_INPUT,
	"AI"
},
{
	OBJECT_ANALOG_VALUE,
	"AV"
},
{
	OBJECT_BINARY_INPUT,
	"BI"
},
{
	OBJECT_BINARY_VALUE,
	"BV"
},
{
	-1,
	NULL
}
};

typedef struct
{
	e_elf_category_t elfDevType;             // zone/fixture/area etc.
	ELF_DATA_TYPE elfDataType;
	BACNET_ENGINEERING_UNITS units;
	const char *text;
} DATATYPE_TO_TEXT;

DATATYPE_TO_TEXT dataTypeText[] = {

#ifdef EM
{

	CATEGORY_AREA,
	ELF_AREA_BASE_ENERGY_OBJECT,
	UNITS_WATT_HOURS,
	"AreaEnergyBaseload"
},
{
	CATEGORY_AREA,
	ELF_AREA_PLUG_CONSUMPTION_TOTAL,
	UNITS_WATT_HOURS,
	"AreaEnergyTotalPlugload"
},
{
	CATEGORY_AREA,
	ELF_AREA_CONSUMED_ENERGY_LIGHTING_OBJECT,
	UNITS_WATT_HOURS,
	"AreaEnergyLighting"
},
{
	CATEGORY_AREA,
	ELF_AREA_SAVED_ENERGY_OBJECT,
	UNITS_WATT_HOURS,
	"AreaEnergySaved"
},
{
	CATEGORY_AREA,
	ELF_AREA_OCC_SAVED_ENERGY_OBJECT,
	UNITS_WATT_HOURS,
	"AreaEnergyOccSavings"
},
{
	CATEGORY_AREA,
	ELF_AREA_OUTAGE_COUNT,
	UNITS_NO_UNITS,
	"AreaFixtureOutCount"
},
{
	CATEGORY_AREA,
	ELF_AREA_DAYLIGHT_SAVED_ENERGY_OBJECT,
	UNITS_WATT_HOURS,
	"AreaEnergyAmbientSavings"
},
{
	CATEGORY_AREA,
	ELF_AREA_TASK_TUNED_ENERGY_OBJECT,
	UNITS_WATT_HOURS,
	"AreaEnergyTunedSavings"
},
{
	CATEGORY_AREA,
	ELF_AREA_MANUAL_SAVED_ENERGY_OBJECT,
	UNITS_WATT_HOURS,
	"AreaEnergyManualSavings"
},
{
	CATEGORY_AREA,
	ELF_AREA_OCCUPANCY,
	UNITS_NO_UNITS,
	"AreaOccupancy"
},
{
	CATEGORY_AREA,
	ELF_AREA_EMERGENCY,
	UNITS_NO_UNITS,
	"AreaEmergency"
},
{
	CATEGORY_AREA,
	ELF_AREA_DIM_LEVEL_OBJECT,
	UNITS_PERCENT,
	"AreaDimLevel"
},
{
	CATEGORY_AREA,
	ELF_AREA_PLUG_CONSUMPTION_UNMANAGED,
	UNITS_WATT_HOURS,
	"AreaEnergyUnmanagedPlugload"
},
{
	CATEGORY_AREA,
	ELF_AREA_PLUG_CONSUMPTION_MANAGED,
	UNITS_WATT_HOURS,
	"AreaEnergyManagedPlugload"
},
{
	CATEGORY_AREA,
	ELF_AREA_EMERGENCY,
	UNITS_NO_UNITS,
	"AreaEmergency"
},

{
	CATEGORY_FIXTURE,
	ELF_FIXTURE_BASE_ENERGY_OBJECT,
	UNITS_WATT_HOURS,
	"FixtureEnergyBaseload"
},
{
	CATEGORY_FIXTURE,
	ELF_FIXTURE_CONSUMED_ENERGY_LIGHTING_OBJECT,
	UNITS_WATT_HOURS,
	"FixtureEnergyLighting"
},
{
	CATEGORY_FIXTURE,
	ELF_FIXTURE_SAVED_ENERGY_OBJECT,
	UNITS_WATT_HOURS,
	"FixtureEnergySaved"
},
{
	CATEGORY_FIXTURE,
	ELF_FIXTURE_OCC_SAVED_ENERGY_OBJECT,
	UNITS_WATT_HOURS,
	"FixtureEnergyOccSavings"
},
{
	CATEGORY_FIXTURE,
	ELF_FIXTURE_DAYLIGHT_SAVED_ENERGY_OBJECT,
	UNITS_WATT_HOURS,
	"FixtureEnergyAmbientSavings"
},
{
	CATEGORY_FIXTURE,
	ELF_FIXTURE_TASK_TUNED_ENERGY_OBJECT,
	UNITS_WATT_HOURS,
	"FixtureEnergyTunedSavings"
},
{
	CATEGORY_FIXTURE,
	ELF_FIXTURE_MANUAL_SAVED_ENERGY_OBJECT,
	UNITS_WATT_HOURS,
	"FixtureEnergyManualSavings"
},
	{
		CATEGORY_FIXTURE,
		ELF_FIXTURE_AMBIENT_LIGHT,
		UNITS_FOOT_CANDLES,
		"FixtureAmbientLight"
	},
	{
	CATEGORY_AREA_PLUGLOAD,
	ELF_PLUG_CONSUMPTION_TOTAL,
	UNITS_WATT_HOURS,
	"PlugloadEnergyTotal"
},
{
	CATEGORY_AREA_PLUGLOAD,
	ELF_PLUG_CONSUMPTION_MANAGED,
	UNITS_WATT_HOURS,
	"PlugloadEnergyManaged"
},
{
	CATEGORY_AREA_PLUGLOAD,
	ELF_PLUG_CONSUMPTION_UNMANAGED,
	UNITS_WATT_HOURS,
	"PlugloadEnergyUnmanaged"
},
{
	CATEGORY_FIXTURE,
	ELF_FIXTURE_DIM_LEVEL_OBJECT,
	UNITS_PERCENT,
	"FixtureDimLevel"
},
{
	CATEGORY_FIXTURE,
	ELF_FIXTURE_OUTAGE,
	UNITS_NO_UNITS,
	"FixtureOutage"
},
{
	CATEGORY_FIXTURE,
	ELF_FIXTURE_OCCUPANCY,
	UNITS_NO_UNITS,
	"FixtureOccupancy"
},

{
	CATEGORY_AREA_PLUGLOAD,
	ELF_PLUG_STATUS,
	UNITS_PERCENT,
	"PlugloadStatus"
},
{
	CATEGORY_SWITCH,
	ELF_SWITCH_SCENE,
	UNITS_NO_UNITS,
	"SwitchScene"
},
{
	CATEGORY_SWITCH,
	ELF_SWITCH_DIM,
	UNITS_PERCENT,
	"SwitchDim"
},

{
	CATEGORY_EM,
	ELF_EM_ENERGY_LIGHTING,
	UNITS_WATT_HOURS,
	"EMEnergyLighting"
},
{
	CATEGORY_EM,
	ELF_EM_ENERGY_PLUGLOAD,
	UNITS_WATT_HOURS,
	"EMEnergyPlugload"
},

{
	CATEGORY_EM,
	ELF_EM_ADR_LEVEL,
	UNITS_NO_UNITS,
	"EMADRlevel"
},
{
	CATEGORY_EM,
	ELF_EM_EMERGENCY,
	UNITS_NO_UNITS,
	"EMEmergency"
},

#endif

#ifdef UEM
{
	CATEGORY_AREA,
	ELF_BMS_SETPOINT,
	UNITS_DEGREES_FAHRENHEIT,
	"bmsSetpoint"
 },
{
	CATEGORY_AREA,
	ELF_BMS_SETPOINT_LOW,
	UNITS_DEGREES_FAHRENHEIT,
	"bmsSetpointLow" 
},
{
	CATEGORY_AREA,
	ELF_BMS_SETPOINT_HIGH,
	UNITS_DEGREES_FAHRENHEIT,
	"bmsSetpointHigh" 
},
{
	CATEGORY_AREA,
	ELF_BMS_TEMPERATURE,
	UNITS_DEGREES_FAHRENHEIT,
	"bmsTemperature" 
},
{
	CATEGORY_AREA,
	ELF_AI_ZONE_MIN_TEMP,
	UNITS_DEGREES_FAHRENHEIT,
	"minTemp" 
},
{
	CATEGORY_AREA,
	ELF_AI_ZONE_MAX_TEMP,
	UNITS_DEGREES_FAHRENHEIT,
	"maxTemp" 
},
{
	CATEGORY_AREA,
	ELF_AI_ZONE_AVG_TEMP,
	UNITS_DEGREES_FAHRENHEIT,
	"avgTemp" 
},
{
	CATEGORY_AREA,
	ELF_ZONE_SETBACK,
	UNITS_NO_UNITS,							// just a mode, 0, 1, 2, 3
	"setback" 
},
{
	CATEGORY_AREA,
	ELF_ZONE_FAILURE,
	UNITS_NO_UNITS,
	"zonefailure" 
},
{
	CATEGORY_AREA,
	ELF_AI_ZONE_TEMP_SP_CHANGE,
	UNITS_DEGREES_FAHRENHEIT,
	"tempSetPointChange" 
},
{
	CATEGORY_AREA,
	ELF_AI_ZONE_AIRFLOW_RECOMMENDATION,
	UNITS_NO_UNITS,
	"airflowRecommendation" 
},
#endif
};

const char *IntToText(const INT_TO_TEXT dict[], int value)
{
	int i = 0;
	while (dict[i].value >= 0)
	{
		if (dict[i].value == value)
		{
		    // we have it
			return dict[i].text;
		}
		i++;
	}
	panic("Illegal text [%d]", value);
	return "Illegal Text Type" ;
}


static int intFromText(const INT_TO_TEXT *dict, const char *text)
{
	int i = 0;
	while (dict[i].value >= 0)
	{
		if (strcasecmp(dict[i].text, text) == 0)
		{
		    // we have it
			return dict[i].value;
		}
		i++;
	}
	panic("Illegal text [%s]", text);
	return 0;
}

static DATATYPE_TO_TEXT *DataTypeFromText(const char *text)
{
	unsigned int i;
	for (i = 0; i < sizeof(dataTypeText) / sizeof(DATATYPE_TO_TEXT); i++)
	{
		if (strcasecmp(dataTypeText[i].text, text) == 0)
		{
		    // we have it
			return &dataTypeText[i];
		}
	}
	panic("Illegal text [%s]", text);
	return NULL;
}

//#ifdef EM
//bool elf_is_fixture_instance_valid(BACNET_OBJECT_TYPE bacnet_object_type, uint8_t instance)
//{
    //int i;
    //ts_elf_template_object_t *optr = (ts_elf_template_object_t *) elf_template_objs;
//
    //for (i = 0; i < ux_template_objs; i++, optr++)
    //{
        //if ((optr->bacnet_obj_type == bacnet_object_type) && optr->elfCategory == CATEGORY_AREA_FIXTURE &&
        //// optr->objectEnabled == true &&
                //optr->objectInstance == GET_ADDER_FROM_INSTANCE(instance))
        //{
            //return true;
        //}
    //}
//
    //return false;
//}
//
//
//bool elf_is_plugload_instance_valid(BACNET_OBJECT_TYPE bacnet_object_type, uint8_t instance)
//{
	//int i;
	//ts_elf_template_object_t *optr = (ts_elf_template_object_t *) elf_template_objs;
//
	//for (i = 0; i < ux_template_objs; i++, optr++)
	//{
		//if ((optr->bacnet_obj_type == bacnet_object_type) && optr->elfCategory == CATEGORY_AREA_PLUGLOAD &&
		//// optr->objectEnabled == true &&
		        //optr->objectInstance == GET_ADDER_FROM_INSTANCE(instance))
		//{
			//return true;
		//}
	//}
//
	//return false;
//}
//#endif // EM

#ifdef EM
uint elf_get_template_object_count_per_category(e_elf_category_t category, BACNET_OBJECT_TYPE object_type)
#else
uint elf_get_template_object_count(BACNET_OBJECT_TYPE object_type)
#endif
{
	uint16_t count = 0;
	unsigned int i;
	ts_elf_template_object_t *optr = (ts_elf_template_object_t *) elf_template_objs;

	for (i = 0; i < ux_template_objs; i++)
	{
#ifdef EM	    
		if (optr->elfCategory == category)
#endif	        
		{
			if (optr->bacnet_obj_type == object_type)
			{
				++count;
			}
		}
		optr++;
	}
	return count;
}

//#ifdef EM
//uint16_t elf_get_number_of_objects_per_fixture(BACNET_OBJECT_TYPE object_type)
//{
    //uint16_t count = 0, i;
    //ts_elf_template_object_t *optr = (ts_elf_template_object_t *) elf_template_objs;
    //for (i = 0; i < ux_template_objs; i++, optr++)
    //{
        //if (((optr->elfCategory == CATEGORY_AREA_FIXTURE)))
        //{
            //if ((optr->bacnet_obj_type == object_type)
            //// && (optr->objectEnabled == true)
            //)
            //{
                //++count;
            //}
        //}
    //}
    //return count;
//}
//#endif

// EKH: OBJECT_CFG_INDEX removed, see cr00001
#if 0
int16_t elf_get_object_cfg_index(uint16_t object_type, uint8_t objectIndex)
{
	ts_elf_template_object_t *optr = (ts_elf_template_object_t *)elf_template_objs;
	uint8_t enl_obj_type = 0;
	uint8_t ltg_zone_obj_count = 0;

	    // Get lighting zone object count
	ltg_zone_obj_count = elf_get_template_object_count_per_category(object_type);

	if (elf_bacnet_config.mode == ZONE_ONLY_MODE)
	{
		if ((objectIndex >= STARTING_ZONES_OBJECT_INDEX) &&
		        (objectIndex < ltg_zone_obj_count))
		{
			enl_obj_type = LTG_ZONE_DEVICE_TYPE;
		}
	}
	else if (elf_bacnet_config.mode == SENSORS_ONLY_MODE)
	{
		enl_obj_type = SENSOR_OBJECT_TYPE;
	}
	else if (elf_bacnet_config.mode == ZONE_SENSORS_MODE)
	{
		if (objectIndex < ltg_zone_obj_count)
		{
			enl_obj_type = LTG_ZONE_DEVICE_TYPE;
		}
		else
		{
			enl_obj_type = SENSOR_OBJECT_TYPE;
		}
	}

	for (; optr->name[0]; optr++)
	{
		if (((optr->enl_obj_type == enl_obj_type)) &&
		        (optr->bacnet_obj_type == object_type) &&
		        (optr->assigned_index == objectIndex) &&
		        (optr->eodEnabled == true))
		{
			return optr->cfg_index;
		}
	}

	return -1;
}
#endif

//extern bool elf_valid_object_instance(BACNET_OBJECT_TYPE bacType, uint32_t instance)
//{
//    ts_elf_template_object_t *optr = (ts_elf_template_object_t *) elf_objs;
//    // cr00002 - uint8_t device_type = elf_get_device_type();
//
//    for (; optr->name[0]; optr++)
//    {
//        if ((optr->bacnet_obj_type == bacType) && (optr->cfg_elf_object_type == instance))
//        {
//            return true;
//        }
//    }
//    return false;
//}

int8_t elf_object_template_setup(void)
{
	FILE *fp;
	char buffer[512];
	unsigned int i, max_objects = MAX_OBJECTS;
	char *ptr = NULL, *tptr;

	if ((fp = fopen(elf_get_objects_file(), "r")) == NULL)
	{
		log_printf(LOG_ERR, "Error opening bacnet objects file");
		exit(1);
	}

	    /* Pre-allocate objects. */
	elf_template_objs = (ts_elf_template_object_t *) calloc((max_objects + 1), sizeof(ts_elf_template_object_t));
	if (elf_template_objs == NULL)
	{
		log_printf(LOG_CRIT, "%s:%d - Error allocating memory", __FUNCTION__, __LINE__);
		exit(EXIT_FAILURE);
	}

	char *nptr[10];
	ts_elf_template_object_t *obj_ptr;

	while (fgets(buffer, sizeof(buffer), fp) != NULL)
	{
		int nptrCount = 0;
		tptr = buffer;

		if (buffer[0] == '#' || buffer[0] == '\r' || buffer[0] == '\n')
		{
			continue;
		}

		buffer[strlen(buffer) - 1] = 0;
		obj_ptr = (ts_elf_template_object_t *) &elf_template_objs[ux_template_objs];

		while ((tptr = strtok_r(tptr, ":", &ptr)) != NULL)
		{
			nptr[nptrCount++] = tptr;
			tptr = NULL;
		}

		if (nptrCount != 5)
		{
			log_printf(LOG_ERR, "Incorrect number of parameters in [%s]", elf_get_objects_file());
			continue;
		}

		obj_ptr->readOnly = false;
		// Is this point enabled?
		switch (toupper(nptr[3][0]))
		{
		case 'Y':
			break ;
		case 'R':
			obj_ptr->readOnly = true; 
			break;
		case 'N':
			continue;
		default:
			panic("Unknown setting");
			continue;
		}

		int j;
		for (j = 0; j < nptrCount; j++)
		{
		    // strip trailing spaces and tabs
			int ii = strlen(nptr[j]) - 1;
			for (; ii > 0; ii--)
			{
				if (nptr[j][ii] != ' ' && nptr[j][ii] != '\t')
					break;
				nptr[j][ii] = 0;
			}
			// todo 4 - strip leading spaces next
		}


		obj_ptr->bacnet_obj_type = (BACNET_OBJECT_TYPE) intFromText(objectTypeText, nptr[0]);
		obj_ptr->objectInstance = atoi(nptr[1]);
		// strcpy(obj_ptr->name, nptr[1]);
		strcpy(obj_ptr->description, nptr[2]);
	    
		DATATYPE_TO_TEXT *dts = DataTypeFromText(nptr[4]);
		if (dts == NULL)
		{
			panic("Unknown Datatype %d", nptr[4]);
			continue;
		}
	    
		obj_ptr->units = dts->units;
		obj_ptr->elfDataType = dts->elfDataType;
		obj_ptr->elfCategory = dts->elfDevType;
	    
#ifdef EM
	    // check for non-supported datatypes in normal (as opposed Detailed) mode
	    if( ! elf_bacnet_config.detailedMode)
	    {
		    // 11/19 Reinstated for v1.14.25
		    //if (obj_ptr->elfDataType == ELF_EM_ENERGY_LIGHTING || 
			    //obj_ptr->elfDataType == ELF_EM_ENERGY_PLUGLOAD || 
			    //obj_ptr->elfDataType == ELF_AREA_PLUG_CONSUMPTION_TOTAL ||
				//obj_ptr->elfDataType == ELF_AREA_PLUG_CONSUMPTION_MANAGED || 
				//obj_ptr->elfDataType == ELF_AREA_PLUG_CONSUMPTION_UNMANAGED)
		    //{
			    //// ignore
			    //continue ;
		    //}
		    
		    
			if (obj_ptr->elfCategory == CATEGORY_FIXTURE || 
				obj_ptr->elfCategory == CATEGORY_AREA_PLUGLOAD )
				// 12/1 Allowing switch groups in simple mode too!
				// obj_ptr->elfCategory == CATEGORY_SWITCH)
			{
				// ignore these
				continue ;
			}
		}
		
		    // Disallow Occupied / Ambient points if not licensed
		if (!elf_bacnet_config.fixtureAmbient)
		{
			if (dts->elfDataType == ELF_FIXTURE_AMBIENT_LIGHT ||
				dts->elfDataType == ELF_FIXTURE_OCCUPANCY)
			{
				continue ;
			}
		}
		
#endif // EM
	    
		++ux_template_objs;
		if (ux_template_objs == max_objects)
		{
			panic("Too many BACnet objects defined");
			break;
			// todo5 - make linked list so list can be expanded on the fly.
		}
	}

	log_printf(LOG_INFO, "Reading objects list");
	for (i = 0; i < ux_template_objs; i++)
	{
		log_printf(LOG_INFO, "bacnet obj type = %d", elf_template_objs[i].bacnet_obj_type);
		// log_printf(LOG_INFO, "name = %s", elf_template_objs[i].name);
		log_printf(LOG_INFO, "description = %s", elf_template_objs[i].description);
		log_printf(LOG_INFO, "units = %d", elf_template_objs[i].units);
		// EKH: OBJECT_CFG_INDEX removed, see cr00001
		log_printf(LOG_INFO, "Instance adder = %d", elf_template_objs[i].objectInstance);
		// log_printf(LOG_INFO, "assigned objectIndex = %d", elf_objs[i].assigned_index);
		// EKH: OBJECT_CFG_INDEX removed, see cr00001
		// log_printf(LOG_INFO, "Object Enabled = %d", elf_template_objs[i].objectEnabled);
#ifdef EM
		const char *emt = "";
		switch (elf_template_objs[i].elfCategory)
		{
		case CATEGORY_AREA:
			emt = "ltg-area";
			break;
		case CATEGORY_EM:
			emt = "EM";
			break;
		case CATEGORY_FIXTURE:
			emt = "Fixture";
			break;
		case CATEGORY_AREA_PLUGLOAD:
			emt = "Plugload";
			break;
		case CATEGORY_SWITCH:
			emt = "Switch";
			break;
		default:
			panic("Unknown");
		}
		log_printf(LOG_INFO, "enl obj type = %d(%s)", elf_template_objs[i].elfCategory, emt);
#endif
	}

	log_printf(LOG_INFO, "num objects = %d", ux_template_objs);

	//    ts_elf_objects_index_t *iptr = NULL;
	//    uint8_t j = 0;
	//    for (i = 0; i < MAX_DEVICE_TYPES; i++)
	//    {
	//        elf_objs_index_list[i] = calloc(1, sizeof(ts_elf_objects_index_list_t));
	//        if (elf_objs_index_list[i] == NULL)
	//        {
	//            log_printf(LOG_CRIT, "Unable to allocate memory for objects index list");
	//            exit(EXIT_FAILURE);
	//        }
	//        elf_objs_index_list[i]->index_ptr = calloc(MAX_OBJECT_TYPES_PER_DEVICE, sizeof(ts_elf_objects_index_t));
	//        if (elf_objs_index_list[i]->index_ptr == NULL)
	//        {
	//            log_printf(LOG_CRIT, "Unable to allocate memory for objects index");
	//            exit(EXIT_FAILURE);
	//        }
	//
	//        iptr = (ts_elf_objects_index_t *) elf_objs_index_list[i]->index_ptr;
	//        for (j = 0; j < MAX_OBJECT_TYPES_PER_DEVICE; j++, iptr++)
	//        {
	//            iptr->elfCategory = i;
	//            iptr->bacnet_obj_type = 0xFFFF;
	//        }
	//    }
	//
	//    for (i = 0; i < num_objs; i++)
	//    {
	//        // Point to the device
	//        iptr = (ts_elf_objects_index_t *) elf_objs_index_list[elf_objs[i].elfCategory]->index_ptr;
	//        for (j = 0; j < MAX_OBJECT_TYPES_PER_DEVICE; j++, iptr++)
	//        {
	//            if (iptr->bacnet_obj_type == 0xFFFF)
	//            {
	//                iptr->bacnet_obj_type = elf_objs[i].bacnet_obj_type;
	//            }
	//            if (iptr->bacnet_obj_type == elf_objs[i].bacnet_obj_type)
	//            {
	//                // Found object type
	//                if (elf_objs[i].index_valid)
	//                {
	//                    // EKH: OBJECT_CFG_INDEX removed, see cr00001
	//                    // iptr->valid_obj_index[iptr->valid_obj_count++] = elf_objs[i].assigned_index;
	//                    iptr->valid_obj_index[iptr->valid_obj_count++] = elf_objs[i].cfg_elf_object_type;
	//                }
	//                break;
	//            }
	//        }
	//    }

	fclose(fp);
	return 0;
}
