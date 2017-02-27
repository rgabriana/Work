/*these scripts are used to cleanup EC data from GLEM. Only to be used by dev team. */
delete from floor_energy_consumption_15min;
delete from floor_energy_consumption_hourly ;
delete from floor_energy_consumption_daily;
delete from bld_energy_consumption_15min;
delete from bld_energy_consumption_hourly;
delete from bld_energy_consumption_daily;
delete from campus_energy_consumption_15min;
delete from campus_energy_consumption_hourly;
delete from campus_energy_consumption_daily;
delete from organization_energy_consumption_15min;
delete from organization_energy_consumption_hourly;
delete from organization_energy_consumption_daily;
update em_last_ec_synctime set last_sync_at = null;
