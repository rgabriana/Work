#include <syslog.h>
#include <time.h>
#include <setjmp.h>		// just to suppress a warning
#include <pthread.h>

#include "elf.h"
#include "elf_std.h"
#include "elf_config.h"
#include "advdebug.h"

static time_t update_config_time;
static time_t update_sector_time;

#ifdef EM
static time_t update_occupancy_time;
#endif

extern pthread_mutex_t ourMutex;

// todo - this is a good place to split the configuration/data refresh timers...

void elf_update_timer_init(void)
{
    update_config_time = time(NULL);
    update_sector_time = time(NULL);
#ifdef EM
    update_occupancy_time = time(NULL);
#endif
}


#ifdef EM
void trigger_rediscovery(void)
{
	update_config_time = 0;
}
#endif


void elf_update_timer_handler(void)
{
    time_t now = time(NULL);

    if ((now - update_config_time) >= elf_get_config(CFG_UPDATE_CONFIG_TIMEOUT))
    {
        update_config_time = time(NULL);
        log_printf(LOG_INFO, "Update Configuration");
	    pthread_mutex_lock(&ourMutex);
		config_refresh_site();
	    pthread_mutex_unlock(&ourMutex);
	    
	    pthread_mutex_lock(&ourMutex);
	    data_refresh_for_site();
	    pthread_mutex_unlock(&ourMutex);
    }

    if ((now - update_sector_time) >= elf_get_config(CFG_UPDATE_SECTOR_TIMEOUT))
    {
        update_sector_time = time(NULL);
        log_printf(LOG_INFO, "Update Area/Zone/Energy Manager/Switches");
	    pthread_mutex_lock(&ourMutex);
	    data_refresh_for_site();
	    pthread_mutex_unlock(&ourMutex);
    }

#ifdef EM
    if ((now - update_occupancy_time) >= elf_get_config(CFG_UPDATE_OCCUPANCY_TIMEOUT))
    {
        update_occupancy_time = time(NULL);
        log_printf(LOG_INFO, "Update occupancy data - time to poll");
	    pthread_mutex_lock(&ourMutex);
        data_refresh_occupancy();
	    pthread_mutex_unlock(&ourMutex);
    }
#endif

}

