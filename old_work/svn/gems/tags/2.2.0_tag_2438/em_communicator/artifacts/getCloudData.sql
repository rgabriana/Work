CREATE OR REPLACE FUNCTION getCloudData() RETURNS VOID  AS $$
DECLARE 
    output varchar;
BEGIN
    COPY ( SELECT id, address, name, contact, email, completion_status, self_login, valid_domain, notification_email, severity_level, timezone, price, time_zone, profile_handler_id, tenant_id, sweep_timer_id FROM company order by id) TO '/tmp/new_cloud_company' with delimiter as '~';

    COPY ( SELECT id, name, location, zipcode, company_id, profile_handler_id, tenant_id, sweep_timer_id FROM campus order by id) TO '/tmp/new_cloud_campus' with delimiter as '~';

    COPY ( SELECT id, name, campus_id, profile_handler_id, tenant_id, sweep_timer_id FROM building order by id) TO '/tmp/new_cloud_building' with delimiter as '~';

    COPY ( SELECT id, name, description, building_id, floorplan_url, no_installed_sensors, no_installed_fixtures, floor_plan_uploaded_time, profile_handler_id, plan_map_id, tenant_id, sweep_timer_id FROM floor order by id) TO '/tmp/new_cloud_floor' with delimiter as '~';

    COPY ( SELECT id, name, description, floor_id, areaplan_url, plan_map_id, profile_handler_id, tenant_id, sweep_timer_id FROM area order by id) TO '/tmp/new_cloud_area' with delimiter as '~';

    COPY ( SELECT id, fixture_name, mac_address, floor_id, area_id, version, x, y, snap_address, gateway_id, firmware_version, bootloader_version,  cu_version, version_synced, bulbs_last_service_date, ballast_last_service_date, ip_address, baseline_power, state, sub_area_id, profile_id, type, ballast_type, ballast_last_changed, campus_id, building_id,  no_of_bulbs, bulb_wattage, wattage, ballast_manufacturer, bulb_manufacturer, profile_handler_id, current_profile, original_profile_from, dimmer_control, current_state, savings_type, last_occupancy_seen, light_level,  channel,  aes_key, location,  description, notes,  active, ballast_id, bulb_id, bulb_life, no_of_fixtures, last_connectivity_at,  comm_type, last_stats_rcvd_time, profile_checksum, global_profile_checksum, curr_app,  group_id, sec_gw_id, upgrade_status, push_profile, push_global_profile, last_cmd_sent, last_cmd_sent_at, last_cmd_status, avg_temperature, voltage, commission_status, is_hopper,  temperature_offset, last_boot_time,  model_no, sensor_id FROM fixture order by id) TO '/tmp/new_cloud_fixture' with delimiter as '~';

    COPY ( SELECT id, gateway_name, floor_id, x, y, mac_address, app1_version, app2_version, ip_address, snap_address, boot_loader_version, no_of_sensors, status, commissioned, unique_identifier_id, port, gateway_type, serial_port, channel, campus_id, building_id, wireless_networkid, wireless_enctype, wireless_enckey, wireless_radiorate, eth_sec_type, eth_sec_integritytype, eth_sec_enctype, eth_sec_key, eth_ipaddrtype, aes_key,  user_name, password, curr_uptime, curr_no_pkts_from_gems, curr_no_pkts_to_gems, curr_no_pkts_from_nodes, curr_no_pkts_to_nodes, last_connectivity_at, last_stats_rcvd_time, subnet_mask, default_gw, upgrade_status, location, no_of_wds FROM gateway order by id) TO '/tmp/new_cloud_gateway' with delimiter as '~';

END;
$$ LANGUAGE plpgsql;

