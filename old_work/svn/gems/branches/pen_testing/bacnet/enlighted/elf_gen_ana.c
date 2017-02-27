#include <syslog.h>
#include <time.h>
#include "elf_std.h"
#include "elf.h"
#include "elf_gems_api.h"
#include "advdebug.h"
#include "elf_objects.h"
#include "elf_functions.h"

// Saves data locally for possible readback scenarios
static void elf_gen_ana_put_present_value_local(BACNET_OBJECT_TYPE objectType, uint32_t object_instance, float value)
{
	uint32_t deviceId = elf_get_current_bacnet_device_instance();
	ts_elf_template_object_t *ptr = find_template_object_record_current_device(objectType, object_instance);
	if (ptr == NULL)
	{
		panic("Not found");
		return;
	}

#ifdef UEM
	s_sector_t *zptr = get_sector_ptr_by_instance(deviceId);
	if (!zptr)
	{
		panic("Not found");
		return;
	}

	switch (ptr->elfDataType)
	{
	case ELF_BMS_SETPOINT:
		StoreAnalogPresentValue(&zptr->bmsSetPoint.analogTypeDescriptor, value);
		break;
	case ELF_BMS_SETPOINT_HIGH:
		StoreAnalogPresentValue(&zptr->bmsSetPointHigh.analogTypeDescriptor, value);
		break;
	case ELF_BMS_SETPOINT_LOW:
		StoreAnalogPresentValue(&zptr->bmsSetPointLow.analogTypeDescriptor, value);
		break;
	case ELF_BMS_TEMPERATURE:
		StoreAnalogPresentValue(&zptr->bmsTemperature.analogTypeDescriptor, value);
		break;
	default:
		panic("Not appropriate for write");
		// todo, there is no "mode" feedback similar to setback...
		// dotonext8 - put in debug statement here
	}
#endif // UEM

#ifdef EM
	switch (ptr->elfCategory)
	{
	case CATEGORY_AREA:
		{
			panic("Not appropriate");
		}
		break;
		
	case CATEGORY_FIXTURE:
		{
		// this is a Fixture point, not a base point
			s_fixture_t2 *sptr = get_area_fixture_ptr(object_instance);
			if (!sptr)
			{
				panic("Not found");
				return;
			}
			switch (ptr->elfDataType)
			{
			case ELF_FIXTURE_DIM_LEVEL_OBJECT:
				StoreAnalogPresentValue(&sptr->dim_level2.analogTypeDescriptor, value);
				break;
			default:
				panic("Not appropriate for write");
				break;
			}
		}
		break;
		
	case CATEGORY_AREA_PLUGLOAD:
		{
			panic("Not appropriate");
		}
		break;
		
	case CATEGORY_EM:
		{
			s_energy_manager_t *emPtr = get_energy_manager_ptr();
			if (emPtr == NULL)
			{
				panic("No EM");
				break;
			}
			switch (ptr->elfDataType)
			{
			case ELF_EM_ADR_LEVEL:
				StoreAnalogPresentValue(&emPtr->adrLevel.analogTypeDescriptor, value);
				break;
			default:
				panic("Not appropriate");
				break;
			}
		}
		break;
		
	case CATEGORY_SWITCH:
		{
			s_switchgroup_t *swPtr = get_switch_ptr(deviceId);
			switch (ptr->elfDataType)
			{
			case ELF_SWITCH_DIM:
				StoreAnalogPresentValue(&swPtr->dimLevel.analogTypeDescriptor, value);
				break;
			case ELF_SWITCH_SCENE:
				StoreAnalogPresentValue(&swPtr->scene.analogTypeDescriptor, value);
				break;
			default:
				panic("Not valid");
				break;
			}
		}
		break;
		
	default:
		panic("Unknown");
	}
#endif // EM
}


static int elf_gen_ana_scan_and_update_pv(
	BACNET_OBJECT_TYPE bacnetObjectType, 
	uint32_t deviceInstance, 
	ts_elf_template_object_t *eoPtr, 
	AnalogCommandableTypeDescriptor *bacnetObject,
	BACNET_WRITE_PROPERTY_DATA *wp_data)
{
	float value = bacnetObject->relinquishDefault;
	int rc = BACNET_STATUS_ERROR;
	int i;

	for (i = 0; i < BACNET_MAX_PRIORITY; i++)
	{
		if (bacnetObject->priorityAsserted[i])
		{
			value = bacnetObject->priorityArray[i];
			break;
		}
	}

	log_printf(LOG_INFO, "Writing %f to object instance d for for device d", value);

#ifdef EM
	s_switchgroup_t *swPtr;
	
	switch (eoPtr->elfCategory)
	{
	case CATEGORY_AREA:
		panic("not appropriate");
		break;
		
	case CATEGORY_FIXTURE:
		switch (eoPtr->elfDataType)
		{
		case ELF_FIXTURE_DIM_LEVEL_OBJECT:
			rc = set_fixture_dim_level(wp_data->object_instance, (int) value);
			break;
		default:
			panic("not appropriate");
			break;
		}
		break;

	case CATEGORY_AREA_PLUGLOAD:
		panic("not appropriate");
		break;
		
	case CATEGORY_SWITCH:
		switch (eoPtr->elfDataType)
		{
		case ELF_SWITCH_DIM:
			swPtr = get_switch_ptr(deviceInstance);
			if (swPtr != NULL)
			{
				rc = set_switch_dim_level(swPtr->floorPtr->id, swPtr->switch_name, (int) value);
			}
			break;

		case ELF_SWITCH_SCENE:
			swPtr = get_switch_ptr(deviceInstance);
			if (swPtr != NULL)
			{
				rc = set_switch_scene(swPtr->switchId, (int) value);
			}
			break;

		default:
			panic("why? %d", eoPtr->elfDataType);
			break;
		}
		break;
		
	case CATEGORY_EM:
		switch (eoPtr->elfDataType)
		{
		case ELF_EM_ADR_LEVEL:
			if (value >= 1.0 && value <= 4.0)
			{
				rc = set_em_demandResponse_level(get_energy_manager_ptr(), value);
			}
			else 
			{
				wp_data->error_class = ERROR_CLASS_PROPERTY;
				wp_data->error_code = ERROR_CODE_VALUE_OUT_OF_RANGE;
			}
			break;
		default:
			panic("not appropriate");
		}
		break;
		
	default:
		panic("Illegal");
		break;
	}

#else
	rc = set_elf_value(deviceInstance, eoPtr->elfDataType, value);
#endif

	if (rc == BACNET_STATUS_OK)
	{
	    // store a 'local copy' of the value in our memory tables for possible readback scenarios
		elf_gen_ana_put_present_value_local(bacnetObjectType, wp_data->object_instance, value);
	}
	else
	{
		wp_data->error_class = ERROR_CLASS_PROPERTY;
		wp_data->error_code = ERROR_CODE_WRITE_ACCESS_DENIED;
	}

	return rc;
}


AnalogObjectTypeDescriptor* elf_get_analog_object_live_data_old(const ts_elf_template_object_t *ptr, const uint32_t objectInstance)
{
	uint32_t deviceInstance = elf_get_current_bacnet_device_instance();
	return elf_get_analog_object_live_data(deviceInstance, ptr, objectInstance);
}


AnalogObjectTypeDescriptor hardcodedAnalogObjectForScenes;

AnalogObjectTypeDescriptor* elf_get_analog_object_live_data(const uint32_t deviceInstance, const ts_elf_template_object_t *ptr, const uint32_t objectInstance)
{
#ifdef UEM
	s_sector_t *sectorPtr = get_sector_ptr_by_instance(deviceInstance);
	if (sectorPtr == NULL)
	{
		panic("Null");
		return NULL;
	}
	switch (ptr->elfDataType)
	{

	case ELF_BMS_SETPOINT:
		return (AnalogObjectTypeDescriptor *)&sectorPtr->bmsSetPoint;
	case ELF_BMS_SETPOINT_HIGH:
		return (AnalogObjectTypeDescriptor *)&sectorPtr->bmsSetPointHigh;
	case ELF_BMS_SETPOINT_LOW:
		return (AnalogObjectTypeDescriptor *)&sectorPtr->bmsSetPointLow;
	case ELF_BMS_TEMPERATURE:
		return (AnalogObjectTypeDescriptor *)&sectorPtr->bmsTemperature;
	case ELF_AI_ZONE_MIN_TEMP:
		return (AnalogObjectTypeDescriptor *)&sectorPtr->minTemp;
	case ELF_AI_ZONE_MAX_TEMP:
		return (AnalogObjectTypeDescriptor *)&sectorPtr->maxTemp;
	case ELF_AI_ZONE_AVG_TEMP:
		return (AnalogObjectTypeDescriptor *)&sectorPtr->avgTemp;
	case ELF_ZONE_SETBACK:
		return (AnalogObjectTypeDescriptor *)&sectorPtr->setback;
	case ELF_ZONE_FAILURE:
		return (AnalogObjectTypeDescriptor *)&sectorPtr->zoneFailure;
	case ELF_AI_ZONE_TEMP_SP_CHANGE:
		return (AnalogObjectTypeDescriptor *)&sectorPtr->tempSetpointChange;
	case ELF_AI_ZONE_AIRFLOW_RECOMMENDATION:
		return (AnalogObjectTypeDescriptor *)&sectorPtr->airflowRecommendation;
	default:
		return NULL;
	}
#endif // UEM

#ifdef EM
	
	switch (ptr->elfCategory)
	{
	case CATEGORY_AREA:
		{
			s_sector_t *sectorPtr = get_sector_ptr_by_instance(deviceInstance);
			if (sectorPtr == NULL)
			{
				panic("did not find ptr");
				return NULL;
			}

			switch (ptr->elfDataType)
			{
			case ELF_AREA_BASE_ENERGY_OBJECT:
				return &sectorPtr->area_base_energy;
			case ELF_AREA_CONSUMED_ENERGY_LIGHTING_OBJECT:
				return &sectorPtr->area_consumed_lighting_energy;
			case ELF_AREA_OUTAGE_COUNT:
				return &sectorPtr->outageCount;
			case ELF_AREA_SAVED_ENERGY_OBJECT:
				return &sectorPtr->area_saved_lighting_energy;
			case ELF_AREA_OCC_SAVED_ENERGY_OBJECT:
				return &sectorPtr->occ_saved_energy;
			case ELF_AREA_DAYLIGHT_SAVED_ENERGY_OBJECT:
				return &sectorPtr->amb_saved_energy;
			case ELF_AREA_TASK_TUNED_ENERGY_OBJECT:
				return &sectorPtr->task_saved_energy;
			case ELF_AREA_MANUAL_SAVED_ENERGY_OBJECT:
				return &sectorPtr->manual_saved_energy;
			case ELF_AREA_DIM_LEVEL_OBJECT:
				return &sectorPtr->avg_dim_level;
		//case ELF_AREA_OCCUPANCY:
			//return  &sectorPtr->occupancy;
			case ELF_AREA_PLUG_CONSUMPTION_MANAGED:
				return &sectorPtr->areaPlugManagedConsumption;
			case ELF_AREA_PLUG_CONSUMPTION_UNMANAGED:
				return &sectorPtr->areaPlugUnManagedConsumption;
			case ELF_AREA_PLUG_CONSUMPTION_TOTAL:
				StoreAnalogPresentValue(&sectorPtr->areaPlugTotalConsumption, sectorPtr->areaPlugManagedConsumption.presentValue + sectorPtr->areaPlugUnManagedConsumption.presentValue);
				return &sectorPtr->areaPlugTotalConsumption;
			default:
				panic("illegal type for write %d", ptr->elfDataType);
			}
		}
		break;
			
	case CATEGORY_SWITCH:
		{
			s_switchgroup_t *swPtr = get_switch_ptr(deviceInstance);
			if (swPtr == NULL)
			{
				panic("did not find ptr for %d", deviceInstance);
				return NULL;
			}
			switch (ptr->elfDataType)
			{
			case ELF_SWITCH_DIM:
				return (AnalogObjectTypeDescriptor *) &swPtr->dimLevel ;
			case ELF_SWITCH_SCENE:
				return (AnalogObjectTypeDescriptor *) &swPtr->scene ;
			default:
				panic("illegal type for write %d", ptr->elfDataType);
			}
		}
		break;
		
	case CATEGORY_SWITCH_SCENE_FIXTURE:
		{
			s_scenelightlevel_t *lightLev = get_switch_scene_fixture_ptr(deviceInstance, objectInstance);
			if (lightLev != NULL)
			{
				StoreAnalogPresentValue(&hardcodedAnalogObjectForScenes, (float) lightLev->lightLevel );
				return &hardcodedAnalogObjectForScenes ;
			}
			else
			{
				panic("Null");
			}
		}
		break;
			
	case CATEGORY_SWITCH_SCENE_PLUGLOAD:
		{
			s_pluglevel_t *plugLev = get_switch_scene_plugload_ptr(deviceInstance, objectInstance);
			if (plugLev != NULL)
			{
				StoreAnalogPresentValue(&hardcodedAnalogObjectForScenes, (float) plugLev->plugLevel);
				return &hardcodedAnalogObjectForScenes ;
			}
			else
			{
				panic("Null");
			}
		}
		break;
		
	case CATEGORY_EM:
		{
			s_energy_manager_t *emPtr = get_energy_manager_ptr();
			if (emPtr == NULL)
			{
				panic("did not find ptr");
				return NULL;
			}
			switch (ptr->elfDataType)
			{
			case ELF_EM_ENERGY_LIGHTING:
				return &emPtr->energyLighting ;
			case ELF_EM_ENERGY_PLUGLOAD:
				return &emPtr->energyPlugload;
			case ELF_EM_ADR_LEVEL:
				return (AnalogObjectTypeDescriptor *) &emPtr->adrLevel ;
			default:
				panic("illegal type %d", ptr->elfDataType);
			}
		}
		break;
		
	case CATEGORY_FIXTURE :
		{
			// s_fixture_t2 *sptr = get_area_fixture_ptr(objectInstance);
			s_fixture_t2 *sptr = get_fixture_ptr(GET_FIXTURE_ID_FROM_INSTANCE(objectInstance));
			if (sptr == NULL)
				{
				// there are no fixtures - e.g. Area 0
				return NULL ;
				}
			
			switch (ptr->elfDataType)
			{
			case ELF_FIXTURE_BASE_ENERGY_OBJECT:
				return &sptr->fixture_base_energy;
			case ELF_FIXTURE_CONSUMED_ENERGY_LIGHTING_OBJECT:
				return &sptr->fixture_used_energy;
			case ELF_FIXTURE_SAVED_ENERGY_OBJECT:
				return &sptr->fixture_saved_energy;
			case ELF_FIXTURE_OCC_SAVED_ENERGY_OBJECT:
				return &sptr->occ_saved_energy;
			case ELF_FIXTURE_DAYLIGHT_SAVED_ENERGY_OBJECT:
				return &sptr->amb_saved_energy2;
			case ELF_FIXTURE_TASK_TUNED_ENERGY_OBJECT:
				return &sptr->task_saved_energy;
			case ELF_FIXTURE_MANUAL_SAVED_ENERGY_OBJECT:
				return &sptr->manual_saved_energy;
			case ELF_FIXTURE_AMBIENT_LIGHT:
				return &sptr->ambient_light;
			case ELF_FIXTURE_DIM_LEVEL_OBJECT:
				return &sptr->dim_level2.analogTypeDescriptor;
			default:
				panic("unexpected data type here %d", ptr->elfDataType);
				break;
			}
		}
		break;
			
	case CATEGORY_AREA_PLUGLOAD:
		{
			s_plugload_t *sptr = get_area_plugload_ptr(deviceInstance, objectInstance);
			if (sptr == NULL)
			{
				// there are no fixtures - e.g. Area 0
				return NULL ;
			}
			switch (ptr->elfDataType)
			{
			case ELF_PLUG_CONSUMPTION_MANAGED:
				return &sptr->plugManagedConsumption;
			case ELF_PLUG_CONSUMPTION_UNMANAGED:
				return &sptr->plugUnManagedConsumption;
			case ELF_PLUG_CONSUMPTION_TOTAL:
				return &sptr->plugTotalConsumption;
			default:
				panic("unexpected data type here %d", ptr->elfDataType);
				break;
			}
		}
		break;
			
	default:
		panic("Unknown");
		break;
	}
	return NULL ;
#endif // EM
}


int elf_gen_ana_relinquish(BACNET_OBJECT_TYPE objectType, BACNET_WRITE_PROPERTY_DATA *wp_data)
{
// s_sector_t *sectorPtr ;
	uint32_t deviceInstance = elf_get_current_bacnet_device_instance();
	AnalogCommandableTypeDescriptor *currentObject;

	ts_elf_template_object_t *ptr = find_template_object_record_current_device(objectType, wp_data->object_instance);
	if (ptr == NULL)
	{
		panic("Commandable Analog BACnet Object not found");
		return BACNET_STATUS_ERROR;
	}

	    // Enter the value into the priority array, and evaluate RD, PA to determine PV

	if (wp_data->priority == 0 ||
	    (wp_data->priority > BACNET_MAX_PRIORITY) ||
	    (wp_data->priority == 6 /* reserved */))
	{
		wp_data->error_class = ERROR_CLASS_PROPERTY;
		wp_data->error_code = ERROR_CODE_WRITE_ACCESS_DENIED;
		panic("Illegal priority for a write");
		return BACNET_STATUS_ERROR;
	}

    // ptr points to the 'elf device template' information, we need to pick out our live data still

	currentObject = (AnalogCommandableTypeDescriptor *) elf_get_analog_object_live_data_old(ptr, wp_data->object_instance);

	currentObject->priorityAsserted[wp_data->priority - 1] = false;

	return elf_gen_ana_scan_and_update_pv(objectType, deviceInstance, ptr, currentObject, wp_data);
}


bool elf_gen_ana_put_present_value_commandable(BACNET_OBJECT_TYPE objectType, BACNET_WRITE_PROPERTY_DATA *wp_data, float value)
{
	uint32_t deviceInstance = elf_get_current_bacnet_device_instance();
	AnalogCommandableTypeDescriptor *currentObject;

	if (wp_data->priority == 6)
	{
	    /* Command priority 6 is reserved for use by Minimum On/Off
	       algorithm and may not be used for other purposes in any
	       object. */
		wp_data->error_class = ERROR_CLASS_PROPERTY;
		wp_data->error_code = ERROR_CODE_WRITE_ACCESS_DENIED;
		return false  ;
	}
	
	if (wp_data->priority == 0 ||
	     wp_data->priority > BACNET_MAX_PRIORITY)
	{
		wp_data->error_class = ERROR_CLASS_PROPERTY;
		wp_data->error_code = ERROR_CODE_WRITE_ACCESS_DENIED;
		panic("Illegal priority for a write");
		return false ;
	}

	
	ts_elf_template_object_t *ptr = find_template_object_record_current_device(objectType, wp_data->object_instance);
	if (ptr == NULL)
	{
		wp_data->error_class = ERROR_CLASS_OBJECT;
		wp_data->error_code = ERROR_CODE_NO_OBJECTS_OF_SPECIFIED_TYPE;
		panic("Analog BACnet Object not found");
		return false ;
	}

	if (ptr->readOnly)
	{
		wp_data->error_class = ERROR_CLASS_PROPERTY;
		wp_data->error_code = ERROR_CODE_WRITE_ACCESS_DENIED;
		return false  ;
	}

	currentObject = (AnalogCommandableTypeDescriptor *) elf_get_analog_object_live_data_old(ptr, wp_data->object_instance);

	    // Enter the value into the priority array, and evaluate RD, PA to determine PV

	currentObject->priorityArray[wp_data->priority - 1] = value;
	currentObject->priorityAsserted[wp_data->priority - 1] = true;

	int err = elf_gen_ana_scan_and_update_pv(objectType, deviceInstance, ptr, currentObject, wp_data);
	if (err == BACNET_STATUS_ERROR)
	{
		return false ;
	}
	else
	{
		return true ;
	}
}


void StoreAnalogPresentValue(AnalogObjectTypeDescriptor *anaObject, float newPresentValue)
{
	// todo 5 range checks?
	anaObject->presentValue = newPresentValue;
	anaObject->objectTypeDescriptor.lastUpdate = time(NULL);
}

void StoreAnalogPresentValueConditionalInt(AnalogObjectTypeDescriptor *bacObject, int value)
{
	if (CheckLicenseAndFault(&bacObject->objectTypeDescriptor, value))
	{
		StoreAnalogPresentValue(bacObject, (float) value);
	}
}

