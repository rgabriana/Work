#include "elf_std.h"
#include "elf.h"
#include "elf_gems_api.h"
#include "bacenum.h"
#include "elf_objects.h"

//float elf_get_ai_present_value(uint32_t object_instance)
//{
    //return elf_gen_ana_get_present_value(OBJECT_ANALOG_INPUT, object_instance);
//}



//const char *elf_get_bacnet_name(BACNET_OBJECT_TYPE type,  uint32_t object_instance, char *name)
//{
//    return elf_get_object_name(type, object_instance, name);
//}


const char *elf_get_bacnet_description( BACNET_OBJECT_TYPE type,  uint32_t object_instance)
{
    ts_elf_template_object_t *ptr = find_template_object_record_current_device( type, object_instance);
    if (ptr == NULL )
    {
        return "Unknown";
    }
    return ptr->description;
}


BACNET_ENGINEERING_UNITS elf_get_bacnet_units(BACNET_OBJECT_TYPE type,  uint32_t object_instance)
{
	ts_elf_template_object_t *eod = find_template_object_record_current_device(type, object_instance);
    if (eod != NULL)
    {
        return eod->units;
    }
    return UNITS_NO_UNITS;
}


