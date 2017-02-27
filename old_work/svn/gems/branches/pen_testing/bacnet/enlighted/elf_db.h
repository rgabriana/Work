/*
 * elf_db.h
 *
 *  Created on: Jun 24, 2015
 *      Author: ed
 */

#ifndef ENLIGHTED_ELF_DB_H_
#define ENLIGHTED_ELF_DB_H_

#include "elf_gems_api.h"
#include "elf_objects.h"

//typedef enum device_state
//{
////    DEVICE_STATE_INIT = 0,
    //DEVICE_STATE_VALID,
    //DEVICE_STATE_INVALID,
//} e_device_state_t;

/* bacnet db is stored in csv format in sorted bacnet id order.
 */
typedef struct elf_bacnet_db
{
	e_elf_device_t		elf_dev_type;
	DEVICE_OBJECT_DATA	bacnetDeviceInfo;
	uint	floor_id;
	uint	sector_id;
	time_t	lastWhoIs;
	uint	whoIsCount;
	bool	setOnlineOnce;			// 3 who-is have been sent
	bool	duplicateDetected;
	bool	onlineOK;
	
#ifdef EM
	uint	switch_id;
	uint	fixture_id;
	uint	plugload_id;
#endif
	
// I don't think we need this - monitor state of each point in future... (driven by fixtures)	e_device_state_t	state;
	
// timestamps todo 4, move to individual structures above uint32_t			update_ts; // last updated timestamp (kept in memory only)
	
} __attribute__ ((__packed__)) elf_bacnet_db_t;


// elf_bacnet_db_t *elf_get_bacnet_db_ptr_specific_device(uint32_t bacnetDeviceInstance);
elf_bacnet_db_t *elf_get_bacnet_db_ptr_current_device(void);
elf_bacnet_db_t *get_bacnet_db_ptr_by_floor_and_area_id(uint floorId, uint areaId);

#endif /* ENLIGHTED_ELF_DB_H_ */
