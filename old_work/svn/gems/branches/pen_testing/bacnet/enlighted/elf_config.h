#ifndef __ELF_CONFIG_H__
#define __ELF_CONFIG_H__

#include "elf_std.h"

#define MX_CONFIG_STRING    128


#define DEFAULT_VENDOR_ID                (516)      /* Read only (Enlighted's BACnet Vendor ID is 516 */
#define DEFAULT_APDU_TIMEOUT             (10*1000)  /* Milli-Seconds */
#define DEFAULT_EM_BASE_INSTANCE         (700000)
#define DEFAULT_SECTOR_BASE_INSTANCE     (800000)
#ifdef EM
#define DEFAULT_SWITCH_BASE_INSTANCE2    (900000)
#endif
#define DEFAULT_LISTEN_PORT              (47808)
#define DEFAULT_MAX_APDU                 (1476)     /* Bytes */
#define DEFAULT_NETWORK_ID               (9999)
#define DEFAULT_GEMS_MSG_TIMEOUT         (20)       /* Seconds */
#define DEFAULT_MAX_OBJECTS              (40)
#define DEFAULT_VERSION                  (4)
#define DEFAULT_DB_FILE                  ("/var/lib/bacnet/bacnet.db")
#define DEFAULT_GEMS_IP_ADDRESS          ("127.0.0.1")
#define DEFAULT_INTERFACE                ("eth0")
#define DEFAULT_OBJECTS_FILE             ("/var/lib/bacnet/bacnet_objects.cfg")

#define DEFAULT_DIM_DELAY_RESPONSE       (1000)     // milli-seconds

#define DEFAULT_UPDATE_CONFIG_TIMEOUT    (30*60)    // Seconds
#define DEFAULT_UPDATE_SECTOR_TIMEOUT    (5*60)     // Seconds

#ifdef EM
#define DEFAULT_UPDATE_OCCUPANCY_TIMEOUT (1*60)     // Seconds
#endif

#define DEFAULT_IGNORE_OWN_BCAST_PACKETS (1)     // Ignore own bcast packets
#define DEFAULT_I_AM_DELAY               (5)     // milli-seconds

/*
# In all format strings the following applies:
#	E	Energy Manager Name
#	C	Company Name
#	c	Campus Name
#	B	Building Name
#	F	Floor Name
#	f	Floor Id
#	A	Area Name
#	a	Area Id
#	S	Switchgroup Name
#	s	Switchgroup Id
#	E	Scene Name
#	e	Scene Id
#   P	Plugload Name
#	p	Plugload Id
#   I	Fixture Name
#   i	Fixture Id
#	M	MAC address
#	D	Description
*/

#ifdef UEM
#define DEFAULT_DEVICE_NAME_FMT_STR     "COMP-%C/CAMP-%c/BLDG-%B/FL-%F/Z-%Z"
// #define DEFAULT_OBJECT_NAME_FMT_STR     "COMP-%C/CAMP-%c/BLDG-%B/FL-%F/Z-%Z/PT-%s"
#define DEFAULT_DEVICE_NAME_FMT_EM      "Enlighted HVAC Manager"
#define DEFAULT_ENERGY_MANAGER_NAME		"Unnamed HVAC Manager"
#else
#define DEFAULT_DEVICE_NAME_FMT_AREA    "COMP-%C/CAMP-%c/BLD-%s/FL-%F/AREA-%A"
#define DEFAULT_DEVICE_NAME_FMT_SWITCH  "COMP-%C/CAMP-%c/BLD-%s/FL-%F/SWITCH-%S"
#define DEFAULT_DEVICE_NAME_FMT_EM      "Enlighted Energy Manager"

#define DEFAULT_OBJECT_NAME_FMT_AREA			"COMP-%C/CAMP-%c/BLD-%B/FL-%F/A-%A/PT-%D"
#define DEFAULT_OBJECT_NAME_FMT_AREA_FIXTURE	"COMP-%C/CAMP-%c/BLD-%B/FL-%F/A-%A/FIX-%M/PT-%D"
#define DEFAULT_OBJECT_NAME_FMT_AREA_PLUGLOAD	"COMP-%C/CAMP-%c/BLD-%B/FL-%F/A-%A/PLUG-%M/PT-%D"
#define DEFAULT_OBJECT_NAME_FMT_SWITCH			"COMP-%C/CAMP-%c/BLD-%B/FL-%F/SW-%S/PT-%D"
#define DEFAULT_OBJECT_NAME_FMT_EM				"Energy Manager/PT-%D"
#define DEFAULT_ENERGY_MANAGER_NAME				"Unnamed Energy Manager"
#define DEFAULT_OBJECT_NAME_FMT_SCENE_DIMLEVEL	"COMP-%C/CAMP-%c/BLD-%B/FL-%f/SW-%s/SC-%e/F-%i"
#define DEFAULT_OBJECT_NAME_FMT_SCENE_PLUGLEVEL "COMP-%C/CAMP-%c/BLD-%B/FL-%F/SW-%s/SC-%e/PL-%p"
# endif

#ifdef UEM
#define DEFAULT_REST_USERNAME            "admin"
#define DEFAULT_REST_PASSWORD            "admin"
#else
#define DEFAULT_REST_API_APIKEY          "enlighted"
#define DEFAULT_REST_API_SECRET          "enlighted"
#endif

// #define DEFAULT_LOG_LEVEL                "info"
// #define DEFAULT_MODE                     (ZONE_ONLY_MODE)

typedef enum
{
//    CFG_VENDOR_ID = 516, // Read only
    CFG_APDU_TIMEOUT = 1,
    CFG_SECTOR_BASE_INSTANCE,
    CFG_EM_BASE_INSTANCE,
	CFG_EM_NAME,
#ifdef EM
    CFG_SWITCH_BASE_INSTANCE2,
#endif
    CFG_LISTEN_PORT,
    CFG_MAX_APDU,
    CFG_NETWORK_ID,
    CFG_MSG_TIMEOUT,
    CFG_MAX_OBJECTS,
    CFG_VERSION,
    CFG_DB_FILE,
    CFG_GEMS_IP_ADDRESS,
//    CFG_INTERFACE,
//    CFG_OBJECTS_FILE_PATH,
    CFG_DIM_DELAY_RESPONSE,

    CFG_UPDATE_CONFIG_TIMEOUT,
    CFG_UPDATE_SECTOR_TIMEOUT,
#ifdef EM
    CFG_UPDATE_OCCUPANCY_TIMEOUT,
#endif

    CFG_IGNORE_OWN_BCAST_PACKETS,
    CFG_I_AM_DELAY,
//    CFG_DEVICE_NAME_FMT_STR,
//    CFG_OBJECT_NAME_FMT_STR,
    // CFG_MODE,
} ELF_CONFIG_TYPES;

typedef struct elf_config
{
    uint16_t vendor_id;
    uint16_t apdu_timeout;
    uint32_t sector_base_instance;
#ifdef EM
    uint32_t switch_base_instance2;
#endif
	uint32_t em_base_instance;		// Used for Lightind
	uint16_t listen_port;
    uint16_t max_apdu;
    uint16_t network_id;
    uint16_t msg_timeout; // BACnet to GEMS UDP message timeout
    uint16_t max_objects;
    uint32_t dim_delay_response;
    uint8_t  version;
    char     db_file[256];
    char     proxy_ip_address[16];
    char     gems_ip_address[16];
    char     interface[9];
    char		objects_file[256];
	const char	*tmpFilePath;

    uint16_t update_config_timeout; // In seconds
    uint16_t update_sector_timeout;    // In seconds
#ifdef EM
    uint16_t update_occupancy_timeout;    // In seconds
#endif

    uint8_t  ignore_own_bcast_packets;
    uint16_t i_am_delay; // In milli-seconds // todo 4 - we need to pace output

	char     device_name_fmt_str_em[MX_CONFIG_STRING];
	char     device_name_fmt_str_sector[MX_CONFIG_STRING];
	
#ifdef EM
    char     device_name_fmt_str_switch[MX_CONFIG_STRING];
#endif

    char    object_name_fmt_str_sector[MX_CONFIG_STRING];
	char	energyManagerName[MX_CONFIG_STRING];
	
#ifdef EM
    char    object_name_fmt_str_switch[MX_CONFIG_STRING];
    char    object_name_fmt_str_em[MX_CONFIG_STRING];
    char	object_name_fmt_str_fixture[MX_CONFIG_STRING];
	char	object_name_fmt_str_plugload[MX_CONFIG_STRING];
	char	object_name_fmt_str_scene_pluglevel[MX_CONFIG_STRING];
	char	object_name_fmt_str_scene_dimlevel[MX_CONFIG_STRING];
#endif

#ifdef UEM
    char	user_name[128];
    char	password[128];
#endif

#ifdef EM
    uint8_t         rest_api_key[128];
    uint8_t         rest_api_secret[128];
    bool			detailedMode;
	bool			fixtureAmbient;
#endif    

    // char            log_level[64];
} __attribute__ ((__packed__)) elf_config_t; 

#endif /* __ELF_CONFIG_H__ */

extern const char *elf_get_config_string(ELF_CONFIG_TYPES cfg_type);
