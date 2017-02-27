//#include "elf_std.h"
//#include "elf.h"
//#include "elf_gems_api.h"
//#include "advdebug.h"
//#include "elf_functions.h"
//
//// todo 2 this is obsolete, consolidate with elf_gen_bin.c
//BACNET_BINARY_PV elf_get_bi_present_value(uint32_t object_instance)
//{
	//BACNET_BINARY_PV returnVal = BINARY_INACTIVE;
//
	//ts_elf_template_object_t *ptr = find_template_object_record_current_device(OBJECT_BINARY_INPUT, object_instance);
	//if (ptr == NULL)
	//{
		//return returnVal ;
	//}
//
//#ifdef EM
	//uint32_t id = elf_get_current_bacnet_device_instance();
	//if (ptr->elfCategory == CATEGORY_AREA)
	//{
		//s_sector_t *zptr = get_sector_ptr_by_instance(id);
		//if (!zptr)
		//{
			//return returnVal ;
		//}
		//switch (ptr->elfDataType)
		//{
		//case ELF_AREA_OCCUPANCY:
			//returnVal = (zptr->occupancy.presentValue) ? BINARY_ACTIVE : BINARY_INACTIVE;
			//break;
		//case ELF_AREA_EMERGENCY:
			//returnVal = zptr->areaEmergency.binaryTypeDescriptor.presentValue ? BINARY_ACTIVE : BINARY_INACTIVE;
			//break;
		//default:
			//panic("Unknown data type");
			//break;
		//}
		////         free(zptr);
	//}
	//else if (ptr->elfCategory == CATEGORY_FIXTURE)
	//{
	    //// this is a Fixture point, not a base point
		//s_fixture_t2 *sptr = get_area_fixture_ptr(object_instance);
		//if (!sptr)
		//{
			//return returnVal ;
		//}
		//switch (ptr->elfDataType)
		//{
		//case ELF_FIXTURE_OCCUPANCY:
			//returnVal = (sptr->occupancy.presentValue) ? BINARY_ACTIVE : BINARY_INACTIVE;
			//break;
		//case ELF_FIXTURE_OUTAGE:
			//returnVal = sptr->fixtureOutage.presentValue ? BINARY_ACTIVE : BINARY_INACTIVE;
			//break;
		//default:
			//panic("unexpected data type here");
			//break;
		//}
	//}
	//else if (ptr->elfCategory == CATEGORY_AREA_PLUGLOAD)
	//{
	    //// this is a Fixture point, not a base point
		//s_plugload_t *sptr = get_area_plugload_ptr(id, object_instance);
		//if (!sptr)
		//{
			//return returnVal ;
		//}
		//switch (ptr->elfDataType)
		//{
		//case ELF_PLUG_STATUS:
			//returnVal = sptr->plugLoadStatus.binaryTypeDescriptor.presentValue ? BINARY_ACTIVE : BINARY_INACTIVE;
			//break;
		//default:
			//panic("unexpected data type here");
			//break;
		//}
	//}
	//else if (ptr->elfCategory == CATEGORY_EM)
	//{
		//if (GET_SUBCAT_FROM_INSTANCE(object_instance) == SUBCAT_FIXTURE)
		//{
			//// todo 1
		//}
		//else
		//{
			//s_energy_manager_t *emPtr = get_energy_manager_ptr();
			//switch (ptr->elfDataType)
			//{
			//case ELF_EM_EMERGENCY:
				//returnVal = emPtr->emergencyStatus.binaryTypeDescriptor.presentValue ? BINARY_ACTIVE : BINARY_INACTIVE;
				//break;
			//default :
				//panic("unknown %d", ptr->elfDataType);
				//break;
			//}
		//}
	//}
	//else
	//{
		//panic("No such device type");
	//}
//
//#endif // EM
//
	//return returnVal;
//}

