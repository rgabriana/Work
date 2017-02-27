select update_all_sequences();

select addTriggers();

DROP TRIGGER ec_cc_update ON energy_consumption;
DROP FUNCTION cloud_config_update_ec_last_capture_at();
update cloud_config set val=-1 where name='ec_last_capture_at';

DROP TRIGGER ecd_cc_update ON energy_consumption_daily;
DROP FUNCTION cloud_config_update_ecd_last_capture_at();
update cloud_config set val=-1 where name='ecd_last_capture_at';

DROP TRIGGER ech_cc_update ON energy_consumption_hourly;
DROP FUNCTION cloud_config_update_ech_last_capture_at();
update cloud_config set val=-1 where name='ech_last_capture_at';

DROP TRIGGER plugload_ec_cc_update ON plugload_energy_consumption;
DROP FUNCTION cloud_config_update_plugload_ec_last_capture_at();
update cloud_config set val=-1 where name='plugload_ec_last_capture_at';

DROP TRIGGER plugload_ecd_cc_update ON plugload_energy_consumption_daily;
DROP FUNCTION cloud_config_update_plugload_ecd_last_capture_at();
update cloud_config set val=-1 where name='plugload_ecd_last_capture_at';

DROP TRIGGER plugload_ech_cc_update ON plugload_energy_consumption_hourly;
DROP FUNCTION cloud_config_update_plugload_ech_last_capture_at();
update cloud_config set val=-1 where name='plugload_ech_last_capture_at';

select add_triggers();

update cloud_config set val = (select extract(epoch from date_trunc('milliseconds', now())) * 1000) where name = 'successful.sync.time';

update cloud_config set val = '0' where name = 'remigration.required';
