
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

	SELECT max(peak_load) INTO peak_load1
     	FROM system_energy_consumption_hourly
     WHERE capture_at <= toDate and capture_at > toDate - interval '1 day';

	SELECT min(min_load) INTO min_load1
	FROM system_energy_consumption_hourly
	WHERE capture_at <= toDate and capture_at > toDate - interval '1 day';

     INSERT INTO system_energy_consumption_daily (id, capture_at, peak_load, min_load) VALUES (nextval('system_energy_consumption_daily_seq'), toDate, peak_load1, min_load1);

	PERFORM prunedatabase();

	PERFORM pruneemsaudit();
	
	PERFORM prune_ems_user_audit();

END;
$$
LANGUAGE plpgsql;

CREATE TYPE system_ec_record AS (
        capture_time timestamp without time zone,
        load numeric
);

CREATE OR REPLACE FUNCTION aggregatehourlyenergyconsumption(todate timestamp with time zone) RETURNS void
    AS $$
DECLARE 
	rec fixture_hour_record;	
	system_rec system_ec_record;
	min_load1 numeric;
	peak_load1 numeric;
BEGIN
	FOR rec IN (
	SELECT f.id as fixt_id, agg_power, agg_cost, min_temp, max_temp, avg_temp, base_power, base_cost, saved_power, saved_cost, occ_saving, amb_saving, tune_saving, manual_saving, no_of_rec, peak_load, min_load, min_price, max_price FROM fixture as f left outer join (
	SELECT fixture_id, AVG(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(avg_temperature) AS avg_temp, AVG(base_power_used) AS base_power, sum(base_cost) AS base_cost, AVG(saved_power_used) AS saved_power, sum(saved_cost) AS  saved_cost, AVG(occ_saving) AS occ_saving, AVG(ambient_saving) AS amb_saving, AVG(tuneup_saving) AS tune_saving, AVG(manual_saving) AS manual_saving, count(*) AS no_of_rec, max(power_used) AS peak_load, min(power_used) AS min_load, min(price) AS min_price, max(price) AS max_price
	FROM energy_consumption as ec 
	WHERE capture_at <= toDate and capture_at > toDate - interval '1 hour' and base_power_used != 0 and zero_bucket != 1 GROUP BY fixture_id) as sub_query on (sub_query.fixture_id = f.id))
	LOOP  
	  IF rec.no_of_rec IS NULL THEN
	    INSERT INTO energy_consumption_hourly (id, fixture_id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, peak_load, min_load, min_price, max_price) VALUES (nextval('energy_consumption_hourly_seq'), rec.fixture_id, 0, 0, 0, toDate, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	  ELSE
		INSERT INTO energy_consumption_hourly (id, fixture_id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, peak_load, min_load, min_price, max_price) VALUES (nextval('energy_consumption_hourly_seq'), rec.fixture_id, rec.agg_power, round(cast (rec.base_cost*12*1000/(rec.no_of_rec *rec.base_power) as numeric), 10), rec.agg_cost, toDate, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_load, rec.min_load, rec.min_price, rec.max_price);
	  END IF;
	END LOOP;

	FOR system_rec IN (
        SELECT capture_at AS capture_time, sum(power_used) AS load
        FROM energy_consumption
        WHERE capture_at <= toDate and capture_at > toDate - interval '1 hour' and zero_bucket != 1 GROUP BY capture_at)
        LOOP
                INSERT INTO system_energy_consumption (id, capture_at, power_used) VALUES (nextval('system_energy_consumption_seq'), system_rec.capture_time, system_rec.load);
        END LOOP;

        SELECT max(power_used) INTO peak_load1
        FROM system_energy_consumption
        WHERE capture_at <= toDate and capture_at > toDate - interval '1 hour';

        SELECT min(power_used) INTO min_load1
        FROM system_energy_consumption
        WHERE capture_at <= toDate and capture_at > toDate - interval '1 hour';

        INSERT INTO system_energy_consumption_hourly (id, capture_at, peak_load, min_load) VALUES (nextval('system_energy_consumption_hourly_seq'), toDate, peak_load1, min_load1);

END;
$$
LANGUAGE plpgsql;

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
  enabled character varying(100),
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

-- Added on September 29th, 2010 by Shiv --

ALTER TABLE dr_target ADD CONSTRAINT unique_price_level UNIQUE (price_level);

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

INSERT INTO dr_target (id, price_level, pricing, duration, target_reduction, enabled) VALUES (nextval('dr_target_seq'), 'Off Peak', 0.085, 60, 0, 'No');
INSERT INTO dr_target (id, price_level, pricing, duration, target_reduction, enabled) VALUES (nextval('dr_target_seq'), 'Normal', 0.105, 60, 0, 'No');
INSERT INTO dr_target (id, price_level, pricing, duration, target_reduction, enabled) VALUES (nextval('dr_target_seq'), 'Peak', 0.150, 60, 10, 'No');
INSERT INTO dr_target (id, price_level, pricing, duration, target_reduction, enabled) VALUES (nextval('dr_target_seq'), 'Moderate', 0.24, 60, 30, 'No');
INSERT INTO dr_target (id, price_level, pricing, duration, target_reduction, enabled) VALUES (nextval('dr_target_seq'), 'High', 0.36, 60, 50, 'No');
INSERT INTO dr_target (id, price_level, pricing, duration, target_reduction, enabled) VALUES (nextval('dr_target_seq'), 'Critical', 0.48, 60, 70, 'No');

-- Added on September 22nd, 2010 by Shiv --

ALTER TABLE dr_target ADD COLUMN start_time timestamp without time zone;
ALTER TABLE dr_target ALTER COLUMN enabled SET DEFAULT 'No';
UPDATE dr_target SET enabled = 'No';

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

-- Added by Sreedhar 10/18
CREATE TABLE system_energy_consumption
(
  id bigint NOT NULL,
  capture_at timestamp without time zone,
  power_used numeric(19,2),
  CONSTRAINT system_energy_consumption_pkey PRIMARY KEY (id)  
);

ALTER TABLE system_energy_consumption OWNER TO postgres;

--
-- Name: system_energy_consumption_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE system_energy_consumption_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE system_energy_consumption_seq OWNER TO postgres;

CREATE TABLE system_energy_consumption_hourly
(
  id bigint NOT NULL,
  capture_at timestamp without time zone,
  peak_load numeric(19,2),
  min_load numeric(19,2),
  CONSTRAINT system_energy_consumption_hourly_pkey PRIMARY KEY (id)  
);

ALTER TABLE system_energy_consumption_hourly OWNER TO postgres;

--
-- Name: system_energy_consumption_hoyrly_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE system_energy_consumption_hourly_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE system_energy_consumption_hourly_seq OWNER TO postgres;

CREATE TABLE system_energy_consumption_daily
(
  id bigint NOT NULL,
  capture_at timestamp without time zone,
  peak_load numeric(19,2),
  min_load numeric(19,2),
  CONSTRAINT system_energy_consumption_daily_pkey PRIMARY KEY (id)
);

ALTER TABLE system_energy_consumption_daily OWNER TO postgres;

--
-- Name: system_energy_consumption_daily_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE system_energy_consumption_daily_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;

ALTER TABLE system_energy_consumption_daily_seq OWNER TO postgres;

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
CREATE UNIQUE INDEX unique_voltpowermap_in_ballast_volt_power ON ballast_volt_power(volt_power_map_id, volt);

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
	sValue = 'Default,Breakroom,Conference Room,Open Corridor,Closed Corridor,Egress,Lobby,Warehouse,Open Office,Private Office,Restroom,Lab,Custom1,Custom2,Standalone,Highbay';
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
	profiles_adv_cols text[] = ARRAY['groupnameholder', 'pfh.dark_lux', 'pfh.neighbor_lux', 'pfh.envelope_on_level', 'pfh.drop', 'pfh.rise', 'pfh.dim_backoff_time', 'pfh.intensity_norm_time', 'pfh.on_amb_light_level', 'pfh.min_level_before_off', 'pfh.relays_connected', 'pfh.standalone_motion_override', 'pfh.dr_reactivity', 'pfh.to_off_linger', 'pfh.initial_on_level', 'pfc.morning_time', 'pfc.day_time', 'pfc.evening_time', 'pfc.night_time', 'pfh.initial_on_time'];
	profiles_groups_with_adv_defaults text[] = '{
	{"default", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.breakroom", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "10", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.conferenceroom", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.opencorridor", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "10", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.closedcorridor", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "10", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.egress", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.lobby", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.warehouse", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.openoffice", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.privateoffice", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.restroom", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "300", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.lab", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.custom1", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.custom2", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.standalone", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"},
	{"default.highbay", "20", "200", "50", "10", "20", "10", "10", "0", "20", "1", "0", "0", "30", "50", "6:00 AM", "9:00 AM", "6:00 PM", "9:00 PM", "5"}}';

	profile_pahers text[] = ARRAY['profile.morning', 'profile.day', 'profile.evening', 'profile.night','weekend.profile.morning', 'weekend.profile.day', 'weekend.profile.evening', 'weekend.profile.night','holiday.profile.morning', 'holiday.profile.day', 'holiday.profile.evening', 'holiday.profile.night'];

	profile_cols text[] = ARRAY['min_level', 'on_level', 'ramp_up_time', 'motion_detect_duration', 'motion_sensitivity', 'ambient_sensitivity', 'manual_override_duration'];

	profiles_defaults int[] = ARRAY[
		--default
		[
			-- weekday [morning, day, evening, night]
			[0, 100, 0, 5, 1, 5, 60], [20, 100, 0, 15, 1, 5, 60], [0, 100, 0, 5, 1, 5, 60], [0, 100, 0, 5, 1, 0, 60], 
			-- weekend
			[0, 100, 0, 3, 1, 5, 60], [0, 100, 0, 3, 1, 5, 60], [0, 100, 0, 3, 1, 5, 60], [0, 100, 0, 3, 1, 0, 60], 
			-- holiday
			[0, 100, 0, 3, 1, 5, 60], [0, 100, 0, 3, 1, 5, 60], [0, 100, 0, 3, 1, 5, 60], [0, 100, 0, 3, 1, 0, 60]
		],
		--breakroom
		[
			[0, 75, 2, 3, 1, 5, 60], [20, 75, 2, 5, 1, 8, 60], [0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 0, 60], 
			[0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 0, 60], 
			[0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 0, 60]
		],
		--conferenceroom
		[
			[0, 75, 3, 15, 1, 8, 60], [20, 75, 3, 15, 1, 8, 60], [0, 75, 3, 15, 1, 8, 60], [0, 75, 3, 5, 1, 0, 60], 
			[0, 75, 3, 3, 1, 8, 60], [0, 75, 3, 3, 1, 8, 60], [0, 75, 3, 3, 1, 8, 60], [0, 75, 3, 3, 1, 0, 60], 
			[0, 75, 3, 3, 1, 8, 60], [0, 75, 3, 3, 1, 8, 60], [0, 75, 3, 3, 1, 8, 60], [0, 75, 3, 3, 1, 0, 60]
		],
		--opencorridor
		[
			[0, 75, 0, 10, 1, 5, 60], [20, 75, 0, 20, 1, 5, 60], [0, 75, 0, 10, 1, 5, 60], [0, 75, 0, 5, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60]
		],
		--closecorridor
		[
			[0, 50, 0, 5, 1, 5, 60], [20, 50, 0, 5, 1, 5, 60], [20, 50, 0, 5, 1, 5, 60], [0, 50, 0, 5, 1, 0, 60], 
			[0, 50, 0, 3, 1, 5, 60], [0, 50, 0, 3, 1, 5, 60], [0, 50, 0, 3, 1, 5, 60], [0, 50, 0, 3, 1, 0, 60], 
			[0, 50, 0, 3, 1, 5, 60], [0, 50, 0, 3, 1, 5, 60], [0, 50, 0, 3, 1, 5, 60], [0, 50, 0, 3, 1, 0, 60]
		],
		--egresslights
		[
			[0, 75, 0, 5, 1, 5, 60], [20, 75, 0, 15, 1, 5, 60], [0, 75, 0, 5, 1, 5, 60], [0, 75, 0, 5, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60]
		],
		--lobby
		[
			[40, 100, 4, 5, 1, 5, 60], [40, 100, 4, 15, 1, 5, 60], [40, 100, 4, 5, 1, 5, 60], [40, 100, 4, 5, 1, 0, 60], 
			[40, 100, 4, 3, 1, 5, 60], [40, 100, 4, 3, 1, 5, 60], [40, 100, 4, 3, 1, 5, 60], [40, 100, 4, 3, 1, 0, 60], 
			[40, 100, 4, 3, 1, 5, 60], [40, 100, 4, 3, 1, 5, 60], [40, 100, 4, 3, 1, 5, 60], [40, 100, 4, 3, 1, 0, 60]
		],
		--warehouse 
		[
			[0, 100, 0, 3, 2, 0, 60], [0, 100, 0, 5, 2, 0, 60], [0, 100, 0, 5, 2, 0, 60], [0, 100, 0, 3, 2, 0, 60], 
			[0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], 
			[0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60]
		],
		--openoffice
		[
			[0, 75, 0, 5, 1, 5, 60], [20, 75, 2, 15, 1, 5, 60], [0, 75, 0, 5, 1, 5, 60], [0, 75, 0, 5, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60]
		],
		--privateoffices
		[
			[0, 75, 2, 10, 1, 8, 60], [20, 75, 2, 20, 1, 8, 60], [0, 75, 2, 10, 1, 8, 60], [0, 75, 2, 5, 1, 0, 60], 
			[0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 0, 60], 
			[0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 0, 60]
		],
		--restroom 
		[
			[0, 75, 2, 3, 1, 5, 60], [20, 75, 2, 5, 1, 8, 60], [0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 0, 60], 
			[0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 0, 60], 
			[0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 8, 60], [0, 75, 2, 3, 1, 5, 60], [0, 75, 2, 3, 1, 0, 60]
		],
		--labs
		[
			[0, 100, 0, 30, 1, 3, 60], [30, 100, 0, 30, 1, 3, 60], [0, 100, 0, 5, 1, 3, 60], [0, 100, 0, 5, 1, 0, 60], 
			[0, 100, 0, 3, 1, 3, 60], [0, 100, 0, 3, 1, 3, 60], [0, 100, 0, 3, 1, 3, 60], [0, 100, 0, 3, 1, 0, 60], 
			[0, 100, 0, 3, 1, 3, 60], [0, 100, 0, 3, 1, 3, 60], [0, 100, 0, 3, 1, 3, 60], [0, 100, 0, 3, 1, 0, 60]
		],
		--custom1
		[
			[0, 75, 0, 5, 1, 5, 60], [20, 75, 2, 15, 1, 5, 60], [0, 75, 0, 5, 1, 5, 60], [0, 75, 0, 5, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60]
		],
		--custom2
		[
			[0, 75, 0, 5, 1, 5, 60], [20, 75, 2, 15, 1, 5, 60], [0, 75, 0, 5, 1, 5, 60], [0, 75, 0, 5, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60], 
			[0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 5, 60], [0, 75, 0, 3, 1, 0, 60]
		],
		--standalone
		[
			[35, 100, 0, 5, 1, 5, 60], [35, 100, 0, 15, 1, 5, 60], [35, 100, 0, 5, 1, 5, 60], [35, 100, 0, 5, 1, 0, 60], 
			[35, 100, 0, 3, 1, 5, 60], [35, 100, 0, 3, 1, 5, 60], [35, 100, 0, 3, 1, 5, 60], [35, 100, 0, 3, 1, 0, 60], 
			[35, 100, 0, 3, 1, 5, 60], [35, 100, 0, 3, 1, 5, 60], [35, 100, 0, 3, 1, 5, 60], [35, 100, 0, 3, 1, 0, 60]
		],
		--highbay
		[
			[0, 100, 0, 3, 2, 0, 60], [0, 100, 0, 5, 2, 0, 60], [0, 100, 0, 5, 2, 0, 60], [0, 100, 0, 3, 2, 0, 60], 
			[0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], 
			[0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60], [0, 100, 0, 10, 2, 0, 60]
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

--Added by Sreedhar 02/23
--
-- TOC entry 1798 (class 1259 OID 716506)
-- Dependencies: 1385
-- Name: energy_consumption_hourly_capture_at_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX energy_consumption_hourly_capture_at_index ON energy_consumption_hourly USING btree (capture_at);

--
-- TOC entry 1799 (class 1259 OID 716505)
-- Dependencies: 1385
-- Name: energy_consumption_hourly_fixture_id_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX energy_consumption_hourly_fixture_id_index ON energy_consumption_hourly USING btree (fixture_id);


--
-- TOC entry 1802 (class 1259 OID 716507)
-- Dependencies: 1385
-- Name: energy_consumption_power_used_hourly_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX energy_consumption_hourly_power_used_index ON energy_consumption_hourly USING btree (power_used);

--
-- TOC entry 1798 (class 1259 OID 716506)
-- Dependencies: 1385
-- Name: energy_consumption_daily_capture_at_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX energy_consumption_daily_capture_at_index ON energy_consumption_daily USING btree (capture_at);

--
-- TOC entry 1799 (class 1259 OID 716505)
-- Dependencies: 1385
-- Name: energy_consumption_daily_fixture_id_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX energy_consumption_daily_fixture_id_index ON energy_consumption_daily USING btree (fixture_id);

--
-- TOC entry 1802 (class 1259 OID 716507)
-- Dependencies: 1385
-- Name: energy_consumption_power_used_daily_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX energy_consumption_daily_power_used_index ON energy_consumption_daily USING btree (power_used);

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
BEGIN
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
--Added Yogesh 04/08
UPDATE ballasts set lamp_num=1 where lamp_type = 'LED';

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
		UPDATE fixture SET location = TEXTCAT(c.name,(TEXTCAT (' -> ', TEXTCAT(b.name, TEXTCAT(' -> ', f.name)))))			
		from campus c, 
		building b , 
		floor f,
		company co where
		floor_id=f.id and
		f.building_id=b.id and
		b.campus_id=c.id and
		c.company_id = co.id;

		UPDATE gateway SET location = TEXTCAT(c.name,(TEXTCAT (' -> ', TEXTCAT(b.name, TEXTCAT(' -> ', f.name)))))			
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

--Added by Sreedhar on 06/10
ALTER TABLE fixture ADD COLUMN voltage smallint DEFAULT 277;
UPDATE fixture set voltage = 277 WHERE voltage IS NULL;

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'fixture.default_voltage', '277');

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
CREATE FUNCTION update_sec_gateway_change() RETURNS "trigger"
    AS $$
	BEGIN
	  IF tg_op = 'UPDATE' THEN
	     IF old.sec_gw_id <> new.sec_gw_id THEN
		UPDATE fixture SET gateway_id = sec_gw_id;
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

INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture Out', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Push Profile', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Profile Mismatch', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Bad Profile', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture Upgrade', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Gateway Upgrade', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture CU Failure', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture Image Checksum Failure', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'DR Condition', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture associated Group Changed', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Bacnet', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Discovery', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Commissioning', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Profile Mismatch User Action', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture Hardware Failure', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture Too Hot', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture CPU Usage is High', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Gateway configuration error', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Erroneous Energy Reading', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Gateway Connection Failure', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'EM upgrade', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Scheduler', NULL);

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

ALTER TABLE USERS ADD COLUMN tenant_id bigint;
ALTER TABLE USERS ADD COLUMN  status character varying;
ALTER TABLE ONLY public.users  
    ADD CONSTRAINT user_tenant_id FOREIGN KEY(tenant_id) REFERENCES tenants(id);
    
--Set all the user status to active
update users set status='ACTIVE';

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

	FOR system_rec IN (
        SELECT capture_at AS capture_time, sum(power_used) AS load
        FROM energy_consumption
        WHERE capture_at <= toDate and capture_at > toDate - interval '1 hour' and zero_bucket != 1 GROUP BY capture_at)
        LOOP
                INSERT INTO system_energy_consumption (id, capture_at, power_used) VALUES (nextval('system_energy_consumption_seq'), system_rec.capture_time, system_rec.load);
        END LOOP;

        SELECT max(power_used) INTO peak_load1
        FROM system_energy_consumption
        WHERE capture_at <= toDate and capture_at > toDate - interval '1 hour';

        SELECT min(power_used) INTO min_load1
        FROM system_energy_consumption
        WHERE capture_at <= toDate and capture_at > toDate - interval '1 hour';

        INSERT INTO system_energy_consumption_hourly (id, capture_at, peak_load, min_load) VALUES (nextval('system_energy_consumption_hourly_seq'), toDate, peak_load1, min_load1);

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
		day_type := wd.type from fixture f, profile_configuration pc, profile_handler pr, weekday wd 
		             where wd.profile_configuration_id = pc.id and 
		                   pc.id = pr.profile_configuration_id and 	                   
		                   pr.id = f.profile_handler_id and
		                   wd.short_order = $2 and f.id = $1;
		 
		 select into w,x,y,z
		 				getDayQuadrantForGivenTime(morning_time), getDayQuadrantForGivenTime(day_time), getDayQuadrantForGivenTime(evening_time), getDayQuadrantForGivenTime(night_time)
		 		from fixture f, profile_configuration pc, profile_handler pr where pc.id = pr.profile_configuration_id and 
		                             f.profile_handler_id = pr.id  and f.id = $1;
		                             
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
		       THEN  profile_id := morning_profile_id from  fixture f, profile_handler pr where f.profile_handler_id = pr.id and f.id = $1;
		    ELSEIF quadrant_type = 'day' 
		       THEN  profile_id := day_profile_id from  fixture f, profile_handler pr where f.profile_handler_id = pr.id and f.id = $1;
		    ELSEIF quadrant_type = 'evening' 
		       THEN  profile_id := evening_profile_id from  fixture f, profile_handler pr where f.profile_handler_id = pr.id and f.id = $1;
		    ELSE
		        profile_id := night_profile_id from  fixture f, profile_handler pr where f.profile_handler_id = pr.id and f.id = $1;
		    END IF;
		ELSE
		     IF quadrant_type = 'morning' 
		       THEN  profile_id := morning_profile_weekend from  fixture f, profile_handler pr where f.profile_handler_id = pr.id and f.id = $1;
		    ELSEIF quadrant_type = 'day' 
		       THEN  profile_id := day_profile_weekend from  fixture f, profile_handler pr where f.profile_handler_id = pr.id and f.id = $1;
		    ELSEIF quadrant_type = 'evening' 
		       THEN  profile_id :=  evening_profile_weekend from  fixture f, profile_handler pr where f.profile_handler_id = pr.id and f.id = $1;
		    ELSE
		        profile_id := night_profile_weekend from  fixture f, profile_handler pr where f.profile_handler_id = pr.id and f.id = $1;
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

--Added by Sree 06/13
ALTER TABLE energy_consumption ADD COLUMN last_volts smallint DEFAULT 0;
ALTER TABLE energy_consumption ADD COLUMN saving_type smallint  DEFAULT 0;

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
  CONSTRAINT unique_device_name UNIQUE (name),
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
  location character varying(500)
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
 
  FOR rec IN (
    SELECT id, fixture_name, location, floor_id, area_id, campus_id, building_id, x, y, 
	mac_address, version, model_no FROM fixture)
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
	rec_gw.curr_no_pkts_to_nodes, rec_gw.last_connectivity_at, rec_gw.last_stats_rcvd_time, rec_gw.subnet_mask, rec_gw.default_gw, rec_gw.no_of_sensors, rec_gw.upgrade_status, rec_gw.boot_loader_version);

    UPDATE fixture set gateway_id = device_id, sec_gw_id = device_id where sec_gw_id = rec_gw.id;

    device_id = device_id + 1;
  END LOOP;
  
  ALTER TABLE gateway RENAME TO gateway_orig;
  ALTER TABLE gateway_new RENAME TO gateway;

  FOR rec_fix_stats IN (
    SELECT id, last_occupancy_seen, light_level, last_connectivity_at, last_stats_rcvd_time, profile_checksum, global_profile_checksum, avg_temperature, dimmer_control, wattage, current_state, curr_app, 	baseline_power, bulb_life FROM fixture) 
  LOOP
    INSERT INTO fixture_current_data VALUES (rec_fix_stats.id, rec_fix_stats.last_occupancy_seen, rec_fix_stats.light_level, rec_fix_stats.last_connectivity_at, rec_fix_stats.last_stats_rcvd_time, rec_fix_stats.profile_checksum, rec_fix_stats.global_profile_checksum, rec_fix_stats.avg_temperature, rec_fix_stats.dimmer_control, rec_fix_stats.wattage, rec_fix_stats.current_state, rec_fix_stats.curr_app, rec_fix_stats.baseline_power,
	rec_fix_stats.bulb_life);
  END LOOP;

  UPDATE fixture SET current_data_id = id;
 
end;
$$
LANGUAGE plpgsql;

SELECT restructure_schema();

ALTER TABLE fixture ALTER COLUMN floor_id DROP NOT NULL;

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

-- Added by Shilpa - To upgrade Quartz scheduler from 1.x to 2.x
--
-- drop tables that are no longer used
drop table qrtz_job_listeners;
drop table qrtz_trigger_listeners;

-- drop columns that are no longer used

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

CREATE OR REPLACE FUNCTION switch_groups_migration() RETURNS character varying
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

        insert into motion_group (id, group_no, gems_group_id) select nextval('motion_group_seq'), ggt.group_no, gg.id from gems_group_type ggt, gems_groups gg where gg.group_type = ggt.id and ggt.group_type = 'MotionGroup';

        insert into gems_groups (id, group_name, description, floor_id) select nextval('gems_group_seq'), sw.name, sw.id, sw.floor_id from switch sw;

        insert into gems_group_fixture (id, group_id, fixture_id, need_sync, user_action) select nextval('gems_group_fixture_seq'), gg.id , swf.fixture_id, 0, 0 from switch_fixtures swf, gems_groups gg where gg.description = cast(swf.switch_id as character varying) group by gg.id, swf.fixture_id;

        update switch set gems_groups_id = (select gg.id from gems_groups gg where gg.description = cast(switch.id as character varying));
        
        insert into switch_group (id, gems_group_id) select nextval('switch_group_seq'), sw.gems_groups_id from switch sw;
        
        update gems_group_fixture set user_action = 0 where user_action is null;

        offsetby := 0;

        for scene_count in ( select count(s1.id) from scene s1 group by s1.switch_id order by s1.switch_id)
        LOOP

            update scene set scene_order = (SELECT row_num - 1 FROM (SELECT ARRAY(SELECT s5.id FROM scene s5 where s5.switch_id = scene.switch_id order by s5.id) As order_id)  AS oldids CROSS JOIN generate_series(1, scene_count) AS row_num WHERE oldids.order_id[row_num] =  scene.id ORDER BY row_num) where scene.switch_id = (select s7.switch_id from scene s7 group by s7.switch_id order by s7.switch_id offset offsetby limit 1);
           
           offsetby := offsetby + 1;

        end LOOP;

        ALTER TABLE gems_groups DROP CONSTRAINT fk_gems_groups_company;
        ALTER TABLE gems_groups DROP CONSTRAINT gems_groups_group_type_fkey;
        ALTER TABLE gems_groups DROP COLUMN group_type;
        ALTER TABLE gems_groups DROP COLUMN company_id;

        ALTER TABLE switch DROP COLUMN dimmer_control;
        ALTER TABLE switch DROP COLUMN scene_id;
        ALTER TABLE switch DROP COLUMN active_control;

        DROP TABLE gems_group_type;
        DROP SEQUENCE gems_group_type_seq;
        DROP TABLE switch_fixtures;
        DROP SEQUENCE switch_fixtures_seq;

    end if;
	
    return tablename;
END;
$$
LANGUAGE plpgsql;

select switch_groups_migration();

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
