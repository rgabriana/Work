CREATE TABLE floor_energy_consumption_daily
(
  id bigint NOT NULL,  
  cust_id bigint,
  floor_id bigint,
  capture_at timestamp without time zone,
  base_power_used numeric(19,2),
  power_used numeric(19,2),  
  price double precision,
  base_cost double precision,
  cost double precision,  
  saved_power_used numeric(19,2),
  saved_cost double precision,
  occ_saving numeric(19,2),
  tuneup_saving numeric(19,2),
  ambient_saving numeric(19,2),
  manual_saving numeric(19,2),  
  CONSTRAINT floor_energy_consumption_daily_pkey PRIMARY KEY (id),
  CONSTRAINT floor_unique_energy_consumption_daily UNIQUE(capture_at, cust_id, floor_id)
);

ALTER TABLE floor_energy_consumption_daily OWNER TO postgres;

CREATE SEQUENCE floor_energy_consumption_daily_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE floor_energy_consumption_daily_seq OWNER TO postgres;

CREATE INDEX floor_energy_consumption_daily_capture_at_index ON floor_energy_consumption_daily USING btree (capture_at);

CREATE INDEX floor_energy_consumption_daily_floor_id_index ON floor_energy_consumption_daily USING btree (floor_id);

CREATE TABLE floor_energy_consumption_hourly
(
  id bigint NOT NULL,  
  cust_id bigint,
  floor_id bigint,
  capture_at timestamp without time zone,
  base_power_used numeric(19,2),
  power_used numeric(19,2),  
  price double precision,
  base_cost double precision,
  cost double precision,  
  saved_power_used numeric(19,2),
  saved_cost double precision,
  occ_saving numeric(19,2),
  tuneup_saving numeric(19,2),
  ambient_saving numeric(19,2),
  manual_saving numeric(19,2),  
  CONSTRAINT floor_energy_consumption_hourly_pkey PRIMARY KEY (id),
  CONSTRAINT floor_unique_energy_consumption_hourly UNIQUE(capture_at, cust_id, floor_id)
);

ALTER TABLE floor_energy_consumption_hourly OWNER TO postgres;

CREATE SEQUENCE floor_energy_consumption_hourly_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE floor_energy_consumption_hourly_seq OWNER TO postgres;

CREATE INDEX floor_energy_consumption_hourly_capture_at_index ON floor_energy_consumption_hourly USING btree (capture_at);

CREATE INDEX floor_energy_consumption_hourly_floor_id_index ON floor_energy_consumption_hourly USING btree (floor_id);

CREATE TABLE floor_hierarchy_mapping
(
  id bigint,
  cust_id bigint,
  floor_id bigint,
  em_id bigint,
  em_floor_id bigint
);

CREATE TABLE bld_hierarchy_mapping
(
  id bigint,
  cust_id bigint,
  bld_id bigint,
  em_id bigint,
  em_bld_id bigint
);

CREATE TABLE campus_hierarchy_mapping
(
  id bigint,
  cust_id bigint,
  campus_id bigint,
  em_id bigint,
  em_campus_id bigint
);