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

