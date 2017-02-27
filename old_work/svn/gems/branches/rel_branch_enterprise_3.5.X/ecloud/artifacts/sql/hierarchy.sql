CREATE TABLE facility_type
(
  id bigint,
  name character varying(50),
  CONSTRAINT facility_type_pkey PRIMARY KEY(id),
  CONSTRAINT facility_type_unique_name UNIQUE(name)

);

ALTER TABLE public.facility_type OWNER TO postgres;

CREATE SEQUENCE facility_type_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.facility_type_seq OWNER TO postgres;

INSERT INTO facility_type VALUES (nextval('facility_type_seq'), 'Organization');
INSERT INTO facility_type VALUES (nextval('facility_type_seq'), 'Campus'); 
INSERT INTO facility_type VALUES (nextval('facility_type_seq'), 'Building');
INSERT INTO facility_type VALUES (nextval('facility_type_seq'), 'Floor');
INSERT INTO facility_type VALUES (nextval('facility_type_seq'), 'Region');
INSERT INTO facility_type VALUES (nextval('facility_type_seq'), 'Zone');
INSERT INTO facility_type VALUES (nextval('facility_type_seq'), 'Area');
INSERT INTO facility_type VALUES (nextval('facility_type_seq'), 'Room');
INSERT INTO facility_type VALUES (nextval('facility_type_seq'), 'Site');

CREATE TABLE facility
(
  id bigint,
  customer_id bigint,
  name character varying(100),
  parent_id bigint,
  type bigint,
  hierarchy character varying(255),
  locX real,
  locY real,
  square_foot numeric(19, 2),
  CONSTRAINT facility_pkey PRIMARY KEY(id)
);

ALTER TABLE public.facility OWNER TO postgres;

CREATE SEQUENCE facility_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.facility_seq OWNER TO postgres;

CREATE TABLE facility_em_mapping
(
  id bigint,
  cust_id bigint,
  facility_id bigint,  
  em_id bigint,
  em_facility_type bigint,
  em_facility_id bigint,  
  CONSTRAINT facility_em_mapping_pk PRIMARY KEY(id)
);

ALTER TABLE public.facility_em_mapping OWNER TO postgres;

CREATE SEQUENCE facility_em_mapping_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.facility_em_mapping_seq OWNER TO postgres;

CREATE TABLE sensor_facility
(
  id bigint,
  sensor_id bigint,
  facility_id bigint,
  faclity_type bigint,
  CONSTRAINT sensor_facility_pk PRIMARY KEY(id)
);

ALTER TABLE public.sensor_facility OWNER TO postgres;

CREATE SEQUENCE sensor_facility_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.sensor_facility_seq OWNER TO postgres;

CREATE TABLE floor_energy_consumption_hourly
(
  id bigint NOT NULL,  
  cust_id bigint,
  level_id bigint,
  capture_at timestamp without time zone,
  base_energy numeric(19,2),
  energy numeric(19,2),  
  price double precision,
  base_cost double precision,
  cost double precision,  
  saved_energy numeric(19,2),
  saved_cost double precision,
  occ_savings numeric(19,2),
  tuneup_savings numeric(19,2),
  ambient_savings numeric(19,2),
  manual_savings numeric(19,2),
  avg_temp smallint,
  min_temp smallint,
  max_temp smallint,
  avg_amb smallint,
  min_amb smallint,
  max_amb smallint,
  motion_events bigint,
  no_of_records integer,
  CONSTRAINT floor_energy_consumption_hourly_pkey PRIMARY KEY (id),
  CONSTRAINT floor_unique_energy_consumption_hourly UNIQUE(capture_at, cust_id, level_id)
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

CREATE INDEX floor_energy_consumption_hourly_level_id_index ON floor_energy_consumption_hourly USING btree (level_id);

CREATE TABLE floor_energy_consumption_daily
(
  id bigint NOT NULL,  
  cust_id bigint,
  level_id bigint,
  capture_at timestamp without time zone,
  base_energy numeric(19,2),
  energy numeric(19,2),  
  price double precision,
  base_cost double precision,
  cost double precision,  
  saved_energy numeric(19,2),
  saved_cost double precision,
  occ_savings numeric(19,2),
  tuneup_savings numeric(19,2),
  ambient_savings numeric(19,2),
  manual_savings numeric(19,2),  
  avg_temp smallint,
  min_temp smallint,
  max_temp smallint,
  avg_amb smallint,
  min_amb smallint,
  max_amb smallint,
  motion_events bigint,
  no_of_records integer,
  CONSTRAINT floor_energy_consumption_daily_pkey PRIMARY KEY (id),
  CONSTRAINT floor_unique_energy_consumption_daily UNIQUE(capture_at, cust_id, level_id)
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

CREATE INDEX floor_energy_consumption_daily_level_id_index ON floor_energy_consumption_daily USING btree (level_id);

CREATE TABLE bld_energy_consumption_hourly
(
  id bigint NOT NULL,  
  cust_id bigint,
  level_id bigint,
  capture_at timestamp without time zone,
  base_energy numeric(19,2),
  energy numeric(19,2),  
  price double precision,
  base_cost double precision,
  cost double precision,  
  saved_energy numeric(19,2),
  saved_cost double precision,
  occ_savings numeric(19,2),
  tuneup_savings numeric(19,2),
  ambient_savings numeric(19,2),
  manual_savings numeric(19,2),  
  avg_temp smallint,
  min_temp smallint,
  max_temp smallint,
  avg_amb smallint,
  min_amb smallint,
  max_amb smallint,
  motion_events bigint,
  no_of_records integer,
  CONSTRAINT bld_energy_consumption_hourly_pkey PRIMARY KEY (id),
  CONSTRAINT bld_unique_energy_consumption_hourly UNIQUE(capture_at, cust_id, level_id)
);

ALTER TABLE bld_energy_consumption_hourly OWNER TO postgres;

CREATE SEQUENCE bld_energy_consumption_hourly_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE bld_energy_consumption_hourly_seq OWNER TO postgres;

CREATE INDEX bld_energy_consumption_hourly_capture_at_index ON bld_energy_consumption_hourly USING btree (capture_at);

CREATE INDEX bld_energy_consumption_hourly_level_id_index ON bld_energy_consumption_hourly USING btree (level_id);

CREATE TABLE bld_energy_consumption_daily
(
  id bigint NOT NULL,  
  cust_id bigint,
  level_id bigint,
  capture_at timestamp without time zone,
  base_energy numeric(19,2),
  energy numeric(19,2),  
  price double precision,
  base_cost double precision,
  cost double precision,  
  saved_energy numeric(19,2),
  saved_cost double precision,
  occ_savings numeric(19,2),
  tuneup_savings numeric(19,2),
  ambient_savings numeric(19,2),
  manual_savings numeric(19,2),  
  avg_temp smallint,
  min_temp smallint,
  max_temp smallint,
  avg_amb smallint,
  min_amb smallint,
  max_amb smallint,
  motion_events bigint,
  no_of_records integer,
  CONSTRAINT bld_energy_consumption_daily_pkey PRIMARY KEY (id),
  CONSTRAINT bld_unique_energy_consumption_daily UNIQUE(capture_at, cust_id, level_id)
);

ALTER TABLE bld_energy_consumption_daily OWNER TO postgres;

CREATE SEQUENCE bld_energy_consumption_daily_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE bld_energy_consumption_daily_seq OWNER TO postgres;

CREATE INDEX bld_energy_consumption_daily_capture_at_index ON bld_energy_consumption_daily USING btree (capture_at);

CREATE INDEX bld_energy_consumption_daily_level_id_index ON bld_energy_consumption_daily USING btree (level_id);

CREATE TABLE campus_energy_consumption_hourly
(
  id bigint NOT NULL,  
  cust_id bigint,
  level_id bigint,
  capture_at timestamp without time zone,
  base_energy numeric(19,2),
  energy numeric(19,2),  
  price double precision,
  base_cost double precision,
  cost double precision,  
  saved_energy numeric(19,2),
  saved_cost double precision,
  occ_savings numeric(19,2),
  tuneup_savings numeric(19,2),
  ambient_savings numeric(19,2),
  manual_savings numeric(19,2),  
  avg_temp smallint,
  min_temp smallint,
  max_temp smallint,
  avg_amb smallint,
  min_amb smallint,
  max_amb smallint,
  motion_events bigint,
  no_of_records integer,
  CONSTRAINT campus_energy_consumption_hourly_pkey PRIMARY KEY (id),
  CONSTRAINT campus_unique_energy_consumption_hourly UNIQUE(capture_at, cust_id, level_id)
);

ALTER TABLE campus_energy_consumption_hourly OWNER TO postgres;

CREATE SEQUENCE campus_energy_consumption_hourly_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE campus_energy_consumption_hourly_seq OWNER TO postgres;

CREATE INDEX campus_energy_consumption_hourly_capture_at_index ON campus_energy_consumption_hourly USING btree (capture_at);

CREATE INDEX campus_energy_consumption_hourly_level_id_index ON campus_energy_consumption_hourly USING btree (level_id);

CREATE TABLE campus_energy_consumption_daily
(
  id bigint NOT NULL,  
  cust_id bigint,
  level_id bigint,
  capture_at timestamp without time zone,
  base_energy numeric(19,2),
  energy numeric(19,2),  
  price double precision,
  base_cost double precision,
  cost double precision,  
  saved_energy numeric(19,2),
  saved_cost double precision,
  occ_savings numeric(19,2),
  tuneup_savings numeric(19,2),
  ambient_savings numeric(19,2),
  manual_savings numeric(19,2),  
  avg_temp smallint,
  min_temp smallint,
  max_temp smallint,
  avg_amb smallint,
  min_amb smallint,
  max_amb smallint,
  motion_events bigint,
  no_of_records integer,
  CONSTRAINT campus_energy_consumption_daily_pkey PRIMARY KEY (id),
  CONSTRAINT campus_unique_energy_consumption_daily UNIQUE(capture_at, cust_id, level_id)
);

ALTER TABLE campus_energy_consumption_daily OWNER TO postgres;

CREATE SEQUENCE campus_energy_consumption_daily_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE campus_energy_consumption_daily_seq OWNER TO postgres;

CREATE INDEX campus_energy_consumption_daily_capture_at_index ON campus_energy_consumption_daily USING btree (capture_at);

CREATE INDEX campus_energy_consumption_daily_level_id_index ON campus_energy_consumption_daily USING btree (level_id);

CREATE TABLE organization_energy_consumption_hourly
(
  id bigint NOT NULL,  
  cust_id bigint,
  level_id bigint,
  capture_at timestamp without time zone,
  base_energy numeric(19,2),
  energy numeric(19,2),  
  price double precision,
  base_cost double precision,
  cost double precision,  
  saved_energy numeric(19,2),
  saved_cost double precision,
  occ_savings numeric(19,2),
  tuneup_savings numeric(19,2),
  ambient_savings numeric(19,2),
  manual_savings numeric(19,2),  
  avg_temp smallint,
  min_temp smallint,
  max_temp smallint,
  avg_amb smallint,
  min_amb smallint,
  max_amb smallint,
  motion_events bigint,
  no_of_records integer,
  CONSTRAINT organization_energy_consumption_hourly_pkey PRIMARY KEY (id),
  CONSTRAINT organization_unique_energy_consumption_hourly UNIQUE(capture_at, cust_id, level_id)
);

ALTER TABLE organization_energy_consumption_hourly OWNER TO postgres;

CREATE SEQUENCE organization_energy_consumption_hourly_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE organization_energy_consumption_hourly_seq OWNER TO postgres;

CREATE INDEX organization_energy_consumption_hourly_capture_at_index ON organization_energy_consumption_hourly USING btree (capture_at);

CREATE INDEX organization_energy_consumption_hourly_level_id_index ON organization_energy_consumption_hourly USING btree (level_id);

CREATE TABLE organization_energy_consumption_daily
(
  id bigint NOT NULL,  
  cust_id bigint,
  level_id bigint,
  capture_at timestamp without time zone,
  base_energy numeric(19,2),
  energy numeric(19,2),  
  price double precision,
  base_cost double precision,
  cost double precision,  
  saved_energy numeric(19,2),
  saved_cost double precision,
  occ_savings numeric(19,2),
  tuneup_savings numeric(19,2),
  ambient_savings numeric(19,2),
  manual_savings numeric(19,2),  
  avg_temp smallint,
  min_temp smallint,
  max_temp smallint,
  avg_amb smallint,
  min_amb smallint,
  max_amb smallint,
  motion_events bigint,
  no_of_records integer,
  CONSTRAINT organization_energy_consumption_daily_pkey PRIMARY KEY (id),
  CONSTRAINT organization_unique_energy_consumption_daily UNIQUE(capture_at, cust_id, level_id)
);

ALTER TABLE organization_energy_consumption_daily OWNER TO postgres;

CREATE SEQUENCE organization_energy_consumption_daily_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE organization_energy_consumption_daily_seq OWNER TO postgres;

CREATE INDEX organization_energy_consumption_daily_capture_at_index ON organization_energy_consumption_daily USING btree (capture_at);

CREATE INDEX organization_energy_consumption_daily_level_id_index ON organization_energy_consumption_daily USING btree (level_id);

CREATE TABLE floor_energy_consumption_15min
(
  id bigint NOT NULL, 
  cust_id bigint,
  level_id bigint,
  capture_at timestamp without time zone,
  base_energy numeric(19,2),
  energy numeric(19,2), 
  price double precision,
  base_cost double precision,
  cost double precision, 
  saved_energy numeric(19,2),
  saved_cost double precision,
  occ_savings numeric(19,2),
  tuneup_savings numeric(19,2),
  ambient_savings numeric(19,2),
  manual_savings numeric(19,2),
  avg_temp smallint,
  min_temp smallint,
  max_temp smallint,
  avg_amb smallint,
  min_amb smallint,
  max_amb smallint,
  motion_events bigint,
  no_of_records integer,
  CONSTRAINT floor_energy_consumption_15min_pkey PRIMARY KEY (id),
  CONSTRAINT floor_unique_energy_consumption_15min UNIQUE(capture_at, cust_id, level_id)
);

ALTER TABLE floor_energy_consumption_15min OWNER TO postgres;

CREATE SEQUENCE floor_energy_consumption_15min_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE floor_energy_consumption_15min_seq OWNER TO postgres;

CREATE INDEX floor_energy_consumption_15min_capture_at_index ON floor_energy_consumption_15min USING btree (capture_at);

CREATE INDEX floor_energy_consumption_15min_level_id_index ON floor_energy_consumption_15min USING btree (level_id);

CREATE TABLE bld_energy_consumption_15min
(
  id bigint NOT NULL, 
  cust_id bigint,
  level_id bigint,
  capture_at timestamp without time zone,
  base_energy numeric(19,2),
  energy numeric(19,2), 
  price double precision,
  base_cost double precision,
  cost double precision, 
  saved_energy numeric(19,2),
  saved_cost double precision,
  occ_savings numeric(19,2),
  tuneup_savings numeric(19,2),
  ambient_savings numeric(19,2),
  manual_savings numeric(19,2),
  avg_temp smallint,
  min_temp smallint,
  max_temp smallint,
  avg_amb smallint,
  min_amb smallint,
  max_amb smallint,
  motion_events bigint,
  no_of_records integer,
  CONSTRAINT bld_energy_consumption_15min_pkey PRIMARY KEY (id),
  CONSTRAINT bld_unique_energy_consumption_15min UNIQUE(capture_at, cust_id, level_id)
);

ALTER TABLE bld_energy_consumption_15min OWNER TO postgres;

CREATE SEQUENCE bld_energy_consumption_15min_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE bld_energy_consumption_15min_seq OWNER TO postgres;

CREATE INDEX bld_energy_consumption_15min_capture_at_index ON bld_energy_consumption_15min USING btree (capture_at);

CREATE INDEX bld_energy_consumption_15min_level_id_index ON bld_energy_consumption_15min USING btree (level_id);

CREATE TABLE campus_energy_consumption_15min
(
  id bigint NOT NULL, 
  cust_id bigint,
  level_id bigint,
  capture_at timestamp without time zone,
  base_energy numeric(19,2),
  energy numeric(19,2), 
  price double precision,
  base_cost double precision,
  cost double precision, 
  saved_energy numeric(19,2),
  saved_cost double precision,
  occ_savings numeric(19,2),
  tuneup_savings numeric(19,2),
  ambient_savings numeric(19,2),
  manual_savings numeric(19,2),
  avg_temp smallint,
  min_temp smallint,
  max_temp smallint,
  avg_amb smallint,
  min_amb smallint,
  max_amb smallint,
  motion_events bigint,
  no_of_records integer,
  CONSTRAINT campus_energy_consumption_15min_pkey PRIMARY KEY (id),
  CONSTRAINT campus_unique_energy_consumption_15min UNIQUE(capture_at, cust_id, level_id)
);

ALTER TABLE campus_energy_consumption_15min OWNER TO postgres;

CREATE SEQUENCE campus_energy_consumption_15min_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE campus_energy_consumption_15min_seq OWNER TO postgres;

CREATE INDEX campus_energy_consumption_15min_capture_at_index ON campus_energy_consumption_15min USING btree (capture_at);

CREATE INDEX campus_energy_consumption_15min_level_id_index ON camus_energy_consumption_15min USING btree (level_id);

CREATE TABLE organization_energy_consumption_15min
(
  id bigint NOT NULL, 
  cust_id bigint,
  level_id bigint,
  capture_at timestamp without time zone,
  base_energy numeric(19,2),
  energy numeric(19,2), 
  price double precision,
  base_cost double precision,
  cost double precision, 
  saved_energy numeric(19,2),
  saved_cost double precision,
  occ_savings numeric(19,2),
  tuneup_savings numeric(19,2),
  ambient_savings numeric(19,2),
  manual_savings numeric(19,2),
  avg_temp smallint,
  min_temp smallint,
  max_temp smallint,
  avg_amb smallint,
  min_amb smallint,
  max_amb smallint,
  motion_events bigint,
  no_of_records integer,
  CONSTRAINT organization_energy_consumption_15min_pkey PRIMARY KEY (id),
  CONSTRAINT organization_unique_energy_consumption_15min UNIQUE(capture_at, cust_id, level_id)
);

ALTER TABLE organization_energy_consumption_15min OWNER TO postgres;

CREATE SEQUENCE organization_energy_consumption_15min_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE organization_energy_consumption_15min_seq OWNER TO postgres;

CREATE INDEX organization_energy_consumption_15min_capture_at_index ON organization_energy_consumption_15min USING btree (capture_at);

CREATE INDEX organization_energy_consumption_15min_level_id_index ON organization_energy_consumption_15min USING btree (level_id);

--Added by Sharad on 11/25
ALTER TABLE floor_energy_consumption_15min ADD COLUMN motion_events bigint;
ALTER TABLE bld_energy_consumption_15min ADD COLUMN motion_events bigint;
ALTER TABLE campus_energy_consumption_15min ADD COLUMN motion_events bigint;
ALTER TABLE organization_energy_consumption_15min ADD COLUMN motion_events bigint;

ALTER TABLE floor_energy_consumption_daily ADD COLUMN motion_events bigint;
ALTER TABLE bld_energy_consumption_daily ADD COLUMN motion_events bigint;
ALTER TABLE campus_energy_consumption_daily ADD COLUMN motion_events bigint;
ALTER TABLE organization_energy_consumption_daily ADD COLUMN motion_events bigint;

ALTER TABLE floor_energy_consumption_hourly ADD COLUMN motion_events bigint;
ALTER TABLE bld_energy_consumption_hourly ADD COLUMN motion_events bigint;
ALTER TABLE campus_energy_consumption_hourly ADD COLUMN motion_events bigint;
ALTER TABLE organization_energy_consumption_hourly ADD COLUMN motion_events bigint;

CREATE OR REPLACE FUNCTION aggregatehourlymotionevents(todate timestamp with time zone) RETURNS integer
    AS $$
DECLARE 

BEGIN
	--FOR system_rec IN (
        --SELECT capture_at AS capture_time, SUM(LENGTH(REPLACE(CAST(motion_bits::bit(64) AS TEXT), '0', '')))  AS motionevent
       -- FROM floor_energy_consumption_15min
       -- WHERE capture_at <= toDate and capture_at > toDate - interval '1 hour' GROUP BY capture_at)
       -- LOOP
 Raise Notice '=> %', now();
               -- UPDATE floor_energy_consumption_hourly (capture_at, motion_event) VALUES (system_rec.capture_time, system_rec.motionevent);
       -- END LOOP;
       RETURN 111111222;
END;
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aggregatedailymotionevents(todate timestamp with time zone) RETURNS void
    AS $$
DECLARE 
	
BEGIN
	--FOR system_rec IN (
       -- SELECT capture_at AS capture_time, SUM(LENGTH(REPLACE(CAST(motion_bits::bit(64) AS TEXT), '0', '')))  AS motionevent
       -- FROM floor_energy_consumption_hourly
      --  WHERE capture_at <= toDate and capture_at > toDate - interval '1 hour' GROUP BY capture_at)
      --  LOOP
             --   UPDATE floor_energy_consumption_daily (capture_at, motion_event) VALUES (system_rec.capture_time, system_rec.motionevent);
      --  END LOOP;
 Raise Notice '=> %', now();
END;
$$
LANGUAGE plpgsql;

--added by Sree 01/23
ALTER TABLE floor_energy_consumption_15min ADD COLUMN no_of_records integer;
ALTER TABLE floor_energy_consumption_hourly ADD COLUMN no_of_records integer;
ALTER TABLE floor_energy_consumption_daily ADD COLUMN no_of_records integer;
ALTER TABLE bld_energy_consumption_15min ADD COLUMN no_of_records integer;
ALTER TABLE bld_energy_consumption_hourly ADD COLUMN no_of_records integer;
ALTER TABLE bld_energy_consumption_daily ADD COLUMN no_of_records integer;
ALTER TABLE campus_energy_consumption_15min ADD COLUMN no_of_records integer;
ALTER TABLE campus_energy_consumption_hourly ADD COLUMN no_of_records integer;
ALTER TABLE campus_energy_consumption_daily ADD COLUMN no_of_records integer;
ALTER TABLE organization_energy_consumption_15min ADD COLUMN no_of_records integer;
ALTER TABLE organization_energy_consumption_hourly ADD COLUMN no_of_records integer;
ALTER TABLE organization_energy_consumption_daily ADD COLUMN no_of_records integer;

DROP TYPE hier_record;

CREATE TYPE hier_record AS (
	facility_id integer,
	cust_id integer,
	base_energy numeric,
	energy numeric,
	base_cost numeric,
	cost numeric,
	saved_energy numeric,
	saved_cost numeric,
	occ_savings numeric,
	tuneup_savings numeric,
	ambient_savings numeric,
	manual_savings numeric,
	agg_temp numeric,
	min_temp numeric,
	max_temp numeric,
	agg_amb integer,
	min_amb integer,
	max_amb integer,
	motion_events integer,
	no_of_rec integer
);

CREATE OR REPLACE FUNCTION aggHierEnergyCons(todate timestamp with time zone) RETURNS void
    AS $$
DECLARE 	
	hier_rec hier_record;
		
BEGIN

--building aggregation
FOR hier_rec IN (
	SELECT parent_id AS facility_id, cust_id, SUM(base_energy) AS base_energy, SUM(energy) AS energy, SUM(base_cost) as base_cost, SUM(cost) AS cost, SUM(saved_energy) as saved_energy, SUM(saved_cost) as saved_cost, SUM(occ_savings) as occ_savings, SUM(tuneup_savings) as tuneup_savings, SUM(ambient_savings) as ambient_savings, SUM(manual_savings) as manual_savings, SUM(avg_temp) as agg_temp, MIN(min_temp) AS min_temp, MAX(max_temp) AS max_temp, SUM(avg_amb) as agg_amb, MIN(min_amb) as min_amb, MAX(max_amb) as max_amb, SUM(motion_events) as motion_events, SUM(no_rec) AS no_of_rec
	FROM floor_energy_consumption_15min AS ec, facility AS f
	WHERE capture_at = toDate AND f.id = ec.level_id AND type = 4 GROUP BY parent_id, cust_id ORDER BY parent_id)
LOOP  
	INSERT INTO bld_energy_consumption_15min (id, cust_id, level_id, capture_at, base_energy, energy, base_cost, cost, saved_energy, saved_cost, occ_savings, tuneup_savings, ambient_savings, manual_savings, avg_temp, min_temp, max_temp, avg_amb, min_amb, max_amb, motion_events, no_of_records) VALUES (nextval('bld_energy_consumption_15min_seq'), hier_rec.cust_id, hier_rec.facility_id, toDate, hier_rec.base_energy, hier_rec.energy, hier_rec.base_cost, hier_rec.cost, hier_rec.saved_energy, hier_rec.saved_cost, hier_rec.occ_savings, hier_rec.tuneup_savings, hier_rec.ambient_savings, hier_rec.manual_savings, hier_rec.agg_temp, hier_rec.min_temp, hier_rec.max_temp, hier_rec.agg_amb, hier_rec.min_amb, hier_rec.max_amb, hier_rec.motion_events, hier_rec.no_of_rec);

END LOOP;

--campus aggregation
FOR hier_rec IN (
	SELECT parent_id AS facility_id, cust_id, SUM(base_energy) AS base_energy, SUM(energy) AS energy, SUM(base_cost) as base_cost, SUM(cost) AS cost, SUM(saved_energy) as saved_energy, SUM(saved_cost) as saved_cost, SUM(occ_savings) as occ_savings, SUM(tuneup_savings) as tuneup_savings, SUM(ambient_savings) as ambient_savings, SUM(manual_savings) as manual_savings, SUM(avg_temp) as agg_temp, MIN(min_temp) AS min_temp, MAX(max_temp) AS max_temp, SUM(avg_amb) as agg_amb, MIN(min_amb) as min_amb, MAX(max_amb) as max_amb, SUM(motion_events) as motion_events, SUM(no_rec) AS no_of_rec
	FROM bld_energy_consumption_15min AS ec, facility AS f
	WHERE capture_at = toDate AND f.id = ec.level_id AND type = 3 GROUP BY parent_id, cust_id ORDER BY parent_id)
LOOP  
	INSERT INTO campus_energy_consumption_15min (id, cust_id, level_id, capture_at, base_energy, energy, base_cost, cost, saved_energy, saved_cost, occ_savings, tuneup_savings, ambient_savings, manual_savings, avg_temp, min_temp, max_temp, avg_amb, min_amb, max_amb, motion_events, no_of_records) VALUES (nextval('campus_energy_consumption_15min_seq'), hier_rec.cust_id, hier_rec.facility_id, toDate, hier_rec.base_energy, hier_rec.energy, hier_rec.base_cost, hier_rec.cost, hier_rec.saved_energy, hier_rec.saved_cost, hier_rec.occ_savings, hier_rec.tuneup_savings, hier_rec.ambient_savings, hier_rec.manual_savings, hier_rec.agg_temp, hier_rec.min_temp, hier_rec.max_temp, hier_rec.agg_amb, hier_rec.min_amb, hier_rec.max_amb, hier_rec.motion_events, hier_rec.no_of_rec);

END LOOP;

--organization aggregation
FOR hier_rec IN (
	SELECT parent_id AS facility_id, cust_id, SUM(base_energy) AS base_energy, SUM(energy) AS energy, SUM(base_cost) as base_cost, SUM(cost) AS cost, SUM(saved_energy) as saved_energy, SUM(saved_cost) as saved_cost, SUM(occ_savings) as occ_savings, SUM(tuneup_savings) as tuneup_savings, SUM(ambient_savings) as ambient_savings, SUM(manual_savings) as manual_savings, SUM(avg_temp) as agg_temp, MIN(min_temp) AS min_temp, MAX(max_temp) AS max_temp, SUM(avg_amb) as agg_amb, MIN(min_amb) as min_amb, MAX(max_amb) as max_amb, SUM(motion_events) as motion_events, SUM(no_rec) AS no_of_rec
	FROM campus_energy_consumption_15min AS ec, facility AS f
	WHERE capture_at = toDate AND f.id = ec.level_id AND type = 3 GROUP BY parent_id, cust_id ORDER BY parent_id)
LOOP  
	INSERT INTO organization_energy_consumption_15min (id, cust_id, level_id, capture_at, base_energy, energy, base_cost, cost, saved_energy, saved_cost, occ_savings, tuneup_savings, ambient_savings, manual_savings, avg_temp, min_temp, max_temp, avg_amb, min_amb, max_amb, motion_events, no_of_records) VALUES (nextval('organization_energy_consumption_15min_seq'), hier_rec.cust_id, hier_rec.facility_id, toDate, hier_rec.base_energy, hier_rec.energy, hier_rec.base_cost, hier_rec.cost, hier_rec.saved_energy, hier_rec.saved_cost, hier_rec.occ_savings, hier_rec.tuneup_savings, hier_rec.ambient_savings, hier_rec.manual_savings, hier_rec.agg_temp, hier_rec.min_temp, hier_rec.max_temp, hier_rec.agg_amb, hier_rec.min_amb, hier_rec.max_amb, hier_rec.motion_events, hier_rec.no_of_rec);

END LOOP;

--if it is end of the hour, add it to hourly table
SELECT date_part('min', toDate) INTO curr_agg_min;
IF(curr_agg_min > 0)
THEN
	RETURN;
END IF;
--floor aggregation
FOR hier_rec IN (
	SELECT level_id AS facility_id, cust_id, SUM(base_energy) AS base_energy, SUM(energy) AS energy, SUM(base_cost) as base_cost, SUM(cost) AS cost, SUM(saved_energy) as saved_energy, SUM(saved_cost) as saved_cost, SUM(occ_savings) as occ_savings, SUM(tuneup_savings) as tuneup_savings, SUM(ambient_savings) as ambient_savings, SUM(manual_savings) as manual_savings, SUM(avg_temp) as agg_temp, MIN(min_temp) AS min_temp, MAX(max_temp) AS max_temp, SUM(avg_amb) as agg_amb, MIN(min_amb) as min_amb, MAX(max_amb) as max_amb, SUM(motion_events) as motion_events, SUM(no_rec) AS no_of_rec
	FROM floor_energy_consumption_15min AS ec
	WHERE capture_at = toDate GROUP BY level_id, cust_id ORDER BY level_id)
LOOP  
	INSERT INTO floor_energy_consumption_hourly (id, cust_id, level_id, capture_at, base_energy, energy, base_cost, cost, saved_energy, saved_cost, occ_savings, tuneup_savings, ambient_savings, manual_savings, avg_temp, min_temp, max_temp, avg_amb, min_amb, max_amb, motion_events, no_of_records) VALUES (nextval('floor_energy_consumption_hourly_seq'), hier_rec.cust_id, hier_rec.facility_id, toDate, hier_rec.base_energy, hier_rec.energy, hier_rec.base_cost, hier_rec.cost, hier_rec.saved_energy, hier_rec.saved_cost, hier_rec.occ_savings, hier_rec.tuneup_savings, hier_rec.ambient_savings, hier_rec.manual_savings, hier_rec.agg_temp, hier_rec.min_temp, hier_rec.max_temp, hier_rec.agg_amb, hier_rec.min_amb, hier_rec.max_amb, hier_rec.motion_events, hier_rec.no_of_rec);

END LOOP;

--building aggregation
FOR hier_rec IN (
	SELECT level_id AS facility_id, cust_id, SUM(base_energy) AS base_energy, SUM(energy) AS energy, SUM(base_cost) as base_cost, SUM(cost) AS cost, SUM(saved_energy) as saved_energy, SUM(saved_cost) as saved_cost, SUM(occ_savings) as occ_savings, SUM(tuneup_savings) as tuneup_savings, SUM(ambient_savings) as ambient_savings, SUM(manual_savings) as manual_savings, SUM(avg_temp) as agg_temp, MIN(min_temp) AS min_temp, MAX(max_temp) AS max_temp, SUM(avg_amb) as agg_amb, MIN(min_amb) as min_amb, MAX(max_amb) as max_amb, SUM(motion_events) as motion_events, SUM(no_rec) AS no_of_rec
	FROM bld_energy_consumption_15min AS ec
	WHERE capture_at = toDate GROUP BY level_id, cust_id ORDER BY level_id)
LOOP  
	INSERT INTO bld_energy_consumption_hourly (id, cust_id, level_id, capture_at, base_energy, energy, base_cost, cost, saved_energy, saved_cost, occ_savings, tuneup_savings, ambient_savings, manual_savings, avg_temp, min_temp, max_temp, avg_amb, min_amb, max_amb, motion_events, no_of_records) VALUES (nextval('bld_energy_consumption_hourly_seq'), hier_rec.cust_id, hier_rec.facility_id, toDate, hier_rec.base_energy, hier_rec.energy, hier_rec.base_cost, hier_rec.cost, hier_rec.saved_energy, hier_rec.saved_cost, hier_rec.occ_savings, hier_rec.tuneup_savings, hier_rec.ambient_savings, hier_rec.manual_savings, hier_rec.agg_temp, hier_rec.min_temp, hier_rec.max_temp, hier_rec.agg_amb, hier_rec.min_amb, hier_rec.max_amb, hier_rec.motion_events, hier_rec.no_of_rec);

END LOOP;

--campus aggregation
FOR hier_rec IN (
	SELECT level_id AS facility_id, cust_id, SUM(base_energy) AS base_energy, SUM(energy) AS energy, SUM(base_cost) as base_cost, SUM(cost) AS cost, SUM(saved_energy) as saved_energy, SUM(saved_cost) as saved_cost, SUM(occ_savings) as occ_savings, SUM(tuneup_savings) as tuneup_savings, SUM(ambient_savings) as ambient_savings, SUM(manual_savings) as manual_savings, SUM(avg_temp) as agg_temp, MIN(min_temp) AS min_temp, MAX(max_temp) AS max_temp, SUM(avg_amb) as agg_amb, MIN(min_amb) as min_amb, MAX(max_amb) as max_amb, SUM(motion_events) as motion_events, SUM(no_rec) AS no_of_rec
	FROM campus_energy_consumption_15min AS ec
	WHERE capture_at = toDate GROUP BY level_id, cust_id ORDER BY level_id)
LOOP  
	INSERT INTO campus_energy_consumption_hourly (id, cust_id, level_id, capture_at, base_energy, energy, base_cost, cost, saved_energy, saved_cost, occ_savings, tuneup_savings, ambient_savings, manual_savings, avg_temp, min_temp, max_temp, avg_amb, min_amb, max_amb, motion_events, no_of_records) VALUES (nextval('campus_energy_consumption_hourly_seq'), hier_rec.cust_id, hier_rec.facility_id, toDate, hier_rec.base_energy, hier_rec.energy, hier_rec.base_cost, hier_rec.cost, hier_rec.saved_energy, hier_rec.saved_cost, hier_rec.occ_savings, hier_rec.tuneup_savings, hier_rec.ambient_savings, hier_rec.manual_savings, hier_rec.agg_temp, hier_rec.min_temp, hier_rec.max_temp, hier_rec.agg_amb, hier_rec.min_amb, hier_rec.max_amb, hier_rec.motion_events, hier_rec.no_of_rec);

END LOOP;

--organization aggregation
FOR hier_rec IN (
	SELECT level_id AS facility_id, cust_id, SUM(base_energy) AS base_energy, SUM(energy) AS energy, SUM(base_cost) as base_cost, SUM(cost) AS cost, SUM(saved_energy) as saved_energy, SUM(saved_cost) as saved_cost, SUM(occ_savings) as occ_savings, SUM(tuneup_savings) as tuneup_savings, SUM(ambient_savings) as ambient_savings, SUM(manual_savings) as manual_savings, SUM(avg_temp) as agg_temp, MIN(min_temp) AS min_temp, MAX(max_temp) AS max_temp, SUM(avg_amb) as agg_amb, MIN(min_amb) as min_amb, MAX(max_amb) as max_amb, SUM(motion_events) as motion_events, SUM(no_rec) AS no_of_rec
	FROM organization_energy_consumption_15min AS ec
	WHERE capture_at = toDate GROUP BY level_id, cust_id ORDER BY level_id)
LOOP  
	INSERT INTO organization_energy_consumption_hourly (id, cust_id, level_id, capture_at, base_energy, energy, base_cost, cost, saved_energy, saved_cost, occ_savings, tuneup_savings, ambient_savings, manual_savings, avg_temp, min_temp, max_temp, avg_amb, min_amb, max_amb, motion_events, no_of_records) VALUES (nextval('organization_energy_consumption_hourly_seq'), hier_rec.cust_id, hier_rec.facility_id, toDate, hier_rec.base_energy, hier_rec.energy, hier_rec.base_cost, hier_rec.cost, hier_rec.saved_energy, hier_rec.saved_cost, hier_rec.occ_savings, hier_rec.tuneup_savings, hier_rec.ambient_savings, hier_rec.manual_savings, hier_rec.agg_temp, hier_rec.min_temp, hier_rec.max_temp, hier_rec.agg_amb, hier_rec.min_amb, hier_rec.max_amb, hier_rec.motion_events, hier_rec.no_of_rec);

END LOOP;

END;
$$
LANGUAGE plpgsql;
