#include <sys/ioctl.h>
#include <net/if.h> 
#include <unistd.h>
#include <netinet/in.h>
#include <string.h>
#include <sys/types.h>
#include <syslog.h>
#include <pwd.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>		//lint !e451
#include <time.h>		//lint !e451

#include "elf.h"
#include "elf_gems_api.h"
#include "elf_std.h"
#include "advdebug.h"


extern int g_log_level, g_interactive_log_level;
extern ts_elf_template_object_t *elf_template_objs;
extern bool interactiveExplore;


ts_elf_template_object_t *find_template_object_record_em(e_elf_category_t category, BACNET_OBJECT_TYPE bacnet_object_type, uint32_t objectInstance)
{
	unsigned int i;
	ts_elf_template_object_t *optr = (ts_elf_template_object_t *) elf_template_objs;

    // now search the template table
	for (i = 0; i < ux_template_objs; i++, optr++)
	{
		if (optr->bacnet_obj_type == bacnet_object_type &&
		                optr->elfCategory == category &&
		                optr->objectInstance == objectInstance)
		{
			return optr;
		}
	}
	return NULL;
}


static e_elf_category_t get_category_from_elf_device( e_elf_device_t devType, BACNET_OBJECT_TYPE objectType, uint32_t bacnetObjectInstance)
{
	switch (devType)
	{
	case ELF_DEVICE_EM:
#ifdef EM
		switch (GET_SUBCAT_FROM_INSTANCE(bacnetObjectInstance))
		{
		case SUBCAT_FIXTURE:
			return CATEGORY_FIXTURE;
		default:
			break;
		}
#endif // EM
		return CATEGORY_EM;
		
#ifdef EM
	case ELF_DEVICE_SWITCH:
		switch (objectType)
		{
		case OBJECT_ANALOG_VALUE:
			return CATEGORY_SWITCH;
		case OBJECT_ANALOG_INPUT:
			switch (GET_SCENE_SUBCAT_FROM_INSTANCE(bacnetObjectInstance))
			{
			case SUBCAT_FIXTURE:
				return CATEGORY_SWITCH_SCENE_FIXTURE;
			case SUBCAT_PLUGLOAD:
				return CATEGORY_SWITCH_SCENE_PLUGLOAD;
			default:
				panic("not applicable");
				return CATEGORY_SWITCH ;
			}
		default:
			// todo 5 BTL - we get here if someone tries to e.g. read a non-existing BI. For BTL we must definitely respond with 
			// no such object rather than a dummy value.
			return CATEGORY_UNKNOWN;
		}
#endif // EM
		
	case ELF_DEVICE_AREA:
#ifdef EM
		{
			switch (GET_SUBCAT_FROM_INSTANCE(bacnetObjectInstance))
			{
			case SUBCAT_FIXTURE:
				return CATEGORY_FIXTURE;
			case SUBCAT_PLUGLOAD:
				return CATEGORY_AREA_PLUGLOAD;
			default:
				return CATEGORY_AREA;
			}
		}
#else // UEM
		return CATEGORY_AREA;
#endif // EM/UEM

	default:
		panic("Why?");
		return CATEGORY_UNKNOWN ;
	}
}


static ts_elf_template_object_t *find_template_object_record_specific_device( uint32_t bacnetDeviceInstance, BACNET_OBJECT_TYPE bacnet_object_type, uint32_t objectInstance )
{
	elf_bacnet_db_t *elfdev = elf_get_bacnet_db_ptr_specific_device( bacnetDeviceInstance );
	if (elfdev == NULL)
	{
		panic("null");
		return NULL ;
	}
	e_elf_category_t category = get_category_from_elf_device(elfdev->elf_dev_type, bacnet_object_type, objectInstance);
	if (category == CATEGORY_UNKNOWN)
	{
		panic("unknown category");
		return NULL;
	}

#ifdef EM	
	switch (category)
	{
	case CATEGORY_FIXTURE:
	case CATEGORY_AREA_PLUGLOAD:
		objectInstance = GET_ADDER_FROM_INSTANCE(objectInstance);
		break;
	default:
		// the other categories can use the base objectInstance just fine.
		break;
	}
#endif // EM

	return find_template_object_record_em(category, bacnet_object_type, objectInstance) ;
}


#ifdef EM
static 	ts_elf_template_object_t	hardCodedScenePlugloadTemplate = { 
	OBJECT_ANALOG_INPUT,
	"Scene Dim Leval",
	UNITS_PERCENT,
	0,
	ELF_SWITCH_SCENE_PLUGLOAD,
	true,								// read-only?
	CATEGORY_SWITCH_SCENE_PLUGLOAD
};

static 	ts_elf_template_object_t	hardCodedSceneDimlevelTemplate = { 
	OBJECT_ANALOG_INPUT,
	"Scene Dim Level",
	UNITS_PERCENT,
	0,
	ELF_SWITCH_SCENE_DIM,
	true,								// read-only?
	CATEGORY_SWITCH_SCENE_FIXTURE
};
#endif // EM


ts_elf_template_object_t *find_template_object_record_current_device(BACNET_OBJECT_TYPE bacnet_object_type, uint32_t objectInstance)
{
	elf_bacnet_db_t *elfdev = elf_get_bacnet_db_ptr_current_device();
	e_elf_category_t category = get_category_from_elf_device( elfdev->elf_dev_type, bacnet_object_type, objectInstance);
	if (category == CATEGORY_UNKNOWN)
	{
		// panic("unknown category"); commented out to suppress
		return NULL;
	}

#ifdef EM
	switch (category)
	{
	case CATEGORY_FIXTURE:
	case CATEGORY_AREA_PLUGLOAD:
		objectInstance = GET_ADDER_FROM_INSTANCE(objectInstance);
		break;
	case CATEGORY_SWITCH_SCENE_FIXTURE:
		return &hardCodedSceneDimlevelTemplate;
	case CATEGORY_SWITCH_SCENE_PLUGLOAD:
		return &hardCodedScenePlugloadTemplate;
	default:
		// the other categories can use the base objectInstance just fine.
		break;
	}
#endif
	return find_template_object_record_em(category, bacnet_object_type, objectInstance) ;
}


#ifdef UEM
ts_elf_template_object_t *find_template_object_record(BACNET_OBJECT_TYPE bacnet_object_type, uint32_t objectInstance)
{
	unsigned int i;
	ts_elf_template_object_t *optr = (ts_elf_template_object_t *) elf_template_objs;

    // now search the template table
	for (i = 0; i < ux_template_objs; i++, optr++)
	{
		if (optr->bacnet_obj_type == bacnet_object_type &&
		                optr->objectInstance == objectInstance)
		{
			return optr;
		}
	}
	return NULL;
}
#endif // EM


bool is_object_instance_valid(uint32_t instance, BACNET_OBJECT_TYPE bacnet_object_type)
{

#ifdef EM
	// todo 4 - we refind this pointer inside called functions, optimize
	elf_bacnet_db_t *elfdev = elf_get_bacnet_db_ptr_current_device();

	switch (elfdev->elf_dev_type)
	{
	case ELF_DEVICE_SWITCH:
		// todo 2  - need to fix this...
		return true;
	default:
		// do it the 'old' way (below)
		break;
	}
#endif // EM

	return (find_template_object_record_current_device(bacnet_object_type, instance) != NULL) ? true : false ;
}


//char *elf_convert_mac2str(uint8_t *mac, char *str_value)
//{
    //uint8_t ln;
    //uint8_t hn;
    //uint8_t i, value;
//
    //for (i = 0; i < 6; i++)
    //{
        //value = mac[i];
        //ln = value & 0x0F;
//
        //if (ln >= 0 && ln <= 9)
        //{
            //str_value[i * 2 + 1] = ln + '0';
        //}
        //else if (ln >= 10 && ln <= 15)
        //{
            //str_value[i * 2 + 1] = ln - 10 + 'a';
        //}
//
        //hn = ((value >> 4) & 0x0F);
//
        //if (hn >= 0 && hn <= 9)
        //{
            //str_value[i * 2 + 0] = hn + '0';
        //}
        //else if (hn >= 10 && hn <= 15)
        //{
            //str_value[i * 2 + 0] = hn - 10 + 'a';
        //}
    //}
//
    //str_value[12] = 0;
//
    //return str_value;
//}

//void elf_set_device_count(unsigned int count)
//{
    //elf_device_count = count;
//}


//unsigned int elf_get_device_count()
//{
    //return elf_device_count;
//}

//int elf_create_bacnet_devices(void)
//{
    //if (Devices)
    //{
        //free(Devices);
    //}
//
	//log_printf(LOG_INFO, "Max number of device=%d", g_bacnet_device_count);
	//Devices = (DEVICE_OBJECT_DATA *) calloc(g_bacnet_device_count, sizeof(DEVICE_OBJECT_DATA));
    //if (!Devices)
    //{
        //log_printf(LOG_CRIT, "%s:%d - Error allocating memory for bacnet devices", __FUNCTION__, __LINE__);
        //return -1;
    //}
//
    //return 0;
//}


const char *elf_get_version(void)
{
	return ELF_VERSION; // this is coming from Makefile
}

int elf_get_mac_address(unsigned char mac[])
{
	struct ifreq ifr ;
	struct ifconf ifc;
	char buf[1024];
	bool success = false;

	int sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_IP);
	if (sock == -1)
	{
		return 1;
	}

	ifc.ifc_len = sizeof(buf);
	ifc.ifc_buf = buf;
	if (ioctl(sock, SIOCGIFCONF, &ifc) == -1)
	{
		return 2;
	}

	struct ifreq* it = ifc.ifc_req;
	struct ifreq* end = it + ((unsigned int) ifc.ifc_len / sizeof(struct ifreq));
	char *interface = (char *) elf_get_db_interface();
	
	for (; it != end; ++it)
	{
		if (strcmp(it->ifr_name, interface))
		{
			continue;
		}

		strcpy(ifr.ifr_name, it->ifr_name);
		if (ioctl(sock, SIOCGIFFLAGS, &ifr) == 0)
		{
			if (!(ifr.ifr_flags & IFF_LOOPBACK))
			{ // don't count loopback
				if (ioctl(sock, SIOCGIFHWADDR, &ifr) == 0)
				{
					success = true;
					break;
				}
			}
		}
	}

	close(sock);

	if (success)
	{
		//lint -e{772}
		memcpy(mac, ifr.ifr_hwaddr.sa_data, 6);
	}
	else
	{
		FILE *fp;
		char mac_buf[32];
		char cmd_buf[128];
		snprintf(cmd_buf, sizeof(cmd_buf), "ifconfig %s | head -1 | awk -F' ' '{print $5}'", interface);
		//fp = popen("ifconfig eth0 | head -1 | awk -F' ' '{print $5}'", "r");
		fp = popen(cmd_buf, "r");
		if (fp)
		{
			memset(mac_buf, 0, sizeof(mac_buf));
			fread(mac_buf, sizeof(uint8_t), sizeof(mac_buf), fp);
			if (mac_buf[0] != 0)
			{
				sscanf(
				        mac_buf,
					"%02x:%02x:%02x:%02x:%02x:%02x",
					(unsigned int *) &ifr.ifr_hwaddr.sa_data[0],
					(unsigned int *) &ifr.ifr_hwaddr.sa_data[1],
					(unsigned int *) &ifr.ifr_hwaddr.sa_data[2],
					(unsigned int *) &ifr.ifr_hwaddr.sa_data[3],
					(unsigned int *) &ifr.ifr_hwaddr.sa_data[4],
					(unsigned int *) &ifr.ifr_hwaddr.sa_data[5]);
				memcpy(mac, ifr.ifr_hwaddr.sa_data, 6);
				fclose(fp);
				return 0;
			}
			fclose(fp);
		}
		return 3;
	}

	return 0;
}


static void UpdateName(char *targetString, const char token, const char *insertString) 
{
	unsigned int tPtr;
	char tailCopy[MX_NAME];
	// todo 3 need safe string handling !!!!!!
	strcpy(tailCopy, targetString);
	
	// look for the token in the tailCopy
	for (tPtr = 0; tPtr < strlen(tailCopy); tPtr++)
	{
		if (tailCopy[tPtr] == '%' && tailCopy[tPtr + 1] == token)
		{
			// we have found our location
			// insert our new string
			strcpy(&targetString[tPtr], insertString);
			// and append the remains of the tail
			strcat(targetString, &tailCopy[tPtr + 2]);
			return ;
		}
	}
	// else, never found, just return with original string in place.
}


static void UpdateNameUInt(char *targetString, const char token, const uint value ) 
{
	char tbuf[MX_NAME];
	// todo 3 need safe string handling !!!!!!
	sprintf(tbuf, "%u", value);
	UpdateName(targetString, token, tbuf);
}


#ifdef EM
static const char *object_name_for_em_object(uint32_t bacnetDeviceInstance, BACNET_OBJECT_TYPE object_type, uint32_t object_instance, char *name)
{
	ts_elf_template_object_t *templatePtr = find_template_object_record_specific_device(bacnetDeviceInstance, object_type, object_instance);
	if (!templatePtr)
	{
		panic("why?");
		return name;
	}

	const char *fmt = elf_get_object_name_format_string(CATEGORY_EM);
	if (!fmt)
	{
		panic("why?");
		return name;
	}
	// todo 4 replace strcpy with safe strcpy (or equivalent)
	strcpy(name, fmt);
	s_energy_manager_t *tem = get_energy_manager_ptr();
	if (!tem)
	{
		panic("Null");
		return name ; 
	}
	if (tem->num_floors > 0)
	{
		UpdateName(name, 'E', tem->em_name);
		UpdateName(name, 'D', templatePtr->description);
		UpdateName(name, 'P', templatePtr->description);		// todo 4 resolve GUI configurator issue
	}
	else
	{
		UpdateName(name, 'E', "Unconfigured");
		UpdateName(name, 'D', "Unconfigured");
	}
	return name ;
}
#endif

#ifdef EM
static char *DeviceNameForSwitch(char *name)
{
	uint32_t id = elf_get_current_bacnet_device_instance();
	
	s_switchgroup_t *ptr = get_switch_ptr(id);
	if (!ptr)
	{
		panic("Could not get switch ptr?");
		strcpy(name, "Failed to get switch details");
		return name ;
	}
	
	strcpy(name, elf_get_device_name_format_string(ELF_DEVICE_SWITCH));

	UpdateName(name, 'C', ptr->floorPtr->company_name);
	UpdateName(name, 'c', ptr->floorPtr->campus_name);
	UpdateName(name, 'B', ptr->floorPtr->bldg_name);
	UpdateName(name, 'F', ptr->floorPtr->floor_name); 
	UpdateName(name, 'S', ptr->switch_name);

	UpdateNameUInt(name, 'f', ptr->floorPtr->id); 
	UpdateNameUInt(name, 's', ptr->id ); 
	
	log_printf(LOG_INFO, "Device name = %s", name);

	return name;
}
#endif


const char *elf_get_device_object_name(elf_bacnet_db_t *dbptr, char *name, int name_len)
{
	// todo 4 names are to be writeable (BACnet).
	
	switch (dbptr->elf_dev_type)
	{
	case ELF_DEVICE_EM:
		{
			strcpy(name, elf_get_device_name_format_string(dbptr->elf_dev_type));
			s_energy_manager_t *tem = get_energy_manager_ptr();
			if (!tem)
			{
				panic("why?");
				strcpy(name, "Failed to get name");
				return name ;
			}
			UpdateName(name, 'E', tem->em_name);
		}
		return name; 
		
#ifdef EM
	case ELF_DEVICE_SWITCH:
		return DeviceNameForSwitch(name) ;
#endif // EM
		
	case ELF_DEVICE_AREA:
		break;
	default:
		panic("Why?");
		strcpy(name, "Error");
		return name ;
	}
	
	s_sector_t *ptr = get_sector_ptr_by_instance(dbptr->sector_id);
	if (!ptr)
	{
		panic("why?");
		strcpy(name, "Failed to get name");
		return name ;
	}
	
	// Zone / Area formatting
	strcpy(name, elf_get_device_name_format_string(dbptr->elf_dev_type));

	UpdateName(name, 'C', ptr->floorPtr->company_name);
	UpdateName(name, 'c', ptr->floorPtr->campus_name);
	UpdateName(name, 'B', ptr->floorPtr->bldg_name);
	UpdateName(name, 'F', ptr->floorPtr->floor_name);
	UpdateName(name, 'Z', ptr->za_name);
	UpdateName(name, 'A', ptr->za_name);
	
	UpdateNameUInt(name, 'f', ptr->floorPtr->id );
	UpdateNameUInt(name, 'z', ptr->id);
	UpdateNameUInt(name, 'a', ptr->id);

	log_printf(LOG_INFO, "Device name = %s", name);

	return name;
}


#ifdef EM
static const char *object_name_for_switch_object(uint32_t bacnetDeviceInstance, BACNET_OBJECT_TYPE object_type, uint32_t object_instance, char *name)
{
	e_elf_category_t category = get_category_from_elf_device(ELF_DEVICE_SWITCH, object_type, object_instance);
	if (category == CATEGORY_UNKNOWN)
	{
		panic("unknown category");
		return name ;
	}
	
	const char *fmt = elf_get_object_name_format_string(category);
	s_switchgroup_t *tes = get_switch_ptr(bacnetDeviceInstance);

	strcpy(name, fmt);
	if (tes)
	{
		UpdateName(name, 'C', tes->floorPtr->company_name);
		UpdateName(name, 'c', tes->floorPtr->campus_name);
		UpdateName(name, 'B', tes->floorPtr->bldg_name);
		UpdateName(name, 'F', tes->floorPtr->floor_name);
		UpdateName(name, 'S', tes->switch_name);
		
		UpdateNameUInt(name, 'f', tes->floorPtr->id );
		UpdateNameUInt(name, 's', tes->id);
	}
	
	// some final touches
	switch (category)
	{
	case CATEGORY_SWITCH:
		{
			// todo 3 - surely this could be done for all names, in the calling function 
			ts_elf_template_object_t *templatePtr = find_template_object_record_specific_device(bacnetDeviceInstance, object_type, object_instance);
			if (!templatePtr)
			{
				panic("why?");
				return name;
			}
			UpdateName(name, 'D', templatePtr->description);
			UpdateName(name, 'P', templatePtr->description);		// todo 4 resolve GUI configurator issue
			
		}
		break;
		
	case CATEGORY_SWITCH_SCENE_FIXTURE:
		{
			s_scenelightlevel_t *lightlevel = get_switch_scene_fixture_ptr(bacnetDeviceInstance, object_instance);
			s_scene_t *scene = get_switch_scene_ptr(bacnetDeviceInstance, object_instance);
			if (lightlevel == NULL || scene == NULL)
			{
				panic("Null");
				return name ;
			}
			UpdateName(name, 'E', scene->sceneName);
			UpdateName(name, 'I', "NA" );
			UpdateNameUInt(name, 'e', scene->sceneId);
			UpdateNameUInt(name, 'i', lightlevel->fixtureId);
		}
		break;
		
	case CATEGORY_SWITCH_SCENE_PLUGLOAD:
		{
			s_pluglevel_t *pluglevel = get_switch_scene_plugload_ptr(bacnetDeviceInstance, object_instance);
			s_scene_t *scene = get_switch_scene_ptr(bacnetDeviceInstance, object_instance);
			if (pluglevel == NULL || scene == NULL )
			{
				panic("Null");
				return name ;
			}
			UpdateName(name, 'E', scene->sceneName);
			UpdateNameUInt(name, 'e', scene->sceneId);
			UpdateNameUInt(name, 'p', pluglevel->plugloadId );
			UpdateName(name, 'P', "NA" );
		}
		break;
		
	default:
		// all cool
		break;
	}
	
	return name ;
}
#endif // EM


static const char *object_name_for_area_object( uint32_t bacnetDeviceInstance, BACNET_OBJECT_TYPE object_type, uint32_t object_instance, char *name)
{
	ts_elf_template_object_t *templatePtr = find_template_object_record_specific_device(bacnetDeviceInstance, object_type, object_instance);
	if (!templatePtr)
	{
		panic("why?");
		return name;
	}

#ifdef EM
	const char *fmt = (const char *) elf_get_object_name_format_string(templatePtr->elfCategory);
#else
	const char *fmt = (const char *) elf_get_object_name_format_string();
#endif // EM/UEM

	s_sector_t *zptr = get_sector_ptr_by_instance(bacnetDeviceInstance);

	if (!zptr) 
	{
		panic("Why?");
		return name ;
	}
	if (!fmt)
	{
		panic("Why?");
		return name ;
	}
	
	strcpy(name, fmt);

	UpdateName(name, 'C', zptr->floorPtr->company_name);
	UpdateName(name, 'c', zptr->floorPtr->campus_name);
	UpdateName(name, 'B', zptr->floorPtr->bldg_name);
	UpdateName(name, 'F', zptr->floorPtr->floor_name);
	UpdateName(name, 'Z', zptr->za_name);
	UpdateName(name, 'A', zptr->za_name);
	UpdateName(name, 'D', templatePtr->description);
	UpdateName(name, 'P', templatePtr->description);		// todo 4 resolve GUI configurator issue
	
	UpdateNameUInt(name, 'a', zptr->id);
	UpdateNameUInt(name, 'z', zptr->id);
	
#ifdef EM
	elf_bacnet_db_t *dbDev = elf_get_bacnet_db_ptr_specific_device(bacnetDeviceInstance);
	if (dbDev == NULL)
	{
		panic("Null");
		return name;
	}
	
	switch (get_category_from_elf_device(dbDev->elf_dev_type, object_type, object_instance))
	{
	case CATEGORY_FIXTURE:
		{
			s_fixture_t2 *fixture = get_area_fixture_ptr(object_instance);
			if (fixture)
			{
				uint8_t mac[30];
				mac[0] = fixture->mac_address[0];
				mac[1] = fixture->mac_address[1];
				mac[2] = fixture->mac_address[2];

				char mac_str[9];
				snprintf(mac_str, sizeof(mac_str), "%02hhX:%02hhX:%02hhX", mac[0], mac[1], mac[2]);
				UpdateName(name, 'M', mac_str);
				UpdateNameUInt(name, 'i', fixture->fixtureId);
			}
			else
			{
				panic("Error");
				UpdateName(name, 'M', "Err");
			}
		}
		break;
		    
	case CATEGORY_AREA_PLUGLOAD:
		{
			s_plugload_t *pl = get_area_plugload_ptr(bacnetDeviceInstance, object_instance);
			if (pl)
			{
				char mac_str[30];
				snprintf(mac_str, sizeof(mac_str), "%02hhX:%02hhX:%02hhX", pl->mac_address[0], pl->mac_address[1], pl->mac_address[2]);
				UpdateName(name, 'M', mac_str);
				UpdateNameUInt(name, 'p', pl->id);
				}
			else
			{
				panic("Error");
				UpdateName(name, 'M', "Err");
			}
		}
		break;
		
	case CATEGORY_AREA:
		// all done already
		break;
		    
	default:
		panic("Error");
		UpdateName(name, 'M', "Err");
	}
#endif // EM			    

	log_printf(LOG_DEBUG, "Object name = %s", name);

	return name;

}


const char *elf_get_object_name_for_specific_device(uint32_t bacnetDeviceInstance, BACNET_OBJECT_TYPE obj_type, uint32_t object_instance, char *name)
{
	// const char *fmt; 
	sprintf(name, "%s", "unknown");
	
	elf_bacnet_db_t *dbDev = elf_get_bacnet_db_ptr_specific_device(bacnetDeviceInstance);
	if (dbDev == NULL)
	{
		panic("Null");
		return name;
	}
		
	switch (dbDev->elf_dev_type)
	{
		
#ifdef EM	// (UEM devices don't have any points...)		
	case ELF_DEVICE_EM:
		return object_name_for_em_object(bacnetDeviceInstance, obj_type, object_instance, name) ;
		
	case ELF_DEVICE_SWITCH:
		return object_name_for_switch_object(bacnetDeviceInstance, obj_type, object_instance, name) ;
#endif
		
	case ELF_DEVICE_AREA :
		return object_name_for_area_object(bacnetDeviceInstance, obj_type, object_instance, name) ;
		
	default:
		panic("Why?");
		return name;
	}
}


const char *elf_get_object_name_for_current_device(BACNET_OBJECT_TYPE obj_type, uint32_t object_instance, char *name)
{
	return elf_get_object_name_for_specific_device(elf_get_current_bacnet_device_instance(), obj_type, object_instance, name) ;
}


#define PROCESS_USERNAME    "enlighted"

int elf_get_my_id(const char *name, int *uid, int *gid)
{
    struct passwd *pwd ;
    if (name == NULL)
    {
        name = PROCESS_USERNAME;
    }
    pwd = getpwnam(name);
    if (!pwd)
    {
        return -1;
    }

    *uid = pwd->pw_uid;
    *gid = pwd->pw_gid;

    return 0;
}


void log_printf(int level, const char *fmt, ...)
{
	//lint -esym(530,ap)
	va_list ap;			
	
	if (level <= g_log_level)
	{
		va_start(ap, fmt);					
		vsyslog(level, fmt, ap);
		va_end(ap);
	}
	
	if ( interactiveExplore && level <= g_interactive_log_level)
	{
		va_start(ap, fmt);
		vfprintf(stderr, fmt, ap);
		fprintf(stderr, "\n");
		va_end(ap);
	}
}

