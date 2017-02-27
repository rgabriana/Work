#include <syslog.h>
#include "elf.h"
#include "elf_gems_api.h"
#include "advdebug.h"
#include "elf_functions.h"
#include "elf_objects.h"

//static void elf_get_device_mac_address(uint8_t *mac_address)
//{
    //BACNET_ADDRESS address;
//
    //routed_get_my_address(&address);
//
    //if (mac_address)
    //{
        //memcpy(mac_address, address.adr, 6);
    //}
//}




//const char *elf_get_device_app_sw_version(char *name, int name_len)
//{
    //snprintf(name, name_len, "%s", "0.0.0");
    //return name;
//}

int elf_set_device_system_status(void)
{
// int                   rc = 0;
    BACNET_DEVICE_STATUS system_status = STATUS_OPERATIONAL;

    uint32_t id = elf_get_current_bacnet_device_instance();
	s_sector_t *ptr = get_sector_ptr_by_instance(id);
    if (ptr)
    {
        // Found zone
	    // todo 3
        //if (ptr->state != DEVICE_STATE_VALID)
        //{
            //system_status = STATUS_NON_OPERATIONAL;
        //}
        // free(ptr);
    }
    else
    {
        system_status = STATUS_NON_OPERATIONAL;
    }
// rc =
    Device_Set_System_Status(system_status, true );
    return 0;
}

