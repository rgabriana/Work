/**************************************************************************
*
* Copyright (C) 2005 Steve Karg <skarg@users.sourceforge.net>
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
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <syslog.h>
#include "config.h"
#include "txbuf.h"
#include "bacdef.h"
#include "bacdcode.h"
#include "iam.h"
#include "address.h"
#include "handlers.h"
#include "elf_functions.h"
#include "elf_db.h"
#include "advdebug.h"

/** @file h_iam.c  Handles I-Am requests. */

extern elf_bacnet_db_t *g_bacnet_device_list;
extern uint g_bacnet_device_count;


/** Handler for I-Am responses.
 * Will add the responder to our cache, or update its binding.
 * @ingroup DMDDB
 * @param service_request [in] The received message to be handled.
 * @param service_len [in] Length of the service_request message.
 * @param src [in] The BACNET_ADDRESS of the message's source.
 */
void handler_i_am_add(
    uint8_t * service_request,
	uint16_t service_len,
	BACNET_ADDRESS * src)
{
	uint i;
	int len = 0;
	uint32_t device_id = 0;
	unsigned max_apdu = 0;
	int segmentation = 0;
	uint16_t vendor_id = 0;

	(void) service_len;
	len =
	    iam_decode_service_request(service_request,
		&device_id,
		&max_apdu,
		&segmentation,
		&vendor_id);
#if PRINT_ENABLED
	fprintf(stderr, "Received I-Am Request");
#endif
	if (len != -1) {
#if PRINT_ENABLED
		fprintf(stderr,
			" from %lu, MAC = %d.%d.%d.%d.%d.%d\n",
			(unsigned long) device_id,
			src->mac[0],
			src->mac[1],
			src->mac[2],
			src->mac[3],
			src->mac[4],
			src->mac[5]);
#endif
		// address_add(device_id, max_apdu, src);
	    
		// for Enlighted, we just want to mark any of our devices as duplicate if duplicate, that is
		// (mutex already locked)
	    
		// log_printf(LOG_WARNING, "Iam received %d", device_id);
	    
		// are any ready to go live?
		for (i = 0; i < g_bacnet_device_count; i++)
		{
			if (g_bacnet_device_list[i].bacnetDeviceInfo.bacObj.Object_Instance_Number == device_id && !g_bacnet_device_list[i].duplicateDetected)
			{
				notify_gui("Duplicate BACnet Device (%d) detected", g_bacnet_device_list[i].bacnetDeviceInfo.bacObj.Object_Instance_Number);
				log_printf(LOG_WARNING, "Duplicate BACnet Device (%d) detected", g_bacnet_device_list[i].bacnetDeviceInfo.bacObj.Object_Instance_Number);
				g_bacnet_device_list[i].duplicateDetected = true;
				g_bacnet_device_list[i].onlineOK = false;
			}
		}
	}
	else 
	{
#if PRINT_ENABLED
		fprintf(stderr, ", but unable to decode it.\n");
#endif
	}

	return;
}

/** Handler for I-Am responses (older binding-update-only version).
 * Will update the responder's binding, but if already in our cache.
 * @note This handler is deprecated, in favor of handler_i_am_add().
 *
 * @param service_request [in] The received message to be handled.
 * @param service_len [in] Length of the service_request message.
 * @param src [in] The BACNET_ADDRESS of the message's source.
 */
void handler_i_am_bind(
    uint8_t * service_request,
	uint16_t service_len,
	BACNET_ADDRESS * src)
{
	int len = 0;
	uint32_t device_id = 0;
	unsigned max_apdu = 0;
	int segmentation = 0;
	uint16_t vendor_id = 0;

	(void) service_len;
	len =
	    iam_decode_service_request(service_request,
		&device_id,
		&max_apdu,
		&segmentation,
		&vendor_id);
	if (len > 0) {
	    /* only add address if requested to bind */
		address_add_binding(device_id, max_apdu, src);
	}

	return;
}
