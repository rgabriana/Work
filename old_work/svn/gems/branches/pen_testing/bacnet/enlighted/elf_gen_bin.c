#include <syslog.h>
#include <time.h>
#include "elf_std.h"
#include "elf.h"
#include "elf_gems_api.h"
#include "advdebug.h"
#include "elf_objects.h"
#include "elf_functions.h"

BACNET_BINARY_PV elf_gen_bin_get_present_value(BACNET_OBJECT_TYPE objectType, uint32_t object_instance)
{
	bool trv = false;
	uint32_t id = elf_get_current_bacnet_device_instance();
	ts_elf_template_object_t *ptr = find_template_object_record_current_device(objectType, object_instance);

	if (ptr == NULL)
	{
		return BINARY_INACTIVE ;
	}

#ifdef UEM
	s_sector_t *zptr = get_sector_ptr_by_instance(id);
	if (!zptr)
	{
		return BINARY_INACTIVE;
	}

	switch (ptr->elfDataType)
	{
	default:
		panic("No default");
	}
	// no longer    free(zptr);
	return BINARY_INACTIVE;
#endif

#ifdef EM
	s_sector_t *zptr;

	switch (ptr->elfCategory)
	{
	case CATEGORY_AREA:
		zptr = get_sector_ptr_by_instance(id);
		if (!zptr)
		{
			panic("Could not get sector ptr");
			return BINARY_INACTIVE ;
		}
		switch (ptr->elfDataType)
		{
		case ELF_AREA_EMERGENCY:
			trv = zptr->areaEmergency.binaryTypeDescriptor.presentValue;
			break;
		case ELF_AREA_OCCUPANCY:
			trv = zptr->occupancy.presentValue;
			break;
		default:
			panic("Unknown data type %d", ptr->elfDataType);
		}
		break;
		
	case CATEGORY_FIXTURE:
		{
		    // this is a Fixture point, not a base point
			s_fixture_t2 *sptr = get_fixture_ptr(GET_FIXTURE_ID_FROM_INSTANCE(object_instance));
			if (!sptr)
			{
				panic("Could not get fixture ptr");
				return BINARY_INACTIVE ;
			}
			switch (ptr->elfDataType)
			{
			case ELF_FIXTURE_OCCUPANCY:
				trv = sptr->occupancy.presentValue ;
				break;
			case ELF_FIXTURE_OUTAGE:
				trv = sptr->fixtureOutage.presentValue;
				break;
			default:
				panic("unexpected data type here %d", ptr->elfDataType);
				break;
			}
		}
		break ;

	case CATEGORY_AREA_PLUGLOAD:
		{
		    // this is a Fixture point, not a base point
			s_plugload_t *sptr = get_area_plugload_ptr(id, object_instance);
			if (!sptr)
			{
				panic("Could not get plugload ptr");
				return BINARY_INACTIVE ;
			}
			switch (ptr->elfDataType)
			{
			case ELF_PLUG_STATUS:
				trv = sptr->plugLoadStatus.binaryTypeDescriptor.presentValue;
				break;
			default:
				panic("unexpected data type here %d", ptr->elfDataType);
				break;
			}
		}
		break ;

	case CATEGORY_SWITCH:
		{
			panic("Switches do not have binaries");
		}
		break;
		
	case CATEGORY_EM:
		{
			s_energy_manager_t *eptr = get_energy_manager_ptr();
			if (!eptr)
			{
				panic("Could not get energy manager ptr");
				return BINARY_INACTIVE ;
			}
			switch (ptr->elfDataType)
			{
			case ELF_EM_EMERGENCY:
				trv = eptr->emergencyStatus.binaryTypeDescriptor.presentValue;
				break;
			default:
				panic("Unexpected data type here %d", ptr->elfDataType);
				break;
			}
		}
		break;
		
	default:
		panic("No such device type");
		break;
	}
	
	return trv ? BINARY_ACTIVE : BINARY_INACTIVE ;
	
#endif // EM

}


// Saves data locally for possible readback scenarios
static void elf_gen_bin_put_present_value_local(BACNET_OBJECT_TYPE objectType, uint32_t object_instance, BACNET_BINARY_PV value)
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
			s_sector_t *secPtr = get_sector_ptr_by_instance(deviceId);
			if (secPtr == NULL)
			{
				panic("Pointer not set");
				return ;
			}
			switch (ptr->elfDataType)
			{
			case ELF_AREA_EMERGENCY:
				StoreBinaryPresentValue(&secPtr->areaEmergency.binaryTypeDescriptor, value);
				break;
			default:
				panic("Not appropriate for write");
				break;
			}
		}
		break;
		
	case CATEGORY_FIXTURE:
		panic("Not appropriate");
		break;
		
	case CATEGORY_AREA_PLUGLOAD:
		{
		// this is a Fixture point, not a base point
			s_plugload_t *sptr = get_area_plugload_ptr(deviceId, object_instance);
			if (!sptr)
			{
				panic("Not found");
				return;
			}
			switch (ptr->elfDataType)
			{
			case ELF_PLUG_STATUS:
				StoreBinaryPresentValuePV(&sptr->plugLoadStatus.binaryTypeDescriptor, value);
				break;
			default:
				panic("Not appropriate for write");
				break;
			}
		}
		break;
		
	case CATEGORY_EM:
		{
			s_energy_manager_t *em = get_energy_manager_ptr();
			if (!em)
			{
				panic("Not found");
				return;
			}
			switch (ptr->elfDataType)
			{
			case ELF_EM_EMERGENCY:
				// todo 5 - the interaction here is going to play havoc with BTL, watch out.
				StoreBinaryPresentValue(&em->emergencyStatus.binaryTypeDescriptor, value);
				break;
			default:
				panic("Not appropriate");
				break;
			}
		}
		break;
		
	case CATEGORY_SWITCH:
		panic("Not appropriate");
		break;
		
	default:
		panic("Unknown");
	}
#endif // EM
}


static int elf_gen_bin_scan_and_update_pv(
	BACNET_OBJECT_TYPE bacnetObjectType, 
	ts_elf_template_object_t *eoPtr, 
	BinaryCommandableTypeDescriptor *bacnetObject, 
	BACNET_WRITE_PROPERTY_DATA *wp_data)
{
	BACNET_BINARY_PV value = (bacnetObject->relinquishDefault) ? BINARY_ACTIVE : BINARY_INACTIVE;
	int rc = BACNET_STATUS_ERROR;
	int i;

	for (i = 0; i < BACNET_MAX_PRIORITY; i++)
	{
		if (bacnetObject->priorityAsserted[i])
		{
			value = bacnetObject->priorityArray[i] ? BINARY_ACTIVE : BINARY_INACTIVE;
			break;
		}
	}

	log_printf(LOG_INFO, "Writing %d to object instance d for for device d", value);

#ifdef EM
	switch (eoPtr->elfCategory)
	{
	case CATEGORY_AREA:
		switch (eoPtr->elfDataType)
		{
		case ELF_AREA_EMERGENCY:
			rc = set_area_emergency(wp_data, (unsigned int)value);
			break;
		default:
			panic("why?");
			break;
		}
		break;
		
	case CATEGORY_FIXTURE:
		panic("Not appropriate");
		break;

	case CATEGORY_AREA_PLUGLOAD:
		switch (eoPtr->elfDataType)
		{
		case ELF_PLUG_STATUS:
			rc = set_plugload_state(wp_data, (unsigned int)value);
			break;
		default:
			panic("why?");
			break;
		}
		break;

	case CATEGORY_EM:
		switch (eoPtr->elfDataType)
		{
		case ELF_EM_EMERGENCY:
			// only ever a single object per EM, and objectInstance never used in REST command anyway
			rc = set_energy_manager_emergency(wp_data, (unsigned int)value);
			break;
		default:
			panic("why?");
			break;
		}
		break;
		
	case CATEGORY_SWITCH:
		panic("Not appropriate");
		break;
		
	default:
		panic("Illegal");
	}

	if (rc == BACNET_STATUS_OK)
	{
	    // store a 'local copy' of the value in our memory tables for possible readback scenarios
		elf_gen_bin_put_present_value_local(bacnetObjectType, wp_data->object_instance, value);
	}

#else
	panic("todo");
	// rc = set_elf_value(deviceInstance, eoPtr->elfd, value);
#endif

	return rc;
}


BinaryObjectTypeDescriptor* elf_get_binary_object_live_data_old(const ts_elf_template_object_t *ptr, const uint32_t objectInstance)
{
	uint32_t deviceInstance = elf_get_current_bacnet_device_instance();
	return elf_get_binary_object_live_data(deviceInstance, ptr, objectInstance) ;
}


BinaryObjectTypeDescriptor* elf_get_binary_object_live_data(const uint32_t deviceInstance, const ts_elf_template_object_t *ptr, const uint32_t objectInstance)
{
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
			case ELF_AREA_EMERGENCY:
				return (BinaryObjectTypeDescriptor *) &sectorPtr->areaEmergency;
			case ELF_AREA_OCCUPANCY:
				return &sectorPtr->occupancy ;
			default:
				panic("illegal type for write %d", ptr->elfDataType);
			}
		}
		break ;
				
	case CATEGORY_SWITCH:
		{
			s_switchgroup_t *swPtr = get_switch_ptr(deviceInstance);
			if (swPtr == NULL)
			{
				panic("did not find ptr");
				return NULL;
			}
			switch (ptr->elfDataType)
			{
			default:
				panic("illegal type for write %d", ptr->elfDataType);
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
			case ELF_EM_EMERGENCY:
				return (BinaryObjectTypeDescriptor *) &emPtr->emergencyStatus ;
			default:
				panic("illegal type %d", ptr->elfDataType);
			}
		}
		break;
			
	case CATEGORY_FIXTURE:
		{
			s_fixture_t2 *sptr = get_fixture_ptr(GET_FIXTURE_ID_FROM_INSTANCE(objectInstance));
			if (sptr == NULL)
			{
			// there are no fixtures - e.g. Area 0
				return NULL ;
			}
			
			switch (ptr->elfDataType)
			{
			case ELF_FIXTURE_OUTAGE:
				return &sptr->fixtureOutage;
			case ELF_FIXTURE_OCCUPANCY:
				return &sptr->occupancy;
			default:
				panic("unexpected data type here %d", ptr->elfDataType);
				break;
			}
		}
		break ;
		
	case CATEGORY_AREA_PLUGLOAD:
		{
		    // this is a Fixture point, not a base point
			// We have to drag in an objectInstance for the full constructed OI, not able to just use the template's OI
			s_plugload_t *sptr = get_area_plugload_ptr(deviceInstance, objectInstance);
			if (sptr == NULL)
			{
				panic("Pointer not set");
				break;
			}
			switch (ptr->elfDataType)
			{
			case ELF_PLUG_STATUS :
				return (BinaryObjectTypeDescriptor *) &sptr->plugLoadStatus ;
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
	
#endif // EM
	return NULL ;
}


int elf_gen_bin_relinquish(BACNET_OBJECT_TYPE objectType, BACNET_WRITE_PROPERTY_DATA *wp_data)
{
// s_sector_t *sectorPtr ;
	BinaryCommandableTypeDescriptor  *currentObject;

	ts_elf_template_object_t *ptr = find_template_object_record_current_device(objectType, wp_data->object_instance);
	if (ptr == NULL)
	{
		panic("Commandable Binary BACnet Object not found");
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

	currentObject = (BinaryCommandableTypeDescriptor *)elf_get_binary_object_live_data_old(ptr, wp_data->object_instance);

	currentObject->priorityAsserted[wp_data->priority - 1] = false;

	return elf_gen_bin_scan_and_update_pv(objectType, ptr, currentObject, wp_data);
}


bool elf_gen_bin_put_present_value_commandable(BACNET_OBJECT_TYPE objectType, BACNET_WRITE_PROPERTY_DATA *wp_data, BACNET_BINARY_PV value)
{
	BinaryCommandableTypeDescriptor *currentObject;

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
		panic("Binary BACnet Object not found");
		return false ;
	}

	if (ptr->readOnly)
	{
		wp_data->error_class = ERROR_CLASS_PROPERTY;
		wp_data->error_code = ERROR_CODE_WRITE_ACCESS_DENIED;
		return false  ;
	}

	currentObject = (BinaryCommandableTypeDescriptor *)elf_get_binary_object_live_data_old(ptr, wp_data->object_instance);

	    // Enter the value into the priority array, and evaluate RD, PA to determine PV

	currentObject->priorityArray[wp_data->priority - 1] = value;
	currentObject->priorityAsserted[wp_data->priority - 1] = true;

	int err = elf_gen_bin_scan_and_update_pv(objectType, ptr, currentObject, wp_data);
	if (err == BACNET_STATUS_ERROR)
	{
		return false ;
	}
	else
	{
		return true ;
	}
}


#ifdef EM
void StoreBinaryPresentValuePV(BinaryObjectTypeDescriptor *binObject, BACNET_BINARY_PV newPresentValue)
{
	// todo 5 range checks?
	binObject->presentValue = newPresentValue;
	binObject->objectTypeDescriptor.lastUpdate = time(NULL);
}


void StoreBinaryPresentValue(BinaryObjectTypeDescriptor *binObject, bool newPresentValue)
{
	StoreBinaryPresentValuePV(binObject, (newPresentValue) ? BINARY_ACTIVE : BINARY_INACTIVE);
}


void StoreBinaryPresentValueConditionalInt(BinaryObjectTypeDescriptor *binObject, int value)
{
	if (CheckLicenseAndFault(&binObject->objectTypeDescriptor, value))
	{
		StoreBinaryPresentValuePV(binObject, (value > 0) ? BINARY_ACTIVE : BINARY_INACTIVE);
	}
}
#endif // EM

bool CheckLicenseAndFault(ObjectTypeDescriptor *bacnetObject, int value)
{
	if (value == -2)
	{
		// unlicensed
		bacnetObject->reliability = RELIABILITY_CONFIGURATION_ERROR;
		return false ;
	}
	else if (value == -1)
	{
		// fault 
		bacnetObject->reliability = RELIABILITY_NO_SENSOR;
		return false ;
	}
	return true ;
}


