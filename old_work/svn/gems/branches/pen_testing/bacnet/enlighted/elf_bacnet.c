#include <linux/stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <openssl/sha.h>
#include <syslog.h>
#include <stdbool.h>

#include "elf_config.h"

#include "config.h"
#include "address.h"
#include "bacdef.h"
#include "handlers.h"
#include "client.h"
#include "bacdcode.h"
#include "npdu.h"
#include "apdu.h"
#include "iam.h"
#include "tsm.h"
#include "device.h"
#include "datalink.h"
#include "dcc.h"
#include "txbuf.h"
#include "debug.h"
#include "advdebug.h"
#include "bip.h"

/* include the device object */
#include "device.h"
#ifdef BACNET_TEST_VMAC
#include "vmac.h"
#endif
#include "elf.h"
#include "elf_gems_api.h"
#include "elf_functions.h"

extern elf_bacnet_db_t *g_bacnet_device_list;
extern ts_elf_template_object_t *elf_template_objs;
extern uint16_t iCurrent_Device_Idx;
uint32_t g_first_sector_device_instance;


uint32_t g_first_em_device_instance;

#ifdef EM
extern elf_config_t elf_bacnet_config;
extern s_energy_manager_t *g_energy_manager;
uint32_t g_first_switch_device_instance2;
#endif

uint8_t elf_mac_address[6];

uint32_t elf_get_current_bacnet_device_instance(void)
{
	return Routed_Device_Object_Instance_Number();
}


#ifdef EM
static uint get_switchgroup_object_count(uint32_t bacnetDeviceInstance, BACNET_OBJECT_TYPE objectType)
{
	uint baseCount = elf_get_template_object_count_per_category(CATEGORY_SWITCH, objectType);
	
	if (objectType == OBJECT_ANALOG_INPUT)
	{
		// need to add the scenes, (plugloads and fixture counts)
		s_switchgroup_t *sg = get_switch_ptr(bacnetDeviceInstance);
		
		uint sc;
		for (sc = 0; sc < sg->num_scenes; sc++)
		{
			baseCount += sg->s_scenes[sc].num_lightlevels;
			baseCount += sg->s_scenes[sc].num_pluglevels;
		}
	}
	return baseCount ;
}
#endif // EM


#ifdef EM
static uint get_switchgroup_object_count_for_current_device(BACNET_OBJECT_TYPE objectType)
{
	return get_switchgroup_object_count(elf_get_current_bacnet_device_instance(), objectType) ;

	// todo 3 remember to increment object database version when adding new objects.
}
#endif // EM


// get the count of object_type for the current device...
uint elf_get_object_count_for_current_device(BACNET_OBJECT_TYPE object_type)
{
	switch (g_bacnet_device_list[iCurrent_Device_Idx].elf_dev_type)
	{
	case ELF_DEVICE_EM:
#ifdef EM
		{
			uint count = elf_get_template_object_count_per_category(CATEGORY_EM, object_type);
		
			if (elf_bacnet_config.fixtureAmbient)
			{
				count += g_energy_manager->num_fixtures * elf_get_template_object_count_per_category(CATEGORY_FIXTURE, object_type);
			}
			return count ;
		}
#else
		return 0 ;
#endif // EM/UEM
		
#ifdef EM	
	case ELF_DEVICE_SWITCH:
		return get_switchgroup_object_count_for_current_device(object_type) ;
#endif

	case ELF_DEVICE_AREA:
		{
#ifdef EM	
			unsigned int num_fixtures = elf_get_number_of_fixtures_in_area(elf_get_current_bacnet_device_instance());
			unsigned int num_plugloads = elf_get_number_of_plugloads_in_area(elf_get_current_bacnet_device_instance());
			return elf_get_template_object_count_per_category(CATEGORY_AREA, object_type) +
				num_fixtures * elf_get_template_object_count_per_category(CATEGORY_FIXTURE, object_type) + 
				num_plugloads * elf_get_template_object_count_per_category(CATEGORY_AREA_PLUGLOAD, object_type)  ;
#else
			uint16_t count = 0;
			unsigned int i;

			for (i = 0; i < ux_template_objs; i++)
			{
				if (elf_template_objs[i].bacnet_obj_type == object_type)
				{
					++count;
				}
			}
			return count;
#endif // EM / UEM			
		}
	default:
		panic("why?");
	}
	return 0 ;
}

#ifdef EM
static unsigned int get_category_count_for_deviceInstance(const uint32_t bacnetDeviceInstance, const e_elf_category_t category)
{
	unsigned int i, j;
	
	elf_bacnet_db_t *ptr = (elf_bacnet_db_t *) elf_get_bacnet_db_ptr_specific_device(bacnetDeviceInstance);
	if (!ptr)
	{
		panic("fail");
		return 0 ;
	}
	
	switch (ptr->elf_dev_type)
	{
	case ELF_DEVICE_AREA:
		for (i = 0; i < g_energy_manager->num_floors; i++)
		{
			if (ptr->floor_id == g_energy_manager->floors[i].id)
			{
				for (j = 0; j < g_energy_manager->floors[i].num_sectors; j++)
				{
					if (ptr->sector_id == g_energy_manager->floors[i].s_sector[j].id)
					{
						switch (category)
						{
						case CATEGORY_FIXTURE:
							return g_energy_manager->floors[i].s_sector[j].num_fixtures_in_area;
						case CATEGORY_AREA_PLUGLOAD:
							return g_energy_manager->floors[i].s_sector[j].num_plugloads;
						default:
							panic("Not applicable");
							break;
						}
					}
				}
			}
		}
		panic("Fail");
		break;
		
	case ELF_DEVICE_SWITCH:
		for (i = 0; i < g_energy_manager->num_floors; i++)
		{
			if (ptr->floor_id == g_energy_manager->floors[i].id)
			{
				for (j = 0; j < g_energy_manager->floors[i].num_switches; j++)
				{
					if (ptr->switch_id == g_energy_manager->floors[i].s_switch[j].id)
					{
						if (g_energy_manager->floors[i].s_switch[j].num_scenes == 0) return 0 ;
						switch (category)
						{
						case CATEGORY_SWITCH_SCENE_FIXTURE:
							return g_energy_manager->floors[i].s_switch[j].s_scenes[0].num_lightlevels * g_energy_manager->floors[i].s_switch[j].num_scenes ;
						case CATEGORY_SWITCH_SCENE_PLUGLOAD:
							return g_energy_manager->floors[i].s_switch[j].s_scenes[0].num_pluglevels * g_energy_manager->floors[i].s_switch[j].num_scenes ;
						default:
							panic("Not applicable");
							break;
						}
					}
				}
			}
		}
		panic("fail");
		break;
		
	default:
		panic("Fail");
		break;
	}
	panic("Fail");
	return 0 ;
}
#endif // EM


#ifdef EM
static uint32_t resolve_area_subObjectId(uint obj_count, uint ux_template_objs, uint objectIndex, uint32_t bacnetDeviceInstance, BACNET_OBJECT_TYPE object_type)
{
	uint i;
	
	if (!elf_bacnet_config.detailedMode)
	{
		panic("Should never get here in normal mode");
		return 0; 
	}

	// now that the 'base' amount has been subtracted from obj_count, look at the Fixture or Plugload specific categories
	s_sector_t *sptr = get_sector_ptr_by_instance(bacnetDeviceInstance);
	if (sptr == NULL)
	{
		panic("what?");
		return 0;
	}
	
	// For an _area_, the next  N indicies are objects for all  fixtures, where N = x * num_fixtures,  where x = num of bacnet type for fixture
	// the following			M indicies are objects for all plugloads, where M = y * num_plugload,  where y = num of bacnet type for plugload
	
	unsigned int nofo = elf_get_template_object_count_per_category(CATEGORY_FIXTURE, object_type);
	if (nofo != 0)
	{
		unsigned int nof = get_category_count_for_deviceInstance(bacnetDeviceInstance, CATEGORY_FIXTURE);
		unsigned int n = nofo * nof;
	
		if (objectIndex - obj_count < n)
		{
			unsigned int thisFixture = (objectIndex - obj_count) / nofo;
			obj_count += thisFixture * nofo;
		
			for (i = 0; i < ux_template_objs; i++)
			{
				if (elf_template_objs[i].elfCategory == CATEGORY_FIXTURE)
				{
					if (elf_template_objs[i].bacnet_obj_type == object_type)
					{
						if (objectIndex == obj_count)
						{
							s_fixture_t2 *fixture = get_nth_fixture_ptr_for_area(sptr->id, thisFixture );
							if (fixture == NULL )
							{
								panic("missing fixture for area");
								return 0 ;
							}
							return SET_FIXTURE_INSTANCE( fixture->fixtureId, elf_template_objs[i].objectInstance) ;
						}
						obj_count++;
					}
				}
			}
			panic("why? %d %d", objectIndex, obj_count);
		}
	
		obj_count += n;
	}
	
	unsigned int nopo = elf_get_template_object_count_per_category(CATEGORY_AREA_PLUGLOAD, object_type);
	if (nopo != 0)
	{
		unsigned int nop =	get_category_count_for_deviceInstance(bacnetDeviceInstance, CATEGORY_AREA_PLUGLOAD);
		unsigned m = nopo * nop;
		

		if (objectIndex - obj_count < m)
		{
			unsigned int thisPlugload = (objectIndex - obj_count) / nopo;
			obj_count += thisPlugload * nopo;
		
			for (i = 0; i < ux_template_objs; i++)
			{
				if (elf_template_objs[i].elfCategory == CATEGORY_AREA_PLUGLOAD)
				{
					if (elf_template_objs[i].bacnet_obj_type == object_type)
					{
						if (objectIndex == obj_count)
						{
							return SET_PLUGLOAD_INSTANCE(sptr->plugLoads[thisPlugload].id, elf_template_objs[i].objectInstance) ;
						}
						obj_count++;
					}
				}
			}
			panic("why?");
		}
	}

	panic("Index not found, index=%d, object_type=%d, obj_count=%d\n",
		objectIndex,
		object_type,
		obj_count);

	return 0 ;
}
#endif // EM


#ifdef EM
// todo 3 ux_template_objs is a global anyway. rename, remove where used..
static uint32_t resolve_em_index_to_objectId(uint obj_count, uint ux_template_objs, uint objectIndex, uint32_t bacnetDeviceInstance, BACNET_OBJECT_TYPE object_type)
{
	uint i;
	
	// note that the 'base' amount has already been subtracted from obj_count, look at the Fixture list next
	
	// For an _area_, the next  N indicies are objects for all  fixtures, where N = x * num_fixtures,  where x = num of bacnet type for fixture
	// the following			M indicies are objects for all plugloads, where M = y * num_plugload,  where y = num of bacnet type for plugload
	
	unsigned int nofo = elf_get_template_object_count_per_category(CATEGORY_FIXTURE, object_type);
	if (nofo != 0)
	{
		// unsigned int nof = get_category_count_for_deviceInstance(bacnetDeviceInstance, CATEGORY_AREA_FIXTURE);
		unsigned int nof = g_energy_manager->num_fixtures;
		
		unsigned int n = nofo * nof;
	
		if (objectIndex - obj_count < n)
		{
			unsigned int thisFixture = (objectIndex - obj_count) / nofo;
			obj_count += thisFixture * nofo;
		
			for (i = 0; i < ux_template_objs; i++)
			{
				if (elf_template_objs[i].elfCategory == CATEGORY_FIXTURE)
				{
					if (elf_template_objs[i].bacnet_obj_type == object_type)
					{
						if (objectIndex == obj_count)
						{
							s_fixture_t2 *fixture = &g_energy_manager->fixtures[thisFixture] ;
							return SET_FIXTURE_INSTANCE(fixture->fixtureId, elf_template_objs[i].objectInstance) ;
						}
						obj_count++;
					}
				}
			}
			panic("why? %d %d", objectIndex, obj_count);
		}
		obj_count += n;
	}
	
	panic("Index not found, index=%d, object_type=%d, obj_count=%d\n",
		objectIndex,
		object_type,
		obj_count);

	return 0 ;
}
#endif // EM


#ifdef EM
static uint32_t resolve_switchgroup_objectId(uint obj_count, uint ux_template_objs, uint objectIndex, uint32_t bacnetDeviceInstance, BACNET_OBJECT_TYPE object_type)
{
	// once the base switch instances not resolved, then remaining objects represent scene fixture dim settings, or scene plugload settings
	if (object_type != OBJECT_ANALOG_INPUT)
	{
		panic("Not allowed");
		return 0 ;
	}
	
	s_switchgroup_t *switchgroup = get_switch_ptr(bacnetDeviceInstance);
	if (switchgroup == NULL)
	{
		panic("why?");
		return 0 ;
	}
	
	// 10000 + are fixture dim levels, 
	// 20000 + are plugload levels
	// the problem is.... we will be inserting plugloads and fixtures as we discover them, need to 'space' the index/count....
	uint sc; 
	for (sc = 0; sc < switchgroup->num_scenes; sc++)
	{
		if (objectIndex - obj_count < switchgroup->s_scenes[sc].num_lightlevels)
		{
			// todo 3 - our OID mapping oly allows for 999 fixture IDs, (and plugloads below). Revisit.
			// return switchgroup->s_scenes[sc].sceneId * OID_OFFSET_SCENE + 1 * OID_OFFSET_SGPL_TYPE + switchgroup->s_scenes[sc].s_lightlevels[(objectIndex - obj_count)].fixtureId ;
			return SET_SCENE_FIXTURE_INSTANCE(switchgroup->s_scenes[sc].sceneId, switchgroup->s_scenes[sc].s_lightlevels[(objectIndex - obj_count)].fixtureId) ;
		}
		obj_count += switchgroup->s_scenes[sc].num_lightlevels;

		if (objectIndex - obj_count < switchgroup->s_scenes[sc].num_pluglevels)
		{
			// todo 3 - our OID mapping oly allows for 999 fixture IDs, (and plugloads below). Revisit.
			// return switchgroup->s_scenes[sc].sceneId * OID_OFFSET_SCENE + 2 * OID_OFFSET_SGPL_TYPE + switchgroup->s_scenes[sc].s_pluglevels[(objectIndex - obj_count)].plugloadId ;
			return SET_SCENE_PLUGLOAD_INSTANCE(switchgroup->s_scenes[sc].sceneId, switchgroup->s_scenes[sc].s_pluglevels[(objectIndex - obj_count)].plugloadId) ;
		}
		obj_count += switchgroup->s_scenes[sc].num_pluglevels;
	}
	panic("illegal index");
	return 0 ;
}
#endif

uint32_t elf_index_to_object_instance_new(const uint32_t bacnetDeviceInstance, uint objectIndex, BACNET_OBJECT_TYPE object_type)
{
	unsigned int i;
	unsigned int obj_count = 0;
	ts_elf_template_object_t *optr = (ts_elf_template_object_t *) elf_template_objs;
	elf_bacnet_db_t *dbptr = elf_get_bacnet_db_ptr_specific_device(bacnetDeviceInstance);
	if (dbptr == NULL)
	{
		panic("Null");
		return 0 ;
	}
	
	e_elf_category_t category;
	switch (dbptr->elf_dev_type)
	{
	case ELF_DEVICE_EM:
		category = CATEGORY_EM;
		break;
	case ELF_DEVICE_AREA:
		category = CATEGORY_AREA;
		break;
#ifdef EM		
	case ELF_DEVICE_SWITCH:
		category = CATEGORY_SWITCH;
		break;
#endif		
	default:
		panic("Illegal");
		return 0;
	}
	
	// first look in the sector part of the Enlighted object template
	for (i = 0; i < ux_template_objs; i++, optr++)
	{
		if (optr->elfCategory == category)
		{
			if (optr->bacnet_obj_type == object_type)
			{
				if (objectIndex == obj_count)
				{
					return optr->objectInstance;
				}
				obj_count++;
			}
		}
	}
	
#ifdef UEM
	// if we get here on a uem it is a fail. (UEMs dont have fixtures, plugloads, switchgroups)
	panic("Illegal Index");
	return 0 ;
#endif
		
	
#ifdef EM	
	switch (dbptr->elf_dev_type)
	{
	case ELF_DEVICE_EM :
		if (elf_bacnet_config.fixtureAmbient)
		{
			return resolve_em_index_to_objectId(obj_count, ux_template_objs, objectIndex, bacnetDeviceInstance, object_type) ;
		}
		// this is a fail
		panic("Illegal index %d", objectIndex);
		return 0 ;
		
	case ELF_DEVICE_AREA :
		return resolve_area_subObjectId(obj_count, ux_template_objs, objectIndex, bacnetDeviceInstance, object_type) ;

	case ELF_DEVICE_SWITCH :
		return resolve_switchgroup_objectId(obj_count, ux_template_objs, objectIndex, bacnetDeviceInstance, object_type) ;
		
	default :
		break ;
	}
	
	panic("Illegal index %d", objectIndex);
	return 0 ;
#endif
}


void elf_index_to_object_instance(uint objectIndex, BACNET_OBJECT_TYPE object_type, uint32_t *object_instance)
{
	*object_instance = elf_index_to_object_instance_new(g_bacnet_device_list[iCurrent_Device_Idx].bacnetDeviceInfo.bacObj.Object_Instance_Number, objectIndex, object_type);
}


void elf_initialize_device_addresses(elf_bacnet_db_t *ptr, int networkNumber)
{
	// unsigned int i;
	DEVICE_OBJECT_DATA *pDev = &ptr->bacnetDeviceInfo;
	// char nameText[MAX_DEV_NAME_LEN];

	struct in_addr *netPtr; /* Lets us cast to this type */
	
	// set up the gateway device, must be first... todo 4 i believe we can remove all this
	// todo 3 - set up nocomms startup, and empty database, and check at least routing device gets created.
	// 	Routing_Device_Init(0);
	
		/* Initialize all devices */
		// elf_bacnet_db_t *ptr = (elf_bacnet_db_t *) &g_bacnet_device_list[0];
	
			//for (i = 0; i < g_bacnet_device_count; i++)
			//{
				// todo 3 - i believe this has been removed, but review
				//pDev = Get_Routed_Device_Object(i);
				//if (pDev == NULL)
				//{
					//panic("why?");
					//break;
			//}

	netPtr = (struct in_addr *) &pDev->bacDevAddr.mac[2];

	uint8_t mac[6];
	memset(mac, 0, sizeof(mac));
	get_mac_address(pDev->bacObj.Object_Instance_Number, mac);
	pDev->bacDevAddr.mac[0] = mac[0];
	pDev->bacDevAddr.mac[1] = mac[1];
	pDev->bacDevAddr.mac[2] = mac[2];
	pDev->bacDevAddr.mac[3] = mac[3];
	pDev->bacDevAddr.mac[4] = mac[4];
	pDev->bacDevAddr.mac[5] = mac[5];

	pDev->bacDevAddr.mac_len = 6;
	pDev->bacDevAddr.net = (uint16_t) networkNumber;
	memcpy(&pDev->bacDevAddr.adr[0], &pDev->bacDevAddr.mac[0], 6);
	pDev->bacDevAddr.len = 6;

			// moved to place after routed device has been instantiated
			// elf_get_device_object_name(ptr->elf_dev_type, nameText, MAX_DEV_NAME_LEN);
			//
					//Routed_Device_Set_Object_Name(CHARACTER_UTF8, nameText, strlen(nameText));

	log_printf(
	        LOG_INFO,
		"Routed device ID %u %02x:%02x:%02x:%02x:%02x:%02x at %s",
		pDev->bacObj.Object_Instance_Number,
		pDev->bacDevAddr.mac[0],
		pDev->bacDevAddr.mac[1],
		pDev->bacDevAddr.mac[2],
		pDev->bacDevAddr.mac[3],
		pDev->bacDevAddr.mac[4],
		pDev->bacDevAddr.mac[5],
		inet_ntoa(*netPtr));

				// todo - send this higher up the call chain, once the in-memory routed device has been established.
				//if (send_i_am)
				//{
				    ///* broadcast an I-Am for each routed Device now */
				    //// We don't really care if some packets are dropped, just want to get something out there
				    //// to show we are alive, more than anything else. A true BACnet client will probe
				    //// conscientiously to find 'missing' devices.
					//Send_I_Am(&Handler_Transmit_Buffer[0]);
			//}
		
			//#ifdef EM	    
					//++ptr;
					//#endif // EM		
						//}
}

void elf_device_init(DEVICE_OBJECT_DATA *ptr)
{
	char nameText[MAX_DEV_NAME_LEN];
	char descText[MAX_DEV_DESC_LEN];

	BACNET_CHARACTER_STRING nameTextBCS;
    
#ifdef EM	
	snprintf(nameText, MAX_DEV_NAME_LEN, "%s %d", "Area", ptr->bacObj.Object_Instance_Number);
#else	snprintf(nameText, MAX_DEV_NAME_LEN, "%s %d", "Zone", ptr->bacObj.Object_Instance_Number);
#endif // EM/UEM
	
	strcpy(descText, nameText);
	
	log_printf(LOG_INFO, "Adding %s device %d", nameText, ptr->bacObj.Object_Instance_Number);

	characterstring_init_ansi(&nameTextBCS, nameText);
	Add_Routed_Device(ptr, &nameTextBCS, descText);
}


#ifdef EM
void get_auth_token(const char *ts, uint8_t *token)
{
	uint8_t auth_token[20];
	uint8_t buffer[128];
	unsigned int i;

	unsigned int len = 0;
	len += snprintf((char *) buffer, sizeof(buffer), "%s", elf_get_rest_api_key());
	len += snprintf((char *) buffer + len, sizeof(buffer) - len, "%s", elf_get_rest_api_secret());
	len += snprintf((char *) buffer + len, sizeof(buffer) - len, "%s", ts);

	SHA1(buffer, len, auth_token);

	memset(buffer, 0, sizeof(buffer));

	len = 0;
	for (i = 0; i < sizeof(auth_token); i++)
	{
		len += snprintf((char *) buffer + len, sizeof(buffer) - len, "%02x", auth_token[i]);
	}

	memcpy((void *) token, (const void *) buffer, len);
}
#endif


// extern int DNET_list[2];
// extern uint8_t Rx_Buf[MAX_MPDU];

	//static bool isIAmDuplicate(
	    //BACNET_ADDRESS * src,
		//int *DNET_list,
		//uint8_t * pdu,
		//uint16_t pdu_len)
		//{
			//int apdu_offset = 0;
			//BACNET_ADDRESS dest = { 0 };
			//BACNET_NPDU_DATA npdu_data = { 0 };
			//
				///* only handle the version that we know how to handle */
				//if (pdu[0] == BACNET_PROTOCOL_VERSION) 
				//{
					//apdu_offset = npdu_decode(&pdu[0], &dest, src, &npdu_data);
					//if (apdu_offset <= 0) {
						//debug_printf("NPDU: Decoding failed; Discarded!\n");
						//return false ;
				//}
				//else if (npdu_data.network_layer_message) {
					//if ((dest.net == 0) || (dest.net == BACNET_BROADCAST_NETWORK)) {
						//return false ;
			//}
		//}
		//else if (apdu_offset <= pdu_len) {
			////if ((dest.net == 0) || (npdu_data.hop_count > 1))
				////routed_apdu_handler(src,
					////&dest,
					////DNET_list,
					////&pdu[apdu_offset],
					////(uint16_t)(pdu_len - apdu_offset));
			///* Else, hop_count bottomed out and we discard this one. */
		////}
	//}
	//else {
	    ///* Should we send NETWORK_MESSAGE_REJECT_MESSAGE_TO_NETWORK? */
		//debug_printf
		    //("NPDU: Unsupported BACnet Protocol Version=%u.  Discarded!\n",
			//(unsigned) pdu[0]);
	//}
//
	//return false ;
//}


// Make sure we are not about to duplicate another device already on the BACnet network
// bool check_for_duplicate_instances(void)
// {
	//uint i, pdu_len;
	//BACNET_ADDRESS src;
	//
	//for (i = 0; i < g_bacnet_device_count; i++)
	//{
		//Send_WhoIs_Global(
			//g_bacnet_device_list[i].bacnetDeviceInfo.bacObj.Object_Instance_Number,
			//g_bacnet_device_list[i].bacnetDeviceInfo.bacObj.Object_Instance_Number);
	//sleep(1);
	//
	//pdu_len = datalink_receive(&src, &Rx_Buf[0], MAX_MPDU, timeout);
	//if (pdu_len)
	//{
		//routing_npdu_handler(&src, DNET_list, &Rx_Buf[0], pdu_len);
//}
//
//}
	
// all is OK
// 	return false ;
// }


BACNET_RELIABILITY GetReliability(ObjectTypeDescriptor *bacnetObject)
{
	if (bacnetObject->reliability) return bacnetObject->reliability ;
	
	// todo 2 - use configured poll intervals
	if (time(NULL) - bacnetObject->lastUpdate > 20 * 60)
	{
		return RELIABILITY_UNRELIABLE_OTHER;
	}
	return RELIABILITY_NO_FAULT_DETECTED;
}
