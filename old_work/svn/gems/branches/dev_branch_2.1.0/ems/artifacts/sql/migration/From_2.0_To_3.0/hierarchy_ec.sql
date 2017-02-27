--
-- Name: floor_energy_consumption_hourly; Type: TABLE; Owner: postgres; 
--

ALTER TABLE energy_consumption_daily ADD COLUMN avg_load numeric(19,2);

CREATE TABLE floor_energy_consumption_hourly
(
  id bigint NOT NULL,
  min_temperature smallint,
  max_temperature smallint,
  avg_temperature smallint,
  light_on_seconds smallint,
  light_min_level smallint,
  light_max_level smallint,
  light_avg_level smallint,
  light_on smallint,
  light_off smallint,
  power_used numeric(19,2),
  occ_in smallint,
  occ_out smallint,
  occ_count smallint,
  dim_percentage smallint,
  dim_offset smallint,
  bright_percentage smallint,
  bright_offset smallint,
  capture_at timestamp without time zone,
  floor_id bigint,
  price double precision,
  cost double precision,
  base_power_used numeric(19,2),
  base_cost double precision,
  saved_power_used numeric(19,2),
  saved_cost double precision,
  occ_saving numeric(19,2),
  tuneup_saving numeric(19,2),
  ambient_saving numeric(19,2),
  manual_saving numeric(19,2),
  peak_load numeric(19,2),
  min_load numeric(19,2),
  min_price double precision,
  max_price double precision,
  avg_load numeric(19,2),
  CONSTRAINT floor_energy_consumption_hourly_pkey PRIMARY KEY (id),
  CONSTRAINT floor_unique_energy_consumption_hourly UNIQUE(capture_at, floor_id)
);

ALTER TABLE floor_energy_consumption_hourly OWNER TO postgres;

--
-- Name: floor_energy_consumption_hourly_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE floor_energy_consumption_hourly_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE floor_energy_consumption_hourly_seq OWNER TO postgres;

--
-- TOC entry 1798 (class 1259 OID 716506)
-- Dependencies: 1385
-- Name: floor_energy_consumption_hourly_capture_at_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX floor_energy_consumption_hourly_capture_at_index ON floor_energy_consumption_hourly USING btree (capture_at);

--
-- TOC entry 1799 (class 1259 OID 716505)
-- Dependencies: 1385
-- Name: floor_energy_consumption_hourly_floor_id_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX floor_energy_consumption_hourly_floor_id_index ON floor_energy_consumption_hourly USING btree (floor_id);


--
-- Name: bld_energy_consumption_hourly; Type: TABLE; Owner: postgres; 
--

CREATE TABLE bld_energy_consumption_hourly
(
  id bigint NOT NULL,
  min_temperature smallint,
  max_temperature smallint,
  avg_temperature smallint,
  light_on_seconds smallint,
  light_min_level smallint,
  light_max_level smallint,
  light_avg_level smallint,
  light_on smallint,
  light_off smallint,
  power_used numeric(19,2),
  occ_in smallint,
  occ_out smallint,
  occ_count smallint,
  dim_percentage smallint,
  dim_offset smallint,
  bright_percentage smallint,
  bright_offset smallint,
  capture_at timestamp without time zone,
  bld_id bigint,
  price double precision,
  cost double precision,
  base_power_used numeric(19,2),
  base_cost double precision,
  saved_power_used numeric(19,2),
  saved_cost double precision,
  occ_saving numeric(19,2),
  tuneup_saving numeric(19,2),
  ambient_saving numeric(19,2),
  manual_saving numeric(19,2),
  peak_load numeric(19,2),
  min_load numeric(19,2),
  min_price double precision,
  max_price double precision,
  avg_load numeric(19,2),
  CONSTRAINT bld_energy_consumption_hourly_pkey PRIMARY KEY (id),
  CONSTRAINT bld_unique_energy_consumption_hourly UNIQUE(capture_at, bld_id)
);

ALTER TABLE bld_energy_consumption_hourly OWNER TO postgres;

--
-- Name: bld_energy_consumption_hourly_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE bld_energy_consumption_hourly_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE bld_energy_consumption_hourly_seq OWNER TO postgres;

--
-- TOC entry 1798 (class 1259 OID 716506)
-- Dependencies: 1385
-- Name: bld_energy_consumption_hourly_capture_at_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bld_energy_consumption_hourly_capture_at_index ON bld_energy_consumption_hourly USING btree (capture_at);

--
-- TOC entry 1799 (class 1259 OID 716505)
-- Dependencies: 1385
-- Name: bld_energy_consumption_hourly_bld_id_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bld_energy_consumption_hourly_bld_id_index ON bld_energy_consumption_hourly USING btree (bld_id);

--
-- Name: campus_energy_consumption_hourly; Type: TABLE; Owner: postgres; 
--

CREATE TABLE campus_energy_consumption_hourly
(
  id bigint NOT NULL,
  min_temperature smallint,
  max_temperature smallint,
  avg_temperature smallint,
  light_on_seconds smallint,
  light_min_level smallint,
  light_max_level smallint,
  light_avg_level smallint,
  light_on smallint,
  light_off smallint,
  power_used numeric(19,2),
  occ_in smallint,
  occ_out smallint,
  occ_count smallint,
  dim_percentage smallint,
  dim_offset smallint,
  bright_percentage smallint,
  bright_offset smallint,
  capture_at timestamp without time zone,
  campus_id bigint,
  price double precision,
  cost double precision,
  base_power_used numeric(19,2),
  base_cost double precision,
  saved_power_used numeric(19,2),
  saved_cost double precision,
  occ_saving numeric(19,2),
  tuneup_saving numeric(19,2),
  ambient_saving numeric(19,2),
  manual_saving numeric(19,2),
  peak_load numeric(19,2),
  min_load numeric(19,2),
  min_price double precision,
  max_price double precision,
  avg_load numeric(19,2),
  CONSTRAINT campus_energy_consumption_hourly_pkey PRIMARY KEY (id),
  CONSTRAINT campus_unique_energy_consumption_hourly UNIQUE(capture_at, campus_id)
);

ALTER TABLE campus_energy_consumption_hourly OWNER TO postgres;

--
-- Name: campus_energy_consumption_hourly_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE campus_energy_consumption_hourly_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE campus_energy_consumption_hourly_seq OWNER TO postgres;

--
-- TOC entry 1798 (class 1259 OID 716506)
-- Dependencies: 1385
-- Name: campus_energy_consumption_hourly_capture_at_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX campus_energy_consumption_hourly_capture_at_index ON campus_energy_consumption_hourly USING btree (capture_at);

--
-- TOC entry 1799 (class 1259 OID 716505)
-- Dependencies: 1385
-- Name: campus_energy_consumption_hourly_campus_id_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX campus_energy_consumption_hourly_campus_id_index ON campus_energy_consumption_hourly USING btree (campus_id);

--
-- Name: company_energy_consumption_hourly; Type: TABLE; Owner: postgres; 
--

CREATE TABLE company_energy_consumption_hourly
(
  id bigint NOT NULL,
  min_temperature smallint,
  max_temperature smallint,
  avg_temperature smallint,
  light_on_seconds smallint,
  light_min_level smallint,
  light_max_level smallint,
  light_avg_level smallint,
  light_on smallint,
  light_off smallint,
  power_used numeric(19,2),
  occ_in smallint,
  occ_out smallint,
  occ_count smallint,
  dim_percentage smallint,
  dim_offset smallint,
  bright_percentage smallint,
  bright_offset smallint,
  capture_at timestamp without time zone,
  price double precision,
  cost double precision,
  base_power_used numeric(19,2),
  base_cost double precision,
  saved_power_used numeric(19,2),
  saved_cost double precision,
  occ_saving numeric(19,2),
  tuneup_saving numeric(19,2),
  ambient_saving numeric(19,2),
  manual_saving numeric(19,2),
  peak_load numeric(19,2),
  min_load numeric(19,2),
  min_price double precision,
  max_price double precision,
  avg_load numeric(19,2),
  CONSTRAINT company_energy_consumption_hourly_pkey PRIMARY KEY (id),
  CONSTRAINT company_unique_energy_consumption_hourly UNIQUE(capture_at)
);

ALTER TABLE company_energy_consumption_hourly OWNER TO postgres;

--
-- Name: company_energy_consumption_hourly_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE company_energy_consumption_hourly_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE company_energy_consumption_hourly_seq OWNER TO postgres;

--
-- TOC entry 1798 (class 1259 OID 716506)
-- Dependencies: 1385
-- Name: company_energy_consumption_hourly_capture_at_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX company_energy_consumption_hourly_capture_at_index ON company_energy_consumption_hourly USING btree (capture_at);

--
-- Name: fixture_hour_record; Type: TYPE; Schema: public; Owner: postgres
--

DROP TYPE hier_hour_record;

CREATE TYPE hier_hour_record AS (
	hier_id integer,
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
	price numeric,
	min_price numeric,
	max_price numeric,
	avg_load numeric,
	no_of_rec int
);

DROP TYPE company_hour_record;

CREATE TYPE company_hour_record AS (	
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
	price numeric,
	min_price numeric,
	max_price numeric,
	avg_load numeric,
	no_of_rec int
);

CREATE OR REPLACE FUNCTION aggregatehourlyenergyconsumption(todate timestamp with time zone) RETURNS void
    AS $$
DECLARE 
	rec fixture_hour_record;	
	hier_rec hier_hour_record;
	company_rec company_hour_record;
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

--floor aggregation
FOR hier_rec IN (
	SELECT floor_id, SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(ec.avg_temperature) AS avg_temp, SUM(base_power_used) AS base_power, sum(base_cost) AS base_cost, SUM(saved_power_used) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_saving) AS occ_saving, SUM(ambient_saving) AS amb_saving, SUM(tuneup_saving) AS tune_saving, SUM(manual_saving) AS manual_saving, AVG(price) AS price, min(price) AS min_price, max(price) AS max_price, sum(avg_load) AS avg_load, count(*) AS no_of_rec
	FROM energy_consumption_hourly as ec, fixture as f
	WHERE capture_at = toDate and f.id = ec.fixture_id and base_power_used != 0 GROUP BY floor_id)
	LOOP  
	  IF hier_rec.no_of_rec IS NULL THEN
	    INSERT INTO floor_energy_consumption_hourly (id, floor_id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, min_price, max_price, avg_load) VALUES (nextval('floor_energy_consumption_hourly_seq'), hier_rec.hier_id, 0, 0, 0, toDate, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	  ELSE
		INSERT INTO floor_energy_consumption_hourly (id, floor_id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, min_price, max_price, avg_load) VALUES (nextval('floor_energy_consumption_hourly_seq'), hier_rec.hier_id, hier_rec.agg_power, hier_rec.price, hier_rec.agg_cost, toDate, hier_rec.min_temp, hier_rec.max_temp, round(hier_rec.avg_temp), hier_rec.base_power, hier_rec.base_cost, hier_rec.saved_power, hier_rec.saved_cost, hier_rec.occ_saving, hier_rec.amb_saving, hier_rec.tune_saving, hier_rec.manual_saving, hier_rec.min_price, hier_rec.max_price, hier_rec.avg_load);
	  END IF;
	END LOOP;

--building aggregation
FOR hier_rec IN (
	SELECT building_id, SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(ec.avg_temperature) AS avg_temp, SUM(base_power_used) AS base_power, sum(base_cost) AS base_cost, SUM(saved_power_used) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_saving) AS occ_saving, SUM(ambient_saving) AS amb_saving, SUM(tuneup_saving) AS tune_saving, SUM(manual_saving) AS manual_saving, AVG(price) AS price, min(price) AS min_price, max(price) AS max_price, sum(avg_load) AS avg_load, count(*) AS no_of_rec
	FROM energy_consumption_hourly as ec, fixture as f
	WHERE capture_at = toDate and f.id = ec.fixture_id and base_power_used != 0 GROUP BY building_id)
	LOOP  
	  IF hier_rec.no_of_rec IS NULL THEN
	    INSERT INTO bld_energy_consumption_hourly (id, bld_id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, min_price, max_price, avg_load) VALUES (nextval('bld_energy_consumption_hourly_seq'), hier_rec.hier_id, 0, 0, 0, toDate, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	  ELSE
		INSERT INTO bld_energy_consumption_hourly (id, bld_id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, min_price, max_price, avg_load) VALUES (nextval('bld_energy_consumption_hourly_seq'), hier_rec.hier_id, hier_rec.agg_power, hier_rec.price, hier_rec.agg_cost, toDate, hier_rec.min_temp, hier_rec.max_temp, round(hier_rec.avg_temp), hier_rec.base_power, hier_rec.base_cost, hier_rec.saved_power, hier_rec.saved_cost, hier_rec.occ_saving, hier_rec.amb_saving, hier_rec.tune_saving, hier_rec.manual_saving, hier_rec.min_price, hier_rec.max_price, hier_rec.avg_load);
	  END IF;
	END LOOP;

--campus aggregation
FOR hier_rec IN (
	SELECT campus_id, SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(ec.avg_temperature) AS avg_temp, SUM(base_power_used) AS base_power, sum(base_cost) AS base_cost, SUM(saved_power_used) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_saving) AS occ_saving, SUM(ambient_saving) AS amb_saving, SUM(tuneup_saving) AS tune_saving, SUM(manual_saving) AS manual_saving, AVG(price) AS price, min(price) AS min_price, max(price) AS max_price, sum(avg_load) AS avg_load, count(*) AS no_of_rec
	FROM energy_consumption_hourly as ec, fixture as f
	WHERE capture_at = toDate and f.id = ec.fixture_id and base_power_used != 0 GROUP BY campus_id)
	LOOP  
	  IF hier_rec.no_of_rec IS NULL THEN
	    INSERT INTO campus_energy_consumption_hourly (id, campus_id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, min_price, max_price, avg_load) VALUES (nextval('campus_energy_consumption_hourly_seq'), hier_rec.hier_id, 0, 0, 0, toDate, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	  ELSE
		INSERT INTO campus_energy_consumption_hourly (id, campus_id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, min_price, max_price, avg_load) VALUES (nextval('campus_energy_consumption_hourly_seq'), hier_rec.hier_id, hier_rec.agg_power, hier_rec.price, hier_rec.agg_cost, toDate, hier_rec.min_temp, hier_rec.max_temp, round(hier_rec.avg_temp), hier_rec.base_power, hier_rec.base_cost, hier_rec.saved_power, hier_rec.saved_cost, hier_rec.occ_saving, hier_rec.amb_saving, hier_rec.tune_saving, hier_rec.manual_saving, hier_rec.min_price, hier_rec.max_price, hier_rec.avg_load);
	  END IF;
	END LOOP;

--company aggregation
FOR company_rec IN (
	SELECT SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(ec.avg_temperature) AS avg_temp, SUM(base_power_used) AS base_power, sum(base_cost) AS base_cost, SUM(saved_power_used) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_saving) AS occ_saving, SUM(ambient_saving) AS amb_saving, SUM(tuneup_saving) AS tune_saving, SUM(manual_saving) AS manual_saving, AVG(price) AS price, min(price) AS min_price, max(price) AS max_price, sum(avg_load) AS avg_load, count(*) AS no_of_rec
	FROM energy_consumption_hourly as ec
	WHERE capture_at = toDate and base_power_used != 0)
	LOOP  
	  IF company_rec.no_of_rec IS NULL THEN
	    INSERT INTO company_energy_consumption_hourly (id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, min_price, max_price, avg_load) VALUES (nextval('company_energy_consumption_hourly_seq'), 0, 0, 0, toDate, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	  ELSE
		INSERT INTO company_energy_consumption_hourly (id, power_used, price, cost, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, min_price, max_price, avg_load) VALUES (nextval('company_energy_consumption_hourly_seq'), hier_rec.agg_power, hier_rec.price, hier_rec.agg_cost, toDate, hier_rec.min_temp, hier_rec.max_temp, round(hier_rec.avg_temp), hier_rec.base_power, hier_rec.base_cost, hier_rec.saved_power, hier_rec.saved_cost, hier_rec.occ_saving, hier_rec.amb_saving, hier_rec.tune_saving, hier_rec.manual_saving, hier_rec.min_price, hier_rec.max_price, hier_rec.avg_load);
	  END IF;
	END LOOP;

END;
$$
LANGUAGE plpgsql;

--
-- Name: floor_energy_consumption_daily; Type: TABLE; Owner: postgres; 
--

CREATE TABLE floor_energy_consumption_daily
(
  id bigint NOT NULL,
  min_temperature smallint,
  max_temperature smallint,
  avg_temperature smallint,
  light_on_seconds smallint,
  light_min_level smallint,
  light_max_level smallint,
  light_avg_level smallint,
  light_on smallint,
  light_off smallint,
  power_used numeric(19,2),
  occ_in smallint,
  occ_out smallint,
  occ_count smallint,
  dim_percentage smallint,
  dim_offset smallint,
  bright_percentage smallint,
  bright_offset smallint,
  capture_at timestamp without time zone,
  floor_id bigint,
  price double precision,
  cost double precision,
  base_power_used numeric(19,2),
  base_cost double precision,
  saved_power_used numeric(19,2),
  saved_cost double precision,
  occ_saving numeric(19,2),
  tuneup_saving numeric(19,2),
  ambient_saving numeric(19,2),
  manual_saving numeric(19,2),
  peak_load numeric(19,2),
  min_load numeric(19,2),
  avg_load numeric(19,2),
  min_price double precision,
  max_price double precision,
  CONSTRAINT floor_energy_consumption_daily_pkey PRIMARY KEY (id),
  CONSTRAINT floor_unique_energy_consumption_daily UNIQUE(capture_at, floor_id)
);

ALTER TABLE floor_energy_consumption_daily OWNER TO postgres;

--
-- Name: floor_energy_consumption_daily_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE floor_energy_consumption_daily_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE floor_energy_consumption_daily_seq OWNER TO postgres;

--
-- TOC entry 1798 (class 1259 OID 716506)
-- Dependencies: 1385
-- Name: floor_energy_consumption_daily_capture_at_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX floor_energy_consumption_daily_capture_at_index ON floor_energy_consumption_daily USING btree (capture_at);

--
-- TOC entry 1799 (class 1259 OID 716505)
-- Dependencies: 1385
-- Name: floor_energy_consumption_daily_floor_id_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX floor_energy_consumption_daily_floor_id_index ON floor_energy_consumption_daily USING btree (floor_id);

--
-- Name: bld_energy_consumption_daily; Type: TABLE; Owner: postgres; 
--

CREATE TABLE bld_energy_consumption_daily
(
  id bigint NOT NULL,
  min_temperature smallint,
  max_temperature smallint,
  avg_temperature smallint,
  light_on_seconds smallint,
  light_min_level smallint,
  light_max_level smallint,
  light_avg_level smallint,
  light_on smallint,
  light_off smallint,
  power_used numeric(19,2),
  occ_in smallint,
  occ_out smallint,
  occ_count smallint,
  dim_percentage smallint,
  dim_offset smallint,
  bright_percentage smallint,
  bright_offset smallint,
  capture_at timestamp without time zone,
  bld_id bigint,
  price double precision,
  cost double precision,
  base_power_used numeric(19,2),
  base_cost double precision,
  saved_power_used numeric(19,2),
  saved_cost double precision,
  occ_saving numeric(19,2),
  tuneup_saving numeric(19,2),
  ambient_saving numeric(19,2),
  manual_saving numeric(19,2),
  peak_load numeric(19,2),
  min_load numeric(19,2),
  avg_load numeric(19,2),
  min_price double precision,
  max_price double precision,
  CONSTRAINT bld_energy_consumption_daily_pkey PRIMARY KEY (id),
  CONSTRAINT bld_unique_energy_consumption_daily UNIQUE(capture_at, bld_id)
);

ALTER TABLE bld_energy_consumption_daily OWNER TO postgres;

--
-- Name: bld_energy_consumption_daily_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bld_energy_consumption_daily_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE bld_energy_consumption_daily_seq OWNER TO postgres;

--
-- TOC entry 1798 (class 1259 OID 716506)
-- Dependencies: 1385
-- Name: bld_energy_consumption_daily_capture_at_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bld_energy_consumption_daily_capture_at_index ON bld_energy_consumption_daily USING btree (capture_at);

--
-- TOC entry 1799 (class 1259 OID 716505)
-- Dependencies: 1385
-- Name: bld_energy_consumption_daily_fixture_id_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX bld_energy_consumption_daily_bld_id_index ON bld_energy_consumption_daily USING btree (bld_id);

--
-- Name: campus_energy_consumption_daily; Type: TABLE; Owner: postgres; 
--

CREATE TABLE campus_energy_consumption_daily
(
  id bigint NOT NULL,
  min_temperature smallint,
  max_temperature smallint,
  avg_temperature smallint,
  light_on_seconds smallint,
  light_min_level smallint,
  light_max_level smallint,
  light_avg_level smallint,
  light_on smallint,
  light_off smallint,
  power_used numeric(19,2),
  occ_in smallint,
  occ_out smallint,
  occ_count smallint,
  dim_percentage smallint,
  dim_offset smallint,
  bright_percentage smallint,
  bright_offset smallint,
  capture_at timestamp without time zone,
  campus_id bigint,
  price double precision,
  cost double precision,
  base_power_used numeric(19,2),
  base_cost double precision,
  saved_power_used numeric(19,2),
  saved_cost double precision,
  occ_saving numeric(19,2),
  tuneup_saving numeric(19,2),
  ambient_saving numeric(19,2),
  manual_saving numeric(19,2),
  peak_load numeric(19,2),
  min_load numeric(19,2),
  avg_load numeric(19,2),
  min_price double precision,
  max_price double precision,
  CONSTRAINT campus_energy_consumption_daily_pkey PRIMARY KEY (id),
  CONSTRAINT campus_unique_energy_consumption_daily UNIQUE(capture_at, campus_id)
);

ALTER TABLE campus_energy_consumption_daily OWNER TO postgres;

--
-- Name: campus_energy_consumption_daily_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE campus_energy_consumption_daily_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE campus_energy_consumption_daily_seq OWNER TO postgres;

--
-- TOC entry 1798 (class 1259 OID 716506)
-- Dependencies: 1385
-- Name: campus_energy_consumption_daily_capture_at_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX campus_energy_consumption_daily_capture_at_index ON campus_energy_consumption_daily USING btree (capture_at);

--
-- TOC entry 1799 (class 1259 OID 716505)
-- Dependencies: 1385
-- Name: campus_energy_consumption_daily_campus_id_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX campus_energy_consumption_daily_campus_id_index ON campus_energy_consumption_daily USING btree (campus_id);

--
-- Name: company_energy_consumption_daily; Type: TABLE; Owner: postgres; 
--

CREATE TABLE company_energy_consumption_daily
(
  id bigint NOT NULL,
  min_temperature smallint,
  max_temperature smallint,
  avg_temperature smallint,
  light_on_seconds smallint,
  light_min_level smallint,
  light_max_level smallint,
  light_avg_level smallint,
  light_on smallint,
  light_off smallint,
  power_used numeric(19,2),
  occ_in smallint,
  occ_out smallint,
  occ_count smallint,
  dim_percentage smallint,
  dim_offset smallint,
  bright_percentage smallint,
  bright_offset smallint,
  capture_at timestamp without time zone,
  price double precision,
  cost double precision,
  base_power_used numeric(19,2),
  base_cost double precision,
  saved_power_used numeric(19,2),
  saved_cost double precision,
  occ_saving numeric(19,2),
  tuneup_saving numeric(19,2),
  ambient_saving numeric(19,2),
  manual_saving numeric(19,2),
  peak_load numeric(19,2),
  min_load numeric(19,2),
  avg_load numeric(19,2),
  min_price double precision,
  max_price double precision,
  CONSTRAINT comapny_energy_consumption_daily_pkey PRIMARY KEY (id),
  CONSTRAINT company_unique_energy_consumption_daily UNIQUE(capture_at)
);

ALTER TABLE company_energy_consumption_daily OWNER TO postgres;

--
-- Name: company_energy_consumption_daily_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE company_energy_consumption_daily_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE company_energy_consumption_daily_seq OWNER TO postgres;

--
-- TOC entry 1798 (class 1259 OID 716506)
-- Dependencies: 1385
-- Name: company_energy_consumption_daily_capture_at_index; Type: INDEX; -- Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX company_energy_consumption_daily_capture_at_index ON company_energy_consumption_daily USING btree (capture_at);

-- Name: fixture_daily_record; Type: TYPE; Schema: public; Owner: postgres
--

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
	avg_load numeric,
	min_price numeric,
	max_price numeric
);

-- Name: hier_daily_record; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE hier_daily_record AS (
	hier_id integer,
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
	avg_load numeric,
	min_price numeric,
	max_price numeric
);

ALTER TYPE public.hier_daily_record OWNER TO postgres;

-- Name: company_daily_record; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE company_daily_record AS (	
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
	avg_load numeric,
	min_price numeric,
	max_price numeric
);


ALTER TYPE public.company_daily_record OWNER TO postgres;

--
-- TOC entry 21 (class 1255 OID 786971)
-- Dependencies: 5 367
-- Name: aggregatedailyenergyconsumption(timestamp with time zone); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE OR REPLACE FUNCTION aggregatedailyenergyconsumption(todate timestamp with time zone) RETURNS void
    AS $$
DECLARE 
	rec fixture_daily_record;
	hier_rec hier_daily_record;
	company_rec company_daily_record;
	min_load1 numeric;
	peak_load1 numeric;
	price_calc numeric;
BEGIN

--fixture aggregation
	FOR rec IN (
	SELECT fixture_id, SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(avg_temperature) AS avg_temp, sum(base_power_used) AS base_power, sum(base_cost) AS base_cost, sum(saved_power_used) AS saved_power, sum(saved_cost) AS saved_cost, sum(occ_saving) AS occ_saving, sum(ambient_saving) AS amb_saving, sum(tuneup_saving) AS tune_saving, sum(manual_saving) AS manual_saving, count(*) AS no_of_rec, max(peak_load) AS peak_load, min(min_load) AS min_load, avg(avg_load) AS avg_load, min(min_price) AS min_price, max(max_price) AS max_price
	FROM energy_consumption_hourly as ec 
	WHERE capture_at <= toDate and capture_at > toDate - interval '1 day' GROUP BY fixture_id)
	LOOP  
	  IF rec.base_power > 0 THEN
	    price_calc = rec.base_cost*1000/rec.base_power;
	  ELSE
	    price_calc = 0;
	  END IF;
		INSERT INTO energy_consumption_daily (id, fixture_id, power_used, cost, price, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, peak_load, min_load, avg_load, min_price, max_price) VALUES (nextval('energy_consumption_daily_seq'), rec.fixture_id, rec.agg_power, rec.agg_cost, round(price_calc, 10), toDate, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_load, rec.min_load, rec.avg_load, rec.min_price, rec.max_price);
	END LOOP;

--floor aggregation
FOR hier_rec IN (
	SELECT floor_id AS hier_id, SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(avg_temperature) AS avg_temp, sum(base_power_used) AS base_power, sum(base_cost) AS base_cost, sum(saved_power_used) AS saved_power, sum(saved_cost) AS saved_cost, sum(occ_saving) AS occ_saving, sum(ambient_saving) AS amb_saving, sum(tuneup_saving) AS tune_saving, sum(manual_saving) AS manual_saving, count(*) AS no_of_rec, max(avg_load) AS peak_load, min(avg_load) AS min_load, avg(avg_load) AS avg_load, min(min_price) AS min_price, max(max_price) AS max_price
	FROM floor_energy_consumption_hourly as ec 
	WHERE capture_at <= toDate and capture_at > toDate - interval '1 day' GROUP BY floor_id)
	LOOP  
	  IF hier_rec.base_power > 0 THEN
	    price_calc = hier_rec.base_cost*1000/hier_rec.base_power;
	  ELSE
	    price_calc = 0;
	  END IF;
		INSERT INTO floor_energy_consumption_daily (id, floor_id, power_used, cost, price, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, peak_load, min_load, avg_load, min_price, max_price) VALUES (nextval('floor_energy_consumption_daily_seq'), hier_rec.hier_id, hier_rec.agg_power, hier_rec.agg_cost, round(price_calc, 10), toDate, hier_rec.min_temp, hier_rec.max_temp, round(hier_rec.avg_temp), hier_rec.base_power, hier_rec.base_cost, hier_rec.saved_power, hier_rec.saved_cost, hier_rec.occ_saving, hier_rec.amb_saving, hier_rec.tune_saving, hier_rec.manual_saving, hier_rec.peak_load, hier_rec.min_load, hier_rec.avg_load, hier_rec.min_price, hier_rec.max_price);
	END LOOP;

--building aggregation
FOR hier_rec IN (
	SELECT bld_id AS hier_id, SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(avg_temperature) AS avg_temp, sum(base_power_used) AS base_power, sum(base_cost) AS base_cost, sum(saved_power_used) AS saved_power, sum(saved_cost) AS saved_cost, sum(occ_saving) AS occ_saving, sum(ambient_saving) AS amb_saving, sum(tuneup_saving) AS tune_saving, sum(manual_saving) AS manual_saving, count(*) AS no_of_rec, max(avg_load) AS peak_load, min(avg_load) AS min_load, avg(avg_load) AS avg_load, min(min_price) AS min_price, max(max_price) AS max_price
	FROM bld_energy_consumption_hourly as ec 
	WHERE capture_at <= toDate and capture_at > toDate - interval '1 day' GROUP BY bld_id)
	LOOP  
	  IF hier_rec.base_power > 0 THEN
	    price_calc = hier_rec.base_cost*1000/hier_rec.base_power;
	  ELSE
	    price_calc = 0;
	  END IF;
		INSERT INTO bld_energy_consumption_daily (id, bld_id, power_used, cost, price, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, peak_load, min_load, avg_load, min_price, max_price) VALUES (nextval('bld_energy_consumption_daily_seq'), hier_rec.hier_id, hier_rec.agg_power, hier_rec.agg_cost, round(price_calc, 10), toDate, hier_rec.min_temp, hier_rec.max_temp, round(hier_rec.avg_temp), hier_rec.base_power, hier_rec.base_cost, hier_rec.saved_power, hier_rec.saved_cost, hier_rec.occ_saving, hier_rec.amb_saving, hier_rec.tune_saving, hier_rec.manual_saving, hier_rec.peak_load, hier_rec.min_load, hier_rec.avg_load, hier_rec.min_price, hier_rec.max_price);
	END LOOP;

--campus aggregation
FOR hier_rec IN (
	SELECT campus_id AS hier_id, SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(avg_temperature) AS avg_temp, sum(base_power_used) AS base_power, sum(base_cost) AS base_cost, sum(saved_power_used) AS saved_power, sum(saved_cost) AS saved_cost, sum(occ_saving) AS occ_saving, sum(ambient_saving) AS amb_saving, sum(tuneup_saving) AS tune_saving, sum(manual_saving) AS manual_saving, count(*) AS no_of_rec, max(avg_load) AS peak_load, min(avg_load) AS min_load, avg(avg_load) AS avg_load, min(min_price) AS min_price, max(max_price) AS max_price
	FROM campus_energy_consumption_hourly as ec 
	WHERE capture_at <= toDate and capture_at > toDate - interval '1 day' GROUP BY campus_id)
	LOOP  
	  IF hier_rec.base_power > 0 THEN
	    price_calc = hier_rec.base_cost*1000/hier_rec.base_power;
	  ELSE
	    price_calc = 0;
	  END IF;
		INSERT INTO campus_energy_consumption_daily (id, campus_id, power_used, cost, price, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, peak_load, min_load, avg_load, min_price, max_price) VALUES (nextval('campus_energy_consumption_daily_seq'), hier_rec.hier_id, hier_rec.agg_power, hier_rec.agg_cost, round(price_calc, 10), toDate, hier_rec.min_temp, hier_rec.max_temp, round(hier_rec.avg_temp), hier_rec.base_power, hier_rec.base_cost, hier_rec.saved_power, hier_rec.saved_cost, hier_rec.occ_saving, hier_rec.amb_saving, hier_rec.tune_saving, hier_rec.manual_saving, hier_rec.peak_load, hier_rec.min_load, hier_rec.avg_load, hier_rec.min_price, hier_rec.max_price);
	END LOOP;

--company aggregation
FOR company_rec IN (
	SELECT SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(avg_temperature) AS avg_temp, sum(base_power_used) AS base_power, sum(base_cost) AS base_cost, sum(saved_power_used) AS saved_power, sum(saved_cost) AS saved_cost, sum(occ_saving) AS occ_saving, sum(ambient_saving) AS amb_saving, sum(tuneup_saving) AS tune_saving, sum(manual_saving) AS manual_saving, count(*) AS no_of_rec, max(avg_load) AS peak_load, min(avg_load) AS min_load, avg(avg_load) AS avg_load, min(min_price) AS min_price, max(max_price) AS max_price
	FROM company_energy_consumption_hourly as ec 
	WHERE capture_at <= toDate and capture_at > toDate - interval '1 day')
	LOOP  
	  IF company_rec.base_power > 0 THEN
	    price_calc = company_rec.base_cost*1000/company_rec.base_power;
	  ELSE
	    price_calc = 0;
	  END IF;
		INSERT INTO company_energy_consumption_daily (id, power_used, cost, price, capture_at, min_temperature, max_temperature, avg_temperature, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, ambient_saving, tuneup_saving, manual_saving, peak_load, min_load, avg_load, min_price, max_price) VALUES (nextval('company_energy_consumption_daily_seq'), company_rec.agg_power, company_rec.agg_cost, round(price_calc, 10), toDate, company_rec.min_temp, company_rec.max_temp, round(company_rec.avg_temp), company_rec.base_power, company_rec.base_cost, company_rec.saved_power, company_rec.saved_cost, company_rec.occ_saving, company_rec.amb_saving, company_rec.tune_saving, company_rec.manual_saving, company_rec.peak_load, company_rec.min_load, company_rec.avg_load, company_rec.min_price, company_rec.max_price);
	END LOOP;

	PERFORM prunedatabase();

	PERFORM pruneemsaudit();
END;
$$
LANGUAGE plpgsql;

ALTER FUNCTION public.aggregatedailyenergyconsumption(todate timestamp with time zone) OWNER TO postgres;

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
	  WHERE capture_at >= toDate - interval '20 min' and capture_at <= toDate GROUP BY fixture_id)
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

