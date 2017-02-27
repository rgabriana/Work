--Kushal: Following removeAllTriggers() call, followed by update_all_sequences() call, should always be the first statement during upgrade
select removeAllTriggers();
CREATE OR REPLACE FUNCTION change_sequences() RETURNS character varying as $$
DECLARE 
	wal_seq bigint;
BEGIN
    select last_value + 100 into wal_seq from wal_logs_seq;
    perform update_all_sequences();
    perform setval('wal_logs_seq', wal_seq);
	return '';
END
$$ LANGUAGE plpgsql;

select change_sequences();

-- This is global function used in multiple stored procedure to check column exists in the given table. Hence moving it on the top on 7th Jan, 2013 by Sharad --
CREATE OR REPLACE FUNCTION column_exists(colName text, tablename text)
 RETURNS boolean AS
 $$
 DECLARE
 colname ALIAS FOR $1;
 tablename ALIAS For $2;
 query text;
 onerow record;
 
 BEGIN
 query = 'SELECT attname FROM pg_attribute WHERE attrelid = ( SELECT oid FROM pg_class WHERE relname = ' || '''' || tablename || '''' ||  ') AND attname = ' || '''' || colname || ''''   ;
 FOR onerow IN EXECUTE query
 LOOP
 RETURN true;
 END LOOP;
 RETURN false;
 END;
 $$
 LANGUAGE plpgsql;
 
-- Added on July 27th, 2010 by Shiv --

ALTER TABLE floor ADD COLUMN no_installed_sensors integer DEFAULT 0;
ALTER TABLE floor ADD COLUMN no_installed_fixtures integer DEFAULT 0;

-- Added on July 28th, 2010 by Shiv --

ALTER TABLE fixture ADD COLUMN no_of_fixtures integer DEFAULT 1;

-- Added on Oct 11th, 2010 by Sreedhar --

DROP TYPE fixture_daily_record;

CREATE TYPE fixture_daily_record AS (
	fixture_id integer,
	agg_power numeric,
	agg_cost numeric,
	min_temp smallint,
	max_temp smallint,
	avg_temp numeric,
	base_power numeric,
	base_cost numeric,
	saved_power numeric,
	saved_cost numeric,
	occ_saving numeric,
	amb_saving numeric,
	tune_saving numeric,
	manual_saving numeric,
	no_of_rec int,
	peak_load numeric,
	min_load numeric,
	min_price numeric,
	max_price numeric
);

ALTER TYPE public.fixture_daily_record OWNER TO postgres;

DROP TYPE fixture_hour_record;

CREATE TYPE fixture_hour_record AS (
	fixture_id integer,
	agg_power numeric,
	agg_cost numeric,
	min_temp smallint,
	max_temp smallint,
	avg_temp numeric,
	base_power numeric,
	base_cost numeric,
	saved_power numeric,
	saved_cost numeric,
	occ_saving numeric,
	amb_saving numeric,
	tune_saving numeric,
	manual_saving numeric,
	no_of_rec int,
	peak_load numeric,
	min_load numeric,
	min_price numeric,
	max_price numeric
);

ALTER TYPE public.fixture_hour_record OWNER TO postgres;


-- Type: plugload_daily_record

DROP TYPE plugload_daily_record;

CREATE TYPE plugload_daily_record AS
   (plugload_id integer,
    agg_power numeric,
    agg_cost numeric,
    base_energy numeric,
    base_cost numeric,
    saved_energy numeric,
    saved_cost numeric,
    occ_saving numeric,
    tune_saving numeric,
    manual_saving numeric,
    no_of_rec integer,
    base_unmanaged_energy numeric,
    unmanaged_energy numeric,
    saved_unmanaged_energy numeric);
ALTER TYPE plugload_daily_record
  OWNER TO postgres;
  
-- Type: plugload_hour_record

DROP TYPE plugload_hour_record;

CREATE TYPE plugload_hour_record AS
   (plugload_id integer,
    agg_energy numeric,
    agg_cost numeric,
    avg_temp numeric,
    base_energy numeric,
    base_cost numeric,
    saved_energy numeric,
    saved_cost numeric,
    occ_saving numeric,
    tune_saving numeric,
    manual_saving numeric,
    no_of_rec integer,
    base_unmanaged_energy numeric,
    unmanaged_energy numeric,
    saved_unmanaged_energy numeric);
ALTER TYPE plugload_hour_record
  OWNER TO postgres;
  
  
 DROP TYPE IF EXISTS plugload_record ;
CREATE TYPE plugload_record AS
   (plugload_id integer,
    last_zb_update_time timestamp without time zone
    );
ALTER TYPE plugload_record
  OWNER TO postgres;
  
DROP TYPE IF EXISTS  plugload_zb_record;
  
CREATE TYPE plugload_zb_record AS
   (count integer,
    plugload_id integer,
    capture_at timestamp without time zone);
ALTER TYPE plugload_zb_record
  OWNER TO postgres;

  DROP TYPE IF EXISTS plugload_nzb_record ;
CREATE TYPE plugload_nzb_record AS
   (count integer,
    capture_at timestamp without time zone,
    mec numeric,
    umec numeric
    );
ALTER TYPE plugload_nzb_record 
  OWNER TO postgres;

CREATE OR REPLACE FUNCTION prunedata() RETURNS void
    AS $$
DECLARE 	
BEGIN
	PERFORM prunedatabase();
	
	PERFORM pruneplugloaddatabase();

	PERFORM pruneemsaudit();
	
	PERFORM prune_ems_user_audit();
	
	PERFORM pruneeventsfault();

END;
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aggregatedailyenergyconsumption(todate timestamp with time zone) RETURNS void
    AS $$
DECLARE 
	rec fixture_daily_record;
	min_load1 numeric;
	peak_load1 numeric;
	price_calc numeric;
BEGIN
	FOR rec IN (
	SELECT fixture_id, SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(avg_temperature) AS avg_temp, sum(base_power_used) AS base_power, sum(base_cost) AS base_cost, sum(saved_power_used) AS saved_power, sum(saved_cost) AS saved_cost, sum(occ_saving) AS occ_saving, sum(ambient_saving) AS amb_saving, sum(tuneup_saving) AS tune_saving, sum(manual_saving) AS manual_saving, count(*) AS no_of_rec, max(peak_load) AS peak_load, min(min_load) AS min_load, min(min_price) AS min_price, max(max_price) AS max_price
	FROM energy_consumption_hourly as ec 
	WHERE capture_at <= toDate and capture_at > toDate - interval '1 day' GROUP BY fixture_id)
	LOOP  
	  IF rec.base_power > 0 THEN
	    price_calc = rec.base_cost*1000/rec.base_power;
	  ELSE
	    price_calc = 0;
	  END IF;
		INSERT INTO energy_consumption_daily (id, fixture_id, power_used, cost, price, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, peak_load, min_load, min_price, max_price) VALUES (nextval('energy_consumption_daily_seq'), rec.fixture_id, rec.agg_power, rec.agg_cost, round(price_calc, 10), toDate, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_load, rec.min_load, rec.min_price, rec.max_price);
	END LOOP;

END;
$$
LANGUAGE plpgsql;

  
  


CREATE TYPE system_ec_record AS (
        capture_time timestamp without time zone,
        load numeric
);

-- Added on August 3rd, 2010 by Sreedhar --

CREATE SEQUENCE switch_seq
   INCREMENT 1
   START 1;
ALTER TABLE switch_seq OWNER TO postgres;
    
CREATE TABLE switch (
    id bigint DEFAULT nextval('switch_seq'::regclass) NOT NULL,
    name character varying,
    floor_id bigint,
    building_id bigint,
    campus_id bigint,
    area_id bigint,
    x integer,
    y integer,
    mode_type smallint DEFAULT 0,
    initial_scene_active_time integer DEFAULT 60,
    operation_mode smallint DEFAULT 0,
    gems_groups_id bigint
);

ALTER TABLE public.switch OWNER TO postgres;

ALTER TABLE ONLY switch
    ADD CONSTRAINT switch_pkey PRIMARY KEY (id);
    
CREATE SEQUENCE scene_seq
   INCREMENT 1
   START 1;
ALTER TABLE scene_seq OWNER TO postgres;
    
CREATE TABLE scene (
    id bigint DEFAULT nextval('scene_seq'::regclass) NOT NULL,
    switch_id bigint,
    name character varying,
    scene_order integer
);

ALTER TABLE public.scene OWNER TO postgres;

ALTER TABLE ONLY scene
    ADD CONSTRAINT scene_pkey PRIMARY KEY (id);
    
CREATE SEQUENCE lightlevel_seq
   INCREMENT 1
   START 1;
ALTER TABLE scene_seq OWNER TO postgres;

CREATE TABLE lightlevels (
    id bigint DEFAULT nextval('lightlevel_seq'::regclass) NOT NULL,
    switch_id bigint,
    scene_id bigint,
    f_id bigint,
    lightlevel integer
);

ALTER TABLE public.lightlevels OWNER TO postgres;

ALTER TABLE ONLY lightlevels
    ADD CONSTRAINT lightlevels_pkey PRIMARY KEY (id);

ALTER TABLE scene_seq OWNER TO postgres;

-- Added on August 5th, 2010 by Shiv --

CREATE UNIQUE INDEX unique_name_in_building ON floor (lower(name), building_id);
CREATE UNIQUE INDEX unique_name_in_campus ON building (lower(name), campus_id);
CREATE UNIQUE INDEX unique_name ON campus (lower(name));

-- Added on August 10th, 2010 by Shiv --

ALTER TABLE fixture ADD COLUMN last_connectivity_at timestamp without time zone;

-- Added on August 12th, 2010 by Shiv --

ALTER TABLE profile_handler ADD COLUMN ambient_sensitivity integer DEFAULT 0;

--Added on August 12th, 2010 by Rahul --

ALTER TABLE scene ALTER "name" TYPE character varying;

ALTER TABLE switch ALTER "name" TYPE character varying;

-- Added on August 16th, 2010 by Shiv --

ALTER TABLE profile ADD COLUMN ambient_sensitivity integer DEFAULT 5;
ALTER TABLE profile_handler DROP COLUMN ambient_sensitivity;

ALTER TABLE profile_handler ADD COLUMN dim_backoff_time smallint DEFAULT 10;
ALTER TABLE profile_handler ADD COLUMN intensity_norm_time smallint;
ALTER TABLE profile_handler ADD COLUMN on_amb_light_level integer;
ALTER TABLE profile_handler ADD COLUMN min_level_before_off smallint;

-- Added on August 18th, 2010 by Shiv --

ALTER TABLE fixture DROP COLUMN relays_connected;
ALTER TABLE profile_handler ADD COLUMN relays_connected integer;

-- Added on August 23rd, 2010 by Shiv --

CREATE TABLE pricing
(
  id bigint NOT NULL,
  price_level character varying(255),
  "interval" character varying(255),
  price double precision,
  CONSTRAINT pricing_pkey PRIMARY KEY (id)
);

ALTER TABLE pricing OWNER TO postgres;

CREATE SEQUENCE pricing_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999999999
  START 1
  CACHE 1;

ALTER TABLE pricing_seq OWNER TO postgres;

CREATE TABLE dr_target
(
  id bigint NOT NULL,
  price_level character varying(255),
  pricing double precision,
  duration integer,
  target_reduction integer,
  CONSTRAINT dr_target_pkey PRIMARY KEY (id)
); 

ALTER TABLE dr_target OWNER TO postgres;

CREATE SEQUENCE dr_target_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999999999
  START 1
  CACHE 1;

ALTER TABLE dr_target_seq OWNER TO postgres;

-- Added on August 30th, 2010 by Shiv --

CREATE UNIQUE INDEX unique_name_in_groups ON groups USING btree (lower(name::text));

-- Added on August 31st, 2010 by Shiv --

ALTER TABLE fixture ADD COLUMN ip_address character varying(255);
ALTER TABLE fixture ADD COLUMN comm_type integer;

-- Added on September 6th, 2010 by Shiv --

ALTER TABLE profile_handler ALTER COLUMN dark_lux SET DEFAULT 20;
ALTER TABLE profile_handler ALTER COLUMN neighbor_lux SET DEFAULT 200;
ALTER TABLE profile_handler ALTER COLUMN envelope_on_level SET DEFAULT 50;
ALTER TABLE profile_handler ALTER COLUMN "drop" SET DEFAULT 10;
ALTER TABLE profile_handler ALTER COLUMN rise SET DEFAULT 20;
ALTER TABLE profile_handler ALTER COLUMN intensity_norm_time SET DEFAULT 10;
ALTER TABLE profile_handler ALTER COLUMN min_level_before_off SET DEFAULT 20;

-- Added on September 14th, 2010 by Shiv --

ALTER TABLE pricing ADD COLUMN day_type character varying(255);

-- Added on September 22nd, 2010 by Shiv --

ALTER TABLE dr_target ADD COLUMN start_time timestamp without time zone;

-- Added on September 27th, 2010 by Yogesh --
ALTER TABLE fixture ALTER COLUMN comm_type SET default 1;
-- Added on September 23rd, 2010 by Shiv --

ALTER TABLE fixture_group DROP CONSTRAINT fk38ebe8c9c42c4725;

-- Added on September 27th, 2010 by Yogesh --
ALTER TABLE inventorydevice ADD COLUMN ip_address character varying(255);
ALTER TABLE inventorydevice ADD COLUMN comm_type integer DEFAULT 1;

-- Added on September 27th, 2010 by Shiv --

ALTER TABLE pricing ADD COLUMN from_time timestamp without time zone;
ALTER TABLE pricing ADD COLUMN to_time timestamp without time zone;

CREATE OR REPLACE FUNCTION populate_pricing_metadata() RETURNS void AS
$BODY$
DECLARE
	counter int;
	seq_counter int;
BEGIN
	select into counter count(*) from pricing;
	IF counter = 0 THEN
		INSERT INTO pricing (id, price_level, "interval", price, day_type, from_time, to_time) VALUES (1, 'Off Peak', '09:30 pm - 08:30 am', 0.085, 'weekday', '1970-01-01 21:30:00', '1970-01-02 08:30:00');
INSERT INTO pricing (id, price_level, "interval", price, day_type, from_time, to_time) VALUES (2, 'Partial Peak', '08:30 am - 12:00 pm', 0.105, 'weekday', '1970-01-01 08:30:00', '1970-01-01 12:00:00');
INSERT INTO pricing (id, price_level, "interval", price, day_type, from_time, to_time) VALUES (3, 'Peak', '12:00 pm - 06:00 pm', 0.15, 'weekday', '1970-01-01 12:00:00', '1970-01-01 18:00:00');
INSERT INTO pricing (id, price_level, "interval", price, day_type, from_time, to_time) VALUES (4, 'Partial Peak', '6:00 pm - 09:30 pm', 0.105, 'weekday', '1970-01-01 18:00:00', '1970-01-01 21:30:00');
INSERT INTO pricing (id, price_level, "interval", price, day_type, from_time, to_time) VALUES (5, 'Off Peak', '09:30 pm - 08:30 am', 0.085, 'weekend', '1970-01-01 21:30:00', '1970-01-02 08:30:00');
INSERT INTO pricing (id, price_level, "interval", price, day_type, from_time, to_time) VALUES (6, 'Partial Peak', '08:30 am - 12:00 pm', 0.105, 'weekend', '1970-01-01 08:30:00', '1970-01-01 12:00:00');
INSERT INTO pricing (id, price_level, "interval", price, day_type, from_time, to_time) VALUES (7, 'Peak', '12:00 pm - 06:00 pm', 0.15, 'weekend', '1970-01-01 12:00:00', '1970-01-01 18:00:00');
INSERT INTO pricing (id, price_level, "interval", price, day_type, from_time, to_time) VALUES (8, 'Partial Peak', '6:00 pm - 09:30 pm', 0.105, 'weekend', '1970-01-01 18:00:00', '1970-01-01 21:30:00');
INSERT INTO pricing (id, price_level, "interval", price, day_type, from_time, to_time) VALUES (9, 'Off Peak', '09:30 pm - 08:30 am', 0.085, 'holiday', '1970-01-01 21:30:00', '1970-01-02 08:30:00');
INSERT INTO pricing (id, price_level, "interval", price, day_type, from_time, to_time) VALUES (10, 'Partial Peak', '08:30 am - 12:00 pm', 0.105, 'holiday', '1970-01-01 08:30:00', '1970-01-01 12:00:00');
INSERT INTO pricing (id, price_level, "interval", price, day_type, from_time, to_time) VALUES (11, 'Peak', '12:00 pm - 06:00 pm', 0.15, 'holiday', '1970-01-01 12:00:00', '1970-01-01 18:00:00');
INSERT INTO pricing (id, price_level, "interval", price, day_type, from_time, to_time) VALUES (12, 'Partial Peak', '6:00 pm - 09:30 pm', 0.105, 'holiday', '1970-01-01 18:00:00', '1970-01-01 21:30:00');
	END IF;
	select into seq_counter last_value from pricing_seq;
	IF seq_counter = 1 THEN
		ALTER SEQUENCE pricing_seq RESTART 12;
	END IF;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;

SELECT populate_pricing_metadata();

--Added on Oct 11th by Sreedhar--
ALTER TABLE energy_consumption_hourly ADD COLUMN peak_load numeric(19,2);
ALTER TABLE energy_consumption_hourly ADD COLUMN min_load numeric(19,2);

ALTER TABLE energy_consumption_daily ADD COLUMN peak_load numeric(19,2);
ALTER TABLE energy_consumption_daily ADD COLUMN min_load numeric(19,2);

--Added on Oct 14th by Sreedhar--
ALTER TABLE energy_consumption_hourly ADD COLUMN min_price double precision;
ALTER TABLE energy_consumption_hourly ADD COLUMN max_price double precision;

ALTER TABLE energy_consumption_daily ADD COLUMN min_price double precision;
ALTER TABLE energy_consumption_daily ADD COLUMN max_price double precision;

ALTER TABLE energy_consumption_daily ALTER COLUMN price2 SET DEFAULT 0;
ALTER TABLE energy_consumption_daily ALTER COLUMN price3 SET DEFAULT 0;
ALTER TABLE energy_consumption_daily ALTER COLUMN price4 SET DEFAULT 0;
ALTER TABLE energy_consumption_daily ALTER COLUMN price5 SET DEFAULT 0;

ALTER TABLE energy_consumption_daily ALTER COLUMN power_used2 SET DEFAULT 0;
ALTER TABLE energy_consumption_daily ALTER COLUMN power_used3 SET DEFAULT 0;
ALTER TABLE energy_consumption_daily ALTER COLUMN power_used4 SET DEFAULT 0;
ALTER TABLE energy_consumption_daily ALTER COLUMN power_used5 SET DEFAULT 0;

UPDATE groups set name = 'Private Offices' where name = 'Offices';

--Added by Sreedhar 10/15
ALTER TABLE fixture ADD COLUMN last_stats_rcvd_time timestamp without time zone;
ALTER TABLE energy_consumption ADD COLUMN zero_bucket smallint DEFAULT 0;

--Added by Sreedhar 10/19

CREATE TYPE missing_time_record AS (
        capture_time timestamp without time zone
);

---Added by Sreedhar 10/25
ALTER TABLE energy_consumption ALTER COLUMN occ_saving SET DEFAULT 0;
ALTER TABLE energy_consumption ALTER COLUMN tuneup_saving SET DEFAULT 0;
ALTER TABLE energy_consumption ALTER COLUMN ambient_saving SET DEFAULT 0;
ALTER TABLE energy_consumption ALTER COLUMN manual_saving SET DEFAULT 0;

--Added by Sreedhar 10/28
ALTER TABLE energy_consumption ADD COLUMN avg_volts smallint;

--Added by Sreedhar 10/29
ALTER TABLE fixture ADD COLUMN profile_checksum smallint;
ALTER TABLE fixture ADD COLUMN global_profile_checksum smallint;
ALTER TABLE profile_handler ADD COLUMN profile_checksum smallint;
ALTER TABLE profile_handler ADD COLUMN global_profile_checksum smallint;

CREATE TABLE ballast_volt_power (
  id bigint NOT NULL,
  ballast_id bigint,
  volt smallint,
  power double precision,
  CONSTRAINT ballast_volt_power_pk PRIMARY KEY (id)  
);

ALTER TABLE ballast_volt_power OWNER TO postgres;

--
-- Name: ballast_volt_power_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE ballast_volt_power_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE ballast_volt_power_seq OWNER TO postgres;

---Added by Sreedhar on 11/17
ALTER TABLE energy_consumption ADD COLUMN curr_state smallint;
ALTER TABLE fixture ADD COLUMN curr_app smallint;

---Added by Sreedhar on 11/18
ALTER TABLE fixture ADD COLUMN firmware_version character varying(20);
ALTER TABLE fixture ADD COLUMN bootloader_version character varying(20);

---Added by Yogesh on 11/25
ALTER TABLE fixture ADD COLUMN group_id bigint;

---Added by Sreedhar on 12/02
ALTER TABLE gateway ADD COLUMN ip_address character varying(255);
ALTER TABLE gateway ADD COLUMN port smallint;
ALTER TABLE gateway ADD COLUMN snap_address character varying(20);
ALTER TABLE gateway ADD COLUMN gateway_type smallint;
ALTER TABLE gateway ADD COLUMN serial_port smallint;
ALTER TABLE gateway ADD COLUMN channel smallint;
ALTER TABLE gateway ADD COLUMN aes_key character varying(256);
ALTER TABLE gateway ADD COLUMN mac_address character varying(50);
ALTER TABLE gateway ADD COLUMN user_name character varying(50);
ALTER TABLE gateway ADD COLUMN password character varying(50);

--Added by Sreedhar on 12/09
ALTER TABLE profile_handler ADD COLUMN standalone_motion_override smallint DEFAULT 0;
ALTER TABLE profile_handler ADD COLUMN dr_reactivity smallint DEFAULT 0;

--Added by Sreedhar on 12/19
UPDATE profile set ramp_up_time = 0 where ramp_up_time > 10;

--Added by Sreedhar on 12/29
ALTER TABLE energy_consumption ADD COLUMN motion_bits bigint;
ALTER TABLE energy_consumption ADD COLUMN power_calc smallint;

--Added by Yogesh on 01/18/11
ALTER TABLE ballast_volt_power ALTER COLUMN volt TYPE double precision; 

--Added by Yogesh on 01/21/11
ALTER TABLE ballast_volt_power ADD COLUMN volt_power_map_id bigint;

ALTER TABLE ballasts ADD COLUMN volt_power_map_id bigint DEFAULT 1;

INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 0, 23.6);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 0.5, 23.6);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 1, 23.6);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 1.5, 25.5);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 2, 27.3);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 2.5, 32.7);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 3, 38.2);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 3.5, 47.3);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 4, 52.7);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 4.5, 58.2);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 5, 63.6);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 5.5, 72.7);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 6, 76.4);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 6.5, 81.8);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 7, 89.1);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 7.5, 94.5);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 8, 96.4);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 8.5, 100);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 9, 100);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 9.5, 100);
INSERT  INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 10, 100);

--Added by Sree 01/25
ALTER TABLE fixture ADD COLUMN sec_gw_id bigint DEFAULT 1;
ALTER TABLE inventorydevice ADD COLUMN gw_id bigint;
ALTER TABLE inventorydevice DROP COLUMN gw_ip;

-- Added by Yogesh 01/27/11
CREATE TABLE system_configuration (
  id bigint NOT NULL,
  name character varying,
  value character varying, 
  CONSTRAINT system_configuration_pk PRIMARY KEY (id)  
);

ALTER TABLE system_configuration OWNER TO postgres;

--
-- Name: system_configuration_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE system_configuration_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE system_configuration_seq OWNER TO postgres;

--
-- Insert default system configuration data 
--
-- Update by Yogesh 19/05
ALTER TABLE system_configuration ADD CONSTRAINT unique_system_configuration_name UNIQUE (name);
CREATE INDEX system_configuration_name_index ON system_configuration USING btree (name);
CREATE OR REPLACE FUNCTION setUpSCGroupsDefaults() RETURNS void
	AS $$
DECLARE
	sKey character varying;
	sValue character varying;
BEGIN
	sKey = 'default.metadata.areas';
	sValue = 'Default,Breakroom,Conference Room,Open Corridor,Closed Corridor,Egress,Lobby,Warehouse,Open Office,Private Office,Restroom,Lab,Custom1,Custom2,Standalone,Highbay,Outdoor';
	IF EXISTS (SELECT value from system_configuration where name = sKey) THEN
		UPDATE system_configuration set value = sValue where name = sKey;
	ELSE
		INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), sKey, sValue);
	END IF;

	sKey = 'default.metadata.weekday';
	sValue = 'Monday,Tuesday,Wednesday,Thursday,Friday';
	IF EXISTS (SELECT value from system_configuration where name = sKey) THEN
		UPDATE system_configuration set value = sValue where name = sKey;
	ELSE
		INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), sKey, sValue);
	END IF;

	sKey = 'default.metadata.weekend';
	sValue = 'Saturday,Sunday';
	IF EXISTS (SELECT value from system_configuration where name = sKey) THEN
		UPDATE system_configuration set value = sValue where name = sKey;
	ELSE
		INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), sKey, sValue);
	END IF;
END;
$$
LANGUAGE plpgsql;

-- 
-- Fire setup query
-- 
select setUpSCGroupsDefaults();

-- Function to setup system configuration defaults. 
--
CREATE OR REPLACE FUNCTION setUpSCProfileDefaults() RETURNS void 
    AS $$
DECLARE
	sKey character varying;
	sValue character varying;
	profiles_adv_cols text[] = ARRAY['groupnameholder', 'pfh.dark_lux', 'pfh.neighbor_lux', 'pfh.envelope_on_level', 'pfh.drop', 'pfh.rise', 'pfh.dim_backoff_time', 'pfh.intensity_norm_time', 'pfh.on_amb_light_level', 'pfh.min_level_before_off', 'pfh.relays_connected', 'pfh.standalone_motion_override', 'pfh.dr_reactivity', 'pfh.to_off_linger', 'pfh.initial_on_level', 'pfc.morning_time', 'pfc.day_time', 'pfc.evening_time', 'pfc.night_time', 'pfh.initial_on_time', 'pfh.dr_low', 'pfh.dr_moderate', 'pfh.dr_high', 'pfh.dr_special'];
	profiles_groups_with_adv_defaults text[] = '{
	{"default", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "0", "0", "0", "0"},
	{"default.breakroom", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "10", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.conferenceroom", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.opencorridor", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "10", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.closedcorridor", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "10", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.egress", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.lobby", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.warehouse", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.openoffice", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.privateoffice", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.restroom", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "300", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.lab", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.custom1", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.custom2", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.standalone", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.highbay", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5", "1", "2", "3", "4"},
	{"default.outdoor", "0", "250", "0", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "5:00 AM", "9:00 AM", "5:00 PM", "10:00 PM", "1", "1", "2", "3", "4"}}';

	profile_pahers text[] = ARRAY['profile.morning', 'profile.day', 'profile.evening', 'profile.night','weekend.profile.morning', 'weekend.profile.day', 'weekend.profile.evening', 'weekend.profile.night','holiday.profile.morning', 'holiday.profile.day', 'holiday.profile.evening', 'holiday.profile.night'];

	profile_cols text[] = ARRAY['min_level', 'on_level', 'ramp_up_time', 'motion_detect_duration', 'motion_sensitivity', 'ambient_sensitivity', 'manual_override_duration'];

	profiles_defaults int[] = ARRAY[
		--default
		[
			-- weekday [morning, day, evening, night]
			[0, 100, 0, 5, 1, 5, 60], [20, 100, 0, 15, 1, 5, 60], [0, 100, 0, 5, 1, 5, 60], [0, 100, 0, 5, 1, 0, 60], 
			-- weekend
			[0, 100, 0, 3, 1, 5, 60], [0, 100, 0, 3, 1, 5, 60], [0, 100, 0, 3, 1, 5, 60], [0, 100, 0, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 100, 0, 3, 1, 5, 60], [0, 100, 0, 3, 1, 5, 60], [0, 100, 0, 3, 1, 5, 60], [0, 100, 0, 3, 1, 0, 60]
		],
		--breakroom
		[
			[0, 75, 2, 3, 1, 5, 60], [20, 75, 2, 5, 1, 8, 60], [0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 0, 60], 
			[0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 65, 2, 3, 1, 5, 60], [0, 55, 2, 3, 1, 8, 60], [0, 35, 2, 3, 1, 5, 60], [0, 45, 2, 3, 1, 0, 60]
		],
		--conferenceroom
		[
			[0, 75, 3, 15, 1, 8, 60], [20, 75, 3, 15, 1, 8, 60], [0, 75, 3, 15, 1, 8, 60], [0, 75, 3, 5, 1, 0, 60], 
			[0, 75, 3, 3, 1, 8, 60], [0, 75, 3, 3, 1, 8, 60], [0, 75, 3, 3, 1, 8, 60], [0, 75, 3, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 65, 3, 3, 1, 8, 60], [0, 55, 3, 3, 1, 8, 60], [0, 35, 3, 3, 1, 8, 60], [0, 45, 3, 3, 1, 0, 60]
		],
		--opencorridor
		[
			[0, 75, 0, 10, 1, 5, 60], [20, 75, 0, 20, 1, 5, 60], [0, 75, 0, 10, 1, 5, 60], [0, 75, 0, 5, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 65, 0, 3, 1, 5, 60], [0, 55, 0, 3, 1, 5, 60], [0, 35, 0, 3, 1, 5, 60], [0, 45, 0, 3, 1, 0, 60]
		],
		--closecorridor
		[
			[0, 50, 0, 5, 1, 5, 60], [20, 50, 0, 5, 1, 5, 60], [20, 50, 0, 5, 1, 5, 60], [0, 50, 0, 5, 1, 0, 60], 
			[0, 50, 0, 3, 1, 5, 60], [0, 50, 0, 3, 1, 5, 60], [0, 50, 0, 3, 1, 5, 60], [0, 50, 0, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 45, 0, 3, 1, 5, 60], [0, 35, 0, 3, 1, 5, 60], [0, 25, 0, 3, 1, 5, 60], [0, 30, 0, 3, 1, 0, 60]
		],
		--egresslights
		[
			[0, 75, 0, 5, 1, 5, 60], [20, 75, 0, 15, 1, 5, 60], [0, 75, 0, 5, 1, 5, 60], [0, 75, 0, 5, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 65, 0, 3, 1, 5, 60], [0, 55, 0, 3, 1, 5, 60], [0, 35, 0, 3, 1, 5, 60], [0, 45, 0, 3, 1, 0, 60]
		],
		--lobby
		[
			[40, 100, 4, 5, 1, 5, 60], [40, 100, 4, 15, 1, 5, 60], [40, 100, 4, 5, 1, 5, 60], [40, 100, 4, 5, 1, 0, 60], 
			[40, 100, 4, 3, 1, 5, 60], [40, 100, 4, 3, 1, 5, 60], [40, 100, 4, 3, 1, 5, 60], [40, 100, 4, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[40, 90, 4, 3, 1, 5, 60], [40, 75, 4, 3, 1, 5, 60], [40, 50, 4, 3, 1, 5, 60], [40, 60, 4, 3, 1, 0, 60]
		],
		--warehouse 
		[
			[0, 100, 0, 3, 2, 0, 60], [0, 100, 0, 5, 2, 0, 60], [0, 100, 0, 5, 2, 0, 60], [0, 100, 0, 3, 2, 0, 60], 
			[0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 90, 0, 10, 2, 0, 60], [0, 75, 0, 10, 2, 0, 60], [0, 50, 0, 10, 2, 0, 60], [0, 60, 0, 10, 2, 0, 60]
		],
		--openoffice
		[
			[0, 75, 0, 5, 1, 5, 60], [20, 75, 2, 15, 1, 5, 60], [0, 75, 0, 5, 1, 5, 60], [0, 75, 0, 5, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 65, 0, 3, 1, 5, 60], [0, 55, 0, 3, 1, 5, 60], [0, 35, 0, 3, 1, 5, 60], [0, 45, 0, 3, 1, 0, 60]
		],
		--privateoffices
		[
			[0, 75, 2, 10, 1, 8, 60], [20, 75, 2, 20, 1, 8, 60], [0, 75, 2, 10, 1, 8, 60], [0, 75, 2, 5, 1, 0, 60], 
			[0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 65, 2, 3, 1, 8, 60], [0, 55, 2, 3, 1, 8, 60], [0, 35, 2, 3, 1, 8, 60], [0, 45, 2, 3, 1, 0, 60]
		],
		--restroom 
		[
			[0, 75, 2, 3, 1, 5, 60], [20, 75, 2, 5, 1, 8, 60], [0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 0, 60], 
			[0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 65, 2, 3, 1, 5, 60], [0, 55, 2, 3, 1, 8, 60], [0, 35, 2, 3, 1, 5, 60], [0, 45, 2, 3, 1, 0, 60]
		],
		--labs
		[
			[0, 100, 0, 30, 1, 3, 60], [30, 100, 0, 30, 1, 3, 60], [0, 100, 0, 5, 1, 3, 60], [0, 100, 0, 5, 1, 0, 60], 
			[0, 100, 0, 3, 1, 3, 60], [0, 100, 0, 3, 1, 3, 60], [0, 100, 0, 3, 1, 3, 60], [0, 100, 0, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 90, 0, 3, 1, 3, 60], [0, 75, 0, 3, 1, 3, 60], [0, 50, 0, 3, 1, 3, 60], [0, 60, 0, 3, 1, 0, 60]
		],
		--custom1
		[
			[0, 75, 0, 5, 1, 5, 60], [20, 75, 2, 15, 1, 5, 60], [0, 75, 0, 5, 1, 5, 60], [0, 75, 0, 5, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 65, 0, 3, 1, 5, 60], [0, 55, 0, 3, 1, 5, 60], [0, 35, 0, 3, 1, 5, 60], [0, 45, 0, 3, 1, 0, 60]
		],
		--custom2
		[
			[0, 75, 0, 5, 1, 5, 60], [20, 75, 2, 15, 1, 5, 60], [0, 75, 0, 5, 1, 5, 60], [0, 75, 0, 5, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 65, 0, 3, 1, 5, 60], [0, 55, 0, 3, 1, 5, 60], [0, 35, 0, 3, 1, 5, 60], [0, 45, 0, 3, 1, 0, 60]
		],
		--standalone
		[
			[35, 100, 0, 5, 1, 5, 60], [35, 100, 0, 15, 1, 5, 60], [35, 100, 0, 5, 1, 5, 60], [35, 100, 0, 5, 1, 0, 60], 
			[35, 100, 0, 3, 1, 5, 60], [35, 100, 0, 3, 1, 5, 60], [35, 100, 0, 3, 1, 5, 60], [35, 100, 0, 3, 1, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[35, 90, 0, 3, 1, 5, 60], [35, 75, 0, 3, 1, 5, 60], [35, 50, 0, 3, 1, 5, 60], [35, 60, 0, 3, 1, 0, 60]
		],
		--highbay
		[
			[0, 100, 0, 3, 2, 0, 60], [0, 100, 0, 5, 2, 0, 60], [0, 100, 0, 5, 2, 0, 60], [0, 100, 0, 3, 2, 0, 60], 
			[0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 90, 0, 10, 2, 0, 60], [0, 75, 0, 10, 2, 0, 60], [0, 50, 0, 10, 2, 0, 60], [0, 60, 0, 10, 2, 0, 60]
		],
		--outdoor
		[
			[0, 65, 0, 5, 0, 10, 60], [0, 65, 0, 5, 2, 10, 60], [0, 65, 0, 5, 0, 10, 60], [20, 65, 0, 5, 2, 0, 60], 
			[0, 65, 0, 5, 0, 10, 60], [0, 65, 0, 5, 2, 10, 60], [0, 65, 0, 5, 0, 10, 60], [20, 65, 0, 5, 2, 0, 60], 
			-- profile overrides [override1 10%, override2 25%, override3 50%, override4 40%]
			[0, 50, 0, 3, 2, 10, 60], [0, 40, 0, 2, 2, 10, 60], [0, 30, 0, 1, 2, 10, 60], [0, 40, 0, 2, 2, 10, 60]
		]

	]; 
BEGIN
	FOR i in 1..array_upper(profiles_groups_with_adv_defaults, 1) LOOP
		FOR j in 1..array_upper(profile_pahers, 1) LOOP
			FOR k in 1..array_upper(profile_cols, 1) LOOP
				sKey := profiles_groups_with_adv_defaults[i][1] || '.' ||  profile_pahers[j] || '.' || profile_cols[k];
				sValue := profiles_defaults[i][j][k];
				IF EXISTS (SELECT value from system_configuration where name = sKey) THEN
					--Raise Notice 'U% => %', sKey, sValue;
					UPDATE system_configuration set value = sValue where name = sKey;
				ELSE
					INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), sKey, sValue);
				END IF;
			END LOOP;
		END LOOP;
		FOR l in 2..array_upper(profiles_adv_cols, 1) LOOP
			sKey := profiles_groups_with_adv_defaults[i][1] || '.' || profiles_adv_cols[l];
			sValue := profiles_groups_with_adv_defaults[i][l];
			--Raise Notice '%.% => %', profiles_groups_with_adv_defaults[i][1], profiles_adv_cols[l], profiles_groups_with_adv_defaults[i][l];
			IF EXISTS (SELECT value from system_configuration where name = sKey) THEN
				UPDATE system_configuration set value = sValue where name = sKey;
			ELSE
				INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), sKey, sValue);
			END IF;
		END LOOP;
	END LOOP;
END;
$$
LANGUAGE plpgsql;

-- 
-- Fire setup query
-- 
select setUpSCProfileDefaults();
-- End System Configuration profile defaults


--
-- Insert default system configuration data for plugload profile
--
-- Update by Sampath Akula 19/JAN/2015
CREATE OR REPLACE FUNCTION setUpSCPlugloadGroupsDefaults() RETURNS void
	AS $$
DECLARE
	sKey character varying;
	sValue character varying;
BEGIN
	sKey = 'default.plugloadprofile.metadata.areas';
	sValue = 'Default';
	IF EXISTS (SELECT value from system_configuration where name = sKey) THEN
		UPDATE system_configuration set value = sValue where name = sKey;
	ELSE
		INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), sKey, sValue);
	END IF;

	sKey = 'default.plugloadprofile.metadata.weekday';
	sValue = 'Monday,Tuesday,Wednesday,Thursday,Friday';
	IF EXISTS (SELECT value from system_configuration where name = sKey) THEN
		UPDATE system_configuration set value = sValue where name = sKey;
	ELSE
		INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), sKey, sValue);
	END IF;

	sKey = 'default.plugloadprofile.metadata.weekend';
	sValue = 'Saturday,Sunday';
	IF EXISTS (SELECT value from system_configuration where name = sKey) THEN
		UPDATE system_configuration set value = sValue where name = sKey;
	ELSE
		INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), sKey, sValue);
	END IF;
END;
$$
LANGUAGE plpgsql;

-- 
-- Fire setup query
-- 
select setUpSCPlugloadGroupsDefaults();

-- Function to setup system configuration defaults. 
--
CREATE OR REPLACE FUNCTION setUpSCPlugloadProfileDefaults() RETURNS void 
    AS $$
DECLARE
	sKey character varying;
	sValue character varying;
	plugload_profiles_adv_cols text[] = ARRAY['plugloadgroupnameholder', 'plpfh.initial_on_time', 'plpfh.initial_on_level', 'plpfh.heartbeat_interval', 'plpfh.heartbeat_linger_period', 'plpfh.no_of_missed_heartbeats', 'plpfh.fallback_mode', 'plpfc.morning_time', 'plpfc.day_time', 'plpfc.evening_time', 'plpfc.night_time', 'plpfh.dr_low', 'plpfh.dr_moderate', 'plpfh.dr_high', 'plpfh.dr_special'];
	plugload_profiles_groups_with_adv_defaults text[] = '{
	{"default", "0", "100", "30", "30", "3", "0", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "0", "0", "0", "0"}}';

	plugload_profile_pahers text[] = ARRAY['plugloadprofile.morning', 'plugloadprofile.day', 'plugloadprofile.evening', 'plugloadprofile.night','weekend.plugloadprofile.morning', 'weekend.plugloadprofile.day', 'weekend.plugloadprofile.evening', 'weekend.plugloadprofile.night','holiday.plugloadprofile.morning', 'holiday.plugloadprofile.day', 'holiday.plugloadprofile.evening', 'holiday.plugloadprofile.night'];

	plugload_profile_cols text[] = ARRAY['active_motion_window', 'mode', 'manual_override_time'];

	plugload_profiles_defaults int[] = ARRAY[
		--default
		[
			-- weekday [morning, day, evening, night]
			[30, 1, 60], [30, 1, 60], [30, 1, 60], [30, 1, 60], 
			-- weekend
			[30, 1, 60], [30, 1, 60], [30, 1, 60], [30, 1, 60], 
			-- plugload profile overrides [override1 , override2 , override3 , override4 ]
			[30, 1, 60], [30, 1, 60], [30, 1, 60], [30, 1, 60]
		]

	]; 
BEGIN
	FOR i in 1..array_upper(plugload_profiles_groups_with_adv_defaults, 1) LOOP
		FOR j in 1..array_upper(plugload_profile_pahers, 1) LOOP
			FOR k in 1..array_upper(plugload_profile_cols, 1) LOOP
				sKey := plugload_profiles_groups_with_adv_defaults[i][1] || '.' ||  plugload_profile_pahers[j] || '.' || plugload_profile_cols[k];
				sValue := plugload_profiles_defaults[i][j][k];
				IF EXISTS (SELECT value from system_configuration where name = sKey) THEN
					--Raise Notice 'U% => %', sKey, sValue;
					UPDATE system_configuration set value = sValue where name = sKey;
				ELSE
					INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), sKey, sValue);
				END IF;
			END LOOP;
		END LOOP;
		FOR l in 2..array_upper(plugload_profiles_adv_cols, 1) LOOP
			sKey := plugload_profiles_groups_with_adv_defaults[i][1] || '.' || plugload_profiles_adv_cols[l];
			sValue := plugload_profiles_groups_with_adv_defaults[i][l];
			--Raise Notice '%.% => %', plugload_profiles_groups_with_adv_defaults[i][1], plugload_profiles_adv_cols[l], plugload_profiles_groups_with_adv_defaults[i][l];
			IF EXISTS (SELECT value from system_configuration where name = sKey) THEN
				UPDATE system_configuration set value = sValue where name = sKey;
			ELSE
				INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), sKey, sValue);
			END IF;
		END LOOP;
	END LOOP;
END;
$$
LANGUAGE plpgsql;

-- 
-- Fire setup query
-- 
select setUpSCPlugloadProfileDefaults();
-- End System Configuration plugload profile defaults



--Added by Sreedhar 01/31
ALTER TABLE fixture ADD COLUMN upgrade_status character varying(20);

--Added by Sreedhar 02/07
--
-- Name: gw_stats; Type: TABLE; Owner: postgres; 
--

CREATE TABLE gw_stats
(
  id bigint NOT NULL,
  gw_id bigint NOT NULL,
  capture_at timestamp without time zone,
  no_pkts_from_gems bigint,
  no_pkts_to_gems bigint,
  no_pkts_from_nodes bigint,
  no_pkts_to_nodes bigint,
  uptime bigint,
  CONSTRAINT gw_stats_pkey PRIMARY KEY(id)
);

ALTER TABLE gw_stats OWNER TO postgres;

--
-- Name: gw_stats_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE gw_stats_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE gw_stats_seq OWNER TO postgres;
--Added by Yogesh 01/21
ALTER TABLE fixture ADD COLUMN push_profile boolean DEFAULT false;
ALTER TABLE fixture ADD COLUMN push_global_profile boolean DEFAULT false;

--Added by Yogesh 02/08
ALTER TABLE gateway ADD COLUMN wireless_networkid integer;
ALTER TABLE gateway ADD COLUMN wireless_enctype smallint;
ALTER TABLE gateway ADD COLUMN wireless_enckey character varying(256);
ALTER TABLE gateway ADD COLUMN wireless_radiorate smallint default 0;
ALTER TABLE gateway ADD COLUMN eth_sec_type smallint default 0;
ALTER TABLE gateway ADD COLUMN eth_sec_integritytype smallint default 0;
ALTER TABLE gateway ADD COLUMN eth_sec_enctype smallint default 0;
ALTER TABLE gateway ADD COLUMN eth_sec_key character varying;
ALTER TABLE gateway ADD COLUMN eth_ipaddrtype smallint default 1;
ALTER TABLE gateway ADD COLUMN app1_version character varying(50);

--Added by Sreedhar 02/08
ALTER TABLE gateway ADD COLUMN curr_uptime bigint;
ALTER TABLE gateway ADD COLUMN curr_no_pkts_from_gems bigint;
ALTER TABLE gateway ADD COLUMN curr_no_pkts_to_gems bigint;
ALTER TABLE gateway ADD COLUMN curr_no_pkts_from_nodes bigint;
ALTER TABLE gateway ADD COLUMN curr_no_pkts_to_nodes bigint;
ALTER TABLE gateway ADD COLUMN last_connectivity_at timestamp without time zone;
ALTER TABLE gateway ADD COLUMN last_stats_rcvd_time timestamp without time zone;

--Added by Yogesh 02/10
--Added by Sreedhar 02/10
ALTER TABLE gateway ADD COLUMN subnet_mask character varying(255);
ALTER TABLE gateway ADD COLUMN default_gw character varying(255);

-- Added by Yogesh 02/14
ALTER TABLE inventorydevice ADD COLUMN device_type integer DEFAULT 0;
ALTER TABLE inventorydevice ADD COLUMN subnet_mask character varying(50);

-- Added by Sreedhar 02/15
ALTER TABLE gateway ADD COLUMN no_of_sensors integer DEFAULT 0;

-- Added by Yogesh 02/16

-- Added by Yogesh 02/18
ALTER TABLE firmware_upgrade ADD COLUMN device_type integer DEFAULT 0;

--Added by Sreedhar 02/22
ALTER TABLE gateway ADD COLUMN upgrade_status character varying(20);

ALTER TABLE ONLY energy_consumption_hourly
ADD CONSTRAINT energy_consumption_hourly_pkey PRIMARY KEY (id);

ALTER TABLE ONLY energy_consumption_daily
ADD CONSTRAINT energy_consumption_daily_pkey PRIMARY KEY (id);

--Added by Sreedhar 02/28
ALTER TABLE energy_consumption ADD COLUMN energy_cum bigint;  
ALTER TABLE energy_consumption ADD COLUMN energy_calib int;

--Added by Sreedhar 03/02
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.interPacketDelay', '75');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.interBucketDelay', '4');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.plcPacketSize', '192');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.zigbeePacketSize', '64');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'commandRetryDelay', '1000');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'commandNoOfRetries', '2');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'perf.pmStatsMode', '1');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'event.outageVolts', '70');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'event.outageAmbLight', '100');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'rest.api.key', '');


--Added by Yogesh 03/04
ALTER TABLE profile_handler ADD COLUMN to_off_linger integer DEFAULT 10;

--Added by Yogesh 03/05
ALTER TABLE profile_handler ALTER COLUMN to_off_linger SET DEFAULT 30;

--Added by Sreedhar 03/08
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.no_multicast_targets', '3');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.no_multicast_targets', '10');

--Added by Sreedhar 03/09
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.retry_interval', '10');

--Added by Yogesh 03/17
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.max_no_install_sensors', '100');

--Added by Sreedhar 03/17
DROP TYPE gems_miss_fixture_record;

CREATE TYPE gems_miss_fixture_record AS (
	fixt_id integer,
	last_time timestamp
);

CREATE OR REPLACE FUNCTION fillGemsMissingBuckets(todate timestamp with time zone) RETURNS void
    AS $$
DECLARE 
	rec gems_miss_fixture_record;	
	curr_time timestamp;	
	max_hourly timestamp;
	max_daily timestamp;
BEGIN
	FOR rec IN (
	  SELECT fixture_id as fixt_id, max(capture_at) as last_time 
	  FROM energy_consumption as ec 
	  WHERE capture_at <= toDate GROUP BY fixture_id)
	LOOP  	  
	  curr_time = rec.last_time + interval '5 min';	  
	  WHILE curr_time < todate LOOP
	    INSERT INTO energy_consumption (id, power_used, cost, price, capture_at, fixture_id, manual_saving, ambient_saving, tuneup_saving, occ_saving, zero_bucket) VALUES (nextval('energy_consumption_seq'), 0, 0, 0, curr_time, rec.fixt_id, 0, 0, 0, 0, 1);	    
	    curr_time = curr_time + interval '5 min';		
	  END LOOP;	  
	END LOOP;	

	SELECT max(capture_at) INTO max_hourly 
	FROM energy_consumption_hourly 
	WHERE capture_at <= toDate;
	curr_time = max_hourly + interval '1 hour';	  
	WHILE curr_time < todate LOOP
	  PERFORM aggregatehourlyenergyconsumption(curr_time);
	  curr_time = curr_time + interval '1 hour';	  
	END LOOP;

	SELECT max(capture_at) INTO max_daily 
  	FROM energy_consumption_daily
	WHERE capture_at <= toDate;
	curr_time = max_daily + interval '1 day';	  
	WHILE curr_time < todate LOOP
	  PERFORM aggregatedailyenergyconsumption(curr_time);
	  curr_time = curr_time + interval '1 day';
	END LOOP;

END;
$$
LANGUAGE plpgsql;

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'default.radio_rate', '2');

--Added by Sreedhar 03/22
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.max_time', '180');

--Added by Yogesh 03/23
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.su_app_pattern', 'su_app');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.su_firm_pattern', 'su_firm');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.gw_app_pattern', 'gw_app');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.gw_firm_pattern', 'gw_firm');

--Added by Sreedhar 03/23
DELETE FROM ballasts where ballast_name like '%DALI%';

--these are duplicates. so removing
DELETE FROM ballasts where id IN (14, 15, 16);
UPDATE fixture set ballast_id = 9 where ballast_id < 9;
UPDATE fixture set ballast_id = 13 where ballast_id in (14, 15, 16);

--Added by Sreedhar 03/25
ALTER TABLE fixture ADD COLUMN last_cmd_sent character varying(25);
ALTER TABLE fixture ADD COLUMN last_cmd_sent_at timestamp without time zone;
ALTER TABLE fixture ADD COLUMN last_cmd_status character varying(20);

--Added by Sreedhar 03/31
ALTER TABLE profile_handler ADD COLUMN initial_on_level smallint DEFAULT 50;
ALTER TABLE profile_handler ADD COLUMN profile_group_id smallint DEFAULT 1;

--Added by Yogesh 04/01, Modified 05/05
CREATE OR REPLACE FUNCTION updateProfileGroupIdForGroups() RETURNS void
    AS $$
DECLARE         
        default_groups text[] = ARRAY['Default','Breakroom','Conference Room','Open Corridor','Closed Corridor','Egress','Lobby','Warehouse','Open Office','Private Office','Restroom','Lab','Custom1','Custom2','Standalone','Highbay'];
        sGName character varying;
        iCurrId INTEGER;
        sCurrGName character varying;
        iCurrGPID INTEGER;
        nextGrpID INTEGER;
        isColumnExists boolean;
BEGIN
	isColumnExists = column_exists('profile_no','groups');
	IF isColumnExists = 't' THEN
	RETURN;
	END IF;
	
	UPDATE groups set name='Default' where name='Default Profile';
	UPDATE groups set name='Breakroom' where name='Break Room';
	UPDATE groups set name='Private Office' where name='Private Offices';
	UPDATE groups set name='Lab' where name='Labs';
	UPDATE groups set name='Egress' where name='Egress Lights';
	SELECT g.id, g.name, g.profile_handler_id INTO iCurrId, sCurrGName, iCurrGPID from groups g where g.name='Default';
	IF iCurrId IS NULL THEN
		-- Its a 1.2 database.
		DELETE FROM groups;
	END IF;
	
	FOR i in 1..array_upper(default_groups, 1) LOOP
		sGName = default_groups[i];
		SELECT g.id, g.name, g.profile_handler_id INTO iCurrId, sCurrGName, iCurrGPID from groups g where g.id=i;
		IF sGName <> sCurrGName THEN
			Raise Notice 'U (% => %), (% => %), %', i, iCurrId, sGName, sCurrGName, iCurrGPID;
			IF sCurrGName = 'Emergency Lights' THEN
				UPDATE groups set name=sGName where id = i;
			END IF;
		ELSE
			IF iCurrId IS NULL THEN
				Raise Notice 'I (% => %), (% => %), %', i, iCurrId, sGName, sCurrGName, iCurrGPID;
				INSERT INTO groups (id, name, company_id, profile_handler_id) values (i, sGName, 1, 1);
				SELECT setval('groups_seq', (SELECT MAX(id) FROM groups)) into nextGrpID;
			END IF;
		END IF;
	END LOOP;
	-- Update profile_group_id if necessary
	UPDATE profile_handler set profile_group_id=1 where id=1;

	FOR i in 1..array_upper(default_groups, 1) LOOP
		sGName = default_groups[i];
		SELECT g.id, g.name, g.profile_handler_id INTO iCurrId, sCurrGName, iCurrGPID from groups g where g.id=i;
		IF iCurrGPID <> 1 THEN
			UPDATE profile_handler set profile_group_id=iCurrId where id=iCurrGPID;
		END IF;
	END LOOP;

	-- 1.3 doesnot support Profile 'Corridor', instead there are two new profiles called 'Open Corridor' and 'Closed Corridor'
	UPDATE fixture set group_id=0, current_profile='Custom', original_profile_from='Corridor' where current_profile='Corridor';
	UPDATE fixture set current_profile='Egress' where current_profile='Emergency Lights';
	UPDATE fixture set original_profile_from='Egress' where original_profile_from='Emergency Lights';
	UPDATE fixture set current_profile='Default' where current_profile='Default Profile';
	UPDATE fixture set original_profile_from='Default' where original_profile_from='Default Profile';
	UPDATE fixture set current_profile='Breakroom' where current_profile='Break Room';
	UPDATE fixture set original_profile_from='Breakroom' where original_profile_from='Break Room';
	UPDATE fixture set current_profile='Private Office' where current_profile='Private Offices';
	UPDATE fixture set original_profile_from='Private Office' where original_profile_from='Private Offices';
	UPDATE fixture set current_profile='Lab' where current_profile='Labs';
	UPDATE fixture set original_profile_from='Lab' where original_profile_from='Labs';

	-- In 1.2 there are few profile handler whose checksum were set as null, this creates problems while fetching these profile via hibernate object model.
	UPDATE profile_handler set global_profile_checksum=0 where global_profile_checksum IS NULL;
	UPDATE profile_handler set profile_checksum=0 where profile_checksum IS NULL;

	--Added by Sree 04/27
	UPDATE fixture set group_id = 1 where current_profile = 'Default';
END;
$$
LANGUAGE plpgsql;

--Added by Yogesh 05/20 Upgrade to 1.3
select updateProfileGroupIdForGroups();


--Added by Yogesh 04/04
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (124, NULL, 'Lunera 2200                                                                                                                     ', '100-277                                                                                                                         ', 'LED                                                             ', 4, 1, 59, 'Lunera                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (125, NULL, 'CREE LR24                                                                                                                       ', '100-277                                                                                                                         ', 'LED                                                             ', 4, 1, 52, 'CREE                                                                                                                            ');

--Added by Sreedhar on 04/05
ALTER TABLE gateway ADD COLUMN boot_loader_version character varying(50);

--Added by Sreedhar on 04/07
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.multicast_inter_pkt_delay', '300');

--Added by Yogesh on 04/08
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (126, NULL, 'LDVFLIB100-1-40                                                                                                                 ', '120-277                                                                                                                         ', 'FO32T8                                                          ', 1, 1.15, 32, 'LUMEnergi                                                                                                                       ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (132, NULL, 'LDVFLIB100-2-75                                                                                                                 ', '277                                                                                                                         ', 'FO32T8                                                          ', 2, 1.15, 32, 'LUMEnergi                                                                                                                       ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (133, NULL, 'LDVFLIB100-2-76                                                                                                                 ', '120                                                                                                                         ', 'FO32T8                                                          ', 2, 1.15, 32, 'LUMEnergi                                                                                                                       ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (139, NULL, 'LDVFLIB100-3-112                                                                                                                ', '277                                                                                                                         ', 'FO32T8                                                          ', 3, 1.15, 32, 'LUMEnergi                                                                                                                       ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (140, NULL, 'LDVFLIB100-3-113                                                                                                                ', '120                                                                                                                         ', 'FO32T8                                                          ', 3, 1.15, 32, 'LUMEnergi                                                                                                                       ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (146, NULL, 'LDVFLIB100-2-60                                                                                                                 ', '120-277                                                                                                                         ', 'FO25T8                                                          ', 2, 1.15, 25, 'LUMEnergi                                                                                                                       ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (151, NULL, 'LDVFLIB100-3-92                                                                                                                 ', '120                                                                                                                         ', 'FO25T8                                                          ', 3, 1.15, 25, 'LUMEnergi                                                                                                                       ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (152, NULL, 'LDVFLIB100-3-91                                                                                                                 ', '277                                                                                                                         ', 'FO25T8                                                          ', 3, 1.15, 25, 'LUMEnergi                                                                                                                       ');
--Updated 19/05
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (153, 50726, 'QTP 2x28T5/UNV DIM PLUS-TCL                                                                                                     ', '120-277                                                                                                                     ', 'FP28                                                            ', 2, 1.00, 28, 'OSRAM                                                                                                                           ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (154, 49671, 'QT 1x54/120PHO-DIM                                                                                                              ', '120                                                                                                                         ', 'FP54T5HO                                                        ', 1, 1.00, 62, 'OSRAM                                                                                                                           ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (155, 49672, 'QT 1x54/277PHO-DIM                                                                                                              ', '277                                                                                                                         ', 'FP54T5HO                                                        ', 1, 1.00, 61, 'OSRAM                                                                                                                           ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (156, 49673, 'QT 2x54/120PHO-DIM                                                                                                              ', '120                                                                                                                         ', 'FP54T5HO                                                        ', 2, 1.00, 54, 'OSRAM                                                                                                                           ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (157, 49674, 'QT 2x54/277PHO-DIM                                                                                                              ', '277                                                                                                                         ', 'FP54T5HO                                                        ', 2, 1.00, 54, 'OSRAM                                                                                                                           ');


INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (282, 'Lunera                                                                                                                          ', 'LED', 'LED                                                                                                                             ', NULL, NULL, 32, 0, 0, 1, 48, NULL, NULL);

--Added by Sreedhar 04/11
UPDATE profile_handler set initial_on_level = 50 where initial_on_level = 0;

--Added by Yogesh 04/12
CREATE OR REPLACE FUNCTION updateWeekDayBits(text, text, iOrder integer, text) RETURNS void
    AS $$
DECLARE 
	sDay ALIAS FOR $1;
	sGroupname ALIAS For $2;
	sType ALIAS For $4;
	wid INTEGER;
	pcid INTEGER;
BEGIN
	SELECT profile_configuration_id from profile_handler where id=(select profile_handler_id from groups where name=sGroupname) INTO pcid;
	if (pcid is NOT NULL)
	THEN
		SELECT id from weekday where day=sDay and profile_configuration_id=pcid into wid;
		
		if(wid is NULL) 
		THEN
			INSERT INTO weekday (id, day, profile_configuration_id, short_order, type) values (nextval('weekday_seq'), sDay, pcid, iOrder, sType);
		END IF;
	END IF;
END;
$$
LANGUAGE plpgsql;
-- Insert weekday bits if not present for Open Corridor
select updateWeekDayBits('Monday', 'Open Corridor', 1, 'weekday');
select updateWeekDayBits('Tuesday', 'Open Corridor', 2, 'weekday');
select updateWeekDayBits('Wednesday', 'Open Corridor', 3, 'weekday');
select updateWeekDayBits('Thursday', 'Open Corridor', 4, 'weekday');
select updateWeekDayBits('Friday', 'Open Corridor', 5, 'weekday');
select updateWeekDayBits('Saturday', 'Open Corridor', 6, 'weekend');
select updateWeekDayBits('Sunday', 'Open Corridor', 7, 'weekend');
-- Insert weekday bits if not present for Open Office 
select updateWeekDayBits('Monday', 'Open Office', 1, 'weekday');
select updateWeekDayBits('Tuesday', 'Open Office', 2, 'weekday');
select updateWeekDayBits('Wednesday', 'Open Office', 3, 'weekday');
select updateWeekDayBits('Thursday', 'Open Office', 4, 'weekday');
select updateWeekDayBits('Friday', 'Open Office', 5, 'weekday');
select updateWeekDayBits('Saturday', 'Open Office', 6, 'weekend');
select updateWeekDayBits('Sunday', 'Open Office', 7, 'weekend');

--Added By Yogesh 04/19 (to support mouse over temperature display without going to energyconsumption table => optimization)
ALTER TABLE fixture ADD COLUMN avg_temperature smallint DEFAULT 0;

--Added by Sreedhar 04/22
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.unicast_inter_pkt_delay', '50');

--Added by Sreedhar 04/22
ALTER TABLE fixture ADD COLUMN baseline_power numeric(19,2) DEFAULT 0;

--Added by Sreedhar 04/26
ALTER TABLE inventorydevice ADD COLUMN curr_app smallint DEFAULT 2;


--Added by Yogesh 05/02
CREATE OR REPLACE FUNCTION update_location_change() RETURNS "trigger" AS $$
	BEGIN
	  IF tg_op = 'UPDATE' THEN
	     IF old.name <> new.name THEN
		UPDATE device SET location = TEXTCAT(c.name,(TEXTCAT (' -> ', TEXTCAT(b.name, TEXTCAT(' -> ', f.name)))))			
		from campus c, 
		building b , 
		floor f,
		company co where
		floor_id=f.id and
		f.building_id=b.id and
		b.campus_id=c.id and
		c.company_id = co.id;

	    END IF;
	  END IF;
	  RETURN new;
	END
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_campus_change AFTER UPDATE ON campus
  FOR EACH ROW EXECUTE PROCEDURE update_location_change();

CREATE TRIGGER update_building_change AFTER UPDATE ON building
  FOR EACH ROW EXECUTE PROCEDURE update_location_change();

CREATE TRIGGER update_floor_change AFTER UPDATE ON floor
  FOR EACH ROW EXECUTE PROCEDURE update_location_change();

CREATE TRIGGER update_area_change AFTER UPDATE ON area
  FOR EACH ROW EXECUTE PROCEDURE update_location_change(); 


--Added by Sreedhar 05/06

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.ack_dbupdate_threads', '15');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.response_listener_threads', '15');

--add the unique constraint
ALTER TABLE energy_consumption ADD CONSTRAINT unique_energy_consumption UNIQUE(capture_at, fixture_id);

ALTER TABLE energy_consumption_hourly ADD CONSTRAINT unique_energy_consumption_hourly UNIQUE(capture_at, fixture_id);

ALTER TABLE energy_consumption_daily ADD CONSTRAINT unique_energy_consumption_daily UNIQUE(capture_at, fixture_id);

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.validationTargetAmbLight', '9990');

-- Added by Yogesh 19/05
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (283, 'OSRAM                                                                                                                          ', 'FP28', 'FP28                                                                                                                             ', NULL, NULL, 63, 0, 0, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (284, 'OSRAM                                                                                                                          ', 'FP54T5HO', 'FP54T5HO                                                                                                                             ', NULL, NULL, 120, 0, 0, 1, 48, NULL, NULL);

--Added By Yogesh 24/05

--Added By Yogesh 25/05
UPDATE gateway set location = TEXTCAT(c.name,(TEXTCAT (' -> ', TEXTCAT(b.name, TEXTCAT(' -> ', f.name))))) from campus c, building b, floor f, company co, company_campus cc where floor_id=f.id and f.building_id=b.id and b.campus_id=c.id and cc.campus_id = c.id and co.id = cc.company_id;

--Added by Sreedhar 06/06
CREATE UNIQUE INDEX unique_snap_address ON fixture (snap_address);

--Added by Sreedhar 06/07
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.5min_table', '90');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.hourly_table', '365');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.daily_table', '3650');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.events_and_fault_table', '90');

--function to prune energy consumption tables
CREATE OR REPLACE FUNCTION prunedatabase() RETURNS void
    AS $$
DECLARE 
	no_days numeric;
	no_days_text text;
	no_days_time timestamp;	
	tm timestamp = now();

BEGIN
	
	SELECT value INTO no_days
	FROM system_configuration
	WHERE name = 'db_pruning.5min_table';

	no_days_text = no_days || ' day';
	no_days_time = tm - no_days_text::interval;
	
	DELETE FROM energy_consumption WHERE capture_at < no_days_time;

	SELECT value INTO no_days
	FROM system_configuration
	WHERE name = 'db_pruning.hourly_table';

	no_days_text = no_days || ' day';
	no_days_time = tm - no_days_text::interval;
	
	DELETE FROM energy_consumption_hourly WHERE capture_at < no_days_time;

	SELECT value INTO no_days
	FROM system_configuration
	WHERE name = 'db_pruning.daily_table';

	no_days_text = no_days || ' day';
	no_days_time = tm - no_days_text::interval;
	
	DELETE FROM energy_consumption_daily WHERE capture_at < no_days_time;

	SELECT value INTO no_days
	FROM system_configuration
	WHERE name = 'db_pruning.em_stats_table';

	no_days_text = no_days || ' day';
	no_days_time = tm - no_days_text::interval;
	
	DELETE FROM em_stats WHERE capture_at < no_days_time;

END;
$$
LANGUAGE plpgsql;

ALTER FUNCTION public.prunedatabase() OWNER TO postgres;

--function to prune events and fault table
CREATE OR REPLACE FUNCTION pruneeventsfault() RETURNS void
    AS $$
DECLARE 
	no_days numeric;
	no_days_text text;
	no_days_time timestamp;	
	history_no_days numeric;
    history_no_days_text text;
    history_no_days_time timestamp;
	tm timestamp = now();
	no_records bigint;
BEGIN
		--prune based on records
		SELECT value INTO no_records
		FROM system_configuration
		WHERE name = 'db_pruning.events_and_fault_table_records';
	    
	    INSERT INTO events_and_fault_history (select * from events_and_fault where severity = 'Info' order by event_time desc offset no_records);
	    delete from events_and_fault where id in (select id from events_and_fault WHERE severity = 'Info' order by event_time desc offset no_records);
	    
	    --prune based on days
		SELECT value INTO no_days
		FROM system_configuration
		WHERE name = 'db_pruning.events_and_fault_table';
	
		no_days_text = no_days || ' day';
		no_days_time = tm - no_days_text::interval;
		
		INSERT INTO events_and_fault_history (select * from events_and_fault WHERE event_time < no_days_time and severity = 'Info');
	    DELETE FROM events_and_fault WHERE event_time < no_days_time and severity = 'Info';
		
	    
		--prune history
		SELECT value INTO history_no_days
	    FROM system_configuration
	    WHERE name = 'db_pruning.events_and_fault_history_table';
	
	    history_no_days_text = history_no_days|| ' day';
	    history_no_days_time = tm - history_no_days_text::interval;
	    DELETE FROM events_and_fault_history WHERE event_time < history_no_days_time;	
		
END;
$$
LANGUAGE plpgsql;

ALTER FUNCTION public.pruneeventsfault() OWNER TO postgres;

CREATE OR REPLACE FUNCTION cleanupeventsfault() RETURNS void
    AS $$
DECLARE 	
	rec_limit bigint;
	rec_count bigint;
	rec_x bigint;
BEGIN
		--prune based on records
		SELECT value INTO rec_limit
		FROM system_configuration
		WHERE name = 'db_pruning.events_and_fault_table_cleanup_limit';

		SELECT count(*) INTO rec_count FROM events_and_fault;
		rec_x = rec_count - rec_limit;
		IF rec_x > 0 THEN
		DELETE FROM events_and_fault 
		WHERE id = any (array(SELECT id FROM events_and_fault ORDER BY event_time LIMIT rec_x));
		END IF;	

		SELECT count(*) INTO rec_count FROM events_and_fault_history;

		rec_x = rec_count - rec_limit;
		IF rec_x > 0 THEN
		DELETE FROM events_and_fault_history 
		WHERE id = any (array(SELECT id FROM events_and_fault_history ORDER BY event_time LIMIT rec_x));
		END IF;	
		
END;
$$
LANGUAGE plpgsql;	    

ALTER FUNCTION public.cleanupeventsfault() OWNER TO postgres;


--Added by Sreedhar on 06/10
ALTER TABLE fixture ADD COLUMN voltage smallint DEFAULT 277;
UPDATE fixture set voltage = 277 WHERE voltage IS NULL;

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'fixture.default_voltage', '277');
--Added by Nilesh on 22/05/15 for EM-158
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'plugload.default_voltage', '120');

--Added by Yogesh 06/14 
ALTER TABLE events_and_fault ADD COLUMN gateway_id bigint;

--Added by Sreedhar 06/16
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.su_pyc_pattern', 'su_pyc');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.gw_pyc_pattern', 'gw_pyc');

--Bacnet configuration properties
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.vendor_id', '516');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.server_port', '47808');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.network_id', '9999');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.max_APDU_length', '1476');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.APDU_timeout', '10000');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.device_base_instance', '400000');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.su_ble_pattern', '_ble');
--Added by Yogesh 06/16
--audit log table 
CREATE TABLE ems_audit
(
        id bigint NOT NULL,
        txn_id bigint DEFAULT 0,
        device_id bigint NOT NULL,
        device_type smallint DEFAULT 0,
	device_name character varying,
        attempts smallint DEFAULT 1,
        action character varying,
        start_time timestamp,
        end_time timestamp,
        status character varying,
        comments character varying,
        CONSTRAINT ems_audit_key_pk PRIMARY KEY (id)
);

ALTER TABLE ems_audit OWNER TO postgres;

--
-- Name: ems_audit_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE ems_audit_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;

ALTER TABLE ems_audit_seq OWNER TO postgres;

--Added by Yogesh 06/22
DELETE from ems_audit where device_name IS NULL;
ALTER TABLE ems_audit ADD COLUMN device_name character varying;

--Added by Yogesh 06/27
ALTER TABLE ems_audit ALTER COLUMN status TYPE character varying;

--Added by Yogesh 06/28
ALTER TABLE fixture ADD COLUMN commission_status integer DEFAULT 0;

--Fixture sorting path (0) Top to bottom, (1) Botton to Top, (2) Left to Right, (3) Right to Left
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'fixture.sorting.path', '0');

--
-- Name: update_sec_gateway_change; Type: PROCEDURE; Schema: public; Owner: postgres
--
CREATE OR REPLACE FUNCTION update_sec_gateway_change() RETURNS "trigger"
    AS $$
	BEGIN
	  IF tg_op = 'UPDATE' THEN
	     IF old.sec_gw_id <> new.sec_gw_id THEN
			NEW.GATEWAY_ID := NEW.sec_gw_id;
	     END IF;
	  END IF;
	  RETURN new;
	END
$$
    LANGUAGE plpgsql;

ALTER FUNCTION public.update_sec_gateway_change() OWNER TO postgres;

--
-- Name: update_fixture_gateway_change; Type: TRIGGER; Schema: public; Owner: postgres
--
CREATE TRIGGER update_fixture_gateway_change
    AFTER UPDATE ON fixture
    FOR EACH ROW
    EXECUTE PROCEDURE update_sec_gateway_change();

-- Name: prunemsaudit function to prune ems audit table
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.emsaudit_table', '7');

CREATE OR REPLACE FUNCTION pruneemsaudit() RETURNS void
    AS $$
DECLARE 
	no_days numeric;
	no_days_text text;
	no_days_time timestamp;	
	tm timestamp = now();

BEGIN
	SELECT value INTO no_days
	FROM system_configuration
	WHERE name = 'db_pruning.emsaudit_table';

	no_days_text = no_days || ' day';
	no_days_time = tm - no_days_text::interval;
	
	DELETE FROM ems_audit WHERE start_time < no_days_time;
END;
$$
LANGUAGE plpgsql;

ALTER FUNCTION public.pruneemsaudit() OWNER TO postgres;

-- Scaling factor on energy consumption for various volts
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.apply.scaling.factor', 'true');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.scaling.for.110v', '0.0511');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.adj.for.110v', '6.9192');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.scaling.for.277v', '1.4522');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.adj.for.277v', '12.754');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.scaling.for.240v', '0.5');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.adj.for.240v', '0');

--Added by Sree on 07/20/2011
ALTER TABLE fixture ADD COLUMN is_hopper integer DEFAULT 0;

-- Added by Sree on 07/21/2011. 
-- This should be removed for upgrades from 1.4
delete from event_type;

update pg_attribute set atttypmod = 74 where attrelid = 'event_type'::regclass and attname = 'type';

--12/26/2012 Sree
ALTER TABLE event_type ADD COLUMN severity smallint;
ALTER TABLE event_type ADD COLUMN active smallint;

--Sree 06/11/2014
ALTER TABLE event_type ALTER COLUMN severity SET NOT NULL;

INSERT INTO event_type (id, "type", description, severity, active) VALUES (nextval('event_type_seq'), 'Fixture Out', NULL, 2, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES (nextval('event_type_seq'), 'Push Profile', NULL, 5, 0);
INSERT INTO event_type (id, "type", description, severity, active) VALUES (nextval('event_type_seq'), 'Profile Mismatch', NULL, 4, 0);
INSERT INTO event_type (id, "type", description, severity, active) VALUES (nextval('event_type_seq'), 'Bad Profile', NULL, 3, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES (nextval('event_type_seq'), 'Fixture Upgrade', NULL, 5, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES (nextval('event_type_seq'), 'Gateway Upgrade', NULL, 5, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES (nextval('event_type_seq'), 'ERC Upgrade', NULL, 5, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES (nextval('event_type_seq'), 'Fixture CU Failure', NULL, 2, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES (nextval('event_type_seq'), 'Fixture Image Checksum Failure', NULL, 3, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES (nextval('event_type_seq'), 'DR Condition', NULL, 5, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES (nextval('event_type_seq'), 'Fixture associated Group Changed', NULL, 5, 0);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Bacnet', NULL, 5, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Discovery', NULL, 5, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'ERC Discovery', NULL, 5, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Commissioning', NULL, 5, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'ERC Commissioning', NULL, 5, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Profile Mismatch User Action', NULL, 4, 0);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture Hardware Failure', NULL, 2, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture Too Hot', NULL, 3, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture CPU Usage is High', NULL, 3, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Gateway configuration error', NULL, 2, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Erroneous Energy Reading', NULL, 3, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Gateway Connection Failure', NULL, 2, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'EM upgrade', NULL, 5, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Scheduler', NULL, 5, 0);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture Group change', NULL, 5, 1);
INSERT INTO event_type (id, type, description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Gateway unreachable', NULL, 2, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Wireless Params', NULL, 2, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Lamp Out', NULL, 2, 1);
INSERT INTO event_type (id, "type", description,severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Download Power Usage Characterization', NULL, 5, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Placed Fixture Upload', NULL, 5, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture Configuration Upload', NULL, 5, 1);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'EmailNotification', NULL, 2, 0);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'NetworkNotification', NULL, 2, 0);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Sftp Remote Backup', NULL, 2, 0);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Attached Storage Backup', NULL, 2, 0);
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Plugload Baseline Mismatch', NULL, 2, 1);


--Events should not be added here. It should be added at the end of the file with more attributes

-- adding a configuration that will decide whether bacnet configuration should be shown or not in the menu
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'menu.bacnet.show', 'false');

-- Update Energy Consumption scalaing and Adjustment factors...
UPDATE system_configuration set value='0.0511' where name='ec.scaling.for.110v';
UPDATE system_configuration set value='6.9192' where name='ec.adj.for.110v';
UPDATE system_configuration set value='1.4522' where name='ec.scaling.for.277v';
UPDATE system_configuration set value='12.754' where name='ec.adj.for.277v';
UPDATE system_configuration set value='0.5' where name='ec.scaling.for.240v';
UPDATE system_configuration set value='0' where name='ec.adj.for.240v';

--Added by Sree 07/22
ALTER TABLE profile_handler ADD COLUMN profile_flag smallint DEFAULT 0;
UPDATE profile_handler SET profile_flag = 0 WHERE profile_flag IS NULL;

--Added by Yogesh 07/23
UPDATE fixture SET is_hopper = 0 WHERE is_hopper IS NULL;

--Added by Yogesh 07/28
ALTER TABLE fixture ADD COLUMN version_synced integer DEFAULT 0;
UPDATE fixture SET version_synced=0 WHERE version_synced IS NULL;

-- Added by Naveen 07/29
INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'Mobile');

CREATE SEQUENCE user_switches_seq
   INCREMENT 1
   START 1;

ALTER TABLE user_switches_seq OWNER TO postgres;

CREATE TABLE user_switches (
	user_id bigint,
    switch_id bigint,
    id bigint DEFAULT nextval('user_switches_seq'::regclass) NOT NULL
);

ALTER TABLE public.user_switches OWNER TO postgres;

ALTER TABLE ONLY user_switches
    ADD CONSTRAINT user_switches_pkey PRIMARY KEY (id);

--Added by Sree 08/02
ALTER TABLE roles ADD CONSTRAINT unique_roles_name UNIQUE (name);

--Added by Sree 08/02
ALTER TABLE profile_handler ADD COLUMN initial_on_time integer DEFAULT 5;
UPDATE profile_handler SET initial_on_time = 5 WHERE initial_on_time IS NULL;

--Added by Sreedhar 08/08
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'commissioning.inactivity_timeout', '900');

--Added by Sreedhar 08/10
CREATE TABLE outage_base_power (
  id bigint NOT NULL,
  fixture_id bigint,
  volt_level smallint,
  base_power numeric(19,2),
  CONSTRAINT outage_base_power_pkey PRIMARY KEY (id),
  CONSTRAINT unique_system_configuration_fixture_id_volt_level UNIQUE (fixture_id, volt_level)
);

ALTER TABLE outage_base_power OWNER TO postgres;

--
-- Name: outage_base_power_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE outage_base_power_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE outage_base_power_seq OWNER TO postgres;

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'perf.base_power_correction_percentage', '5');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'event.outage_detect_percentage', '10');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'event.fixture_outage_detect_watts', '7');

--Added by Sree 08/15
ALTER TABLE company ADD COLUMN time_zone character varying(128);
ALTER TABLE company ALTER COLUMN time_zone SET DEFAULT 'America/Los_Angeles';
UPDATE company set time_zone = 'America/Los_Angeles' WHERE time_zone IS NULL;

--Added by Dhanesh 03/13/15
ALTER TABLE company ADD COLUMN ntp_enable character varying DEFAULT 'Y';
ALTER TABLE company ADD COLUMN ntp_server_list  character varying DEFAULT '0.us.pool.ntp.org,1.us.pool.ntp.org,2.us.pool.ntp.org,3.us.pool.ntp.org';

--Added by Yogesh 08/18
CREATE UNIQUE INDEX unique_gateway_name ON gateway (gateway_name);
CREATE UNIQUE INDEX unique_gateway_mac_address ON gateway (mac_address);

--Added by Sree 08/25
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.validationTargetRelAmbLight', '200');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.validationMaxEnergyPercentReading', '40');

--Added by Sree 09/08
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.no_multicast_retransmits', '2');
UPDATE system_configuration SET value = '180' WHERE name = 'imageUpgrade.interPacketDelay';
UPDATE system_configuration SET value = '20' WHERE name = 'imageUpgrade.no_multicast_targets';

--Added by Yogesh 10/13
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'dr.service_enabled', 'false');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'dr.repeat_interval', '60000');

--
-- Name: drrecord; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE drrecord AS (
	"Day" timestamp,
	"powerUsed" numeric,
	"basePowerUsed" numeric,
	"savedPower" numeric,
	"savedCost" numeric,
	"baseCost" numeric,
	"avgPrice" numeric
);

--
-- Name: grooupecrecord; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE groupecrecord AS (
	"i" numeric,
	"name" character varying,
	"powerUsed" numeric,
	"basePowerUsed" numeric,
	"savedPower" numeric,
	"savedCost" numeric,
	"totalFixtures" numeric
);


ALTER TYPE public.drrecord OWNER TO postgres;

--
-- Name: loadAvgEnergyConsumptionBetweenPeriodsPerDay(startTime timestamp with time zone, endTime timestamp with time zone, groupId integer, noOfDays integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE OR REPLACE FUNCTION loadAvgEnergyConsumptionBetweenPeriodsPerDay(startTime timestamp with time zone, endTime timestamp with time zone, groupId integer, noOfDays integer) RETURNS SETOF drrecord
    AS $$
DECLARE
 rec drrecord;
 count int;
 i int;
BEGIN
	i := 0;
	count := $4;
	FOR rec IN (SELECT date_trunc('day', capture_at - interval '1 hour') AS Day,
				AVG(power_used) AS "powerUsed",
				AVG(base_power_used) as "basePowerUsed",
				AVG(saved_power_used) as "savePower",
				AVG(saved_cost) as "savedCost",
				AVG(base_cost) as "baseCost", 
				AVG(price) as "avgPrice" FROM energy_consumption as ec 
				where ec.zero_bucket != 1 and ec.fixture_id in (select id from fixture where group_id=$3) 
				and capture_at::time <= endTime::time and capture_at::time >= startTime::time group by Day order by Day desc)
	LOOP 	
			i := i + 1;
			IF i > count THEN
				EXIT;			
			END IF;
			RETURN NEXT rec;  
	END LOOP;  
END;
$$
    LANGUAGE plpgsql;
    
--
-- Name: loadGroupEnergyConsumptionBetweenPeriods(startTime timestamp with time zone, endTime timestamp with time zone); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE OR REPLACE FUNCTION loadGroupEnergyConsumptionBetweenPeriods(startTime timestamp with time zone, endTime timestamp with time zone) RETURNS SETOF groupecrecord
    AS $$
DECLARE
 rec groupecrecord;
BEGIN
	FOR rec IN (select '0' AS "i",
		'Custom' AS "name",
		(SELECT COALESCE(sum(ec.power_used), 0)) AS "powerUsed",
		(SELECT COALESCE(sum(ec.base_power_used),0)) AS "basePowerUsed", 
		(SELECT COALESCE(sum(ec.saved_power_used),0)) AS "savedPower",
		(SELECT COALESCE(sum(ec.saved_cost),0)) AS "savedCost", 
		(select count(id) from fixture where group_id=0) AS "totalFixtures" from fixture f 
		join energy_consumption ec on ec.fixture_id = f.id where
		ec.zero_bucket != 1 and ec.capture_at::time <= endTime::time and ec.capture_at::time >= startTime::time and f.group_id=0)
	LOOP 	
			RETURN NEXT rec;  
	END LOOP;  
	FOR rec IN (select g.id AS "i",
		g.name AS "name",
		(SELECT COALESCE(sum(ec.power_used), 0)) AS "powerUsed",
		(SELECT COALESCE(sum(ec.base_power_used),0)) AS "basePowerUsed", 
		(SELECT COALESCE(sum(ec.saved_power_used),0)) AS "savedPower",
		(SELECT COALESCE(sum(ec.saved_cost),0)) AS "savedCost", 
		(select count(id) from fixture where group_id=g.id) AS "totalFixtures" from groups g 
		left join fixture f on g.id=f.group_id 
		left join energy_consumption ec on ec.fixture_id = f.id 
		and ec.zero_bucket != 1 and capture_at::time <= endTime::time and capture_at::time >= startTime::time group by g.id, g.name order by g.id)
	LOOP 	
			RETURN NEXT rec;  
	END LOOP;  
END;
$$
    LANGUAGE plpgsql;
    
--Added by Sree 10/20
CREATE TABLE image_upgrade_job (
  id bigint NOT NULL,
  job_name character varying,
  image_name character varying,
  device_type character varying,
  scheduled_time timestamp without time zone,
  start_time timestamp without time zone,
  end_time timestamp without time zone,
  no_of_retries integer,
  status character varying,
  description character varying,
  CONSTRAINT image_upgrade_job_pk PRIMARY KEY (id) 
);

ALTER TABLE image_upgrade_job OWNER TO postgres;

CREATE SEQUENCE image_upgrade_job_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE image_upgrade_job_seq OWNER TO postgres;

CREATE TABLE image_upgrade_device_status (
  id bigint NOT NULL,
  job_id bigint NOT NULL,
  device_id bigint NOT NULL,
  start_time timestamp without time zone,
  end_time timestamp without time zone,
  no_of_attempts integer,
  status character varying,
  description character varying,
  CONSTRAINT image_upgrade_device_status_pk PRIMARY KEY (id) 
);

ALTER TABLE image_upgrade_device_status OWNER TO postgres;

CREATE SEQUENCE image_upgrade_device_status_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE image_upgrade_device_status_seq OWNER TO postgres;

--Added by Sreedhar 10/27
ALTER TABLE energy_consumption ADD COLUMN min_volts smallint;
ALTER TABLE energy_consumption ADD COLUMN max_volts smallint;
ALTER TABLE energy_consumption ADD COLUMN energy_ticks int;

ALTER TABLE fixture ADD COLUMN temperature_offset float(2);
ALTER TABLE fixture ADD COLUMN last_boot_time timestamp without time zone;

CREATE TABLE dr_users (
  id bigint NOT NULL,
  name character varying NOT NULL,
  "password" character varying NOT NULL,
  CONSTRAINT dr_users_pk PRIMARY KEY (id) 
);

ALTER TABLE dr_users OWNER TO postgres;

CREATE SEQUENCE dr_users_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE dr_users_seq OWNER TO postgres;

--Sree 11/16
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.default_fail_retries', '1');

--Sree 11/17
ALTER TABLE fixture ADD COLUMN cu_version character varying(20);
UPDATE fixture set cu_version = '23' WHERE cu_version IS NULL;

--Yogesh 11/21
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.su_20_pattern', 'su.bin');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.gw_20_pattern', 'gw.tar');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.cu_20_pattern', 'cu.bin');

--Yogesh 11/28
ALTER TABLE ems_audit ALTER COLUMN device_id DROP NOT NULL;

--Yogesh 12/18
UPDATE ballasts set wattage = 28 where id = 153;
UPDATE ballasts set wattage = 54 where id = 156;
UPDATE ballasts set wattage = 54 where id = 157;

--Yogesh 12/18
UPDATE system_configuration set value='gw.tar' where name='upgrade.gw_20_pattern';

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.pmstats_processing_threads', '1');

--Sreedhar 01/14
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.no_test_runs', '20');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.test_file', '429_su.bin');

--Sreedhar 01/18
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ssl.enabled', 'true');

--Sreedhar 01/20
UPDATE system_configuration set value = '35' where name = 'cmd.unicast_inter_pkt_delay';

--Sreedhar 01/30
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.interPacketDelay_2', '20');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.multicast_inter_pkt_delay_2', '75');

--Sreedhar 02/02
ALTER TABLE fixture ADD COLUMN model_no character varying(50);

--Sreedhar 02/06
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'stats.temp_offset_1', '18');

--Sreedhar 02/17
UPDATE system_configuration SET value = 'true' where name = 'ssl.enabled';

--Sreedhar 03/02
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'default_su_hop_count', '3');

--Sreedhar 03/06
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.validationTargetRelAmbLight_2', '500');

--
-- GEMS to EM: From 2.0 to 2.1
--

CREATE USER debugems WITH PASSWORD 'debugems';

GRANT CONNECT ON DATABASE ems TO debugems;
GRANT USAGE ON SCHEMA public TO debugems;

SELECT 'GRANT SELECT ON ' || relname || ' TO debugems;'
FROM pg_class JOIN pg_namespace ON pg_namespace.oid = pg_class.relnamespace
WHERE nspname = 'public' AND relkind IN ('r', 'v');


CREATE SEQUENCE tenants_seq
    INCREMENT BY 1
    MAXVALUE 999999999999999999
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.tenants_seq OWNER TO postgres;

CREATE TABLE tenants (
    id bigint NOT NULL,
    name character varying NOT null,
    email character varying NOT NULL,   
    address character varying,
    phone_no character varying,
    status character varying,
    valid_domain character varying    
);

ALTER TABLE ONLY tenants
    ADD CONSTRAINT tenants_pk PRIMARY KEY (id);

ALTER TABLE public.tenants OWNER TO postgres;

CREATE SEQUENCE tenant_locations_seq
    INCREMENT BY 1
    MAXVALUE 999999999999999999
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.tenant_locations_seq OWNER TO postgres;

--
-- TOC entry 1352 (class 1259 OID 16448)
-- Dependencies: 5
-- Name: users; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE tenant_locations (
    id bigint NOT NULL,
    approved_location_type character varying NOT null,
    location_id bigint NOT NULL,   
    tenant_id bigint NOT NULL
);


ALTER TABLE ONLY tenant_locations
    ADD CONSTRAINT tenant_locations_pk PRIMARY KEY (id);
    
ALTER TABLE ONLY public.tenant_locations  
    ADD CONSTRAINT tenant_location_id FOREIGN KEY(tenant_id) REFERENCES tenants(id);

ALTER TABLE public.tenant_locations OWNER TO postgres;


--- convert all users with mobile role to employee and delete mobile role
UPDATE USERS set role_id=3 where role_id in (Select id from roles r where r.name = 'Mobile') ;
DELETE FROM ROLEs where id >= 4 and name = 'Mobile';

--- Update existing roles and define new roles
INSERT INTO roles (id, name) VALUES (4, 'FacilitiesAdmin');
INSERT INTO roles (id, name) VALUES (5, 'TenantAdmin');
INSERT INTO roles (id, name) VALUES (6, 'Bacnet');

ALTER TABLE USERS ADD COLUMN tenant_id bigint;
ALTER TABLE USERS ADD COLUMN  status character varying;
ALTER TABLE USERS ADD COLUMN no_login_attempts bigint DEFAULT 0;
ALTER TABLE USERS ADD COLUMN identifier_forgot_password  character varying DEFAULT NULL;
ALTER TABLE USERS ADD COLUMN password_changed_at date DEFAULT NULL;
ALTER TABLE USERS ADD COLUMN unlock_time timestamp without time zone DEFAULT NULL;
ALTER TABLE USERS ADD COLUMN  secret_key character varying;

ALTER TABLE ONLY public.users  
    ADD CONSTRAINT user_tenant_id FOREIGN KEY(tenant_id) REFERENCES tenants(id);
    
--Set all the user status to active
update users set status='ACTIVE' where status is null;

-- set all users password_changed_at if it null during upgrade
update users set password_changed_at=current_date where password_changed_at is null;

CREATE SEQUENCE user_location_seq
    INCREMENT BY 1
    MAXVALUE 999999999999999999
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.user_location_seq OWNER TO postgres;

CREATE TABLE user_locations (
    id bigint NOT NULL,
    approved_location_type character varying NOT null,
    location_id bigint NOT NULL,   
    user_id bigint NOT NULL
);


ALTER TABLE ONLY user_locations
    ADD CONSTRAINT user_locations_pk PRIMARY KEY (id);
    
ALTER TABLE ONLY public.user_locations  
    ADD CONSTRAINT user_location_id FOREIGN KEY(user_id) REFERENCES users(id);

ALTER TABLE public.user_locations OWNER TO postgres;

--- Add the company_id foreign key in Campus
ALTER TABLE campus ADD COLUMN company_id bigint;
ALTER TABLE ONLY public.campus  
    ADD CONSTRAINT campus_company_id FOREIGN KEY(company_id) REFERENCES company(id);
UPDATE campus set company_id=cc.company_id from campus c, company_campus cc where c.id = cc.campus_id;
 


ALTER TABLE company ADD tenant_id bigint;
ALTER TABLE company ADD CONSTRAINT fk_company_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE campus ADD tenant_id bigint;
ALTER TABLE campus ADD CONSTRAINT fk_campus_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE building ADD tenant_id bigint;
ALTER TABLE building ADD CONSTRAINT fk_building_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE floor ADD tenant_id bigint;
ALTER TABLE floor ADD CONSTRAINT fk_floor_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);
ALTER TABLE floor ADD COLUMN floor_plan_uploaded_time timestamp without time zone;

ALTER TABLE area ADD tenant_id bigint;
ALTER TABLE area ADD CONSTRAINT fk_area_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);

select setval('dr_target_seq', (select max(id) from dr_target) + 1);
select setval('pricing_seq', (select max(id) from pricing) + 1);


--- gems_groups
CREATE TABLE gems_groups
(
  id bigint NOT NULL,
  group_name character varying NOT NULL,
  description character varying,
  floor_id bigint,
  CONSTRAINT gems_groups_pkey PRIMARY KEY (id),
  CONSTRAINT fk_gems_groups_floor FOREIGN KEY (floor_id) REFERENCES floor (id)  MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE gems_groups OWNER TO postgres;

CREATE SEQUENCE gems_group_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


--- gems group fixtures
CREATE TABLE gems_group_fixture
(
  id bigint NOT NULL,
  group_id bigint NOT NULL,
  fixture_id bigint,
  CONSTRAINT gems_group_fixture_pkey PRIMARY KEY (id),
  CONSTRAINT gems_group_fixture_fixture_id_fkey FOREIGN KEY (fixture_id)
      REFERENCES fixture (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT gems_group_fixture_group_id_fkey FOREIGN KEY (group_id)
      REFERENCES gems_groups (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT group_fixture_unique UNIQUE (group_id, fixture_id)
);
ALTER TABLE gems_group_fixture OWNER TO postgres;

CREATE SEQUENCE gems_group_fixture_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

--- Ldap based authentication config
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'auth.auth_type', 'DATABASE');

-- adding a configuration that will decide whether openADR configuration should be shown or not in the menu
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'menu.openADR.show', 'false');

-- adding a configuration that will set the processing parameters of pm stat
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.pmstats_queue_threshold', '5000');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.pmstats_process_batch_size', '10');

---Put constraint on company so that only one company is created (lalit)
ALTER TABLE company ADD CONSTRAINT single_company_check CHECK (id = 1 );


CREATE OR REPLACE FUNCTION fillGemsZeroBucketsForNextTenMinutes(todate timestamp with time zone) RETURNS void
    AS $$
DECLARE 
	rec gems_miss_fixture_record;	
	curr_time timestamp;	
	max_hourly timestamp;
	max_daily timestamp;
BEGIN
	FOR rec IN (
	  SELECT fixture_id as fixt_id, max(capture_at) as last_time 
	  FROM energy_consumption as ec 
	  WHERE capture_at <= toDate GROUP BY fixture_id)
	LOOP  	  
	  curr_time = rec.last_time + interval '5 min';	  
	  WHILE curr_time < todate LOOP
	    INSERT INTO energy_consumption (id, power_used, cost, price, capture_at, fixture_id, manual_saving, ambient_saving, tuneup_saving, occ_saving, zero_bucket) VALUES (nextval('energy_consumption_seq'), 0, 0, 0, curr_time, rec.fixt_id, 0, 0, 0, 0, 1);	    
	    curr_time = curr_time + interval '5 min';		
	  END LOOP;	  
	END LOOP;	
END;
$$
LANGUAGE plpgsql;
      
CREATE INDEX profile_config ON weekday USING btree (profile_configuration_id);
        
CREATE OR REPLACE FUNCTION getOnLightLevelForFixture(fixture_id bigint, dayOfWeek integer, currentMinutes integer) RETURNS integer
    AS $$
DECLARE 
    level integer;
    day_type varchar;
    profile_id bigint;
    quadrant_type varchar;
BEGIN	
	level := 0;
	--- Let's calculate the type of day
	
	profile_id := getProfileByFixtureGroupId($1, $2, 0, $3);
	      
	level := on_level from profile where id = profile_id;
    RETURN level;    
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getMinLightLevelForFixture(fixture_id bigint, dayOfWeek integer, currentMinutes integer) RETURNS integer
    AS $$
DECLARE 
    level integer;
    day_type varchar;
    profile_id bigint;
    quadrant_type varchar;
BEGIN	
	level := 0;
	--- Let's calculate the type of day
	
	profile_id := getProfileByFixtureGroupId($1, $2, 0, $3);
	      
	level := min_level from profile where id = profile_id;
    RETURN level;    
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getProfileModeForPlugload(plugload_id bigint, dayOfWeek integer, currentMinutes integer) RETURNS integer
    AS $$
DECLARE 
    level integer;
    day_type varchar;
    profile_id bigint;
    quadrant_type varchar;
BEGIN	
	level := 0;
	--- Let's calculate the type of day
	
	profile_id := getProfileByPlugloadGroupId($1, $2, 0, $3);
	      
	level := mode from plugload_profile where id = profile_id;
    RETURN level;    
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getDayQuadrantForGivenTime(config_time varchar) RETURNS integer
    AS $$
DECLARE 
    minutes integer;
    temp_minutes integer;
    from_date timestamp;
    to_date timestamp;
    ref_date varchar;
BEGIN	
	ref_date := '01 01 2000 ';
	from_date := to_timestamp('01 01 2000 ' ||  config_time, 'DD MM YYYY HH12:MI AM');
	to_date := to_timestamp('01 01 2000 ' || '12:00 AM', 'DD MM YYYY HH12:MI AM');
    minutes := to_char(from_date - to_date, 'HH24') ;
    minutes := minutes * 60;
    temp_minutes := to_char(from_date - to_date, 'MI') ;
    minutes := minutes + temp_minutes;
    RETURN minutes;   
END;
$$ LANGUAGE plpgsql;

DROP TYPE fixture_hour_record;

CREATE TYPE fixture_hour_record AS (
	fixture_id integer,
	agg_power numeric,
	agg_cost numeric,
	min_temp smallint,
	max_temp smallint,
	avg_temp numeric,
	base_power numeric,
	base_cost numeric,
	saved_power numeric,
	saved_cost numeric,
	occ_saving numeric,
	amb_saving numeric,
	tune_saving numeric,
	manual_saving numeric,
	no_of_rec int,
	peak_load numeric,
	min_load numeric,
	min_price numeric,
	max_price numeric,
	avg_load numeric
);

---Added missing element in update sql
ALTER TABLE energy_consumption_hourly add avg_load numeric(19,2);

CREATE OR REPLACE FUNCTION aggregatehourlyenergyconsumption(todate timestamp with time zone) RETURNS void
    AS $$
DECLARE 
	rec fixture_hour_record;	
	system_rec system_ec_record;
	min_load1 numeric;
	peak_load1 numeric;
BEGIN
	FOR rec IN (
	SELECT f.id as fixt_id, agg_power, agg_cost, min_temp, max_temp, avg_temp, base_power, base_cost, saved_power, saved_cost, occ_saving, amb_saving, tune_saving, manual_saving, no_of_rec, peak_load, min_load, min_price, max_price, avg_load FROM fixture as f left outer join (
	SELECT fixture_id, SUM(power_used)/12 AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(avg_temperature) AS avg_temp, SUM(base_power_used)/12 AS base_power, sum(base_cost) AS base_cost, SUM(saved_power_used)/12 AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_saving)/12 AS occ_saving, SUM(ambient_saving)/12 AS amb_saving, SUM(tuneup_saving)/12 AS tune_saving, SUM(manual_saving)/12 AS manual_saving, count(*) AS no_of_rec, max(power_used) AS peak_load, min(power_used) AS min_load, min(price) AS min_price, max(price) AS max_price, avg(power_used) AS avg_load
	FROM energy_consumption as ec 
	WHERE capture_at <= toDate and capture_at > toDate - interval '1 hour' and base_power_used != 0 and zero_bucket != 1 GROUP BY fixture_id) as sub_query on (sub_query.fixture_id = f.id))
	LOOP  
	  IF rec.no_of_rec IS NULL THEN
	    INSERT INTO energy_consumption_hourly (id, fixture_id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, peak_load, min_load, min_price, max_price, avg_load) VALUES (nextval('energy_consumption_hourly_seq'), rec.fixture_id, 0, 0, 0, toDate, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	  ELSE
		INSERT INTO energy_consumption_hourly (id, fixture_id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, peak_load, min_load, min_price, max_price, avg_load) VALUES (nextval('energy_consumption_hourly_seq'), rec.fixture_id, rec.agg_power, round(cast (rec.base_cost*12*1000/(rec.no_of_rec *rec.base_power) as numeric), 10), rec.agg_cost, toDate, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_load, rec.min_load, rec.min_price, rec.max_price, rec.avg_load);
	  END IF;
	END LOOP;

END;
$$
LANGUAGE plpgsql;

--
-- Name: loadGroupEnergyConsumptionBetweenPeriods(startTime timestamp with time zone, endTime timestamp with time zone); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE OR REPLACE FUNCTION loadGroupEnergyConsumptionBetweenPeriods(startTime timestamp with time zone, endTime timestamp with time zone) RETURNS SETOF groupecrecord
    AS $$
DECLARE
 rec groupecrecord;
BEGIN
	FOR rec IN (select '0' AS "i",
		'Custom' AS "name",
		(SELECT COALESCE(avg(ec.power_used), 0)) AS "powerUsed",
		(SELECT COALESCE(avg(ec.base_power_used),0)) AS "basePowerUsed", 
		(SELECT COALESCE(avg(ec.saved_power_used),0)) AS "savedPower",
		(SELECT COALESCE(avg(ec.saved_cost),0)) AS "savedCost", 
		(select count(id) from fixture where group_id=0 and state = 'COMMISSIONED') AS "totalFixtures"  
		from energy_consumption ec
		join fixture f on f.id = ec.fixture_id and f.state = 'COMMISSIONED' 
		where ec.fixture_id in (select id from fixture where group_id = 0 and state = 'COMMISSIONED') 
				and ec.zero_bucket != 1
				and ec.capture_at::time <= endTime::time and ec.capture_at::time >= startTime::time
				and ec.capture_at >= startTime - interval '240 hour')
	LOOP 	
			RETURN NEXT rec;  
	END LOOP;  
	FOR rec IN (select g.id AS "i",
		g.name AS "name",
		(SELECT COALESCE(avg(ec.power_used), 0)) AS "powerUsed",
		(SELECT COALESCE(avg(ec.base_power_used),0)) AS "basePowerUsed", 
		(SELECT COALESCE(avg(ec.saved_power_used),0)) AS "savedPower",
		(SELECT COALESCE(avg(ec.saved_cost),0)) AS "savedCost", 
		(select count(id) from fixture where group_id=g.id and state = 'COMMISSIONED') AS "totalFixtures"
		from groups g 
		join energy_consumption ec on ec.fixture_id in (select id from fixture where group_id=g.id and state = 'COMMISSIONED')
		join fixture f on f.id = ec.fixture_id and f.state = 'COMMISSIONED'
		where ec.zero_bucket != 1 and ec.capture_at::time <= endTime::time 
				and ec.capture_at::time >= startTime::time and ec.capture_at >= startTime - interval '240 hour'
		group by g.id, g.name order by g.id)
	LOOP 	
			RETURN NEXT rec;  
	END LOOP;  
END;
$$
    LANGUAGE plpgsql;

    
CREATE OR REPLACE FUNCTION getProfileByFixtureGroupId(fixture_id bigint, dayOfWeek integer, group_id bigint,  currentMinutes integer) RETURNS integer
    AS $$
DECLARE 
    day_type varchar;
    profile_id bigint := 0;
    quadrant_type varchar;
    sortedQuad int[] = '{}';
    sortedType varchar[] = '{}';
    w int := 0;
    x int := 0;
    y int := 0;
    z int := 0;
BEGIN	
	
	IF $1 > 0 THEN
	
		--- Let's calculate the type of day
		day_type := wd.type from fixture f, groups g, profile_configuration pc, profile_handler pr, weekday wd 
		             where wd.profile_configuration_id = pc.id and 
		                   pc.id = pr.profile_configuration_id and 	                   
		                   pr.id = g.profile_handler_id and
		                   f.group_id = g.id and
		                   wd.short_order = $2 and f.id = $1;
		 
		 select into w,x,y,z
		 				getDayQuadrantForGivenTime(morning_time), getDayQuadrantForGivenTime(day_time), getDayQuadrantForGivenTime(evening_time), getDayQuadrantForGivenTime(night_time)
		 		from fixture f, groups g, profile_configuration pc, profile_handler pr where pc.id = pr.profile_configuration_id and 
		                             g.profile_handler_id = pr.id  and f.group_id = g.id and f.id = $1;
		                             
		 if w < x and w < y and w < z THEN
		 	sortedQuad[1] := w;
		 	sortedQuad[2] := x;
		 	sortedQuad[3] := y;
		 	sortedQuad[4] := z;
		 	sortedType[1] := 'morning';
		 	sortedType[2] := 'day';
		 	sortedType[3] := 'evening';
		 	sortedType[4] := 'night';
		 elseif x < w and x < y and x < z THEN
		 	sortedQuad[4] := w;
		 	sortedQuad[1] := x;
		 	sortedQuad[2] := y;
		 	sortedQuad[3] := z;
		 	sortedType[4] := 'morning';
		 	sortedType[1] := 'day';
		 	sortedType[2] := 'evening';
		 	sortedType[3] := 'night';
		 elseif y < w and y < x and y < z THEN
		 	sortedQuad[3] := w;
		 	sortedQuad[4] := x;
		 	sortedQuad[1] := y;
		 	sortedQuad[2] := z;
		 	sortedType[3] := 'morning';
		 	sortedType[4] := 'day';
		 	sortedType[1] := 'evening';
		 	sortedType[2] := 'night';
		 else
		 	sortedQuad[2] := w;
		 	sortedQuad[3] := x;
		 	sortedQuad[4] := y;
		 	sortedQuad[1] := z;
		 	sortedType[2] := 'morning';
		 	sortedType[3] := 'day';
		 	sortedType[4] := 'evening';
		 	sortedType[1] := 'night';
		end if;
		
		if currentMinutes < sortedQuad[1] then
			quadrant_type := sortedType[4];
		elseif currentMinutes < sortedQuad[2] then
			quadrant_type := sortedType[1];
		elseif currentMinutes < sortedQuad[3] then
			quadrant_type := sortedType[2];
		else
			quadrant_type := sortedType[3];
		end if;
	
		 
		IF day_type = 'weekday'
		   THEN 
		    IF quadrant_type = 'morning' 
		       THEN  profile_id := morning_profile_id from  fixture f, groups g, profile_handler pr where g.profile_handler_id = pr.id and f.group_id = g.id and f.id = $1;
		    ELSEIF quadrant_type = 'day' 
		       THEN  profile_id := day_profile_id from  fixture f, groups g, profile_handler pr where g.profile_handler_id = pr.id and f.group_id = g.id and f.id = $1;
		    ELSEIF quadrant_type = 'evening' 
		       THEN  profile_id := evening_profile_id from  fixture f, groups g, profile_handler pr where g.profile_handler_id = pr.id and f.group_id = g.id and f.id = $1;
		    ELSE
		        profile_id := night_profile_id from  fixture f, groups g, profile_handler pr where g.profile_handler_id = pr.id and f.group_id = g.id and f.id = $1;
		    END IF;
		ELSE
		     IF quadrant_type = 'morning' 
		       THEN  profile_id := morning_profile_weekend from  fixture f, groups g, profile_handler pr where g.profile_handler_id = pr.id and f.group_id = g.id and f.id = $1;
		    ELSEIF quadrant_type = 'day' 
		       THEN  profile_id := day_profile_weekend from  fixture f, groups g, profile_handler pr where g.profile_handler_id = pr.id and f.group_id = g.id and f.id = $1;
		    ELSEIF quadrant_type = 'evening' 
		       THEN  profile_id :=  evening_profile_weekend from  fixture f, groups g, profile_handler pr where g.profile_handler_id = pr.id and f.group_id = g.id and f.id = $1;
		    ELSE
		        profile_id := night_profile_weekend from  fixture f, groups g, profile_handler pr where g.profile_handler_id = pr.id and f.group_id = g.id and f.id = $1;
		    END IF;
		END IF;
		
	ELSEIF $3 > 0 THEN
		--- Let's calculate the type of day
		day_type := wd.type from groups g, profile_configuration pc, profile_handler pr, weekday wd 
		             where wd.profile_configuration_id = pc.id and 
		                   pc.id = pr.profile_configuration_id and 	                   
		                   pr.id = g.profile_handler_id and
		                   wd.short_order = $2 and g.id = $3;
		 
		 select into w,x,y,z
		 				getDayQuadrantForGivenTime(morning_time), getDayQuadrantForGivenTime(day_time), getDayQuadrantForGivenTime(evening_time), getDayQuadrantForGivenTime(night_time)
		 		from groups g, profile_configuration pc, profile_handler pr where pc.id = pr.profile_configuration_id and 
		                             g.profile_handler_id = pr.id  and g.id = $3;
		                             
		 if w < x and w < y and w < z THEN
		 	sortedQuad[1] := w;
		 	sortedQuad[2] := x;
		 	sortedQuad[3] := y;
		 	sortedQuad[4] := z;
		 	sortedType[1] := 'morning';
		 	sortedType[2] := 'day';
		 	sortedType[3] := 'evening';
		 	sortedType[4] := 'night';
		 elseif x < w and x < y and x < z THEN
		 	sortedQuad[4] := w;
		 	sortedQuad[1] := x;
		 	sortedQuad[2] := y;
		 	sortedQuad[3] := z;
		 	sortedType[4] := 'morning';
		 	sortedType[1] := 'day';
		 	sortedType[2] := 'evening';
		 	sortedType[3] := 'night';
		 elseif y < w and y < x and y < z THEN
		 	sortedQuad[3] := w;
		 	sortedQuad[4] := x;
		 	sortedQuad[1] := y;
		 	sortedQuad[2] := z;
		 	sortedType[3] := 'morning';
		 	sortedType[4] := 'day';
		 	sortedType[1] := 'evening';
		 	sortedType[2] := 'night';
		 else
		 	sortedQuad[2] := w;
		 	sortedQuad[3] := x;
		 	sortedQuad[4] := y;
		 	sortedQuad[1] := z;
		 	sortedType[2] := 'morning';
		 	sortedType[3] := 'day';
		 	sortedType[4] := 'evening';
		 	sortedType[1] := 'night';
		end if;
		
		if currentMinutes < sortedQuad[1] then
			quadrant_type := sortedType[4];
		elseif currentMinutes < sortedQuad[2] then
			quadrant_type := sortedType[1];
		elseif currentMinutes < sortedQuad[3] then
			quadrant_type := sortedType[2];
		else
			quadrant_type := sortedType[3];
		end if;
	
		 
		IF day_type = 'weekday'
		   THEN 
		    IF quadrant_type = 'morning' 
		       THEN  profile_id := morning_profile_id from  groups g, profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    ELSEIF quadrant_type = 'day' 
		       THEN  profile_id := day_profile_id from  groups g, profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    ELSEIF quadrant_type = 'evening' 
		       THEN  profile_id := evening_profile_id from  groups g, profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    ELSE
		        profile_id := night_profile_id from  groups g, profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    END IF;
		ELSE
		     IF quadrant_type = 'morning' 
		       THEN  profile_id := morning_profile_weekend from  groups g, profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    ELSEIF quadrant_type = 'day' 
		       THEN  profile_id := day_profile_weekend from  groups g, profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    ELSEIF quadrant_type = 'evening' 
		       THEN  profile_id :=  evening_profile_weekend from  groups g, profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    ELSE
		        profile_id := night_profile_weekend from  groups g, profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    END IF;
		END IF;
	END IF;
	      
    RETURN profile_id;    
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION getProfileByPlugloadGroupId(plugload_id bigint, dayOfWeek integer, group_id bigint,  currentMinutes integer) RETURNS integer
    AS $$
DECLARE 
    day_type varchar;
    profile_id bigint := 0;
    quadrant_type varchar;
    sortedQuad int[] = '{}';
    sortedType varchar[] = '{}';
    w int := 0;
    x int := 0;
    y int := 0;
    z int := 0;
BEGIN	
	
	IF $1 > 0 THEN
	
		--- Let's calculate the type of day
		day_type := wd.type from plugload p, plugload_groups g, plugload_profile_configuration pc, plugload_profile_handler pr, weekday_plugload wd 
		             where wd.plugload_profile_configuration_id = pc.id and 
		                   pc.id = pr.profile_configuration_id and 	                   
		                   pr.id = g.profile_handler_id and
		                   p.group_id = g.id and
		                   wd.short_order = $2 and p.id = $1;
		 
		 select into w,x,y,z
		 				getDayQuadrantForGivenTime(morning_time), getDayQuadrantForGivenTime(day_time), getDayQuadrantForGivenTime(evening_time), getDayQuadrantForGivenTime(night_time)
		 		from plugload p, plugload_groups g, plugload_profile_configuration pc, plugload_profile_handler pr where pc.id = pr.profile_configuration_id and 
		                             g.profile_handler_id = pr.id  and p.group_id = g.id and p.id = $1;
		                             
		 if w < x and w < y and w < z THEN
		 	sortedQuad[1] := w;
		 	sortedQuad[2] := x;
		 	sortedQuad[3] := y;
		 	sortedQuad[4] := z;
		 	sortedType[1] := 'morning';
		 	sortedType[2] := 'day';
		 	sortedType[3] := 'evening';
		 	sortedType[4] := 'night';
		 elseif x < w and x < y and x < z THEN
		 	sortedQuad[4] := w;
		 	sortedQuad[1] := x;
		 	sortedQuad[2] := y;
		 	sortedQuad[3] := z;
		 	sortedType[4] := 'morning';
		 	sortedType[1] := 'day';
		 	sortedType[2] := 'evening';
		 	sortedType[3] := 'night';
		 elseif y < w and y < x and y < z THEN
		 	sortedQuad[3] := w;
		 	sortedQuad[4] := x;
		 	sortedQuad[1] := y;
		 	sortedQuad[2] := z;
		 	sortedType[3] := 'morning';
		 	sortedType[4] := 'day';
		 	sortedType[1] := 'evening';
		 	sortedType[2] := 'night';
		 else
		 	sortedQuad[2] := w;
		 	sortedQuad[3] := x;
		 	sortedQuad[4] := y;
		 	sortedQuad[1] := z;
		 	sortedType[2] := 'morning';
		 	sortedType[3] := 'day';
		 	sortedType[4] := 'evening';
		 	sortedType[1] := 'night';
		end if;
		
		if currentMinutes < sortedQuad[1] then
			quadrant_type := sortedType[4];
		elseif currentMinutes < sortedQuad[2] then
			quadrant_type := sortedType[1];
		elseif currentMinutes < sortedQuad[3] then
			quadrant_type := sortedType[2];
		else
			quadrant_type := sortedType[3];
		end if;
	
		 
		IF day_type = 'weekday'
		   THEN 
		    IF quadrant_type = 'morning' 
		       THEN  profile_id := morning_profile_id from  plugload p, plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and p.group_id = g.id and p.id = $1;
		    ELSEIF quadrant_type = 'day' 
		       THEN  profile_id := day_profile_id from  plugload p, plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and p.group_id = g.id and p.id = $1;
		    ELSEIF quadrant_type = 'evening' 
		       THEN  profile_id := evening_profile_id from  plugload p, plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and p.group_id = g.id and p.id = $1;
		    ELSE
		        profile_id := night_profile_id from  plugload p, plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and p.group_id = g.id and p.id = $1;
		    END IF;
		ELSE
		     IF quadrant_type = 'morning' 
		       THEN  profile_id := morning_profile_weekend from  plugload p, plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and p.group_id = g.id and p.id = $1;
		    ELSEIF quadrant_type = 'day' 
		       THEN  profile_id := day_profile_weekend from  plugload p, plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and p.group_id = g.id and p.id = $1;
		    ELSEIF quadrant_type = 'evening' 
		       THEN  profile_id :=  evening_profile_weekend from  plugload p, plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and p.group_id = g.id and p.id = $1;
		    ELSE
		        profile_id := night_profile_weekend from  plugload p, plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and p.group_id = g.id and p.id = $1;
		    END IF;
		END IF;
		
	ELSEIF $3 > 0 THEN
		--- Let's calculate the type of day
		day_type := wd.type from plugload_groups g, plugload_profile_configuration pc, plugload_profile_handler pr, weekday_plugload wd 
		             where wd.plugload_profile_configuration_id = pc.id and 
		                   pc.id = pr.profile_configuration_id and 	                   
		                   pr.id = g.profile_handler_id and
		                   wd.short_order = $2 and g.id = $3;
		 
		 select into w,x,y,z
		 				getDayQuadrantForGivenTime(morning_time), getDayQuadrantForGivenTime(day_time), getDayQuadrantForGivenTime(evening_time), getDayQuadrantForGivenTime(night_time)
		 		from plugload_groups g, plugload_profile_configuration pc, plugload_profile_handler pr where pc.id = pr.profile_configuration_id and 
		                             g.profile_handler_id = pr.id  and g.id = $3;
		                             
		 if w < x and w < y and w < z THEN
		 	sortedQuad[1] := w;
		 	sortedQuad[2] := x;
		 	sortedQuad[3] := y;
		 	sortedQuad[4] := z;
		 	sortedType[1] := 'morning';
		 	sortedType[2] := 'day';
		 	sortedType[3] := 'evening';
		 	sortedType[4] := 'night';
		 elseif x < w and x < y and x < z THEN
		 	sortedQuad[4] := w;
		 	sortedQuad[1] := x;
		 	sortedQuad[2] := y;
		 	sortedQuad[3] := z;
		 	sortedType[4] := 'morning';
		 	sortedType[1] := 'day';
		 	sortedType[2] := 'evening';
		 	sortedType[3] := 'night';
		 elseif y < w and y < x and y < z THEN
		 	sortedQuad[3] := w;
		 	sortedQuad[4] := x;
		 	sortedQuad[1] := y;
		 	sortedQuad[2] := z;
		 	sortedType[3] := 'morning';
		 	sortedType[4] := 'day';
		 	sortedType[1] := 'evening';
		 	sortedType[2] := 'night';
		 else
		 	sortedQuad[2] := w;
		 	sortedQuad[3] := x;
		 	sortedQuad[4] := y;
		 	sortedQuad[1] := z;
		 	sortedType[2] := 'morning';
		 	sortedType[3] := 'day';
		 	sortedType[4] := 'evening';
		 	sortedType[1] := 'night';
		end if;
		
		if currentMinutes < sortedQuad[1] then
			quadrant_type := sortedType[4];
		elseif currentMinutes < sortedQuad[2] then
			quadrant_type := sortedType[1];
		elseif currentMinutes < sortedQuad[3] then
			quadrant_type := sortedType[2];
		else
			quadrant_type := sortedType[3];
		end if;
	
		 
		IF day_type = 'weekday'
		   THEN 
		    IF quadrant_type = 'morning' 
		       THEN  profile_id := morning_profile_id from  plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    ELSEIF quadrant_type = 'day' 
		       THEN  profile_id := day_profile_id from  plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    ELSEIF quadrant_type = 'evening' 
		       THEN  profile_id := evening_profile_id from  plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    ELSE
		        profile_id := night_profile_id from  plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    END IF;
		ELSE
		     IF quadrant_type = 'morning' 
		       THEN  profile_id := morning_profile_weekend from  plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    ELSEIF quadrant_type = 'day' 
		       THEN  profile_id := day_profile_weekend from  plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    ELSEIF quadrant_type = 'evening' 
		       THEN  profile_id :=  evening_profile_weekend from  plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    ELSE
		        profile_id := night_profile_weekend from  plugload_groups g, plugload_profile_handler pr where g.profile_handler_id = pr.id and g.id = $3;
		    END IF;
		END IF;
	END IF;
	      
    RETURN profile_id;    
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION updateZeroBuckets(fixtureId integer, lastStatsRcvdDate timestamp with time zone, startDate timestamp with time zone, latestStatsDate timestamp with time zone, newCU integer, sweepEnabled integer) RETURNS void
    AS $$
DECLARE 
	baseAvgPowerUsed numeric;
	latestCum numeric;
	calib numeric;
	lastCum numeric;
	noOfMissing numeric;
	avgTicks numeric;
	avgPowerUsed numeric;
	rec_hourly fixture_hour_record;
	rec_daily fixture_daily_record;
	hour_time missing_time_record;
	day_time missing_time_record;
BEGIN

	SELECT avg(base_power_used) INTO baseAvgPowerUsed
     	FROM energy_consumption
     WHERE (capture_at = latestStatsDate or capture_at = lastStatsRcvdDate) and fixture_id = fixtureId and zero_bucket = 0;

	SELECT energy_cum INTO latestCum
	FROM energy_consumption
	WHERE capture_at = latestStatsDate and fixture_id = fixtureId;
	
	SELECT energy_calib INTO calib
	FROM energy_consumption
	WHERE capture_at = latestStatsDate and fixture_id = fixtureId;
		
	SELECT energy_cum INTO lastCum
	FROM energy_consumption
	WHERE capture_at = lastStatsRcvdDate and fixture_id = fixtureId and zero_bucket = 0;

	if(lastCum is NULL) 
	THEN
	  RETURN;
	END IF;

	-- This is to ignore if the current cumulative value is less than the last cumulative value
	IF(latestCum < lastCum)
	THEN
	  RETURN;
	END IF;

	--This is to ignore if the current cumulative value is big number and last cumulative value is small
	IF(latestCum >= 429490176000 AND lastCum < 429490176000)
	THEN
	  RETURN;
	END IF;

	SELECT count(*) INTO noOfMissing
	FROM energy_consumption
	WHERE capture_at >= startDate and capture_at < latestStatsDate and fixture_id = fixtureId;
	
	IF (newCU = 1)
	THEN
		avgTicks = ((latestCum - lastCum) * 12) /(noOfMissing + 1);
		--su sends cumulative value in watt/hour but em multiplies with 100 and stores in db. 
		--so divide by 100.
		avgPowerUsed = avgTicks / 100;
	ELSE
		avgTicks = (latestCum - lastCum)/(noOfMissing + 1);
		IF (avgTicks < 2)
		THEN
	  		avgPowerUsed = 0;
		ELSE
	  		avgPowerUsed = (calib * 3600 * (avgTicks - 1)) / 300 / 10000000;
		END IF;
	END IF;

	IF (avgPowerUsed > baseAvgPowerUsed)
	THEN
	  avgPowerUsed = baseAvgPowerUsed;
	END IF;

	IF (sweepEnabled = 1)
	THEN
	  baseAvgPowerUsed = avgPowerUsed;
	END IF;
	UPDATE energy_consumption set power_used = avgPowerUsed, cost = (avgPowerUsed * price)/(12 *1000), base_power_used = baseAvgPowerUsed, base_cost = (baseAvgPowerUsed * price)/(12 * 1000), zero_bucket = 2, energy_calib = calib where fixture_id = fixtureId and capture_at >= startDate and capture_at < latestStatsDate;
	
	UPDATE energy_consumption set saved_power_used = (base_power_used - power_used), saved_cost = (base_cost - cost), occ_saving = (base_power_used - power_used) where fixture_id = fixtureId and capture_at >= startDate and capture_at < latestStatsDate;

	--- hourly table
	FOR hour_time IN (
	SELECT capture_at capture_time FROM energy_consumption_hourly
	WHERE fixture_id = fixtureId and capture_at >= startDate and capture_at <= latestStatsDate)
	LOOP
	  FOR rec_hourly IN ( 
	  SELECT fixture_id, AVG(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(avg_temperature) AS avg_temp, AVG(base_power_used) AS base_power, sum(base_cost) AS base_cost, AVG(saved_power_used) AS saved_power, sum(saved_cost) AS  saved_cost, AVG(occ_saving) AS occ_saving, AVG(ambient_saving) AS amb_saving, AVG(tuneup_saving) AS tune_saving, AVG(manual_saving) AS manual_saving, count(*) AS no_of_rec, max(power_used) AS peak_load, min(power_used) AS min_load, min(price) AS min_price, max(price) AS max_price
	  FROM energy_consumption 
	  WHERE capture_at <= hour_time.capture_time and capture_at > hour_time.capture_time - interval '1 hour' and zero_bucket != 1 and fixture_id = fixtureId GROUP BY fixture_id)
	  LOOP  
		IF (rec_hourly.base_power > 0)
		THEN
			UPDATE energy_consumption_hourly set power_used = rec_hourly.agg_power, price = round(cast (rec_hourly.base_cost*12*1000/(rec_hourly.no_of_rec *rec_hourly.base_power) as numeric), 10), cost = rec_hourly.agg_cost, base_power_used = rec_hourly.base_power, base_cost = rec_hourly.base_cost, saved_power_used = rec_hourly.saved_power, saved_cost = rec_hourly.saved_cost, peak_load = rec_hourly.peak_load, min_load = rec_hourly.min_load, min_price = rec_hourly.min_price, max_price = rec_hourly.max_price, occ_saving = rec_hourly.occ_saving, ambient_saving = rec_hourly.amb_saving, manual_saving = rec_hourly.manual_saving, tuneup_saving = rec_hourly.tune_saving WHERE fixture_id = fixtureId and capture_at = hour_time.capture_time;
		END IF;
	  END LOOP;
	END LOOP;

	--- daily table
	FOR day_time IN (
	SELECT capture_at capture_time FROM energy_consumption_daily
	WHERE fixture_id = fixtureId and capture_at >= startDate and capture_at <= latestStatsDate)
	LOOP
	  FOR rec_daily IN ( 
	  SELECT fixture_id, SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(avg_temperature) AS avg_temp, sum(base_power_used) AS base_power, sum(base_cost) AS base_cost, sum(saved_power_used) AS saved_power, sum(saved_cost) AS saved_cost, sum(occ_saving) AS occ_saving, sum(ambient_saving) AS amb_saving, sum(tuneup_saving) AS tune_saving, sum(manual_saving) AS manual_saving, count(*) AS no_of_rec, max(peak_load) AS peak_load, min(min_load) AS min_load, min(min_price) AS min_price, max(max_price) AS max_price
	FROM energy_consumption_hourly 
	WHERE capture_at <= day_time.capture_time and capture_at > day_time.capture_time - interval '1 day' and fixture_id = fixtureId GROUP BY fixture_id)
	  LOOP  
		IF (rec_daily.base_power > 0) 
		THEN
			UPDATE energy_consumption_daily set power_used = rec_daily.agg_power, cost = rec_daily.agg_cost, price = round(cast (rec_daily.base_cost*1000/rec_daily.base_power as numeric), 10), base_power_used = rec_daily.base_power, base_cost = rec_daily.base_cost, saved_power_used = rec_daily.saved_power, saved_cost = rec_daily.saved_cost, peak_load = rec_daily.peak_load, min_load = rec_daily.min_load, min_price = rec_daily.min_price, max_price = rec_daily.max_price, occ_saving = rec_daily.occ_saving, ambient_saving = rec_daily.amb_saving, tuneup_saving = rec_daily.tune_saving, manual_saving = rec_daily.manual_saving WHERE fixture_id = fixtureId and capture_at = day_time.capture_time;
		END IF;
	   END LOOP;
	END LOOP;

END;
$$
LANGUAGE plpgsql;

ALTER FUNCTION public.updateZeroBuckets(fixtureId integer, lastStatsRcvdDate timestamp with time zone, startDate timestamp with time zone, latestStatsDate timestamp with time zone, newCU integer, sweepEnabled integer) OWNER TO postgres;

CREATE TABLE ems_user_audit (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    username character varying NOT null,
    action_type character varying NOT NULL,   
	log_time timestamp without time zone not null,
    description character varying
);

ALTER TABLE ONLY ems_user_audit
    ADD CONSTRAINT ems_user_audit_pk PRIMARY KEY (id);
 ALTER TABLE ems_user_audit ALTER COLUMN user_id drop Not NULL;

ALTER TABLE public.ems_user_audit OWNER TO postgres;

CREATE SEQUENCE ems_user_audit_seq
    INCREMENT BY 1
    MAXVALUE 999999999999999999
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.ems_user_audit_seq OWNER TO postgres;

CREATE INDEX ems_user_audit_log_time_index ON ems_user_audit USING btree (log_time);
CREATE INDEX ems_user_audit_action_type_index ON ems_user_audit USING btree (action_type);
CREATE INDEX ems_user_audit_user_index ON ems_user_audit USING btree (user_id);

INSERT INTO ballasts (id, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (158, 'CREE CR22', '120-277', 'LED', 1, 1.00, 35, 'CREE');
INSERT INTO ballasts (id, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (159, 'CREE CR24', '120-277', 'LED', 1, 1.00, 44, 'CREE');

	--- Ldap Server Settings
CREATE SEQUENCE ldap_settings_seq
    INCREMENT BY 1
    MAXVALUE 999999999999999999
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.ldap_settings_seq OWNER TO postgres;


CREATE TABLE ldap_settings
(
  id bigint NOT NULL,
  "name" character varying(40) NOT NULL,
  server character varying NOT NULL,
  port integer NOT NULL DEFAULT 389,
  tls boolean DEFAULT false,
  password_encryp_type character varying(25),
  base_dns character varying(250) NOT NULL,
  user_attribute character varying,
  allow_anonymous boolean DEFAULT false,
  non_anonymous_dn character varying(250),
  non_anonymous_password character varying(20) NOT NULL,
  CONSTRAINT pk PRIMARY KEY (id),
  CONSTRAINT uniqueipaddress UNIQUE (server)
);

ALTER TABLE public.ldap_settings OWNER TO postgres;

CREATE OR REPLACE FUNCTION hex_to_int(hexval varchar) RETURNS integer AS $$
DECLARE
   result  int;
BEGIN
 EXECUTE 'SELECT x''' || hexval || '''::int' INTO result;  RETURN result;
END;
$$ 
LANGUAGE plpgsql; 

--Sree 04/11
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "energy", "life_ins_start", "life_prog_start", "color_temp", "length", "diameter", "design_lumens", "cri") VALUES (285, 'GE', 'F96T8/XL/SPX41',  'T8', 59, 24000, 24000, 4100, 96, 1, 5950, 86);

INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "energy", "life_ins_start", "life_prog_start", "color_temp", "length", "diameter", "initial_lumens", "design_lumens", "cri") VALUES (286, 'Sylvania', 'FO96/841/XP/ECO',  'T8', 59, 18000, 18000, 4100, 96, 1, 6200, 5890, 82);

--Sharad 04/13
ALTER TABLE users ADD COLUMN term_condition_accepted boolean DEFAULT false;

ALTER TABLE ems_user_audit ADD COLUMN ip_address character varying NOT null DEFAULT '';
CREATE INDEX ems_user_audit_ip_index ON ems_user_audit USING btree (ip_address);

ALTER TABLE gems_groups ADD COLUMN floor_id bigint;
ALTER TABLE gems_groups ADD CONSTRAINT fk_gems_groups_floor FOREIGN KEY (floor_id) REFERENCES floor (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

update gems_groups set floor_id = (select distinct floor_id from fixture f, gems_group_fixture gf 
where gems_groups.id = gf.group_id and gf.fixture_id = f.id and 
(select count(distinct fixt.floor_id) as floor_count from gems_group_fixture ggf, fixture fixt 
where gf.group_id = ggf.group_id and ggf.fixture_id = fixt.id) = 1) where floor_id is null;

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'gems.version.build', '0');

ALTER TABLE switch ADD COLUMN area_id bigint;

update system_configuration set value = '20' where name =  'cmd.pmstats_process_batch_size';
update system_configuration set value = '1' where name =  'cmd.ack_dbupdate_threads';
update system_configuration set value = '1' where name =  'cmd.response_listener_threads';

--Added by Sreedhar 04/22
--
-- Name: em_stats; Type: TABLE; Owner: postgres; 
--

CREATE TABLE em_stats
( 
  id bigint NOT NULL,
  capture_at timestamp without time zone,
  active_thread_count integer,
  gc_count bigint,
  gc_time bigint,
  heap_used numeric(19,2),
  non_heap_used numeric(19,2),
  sys_load numeric(19,2),
  cpu_percentage numeric(19,2),
  CONSTRAINT em_stats_pkey PRIMARY KEY(id)
);

ALTER TABLE em_stats OWNER TO postgres;

--
-- Name: em_stats_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE em_stats_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE em_stats_seq OWNER TO postgres;

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'pmstats_process_batch_time', '2000');

ALTER TABLE ldap_settings ALTER COLUMN non_anonymous_password drop Not NULL;

--Sreedhar 05/23
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'stats.temp_offset_2', '8');


--
-- Name: sweep_timer_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE sweep_timer_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE sweep_timer_seq OWNER TO postgres;

--
-- Name: sweep_timer; Type: TABLE; Owner: postgres;
--

CREATE TABLE sweep_timer
(
  id bigint NOT NULL,
  name character(128),
  CONSTRAINT sweep_timer_pkey PRIMARY KEY (id)
);

ALTER TABLE sweep_timer OWNER TO postgres;

--
-- Name: sweep_timer_details_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE sweep_timer_details_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE sweep_timer_details_seq OWNER TO postgres;

--
-- Name: sweep_timer_details; Type: TABLE; Owner: postgres;
--

CREATE TABLE sweep_timer_details
(
  id bigint NOT NULL,
  sweep_timer_id bigint NOT NULL,
  day_of_week character varying(20),
  short_order integer,
  override_timer integer,
  start_time_1 character varying,
  end_time_1 character varying,
  start_time_2 character varying,
  end_time_2 character varying,
  CONSTRAINT sweep_timer_details_pkey PRIMARY KEY (id),
  CONSTRAINT sweep_timer_id_fk FOREIGN KEY (sweep_timer_id)
      REFERENCES sweep_timer (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE sweep_timer_details ADD COLUMN start_time_3 character varying; 
ALTER TABLE sweep_timer_details ADD COLUMN end_time_3 character varying; 

ALTER TABLE sweep_timer_details OWNER TO postgres;

ALTER TABLE company ADD COLUMN sweep_timer_id bigint; 
ALTER TABLE company ADD CONSTRAINT fk_company_to_sweep_timer FOREIGN KEY (sweep_timer_id) REFERENCES sweep_timer (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE campus ADD COLUMN sweep_timer_id bigint;
ALTER TABLE campus ADD CONSTRAINT fk_campus_to_sweep_timer FOREIGN KEY (sweep_timer_id) REFERENCES sweep_timer (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE building ADD COLUMN sweep_timer_id bigint;
ALTER TABLE building ADD CONSTRAINT fk_building_to_sweep_timer FOREIGN KEY (sweep_timer_id) REFERENCES sweep_timer (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE floor ADD COLUMN sweep_timer_id bigint;
ALTER TABLE floor ADD CONSTRAINT fk_floor_to_sweep_timer FOREIGN KEY (sweep_timer_id) REFERENCES sweep_timer (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE area ADD COLUMN sweep_timer_id bigint;
ALTER TABLE area ADD CONSTRAINT fk_area_to_sweep_timer FOREIGN KEY (sweep_timer_id) REFERENCES sweep_timer (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

--Added by Lalit 3o-May-2012
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'sweeptimer.enable', 'false');

--Added by Yogesh 31-May-2012
DELETE FROM inventorydevice;
ALTER TABLE inventorydevice ADD CONSTRAINT unique_inventory_mac_addr UNIQUE (mac_address);

--Added by Sree 06/01
ALTER TABLE profile_handler ADD COLUMN is_high_bay smallint DEFAULT 0;
ALTER TABLE profile_handler ADD COLUMN motion_threshold_gain integer DEFAULT 0;

--Added by Dhanesh 11/21
ALTER TABLE profile_handler ADD COLUMN daylightharvesting smallint DEFAULT 0;


--Added by Dhanesh 18/08/15
ALTER TABLE profile_handler ADD COLUMN ble_mode  smallint DEFAULT 0;

--Added by Dhanesh 02/19/15
ALTER TABLE profile_handler ADD COLUMN dlh_allow_below_min smallint DEFAULT 0;
ALTER TABLE profile_handler ADD COLUMN dlh_force_profile_min smallint DEFAULT 0;

--Added by Sree 06/13
ALTER TABLE energy_consumption ADD COLUMN last_volts smallint;
ALTER TABLE energy_consumption ADD COLUMN saving_type smallint;

--Added by Sree 06/18
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.em_stats_table', '90');

--- Added by Shilpa 27-June-2012
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'dhcp.enable', 'true');

---Added by Sampath 02-July-2012

INSERT INTO system_configuration (id, name, value) 
values ((select coalesce(max(id),0)+1 from system_configuration),'db_pruning.ems_user_audit_table', '90');

INSERT INTO system_configuration (id, name, value) 
values ((select coalesce(max(id),0)+1 from system_configuration),'db_pruning.ems_user_audit_history_table', '1825');

CREATE TABLE ems_user_audit_history as select * from ems_user_audit where 1=0;

CREATE OR REPLACE FUNCTION prune_ems_user_audit() RETURNS void 
 AS $$
DECLARE
    no_days numeric;
    no_days_text text;
    no_days_time timestamp;
    history_no_days numeric;
    history_no_days_text text;
    history_no_days_time timestamp; 
    tm timestamp = now();
    
BEGIN
 
    SELECT value INTO no_days
    FROM system_configuration
    WHERE name = 'db_pruning.ems_user_audit_table';
	
	no_days_text = no_days || ' day';
    no_days_time = tm - no_days_text::interval;

    SELECT value INTO history_no_days
    FROM system_configuration
    WHERE name = 'db_pruning.ems_user_audit_history_table';

    history_no_days_text = history_no_days|| ' day';
    history_no_days_time = tm - history_no_days_text::interval;

    --Insert values into history table ems_user_audit_backup
    INSERT INTO ems_user_audit_history (select * from ems_user_audit WHERE log_time < no_days_time);

    --delete all the data in the main table leaving the last 'no_days' days
    DELETE FROM ems_user_audit WHERE log_time < no_days_time;

    --delete all the data in the history table leaving the last 'history_no_days' days
    DELETE FROM ems_user_audit_history WHERE log_time < history_no_days_time;


END;
$$ LANGUAGE plpgsql;

ALTER FUNCTION public.prune_ems_user_audit() OWNER TO postgres;

--Sreedhar 07/10
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'default_hopper_tx_power', '0');

--Yogesh 07/23
--

-- Name: em_motion_bits_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE em_motion_bits_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE em_motion_bits_seq OWNER TO postgres;

--
-- Name: em_motion_bits; Type: TABLE; Owner: postgres;
--

CREATE TABLE em_motion_bits
(
  id bigint NOT NULL,
  fixture_id bigint NOT NULL,
  capture_at timestamp NOT NULL,
  motion_bits character varying,
  motion_bit_level integer,
  CONSTRAINT em_motion_bits_pkey PRIMARY KEY (id),
  CONSTRAINT fixture_id_fk FOREIGN KEY (fixture_id)
      REFERENCES fixture (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
--
-- Profile Enhancement Data Model changes starts here
--

CREATE TABLE custom_fixture_profile_group
(
  id bigint NOT NULL,
  fixture_id bigint,
  profile_handler_id bigint,
  group_id bigint,
  CONSTRAINT customFixtGrp_pkey PRIMARY KEY (id)
);

ALTER TABLE custom_fixture_profile_group OWNER TO postgres;

CREATE SEQUENCE custom_fixture_profile_group_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE custom_fixture_profile_group_seq OWNER TO postgres;

CREATE TABLE profile_template
(
  id bigint NOT NULL,
  name character varying(255),
  CONSTRAINT profile_template_pkey PRIMARY KEY (id)
);

ALTER TABLE profile_template OWNER TO postgres;

ALTER TABLE profile_template ADD COLUMN display_template boolean DEFAULT true;
ALTER TABLE profile_template ADD COLUMN template_no bigint;

CREATE SEQUENCE profile_template_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE profile_template_seq OWNER TO postgres;

CREATE TYPE groupRecord AS (id bigint, profile_handler_id bigint, "name" character varying);

CREATE OR REPLACE FUNCTION updateExtendedProfileDataModel() RETURNS void AS
$$
DECLARE  
	isColumnExists boolean;
	grpRec groupRecord%rowtype;
	templateId numeric;
	profileNo numeric;
BEGIN
	isColumnExists = column_exists('profile_no','groups');
	 --Raise Notice '=> %', isColumnExists;
	  IF isColumnExists = 'f' THEN
		   --Raise Notice '=> %', isColumnExists;
		   ALTER TABLE groups ADD COLUMN profile_no smallint;
		   ALTER TABLE groups ADD COLUMN derived_from_group bigint;
		   ALTER TABLE groups ADD COLUMN tenant_id bigint;
		   ALTER TABLE groups ADD COLUMN template_id bigint;
		   ALTER TABLE groups ADD COLUMN display_profile boolean DEFAULT true;
		   ALTER TABLE groups ADD COLUMN default_profile boolean DEFAULT true;
		   ALTER TABLE groups ADD CONSTRAINT fk_gems_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenants (id);
		   ALTER TABLE groups ADD CONSTRAINT fk_gems_template_id FOREIGN KEY (template_id) REFERENCES profile_template (id);

		   -- Update profile_no column for all predefined groups with the id of groups
		   FOR grpRec IN select id, profile_handler_id, name from groups order by id LOOP
		   select ph.profile_group_id into profileNo from profile_handler ph where ph.id = grpRec.profile_handler_id;
		   --Raise Notice '=> %', grpRec.name;
			--IF (grpRec.id <= 16) THEN
				--Raise Notice 'IN GROUP LOOP=> %', grpRec.id;
				IF profileNo = 1 and grpRec.id!=1
				THEN
					UPDATE groups g set profile_no = grpRec.id where g.id = grpRec.id ;
				ELSE
					UPDATE groups g set profile_no = profileNo where g.id = grpRec.id ;
				END IF;
				select nextval('profile_template_seq') into templateId;
				INSERT INTO profile_template (id,name,template_no) values (templateId, grpRec.name,profileNo);
				UPDATE groups g set template_id = templateId where g.id = grpRec.id ;
			--END IF;
		   END LOOP;

		   --Added by Sharad 08/07 - To extent profile model to support enhanced features 
		   PERFORM updateGroupIdForFixtures();
	END IF;
END;
$$
LANGUAGE plpgsql;

CREATE TYPE custom_Profile_fixture_record AS (fixtureid bigint, pfhid bigint, fixturename character varying, currprofile character varying );

CREATE OR REPLACE FUNCTION updateGroupIdForFixtures() RETURNS void AS
$$
DECLARE 
	rec custom_Profile_fixture_record;	
	grpname character varying;
	grpId bigint;
BEGIN
	FOR rec IN (
	  SELECT id as fixtureid, profile_handler_id as pfhid, fixture_name as fixturename, current_profile as currprofile
	  FROM fixture as fxt WHERE group_id = 0 and state = 'COMMISSIONED' order by id)
	LOOP  	  
	 grpname = rec.fixturename || '_Custom';
	 --Raise Notice '=> %', rec.fixtureid;
	 INSERT INTO groups (id, name, company_id, profile_handler_id,profile_no) values (nextval('groups_seq'), grpname,1,rec.pfhid,0);
	 select id  INTO grpId from groups where profile_handler_id = rec.pfhid;
	 UPDATE fixture set group_id = grpId where id = rec.fixtureid ;
	 --Raise Notice '=> %,%', grpId,rec.pfhid ;
	 --UPDATE profile_handler set profile_group_id = 0 where id = rec.pfhid ;
	 -- This will add all fixtures which are in custom profiles into custom_fixture_profile_group
	 INSERT INTO custom_fixture_profile_group (id, fixture_id, profile_handler_id, group_id) values (nextval('custom_fixture_profile_group_seq'),rec.fixtureid,rec.pfhid,grpId);
	END LOOP;
	
	IF exists(select 1 from pg_constraint where conname = 'fkcdb9fa09393e967c')
	THEN
		ALTER TABLE fixture DROP CONSTRAINT fkcdb9fa09393e967c;
		ALTER TABLE fixture DROP COLUMN profile_handler_id;
	ELSE
		ALTER TABLE fixture DROP COLUMN profile_handler_id;
	END IF;
END;
$$
LANGUAGE plpgsql;

--Added by Sharad 08/07 - To extent profile model to support enhanced features 
select updateExtendedProfileDataModel();

--
-- Profile Enhancement Data Model changes ENDS
--
CREATE OR REPLACE FUNCTION getProfileNoForFixture(fixture_id bigint) RETURNS integer
    AS $$
DECLARE 
    profile_no bigint := 0;
BEGIN
	profile_no := g.profile_no from groups g, fixture f where f.group_id = g.id and f.id = $1;
	return profile_no;
END;
$$
LANGUAGE plpgsql;

ALTER TABLE gateway ADD COLUMN no_of_wds integer DEFAULT 0;
ALTER TABLE em_motion_bits ADD COLUMN  motion_bits_frequency integer;
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'motionbits.enable', 'false');

CREATE SEQUENCE motion_bits_scheduler_seq
   INCREMENT BY 1
    MAXVALUE 999999999999999999
    NO MINVALUE
    CACHE 1;

ALTER TABLE motion_bits_scheduler_seq OWNER TO postgres;
    
CREATE TABLE motion_bits_scheduler (
    id bigint NOT NULL,
    name					  character varying NOT NULL,
    group_id bigint NOT NULL,
    motion_bits_capture_start character varying,
    motion_bits_capture_end   character varying,
    transmit_frequency		 integer,
    bit_level				 smallint,
    days_of_week			 smallint,
    CONSTRAINT motion_bits_scheduler_group_id_fkey FOREIGN KEY (group_id)
      REFERENCES gems_groups (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE motion_bits_scheduler OWNER TO postgres;
ALTER TABLE motion_bits_scheduler ADD COLUMN  display_name character varying;
ALTER TABLE ONLY motion_bits_scheduler
    ADD CONSTRAINT motion_bits_scheduler_pkey PRIMARY KEY (id);
	
--For single Sign on from dashboard
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'dashboard_sso', 'false');

--Added by Sree o8/24/2012
--For GE ballasts
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 0, 25);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 0.5, 25);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 1, 25);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 1.5, 25);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 2, 33.9);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 2.5, 39.3);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 3, 46.4);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 3.5, 51.8);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 4, 57.1);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 4.5, 62.5);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 5, 67.9);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 5.5, 75);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 6, 78.6);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 6.5, 85.7);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 7, 85.7);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 7.5, 89.3);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 8, 91.1);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 8.5, 96.4);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 9, 100);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 9.5, 100);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 2, 10, 100);

UPDATE ballasts SET volt_power_map_id = 2 WHERE ballast_name LIKE 'GE%';

--
-- Profile Enhancement Data Model changes 
--
CREATE OR REPLACE FUNCTION getGroupIdFromProfileNoAndTenantId(profile_no bigint, tenant_id bigint) RETURNS integer
    AS $$
DECLARE 
    group_no bigint := 0;
BEGIN
	--Raise Notice 'Details % => %', $1, $2;
	IF ($2::bigint is NULL) THEN
		SELECT id INTO group_no FROM groups g WHERE g.profile_no = $1::smallint and g.tenant_id is null;
	ELSE
		SELECT id INTO group_no FROM groups g WHERE g.profile_no = $1::smallint and g.tenant_id = $2;
    END IF;
	return group_no;
END;
$$
LANGUAGE plpgsql;

-- drop tables that are no longer used
drop table qrtz_job_listeners;
drop table qrtz_trigger_listeners;
alter table qrtz_job_details drop column is_volatile;
alter table qrtz_triggers drop column is_volatile;
alter table qrtz_fired_triggers drop column is_volatile;

-- add new columns that replace the 'is_stateful' column

alter table qrtz_job_details add column is_nonconcurrent bool;
alter table qrtz_job_details add column is_update_data bool;
update qrtz_job_details set is_nonconcurrent = is_stateful;
update qrtz_job_details set is_update_data = is_stateful;
alter table qrtz_job_details drop column is_stateful;
alter table qrtz_fired_triggers add column is_nonconcurrent bool;
alter table qrtz_fired_triggers add column is_update_data bool;
update qrtz_fired_triggers set is_nonconcurrent = is_stateful;
update qrtz_fired_triggers set is_update_data = is_stateful;
alter table qrtz_fired_triggers drop column is_stateful;

-- add new 'sched_name' column to all tables
alter table qrtz_blob_triggers add column sched_name varchar(120) not null DEFAULT 'TestScheduler';
alter table qrtz_calendars add column sched_name varchar(120) not null DEFAULT 'TestScheduler';
alter table qrtz_cron_triggers add column sched_name varchar(120) not null DEFAULT 'TestScheduler';
alter table qrtz_fired_triggers add column sched_name varchar(120) not null DEFAULT 'TestScheduler';
alter table qrtz_job_details add column sched_name varchar(120) not null DEFAULT 'TestScheduler';
alter table qrtz_locks add column sched_name varchar(120) not null DEFAULT 'TestScheduler';
alter table qrtz_paused_trigger_grps add column sched_name varchar(120) not null DEFAULT 'TestScheduler';
alter table qrtz_scheduler_state add column sched_name varchar(120) not null DEFAULT 'TestScheduler';
alter table qrtz_simple_triggers add column sched_name varchar(120) not null DEFAULT 'TestScheduler';
alter table qrtz_triggers add column sched_name varchar(120) not null DEFAULT 'TestScheduler';

-- drop all primary and foreign key constraints, so that we can define new ones

alter table qrtz_triggers drop constraint qrtz_triggers_job_name_fkey;
alter table qrtz_blob_triggers drop constraint qrtz_blob_triggers_pkey;
alter table qrtz_blob_triggers drop constraint qrtz_blob_triggers_trigger_name_fkey;
alter table qrtz_simple_triggers drop constraint qrtz_simple_triggers_pkey;
alter table qrtz_simple_triggers drop constraint qrtz_simple_triggers_trigger_name_fkey;
alter table qrtz_cron_triggers drop constraint qrtz_cron_triggers_pkey;
alter table qrtz_cron_triggers drop constraint qrtz_cron_triggers_trigger_name_fkey;
alter table qrtz_job_details drop constraint qrtz_job_details_pkey;
alter table qrtz_job_details add primary key (sched_name, job_name, job_group);
alter table qrtz_triggers drop constraint qrtz_triggers_pkey;

-- add all primary and foreign key constraints, based on new columns

alter table qrtz_triggers add primary key (sched_name, trigger_name, trigger_group);
alter table qrtz_triggers add foreign key (sched_name, job_name, job_group) references qrtz_job_details(sched_name, job_name, job_group);
alter table qrtz_blob_triggers add primary key (sched_name, trigger_name, trigger_group);
alter table qrtz_blob_triggers add foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers(sched_name, trigger_name, trigger_group);
alter table qrtz_cron_triggers add primary key (sched_name, trigger_name, trigger_group);
alter table qrtz_cron_triggers add foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers(sched_name, trigger_name, trigger_group);
alter table qrtz_simple_triggers add primary key (sched_name, trigger_name, trigger_group);
alter table qrtz_simple_triggers add foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers(sched_name, trigger_name, trigger_group);
alter table qrtz_fired_triggers drop constraint qrtz_fired_triggers_pkey;
alter table qrtz_fired_triggers add primary key (sched_name, entry_id);
alter table qrtz_calendars drop constraint qrtz_calendars_pkey;
alter table qrtz_calendars add primary key (sched_name, calendar_name);
alter table qrtz_locks drop constraint qrtz_locks_pkey;
alter table qrtz_locks add primary key (sched_name, lock_name);
alter table qrtz_paused_trigger_grps drop constraint qrtz_paused_trigger_grps_pkey;
alter table qrtz_paused_trigger_grps add primary key (sched_name, trigger_group);
alter table qrtz_scheduler_state drop constraint qrtz_scheduler_state_pkey;
alter table qrtz_scheduler_state add primary key (sched_name, instance_name);

-- add new simprop_triggers table
CREATE TABLE qrtz_simprop_triggers
 (          
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 INT NULL,
    INT_PROP_2 INT NULL,
    LONG_PROP_1 BIGINT NULL,
    LONG_PROP_2 BIGINT NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 BOOL NULL,
    BOOL_PROP_2 BOOL NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

-- create indexes for faster queries

create index idx_qrtz_j_req_recovery on qrtz_job_details(SCHED_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_j_grp on qrtz_job_details(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_t_j on qrtz_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_t_jg on qrtz_triggers(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_t_c on qrtz_triggers(SCHED_NAME,CALENDAR_NAME);
create index idx_qrtz_t_g on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP);
create index idx_qrtz_t_state on qrtz_triggers(SCHED_NAME,TRIGGER_STATE);
create index idx_qrtz_t_n_state on qrtz_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_t_n_g_state on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_t_next_fire_time on qrtz_triggers(SCHED_NAME,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_st on qrtz_triggers(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_st_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
create index idx_qrtz_t_nft_st_misfire_grp on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_ft_trig_inst_name on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME);
create index idx_qrtz_ft_inst_job_req_rcvry on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_ft_j_g on qrtz_fired_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_ft_jg on qrtz_fired_triggers(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_ft_t_g on qrtz_fired_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
create index idx_qrtz_ft_tg on qrtz_fired_triggers(SCHED_NAME,TRIGGER_GROUP);

--Sreedhar 08/31
ALTER TABLE energy_consumption ADD COLUMN cu_status integer;

--Sreedhar 09/27
UPDATE ballast_volt_power SET power = 24.60 WHERE volt_power_map_id = 1 AND volt = 0;
UPDATE ballast_volt_power SET power = 24.60 WHERE volt_power_map_id = 1 AND volt = 0.5;
UPDATE ballast_volt_power SET power = 24.95 WHERE volt_power_map_id = 1 AND volt = 1;
UPDATE ballast_volt_power SET power = 26.67 WHERE volt_power_map_id = 1 AND volt = 1.5;
UPDATE ballast_volt_power SET power = 30.27 WHERE volt_power_map_id = 1 AND volt = 2;
UPDATE ballast_volt_power SET power = 35.67 WHERE volt_power_map_id = 1 AND volt = 2.5;
UPDATE ballast_volt_power SET power = 42.36 WHERE volt_power_map_id = 1 AND volt = 3;
UPDATE ballast_volt_power SET power = 48.74 WHERE volt_power_map_id = 1 AND volt = 3.5;
UPDATE ballast_volt_power SET power = 55.17 WHERE volt_power_map_id = 1 AND volt = 4;
UPDATE ballast_volt_power SET power = 61.69 WHERE volt_power_map_id = 1 AND volt = 4.5;
UPDATE ballast_volt_power SET power = 67.73 WHERE volt_power_map_id = 1 AND volt = 5;
UPDATE ballast_volt_power SET power = 72.93 WHERE volt_power_map_id = 1 AND volt = 5.5;
UPDATE ballast_volt_power SET power = 77.66 WHERE volt_power_map_id = 1 AND volt = 6;
UPDATE ballast_volt_power SET power = 81.70 WHERE volt_power_map_id = 1 AND volt = 6.5;
UPDATE ballast_volt_power SET power = 86.74 WHERE volt_power_map_id = 1 AND volt = 7;
UPDATE ballast_volt_power SET power = 90.47 WHERE volt_power_map_id = 1 AND volt = 7.5;
UPDATE ballast_volt_power SET power = 95.16 WHERE volt_power_map_id = 1 AND volt = 8;
UPDATE ballast_volt_power SET power = 97.08 WHERE volt_power_map_id = 1 AND volt = 8.5;
UPDATE ballast_volt_power SET power = 100.03 WHERE volt_power_map_id = 1 AND volt = 9;
UPDATE ballast_volt_power SET power = 100.25 WHERE volt_power_map_id = 1 AND volt = 9.5;
UPDATE ballast_volt_power SET power = 102.80 WHERE volt_power_map_id = 1 AND volt = 10;

--Kushal WDS Schema Changes and Switch Group Migration
ALTER TABLE switch DROP CONSTRAINT fk_gems_groups_id;
ALTER TABLE switch DROP CONSTRAINT fk_gems_gateway_id;
ALTER TABLE switch DROP COLUMN switch_type;
ALTER TABLE switch DROP COLUMN state;
ALTER TABLE switch DROP COLUMN gateway_id;
ALTER TABLE switch DROP COLUMN gems_group_id;
ALTER TABLE switch DROP COLUMN snap_address;

ALTER TABLE switch add COLUMN mode_type smallint DEFAULT 0;
ALTER TABLE switch add COLUMN initial_scene_active_time integer DEFAULT 60;
ALTER TABLE switch add COLUMN operation_mode smallint DEFAULT 0;
ALTER TABLE switch add COLUMN gems_groups_id bigint;

ALTER TABLE switch ADD CONSTRAINT fk_switch_gems_groups_id FOREIGN KEY (gems_groups_id) 
	REFERENCES gems_groups (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE scene add COLUMN scene_order integer;

ALTER TABLE scene ADD CONSTRAINT fk_switch_id FOREIGN KEY (switch_id) 
	REFERENCES switch (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;


ALTER TABLE lightlevels ADD CONSTRAINT fk_switch_id FOREIGN KEY (switch_id) 
	REFERENCES switch (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;


ALTER TABLE lightlevels ADD CONSTRAINT fk_scene_id FOREIGN KEY (scene_id) 
	REFERENCES scene (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE lightlevels ADD CONSTRAINT fk_fixture_id FOREIGN KEY (f_id) 
	REFERENCES fixture (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;


ALTER TABLE gems_group_fixture add COLUMN need_sync bigint;
ALTER TABLE gems_group_fixture add COLUMN user_action bigint;


CREATE TABLE motion_group
(
  id bigint NOT NULL,
  group_no integer,
  gems_group_id bigint,
  CONSTRAINT motion_group_pkey PRIMARY KEY (id),
  CONSTRAINT fk_gems_group_id FOREIGN KEY (gems_group_id)
      REFERENCES gems_groups (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE public.motion_group OWNER TO postgres;

CREATE TABLE switch_group
(
  id bigint NOT NULL,
  group_no integer,
  gems_group_id bigint,
  CONSTRAINT switch_group_pkey PRIMARY KEY (id),
  CONSTRAINT fk_gems_group_id FOREIGN KEY (gems_group_id)
      REFERENCES gems_groups (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE public.switch_group OWNER TO postgres;


CREATE TABLE wds_model_type
(
    id bigint NOT NULL,
    name character varying(50),
    model_image oid,
    no_of_buttons integer,
    CONSTRAINT wds_model_type_pkey PRIMARY KEY (id)
);

ALTER TABLE public.wds_model_type OWNER TO postgres;

CREATE TABLE wds_model_type_button
(
    id bigint NOT NULL,
    button_name character varying(50),
    wds_model_type_id bigint,
    button_no integer,
    CONSTRAINT wds_model_type_button_pkey PRIMARY KEY (id),
    CONSTRAINT fk_wds_model_type_id FOREIGN KEY (wds_model_type_id)
      REFERENCES wds_model_type (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE public.wds_model_type_button OWNER TO postgres;

CREATE TABLE button_map
(
    id bigint NOT NULL,
    wds_model_type_id bigint,
    CONSTRAINT button_map_pkey PRIMARY KEY (id),
    CONSTRAINT fk_wds_model_type_id FOREIGN KEY (wds_model_type_id)
      REFERENCES wds_model_type (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE public.button_map OWNER TO postgres;


CREATE TABLE button_manipulation
(
    id bigint NOT NULL,
    button_map_id bigint,
    CONSTRAINT button_manipulation_pkey PRIMARY KEY (id),
    CONSTRAINT fk_button_map_id FOREIGN KEY (button_map_id)
      REFERENCES button_map (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE public.button_manipulation OWNER TO postgres;
ALTER TABLE button_manipulation DROP COLUMN button_no;
ALTER TABLE button_manipulation DROP COLUMN manipulation;
ALTER TABLE button_manipulation DROP COLUMN action;

ALTER TABLE button_manipulation ADD COLUMN scene_toggle_order integer;
ALTER TABLE button_manipulation ADD COLUMN button_manip_action integer;
ALTER TABLE button_manipulation ALTER COLUMN button_manip_action TYPE bigint;


CREATE TABLE wds
(
    id bigint NOT NULL,
    name character varying(30),
    mac_address character varying(20),
    state character varying(20),
    gateway_id bigint,
    floor_id bigint,
    building_id bigint,
    campus_id bigint,
    area_id bigint,
    x integer,
    y integer,
    switch_id bigint,
    wds_model_type_id bigint,
    button_map_id bigint,
    switch_group_id bigint,
    CONSTRAINT wds_pkey PRIMARY KEY (id),
    CONSTRAINT fk_gateway_id FOREIGN KEY (gateway_id)
      REFERENCES gateway (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT fk_switch_id FOREIGN KEY (switch_id)
      REFERENCES switch (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT fk_wds_model_id FOREIGN KEY (wds_model_type_id)
      REFERENCES wds_model_type (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT fk_button_map_id FOREIGN KEY (button_map_id)
      REFERENCES button_map (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT fk_switch_group_id FOREIGN KEY (switch_group_id)
      REFERENCES switch_group (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE public.wds OWNER TO postgres;


CREATE SEQUENCE motion_group_seq INCREMENT BY 1 MAXVALUE 999999999999999999 NO MINVALUE CACHE 1;
ALTER TABLE public.motion_group_seq OWNER TO postgres;

CREATE SEQUENCE switch_group_seq INCREMENT BY 1 MAXVALUE 999999999999999999 NO MINVALUE CACHE 1;
ALTER TABLE public.switch_group_seq OWNER TO postgres;

CREATE SEQUENCE wds_seq INCREMENT BY 1 MAXVALUE 999999999999999999 NO MINVALUE CACHE 1;
ALTER TABLE public.wds_seq OWNER TO postgres;

CREATE SEQUENCE wds_model_type_seq INCREMENT BY 1 MAXVALUE 999999999999999999 NO MINVALUE CACHE 1;
ALTER TABLE public.wds_model_type_seq OWNER TO postgres;

CREATE SEQUENCE wds_model_type_button_seq INCREMENT BY 1 MAXVALUE 999999999999999999 NO MINVALUE CACHE 1;
ALTER TABLE public.wds_model_type_button_seq OWNER TO postgres;

CREATE SEQUENCE button_map_seq INCREMENT BY 1 MAXVALUE 999999999999999999 NO MINVALUE CACHE 1;
ALTER TABLE public.button_map_seq OWNER TO postgres;

CREATE SEQUENCE button_manipulation_seq INCREMENT BY 1 MAXVALUE 999999999999999999 NO MINVALUE CACHE 1;
ALTER TABLE public.button_manipulation_seq OWNER TO postgres;

CREATE SEQUENCE group_no_seq INCREMENT BY 1 MAXVALUE 999999 NO MINVALUE CACHE 1;
ALTER TABLE public.group_no_seq OWNER TO postgres;

ALTER TABLE wds ADD COLUMN association_state integer default 0;

CREATE OR REPLACE FUNCTION switch_migration() RETURNS character varying
    AS $$
DECLARE 
	tablename character varying;
    scene_count bigint;
    switch_id bigint;
    offsetby bigint;
BEGIN

    tablename := lower(relname) from pg_class where relname = 'switch_fixtures';

    if(tablename = 'switch_fixtures')
    then
        
        insert into gems_groups (id, group_name, description, floor_id) select nextval('gems_group_seq'), sw.name, sw.id, sw.floor_id from switch sw;

        insert into gems_group_fixture (id, group_id, fixture_id, need_sync, user_action) select nextval('gems_group_fixture_seq'), gg.id , swf.fixture_id, 0, 0 from switch_fixtures swf, gems_groups gg where gg.description = cast(swf.switch_id as character varying) group by gg.id, swf.fixture_id;

        update switch set gems_groups_id = (select gg.id from gems_groups gg where gg.description = cast(switch.id as character varying));
        
        insert into switch_group (id, group_no, gems_group_id) select nextval('switch_group_seq'), 14000000 + sw.gems_groups_id, sw.gems_groups_id from switch sw;
        
        update gems_group_fixture set user_action = 0 where user_action is null;

        offsetby := 0;

        for scene_count in ( select count(s1.id) from scene s1 group by s1.switch_id order by s1.switch_id)
        LOOP

            update scene set scene_order = (SELECT row_num - 1 FROM (SELECT ARRAY(SELECT s5.id FROM scene s5 where s5.switch_id = scene.switch_id order by s5.id) As order_id)  AS oldids CROSS JOIN generate_series(1, scene_count) AS row_num WHERE oldids.order_id[row_num] =  scene.id ORDER BY row_num) where scene.switch_id = (select s7.switch_id from scene s7 group by s7.switch_id order by s7.switch_id offset offsetby limit 1);
           
           offsetby := offsetby + 1;

        end LOOP;


        ALTER TABLE switch DROP COLUMN dimmer_control;
        ALTER TABLE switch DROP COLUMN scene_id;
        ALTER TABLE switch DROP COLUMN active_control;

        DROP TABLE switch_fixtures;
        DROP SEQUENCE switch_fixtures_seq;

    end if;
	
    return tablename;
END;
$$
LANGUAGE plpgsql;

select switch_migration();


CREATE OR REPLACE FUNCTION groups_migration() RETURNS character varying
    AS $$
DECLARE 
	tablename character varying;
    dummy bigint;
BEGIN

    tablename := lower(relname) from pg_class where relname = 'gems_group_type';

    if(tablename = 'gems_group_type')
    then

        insert into motion_group (id, group_no, gems_group_id) select nextval('motion_group_seq'), ggt.group_no, gg.id from gems_group_type ggt, gems_groups gg where gg.group_type = ggt.id and ggt.group_type = 'MotionGroup';
        
        update motion_group set group_no = 12000000 + group_no;
        
        select setval('group_no_seq', (select max(group_no)+2 from gems_group_type), false) into dummy;
        
        update gems_group_fixture set user_action = 0 where user_action is null;

        ALTER TABLE gems_groups DROP CONSTRAINT fk_gems_groups_company;
        ALTER TABLE gems_groups DROP CONSTRAINT gems_groups_group_type_fkey;
        ALTER TABLE gems_groups DROP COLUMN group_type;
        ALTER TABLE gems_groups DROP COLUMN company_id;

        DROP TABLE gems_group_type;
        DROP SEQUENCE gems_group_type_seq;

    end if;
	
    return tablename;
END;
$$
LANGUAGE plpgsql;

select groups_migration();

--Added by Sharad 03/05/13
update gems_group_fixture set need_sync = 0 where need_sync is null;

ALTER TABLE wds add COLUMN wds_no integer;

CREATE SEQUENCE wds_no_seq INCREMENT BY 1 MAXVALUE 999999 NO MINVALUE CACHE 1;
ALTER TABLE public.wds_no_seq OWNER TO postgres;

--- Added on 22/10/12, YGC
INSERT INTO wds_model_type (id, name, no_of_buttons) values (1, 'WDS4B1S', 4);

--Added by Sreedhar 10/22
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'default_utc_time_cmd_frequency', '300000');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'default_utc_time_cmd_offset', '240000');

--- Added on 23/10/12 YGC
UPDATE button_manipulation set button_manip_action=1164762395524923647 where button_manip_action=1169265995152294143;

--Yogesh 12/25
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.sw_20_pattern', 'sw.bin');

--Added by Sreedhar 10/30
ALTER TABLE wds ADD COLUMN version character varying(50);
ALTER TABLE wds ADD COLUMN upgrade_status character varying(20);

--Added by Kushal Nov/1/12
ALTER TABLE motion_bits_scheduler add COLUMN group_no integer;

--Sreedhar 07/26

CREATE TABLE device (
  id bigint not null,
  name character varying(50),
  location character varying(500),
  floor_id bigint NOT NULL,
  area_id bigint,
  type character varying(100),
  campus_id bigint,
  building_id bigint,
  x integer,
  y integer,
  mac_address character varying(50),
  version character varying(20),
  model_no character varying(50),
  reachability_status character varying(20),
  sev character varying(20),
  CONSTRAINT device_pk PRIMARY KEY (id),
  CONSTRAINT unique_device_mac_address UNIQUE (mac_address)
);

ALTER TABLE fixture add COLUMN current_data_id bigint;

DROP TYPE fixture_device_record;

CREATE TYPE fixture_device_record as (
  id bigint,
  name character varying(50),
  location character varying(500),
  floor_id bigint,
  area_id bigint,
  campus_id bigint,
  building_id bigint,
  x integer,
  y integer,
  mac_address character varying(50),
  version character varying(20),
  model_no character varying(50)

);

drop table gateway_new;

CREATE TABLE gateway_new (
  id bigint NOT NULL,
  status boolean,
  commissioned boolean,
  unique_identifier_id character varying,
  ip_address character varying(255),
  port smallint,
  snap_address character varying(20),
  gateway_type smallint,
  serial_port smallint,
  channel smallint,
  wireless_networkid integer,
  wireless_enctype smallint,
  wireless_enckey character varying(256),
  wireless_radiorate smallint default 0,
  eth_sec_type smallint default 0,
  eth_sec_integritytype smallint default 0,
  eth_sec_enctype smallint default 0,
  eth_sec_key character varying,
  eth_ipaddrtype smallint default 1,
  aes_key character varying(256),
  user_name character varying(50),
  password character varying(50),
  app1_version character varying(50),
  curr_uptime bigint,
  curr_no_pkts_from_gems bigint,
  curr_no_pkts_to_gems bigint,
  curr_no_pkts_from_nodes bigint,
  curr_no_pkts_to_nodes bigint,
  last_connectivity_at timestamp without time zone,
  last_stats_rcvd_time timestamp without time zone,
  subnet_mask character varying(255),
  default_gw character varying(255),
  no_of_sensors integer default 0,
  upgrade_status character varying(20),
  boot_loader_version character varying(50),
  no_of_wds integer default 0, 
  CONSTRAINT gateway_new_pk PRIMARY KEY (id)
);

DROP TYPE gw_device_record;

CREATE TYPE gw_device_record as (
  id bigint,
  gateway_name character varying(255),
  floor_id bigint,
  campus_id bigint,
  building_id bigint,
  status boolean,
  commissioned boolean,
  unique_identifier_id character varying,
  x integer,
  y integer,
  ip_address character varying(255),
  port smallint,
  snap_address character varying(20),
  gateway_type smallint,
  serial_port smallint,
  channel smallint,
  wireless_networkid integer,
  wireless_enctype smallint,
  wireless_enckey character varying(256),
  wireless_radiorate smallint,
  eth_sec_type smallint,
  eth_sec_integritytype smallint,
  eth_sec_enctype smallint,
  eth_sec_key character varying,
  eth_ipaddrtype smallint,
  aes_key character varying(256),
  mac_address character varying(50),
  user_name character varying(50),
  password character varying(50),
  app1_version character varying(50),
  app2_version character varying(50),
  curr_uptime bigint,
  curr_no_pkts_from_gems bigint,
  curr_no_pkts_to_gems bigint,
  curr_no_pkts_from_nodes bigint,
  curr_no_pkts_to_nodes bigint,
  last_connectivity_at timestamp without time zone,
  last_stats_rcvd_time timestamp without time zone,
  subnet_mask character varying(255),
  default_gw character varying(255),
  no_of_sensors integer,
  upgrade_status character varying(20),
  boot_loader_version character varying(50),
  location character varying(500),
  no_of_wds integer
);

CREATE TABLE fixture_current_data (
  id bigint not null,
  last_occupancy_seen integer,
  light_level integer,
  last_connectivity_at timestamp without time zone,
  last_stats_rcvd_time timestamp without time zone,
  profile_checksum smallint,
  global_profile_checksum smallint,
  avg_temperature smallint,
  dimmer_control integer,
  wattage integer,
  current_state character varying,
  curr_app smallint,
  baseline_power numeric(19,2),
  bulb_life double precision,
  CONSTRAINT fixture_current_data_pk PRIMARY KEY (id)
);

DROP TYPE fixture_stats_record;

CREATE TYPE fixture_stats_record as (
  id bigint,
  last_occupancy_seen integer,
  light_level integer,
  last_connectivity_at timestamp without time zone,
  last_stats_rcvd_time timestamp without time zone,
  profile_checksum smallint,
  global_profile_checksum smallint,
  avg_temperature smallint,
  dimmer_control integer,
  wattage integer,
  current_state character varying,
  curr_app smallint,
  baseline_power numeric(19,2),
  bulb_life double precision
);

CREATE OR REPLACE FUNCTION restructure_schema() returns void
as $$
DECLARE
  rec fixture_device_record;
  rec_gw gw_device_record;
  device_id bigint;
  no_of_rows bigint;
  rec_fix_stats fixture_stats_record;
BEGIN

  SELECT count(*) INTO no_of_rows
  FROM device;

  IF no_of_rows > 0 THEN
    RETURN;
  END IF;
 
  ALTER TABLE wds DROP CONSTRAINT fk_gateway_id;
  DROP TRIGGER update_fixture_gateway_change ON fixture;

  FOR rec IN (
    SELECT id, fixture_name, location, floor_id, area_id, campus_id, building_id, x, y, 
	snap_address, version, model_no FROM fixture)
  LOOP
    INSERT INTO device VALUES (rec.id, rec.name, rec.location, rec.floor_id, rec.area_id, 'Fixture',
	rec.campus_id, rec.building_id, rec.x, rec.y, rec.mac_address, rec.version, rec.model_no);
  END LOOP;

  SELECT count(*) INTO no_of_rows
  FROM device;

  IF no_of_rows = 0 THEN
    device_id = 1;
  ELSE 
    SELECT max(id) INTO device_id FROM device;
    device_id = device_id + 1;
  END IF;

  FOR rec_gw IN (SELECT * FROM gateway)
  LOOP
    INSERT INTO device (id, name, location, floor_id, type, campus_id, building_id, x, y, mac_address, version, model_no) VALUES (device_id, rec_gw.gateway_name, rec_gw.location, rec_gw.floor_id, 'Gateway',
     	rec_gw.campus_id, rec_gw.building_id, rec_gw.x, rec_gw.y, rec_gw.mac_address, rec_gw.app2_version, 'EN-GW');

    INSERT INTO gateway_new VALUES (device_id, rec_gw.status, rec_gw.commissioned, rec_gw.unique_identifier_id, 
	rec_gw.ip_address, rec_gw.port, rec_gw.snap_address, rec_gw.gateway_type, rec_gw.serial_port, 
	rec_gw.channel, rec_gw.wireless_networkid, rec_gw.wireless_enctype, rec_gw.wireless_enckey, rec_gw.wireless_radiorate, rec_gw.eth_sec_type, rec_gw.eth_sec_integritytype, rec_gw.eth_sec_enctype, rec_gw.eth_sec_key, rec_gw.eth_ipaddrtype, rec_gw.aes_key, rec_gw.user_name, rec_gw.password, rec_gw.app1_version,
	rec_gw.curr_uptime, rec_gw.curr_no_pkts_from_gems, rec_gw.curr_no_pkts_to_gems, rec_gw.curr_no_pkts_from_nodes,
	rec_gw.curr_no_pkts_to_nodes, rec_gw.last_connectivity_at, rec_gw.last_stats_rcvd_time, rec_gw.subnet_mask, rec_gw.default_gw, rec_gw.no_of_sensors, rec_gw.upgrade_status, rec_gw.boot_loader_version, rec_gw.no_of_wds);

    UPDATE fixture set gateway_id = device_id, sec_gw_id = device_id where sec_gw_id = rec_gw.id;
    UPDATE wds set gateway_id = device_id where gateway_id = rec_gw.id;
    UPDATE events_and_fault set gateway_id = device_id where gateway_id = rec_gw.id;

    device_id = device_id + 1;
  END LOOP;
  
  --Assign the new device id to device table seq
  PERFORM setval('fixture_seq', device_id);
  ALTER TABLE gateway RENAME TO gateway_orig;
  ALTER TABLE gateway_new RENAME TO gateway;

  ALTER TABLE wds ADD CONSTRAINT fk_gateway_id FOREIGN KEY (gateway_id) REFERENCES gateway(id);

  CREATE TRIGGER update_fixture_gateway_change
  BEFORE UPDATE ON fixture
  FOR EACH ROW
  EXECUTE PROCEDURE update_sec_gateway_change();

end;
$$
LANGUAGE plpgsql;

SELECT restructure_schema();
ALTER TABLE gateway DROP COLUMN mac_address;

-- Restructure events and fault schema, confirming that device table is present.
CREATE OR REPLACE FUNCTION restructure_eventsandfault_schema() returns void
as $$
DECLARE
	tablename character varying;
	isColumnExists boolean;
BEGIN
	tablename := lower(relname) from pg_class where relname = 'device';

    if(tablename = 'device')
    THEN
		isColumnExists = column_exists('device_id','events_and_fault');
		 --Raise Notice '=> %', isColumnExists;
		IF isColumnExists = 'f' THEN   
			ALTER TABLE events_and_fault RENAME COLUMN fixture_id TO device_id;
		  	UPDATE events_and_fault SET device_id = gateway_id WHERE gateway_id IN (SELECT id from gateway);  
	    END IF;
	 END IF;
END;
$$
LANGUAGE plpgsql;
SELECT restructure_eventsandfault_schema();

ALTER TABLE events_and_fault DROP COLUMN gateway_id;

CREATE TYPE wds_record AS (
  id bigint,
  name character varying,
  mac_address character varying,
  state character varying,
  gateway_id bigint,
  floor_id bigint,
  building_id bigint,
  campus_id bigint,
  area_id bigint,
  x integer,
  y integer,
  switch_id bigint,
  wds_model_type_id bigint,
  button_map_id bigint,
  switch_group_id bigint,
  wds_no integer,
  association_state integer,
  version character varying,
  upgrade_status character varying
);

DROP TABLE wds_new;

CREATE TABLE wds_new (
  id bigint,
  state character varying,
  gateway_id bigint,
  switch_id bigint,
  wds_model_type_id bigint,
  button_map_id bigint,
  switch_group_id bigint,
  wds_no integer,
  association_state integer,
  upgrade_status character varying
);

CREATE OR REPLACE FUNCTION restructure_switch_schema() returns void
as $$
DECLARE
  device_id bigint;
  no_of_rows bigint;
  rec_wds wds_record;
BEGIN

  SELECT count(*) INTO no_of_rows
  FROM device WHERE type = 'WDS';

  IF no_of_rows > 0 THEN
    RETURN;
  END IF;
 
  SELECT max(id) INTO device_id FROM device;
  device_id = device_id + 1;

  ALTER TABLE wds DROP CONSTRAINT fk_gateway_id; 
  ALTER TABLE wds DROP CONSTRAINT fk_switch_id; 
  ALTER TABLE wds DROP CONSTRAINT fk_wds_model_id; 
  ALTER TABLE wds DROP CONSTRAINT fk_button_map_id; 
  ALTER TABLE wds DROP CONSTRAINT fk_switch_group_id; 

  FOR rec_wds IN (SELECT * FROM wds)
  LOOP
    INSERT INTO device(id, name, type, floor_id, area_id, campus_id, building_id, x, y, mac_address, version) VALUES (device_id, 
	rec_wds.name, 'WDS', rec_wds.floor_id, rec_wds.area_id, rec_wds.campus_id, rec_wds.building_id, rec_wds.x, rec_wds.y, rec_wds.mac_address,
	rec_wds.version);

    INSERT INTO wds_new VALUES(device_id, rec_wds.state, rec_wds.gateway_id, rec_wds.switch_id, rec_wds.wds_model_type_id, rec_wds.button_map_id,
	rec_wds.switch_group_id, rec_wds.wds_no, rec_wds.association_state, rec_wds.upgrade_status);

    device_id = device_id + 1;
  END LOOP;

  --Assign the new device id to device table seq
  PERFORM setval('fixture_seq', device_id);
  ALTER TABLE wds RENAME TO wds_orig;
  ALTER TABLE wds_new RENAME TO wds;

  ALTER TABLE wds ADD CONSTRAINT fk_button_map_id FOREIGN KEY(button_map_id) REFERENCES button_map(id); 
  ALTER TABLE wds ADD CONSTRAINT fk_gateway_id FOREIGN KEY(gateway_id) REFERENCES gateway(id); 
  ALTER TABLE wds ADD CONSTRAINT fk_switch_group_id FOREIGN KEY(switch_group_id) REFERENCES switch_group(id); 
  ALTER TABLE wds ADD CONSTRAINT fk_switch_id FOREIGN KEY(switch_id) REFERENCES switch(id);
  ALTER TABLE wds ADD CONSTRAINT fk_wds_model_id FOREIGN KEY(wds_model_type_id) REFERENCES wds_model_type(id); 

end;
$$
LANGUAGE plpgsql;

SELECT restructure_switch_schema();


ALTER TABLE fixture DROP COLUMN floor_id;
ALTER TABLE fixture DROP COLUMN area_id;
ALTER TABLE fixture DROP COLUMN campus_id;
ALTER TABLE fixture DROP COLUMN building_id;
ALTER TABLE fixture DROP COLUMN x;
ALTER TABLE fixture DROP COLUMN y;
ALTER TABLE fixture DROP COLUMN mac_address;
ALTER TABLE fixture DROP COLUMN version;
ALTER TABLE fixture DROP COLUMN model_no;
ALTER TABLE fixture DROP COLUMN fixture_name;
ALTER TABLE fixture DROP COLUMN location;

--Sree 11/08
ALTER TABLE fixture ADD COLUMN reset_reason smallint;
ALTER TABLE energy_consumption ADD COLUMN last_temperature smallint;

--Sree 11/12
ALTER TABLE fixture ADD groups_checksum integer;
ALTER TABLE fixture ADD groups_sync_pending boolean default false;

--Yogesh 11/26
UPDATE fixture set groups_sync_pending=false where groups_sync_pending IS NULL;

ALTER TABLE motion_group ADD COLUMN fixture_version character varying;

--Sree 11/26 added new ballasts Modified sampath 01/JUL/2013 Moved below inserts with display label after ballasts_upgrade
--INSERT INTO ballasts (id, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES ((select coalesce(max(id),0)+1 from ballasts), 'EL1X70SC', '120-277', 'T8', 1, 1.00, 70, 'Helvar');
--INSERT INTO ballasts (id, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES ((select coalesce(max(id),0)+1 from ballasts), 'EL1X36SC', '120-277', 'T8', 1, 1.00, 36, 'Helvar');
--INSERT INTO ballasts (id, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES ((select coalesce(max(id),0)+1 from ballasts), 'EL2X36SC', '120-277', 'T8', 2, 1.00, 36, 'Helvar');

--Sampath 12/7 locator device support
CREATE TABLE locator_device
(
  id bigint NOT NULL,
  locator_device_type character varying(50),
  CONSTRAINT locator_device_pk PRIMARY KEY (id)
);
ALTER TABLE public.locator_device OWNER TO postgres;

ALTER TABLE locator_device ADD COLUMN fixture_class_id bigint;

ALTER TABLE locator_device ADD COLUMN estimated_burn_hours bigint;

--sharad 12/12 - Added FK constraint on derived_from_group for maintaining data integrity.
ALTER TABLE groups ADD CONSTRAINT fk_derived_from_group_id FOREIGN KEY (derived_from_group) REFERENCES groups (id);

--sharad 12/12 - changes in profile name trigger change in fixture current_profile and origial_profile_from

CREATE OR REPLACE FUNCTION update_fixture_currentprofilename_change() RETURNS "trigger" AS $$
	BEGIN
	  IF tg_op = 'UPDATE' THEN
	    IF old.name <> new.name THEN
	    	IF old.default_profile = 't' THEN
				UPDATE fixture SET current_profile = new.name
				 where current_profile= old.name OR current_profile=old.name || '_Default' and old.profile_no>0;
				
				UPDATE fixture SET original_profile_from = new.name
				where original_profile_from = old.name OR original_profile_from = old.name || '_Default' and old.profile_no>0;
	    	ELSE
				UPDATE fixture SET current_profile = new.name
				 where current_profile= old.name and old.profile_no>0;
				
				UPDATE fixture SET original_profile_from = new.name
				where original_profile_from = old.name and old.profile_no>0;
	    	END IF;
	    END IF;
	   ELSEIF TG_OP = 'DELETE' THEN
	     	UPDATE fixture SET original_profile_from = 'None'
			where original_profile_from = old.name;
	   END IF;
	  RETURN new;
	END
$$ LANGUAGE plpgsql;

ALTER FUNCTION public.update_fixture_currentprofilename_change() OWNER TO postgres;

CREATE TRIGGER update_profilename_change AFTER DELETE OR UPDATE ON groups
FOR EACH ROW
Execute procedure update_fixture_currentprofilename_change();

--sharad 17/12 - Name of default Profile Instance (_default), Should always match Profile Template name.  
CREATE OR REPLACE FUNCTION update_profilename_on_templatename_change() RETURNS "trigger" AS $$
	BEGIN
	   IF tg_op = 'UPDATE' THEN
	    IF old.name <> new.name THEN
	       	UPDATE groups SET name = new.name
			 where template_id= old.id and default_profile='true';
	    END IF;
	   END IF;
	 RETURN new;
	END
$$ LANGUAGE plpgsql;

ALTER FUNCTION public.update_profilename_on_templatename_change() OWNER TO postgres;

CREATE TRIGGER update_templatename_change AFTER UPDATE ON profile_template
FOR EACH ROW
Execute procedure update_profilename_on_templatename_change();

-- 19/12/2012 Yogesh
CREATE INDEX device_area_id_index ON device USING btree (area_id);
CREATE INDEX device_floor_id_index ON device USING btree (floor_id);
CREATE INDEX device_building_id_index ON device USING btree (building_id);
CREATE INDEX device_campus_id_index ON device USING btree (campus_id);

--Chetan 12/27
ALTER TABLE image_upgrade_device_status ADD device_type character varying;

ALTER TABLE switch_group ADD COLUMN fixture_version character varying;

-- 27/12/2012 Chetan - Added profile upgrade
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'profileupgrade.enable', 'true');

-- 31/12/2012 Chetan - modified the switch group table
DROP TYPE switchrecord;
DROP TYPE fixturerecord;

CREATE TYPE switchrecord as (
  gems_group_id bigint,
  fixture_version character varying(50)  
);

CREATE TYPE fixturerecord as (
  group_id bigint,
  fixture_id bigint,
  version character varying
);

CREATE OR REPLACE FUNCTION restructure_switchgroupschema() returns void
as $$
DECLARE
  rec switchrecord;  
  fixrec fixturerecord;
  sGroupId bigint;
  sValue character varying;
  fixtureId bigint;
  firmwareVersion character varying;
  fxVersion character varying;

  groupId bigint;
  fId bigint;	
  isNewFixture boolean;
  
BEGIN
 
FOR rec IN (SELECT gems_group_id,fixture_version FROM switch_group where fixture_version is null)
LOOP
  isNewFixture = 't';
  sGroupId = rec.gems_group_id;   
		--loop through gems_group_fixture for each fixture and find version of each fixture
		FOR fixrec IN (SELECT g.group_id , g.fixture_id, d.version from gems_group_fixture g,device d where group_id = sGroupId and g.fixture_id=d.id)
		LOOP
		fxVersion = SUBSTR(fixrec.version,0,2);
		--Raise Notice 'For Group% => Fixture Id => % Fixture Version %', sGroupId,fixrec.fixture_id,fxVersion;
			IF fxVersion = '1' THEN			
			isNewFixture = 'f';
			EXIT;			
			END IF;
		END LOOP;
  If isNewFixture = 'f' THEN
	--Raise Notice 'Switch Id % , Switch Version 1.x',sGroupId;
	UPDATE switch_group set fixture_version = '1.x' where gems_group_id = sGroupId;
  ELSIF isNewFixture = 't' THEN
  	--Raise Notice 'Switch Id % , Switch Version 2.x',sGroupId;
  	UPDATE switch_group set fixture_version = '2.x' where gems_group_id = sGroupId;
  END IF;
  
  fxVersion = '';
END LOOP;  
  

END;
$$
LANGUAGE plpgsql;

SELECT restructure_switchgroupschema();

CREATE OR REPLACE FUNCTION restructure_motiongroupschema() returns void
as $$
DECLARE
  rec switchrecord;  
  fixrec fixturerecord;
  mGroupId bigint;
  mValue character varying;
  fixtureId bigint;
  firmwareVersion character varying;
  fxVersion character varying;

  groupId bigint;
  fId bigint;	
  isNewFixture boolean;
  
BEGIN
 
FOR rec IN (SELECT gems_group_id,fixture_version FROM motion_group where fixture_version is null)
LOOP
  isNewFixture = 't';
  mGroupId = rec.gems_group_id;   
		--loop through gems_group_fixture for each fixture and find version of each fixture
		FOR fixrec IN (SELECT g.group_id , g.fixture_id, d.version from gems_group_fixture g,device d where group_id = mGroupId and g.fixture_id=d.id)
		LOOP
		fxVersion = SUBSTR(fixrec.version,0,2);
		--Raise Notice 'For Group% => Fixture Id => % Fixture Version %', sGroupId,fixrec.fixture_id,fxVersion;
			IF fxVersion = '1' THEN			
			isNewFixture = 'f';
			EXIT;			
			END IF;
		END LOOP;
  If isNewFixture = 'f' THEN
	--Raise Notice 'Switch Id % , Switch Version 1.x',sGroupId;
	UPDATE motion_group set fixture_version = '1.5' where gems_group_id = mGroupId;
  ELSIF isNewFixture = 't' THEN
  	--Raise Notice 'Switch Id % , Switch Version 2.x',sGroupId;
  	UPDATE motion_group set fixture_version = '2.0' where gems_group_id = mGroupId;
  END IF;
  
  fxVersion = '';
END LOOP;  
  

END;
$$
LANGUAGE plpgsql;

SELECT restructure_motiongroupschema();



UPDATE event_type SET severity = 2, active = 1 WHERE type = 'Fixture Out';
UPDATE event_type SET severity = 5, active = 0 WHERE type = 'Push Profile';
UPDATE event_type SET severity = 4, active = 0 WHERE type = 'Profile Mismatch';
UPDATE event_type SET severity = 3, active = 1 WHERE type = 'Bad Profile';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Fixture Upgrade';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Gateway Upgrade';
UPDATE event_type SET severity = 2, active = 1 WHERE type = 'Fixture CU Failure';
UPDATE event_type SET severity = 3, active = 1 WHERE type = 'Fixture Image Checksum Failure';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'DR Condition';
UPDATE event_type SET severity = 5, active = 0 WHERE type = 'Fixture associated Group Changed';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Bacnet';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Discovery';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Commissioning';
UPDATE event_type SET severity = 4, active = 0 WHERE type = 'Profile Mismatch User Action';
UPDATE event_type SET severity = 2, active = 1 WHERE type = 'Fixture Hardware Failure';
UPDATE event_type SET severity = 3, active = 1 WHERE type = 'Fixture Too Hot';
UPDATE event_type SET severity = 3, active = 1 WHERE type = 'Fixture CPU Usage is High';
UPDATE event_type SET severity = 2, active = 1 WHERE type = 'Gateway configuration error';
UPDATE event_type SET severity = 3, active = 1 WHERE type = 'Erroneous Energy Reading';
UPDATE event_type SET severity = 2, active = 1 WHERE type = 'Gateway Connection Failure';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'EM upgrade';
UPDATE event_type SET severity = 5, active = 0 WHERE type = 'Scheduler';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Fixture Group change';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'EWS Upgrade';
UPDATE event_type SET severity = 2, active = 1 WHERE type = 'Lamp Out';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Download Power Usage Characterization';

UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Placed Fixture Upload';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Fixture Configuration Upload';

-- 3rd Jan 2013 Kushal - cloud communicator
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cloud.communicate.type', '1');

ALTER TABLE image_upgrade_device_status ADD COLUMN new_version character varying;

ALTER TABLE device ADD COLUMN pcba_part_no character varying(50);
ALTER TABLE device ADD COLUMN pcba_serial_no character varying(50);
ALTER TABLE device ADD COLUMN hla_part_no character varying(50);
ALTER TABLE device ADD COLUMN hla_serial_no character varying(50);


--Sree 01/25/2013
ALTER TABLE energy_consumption ADD COLUMN sys_uptime bigint;

create language plperl;

CREATE OR REPLACE FUNCTION general_wal_trigger() RETURNS trigger AS $$
if($_TD->{event} eq 'INSERT') {

    my $isFirst = 0;
    my $statement = "insert into ";
    $statement .= "$_TD->{table_name} ";
    my $col_names = "(";
    my $col_values = "(";

    my %newrow = %{$_TD->{new}};
    my %oldrow = %{$_TD->{old}};

    while (($column,$value) = each %newrow) 
    {
		my $columnLength = length($column);

		if ($columnLength != 0 ) 
		{
            my $isNull = 0;
            if (!defined($newrow{$column})) 
            {
                $isNull = 1;
            }

            if ( $isFirst == 0 ) {
                $isFirst = 1;
            } else {
                $col_names .= ", ";
                $col_values .= ", ";
            }
            $col_names .= "$column";
            if ( $isNull == 0 ) {
				$value =~ s/\\/\\\\\\\\/g;
				$value =~ s/'/''''/g;		
                $col_values .= "E\\\'";
                $col_values .= $value;
                $col_values .= "\\\'";
            } else {
                $col_values .= "null";
            }
        }                     
    }
    $statement .= "$col_names";
    $statement .= ") values ";
    $statement .= "$col_values";
    $statement .= ");";
    if($_TD->{table_name} =~ m/^qrtz/) {
    	spi_exec_query("insert into wal_logs (id, creation_time, action, table_name, record_id, sql_statement) values (nextval('wal_logs_seq'), current_timestamp, 'INSERT', '$_TD->{table_name}' , null, E'$statement' )");
    } else {
    	spi_exec_query("insert into wal_logs (id, creation_time, action, table_name, record_id, sql_statement) values (nextval('wal_logs_seq'), current_timestamp, 'INSERT', '$_TD->{table_name}' , '$newrow{id}', E'$statement' )");
    }
	return;
}
if($_TD->{event} eq 'UPDATE') {
    my $isFirst  = 0;
    my $statement = "update ";
    $statement .= "$_TD->{table_name} ";
    $statement .= " set ";

    my %newrow = %{$_TD->{new}};
    my %oldrow = %{$_TD->{old}};

    while (($column,$value) = each %newrow) {
		my $columnLength = length($column);
        if ($columnLength != 0 ) {
            my $isNull = 0;
            if (!defined($newrow{$column})) {
                $isNull = 1;
            }

            if ( $isFirst == 0 ) {
                $isFirst = 1;
            } else {
                $statement .= ", ";
            }
            $statement .= "$column = ";
            if ( $isNull == 0 ) {
				$value =~ s/\\/\\\\\\\\/g;
				$value =~ s/'/''''/g;		
                $statement .= 'E\\\'';
				$statement .= "$value";
				$statement .= '\\\'';
            } else {
                $statement .= "null";
            }
        }                     
    }
    $statement .= " where ";
    if($_TD->{table_name} =~ m/^qrtz/) {
        $isFirst = 0;
    	while (($column,$value) = each %oldrow) {
		    my $columnLength = length($column);
		    if ($columnLength != 0 ) {
	        	my $isNull = 0;
		        if (!defined($oldrow{$column})) {
	        	    $isNull = 1;
		        }
		
		        if($isNull == 0) {
		        	if($isFirst == 0) {
		        		$isFirst = 1;
		        	}
		        	else {
		        		$statement .= " and ";
				    }

				    $statement .= "$column = ";
					$value =~ s/\\/\\\\\\\\/g;
					$value =~ s/'/''''/g;		
		            $statement .= 'E\\\'';
					$statement .= "$value";
					$statement .= '\\\'';
		       	}                     
		    }
		}
		$statement .= ";";
    } else {
		$statement .= " id = $oldrow{id};";
    }
    if($_TD->{table_name} =~ m/^qrtz/) {
       spi_exec_query("insert into wal_logs (id, creation_time, action, table_name, record_id, sql_statement) values (nextval('wal_logs_seq'), current_timestamp, 'UPDATE', '$_TD->{table_name}' , null, E'$statement' )");
    } else {
    	spi_exec_query("insert into wal_logs (id, creation_time, action, table_name, record_id, sql_statement) values (nextval('wal_logs_seq'), current_timestamp, 'UPDATE', '$_TD->{table_name}' , '$oldrow{id}', E'$statement' )");
    }
    return;
}
if($_TD->{event} eq 'DELETE') {
    my %oldrow = %{$_TD->{old}};
    my %newrow = %{$_TD->{new}};
    my $statement = "delete from ";

    $statement .= "$_TD->{table_name}";
    $statement .= " where ";
    
    if($_TD->{table_name} =~ m/^qrtz/) {
        $isFirst = 0;
    	while (($column,$value) = each %oldrow) {
	    	my $columnLength = length($column);
	    	if ($columnLength != 0 ) {
        		my $isNull = 0;
	        	if (!defined($oldrow{$column})) {
        	    	$isNull = 1;
	        	}
	
		        if($isNull == 0) {
		        	if($isFirst == 0) {
		        		$isFirst = 1;
		        	}
		        	else {
		        		$statement .= " and ";
				    }

                 	$statement .= "$column = ";
					$value =~ s/\\/\\\\\\\\/g;
					$value =~ s/'/''''/g;		
	                $statement .= 'E\\\'';
					$statement .= "$value";
					$statement .= '\\\'';
	       		}                     
	    	}
		}
        $statement .= ";";
    } else {
		$statement .= "id = $oldrow{id};";
    }
    if($_TD->{table_name} =~ m/^qrtz/) {
    	spi_exec_query("insert into wal_logs (id, creation_time, action, table_name, record_id, sql_statement) values (nextval('wal_logs_seq'), current_timestamp, 'DELETE', '$_TD->{table_name}' , null, E'$statement' )");
    } else {
    	spi_exec_query("insert into wal_logs (id, creation_time, action, table_name, record_id, sql_statement) values (nextval('wal_logs_seq'), current_timestamp, 'DELETE', '$_TD->{table_name}' , '$oldrow{id}', E'$statement' )");
    }
    return;
}
$$ LANGUAGE plperl;

--update dropRelations function as well
CREATE OR REPLACE FUNCTION addTriggers() RETURNS character varying
AS $$
DECLARE 
    output character varying;
    rec character varying;
BEGIN	
	select value from system_configuration into output where name = 'cloud.communicate.type'; 
    if output = '2' then
	    FOR rec IN (SELECT table_name FROM information_schema.tables WHERE table_schema='public')
	    LOOP
	       if rec != 'energy_consumption' AND rec != 'energy_consumption_hourly' AND rec != 'energy_consumption_daily' AND rec != 'wal_logs' AND rec != 'cloud_config' AND rec != 'sync_tasks' AND rec != 'plugload_energy_consumption' AND rec != 'plugload_energy_consumption_hourly' AND rec != 'plugload_energy_consumption_daily' then
	        	EXECUTE  'DROP TRIGGER IF EXISTS ' || rec || '_wal_trigger' || ' ON ' ||    rec   ;
	            EXECUTE 'CREATE TRIGGER ' || rec || '_wal_trigger' || ' AFTER INSERT OR UPDATE OR DELETE ON ' ||    rec::regclass || ' FOR EACH ROW EXECUTE PROCEDURE general_wal_trigger()';
	        end if;
	        if rec = 'energy_consumption' OR rec = 'energy_consumption_hourly' OR rec = 'energy_consumption_daily' or rec = 'plugload_energy_consumption' OR rec = 'plugload_energy_consumption_hourly' OR rec = 'plugload_energy_consumption_daily' then
	        EXECUTE  'DROP TRIGGER IF EXISTS ' || rec || '_wal_trigger' || ' ON ' ||    rec ;
	        EXECUTE 'CREATE TRIGGER ' || rec || '_wal_trigger' || ' AFTER INSERT OR UPDATE ON ' ||    rec::regclass || ' FOR EACH ROW EXECUTE PROCEDURE general_wal_trigger()';
	        end if;
	    END LOOP;
    end if;
    RETURN output;
END;
$$ LANGUAGE plpgsql; 

CREATE OR REPLACE FUNCTION removeAllTriggers() RETURNS text AS $$
  DECLARE 
    output character varying;
    rec RECORD;
BEGIN	
    output := '';
    FOR rec IN (select * from information_schema.triggers where trigger_name like '%wal_trigger' order by trigger_name)
    LOOP
        EXECUTE  'DROP TRIGGER IF EXISTS ' || rec.trigger_name  || ' ON ' ||    rec.event_object_table   ;
    END LOOP;
    RETURN output;   
END;
$$ LANGUAGE plpgsql; 

CREATE OR REPLACE FUNCTION dropRelations() RETURNS character varying
AS $$
DECLARE 
    output character varying;
    rec character varying;
    fks RECORD;
BEGIN	
	
	delete from pg_largeobject;
	delete from pg_largeobject_metadata;
	
    
    for fks in (SELECT tc.constraint_name, tc.table_name FROM information_schema.table_constraints AS tc WHERE constraint_type = 'FOREIGN KEY')
    LOOP
        EXECUTE  'ALTER TABLE public.' || fks.table_name ||  ' DROP CONSTRAINT ' ||    fks.constraint_name   ;
    END LOOP;

	FOR rec IN (SELECT table_name FROM information_schema.tables WHERE table_schema='public')
	LOOP
       if rec != 'energy_consumption' AND rec != 'energy_consumption_hourly' AND rec != 'energy_consumption_daily' AND rec != 'em_motion_bits' AND rec != 'plugload_energy_consumption' AND rec != 'plugload_energy_consumption_hourly' AND rec != 'plugload_energy_consumption_daily' then
        	EXECUTE  'DROP TABLE ' || rec ;
       end if;
	 END LOOP;

    FOR rec IN (SELECT c.relname FROM pg_class c WHERE c.relkind = 'S')
	LOOP
       if rec != 'energy_consumption_seq' AND rec != 'energy_consumption_hourly_seq' AND rec != 'energy_consumption_daily_seq' AND rec != 'em_motion_bits_seq' AND rec != 'plugload_energy_consumption_seq' AND rec != 'plugload_energy_consumption_hourly_seq' AND rec != 'plugload_energy_consumption_daily_seq'  then
        	EXECUTE  'DROP SEQUENCE ' || rec ;
       end if;
	 END LOOP;

    RETURN output;
END;
$$ LANGUAGE plpgsql; 


CREATE TABLE wal_logs
(
  id bigint NOT NULL,
  creation_time timestamp without time zone,
  action character varying,
  table_name character varying,
  record_id bigint,
  sql_statement character varying,
  CONSTRAINT wal_logs_pk PRIMARY KEY (id)
);

ALTER TABLE wal_logs OWNER TO postgres;

CREATE SEQUENCE wal_logs_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE wal_logs_seq OWNER TO postgres;

CREATE TABLE cloud_config (
  id bigint NOT NULL,
  name character varying,
  val character varying,
  CONSTRAINT cloud_config_pk PRIMARY KEY (id),  
  CONSTRAINT unique_cloud_config_name UNIQUE (name)
);

ALTER TABLE cloud_config OWNER TO postgres;

insert into cloud_config (id, name, val) values ((select coalesce(max(id),0)+1 from cloud_config), 'lastWalSyncId', '-99');

CREATE TABLE email_configuration (
  id bigint NOT NULL,
  email_smtp_host character varying DEFAULT 'smtp.office365.com',
  email_smtp_port character varying DEFAULT '587',
  email_smtp_user character varying DEFAULT '',
  email_smtp_pass character varying DEFAULT '',
  email_transport_protocol character varying DEFAULT 'smtp',
  email_smtp_auth character varying DEFAULT 'true',
  email_smtp_starttls_enable character varying DEFAULT 'true',
  CONSTRAINT email_configuration_pk PRIMARY KEY (id)
);
ALTER TABLE email_configuration OWNER TO postgres;
CREATE SEQUENCE email_configuration_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
ALTER TABLE email_configuration_seq OWNER TO postgres;


-- 11th Feb 2013 Chetan - EWS Update logs , update for older logs.
update ems_user_audit set action_type = 'EWS Update' where action_type='Ews Update';
update ems_user_audit set action_type = 'LDAP Update' where action_type='Ldap Update';
update ems_user_audit set action_type = 'Organization Update' where action_type='Company Update';

--Added by Sreedhar 02/14
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.hopper_channel_change_no_of_retries', '6');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.gw_wait_time_for_hoppers', '10');

UPDATE system_configuration SET value = '100' WHERE name =  'imageUpgrade.interPacketDelay_2';

--Added by Sree 02/15 Modified sampath 01/JUL/2013 Moved below insert with display label after ballasts_upgrade
--INSERT INTO ballasts (id, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES ((select coalesce(max(id),0)+1 from ballasts), 'PTDCCD15350S10', '230', 'LED', 1, 1.00, 3, 'VLM');

INSERT INTO bulbs (id, manufacturer, bulb_name, type, energy, life_ins_start, life_prog_start, color_temp, diameter) VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Nebula', 'LEDSH10083W COOL',  'LED', 3, 50000, 50000, 4000, 43);

--Sree 02/26  ballasts upgrade Modified sampath 01/JUL/2013
CREATE OR REPLACE FUNCTION ballasts_upgrade() returns void
as $$
DECLARE
  col_name character varying;
BEGIN
  col_name := lower(attname) from pg_attribute where attrelid = (select oid from pg_class where relname = 'ballasts') and attname = 'baseline_load'; 
  if(col_name  IS NULL )
  then
  		ALTER TABLE ballasts ADD COLUMN baseline_load numeric(19,2);
  end if;
  
  col_name := lower(attname) from pg_attribute where attrelid = (select oid from pg_class where relname = 'ballasts') and attname = 'display_label'; 
  if(col_name  IS NULL )
  then
  	   ALTER TABLE ballasts ADD COLUMN display_label character varying;
  end if;

  update ballasts set display_label =  trim(ballast_name) || '(' || trim(manufacturer) || ',' || trim(lamp_type) || ',' || wattage || 'W,' || lamp_num || ' bulb)' where lamp_num = 1 and display_label is null;
  update ballasts set display_label =  trim(ballast_name) || '(' || trim(manufacturer) || ',' || trim(lamp_type) || ',' || wattage || 'W,' || lamp_num || ' bulbs)' where lamp_num > 1 and display_label is null;

END;
$$
LANGUAGE plpgsql;

select ballasts_upgrade();

CREATE OR REPLACE FUNCTION oid_to_bytea(val oid) 
returns bytea as $$
declare merged bytea;
declare arr bytea;
 BEGIN  
   FOR arr IN SELECT data from pg_largeobject WHERE loid = val ORDER BY pageno LOOP
     IF merged IS NULL THEN
       merged := arr;
     ELSE
       merged := merged || arr;
     END IF;
   END LOOP;
  RETURN merged;

END  
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION plan_map_oid_to_bytea() returns void as $$
declare plan_oid integer;
declare plan_data_type character varying;
BEGIN
    SELECT data_type into plan_data_type FROM information_schema.columns WHERE table_name='plan_map' and column_name='plan';

    IF plan_data_type = 'oid' then

        alter table plan_map add column plan_bytea bytea;

        FOR plan_oid in SELECT cast(plan as integer) from plan_map LOOP
            EXECUTE 'update plan_map set plan_bytea = (select cast((oid_to_bytea(' || plan_oid || ')) as bytea)) where cast(plan as integer) = ' || plan_oid;
        END LOOP;
        alter table plan_map drop column plan;
    END IF;

END
$$ LANGUAGE plpgsql;

select plan_map_oid_to_bytea();

alter table plan_map rename column plan_bytea to plan;

alter table wds_model_type drop column model_image;

select setval('lightlevel_seq', (select max(id)+1 from lightlevels));



INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'enable.cloud.communication', '0');

--10/04/2013 - Addition of advanced profile
ALTER TABLE profile_handler ADD COLUMN dr_low_level smallint DEFAULT 0;
ALTER TABLE profile_handler ADD COLUMN dr_moderate_level smallint DEFAULT 0;
ALTER TABLE profile_handler ADD COLUMN dr_high_level smallint DEFAULT 0;
ALTER TABLE profile_handler ADD COLUMN dr_special_level smallint DEFAULT 0;

CREATE OR REPLACE FUNCTION update_dr_target() returns void as $$
declare dr_constraint_name character varying;
declare dr_type_column character varying;
BEGIN
    select constraint_name into dr_constraint_name from information_schema.table_constraints where table_name='dr_target' and constraint_name='unique_price_level';
    IF dr_constraint_name = 'unique_price_level' then
        ALTER TABLE dr_target DROP CONSTRAINT unique_price_level;
	END IF;
	
	SELECT column_name into dr_type_column  FROM information_schema.columns WHERE table_name='dr_target' and column_name='dr_type';
    IF dr_type_column is null then
        ALTER TABLE dr_target ADD COLUMN dr_identifier character varying(255);
		ALTER TABLE dr_target ADD COLUMN dr_status character varying(63);
		ALTER TABLE dr_target ADD COLUMN dr_type character varying(20);
		delete from dr_target;
	END IF;
END
$$ LANGUAGE plpgsql;

--Added sampath 01/JUL/2013

INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer, display_label) VALUES ((select coalesce(max(id),0)+1 from ballasts), NULL, 'PTDCCD15350S10', '230', 'LED', 1, 1.00, 3, 'VLM', 'PTDCCD15350S10(VLM,LED,3W,1 bulb)');

INSERT INTO ballasts (id, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer, display_label) VALUES ((select coalesce(max(id),0)+1 from ballasts), 'EL1X70SC', '120-277', 'T8', 1, 1.00, 70, 'Helvar', 'EL1X70SC(Helvar,T8,70W,1 bulb)');

INSERT INTO ballasts (id, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer, display_label) VALUES ((select coalesce(max(id),0)+1 from ballasts), 'EL1X36SC', '120-277', 'T8', 1, 1.00, 36, 'Helvar', 'EL1X36SC(Helvar,T8,36W,1 bulb)');

INSERT INTO ballasts (id, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer, display_label) VALUES ((select coalesce(max(id),0)+1 from ballasts), 'EL2X36SC', '120-277', 'T8', 2, 1.00, 36, 'Helvar', 'EL2X36SC(Helvar,T8,36W,2 bulbs)');

--Sree 04/19
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer, display_label) VALUES ((select coalesce(max(id),0)+1 from ballasts), NULL, 'Axlen LED Driver', '100-277', 'LED', 2, 1, 23, 'Axlen Lighting', 'Axlen LED Driver(Axlen Lighting,LED,23W,2 bulbs)');

INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer, display_label) VALUES ((select coalesce(max(id),0)+1 from ballasts), NULL, 'Axlen LED Driver', '100-277', 'LED', 3, 1, 23, 'Axlen Lighting', 'Axlen LED Driver(Axlen Lighting,LED,23W,3 bulbs)');

INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer, display_label) VALUES ((select coalesce(max(id),0)+1 from ballasts), NULL, 'Axlen LED Driver', '100-277', 'LED', 2, 1, 10, 'Axlen Lighting', 'Axlen LED Driver(Axlen Lighting,LED,10W,2 bulbs)');

INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer, display_label) VALUES ((select coalesce(max(id),0)+1 from ballasts), NULL, 'Axlen LED Driver', '100-277', 'LED', 3, 1, 10, 'Axlen Lighting', 'Axlen LED Driver(Axlen Lighting,LED,10W,3 bulbs)');

INSERT INTO bulbs (id, manufacturer, bulb_name, type, energy, life_ins_start, life_prog_start, color_temp, length) VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Axlen Lighting', 'AXTJ-T8-4F', 'LED', 23, 40000, 40000, 4000, 4);

INSERT INTO bulbs (id, manufacturer, bulb_name, type, energy, life_ins_start, life_prog_start, color_temp, length) VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Axlen Lighting', 'AXTJ-T8-2F', 'LED', 10, 40000, 40000, 4000, 2);

-- 25/04/2013 Yogesh - profile override init setup
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'profileoverride.init.enable', 'true');

UPDATE event_type set type = 'ERC Upgrade' where type = 'EWS Upgrade';
UPDATE event_type set type = 'ERC Discovery' where type = 'EWS Discovery';
UPDATE event_type set type = 'ERC Commissioning' where type = 'EWS Commissioning';

--Creation of canned profiles
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cannedprofile.enable', '1');

CREATE SEQUENCE cannedprofile_configuration_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE cannedprofile_configuration_seq OWNER TO postgres;

CREATE TABLE canned_profiles_configuration
(
  id bigint NOT NULL,
  "name" character varying,
  status boolean DEFAULT false,
  parentprofileid integer,
  CONSTRAINT canned_profiles_configuration_pk PRIMARY KEY (id),
  CONSTRAINT unique_cannedprofiles_configuration_name UNIQUE (name)
);

--Canned profiles creation Start 31May2013

CREATE OR REPLACE FUNCTION setUpCannedProfileDefaults() RETURNS void 
    AS $$
DECLARE
     sKey character varying;
     sValue character varying;
     sGroupId int;

     profiles_parent_mapping text[] = '{
     {"Breakroom_Normal","default.breakroom.","2"},
     {"Closed Corridor_Normal","default.closedcorridor.","5"},
     {"Closed Corridor_AlwaysOn","default.closedcorridor.","5"},
     {"Open Office_Normal","default.openoffice.","9"},
     {"Open Office_AlwaysOn","default.openoffice.","9"},
     {"Open Office_Dim","default.openoffice.","9"},
     {"Private Office_Normal","default.privateoffice.","10"},
     {"Highbay_Normal","default.highbay.","16"},
     {"Highbay_AlwaysOn","default.highbay.","16"},
     {"Conference Room_Normal","default.conferenceroom.","3"}
     }';  
     
     profiles_adv_cols text[] = ARRAY['groupnameholder','pfh.dark_lux','pfh.neighbor_lux','pfh.to_off_linger','pfh.envelope_on_level'];
     profiles_groups_with_adv_defaults text[] = '{
     {"default.breakroom_normal","5","250","900","20"},
     {"default.closedcorridor_normal","5","250","30","20"},
     {"default.closedcorridor_alwayson","5","250","10","20"},
     {"default.openoffice_normal","5","250","30","20"},
     {"default.openoffice_alwayson","5","250","30","20"},
     {"default.openoffice_dim","5","250","30","20"},
     {"default.privateoffice_normal","5","250","30","20"},                                        
     {"default.highbay_normal","5","250","300","20"},
     {"default.highbay_alwayson","5","250","300","20"},
     {"default.conferenceroom_normal","5","250","30","50"}
     }';

     profile_pahers text[] = ARRAY['profile.morning', 'profile.day', 'profile.evening', 'profile.night','weekend.profile.morning',
'weekend.profile.day', 'weekend.profile.evening', 'weekend.profile.night','holiday.profile.morning',
'holiday.profile.day', 'holiday.profile.evening', 'holiday.profile.night'];

     profile_cols text[] = ARRAY['min_level','on_level','motion_detect_duration','ramp_up_time','ambient_sensitivity'];

     
     profiles_defaults int[] = ARRAY[
         --breakroom_normal
         [
             -- weekday [morning, day, evening, night]
             [0,60,1,2,10], [0,60,3,2,10], [0,60,1,2,10], [0,60,1,2,0],
             -- weekend
             [0,60,1,2,10], [0,60,1,2,10], [0,60,1,2,10], [0,60,1,2,0],
             -- profile overrides [override1 10%, override2 25%,override3 50%, override4 40%]
             [0,50,1,2,10], [0,40,1,2,10], [0,30,1,2,10], [0,40,1,2,10]
         ],         
         --Closed corridor normal
         [
	     -- weekday [morning, day, evening, night]
             [20,50,1,0,10], [20,50,1,0,10], [20,50,1,0,10], [0,50,1,0,0],
             -- weekend
             [0,50,1,0,10], [0,50,1,0,10], [0,50,1,0,10], [0,50,1,0,0],
             -- profile overrides [override1 10%, override2 25%,override3 50%, override4 40%]
             [0,50,1,0,10], [0,50,1,0,10], [0,50,1,0,10], [0,50,1,0,0]
         ],
         --Closed corridor Always On
         [
	     -- weekday [morning, day, evening, night]
             [20,50,1,0,10], [20,50,1,0,10], [20,50,1,0,10], [20,50,1,0,0],
             -- weekend
             [20,50,1,0,10], [20,50,1,0,10], [20,50,1,0,10], [20,50,1,0,0],
             -- profile overrides [override1 10%, override2 25%,override3 50%, override4 40%]
             [20,40,1,0,10], [20,30,1,0,10], [20,20,1,0,10], [20,30,1,0,0]
         ],
	-- Open Office Normal	
         [
	     -- weekday [morning, day, evening, night]
             [20,60,7,2,10], [20,60,10,2,10], [20,60,7,2,10], [0,60,1,2,0],
             -- weekend
             [0,60,3,2,10], [0,60,10,2,10], [0,60,3,2,10], [0,60,1,2,0],
             -- profile overrides [override1 10%, override2 25%,override3 50%, override4 40%]
             [10,50,7,2,10], [10,40,5,2,10], [0,30,3,2,10], [10,40,5,2,0]
         ],
	-- Open Office Always On
         [
	     -- weekday [morning, day, evening, night]
             [20,60,7,2,10], [20,60,10,2,10], [20,60,7,2,10], [20,60,1,2,0],
             -- weekend
             [20,60,3,2,10], [20,60,10,2,10], [20,60,3,2,10], [20,60,1,2,0],
             -- profile overrides [override1 10%, override2 25%,override3 50%, override4 40%]
             [10,50,7,2,10], [10,40,5,2,10], [10,30,3,2,10], [10,40,5,2,0]
         ],
	-- Open Office Dim
         [
	     -- weekday [morning, day, evening, night]
             [20,60,7,2,10], [20,60,10,2,10], [20,60,7,2,10], [0,60,1,2,0],
             -- weekend
             [0,60,3,2,10], [0,60,10,2,10], [0,60,3,2,10], [0,60,1,2,0],
             -- profile overrides [override1 10%, override2 25%,override3 50%, override4 40%]
             [10,50,7,2,10], [10,40,5,2,10], [0,30,3,2,10], [10,40,5,2,0]
         ],
	-- Private Office Normal
         [
	     -- weekday [morning, day, evening, night]
             [0,60,7,2,8], [0,60,15,2,8], [0,60,7,2,8], [0,60,1,2,0],
             -- weekend
             [0,60,3,2,8], [0,60,10,2,8], [0,60,3,2,8], [0,60,3,2,0],
             -- profile overrides [override1 10%, override2 25%,override3 50%, override4 40%]
             [0,50,7,2,10], [0,40,5,2,10], [0,30,3,2,10], [0,40,3,2,0]
         ] ,
	-- HighBay Normal
         [
	     -- weekday [morning, day, evening, night]
             [0,70,1,0,10], [0,70,2,0,10], [0,70,1,0,10], [0,70,1,0,0],
             -- weekend
             [0,70,1,0,10], [0,70,1,0,10], [0,70,1,0,10], [0,70,1,0,0],
             -- profile overrides [override1 10%, override2 25%,override3 50%, override4 40%]
             [0,60,1,0,10], [0,50,1,0,10], [0,40,1,0,10], [0,50,1,0,10]
         ],
	-- HighBay Always On
         [
	     -- weekday [morning, day, evening, night]
             [20,70,1,0,10], [20,70,2,0,10], [20,70,1,0,10], [20,70,1,0,0],
             -- weekend
             [20,70,1,0,10], [20,70,1,0,10], [20,70,1,0,10], [20,70,1,0,0],
             -- profile overrides [override1 10%, override2 25%,override3 50%, override4 40%]
             [10,60,1,0,10], [10,50,1,0,10], [10,40,1,0,10], [10,50,1,0,10]
         ],
	-- Conference Room Normal
         [
	     -- weekday [morning, day, evening, night]
             [0,60,10,3,8], [0,60,10,3,8], [0,60,10,3,8], [0,60,3,3,0],
             -- weekend
             [0,60,3,3,8], [0,60,10,3,8], [0,60,3,3,8], [0,60,3,3,0],
             -- profile overrides [override1 10%, override2 25%,override3 50%, override4 40%]
             [0,50,5,3,8], [0,40,5,3,8], [0,30,5,3,10], [0,40,3,3,8]
         ]

        ];
BEGIN     
      FOR i in 1..array_upper(profiles_groups_with_adv_defaults, 1) LOOP
      sKey := profiles_groups_with_adv_defaults[i][1];
      FOR j in 1..array_upper(profile_pahers, 1) LOOP
             FOR k in 1..array_upper(profile_cols, 1) LOOP
                 sKey := profiles_groups_with_adv_defaults[i][1] || '.' ||  profile_pahers[j] || '.' || profile_cols[k];
                 sValue := profiles_defaults[i][j][k];
                 IF EXISTS (SELECT value from system_configuration where name = sKey) THEN
                     Raise Notice 'U% => %', sKey, sValue;
                     UPDATE system_configuration set value = sValue where name = sKey;
                 ELSE               
                     INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), sKey, sValue); 
                 END IF;  
             END LOOP;
         END LOOP;
         FOR l in 2..array_upper(profiles_adv_cols, 1) LOOP
             sKey := profiles_groups_with_adv_defaults[i][1] || '.' || profiles_adv_cols[l];
             sValue := profiles_groups_with_adv_defaults[i][l];
             --Raise Notice '%.% => %', profiles_groups_with_adv_defaults[i][1], profiles_adv_cols[l], profiles_groups_with_adv_defaults[i][l]; 
             IF EXISTS (SELECT value from system_configuration where name = sKey) THEN
                 UPDATE system_configuration set value = sValue where name = sKey;
             ELSE
                 INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), sKey, sValue); 
             END IF;    
         END LOOP;
     END LOOP;         

     --insert the mapping values in system_configuration table
     FOR m in 1..array_upper(profiles_parent_mapping, 1) LOOP
     sKey := profiles_parent_mapping[m][1];
     sValue := profiles_parent_mapping[m][2];
     sGroupId := profiles_parent_mapping[m][3];
     --Raise Notice '%=>%', sKey,sValue;
     		 IF EXISTS (SELECT value from system_configuration where name = sKey) THEN
                 UPDATE system_configuration set value = sValue where name = lower(sKey);
             ELSE
                 INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), lower(sKey), sValue);                 
             END IF;
             
             IF EXISTS (SELECT name from canned_profiles_configuration where name = sKey) THEN                 
                 --Raise Notice 'Not modifying as already exist%=>%', sKey,sValue;
             ELSE                  
                 --Insert if upgrading for first time
                 INSERT INTO canned_profiles_configuration(id,name,status,parentprofileid ) values((select coalesce(max(id),0)+1 from canned_profiles_configuration),sKey,'false',sGroupId);
                 --Raise Notice 'Inserted%=>%', sKey,sValue;
             END IF;
     END LOOP;          
END;
$$
LANGUAGE plpgsql;

Select setUpCannedProfileDefaults();

ALTER TABLE dr_target ADD COLUMN opt_in boolean DEFAULT true;
ALTER TABLE dr_target ADD COLUMN priority integer;
ALTER TABLE dr_target ADD COLUMN start_after bigint;
ALTER TABLE dr_target ADD COLUMN jitter bigint;
ALTER TABLE dr_target ADD COLUMN cancel_time timestamp without time zone;
ALTER TABLE dr_target ADD COLUMN uid integer;
select update_dr_target();

-- 09/05/2013 Sharad -  Updating DR Level and Target Redction mapping into dr_target
UPDATE dr_target set target_reduction = 10 where dr_type = 'MANUAL' and price_level ='Low';
UPDATE dr_target set target_reduction = 25 where dr_type = 'MANUAL' and price_level ='Moderate';
UPDATE dr_target set target_reduction = 50 where dr_type = 'MANUAL' and price_level ='High';
UPDATE dr_target set target_reduction = 40 where dr_type = 'MANUAL' and price_level ='Special';

update dr_target set dr_status = 'Cancelled' where dr_status is null;
update dr_target set jitter = 0 where jitter is null;
update dr_target set start_time = current_timestamp where start_time is null;
update dr_target set start_after = 0 where start_after is null;

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'dr.minimum.polltimeinterval', '20');

--Sampath 01/JUL/2013 ALL the new insert ballast statements should be with display_label column and executed after this function
CREATE OR REPLACE FUNCTION remove_duplicate_ballasts() returns void
as $$
DECLARE 
              rec RECORD;
              temp bigint;

BEGIN
			  FOR rec IN select display_label from ballasts group by display_label HAVING count(*) > 1 LOOP
              				select id INTO temp from ballasts b where b.display_label = rec.display_label order by id LIMIT 1;
              				UPDATE fixture SET ballast_id = temp where ballast_id IN (select id from ballasts b where b.display_label = rec.display_label);
              END LOOP;

              DELETE FROM ballasts WHERE ID NOT IN(SELECT MIN(ID)FROM ballasts GROUP BY display_label);
              
              ALTER TABLE ONLY public.ballasts  
              	ADD CONSTRAINT unique_display_label UNIQUE(display_label);

END;
$$
LANGUAGE plpgsql;

select remove_duplicate_ballasts();

CREATE SEQUENCE ballast_seq
	INCREMENT 1
	MINVALUE 1
	NO MAXVALUE
	START 1
	CACHE 1;
ALTER TABLE ballast_seq OWNER TO postgres;


ALTER TABLE ONLY public.fixture  
    ADD CONSTRAINT fk_fixture_to_ballasts FOREIGN KEY(ballast_id) REFERENCES ballasts(id);

--Start - Added 09/07/2013 for Fixture Class , Addition of the new bulbs through this script should be done before this sequence.
CREATE SEQUENCE bulb_seq
	INCREMENT 1
	MINVALUE 1
	NO MAXVALUE
	START 1
	CACHE 1;
ALTER TABLE bulb_seq OWNER TO postgres;
-- End

ALTER TABLE fixture_class DROP CONSTRAINT unique_fixture_class;

CREATE TABLE fixture_class
(
  id bigint NOT NULL,
  name character varying(255),
  voltage integer,
  no_of_ballasts integer,
  ballast_id bigint,
  bulb_id bigint,
  CONSTRAINT fixture_class_pkey PRIMARY KEY (id),
  CONSTRAINT unique_fixture_class_name UNIQUE (name),
  CONSTRAINT fk_fixture_class_to_ballasts FOREIGN KEY (ballast_id)
  REFERENCES ballasts (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_fixture_class_to_bulbs FOREIGN KEY (bulb_id)
  REFERENCES bulbs (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
); 

ALTER TABLE fixture_class OWNER TO postgres;

CREATE SEQUENCE fixture_class_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999999999
  START 1
  CACHE 1;

ALTER TABLE fixture_class_seq OWNER TO postgres;

ALTER TABLE fixture ADD COLUMN fixture_class_id bigint;

--Added by Sharad 19-July-2013
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bulbconfiguration.enable', 'false');

--by Default Bulb Configuration Feature will be disable as per new request from Product Manager  - User has to set it to 'true' to enable the feature
UPDATE system_configuration SET value = 'false' where name = 'bulbconfiguration.enable';


--Yogesh: Support for storing fixture volt power map calibration
CREATE TABLE fixture_lamp_calibration
(
  id bigint NOT NULL,
  capture_at timestamp without time zone NOT NULL,
  fixture_id bigint NOT NULL,
  initial boolean default false,
  CONSTRAINT fixture_lamp_calibration_pk PRIMARY KEY (id),
  CONSTRAINT unique_fixture_lamp_calibration_time_fxid UNIQUE (fixture_id, capture_at)
);

CREATE SEQUENCE fixture_lamp_calibration_seq
   INCREMENT 1
   MINVALUE 1
   MAXVALUE 999999999999999999
   START 1;
ALTER TABLE fixture_lamp_calibration_seq OWNER TO postgres;

CREATE TABLE fixture_calibration_map
(
  id bigint NOT NULL,
  fixture_lamp_calibration_id bigint NOT NULL,
  volt double precision,
  power double precision,
  lux double precision,
  CONSTRAINT fixture_calibration_map_pk PRIMARY KEY (id),
  CONSTRAINT fixture_lamp_calibration_id_fk FOREIGN KEY (fixture_lamp_calibration_id) REFERENCES fixture_lamp_calibration(id)
);

CREATE SEQUENCE fixture_calibration_map_seq
   INCREMENT 1
   MINVALUE 1
   MAXVALUE 999999999999999999
   START 1;
ALTER TABLE fixture_calibration_map_seq OWNER TO postgres;

CREATE TABLE lamp_calibration_configuration
(
  id bigint NOT NULL,
  enabled boolean default true,
  facility_type smallint default 0,
  frequency smallint default 30,
  scheduled_time int,
  mode smallint,
  warmup_time smallint default 5,
  stabilization_time smallint default 5,
  excluded_fixtures varchar,
  potential_degrade_threshold smallint default 5,
  degrade_threshold smallint default 50,
  CONSTRAINT lamp_calibration_configuration_pk PRIMARY KEY (id)
);

CREATE SEQUENCE lamp_calibration_configuration_seq
   INCREMENT 1
   MINVALUE 1
   MAXVALUE 999999999999999999
   START 1;
ALTER TABLE lamp_calibration_configuration_seq OWNER TO postgres;

CREATE UNIQUE INDEX lamp_calibration_configuration_unique_row_constraint ON lamp_calibration_configuration((id IS NOT NULL));

INSERT INTO lamp_calibration_configuration (id, enabled, facility_type, frequency, scheduled_time, mode, warmup_time, stabilization_time, excluded_fixtures, potential_degrade_threshold, degrade_threshold) VALUES (nextval('lamp_calibration_configuration_seq'), true, 0, 0, 0, 0, 120, 10, '', 5, 50);
--End

CREATE TABLE placed_fixture
(
  id bigint NOT NULL,
  name character varying(50),
  location character varying(500),
  floor_id bigint NOT NULL,
  campus_id bigint,
  building_id bigint,
  x integer,
  y integer,
  mac_address character varying(50),
  "type" character varying,
  ballast_manufacturer character varying,
  bulb_manufacturer character varying,
  ballast_id bigint,
  bulb_id bigint,
  no_of_fixtures integer DEFAULT 1,
  voltage smallint DEFAULT 277,
  CONSTRAINT placed_fixture_pk PRIMARY KEY (id),
  CONSTRAINT unique_mac_address UNIQUE (mac_address)
);

ALTER TABLE placed_fixture OWNER TO postgres;

--
-- Name: placed_fixture_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE placed_fixture_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE placed_fixture_seq OWNER TO postgres;


--Added by Yogesh on 28/07/13
ALTER TABLE ballast_volt_power ADD COLUMN inputvolt double precision default 277;
CREATE UNIQUE INDEX unique_ballast_volt_power_inputvolt ON ballast_volt_power (volt_power_map_id, volt, inputvolt);
UPDATE ballast_volt_power set inputvolt = 277 where inputvolt is NULL;

--Added by Sharad on 08/08/13
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'enable.softmetering', 'true');

--Added by Sree on 08/19
INSERT INTO ballasts (id, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer, display_label) VALUES ((select coalesce(max(id),0)+1 from ballasts), 'EL2X54SC' , '220-240', 'T5', 2, 1.00, 54, 'Helvar', 'EL2X54SC(Helvar,T5,54W,2 bulbs)');

INSERT INTO ballasts (id, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer, display_label) VALUES ((select coalesce(max(id),0)+1 from ballasts), 'BCD80.2F-01'  , '220-240', 'T5', 2, 1.00, 80, 'BAG Electronics', 'BCD80.2F-01(BAG Electronics,T5,80W,2 bulbs)');

INSERT INTO bulbs (id, manufacturer, bulb_name, type, energy, life_ins_start, life_prog_start, color_temp, diameter, cri, length) VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'Master TL5 HO 54W/840' ,  'T5', 54, 19000, 24000, 4000, 17, 85, 4.5);

INSERT INTO bulbs (id, manufacturer, bulb_name, type, energy, life_ins_start, life_prog_start, color_temp, diameter, cri, length) VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'Master TL5 HO 80W/840' ,  'T5', 80, 19000, 24000, 4000, 17, 85, 4.5);

--Added by Chetan 04/09/2013
ALTER TABLE ballasts ADD COLUMN is_default integer DEFAULT 0;
UPDATE ballasts set is_default=1 where id=9;
UPDATE ballasts set is_default=0 where is_default is NULL;

--Added by Kushal 19/9/2013
ALTER TABLE ballast_volt_power ADD COLUMN enabled boolean default true;
update ballast_volt_power set enabled = true where enabled is null;

ALTER TABLE fixture_calibration_map ADD COLUMN enabled boolean default true;
update fixture_calibration_map set enabled = true where enabled is null;

--Set up default ballast curve for GE ballast 30Sep2013

CREATE OR REPLACE FUNCTION setUpDefaultBallastCurve() RETURNS void 
    AS $$
DECLARE
     sKey character varying;
     sValue character varying;
     sBallastId int;
	 sInputVolt double precision;
	 bvpNextIndex int;
	 voltPowerMapId int;
	 dbVoltPowerMapId int;
	 dbPower double precision;
	 cnt int;
	 --Ballast Id to be updated with Default Ballast Curve
     ballastsIds int[] = ARRAY[23,19,27];
     
     --Input voltage for which the curve to be stored
     inputVoltages int[] = ARRAY[120,277];
     
     --Voltages range from 10.0 to 0.5, first value denotes the input reference voltage and other values are the power readings
     volts_ref text[] = ARRAY['inputVolt','10','9.5','9.0','8.5','8.0','7.5','7.0','6.5','6.0','5.5','5.0','4.5','4.0','3.5','3.0','2.5','2.0','1.5','1.0','0.5'];

     -- For each and every ballast and input voltage add the curve data into below multidimentional array.
     -- NOTE : If we need to add curve for only for single input voltage then make the entry in the below array with the excluded curve data starting from 0
     ballastVoltPower_values text[] = ARRAY[
         --GE332MVPS-N-V03
         [
             --InputVoltage (110/277) followed by power readings for [10.0-0.5] volt
             [120,82,81,77,73,69,65,70.5,66,61.5,58,54,49.5,45,40.5,36.5,31.5,25.5,17,12,12],
             [277,83.5,83.5,80,76,72,72.5,72.5,68.5,64.5,60,56,51,46.5,42,37.5,32.5,27,18,14,14]
         ]
         ,         
         --GE232MVPSN-V03
         [
	      	  --InputVoltage (110/277) followed by power readings for [10.0-0.5] volt
             [120,57,56.5,55,52.5,49.5,52,49.5,46.5,43,40,37,34,30.5,27,24,20.5,16.5,11,9,9],
             [277,56,56,53.5,51,49.5,47.5,44,41,38.5,35,32.5,29.5,26.5,24,21,18,13.5,8.5,8,8]
         ],
         --GE432MVPSN-V03
         [
	    	 --InputVoltage (110/277) followed by power readings for [10.0-0.5] volt
             [120,114,110.5,105.5,100.5,95.5,101.5,96,90.5,84.5,78.5,72.5,66,60,53.5,46,39.5,31,22,15,15],
             [277,112.5,110,103.5,98.5,93,99,92.5,85.5,81,75,70.4,64,58.5,53.5,46.5,39,32.5,21.5,16,16]
         ]
         
        ];
BEGIN     
    FOR i in 1..array_upper(ballastsIds, 1) LOOP
      sBallastId := ballastsIds[i];
      FOR j in 1..array_upper(inputVoltages, 1) LOOP
      SELECT max(volt_power_map_id) INTO voltPowerMapId FROM ballast_volt_power;
      voltPowerMapId = voltPowerMapId + 1;
      sInputVolt := inputVoltages[j];
      cnt=0;
      SELECT count(*) INTO cnt FROM ballast_volt_power where ballast_id = sBallastId and inputvolt=sInputVolt;
      --Raise Notice 'cnt==>%',cnt;
	      IF cnt < 20 THEN
	            FOR k in 1..array_upper(volts_ref, 1) LOOP
	                sKey := volts_ref[k];
	                 sValue := ballastVoltPower_values[i][j][k];
	                 
	                 IF k = 1 THEN
	                 	sInputVolt = sValue;
	                 END IF;
	    			 
	                 IF sInputVolt!=0 and k!=1 THEN
	               	 	SELECT power,volt_power_map_id INTO dbPower,dbVoltPowerMapId FROM ballast_volt_power where ballast_id = sBallastId and inputvolt=sInputVolt and volt= cast(sKey as double precision);
	               	 	IF dbPower IS NOT NULL and cnt > 0 and cnt <= 20 THEN
	               	 		voltPowerMapId = dbVoltPowerMapId;
	               	 	END IF;
	               	 	--Raise Notice '%',dbPower;
	               	 	IF dbPower IS NULL THEN
	               	 		--Raise Notice 'Inserted %=>%=>%=>%', sBallastId,sInputVolt, sKey,sValue;
	               	 		INSERT INTO ballast_volt_power (id, ballast_id, volt_power_map_id, volt, power,inputvolt) values (nextval('ballast_volt_power_seq'),sBallastId, voltPowerMapId, cast(sKey as double precision), cast(sValue as double precision),sInputVolt);
	               	 	END IF;
	               	 	
	               	 END IF;
	             END LOOP;
	        END IF;
       END LOOP;
     END LOOP; 
END;
$$
LANGUAGE plpgsql;

Select setUpDefaultBallastCurve();
-- Changing inputvoltage from 110 to 120 as EM is using 120V everywhere.
update ballast_volt_power set inputvolt=120 where inputvolt=110;

-- Adding useFXcurve flag
ALTER TABLE fixture ADD COLUMN use_fx_curve boolean DEFAULT TRUE;

-- Adding warm up time and stabilization time details into fixture_lamp_calibration table
ALTER TABLE fixture_lamp_calibration ADD COLUMN warmup_time smallint;
ALTER TABLE fixture_lamp_calibration ADD COLUMN stabilization_time smallint;

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'temperature_unit', 'F');

--Added by Sharad on 13/11/13
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'enable.connexusfeature', '0');

--Added by Chetan 11/Dec/2013
--This procedure needs to be placed here only before fxtypecreate procedure
CREATE OR REPLACE FUNCTION remove_duplicate_bulbs_from_upgradescript() returns void
as $$
DECLARE 	
        minid1 numeric;
BEGIN
	--For First bulb
	select min(id) from bulbs where manufacturer='Nebula' and bulb_name='LEDSH10083W COOL' and type='LED' and energy=3 and initial_lumens is null and design_lumens is null and life_ins_start=50000 and life_prog_start=50000 and diameter=43 and length is null and cri is null and color_temp=4000 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer='Nebula' and bulb_name='LEDSH10083W COOL' and type='LED' and energy=3 and initial_lumens is null and design_lumens is null and life_ins_start=50000 and life_prog_start=50000 and diameter=43 and length is null and cri is null and color_temp=4000 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer='Nebula' and bulb_name='LEDSH10083W COOL' and type='LED' and energy=3 and initial_lumens is null and design_lumens is null and life_ins_start=50000 and life_prog_start=50000 and diameter=43 and length is null and cri is null and color_temp=4000 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer='Nebula' and bulb_name='LEDSH10083W COOL' and type='LED' and energy=3 and initial_lumens is null and design_lumens is null and life_ins_start=50000 and life_prog_start=50000 and diameter=43 and length is null and cri is null and color_temp=4000 and id > minid1);

	--For second bulb	
	select min(id) from bulbs where manufacturer = 'Axlen Lighting' and bulb_name = 'AXTJ-T8-4F' and type = 'LED' and initial_lumens is null and design_lumens is null and energy = 23 and life_ins_start = 40000 and life_prog_start = 40000 and diameter is null and length = 4 and cri is null and color_temp = 4000 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Axlen Lighting' and bulb_name = 'AXTJ-T8-4F' and type = 'LED' and initial_lumens is null and design_lumens is null and energy = 23 and life_ins_start = 40000 and life_prog_start = 40000 and diameter is null and length = 4 and cri is null and color_temp = 4000 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Axlen Lighting' and bulb_name = 'AXTJ-T8-4F' and type = 'LED' and initial_lumens is null and design_lumens is null and energy = 23 and life_ins_start = 40000 and life_prog_start = 40000 and diameter is null and length = 4 and cri is null and color_temp = 4000 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Axlen Lighting' and bulb_name = 'AXTJ-T8-4F' and type = 'LED' and initial_lumens is null and design_lumens is null and energy = 23 and life_ins_start = 40000 and life_prog_start = 40000 and diameter is null and length = 4 and cri is null and color_temp = 4000 and id > minid1);

	--For Third bulb
	select min(id) from bulbs where manufacturer = 'Axlen Lighting' and bulb_name = 'AXTJ-T8-2F' and type = 'LED' and initial_lumens is null and design_lumens is null and energy=10 and life_ins_start=40000 and life_prog_start=40000 and diameter is null and length = 2 and cri is null and color_temp = 4000 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Axlen Lighting' and bulb_name = 'AXTJ-T8-2F' and type = 'LED' and initial_lumens is null and design_lumens is null and energy=10 and life_ins_start=40000 and life_prog_start=40000 and diameter is null and length = 2 and cri is null and color_temp = 4000 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Axlen Lighting' and bulb_name = 'AXTJ-T8-2F' and type = 'LED' and initial_lumens is null and design_lumens is null and energy=10 and life_ins_start=40000 and life_prog_start=40000 and diameter is null and length = 2 and cri is null and color_temp = 4000 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Axlen Lighting' and bulb_name = 'AXTJ-T8-2F' and type = 'LED' and initial_lumens is null and design_lumens is null and energy=10 and life_ins_start=40000 and life_prog_start=40000 and diameter is null and length = 2 and cri is null and color_temp = 4000 and id > minid1);

	--For Fourth bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'Master TL5 HO 54W/840' and type ='T5' and initial_lumens is null and design_lumens is null and energy = 54 and life_ins_start = 19000 and life_prog_start = 24000 and diameter = 17 and length = 4.5 and cri = 85 and color_temp = 4000 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'Master TL5 HO 54W/840' and type ='T5' and initial_lumens is null and design_lumens is null and energy = 54 and life_ins_start = 19000 and life_prog_start = 24000 and diameter = 17 and length = 4.5 and cri = 85 and color_temp = 4000 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'Master TL5 HO 54W/840' and type ='T5' and initial_lumens is null and design_lumens is null and energy = 54 and life_ins_start = 19000 and life_prog_start = 24000 and diameter = 17 and length = 4.5 and cri = 85 and color_temp = 4000 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'Master TL5 HO 54W/840' and type ='T5' and initial_lumens is null and design_lumens is null and energy = 54 and life_ins_start = 19000 and life_prog_start = 24000 and diameter = 17 and length = 4.5 and cri = 85 and color_temp = 4000 and id > minid1);

	--For Fifth bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'Master TL5 HO 80W/840' and type = 'T5' and initial_lumens is null and design_lumens is null and energy = 80 and life_ins_start=19000 and life_prog_start=24000 and diameter = 17 and length = 4.5 and cri = 85 and color_temp = 4000 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'Master TL5 HO 80W/840' and type = 'T5' and initial_lumens is null and design_lumens is null and energy = 80 and life_ins_start=19000 and life_prog_start=24000 and diameter = 17 and length = 4.5 and cri = 85 and color_temp = 4000 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'Master TL5 HO 80W/840' and type = 'T5' and initial_lumens is null and design_lumens is null and energy = 80 and life_ins_start=19000 and life_prog_start=24000 and diameter = 17 and length = 4.5 and cri = 85 and color_temp = 4000 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'Master TL5 HO 80W/840' and type = 'T5' and initial_lumens is null and design_lumens is null and energy = 80 and life_ins_start=19000 and life_prog_start=24000 and diameter = 17 and length = 4.5 and cri = 85 and color_temp = 4000 and id > minid1);	
	
	--For DALI 1st bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 39W 835 2 0-10 7 G2' and type = 'LED' and initial_lumens = 4180 and design_lumens = 4180 and energy = 39 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 3500 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 39W 835 2 0-10 7 G2' and type = 'LED' and initial_lumens = 4180 and design_lumens = 4180 and energy = 39 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 3500 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 39W 835 2 0-10 7 G2' and type = 'LED' and initial_lumens = 4180 and design_lumens = 4180 and energy = 39 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 3500 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 39W 835 2 0-10 7 G2' and type = 'LED' and initial_lumens = 4180 and design_lumens = 4180 and energy = 39 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 3500 and id > minid1);

	--For DALI 2nd bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 39W 840 2 0-10 7 G2' and type = 'LED' and initial_lumens = 4280 and design_lumens = 4280 and energy = 39 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 4000 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 39W 840 2 0-10 7 G2' and type = 'LED' and initial_lumens = 4280 and design_lumens = 4280 and energy = 39 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 4000 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 39W 840 2 0-10 7 G2' and type = 'LED' and initial_lumens = 4280 and design_lumens = 4280 and energy = 39 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 4000 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 39W 840 2 0-10 7 G2' and type = 'LED' and initial_lumens = 4280 and design_lumens = 4280 and energy = 39 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 4000 and id > minid1);

	--For DALI 3rd bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 40W 835 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 4180 and design_lumens = 4180 and energy = 40 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 3500 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 40W 835 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 4180 and design_lumens = 4180 and energy = 40 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 3500 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 40W 835 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 4180 and design_lumens = 4180 and energy = 40 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 3500 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 40W 835 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 4180 and design_lumens = 4180 and energy = 40 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 3500 and id > minid1);

	--For DALI 4th bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 40W 840 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 4280 and design_lumens = 4280 and energy = 40 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 4000 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 40W 840 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 4280 and design_lumens = 4280 and energy = 40 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 4000 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 40W 840 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 4280 and design_lumens = 4280 and energy = 40 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 4000 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 40W 840 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 4280 and design_lumens = 4280 and energy = 40 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 4000 and id > minid1);

	--For DALI 5th bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 42W 835 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 4180 and design_lumens = 4180 and energy = 42 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 3500 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 42W 835 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 4180 and design_lumens = 4180 and energy = 42 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 3500 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 42W 835 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 4180 and design_lumens = 4180 and energy = 42 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 3500 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 42W 835 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 4180 and design_lumens = 4180 and energy = 42 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 3500 and id > minid1);

	--For DALI 6th bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 42W 840 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 4280 and design_lumens = 4280 and energy = 42 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 4000 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 42W 840 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 4280 and design_lumens = 4280 and energy = 42 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 4000 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 42W 840 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 4280 and design_lumens = 4280 and energy = 42 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 4000 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x4 P 42L 42W 840 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 4280 and design_lumens = 4280 and energy = 42 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 4 and cri = 80 and color_temp = 4000 and id > minid1);	

	--For DALI 7th bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 31W 835 2 0-10 7 G2' and type = 'LED' and initial_lumens = 3210 and design_lumens = 3210 and energy = 31 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 3500 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 31W 835 2 0-10 7 G2' and type = 'LED' and initial_lumens = 3210 and design_lumens = 3210 and energy = 31 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 3500 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 31W 835 2 0-10 7 G2' and type = 'LED' and initial_lumens = 3210 and design_lumens = 3210 and energy = 31 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 3500 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 31W 835 2 0-10 7 G2' and type = 'LED' and initial_lumens = 3210 and design_lumens = 3210 and energy = 31 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 3500 and id > minid1);

	--For DALI 8th bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 31W 840 2 0-10 7 G2' and type = 'LED' and initial_lumens = 3280 and design_lumens = 3280 and energy = 31 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 4000 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 31W 840 2 0-10 7 G2' and type = 'LED' and initial_lumens = 3280 and design_lumens = 3280 and energy = 31 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 4000 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 31W 840 2 0-10 7 G2' and type = 'LED' and initial_lumens = 3280 and design_lumens = 3280 and energy = 31 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 4000 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 31W 840 2 0-10 7 G2' and type = 'LED' and initial_lumens = 3280 and design_lumens = 3280 and energy = 31 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 4000 and id > minid1);	
	
	--For DALI 9th bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 32W 835 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 3210 and design_lumens = 3210 and energy = 32 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 3500 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 32W 835 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 3210 and design_lumens = 3210 and energy = 32 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 3500 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 32W 835 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 3210 and design_lumens = 3210 and energy = 32 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 3500 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 32W 835 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 3210 and design_lumens = 3210 and energy = 32 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 3500 and id > minid1);
	
	--For DALI 10th bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 32W 840 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 3280 and design_lumens = 3280 and energy = 32 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 4000 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 32W 840 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 3280 and design_lumens = 3280 and energy = 32 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 4000 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 32W 840 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 3280 and design_lumens = 3280 and energy = 32 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 4000 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 32W 840 1 Mk10 7 G2' and type = 'LED' and initial_lumens = 3280 and design_lumens = 3280 and energy = 32 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 4000 and id > minid1);
	
	--For DALI 11th bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 34W 835 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 3210 and design_lumens = 3210 and energy = 34 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 3500 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 34W 835 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 3210 and design_lumens = 3210 and energy = 34 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 3500 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 34W 835 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 3210 and design_lumens = 3210 and energy = 34 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 3500 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 34W 835 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 3210 and design_lumens = 3210 and energy = 34 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 3500 and id > minid1);
	
	--For DALI 12th bulb
	select min(id) from bulbs where manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 34W 840 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 3280 and design_lumens = 3280 and energy = 34 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 4000 INTO minid1;
	--Update the record
	update fixture set bulb_id = minid1 where bulb_id IN (select id from bulbs where  manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 34W 840 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 3280 and design_lumens = 3280 and energy = 34 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 4000 and id > minid1); 
	update fixture_class set bulb_id = minid1 where bulb_id IN (select id from bulbs where  manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 34W 840 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 3280 and design_lumens = 3280 and energy = 34 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 4000 and id > minid1);

	--Delete the record
	delete from bulbs where id IN (select id from bulbs where  manufacturer = 'Philips' and bulb_name = 'EvoKit 2x2 P 32L 34W 840 5 Mk10 7 G2' and type = 'LED' and initial_lumens = 3280 and design_lumens = 3280 and energy = 34 and life_ins_start=70000 and life_prog_start=70000 and diameter is null and length = 2 and cri = 80 and color_temp = 4000 and id > minid1);
END;
$$
LANGUAGE plpgsql;



--Added by Chetan 29/11/2013

DROP TYPE fxtype_record;

CREATE TYPE fxtype_record AS (
fixture_id numeric,
ballastt_id numeric,
bulbb_id numeric,
no_of_f numeric,
voltagee numeric
 
);

ALTER TYPE public.fxtype_record OWNER TO postgres;

CREATE OR REPLACE FUNCTION fxtypecreate() RETURNS void
    AS $$
DECLARE 
fixturerec fxtype_record;
count numeric = 1;
fxtypeid numeric;
fxtypename character varying;
fxtypestatus numeric;

BEGIN
fxtypename = 'FxType';
FOR fixturerec IN (select f.id,f.ballast_id,f.bulb_id,f.no_of_fixtures,f.voltage from fixture f where f.state='COMMISSIONED' and f.fixture_class_id is NULL)
LOOP
	fxtypeid = 0;
	select fx.id from fixture_class fx where fx.ballast_id=fixturerec.ballastt_id and fx.bulb_id=fixturerec.bulbb_id and fx.voltage=fixturerec.voltagee and fx.no_of_ballasts=fixturerec.no_of_f INTO fxtypeid;
	IF (fxtypeid <> 0 and fxtypeid is NOT NULL)THEN
		--Apply the same fxclass to the fixture.	
		update fixture set fixture_class_id = fxtypeid where id = fixturerec.fixture_id;
	ELSE 
		fxtypename = 'FxType';
		--Create and apply the same fxclass to fixture.
		fxtypename = fxtypename || trim(to_char(count, '999'));
		count = count + 1;
		INSERT INTO fixture_class (id, name, voltage,no_of_ballasts,ballast_id,bulb_id) values ((select coalesce(max(id),0)+1 from fixture_class), fxtypename,fixturerec.voltagee,fixturerec.no_of_f,fixturerec.ballastt_id,fixturerec.bulbb_id);		
		select fx.id from fixture_class fx where fx.ballast_id=fixturerec.ballastt_id and fx.bulb_id=fixturerec.bulbb_id and fx.voltage=fixturerec.voltagee and fx.no_of_ballasts=fixturerec.no_of_f INTO fxtypeid;		
		update fixture set fixture_class_id = fxtypeid where id = fixturerec.fixture_id;
	END IF;
END LOOP;
END;
$$
LANGUAGE plpgsql;
Select fxtypecreate();

--Added by Chetan 03/12/2013
SELECT setval('fixture_class_seq', (SELECT max(id)+1 FROM fixture_class));

-- For Monitoring App
create table Fixture_Diagnostic_Reference(
   id bigint NOT NULL,
   fixture_id bigint NOT Null,
   hour_of_day int,
   power_used_average numeric(19,2),
   power_used_variance numeric(19,2),
   CONSTRAINT fixture_diagnostic_reference_pk PRIMARY KEY (id)
   );

   ALTER TABLE Fixture_Diagnostic_Reference OWNER TO postgres;
   ALTER TABLE ONLY public.Fixture_Diagnostic_Reference  
    ADD CONSTRAINT fixture_Diagnostic_reference_fixture_fk FOREIGN KEY(fixture_id) REFERENCES fixture(id);
    
CREATE SEQUENCE Fixture_Diagnostic_Reference_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE Fixture_Diagnostic_Reference_seq OWNER TO postgres;

create table Fixture_Diagnostics(
   id bigint NOT NULL,
   fixture_id bigint NOT NULL,
   fixture_diagnostic_reference_id bigint NOT NULL,
   energy_consumption_hourly_id bigint NOT NULL,
   state character varying(20),
   last_connectivity_at timestamp without time zone,
   power_used_status character varying,
   CONSTRAINT fixture_diagnostics_pk PRIMARY KEY (id)
   );

ALTER TABLE Fixture_Diagnostics OWNER TO postgres;
ALTER TABLE ONLY public.Fixture_Diagnostics  
    ADD CONSTRAINT fixture_Diagnostics_fixture_fk FOREIGN KEY(fixture_id) REFERENCES fixture(id);
ALTER TABLE ONLY public.Fixture_Diagnostics  
    ADD CONSTRAINT fixture_Diagnostics_reference_fk FOREIGN KEY(fixture_diagnostic_reference_id) REFERENCES Fixture_Diagnostic_Reference(id);

ALTER TABLE ONLY public.Fixture_Diagnostics DROP CONSTRAINT fixture_Diagnostics_ec_hourly_fk;

CREATE SEQUENCE Fixture_Diagnostics_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE Fixture_Diagnostics_seq OWNER TO postgres;

INSERT INTO cloud_config (id, name, val) values ((select coalesce(max(id),0)+1 from system_configuration), 'diagnostics.last_capture_at', '2013-01-01 00:00:00');

--Added by chetan 24/12/2013
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'uem.enable', '0');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'uem.ip', ' ');

--Added by chetan 20/1/2014
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'uem.secretkey', ' ');

CREATE OR REPLACE FUNCTION round_minutes( TIMESTAMP WITHOUT TIME ZONE, integer)
RETURNS TIMESTAMP WITHOUT TIME ZONE AS $$

	SELECT date_trunc('hour', $1) + (cast(($2::varchar||' min') as interval) * round( (date_part('minute',$1)::float + date_part('second',$1)/ 60.)::float / cast($2 as float)))

$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION floor_minutes( TIMESTAMP WITHOUT TIME ZONE, integer ) 
RETURNS TIMESTAMP WITHOUT TIME ZONE AS $$
    
SELECT round_minutes( $1 - cast((($2/2)::varchar ||' min') as interval ), $2 );

$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION ceiling_minutes( TIMESTAMP WITHOUT TIME ZONE, integer )
RETURNS TIMESTAMP WITHOUT TIME ZONE AS $$
    
	SELECT round_minutes( $1 + cast((($2/2)::varchar ||' min') as interval ), $2 );

$$ LANGUAGE SQL;

--ADDED BY SHARAD 14-01-14 - PROFILE Feature will be shown/hide depend on this flag
INSERT INTO system_configuration (id, name, value) VALUES ((select coalesce(max(id),0)+1 from system_configuration), 'enable.profilefeature', 'true'); 

--ADDED BY SAMPATH 19-01-15 - PLUGLOAD PROFILE Feature will be shown/hide depend on this flag
INSERT INTO system_configuration (id, name, value) VALUES ((select coalesce(max(id),0)+1 from system_configuration), 'enable.plugloadprofilefeature', 'true');

UPDATE system_configuration set value=true where name='enable.plugloadprofilefeature';
--Added by Chetan 16/01/2014
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'uem.apikey', ' ');

--Added by Sree 01/17/2015
ALTER TABLE fixture ADD COLUMN commissioned_time timestamp without time zone;

--Added by Yogesh 20/02/2014
ALTER TABLE fixture ADD COLUMN fixture_type integer DEFAULT 0;
ALTER TABLE ballasts ADD COLUMN ballast_type integer DEFAULT 0;
UPDATE fixture set fixture_type=0 where fixture_type is NULL;
UPDATE ballasts set ballast_type=0 where ballast_type is NULL;
UPDATE ballasts set ballast_name='Metered LED Driver' , manufacturer='Enlighted' , display_label='Metered LED Driver(Enlighted,LED,23W,2 bulb)' where display_label='Tomcat (enLighted,LED,23W,2 bulb)' and ballast_name='Tomcat' and manufacturer='Tomcat';
INSERT INTO ballasts (id, item_num, display_label, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer, ballast_type) VALUES ((select max(id)+1 from ballasts), 1, 'Metered LED Driver(Enlighted,LED,23W,2 bulb)', 'Metered LED Driver', '120-277', 'T8-LED', 2, 1.0, 23, 'Enlighted', 1);
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'enable.emergencyfx.calc', 'true');

--Added by Sampath 07/03/2014
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'floorplan.imagesize.limit', 2);



create table floor_zbupdate(
   id bigint NOT NULL,
   floor_id bigint NOT NULL,
   start_time timestamp without time zone,
   end_time timestamp without time zone,
   processed_state bigint,
   CONSTRAINT floor_zbupdate_pk PRIMARY KEY (id)
   );

ALTER TABLE floor_zbupdate OWNER TO postgres;
ALTER TABLE ONLY public.floor_zbupdate  
    ADD CONSTRAINT floor_zbupdate_floor_fk FOREIGN KEY(floor_id) REFERENCES floor(id);

    
CREATE SEQUENCE floor_zbupdate_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE floor_zbupdate_seq OWNER TO postgres;

--Added by Yogesh (support for tx/rx motion groups)
CREATE TABLE motion_group_fixture_details
(
  id bigint NOT NULL,
  gems_group_fixture_id bigint,
  type smallint default 3,
  ambient_type smallint default 0,
  use_em_values smallint default 0,
  lo_amb_level smallint default 0,
  hi_amb_level smallint default 0,
  time_of_day int,
  light_level smallint default 0,
  CONSTRAINT motion_group_fixture_details_pkey PRIMARY KEY (id),
  CONSTRAINT fk_motion_group_fx_details_id FOREIGN KEY (gems_group_fixture_id)
      REFERENCES gems_group_fixture (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);

ALTER TABLE public.motion_group_fixture_details OWNER TO postgres;

CREATE SEQUENCE motion_group_fixture_details_seq
   INCREMENT 1
   START 1;
ALTER TABLE motion_group_fixture_details_seq OWNER TO postgres;

--Added by Yogesh
--Only update the initial scene active time for the already existing switch groups, so that the checksum match
--Decision ceriteria is extend_scene_active_time column will not be yet present so this can be done.
CREATE OR REPLACE FUNCTION updateInitialSceneActiveTimeForSwitchgrp() RETURNS void
    AS $$
DECLARE         
        isColumnExists boolean;
BEGIN
	isColumnExists = column_exists('extend_scene_active_time','switch');
	IF isColumnExists = 'f' THEN
		ALTER TABLE switch ADD COLUMN extend_scene_active_time integer default 0;
		UPDATE switch set extend_scene_active_time = 15; 
		UPDATE switch set initial_scene_active_time = 30 where initial_scene_active_time = 60;
	END IF;
END
$$
LANGUAGE plpgsql;
select updateInitialSceneActiveTimeForSwitchgrp();

ALTER TABLE switch ALTER COLUMN initial_scene_active_time SET DEFAULT 0;


INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'switch.initial_scene_active_time', 0);
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'switch.extend_scene_active_time', 0);

-- Added by Yogesh to support bulk configuration of group
CREATE TABLE scene_templates (
    id bigint,
    name character varying,
    CONSTRAINT scene_templates_pk PRIMARY KEY (id),  
    CONSTRAINT unique_scene_templates_name UNIQUE (name)
);

ALTER TABLE public.scene_templates OWNER TO postgres;

CREATE SEQUENCE scene_templates_seq
   INCREMENT 1
   START 1;
ALTER TABLE scene_templates_seq OWNER TO postgres;


CREATE TABLE scene_light_levels_templates (
    id bigint,
    scene_template_id bigint,
    name character varying,
    lightlevel integer,
    scene_order integer,
    CONSTRAINT scene_light_levels_templates_pk PRIMARY KEY (id),
    CONSTRAINT scene_template_id_fk FOREIGN KEY(scene_template_id) REFERENCES scene_templates(id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
    CONSTRAINT unique_scene_light_levels_templates_id UNIQUE (scene_template_id, lightlevel)
);

ALTER TABLE public.scene_light_levels_templates OWNER TO postgres;

CREATE SEQUENCE scene_light_levels_templates_seq
   INCREMENT 1
   START 1;
ALTER TABLE scene_light_levels_templates_seq OWNER TO postgres;

insert into scene_templates values (nextval('scene_templates_seq'), 'Template_20_40_60');
insert into scene_light_levels_templates values (nextval('scene_light_levels_templates_seq'), 1, 'TM_All_20', 20, 0), (nextval('scene_light_levels_templates_seq'), 1, 'TM_All_40', 40, 1), (nextval('scene_light_levels_templates_seq'), 1, 'TM_All_60', 60, 2);

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'uem.pkt.forwarding.enable', '0');

--Added by chetan 16/04/2014
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'enable.pricing', '1');

--Sharad 05/02/14
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'add.more.defaultprofile', 'true');

--Chetan 26/05/2014
CREATE TABLE events_and_fault_history as select * from events_and_fault where 1=0;
INSERT INTO system_configuration (id, name, value) 
values ((select coalesce(max(id),0)+1 from system_configuration),'db_pruning.events_and_fault_history_table', '365');


--Drop energy consumption non used table indexes
drop index energy_consumption_capture_at_index;
drop index energy_consumption_fixture_id_index;
drop index energy_consumption_power_used_index;

drop index energy_consumption_hourly_capture_at_index;
drop index energy_consumption_hourly_fixture_id_index;
drop index energy_consumption_hourly_power_used_index;

drop index energy_consumption_daily_capture_at_index;
drop index energy_consumption_daily_fixture_id_index;
drop index energy_consumption_daily_power_used_index;

DROP TABLE system_energy_consumption;
DROP TABLE system_energy_consumption_hourly;
DROP TABLE system_energy_consumption_daily;

alter table ballast_volt_power drop CONSTRAINT ballast_volt_power_volt_power_map_id_key;
alter table ballast_volt_power drop CONSTRAINT ballast_volt_power_volt_power_map_id_volt_key;
alter table ballast_volt_power drop CONSTRAINT unique_voltpowermap_in_ballast_volt_power;
drop index ballast_volt_power_volt_power_map_id_key;
drop index ballast_volt_power_volt_power_map_id_volt_key;
drop index unique_voltpowermap_in_ballast_volt_power;

CREATE INDEX events_time_index ON events_and_fault USING btree (event_time);
CREATE INDEX events_device_index ON events_and_fault USING btree (device_id);

--Sachin 17/06/2014
CREATE UNIQUE INDEX floor_zb_indx on floor_zbupdate(floor_id,processed_state) WHERE processed_state=0;

--Sachin 19/06/2014
ALTER TABLE wds ADD COLUMN volt_capture_at timestamp without time zone;
ALTER TABLE wds ADD COLUMN battery_volt integer DEFAULT NULL;
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'wds.normal.level.min', '2700');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'wds.low.level.min', '2100');

--sharad 11/14 - Restore "Default" profile name (i.e. Profile No = 1 ) to "Default" if it has been changed by any chance.
-- If default profile (i.e Profile No = 1) has been changed, it creates issues in DISCOVERY/COMMISSIONING Process.
CREATE OR REPLACE FUNCTION restore_default_profile_name() RETURNS void
    AS $$
DECLARE         
        isColumnExists boolean;
		grpName character varying;
BEGIN
	isColumnExists = column_exists('profile_no','groups');
	IF isColumnExists = 't' THEN
		select g.name from groups g where g.profile_no=1  and profile_handler_id =1 and derived_from_group is NULL INTO grpName;
		--Raise Notice 'name%',grpName;
		IF (grpName is NOT NULL and grpName <> 'Default')THEN
			UPDATE groups set name = 'Default' where profile_no=1 and profile_handler_id =1 and derived_from_group is NULL; 
		END IF;
	END IF;
END
$$
LANGUAGE plpgsql;
Select restore_default_profile_name();

ALTER TABLE fixture ALTER COLUMN avg_temperature TYPE numeric(7,2);

-- request by sreedhar on 4/21/2015 11:36 AM

DROP FUNCTION IF EXISTS avg_temp_alter(VARCHAR, VARCHAR , VARCHAR);
CREATE OR REPLACE FUNCTION avg_temp_alter(cmd VARCHAR, tablename VARCHAR , columnname VARCHAR)
RETURNS bool AS
$$
DECLARE
          querytxt text;
          onerow record;
BEGIN 
	  querytxt = 'select table_name, column_name, data_type, numeric_precision , numeric_scale from information_schema.columns where table_name=' || '''' || tablename || '''' || ' and column_name=' || '''' || columnname || '''' ;
	  For onerow IN execute querytxt
	  LOOP
		  if ( onerow.data_type <> 'numeric' ) OR ( onerow.numeric_precision <> '7' ) OR (  onerow.numeric_scale <> '2' )
		  then
			execute cmd;
			return true;
		  end if;
		  return false;

	  END LOOP;
END;
$$
LANGUAGE plpgsql;
select avg_temp_alter('ALTER TABLE energy_consumption ALTER COLUMN avg_temperature TYPE numeric(7,2);', 'energy_consumption', 'avg_temperature');
select avg_temp_alter('ALTER TABLE energy_consumption_hourly ALTER COLUMN avg_temperature TYPE numeric(7,2);', 'energy_consumption_hourly', 'avg_temperature');
select avg_temp_alter('ALTER TABLE energy_consumption_daily ALTER COLUMN avg_temperature TYPE numeric(7,2);', 'energy_consumption_daily', 'avg_temperature');

ALTER TABLE fixture ADD COLUMN current_ambient_val integer DEFAULT 0;
ALTER TABLE fixture ADD COLUMN manual_ambient_val integer DEFAULT -1;
UPDATE fixture set current_ambient_val = 0 where current_ambient_val is null;
UPDATE fixture set manual_ambient_val = -1 where manual_ambient_val is null;

--Sree image upgrade changes
CREATE TABLE firmware_upgrade_schedule (
  id bigint NOT NULL,
  file_name character varying(100),
  device_type character varying(25),
  model_no character varying(50),
  version character varying(20),
  added_time timestamp without time zone,
  scheduled_time timestamp without time zone,
  start_time timestamp without time zone,
  duration integer,
  on_reboot boolean,
  retries integer,
  retry_interval integer,
  include_list character varying,
  exclude_list character varying,
  active boolean,
  description character varying,
  job_prefix character varying,
  CONSTRAINT firmware_upgrade_schedule_pkey PRIMARY KEY(id)
);

ALTER TABLE firmware_upgrade_schedule OWNER TO postgres;

CREATE SEQUENCE firmware_upgrade_schedule_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE firmware_upgrade_schedule_seq OWNER TO postgres;

ALTER TABLE firmware_upgrade_schedule ADD COLUMN job_prefix character varying;
ALTER TABLE image_upgrade_job ADD COLUMN retry_interval integer DEFAULT 30;

CREATE OR REPLACE FUNCTION fixture_update_trigger() RETURNS "trigger" AS $$
	BEGIN
	  IF tg_op = 'UPDATE' THEN
		IF old.cu_version <> new.cu_version THEN
			if new.cu_version = '0' then
				insert into events_and_fault (id, event_time, severity, event_type, description, active, device_id) values (nextval('events_seq'), current_timestamp, 'Critical', 'Fixture CU Failure', 'CU Failure', true, new.id);
			end if;
			if old.cu_version = '0' then
				update events_and_fault set resolved_on = current_timestamp, active = false where device_id = new.id and event_type = 'Fixture CU Failure';
			end if;
		END IF;
	  END IF;
	  RETURN new;
	END
$$ LANGUAGE plpgsql;

CREATE TRIGGER fixture_update AFTER UPDATE ON fixture FOR EACH ROW EXECUTE PROCEDURE fixture_update_trigger();

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'show.cu.failure.in.outage.report', 'false');

INSERT INTO system_configuration (id, name, value) VALUES ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.events_and_fault_table_records', '50000');

INSERT INTO system_configuration (id, name, value) VALUES ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.events_and_fault_table_cleanup_limit', '100000');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'mobile.apikey', (select value from system_configuration where name like 'uem.apikey'));

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'mobile.secretkey', (select value from system_configuration where name like 'uem.secretkey'));

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'em.forcepasswordexpiry', 'false');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'default.replayAttackTimeInmillis', '225');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'flag.ems.apply.validation', 'true');

create table sync_tasks (
  last_wal_id bigint,
  file_name character varying (100),
  creation_time timestamp without time zone,
  action_type character varying (20),
  failed_attemps integer default 0,
  checksum character varying(40),
  status character varying(20),
  CONSTRAINT sync_tasks_pk PRIMARY KEY(last_wal_id, file_name)
);

CREATE INDEX sync_tasks_status_index ON sync_tasks USING btree (status);

-- plugload tables

CREATE TABLE plugload
(
  id bigint NOT NULL,
  profile_id bigint,
  managed_load real,
  unmanaged_load real,
  profile_handler_id bigint,
  current_profile character varying(255),
  original_profile_from character varying(255),
  current_state character varying,
  last_occupancy_seen integer,
  snap_address character varying(20),
  gateway_id bigint,
  description character varying,
  notes character varying,
  active boolean,
  state character varying,
  last_connectivity_at timestamp without time zone,
  global_profile_checksum smallint,
  curr_app smallint,
  firmware_version character varying(20),
  bootloader_version character varying(20),
  group_id bigint,
  sec_gw_id bigint DEFAULT 1,
  upgrade_status character varying(20),
  push_profile boolean DEFAULT false,
  push_global_profile boolean DEFAULT false,
  last_cmd_status character varying(20),
  managed_baseline_load numeric(19,2) DEFAULT 0,
  unmanaged_baseline_load numeric(19,2) DEFAULT 0,
  voltage smallint DEFAULT 277,
  commission_status integer DEFAULT 0,
  is_hopper integer DEFAULT 0,
  version_synced integer DEFAULT 0,
  temperature_offset real,
  last_boot_time timestamp without time zone,
  cu_version character varying(20),
  current_data_id bigint,
  reset_reason smallint,
  scheduled_profile_checksum integer,
  config_checksum integer,
  groups_sync_pending boolean DEFAULT false,
  commissioned_time timestamp without time zone,
  avg_temperature real,
  avg_volts real,  
  CONSTRAINT plugload_pkey PRIMARY KEY (id),
  CONSTRAINT plugload_gateway_id_fkey FOREIGN KEY (gateway_id)
      REFERENCES gateway (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE plugload
  OWNER TO postgres;


CREATE TABLE plugload_profile
(
  id bigint NOT NULL,
  active_motion_window integer,
  mode smallint,
  manual_override_time integer,
  CONSTRAINT plugload_profile_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE plugload_profile
  OWNER TO postgres;

CREATE TABLE plugload_profile_configuration
(
  id bigint NOT NULL,
  morning_time character varying,
  day_time character varying,
  evening_time character varying,
  night_time character varying,
  CONSTRAINT plugload_profile_configuration_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE plugload_profile_configuration
  OWNER TO postgres;


CREATE TABLE plugload_profile_template
(
  id bigint NOT NULL,
  name character varying(255),
  display_template boolean DEFAULT true,
  template_no bigint,
  CONSTRAINT plugload_profile_template_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE plugload_profile_template
  OWNER TO postgres;

CREATE TABLE plugload_profile_handler
(
  id bigint NOT NULL,
  morning_profile_id bigint,
  day_profile_id bigint,
  evening_profile_id bigint,
  night_profile_id bigint,
  morning_profile_weekend bigint,
  day_profile_weekend bigint,
  evening_profile_weekend bigint,
  night_profile_weekend bigint,
  morning_profile_holiday bigint,
  day_profile_holiday bigint,
  evening_profile_holiday bigint,
  night_profile_holiday bigint,
  profile_configuration_id bigint,
  profile_checksum smallint,
  global_profile_checksum smallint,
  standalone_motion_override smallint DEFAULT 0,
  dr_reactivity smallint DEFAULT 0,
  profile_group_id smallint DEFAULT 1,
  initial_on_time integer DEFAULT 5,
  profile_flag smallint DEFAULT 0,
  initial_on_level integer DEFAULT 50,
  dr_low_level smallint DEFAULT 0,
  dr_moderate_level smallint DEFAULT 0,
  dr_high_level smallint DEFAULT 0,
  dr_special_level smallint DEFAULT 0,
  heartbeat_interval bigint,
  heartbeat_linger_period bigint,
  no_of_missed_heartbeats bigint,
  safety_mode smallint,
  CONSTRAINT plugload_profile_handler_pkey PRIMARY KEY (id),
  CONSTRAINT plugload_profile_handler_day_profile_holiday_fkey FOREIGN KEY (day_profile_holiday)
      REFERENCES plugload_profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_profile_handler_day_profile_id_fkey FOREIGN KEY (day_profile_id)
      REFERENCES plugload_profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_profile_handler_day_profile_weekend_fkey FOREIGN KEY (day_profile_weekend)
      REFERENCES plugload_profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_profile_handler_evening_profile_holiday_fkey FOREIGN KEY (evening_profile_holiday)
      REFERENCES plugload_profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_profile_handler_evening_profile_id_fkey FOREIGN KEY (evening_profile_id)
      REFERENCES plugload_profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_profile_handler_evening_profile_weekend_fkey FOREIGN KEY (evening_profile_weekend)
      REFERENCES plugload_profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_profile_handler_morning_profile_holiday_fkey FOREIGN KEY (morning_profile_holiday)
      REFERENCES plugload_profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_profile_handler_morning_profile_id_fkey FOREIGN KEY (morning_profile_id)
      REFERENCES plugload_profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_profile_handler_morning_profile_weekend_fkey FOREIGN KEY (morning_profile_weekend)
      REFERENCES plugload_profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_profile_handler_night_profile_holiday_fkey FOREIGN KEY (night_profile_holiday)
      REFERENCES plugload_profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_profile_handler_night_profile_id_fkey FOREIGN KEY (night_profile_id)
      REFERENCES plugload_profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_profile_handler_night_profile_weekend_fkey FOREIGN KEY (night_profile_weekend)
      REFERENCES plugload_profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_profile_handler_profile_configuration_id_fkey FOREIGN KEY (profile_configuration_id)
      REFERENCES plugload_profile_configuration (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE plugload_profile_handler
  OWNER TO postgres;


CREATE TABLE plugload_groups
(
  id bigint NOT NULL,
  name character varying(255),
  company_id bigint,
  profile_handler_id bigint,
  profile_no smallint,
  derived_from_group bigint,
  tenant_id bigint,
  template_id bigint,
  display_profile boolean DEFAULT true,
  default_profile boolean DEFAULT true,
  CONSTRAINT plugload_groups_pkey PRIMARY KEY (id),
  CONSTRAINT plugload_groups_company_id_fkey FOREIGN KEY (company_id)
      REFERENCES company (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_groups_derived_from_group_fkey FOREIGN KEY (derived_from_group)
      REFERENCES plugload_groups (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_groups_profile_handler_id_fkey FOREIGN KEY (profile_handler_id)
      REFERENCES plugload_profile_handler (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_groups_template_id_fkey FOREIGN KEY (template_id)
      REFERENCES plugload_profile_template (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_groups_tenant_id_fkey FOREIGN KEY (tenant_id)
      REFERENCES tenants (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE plugload_groups
  OWNER TO postgres;


CREATE TABLE weekday_plugload
(
  id bigint NOT NULL,
  day character varying(255),
  plugload_profile_configuration_id bigint,
  short_order integer,
  type character varying,
  CONSTRAINT weekday_plugload_pkey PRIMARY KEY (id),
  CONSTRAINT fk49206f28971b5d7c FOREIGN KEY (plugload_profile_configuration_id)
      REFERENCES plugload_profile_configuration (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE weekday_plugload
  OWNER TO postgres;



CREATE INDEX plugload_profile_config
  ON weekday_plugload
  USING btree
  (plugload_profile_configuration_id);


CREATE TABLE plugload_energy_consumption
(
  id bigint NOT NULL,
  capture_at timestamp without time zone,
  plugload_id bigint,
  min_temperature numeric(5,1),
  max_temperature numeric(5,1),
  avg_temperature numeric(5,1),
  last_temperature numeric(5,1),
  min_volts smallint,
  max_volts smallint,
  avg_volts numeric(6,2),
  last_volts smallint,
  managed_on_secs smallint,
  managed_on_to_off_sec smallint,
  managed_off_to_on_sec smallint,
  last_motion_secs_ago integer,
  curr_state smallint,
  curr_behavior smallint,
  no_of_load_changes smallint,
  no_of_peers_heard_from smallint,
  cost double precision,
  price double precision,
  base_cost double precision,
  saved_cost double precision,
  base_energy numeric(19,2),
  energy numeric(19,2),
  saved_energy numeric(19,2),
  saving_type smallint,
  occ_saving numeric(19,2),
  tuneup_saving numeric(19,2),
  manual_saving numeric(19,2),
  managed_energy_cum bigint,
  base_unmanaged_energy numeric(19,2),
  unmanaged_energy numeric(19, 2),
  saved_unmanaged_energy numeric(19,2),
  unmanaged_energy_cum numeric(19,2),
  managed_current numeric(19,2),
  managed_last_load numeric(19,2),
  managed_power_factor numeric(19,2),
  unmanaged_current numeric(19,2),
  unmanaged_last_load numeric(19,2),
  unmanaged_power_factor numeric(19,2),
  zero_bucket smallint,
  cu_cmd_status integer,
  no_of_cu_resets integer,
  cu_status integer,
  sys_uptime bigint,
  current_app smallint,
  CONSTRAINT plugload_energy_consumption_pkey PRIMARY KEY (id),
  CONSTRAINT plugload_energy_consumption_plugload_id_fkey FOREIGN KEY (plugload_id)
      REFERENCES plugload (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_energy_consumption_capture_at_plugload_id_key UNIQUE (capture_at, plugload_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE plugload_energy_consumption
  OWNER TO postgres;

  CREATE TABLE plugload_energy_consumption_hourly
(
  id bigint NOT NULL,
  capture_at timestamp without time zone,
  plugload_id bigint,
  min_temperature numeric(5,1),
  max_temperature numeric(5,1),
  avg_temperature numeric(5,1),
  last_temperature numeric(5,1),
  min_volts smallint,
  max_volts smallint,
  avg_volts numeric(6,2),
  last_volts smallint,
  managed_on_secs smallint,
  managed_on_to_off_sec smallint,
  managed_off_to_on_sec smallint,
  last_motion_secs_ago integer,
  curr_state smallint,
  curr_behavior smallint,
  no_of_load_changes smallint,
  no_of_peers_heard_from smallint,
  cost double precision,
  price double precision,
  base_cost double precision,
  saved_cost double precision,
  base_energy numeric(19,2),
  energy numeric(19,2),
  saved_energy numeric(19,2),
  saving_type smallint,
  occ_saving numeric(19,2),
  tuneup_saving numeric(19,2),
  manual_saving numeric(19,2),
  managed_energy_cum bigint,
  base_unmanaged_energy numeric(19,2),
  unmanaged_energy numeric(19, 2),
  saved_unmanaged_energy numeric(19,2),
  unmanaged_energy_cum numeric(19,2),
  managed_current numeric(19,2),
  managed_last_load numeric(19,2),
  managed_power_factor numeric(19,2),
  unmanaged_current numeric(19,2),
  unmanaged_last_load numeric(19,2),
  unmanaged_power_factor numeric(19,2),
  zero_bucket smallint,
  cu_cmd_status integer,
  no_of_cu_resets integer,
  cu_status integer,
  sys_uptime bigint,
  current_app smallint,
  CONSTRAINT plugload_energy_consumption_hourly_pkey PRIMARY KEY (id),
  CONSTRAINT plugload_energy_consumption_hourly_plugload_id_fkey FOREIGN KEY (plugload_id)
      REFERENCES plugload (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_energy_consumption_hourly_capture_at_plugload_id_key UNIQUE (capture_at, plugload_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE plugload_energy_consumption_hourly
  OWNER TO postgres;
  
  
 CREATE TABLE plugload_energy_consumption_daily
(
  id bigint NOT NULL,
  capture_at timestamp without time zone,
  plugload_id bigint,
  min_temperature numeric(5,1),
  max_temperature numeric(5,1),
  avg_temperature numeric(5,1),
  last_temperature numeric(5,1),
  min_volts smallint,
  max_volts smallint,
  avg_volts numeric(6,2),
  last_volts smallint,
  managed_on_secs smallint,
  managed_on_to_off_sec smallint,
  managed_off_to_on_sec smallint,
  last_motion_secs_ago integer,
  curr_state smallint,
  curr_behavior smallint,
  no_of_load_changes smallint,
  no_of_peers_heard_from smallint,
  cost double precision,
  price double precision,
  base_cost double precision,
  saved_cost double precision,
  base_energy numeric(19,2),
  energy numeric(19,2),
  saved_energy numeric(19,2),
  saving_type smallint,
  occ_saving numeric(19,2),
  tuneup_saving numeric(19,2),
  manual_saving numeric(19,2),
  managed_energy_cum bigint,
  base_unmanaged_energy numeric(19,2),
  unmanaged_energy numeric(19, 2),
  saved_unmanaged_energy numeric(19,2),
  unmanaged_energy_cum numeric(19,2),
  managed_current numeric(19,2),
  managed_last_load numeric(19,2),
  managed_power_factor numeric(19,2),
  unmanaged_current numeric(19,2),
  unmanaged_last_load numeric(19,2),
  unmanaged_power_factor numeric(19,2),
  zero_bucket smallint,
  cu_cmd_status integer,
  no_of_cu_resets integer,
  cu_status integer,
  sys_uptime bigint,
  current_app smallint,
  CONSTRAINT plugload_energy_consumption_daily_pkey PRIMARY KEY (id),
  CONSTRAINT plugload_energy_consumption_daily_plugload_id_fkey FOREIGN KEY (plugload_id)
      REFERENCES plugload (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT plugload_energy_consumption_daily_capture_at_plugload_id_key UNIQUE (capture_at, plugload_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE plugload_energy_consumption_daily
  OWNER TO postgres;

CREATE TABLE gems_group_plugload
(
  id bigint NOT NULL,
  group_id bigint NOT NULL,
  plugload_id bigint,
  need_sync bigint,
  user_action bigint,
  CONSTRAINT gems_group_plugload_pkey PRIMARY KEY (id),
  CONSTRAINT gems_group_plugload_group_id_fkey FOREIGN KEY (group_id)
      REFERENCES gems_groups (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT gems_group_plugload_plugload_id_fkey FOREIGN KEY (plugload_id)
      REFERENCES plugload (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT gems_group_plugload_plugload_id_group_id_key UNIQUE (plugload_id, group_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE gems_group_plugload
  OWNER TO postgres;

ALTER TABLE plugload ADD COLUMN last_stats_rcvd_time  timestamp without time zone;  

ALTER TABLE plugload ADD COLUMN last_zb_update_time  timestamp without time zone;  

ALTER TABLE plugload ADD COLUMN pre_defined_baseline_load numeric(19,2) default 0;

CREATE TABLE plugload_zb
(
  id bigint NOT NULL,
  plugload_id numeric,
  last_zb_update_time timestamp without time zone,
  CONSTRAINT plugload_zb_plugload_id_key UNIQUE (plugload_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE plugload_zb
  OWNER TO postgres;
  
CREATE SEQUENCE plugload_zb_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE plugload_zb_seq
  OWNER TO postgres;

  
CREATE SEQUENCE plugload_profile_template_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 17
  CACHE 1;
ALTER TABLE plugload_profile_template_seq
  OWNER TO postgres;

  CREATE SEQUENCE gems_group_plugload_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 17
  CACHE 1;
ALTER TABLE gems_group_plugload_seq
  OWNER TO postgres;

    CREATE SEQUENCE energy_consumption_plugload_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 17
  CACHE 1;
ALTER TABLE energy_consumption_plugload_seq
  OWNER TO postgres;
  
  CREATE SEQUENCE plugload_energy_consumption_hourly_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 17
  CACHE 1;
ALTER TABLE plugload_energy_consumption_hourly_seq
  OWNER TO postgres;
  
  CREATE SEQUENCE plugload_energy_consumption_daily_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 17
  CACHE 1;
ALTER TABLE plugload_energy_consumption_daily_seq
  OWNER TO postgres;

  CREATE SEQUENCE plugload_profile_configuration_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 17
  CACHE 1;
ALTER TABLE plugload_profile_configuration_seq
  OWNER TO postgres;

   CREATE SEQUENCE weekday_plugload_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 17
  CACHE 1;
ALTER TABLE weekday_plugload_seq
  OWNER TO postgres;

  CREATE SEQUENCE plugload_profile_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 17
  CACHE 1;
ALTER TABLE plugload_profile_seq
  OWNER TO postgres;

  CREATE SEQUENCE plugload_profile_handler_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 17
  CACHE 1;
ALTER TABLE plugload_profile_handler_seq
  OWNER TO postgres;

  CREATE SEQUENCE plugload_group_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 17
  CACHE 1;
ALTER TABLE plugload_group_seq
  OWNER TO postgres;
  
  
insert into cloud_config (id, name, val) values ((select coalesce(max(id),0)+1 from cloud_config), 'remigration.required', '0');

insert into cloud_config (id, name, val) values ((select coalesce(max(id),0)+1 from cloud_config), 'successful.sync.time', '-1');

-- sharad 10/12/14 - Adding New Ballasts and Bulb to support Philips DALI LED Drivers 

INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer, display_label) VALUES ((select coalesce(max(id),0)+1 from ballasts), NULL, 'Xitanium SR' , '120-277', 'LED', 1, 1.00, 40, 'Philips', 'Xitanium SR(Philips,LED,40W,1 bulb)');
--DALI 1st bulb
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter","length", "cri", "color_temp") VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'EvoKit 2x4 P 42L 39W 835 2 0-10 7 G2' ,  'LED', 4180, 4180, 39, 70000, 70000, NULL, 4, 80, 3500);

--DALI 2nd bulb
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter","length", "cri", "color_temp") VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'EvoKit 2x4 P 42L 39W 840 2 0-10 7 G2' ,  'LED', 4280, 4280, 39, 70000, 70000, NULL, 4, 80, 4000);

--DALI 3rd bulb
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter","length", "cri", "color_temp") VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'EvoKit 2x4 P 42L 40W 835 1 Mk10 7 G2' ,  'LED', 4180, 4180, 40, 70000, 70000, NULL, 4, 80, 3500);

--DALI 4th bulb
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter","length", "cri", "color_temp") VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'EvoKit 2x4 P 42L 40W 840 1 Mk10 7 G2' ,  'LED', 4280, 4280, 40, 70000, 70000, NULL, 4, 80, 4000);

--DALI 5th bulb
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter","length", "cri", "color_temp") VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'EvoKit 2x4 P 42L 42W 835 5 Mk10 7 G2' ,  'LED', 4180, 4180, 42, 70000, 70000, NULL, 4, 80, 3500	);

--DALI 6th bulb
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter","length", "cri", "color_temp") VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'EvoKit 2x4 P 42L 42W 840 5 Mk10 7 G2' ,  'LED', 4280, 4280, 42, 70000, 70000, NULL, 4, 80, 4000	);

--DALI 7th bulb
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter","length", "cri", "color_temp") VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'EvoKit 2x2 P 32L 31W 835 2 0-10 7 G2' ,  'LED', 3210, 3210, 31, 70000, 70000, NULL, 2, 80, 3500	);

--DALI 8th bulb
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter","length", "cri", "color_temp") VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'EvoKit 2x2 P 32L 31W 840 2 0-10 7 G2' ,  'LED', 3280, 3280, 31, 70000, 70000, NULL, 2, 80, 4000	);

--DALI 9th bulb
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter","length", "cri", "color_temp") VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'EvoKit 2x2 P 32L 32W 835 1 Mk10 7 G2' ,  'LED', 3210, 3210, 32, 70000, 70000, NULL, 2, 80, 3500	);

--DALI 10th bulb
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter","length", "cri", "color_temp") VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'EvoKit 2x2 P 32L 32W 840 1 Mk10 7 G2' ,  'LED', 3280, 3280, 32, 70000, 70000, NULL, 2, 80, 4000	);

--DALI 11th bulb
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter","length", "cri", "color_temp") VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'EvoKit 2x2 P 32L 34W 835 5 Mk10 7 G2' ,  'LED', 3210, 3210, 34, 70000, 70000, NULL, 2, 80, 3500	);

--DALI 12th bulb
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter","length", "cri", "color_temp") VALUES ((select coalesce(max(id), 0) + 1 from bulbs), 'Philips', 'EvoKit 2x2 P 32L 34W 840 5 Mk10 7 G2' ,  'LED', 3280, 3280, 34, 70000, 70000, NULL, 2, 80, 4000	);


-- BELOW STORED PROC NEED TO BE PLACED AT LAST AND ABOVE update_all_sequences()
Select remove_duplicate_bulbs_from_upgradescript();

--Sree 12/23
ALTER TABLE gateway ADD COLUMN no_of_plugloads integer DEFAULT 0;

ALTER TABLE gateway ADD CONSTRAINT unique_gateway_ip_address UNIQUE (ip_address);

ALTER TABLE profile_handler ADD COLUMN holiday_level smallint DEFAULT 0;

ALTER TABLE profile_handler ADD COLUMN override5 bigint;
ALTER TABLE ONLY profile_handler ADD CONSTRAINT override5_profile_fk FOREIGN KEY(override5) REFERENCES profile(id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE profile_handler ADD COLUMN override6 bigint;
ALTER TABLE ONLY profile_handler ADD CONSTRAINT override6_profile_fk FOREIGN KEY(override6) REFERENCES profile(id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE profile_handler ADD COLUMN override7 bigint;
ALTER TABLE ONLY profile_handler ADD CONSTRAINT override7_profile_fk FOREIGN KEY(override7) REFERENCES profile(id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE profile_handler ADD COLUMN override8 bigint;
ALTER TABLE ONLY profile_handler ADD CONSTRAINT override8_profile_fk FOREIGN KEY(override8) REFERENCES profile(id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

CREATE OR REPLACE FUNCTION add_overrides() RETURNS void
    AS $$
DECLARE 
	ph_id bigint;
    profile_id bigint;
    override_id bigint;
BEGIN
	FOR ph_id IN (SELECT id FROM profile_handler ORDER BY id)
	LOOP
      override_id:=null;
      select override5 into override_id from profile_handler where id = ph_id;
      if (override_id is null)
      then
          select nextval('profile_seq') into profile_id;
          insert into profile (id, min_level, on_level, motion_detect_duration, manual_override_duration, motion_sensitivity, ramp_up_time, ambient_sensitivity)
            values (profile_id, 0, 0, 1, 60, 10, 0, 10);
          update profile_handler set override5 = profile_id where id = ph_id;
      end if;

      override_id:=null;
      select override6 into override_id from profile_handler where id = ph_id;
      if (override_id is null)
      then
          select nextval('profile_seq') into profile_id;
          insert into profile (id, min_level, on_level, motion_detect_duration, manual_override_duration, motion_sensitivity, ramp_up_time, ambient_sensitivity)
            values (profile_id, 0, 50, 1, 60, 10, 0, 10);
          update profile_handler set override6 = profile_id where id = ph_id;
      end if;

      override_id:=null;
      select override7 into override_id from profile_handler where id = ph_id;
      if (override_id is null)
      then
          select nextval('profile_seq') into profile_id;
          insert into profile (id, min_level, on_level, motion_detect_duration, manual_override_duration, motion_sensitivity, ramp_up_time, ambient_sensitivity)
            values (profile_id, 0, 50, 1, 60, 10, 0, 10);
          update profile_handler set override7 = profile_id where id = ph_id;
      end if;

      override_id:=null;
      select override8 into override_id from profile_handler where id = ph_id;
      if (override_id is null)
      then
          select nextval('profile_seq') into profile_id;
          insert into profile (id, min_level, on_level, motion_detect_duration, manual_override_duration, motion_sensitivity, ramp_up_time, ambient_sensitivity)
            values (profile_id, 0, 50, 1, 60, 10, 0, 10);
          update profile_handler set override8 = profile_id where id = ph_id;
      end if;

	END LOOP;

END;
$$
LANGUAGE plpgsql;

select add_overrides();


ALTER TABLE plugload_profile_handler ADD COLUMN holiday_level smallint DEFAULT 0;

ALTER TABLE plugload_profile_handler ADD COLUMN override5 bigint;
ALTER TABLE ONLY plugload_profile_handler ADD CONSTRAINT override5_plugload_profile_fk FOREIGN KEY(override5) REFERENCES plugload_profile(id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE plugload_profile_handler ADD COLUMN override6 bigint;
ALTER TABLE ONLY plugload_profile_handler ADD CONSTRAINT override6_plugload_profile_fk FOREIGN KEY(override6) REFERENCES plugload_profile(id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE plugload_profile_handler ADD COLUMN override7 bigint;
ALTER TABLE ONLY plugload_profile_handler ADD CONSTRAINT override7_plugload_profile_fk FOREIGN KEY(override7) REFERENCES plugload_profile(id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE plugload_profile_handler ADD COLUMN override8 bigint;
ALTER TABLE ONLY plugload_profile_handler ADD CONSTRAINT override8_plugload_profile_fk FOREIGN KEY(override8) REFERENCES plugload_profile(id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

CREATE OR REPLACE FUNCTION add_plugload_overrides() RETURNS void
AS $$
DECLARE 
ph_id bigint;
profile_id bigint;
override_id bigint;
BEGIN
FOR ph_id IN (SELECT id FROM plugload_profile_handler)
LOOP
  override_id:=null;
  select override5 into override_id from plugload_profile_handler where id = ph_id;
  if (override_id is null)
  then
      select nextval('plugload_profile_seq') into profile_id;
      insert into plugload_profile (id,active_motion_window,mode,manual_override_time)
        values (profile_id, 30, 1, 60);
      update plugload_profile_handler set override5 = profile_id where id = ph_id;
  end if;

  override_id:=null;
  select override6 into override_id from plugload_profile_handler where id = ph_id;
  if (override_id is null)
  then
      select nextval('plugload_profile_seq') into profile_id;
	  insert into plugload_profile (id,active_motion_window,mode,manual_override_time)
  		values (profile_id, 30, 1, 60);
      update plugload_profile_handler set override6 = profile_id where id = ph_id;
  end if;

  override_id:=null;
  select override7 into override_id from plugload_profile_handler where id = ph_id;
  if (override_id is null)
  then
      select nextval('plugload_profile_seq') into profile_id;
  	  insert into plugload_profile (id,active_motion_window,mode,manual_override_time)
  		values (profile_id, 30, 1, 60);
      update plugload_profile_handler set override7 = profile_id where id = ph_id;
  end if;

  override_id:=null;
  select override8 into override_id from plugload_profile_handler where id = ph_id;
  if (override_id is null)
  then
      select nextval('plugload_profile_seq') into profile_id;
  	  insert into plugload_profile (id,active_motion_window,mode,manual_override_time)
  		values (profile_id, 30, 1, 60);
      update plugload_profile_handler set override8 = profile_id where id = ph_id;
  end if;

END LOOP;

END;
$$
LANGUAGE plpgsql;

select add_plugload_overrides();

  
CREATE OR REPLACE FUNCTION updatezerobucketsforplugload()
  RETURNS void AS
$BODY$
DECLARE 
	mec1 numeric;
	mec2 numeric;
	umec1 numeric;
	umec2 numeric;	
	diffInMinutes numeric;
	diffInHours numeric;
	diffInDays numeric;		
	spreadManagedEnergyCumValues numeric;
	spreadUnManagedEnergyCumValues numeric;		
	hourlyCaptureAtArray timestamp[];
	dailyCaptureAtArray timestamp[];
	rec_plugload plugload_record;	
	rec_zb1 plugload_zb_record;
	rec_nzb1 plugload_nzb_record;
	rec_nzb2 plugload_nzb_record;
	captureAtHourly timestamp;		
	captureAtDay timestamp;	
	nzb1CaptureAt timestamp;
	nzb2CaptureAt timestamp;
	tempnzbCaptureAt timestamp;	
	lastZbUpdateTime timestamp;	
	lastProcesssedCaptureAt timestamp;
	lastZBCount numeric;
	BEGIN		
	FOR rec_plugload IN(
	select p.id,coalesce(pzb.last_zb_update_time,date_trunc('hour',now()::timestamp) - interval '7 days') from plugload p left join 
	plugload_zb pzb on p.id=pzb.plugload_id
	where  p.state='COMMISSIONED')
	LOOP		
	lastProcesssedCaptureAt = rec_plugload.last_zb_update_time;
	lastZbUpdateTime = lastProcesssedCaptureAt;
	if lastProcesssedCaptureAt < (date_trunc('hour',now()::timestamp) - interval '7 days') then	
	lastProcesssedCaptureAt = (date_trunc('hour',now()::timestamp) - interval '7 days');		
	end if;
	--raise notice 'plugload id is %,%,%',rec_plugload.plugload_id,lastProcesssedCaptureAt,lastZbUpdateTime;	

	select * into rec_zb1 from (
	select count(*) as zb_count ,plugload_id,MIN(pec.capture_at) as zb_capture_at from plugload_energy_consumption pec where zero_bucket =1 
	and capture_at >= lastProcesssedCaptureAt and plugload_id = rec_plugload.plugload_id group by capture_at,plugload_id order by capture_At limit 1 ) zb;

	WHILE rec_zb1.count > 0 LOOP			
	--raise notice 'zb1 record is %',rec_zb1.capture_at;
	diffInMinutes=0;spreadUnManagedEnergyCumValues=0;spreadUnManagedEnergyCumValues=0;	
	select * into rec_nzb1 from (select count(*) ,MAX(capture_at) ,COALESCE(managed_energy_cum,0) as managed_energy_cum ,
	COALESCE(unmanaged_energy_cum,0) as unmanaged_energy_cum from plugload_energy_consumption where zero_bucket = 0
	and managed_energy_cum is not null and plugload_id=rec_plugload.plugload_id and capture_at < (rec_zb1.capture_at)
	group by capture_at,managed_energy_cum,unmanaged_energy_cum order by capture_at desc limit 1) nzb1;
	
	select * into rec_nzb2 from (select count(*),min(capture_at),COALESCE(managed_energy_cum,0) as managed_energy_cum ,
	COALESCE(unmanaged_energy_cum,0) as unmanaged_energy_cum from plugload_energy_consumption where zero_bucket=0 
	and plugload_id=rec_plugload.plugload_id and 
	capture_at > (rec_zb1.capture_at)
	group by capture_at,managed_energy_cum,unmanaged_energy_cum order by capture_at asc limit 1 ) nzb2;	

	nzb1CaptureAt = rec_nzb1.capture_at;
	nzb2CaptureAt = rec_nzb2.capture_at;
	--raise notice 'capture_At ranges are %,%,%',rec_plugload.plugload_id,nzb1CaptureAt,nzb2CaptureAt;
	IF ((rec_nzb1.count + rec_nzb2.count) = 2) THEN 			
	if(nzb1CaptureAt is not null and nzb1CaptureAt < lastProcesssedCaptureAt) then
	nzb1CaptureAt = lastProcesssedCaptureAt;
	end if;	
		SELECT EXTRACT(EPOCH FROM (rec_nzb2.capture_at - rec_nzb1.capture_at))/60 into diffInMinutes;		
		IF diffInMinutes > 0 THEN		
		spreadManagedEnergyCumValues = (rec_nzb2.mec - rec_nzb1.mec)/(100*diffInMinutes);
		spreadUnManagedEnergyCumValues = (rec_nzb2.umec - rec_nzb1.umec)/(100*diffInMinutes);	
		--raise notice 'spread is %,%',spreadManagedEnergyCumValues,spreadUnManagedEnergyCumValues;
			IF(spreadManagedEnergyCumValues >= 0 and spreadUnManagedEnergyCumValues >=0) THEN 
			UPDATE plugload_energy_consumption set energy =spreadManagedEnergyCumValues *5,
			unmanaged_energy=spreadUnManagedEnergyCumValues*5, 
			base_energy=spreadManagedEnergyCumValues*5, 
			base_unmanaged_energy=spreadUnManagedEnergyCumValues*5,	
			managed_last_load=spreadManagedEnergyCumValues*5*12,
			unmanaged_last_load=spreadUnManagedEnergyCumValues*5*12,
			saved_energy=0,
			zero_bucket = 2 where plugload_id=rec_plugload.plugload_id and zero_bucket = 1 and capture_at >= nzb1CaptureAt
			and capture_at < nzb2CaptureAt;						
			END IF;					
			
			SELECT EXTRACT(EPOCH FROM (date_trunc('hour', nzb2CaptureAt) - date_trunc('hour', nzb1CaptureAt)))/3600 into diffInHours;
			SELECT EXTRACT(EPOCH FROM (date_trunc('day', nzb2CaptureAt) - date_trunc('day', nzb1CaptureAt)))/(3600*24) into diffInDays;

				tempnzbCaptureAt = date_trunc('hour', nzb1CaptureAt) + interval '1 hour';
				WHILE diffInHours >= 0 LOOP												
				IF tempnzbCaptureAt = any(hourlyCaptureAtArray) then raise notice 'captureat hourly is present %',tempnzbCaptureAt;
				ELSE
				tempnzbCaptureAt = date_trunc('hour', tempnzbCaptureAt);
				SELECT array_append(hourlyCaptureAtArray,tempnzbCaptureAt) into hourlyCaptureAtArray;
				END IF;			
				diffInHours = diffInHours - 1;	
				tempnzbCaptureAt = date_trunc('hour', tempnzbCaptureAt)+ interval '1 hour';			
				END LOOP;

				tempnzbCaptureAt = date_trunc('day', nzb1CaptureAt) + interval '1 day';
				WHILE diffInDays >= 0 LOOP											
				IF tempnzbCaptureAt = any(dailyCaptureAtArray) then raise notice 'captureat daily is present %',tempnzbCaptureAt;
				ELSE
				tempnzbCaptureAt = date_trunc('day', tempnzbCaptureAt);
				SELECT array_append(dailyCaptureAtArray,tempnzbCaptureAt) into dailyCaptureAtArray;
				END IF;			
				diffInDays = diffInDays - 1;	
				tempnzbCaptureAt = date_trunc('day', tempnzbCaptureAt)+ interval '1 day';			
				END LOOP;

						
		END IF;	
		if nzb2CaptureAt is not null then
		lastProcesssedCaptureAt = nzb2CaptureAt;
		lastZbUpdateTime = nzb2CaptureAt;	
		end if;
	ELSE	
	lastProcesssedCaptureAt = rec_zb1.capture_at;
	--raise notice 'last_zb_update_time is %',lastZbUpdateTime;
	if lastProcesssedCaptureAt < lastZbUpdateTime then
	lastZbUpdateTime = rec_zb1.capture_at;
	end if;
	--raise notice 'last_zb_update_time is %',lastZbUpdateTime;
	END IF;	
	select * into rec_zb1 from (
	select count(*) as zb_count ,plugload_id,MIN(pec.capture_at) as zb_capture_at from plugload_energy_consumption pec where zero_bucket =1  and plugload_id=rec_plugload.plugload_id
	and capture_at > lastProcesssedCaptureAt and plugload_id  = rec_plugload.plugload_id group by capture_at,plugload_id order by capture_at limit 1) zb;		
	END LOOP;		
	select count(*) into lastZBCount from plugload_zb where plugload_id=rec_plugload.plugload_id;
	if lastZBCount > 0 then 
	--raise notice 'updating last_zb_update_time is %',lastZbUpdateTime;
	update plugload_zb set last_zb_update_time = lastZbUpdateTime where plugload_id=rec_plugload.plugload_id; 
	else 
	--raise notice 'inserting last_zb_update_time is %',lastZbUpdateTime;
	insert into plugload_zb(id,plugload_id,last_zb_update_time) values(nextval('plugload_zb_seq'),rec_plugload.plugload_id,lastZbUpdateTime);
	end if;
	
	END LOOP;	
	if array_length(hourlyCaptureAtArray, 1) >=1 then
	 FOR k in 1..array_upper(hourlyCaptureAtArray, 1) 
	 LOOP			
                 PERFORM plugload_ec_aggregation_hourly(hourlyCaptureAtArray[k]);
             END LOOP;	
             end if;

             if array_length(dailyCaptureAtArray, 1) >=1 then
	 FOR k in 1..array_upper(dailyCaptureAtArray, 1) 
	 LOOP			
                 PERFORM plugload_ec_aggregation_daily(dailyCaptureAtArray[k]);
             END LOOP;	
             end if;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION updatezerobucketsforplugload()
  OWNER TO postgres;    
 
-- Function: aggregatedailyenergyconsumptionforplugload(timestamp without time zone)

-- DROP FUNCTION aggregatedailyenergyconsumptionforplugload(timestamp without time zone);

DROP FUNCTION IF EXISTS aggregatedailyenergyconsumptionforplugload(timestamp with time zone);
DROP FUNCTION IF EXISTS aggregatedailyenergyconsumptionforplugload(timestamp without time zone);

CREATE OR REPLACE FUNCTION aggregatedailyenergyconsumption_plugload(todate timestamp without time zone)
  RETURNS void AS
$BODY$
DECLARE 
	rec plugload_daily_record;	
	diffInDays numeric;	
	rec_plugload plugload_record;
	aggregation_time timestamp;
BEGIN
	FOR rec_plugload IN(select id as plugload_id from plugload where state='COMMISSIONED')
	LOOP
	select max(capture_at) into aggregation_time from plugload_energy_consumption_daily where plugload_id=rec_plugload.plugload_id;
	if aggregation_time < (date_trunc('day',now()::timestamp) - interval '7 days') then	
	aggregation_time = (date_trunc('day',now()::timestamp) - interval '7 days');	
	end if;
	IF aggregation_time is not null THEN
		select (EXTRACT(EPOCH FROM toDate) - EXTRACT(EPOCH FROM aggregation_time))/(3600*24) into diffInDays;		
		if diffInDays >1 then
			aggregation_time = aggregation_time + interval '1 day';		
			WHILE aggregation_time <= toDate LOOP			
			perform plugload_ec_aggregation_daily(aggregation_time);
			aggregation_time = aggregation_time + interval '1 day';				
			end loop;
		else		
		perform plugload_ec_aggregation_daily(todate);			
		end if;
	else
	perform plugload_ec_aggregation_daily(todate);		
	end IF;
	END LOOP;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION aggregatedailyenergyconsumption_plugload(timestamp without time zone)
  OWNER TO postgres;



  CREATE OR REPLACE FUNCTION plugload_ec_aggregation_daily(todate timestamp without time zone)
  RETURNS void AS
$BODY$
DECLARE 
	rec plugload_daily_record;
	min_load1 numeric;
	peak_load1 numeric;
	price_calc numeric;	
	rec_count integer;
	
	
BEGIN		
			FOR rec IN (
			SELECT plugload_id, SUM(energy) AS agg_power, sum(cost) AS agg_cost, sum(base_energy) AS base_energy, sum(base_cost) AS base_cost, sum(saved_energy) AS saved_energy, sum(saved_cost) AS saved_cost, sum(occ_saving) AS occ_saving,
			sum(tuneup_saving) AS tune_saving, sum(manual_saving) AS manual_saving, count(*) AS no_of_rec,
			SUM(base_unmanaged_energy) as base_unmanaged_energy,SUM(unmanaged_energy) as unmanaged_energy,SUM(saved_unmanaged_energy) as saved_unmanaged_energy
			FROM plugload_energy_consumption_hourly as ec 
			WHERE capture_at <= toDate and capture_at > toDate - interval '1 day' GROUP BY plugload_id)
			LOOP  
			IF rec.base_energy > 0 THEN
				price_calc = rec.base_cost*1000/rec.base_energy;
			ELSE
				price_calc = 0;
			END IF;
			select count(*) into rec_count from plugload_energy_consumption_daily
			where capture_at=toDate and plugload_id=rec.plugload_id;
			if rec_count = 0 then
				INSERT INTO plugload_energy_consumption_daily (id, plugload_id, energy, cost, price, capture_at, 
				base_energy, base_cost, saved_energy, saved_cost, occ_saving, tuneup_saving, manual_saving,base_unmanaged_energy,unmanaged_energy,saved_unmanaged_energy) 
				VALUES (nextval('plugload_energy_consumption_daily_seq'), rec.plugload_id, rec.agg_power, rec.agg_cost, round(price_calc, 10), 
				toDate, rec.base_energy, rec.base_cost, rec.saved_energy, rec.saved_cost, rec.occ_saving,rec.tune_saving, rec.manual_saving,rec.base_unmanaged_energy,rec.unmanaged_energy,rec.saved_unmanaged_energy 
				);
			else 
			update plugload_energy_consumption_daily
			set energy=rec.agg_power,cost=rec.agg_cost,price=round(price_calc, 10),
			base_energy=rec.base_energy,base_cost=rec.base_cost,saved_energy=rec.saved_energy,
			saved_cost=rec.saved_cost,occ_saving=rec.occ_saving,tuneup_saving=rec.tune_saving,
			manual_saving=rec.manual_saving,base_unmanaged_energy=rec.base_unmanaged_energy,unmanaged_energy=rec.unmanaged_energy,
			saved_unmanaged_energy=rec.saved_unmanaged_energy where plugload_id=rec.plugload_id and capture_at=toDate;
			end if;			
			end loop;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION plugload_ec_aggregation_daily(timestamp without time zone)
  OWNER TO postgres;
  
-- Function: aggregatehourlyenergyconsumptionforplugload(timestamp without time zone)

-- DROP FUNCTION aggregatehourlyenergyconsumptionforplugload(timestamp without time zone);

DROP FUNCTION IF EXISTS aggregatehourlyenergyconsumptionforplugload(timestamp with time zone);
DROP FUNCTION IF EXISTS aggregatehourlyenergyconsumptionforplugload(timestamp without time zone);

CREATE OR REPLACE FUNCTION aggregatehourlyenergyconsumption_plugload(todate timestamp without time zone)
  RETURNS void AS
$BODY$
DECLARE 
	rec_plugload plugload_record;
	diffInHours numeric;
	rec_count integer;
	aggregation_time timestamp;
BEGIN

FOR rec_plugload IN(select id as plugload_id from plugload where state='COMMISSIONED')
LOOP

	select max(capture_at) into aggregation_time from plugload_energy_consumption_hourly where plugload_id=rec_plugload.plugload_id;
	if aggregation_time < (date_trunc('hour',now()::timestamp) - interval '7 days') then	
	aggregation_time = (date_trunc('hour',now()::timestamp) - interval '7 days');	
	end if;
	IF aggregation_time is not null THEN
		select (EXTRACT(EPOCH FROM toDate) - EXTRACT(EPOCH FROM aggregation_time))/3600 into diffInHours;
		
		if diffInHours >1 then					
		aggregation_time = aggregation_time + interval '1 hour';
		WHILE aggregation_time <= toDate LOOP								
		perform plugload_ec_aggregation_hourly(aggregation_time);		
		aggregation_time = aggregation_time + interval '1 hour';
		END LOOP;
		else
		perform plugload_ec_aggregation_hourly(todate);
		end if;
	else
	perform plugload_ec_aggregation_hourly(todate);
	end if;
	END LOOP;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION aggregatehourlyenergyconsumption_plugload(timestamp without time zone)
  OWNER TO postgres;






  CREATE OR REPLACE FUNCTION plugload_ec_aggregation_hourly(todate timestamp without time zone)
  RETURNS void AS
$BODY$
DECLARE 	
	rec plugload_hour_record;	
	system_rec system_ec_record;
	min_load1 numeric;
	peak_load1 numeric;
	diffInHours numeric;
	rec_count integer;	
BEGIN
		FOR rec IN (
	SELECT p.id as plugload_id, agg_power, agg_cost, avg_temp, base_power, base_cost, saved_energy, saved_cost, 
	occ_saving,tune_saving, manual_saving, no_of_rec,base_unmanaged_energy,unmanaged_energy,saved_unmanaged_energy
	FROM plugload as p left outer join (
	SELECT plugload_id, SUM(energy) AS agg_power, sum(cost) AS agg_cost ,avg(avg_temperature) AS avg_temp, 
	SUM(base_energy) AS base_power, SUM(base_unmanaged_energy) AS base_unmanaged_energy,SUM(unmanaged_energy) AS unmanaged_energy,
	SUM(saved_unmanaged_energy) AS saved_unmanaged_energy,
	sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_energy, sum(saved_cost) AS  saved_cost, 
	SUM(occ_saving) AS occ_saving, SUM(tuneup_saving) AS tune_saving, 
	SUM(manual_saving) AS manual_saving, count(*) AS no_of_rec
	FROM plugload_energy_consumption as ec 
	WHERE capture_at <= toDate and capture_at > toDate - interval '1 hour' and 
	zero_bucket != 1 GROUP BY plugload_id) as sub_query on (sub_query.plugload_id = p.id))
	LOOP  	
	  IF rec.no_of_rec IS NULL THEN
	   select count(*) into rec_count from plugload_energy_consumption_hourly
		where capture_at=toDate and plugload_id=rec.plugload_id;		
		if rec_count = 0 then 
	    INSERT INTO plugload_energy_consumption_hourly (id, plugload_id, energy, price, cost, capture_at, 
	    avg_temperature, base_energy, base_cost, saved_energy, saved_cost, occ_saving, tuneup_saving, 
	    manual_saving,base_unmanaged_energy,unmanaged_energy,saved_unmanaged_energy) VALUES (nextval('plugload_energy_consumption_hourly_seq'), 
	    rec.plugload_id, 0, 0, 0, toDate, 0, 0, 0, 0, 0, 0, 0, 0,0,0,0);
		end if;
	  ELSE	
		select count(*) into rec_count from plugload_energy_consumption_hourly
		where capture_at=toDate and plugload_id=rec.plugload_id;		
		if rec_count = 0 then 
		INSERT INTO plugload_energy_consumption_hourly (id, plugload_id, energy, price, cost, capture_at, 
		avg_temperature, base_energy, base_cost, saved_energy, saved_cost, occ_saving, 
		tuneup_saving, manual_saving,base_unmanaged_energy,unmanaged_energy,saved_unmanaged_energy,zero_bucket) 
		VALUES (nextval('plugload_energy_consumption_hourly_seq'), rec.plugload_id, rec.agg_energy, 
		round(
		case 
		when rec.base_energy = 0 THEN 0 
		ELSE 
		cast(rec.base_cost *1000/(rec.no_of_rec*rec.base_energy) as numeric) end,10), 
		rec.agg_cost, toDate, round(rec.avg_temp), rec.base_energy, 
		rec.base_cost, rec.saved_energy, rec.saved_cost, rec.occ_saving, 
		rec.tune_saving, rec.manual_saving,rec.base_unmanaged_energy,rec.unmanaged_energy,rec.saved_unmanaged_energy,0);
		else 		
		update plugload_energy_consumption_hourly set energy=rec.agg_energy,
		price=round(case when rec.base_energy = 0 THEN 0 ELSE 
		cast(rec.base_cost *1000/(rec.no_of_rec*rec.base_energy) as numeric) end,10),
		cost = rec.agg_cost,
		avg_temperature=round(rec.avg_temp),
		base_energy=rec.base_energy,base_cost = rec.base_cost,saved_energy=rec.saved_energy,saved_cost=rec.saved_cost,
		occ_saving=rec.occ_saving,tuneup_saving=rec.tune_saving,manual_saving=rec.manual_saving,
		base_unmanaged_energy=rec.base_unmanaged_energy,unmanaged_energy=rec.unmanaged_energy,
		saved_unmanaged_energy=rec.saved_unmanaged_energy where capture_at=toDate and plugload_id=rec.plugload_id;		
		end if;
	  END IF;
	END LOOP;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION plugload_ec_aggregation_hourly(timestamp without time zone)
  OWNER TO postgres;


  
CREATE OR REPLACE FUNCTION override_schedules_update() returns void as $$
declare description_column character varying;
BEGIN
    	
	SELECT column_name into description_column  FROM information_schema.columns WHERE table_name='dr_target' and column_name='description';
    IF description_column is null then
        ALTER TABLE dr_target ADD COLUMN description character varying(255);
		delete from dr_target where dr_type like 'MANUAL';
	END IF;
END
$$ LANGUAGE plpgsql;

select override_schedules_update();
  
  
--Added by Shrihari to prune plugload energy consumption tables.

CREATE OR REPLACE FUNCTION pruneplugloaddatabase()
  RETURNS void AS
$BODY$
DECLARE 
	no_days numeric;
	no_days_text text;
	no_days_time timestamp;	
	tm timestamp = now();

BEGIN
	
	SELECT value INTO no_days
	FROM system_configuration
	WHERE name = 'db_pruning.5min_table';

	no_days_text = no_days || ' day';
	no_days_time = tm - no_days_text::interval;
	
	DELETE FROM plugload_energy_consumption WHERE capture_at < no_days_time;

	SELECT value INTO no_days
	FROM system_configuration
	WHERE name = 'db_pruning.hourly_table';

	no_days_text = no_days || ' day';
	no_days_time = tm - no_days_text::interval;
	
	DELETE FROM plugload_energy_consumption_hourly WHERE capture_at < no_days_time;

	SELECT value INTO no_days
	FROM system_configuration
	WHERE name = 'db_pruning.daily_table';

	no_days_text = no_days || ' day';
	no_days_time = tm - no_days_text::interval;
	
	DELETE FROM plugload_energy_consumption_daily WHERE capture_at < no_days_time;	

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION pruneplugloaddatabase()
  OWNER TO postgres;  
  
CREATE OR REPLACE FUNCTION deleteecdatabyfloor(p_floor_id integer)
  RETURNS void AS
$BODY$
BEGIN	
	delete from energy_consumption_daily where fixture_id in (select id from device  where floor_id = p_floor_id and type='Fixture');
	delete from energy_consumption_hourly where fixture_id in (select id from device where floor_id = p_floor_id and type='Fixture');
	delete from energy_consumption where fixture_id in (select id from device where floor_id = p_floor_id and type='Fixture');

	delete from plugload_energy_consumption_daily where plugload_id in (select id from device where floor_id = p_floor_id and type='Plugload');
	delete from plugload_energy_consumption_hourly where plugload_id in (select id from device where floor_id = p_floor_id and type='Plugload');
	delete from plugload_energy_consumption where plugload_id in (select id from device where floor_id = p_floor_id and type='Plugload');
	
	delete from events_and_fault where device_id in (select id from device where floor_id = p_floor_id);
	
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION deleteecdatabyfloor(integer)
  OWNER TO postgres;
 
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.plugload_pattern', 'plugload.bin');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'erc.batteryreportscheduler.enable', 'false');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'erc.batteryreportscheduler.email', '');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'erc.batteryreportscheduler.cronexpression', '');

alter table dr_target drop COLUMN enabled ;

delete from qrtz_simple_triggers where trigger_group = 'uemjobgroup';
delete from qrtz_triggers where trigger_group = 'uemjobgroup';
delete from qrtz_job_details where job_group = 'uemjobgroup';

delete from qrtz_simple_triggers where trigger_name = 'cleanup_trigger';
delete from qrtz_triggers where trigger_name = 'cleanup_trigger';
delete from qrtz_job_details where job_name = 'cleanup_job';
 
CREATE TABLE lightlevels_plugload
(
  id bigint NOT NULL,
  switch_id bigint,
  scene_id bigint,
  p_id bigint,
  lightlevel integer,
  CONSTRAINT lightlevels_plugload_pkey PRIMARY KEY (id),
  CONSTRAINT fk_plugload_id FOREIGN KEY (p_id)
      REFERENCES plugload (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_scene_id FOREIGN KEY (scene_id)
      REFERENCES scene (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_switch_id FOREIGN KEY (switch_id)
      REFERENCES switch (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE lightlevels_plugload OWNER TO postgres;

CREATE SEQUENCE lightlevels_plugload_seq
   INCREMENT 1
   START 1;
ALTER TABLE lightlevels_plugload_seq OWNER TO postgres;

  
CREATE TABLE motion_group_plugload_details
(
  id bigint NOT NULL,
  gems_group_plugload_id bigint,
  type smallint default 3,
  ambient_type smallint default 0,
  use_em_values smallint default 0,
  lo_amb_level smallint default 0,
  hi_amb_level smallint default 0,
  time_of_day int,
  light_level smallint default 0,
  CONSTRAINT motion_group_plugload_details_pkey PRIMARY KEY (id),
  CONSTRAINT fk_motion_group_pl_details_id FOREIGN KEY (gems_group_plugload_id)
      REFERENCES gems_group_plugload (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);
 
ALTER TABLE public.motion_group_plugload_details OWNER TO postgres;

CREATE SEQUENCE motion_group_plugload_details_seq
   INCREMENT 1
   START 1;
ALTER TABLE motion_group_plugload_details_seq OWNER TO postgres;

--Dhanesh 26/2/15 For turning manual mode to auto mode depending on battery level
ALTER TABLE switch add COLUMN force_auto_mode smallint DEFAULT 0;
UPDATE switch set force_auto_mode = 0 WHERE force_auto_mode IS NULL;

--Sreedhar 03/06/2015 adding plugload alarm
INSERT INTO event_type (id, "type", description, severity, active) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Plugload High Current', NULL, 2, 1);

UPDATE system_configuration set value='pl.bin' where name='upgrade.plugload_pattern';

--Sreedhar 03/20/2015 adding default values to plugload_profile_handler

ALTER TABLE plugload_profile_handler ALTER COLUMN heartbeat_interval SET DEFAULT 30;
ALTER TABLE plugload_profile_handler ALTER COLUMN no_of_missed_heartbeats SET DEFAULT 3;

-- Added by Sampath on 22/OCT/2013 Add all Ballasts and Bulbs Insert Statements above this .
SELECT setval('ballast_seq', (SELECT max(id)+1 FROM ballasts));

SELECT setval('bulb_seq', (SELECT max(id)+1 FROM bulbs));

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'energy_consumption.sync.max.id', '');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'energy_consumption_hourly.sync.max.id', '');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'energy_consumption_daily.sync.max.id', '');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'plugload_energy_consumption.sync.max.id', '');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'plugload_energy_consumption_hourly.sync.max.id', '');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'plugload_energy_consumption_daily.sync.max.id', '');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'em_motion_bits.sync.max.id', '');

--Added by Sree on 04/16 for lighting based occ status
ALTER TABLE fixture ADD COLUMN lighting_occ_status smallint DEFAULT 0;
UPDATE fixture SET lighting_occ_status = 0 WHERE lighting_occ_status IS NULL;

--Added by dhanesh 28-05-15 for lighting based occ status in AREA 
ALTER TABLE area ADD COLUMN lighting_occ_count smallint DEFAULT 0;
UPDATE area SET lighting_occ_count = 0 WHERE lighting_occ_count IS NULL;

CREATE OR REPLACE FUNCTION update_lighting_occ_count() RETURNS "trigger" AS $$
	BEGIN
	  IF tg_op = 'UPDATE' THEN
	    IF old.lighting_occ_status <> new.lighting_occ_status THEN
	    	IF new.lighting_occ_status > 0 THEN
				 UPDATE area SET lighting_occ_count = lighting_occ_count + 1 where id in (select area_id from device where id=new.id);
	    	ELSE
	    		UPDATE area SET lighting_occ_count = lighting_occ_count - 1 where id in (select area_id from device where id=new.id);
	    	END IF;
	    END IF;
	   END  IF;
	  RETURN new;
	END
$$ LANGUAGE plpgsql;

ALTER FUNCTION public.update_lighting_occ_count() OWNER TO postgres;

CREATE TRIGGER update_lighting_occ_count_trigger AFTER UPDATE ON fixture
FOR EACH ROW
Execute procedure update_lighting_occ_count();


--Added by Dhanesh EM-159
alter table plugload alter column managed_load type numeric(19,2);
alter table plugload alter column unmanaged_load type numeric(19,2);

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'glem.apikey', (select value from system_configuration where name like 'uem.apikey'));

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'glem.secretkey', (select value from system_configuration where name like 'uem.secretkey'));

alter table fixture add column current_trigger_type integer not null default 0;
alter table fixture add column change_trigger_type integer not null default 0;
alter table fixture add column occ_level_trigger_time integer not null default 90;
ALTER TABLE area ADD column zone_sensor_enable boolean DEFAULT false;

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'emLicenseKeyValue', '');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'em.UUID', '');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'pricing.currency', 'USD');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'email_notification_configuraiton', '');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet_config_enable', 'true');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'contact_closure_configuration', '');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ttl_configuration_map', '');

-- network settings start

CREATE TABLE network_types
(
  id bigint NOT NULL,
  name character varying,
  CONSTRAINT network_types_pk PRIMARY KEY (id),
  CONSTRAINT unique_network_types_name UNIQUE (name)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE network_types
  OWNER TO postgres;

  CREATE TABLE network_settings
(
  id bigint NOT NULL,
  port_enabled boolean DEFAULT true,   
  interface_name character varying(255),
  configure_ipv4 character varying(255),
  mac_address character varying(255), 
  ipv4_address character varying(255),
  subnet_mask character varying(255),
  gateway character varying(255),
  dns character varying(255),
  search_domain character varying(255),
  CONSTRAINT network_settings_pkey PRIMARY KEY (id),
  CONSTRAINT unique_interface_name UNIQUE (interface_name)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE network_settings
  OWNER TO postgres;
  
CREATE TABLE network_interface_mapping
(
   id integer, 
   network_settings_id integer, 
   network_type_id integer, 
   FOREIGN KEY (network_type_id) REFERENCES network_types (id) ON UPDATE NO ACTION ON DELETE NO ACTION, 
   FOREIGN KEY (network_settings_id) REFERENCES network_settings (id) ON UPDATE NO ACTION ON DELETE NO ACTION
) 
WITH (
  OIDS = FALSE
)
;

 CREATE SEQUENCE network_types_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;

 CREATE SEQUENCE network_interface_mapping_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
  CREATE SEQUENCE network_settings_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
  INSERT INTO network_types VALUES(1,'Building');
 INSERT INTO network_types VALUES(2,'Corporate');
 INSERT INTO network_types VALUES(3,'BACnet');
 
 INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'networksettings.isSetupReady', 'false');


-- Added by Yogesh on 15/9/2015
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ble.forwarding.server', ''); 
 INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'iptables.rules.static.ports', '22,8085,7,443,8443,80,389,12302');
 update system_configuration  set value ='22,8085,7,443,8443,80,389,12302,9191,2332,9092' where name = 'iptables.rules.static.ports';
 
 INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'apply.network.status', 'SUCCESS');
 
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'deleteDeviceStatus', 'SUCCESS');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'isMaintenanceMode', 'false');

 
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'backup.option.selected', 'usb');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'backup.usb.path', '/media/usb1');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'backup.sftp.username', '');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'backup.sftp.password', '');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'backup.sftp.ip', '');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'backup.sftp.directory', '');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'title24.json.key', '');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'list.timezone', 'America/Scoresbysund,Africa/Abidjan,Africa/Accra,Africa/Algiers,Africa/Bissau,Africa/Cairo,Africa/Casablanca,Africa/Ceuta,Africa/El_Aaiun,Africa/Johannesburg,Africa/Khartoum,Africa/Lagos,Africa/Maputo,Africa/Monrovia,Africa/Nairobi,Africa/Ndjamena,Africa/Tripoli,Africa/Tunis,Africa/Windhoek,America/Adak,America/Anchorage,America/Araguaina,America/Argentina/Buenos_Aires,America/Argentina/Catamarca,America/Argentina/Cordoba,America/Argentina/Jujuy,America/Argentina/La_Rioja,America/Argentina/Mendoza,America/Argentina/Rio_Gallegos,America/Argentina/Salta,America/Argentina/San_Juan,America/Argentina/San_Luis,America/Argentina/Tucuman,America/Argentina/Ushuaia,America/Asuncion,America/Atikokan,America/Bahia,America/Bahia_Banderas,America/Barbados,America/Belem,America/Belize,America/Blanc-Sablon,America/Boa_Vista,America/Bogota,America/Boise,America/Cambridge_Bay,America/Campo_Grande,America/Cancun,America/Caracas,America/Cayenne,America/Chicago,America/Chihuahua,America/Costa_Rica,America/Creston,America/Cuiaba,America/Curacao,America/Danmarkshavn,America/Dawson,America/Dawson_Creek,America/Denver,America/Detroit,America/Edmonton,America/Eirunepe,America/El_Salvador,America/Fort_Nelson,America/Fortaleza,America/Glace_Bay,America/Godthab,America/Goose_Bay,America/Grand_Turk,America/Guatemala,America/Guayaquil,America/Guyana,America/Halifax,America/Havana,America/Hermosillo,America/Indiana/Indianapolis,America/Indiana/Knox,America/Indiana/Marengo,America/Indiana/Petersburg,America/Indiana/Tell_City,America/Indiana/Vevay,America/Indiana/Vincennes,America/Indiana/Winamac,America/Inuvik,America/Iqaluit,America/Jamaica,America/Juneau,America/Kentucky/Louisville,America/Kentucky/Monticello,America/La_Paz,America/Lima,America/Los_Angeles,America/Maceio,America/Managua,America/Manaus,America/Martinique,America/Matamoros,America/Mazatlan,America/Menominee,America/Merida,America/Metlakatla,America/Mexico_City,America/Miquelon,America/Moncton,America/Monterrey,America/Montevideo,America/Nassau,America/New_York,America/Nipigon,America/Nome,America/Noronha,America/North_Dakota/Beulah,America/North_Dakota/Center,America/North_Dakota/New_Salem,America/Ojinaga,America/Panama,America/Pangnirtung,America/Paramaribo,America/Phoenix,America/Port_of_Spain,America/Port-au-Prince,America/Porto_Velho,America/Puerto_Rico,America/Rainy_River,America/Rankin_Inlet,America/Recife,America/Regina,America/Resolute,America/Rio_Branco,America/Santarem,America/Santiago,America/Santo_Domingo,America/Sao_Paulo,America/Sitka,America/St_Johns,America/Swift_Current,America/Tegucigalpa,America/Thule,America/Thunder_Bay,America/Tijuana,America/Toronto,America/Vancouver,America/Whitehorse,America/Winnipeg,America/Yakutat,America/Yellowknife,Antarctica/Casey,Antarctica/Davis,Antarctica/DumontDUrville,Antarctica/Macquarie,Antarctica/Mawson,Antarctica/Palmer,Antarctica/Rothera,Antarctica/Syowa,Antarctica/Troll,Antarctica/Vostok,Asia/Almaty,Asia/Amman,Asia/Anadyr,Asia/Aqtau,Asia/Aqtobe,Asia/Ashgabat,Asia/Baghdad,Asia/Baku,Asia/Bangkok,Asia/Barnaul,Asia/Beirut,Asia/Bishkek,Asia/Brunei,Asia/Chita,Asia/Choibalsan,Asia/Colombo,Asia/Damascus,Asia/Dhaka,Asia/Dili,Asia/Dubai,Asia/Dushanbe,Asia/Gaza,Asia/Hebron,Asia/Ho_Chi_Minh,Asia/Hong_Kong,Asia/Hovd,Asia/Irkutsk,Asia/Jakarta,Asia/Jayapura,Asia/Jerusalem,Asia/Kabul,Asia/Kamchatka,Asia/Karachi,Asia/Kathmandu,Asia/Khandyga,Asia/Kolkata,Asia/Krasnoyarsk,Asia/Kuala_Lumpur,Asia/Kuching,Asia/Macau,Asia/Magadan,Asia/Makassar,Asia/Manila,Asia/Nicosia,Asia/Novokuznetsk,Asia/Novosibirsk,Asia/Omsk,Asia/Oral,Asia/Pontianak,Asia/Pyongyang,Asia/Qatar,Asia/Qyzylorda,Asia/Rangoon,Asia/Riyadh,Asia/Sakhalin,Asia/Samarkand,Asia/Seoul,Asia/Shanghai,Asia/Singapore,Asia/Srednekolymsk,Asia/Taipei,Asia/Tashkent,Asia/Tbilisi,Asia/Tehran,Asia/Thimphu,Asia/Tokyo,Asia/Ulaanbaatar,Asia/Urumqi,Asia/Ust-Nera,Asia/Vladivostok,Asia/Yakutsk,Asia/Yekaterinburg,Asia/Yerevan,Atlantic/Azores,Atlantic/Bermuda,Atlantic/Canary,Atlantic/Cape_Verde,Atlantic/Faroe,Atlantic/Madeira,Atlantic/Reykjavik,Atlantic/South_Georgia,Atlantic/Stanley,Australia/Adelaide,Australia/Brisbane,Australia/Broken_Hill,Australia/Currie,Australia/Darwin,Australia/Eucla,Australia/Hobart,Australia/Lindeman,Australia/Lord_Howe,Australia/Melbourne,Australia/Perth,Australia/Sydney,Europe/Amsterdam,Europe/Andorra,Europe/Astrakhan,Europe/Athens,Europe/Belgrade,Europe/Berlin,Europe/Brussels,Europe/Bucharest,Europe/Budapest,Europe/Chisinau,Europe/Copenhagen,Europe/Dublin,Europe/Gibraltar,Europe/Helsinki,Europe/Istanbul,Europe/Kaliningrad,Europe/Kiev,Europe/Lisbon,Europe/London,Europe/Luxembourg,Europe/Madrid,Europe/Malta,Europe/Minsk,Europe/Monaco,Europe/Moscow,Europe/Oslo,Europe/Paris,Europe/Prague,Europe/Riga,Europe/Rome,Europe/Samara,Europe/Simferopol,Europe/Sofia,Europe/Stockholm,Europe/Tallinn,Europe/Tirane,Europe/Ulyanovsk,Europe/Uzhgorod,Europe/Vienna,Europe/Vilnius,Europe/Volgograd,Europe/Warsaw,Europe/Zaporozhye,Europe/Zurich,Indian/Chagos,Indian/Christmas,Indian/Cocos,Indian/Kerguelen,Indian/Mahe,Indian/Maldives,Indian/Mauritius,Indian/Reunion,Pacific/Apia,Pacific/Auckland,Pacific/Bougainville,Pacific/Chatham,Pacific/Chuuk,Pacific/Easter,Pacific/Efate,Pacific/Enderbury,Pacific/Fakaofo,Pacific/Fiji,Pacific/Funafuti,Pacific/Galapagos,Pacific/Gambier,Pacific/Guadalcanal,Pacific/Guam,Pacific/Honolulu,Pacific/Kiritimati,Pacific/Kosrae,Pacific/Kwajalein,Pacific/Majuro,Pacific/Marquesas,Pacific/Nauru,Pacific/Niue,Pacific/Norfolk,Pacific/Noumea,Pacific/Pago_Pago,Pacific/Palau,Pacific/Pitcairn,Pacific/Pohnpei,Pacific/Port_Moresby,Pacific/Rarotonga,Pacific/Tahiti,Pacific/Tarawa,Pacific/Tongatapu,Pacific/Wake,Pacific/Wallis');
 -- network settings end

--Added by Nilesh for EM-747,EM-748 on 04/03/2016

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'enlightedBacnetConfiguration.authKey', 'adbde73992a075074c159abace804039a3f13527');

-- Table: bacnet_configuration

-- DROP TABLE bacnet_configuration;

CREATE TABLE bacnet_configuration
(
  id bigint NOT NULL,
  "name" character varying,
  "value" character varying,
  CONSTRAINT bacnet_configuration_pk PRIMARY KEY (id),
  CONSTRAINT unique_bacnet_configuration_name UNIQUE (name)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE bacnet_configuration OWNER TO postgres;

-- Index: bacnet_configuration_name_index

-- DROP INDEX bacnet_configuration_name_index;

CREATE INDEX bacnet_configuration_name_index
  ON bacnet_configuration
  USING btree
  (name);
  
-- Sequence: bacnet_configuration_seq

-- DROP SEQUENCE bacnet_configuration_seq;

CREATE SEQUENCE bacnet_configuration_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE bacnet_configuration_seq OWNER TO postgres;

ALTER TABLE bacnet_configuration ADD COLUMN "isallowedtoshow" boolean DEFAULT false;

-- Table: bacnet_objects_cfg

CREATE TABLE bacnet_objects_cfg
(
  id bigint NOT NULL,
  bacnetobjecttype character varying NOT NULL,
  bacnetobjectinstance bigint NOT NULL,
  bacnetobjectdescription character varying NOT NULL,
  isvalidobject character varying NOT NULL,
  pointkeyword character varying NOT NULL,
  bacnetpointtype character varying NOT NULL,
  CONSTRAINT bacnet_objects_cfg_pk PRIMARY KEY (id),
  CONSTRAINT unique_bacnet_objects_cfg_instance UNIQUE (bacnetobjecttype, bacnetobjectinstance, bacnetpointtype),
  CONSTRAINT unique_bacnet_objects_cfg_pointkeyword UNIQUE (pointkeyword)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE bacnet_objects_cfg OWNER TO postgres;

-- Index: bacnet_objects_cfg_pointkeyword_index

CREATE INDEX bacnet_objects_cfg_pointkeyword_index
  ON bacnet_objects_cfg
  USING btree
  (pointkeyword);

-- Sequence: bacnet_objects_cfg_seq

CREATE SEQUENCE bacnet_objects_cfg_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE bacnet_objects_cfg_seq OWNER TO postgres;


-- Table: bacnet_report_cfg

CREATE TABLE bacnet_report_cfg
(
  id bigint NOT NULL,
  deviceid character varying NOT NULL,
  objecttype character varying NOT NULL,
  objectinstance character varying NOT NULL,
  objectname character varying NOT NULL,
  CONSTRAINT bacnet_report_cfg_pk PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE bacnet_report_cfg OWNER TO postgres;

-- Index: bacnet_report_cfg_deviceid_index

CREATE INDEX bacnet_report_cfg_deviceid_index
  ON bacnet_report_cfg
  USING btree
  (deviceid);
  
-- Sequence: bacnet_report_cfg_seq

CREATE SEQUENCE bacnet_report_cfg_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE bacnet_report_cfg_seq OWNER TO postgres;

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.report.updatedAt', '');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.report.flag', 'done');

--Added by Sree on 10/22 for lighting based occ status
ALTER TABLE area ADD COLUMN sensor_faulty_time smallint DEFAULT 15;
UPDATE area SET sensor_faulty_time = 15 WHERE sensor_faulty_time IS NULL;
 
ALTER TABLE area ADD COLUMN percentage_sensors_faulty smallint DEFAULT 75;
UPDATE area SET percentage_sensors_faulty = 75 WHERE percentage_sensors_faulty IS NULL; 

ALTER TABLE fixture ADD COLUMN heartbeat_status integer;


--Added by Yogesh 12/10/2015
ALTER TABLE company ADD COLUMN latitude float(4);
ALTER TABLE company ADD COLUMN longitude float(4);

-- Added by Sree on 10/27/2015
-- Name: update_area_occupancy_count; Type: PROCEDURE; Schema: public; Owner: postgres
--
CREATE OR REPLACE FUNCTION update_area_occupancy_count() RETURNS "trigger"
AS $$
BEGIN
  IF tg_op = 'UPDATE' THEN
    IF old.area_id = new.area_id THEN
      RETURN new;
    END IF;
    IF new.type <> 'Fixture' THEN
      RETURN new;
    END IF;
    IF old.area_id IS NOT NULL THEN
      UPDATE area SET lighting_occ_count = lighting_occ_count - (SELECT lighting_occ_status FROM fixture WHERE id = old.id) WHERE id = old.area_id;
      IF new.area_id IS NOT NULL THEN
        UPDATE area SET lighting_occ_count = lighting_occ_count + (SELECT lighting_occ_status FROM fixture WHERE id = old.id) WHERE id = new.area_id;
      END IF;
    ELSE
      UPDATE area SET lighting_occ_count = lighting_occ_count + (SELECT lighting_occ_status FROM fixture WHERE id = old.id) WHERE id = new.area_id;
    END IF;
  END IF;
  RETURN new;
END
$$
LANGUAGE plpgsql;

ALTER FUNCTION public.update_area_occupancy_count() OWNER TO postgres;

--
-- Name: update_fixture_gateway_change; Type: TRIGGER; Schema: public; Owner: postgres
--
CREATE TRIGGER update_device_area_occupancy_count
    AFTER UPDATE ON device
    FOR EACH ROW
    EXECUTE PROCEDURE update_area_occupancy_count();

    
--Added on 250116 EM-740
UPDATE system_configuration set value='45' where name='db_pruning.5min_table';    


--Added on 10032016
ALTER TABLE fixture ADD COLUMN dali_driver_cnt smallint;

--Added 14/03/16
alter table fixture add column manual_mode_duration smallint not null default 60;
UPDATE fixture SET manual_mode_duration = 60 WHERE manual_mode_duration IS NULL; 

--Added on 30032016
ALTER TABLE fixture ADD COLUMN ble_firmware_version character varying(20);
ALTER TABLE fixture add COLUMN ble_mode character varying;    


--Support for EM-843
ALTER TABLE profile_handler ADD COLUMN circadian_option smallint default 0;
ALTER TABLE profile_handler ADD COLUMN color_fx smallint default 0;
ALTER TABLE profile_handler ADD COLUMN pwmbehaviour smallint default 0;
ALTER TABLE profile_handler ADD COLUMN sunrise_temperature_ratio smallint default 0;
ALTER TABLE profile_handler ADD COLUMN noon_temperature_ratio smallint default 0;
ALTER TABLE profile_handler ADD COLUMN sunset_temperature_ratio smallint default 0;
ALTER TABLE profile_handler ADD COLUMN night_temperature_ratio smallint default 0;
UPDATE profile_handler SET circadian_option = 0 WHERE circadian_option is null;
UPDATE profile_handler SET color_fx = 0 WHERE color_fx is null;
UPDATE profile_handler SET pwmbehaviour = 0 WHERE pwmbehaviour is null;
UPDATE profile_handler SET sunrise_temperature_ratio = 0 WHERE sunrise_temperature_ratio is null;
UPDATE profile_handler SET noon_temperature_ratio = 0 WHERE noon_temperature_ratio is null;
UPDATE profile_handler SET sunset_temperature_ratio = 0 WHERE sunset_temperature_ratio is null;
UPDATE profile_handler SET night_temperature_ratio = 0 WHERE night_temperature_ratio is null;

ALTER TABLE fixture ADD COLUMN dual_channel_led_value integer;
alter table fixture add column dualchannel_led_duration smallint not null default 60;
UPDATE fixture SET dualchannel_led_duration = 60 WHERE dualchannel_led_duration IS NULL; 
ALTER TABLE floor ADD COLUMN site_id bigint;
ALTER TABLE fixture ADD COLUMN circ_ratio smallint;

--EM-843: Astro clock sync support time in (seconds)
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'astroclock.sync', '3600');

--Added by Sree 05/25/2016
CREATE OR REPLACE FUNCTION aggregatefixturehourlyec(todate timestamp with time zone, gwid integer) RETURNS void
    AS $$
DECLARE
        rec fixture_hour_record;
        system_rec system_ec_record;
        min_load1 numeric;
        peak_load1 numeric;
BEGIN
        FOR rec IN (
        SELECT f.id as fixt_id, agg_power, agg_cost, min_temp, max_temp, avg_temp, base_power, base_cost, saved_power, saved_cost, occ_saving, amb_saving, tune_saving, manual_saving, no_of_rec, peak_load, min_load, min_price, max_price, avg_load FROM fixture as f join (
        SELECT fixture_id, SUM(power_used)/12 AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(avg_temperature) AS avg_temp, SUM(base_power_used)/12 AS base_power, sum(base_cost) AS base_cost, SUM(saved_power_used)/12 AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_saving)/12 AS occ_saving, SUM(ambient_saving)/12 AS amb_saving, SUM(tuneup_saving)/12 AS tune_saving, SUM(manual_saving)/12 AS manual_saving, count(*) AS no_of_rec, max(power_used) AS peak_load, min(power_used) AS min_load, min(price) AS min_price, max(price) AS max_price, avg(power_used) AS avg_load
        FROM energy_consumption as ec
        WHERE capture_at <= toDate and capture_at > toDate - interval '1 hour' and fixture_id in (select fixture_id from fixture where gateway_id = gwid) and base_power_used != 0 and zero_bucket != 1 GROUP BY fixture_id) as sub_query on (sub_query.fixture_id = f.id))
        LOOP
          IF rec.no_of_rec IS NULL THEN
            INSERT INTO energy_consumption_hourly (id, fixture_id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, peak_load, min_load, min_price, max_price, avg_load) VALUES (nextval('energy_consumption_hourly_seq'), rec.fixture_id, 0, 0, 0, toDate, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
          ELSE
                INSERT INTO energy_consumption_hourly (id, fixture_id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, peak_load, min_load, min_price, max_price, avg_load) VALUES (nextval('energy_consumption_hourly_seq'), rec.fixture_id, rec.agg_power, round(cast (rec.base_cost*12*1000/(rec.no_of_rec *rec.base_power) as numeric), 10), rec.agg_cost, toDate, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_load, rec.min_load, rec.min_price, rec.max_price, rec.avg_load);
          END IF;
        END LOOP;

END;
$$
LANGUAGE plpgsql;

--Sree 06/06
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'emc.mode', '0');
--Sree 06/13
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'emc.utc_time_freq_sec', '3600');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'emc.astro_time_freq_sec', '86400');

--Sree 07/05
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'emc.kafka_server', 'iec2-54-200-59-27.us-west-2.compute.amazonaws.com');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'emc.kafka_server_port', '9092');

ALTER TABLE building ADD COLUMN latitude float(4);
ALTER TABLE building ADD COLUMN longitude float(4);
ALTER TABLE building ADD COLUMN use_org_location boolean DEFAULT true;

CREATE SEQUENCE override_schedules_facility_seq
INCREMENT BY 1
MAXVALUE 999999999999999999
NO MINVALUE
CACHE 1;


ALTER TABLE public.override_schedules_facility_seq OWNER TO postgres;

CREATE TABLE  override_schedules_facility (
id bigint NOT NULL,
facility_type character varying NOT null,
facility_id bigint NOT NULL,   
schedule_id bigint NOT NULL
);


ALTER TABLE ONLY override_schedules_facility
ADD CONSTRAINT  override_schedules_facility_pk PRIMARY KEY (id);

ALTER TABLE ONLY public.override_schedules_facility  
ADD CONSTRAINT override_schedules_facility_id FOREIGN KEY(schedule_id) REFERENCES dr_target(id);

ALTER TABLE public.override_schedules_facility OWNER TO postgres;


ALTER TABLE campus ADD COLUMN visible boolean DEFAULT true;
ALTER TABLE building ADD COLUMN visible boolean DEFAULT true;
ALTER TABLE floor ADD COLUMN visible boolean DEFAULT true;
ALTER TABLE area ADD COLUMN visible boolean DEFAULT true;

UPDATE campus set visible = true;
UPDATE building set visible = true;
UPDATE floor set visible = true;
UPDATE area set visible = true;

--Sree 07/13
--Add the capability to update the facilities based on uid from CEM
ALTER TABLE company ADD COLUMN uid character varying(100);
ALTER TABLE campus ADD COLUMN uid character varying(100);
ALTER TABLE building ADD COLUMN uid character varying(100);
ALTER TABLE floor ADD COLUMN uid character varying(100);

ALTER TABLE company ADD CONSTRAINT unique_company_uid UNIQUE (uid);
ALTER TABLE campus ADD CONSTRAINT unique_campus_uid UNIQUE (uid);
ALTER TABLE building ADD CONSTRAINT unique_building_uid UNIQUE (uid);
ALTER TABLE floor ADD CONSTRAINT unique_floor_uid UNIQUE (uid);

--Sree 07/27 EMC-68
UPDATE system_configuration set value='1' where name='imageUpgrade.no_multicast_retransmits';

--The below functions should always be at the end of this file.Please add any changes above this function.
--Kushal: update drop_triggers and add_triggers whenever triggers are added/updated/removed.
CREATE OR REPLACE FUNCTION drop_triggers() RETURNS character varying as $$
BEGIN	
	DROP TRIGGER IF EXISTS update_area_change ON AREA;
	DROP TRIGGER IF EXISTS update_building_change ON building;
	DROP TRIGGER IF EXISTS update_campus_change ON campus;
	DROP TRIGGER IF EXISTS update_floor_change ON floor;
	DROP TRIGGER IF EXISTS update_fixture_gateway_change ON fixture;
	DROP TRIGGER IF EXISTS update_profilename_change ON groups;
	DROP TRIGGER IF EXISTS update_templatename_change ON profile_template;
	DROP TRIGGER IF EXISTS fixture_update ON fixture;
	DROP TRIGGER IF EXISTS update_lighting_occ_count_trigger ON fixture;
	DROP TRIGGER IF EXISTS update_device_area_occupancy_count ON device;
	return '';
END 
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_triggers() RETURNS character varying as $$
BEGIN
	DROP TRIGGER IF EXISTS update_area_change ON AREA;
	CREATE TRIGGER update_area_change AFTER UPDATE ON area FOR EACH ROW EXECUTE PROCEDURE update_location_change();
	
	DROP TRIGGER IF EXISTS update_building_change ON building;
	CREATE TRIGGER update_building_change AFTER UPDATE ON building FOR EACH ROW EXECUTE PROCEDURE update_location_change();
	
	DROP TRIGGER IF EXISTS update_campus_change ON campus;
	CREATE TRIGGER update_campus_change AFTER UPDATE ON campus FOR EACH ROW EXECUTE PROCEDURE update_location_change();
	
	DROP TRIGGER IF EXISTS update_floor_change ON floor;
	CREATE TRIGGER update_floor_change AFTER UPDATE ON floor FOR EACH ROW EXECUTE PROCEDURE update_location_change();
	
	DROP TRIGGER IF EXISTS update_fixture_gateway_change ON fixture;
	CREATE TRIGGER update_fixture_gateway_change BEFORE UPDATE ON fixture FOR EACH ROW EXECUTE PROCEDURE update_sec_gateway_change();
	
	DROP TRIGGER IF EXISTS update_profilename_change ON groups;
	CREATE TRIGGER update_profilename_change AFTER DELETE OR UPDATE ON groups FOR EACH ROW Execute procedure update_fixture_currentprofilename_change();
	
	DROP TRIGGER IF EXISTS update_templatename_change ON profile_template;
	CREATE TRIGGER update_templatename_change AFTER UPDATE ON profile_template FOR EACH ROW Execute procedure update_profilename_on_templatename_change();
	
	DROP TRIGGER IF EXISTS fixture_update ON fixture;
	CREATE TRIGGER fixture_update AFTER UPDATE ON fixture FOR EACH ROW EXECUTE PROCEDURE fixture_update_trigger();
	
	DROP TRIGGER IF EXISTS update_lighting_occ_count_trigger ON fixture;
	CREATE TRIGGER update_lighting_occ_count_trigger AFTER UPDATE ON fixture FOR EACH ROW Execute procedure update_lighting_occ_count();

	DROP TRIGGER IF EXISTS update_device_area_occupancy_count ON device;
	CREATE TRIGGER update_device_area_occupancy_count AFTER UPDATE ON device FOR EACH ROW EXECUTE PROCEDURE update_area_occupancy_count();
	return '';
END 
$$ LANGUAGE plpgsql;
--Kushal: Need to update this function whenever a new sequence is introduced or its usage is modified.
CREATE OR REPLACE FUNCTION update_all_sequences() RETURNS character varying as $$
BEGIN	
    perform setval('application_configuration_seq', (select max(id)+1 from application_configuration));
    perform setval('area_seq', (select max(id)+1 from  area));
    perform setval('ballast_volt_power_seq', (select max(id)+1 from ballast_volt_power));
    perform setval('building_seq', (select max(id)+1 from building));
    perform setval('bulb_seq', (select max(id)+1 from bulbs));
    perform setval('button_manipulation_seq', (select max(id)+1 from button_manipulation));
    perform setval('button_map_seq', (select max(id)+1 from button_map));
    perform setval('campus_seq', (select max(id)+1 from campus));
    perform setval('cannedprofile_configuration_seq', (select max(id)+1 from canned_profiles_configuration));
    perform setval('company_campus_seq', (select max(id)+1 from company_campus));

    perform setval('company_seq', (select max(id)+1 from company));
    perform setval('custom_fixture_profile_group_seq', (select max(id)+1 from custom_fixture_profile_group));
    perform setval('dr_target_seq', (select max(id)+1 from dr_target));
    perform setval('dr_users_seq', (select max(id)+1 from dr_users));
    perform setval('em_motion_bits_seq', (select max(id)+1 from em_motion_bits));
    perform setval('em_stats_seq', (select max(id)+1 from em_stats));
    perform setval('ems_audit_seq', (select max(id)+1 from ems_audit));
    perform setval('ems_user_audit_seq', (select max(id)+1 from ems_user_audit));
    perform setval('energy_consumption_daily_seq', (select max(id)+1 from energy_consumption_daily));
    perform setval('energy_consumption_hourly_seq', (select max(id)+1 from energy_consumption_hourly));
    perform setval('energy_consumption_seq', (select max(id)+1 from energy_consumption));
    perform setval('event_type_seq', (select max(id)+1 from event_type));
    perform setval('events_seq', (select max(id)+1 from events_and_fault));
    perform setval('firmware_upgrade_seq', (select max(id)+1 from firmware_upgrade));
    --perform setval('fixturedistances_seq', (select max(id)+1 from fixturedistances));
    perform setval('fixture_class_seq', (select max(id)+1 from fixture_class));
    perform setval('fixture_group_seq', (select max(id)+1 from fixture_group));
    --perform setval('fixture_image_version_seq', (select max(id)+1 from fixture_image_version));
    --perform setval('fixture_images_seq', (select max(id)+1 from fixture_images));
    perform setval('fixture_seq', (select max(id)+1 from device));
    --perform setval('fixture_upgrade_time_seq', (select max(id)+1 from fixture_upgrade_time));
    perform setval('floor_seq', (select max(id)+1 from floor));
    perform setval('gateway_seq', (select max(id)+1 from gateway));
    perform setval('gems_group_fixture_seq', (select max(id)+1 from gems_group_fixture));
    perform setval('gems_group_seq', (select max(id)+1 from gems_groups));
    perform setval('group_no_seq', (select greatest((select cast(substring(cast(coalesce(max(group_no),100) as character varying) from 3) as bigint) from switch_group), (select cast(substring(cast(coalesce(max(group_no),100) as character varying) from 3) as bigint) from motion_bits_scheduler), (select cast(substring(cast(coalesce(max(group_no),100) as character varying) from 3) as bigint) from motion_group)) + 1));
    perform setval('groups_seq', (select max(id)+1 from groups));
    perform setval('gw_stats_seq', (select max(id)+1 from gw_stats));
    perform setval('holiday_seq', (select max(id)+1 from holiday));
    perform setval('image_upgrade_device_status_seq', (select max(id)+1 from image_upgrade_device_status));
    perform setval('image_upgrade_job_seq', (select max(id)+1 from image_upgrade_job));
    perform setval('inventory_device_seq', (select max(id)+1 from inventorydevice));
    perform setval('ldap_settings_seq', (select max(id)+1 from ldap_settings));
    perform setval('lightlevel_seq', (select max(id)+1 from lightlevels));
    perform setval('module_permission_seq', (select max(id)+1 from module_permission));
    perform setval('module_seq', (select max(id)+1 from module));
    perform setval('motion_bits_scheduler_seq', (select max(id)+1 from motion_bits_scheduler));
    perform setval('motion_group_seq', (select max(id)+1 from motion_group));
    perform setval('outage_base_power_seq', (select max(id)+1 from outage_base_power));
    perform setval('plan_map_seq', (select max(id)+1 from plan_map));
    perform setval('pricing_seq', (select max(id)+1 from pricing));
    perform setval('profile_configuration_seq', (select max(id)+1 from profile_configuration));
    perform setval('profile_handler_seq', (select max(id)+1 from profile_handler));
    perform setval('profile_template_seq', (select max(id)+1 from profile_template));
    perform setval('profile_seq', (select max(id)+1 from profile));
    perform setval('role_seq', (select max(id)+1 from roles));
    perform setval('scene_seq', (select max(id)+1 from scene));
    perform setval('sub_area_seq', (select max(id)+1 from sub_area));
    perform setval('sweep_timer_details_seq', (select max(id)+1 from sweep_timer_details));
    perform setval('sweep_timer_seq', (select max(id)+1 from sweep_timer));

    perform setval('switch_seq', (select max(id)+1 from switch));
    perform setval('switch_group_seq', (select max(id)+1 from switch_group));
    perform setval('system_configuration_seq', (select max(id)+1 from system_configuration));
    perform setval('tenant_locations_seq', (select max(id)+1 from tenant_locations));
    perform setval('tenants_seq', (select max(id)+1 from tenants));
    perform setval('timezone_seq', (select max(id)+1 from timezone));
    perform setval('user_location_seq', (select max(id)+1 from user_locations));
    perform setval('user_seq', (select max(id)+1 from users));
    perform setval('user_switches_seq', (select max(id)+1 from user_switches));
    perform setval('wds_model_type_seq', (select max(id)+1 from wds_model_type));
    perform setval('wds_model_type_button_seq', (select max(id)+1 from wds_model_type_button));
    perform setval('wds_no_seq', (select cast(substring(cast(coalesce(max(wds_no),100) as character varying) from 3) as bigint) + 1 from wds));
    perform setval('weekday_seq', (select max(id)+1 from weekday));
    perform setval('wal_logs_seq', (select cast(val as bigint)+100 from cloud_config where name like 'lastWalSyncId'));
    perform setval('ballast_seq', (SELECT max(id)+1 FROM ballasts));	

    perform setval('fixture_lamp_calibration_seq', (SELECT max(id)+1 FROM fixture_lamp_calibration));	
    perform setval('fixture_calibration_map_seq', (SELECT max(id)+1 FROM fixture_calibration_map));	
    perform setval('lamp_calibration_configuration_seq', (SELECT max(id)+1 FROM lamp_calibration_configuration));	
    perform setval('placed_fixture_seq', (SELECT max(id)+1 FROM placed_fixture));
    perform setval('Fixture_Diagnostic_Reference_seq', (SELECT max(id)+1 FROM Fixture_Diagnostic_Reference));	
    perform setval('Fixture_Diagnostics_seq', (SELECT max(id)+1 FROM Fixture_Diagnostics));
    perform setval('floor_zbupdate_seq', (SELECT max(id)+1 FROM floor_zbupdate));	
    perform setval('motion_group_fixture_details_seq', (SELECT max(id)+1 FROM motion_group_fixture_details));	
    perform setval('scene_templates_seq', (SELECT max(id)+1 FROM scene_templates));	
    perform setval('scene_light_levels_templates_seq', (SELECT max(id)+1 FROM scene_light_levels_templates));	
    perform setval('firmware_upgrade_schedule_seq', (SELECT max(id)+1 FROM firmware_upgrade_schedule));
	
	--plugload sequences
	perform setval('plugload_profile_template_seq', (SELECT max(id)+1 FROM plugload_profile_template));
	perform setval('gems_group_plugload_seq', (SELECT max(id)+1 FROM gems_group_plugload));
	perform setval('energy_consumption_plugload_seq', (SELECT max(id)+1 FROM plugload_energy_consumption));
	perform setval('plugload_energy_consumption_hourly_seq', (SELECT max(id)+1 FROM plugload_energy_consumption_hourly));
	perform setval('plugload_energy_consumption_daily_seq', (SELECT max(id)+1 FROM plugload_energy_consumption_daily));	
	perform setval('plugload_profile_configuration_seq', (SELECT max(id)+1 FROM plugload_profile_configuration));
	perform setval('weekday_plugload_seq', (SELECT max(id)+1 FROM weekday_plugload));
	perform setval('plugload_profile_seq', (SELECT max(id)+1 FROM plugload_profile));
	perform setval('plugload_profile_handler_seq', (SELECT max(id)+1 FROM plugload_profile_handler));
	perform setval('plugload_group_seq', (SELECT max(id)+1 FROM plugload_groups));
	perform setval('lightlevels_plugload_seq', (SELECT max(id)+1 FROM lightlevels_plugload));
	perform setval('motion_group_plugload_details_seq', (SELECT max(id)+1 FROM motion_group_plugload_details));


	perform setval('network_settings_seq', (SELECT max(id)+1 FROM network_settings));
	perform setval('network_types_seq', (SELECT max(id)+1 FROM network_types));
	perform setval('network_interface_mapping_seq', (SELECT max(id)+1 FROM network_interface_mapping));
	perform setval('plugload_zb_seq', (SELECT max(id)+1 FROM plugload_zb));
	perform setval('override_schedules_facility_seq', (select max(id)+1 from override_schedules_facility));

	return '';
END

$$ LANGUAGE plpgsql;

--The above function should always be at the end of this file.Please add any changes above this function.
