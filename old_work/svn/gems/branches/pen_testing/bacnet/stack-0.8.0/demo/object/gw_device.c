/**************************************************************************
*
* Copyright (C) 2005,2010 Steve Karg <skarg@users.sourceforge.net>
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal in the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject to
* the following conditions:
*
* The above copyright notice and this permission notice shall be included
* in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
*********************************************************************/

/** @file gw_device.c  Functions that extend the Device object to support routing. */

#include <stdbool.h>
#include <stdint.h>
#include <string.h>     /* for memmove */
#include <time.h>       /* for timezone, localtime */
#include <syslog.h>
#include "bacdef.h"
#include "bacdcode.h"
#include "bacenum.h"
#include "bacapp.h"
#include "config.h"     /* the custom stuff */
#include "apdu.h"
#include "wp.h" /* write property handling */
#include "rp.h" /* read property handling */
#include "version.h"
#include "device.h"     /* me */
#include "handlers.h"
#include "datalink.h"
#include "address.h"
#include "reject.h"
/* include the objects */
#include "ai.h"
#include "ao.h"
#include "av.h"
#include "bi.h"
#include "bv.h"
#if defined(BACFILE)
#include "bacfile.h"    /* object list dependency */
#endif
/* os specfic includes */
#include "timer.h"
#include "elf_db.h"
#include "elf_functions.h"

#if !defined(BAC_ROUTING)
#warning This file should not be included in the build unless BAC_ROUTING is enabled.
#endif

/****************************************************************************
 ************* BACnet Routing Functionality (Optional) **********************
 ****************************************************************************
 * It would be correct to view the routing functionality here as inheriting
 * and extending the regular Device Object functionality.
 ****************************************************************************/

/** Model the gateway as the main Device, with (two) remote
 * Devices that are reached via its routing capabilities.
 */
#include "elf.h"
//DEVICE_OBJECT_DATA *Devices = NULL;

extern uint g_bacnet_device_count;
extern elf_bacnet_db_t *g_bacnet_device_list;

/** Keep track of the number of managed devices, including the gateway */
// uint16_t Num_Managed_Devices = 0;
/** Which Device entry are we currently managing.
 * Since we are not using actual class objects here, the best we can do is
 * keep this local variable which notes which of the Devices the current
 * request is addressing.  Should default to 0, the main gateway Device.
 */
uint16_t iCurrent_Device_Idx = 0;
// extern unsigned int elf_device_count;

/* void Routing_Device_Init(uint32_t first_object_instance) is
 * found in device.c
 */

/** Add a Device to our table of Devices[].
 * The first entry must be the gateway device.
 * @param Object_Instance [in] Set the new Device to this instance number.
 * @param sObject_Name [in] Use this Object Name for the Device.
 * @param sDescription [in] Set this Description for the Device.
 * @return The index of this instance in the Devices[] array,
 *         or -1 if there isn't enough room to add this Device.
 */
 
void Add_Routed_Device(
	DEVICE_OBJECT_DATA *pDev,
	BACNET_CHARACTER_STRING * sObject_Name,
	const char *sDescription)
{
	pDev->bacObj.mObject_Type = OBJECT_DEVICE;
	if (sObject_Name != NULL)
		Routed_Device_Set_Object_Name(pDev,
			sObject_Name->encoding,
			sObject_Name->value,
			sObject_Name->length);
	else
		Routed_Device_Set_Object_Name(pDev,
			CHARACTER_UTF8,
			"No Name",
			strlen("No Name"));
	if (sDescription != NULL)
		Routed_Device_Set_Description(pDev, sDescription, strlen(sDescription));
	else
		Routed_Device_Set_Description(pDev, "No Descr", strlen("No Descr"));
	pDev->Database_Revision = 0;    /* Reset/Initialize now */
}


/** Get the currently active BACnet address.
 * This is an implementation of the datalink_get_my_address() template for
 * devices with routing.
 *
 * @param my_address [out] Points to the currently active Device Object's
 * 							BACnet address.
 */
void routed_get_my_address(
    BACNET_ADDRESS * my_address)
{
	if (my_address) {
		memcpy(my_address,
			&g_bacnet_device_list[iCurrent_Device_Idx].bacnetDeviceInfo.bacDevAddr,
			sizeof(BACNET_ADDRESS));
	}
}


/** See if the Gateway or Routed Device at the given idx matches
 * the given MAC address.
 * Has the desirable side-effect of setting iCurrent_Device_Idx to the
 * given idx if a match is found, for use in the subsequent routing handling
 * functions here.
 *
 * @param idx [in] Index into Devices[] array being requested.
 *                 0 is for the main, gateway Device entry.
 * @param address_len [in] Length of the mac_adress[] field.
 *         If 0, then this is a MAC broadcast.  Otherwise, size is determined
 *         by the DLL type (eg, 6 for BIP and 2 for MSTP).
 * @param mac_adress [in] The desired MAC address of a Device;
 *
 * @return True if the MAC addresses match (or the address_len is 0,
 *         meaning MAC broadcast, so it's an automatic match).
 *         Else False if no match or invalid idx is given.
 */
bool Routed_Device_Address_Lookup(
    unsigned int idx,
	uint8_t address_len,
	uint8_t * mac_adress)
{
	bool result = false;
	// DEVICE_OBJECT_DATA *pDev = &Devices[idx];
	elf_bacnet_db_t *pDev = &g_bacnet_device_list[idx];
	
	int i;

	if ((idx < g_bacnet_device_count)) {
		if (address_len == 0) {
		    /* Automatic match */
			iCurrent_Device_Idx = idx;
			result = true;
		}
		else if (mac_adress != NULL) {
			for (i = 0; i < address_len; i++) {
				if (pDev->bacnetDeviceInfo.bacDevAddr.mac[i] != mac_adress[i])
					break;
			}
			if (i == address_len) {     /* Success! */
				iCurrent_Device_Idx = idx;
				result = true;
			}
		}
	}
	return result;
}


/** Find the next Gateway or Routed Device at the given MAC address,
 * starting the search at the "cursor".
 * Has the desirable side-effect of setting internal iCurrent_Device_Idx
 * if a match is found, for use in the subsequent routing handling
 * functions.
 *
 * @param dest [in] The BACNET_ADDRESS of the message's destination.
 * 		   If the Length of the mac_adress[] field is 0, then this is a MAC
 * 		   broadcast.  Otherwise, size is determined
 *         by the DLL type (eg, 6 for BIP and 2 for MSTP).
 * @param DNET_list [in] List of our reachable downstream BACnet Network numbers.
 * 					 Normally just one valid entry; terminated with a -1 value.
 * @param cursor [in,out] The concept of the cursor is that it is a starting
 * 		   "hint" for the search; on return, it is updated to provide the
 * 		   cursor value to use with a subsequent GetNext call, or it
 *         equals -1 if there are no further matches.
 *         Set it to 0 on entry to access the main, gateway Device entry, or
 *         to start looping through the routed devices.
 *         Otherwise, its returned value is implementation-dependent and the
 *         calling function should not alter or interpret it.
 *
 * @return True if the MAC addresses match (or if BACNET_BROADCAST_NETWORK and
 * 		   the dest->len is 0, meaning MAC bcast, so it's an automatic match).
 *         Else False if no match or invalid idx is given; the cursor will
 *         be returned as -1 in these cases.
 */
bool Routed_Device_GetNext(
    BACNET_ADDRESS * dest,
	int *DNET_list,
	int *cursor)
{
	int dnet = DNET_list[0];    /* Get the DNET of our virtual network */
	int idx = *cursor;
	bool bSuccess = false;
	
	if (!g_bacnet_device_count)
	{
		// just not ready.
		*cursor = -1;
		return false ;
	}
	
	// is our router out-of-commission because of duplicate address, or not yet online?
	if ( g_bacnet_device_list[0].duplicateDetected)
	{
		// Router cannot respond. Nobody can respond.
		log_printf(LOG_CRIT, "Nobody can respond because router device [%d] has a duplicate on the BACnet Network", g_bacnet_device_list[0].bacnetDeviceInfo.bacObj.Object_Instance_Number);
		*cursor = -1;
		return false ;
	}
	if ( !g_bacnet_device_list[0].onlineOK)
	{
		// Router cannot respond. Nobody can respond.
		log_printf(LOG_CRIT, "Nobody can respond because router device [%d] not ready", g_bacnet_device_list[0].bacnetDeviceInfo.bacObj.Object_Instance_Number);
		*cursor = -1;
		return false ;
	}
	
RETRY:	

    /* First, see if the index is out of range.
     * Eg, last call to GetNext may have been the last successful one.
     */
	if ((idx < 0) || (idx >= (int) g_bacnet_device_count))
	{
		idx = -1;
	}
    /* Next, see if it's a BACnet broadcast.
     * For broadcasts, all Devices get a chance at it.
     */
	else if (dest->net == BACNET_BROADCAST_NETWORK) 
	{
	    /* Just take the entry indexed by the cursor */
		// todo 4 - for BTL, we need to check for remote broadcast, local broadcast !
		bSuccess = Routed_Device_Address_Lookup(idx++, dest->len, dest->adr);
		if ( g_bacnet_device_list[iCurrent_Device_Idx].duplicateDetected)
		{
			log_printf(LOG_CRIT, "Cannot respond because device [%d] has a duplicate on the BACnet Network", g_bacnet_device_list[iCurrent_Device_Idx].bacnetDeviceInfo.bacObj.Object_Instance_Number);
			bSuccess = false;
			goto RETRY;
		}
		if (!g_bacnet_device_list[iCurrent_Device_Idx].onlineOK)
		{
			log_printf(LOG_CRIT, "Cannot respond because device [%d] not ready", g_bacnet_device_list[iCurrent_Device_Idx].bacnetDeviceInfo.bacObj.Object_Instance_Number);
			bSuccess = false;
			goto RETRY;
		}
	}
	/* Or see if it's for the main Gateway Device, because
	 * there's no routing info.
	 */
	else if (dest->net == 0) {
	    /* Handle like a normal, non-routed access of the Gateway Device.
	     * But first, make sure our internal access is pointing at
	     * that Device in our table by telling it "no routing info" : */
		bSuccess = Routed_Device_Address_Lookup(0, dest->len, dest->adr);
		/* Next step: no more matches: */
		idx = -1;
	}
	/* Or if is our virtual DNET, check
	 * against each of our virtually routed Devices.
	 * If we get a match, have it handle the APDU.
	 * For broadcasts, all Devices get a chance at it.
	 */
	else if (dest->net == dnet) {
	// todo - this change goes back to karg??
	// todo, ekh removed this, because we were not seeing the first device
	   	if (idx == 0)   /* Step over this case (starting point) */
	            idx = 1;
	// and now I must restore it, because we don't want the gw device after all	    
		while (idx < (int) g_bacnet_device_count) 
		{
			bSuccess = Routed_Device_Address_Lookup(idx++, dest->len, dest->adr);
			if ( g_bacnet_device_list[iCurrent_Device_Idx].duplicateDetected )
			{
				log_printf(LOG_CRIT, "Cannot respond because device [%d] has a duplicate on the BACnet Network", g_bacnet_device_list[iCurrent_Device_Idx].bacnetDeviceInfo.bacObj.Object_Instance_Number);
				bSuccess = false;
			}
			else if (!g_bacnet_device_list[iCurrent_Device_Idx].onlineOK)
			{
				log_printf(LOG_CRIT, "Cannot respond because device [%d] not ready", g_bacnet_device_list[iCurrent_Device_Idx].bacnetDeviceInfo.bacObj.Object_Instance_Number);
				bSuccess = false;
			}
		    
			if (bSuccess)
				break;  /* We don't need to keep looking */
		}
	}

	if (!bSuccess)
		*cursor = -1;
	else if (idx == (int) g_bacnet_device_count)    /* No more to GetNext */
		*cursor = -1;
	else
		*cursor = idx;
	
	return bSuccess;
}


/** Check if the destination network is reachable - is it our virtual network,
 *  or local or else broadcast.
 *
 * @param dest_net [in] The BACnet network number of a message's destination.
 * 		   Success if it is our virtual network number, or 0 (local for the
 * 			gateway, or 0xFFFF for a broadcast network number.
 * @param DNET_list [in] List of our reachable downstream BACnet Network numbers.
 * 					 Normally just one valid entry; terminated with a -1 value.
 * @return True if matches our virtual network, or is for the local network
 * 			Device (the gateway), or is BACNET_BROADCAST_NETWORK, which is
 * 		    an automatic match.
 *         Else False if not a reachable network.
 */
bool Routed_Device_Is_Valid_Network(
    uint16_t dest_net,
	int *DNET_list)
{
	int dnet = DNET_list[0];    /* Get the DNET of our virtual network */
	bool bSuccess = false;

	    /* First, see if it's a BACnet broadcast (automatic pass). */
	if (dest_net == BACNET_BROADCAST_NETWORK)
		bSuccess = true;
	/* Or see if it's for the main Gateway Device, because
	 * there's no routing info.
	 */
	else if (dest_net == 0)
		bSuccess = true;
	/* Or see if matches our virtual DNET */
	else if (dest_net == dnet)
		bSuccess = true;

	return bSuccess;
}


/* methods to override the normal Device objection functions */

uint32_t Routed_Device_Index_To_Instance(
    unsigned index)
{
   // index = index;
	return g_bacnet_device_list[iCurrent_Device_Idx].bacnetDeviceInfo.bacObj.Object_Instance_Number;
}

/** See if the requested Object instance matches that for the currently
 * indexed Device Object.
 * iCurrent_Device_Idx must have been set to point to this Device Object
 * before this function is called.
 * @param object_id [in] Object ID of the desired Device object.
 * 			If the wildcard value (BACNET_MAX_INSTANCE), always matches.
 * @return True if Object ID matches the present Device, else False.
 */
bool Routed_Device_Valid_Object_Instance_Number(
    uint32_t object_id)
{
	bool bResult = false;
	elf_bacnet_db_t *pDev = &g_bacnet_device_list[iCurrent_Device_Idx];

	if (pDev->bacnetDeviceInfo.bacObj.Object_Instance_Number == object_id)
		bResult = true;

	return bResult;
}

bool Routed_Device_Name(
    uint32_t object_instance,
	BACNET_CHARACTER_STRING * object_name)
{
	elf_bacnet_db_t *pDev = &g_bacnet_device_list[iCurrent_Device_Idx];
	if (object_instance == pDev->bacnetDeviceInfo.bacObj.Object_Instance_Number) {
		return characterstring_init_ansi(object_name,
			pDev->bacnetDeviceInfo.bacObj.Object_Name2);
	}

	return false;
}

/** Manages ReadProperty service for fields which are different for routed
 * Devices, or hands off to the default Device RP function for the rest.
 * @param rpdata [in] Structure which describes the property to be read.
 * @return The length of the apdu encoded, or BACNET_STATUS_ERROR for error or
 * BACNET_STATUS_ABORT for abort message.
 */
int Routed_Device_Read_Property_Local(
    BACNET_READ_PROPERTY_DATA * rpdata)
{
	int apdu_len = 0;   /* return value */
	BACNET_CHARACTER_STRING char_string;
	uint8_t *apdu = NULL;
	DEVICE_OBJECT_DATA *pDev = &g_bacnet_device_list[iCurrent_Device_Idx].bacnetDeviceInfo;

	if ((rpdata == NULL) || (rpdata->application_data == NULL) ||
	    (rpdata->application_data_len == 0)) {
		return 0;
	}
	apdu = rpdata->application_data;
	switch (rpdata->object_property) {
	case PROP_OBJECT_IDENTIFIER:
		apdu_len =
		    encode_application_object_id(&apdu[0],
			OBJECT_DEVICE,
			pDev->bacObj.Object_Instance_Number);
		break;
	case PROP_OBJECT_NAME:
		characterstring_init_ansi(&char_string, pDev->bacObj.Object_Name2);
		apdu_len =
		    encode_application_character_string(&apdu[0], &char_string);
		break;
	case PROP_DESCRIPTION:
		characterstring_init_ansi(&char_string, pDev->Description);
		apdu_len =
		    encode_application_character_string(&apdu[0], &char_string);
		break;
	case PROP_DATABASE_REVISION:
		apdu_len =
		    encode_application_unsigned(&apdu[0], pDev->Database_Revision);
		break;
	default:
		apdu_len = Device_Read_Property_Local(rpdata);
		break;
	}

	return (apdu_len);
}

bool Routed_Device_Write_Property_Local(
    BACNET_WRITE_PROPERTY_DATA * wp_data)
{
	bool status = false;        /* return value */
	int len = 0;
	BACNET_APPLICATION_DATA_VALUE value;
	elf_bacnet_db_t *pDev = elf_get_bacnet_db_ptr_current_device();

    /* decode the some of the request */
	len =
	    bacapp_decode_application_data(wp_data->application_data,
		wp_data->application_data_len,
		&value);
	if (len < 0) {
	    /* error while decoding - a value larger than we can handle */
		wp_data->error_class = ERROR_CLASS_PROPERTY;
		wp_data->error_code = ERROR_CODE_VALUE_OUT_OF_RANGE;
		return false;
	}
	/*  only array properties can have array options */
	if ((wp_data->object_property != PROP_OBJECT_LIST) &&
	    (wp_data->array_index != BACNET_ARRAY_ALL)) {
		wp_data->error_class = ERROR_CLASS_PROPERTY;
		wp_data->error_code = ERROR_CODE_PROPERTY_IS_NOT_AN_ARRAY;
		return false;
	}
	/* FIXME: len < application_data_len: more data? */
	switch (wp_data->object_property) {
	case PROP_OBJECT_IDENTIFIER:
		status =
		    WPValidateArgType(&value,
			BACNET_APPLICATION_TAG_OBJECT_ID,
			&wp_data->error_class,
			&wp_data->error_code);
		if (status) {
			if ((value.type.Object_Id.type == OBJECT_DEVICE) &&
			    (Routed_Device_Set_Object_Instance_Number(value.type.
			            Object_Id.instance))) {
				/* FIXME: we could send an I-Am broadcast to let the world know */
			}
			else {
				status = false;
				wp_data->error_class = ERROR_CLASS_PROPERTY;
				wp_data->error_code = ERROR_CODE_VALUE_OUT_OF_RANGE;
			}
		}
		break;
	case PROP_OBJECT_NAME:
		status =
		    WPValidateString(&value,
			MAX_DEV_NAME_LEN,
			false,
			&wp_data->error_class,
			&wp_data->error_code);
		if (status) {
			Routed_Device_Set_Object_Name( &pDev->bacnetDeviceInfo,
				characterstring_encoding(&value.
			        type.Character_String),
				characterstring_value(&value.type.Character_String),
				characterstring_length(&value.type.Character_String));
		}
		break;
	default:
		status = Device_Write_Property_Local(wp_data);
		break;
	}
	return status;
}

/* methods to manipulate the data */

/** Return the Object Instance number for the currently active Device Object.
 * This is an overload of the important, widely used
 * Device_Object_Instance_Number() function.
 *
 * @return The Instance number of the currently active Device.
 */
uint32_t Routed_Device_Object_Instance_Number(
    void)
{
	return g_bacnet_device_list[iCurrent_Device_Idx].bacnetDeviceInfo.bacObj.Object_Instance_Number;
}

bool Routed_Device_Set_Object_Instance_Number(
    uint32_t object_id)
{
	bool status = true; /* return value */

	if (object_id <= BACNET_MAX_INSTANCE) {
	    /* Make the change and update the database revision */
		g_bacnet_device_list[iCurrent_Device_Idx].bacnetDeviceInfo.bacObj.Object_Instance_Number = object_id;
		Routed_Device_Inc_Database_Revision();
	}
	else
		status = false;

	return status;
}

/** Sets the Object Name for a routed Device (or the gateway).
 * Uses local variable iCurrent_Device_Idx to know which Device
 * is to be updated.
 * @param object_name [in] Character String for the new Object Name.
 * @return True if succeed in updating Object Name, else False.
 */
bool Routed_Device_Set_Object_Name(
	DEVICE_OBJECT_DATA *pDev,
	uint8_t encoding,
	const char *value,
	size_t length)
{
	bool status = false;        /*return value */

	if ((encoding == CHARACTER_UTF8) && (length < MAX_DEV_NAME_LEN)) {
	    /* Make the change and update the database revision */
		memmove(pDev->bacObj.Object_Name2, value, length);
		pDev->bacObj.Object_Name2[length] = 0;
		// todo 3 - need a way to increment ONCE device memory block established Routed_Device_Inc_Database_Revision();
		status = true;
	}

	return status;
}

bool Routed_Device_Set_Description(
	DEVICE_OBJECT_DATA *pDev,
	const char *name,
	size_t length)
{
	bool status = false;        /*return value */

	if (length < MAX_DEV_DESC_LEN) {
		memmove(pDev->Description, name, length);
		pDev->Description[length] = 0;
		status = true;
	}

	return status;
}


/*
 * Shortcut for incrementing database revision as this is potentially
 * the most common operation if changing object names and ids is
 * implemented.
 */
void Routed_Device_Inc_Database_Revision(
    void)
{
	DEVICE_OBJECT_DATA *pDev = &g_bacnet_device_list[iCurrent_Device_Idx].bacnetDeviceInfo;
	pDev->Database_Revision++;
}


/** Check to see if the current Device supports this service.
 * Presently checks for RD and DCC and only allows them if the current
 * device is the gateway device.
 *
 * @param service [in] The service being requested.
 * @param service_argument [in] An optional argument (eg, service type).
 * @param apdu_buff [in,out] The buffer where we will encode a Reject message.
 *                              May be NULL if don't want an encoded response.
 * @param invoke_id [in] The invoke_id of the service request.
 * @return Length of bytes encoded in apdu_buff[] for a Reject message,
 *          just 1 if no apdu_buff was supplied and service is not supported,
 *          else 0 if service is approved for the current device.
 */
int Routed_Device_Service_Approval(
    BACNET_CONFIRMED_SERVICE service,
	int service_argument,
	uint8_t * apdu_buff,
	uint8_t invoke_id)
{
	int len = 0;
	switch (service) {
	case SERVICE_CONFIRMED_REINITIALIZE_DEVICE:
	    /* If not the gateway device, we don't support RD */
		if (iCurrent_Device_Idx > 0) {
			if (apdu_buff != NULL)
				len =
				    reject_encode_apdu(apdu_buff,
					invoke_id,
					REJECT_REASON_UNRECOGNIZED_SERVICE);
			else
				len = 1;    /* Non-zero return */
		}
		break;
	case SERVICE_CONFIRMED_DEVICE_COMMUNICATION_CONTROL:
	    /* If not the gateway device, we don't support DCC */
		if (iCurrent_Device_Idx > 0) {
			if (apdu_buff != NULL)
				len =
				    reject_encode_apdu(apdu_buff,
					invoke_id,
					REJECT_REASON_UNRECOGNIZED_SERVICE);
			else
				len = 1;    /* Non-zero return */
		}
		break;
	default:
	    /* Everything else is a pass, at this time. */
		break;
	}
	return len;
}
