#ifndef __ELF_FUNCTIONS_H__
#define __ELF_FUNCTIONS_H__

#include "elf_std.h"
#include "elf_gems_api.h"
#include "elf_objects.h"

ts_elf_template_object_t *find_template_object_record_em(e_elf_category_t category, BACNET_OBJECT_TYPE bacnet_object_type, uint32_t objectInstance);
ts_elf_template_object_t *find_template_object_record_current_device(BACNET_OBJECT_TYPE bacnet_object_type, uint32_t objectInstance);

extern int elf_read_config_file(const char *config_file);
extern const char *elf_get_device_object_name(elf_bacnet_db_t *dbPtr, char *name, int name_len);
extern int elf_set_device_system_status(void);
extern int elf_get_config(int cfg_type);
// extern void elf_set_device_count(unsigned int count);
// extern int elf_create_bacnet_devices(void);
extern const char *elf_get_db_file_config(void);
extern const char *elf_get_db_gems_ip_address_config(void);
extern const char *elf_get_db_proxy_ip_address_config(void);
extern const char *elf_get_db_interface(void);
extern void elf_init_config(void);
extern void elf_device_init(DEVICE_OBJECT_DATA *ptr);

// extern void elf_devices_setup(void);

extern const char *elf_get_version(void);
extern int elf_get_mac_address(unsigned char mac[]);

#ifdef EM
uint elf_get_template_object_count_per_category(e_elf_category_t category, BACNET_OBJECT_TYPE bacnet_object_type);
#else
uint elf_get_template_object_count(BACNET_OBJECT_TYPE bacnet_object_type);
#endif
extern int8_t   elf_object_template_setup(void);

// extern int   read_bacnet_device_db(void);
// extern int   write_bacnet_device_list(void);
extern void  add_to_bacnet_device_list(elf_bacnet_db_t *data);
// extern void  mark_bacnet_device_list(void);
// bool check_for_duplicate_instances(void);

elf_bacnet_db_t *elf_get_bacnet_db_ptr_specific_device(const uint32_t bacnet_id);

#ifdef EM
elf_bacnet_db_t *get_bacnet_db_ptr_by_switch_id(uint switchId);
// extern void  update_bacnet_device_list(elf_bacnet_db_t *data);
#endif

extern uint32_t elf_get_current_bacnet_device_instance(void);

void elf_index_to_object_instance(uint objectIndex,
		BACNET_OBJECT_TYPE object_type,
        uint32_t *object_instance);
uint32_t elf_index_to_object_instance_new(const uint32_t bacnetDeviceInstance, uint objectIndex, BACNET_OBJECT_TYPE object_type);
// uint get_switchgroup_object_count(uint32_t bacnetDeviceInstance, BACNET_OBJECT_TYPE objectType);

extern uint elf_get_object_count_for_current_device(BACNET_OBJECT_TYPE object_type);

// extern ts_elf_template_object_t *elf_get_object_template( BACNET_OBJECT_TYPE obj_type, uint32_t object_instance ) ;
extern const char *elf_get_objects_file(void);
extern void get_mac_address(uint32_t bacnet_id, uint8_t *mac);
extern int elf_get_my_id(const char *name, int *uid, int *gid);

extern void elf_update_timer_init(void);
extern void elf_update_timer_handler(void);
ELF_RETURN   config_refresh_site(void);

#ifdef EM
unsigned int   elf_get_number_of_fixtures_in_area(uint32_t bacnetDeviceInstance);
unsigned int   elf_get_number_of_plugloads_in_area(uint32_t bacnetDeviceInstance);
#endif

s_sector_t *get_sector_ptr_by_instance(uint32_t deviceInstance);
// s_sector_t *get_sector_ptr_by_id( unsigned int id );

#ifdef EM
s_fixture_t2 *get_area_fixture_ptr(uint32_t object_instance);
s_fixture_t2 *get_nth_fixture_ptr_for_area(uint areaId, uint fixtureIndex);
s_fixture_t2 *get_fixture_ptr(uint fixtureId );
s_plugload_t *get_area_plugload_ptr(uint32_t bacnet_id, uint32_t object_instance);
s_switchgroup_t *get_switch_ptr(uint32_t deviceInstance);
void trigger_rediscovery(void);
#endif

s_energy_manager_t *get_energy_manager_ptr(void);

extern const char *elf_get_device_name_format_string(e_elf_device_t devType);

#ifdef EM
extern const char *elf_get_object_name_format_string(e_elf_category_t elfCategory);
#else
extern const char *elf_get_object_name_format_string(void);
#endif

extern BACNET_BINARY_PV elf_get_bi_present_value(uint32_t object_instance);

extern const char *elf_get_bacnet_description(BACNET_OBJECT_TYPE type,  uint32_t object_instance);
extern BACNET_ENGINEERING_UNITS elf_get_bacnet_units(BACNET_OBJECT_TYPE type,  uint32_t object_instance);

// extern float elf_get_ai_present_value(uint32_t object_instance);

#ifdef UEM
extern const char *elf_get_rest_username(void);
extern const char *elf_get_rest_password(void);
#else
extern const char *elf_get_rest_api_key(void);
extern const char *elf_get_rest_api_secret(void);
void get_auth_token(const char *ts, uint8_t *token) ;
#endif

#ifdef UEM
ELF_RETURN set_elf_value(
    uint32_t deviceInstance,
	ELF_DATA_TYPE elfObjectType,
	float value);
#else
ELF_RETURN set_switch_scene(int switchId, int sceneId);
ELF_RETURN set_switch_dim_level(int floor_id, char *switch_name, int dim_level);
ELF_RETURN set_fixture_dim_level( unsigned int object_instance, unsigned int value);
ELF_RETURN set_area_emergency(BACNET_WRITE_PROPERTY_DATA *wp_data, unsigned int value);
ELF_RETURN set_energy_manager_emergency(BACNET_WRITE_PROPERTY_DATA *wp_data, unsigned int value);
ELF_RETURN set_plugload_state(const BACNET_WRITE_PROPERTY_DATA *wp_data, unsigned int value);
void data_refresh_occupancy(void);
const char *elf_get_object_name_for_specific_device(uint32_t bacnetDeviceInstance, BACNET_OBJECT_TYPE obj_type, uint32_t object_instance, char *name);
#endif // UEM/EM

const char *elf_get_object_name_for_current_device(BACNET_OBJECT_TYPE obj_type, uint32_t object_instance, char *name);

void data_refresh_for_site(void);
void log_printf(int level, const char *fmt, ...);
bool is_object_instance_valid(uint32_t instance, BACNET_OBJECT_TYPE bacnet_object_type);
// void UpdateName(char *targetString, const char token, const char *insertString);
const char *IntToText(const INT_TO_TEXT dict[], int value);
void FreeEnergyManagerMemory(void);

void establish_energy_manager(uint32_t emId);
s_floor_t *establish_floor(const s_floor_t *floor);
s_sector_t *establish_sector( s_floor_t *floorId, const s_sector_t *sector);

#ifdef EM
s_switchgroup_t *establish_switch( s_floor_t *floorId, const s_switchgroup_t *switchPtr);
s_scene_t* get_switch_scene_ptr(uint32_t bacnetDeviceInstance, uint32_t object_instance);
s_scenelightlevel_t* get_switch_scene_fixture_ptr(uint32_t bacnetDeviceInstance, uint32_t object_instance);
s_pluglevel_t* get_switch_scene_plugload_ptr(uint32_t bacnetDeviceInstance, uint32_t object_instance);
ELF_RETURN set_em_demandResponse_level(s_energy_manager_t *em, float level);
#endif

void elf_initialize_device_addresses(elf_bacnet_db_t *ptr, int networkNumber);
void notify_gui(const char *fmt, ...);

#endif /* __ELF_FUNCTIONS_H__ */
