SET statement_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;


alter table energy_consumption drop constraint energy_consumption_pkey;
alter table energy_consumption_hourly drop constraint energy_consumption_hourly_pkey;
alter table energy_consumption_daily drop constraint energy_consumption_daily_pkey;

alter table energy_consumption drop constraint fk7d55f8647758dc25;
alter table energy_consumption drop constraint unique_energy_consumption;
alter table energy_consumption_hourly drop constraint unique_energy_consumption_hourly;
alter table energy_consumption_daily drop constraint unique_energy_consumption_daily;

drop index energy_consumption_capture_at_index;
drop index energy_consumption_daily_capture_at_index;
drop index energy_consumption_daily_fixture_id_index;
drop index energy_consumption_daily_power_used_index;
drop index energy_consumption_fixture_id_index;
drop index energy_consumption_hourly_capture_at_index;
drop index energy_consumption_hourly_fixture_id_index;
drop index energy_consumption_hourly_power_used_index;
drop index energy_consumption_power_used_index;