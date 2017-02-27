/**************************************************************************
 *
 * Copyright (C) 2006 Steve Karg <skarg@users.sourceforge.net>
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
/**
 * Code for this project began with code from the demo/server project and
 * Paul Chapman's vmac project.
 */

#include <stddef.h>
#include <stdint.h>
#include <stdlib.h>
#include <signal.h>
#include <ctype.h>      // for toupper
#include <syslog.h>
#include <setjmp.h>		// just to suppress a warning
#include <pthread.h>

#include "config.h"
#include "gateway.h"
#include "address.h"
#include "bacdef.h"
#include "handlers.h"
#include "client.h"
#include "dlenv.h"
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
/* include the device object */
#include "device.h"
#ifdef BACNET_TEST_VMAC
#include "vmac.h"
#endif
#include "ai.h"
#include "ao.h"
#include "av.h"
#include "bi.h"
#include "bv.h"

#include "elf.h"
#include "elf_std.h"
#include "elf_db.h"
#include "elf_functions.h"
#include "elf_config.h"

#include "bactext.h"
#include "advdebug.h"
#include "genAna.h"
#include "genBin.h"
#include "version.h"

#define BACNET_LOG_NAME "bacnetd"


extern unsigned char elf_mac_address[];

extern uint32_t g_first_sector_device_instance;
extern uint32_t g_first_em_device_instance;

#ifdef EM
extern uint32_t g_first_switch_device_instance2;
#endif
extern elf_config_t elf_bacnet_config;

bool duplicateNetwork;
bool stopTimer;
bool muteFlag;
bool interactiveExplore ;
pthread_t ourThread;
pthread_mutex_t ourMutex;

/* All included BACnet objects */
static object_functions_t Object_Table[] = {
{
	OBJECT_DEVICE,
	NULL /* Init - don't init Device or it will recurse! */,
	Device_Count,
	Routed_Device_Index_To_Instance,
	Routed_Device_Valid_Object_Instance_Number,
	Routed_Device_Name,
	Routed_Device_Read_Property_Local,
	Routed_Device_Write_Property_Local,
	Device_Property_Lists,
	DeviceGetRRInfo,
	NULL /* Iterator */,
	NULL /* Value_Lists */,
	NULL /* COV */,
	NULL /* COV Clear */,
	NULL /* Intrinsic Reporting */ 
},

{
	OBJECT_ANALOG_INPUT,
	Analog_Input_Init,
	Analog_Input_Count,
	Analog_Input_Index_To_Instance,
	Analog_Input_Valid_Instance,
	Analog_Input_Object_Name,
	Analog_Input_Read_Property,
	Analog_Input_Write_Property,
	Analog_Input_Property_Lists,
	NULL /* ReadRangeInfo */,
	NULL /* Iterator */,
	NULL, // Analog_Input_Encode_Value_List,
	NULL, // Analog_Input_Change_Of_Value,
	NULL, // Analog_Input_Change_Of_Value_Clear,
	NULL, // Analog_Input_Intrinsic_Reporting
},

{
	OBJECT_ANALOG_VALUE,
	Analog_Value_Init,
	Analog_Value_Count,
	Analog_Value_Index_To_Instance,
	Analog_Value_Valid_Instance,
	Analog_Value_Object_Name,
	Analog_Value_Read_Property,
	Analog_Value_Write_Property,
	Analog_Value_Property_Lists,
	NULL /* ReadRangeInfo */,
	NULL /* Iterator */,
	NULL /* Value_Lists */,
	NULL /* COV */,
	NULL /* COV Clear */,
	NULL /* Intrinsic Reporting */ 
},
#if 0
{
	OBJECT_ANALOG_OUTPUT,
	Analog_Output_Init,
	Analog_Output_Count,
	Analog_Output_Index_To_Instance,
	Analog_Output_Valid_Instance,
	Analog_Output_Object_Name,
	Analog_Output_Read_Property,
	Analog_Output_Write_Property,
	Analog_Output_Property_Lists,
	NULL /* ReadRangeInfo */,
	NULL /* Iterator */,
	NULL, // Analog_Output_Encode_Value_List,
	NULL, // Analog_Output_Change_Of_Value,
	NULL, // Analog_Output_Change_Of_Value_Clear,
	NULL
}, // Analog_Output_Intrinsic_Reporting},
#endif

{
	OBJECT_BINARY_INPUT,
	Binary_Input_Init,
	Binary_Input_Count,
	Binary_Input_Index_To_Instance,
	Binary_Input_Valid_Instance,
	Binary_Input_Object_Name,
	Binary_Input_Read_Property,
	Binary_Input_Write_Property,
	Binary_Input_Property_Lists,
	NULL /* ReadRangeInfo */,
	NULL /* Iterator */,
	Binary_Input_Encode_Value_List,
	Binary_Input_Change_Of_Value,
	Binary_Input_Change_Of_Value_Clear,
	NULL /* Intrinsic Reporting */ 
},

#ifdef todo // I do not think we need binary outputs.. perhaps only when channel objects start up

{
	OBJECT_BINARY_OUTPUT,
	Binary_Output_Init,
	Binary_Output_Count,
	Binary_Output_Index_To_Instance,
	Binary_Output_Valid_Instance,
	Binary_Output_Object_Name,
	Binary_Output_Read_Property,
	Binary_Output_Write_Property,
	Binary_Output_Property_Lists,
	NULL /* ReadRangeInfo */,
	NULL /* Iterator */,
	NULL /* Value_Lists */,
	NULL /* COV */,
	NULL /* COV Clear */,
	NULL /* Intrinsic Reporting */
},
#endif

#ifdef EM
{
	OBJECT_BINARY_VALUE,
	Binary_Value_Init,
	Binary_Value_Count,
	Binary_Value_Index_To_Instance,
	Binary_Value_Valid_Instance,
	Binary_Value_Object_Name,
	Binary_Value_Read_Property,
	Binary_Value_Write_Property,
	Binary_Value_Property_Lists,
	NULL /* ReadRangeInfo */,
	NULL /* Iterator */,
	NULL /* Value_Lists */,
	NULL /* COV */,
	NULL /* COV Clear */,
	NULL /* Intrinsic Reporting */ 
},
#endif // EM

#ifdef EM_todo
	        // todo, put AV back in there
	        // todo, channel object will only apply to EM
{
	OBJECT_CHANNEL,
	Channel_Init,
	Channel_Count,
	Channel_Index_To_Instance,
	Channel_Valid_Instance,
	Channel_Object_Name,
	Channel_Read_Property,
	Channel_Write_Property,
	Channel_Property_Lists,
	NULL /* ReadRangeInfo */,
	NULL /* Iterator */,
	NULL /* Value_Lists */,
	NULL /* COV */,
	NULL /* COV Clear */,
	NULL /* Intrinsic Reporting */
},
#endif
{
	MAX_BACNET_OBJECT_TYPE,
	NULL /* Init */,
	NULL /* Count */,
	NULL /* Index_To_Instance */,
	NULL /* Valid_Instance */,
	NULL /* Object_Name */,
	NULL /* Read_Property */,
	NULL /* Write_Property */,
	NULL /* Property_Lists */,
	NULL /* ReadRangeInfo */,
	NULL /* Iterator */,
	NULL /* Value_Lists */,
	NULL /* COV */,
	NULL /* COV Clear */,
	NULL /* Intrinsic Reporting */
}
};

	/** Buffer used for receiving */
uint8_t Rx_Buf[MAX_MPDU] = {
	0
};

	/** The list of DNETs that our router can reach.
	 *  Only one entry since we don't support downstream routers.
	 */
int DNET_list[2] = {
	VIRTUAL_DNET,
	-1 /* Need -1 terminator */
};

#include <sys/time.h> /* struct timeval, select() */
/* ICANON, ECHO, TCSANOW, struct termios */
#include <termios.h> /* tcgetattr(), tcsetattr() */
#include <unistd.h> /* read() */

static struct termios g_old_kbd_mode;
/*****************************************************************************
 *****************************************************************************/
static void cooked(void)
{
	tcsetattr(0, TCSANOW, &g_old_kbd_mode);
}
/*****************************************************************************
 *****************************************************************************/
static void raw(void)
{
	static char init = 0;
	/**/
	struct termios new_kbd_mode;

	if (init)
		return;
	/* get current state of console */
	tcgetattr(0, &g_old_kbd_mode);
	/* put keyboard (stdin, actually) in raw, unbuffered mode */
	memcpy(&new_kbd_mode, &g_old_kbd_mode, sizeof(struct termios));
	new_kbd_mode.c_lflag &= ~(ICANON | ECHO);
	new_kbd_mode.c_cc[VTIME] = 0;
	new_kbd_mode.c_cc[VMIN] = 1;
	tcsetattr(0, TCSANOW, &new_kbd_mode);
	/* when we exit, go back to normal, "cooked" mode */
	atexit(cooked);

	init = 1;
}
/*****************************************************************************
 *****************************************************************************/
static int kbhit(void)
{
	struct timeval timeout;
	fd_set read_handles;
	int status;

	raw();
	/* check stdin (fd 0) for activity */
	FD_ZERO(&read_handles);
	FD_SET(0, &read_handles);
	timeout.tv_sec = timeout.tv_usec = 0;
	status = select(1, &read_handles, NULL, NULL, &timeout);
	if (status < 0)
	{
		printf("select() failed in kbhit()\n");
		exit(1);
	}
	return status;
}
/*****************************************************************************
 *****************************************************************************/
static int getch(void)
{
	unsigned char temp;

	raw();
	/* stdin = fd 0 */
	if (read(0, &temp, 1) != 1)
		return 0;
	return temp;
}

static int doInteractive(void);

/** Initialize the handlers we will utilize.
 * @see Device_Init, apdu_set_unconfirmed_handler, apdu_set_confirmed_handler
 */
static void Init_Service_Handlers(void)
{
	Device_Init(Object_Table);
	// Done elsewhere by Enlighted
	// Routing_Device_Init(first_object_instance);

	    /* we need to handle who-is to support dynamic device binding
	     * For the gateway, we will use the unicast variety so we can
	     * get back through switches to different subnets.
	     * Don't need the routed versions, since the npdu handler calls
	     * each device in turn.
	     */
	 // Be careful - there is a h_routed_npdu function for routing, don't double up by adding a routed handler to the handlers...
	apdu_set_unconfirmed_handler(SERVICE_UNCONFIRMED_WHO_IS, handler_who_is_unicast);
	apdu_set_unconfirmed_handler(SERVICE_UNCONFIRMED_WHO_HAS, handler_who_has);
	apdu_set_unconfirmed_handler(SERVICE_UNCONFIRMED_I_AM, handler_i_am_add);
	
	/* set the handler for all the services we don't implement */
	/* It is required to send the proper reject message... */
	apdu_set_unrecognized_service_handler_handler(handler_unrecognized_service);
	/* Set the handlers for any confirmed services that we support. */
	/* We must implement read property - it's required! */
	apdu_set_confirmed_handler(SERVICE_CONFIRMED_READ_PROPERTY, handler_read_property);
	apdu_set_confirmed_handler(SERVICE_CONFIRMED_READ_PROP_MULTIPLE, handler_read_property_multiple);
	apdu_set_confirmed_handler(SERVICE_CONFIRMED_WRITE_PROPERTY, handler_write_property);
#ifdef ENLIGHTED_INC
#else /* ENLIGHTED_INC */
	apdu_set_confirmed_handler(SERVICE_CONFIRMED_READ_RANGE,
		handler_read_range);
#if defined(BACFILE)
	apdu_set_confirmed_handler(SERVICE_CONFIRMED_ATOMIC_READ_FILE,
		handler_atomic_read_file);
	apdu_set_confirmed_handler(SERVICE_CONFIRMED_ATOMIC_WRITE_FILE,
		handler_atomic_write_file);
#endif
	apdu_set_confirmed_handler(SERVICE_CONFIRMED_REINITIALIZE_DEVICE,
		handler_reinitialize_device);
	apdu_set_unconfirmed_handler(SERVICE_UNCONFIRMED_UTC_TIME_SYNCHRONIZATION,
		handler_timesync_utc);
	apdu_set_unconfirmed_handler(SERVICE_UNCONFIRMED_TIME_SYNCHRONIZATION,
		handler_timesync);
#endif /* ENLIGHTED_INC */
	apdu_set_confirmed_handler(SERVICE_CONFIRMED_SUBSCRIBE_COV, handler_cov_subscribe);
	apdu_set_unconfirmed_handler(SERVICE_UNCONFIRMED_COV_NOTIFICATION, handler_ucov_notification);
	/* handle communication so we can shutup when asked */
	apdu_set_confirmed_handler(SERVICE_CONFIRMED_DEVICE_COMMUNICATION_CONTROL, handler_device_communication_control);
}


// Note:  Code used to find firewall issue:     sudo ufw allow 47808/udp

void *curlThread(void *treadId)
{
	while (true)
	{
		elf_update_timer_handler();
		sleep(1);
	}

}


extern elf_bacnet_db_t *g_bacnet_device_list;
extern uint g_bacnet_device_count;

static void duplicate_detection_housekeeping(void)
{
	uint i;
	// look for the next device of interest ;
	pthread_mutex_lock(&ourMutex);
	
	// are any ready to go live?
	for (i = 0; i < g_bacnet_device_count; i++)
	{
		if (!g_bacnet_device_list[i].setOnlineOnce && !g_bacnet_device_list[i].duplicateDetected)
		{
			if (g_bacnet_device_list[i].whoIsCount > 2 && g_bacnet_device_list[i].lastWhoIs != time(NULL))
			{
				log_printf(LOG_INFO, "Setting online %d", g_bacnet_device_list[i].bacnetDeviceInfo.bacObj.Object_Instance_Number);
				g_bacnet_device_list[i].setOnlineOnce = true;
				g_bacnet_device_list[i].onlineOK = true;
			}
		}
	}

	// any that still need to be pinged?
	for (i = 0; i < g_bacnet_device_count; i++)
	{
		if (!g_bacnet_device_list[i].setOnlineOnce && !g_bacnet_device_list[i].duplicateDetected)
		{
			if (time(NULL) - g_bacnet_device_list[i].lastWhoIs > 5)
			{
				log_printf(LOG_INFO, "Checking for duplicate device instances, sending WhoIs to %d, try #%d", g_bacnet_device_list[i].bacnetDeviceInfo.bacObj.Object_Instance_Number, g_bacnet_device_list[i].whoIsCount + 1);
				
				Send_WhoIs_Global(	g_bacnet_device_list[i].bacnetDeviceInfo.bacObj.Object_Instance_Number,
					g_bacnet_device_list[i].bacnetDeviceInfo.bacObj.Object_Instance_Number);
				
				g_bacnet_device_list[i].lastWhoIs = time(NULL);
				g_bacnet_device_list[i].whoIsCount++;
				// let us not swamp the network.
				break ;
			}
		}
	}
	pthread_mutex_unlock(&ourMutex);
}

bool iAmRouterComplete;


static void process_bacnet(uint timeout)
{
	BACNET_ADDRESS src;
	uint16_t pdu_len;
	
    /* returns 0 bytes on timeout */
	pdu_len = datalink_receive(&src, &Rx_Buf[0], MAX_MPDU, timeout);
	if (pdu_len)
	{
		pthread_mutex_lock(&ourMutex);
		routing_npdu_handler(&src, DNET_list, &Rx_Buf[0], pdu_len);
		pthread_mutex_unlock(&ourMutex);
	}
}
	

int main(int argc, char *argv[])
{
	uint timeout = 1000; /* milliseconds */
	time_t last_seconds = 0;
	time_t current_seconds = 0;
	uint32_t elapsed_seconds = 0;
	uint32_t elapsed_milliseconds = 0;
	uint i;
	// uint32_t first_object_instance = FIRST_DEVICE_NUMBER;

	bool noPermissions = false;		// to stop noise while leak-checking in valgrind
	//	bool cleanDatabase = true;      // Decided to remove database file on startup in all cases. Keep -c for compatibility?

	if (pthread_mutex_init(&ourMutex, NULL) != 0)
	{
		panic("mutex init failed");
		exit(EXIT_FAILURE);
	}	
	
#ifdef BACNET_TEST_VMAC
	    /* Router data */
	BACNET_DEVICE_PROFILE *device;
	BACNET_VMAC_ADDRESS adr;
#endif

#ifdef ENLIGHTED_INC
	openlog(BACNET_LOG_NAME, 0, 0);
	char *conf_file = NULL;
	int opt;
	
	elf_init_config();

	while ((opt = getopt(argc, argv, "df:prvcim:s")) != -1)
	{
		switch (opt)
		{
		case 'f':
			conf_file = optarg;
			break;
		case 'd':
			log_printf(LOG_NOTICE, "-d switch no longer used");
		    // todo - why might we want to do this?				daemonize = 0;
			break;
		case 'p':
			noPermissions = true;
			break;
		case 'c':
			log_printf(LOG_NOTICE, "-c switch no longer used");
			break;
		case 'm':
			if (optarg)
			{
				elf_bacnet_config.tmpFilePath = optarg;
			}
			log_printf(LOG_WARNING, "Warning: Mute is ON, temp file path=[%s]!", elf_bacnet_config.tmpFilePath);
			muteFlag = true;
			break;
		case 'i':
			interactiveExplore = true;
			break;
		case 's':
			stopTimer = true;
			break;
		case 'v':
#ifdef UEM
			printf("\nBacnet HVAC versions...\n   SCM version: %s\n   Dev version: %s\n", elf_get_version(), BACNET_VERSION_TEXT);
#else
			printf("\nBacnet Lighting versions...\n   SCM version: %s\n   Dev version: %s\n", elf_get_version(), BACNET_VERSION_TEXT);
#endif
			exit(0);
		default:
			fprintf(stderr, "Usage: %s [-f filename] [-d] [-v] [-p]\n", argv[0]);
			fprintf(stderr, "-f filename      location of config file\n");
			fprintf(stderr, "-p               do not resolve permissions\n");
			fprintf(stderr, "-d               do not daemonize\n");
			fprintf(stderr, "-c               remove (clean) database file\n");
			fprintf(stderr, "-m path          Don't issue curl commands. (mute)\n");
			fprintf(stderr, "-i               interactive memory table display\n");
			fprintf(stderr, "-s               Stop (for debugging)\n");
			fprintf(stderr, "-v               print version and exit\n");
			fprintf(stderr, "\n");
			exit(EXIT_FAILURE);
		}
	}

	if (optind == 1)
	{
		fprintf(stderr, "Usage: %s [-f filename] [-d] [-v]\n", argv[0]);
		exit(EXIT_FAILURE);
	}

	if (noPermissions)
	{
		log_printf(LOG_WARNING, "No ID permissions option (-p) selected. Remove for delivery");
	}
	else
	{
		int uid = -1, gid = -1;
		if (elf_get_my_id(NULL, &uid, &gid) == 0)
		{
			log_printf(LOG_INFO, "Setting [e]uid to %d and [e]gid to %d", uid, gid);
			if (setuid(uid) < 0)
			{
				log_printf(LOG_ERR, "Error setting uid %s", strerror(errno));
			}
			if (seteuid(uid) < 0)
			{
				log_printf(LOG_ERR, "Error setting euid %s", strerror(errno));
			}
			if (setgid(gid) < 0)
			{
				log_printf(LOG_ERR, "Error setting gid %s", strerror(errno));
			}
			if (setegid(gid) < 0)
			{
				log_printf(LOG_ERR, "Error setting egid %s", strerror(errno));
			}
		}

		log_printf(LOG_INFO, "Real UID=%d, Effective UID=%d, Real GID=%d, Effective GID=%d", getuid(), geteuid(), getgid(), getegid());
	}

	log_printf(LOG_INFO, "Reading configuration from %s file", conf_file);
	if (elf_read_config_file(conf_file) < 0)
	{
		exit(EXIT_FAILURE);
	}
	log_printf(LOG_INFO, "Successfully read configuration from %s file", conf_file);
	log_printf(LOG_NOTICE, "Target server is %s", elf_get_db_gems_ip_address_config());

	//if (cleanDatabase) 
	//{
		//remove(elf_get_db_file_config());
	//}

	g_first_em_device_instance = elf_get_config(CFG_EM_BASE_INSTANCE);
	g_first_sector_device_instance = elf_get_config(CFG_SECTOR_BASE_INSTANCE);
#ifdef EM
	g_first_switch_device_instance2 = elf_get_config(CFG_SWITCH_BASE_INSTANCE2);
#endif

	    // Device_Set_Object_Instance_Number();       // Effectively floor 0, area 0 (unassigned area)
	DNET_list[0] = elf_get_config(CFG_NETWORK_ID);

	log_printf(LOG_NOTICE, "BACnet Router Version %s", BACnet_Version);



#else /* ENLIGHTED_INC */
	    /* allow the device ID to be set */
	if (argc > 1)
	{
		first_object_instance = strtol(argv[1], NULL, 0);
		if ((first_object_instance == 0) ||
		        (first_object_instance >= BACNET_MAX_INSTANCE))
		{
			printf("Error: Invalid Object Instance %s \n", argv[1]);
			printf("Provide a number from 1 to %ul \n",
				BACNET_MAX_INSTANCE - 1);
			exit(1);
		}
	}

	printf("BACnet Router Demo\n" "BACnet Stack Version %s\n"
	        "BACnet Device ID: %u\n",
		BACnet_Version,
		first_object_instance);
#endif /* ENLIGHTED_INC */

#ifdef ENLIGHTED_INC
		    /* Setup objects */
	elf_object_template_setup();

	    /* Save our mac address */
	if (elf_get_mac_address(elf_mac_address) != 0)
	{
		log_printf(LOG_CRIT, "Failed to get system mac address");
		exit(EXIT_FAILURE);
	}

	Init_Service_Handlers();
	char port[8], apdu_timeoutval[10];
	snprintf(port, sizeof(port), "%d", elf_get_config(CFG_LISTEN_PORT));
	setenv("BACNET_IP_PORT", port, 1);
	setenv("BACNET_IFACE", elf_get_db_interface(), 1);
	snprintf(apdu_timeoutval, sizeof(apdu_timeoutval), "%d", elf_get_config(CFG_APDU_TIMEOUT));
	setenv("BACNET_APDU_TIMEOUT", apdu_timeoutval, 1);
	dlenv_init();
	
	
	for (i = 0; i < 3; i++)
	{
		Broadcast_Who_Is_Router_To_Network(elf_get_config(CFG_NETWORK_ID));
		process_bacnet(100);
		if (duplicateNetwork)
		{
			sleep(5);
			exit(EXIT_FAILURE);
		}
		sleep(1);
	}

	establish_energy_manager(elf_get_config(CFG_EM_BASE_INSTANCE));
	
	// elf_devices_setup();
	// read_bacnet_device_db();

	if (!stopTimer)
	{
		config_refresh_site();
		data_refresh_for_site();
	}

	elf_update_timer_init();
#endif /* ENLIGHTED_INC */

#ifdef ENLIGHTED_INC
#else /* ENLIGHTED_INC */
	Devices_Init(first_object_instance);
	Initialize_Device_Addresses();
#endif /* ENLIGHTED_INC */
	atexit(datalink_cleanup);

#ifdef BACNET_TEST_VMAC
	    /* initialize vmac table and router device */
	device = vmac_initialize(99, 2001);
	debug_printf(device->name, "ROUTER:%u", vmac_get_subnet());
#endif
	    /* configure the timeout values */
	last_seconds = time(NULL);

    /* broadcast an I-am-router-to-network on startup */
#ifdef ENLIGHTED_INC
	log_printf(LOG_INFO, "Remote Network DNET Number %d \n", DNET_list[0]);
#else /* ENLIGHTED_INC */
	printf("Remote Network DNET Number %d \n", DNET_list[0]);
#endif /* ENLIGHTED_INC */
	
	// only do this once we know there are no others... ! 
	// I have moved it to an appropriate place
	// Send_I_Am_Router_To_Network(DNET_list);

#ifdef ENLIGHTED_INC
	    // Daemonize this process
#if 0
	int rc = 0;
	if (daemonize)
	{
		rc = daemon(0, 0);
	}
#endif
#endif /* ENLIGHTED_INC */
	
	if (interactiveExplore)
	{
		fprintf(stderr, "Press a key for interactive explorer : ");
		timeout = 100; // milliseconds, make everything more responsive.
	}
	
	// start thread for curl operations.
	int rc = pthread_create(&ourThread, NULL, curlThread, (void *)&rc);
	if (rc)
	{
		panic("Could not start thread");
		exit(EXIT_FAILURE);
	}
	
	/* loop forever */
	while (!duplicateNetwork)
	{
	    /* input */
		current_seconds = time(NULL);

#ifdef ENLIGHTED_INC
		if (!stopTimer) // todo 5 remove all?
		{
			// replaced by thread elf_update_timer_handler();
		}
#endif /* ENLIGHTED_INC */

		process_bacnet(timeout);
		
		/* at least one second has passed */
		elapsed_seconds = current_seconds - last_seconds;
		if (elapsed_seconds)
		{
			last_seconds = current_seconds;
			dcc_timer_seconds(elapsed_seconds);
#if defined(BACDL_BIP) && BBMD_ENABLED
			bvlc_maintenance_timer(elapsed_seconds);
#endif
			dlenv_maintenance_timer(elapsed_seconds);
			elapsed_milliseconds = elapsed_seconds * 1000;
			tsm_timer_milliseconds(elapsed_milliseconds);
			
			duplicate_detection_housekeeping();

			// is the first (router) device alive, for the first time?
			if (!iAmRouterComplete)
			{
				if (g_bacnet_device_count && g_bacnet_device_list[0].onlineOK)
				{
					Send_I_Am_Router_To_Network(DNET_list);
					iAmRouterComplete = true;
				}
			}
			
		}
		handler_cov_task();

		if (interactiveExplore)
		{
			pthread_mutex_lock(&ourMutex);
			if (doInteractive() < 0)
			{
				pthread_mutex_unlock(&ourMutex);
				return 0;
			}
			pthread_mutex_unlock(&ourMutex);
		}
	}
	return 0;
}

extern ts_elf_template_object_t *elf_template_objs;
extern s_energy_manager_t *g_energy_manager;
// extern ts_elf_objects_index_list_t *elf_objs_index_list[MAX_DEVICE_TYPES];
extern BACNET_OBJECT_TYPE elf_valid_bacnet_obj_types[MAX_BACNET_TYPES_PER_ELF_DEVICE];
extern INT_TO_TEXT objectTypeText[];

//static const char *text_bacnet_object_type(BACNET_OBJECT_TYPE type)
//{
	//return bactext_object_type_name((unsigned) type);
//}


static int iFloor = -1;
static int iArea = -1;
#ifdef EM
static int iSwitch = -1;
static int iScene = -1;
static int iFixture = -1;
static int iPlugload = -1;
// static int iLightlevel = -1;
static uint analogObjectIndexForScenes;
#endif

static int doInteractive(void)
{
	const char *indent = "    ";

	if (kbhit())
	{
		int ch = getch();
		switch (tolower(ch))
		{
#if 0			
		case 'r':
			read_bacnet_device_db();
			break;
		case 'w':
			write_bacnet_device_list();
			break;
		case 'z':
			FreeEnergyManagerMemory();
			break;
#endif
		case '0':
			if (!stopTimer)
			{
				printf("Stopping refreshes\n");
				stopTimer = true;
			}
			else
			{
				printf("Starting refreshes\n");
				stopTimer = false;
			}
			break;
			
		case 'q':
			FreeEnergyManagerMemory();
			return -1;
			
		case 'r':
			fprintf(stderr, "Refreshing Data. Please wait.\n");
			data_refresh_for_site();
			fprintf(stderr, "Refreshing Complete.\n");
			break;

#ifdef EM
		case 'e':
			{
				int i,j;
				if (g_energy_manager == NULL)
				{
		        
					fprintf(stderr, "Error: g_energy_manager is NULL\n");
					return 0 ; 
				}
				
				const char *indent = "   ";
				fprintf(stderr, "\nEnergy Manager ******************************* %s\n", BACnet_Version);
				fprintf(stderr, "Number of Fixtures: %d\n", g_energy_manager->num_fixtures);
				fprintf(stderr, "%s Energy Lighting:  %6.2f\n", indent, g_energy_manager->energyLighting.presentValue);
				fprintf(stderr, "%s Energy Plugload:  %6.2f\n", indent, g_energy_manager->energyPlugload.presentValue);
				fprintf(stderr, "%s ADR Level:        %6.2f\n", indent, g_energy_manager->adrLevel.analogTypeDescriptor.presentValue);
				fprintf(stderr, "%s Emergency Status:   %4d\n", indent, g_energy_manager->emergencyStatus.binaryTypeDescriptor.presentValue);
				
				if (elf_bacnet_config.fixtureAmbient)
				{
					for (i = 0; i < g_energy_manager->num_fixtures; i++)
					{
						s_fixture_t2 *fixture = &g_energy_manager->fixtures[i];
						// fprintf(stderr, "%s%s Fixture ID: %u,   (AreaId:%u, FloorId:%u)\n", indent, indent, fixture->fixtureId, fixture->areaId, fixture->floorId);
						fprintf(stderr, "%s%s Fixture ID: %u,   (AreaId:%u)\n", indent, indent, fixture->fixtureId, fixture->areaId);
					
						// now print each value for each fixture
						BACNET_OBJECT_TYPE objType = OBJECT_ANALOG_INPUT;
						uint baseIndex = elf_get_template_object_count_per_category(CATEGORY_EM, objType) +  elf_get_template_object_count_per_category(CATEGORY_FIXTURE, objType) * i;
						for (j = 0; j < elf_get_template_object_count_per_category(CATEGORY_FIXTURE, objType); j++)
						{
							uint32_t	deviceInstance = g_bacnet_device_list[0].bacnetDeviceInfo.bacObj.Object_Instance_Number;
							uint32_t	object_instance = elf_index_to_object_instance_new(deviceInstance, baseIndex + j, objType);
							ts_elf_template_object_t *currentTemplateObject = find_template_object_record_em(CATEGORY_FIXTURE, objType, GET_ADDER_FROM_INSTANCE(object_instance));
							if (currentTemplateObject == NULL) continue ;
							AnalogObjectTypeDescriptor *aod = elf_get_analog_object_live_data(deviceInstance, currentTemplateObject, object_instance);
							if (aod == NULL) continue ;
							fprintf(stderr,
								"%s %-35s %6.2f   BACnet %s:%u\n",
								indent,
								currentTemplateObject->description, 
								aod->presentValue, 
								IntToText(objectTypeText, 
									(int) objType),
								object_instance);
						}
					
						// now print each value for each fixture for Analog Values
						objType = OBJECT_ANALOG_VALUE;
						baseIndex = elf_get_template_object_count_per_category(CATEGORY_EM, objType) +  elf_get_template_object_count_per_category(CATEGORY_FIXTURE, objType) * i;
						for (j = 0; j < elf_get_template_object_count_per_category(CATEGORY_FIXTURE, objType); j++)
						{
							uint32_t	deviceInstance = g_bacnet_device_list[0].bacnetDeviceInfo.bacObj.Object_Instance_Number;
							uint32_t	object_instance = elf_index_to_object_instance_new(deviceInstance, baseIndex + j, objType);
							ts_elf_template_object_t *currentTemplateObject = find_template_object_record_em(CATEGORY_FIXTURE, objType, GET_ADDER_FROM_INSTANCE(object_instance));
							if (currentTemplateObject == NULL) continue ;
							AnalogObjectTypeDescriptor *aod = elf_get_analog_object_live_data(deviceInstance, currentTemplateObject, object_instance);
							if (aod == NULL) continue ;
							fprintf(stderr,
								"%s %-35s %6.2f   BACnet %s:%u\n",
								indent,
								currentTemplateObject->description, 
								aod->presentValue, 
								IntToText(objectTypeText, 
									(int) objType),
								object_instance);
						}

					
											// now print each value for each fixture for Binary Inputs
						objType = OBJECT_BINARY_INPUT;
						baseIndex = elf_get_template_object_count_per_category(CATEGORY_EM, objType) +  elf_get_template_object_count_per_category(CATEGORY_FIXTURE, objType) * i;
						for (j = 0; j < elf_get_template_object_count_per_category(CATEGORY_FIXTURE, objType); j++)
						{
							uint32_t	deviceInstance = g_bacnet_device_list[0].bacnetDeviceInfo.bacObj.Object_Instance_Number;
							uint32_t	object_instance = elf_index_to_object_instance_new(deviceInstance, baseIndex + j, objType);
							ts_elf_template_object_t *currentTemplateObject = find_template_object_record_em(CATEGORY_FIXTURE, objType, GET_ADDER_FROM_INSTANCE(object_instance));
							if (currentTemplateObject == NULL) continue ;
							BinaryObjectTypeDescriptor *aod = elf_get_binary_object_live_data(deviceInstance, currentTemplateObject, object_instance);
							if (aod == NULL) continue ;
							fprintf(stderr,
								"%s %-35s %6u   BACnet %s:%u\n",
								indent,
								currentTemplateObject->description, 
								aod->presentValue, 
								IntToText(objectTypeText, 
									(int) objType),
								object_instance);
						}

					
											// now print each value for each fixture for Binary Values
						objType = OBJECT_BINARY_VALUE;
						baseIndex = elf_get_template_object_count_per_category(CATEGORY_EM, objType) +  elf_get_template_object_count_per_category(CATEGORY_FIXTURE, objType) * i;
						for (j = 0; j < elf_get_template_object_count_per_category(CATEGORY_FIXTURE, objType); j++)
						{
							uint32_t	deviceInstance = g_bacnet_device_list[0].bacnetDeviceInfo.bacObj.Object_Instance_Number;
							uint32_t	object_instance = elf_index_to_object_instance_new(deviceInstance, baseIndex + j, objType);
							ts_elf_template_object_t *currentTemplateObject = find_template_object_record_em(CATEGORY_FIXTURE, objType, GET_ADDER_FROM_INSTANCE(object_instance));
							if (currentTemplateObject == NULL) continue ;
							BinaryObjectTypeDescriptor *aod = elf_get_binary_object_live_data(deviceInstance, currentTemplateObject, object_instance);
							if (aod == NULL) continue ;
							fprintf(stderr,
								"%s %-35s %6u   BACnet %s:%u\n",
								indent,
								currentTemplateObject->description, 
								aod->presentValue, 
								IntToText(objectTypeText, 
									(int) objType),
								object_instance);
						}
					}
				}
			}
			break;
#endif
				
		case 'f':
			if (g_energy_manager == NULL)
			{
				fprintf(stderr, "Error: g_energy_manager is NULL\n");
				return 0 ; 
			}
			if (++iFloor >= (int) g_energy_manager->num_floors)
			{
				iFloor = 0;
			}
			if (g_energy_manager->num_floors < 1)
			{
				fprintf(stderr, "No floors established\n");
				return 0 ;
			}
			fprintf(stderr,
				"\nFloor[%d/%d] ID: %d ********************************* %s\n", 
				iFloor + 1,
				g_energy_manager->num_floors,
				g_energy_manager->floors[iFloor].id,
				BACnet_Version);
			// fprintf(stderr, "Floor[%d] ID: %d\n", iFloor, g_energy_manager->floors[iFloor].id);
			fprintf(stderr, "  Num Areas        : %d\n", g_energy_manager->floors[iFloor].num_sectors);
#ifdef EM			
			fprintf(stderr, "  Num Switch Groups: %d\n", g_energy_manager->floors[iFloor].num_switches);
#endif			
			break;
			
		case 'a':
			{
				if (iFloor < 0) iFloor = 0;

				if (g_energy_manager == NULL)
				{
					fprintf(stderr, "Error: g_energy_manager is NULL\n");
					return 0 ; 
				}
				if (g_energy_manager->num_floors < 1)
				{
					fprintf(stderr, "No floors established\n");
					return 0 ;
				}
				if (!g_energy_manager->floors[iFloor].num_sectors)
				{
					fprintf(stderr, "Floor has no areas\n");
					return 0;
				}
				if (++iArea >= (int) g_energy_manager->floors[iFloor].num_sectors)
				{
					iArea = 0;
				}
				
				elf_bacnet_db_t *dbptr = get_bacnet_db_ptr_by_floor_and_area_id(g_energy_manager->floors[iFloor].id, g_energy_manager->floors[iFloor].s_sector[iArea].id);
				if (!dbptr)
				{
					panic("could not resolve ptr");
				}
				else 
				{
					fprintf(stderr,
						"\nFloor %d, Area[%d/%d] ID:%d ========================= %s\n", 
						g_energy_manager->floors[iFloor].id, 
						iArea+1,
						g_energy_manager->floors[iFloor].num_sectors,
						g_energy_manager->floors[iFloor].s_sector[iArea].id,
						BACnet_Version);
					fprintf(stderr, "%s BACnet Dev:   %d\n", indent, dbptr->bacnetDeviceInfo.bacObj.Object_Instance_Number);
					fprintf(stderr, "%s Area Name:    %-30s\n", indent, g_energy_manager->floors[iFloor].s_sector[iArea].za_name);
#ifdef EM					
					fprintf(stderr, "%s Num Fixtures:    %4d\n", indent, g_energy_manager->floors[iFloor].s_sector[iArea].num_fixtures_in_area);
					fprintf(stderr, "%s Num Plugloads:   %4d\n", indent, g_energy_manager->floors[iFloor].s_sector[iArea].num_plugloads);
#endif
				
					// displayBACnetObjects(dbptr->bacnetDeviceInstance, CATEGORY_AREA);
					
					unsigned int i, j;
					for (i = 0; i < MAX_BACNET_TYPES_PER_ELF_DEVICE; i++)
					{
						BACNET_OBJECT_TYPE bobjType = elf_valid_bacnet_obj_types[i];
#ifdef EM							
						unsigned int count = elf_get_template_object_count_per_category(CATEGORY_AREA, bobjType);
#else						unsigned int count = elf_get_template_object_count(bobjType);
#endif						
						for (j = 0; j < count; j++)
						{
							uint32_t object_instance;
							object_instance = elf_index_to_object_instance_new(dbptr->bacnetDeviceInfo.bacObj.Object_Instance_Number, j, bobjType);
							ts_elf_template_object_t *currentObject = find_template_object_record_em(CATEGORY_AREA, bobjType, object_instance);
							if (currentObject == NULL) 
							{
								panic("why?");
								continue ;
							}
							 
							switch (bobjType)
							{
							case OBJECT_ANALOG_INPUT:
							case OBJECT_ANALOG_VALUE:
								{
									AnalogObjectTypeDescriptor *aod = elf_get_analog_object_live_data(dbptr->bacnetDeviceInfo.bacObj.Object_Instance_Number, currentObject, object_instance);
									fprintf(stderr,
										"%s %-35s %6.2f   BACnet %s:%u\n",
										indent,
										currentObject->description, 
										aod->presentValue, 
										IntToText(objectTypeText, (int) bobjType),
										object_instance);
								}
								break;
							case OBJECT_BINARY_INPUT:
							case OBJECT_BINARY_VALUE:
								{
									BinaryObjectTypeDescriptor *bod = elf_get_binary_object_live_data(dbptr->bacnetDeviceInfo.bacObj.Object_Instance_Number, currentObject, object_instance);
									fprintf(stderr,
										"%s %-35s %6d   BACnet %s:%u\n",
										indent,
										currentObject->description, 
										bod->presentValue, 
										IntToText(objectTypeText, (int) bobjType),
										object_instance);
								}
								break;
							default:
								panic("Unhandled type %d", bobjType);
								break;
							}
						}
					}
				}
			}
			break;
		
#ifdef EM			
		case 'l':
			{
				if (iFloor < 0) iFloor = 0;
				if (iArea < 0) iArea = 0;
				if (iArea >= (int) g_energy_manager->floors[iFloor].num_sectors) iArea = 0;	
				
				if (g_energy_manager == NULL)
				{
					fprintf(stderr, "Error: g_energy_manager is NULL\n");
					return 0 ; 
				}
				if (g_energy_manager->num_floors < 1)
				{
					fprintf(stderr, "No floors established\n");
					return 0 ;
				}
				if (!g_energy_manager->floors[iFloor].s_sector[iArea].num_fixtures_in_area)
				{
					fprintf(stderr,
						"This Area does not have any fixtures\n");
					return 0;
				}
				if (++iFixture >= (int) g_energy_manager->floors[iFloor].s_sector[iArea].num_fixtures_in_area)
				{
					iFixture = 0;
				}
				s_fixture_t2 *thisFix = get_nth_fixture_ptr_for_area(g_energy_manager->floors[iFloor].s_sector[iArea].id, iFixture);
				fprintf(stderr,
					"\nFloorID %d, AreaID %d, FixtureID %d -------------------------- %s\n", 
					g_energy_manager->floors[iFloor].id,
					g_energy_manager->floors[iFloor].s_sector[iArea].id,
					thisFix->fixtureId,
					BACnet_Version);
				
				fprintf(stderr, "%s Fixture[%3d] ID:   %d\n", indent, iFixture, thisFix->fixtureId);
				fprintf(stderr, "%s              Name: %-50s\n", indent, thisFix->name);

				fprintf(stderr, "%s base_energy:                %6.2f\n", indent, thisFix->fixture_base_energy.presentValue);
				fprintf(stderr, "%s Used Energy:                %6.2f\n", indent, thisFix->fixture_used_energy.presentValue);
				fprintf(stderr, "%s Saved Energy:               %6.2f\n", indent, thisFix->fixture_saved_energy.presentValue);
				fprintf(stderr, "%s Ambient Saved Energy:       %6.2f\n", indent, thisFix->amb_saved_energy2.presentValue);
				fprintf(stderr, "%s Task Saved Energy:          %6.2f\n", indent, thisFix->task_saved_energy.presentValue);
				fprintf(stderr, "%s Manual Saved Energy:        %6.2f\n", indent, thisFix->manual_saved_energy.presentValue);
				fprintf(stderr, "%s Occupancy Saved Energy:     %6.2f\n", indent, thisFix->occ_saved_energy.presentValue);
				fprintf(stderr, "%s Fixture Dim Level:          %6.2f\n", indent, thisFix->dim_level2.analogTypeDescriptor.presentValue);
				if (elf_bacnet_config.fixtureAmbient)
				{
					fprintf(stderr, "%s Occupancy:                    %4d\n", indent, thisFix->occupancy.presentValue);
					fprintf(stderr, "%s Ambient Light Level:        %6.2f\n", indent, thisFix->ambient_light.presentValue);
				}
				fprintf(stderr, "\n");
			}
			break;

		case 'p':
			{
				if (iFloor < 0) iFloor = 0;
				if (iArea < 0) iArea = 0;
				
				if (g_energy_manager == NULL)
				{
					fprintf(stderr, "Error: g_energy_manager is NULL\n");
					return 0 ; 
				}
				if (g_energy_manager->num_floors < 1)
				{
					fprintf(stderr, "No floors established\n");
					return 0 ;
				}
				if (!g_energy_manager->floors[iFloor].s_sector[iArea].num_plugloads)
				{
					fprintf(stderr,
						"This Area does not have any Plugloads\n");
					return 0;
				}
				if (++iPlugload >= (int) g_energy_manager->floors[iFloor].s_sector[iArea].num_plugloads)
				{
					iPlugload = 0;
				}
				fprintf(stderr,
					"\nFloorID %u, AreaID %u, Plugload[%d/%d] ID %u ------------------------- %s\n", 
					g_energy_manager->floors[iFloor].id,
					g_energy_manager->floors[iFloor].s_sector[iArea].id,
					iPlugload+1,
					g_energy_manager->floors[iFloor].s_sector[iArea].num_plugloads,
					g_energy_manager->floors[iFloor].s_sector[iArea].plugLoads[iPlugload].id,
					BACnet_Version);
				
				fprintf(stderr, "%s Plugload[%3d] ID:   %u\n", indent, iPlugload, g_energy_manager->floors[iFloor].s_sector[iArea].plugLoads[iPlugload].id);
				fprintf(stderr, "%s               Name: %-50s\n", indent, g_energy_manager->floors[iFloor].s_sector[iArea].plugLoads[iPlugload].name);

				fprintf(stderr, "%s Total plug consumption:     %6.2f\n", indent, g_energy_manager->floors[iFloor].s_sector[iArea].plugLoads[iPlugload].plugTotalConsumption.presentValue);
				fprintf(stderr, "%s Managed plug consumption:   %6.2f\n", indent, g_energy_manager->floors[iFloor].s_sector[iArea].plugLoads[iPlugload].plugManagedConsumption.presentValue);
				fprintf(stderr, "%s Unmanaged plug consumption: %6.2f\n", indent, g_energy_manager->floors[iFloor].s_sector[iArea].plugLoads[iPlugload].plugUnManagedConsumption.presentValue);
				fprintf(stderr, "%s Fixture Plugload Status:      %4d\n", indent, g_energy_manager->floors[iFloor].s_sector[iArea].plugLoads[iPlugload].plugLoadStatus.binaryTypeDescriptor.presentValue);
				
				fprintf(stderr, "\n");
			}
			break;
#endif

#ifdef EM
		case 's':
			{
				iScene = -1;
				analogObjectIndexForScenes = 0;
				
				if (iFloor < 0) iFloor = 0;

				if (g_energy_manager == NULL)
				{
					fprintf(stderr, "Error: g_energy_manager is NULL\n");
					return 0 ; 
				}
				if (g_energy_manager->num_floors < 1)
				{
					fprintf(stderr, "No floors established\n");
					return 0 ;
				}
				if (!g_energy_manager->floors[iFloor].num_switches)
				{
					fprintf(stderr, "No switch groups for this floor\n");
					return 0 ;
				}
				if (++iSwitch >= (int) g_energy_manager->floors[iFloor].num_switches)
				{
					iSwitch = 0;
				}
	
				uint32_t object_instance;
				
				s_switchgroup_t *sw = &g_energy_manager->floors[iFloor].s_switch[iSwitch];
				elf_bacnet_db_t *dbptr = get_bacnet_db_ptr_by_switch_id(g_energy_manager->floors[iFloor].s_switch[iSwitch].id);
				if (dbptr == NULL)
				{
					fprintf(stderr, "Could not establish DB ptr switch ID %u\n", g_energy_manager->floors[iFloor].s_switch[iSwitch].id);
					break;
				}
				fprintf(stderr,
					"\nSwitch Group: FloorID:%2u, SwitchGroup[%d/%d] ID:%2u ========================= %s\n",
					g_energy_manager->floors[iFloor].id,
					iSwitch+1,
					g_energy_manager->floors[iFloor].num_switches,
					sw->id, 
					BACnet_Version);
				fprintf(stderr, "%s Switch Group ID:   %2u    BACnet Dev:%u\n", indent, sw->id, dbptr->bacnetDeviceInfo.bacObj.Object_Instance_Number);
				fprintf(stderr, "%s Switch Group Name: %-15s\n", indent, sw->switch_name);
				fprintf(stderr, "%s Number of Scenes:  %u\n", indent, sw->num_scenes);

				object_instance = elf_index_to_object_instance_new(dbptr->bacnetDeviceInfo.bacObj.Object_Instance_Number, 0, OBJECT_ANALOG_VALUE);
				ts_elf_template_object_t *currentObject = find_template_object_record_em(CATEGORY_SWITCH, OBJECT_ANALOG_VALUE, object_instance);
				if (!currentObject)
				{
					fprintf(stderr, "Could not establish switch Template Object for index 0\n");
				}
				else
				{
					fprintf(stderr, "%s Scene:           %6.2f   BACnet AV:%d   Desc:%s\n", indent, sw->scene.analogTypeDescriptor.presentValue, object_instance, currentObject->description);
				}
				// todo 4 - make a success return code... and use it
				object_instance = elf_index_to_object_instance_new(dbptr->bacnetDeviceInfo.bacObj.Object_Instance_Number, 1, OBJECT_ANALOG_VALUE);
				currentObject = find_template_object_record_em(CATEGORY_SWITCH, OBJECT_ANALOG_VALUE, object_instance);
				if (!currentObject)
				{
					fprintf(stderr, "Could not establish switchTemplate Object for index 1\n");
				}
				else
				{
					fprintf(stderr, "%s Dim Level:       %6.2f   BACnet AV:%u   Desc:%s\n", indent, sw->dimLevel.analogTypeDescriptor.presentValue, object_instance, currentObject->description);
				}
				fprintf(stderr, "\n");
			}
			break;
#endif

#ifdef EM
			// Scenes
		case 'c':
			{
				if (iFloor < 0) iFloor = 0;
				if (g_energy_manager == NULL)
				{
					fprintf(stderr, "Error: g_energy_manager is NULL\n");
					return 0 ; 
				}
				if (g_energy_manager->num_floors < 1)
				{
					fprintf(stderr, "No floors established\n");
					return 0 ;
				}
				if (!g_energy_manager->floors[iFloor].num_switches)
				{
					fprintf(stderr, "No switch groups for this floor\n");
					return 0 ;
				}
				if (!g_energy_manager->floors[iFloor].s_switch[iSwitch].num_scenes)
				{
					fprintf(stderr, "No scenes for this switch group\n");
					return 0 ;
				}
				if (++iScene >= (int) g_energy_manager->floors[iFloor].s_switch[iSwitch].num_scenes)
				{
					iScene = 0;
					analogObjectIndexForScenes = 0;
				}
	
				// uint32_t object_instance;
				
				s_scene_t *scene = &g_energy_manager->floors[iFloor].s_switch[iSwitch].s_scenes[iScene];
				elf_bacnet_db_t *dbptr = get_bacnet_db_ptr_by_switch_id(g_energy_manager->floors[iFloor].s_switch[iSwitch].id);
				if (dbptr == NULL)
				{
					fprintf(stderr, "Could not establish DB ptr switch ID %u\n", g_energy_manager->floors[iFloor].s_switch[iSwitch].id);
					break;
				}
				fprintf(stderr,
					"\nScene: FloorID:%2u, SwitchGroup[%d/%d] Scene [%d/%d] ID:%2u ========================= %s\n",
					g_energy_manager->floors[iFloor].id,
					iSwitch+1,
					g_energy_manager->floors[iFloor].num_switches,
					iScene+1,
					g_energy_manager->floors[iFloor].s_switch[iSwitch].num_scenes,
					scene->sceneId, 
					BACnet_Version);
				fprintf(stderr, "%s Scene ID:    %2u\n", indent, scene->sceneId);
				fprintf(stderr, "%s Switch ID:   %2u\n", indent, scene->switchId);
				
				fprintf(stderr, "%s Scene Name:  %-15s\n", indent, scene->sceneName);
				
				uint ll;
				char tname[MX_NAME];
				for (ll = 0; ll < scene->num_lightlevels; ll++)
				{
					uint32_t objectInstance = elf_index_to_object_instance_new(dbptr->bacnetDeviceInfo.bacObj.Object_Instance_Number, analogObjectIndexForScenes, OBJECT_ANALOG_INPUT);
					fprintf(stderr,
						"%s    ID:%d SwitchId:%d SceneId:%d FixtureId:%d Lightlevel:%d  AI:%07u\n",
						indent, 
						scene->s_lightlevels[ll].id,
						scene->s_lightlevels[ll].switchId,
						scene->s_lightlevels[ll].sceneId,
						scene->s_lightlevels[ll].fixtureId,
						scene->s_lightlevels[ll].lightLevel,
						objectInstance);

					fprintf(stderr,
						"%s%s%s BACnet Name: %s\n",
						indent,
						indent,
						indent,
						elf_get_object_name_for_specific_device(dbptr->bacnetDeviceInfo.bacObj.Object_Instance_Number, OBJECT_ANALOG_INPUT, objectInstance, tname));
					analogObjectIndexForScenes++;
				}
				for (ll = 0; ll < scene->num_pluglevels; ll++)
				{
					uint32_t objectInstance = elf_index_to_object_instance_new(dbptr->bacnetDeviceInfo.bacObj.Object_Instance_Number, analogObjectIndexForScenes, OBJECT_ANALOG_INPUT);
					fprintf(stderr,
						"%s    ID:%d SwitchId:%d SceneId:%d PlugloadId:%d Lightlevel:%d  AI:%07u\n",
						indent, 
						scene->s_pluglevels[ll].id,
						scene->s_pluglevels[ll].switchId,
						scene->s_pluglevels[ll].sceneId,
						scene->s_pluglevels[ll].plugloadId,
						scene->s_pluglevels[ll].plugLevel,
						objectInstance);
					
					fprintf(stderr,
						"%s%s%s BACnet Name: %s\n",
						indent,
						indent,
						indent,
						elf_get_object_name_for_specific_device(dbptr->bacnetDeviceInfo.bacObj.Object_Instance_Number, OBJECT_ANALOG_INPUT, objectInstance, tname));
					
					analogObjectIndexForScenes++;
				}
				
				
				
				//object_instance = elf_index_to_object_instance_new(dbptr->bacnetDeviceInfo.bacObj.Object_Instance_Number, 0, OBJECT_ANALOG_VALUE);
				//ts_elf_template_object_t *currentObject = find_template_object_record_em(CATEGORY_SWITCH, OBJECT_ANALOG_VALUE, object_instance);
				//if (!currentObject)
				//{
					//fprintf(stderr, "Could not establish switch Template Object for index 0\n");
				//}
				//else
				//{
					//fprintf(stderr, "%s Scene:           %6.2f   BACnet AV:%d   Desc:%s\n", indent, sw->scene.analogTypeDescriptor.presentValue, object_instance, currentObject->description);
				//}
				//// todo 4 - make a success return code... and use it
				//object_instance = elf_index_to_object_instance_new(dbptr->bacnetDeviceInfo.bacObj.Object_Instance_Number, 1, OBJECT_ANALOG_VALUE);
				//currentObject = find_template_object_record_em(CATEGORY_SWITCH, OBJECT_ANALOG_VALUE, object_instance);
				//if (!currentObject)
				//{
					//fprintf(stderr, "Could not establish switchTemplate Object for index 1\n");
				//}
				//else
				//{
					//fprintf(stderr, "%s Dim Level:       %6.2f   BACnet AV:%u   Desc:%s\n", indent, sw->dimLevel.analogTypeDescriptor.presentValue, object_instance, currentObject->description);
				//}
				fprintf(stderr, "\n");
			}
			break;
			
			
#endif

		case 'd':
			{
				unsigned int i;
				// todo 4 - name and desc end up being the same. consolidate??
				fprintf(stderr, "BACnet 'Database'\nBACnetID,  FloorID,  AreaID,  Type, %20s %20s\n", "Desc", "Name");
				for (i = 0; i < g_bacnet_device_count; i++)
				{
					elf_bacnet_db_t *ptr = &g_bacnet_device_list[i];
					fprintf(stderr,
						"%8u %9u %8u %6d  %20s %20s\n",
						ptr->bacnetDeviceInfo.bacObj.Object_Instance_Number,
						ptr->floor_id,
						ptr->sector_id,
						ptr->elf_dev_type,
						ptr->bacnetDeviceInfo.Description,
						ptr->bacnetDeviceInfo.bacObj.Object_Name2);
						// todo 4 ptr->state);
				}
			}
			break;

		case 't':
			{
				int i;
				fprintf(stderr, "Elf Objects\n");
				for (i = 0; i < MAX_OBJECTS + 1; i++)
				{
				    // todonext - there must be a better way of limiting this table...
					if (elf_template_objs[i].description[0])
					{

						fprintf(
						        stderr,
							"BACtype:%2d, objInstance:%2d, EnlDevType:%2d, elfDataType:%d Desc:%s\n",
							elf_template_objs[i].bacnet_obj_type,
							elf_template_objs[i].objectInstance,
#ifdef EM
							elf_template_objs[i].elfCategory,
#else
							0,
#endif
							elf_template_objs[i].elfDataType,
							elf_template_objs[i].description);
					}
				}
			}
			break;

		case 'h':
		default:
#ifdef EM		
			fprintf(stderr, "\nType: Q)uit D)atabase R)efresh Scan T)emplate_Objects H)elp\n      E)nergyManager\n      F)loors, A)reas L)ighting_Fixtures P)lugloads\n               S)witchGroups, S(c)enes\n");
#endif
#ifdef UEM		
			fprintf(stderr, "\nType: Q)uit D)atabase F)loors, A)reas T)emplate_Objects\n");
#endif
			
			break;
		}
	}

	return 0;
}
