#include "elf.h"
#include <sys/time.h>
#include <setjmp.h>		// just to suppress a VisualGDB warning
#include <pthread.h>
#include <syslog.h>
#include <ctype.h>
#include <stdarg.h>

#include "elf_xml_parse.h"
#include "elf_gems_api.h"
#include "elf_std.h"
#include "elf_functions.h"
#include "bacdef.h"
#include "advdebug.h"
#include "elf_objects.h"
#include "elf_config.h"

#ifdef __cplusplus
extern "C" {
#include <roxml.h>
}
#else 
#include <roxml.h>
#endif

/*

	punchlist
	---------
	
	Coded read ADR, but API broken. still need to test. See: https://enlightedinc.atlassian.net/browse/EM-490
	Then test demandResponse write... 
	
	test set dim level again (if i beat sachin to it)
	test interface keyword
	test 127.0.0.1


	====== Future =============================================================================
	
	detect and report duplicate network number too

	improve command line tools
		Command line tool update to show unreliable point status.
		all devices (in range, all points, and dummy writes too!)
	
	create a min and max base address for devices, and check that the whole block is clear when detecting duplicate devices.
	
	There are still a few Lint (static code analysis tool) warnings, some of them quite suspicious

	the virtual router has to be able to pass BTL too.. there are some missing bits that I have to add to the stack for this to pass. This will be about a week or two of work at least. In parallel I will get the BTL registration going.
	out-of-service’ to make BTL approval faster and cheaper. Not a lot of work.

	I am holding off on the Java JNI until above is complete / under control.
	
	when writing dim level to a fixture, recalculate the average for an area.
	
	consider moving a sensor to a new area - 'insert' needs to search for existing and update
	
	bacnet db file 'could not be created' and crashes - make a plan when permissions too high. (e.g. err message, quit).
		discuss with Vasu
		also - what is the current intent with BACnet.db. Do we want to 'reallocate' devices still??
		bacnet.db needs to save many more items to be useful (floor, area etc. names).
		
	cli tools seg fault when no ID
	Should we do NN test before startup, device instance tests??? (looking for duplicates on the network).
	

	Aditya:
	Aditya tells me that some company details will never appear in XML in future, take care, test this case
	
	==========================================================================================================

	To Test
	-------
	
	Energy Manager
	--------------
	adr level
		when i attempted read - no data, perhaps because no level set?   - use list DR API - use pricelevel 1/2/3 high/moderate/low
		need to resolve 'never written' indication - 999??
		not sure how to structure xml payload for level set

	Area
	----
	read  - area occupancy
	read  - area fixture outage count

	Fixtures
	--------
	fixture outage
	
		
	Switch Groups
	-------------
	switch group scene
		write - Get: 415 Unsupported Media Type when trying from Adv Rest Client (ARC)
				- 2015.10.29 now when testing, often get a 400 error, other times a '3'


	Tested:
	=======	
	Energy Manager
	--------------
	em power consumed lighting
		aditya's doc and 3.5 show power, but confluence docs show energy. Need to resolve.
		coding for power for now.
		https://enlightedinc.atlassian.net/browse/EM-418
		tested with 'power', not energy
	em power consumed plug load
		aditya's doc and 3.5 show power, but confluence docs show energy. Need to resolve.
		https://enlightedinc.atlassian.net/browse/EM-418
		tested with 'power', not energy
	em emergency on
		read - API does not allow read. todo 4 - add request to Enlighted to add readback for confirmation, if nothing else.
		write - tested. There is only a way to set this in the API. Request a way to reset this too?
		
	Area
	----
	Area average dim level (and it does calculate average)
	write - area emergency. There is only a way to set this in the API. Request a way to reset this too?
	read  - area emergency - read - I don't think there is a way to read this, confirm. Add request to add to API
		
	Fixtures
	--------
	fixture power consumed
	Read - fixture dim level
	write - fixture dim level

	Plugloads
	---------
	read - plug load power consumed
		aditya's doc and 3.5 show power, but confluence docs show energy. Need to resolve.
		https://enlightedinc.atlassian.net/browse/EM-418
	write - plug load on/off
		Tested Suman's update 2015.10.29 - Seems OK


	Switch Groups
	-------------
	switch group dim
		cannot read. Create jira item, document here and at other bookmark. Can never read, hardware issue.
		write (and read-back) tests all the way throught to good xml response. Not tested in EM database - not sure where to see the parameter on UI
	switch group scene
		cannot read - ask Enlighted to add API


	Other - Done
	------------
		multi-thread
		making points “write only” – I have added this, and this works OK
		Reliability added to points.
1.	Switch Groups 
	1.	Adding Plug Loads
	2.	Dim Levels of the fixtures in scenes
4.	No Un-assigned Area
5.	1 Sec delay on API calls
6.	Removed use of BACnet DB

	setuid - discuss privilege handling with Vasu, and reimplement?
		Discussed, found to be ineffective, commented out.
		Update: root privileges had issues, had to reinstate this functionality

	test bacnet.db creation with new discovery process.. see also https://goo.gl/DJeUVO	
	
	cleaner error messages if API key not correct
	
	Detect and report (via new API), duplicate device instances on the network
	Duplicate device notification, done, but event API not working yet
	note: if the virtual router is a duplicate, NONE of the other devices can be allowed to communicate!
	
*/

typedef enum
{
	REQ_TYPE_POST = 1,
	REQ_TYPE_GET  = 2,
} REQ_TYPE ;


#ifdef EM
// todo 4 - this is spectaclularly redundant, resolve
typedef struct area_occ
{
	unsigned int id;
	// bool occupancy;
} s_area_occ_t;

#endif // EM

#ifdef UEM
typedef struct
{
	ELF_DATA_TYPE elfObjectType;
	const char *apiString;
	const char *xmlFilename;
} ApiStrings;

#define MX_BMS_API_STRINGS  4
ApiStrings apiStrings[MX_BMS_API_STRINGS] =
{
	{ ELF_BMS_SETPOINT_HIGH, "https://%s/uem/services/hvac/zone/%d/setpointhigh/%f/%ld", "bmsSetpointHigh" },
	{ ELF_BMS_SETPOINT_LOW, "https://%s/uem/services/hvac/zone/%d/setpointlow/%f/%ld", "bmsStpointLow" },
	{ ELF_BMS_SETPOINT, "https://%s/uem/services/hvac/zone/%d/setpoint/%f/%ld", "bmsSetpoint" },
	{ ELF_BMS_TEMPERATURE, "https://%s/uem/services/hvac/zone/%d/settemperature/%f/%ld", "bmsTemp" },
};
#endif // UEM

s_energy_manager_t *g_energy_manager;

//#ifdef EM
//extern ts_elf_template_object_t *elf_template_objs;
//#endif

extern bool muteFlag;
extern elf_config_t elf_bacnet_config;
// extern DEVICE_OBJECT_DATA *Devices;
extern ts_elf_template_object_t *elf_template_objs;
extern elf_bacnet_db_t *g_bacnet_device_list;

static const char* create_url_headers(char *header, int content_type, int len);

BACNET_OBJECT_TYPE elf_valid_bacnet_obj_types[MAX_BACNET_TYPES_PER_ELF_DEVICE] = {
	OBJECT_ANALOG_INPUT,
	OBJECT_BINARY_INPUT,
	OBJECT_ANALOG_VALUE,
	OBJECT_BINARY_VALUE,
};

static const char* create_url_headers(char *header, int content_type, int len)
{
#ifdef UEM
	int count = 0;
	count += snprintf(header,
		len,
		"-u %s:%s -c cookie.txt -H \"%s\"",
		elf_get_rest_username(),
		elf_get_rest_password(),
		"Accept: application/xml");
#else
	char ts[32];
	uint8_t auth_token[41];

	memset(auth_token, 0, sizeof(auth_token));

	if (muteFlag)
	{
		strcpy(ts, "1446600551");	
	}
	else
	{
		time_t now = time(NULL);
		snprintf(ts, sizeof(ts) - 1, "%lu", now);
	}
	
	get_auth_token((const char *) ts, (uint8_t *) auth_token);

	int count = 0;
	count += snprintf(
	        header,
		len,
		"-H \"%s %s\" -H \"%s %s\" -H \"%s %s\" -H \"%s\"",
		"ApiKey: ",
		elf_get_rest_api_key(),
		"Authorization: ",
		auth_token,
		"ts: ",
		ts,
		"Accept: application/xml");
#endif

	if (content_type)
	{
		count += snprintf(header + count, len - count, " -H \"%s\"", "Content-Type: application/xml");
	}
	return header;
}

#ifdef UEM
static const char* create_url_headers_write(char *header, int len)
{
#ifdef UEM
	int count = 0;
	count += snprintf(header,
		len,
		"-u %s:%s -c cookie.txt ",
		elf_get_rest_username(),
		elf_get_rest_password());
#else
	char ts[32];
	uint8_t auth_token[41];

	memset(auth_token, 0, sizeof(auth_token));

	time_t now = time(NULL);
	snprintf(ts, sizeof(ts) - 1, "%lu", now);
	get_auth_token(ts, auth_token);

	int count = 0;
	count += snprintf(header,
		len,
		"-H \"%s %s\" -H \"%s %s\" -H \"%s %s\" -H \"%s\"",
		"ApiKey: ",
		elf_get_rest_api_key(),
		"Authorization: ",
		auth_token,
		"ts: ",
		ts,
		"Accept: application/xml");
#endif

	count += snprintf(header + count,
		len - count,
		" -H \"%s\"",
		"Content-Type: application/xml");

	return header;
}
#endif // UEM

#ifdef UEM
static void send_curl_message_write(uint8_t req_type,
	const char *url,
	const char *ifile,
	const char *ofile)
{
	char cmd[1024];
	char req_headers[1024];
	char redirect_str[1024];
	// int  rc = 0;

	    // Setup request headers.
	create_url_headers_write(req_headers, sizeof(req_headers));

	snprintf(redirect_str, sizeof(redirect_str), " > %s", ofile);

    // remove any existing file.
	if (!muteFlag)
	{
		unlink(ofile);
	}

	    // setup curl command
	if (req_type == REQ_TYPE_POST)
	{
		sprintf(cmd,
			"curl -s %s -X POST -d @%s -k %s %s",
			req_headers,
			ifile,
			url,
			redirect_str);
	}
	else
	{
		sprintf(cmd,
			"curl -s --get %s -k %s %s",
			req_headers,
			url,
			redirect_str);
	}

	log_printf(LOG_INFO, "cmd=%s", cmd);

	if (system(cmd) < 0)
	{
		log_printf(LOG_CRIT,
			"In %s: can't execute command %s!\n",
			__func__,
			cmd);
		exit(EXIT_FAILURE);
	}
	// todo 2 - remove these sleeps when API CPU load issues resolved
	sleep(1);
}
#endif

static void trim(char * s) {
	char * p = s;
	int l = strlen(p);

	while (isspace(p[l - 1])) p[--l] = 0;
	while (* p && isspace(* p)) ++p, --l;

	memmove(s, p, l + 1);
}

static bool XmlSetup(void *xmlb, xml_parse_ctx_t *x, const char *name)
{
	x->xml_buf = (char *) xmlb;
	x->xml_xpath = name;
	int err = parse_xml(x);
	if (err)
	{
		// todo 4 - review this list
		if (strcmp(name, "html/head") == 0 ||
			strcmp(name, "areas/area") == 0 ||
			strcmp(name, "areas/area/id") == 0 ||		// todo 4 - in particular
			strcmp(name, "areas/area/name") == 0 ||		// todo 4 - in particular
			strcmp(name, "areaOutage/totalSensor") == 0 ||
			strcmp(name, "sensorEnergyStatss/sensorEnergyStats/sensor") == 0 ||
			strcmp(name, "gemsGroups/gemsGroup") == 0 ||
#ifdef EM
			strcmp(name, "fixtures/fixture") == 0 ||
			strcmp(name, "emEnergyConsumption/energy-plugload") == 0 ||
			strcmp(name, "plugloadEnergy/managed-energy") == 0 ||
			strcmp(name, "plugloadEnergy/unmanaged-energy") == 0 ||
			strcmp(name, "plugloads/plugload") == 0 ||
			strcmp(name, "dRTargets/drTarget/pricelevel") == 0 ||
			strcmp(name, "dRTargets/drTarget/dridentifier") == 0 ||
#else
			strcmp(name, "hVACZoneVOes/hvacZoneVO") == 0 ||
#endif
			strcmp(name, "response/status") == 0)
		{
			// these are 'expected' - dont report
		}
		else
		{
			log_printf(LOG_CRIT, "Could not read data from EM server, check authentication parameters");
		}
	}
	else 
	{
		// trim leading/trailing whitespace from strings
		int i;
		for (i = 0; i < x->num_results; i++)
		{
			trim(x->results[i]);
		}
	}
	
	return (err) ?
	        false :
	        true;
}

static void XmlCleanup(const xml_parse_ctx_t *x)
{
	free(x->results);
	roxml_release(RELEASE_ALL);
	roxml_close(x->root);
}


static char *XMLmoveIntoBuffer(const char *ofile)
{
	FILE *fp1 = fopen(ofile, "r");
	if (fp1)
	{
		struct stat st;
		if (!stat(ofile, &st))
		{
			if (st.st_size != 0)
			{
				char *xmlc = (char *) calloc(1, st.st_size + 1); // +1 for null termination
				if (!xmlc)
				{
					log_printf(LOG_CRIT, "In %s: can't allocate memory for xml data!\n", __func__);
					fclose(fp1);
					exit(EXIT_FAILURE);
				}

				int count = 0;
				while (!feof(fp1))
				{
					count += fread(xmlc + count, 1, 1, fp1);
				}
				fclose(fp1);
				return xmlc;
			}
			else
			{
				// I opened https://goo.gl/kNo5ft EM-506 to see if this should be logged...
				log_printf(LOG_CRIT, "Returned filesize for %s is zero", ofile);
			}
		}
		else
		{
			log_printf(LOG_CRIT, "In %s: can't stat file %s\n", __func__, ofile);
		}
		fclose(fp1);
	}
	else
	{
		panic("In %s: can't open file %s\n", __func__, ofile);
	}
	return NULL;
}


static ELF_RETURN send_curl_message(REQ_TYPE req_type, const char *url, const char *ifile, const char *ofile)
{
	char cmd[1024];
	char req_headers[1024];
	char redirect_str[1024];
	char proxy_str[1024];
	ELF_RETURN rc;

	    // Setup request headers.
	create_url_headers(req_headers, 1, sizeof(req_headers));

	if (ofile != NULL)
	{
		snprintf(redirect_str, sizeof(redirect_str), " > %s", ofile);
	}
	else
	{
	    // empty string
		snprintf(redirect_str, sizeof(redirect_str), " ");
	}

	if (strlen(elf_get_db_proxy_ip_address_config()) == 0)
	{
		snprintf(proxy_str, sizeof(proxy_str), " ");
	}
	else
	{
		snprintf(proxy_str, sizeof(proxy_str), "--proxy %s", elf_get_db_proxy_ip_address_config());
	}
	
	uint8_t attempts = 0;
	do
	{
		rc = ELF_RETURN_OK;
		
		log_printf(LOG_INFO, "%s:%d attempts=%d", __FUNCTION__, __LINE__, attempts);
		// remove any existing file.
		if (!muteFlag)
		{
			unlink(ofile);
		}

		        // setup curl command
		if (req_type == REQ_TYPE_POST)
		{
			if (ifile != NULL)
			{
				sprintf(cmd, "curl %s-s %s -X POST -d @%s -k %s %s", proxy_str, req_headers, ifile, url, redirect_str);
			}
			else
			{
				sprintf(cmd, "curl %s-s %s -X POST -k %s %s", proxy_str, req_headers, url, redirect_str);
			}
		}
		else
		{
			if (ofile != NULL)
			{
				sprintf(cmd, "curl %s -s --get %s -k %s %s", proxy_str, req_headers, url, redirect_str);
			}
			else
			{
				sprintf(cmd, "curl %s -s --get %s -o %s -k %s %s", proxy_str, req_headers, ofile, url, redirect_str);
			}
		}

		log_printf(LOG_INFO, "cmd=%s", cmd);
		
		if (muteFlag)
		{
			if (ofile)
			{
			// save the curl message to a temp file, based on ofile
				char tfile[256];
				strcpy(tfile, ofile);
				strtok(tfile, ".");
				strcat(tfile, ".txt");
			
				FILE *fp = fopen(tfile, "w");
				if (fp)
				{
					fprintf(fp, "Curl Cmd: %s", cmd);
					fclose(fp);
				}
			}
			else 
			{
				panic("wanted to use ofile, but it is null");
			}			
		}
		else 
		{
			// pace the calls for the sake of Tomcat
			// todo 3 - did not seem to help, check in with Vasu.... sleep(1);
			if (system(cmd) < 0)
			{
				log_printf(LOG_CRIT, "In %s: can't execute command %s!\n", __func__, cmd);
				exit(EXIT_FAILURE);
			}
			// todo 2 - remove these sleeps when API CPU load issues resolved
			sleep(1);
		}

		char *xmla = XMLmoveIntoBuffer(ofile);
		if (xmla == NULL)
		{
		// panic("why?"); // todo 4 - seems like a request to an area with no plugloads returns the wrong sort of response (204 - No Content) - not consistent with the reset
			// investigate and then reinstate this panic
			return ELF_RETURN_FAIL ;
		}
		int num_responses = 0;
		xml_parse_ctx_t x;

		if (XmlSetup(xmla, &x, "html/head"))
		{
			// a successful html/head is actually an error message, so we are actually going to fail the operation if this succeeds..
			num_responses = x.num_results;
			if (num_responses == 1)
			{
			    // we should not get <html><head>...</html>
				rc = ELF_RETURN_FAIL;
				// dont panic here, happens very often
				// todo 4 - resolve with aditya
				// log_printf(LOG_NOTICE, "html/head error response received");
			}
			XmlCleanup(&x);
		}
		
#if 0 
		// response/status is an OK response for get area emergency,,,,,
		// some messages send a status in their responses
		if (XmlSetup(xmla, &x, "response/status"))
		{
			// a successful html/head is actually an error message, so we are actually going to fail the operation if this succeeds..
			num_responses = x.num_results;
			if (num_responses != 1)
			{
				log_printf(LOG_DEBUG, "XML response .. too many parameters");
				rc = ELF_RETURN_FAIL;
			}
			else
			{
				if (atoi(x.results[0]) != 0)
				{
					log_printf(LOG_DEBUG, "XML response .. not 0");
					rc = ELF_RETURN_FAIL;
				}
			}
			XmlCleanup(&x);
		}
#endif		
		free(xmla);
		
		// give the server a break.
		if (rc != ELF_RETURN_OK)
		{
			sleep(1);
		}
		
	} while ((rc != ELF_RETURN_OK) && (attempts++ < 5));		// todo  5 - Make user configurable in bacnet.conf, will need the UI to change too

	
	// logging for diagnosing retry issues. Track in: https://enlightedinc.atlassian.net/browse/EM-461
	if (rc != ELF_RETURN_OK)
	{
		log_printf(LOG_NOTICE, "Transaction [%s] failed, see final response in file [%s]", url, ofile);
	}
	else if (attempts > 0)
	{
		log_printf(LOG_NOTICE, "Transaction [%s] took %d attempts", url, attempts + 1);
	}
	
	return rc;
}


static char *XMLgetIntoBuffer(REQ_TYPE reqType, const char *url, const char *ofile)
{
	if (send_curl_message(reqType, url, NULL, ofile) != ELF_RETURN_OK)
	{
		// todo 4 - investigate this for plugload fails...
		// panic("Failed to get XML data");
		return NULL;
	}
	return XMLmoveIntoBuffer(ofile) ;
}


s_energy_manager_t* get_energy_manager_ptr()
{
    // one day, with multiple energy managers, we will get more sophisticated here
	return g_energy_manager ;
}


#ifdef EM
static int compare_fixture_id_sort(const void *s1, const void *s2)
{
	const s_fixture_t2 *d1 = (s_fixture_t2 *) s1;
	const s_fixture_t2 *d2 = (s_fixture_t2 *) s2;

	return ((int) d1->fixtureId - (int) d2->fixtureId);
}

static int compare_fixture_id_search(const void *key, const void *s)
{
	const s_fixture_t2 *d = (const s_fixture_t2 *) s;

	int x = *(int *) key;
	int y = (int) d->fixtureId;

	return (x - y);
}

static int compare_plugload_id_sort(const void *s1, const void *s2)
{
	s_plugload_t *d1 = (s_plugload_t *) s1;
	s_plugload_t *d2 = (s_plugload_t *) s2;

	return ((int) d1->id - (int) d2->id);
}

static int compare_plugload_id_search(const void *key, const void *s)
{
	s_plugload_t *d = (s_plugload_t *) s;

	int x = *(int *) key;
	int y = (int) d->id;

	return (x - y);
}


s_switchgroup_t* get_switch_ptr(uint32_t deviceInstance)
{
	elf_bacnet_db_t *ptr = (elf_bacnet_db_t *) elf_get_bacnet_db_ptr_specific_device(deviceInstance);
	if (!ptr)
	{
		return NULL;
	}

	unsigned int i, j;
	for (i = 0; i < g_energy_manager->num_floors; i++)
	{
		if (ptr->floor_id != g_energy_manager->floors[i].id)
		{
			continue;
		}
		if (g_energy_manager->floors[i].s_switch == NULL)
		{
			panic("NULL");
			continue ;
		}
		for (j = 0; j < g_energy_manager->floors[i].num_switches; j++)
		{
			if (ptr->switch_id == g_energy_manager->floors[i].s_switch[j].id)
			{
			    // make sure we are pointing to our floor for reference
				g_energy_manager->floors[i].s_switch[j].floorPtr = &g_energy_manager->floors[i];
				return &g_energy_manager->floors[i].s_switch[j];
			}
		}
	}
	return NULL;
}


//static s_plugload_t* get_area_plugload_ptr(uint32_t deviceInstance, uint32_t objectInstance)
//{
	//elf_bacnet_db_t *ptr = (elf_bacnet_db_t *) elf_get_bacnet_db_ptr_specific_device(deviceInstance);
	//if (!ptr)
	//{
		//panic("null");
		//return NULL;
	//}
//
	//unsigned int i, j, k;
	//for (i = 0; i < g_energy_manager->num_floors; i++)
	//{
		//if (ptr->floor_id == g_energy_manager->floors[i].id)
		//{
			//for (j = 0; j < g_energy_manager->floors[i].num_sectors; j++)
			//{
				//if (g_energy_manager->floors[i].s_sector[j].id == ptr->zone_id3)
				//{
					//for (k = 0; k < g_energy_manager->floors[i].s_sector[j].num_plugloads; k++)
					//{
						//if (g_energy_manager->floors[i].s_sector[j].plugLoads[k].id == GET_PLUGLOAD_ID_FROM_INSTANCE(objectInstance))
						//{
							//return &g_energy_manager->floors[i].s_sector[j].plugLoads[k];
						//}
							//
					//}
				//}
			//}
		//}
	//}
	//panic("null");
	//return NULL;
//}


 #ifdef EM
static void get_individual_plugload_consumption(s_plugload_t *plugLoad)   // todo, with new api to read plugload status, this call is redundant. remove..
{
	xml_parse_ctx_t x;
	char url[1024];
	char ofile[256];
	
	snprintf(url, sizeof(url), GET_INDIVIDUAL_PLUGLOAD_CONSUMPTION_API, elf_get_db_gems_ip_address_config(), plugLoad->id);
	snprintf(ofile, sizeof(ofile), "%s/%s_%d.%s", elf_bacnet_config.tmpFilePath, GET_INDIVIDUAL_PLUGLOAD_CONSUMPTION_XML, plugLoad->id, TMP_FILE_PREFIX);

	char *xmlc = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (!xmlc)
		return;

	if (XmlSetup(xmlc, &x, "plugloadEnergy/managed-energy"))
	{
		StoreAnalogPresentValue(&plugLoad->plugManagedConsumption, (float) atof(x.results[0]));
		XmlCleanup(&x);
	}

	if (XmlSetup(xmlc, &x, "plugloadEnergy/unmanaged-energy"))
	{
		StoreAnalogPresentValue(&plugLoad->plugUnManagedConsumption, (float) atof(x.results[0]));
		XmlCleanup(&x);
	}
		
	StoreAnalogPresentValue(&plugLoad->plugTotalConsumption, plugLoad->plugManagedConsumption.presentValue + plugLoad->plugUnManagedConsumption.presentValue);
		
	free(xmlc);
}
#endif


#ifdef EM
static void data_refresh_emergency_for_area(s_sector_t *area)
{
	xml_parse_ctx_t x;
	char url[1024];
	char ofile[256];
	
	// first get the reported consumption for the whole area
	snprintf(url, sizeof(url), GET_AREA_EMERGENCY_STATUS, elf_get_db_gems_ip_address_config(), area->id);
	snprintf(ofile, sizeof(ofile), "%s/%s_%d.%s", elf_bacnet_config.tmpFilePath, GET_AREA_EMERGENCY_STATUS_XML, area->id, TMP_FILE_PREFIX);

	char *xmlc = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (!xmlc)
		return;

	if (XmlSetup(xmlc, &x, "response/status"))
	{
		StoreBinaryPresentValue(&area->areaEmergency.binaryTypeDescriptor, (atoi(x.results[0])) ? true : false);
		XmlCleanup(&x);
	}
}
#endif // EM


#ifdef EM
static void data_refresh_plugload_consumption_for_area(s_sector_t *area)
{
	xml_parse_ctx_t x;
	char url[1024];
	char ofile[256];
	uint iPlugload;
	
	// check that there are configured plugloads
	if (!area->num_plugloads)
	{
		return ;
	}

	// first get the reported consumption for the whole area
	snprintf(url, sizeof(url), GET_AREA_PLUGLOAD_CONSUMPTION_API, elf_get_db_gems_ip_address_config(), area->id);
	snprintf(ofile, sizeof(ofile), "%s/%s_%d.%s", elf_bacnet_config.tmpFilePath, GET_AREA_PLUGLOAD_CONSUMPTION_XML, area->id, TMP_FILE_PREFIX);

	char *xmlc = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (!xmlc)
		return;

	if (XmlSetup(xmlc, &x, "plugloadEnergy/managed-energy"))
	{
		StoreAnalogPresentValue(&area->areaPlugManagedConsumption, (float) atof(x.results[0]));
		XmlCleanup(&x);
	}

	if (XmlSetup(xmlc, &x, "plugloadEnergy/unmanaged-energy"))
	{
		StoreAnalogPresentValue(&area->areaPlugUnManagedConsumption, (float) atof(x.results[0]));
		XmlCleanup(&x);
	}
	free(xmlc);

	for (iPlugload = 0; iPlugload < area->num_plugloads; iPlugload++)
	{
		// now get the consumption for each plugload
		 get_individual_plugload_consumption(&area->plugLoads[iPlugload]);
	}
}
#endif // EM


#ifdef EM
void data_refresh_occupancy(void)
{
	char ofile[256];
	char url[1024];

	if (g_energy_manager)
	{
		unsigned int i, area_id, floor_id;
		for (i = 0; i < g_energy_manager->num_floors; i++)
		{
			floor_id = g_energy_manager->floors[i].id;
			snprintf(url, sizeof(url), GET_FLOOR_AREA_OCC_STATE_API, elf_get_db_gems_ip_address_config(), floor_id);
			snprintf(ofile,
				sizeof(ofile),
				"%s/%s_%d.%s",
				elf_bacnet_config.tmpFilePath,
				GET_FLOOR_AREA_OCC_XML,
				floor_id,
				TMP_FILE_PREFIX);

			char *xml3 = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
			if (xml3 == NULL)
			{
				// panic("Could not process %s, output in file %s", url, ofile);
				return;
			}
			
			int num_areas, k = 0;
			xml_parse_ctx_t x;
			if (XmlSetup(xml3, &x, "areas/area"))
			{
				num_areas = x.num_results;
				XmlCleanup(&x);
			}
			else
			{
				// call failed for _this_floor
				continue ;
			}

			s_area_occ_t *p_area_occ = (s_area_occ_t *) calloc(num_areas, sizeof(s_area_occ_t));
			if (!p_area_occ)
			{
				log_printf(LOG_CRIT, "In %s: can't allocate memory for area occupancy data!\n", __func__);
				exit(EXIT_FAILURE);
			}
						
									// Get the id of area
			if (XmlSetup(xml3, &x, "areas/area/id"))
			{
				for (k = 0; k < num_areas; k++)
				{
					p_area_occ[k].id = (unsigned int) atoi(x.results[k]);
				}
				XmlCleanup(&x);
			}
			
			
			// Get the occupancy of area
			if (XmlSetup(xml3, &x, "areas/area/occupancyState"))
			{

				for (k = 0; k < num_areas; k++)
				{
					unsigned int l;
					for (l = 0; l < g_energy_manager->floors[i].num_sectors; l++)
					{
						s_sector_t *p_area = &g_energy_manager->floors[i].s_sector[l];
						area_id = p_area->id;
						if (area_id == p_area_occ[k].id)
						{
							int occ = atoi(x.results[k]);
							switch (occ)
							{
							case 0 :
								StoreBinaryPresentValue(&p_area->occupancy, BINARY_INACTIVE);
								break;
							case 1 :
								StoreBinaryPresentValue(&p_area->occupancy, BINARY_ACTIVE);
								break;
							default:
								// don't store anything, and reliability timer will take care of notifying user
								break;
							}
							log_printf(LOG_INFO, "Occupied=%d", occ);
							break;
						}
					}
				}
				XmlCleanup(&x);
			}

			free(xml3);
			free(p_area_occ);
		}
	}
}
#endif


ELF_RETURN set_switch_dim_level(int floor_id, char *switch_name, int dim_level)
{
	char url[1024];
	char ofile[256];

	snprintf(url,
		sizeof(url),
		SET_SWITCH_DIM_LEVEL_API,
		elf_get_db_gems_ip_address_config(),
		floor_id,
		switch_name,
		dim_level);
	snprintf(ofile, sizeof(ofile), "%s/%s_%d.%s", elf_bacnet_config.tmpFilePath, SET_SWITCH_DIM_LEVEL_XML, floor_id, TMP_FILE_PREFIX);

	return send_curl_message(REQ_TYPE_POST, url, NULL, ofile);

	    // todo 4 - examine returned file for true return code.
}


ELF_RETURN set_em_demandResponse_level(s_energy_manager_t *em, float level)
{
	char url[1024];
	char ofile[256];
	char ifile[256];
	char cmd[1024];
	char *drLevel;
	time_t timeNow = time(NULL);
	// char *ascTimeNow = ctime(&timeNow);
	struct tm *timeStrNow = localtime(&timeNow);

	snprintf(ifile,
		sizeof(ifile),
		"%s/%s.%s",
		elf_bacnet_config.tmpFilePath,
		SET_DEMANDRESPONSE_IN_XML,
		TMP_FILE_PREFIX);
	
	switch ((int) level)
	{
	case 1:
		drLevel = "HIGH";
		break;
	case 2:
		drLevel = "MODERATE";
		break;
	case 3:
		drLevel = "LOW";
		break;
	case 4:
		drLevel = "SPECIAL";
		break;
	default:
		panic("Do we need another level?");
		return ELF_RETURN_FAIL ;
	}
	
	snprintf(cmd,
		sizeof(cmd),
		"echo \"<drTarget><pricelevel>%s</pricelevel><duration>60</duration><starttime>%d-%02d-%02dT%02d:%02d:%02d</starttime><dridentifier>DR%d</dridentifier></drTarget>\" > %s",
		// "echo \"<drTarget><pricelevel>%s</pricelevel><duration>60</duration><starttime>%d-%d-%dT%02d:%02d:%02d</starttime></drTarget>\" > %s",
		drLevel,
		// 2015-11-18T01:20:49
		timeStrNow->tm_year + 1900,
		timeStrNow->tm_mon +1,
		timeStrNow->tm_mday,
		timeStrNow->tm_hour,
		timeStrNow->tm_min,
		timeStrNow->tm_sec,
		(uint) time(NULL), 
		ifile);

	if (system(cmd) < 0)
	{
		log_printf(LOG_CRIT, "In %s: can't execute command %s!\n", __func__, cmd);
		exit(EXIT_FAILURE);
	}
	// todo 2 - remove these sleeps when API CPU load issues resolved
	sleep(1);

	snprintf(url, sizeof(url), SET_EM_ADR_LEVEL, elf_get_db_gems_ip_address_config());

	snprintf(ofile,
		sizeof(ofile),
		"%s/%s.%s",
		elf_bacnet_config.tmpFilePath,
		SET_DEMANDRESPONSE_OUT_XML,
		TMP_FILE_PREFIX);

	return send_curl_message(REQ_TYPE_POST, url, ifile, ofile);
	
}


ELF_RETURN set_switch_scene(int switchId, int sceneId)
{
	char url[1024];
	char ofile[256];

	snprintf(url,
		sizeof(url),
		SET_SWITCH_SCENE_API,
		elf_get_db_gems_ip_address_config(),
		switchId,
		sceneId);
	snprintf(ofile, sizeof(ofile), "%s/%s_%d.%s", elf_bacnet_config.tmpFilePath, SET_SWITCH_SCENE_XML, switchId, TMP_FILE_PREFIX);

	return send_curl_message(REQ_TYPE_POST, url, NULL, ofile);

	    // todo 4 - examine returned file for true return code.
}


ELF_RETURN set_fixture_dim_level(uint32_t object_instance, unsigned int value)
{
	char ifile[256];
	char ofile[256];

	int id = GET_FIXTURE_ID_FROM_INSTANCE(object_instance);

	char cmd[1024];
	char url[1024];

	snprintf(ifile,
		sizeof(ifile),
		"%s/%s.%s",
		elf_bacnet_config.tmpFilePath,
		// todo 3 correct these names
		SET_FIXTURE_DIM_XML,
		TMP_FILE_PREFIX);

    // todo 4 - are we _sure_ there is only < 100 fixtures/_site_ ! I don't think so
	snprintf(cmd, sizeof(cmd), "echo \"<sensors><sensor><id>%d</id></sensor></sensors>\" > %s", id, ifile);
	if (system(cmd) < 0)
	{
		log_printf(LOG_CRIT, "In %s: can't execute command %s!\n", __func__, cmd);
		exit(EXIT_FAILURE);
	}
	// todo 2 - remove these sleeps when API CPU load issues resolved
	sleep(1);

	    // 60 == timeout periods in minutes of the dim level override. Todonext8 - establish if fixed value is OK
	snprintf(url, sizeof(url), SET_FIXTURE_DIM_LEVEL_API, elf_get_db_gems_ip_address_config(), "abs", (int) value, 60);

	snprintf(ofile,
		sizeof(ofile),
		"%s/%s.%s",
		elf_bacnet_config.tmpFilePath,
		SET_FIXTURE_DIM_OUT_XML,
		TMP_FILE_PREFIX);

	return send_curl_message(REQ_TYPE_POST, url, ifile, ofile);
}
#endif // EM

#ifdef EM
ELF_RETURN set_plugload_state(const BACNET_WRITE_PROPERTY_DATA *wp_data, unsigned int value)
{
	char ofile[256];
	char url[1024];

	s_plugload_t *plugLoad = get_area_plugload_ptr(elf_get_current_bacnet_device_instance(), wp_data->object_instance);
	if (plugLoad == NULL)
	{
		panic("null");
		return ELF_RETURN_FAIL ;
	}
	
	snprintf(url, sizeof(url), SET_PLUGLOAD_STATUS_API, elf_get_db_gems_ip_address_config(), plugLoad->id, (int) value);

	snprintf(ofile,
		sizeof(ofile),
		"%s/%s.%s",
		elf_bacnet_config.tmpFilePath,
		SET_PLUGLOAD_STATUS_XML,
		TMP_FILE_PREFIX);

	return send_curl_message(REQ_TYPE_POST, url, NULL, ofile);
}
#endif //EM


#ifdef EM
ELF_RETURN set_energy_manager_emergency(BACNET_WRITE_PROPERTY_DATA *wp_data, unsigned int value)
{
	char ofile[256];
	char url[1024];
	
	// Notes, emergency can only ever be "set" (no reset, write inactive) and no reads...
	
	if (!value)
	{
		log_printf(LOG_NOTICE, "Cannot reset EM Emergency Status - only set it");
		// returning quietly as if everything succeeded
		return ELF_RETURN_OK;	
	}
	
	snprintf(url, sizeof(url), SET_EM_EMERGENCY, elf_get_db_gems_ip_address_config());

	snprintf(ofile,
		sizeof(ofile),
		"%s/%s.%s",
		elf_bacnet_config.tmpFilePath,
		SET_EM_EMERGENCY_XML,
		TMP_FILE_PREFIX);

	return send_curl_message(REQ_TYPE_POST, url, NULL, ofile);
}
#endif


#ifdef EM
ELF_RETURN set_area_emergency(BACNET_WRITE_PROPERTY_DATA *wp_data, unsigned int value)
{
	char ofile[256];
	char url[1024];
	
	s_sector_t *area = get_sector_ptr_by_instance(elf_get_current_bacnet_device_instance());
	if (!area)
	{
		panic("null");
		return ELF_RETURN_FAIL;
	}

	if (!value)
	{
		// todo 5 - we should revisit.
		log_printf(LOG_NOTICE, "Cannot reset Area Emergency Status - only set it");
		// returning quietly as if everything succeeded
		return ELF_RETURN_OK;	
	}

	snprintf(url, sizeof(url), SET_AREA_EMERGENCY_API, elf_get_db_gems_ip_address_config(), area->id);

	snprintf(ofile,
		sizeof(ofile),
		"%s/%s.%s",
		elf_bacnet_config.tmpFilePath,
		SET_AREA_EMERGENCY_XML,
		TMP_FILE_PREFIX);

	return send_curl_message(REQ_TYPE_POST, url, NULL, ofile);
}
#endif

#ifdef UEM

ELF_RETURN set_elf_value(
    uint32_t deviceInstance,
	ELF_DATA_TYPE elfObjectType,
	float value)
{
	char ifile[256] =
	{ 0 };
	char ofile[256] =
	{ 0 };

	elf_bacnet_db_t *ptr = (elf_bacnet_db_t *)elf_get_bacnet_db_ptr_specific_device(deviceInstance);
	if (!ptr)
	{
		return ELF_RETURN_FAIL ;
	}

	int id = ptr->sector_id;
	char url[1024];

	time_t timestamp = time(NULL);

	int i;
	for (i = 0; i < MX_BMS_API_STRINGS; i++)
	{
		if (apiStrings[i].elfObjectType == elfObjectType)
		{
			snprintf(url, sizeof(url), apiStrings[i].apiString, elf_get_db_gems_ip_address_config(), id, value, timestamp);
			snprintf(ifile, sizeof(ifile), "%s/%s_if.%s", elf_bacnet_config.tmpFilePath, apiStrings[i].xmlFilename, TMP_FILE_PREFIX);
			snprintf(ofile, sizeof(ofile), "%s/%s_of.%s", elf_bacnet_config.tmpFilePath, apiStrings[i].xmlFilename, TMP_FILE_PREFIX);
			send_curl_message_write(REQ_TYPE_POST, url, ifile, ofile);
			return ELF_RETURN_OK;
		}
	}
	return ELF_RETURN_FAIL;
}
#endif // UEM


#ifdef EM
s_fixture_t2 *get_fixture_ptr(uint fixtureId)
{
	return (s_fixture_t2 *) bsearch(
		&fixtureId, 
		g_energy_manager->fixtures, 
		(int) g_energy_manager->num_fixtures, 
		sizeof(s_fixture_t2), 
		compare_fixture_id_search) ;
}


s_fixture_t2 *get_area_fixture_ptr(uint32_t object_instance)
{
	return get_fixture_ptr(GET_FIXTURE_ID_FROM_INSTANCE(object_instance)) ;
}


s_fixture_t2 *get_nth_fixture_ptr_for_area(uint areaId, uint fixtureIndex)
{
	uint i;
	for (i = 0; i < g_energy_manager->num_fixtures; i++)
	{
		if (g_energy_manager->fixtures[i].areaId == areaId)
		{
			if (fixtureIndex == 0) return &g_energy_manager->fixtures[i];
			fixtureIndex--;
		}
	}
	panic("Not found");
	return NULL ;
}

#endif // EM


#ifdef EM

s_scene_t* get_switch_scene_ptr(uint32_t bacnetDeviceInstance, uint32_t object_instance)
{
	elf_bacnet_db_t *ptr = (elf_bacnet_db_t *) elf_get_bacnet_db_ptr_specific_device(bacnetDeviceInstance);
	if (!ptr)
	{
		panic("fail");
		return NULL;
	}

	// s_fixture_t *sptr;
	unsigned int i, j, k;
	int id = GET_SCENE_SCENE_ID_FROM_INSTANCE(object_instance);

	for (i = 0; i < g_energy_manager->num_floors; i++)
	{
		if (ptr->floor_id == g_energy_manager->floors[i].id)
		{
			for (j = 0; j < g_energy_manager->floors[i].num_switches; j++)
			{
				if (ptr->switch_id == g_energy_manager->floors[i].s_switch[j].id)
				{
					for (k = 0; k < g_energy_manager->floors[i].s_switch[j].num_scenes; k++)
					{
						if (g_energy_manager->floors[i].s_switch[j].s_scenes[k].sceneId == id)
						{
							return &g_energy_manager->floors[i].s_switch[j].s_scenes[k] ;
						}
					}
				}
			}
		}
	}
	return NULL ;
}


s_scenelightlevel_t* get_switch_scene_fixture_ptr(uint32_t bacnetDeviceInstance, uint32_t object_instance)
{
	elf_bacnet_db_t *ptr = (elf_bacnet_db_t *) elf_get_bacnet_db_ptr_specific_device(bacnetDeviceInstance);
	if (!ptr)
	{
		panic("fail");
		return NULL;
	}

	// s_fixture_t *sptr;
	unsigned int i, j, k, m;
	int id = GET_SCENE_SCENE_ID_FROM_INSTANCE(object_instance);

	// todo 5 - use elf_get_scene....
	for (i = 0; i < g_energy_manager->num_floors; i++)
	{
		if (ptr->floor_id != g_energy_manager->floors[i].id)
		{
			continue;
		}
		for (j = 0; j < g_energy_manager->floors[i].num_switches; j++)
		{
			if (ptr->switch_id == g_energy_manager->floors[i].s_switch[j].id)
			{
				for (k = 0; k < g_energy_manager->floors[i].s_switch[j].num_scenes; k++)
				{
					if (g_energy_manager->floors[i].s_switch[j].s_scenes[k].sceneId == id)
					{
						for (m = 0; m < g_energy_manager->floors[i].s_switch[j].s_scenes[k].num_lightlevels; m++)
						{
							if (g_energy_manager->floors[i].s_switch[j].s_scenes[k].s_lightlevels[m].fixtureId == GET_SCENE_FIXTURE_ID_FROM_INSTANCE(object_instance))
							{
								return &g_energy_manager->floors[i].s_switch[j].s_scenes[k].s_lightlevels[m] ;
							}
						}
					}
				}
			}
		}
	}
	return NULL ;
}


s_pluglevel_t* get_switch_scene_plugload_ptr(uint32_t bacnetDeviceInstance, uint32_t object_instance)
{
	elf_bacnet_db_t *ptr = (elf_bacnet_db_t *) elf_get_bacnet_db_ptr_specific_device(bacnetDeviceInstance);
	if (!ptr)
	{
		panic("fail");
		return NULL;
	}

	unsigned int i, j, k, m;
	int id = GET_SCENE_SCENE_ID_FROM_INSTANCE(object_instance);

	// todo 5 - use elf_get_scene....
	for (i = 0; i < g_energy_manager->num_floors; i++)
	{
		if (ptr->floor_id != g_energy_manager->floors[i].id)
		{
			continue;
		}
		for (j = 0; j < g_energy_manager->floors[i].num_switches; j++)
		{
			if (ptr->switch_id == g_energy_manager->floors[i].s_switch[j].id)
			{
				for (k = 0; k < g_energy_manager->floors[i].s_switch[j].num_scenes; k++)
				{
					if (g_energy_manager->floors[i].s_switch[j].s_scenes[k].sceneId == id)
					{
						for (m = 0; m < g_energy_manager->floors[i].s_switch[j].s_scenes[k].num_pluglevels; m++)
						{
							if (g_energy_manager->floors[i].s_switch[j].s_scenes[k].s_pluglevels[m].plugloadId == GET_SCENE_PLUGLOAD_ID_FROM_INSTANCE(object_instance))
							{
								return &g_energy_manager->floors[i].s_switch[j].s_scenes[k].s_pluglevels[m] ;
							}
						}
					}
				}
			}
		}
	}
	return NULL ;
}
#endif // EM


#ifdef EM
s_plugload_t* get_area_plugload_ptr(uint32_t bacnetDeviceInstance, uint32_t object_instance)
{
	elf_bacnet_db_t *ptr = (elf_bacnet_db_t *) elf_get_bacnet_db_ptr_specific_device(bacnetDeviceInstance);
	if (!ptr)
	{
		return NULL;
	}

	s_plugload_t *sptr;
	unsigned int i, j;
	int id = GET_PLUGLOAD_ID_FROM_INSTANCE(object_instance);

	for (i = 0; i < g_energy_manager->num_floors; i++)
	{
		if (ptr->floor_id != g_energy_manager->floors[i].id)
		{
			continue;
		}
		for (j = 0; j < g_energy_manager->floors[i].num_sectors; j++)
		{
			if (ptr->sector_id != g_energy_manager->floors[i].s_sector[j].id)
			{
				continue;
			}
			// todo 5 - qsort _every_ time we bsearch? What is the point, it would be faster to iterate through the list
			// review this at a safe time. Same seems to apply to fixtures.
			qsort((void *) &g_energy_manager->floors[i].s_sector[j].plugLoads[0], 
				(int) g_energy_manager->floors[i].s_sector[j].num_plugloads,
				sizeof(s_plugload_t),
				compare_plugload_id_sort);
			if ((sptr = (s_plugload_t *) bsearch(
			        &id,
				&g_energy_manager->floors[i].s_sector[j].plugLoads[0],
				(int) g_energy_manager->floors[i].s_sector[j].num_plugloads,
				sizeof(s_plugload_t),
				compare_plugload_id_search)) != NULL)
			{
			    // Found sensor that matches the object instance.
				return sptr ;
			}
		}
	}

	return NULL ;
}


unsigned int elf_get_number_of_fixtures_in_area(uint32_t bacnetDeviceInstance)
{
	s_sector_t *sector = get_sector_ptr_by_instance(bacnetDeviceInstance);
	if (!sector)
	{
		panic("null");
		return 0;
	}
	return sector->num_fixtures_in_area ;
}


unsigned int elf_get_number_of_plugloads_in_area(uint32_t bacnetDeviceInstance)
{
	s_sector_t *sector = get_sector_ptr_by_instance(bacnetDeviceInstance);
	if (!sector)
	{
		panic("null");
		return 0;
	}
	return sector->num_plugloads ;
}
#endif // EM


//static s_floor_t* get_floor_ptr_by_id(unsigned int floorId)
//{
	//unsigned int i;
	//for (i = 0; i < g_energy_manager->num_floors; i++)
	//{
		//if (g_energy_manager->floors[i].id == floorId) return &g_energy_manager->floors[i];
	//}
	//return NULL ;
//}


static s_sector_t* get_sector_ptr_by_floorId_and_sectorId(unsigned int floorId, unsigned int sectorId)
{
	unsigned int i, j;
	for (i = 0; i < g_energy_manager->num_floors; i++)
	{
		if (g_energy_manager->floors[i].id == floorId)
		{
			for (j = 0; j < g_energy_manager->floors[i].num_sectors; j++)
			{
				if (g_energy_manager->floors[i].s_sector[j].id == sectorId)
				{
				    // Note: Cannot do this during creation of structures, the floor structure array can change dynamically
					g_energy_manager->floors[i].s_sector[j].floorPtr = &g_energy_manager->floors[i];
					return &g_energy_manager->floors[i].s_sector[j];
				}
			}
		}
	}
	return NULL;
}


s_sector_t* get_sector_ptr_by_instance(uint32_t deviceInstance)
{
	elf_bacnet_db_t *ptr = elf_get_bacnet_db_ptr_specific_device(deviceInstance);
	if (!ptr) return NULL;
	return get_sector_ptr_by_floorId_and_sectorId(ptr->floor_id, ptr->sector_id) ;
}



#ifdef UEM
static float GrabFloat(void *xmlptr, const char *fieldName)
{
	float rc = 0.0;
	xml_parse_ctx_t x;
	int err;

	x.xml_buf = (char *) xmlptr;
	x.xml_xpath = fieldName;
	err = parse_xml(&x);
	if (!err)
	{
		if (x.num_results)
		{
			rc = (float) atof(x.results[0]);
		}
		else
		{
			panic("Could not parse %s", fieldName);
		}
		free(x.results);
	}
	else
	{
		panic("parse_xml() error %d", err);
	}
	roxml_release(RELEASE_ALL);
	roxml_close(x.root);
	return rc;
}


static int get_hvac_stats_per_zone(unsigned int zone_id)
{
	char url[1024];
	char ofile[256];

	snprintf(url,
		sizeof(url),
		GET_HVAC_STATS_BY_ZONE_API,
		elf_get_db_gems_ip_address_config(),
		zone_id);

	snprintf(ofile,
		sizeof(ofile),
		"%s/%s_%d.%s",
		elf_bacnet_config.tmpFilePath,
		FILENAME_HVAC_STATS_ZONE_XML,
		zone_id,
		TMP_FILE_PREFIX);

	char *xml5 = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (xml5 == NULL)
	{
		log_printf(LOG_NOTICE, "Empty response: [%s]", ofile);
		return ELF_RETURN_FAIL ;
	}

	unsigned int i, zone;
	for (i = 0; i < g_energy_manager->num_floors; i++)
	{
		for (zone = 0; zone < g_energy_manager->floors[i].num_sectors; zone++)
		{
			if (g_energy_manager->floors[i].s_sector[zone].id == zone_id)
			{
				float tval;
				
				StoreAnalogPresentValue(&g_energy_manager->floors[i].s_sector[zone].bmsSetPoint.analogTypeDescriptor, GrabFloat(xml5, "hvacZoneVO/bmsSetPoint"));
				StoreAnalogPresentValue(&g_energy_manager->floors[i].s_sector[zone].bmsSetPointLow.analogTypeDescriptor, GrabFloat(xml5, "hvacZoneVO/bmsSetPointLow"));
				StoreAnalogPresentValue(&g_energy_manager->floors[i].s_sector[zone].bmsSetPointHigh.analogTypeDescriptor, GrabFloat(xml5, "hvacZoneVO/bmsSetPointHigh"));
				StoreAnalogPresentValue(&g_energy_manager->floors[i].s_sector[zone].bmsTemperature.analogTypeDescriptor, GrabFloat(xml5, "hvacZoneVO/bmsTemperature"));
				StoreAnalogPresentValue(&g_energy_manager->floors[i].s_sector[zone].maxTemp, GrabFloat(xml5, "hvacZoneVO/maxTemp"));
				StoreAnalogPresentValue(&g_energy_manager->floors[i].s_sector[zone].minTemp, GrabFloat(xml5, "hvacZoneVO/minTemp"));
				StoreAnalogPresentValue(&g_energy_manager->floors[i].s_sector[zone].avgTemp, GrabFloat(xml5, "hvacZoneVO/avgTemp"));
				
				tval = GrabFloat(xml5, "hvacZoneVO/setback");
				
				// todo 5 - encapsulate this / do we want to pre-process BACnet stuff? Can't we depend on the EM/UEM??
				//if (tval > 3.0)
				//{
					//log_printf(LOG_NOTICE, "Setback for Zone %d is over-range %f", zone_id, tval);
					//g_energy_manager->floors[i].s_sector[zone].setback.objectTypeDescriptor.reliability = RELIABILITY_OVER_RANGE;
				//}
				//else if (tval < 0.0)
				//{
					//log_printf(LOG_NOTICE, "Setback for Zone %d is under-range %f", zone_id, tval);
					//g_energy_manager->floors[i].s_sector[zone].setback.objectTypeDescriptor.reliability = RELIABILITY_OVER_RANGE;
				//}
				//else
				//{
					//g_energy_manager->floors[i].s_sector[zone].setback.objectTypeDescriptor.reliability = RELIABILITY_NO_FAULT_DETECTED;
				//}
				
				StoreAnalogPresentValue(&g_energy_manager->floors[i].s_sector[zone].setback, tval);	
			
				StoreAnalogPresentValue(&g_energy_manager->floors[i].s_sector[zone].zoneFailure, GrabFloat(xml5, "hvacZoneVO/zonefailure"));
				StoreAnalogPresentValue(&g_energy_manager->floors[i].s_sector[zone].tempSetpointChange, GrabFloat(xml5, "hvacZoneVO/tempSetPointChange"));
				StoreAnalogPresentValue(&g_energy_manager->floors[i].s_sector[zone].airflowRecommendation, GrabFloat(xml5, "hvacZoneVO/airflowRecommendation"));
			}
		}
	}
	free(xml5);
	
	return ELF_RETURN_OK;
}
#endif

#ifdef EM
static ELF_RETURN get_fixtures_energy_stats_per_floor(unsigned int floor_id)
{
	char url[1024];
	char ofile[256];

	snprintf(url, sizeof(url), GET_FLOOR_SENSORS_ENERGY_API, elf_get_db_gems_ip_address_config(), floor_id);
	snprintf(ofile,
		sizeof(ofile),
		"%s/%s_%d.%s",
		elf_bacnet_config.tmpFilePath,
		GET_FLOOR_SENSORS_ENERGY_XML,
		floor_id,
		TMP_FILE_PREFIX);
	
	
	char *xmlb = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (xmlb == NULL)
	{
		// panic("Null");
		return ELF_RETURN_FAIL ;
	}

	unsigned int i, k;
	unsigned int num_fixtures = 0;
	xml_parse_ctx_t x;
	
	for (i = 0; i < g_energy_manager->num_floors; i++)
	{
		s_fixture_t2 *p_fixture;
		
		if (g_energy_manager->floors[i].id != floor_id)
		{
			continue;
		}
		
		if (XmlSetup(xmlb, &x, "sensorEnergyStatss/sensorEnergyStats/sensor"))
		{
			num_fixtures = (unsigned int) x.num_results; // todo 4 resolve that -ve possibility....
			XmlCleanup(&x);
		}
		
		if (!num_fixtures) continue ;

		if (!(p_fixture = (s_fixture_t2 *) calloc((int) num_fixtures, sizeof(s_fixture_t2))))
		{
			log_printf(LOG_CRIT, "In %s: can't allocate memory for sensors!\n", __func__);
			exit(EXIT_FAILURE);
		}
		
							                    // Get the id of sensor
		if (XmlSetup(xmlb, &x, "sensorEnergyStatss/sensorEnergyStats/sensor/id"))
		{
			for (k = 0; k < num_fixtures; k++)
			{
				p_fixture[k].fixtureId = (unsigned int) atoi(x.results[k]);
			}
			XmlCleanup(&x);
		}


									                    // Get the base energy of sensor
		if (XmlSetup(xmlb, &x, "sensorEnergyStatss/sensorEnergyStats/sensor/baseEnergy"))
		{
			for (k = 0; k < num_fixtures; k++)
			{
				StoreAnalogPresentValue(&p_fixture[k].fixture_base_energy, (float) atof(x.results[k]));
			}
			XmlCleanup(&x);
		}

									                    // Get the used energy of sensor
		if (XmlSetup(xmlb, &x, "sensorEnergyStatss/sensorEnergyStats/sensor/energy"))
		{
			for (k = 0; k < num_fixtures; k++)
			{
				StoreAnalogPresentValue(&p_fixture[k].fixture_used_energy, (float) atof(x.results[k]));
			}
			XmlCleanup(&x);
		}

									                    // Get the saved energy of sensor
		if (XmlSetup(xmlb, &x, "sensorEnergyStatss/sensorEnergyStats/sensor/savedEnergy"))
		{
			for (k = 0; k < num_fixtures; k++)
			{
				StoreAnalogPresentValue(&p_fixture[k].fixture_saved_energy, (float) atof(x.results[k]));
			}
			XmlCleanup(&x);
		}

									                    // Get the occupancy savings of sensor
		if (XmlSetup(xmlb, &x, "sensorEnergyStatss/sensorEnergyStats/sensor/occSavings"))
		{
			for (k = 0; k < num_fixtures; k++)
			{
				StoreAnalogPresentValue(&p_fixture[k].occ_saved_energy, (float)  atof(x.results[k]));
			}
			XmlCleanup(&x);
		}
		
		                    // Get the ambient savings of sensor
		if (XmlSetup(xmlb, &x, "sensorEnergyStatss/sensorEnergyStats/sensor/ambientSavings"))
		{
			for (k = 0; k < num_fixtures; k++)
			{
				StoreAnalogPresentValue(&p_fixture[k].amb_saved_energy2, (float) atof(x.results[k]));
			}
			XmlCleanup(&x);
		}
						// Get the task tuned savings of sensor
		if (XmlSetup(xmlb, &x, "sensorEnergyStatss/sensorEnergyStats/sensor/tuneupSavings"))
		{
			for (k = 0; k < num_fixtures; k++)
			{
				StoreAnalogPresentValue(&p_fixture[k].task_saved_energy, (float) atof(x.results[k]));
			}
			XmlCleanup(&x);
		}

									                    // Get the manual savings of sensor
		if (XmlSetup(xmlb, &x, "sensorEnergyStatss/sensorEnergyStats/sensor/manualSavings"))
		{
			for (k = 0; k < num_fixtures; k++)
			{
				StoreAnalogPresentValue(&p_fixture[k].manual_saved_energy, (float) atof(x.results[k]));
			}
			XmlCleanup(&x);
		}

									                    // Get the dim level of sensor
		if (XmlSetup(xmlb, &x, "sensorEnergyStatss/sensorEnergyStats/sensor/dimLevel"))
		{
			for (k = 0; k < num_fixtures; k++)
			{
				StoreAnalogPresentValue(&p_fixture[k].dim_level2.analogTypeDescriptor, (float) atoi(x.results[k]));
			}
			XmlCleanup(&x);
		}

		
        // Get the occupancy of sensor
		if (XmlSetup(xmlb, &x, "sensorEnergyStatss/sensorEnergyStats/sensor/occupancy"))
		{
			for (k = 0; k < num_fixtures; k++)
			{
				StoreBinaryPresentValueConditionalInt(&p_fixture[k].occupancy, atoi(x.results[k]) );
			}
			XmlCleanup(&x);
		}

		if (XmlSetup(xmlb, &x, "sensorEnergyStatss/sensorEnergyStats/sensor/ambientLight"))
		{
			for (k = 0; k < num_fixtures; k++)
			{
				StoreAnalogPresentValueConditionalInt(&p_fixture[k].ambient_light, (float) atoi(x.results[k]));
			}
			XmlCleanup(&x);
		}
		
		s_fixture_t2 *tptr = NULL;
		for (k = 0; k < num_fixtures; k++)
		{
			if ((tptr = (s_fixture_t2 *) bsearch(
				&p_fixture[k].fixtureId, 
				g_energy_manager->fixtures, 
				(int) g_energy_manager->num_fixtures, 
				sizeof(s_fixture_t2), 
				compare_fixture_id_search)) != NULL)
			{
			    // Copy the energy data.
			    // Mark state as valid.
				s_fixture_t2 *sptr = &p_fixture[k];
				tptr->fixture_base_energy = sptr->fixture_base_energy;
				tptr->fixture_used_energy = sptr->fixture_used_energy;
				tptr->fixture_saved_energy = sptr->fixture_saved_energy;
				tptr->occ_saved_energy = sptr->occ_saved_energy;
				tptr->amb_saved_energy2 = sptr->amb_saved_energy2;
				tptr->task_saved_energy = sptr->task_saved_energy;
				tptr->manual_saved_energy = sptr->manual_saved_energy;

				tptr->occupancy = sptr->occupancy;
				tptr->fixtureOutage = sptr->fixtureOutage;
				tptr->ambient_light = sptr->ambient_light;

									// todo 2 - why is this different?
				StoreAnalogPresentValue(&tptr->dim_level2.analogTypeDescriptor, sptr->dim_level2.analogTypeDescriptor.presentValue);

									// todo 4 tptr->state = DEVICE_STATE_VALID;
			}
			else
			{
				// well, we did not find the fixture, but we have just received data for it. What gives... it must be new!
				// but, the data read of fixtures does not tell us what area the fixture is in, so all we can do is...
				trigger_rediscovery();
			}
		}

		free(p_fixture);
	}
	
	free(xmlb);
	return ELF_RETURN_OK;
}
#endif


#ifdef EM
static void elf_update_energy_manager(void)
{
	char url[1024];
	char ofile[256];

	snprintf(url, sizeof(url), GET_EM_CONSUMPTION, elf_get_db_gems_ip_address_config());
	snprintf(ofile,
		sizeof(ofile),
		"%s/%s.%s",
		elf_bacnet_config.tmpFilePath,
		GET_EM_ENERGY_XML,
		TMP_FILE_PREFIX);
	
	
	char *xmlb = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (xmlb == NULL)
	{
		// panic("Null");
		return ;
	}

	xml_parse_ctx_t x;
	s_energy_manager_t *em = get_energy_manager_ptr();
		
	if (XmlSetup(xmlb, &x, "emEnergyConsumption/energy-lighting"))
	{
		StoreAnalogPresentValue(&em->energyLighting, (float) atof(x.results[0]));
		XmlCleanup(&x);
	}
		
	if (XmlSetup(xmlb, &x, "emEnergyConsumption/energy-plugload"))
	{
		StoreAnalogPresentValue(&em->energyPlugload, (float) atof(x.results[0]));
		XmlCleanup(&x);
	}
		
	free(xmlb);
}
#endif


#ifdef EM
static void data_refresh_em_demandResponse(void)
{
	char url[1024];
	char ofile[256];
	int i;
	int active = -1;

	snprintf(url, sizeof(url), GET_EM_ADR_LEVEL, elf_get_db_gems_ip_address_config());
	snprintf(ofile,
		sizeof(ofile),
		"%s/%s.%s",
		elf_bacnet_config.tmpFilePath,
		GET_EM_ADR_LEVEL_XML,
		TMP_FILE_PREFIX);
	
	char *xmlb = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (xmlb == NULL)
	{
		// panic("Null");
		return ;
	}

	xml_parse_ctx_t x;
	s_energy_manager_t *em = get_energy_manager_ptr();
	
	// see if we can identify an 'active' one
	if (XmlSetup(xmlb, &x, "dRTargets/drTarget/drstatus"))
	{
		for (i = 0; i < x.num_results; i++)
		{
			// there _may_ be multiple, or none. Just take the first one (default), or if found, the first active one, for now (todo 5 - clean up ADR handling with Aditya).
			// or the first Active one
			if (!strcmp(x.results[0], "Active"))
			{
				active = i;
				break;
			} 
		}
		XmlCleanup(&x);
	}
	
	if (active < 0)
	{
		// no active DR detected
		StoreAnalogPresentValue((AnalogObjectTypeDescriptor *) &em->adrLevel, 0.0);	
	}
	else	{		if (XmlSetup(xmlb, &x, "dRTargets/drTarget/pricelevel"))
		{
			if (!strcmp(x.results[active], "LOW"))
			{
				StoreAnalogPresentValue((AnalogObjectTypeDescriptor *) &em->adrLevel, 3.0);	
			} 
			else if (!strcmp(x.results[active], "MODERATE"))
			{
				StoreAnalogPresentValue((AnalogObjectTypeDescriptor *) &em->adrLevel, 2.0);	
			}
			else if (!strcmp(x.results[active], "HIGH"))
			{
				StoreAnalogPresentValue((AnalogObjectTypeDescriptor *) &em->adrLevel, 1.0);	
			}
			XmlCleanup(&x);
		}
	
		//if (XmlSetup(xmlb, &x, "dRTargets/drTarget/dridentifier"))
		//{
			//strncpy(em->adrName, x.results[active], sizeof(em->adrName));
			//XmlCleanup(&x);
		//}
	}	
	free(xmlb);
}
#endif


#ifdef EM
static void data_refresh_fixture_outages_for_area(s_sector_t *area)
{
	char url[1024];
	char ofile[256];

	snprintf(url, sizeof(url), GET_AREA_FIXTURE_OUTAGES_API, elf_get_db_gems_ip_address_config(), area->id);
	snprintf(ofile, sizeof(ofile), "%s/%s_%d.%s", elf_bacnet_config.tmpFilePath, AREA_FIXTURE_OUTAGES_XML, area->id, TMP_FILE_PREFIX);

	char *xmlc = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (!xmlc)
		return;

	xml_parse_ctx_t x;

	if (XmlSetup(xmlc, &x, "areaOutage/OutSensors"))
	{
		if (x.num_results != 1)
		{
			panic("Unexpected result count");
		}
		else
		{
			StoreAnalogPresentValue(&area->outageCount, (float) atoi(x.results[0]));
		}
		XmlCleanup(&x);
	}
	free(xmlc);
}
#endif // EM


#ifdef EM
static ELF_RETURN data_refresh_plugloads_for_area(s_sector_t *area)
{
	int k;
	xml_parse_ctx_t x;
	char url[1024];
	char ofile[256];
	int count = 0;
	float managedSum = 0.0f;
	float unmanagedSum = 0.0f;

	// note that this is the same API call for config... just we extract other information from the data
	snprintf(url, sizeof(url), GET_AREA_PLUGLOAD_CONFIGURATION_API, elf_get_db_gems_ip_address_config(), area->id);
	snprintf(ofile, sizeof(ofile), "%s/%s_%d.%s", elf_bacnet_config.tmpFilePath, GET_INDIVIDUAL_PLUGLOAD_DATA_XML, area->id, TMP_FILE_PREFIX); 

	char *xmlc = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (!xmlc)
	{
		return ELF_RETURN_FAIL ;
	}

	if (XmlSetup(xmlc, &x, "plugloads/plugload"))
	{
		count = x.num_results;
		XmlCleanup(&x);
	}
	if (!count)
	{
		// no plugloads for this area, return
		free(xmlc);
		return ELF_RETURN_FAIL ;
	}
	
	s_plugload_t *plugLoads = (s_plugload_t *) calloc(sizeof(s_plugload_t), count);
	if (plugLoads == NULL)
	{
		panic("Failed to calloc");
		exit(-1);
	}
	
	if (XmlSetup(xmlc, &x, "plugloads/plugload/id"))
	{
		for (k = 0; k < count; k++)
		{
			plugLoads[k].id = (unsigned int) atoi(x.results[k]);
		}
		XmlCleanup(&x);
	}
	
	// todo 1 - does this get presented by reinstated individual calls?
	if (XmlSetup(xmlc, &x, "plugloads/plugload/avgVolts"))
	{
		for (k = 0; k < count; k++)
		{
			bool tstat =  (atof(x.results[k]) > 99.9) ? true : false;
			StoreBinaryPresentValue((BinaryObjectTypeDescriptor *)&plugLoads[k].plugLoadStatus, tstat);
		}
		XmlCleanup(&x);
	}
	
	//if (XmlSetup(xmlc, &x, "plugloads/plugload/managedLoad"))
	//{
		//for (k = 0; k < count; k++)
		//{
			//StoreAnalogPresentValue(&plugLoads[k].plugManagedConsumption, (float) atof(x.results[k]));
			//managedSum += (float) atof(x.results[k]);
		//}
		//XmlCleanup(&x);
	//}
	//
	//
	//if (XmlSetup(xmlc, &x, "plugloads/plugload/unmanagedLoad"))
	//{
		//for (k = 0; k < count; k++)
		//{
			//StoreAnalogPresentValue(&plugLoads[k].plugUnManagedConsumption, (float) atof(x.results[k]));
			//unmanagedSum += (float) atof(x.results[k]);
		//}
		//XmlCleanup(&x);
	//}
	
	free(xmlc);
	

	// todo 5 - later we will qsort in-memory structures, so no sort will be needed here anymore.. and invert lookup loop..
	qsort(plugLoads, count, sizeof(s_plugload_t), compare_plugload_id_sort);

	// now transfer the relevant data from temp structure to permanent data structures
	
	for (k = 0; k < area->num_plugloads; k++)
	{
		s_plugload_t *tplugload = bsearch(&area->plugLoads[k].id, plugLoads, count, sizeof(s_plugload_t), compare_plugload_id_search);
		if (tplugload == NULL)
		{
			// means a plugload has been removed from the EM...
			continue ;
		}
		// all found, transfer data of interest
		area->plugLoads[k].plugLoadStatus = tplugload->plugLoadStatus;
		//area->plugLoads[k].plugManagedConsumption = tplugload->plugManagedConsumption;
		//area->plugLoads[k].plugUnManagedConsumption = tplugload->plugUnManagedConsumption;
		
		// StoreAnalogPresentValue(&area->plugLoads[k].plugTotalConsumption, area->plugLoads[k].plugManagedConsumption.presentValue + area->plugLoads[k].plugUnManagedConsumption.presentValue);
	}

	// and store the area totalized amounts
	//StoreAnalogPresentValue(&area->plugUnManagedConsumption, unmanagedSum);
	//StoreAnalogPresentValue(&area->plugManagedConsumption, managedSum);
	
	return ELF_RETURN_OK  ;
}


static s_plugload_t *discover_plugloads_for_area(s_sector_t *area, unsigned int *count)
{
	unsigned int k;
	xml_parse_ctx_t x;
	char url[1024];
	char ofile[256];

	snprintf(url, sizeof(url), GET_AREA_PLUGLOAD_CONFIGURATION_API, elf_get_db_gems_ip_address_config(), area->id);
	snprintf(ofile, sizeof(ofile), "%s/%s_%d.%s", elf_bacnet_config.tmpFilePath, GET_AREA_PLUGLOAD_CONFIGURATION_XML, area->id, TMP_FILE_PREFIX);

	char *xmlc = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (!xmlc)
	{
//		panic("why?");
		return NULL ;
	}

	*count = 0;
	if (XmlSetup(xmlc, &x, "plugloads/plugload"))
	{
		*count = (unsigned int) x.num_results;
		XmlCleanup(&x);
	}
	if (!*count)
	{
		// no plugloads for this area, return
		free(xmlc);
		return NULL ;
	}
	
	s_plugload_t *plugLoads = (s_plugload_t *) calloc(sizeof(s_plugload_t), (int) *count);
	if (plugLoads == NULL)
	{
		panic("Failed to calloc");
		exit(-1);
	}
	
	if (XmlSetup(xmlc, &x, "plugloads/plugload/id"))
	{
		for (k = 0; k < *count; k++)
		{
			plugLoads[k].id = (unsigned int) atoi(x.results[k]);
		}
		XmlCleanup(&x);
	}
	
	if (XmlSetup(xmlc, &x, "plugloads/plugload/name"))
	{
		for (k = 0; k < *count; k++)
		{
			strcpy(plugLoads[k].name, x.results[k]);
		}
		XmlCleanup(&x);
	}
	
	//if (XmlSetup(xmlc, &x, "plugloads/plugload/managedLoad"))
	//{
		//for (k = 0; k < *count; k++)
		//{
			//StoreAnalogPresentValue(&plugLoads[k].plugManagedConsumption, (float) atof(x.results[k]));
		//}
		//XmlCleanup(&x);
	//}
	//
	//
	//if (XmlSetup(xmlc, &x, "plugloads/plugload/unmanagedLoad"))
	//{
		//for (k = 0; k < *count; k++)
		//{
			//StoreAnalogPresentValue(&plugLoads[k].plugUnManagedConsumption, (float) atof(x.results[k]));
		//}
		//XmlCleanup(&x);
	//}
	
	if (XmlSetup(xmlc, &x, "plugloads/plugload/macaddress"))
	{
		for (k = 0; k < *count; k++)
		{
			sscanf(
			    x.results[k],
				"%hhx:%hhx:%hhx",
				&plugLoads[k].mac_address[0],
				&plugLoads[k].mac_address[1],
				&plugLoads[k].mac_address[2]);
		}
		XmlCleanup(&x);
	}

	free(xmlc);
	
	return plugLoads ;
}
#endif // EM

#ifdef EM
static s_plugload_t *establish_plugload_for_area(s_sector_t *area, s_plugload_t *plugload)
{
	unsigned int i;

	if (chkPtr(area)) return NULL ;
	if (chkPtr(plugload)) return NULL ;
	
	// does the fixture already exist?
	
	for (i = 0; i < area->num_plugloads; i++)
	{
		if (area->plugLoads[i].id == plugload->id)
		{
			// already exists
			return NULL ;
		}
	}
	
	area->num_plugloads++;
	// does not exist, time to create one
	s_plugload_t *plugloadGroup = (s_plugload_t *) realloc(area->plugLoads, sizeof(s_plugload_t)*area->num_plugloads);
	if (plugloadGroup == NULL)
	{
		log_printf(LOG_CRIT, "In %s: can't allocate memory for areas!\n", __func__);
		exit(EXIT_FAILURE);
	}
	area->plugLoads = plugloadGroup;
	
	plugloadGroup = &area->plugLoads[area->num_plugloads - 1];
	*plugloadGroup = *plugload;
	return plugloadGroup ;
}
#endif // EM


#ifdef EM
static void config_refresh_plugloads_for_area(s_sector_t *area)
{
	unsigned int count = 0, i;
	
	if (area->id == 0)
	{
		// todo 3 check with aditya, is it possible to have unassigned plugloads 
		// what about areas spread across floors? : https://goo.gl/VCb3MH
		return ;
	}
	
	s_plugload_t *discoveredPlugloads = discover_plugloads_for_area(area, &count);
	
	if (discoveredPlugloads == NULL) return ;
	
	for (i = 0; i < count; i++)
	{
		establish_plugload_for_area(area, &discoveredPlugloads[i]);
	}
	
}
#endif // EM


#ifdef EM
static void get_area_energy_data_per_floor(const s_floor_t *floor)
{
	unsigned int area, k;

	if (get_fixtures_energy_stats_per_floor(floor->id) == ELF_RETURN_OK)
	{
		// For each area, totalize the (relevant only) stats
		
		for (area = 0; area < floor->num_sectors; area++)
		{
			uint num_fixtures = 0;
			s_sector_t *p_area = &floor->s_sector[area];
			float fixture_base_energy_value = 0.0f;
			float fixture_used_energy_value  = 0.0f;
			float fixture_saved_energy_value  = 0.0f;
			float occ_saved_energy_value  = 0.0f;
			float amb_saved_energy2_value  = 0.0f;
			float task_saved_energy_value  = 0.0f;
			float manual_saved_energy_value  = 0.0f;
			float dim_level2_dim_level  = 0.0f;
			
			for (k = 0; k < g_energy_manager->num_fixtures; k++)
			{
				if (g_energy_manager->fixtures[k].areaId == p_area->id)
				{
					num_fixtures++;
					fixture_base_energy_value += g_energy_manager->fixtures[k].fixture_base_energy.presentValue;
					fixture_used_energy_value += g_energy_manager->fixtures[k].fixture_used_energy.presentValue;
					fixture_saved_energy_value += g_energy_manager->fixtures[k].fixture_saved_energy.presentValue;
					occ_saved_energy_value += g_energy_manager->fixtures[k].occ_saved_energy.presentValue;
					amb_saved_energy2_value += g_energy_manager->fixtures[k].amb_saved_energy2.presentValue;
					task_saved_energy_value += g_energy_manager->fixtures[k].task_saved_energy.presentValue;
					manual_saved_energy_value += g_energy_manager->fixtures[k].manual_saved_energy.presentValue;
					dim_level2_dim_level += g_energy_manager->fixtures[k].dim_level2.analogTypeDescriptor.presentValue;
				}
			
				StoreAnalogPresentValue(&p_area->area_base_energy, fixture_base_energy_value);
				StoreAnalogPresentValue(&p_area->area_consumed_lighting_energy, fixture_used_energy_value);
				StoreAnalogPresentValue(&p_area->area_saved_lighting_energy, fixture_saved_energy_value);
				StoreAnalogPresentValue(&p_area->occ_saved_energy, occ_saved_energy_value);
				StoreAnalogPresentValue(&p_area->amb_saved_energy, amb_saved_energy2_value);
				StoreAnalogPresentValue(&p_area->task_saved_energy, task_saved_energy_value);
				StoreAnalogPresentValue(&p_area->manual_saved_energy, manual_saved_energy_value);
				
				// calculate averages
				if (num_fixtures)
				{
					StoreAnalogPresentValue(&p_area->avg_dim_level, dim_level2_dim_level / num_fixtures);
				}
			}
		}
	}
}			


static s_fixture_t2 *discover_config_fixtures_by_area( s_sector_t *area, unsigned int *count)
{
	char url[1024];
	char ofile[256];

	snprintf(url, sizeof(url), GET_FIXTURE_LOC_BY_AREA_API, elf_get_db_gems_ip_address_config(), area->id );
	snprintf(ofile,
		sizeof(ofile),
		"%s/%s_%d.%s",
		elf_bacnet_config.tmpFilePath,
		SENSORS_LOC_BY_AREA_XML,
		area->id,
		TMP_FILE_PREFIX);

	char *xmlc = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (xmlc == NULL)
	{
		panic("Why?");
		return NULL ;
	}

	unsigned int k;
	xml_parse_ctx_t x;
			
			// just in case the read is an empty one, make sure num_fixtures all set to 0 for now.
	*count = 0;
	if (XmlSetup(xmlc, &x, "fixtures/fixture"))
	{
		*count = (unsigned int) x.num_results;
		XmlCleanup(&x);
	}
	
	if (*count == 0)
	{
		free(xmlc);
		return NULL ;
	}

	s_fixture_t2 *discoveredFixtures =  (s_fixture_t2 *) calloc((int) *count, sizeof(s_fixture_t2));
	if (discoveredFixtures == NULL)		
	{
		log_printf(LOG_CRIT, "In %s: can't allocate memory for sensors!\n", __func__);
		exit(EXIT_FAILURE);
	}

    // Get the id of sensor
	if (XmlSetup(xmlc, &x, "fixtures/fixture/id"))
	{
		for (k = 0; k < *count; k++)
		{
			discoveredFixtures[k].fixtureId = (unsigned int) atoi(x.results[k]);
			discoveredFixtures[k].areaId = area->id;
		}
		XmlCleanup(&x);
	}

    // Get the name of sensor
	if (XmlSetup(xmlc, &x, "fixtures/fixture/name"))
	{
		for (k = 0; k < *count; k++)
		{
			memcpy(discoveredFixtures[k].name, x.results[k], strlen(x.results[k]));
		}
		XmlCleanup(&x);
	}

    // Get the mac-address of sensor
	if (XmlSetup(xmlc, &x, "fixtures/fixture/macaddress"))
	{
		for (k = 0; k < *count; k++)
		{
			sscanf(
			        x.results[k],
				"%hhx:%hhx:%hhx",
				&discoveredFixtures[k].mac_address[0],
				&discoveredFixtures[k].mac_address[1],
				&discoveredFixtures[k].mac_address[2]);
		}
		XmlCleanup(&x);
	}
		
	free(xmlc);
	return discoveredFixtures ;
}


// this funtion only picks sensors appropriate for the area of interest. Results in multiple calls for sensor IDs
// but at this point that can't be helped

static s_scene_t *discover_scenes_for_floor_and_switchgroup(s_floor_t *floor, s_switchgroup_t *switchGroup, unsigned int *count)
{
	char url[1024];
	char ofile[256];

	snprintf(url, sizeof(url), GET_SCENES_BY_FLOOR_SWITCH_NAME, elf_get_db_gems_ip_address_config(), floor->id, switchGroup->switch_name);
	snprintf(ofile,
		sizeof(ofile),
		"%s/%s_%d_%s.%s",
		elf_bacnet_config.tmpFilePath,
		GET_SCENE_BY_FLOOR_SWITCH_NAME,
		floor->id,
		switchGroup->switch_name,
		TMP_FILE_PREFIX);

	char *xmlc = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (xmlc == NULL)
	{
		panic("Why?");
		return NULL ;
	}

	// show me the format
	unsigned int k;
	xml_parse_ctx_t x;
			
			// just in case the read is an empty one, make sure num_fixtures all set to 0 for now.
	*count = 0;
	if (XmlSetup(xmlc, &x, "switchScenes/scenes"))
	{
		*count = (unsigned int) x.num_results;
		XmlCleanup(&x);
	}
	
	if (*count == 0)
	{
		free(xmlc);
		return NULL ;
	}

	s_scene_t *discoveredScenes =  (s_scene_t *) calloc((int) *count, sizeof(s_scene_t));
	if (discoveredScenes == NULL)		
	{
		log_printf(LOG_CRIT, "In %s: can't allocate memory for scenes!\n", __func__);
		exit(EXIT_FAILURE);
	}

            // Get the id of sensor
	if (XmlSetup(xmlc, &x, "switchScenes/scenes/id"))
	{
		for (k = 0; k < *count; k++)
		{
			discoveredScenes[k].sceneId = (unsigned int) atoi(x.results[k]);
		}
		XmlCleanup(&x);
	}

	if (XmlSetup(xmlc, &x, "switchScenes/scenes/switchid"))
	{
		for (k = 0; k < *count; k++)
		{
			discoveredScenes[k].switchId = (unsigned int) atoi(x.results[k]);
			// todo 4 - close out with Sachin, Sreedhar, Aditya why there is this extra Id
			switchGroup->switchId = discoveredScenes[k].switchId;
			//if (discoveredScenes[k].switchId != switchGroup->id)
			//{
				//// todo sreedhar - this fails???
				//// panic("Something wrong");
				//// break ;
			//}
		}
		XmlCleanup(&x);
	}

	if (XmlSetup(xmlc, &x, "switchScenes/scenes/name"))
	{
		for (k = 0; k < *count; k++)
		{
			memcpy(discoveredScenes[k].sceneName, x.results[k], strlen(x.results[k]));
		}
		XmlCleanup(&x);
	}
		
	free(xmlc);
	return discoveredScenes ;
}
#endif

#ifdef EM
static s_scenelightlevel_t *discover_lightlevels_for_scene(s_scene_t *scene, unsigned int *count)
{
	char url[1024];
	char ofile[256];

	snprintf(url, sizeof(url), GET_LIGHTLEVELS_FOR_SCENE, elf_get_db_gems_ip_address_config(), scene->sceneId);
	snprintf(ofile,
		sizeof(ofile),
		"%s/%s_%d.%s",
		elf_bacnet_config.tmpFilePath,
		GET_LIGHTLEVEL_FOR_SCENE_XML,
		scene->sceneId,
		TMP_FILE_PREFIX);

	char *xmlc = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (xmlc == NULL)
	{
		// panic("Why?");
		return NULL ;
	}

	// show me the format
	unsigned int k;
	xml_parse_ctx_t x;
			
			// just in case the read is an empty one, make sure num_fixtures all set to 0 for now.
	*count = 0;
	if (XmlSetup(xmlc, &x, "sceneLevels/sceneLevel"))
	{
		*count = (unsigned int) x.num_results;
		XmlCleanup(&x);
	}
	
	if (*count == 0)
	{
		free(xmlc);
		return NULL ;
	}

	s_scenelightlevel_t *discoveredLightlevels =  (s_scenelightlevel_t *) calloc((int) *count, sizeof(s_scenelightlevel_t));
	if (discoveredLightlevels == NULL)		
	{
		log_printf(LOG_CRIT, "In %s: can't allocate memory for lightlevels!\n", __func__);
		exit(EXIT_FAILURE);
	}

	if (XmlSetup(xmlc, &x, "sceneLevels/sceneLevel/id"))
	{
		for (k = 0; k < *count; k++)
		{
			discoveredLightlevels[k].id = (unsigned int) atoi(x.results[k]);
		}
		XmlCleanup(&x);
	}

	if (XmlSetup(xmlc, &x, "sceneLevels/sceneLevel/switchid"))
	{
		for (k = 0; k < *count; k++)
		{
			discoveredLightlevels[k].switchId = (unsigned int) atoi(x.results[k]);
			if (discoveredLightlevels[k].switchId != scene->switchId)
			{
				// todo sreedhar - this fails???
				panic("Something wrong");
				// break ;
			}
		}
		XmlCleanup(&x);
	}

	if (XmlSetup(xmlc, &x, "sceneLevels/sceneLevel/sceneid"))
	{
		for (k = 0; k < *count; k++)
		{
			discoveredLightlevels[k].sceneId = (unsigned int) atoi(x.results[k]);
			if (discoveredLightlevels[k].sceneId != scene->sceneId)
			{
				// todo 3 sreedhar - this fails???
				panic("Something wrong");
				// break ;
			}
		}
		XmlCleanup(&x);
	}

	if (XmlSetup(xmlc, &x, "sceneLevels/sceneLevel/fixtureid"))
	{
		for (k = 0; k < *count; k++)
		{
			discoveredLightlevels[k].fixtureId = (unsigned int) atoi(x.results[k]);
		}
		XmlCleanup(&x);
	}

	if (XmlSetup(xmlc, &x, "sceneLevels/sceneLevel/lightlevel"))
	{
		for (k = 0; k < *count; k++)
		{
			discoveredLightlevels[k].lightLevel = (unsigned int) atoi(x.results[k]);
		}
		XmlCleanup(&x);
	}
		
	free(xmlc);
	return discoveredLightlevels  ;
}
#endif


#ifdef EM
static s_pluglevel_t *discover_pluglevels_for_scene(s_scene_t *scene, unsigned int *count)
{
	char url[1024];
	char ofile[256];

	snprintf(url, sizeof(url), GET_PLUGLEVELS_FOR_SCENE, elf_get_db_gems_ip_address_config(), scene->sceneId);
	snprintf(ofile,
		sizeof(ofile),
		"%s/%s_%d.%s",
		elf_bacnet_config.tmpFilePath,
		GET_PLUGLEVEL_FOR_SCENE_XML,
		scene->sceneId,
		TMP_FILE_PREFIX);

	char *xmlc = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (xmlc == NULL)
	{
		// panic("Why?");
		return NULL ;
	}

	// show me the format
	unsigned int k;
	xml_parse_ctx_t x;
			
			// just in case the read is an empty one, make sure num_fixtures all set to 0 for now.
	*count = 0;
	if (XmlSetup(xmlc, &x, "plugloadSceneLevels/plugloadSceneLevel"))
	{
		*count = (unsigned int) x.num_results;
		XmlCleanup(&x);
	}
	
	if (*count == 0)
	{
		free(xmlc);
		return NULL ;
	}

	s_pluglevel_t *discoveredPluglevels =  (s_pluglevel_t *) calloc((int) *count, sizeof(s_pluglevel_t));
	if (discoveredPluglevels == NULL)		
	{
		log_printf(LOG_CRIT, "In %s: can't allocate memory for plug levels!\n", __func__);
		exit(EXIT_FAILURE);
	}

	if (XmlSetup(xmlc, &x, "plugloadSceneLevels/plugloadSceneLevel/id"))
	{
		for (k = 0; k < *count; k++)
		{
			discoveredPluglevels[k].id = (unsigned int) atoi(x.results[k]);
		}
		XmlCleanup(&x);
	}

	if (XmlSetup(xmlc, &x, "plugloadSceneLevels/plugloadSceneLevel/switchid"))
	{
		for (k = 0; k < *count; k++)
		{
			discoveredPluglevels[k].switchId = (unsigned int) atoi(x.results[k]);
			if (discoveredPluglevels[k].switchId != scene->switchId)
			{
				// todo 4 sreedhar - resolve the whole switchId/switchgroupId thing
				panic("Something wrong");
				// break ;
			}
		}
		XmlCleanup(&x);
	}

	if (XmlSetup(xmlc, &x, "plugloadSceneLevels/plugloadSceneLevel/sceneid"))
	{
		for (k = 0; k < *count; k++)
		{
			discoveredPluglevels[k].sceneId = (unsigned int) atoi(x.results[k]);
			if (discoveredPluglevels[k].sceneId != scene->sceneId)
			{
				// todo 3 sreedhar - this fails???
				panic("Something wrong");
				// break ;
			}
		}
		XmlCleanup(&x);
	}

	if (XmlSetup(xmlc, &x, "plugloadSceneLevels/plugloadSceneLevel/plugloadid"))
	{
		for (k = 0; k < *count; k++)
		{
			discoveredPluglevels[k].plugloadId = (unsigned int) atoi(x.results[k]);
		}
		XmlCleanup(&x);
	}

	if (XmlSetup(xmlc, &x, "plugloadSceneLevels/plugloadSceneLevel/lightlevel"))
	{
		for (k = 0; k < *count; k++)
		{
			discoveredPluglevels[k].plugLevel = (unsigned int) atoi(x.results[k]);
		}
		XmlCleanup(&x);
	}
		
	free(xmlc);
	return discoveredPluglevels  ;
}
#endif




#ifdef EM
static void establish_fixture( s_sector_t *area, s_fixture_t2 *fixture)
{
	if (chkPtr(fixture)) return ;
	
	// does the fixture already exist?
	if (get_fixture_ptr(fixture->fixtureId)) return ;
	
	g_energy_manager->num_fixtures++;
	area->num_fixtures_in_area++;
	
	// does not exist, time to create one
	s_fixture_t2 *fixtureGroup = (s_fixture_t2 *) realloc(g_energy_manager->fixtures, sizeof(s_fixture_t2)*g_energy_manager->num_fixtures);
	if (fixtureGroup == NULL)
	{
		log_printf(LOG_CRIT, "In %s: can't allocate memory for areas!\n", __func__);
		exit(EXIT_FAILURE);
	}
	g_energy_manager->fixtures = fixtureGroup;
	
	s_fixture_t2 *newFixture = &g_energy_manager->fixtures[g_energy_manager->num_fixtures - 1];
	*newFixture = *fixture;
	
	qsort((void *) g_energy_manager->fixtures, g_energy_manager->num_fixtures, sizeof(s_fixture_t2), compare_fixture_id_sort);
}


static void establish_scene_for_switchgroup(s_switchgroup_t *switchgroup, s_scene_t *scene)
{
	unsigned int i;

	// does it already exist?
	
	for (i = 0; i < switchgroup->num_scenes; i++)
	{
		if (switchgroup->s_scenes[i].sceneId == scene->sceneId)
		{
			// already exists
			return ;
		}
	}
	
	switchgroup->num_scenes++;
	// does not exist, time to create one
	s_scene_t *newscenes = (s_scene_t *) realloc(switchgroup->s_scenes, sizeof(s_scene_t)*switchgroup->num_scenes);
	if (newscenes == NULL)
	{
		log_printf(LOG_CRIT, "In %s: can't allocate memory for scenes!\n", __func__);
		exit(EXIT_FAILURE);
	}
	switchgroup->s_scenes = newscenes;
	
	s_scene_t *newsceneptr = &switchgroup->s_scenes[switchgroup->num_scenes - 1];
	*newsceneptr = *scene;
	
	// todo 5 - need to qsort here, and remove qsorts on lookups?
}


static int compare_lightlevel(const void *a, const void *b)
{
	const s_scenelightlevel_t *aa = a;
	const s_scenelightlevel_t *bb = b;
	return (int) aa->id - (int) bb->id ;
}


static int compare_pluglevel(const void *a, const void *b)
{
	const s_pluglevel_t *aa = a;
	const s_pluglevel_t *bb = b;
	return (int) aa->id - (int) bb->id ;
}


static void establish_lightlevels_for_scene(s_scene_t *scene, s_scenelightlevel_t *lightlevel)
{
	// does it already exist?
	unsigned int i;
	for (i = 0; i < scene->num_lightlevels; i++)
	{
		if (scene->s_lightlevels[i].id == lightlevel->id)
		{
			// already exists
			return ;
		}
	}
	
	scene->num_lightlevels++;
	// does not exist, time to create one
	s_scenelightlevel_t *newslevels = (s_scenelightlevel_t *) realloc(scene->s_lightlevels, sizeof(s_scenelightlevel_t)*scene->num_lightlevels);
	if (newslevels == NULL)
	{
		log_printf(LOG_CRIT, "In %s: can't allocate memory for scenes!\n", __func__);
		exit(EXIT_FAILURE);
	}
	scene->s_lightlevels = newslevels;
	
	s_scenelightlevel_t *newlevelptr = &scene->s_lightlevels[scene->num_lightlevels - 1];
	*newlevelptr = *lightlevel;
	
	qsort(newslevels, scene->num_lightlevels, sizeof(s_scenelightlevel_t), compare_lightlevel);
}


static void establish_pluglevels_for_scene(s_scene_t *scene, s_pluglevel_t *pluglevel)
{
	// does it already exist?
	unsigned int i;
	for (i = 0; i < scene->num_pluglevels; i++)
	{
		if (scene->s_pluglevels[i].id == pluglevel->id)
		{
			// already exists
			return ;
		}
	}
	
	scene->num_pluglevels++;
	// does not exist, time to create one
	s_pluglevel_t *newlevels = (s_pluglevel_t *) realloc(scene->s_pluglevels, sizeof(s_pluglevel_t)*scene->num_pluglevels);
	if (newlevels == NULL)
	{
		log_printf(LOG_CRIT, "In %s: can't allocate memory for scenes!\n", __func__);
		exit(EXIT_FAILURE);
	}
	scene->s_pluglevels = newlevels;
	
	s_pluglevel_t *newlevelptr = &scene->s_pluglevels[scene->num_pluglevels - 1];
	*newlevelptr = *pluglevel;
	
	qsort(newlevels, scene->num_pluglevels, sizeof(s_pluglevel_t), compare_pluglevel);
}
#endif // EM


#ifdef EM
static void config_refresh_fixtures_for_area(s_sector_t *area)
{
	unsigned int count = 0, i;
	
	s_fixture_t2 *discoveredFixtures = discover_config_fixtures_by_area(area, &count);
	
	if (discoveredFixtures == NULL) return ;
	
	for (i = 0; i < count; i++)
	{
		establish_fixture( area, &discoveredFixtures[i]);
	}
	free(discoveredFixtures);
}
#endif // EM


#ifdef EM
static void config_refresh_scenes_for_floor_and_switchgroup(s_floor_t *floor, s_switchgroup_t *switchGroup)
{
	unsigned int count = 0, i;
	
	s_scene_t *discoveredScenes = discover_scenes_for_floor_and_switchgroup(floor, switchGroup, &count);
	
	if (discoveredScenes == NULL) return ;
	
	for (i = 0; i < count; i++)
	{
		establish_scene_for_switchgroup(switchGroup, &discoveredScenes[i]);
	}
	free(discoveredScenes);
}
#endif // EM


#ifdef EM
static void config_refresh_lightlevels_for_scene(s_scene_t *scene)
{
	unsigned int count = 0, i;
	
	s_scenelightlevel_t *discoveredLightLevels = discover_lightlevels_for_scene(scene, &count);
	
	if (discoveredLightLevels == NULL) return ;
	
	for (i = 0; i < count; i++)
	{
		establish_lightlevels_for_scene(scene, &discoveredLightLevels[i]);
	}
	free(discoveredLightLevels);
}


static void data_refresh_lightlevels_for_scene(s_scene_t *scene)
{
	// config_refresh_lightlevels_for_scene(scene);
	// EM-546
	uint count = 0, i;
	s_scenelightlevel_t *discoveredLightLevels = discover_lightlevels_for_scene(scene, &count);
	if (discoveredLightLevels == NULL) return ;
	
	for (i = 0; i < count; i++)
	{
		s_scenelightlevel_t *found = bsearch(&discoveredLightLevels[i], scene->s_lightlevels, scene->num_lightlevels, sizeof(s_scenelightlevel_t), compare_lightlevel);
		if (found != NULL)
		{
			// update that scene
			found->lightLevel = discoveredLightLevels[i].lightLevel;
		}
	}
	
	free(discoveredLightLevels);	
}
#endif // EM



#ifdef EM
static void config_refresh_plugloads_for_scene(s_scene_t *scene)
{
	unsigned int count = 0, i;
	
	s_pluglevel_t *discoveredPlugLevels = discover_pluglevels_for_scene(scene, &count);
	
	if (discoveredPlugLevels == NULL) return ;
	
	for (i = 0; i < count; i++)
	{
		establish_pluglevels_for_scene(scene, &discoveredPlugLevels[i]);
	}
	free(discoveredPlugLevels);
}


static void data_refresh_plugloads_for_scene(s_scene_t *scene)
{
	// EM-546 - replacing 'config' with a true update
	// config_refresh_plugloads_for_scene(scene);
	
	uint count = 0, i;
	s_pluglevel_t *discoveredPlugLevels = discover_pluglevels_for_scene(scene, &count);
	if (discoveredPlugLevels == NULL) return ;
	
	for (i = 0; i < count; i++)
	{
		s_pluglevel_t *found = bsearch(&discoveredPlugLevels[i], scene->s_pluglevels, scene->num_pluglevels, sizeof(s_pluglevel_t), compare_pluglevel);
		if (found != NULL)
		{
			// update that scene
			found->plugLevel = discoveredPlugLevels[i].plugLevel;
		}
	}
	free(discoveredPlugLevels);
}
#endif // EM


#ifdef EM
static s_switchgroup_t *discover_switches_for_floor(const unsigned int floorId, unsigned int *count)
{
	char url[1024];
	char ofile[256];

	snprintf(url, sizeof(url), GET_FLOOR_SWITCH_GROUPS, elf_get_db_gems_ip_address_config(), floorId);
	snprintf(ofile, sizeof(ofile), "%s/%s_%d.%s", elf_bacnet_config.tmpFilePath, GET_FLOOR_SWITCH_GROUPS_XML, floorId, TMP_FILE_PREFIX);

	char *xml8 = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (!xml8)
	{
		panic("Why?");
		*count = 0;
		return NULL ;
	}

	    // int err,
	unsigned int j;
	xml_parse_ctx_t x;

	if (!XmlSetup(xml8, &x, "gemsGroups/gemsGroup"))
	{
		// If no switch groups are defined, can end up here
		*count = 0;
		free(xml8);
		return NULL ;
	}
	
	*count = (unsigned int) x.num_results;
	XmlCleanup(&x);

	s_switchgroup_t *switches = (s_switchgroup_t *) calloc((int) *count, sizeof(s_switchgroup_t));
	if (switches == NULL)
	{
		log_printf(LOG_CRIT, "In %s: can't allocate memory for areas!\n", __func__);
		exit(EXIT_FAILURE);
	}

	//for (j = 0; j < floor->num_switches; j++)
	//{
		//floor->s_switch[j].valid = true;
	//}

	if (XmlSetup(xml8, &x, "gemsGroups/gemsGroup/id"))
	{
		for (j = 0; j < *count; j++)
		{
			switches[j].id = (unsigned int) atoi(x.results[j]); 
		}
		XmlCleanup(&x);
	}

	if (XmlSetup(xml8, &x, "gemsGroups/gemsGroup/name"))
	{
		for (j = 0; j < *count; j++)
		{
			strcpy(switches[j].switch_name, x.results[j]);
		}
		XmlCleanup(&x);
	}
	free(xml8);
	
	return switches ;
}

//static s_switchgroup_t *find_switch_ptr_by_floor_and_switchId(s_floor_t *floor, unsigned int id)
//{
	//unsigned int i;
	//if (chkPtr(floor)) return NULL ;
	//
	//// no switches defined (yet)
	//if (floor->s_switch == NULL) return NULL ;
	//
	//for (i = 0; i < floor->num_switches; i++)
	//{
		//if (floor->s_switch[i].id == id) return &floor->s_switch[i];
	//}
	//return NULL;
//}
//
//
//static s_switchgroup_t *establish_switch_by_floor_and_switch(s_floor_t *floor, s_switchgroup_t *newSwitch)
//{
	//if (chkPtr(floor)) return NULL;
	//if (chkPtr(newSwitch)) return NULL;
	//
	//s_switchgroup_t *switchGroup = find_switch_ptr_by_floor_and_switchId(floor, newSwitch->id);
	//if (switchGroup != NULL) return switchGroup ;
	//
	//floor->num_switches++; 
	//// does not exist, time to create one
	//switchGroup = (s_switchgroup_t *) realloc(floor->s_switch, sizeof(s_switchgroup_t)*floor->num_switches);
	//if (chkPtr(switchGroup))return NULL ;
	//floor->s_switch = switchGroup;
	//switchGroup = &floor->s_switch[floor->num_switches - 1];
	//*switchGroup = *newSwitch;
	//return switchGroup ;
//}


static void config_refresh_switches_for_floor(s_floor_t *floor)
{
	unsigned int count, i;
	
	s_switchgroup_t *newSwitches = discover_switches_for_floor(floor->id, &count);
	
	// if none are found, return 
	if (newSwitches == NULL) return ;

	// transfer the new switches into our memory structures, see if it exists, in the right place etc..
	
	for (i = 0; i < count; i++)
	{
		// establish_switch_by_floor_and_switch(floor, &newSwitches[i]);
		establish_switch(floor, &newSwitches[i]);
		// *newSwitch = newSwitches[i];		
	}
	free(newSwitches);
}
#endif // EM


static s_sector_t *discover_sectors_for_floor(unsigned int floorId, unsigned int *count)
{
	char url[1024];
	char ofile[256];

	snprintf(url, sizeof(url), GET_AREA_LIST_API, elf_get_db_gems_ip_address_config(), floorId);
	snprintf(ofile,
		sizeof(ofile),
		"%s/%s_%u.%s",
		elf_bacnet_config.tmpFilePath,
		GET_CONFIG_AND_DATA_SECTORS_XML,
		floorId,
		TMP_FILE_PREFIX);


	*count = 0;
	char *xml8 = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (chkPtr(xml8)) return NULL ;
	
	unsigned int j;
	xml_parse_ctx_t x;

	
#ifdef EM		
	if (XmlSetup(xml8, &x, "areas/area"))
#else		if (XmlSetup(xml8, &x, "hVACZoneVOes/hvacZoneVO"))
#endif		
		{
			*count = (unsigned int) x.num_results;
			XmlCleanup(&x);
		}
		else
		{
			// could not read anything at all
			free(xml8);
			return NULL;
		}
	if (*count == 0) return NULL;
		
	s_sector_t *newSectors = (s_sector_t *) calloc((int) *count, sizeof(s_sector_t));
	if (newSectors == NULL)
	{
		log_printf(LOG_CRIT, "In %s: can't allocate memory for areas!\n", __func__);
		exit(EXIT_FAILURE);
	}
	

	// Get the id of sector
#ifdef EM	
	if (XmlSetup(xml8, &x, "areas/area/id"))
#else		if (XmlSetup(xml8, &x, "hVACZoneVOes/hvacZoneVO/id"))
#endif // EM
		{
			// todo 3 we need to put checks in here, what happens if a bad xml file with fewer than expected items is presented. seg fault?
			for (j = 0; j < *count; j++)
			{
				newSectors[j].id = (unsigned int) atoi(x.results[j]);
			}
			XmlCleanup(&x);
		}

						                    // Get the name of area
#ifdef UEM
	if (XmlSetup(xml8, &x, "hVACZoneVOes/hvacZoneVO/name"))
#else
		if (XmlSetup(xml8, &x, "areas/area/name"))
#endif
		{
			for (j = 0; j < *count; j++)
			{
				memcpy(newSectors[j].za_name, x.results[j], strlen(x.results[j]));
			}
			XmlCleanup(&x);
		}
	free(xml8);

	return newSectors;
}

//static void FreeOidLists(s_bacObjIDlistForBACtype_t *oidLists)
//{
	//// not every oidLists gets created. Need to check, and not complain if it does not exist.
	//if (oidLists)
	//{
		//int k;
		//for (k = 0; k < MAX_BACNET_TYPES_PER_ELF_DEVICE; k++)
		//{
			//if (oidLists[k].oid)
			//{
				//free(oidLists[k].oid);
			//}
			//else
			//{
				//panic("OID sublist is NULL");
			//}
		//}
		//free(oidLists);
	//}
//}

static void TestThenFree(void *ptr)
{
	if (ptr != NULL) free(ptr);
}


// FreeEnergyManagerMemory is not to be used during operation anymore, it is only included to allow controlled
// freeing of mem during exit for memory leak testing, etc.
void FreeEnergyManagerMemory(void)
{
	if (g_energy_manager)
	{
		unsigned int i, j;
		for (i = 0; i < g_energy_manager->num_floors; i++)
		{
#ifdef EM
			for (j = 0; j < g_energy_manager->floors[i].num_sectors; j++)
			{
				TestThenFree(g_energy_manager->floors[i].s_sector[j].plugLoads);
			}
			TestThenFree(g_energy_manager->floors[i].s_switch);
#endif
			TestThenFree(g_energy_manager->floors[i].s_sector);
		}
		TestThenFree(g_energy_manager->floors);
#ifdef EM
		TestThenFree(g_energy_manager->fixtures);
#endif
		free(g_energy_manager);
		g_energy_manager = NULL;
	}
	
	// todo: deal with getpwnam leak: https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=273051

	TestThenFree(elf_template_objs);
	free(g_bacnet_device_list);
	g_bacnet_device_list = NULL;
	// free(Devices);
}

//static s_sector_t *find_sector_ptr_by_floor_and_sectorId(s_floor_t *floor, unsigned int id)
//{
	//unsigned int i;
	//if (chkPtr(floor)) return NULL ;
	//
	//// no sectors defined (yet)
	//if (floor->s_sector == NULL) return NULL ;
	//
	//for (i = 0; i < floor->num_sectors; i++)
	//{
		//if (floor->s_sector[i].id == id) return &floor->s_sector[i];
	//}
	//return NULL;
//}

//static s_sector_t *establish_sector_by_floor_and_sector(s_floor_t *floor, s_sector_t *newSector)
//{
	//if (chkPtr(floor)) return NULL;
	//if (chkPtr(newSector)) return NULL;
	//
	//s_sector_t *sector = find_sector_ptr_by_floor_and_sectorId(floor, newSector->id);
	//if (sector != NULL) return sector ;
	//
	//floor->num_sectors++;
	//// does not exist, time to create one
	//sector = (s_sector_t *) realloc(floor->s_sector, sizeof(s_sector_t)*floor->num_sectors);
	//if (chkPtr(sector))return NULL ;
	//floor->s_sector = sector;
	//sector = &floor->s_sector[floor->num_sectors - 1];
	//*sector = *newSector;
	//
	//// since it is new, add to the database
	//
	//elf_bacnet_db_t bacnet_db;
	//memset((void *) &bacnet_db, 0, sizeof(bacnet_db));
	            //
	//bacnet_db.bacnetDeviceInstance = g_first_sector_device_instance + 
		//floor->id * FLOOR_OFFSET_ID + 
		//sector->id;
	            //
	//bacnet_db.floor_id = floor->id;
	//bacnet_db.sector_id = sector->id;
	//bacnet_db.elf_dev_type = ELF_DEVICE_AREA;
//
	//add_to_bacnet_device_list(&bacnet_db);
	//
	//return sector ;
//}


//#ifdef EM
//static s_sector_t *establish_sector_by_floor_and_sectorId(s_floor_t *floor, unsigned int sectorId)
//{
	//if (chkPtr(floor)) return NULL;
	//
	//s_sector_t *sector = find_sector_ptr_by_floor_and_sectorId(floor, sectorId);
	//if (sector != NULL) return sector ;
	//
	//// does not exist, time to create one
	//floor->num_sectors++;
	//sector = (s_sector_t *) realloc(floor->s_sector, sizeof(s_sector_t));
	//if (chkPtr(sector))return NULL ;
	//memset(sector, 0, sizeof(s_sector_t));
	//floor->s_sector = sector;
	//
	//sector = &floor->s_sector[floor->num_sectors - 1];
	//sector->id = sectorId;
	//strncpy(sector->za_name, "Unassigned", MX_NAME);
	//
	//return sector ;
//}
//#endif // EM


static void config_refresh_sectors_for_floor(s_floor_t *floor)
{
	unsigned int count, i;
	
// As of 2015.11.14, no longer going to create unassigned areas for Lighting Areas.
//#ifdef EM	
	//// make sure there is a sector 0 (unassigned) at least for EMs
	//establish_sector(floor->id, 0);
//#endif
	s_sector_t *newSectors = discover_sectors_for_floor(floor->id, &count);
	
	// if none are found, return 
	if (newSectors == NULL) return ;

	// transfer the new sectors into our memory structures, see if it exists, in the right place etc..
	
	for (i = 0; i < count; i++)
	{
		if (&newSectors[i].id == 0) 
		{
			continue ;
		}
		establish_sector(floor, &newSectors[i]);
	}
	free(newSectors);
}


// returns NULL if no floors are discovered
static s_floor_t *discover_floors(unsigned int *numFloorsOut)
{
	char url[1024];
	char ofile[256];
	s_floor_t	*floorStructures;

	snprintf(url, sizeof(url), GET_FLOOR_LIST_API, elf_get_db_gems_ip_address_config());
	snprintf(ofile,
		sizeof(ofile),
		"%s/%s.%s",
		elf_bacnet_config.tmpFilePath,
		FLOORS_XML,
		TMP_FILE_PREFIX);
	if (!muteFlag)
	{
		unlink(ofile);
	}

	char *xml9 = XMLgetIntoBuffer(REQ_TYPE_GET, url, ofile);
	if (xml9 == NULL)
	{
		*numFloorsOut = 0;
		// panic("Null");
		return NULL ;
	}

	// At this point all the data from the file has been read.
	// we can now parse the xml and get the list of floors.
	// int err,
	unsigned int i;
	xml_parse_ctx_t x;

	// Get the list of floors.
	if (XmlSetup(xml9, &x, "floors/floor"))
	{
		*numFloorsOut = (unsigned int) x.num_results;
		
		if (*numFloorsOut == 0) return NULL ;

		if (!(floorStructures = (s_floor_t *) calloc((int) *numFloorsOut, sizeof(s_floor_t))))
		{
			log_printf(LOG_CRIT, "In %s: can't allocate memory for floors!\n", __func__);
			exit(EXIT_FAILURE);
		}
		XmlCleanup(&x);
	}
	else
	{
		*numFloorsOut = 0;
		free(xml9);
		return NULL ;
	}

    // Get the id of floors
	if (XmlSetup(xml9, &x, "floors/floor/id"))
	{
		for (i = 0; i < *numFloorsOut; i++)
		{
			floorStructures[i].id = (unsigned int) atoi(x.results[i]);
		}
		XmlCleanup(&x);
	}

    // Get the name of floors
	if (XmlSetup(xml9, &x, "floors/floor/name"))
	{
		for (i = 0; i < *numFloorsOut; i++)
		{
			memcpy(floorStructures[i].floor_name, x.results[i], strlen(x.results[i]));
		}
		XmlCleanup(&x);
	}

    // Get the building name of floors
	if (XmlSetup(xml9, &x, "floors/floor/buildingName"))
	{
		for (i = 0; i < *numFloorsOut; i++)
		{
			memcpy(floorStructures[i].bldg_name, x.results[i], strlen(x.results[i]));
		}
		XmlCleanup(&x);
	}

    // Get the campus name of floors
	if (XmlSetup(xml9, &x, "floors/floor/campusName"))
	{
		for (i = 0; i < *numFloorsOut; i++)
		{
			memcpy(floorStructures[i].campus_name, x.results[i], strlen(x.results[i]));
		}
		XmlCleanup(&x);
	}

   // Get the Company name of floors
#ifdef UEM
    // todo, confirm this difference in APIs is deliberate/will persist...
	if (XmlSetup(xml9, &x, "floors/floor/companyName"))
#else
		if (XmlSetup(xml9, &x, "floors/floor/organization"))
#endif // UEM/EM
		{
			for (i = 0; i < *numFloorsOut; i++)
			{
				memcpy(floorStructures[i].company_name, x.results[i], strlen(x.results[i]));
			}
			XmlCleanup(&x);
		}
	
	free(xml9);

	return floorStructures ;
}


ELF_RETURN config_refresh_site(void)
{
	unsigned int i, numFloors;

	s_floor_t *floorStructures = discover_floors(&numFloors);

	if (!floorStructures)
	{
		return ELF_RETURN_FAIL ;
	}
	
	// now, for each of the newly discovered floors, check to see if we already know about it or not, and if not
	// create a new floor entry (and discover further information about that floor if new)
	
	for (i = 0; i < numFloors; i++)
	{
		s_floor_t *floor = establish_floor(&floorStructures[i]);
		
		// cant do this - overwrites all sorts of parameters.. pointers, counts, etc. *floor = floorStructures[i];

		
		// Now for each floor, new or already existing, check for any new areas/switches
		config_refresh_sectors_for_floor(floor);
		
#ifdef EM	
		// 12/1 In detailed mode, still have to gather energy stats for aggregation..
		unsigned int sector;
		for (sector = 0; sector < floor->num_sectors; sector++)
		{
			config_refresh_fixtures_for_area(&floor->s_sector[sector]);				
			config_refresh_plugloads_for_area(&floor->s_sector[sector]);
		}
		
		// 12/1 Enable switches in all modes
		config_refresh_switches_for_floor(floor);
			
		uint sw;
		for (sw = 0; sw < floor->num_switches; sw++)
		{
			config_refresh_scenes_for_floor_and_switchgroup(floor, &floor->s_switch[sw]);
				
			uint scene;
			for (scene = 0; scene < floor->s_switch[sw].num_scenes; scene++)
			{
				config_refresh_lightlevels_for_scene(&floor->s_switch[sw].s_scenes[scene]);
				config_refresh_plugloads_for_scene(&floor->s_switch[sw].s_scenes[scene]);
			}
				
		}
		
#endif //EM
	}
	
	free(floorStructures);

#ifdef EM
	// todo - in special mode
	// and finally, don't forget area 0
	s_sector_t	dummyArea;
	dummyArea.id = 0;
	config_refresh_fixtures_for_area(&dummyArea);
#endif // EM
	
#if 0	
	/* Write bacnet database to file. */
	// todo 5 - use a change flag to save disk...
	if (write_bacnet_device_list() < 0)
	{
		log_printf(LOG_CRIT, "Failed to write BACnet database.");
		exit(EXIT_FAILURE);
	}
#endif	
	return ELF_RETURN_OK ;
}


void data_refresh_for_site(void)
{
	unsigned int iFloor, iSector;
	
#ifdef EM	
	elf_update_energy_manager();
	data_refresh_em_demandResponse();
	data_refresh_occupancy();
#endif // EM	
	for (iFloor = 0; iFloor < g_energy_manager->num_floors; iFloor++)
	{
		s_floor_t *floor = &g_energy_manager->floors[iFloor];

#ifdef EM		
		get_area_energy_data_per_floor(floor);
		
		uint sw;
		for (sw = 0; sw < floor->num_switches; sw++)
		{
			uint scene;
			for (scene = 0; scene < floor->s_switch[sw].num_scenes; scene++)
			{
				data_refresh_lightlevels_for_scene(&floor->s_switch[sw].s_scenes[scene]);
				data_refresh_plugloads_for_scene(&floor->s_switch[sw].s_scenes[scene]);
			}
		}		
#endif // EM		

		for (iSector = 0; iSector < floor->num_sectors; iSector++)
		{
			
#ifdef EM		
			s_sector_t *sector = &floor->s_sector[iSector];
			
			data_refresh_fixture_outages_for_area(sector);
			data_refresh_plugload_consumption_for_area(sector);
			data_refresh_emergency_for_area(sector);
			data_refresh_plugloads_for_area(sector);
			
#else			
			get_hvac_stats_per_zone(g_energy_manager->floors[iFloor].s_sector[iSector].id);
#endif // EM/UEM
			
		}
	}
}


void notify_gui(const char *fmt, ...)
{
	uint i;
	char	messageTxt[1000];
	//lint -esym(530,ap)
	va_list ap;			
	
	va_start(ap, fmt);
	vsprintf(messageTxt, fmt, ap);
	va_end(ap);
	
	// strip special characters
	for (i = 0; i < strlen(messageTxt); i++)
	{
		if (messageTxt[i] == '[') messageTxt[i] = '(';
		if (messageTxt[i] == ']') messageTxt[i] = ')';
	}

	// Add bacnet event: https://enlightedinc.atlassian.net/wiki/display/EM/API+Extensions#APIExtensions-AddBACnetEvent
	
	char ifile[256];
	char ofile[256];
	char cmd[1024];
	char url[1024];

	snprintf(ifile,
		sizeof(ifile),
		"%s/%s.%s",
		elf_bacnet_config.tmpFilePath,
		POST_BACNET_EVENT_IN_XML,
		TMP_FILE_PREFIX);

	snprintf(cmd, sizeof(cmd), "echo \"<eventsAndFault><severity>Critical</severity><eventType>Bacnet</eventType><description>%s</description></eventsAndFault>\" > %s", messageTxt, ifile);
	if (system(cmd) < 0)
	{
		log_printf(LOG_CRIT, "In %s: can't execute command %s!\n", __func__, cmd);
		exit(EXIT_FAILURE);
	}
	// todo 2 - remove these sleeps when API CPU load issues resolved
	sleep(1);

	snprintf(url, sizeof(url), POST_BACNET_EVENT, elf_get_db_gems_ip_address_config()); 

	snprintf(ofile,
		sizeof(ofile),
		"%s/%s.%s",
		elf_bacnet_config.tmpFilePath,
		POST_BACNET_EVENT_OUT_XML,
		TMP_FILE_PREFIX);

	send_curl_message(REQ_TYPE_POST, url, ifile, ofile);
}
