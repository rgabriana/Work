#include <syslog.h>
#include <stdlib.h>
#include "elf.h"
#include "elf_gems_api.h"
#include "elf_config.h"
#include "elf_objects.h"
#include "advdebug.h"
#include "txbuf.h"
#include "client.h"

extern uint32_t g_first_sector_device_instance;
extern uint32_t g_first_em_device_instance;

#ifdef EM
extern uint32_t g_first_switch_device_instance2;
#endif

// extern elf_bacnet_db_t *g_bacnet_device_list;
extern s_energy_manager_t *g_energy_manager;
// extern uint g_bacnet_device_count;
// extern elf_config_t elf_bacnet_config;


void get_mac_address(uint32_t bacnet_id, uint8_t *mac)
{
	memset(mac, 0, 6);
	mac[3] = ((bacnet_id >> 16) & 0xFF);
	mac[4] = ((bacnet_id >> 8) & 0xFF);
	mac[5] = (bacnet_id & 0xFF);
}


void establish_energy_manager(uint32_t emId)
{
	char nameText[MAX_DEV_NAME_LEN];
	
 	if (g_energy_manager != NULL)
	{
		// aready established, and there can only be one (for now)
		// it must also be the first device, to handle BACnet routing
		panic("already established");
		return ;
	}
	
	// Set up the BACnet device for the single Energy Manager
	if (!(g_energy_manager = (s_energy_manager_t *) calloc(1, sizeof(s_energy_manager_t))))
	{
		log_printf(LOG_CRIT, "In %s: can't allocate memory for Energy Manager!\n", __func__);
		exit(EXIT_FAILURE);
	}

	// todo 4 this seems very redundant
	strcpy(g_energy_manager->em_name, elf_get_config_string(CFG_EM_NAME));
	
	// todo 3 save to file
	
	//uint k;
	//for (k = 0; k < g_bacnet_device_count; k++)
	//{
	//if (g_bacnet_device_list[k].elf_dev_type == ELF_DEVICE_EM && 
		//g_bacnet_device_list[k].bacnetDeviceInstance == g_first_em_device_instance)
	//{
	    //// ptr->state = DEVICE_STATE_VALID;
		//return ;
	//}
	//}

	// Device does not exist in db. Create it.
	elf_bacnet_db_t bacnet_db;
	memset((void *) &bacnet_db, 0, sizeof(bacnet_db));
	// note, we override whatever device instance is in the db file with the instance in the config file... // todo 3, do something similar with other base addresses
	bacnet_db.bacnetDeviceInfo.bacObj.Object_Instance_Number = g_first_em_device_instance;
	bacnet_db.elf_dev_type = ELF_DEVICE_EM;
	
	add_to_bacnet_device_list(&bacnet_db);
	// todo 3 bacnet_db.state = DEVICE_STATE_VALID;
	
	elf_get_device_object_name( &bacnet_db, nameText, MAX_DEV_NAME_LEN);

	Routed_Device_Set_Object_Name( &bacnet_db.bacnetDeviceInfo, CHARACTER_UTF8, nameText, strlen(nameText));
	
		// todo - send this higher up the call chain, once the in-memory routed device has been established.
		//if (send_i_am)
		//{
		    /* broadcast an I-Am for each routed Device now */
		    // We don't really care if some packets are dropped, just want to get something out there
		    // to show we are alive, more than anything else. A true BACnet client will probe
		    // conscientiously to find 'missing' devices.
			Send_I_Am(&Handler_Transmit_Buffer[0]);
		//}
	
}


s_floor_t *establish_floor(const s_floor_t *floor) 
{
	unsigned int i; 
	
	// Energy Manager device / router needs to be established first
	if (!g_energy_manager)
	{
		panic("Energy Manager not established");
		exit(EXIT_FAILURE);
	}
	
	for (i = 0; i < g_energy_manager->num_floors; i++)
	{
		if (g_energy_manager->floors[i].id == floor->id) return &g_energy_manager->floors[i];
	}	
	
	// else create a new floor entry (not in the database, just in memory)

	s_floor_t *emFloor = (s_floor_t *) realloc(g_energy_manager->floors, ++g_energy_manager->num_floors * sizeof(s_floor_t));
	if (!emFloor)
	{
		panic("Could not realloc");
		exit(EXIT_FAILURE);
	}
	g_energy_manager->floors = emFloor;
	s_floor_t *newFloor = &g_energy_manager->floors[g_energy_manager->num_floors - 1];
	// memset( newFloor, 0, sizeof(s_floor_t));
	*newFloor = *floor;
	
	return newFloor;
}


s_sector_t *establish_sector(s_floor_t *floor, const s_sector_t *sector)
{
	unsigned int k;

	// s_floor_t *floor = establish_floor(floor);

	// make sure it does not already exist
	for (k = 0; k < floor->num_sectors; k++)
	{
		if (floor->s_sector[k].id == sector->id ) return &floor->s_sector[k] ;
	}
	
	// go ahead and create it

    // Device does not exist in db. Create it.
	elf_bacnet_db_t bacnet_db;
	memset(&bacnet_db, 0, sizeof(bacnet_db));
	            
	bacnet_db.bacnetDeviceInfo.bacObj.Object_Instance_Number = g_first_sector_device_instance + 
		floor->id * FLOOR_OFFSET_ID + 
		sector->id;
	            
	bacnet_db.floor_id = floor->id;
	bacnet_db.sector_id = sector->id ;
	bacnet_db.elf_dev_type = ELF_DEVICE_AREA;

	add_to_bacnet_device_list(&bacnet_db);
		// bacnet_db.state = DEVICE_STATE_VALID;
	
	// make in-memory structure allocations
	s_sector_t *emSector = (s_sector_t *) realloc(floor->s_sector, ++floor->num_sectors * sizeof(s_sector_t));
	if (!emSector)
	{
		panic("Could not realloc");
		exit(EXIT_FAILURE);
	}
	floor->s_sector = emSector;
	s_sector_t *newSector = &floor->s_sector[floor->num_sectors - 1];
	// memset(newSector, 0, sizeof(s_sector_t));
	*newSector = *sector ;
	return newSector ;
}


#ifdef EM 
s_switchgroup_t *establish_switch( s_floor_t *floor, const s_switchgroup_t *switchPtr)
{
	unsigned int k;

	// s_floor_t *floor = establish_floor(floorId);

	// make sure it does not already exist
	for (k = 0; k < floor->num_switches; k++)
	{
		if (floor->s_switch[k].id == switchPtr->id ) return &floor->s_switch[k];
	}
	
	// go ahead and create it

    // Device does not exist in db. Create it.
	elf_bacnet_db_t bacnet_db;
	memset((void *) &bacnet_db, 0, sizeof(bacnet_db));
	            
	bacnet_db.bacnetDeviceInfo.bacObj.Object_Instance_Number = g_first_switch_device_instance2 + 
		floor->id * FLOOR_OFFSET_ID + 
		switchPtr->id;
	            
	bacnet_db.floor_id = floor->id;
	bacnet_db.switch_id = switchPtr->id;
	bacnet_db.elf_dev_type = ELF_DEVICE_SWITCH;

	add_to_bacnet_device_list(&bacnet_db);
	
	// make in-memory structure allocations
	s_switchgroup_t *emswitch = (s_switchgroup_t *) realloc(floor->s_switch, ++floor->num_switches * sizeof(s_switchgroup_t));
	if (!emswitch)
	{
		panic("Could not realloc");
		exit(EXIT_FAILURE);
	}
	floor->s_switch = emswitch;
	s_switchgroup_t *newswitch = &floor->s_switch[floor->num_switches - 1];
	// memset(newswitch, 0, sizeof(s_switchgroup_t));
	*newswitch = *switchPtr ;
	return newswitch;
}
#endif // EM

