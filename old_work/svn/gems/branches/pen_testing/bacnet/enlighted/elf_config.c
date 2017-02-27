#include <stdlib.h>
#include <ctype.h>
#include <syslog.h>
#include "elf.h"
#include "elf_std.h"
#include "elf_config.h"
#include "advdebug.h"

elf_config_t elf_bacnet_config;

int g_log_level = LOG_ERR;
int g_interactive_log_level = LOG_NOTICE;

typedef struct log_levels
{
	const char *name;
	int level;
} s_log_levels_t;

static const s_log_levels_t s_log_levels[] = {
{
	"alert",
	LOG_ALERT
},
{
	"crit",
	LOG_CRIT
},
{
	"debug",
	LOG_DEBUG
},
{
	"emerg",
	LOG_EMERG
},
{
	"err",
	LOG_ERR
},
{
	"info",
	LOG_INFO
},
{
	"notice",
	LOG_NOTICE
},
{
	"warning",
	LOG_WARNING
},
{
	NULL,
	-1
}
};

#define NUM_LOG_LEVELS (sizeof(s_log_levels)/sizeof(s_log_levels[0]))

static int get_log_level(const char *log_level)
{
	unsigned int i;

	for (i = 0; i < NUM_LOG_LEVELS; i++)
	{
		if (s_log_levels[i].level != -1)
		{
			if (!strcasecmp(s_log_levels[i].name, log_level))
			{
				return s_log_levels[i].level;
			}
		}
	}

	return -1;
}

int elf_read_config_file(const char *config_file)
{
	char buffer[256], *attr = NULL, *value = NULL;
	FILE *fp = fopen(config_file, "r");
	char *ptr = NULL;

	if (fp == NULL)
	{
		log_printf(LOG_CRIT, "Error opening config file %s", config_file);
		return -1;
	}
	else
	{
		while (fgets(buffer, sizeof(buffer), fp))
		{
			if (buffer[0] != '#' && buffer[0] != '\n' && buffer[0] != '\r')
			{
				buffer[strlen(buffer) - 1] = 0;
				ptr = strchr(buffer, '=');
				if (ptr == NULL)
				{
					ptr = strchr(buffer, ':');
				}
				if (ptr)
				{
					*ptr = 0;
					attr = buffer;
					value = ptr + 1;
				}
				if (!value) continue ;

				if (!strcasecmp(attr, "VendorId"))
				{
					elf_bacnet_config.vendor_id = (uint16_t) atoi(value);
					log_printf(LOG_INFO, "Vendor Id = %d", elf_bacnet_config.vendor_id);
				}
				if (!strcasecmp(attr, "APDUTimeout"))
				{
				    // Timeout is in milli-seconds
					elf_bacnet_config.apdu_timeout = atoi(value);
					log_printf(LOG_INFO, "APDU Timeout = %d", elf_bacnet_config.apdu_timeout);
				}
#ifdef EM				
				else if (!strcasecmp(attr, "EnergyManagerInstance") || !strcasecmp(attr, "EnergyManagerBaseInstance"))
				{
					elf_bacnet_config.em_base_instance = atoi(value);
					if (elf_bacnet_config.em_base_instance > 4194302)
					{
						log_printf(LOG_ERR, "BACnet Instances must be limited to below 4194303");
					}					
					log_printf(LOG_INFO, " %s = %d", attr, elf_bacnet_config.em_base_instance);
				}
				else if (!strcasecmp(attr, "SwitchGroupBaseInstance"))
				{
					elf_bacnet_config.switch_base_instance2 = atoi(value);
					if (elf_bacnet_config.switch_base_instance2 > 4000000)
					{
						log_printf(LOG_ERR, "BACnet Base Instances must be limited to below 4000000");
					}
					log_printf(LOG_INFO, " %s = %d", attr, elf_bacnet_config.switch_base_instance2);
				}
#else // UEM
				else if (!strcasecmp(attr, "HVACManagerInstance") )
				{
					elf_bacnet_config.em_base_instance = atoi(value);
					if (elf_bacnet_config.em_base_instance > 4194302)
					{
						log_printf(LOG_ERR, "BACnet Instances must be limited to below 4194303");
					}					
					log_printf(LOG_INFO, " %s = %d", attr, elf_bacnet_config.em_base_instance);
				}
#endif // EM/UEM
				
				else if (!strcasecmp(attr, "DeviceBaseInstance") || !strcasecmp(attr, "AreaBaseInstance") || !strcasecmp(attr, "ZoneBaseInstance"))
				{
					elf_bacnet_config.sector_base_instance = atoi(value);
					if (elf_bacnet_config.sector_base_instance > 4000000)
					{
						log_printf(LOG_ERR, "BACnet Base Instances must be limited to below 4000000");
					}
					log_printf(LOG_INFO, " %s = %d", attr, elf_bacnet_config.sector_base_instance);
				}
				else if (!strcasecmp(attr, "ListenPort"))
				{
					elf_bacnet_config.listen_port = atoi(value);
					log_printf(LOG_INFO, "Server Port = %d", elf_bacnet_config.listen_port);
				}
				else if (!strcasecmp(attr, "MaxAPDU"))
				{
					elf_bacnet_config.max_apdu = atol(value);
					log_printf(LOG_INFO, "Max APDU Length= %d", elf_bacnet_config.max_apdu);
				}
				else if (!strcasecmp(attr, "NetworkId") || !strcasecmp(attr, "NetworkNumber") )
				{
					elf_bacnet_config.network_id = atoi(value);
					log_printf(LOG_INFO, "Network Id = %d", elf_bacnet_config.network_id);
				}
				else if (!strcasecmp(attr, "MaxObjects"))
				{
					elf_bacnet_config.max_objects = atoi(value);
					log_printf(LOG_INFO, "Max Objects = %d", elf_bacnet_config.max_objects);
				}
				else if (!strcasecmp(attr, "Version"))
				{
					elf_bacnet_config.version = atoi(value);
					log_printf(LOG_INFO, "Version = %d", elf_bacnet_config.version);
				}
				else if (!strcasecmp(attr, "DBFile"))
				{
					memset(elf_bacnet_config.db_file, 0, sizeof(elf_bacnet_config.db_file));
					strcpy(elf_bacnet_config.db_file, value);
					log_printf(LOG_INFO, "Bacnet DB File = %s", elf_bacnet_config.db_file);
				}
				else if (!strcasecmp(attr, "GemsIpAddress"))
				{
					strcpy((char *) elf_bacnet_config.gems_ip_address, value);
					log_printf(LOG_INFO, "GEMS IP Address = %s", elf_bacnet_config.gems_ip_address);
				}
				else if (!strcasecmp(attr, "UseProxy"))
				{
					strcpy((char *) elf_bacnet_config.proxy_ip_address, value);
					log_printf(LOG_INFO, "Proxy IP Address = %s", elf_bacnet_config.proxy_ip_address);
				}
				else if (!strcasecmp(attr, "Interface"))
				{
					strcpy((char *) elf_bacnet_config.interface, value);
					log_printf(LOG_INFO, "Bacnet Interface = %s", elf_bacnet_config.interface);
				}
				else if (!strcasecmp(attr, "ObjectsFile"))
				{
					strcpy((char *) elf_bacnet_config.objects_file, value);
					log_printf(LOG_INFO, "Bacnet Objects Files = %s", elf_bacnet_config.objects_file);
				}
				else if (!strcasecmp(attr, "DimDelayResponse"))
				{
					elf_bacnet_config.dim_delay_response = atol(value);
					log_printf(LOG_INFO, "Dim Delay Response = %d", elf_bacnet_config.dim_delay_response);
				}
				else if (!strcasecmp(attr, "UpdateAreaTimeout") || !strcasecmp(attr, "UpdateZoneTimeout"))
				{
					elf_bacnet_config.update_sector_timeout = atoi(value);
					log_printf(LOG_INFO, "Update Sector Timeout = %d", elf_bacnet_config.update_sector_timeout);
				}
				else if (!strcasecmp(attr, "UpdateConfigTimeout"))
				{
					elf_bacnet_config.update_config_timeout = atoi(value);
					log_printf(LOG_INFO, "Update Config Timeout = %d", elf_bacnet_config.update_config_timeout);
				}
#ifdef EM                
				else if (!strcasecmp(attr, "UpdateOccupancyTimeout"))
				{
					elf_bacnet_config.update_occupancy_timeout = atoi(value);
					log_printf(LOG_INFO, "Update Occupancy Timeout = %d", elf_bacnet_config.update_occupancy_timeout);
				}
#endif                
				else if (!strcasecmp(attr, "IgnoreOwnBcastPackets"))
				{
					elf_bacnet_config.ignore_own_bcast_packets = atoi(value);
					log_printf(LOG_INFO, "Ignore Own Bcast Packets = %d", elf_bacnet_config.ignore_own_bcast_packets);
				}
				else if (!strcasecmp(attr, "IamDelay"))
				{
					elf_bacnet_config.i_am_delay = atoi(value);
					log_printf(LOG_INFO, "I Am Delay = %d", elf_bacnet_config.i_am_delay);
				}
				
#ifdef UEM				
				else if (!strcasecmp(attr, "DeviceNameFormatString")  )
				{
					strcpy((char *) elf_bacnet_config.device_name_fmt_str_sector,
						value);
				}
				else if (!strcasecmp(attr, "ObjectNameFormatString"))
				{
					strcpy((char *) elf_bacnet_config.object_name_fmt_str_sector,
						value);
				}
				else if (!strcasecmp(attr, "HVACManagerName"))
				{
					strcpy((char *) elf_bacnet_config.energyManagerName,
						value);
				}
#else
				else if (!strcasecmp(attr, "DeviceNameFormatStringArea") )
				{
					strcpy((char *) elf_bacnet_config.device_name_fmt_str_sector,
						value);
				}
				else if (!strcasecmp(attr, "ObjectNameFormatStringArea"))
				{
					strcpy((char *) elf_bacnet_config.object_name_fmt_str_sector,
						value);
				}
				else if (!strcasecmp(attr, "DeviceNameFormatStringSwitchGroup"))
				{
					strcpy((char *) elf_bacnet_config.device_name_fmt_str_switch,
						value);
				}
				else if (!strcasecmp(attr, "DeviceNameFormatStringEM"))
				{
					strcpy((char *) elf_bacnet_config.device_name_fmt_str_em,
						value);
				}
				else if (!strcasecmp(attr, "ObjectNameFormatStringFixture"))
				{
					strcpy((char *) elf_bacnet_config.object_name_fmt_str_fixture,
						value);
				}
				else if (!strcasecmp(attr, "ObjectNameFormatStringPlugload"))
				{
					strcpy((char *) elf_bacnet_config.object_name_fmt_str_plugload,
						value);
				}
				else if (!strcasecmp(attr, "ObjectNameFormatStringSwitchGroup"))
				{
					strcpy((char *) elf_bacnet_config.object_name_fmt_str_switch,
						value);
				}
				else if (!strcasecmp(attr, "EnergyManagerName"))
				{
					strcpy((char *) elf_bacnet_config.energyManagerName,
						value);
				}
				else if (!strcasecmp(attr, "ObjectNameFormatStringEM"))
				{
					strcpy((char *) elf_bacnet_config.object_name_fmt_str_em,
						value);
				}
				else if (!strcasecmp(attr, "ObjectNameFormatStringSwitchScenePluglevel"))
				{
					strcpy((char *) elf_bacnet_config.object_name_fmt_str_scene_pluglevel,
						value);
				}
				else if (!strcasecmp(attr, "ObjectNameFormatStringSwitchSceneDimLevel"))
				{
					strcpy((char *) elf_bacnet_config.object_name_fmt_str_scene_dimlevel,
						value);
				}
#endif // UEM/EM


#ifdef UEM
				else if (!strcasecmp(attr, "Username"))
				{
					strcpy(elf_bacnet_config.user_name,
						value);
					log_printf(LOG_INFO,
						"Username= %s",
						elf_bacnet_config.user_name);
				}
				else if (!strcasecmp(attr, "Password"))
				{
					strcpy(elf_bacnet_config.password,
						value);
					log_printf(LOG_INFO,
						"Password= %s",
						elf_bacnet_config.password);
				}
#endif
				
#ifdef EM 
				// todo 4 - are we ready for API keys on UEM yet?
				else if (!strcasecmp(attr, "RestApiKey"))
				{
					strcpy((char *) elf_bacnet_config.rest_api_key, value);
					log_printf(LOG_INFO, "Rest API Key= %s", elf_bacnet_config.rest_api_key);
				}
				else if (!strcasecmp(attr, "RestApiSecret"))
				{
					strcpy((char *) elf_bacnet_config.rest_api_secret, value);
					log_printf(LOG_INFO, "Rest API Secret= %s", elf_bacnet_config.rest_api_secret);
				}
#endif				
				else if (!strcasecmp(attr, "LogLevel"))
				{
					// strcpy((char *) elf_bacnet_config.log_level, value);
					g_log_level = get_log_level(value);
					log_printf(LOG_INFO, "Log Level= %s (%d)", value, g_log_level);
				}
				else if (!strcasecmp(attr, "InteractiveLogLevel"))
				{
					// strcpy((char *) elf_bacnet_config.g_interactive_log_level, value);
					g_interactive_log_level  = get_log_level(value);
					log_printf(LOG_INFO, "Interactive Log Level= %s(%d)", value, g_interactive_log_level);
				}
#ifdef EM
				else if (!strcasecmp(attr, "DetailedMode"))
				{
					if ( toupper(value[0]) == 'T')
					{
						elf_bacnet_config.detailedMode = true;
						log_printf(LOG_NOTICE, "System in Detailed Mode");
					}
					else 
					{
						log_printf(LOG_NOTICE, "System in Normal Mode");
					}
				}
				else if (!strcasecmp(attr, "fixtureOccupancySensor"))
				{
					if (toupper(value[0]) == 'E' || toupper(value[0]) == 'T' )
					{
						elf_bacnet_config.fixtureAmbient = true;
						log_printf(LOG_NOTICE, "System in Fixture Occupancy/Ambient Light mode");
					}
				}
#endif
				else
				{
					log_printf(LOG_ERR, "Unknown attribute [%s] in file [%s]", attr, config_file);
				}
			}
		}

		fclose(fp);
	}

	return 0;
}


const char *elf_get_config_string(ELF_CONFIG_TYPES cfg_type)
{
	switch (cfg_type)
	{
	case CFG_EM_NAME:
		return elf_bacnet_config.energyManagerName;
	default:
		break;
	}
	panic("Unknown %d", cfg_type);
	return "Unknown" ;
}
	
int elf_get_config(int cfg_type)
{
	switch (cfg_type)
	{
	case CFG_APDU_TIMEOUT:
		return elf_bacnet_config.apdu_timeout;
	case CFG_SECTOR_BASE_INSTANCE:
		return (int) elf_bacnet_config.sector_base_instance;
#ifdef EM
	case CFG_SWITCH_BASE_INSTANCE2:
		return (int) elf_bacnet_config.switch_base_instance2;
#endif
	case CFG_EM_BASE_INSTANCE:
		return (int) elf_bacnet_config.em_base_instance;
	case CFG_LISTEN_PORT:
		return elf_bacnet_config.listen_port;
	case CFG_MAX_APDU:
		return elf_bacnet_config.max_apdu;
	case CFG_NETWORK_ID:
		return elf_bacnet_config.network_id;
	case CFG_MSG_TIMEOUT:
		return elf_bacnet_config.msg_timeout;
	case CFG_MAX_OBJECTS:
		return elf_bacnet_config.max_objects;
	case CFG_VERSION:
		return elf_bacnet_config.version;
	case CFG_DB_FILE:
	    /* Use elf_get_db_file_config() function */
		break;
	case CFG_GEMS_IP_ADDRESS:
	    /* Use elf_get_gems_ip_address_config() function */
		break;
	case CFG_DIM_DELAY_RESPONSE:
		return (int) elf_bacnet_config.dim_delay_response;
	case CFG_UPDATE_CONFIG_TIMEOUT:
		return elf_bacnet_config.update_config_timeout;
	case CFG_UPDATE_SECTOR_TIMEOUT:
		return elf_bacnet_config.update_sector_timeout;
#ifdef EM
	case CFG_UPDATE_OCCUPANCY_TIMEOUT:
		return elf_bacnet_config.update_occupancy_timeout;
#endif            
	case CFG_IGNORE_OWN_BCAST_PACKETS:
		return elf_bacnet_config.ignore_own_bcast_packets;
	case CFG_I_AM_DELAY:
		return elf_bacnet_config.i_am_delay;
	default:
		break;
	}

	return -1;
}

const char *elf_get_db_file_config(void)
{
	return elf_bacnet_config.db_file;
}

const char *elf_get_db_gems_ip_address_config(void)
{
	return elf_bacnet_config.gems_ip_address;
}

const char *elf_get_db_proxy_ip_address_config(void)
{
	return elf_bacnet_config.proxy_ip_address;
}

const char *elf_get_db_interface(void)
{
	return elf_bacnet_config.interface;
}

const char *elf_get_objects_file(void)
{
	return elf_bacnet_config.objects_file;
}

//#ifdef UEM
//const char *elf_get_device_name_format_string(void)
//{
	//return (char *) elf_bacnet_config.device_name_fmt_str_sector ;
//}
//#endif // UEM

const char *elf_get_device_name_format_string(e_elf_device_t devType)
{
	switch (devType)
	{
	case ELF_DEVICE_AREA:
		return elf_bacnet_config.device_name_fmt_str_sector ;
#ifdef EM		
	case ELF_DEVICE_SWITCH:
		return elf_bacnet_config.device_name_fmt_str_switch ;
#endif
	case ELF_DEVICE_EM:
		return elf_bacnet_config.device_name_fmt_str_em;
	default:
		panic("Why?");
		break;
	}
	return "" ;
}


#ifdef UEM
const char *elf_get_object_name_format_string(void)
{
	return elf_bacnet_config.object_name_fmt_str_sector;
}
#endif

#ifdef EM
const char *elf_get_object_name_format_string(e_elf_category_t elfCategory)
{
	switch (elfCategory)
	{
	case CATEGORY_AREA:
		return elf_bacnet_config.object_name_fmt_str_sector;
	case CATEGORY_FIXTURE:
		return elf_bacnet_config.object_name_fmt_str_fixture;
	case CATEGORY_AREA_PLUGLOAD:
		return elf_bacnet_config.object_name_fmt_str_plugload; 
	case CATEGORY_SWITCH:
		return elf_bacnet_config.object_name_fmt_str_switch;
	case CATEGORY_EM:
		return elf_bacnet_config.object_name_fmt_str_em;
	case CATEGORY_SWITCH_SCENE_PLUGLOAD:
		return elf_bacnet_config.object_name_fmt_str_scene_pluglevel;
	case CATEGORY_SWITCH_SCENE_FIXTURE:
		return elf_bacnet_config.object_name_fmt_str_scene_dimlevel ;
	default:
		panic("Illegal type");
		break;
	}
	return "" ;
}
#endif


#ifdef UEM
const char *elf_get_rest_username(void)
{
	return (char *)elf_bacnet_config.user_name;
}

const char *elf_get_rest_password(void)
{
	return (char *)elf_bacnet_config.password;
}
#endif

#ifdef EM
const char *elf_get_rest_api_key(void)
{
	return (char *) elf_bacnet_config.rest_api_key;
}

const char *elf_get_rest_api_secret(void)
{
	return (char *) elf_bacnet_config.rest_api_secret;
}
#endif // EM


void elf_init_config(void)
{
	memset(&elf_bacnet_config, 0, sizeof(elf_bacnet_config));
	elf_bacnet_config.apdu_timeout = DEFAULT_APDU_TIMEOUT;
	elf_bacnet_config.vendor_id = DEFAULT_VENDOR_ID;
	elf_bacnet_config.sector_base_instance = DEFAULT_SECTOR_BASE_INSTANCE;
	elf_bacnet_config.em_base_instance = DEFAULT_EM_BASE_INSTANCE;
#ifdef EM
	elf_bacnet_config.switch_base_instance2 = DEFAULT_SWITCH_BASE_INSTANCE2;
#endif
	elf_bacnet_config.listen_port = DEFAULT_LISTEN_PORT;
	elf_bacnet_config.max_apdu = DEFAULT_MAX_APDU;
	elf_bacnet_config.network_id = DEFAULT_NETWORK_ID;
	elf_bacnet_config.msg_timeout = DEFAULT_GEMS_MSG_TIMEOUT;
	elf_bacnet_config.max_objects = DEFAULT_MAX_OBJECTS;
	elf_bacnet_config.version = DEFAULT_VERSION;
	strcpy(elf_bacnet_config.db_file, DEFAULT_DB_FILE);
	memset(elf_bacnet_config.proxy_ip_address, 0, sizeof(elf_bacnet_config.proxy_ip_address));
	memset(elf_bacnet_config.gems_ip_address, 0, sizeof(elf_bacnet_config.gems_ip_address));
	strcpy((char *) elf_bacnet_config.gems_ip_address, DEFAULT_GEMS_IP_ADDRESS);
	strcpy((char *) elf_bacnet_config.interface, DEFAULT_INTERFACE);
	strcpy((char *) elf_bacnet_config.objects_file, DEFAULT_OBJECTS_FILE);
	elf_bacnet_config.dim_delay_response = DEFAULT_DIM_DELAY_RESPONSE;
	elf_bacnet_config.update_config_timeout = DEFAULT_UPDATE_CONFIG_TIMEOUT;
	elf_bacnet_config.update_sector_timeout = DEFAULT_UPDATE_SECTOR_TIMEOUT;
#ifdef EM    
	elf_bacnet_config.update_occupancy_timeout = DEFAULT_UPDATE_OCCUPANCY_TIMEOUT;
#endif

	elf_bacnet_config.ignore_own_bcast_packets = DEFAULT_IGNORE_OWN_BCAST_PACKETS;
	elf_bacnet_config.i_am_delay = DEFAULT_I_AM_DELAY;

	strcpy(elf_bacnet_config.device_name_fmt_str_em, DEFAULT_DEVICE_NAME_FMT_EM);

#ifdef UEM
	strcpy(elf_bacnet_config.device_name_fmt_str_sector, DEFAULT_DEVICE_NAME_FMT_STR);
#else
	strcpy((char *) elf_bacnet_config.device_name_fmt_str_sector, DEFAULT_DEVICE_NAME_FMT_AREA);
	strcpy((char *) elf_bacnet_config.device_name_fmt_str_switch, DEFAULT_DEVICE_NAME_FMT_SWITCH);

	strcpy((char *) elf_bacnet_config.object_name_fmt_str_sector, DEFAULT_OBJECT_NAME_FMT_AREA);
	strcpy((char *) elf_bacnet_config.object_name_fmt_str_fixture, DEFAULT_OBJECT_NAME_FMT_AREA_FIXTURE);
	strcpy((char *) elf_bacnet_config.object_name_fmt_str_plugload, DEFAULT_OBJECT_NAME_FMT_AREA_PLUGLOAD);
	strcpy((char *) elf_bacnet_config.object_name_fmt_str_switch, DEFAULT_OBJECT_NAME_FMT_SWITCH);
	strcpy((char *) elf_bacnet_config.object_name_fmt_str_em, DEFAULT_OBJECT_NAME_FMT_EM);
	strcpy((char *) elf_bacnet_config.object_name_fmt_str_scene_dimlevel, DEFAULT_OBJECT_NAME_FMT_SCENE_DIMLEVEL);
	strcpy((char *) elf_bacnet_config.object_name_fmt_str_scene_pluglevel, DEFAULT_OBJECT_NAME_FMT_SCENE_PLUGLEVEL);
#endif
	
	strcpy((char *) elf_bacnet_config.energyManagerName, DEFAULT_ENERGY_MANAGER_NAME);

#ifdef UEM
	strcpy(elf_bacnet_config.user_name,
		DEFAULT_REST_USERNAME);
	strcpy(elf_bacnet_config.password,
		DEFAULT_REST_PASSWORD);
#endif

#ifdef EM	
	strcpy((char *) elf_bacnet_config.rest_api_key, DEFAULT_REST_API_APIKEY);
	strcpy((char *) elf_bacnet_config.rest_api_secret, DEFAULT_REST_API_SECRET);
#endif // EM
	
	elf_bacnet_config.tmpFilePath = TMP_FILES_PATH2;
}
