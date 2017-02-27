SET statement_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

ALTER TABLE ONLY plugload_energy_consumption ADD CONSTRAINT plugload_energy_consumption_pkey PRIMARY KEY (id);
ALTER TABLE ONLY plugload_energy_consumption ADD CONSTRAINT plugload_energy_consumption_capture_at_plugload_id_key UNIQUE (capture_at, plugload_id);
ALTER TABLE ONLY plugload_energy_consumption ADD CONSTRAINT plugload_energy_consumption_plugload_id_fkey FOREIGN KEY (plugload_id) REFERENCES plugload(id);

ALTER TABLE ONLY plugload_energy_consumption_hourly ADD CONSTRAINT plugload_energy_consumption_hourly_pkey PRIMARY KEY (id);
ALTER TABLE ONLY plugload_energy_consumption_hourly ADD CONSTRAINT plugload_energy_consumption_hourly_capture_at_plugload_id_key UNIQUE (capture_at, plugload_id);
ALTER TABLE ONLY plugload_energy_consumption_hourly ADD CONSTRAINT plugload_energy_consumption_hourly_plugload_id_fkey FOREIGN KEY (plugload_id) REFERENCES plugload(id);


ALTER TABLE ONLY plugload_energy_consumption_daily ADD CONSTRAINT plugload_energy_consumption_daily_pkey PRIMARY KEY (id);
ALTER TABLE ONLY plugload_energy_consumption_daily ADD CONSTRAINT plugload_energy_consumption_daily_capture_at_plugload_id_key UNIQUE (capture_at, plugload_id);
ALTER TABLE ONLY plugload_energy_consumption_daily ADD CONSTRAINT plugload_energy_consumption_daily_plugload_id_fkey FOREIGN KEY (plugload_id) REFERENCES plugload(id);