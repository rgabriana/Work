#include <stdlib.h>
#include <syslog.h>
#include <openssl/md5.h>
#include <unistd.h>
#include <errno.h>
#include "elf.h"
#include "advdebug.h"
#include "elf_functions.h"
#include "elf_config.h"

uint			g_bacnet_device_count = 0;
elf_bacnet_db_t *g_bacnet_device_list = NULL;
extern uint16_t iCurrent_Device_Idx;

//static int compare_bacnet_id_sort(const void *s1, const void *s2)
//{
	//elf_bacnet_db_t *d1 = (elf_bacnet_db_t *) s1;
	//elf_bacnet_db_t *d2 = (elf_bacnet_db_t *) s2;
//
	//return ((int) d1->bacnetDeviceInfo.bacObj.Object_Instance_Number - (int) d2->bacnetDeviceInfo.bacObj.Object_Instance_Number);
//}
//
//static int compare_bacnet_id_search(const void *key, const void *s)
//{
	//elf_bacnet_db_t *d = (elf_bacnet_db_t *) s;
//
	//int x = *(int *) key;
	//int y = (int) d->bacnetDeviceInfo.bacObj.Object_Instance_Number;
	//return (x - y);
//}

static void *realloc_bacnet_device_list(void *ptr, size_t size)
{
	if ((ptr = realloc(ptr, size)) == NULL)
	{
		log_printf(LOG_CRIT, "%s:%d - Error allocating memory", __FUNCTION__, __LINE__);
		exit(EXIT_FAILURE);
	}

	return ptr;
}

#if 0
int read_bacnet_device_db(void)
{
	FILE *fp;
	char buffer[128];
	char *ptr = NULL, *tptr = buffer;
	char *nptr[10] = {
		NULL
	};
	uint16_t count = 0;
	char *db_file = (char *) elf_get_db_file_config();

	if ((fp = fopen(db_file, "r")))
	{
		while (fgets(buffer, sizeof(buffer), fp) != NULL)
		{
			if (buffer[0] == '#' || buffer[0] == '\n' || buffer[0] == '\r')
			{
			    /* Comment line - ignore. */
				continue;
			}
			buffer[strlen(buffer) - 1] = 0;

			count = 0;
			while ((tptr = strtok_r(tptr, ",", &ptr)) != NULL)
			{
				nptr[count++] = tptr;
				tptr = NULL;
			}

			            //g_bacnet_device_list = (elf_bacnet_db_t *) realloc_bacnet_device_list(g_bacnet_device_list, (g_bacnet_device_count + 1) * sizeof(elf_bacnet_db_t));
			            //
			            	        //g_bacnet_device_list[g_bacnet_device_count].elf_dev_type = (e_elf_device_t) atoi(nptr[0]);
									//g_bacnet_device_list[g_bacnet_device_count].floor_id = (unsigned int) atoi(nptr[1]);
									//g_bacnet_device_list[g_bacnet_device_count].sector_id = (unsigned int) atoi(nptr[2]);
									//g_bacnet_device_list[g_bacnet_device_count].bacnetDeviceInstance = atol(nptr[3]);
									//// g_bacnet_device_list[g_bacnet_device_count].state = (e_device_state_t) atoi(nptr[4]);
									//// todo 4 g_bacnet_device_list[g_bacnet_device_count].update_ts = 0;
									//++g_bacnet_device_count;
	        
			e_elf_device_t elf_dev_type = (e_elf_device_t) atoi(nptr[0]);
			uint floor_id = (uint) atoi(nptr[1]);
			uint sector_id = (uint) atoi(nptr[2]);
			uint switch_id = (uint) atoi(nptr[3]);
			uint32_t Object_Instance_Number = atol(nptr[4]);
	        
			// now create the data tables (which in turn, now, creates the device_list entry
	        
			// todo 3 - 2 flags, one to rewrite the bacnet.db file, another to send I-Am.
			
			switch (elf_dev_type)
			{
			case ELF_DEVICE_EM:
				// ALWAYS ignore the EM in the file. Established by Main
				break;
			case ELF_DEVICE_AREA:
				establish_sector(floor_id, sector_id );
				break;
#ifdef EM				
			case ELF_DEVICE_SWITCH:
				establish_switch(floor_id,switch_id);
				break;
#endif // EM				
			default:
				panic("no such device");
			}
	        
			tptr = buffer;
		}
		fclose(fp);
	}
	return 0;
}
#endif


#if 0
int write_bacnet_device_list(void)
{
	FILE *fp;
	unsigned int count;

	if (g_bacnet_device_count == 0)
	{
		return 0;
	}

	// todo 4 should be sorted !
	qsort((void *) g_bacnet_device_list, (int) g_bacnet_device_count, sizeof(elf_bacnet_db_t), compare_bacnet_id_sort);

	elf_bacnet_db_t *ptr = g_bacnet_device_list;
	char *db_file = (char *) elf_get_db_file_config();

	if ((fp = fopen(db_file, "w+")))
	{
		for (count = 0; count < g_bacnet_device_count; count++, ptr++)
		{
#ifdef EM			
			fprintf(fp, "%u,%u,%u,%u,%u\n", 
				ptr->elf_dev_type, 
				ptr->floor_id, 
				ptr->sector_id, 
				ptr->switch_id, 
				ptr->bacnetDeviceInfo.bacObj.Object_Instance_Number);
#else // EM/UEM
			fprintf(fp,
				"%u,%u,%u,%u\n", 
				ptr->elf_dev_type, 
				ptr->floor_id, 
				ptr->sector_id, 
				ptr->bacnetDeviceInfo.bacObj.Object_Instance_Number);
#endif
		}

		fclose(fp);

		return 0;
	}

	log_printf(LOG_INFO, "Error=%s", strerror(errno));
	return -1;
}
#endif


void add_to_bacnet_device_list(elf_bacnet_db_t *ptr)
{
	elf_device_init(&ptr->bacnetDeviceInfo);
	
	if (g_bacnet_device_count == 0)
	{
		// the first device, the Router, does not have a network number, so as not to appear behind the router, but rather as the router
		// i.e. It must not have any "address" entry 
		memset(&ptr->bacnetDeviceInfo.bacDevAddr, 0, sizeof(ptr->bacnetDeviceInfo.bacDevAddr));
	}
	else
	{
		elf_initialize_device_addresses(ptr, elf_get_config(CFG_NETWORK_ID));
	}
	
	g_bacnet_device_list = (elf_bacnet_db_t *) realloc_bacnet_device_list(g_bacnet_device_list, (g_bacnet_device_count + 1) * sizeof(elf_bacnet_db_t));

	memcpy((void *) &g_bacnet_device_list[g_bacnet_device_count], ptr, sizeof(*ptr));

	g_bacnet_device_count++;

	// don't qsort - we need to ensure the em/router remains first on the list
	// qsort((void *) g_bacnet_device_list, g_bacnet_device_count, sizeof(elf_bacnet_db_t), compare_bacnet_id_sort);
}


//void mark_bacnet_device_list(void)
//{
    //uint16_t count ;
    //elf_bacnet_db_t *ptr = (elf_bacnet_db_t *) &g_bacnet_device_list[0];
    //for (count = 0; count < g_bacnet_device_count; count++, ptr++)
    //{
        //// Mark state as invalid.
        //ptr->state = DEVICE_STATE_INVALID;
    //}
//}

//void update_bacnet_device_list(elf_bacnet_db_t *data)
//{
    //uint16_t i;
//
    //if (!data)
    //{
        //return;
    //}
//
    //uint32_t device_id = data->bacnetDeviceInstance;
//
    //// cr00003 - uint8_t  new_entry = 0;
    //elf_bacnet_db_t *sptr = NULL;
    //elf_bacnet_db_t *zptr = data;
	//for (i = 0; i < elf_device_count; i++, zptr++)
    //{
        //// cr00003 - new_entry = 0;
        //if ((sptr = (elf_bacnet_db_t *) bsearch(&device_id, g_bacnet_device_list, (int) g_bacnet_device_count, sizeof(elf_bacnet_db_t), compare_bacnet_id_search)) != NULL)
        //{
            //// Found the device.
            //sptr->state = zptr->state;
        //}
    //}
//
	//// todo 4 - if we ever reinstate this function, sort before search!
    //qsort((void *) g_bacnet_device_list, (int) g_bacnet_device_count, sizeof(elf_bacnet_db_t), compare_bacnet_id_sort);
//}

elf_bacnet_db_t *elf_get_bacnet_db_ptr_current_device(void)
{
	return &g_bacnet_device_list[iCurrent_Device_Idx];
}

elf_bacnet_db_t *elf_get_bacnet_db_ptr_specific_device(const uint32_t bacnet_id) 
{
 	//void *ptr = bsearch(&bacnet_id, g_bacnet_device_list, (int) g_bacnet_device_count, sizeof(elf_bacnet_db_t), compare_bacnet_id_search);
	//if (ptr == NULL)
	//{
		//panic("why?");
	//}
	//return (elf_bacnet_db_t *) ptr;
	
	// list is no longer being sorted, since we need our router device to remain at the head of the list.
	uint i;
	for (i = 0; i < g_bacnet_device_count; i++)
	{
		if (g_bacnet_device_list[i].bacnetDeviceInfo.bacObj.Object_Instance_Number == bacnet_id)
		{
			return &g_bacnet_device_list[i] ;
		}
	}
	return NULL;
}


#ifdef EM
elf_bacnet_db_t *get_bacnet_db_ptr_by_switch_id(uint switchId)
{
	// cannot use bsearch here, not sorted by switchId
	unsigned int i;
	for (i = 0; i < g_bacnet_device_count; i++)
	{
		if (g_bacnet_device_list[i].elf_dev_type == ELF_DEVICE_SWITCH)
		{
			if (g_bacnet_device_list[i].switch_id == switchId) return &g_bacnet_device_list[i];
		}
	}
	panic("why?");
	return NULL;
}
#endif // EM

elf_bacnet_db_t *get_bacnet_db_ptr_by_floor_and_area_id(uint floorId, uint areaId)
{
	// cannot use bsearch here, not sorted by switchId
	unsigned int i;
	for (i = 0; i < g_bacnet_device_count; i++)
	{
		if (g_bacnet_device_list[i].elf_dev_type == ELF_DEVICE_AREA)
		{
			if (g_bacnet_device_list[i].sector_id == areaId &&
				g_bacnet_device_list[i].floor_id == floorId) return &g_bacnet_device_list[i];
		}
	}
	panic("why?");
	return NULL;
}

