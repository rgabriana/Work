SET statement_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

ALTER TABLE ONLY energy_consumption ADD CONSTRAINT energy_consumption_pkey PRIMARY KEY (id);
ALTER TABLE ONLY energy_consumption ADD CONSTRAINT unique_energy_consumption UNIQUE (capture_at, fixture_id);
ALTER TABLE ONLY energy_consumption ADD CONSTRAINT fk7d55f8647758dc25 FOREIGN KEY (fixture_id) REFERENCES fixture(id);

ALTER TABLE ONLY energy_consumption_hourly ADD CONSTRAINT energy_consumption_hourly_pkey PRIMARY KEY (id);
ALTER TABLE ONLY energy_consumption_hourly ADD CONSTRAINT unique_energy_consumption_hourly UNIQUE (capture_at, fixture_id);

ALTER TABLE ONLY energy_consumption_daily ADD CONSTRAINT energy_consumption_daily_pkey PRIMARY KEY (id);
ALTER TABLE ONLY energy_consumption_daily ADD CONSTRAINT unique_energy_consumption_daily UNIQUE (capture_at, fixture_id);
