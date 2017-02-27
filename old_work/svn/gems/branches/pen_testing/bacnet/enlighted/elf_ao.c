#include "elf.h"
// #include "elf_gems_api.h"

float   dr_value, dr_time;
uint8_t dr_enabled;

//enum profile_bmap
//{
    //PROFILE_ID = 0,       // 0
    //PROFILE_TYPE,         // 1
    //PROFILE_VALUE,        // 2
    //PROFILE_DURATION,     // 3
    //PROFILE_APPLY,        // 4
    //// PROFILE_LAST must be the last enum.
    //PROFILE_LAST, // this tells how many bits need to be on before applying the
                  //// profile.
//};

//typedef struct elf_su_profile
//{
    //uint16_t profile_id;
    //uint8_t  type;
    //int16_t  value;
    //uint32_t duration;
    //uint16_t bmap; // bitmap represents if all objects are received.
//} __attribute__ ((__packed__)) elf_su_profile_t;
//
//elf_su_profile_t g_elf_su_profile_object;


int16_t g_profile_id = -1;
//float elf_get_ao_present_value(uint32_t object_instance)
//{
//	return elf_gen_ana_get_present_value ( OBJECT_ANALOG_OUTPUT, object_instance ) ;
//
//}


