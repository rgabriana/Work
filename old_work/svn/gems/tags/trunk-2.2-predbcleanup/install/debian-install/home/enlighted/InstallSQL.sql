--
-- PostgreSQL database dump
--

-- Started on 2010-05-28 13:36:34

SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 1937 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'Standard public schema';


--
-- TOC entry 367 (class 2612 OID 16386)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

DROP PROCEDURAL LANGUAGE IF EXISTS plpgsql CASCADE ;
CREATE PROCEDURAL LANGUAGE plpgsql;


SET search_path = public, pg_catalog;

--
-- TOC entry 345 (class 1247 OID 765705)
-- Dependencies: 1433
-- Name: avgbarchartrecord; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE avgbarchartrecord AS (
	id integer,
	name character varying,
	"EN" numeric,
	ondate timestamp with time zone
);


ALTER TYPE public.avgbarchartrecord OWNER TO postgres;

--
-- TOC entry 360 (class 1247 OID 795168)
-- Dependencies: 1448
-- Name: avgrecord; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE avgrecord AS (
	i integer,
	"EN" numeric,
	price numeric,
	cost numeric,
	"basePowerUsed" numeric,
	"baseCost" numeric
);


ALTER TYPE public.avgrecord OWNER TO postgres;

--
-- TOC entry 359 (class 1247 OID 795166)
-- Dependencies: 1447
-- Name: fixture_daily_record; Type: TYPE; Schema: public; Owner: postgres
--

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

--
-- TOC entry 358 (class 1247 OID 795164)
-- Dependencies: 1446
-- Name: fixture_hour_record; Type: TYPE; Schema: public; Owner: postgres
--

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

ALTER TYPE public.fixture_hour_record OWNER TO postgres;

--
-- TOC entry 21 (class 1255 OID 786971)
-- Dependencies: 5 367
-- Name: aggregatedailyenergyconsumption(timestamp with time zone); Type: FUNCTION; Schema: public; Owner: postgres
--

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


ALTER FUNCTION public.aggregatedailyenergyconsumption(todate timestamp with time zone) OWNER TO postgres;

CREATE TYPE system_ec_record AS (
        capture_time timestamp without time zone,
        load numeric
);

--
-- TOC entry 20 (class 1255 OID 786968)
-- Dependencies: 367 5
-- Name: aggregatehourlyenergyconsumption(timestamp with time zone); Type: FUNCTION; Schema: public; Owner: postgres
--

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


ALTER FUNCTION public.aggregatehourlyenergyconsumption(todate timestamp with time zone) OWNER TO postgres;

--
-- TOC entry 24 (class 1255 OID 795171)
-- Dependencies: 5 367 360
-- Name: loaddailyenergyconsumption(timestamp with time zone, integer, integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE OR REPLACE FUNCTION loaddailyenergyconsumption(todate timestamp with time zone, points integer, intervalvalue integer, columnname text, columnvalue integer)
  RETURNS SETOF avgrecord AS
$BODY$
DECLARE
 rec avgrecord;
 count int;
 current timestamp without time zone;
BEGIN
	count := $2; 
	current := now();
	IF $4 = 'campus' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used+power_used2+power_used3+power_used4+power_used5) AS "EN",avg(greatest(price,price2,price3,price4,price5)),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_daily as ec where ec.fixture_id in(select id from fixture where campus_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'building' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used+power_used2+power_used3+power_used4+power_used5) AS "EN",avg(greatest(price,price2,price3,price4,price5)),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_daily as ec where ec.fixture_id in(select id from fixture where building_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'floor' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used+power_used2+power_used3+power_used4+power_used5) AS "EN",avg(greatest(price,price2,price3,price4,price5)),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_daily as ec where ec.fixture_id in(select id from fixture where floor_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'area' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used+power_used2+power_used3+power_used4+power_used5) AS "EN",avg(greatest(price,price2,price3,price4,price5)),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_daily as ec where ec.fixture_id in(select id from fixture where area_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'subArea' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used+power_used2+power_used3+power_used4+power_used5) AS "EN",avg(greatest(price,price2,price3,price4,price5)),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_daily as ec where ec.fixture_id in(select id from fixture where sub_area_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
	END LOOP;
	ELSE
	IF $4 = 'group' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used+power_used2+power_used3+power_used4+power_used5) AS "EN",avg(greatest(price,price2,price3,price4,price5)),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_daily as ec where ec.fixture_id in(select id from fixture where group_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'company' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used+power_used2+power_used3+power_used4+power_used5) AS "EN",avg(greatest(price,price2,price3,price4,price5)),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_daily as ec where ec.fixture_id in(select id from fixture)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
	END LOOP;
	ELSE
	IF $4 = 'fixture' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used+power_used2+power_used3+power_used4+power_used5) AS "EN",avg(greatest(price,price2,price3,price4,price5)),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_daily as ec where ec.fixture_id = $5
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
	END LOOP;
	END IF;
	END IF;
	END IF;
	END IF;
	END IF;
	END IF;
	END IF;	
	END IF;	
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION loaddailyenergyconsumption(todate timestamp with time zone, points integer, intervalvalue integer, columnname text, columnvalue integer) OWNER TO postgres;

--
-- TOC entry 22 (class 1255 OID 795169)
-- Dependencies: 367 5 360
-- Name: loadenergyconsumption(timestamp with time zone, integer, integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION loadenergyconsumption(todate timestamp with time zone, points integer, intervalvalue integer, columnname text, columnvalue integer) RETURNS SETOF avgrecord
    AS $_$
DECLARE
 rec avgrecord;
 count int;
 current timestamp without time zone;
BEGIN
	count := $2; 
	current := now();
	IF $4 = 'campus' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption as ec where ec.fixture_id in(select id from fixture where campus_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'building' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption as ec where ec.fixture_id in(select id from fixture where building_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'floor' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption as ec where ec.fixture_id in(select id from fixture where floor_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'area' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption as ec where ec.fixture_id in(select id from fixture where area_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'subArea' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption as ec where ec.fixture_id in(select id from fixture where sub_area_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
	END LOOP;
	ELSE
	IF $4 = 'group' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption as ec where ec.fixture_id in(select id from fixture where group_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'company' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption as ec where ec.fixture_id in(select id from fixture)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
	END LOOP;
	ELSE
	IF $4 = 'fixture' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption as ec where ec.fixture_id = $5
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
	END LOOP;
	END IF;
	END IF;
	END IF;
	END IF;
	END IF;
	END IF;
	END IF;	
	END IF;	
END;
$_$
    LANGUAGE plpgsql;


ALTER FUNCTION public.loadenergyconsumption(todate timestamp with time zone, points integer, intervalvalue integer, columnname text, columnvalue integer) OWNER TO postgres;

--
-- TOC entry 19 (class 1255 OID 773923)
-- Dependencies: 367 345 5
-- Name: loadenergyconsumptionbarchart(timestamp with time zone, integer, integer, text, integer, timestamp with time zone); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION loadenergyconsumptionbarchart(todate timestamp with time zone, points integer, intervalvalue integer, columnname text, columnvalue integer, fromdate timestamp with time zone) RETURNS SETOF avgbarchartrecord
    AS $_$
DECLARE
 rec avgbarchartrecord;
 count int;
 current timestamp without time zone;
 intervalValue interval;
BEGIN
	count := $2; 
	current := fromDate;
	IF $4 = 'campus' THEN
		FOR i IN REVERSE count..1 LOOP
		IF i=1 THEN
			intervalValue = toDate - current;
		ELSE IF i = count THEN
			intervalValue =  fromDate - to_timestamp(to_char(fromDate, 'yyyy-MM-dd'), 'yyyy-MM-dd');
			intervalValue = interval '24 hour' - intervalValue;
			IF $3 > 24*60*1 THEN
				intervalValue = intervalValue + ((interval '1 minute' * $3) - (interval '1 minute' * ($3-($3-1*24*60))));
			END IF;
		ELSE
		intervalValue = interval '1 minute' * $3;
		END IF;
		END IF;
		FOR rec IN (select g.id,g.name as name,sum(ec.power_used) as EN,current as onDate
			from groups g left join fixture f on f.group_id = g.id and f.campus_id=$5 
			left join energy_consumption_hourly ec on ec.fixture_id = f.id
			and capture_at >current
			and capture_at < current + intervalValue
			and f.campus_id=$5 group by g.id ,g.name order by g.id
			)
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		RAISE NOTICE 'date and time(%)(%)(%)', current,intervalValue,current + intervalValue;
		current = current + intervalValue;
		END LOOP;
	ELSE
	IF $4 = 'building' THEN
		FOR i IN REVERSE count..1 LOOP
		IF i=1 THEN
			intervalValue = toDate - current;
		ELSE IF i = count THEN
			intervalValue =  fromDate - to_timestamp(to_char(fromDate, 'yyyy-MM-dd'), 'yyyy-MM-dd');
			intervalValue = interval '24 hour' - intervalValue;
			IF $3 > 24*60*1 THEN
				intervalValue = intervalValue + ((interval '1 minute' * $3) - (interval '1 minute' * ($3-($3-1*24*60))));
			END IF;
		ELSE
		intervalValue = interval '1 minute' * $3;
		END IF;
		END IF;
		FOR rec IN (select g.id,g.name as name,sum(ec.power_used) as EN,current as onDate
			from groups g left join fixture f on f.group_id = g.id and f.building_id=$5
			left join energy_consumption_hourly ec on ec.fixture_id = f.id
			and capture_at > current
			and capture_at < current + intervalValue 
			and f.building_id=$5 group by g.id ,g.name order by g.id	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP; 
		RAISE NOTICE 'date and time(%)(%)(%)', current,intervalValue,current + intervalValue;
		current = current + intervalValue;
		END LOOP;
	ELSE
	IF $4 = 'floor' THEN
		FOR i IN REVERSE count..1 LOOP
		IF i=1 THEN
			intervalValue = toDate - current;
		ELSE IF i = count THEN
			intervalValue =  fromDate - to_timestamp(to_char(fromDate, 'yyyy-MM-dd'), 'yyyy-MM-dd');
			intervalValue = interval '24 hour' - intervalValue;
			IF $3 > 24*60*1 THEN
				intervalValue = intervalValue + ((interval '1 minute' * $3) - (interval '1 minute' * ($3-($3-1*24*60))));
			END IF;
		ELSE
		intervalValue = interval '1 minute' * $3;
		END IF;
		END IF;
		FOR rec IN (select g.id,g.name as name,sum(ec.power_used) as EN,current as onDate
			from groups g left join fixture f on f.group_id = g.id and f.floor_id=$5 
			left join energy_consumption_hourly ec on ec.fixture_id = f.id
			and capture_at > current
			and capture_at < current + intervalValue 
			and f.floor_id=$5  group by g.id ,g.name order by g.id	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		RAISE NOTICE 'date and time(%)(%)(%)', current,intervalValue,current + intervalValue;
		current = current + intervalValue;
		END LOOP;
	ELSE
	IF $4 = 'area' THEN
		FOR i IN REVERSE count..1 LOOP
		IF i=1 THEN
			intervalValue = toDate - current;
		ELSE IF i = count THEN
			intervalValue =  fromDate - to_timestamp(to_char(fromDate, 'yyyy-MM-dd'), 'yyyy-MM-dd');
			intervalValue = interval '24 hour' - intervalValue;
			IF $3 > 24*60*1 THEN
				intervalValue = intervalValue + ((interval '1 minute' * $3) - (interval '1 minute' * ($3-($3-1*24*60))));
			END IF;
		ELSE
		intervalValue = interval '1 minute' * $3;
		END IF;
		END IF;
		FOR rec IN (select g.id,g.name as name,sum(ec.power_used) as EN,current as onDate
			from groups g left join fixture f on f.group_id = g.id and f.area_id=$5 
			left join energy_consumption_hourly ec on ec.fixture_id = f.id
			and capture_at > current
			and capture_at < current + intervalValue 
			and f.area_id=$5 group by g.id ,g.name order by g.id	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		RAISE NOTICE 'date and time(%)(%)(%)', current,intervalValue,current + intervalValue;
		current = current + intervalValue;
		END LOOP;
	ELSE
	IF $4 = 'subArea' THEN
		FOR i IN REVERSE count..1 LOOP
		IF i=1 THEN
			intervalValue = toDate - current;
		ELSE IF i = count THEN
			intervalValue =  fromDate - to_timestamp(to_char(fromDate, 'yyyy-MM-dd'), 'yyyy-MM-dd');
			intervalValue = interval '24 hour' - intervalValue;
			IF $3 > 24*60*1 THEN
				intervalValue = intervalValue + ((interval '1 minute' * $3) - (interval '1 minute' * ($3-($3-1*24*60))));
			END IF;
		ELSE
		intervalValue = interval '1 minute' * $3;
		END IF;
		END IF;
		FOR rec IN (select g.id,g.name as name,sum(ec.power_used) as EN,current as onDate
			from groups g left join fixture f on f.group_id = g.id and f.sub_area_id=$5 
			left join energy_consumption_hourly ec on ec.fixture_id = f.id
			and capture_at > current
			and capture_at < current + intervalValue 
			and f.sub_area_id=$5  group by g.id ,g.name order by g.id	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		RAISE NOTICE 'date and time(%)(%)(%)', current,intervalValue,current + intervalValue;
		current = current + intervalValue;
	END LOOP;
	ELSE
	IF $4 = 'group' THEN
		FOR i IN REVERSE count..1 LOOP
		IF i=1 THEN
			intervalValue = toDate - current;
		ELSE IF i = count THEN
			intervalValue =  fromDate - to_timestamp(to_char(fromDate, 'yyyy-MM-dd'), 'yyyy-MM-dd');
			intervalValue = interval '24 hour' - intervalValue;
			IF $3 > 24*60*1 THEN
				intervalValue = intervalValue + ((interval '1 minute' * $3) - (interval '1 minute' * ($3-($3-1*24*60))));
			END IF;
		ELSE
		intervalValue = interval '1 minute' * $3;
		END IF;
		END IF;
		FOR rec IN (select g.id,g.name as name,sum(ec.power_used) as EN,current as onDate
			from groups g left join fixture f on f.group_id = g.id and f.group_id=$5 
			left join energy_consumption_hourly ec on ec.fixture_id = f.id
			and capture_at > current
			and capture_at < current + intervalValue 
			and f.group_id=$5 group by g.id ,g.name order by g.id	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		RAISE NOTICE 'date and time(%)(%)(%)', current,intervalValue,current + intervalValue;
		current = current + intervalValue;
		END LOOP;
	ELSE
	IF $4 = 'company' THEN
		FOR i IN REVERSE count..1 LOOP
		IF i=1 THEN
			intervalValue = toDate - current;
		ELSE IF i = count THEN
			intervalValue =  fromDate - to_timestamp(to_char(fromDate, 'yyyy-MM-dd'), 'yyyy-MM-dd');
			intervalValue = interval '24 hour' - intervalValue;
			IF $3 > 24*60*1 THEN
				intervalValue = intervalValue + ((interval '1 minute' * $3) - (interval '1 minute' * ($3-($3-1*24*60))));
			END IF;
		ELSE
		intervalValue = interval '1 minute' * $3;
		END IF;
		END IF;
		FOR rec IN (select g.id,g.name as name,sum(ec.power_used) as EN,current as onDate
			from groups g left join fixture f on f.group_id = g.id 
			left join energy_consumption_hourly ec on ec.fixture_id = f.id
			and capture_at > current
			and capture_at < current + intervalValue group by g.id ,g.name order by g.id	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		RAISE NOTICE 'date and time(%)(%)(%)', current,intervalValue,current + intervalValue;
		current = current + intervalValue;
	END LOOP;
	END IF;
	END IF;
	END IF;
	END IF;
	END IF;
	END IF;
	END IF;
END;
$_$
    LANGUAGE plpgsql;


ALTER FUNCTION public.loadenergyconsumptionbarchart(todate timestamp with time zone, points integer, intervalvalue integer, columnname text, columnvalue integer, fromdate timestamp with time zone) OWNER TO postgres;

--
-- TOC entry 23 (class 1255 OID 795170)
-- Dependencies: 360 5 367
-- Name: loadhourlyenergyconsumption(timestamp with time zone, integer, integer, text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION loadhourlyenergyconsumption(todate timestamp with time zone, points integer, intervalvalue integer, columnname text, columnvalue integer) RETURNS SETOF avgrecord
    AS $_$
DECLARE
 rec avgrecord;
 count int;
 current timestamp without time zone;
BEGIN
	count := $2; 
	current := now();
	IF $4 = 'campus' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_hourly as ec where ec.fixture_id in(select id from fixture where campus_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'building' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_hourly as ec where ec.fixture_id in(select id from fixture where building_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'floor' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_hourly as ec where ec.fixture_id in(select id from fixture where floor_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'area' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_hourly as ec where ec.fixture_id in(select id from fixture where area_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'subArea' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_hourly as ec where ec.fixture_id in(select id from fixture where sub_area_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
	END LOOP;
	ELSE
	IF $4 = 'group' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_hourly as ec where ec.fixture_id in(select id from fixture where group_id = $5)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
		END LOOP;
	ELSE
	IF $4 = 'company' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_hourly as ec where ec.fixture_id in(select id from fixture)
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
	END LOOP;
	ELSE
	IF $4 = 'fixture' THEN
		FOR i IN 1..count LOOP
		FOR rec IN (SELECT i,SUM(power_used) AS "EN",avg(price),sum(cost),sum(base_power_used) as basePowerUsed,sum(base_cost) as baseCost
				FROM energy_consumption_hourly as ec where ec.fixture_id = $5
				and capture_at <current
				and capture_at > current - interval '1 minute'* $3	
			   )
			LOOP  
				RETURN NEXT rec;  
			END LOOP;  
		current = current - interval '1 minute' * $3;
	END LOOP;
	END IF;
	END IF;
	END IF;
	END IF;
	END IF;
	END IF;
	END IF;	
	END IF;	
END;
$_$
    LANGUAGE plpgsql;


ALTER FUNCTION public.loadhourlyenergyconsumption(todate timestamp with time zone, points integer, intervalvalue integer, columnname text, columnvalue integer) OWNER TO postgres;

--
-- TOC entry 25 (class 1255 OID 795193)
-- Dependencies: 367 5
-- Name: update_location_change(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_location_change() RETURNS "trigger"
    AS $$
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
$$
    LANGUAGE plpgsql;


ALTER FUNCTION public.update_location_change() OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: application_configuration; Type: TABLE; Owner: postgres;
--

CREATE TABLE application_configuration
(
  id bigint NOT NULL,
  self_login boolean DEFAULT false,
  valid_domain character varying,
  price double precision,
  CONSTRAINT application_configuration_pk PRIMARY KEY (id)
);

ALTER TABLE application_configuration OWNER TO postgres;;

--
-- Name: application_configuration_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE application_configuration_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE application_configuration_seq OWNER TO postgres;

--
-- Name: ballasts; Type: TABLE; Owner: postgres;
--

CREATE TABLE ballasts
(
  id bigint NOT NULL,
  item_num bigint,
  ballast_name character(128),
  input_voltage character(128),
  lamp_type character(64),
  lamp_num integer,
  ballast_factor double precision,
  volt_power_map_id bigint DEFAULT 1,
  wattage integer,
  manufacturer character(128),
  CONSTRAINT ballasts_pkey PRIMARY KEY (id)
);

ALTER TABLE ballasts OWNER TO postgres;

--
-- Name: bulbs; type: TABLE; Owner: postgres
--

CREATE TABLE bulbs
(
  id bigint NOT NULL,
  manufacturer character(128),
  bulb_name character varying(128),
  "type" character(128),
  initial_lumens bigint,
  design_lumens bigint,
  energy integer,
  life_ins_start bigint,
  life_prog_start bigint,
  diameter integer,
  length double precision,
  cri integer,
  color_temp integer,
  CONSTRAINT "Bulbs_pkey" PRIMARY KEY (id)
);

ALTER TABLE bulbs OWNER TO postgres;

--
-- Name: event_type; Type: TABLE; Owner: postgres; 
--

CREATE TABLE event_type
(
  id bigint NOT NULL,
  "type" character varying(70),
  description character varying(255),
  CONSTRAINT event_type_id PRIMARY KEY (id)
);

ALTER TABLE event_type OWNER TO postgres;

--
-- Name: event_type_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE event_type_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE event_type_seq OWNER TO postgres;

--
-- Name: events_and_fault; Type: TABLE; Owner: postgres; 
--

CREATE TABLE events_and_fault
(
  id bigint NOT NULL,
  event_time timestamp without time zone,
  severity character varying(255),
  event_type character varying(255),
  event_value bigint,
  description character varying(500),
  active boolean DEFAULT true,
  fixture_id bigint,
  gateway_id bigint,
  resolution_comments character varying(1000),
  resolved_by bigint,
  resolved_on timestamp without time zone,
  CONSTRAINT events_and_fault_id PRIMARY KEY (id)
);

ALTER TABLE events_and_fault OWNER TO postgres;

--
-- Name: events_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE events_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE events_seq OWNER TO postgres;

--
-- Name: profile; Type: TABLE; Owner: postgres; 
--

CREATE TABLE profile
(
  id bigint NOT NULL,
  min_level bigint,
  on_level bigint,
  motion_detect_duration bigint,
  manual_override_duration bigint,
  motion_sensitivity bigint DEFAULT 1,
  ramp_up_time bigint DEFAULT 0,
  ambient_sensitivity integer DEFAULT 5,
  CONSTRAINT profile_id PRIMARY KEY (id)
);

ALTER TABLE profile OWNER TO postgres;

--
-- Name: profile_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE profile_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE profile_seq OWNER TO postgres;

--
-- Name: profile_configuration; Type: TABLE; Owner: postgres;
--

CREATE TABLE profile_configuration
(
  id bigint NOT NULL,
  morning_time character varying,
  day_time character varying,
  evening_time character varying,
  night_time character varying,
  CONSTRAINT profile_configuration_pkey PRIMARY KEY (id)
);

ALTER TABLE profile_configuration OWNER TO postgres;

--
-- Name: profile_configuration_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE profile_configuration_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE profile_configuration_seq OWNER TO postgres;

--
-- Name: weekday; Type: TABLE; Owner: postgres;
--

CREATE TABLE weekday
(
  id bigint NOT NULL,
  "day" character varying(255),
  profile_configuration_id bigint,
  short_order integer,
  "type" character varying,
  CONSTRAINT weekday_pkey PRIMARY KEY (id),
  CONSTRAINT fk49206f28971b5d7c FOREIGN KEY (profile_configuration_id)
      REFERENCES profile_configuration (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE weekday OWNER TO postgres;

--
-- Name: weekday_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE weekday_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE weekday_seq OWNER TO postgres;

--
-- Name: holiday; Type: TABLE; Owner: postgres;
--

CREATE TABLE holiday
(
  id bigint NOT NULL,
  holiday date,
  profile_configuration_id bigint,
  CONSTRAINT holiday_pkey PRIMARY KEY (id),
  CONSTRAINT fk41152858971b5d7c FOREIGN KEY (profile_configuration_id)
      REFERENCES profile_configuration (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE holiday OWNER TO postgres;

--
-- Name: holiday_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE holiday_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE holiday_seq OWNER TO postgres;

--
-- Name: profile_handler; Type: TABLE; Owner: postgres;
--

CREATE TABLE profile_handler
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
  dark_lux integer DEFAULT 20,
  neighbor_lux integer DEFAULT 200,
  envelope_on_level integer DEFAULT 50,
  "drop" integer DEFAULT 10,
  rise integer DEFAULT 20,
  dim_backoff_time smallint DEFAULT 10,
  intensity_norm_time smallint DEFAULT 10,
  on_amb_light_level integer,
  min_level_before_off smallint DEFAULT 20,
  relays_connected integer DEFAULT 1,
  profile_checksum smallint,
  global_profile_checksum smallint,
  standalone_motion_override smallint DEFAULT 0,
  dr_reactivity smallint DEFAULT 0,
  to_off_linger integer DEFAULT 30,
  initial_on_level smallint DEFAULT 50,
  profile_group_id smallint DEFAULT 1,
  profile_flag smallint DEFAULT 0,
  initial_on_time integer DEFAULT 5,
  is_high_bay smallint DEFAULT 0,
  motion_threshold_gain integer DEFAULT 0,
  CONSTRAINT profile_handler_pkey PRIMARY KEY (id),
  CONSTRAINT fkb5fdceb417b4925e FOREIGN KEY (night_profile_weekend)
      REFERENCES profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb5fdceb42a32aac FOREIGN KEY (night_profile_id)
      REFERENCES profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb5fdceb43640f0a8 FOREIGN KEY (day_profile_id)
      REFERENCES profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb5fdceb46ee3ddd3 FOREIGN KEY (day_profile_holiday)
      REFERENCES profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb5fdceb476ef29e2 FOREIGN KEY (day_profile_weekend)
      REFERENCES profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb5fdceb4886e503c FOREIGN KEY (evening_profile_id)
      REFERENCES profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb5fdceb489c71abf FOREIGN KEY (evening_profile_holiday)
      REFERENCES profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb5fdceb491d266ce FOREIGN KEY (evening_profile_weekend)
      REFERENCES profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb5fdceb4971b5d7c FOREIGN KEY (profile_configuration_id)
      REFERENCES profile_configuration (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb5fdceb4d17041fb FOREIGN KEY (morning_profile_holiday)
      REFERENCES profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb5fdceb4d97b8e0a FOREIGN KEY (morning_profile_weekend)
      REFERENCES profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb5fdceb4ecc09380 FOREIGN KEY (morning_profile_id)
      REFERENCES profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb5fdceb4fa9464f FOREIGN KEY (night_profile_holiday)
      REFERENCES profile (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE profile_handler OWNER TO postgres;

--
-- Name: profile_handler_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE profile_handler_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE profile_handler_seq OWNER TO postgres;

--
-- Name: inventorydevice; Type: TABLE; Owner: postgres;
--

CREATE TABLE inventorydevice
(
  snap_address character varying(20) NOT NULL,
  network_id character varying(10),
  fixture_name character varying(50),
  mac_address character varying(50),
  channel integer,
  version character varying(20),
  discovered_time timestamp without time zone,
  floor_id integer,
  status character varying(20),
  id bigint NOT NULL,
  ip_address character varying(255),
  comm_type integer DEFAULT 1,
  gw_id bigint,
  device_type integer DEFAULT 0,
  subnet_mask character varying(50),
  curr_app smallint DEFAULT 2,
  CONSTRAINT inventory_device_id PRIMARY KEY (id),
  CONSTRAINT unique_inventory_mac_addr UNIQUE (mac_address),
  CONSTRAINT unique_snap_addr UNIQUE (snap_address)
);

ALTER TABLE inventorydevice OWNER TO postgres;

--
-- Name: inventory_device_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE inventory_device_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE inventory_device_seq OWNER TO postgres;

--
-- Name: module; Type: TABLE; Owner: postgres;
--

CREATE TABLE module
(
  id bigint NOT NULL,
  name character varying,
  description character varying,
  CONSTRAINT module_pk PRIMARY KEY (id)
);

ALTER TABLE module OWNER TO postgres;

--
-- Name: module_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE module_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE module_seq OWNER TO postgres;

--
-- Name: permission_details; Type: TABLE; Owner: postgres; 
--

CREATE TABLE permission_details
(
  id bigint NOT NULL,
  name character varying NOT NULL,
  description character varying,
  CONSTRAINT permission_details_pkey PRIMARY KEY (id)
);

ALTER TABLE permission_details OWNER TO postgres;

--
-- Name: plan_map; Type: TABLE; Owner: postgres;
--

CREATE TABLE plan_map
(
  id bigint NOT NULL,
  plan oid,
  CONSTRAINT plan_map_pkey PRIMARY KEY (id)
);

ALTER TABLE plan_map OWNER TO postgres;

--
-- Name: plan_map_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE plan_map_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE plan_map_seq OWNER TO postgres;

--
-- Name: roles; Type: TABLE; Owner: postgres; 
--

CREATE TABLE roles
(
  id bigint NOT NULL,
  name character varying,
  CONSTRAINT role_pk PRIMARY KEY (id),
  CONSTRAINT unique_roles_name UNIQUE (name)
);

ALTER TABLE roles OWNER TO postgres;

--
-- Name: role_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE role_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE role_seq OWNER TO postgres;

--
-- Name: timezone; Type: TABLE; Owner: postgres;
--

CREATE TABLE timezone
(
  id bigint NOT NULL,
  name character varying,
  description character varying,
  CONSTRAINT timezone_pkey PRIMARY KEY (id)
);

ALTER TABLE timezone OWNER TO postgres;

--
-- Name: timezone_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE timezone_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE timezone_seq OWNER TO postgres;

--
-- Name: module_permission; Type: TABLE; Owner: postgres; 
--

CREATE TABLE module_permission
(
  id bigint NOT NULL,
  module_id bigint,
  role_id bigint,
  permission integer,
  CONSTRAINT module_permission_pk PRIMARY KEY (id),
  CONSTRAINT fk4506798275db9b85 FOREIGN KEY (role_id)
      REFERENCES module (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk45067982c74729cf FOREIGN KEY (module_id)
      REFERENCES module (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk45067982db9256cf FOREIGN KEY (role_id)
      REFERENCES roles (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT module_permission_module_fk FOREIGN KEY (module_id)
      REFERENCES module (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT module_permission_role_fk FOREIGN KEY (role_id)
      REFERENCES module (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE module_permission OWNER TO postgres;

--
-- Name: module_permission_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE module_permission_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE module_permission_seq OWNER TO postgres;

--
-- Name: users; Type: TABLE; Owner: postgres;
--

CREATE TABLE users
(
  id bigint NOT NULL,
  email character varying NOT NULL,
  "password" character varying NOT NULL,
  first_name character varying,
  last_name character varying,
  created_on date,
  role_id bigint,
  contact character varying,
  old_passwords character varying,
  location_id bigint,
  location_type character varying,
  approved_location_id bigint,
  approved_location_type character varying,
  term_condition_accepted boolean DEFAULT false,
  CONSTRAINT users_pk PRIMARY KEY (id),
  CONSTRAINT fk6a68e08db9256cf FOREIGN KEY (role_id)
      REFERENCES roles (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT user_role_fk FOREIGN KEY (role_id)
      REFERENCES roles (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE users OWNER TO postgres;

--
-- Name: user_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE user_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE user_seq OWNER TO postgres;

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
  start_time_3 character varying,
  end_time_3 character varying,
  CONSTRAINT sweep_timer_details_pkey PRIMARY KEY (id),
  CONSTRAINT fk49206f28971a23423 FOREIGN KEY (sweep_timer_id)
      REFERENCES sweep_timer (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE sweep_timer_details OWNER TO postgres;

--
-- Name: company; Type: TABLE; Owner: postgres; 
--

CREATE TABLE company
(
  id bigint NOT NULL,
  address character varying(511),
  name character varying(128) NOT NULL,
  contact character varying(128),
  profile_handler_id bigint,
  email character varying,
  completion_status integer,
  self_login boolean DEFAULT false,
  valid_domain character varying,
  notification_email character varying,
  severity_level character varying,
  timezone bigint,
  price double precision,
  time_zone character varying(128) DEFAULT 'America/Los_Angeles'::character varying,
  sweep_timer_id bigint,
  CONSTRAINT fk_company_to_sweep_timer FOREIGN KEY (sweep_timer_id) 
  		REFERENCES sweep_timer (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT company_pk PRIMARY KEY (id),
  CONSTRAINT fk38a73c7d393e967c FOREIGN KEY (profile_handler_id)
      REFERENCES profile_handler (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE company OWNER TO postgres;

--
-- Name: company_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE company_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;

ALTER TABLE company_seq OWNER TO postgres;

--
-- Name: campus; Type: TABLE; Owner: postgres; Tablespace: 
--

CREATE TABLE campus
(
  id bigint NOT NULL,
  name character varying(256) NOT NULL,
  "location" character varying(256),
  zipcode character varying(16),
  profile_handler_id bigint,
  sweep_timer_id bigint,
  CONSTRAINT fk_campus_to_sweep_timer FOREIGN KEY (sweep_timer_id) 
  		REFERENCES sweep_timer (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT campus_pk PRIMARY KEY (id),
  CONSTRAINT fkae79ecdf393e967c FOREIGN KEY (profile_handler_id)
      REFERENCES profile_handler (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE campus OWNER TO postgres;

--
-- Name: campus_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE campus_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE campus_seq OWNER TO postgres;

-- Index: unique_name
-- DROP INDEX unique_name;

CREATE UNIQUE INDEX unique_name ON campus USING btree (lower(name::text));

--
-- Name: company_campus; Type: TABLE; Owner: postgres;
--

CREATE TABLE company_campus
(
  id bigint NOT NULL,
  company_id bigint,
  campus_id bigint,
  CONSTRAINT company_campus_pk PRIMARY KEY (id),
  CONSTRAINT fkdcda16e1108db0ef FOREIGN KEY (campus_id)
      REFERENCES campus (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkdcda16e1126e57a5 FOREIGN KEY (company_id)
      REFERENCES company (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE company_campus OWNER TO postgres;

--
-- Name: company_campus_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE company_campus_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE company_campus_seq OWNER TO postgres;

--
-- Name: building; Type: TABLE; Owner: postgres;
--

CREATE TABLE building
(
  id bigint NOT NULL,
  name character varying(256) NOT NULL,
  campus_id bigint NOT NULL,
  profile_handler_id bigint,
  sweep_timer_id bigint,
  CONSTRAINT fk_building_to_sweep_timer FOREIGN KEY (sweep_timer_id) 
  		REFERENCES sweep_timer (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT building_pk PRIMARY KEY (id),
  CONSTRAINT fkaaba12b4108db0ef FOREIGN KEY (campus_id)
      REFERENCES campus (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkaaba12b4393e967c FOREIGN KEY (profile_handler_id)
      REFERENCES profile_handler (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE building OWNER TO postgres;

--
-- Name: building_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE building_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE building_seq OWNER TO postgres;

-- Index: unique_name_in_campus
-- DROP INDEX unique_name_in_campus;

CREATE UNIQUE INDEX unique_name_in_campus ON building USING btree (lower(name::text), campus_id);

--
-- Name: floor; Type: TABLE; Owner: postgres;
--

CREATE TABLE floor
(
  id bigint NOT NULL,
  name character varying(128) NOT NULL,
  description character varying(512),
  building_id bigint,
  profile_handler_id bigint,
  floorplan_url character varying(255),
  plan_map_id bigint,
  no_installed_sensors integer DEFAULT 0,
  no_installed_fixtures integer DEFAULT 0,
  floor_plan_uploaded_time timestamp without time zone,
  sweep_timer_id bigint,
  CONSTRAINT fk_floor_to_sweep_timer FOREIGN KEY (sweep_timer_id) 
  		REFERENCES sweep_timer (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT floor_pk PRIMARY KEY (id),
  CONSTRAINT fk5d0240c393e967c FOREIGN KEY (profile_handler_id)
      REFERENCES profile_handler (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk5d0240cd3a6d38f FOREIGN KEY (building_id)
      REFERENCES building (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE floor OWNER TO postgres;

--
-- Name: floor_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE floor_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE floor_seq OWNER TO postgres;

-- Index: unique_name_in_building
-- DROP INDEX unique_name_in_building;

CREATE UNIQUE INDEX unique_name_in_building ON floor USING btree (lower(name::text), building_id);

--
-- Name: area; Type: TABLE; Owner: postgres;
--

CREATE TABLE area
(
  id bigint NOT NULL,
  name character varying NOT NULL,
  description character varying,
  floor_id bigint NOT NULL,
  profile_handler_id bigint,
  areaplan_url character varying,
  plan_map_id bigint,
  sweep_timer_id bigint,
  CONSTRAINT fk_area_to_sweep_timer FOREIGN KEY (sweep_timer_id) 
  		REFERENCES sweep_timer (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT area_pk PRIMARY KEY (id),
  CONSTRAINT fk2dd08d1e814005 FOREIGN KEY (floor_id)
      REFERENCES floor (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk2dd08d393e967c FOREIGN KEY (profile_handler_id)
      REFERENCES profile_handler (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE area OWNER TO postgres;

--
-- Name: area_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE area_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE area_seq OWNER TO postgres;

--
-- Name: sub_area; Type: TABLE; Owner: postgres;
--

CREATE TABLE sub_area
(
  id bigint NOT NULL,
  name character varying NOT NULL,
  description character varying,
  area_id bigint NOT NULL,
  profile_handler_id bigint,
  CONSTRAINT sub_area_pk PRIMARY KEY (id),
  CONSTRAINT fk8403b32c393e967c FOREIGN KEY (profile_handler_id)
      REFERENCES profile_handler (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk8403b32c5cfacb6f FOREIGN KEY (area_id)
      REFERENCES area (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE sub_area OWNER TO postgres;

--
-- Name: sub_area_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE sub_area_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE sub_area_seq OWNER TO postgres;

--
-- Name: gateway; Type: TABLE; Owner: postgres; 
--

CREATE TABLE gateway
(
  id bigint NOT NULL,
  gateway_name character varying(255),
  floor_id bigint,
  campus_id bigint,
  building_id bigint,
  status boolean,
  commissioned boolean,
  unique_identifier_id character varying,
  x integer default 0,
  y integer default 0,
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
  no_of_sensors integer default 0,
  upgrade_status character varying(20),
  boot_loader_version character varying(50),
  "location" character varying(500),
  no_of_wds integer DEFAULT 0,
  CONSTRAINT gateway_pk PRIMARY KEY (id),
  CONSTRAINT unique_gateway_name UNIQUE (gateway_name),
  CONSTRAINT unique_gateway_mac_address UNIQUE (mac_address),
  CONSTRAINT gateway_floor_fk FOREIGN KEY (floor_id)
      REFERENCES floor (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE gateway OWNER TO postgres;

--
-- Name: gateway_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE gateway_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE gateway_seq OWNER TO postgres;

--
-- Name: fixture; Type: TABLE; Owner: postgres; 
--

CREATE TABLE fixture
(
  id bigint NOT NULL,
  sensor_id character varying,
  floor_id bigint NOT NULL,
  area_id bigint,
  sub_area_id bigint,
  profile_id bigint,
  "type" character varying,
  ballast_type character varying,
  ballast_last_changed timestamp with time zone,
  campus_id bigint,
  building_id bigint,
  x integer,
  y integer,
  no_of_bulbs integer,
  bulb_wattage integer,
  wattage integer,
  ballast_manufacturer character varying,
  bulb_manufacturer character varying,
  profile_handler_id bigint,
  current_profile character varying(255),
  original_profile_from character varying(255),
  dimmer_control integer,
  current_state character varying,
  savings_type character varying,
  last_occupancy_seen integer,
  light_level integer,
  snap_address character varying(20),
  fixture_name character varying(50),
  mac_address character varying(50),
  channel integer,
  version character varying(20),
  aes_key character varying(256),
  "location" character varying(500),
  gateway_id bigint,
  description character varying,
  notes character varying,
  bulbs_last_service_date date,
  ballast_last_service_date date,
  active boolean,
  state character varying,
  ballast_id bigint,
  bulb_id bigint,
  bulb_life double precision,
  no_of_fixtures integer DEFAULT 1,
  last_connectivity_at timestamp without time zone,
  ip_address character varying(255),
  comm_type integer DEFAULT 1,
  last_stats_rcvd_time timestamp without time zone,
  profile_checksum smallint,
  global_profile_checksum smallint,
  curr_app smallint,
  firmware_version character varying(20),
  bootloader_version character varying(20),
  group_id bigint,
  sec_gw_id bigint DEFAULT 1,
  upgrade_status character varying(20),
  push_profile boolean DEFAULT false,
  push_global_profile boolean DEFAULT false,
  last_cmd_sent character varying(25),
  last_cmd_sent_at timestamp without time zone,
  last_cmd_status character varying(20),
  avg_temperature smallint DEFAULT 0,
  baseline_power numeric(19,2) DEFAULT 0,
  voltage smallint DEFAULT 277,
  commission_status integer DEFAULT 0,
  is_hopper integer DEFAULT 0,
  version_synced integer DEFAULT 0,
  temperature_offset float(2),
  last_boot_time timestamp without time zone,
  cu_version character varying(20),
  model_no character varying(50),
  CONSTRAINT fixture_pk PRIMARY KEY (id),
  CONSTRAINT unique_snap_address UNIQUE (snap_address),
  CONSTRAINT fkcdb9fa09393e967c FOREIGN KEY (profile_handler_id)
      REFERENCES profile_handler (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE fixture OWNER TO postgres;

--
-- Name: fixture_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE fixture_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE fixture_seq OWNER TO postgres;

--
-- Name: groups; Type: TABLE; Owner: postgres; 
--

CREATE TABLE groups
(
  id bigint NOT NULL,
  name character varying(255),
  company_id bigint,
  profile_handler_id bigint,
  CONSTRAINT groups_pkey PRIMARY KEY (id),
  CONSTRAINT fkb63dd9d4126e57a5 FOREIGN KEY (company_id)
      REFERENCES company (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fkb63dd9d4393e967c FOREIGN KEY (profile_handler_id)
      REFERENCES profile_handler (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE groups OWNER TO postgres;

--
-- Name: groups_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE groups_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE groups_seq OWNER TO postgres;

-- Index: unique_name_in_groups
-- DROP INDEX unique_name_in_groups;

CREATE UNIQUE INDEX unique_name_in_groups ON groups USING btree (lower(name::text));

--
-- Name: fixture_group; Type: TABLE; Owner: postgres; 
--

CREATE TABLE fixture_group
(
  id bigint NOT NULL,
  group_id bigint,
  fixture_id bigint,
  CONSTRAINT fixture_group_pkey PRIMARY KEY (id),
  CONSTRAINT fk38ebe8c937eab044 FOREIGN KEY (group_id)
      REFERENCES groups (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk38ebe8c97758dc25 FOREIGN KEY (fixture_id)
      REFERENCES fixture (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE fixture_group OWNER TO postgres;

--
-- Name: fixture_group_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE fixture_group_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE fixture_group_seq OWNER TO postgres;

--
-- Name: energy_consumption; Type: TABLE; Owner: postgres; 
--

CREATE TABLE energy_consumption
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
  fixture_id bigint,
  price double precision,
  cost double precision,
  base_power_used numeric(19,2),
  base_cost double precision,
  saved_power_used numeric(19,2),
  saved_cost double precision,
  occ_saving numeric(19,2) DEFAULT 0,
  tuneup_saving numeric(19,2) DEFAULT 0,
  ambient_saving numeric(19,2) DEFAULT 0,
  manual_saving numeric(19,2) DEFAULT 0,
  zero_bucket smallint DEFAULT 0,
  avg_volts smallint,
  curr_state smallint,
  motion_bits bigint,
  power_calc smallint,
  energy_cum bigint,
  energy_calib int,
  min_volts smallint,
  max_volts smallint,
  energy_ticks int,
  last_volts smallint DEFAULT 0,
  saving_type smallint DEFAULT 0,
  cu_status int,
  CONSTRAINT energy_consumption_pkey PRIMARY KEY (id),
  CONSTRAINT unique_energy_consumption UNIQUE(capture_at, fixture_id),
  CONSTRAINT fk7d55f8647758dc25 FOREIGN KEY (fixture_id)
      REFERENCES fixture (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE energy_consumption OWNER TO postgres;

--
-- Name: energy_consumption_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE energy_consumption_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE energy_consumption_seq OWNER TO postgres;

--
-- Name: energy_consumption_daily; Type: TABLE; Owner: postgres; 
--

CREATE TABLE energy_consumption_daily
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
  fixture_id bigint,
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
  power_used2 numeric(19,2) default 0,
  power_used3 numeric(19,2) default 0,
  power_used4 numeric(19,2) default 0,
  power_used5 numeric(19,2) default 0,
  price2 double precision default 0,
  price3 double precision default 0,
  price4 double precision default 0,
  price5 double precision default 0,
  peak_load numeric(19,2),
  min_load numeric(19,2),
  min_price double precision,
  max_price double precision,
  CONSTRAINT energy_consumption_daily_pkey PRIMARY KEY (id),
  CONSTRAINT unique_energy_consumption_daily UNIQUE(capture_at, fixture_id)
);

ALTER TABLE energy_consumption_daily OWNER TO postgres;

--
-- Name: energy_consumption_daily_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE energy_consumption_daily_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE energy_consumption_daily_seq OWNER TO postgres;

--
-- Name: energy_consumption_hourly; Type: TABLE; Owner: postgres; 
--

CREATE TABLE energy_consumption_hourly
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
  fixture_id bigint,
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
  CONSTRAINT energy_consumption_hourly_pkey PRIMARY KEY (id),
  CONSTRAINT unique_energy_consumption_hourly UNIQUE(capture_at, fixture_id)
);

ALTER TABLE energy_consumption_hourly OWNER TO postgres;

--
-- Name: energy_consumption_hourly_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE energy_consumption_hourly_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE energy_consumption_hourly_seq OWNER TO postgres;

--
-- Name: firmware_upgrade; Type: TABLE; Owner: postgres; 
--

CREATE TABLE firmware_upgrade
(
  id bigint NOT NULL,
  user_id bigint,
  file_name character varying,
  version character varying,
  upgrade_on timestamp without time zone,
  device_type int DEFAULT 0,
  CONSTRAINT firmware_upgrade_pk PRIMARY KEY (id),
  CONSTRAINT user_fk FOREIGN KEY (user_id)
      REFERENCES users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE firmware_upgrade OWNER TO postgres;

--
-- Name: firmware_upgrade_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE firmware_upgrade_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE firmware_upgrade_seq OWNER TO postgres;

CREATE TABLE qrtz_job_details
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    JOB_NAME  VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    JOB_CLASS_NAME   VARCHAR(250) NOT NULL, 
    IS_DURABLE BOOL NOT NULL,
    IS_NONCONCURRENT BOOL NOT NULL,
    IS_UPDATE_DATA BOOL NOT NULL,
    REQUESTS_RECOVERY BOOL NOT NULL,
    JOB_DATA BYTEA NULL,
    PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
);

ALTER TABLE qrtz_job_details OWNER TO postgres;

CREATE TABLE qrtz_triggers
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    JOB_NAME  VARCHAR(200) NOT NULL, 
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    NEXT_FIRE_TIME BIGINT NULL,
    PREV_FIRE_TIME BIGINT NULL,
    PRIORITY INTEGER NULL,
    TRIGGER_STATE VARCHAR(16) NOT NULL,
    TRIGGER_TYPE VARCHAR(8) NOT NULL,
    START_TIME BIGINT NOT NULL,
    END_TIME BIGINT NULL,
    CALENDAR_NAME VARCHAR(200) NULL,
    MISFIRE_INSTR SMALLINT NULL,
    JOB_DATA BYTEA NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP) 
	REFERENCES QRTZ_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP) 
);

ALTER TABLE qrtz_triggers OWNER TO postgres;

CREATE TABLE qrtz_simple_triggers
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    REPEAT_COUNT BIGINT NOT NULL,
    REPEAT_INTERVAL BIGINT NOT NULL,
    TIMES_TRIGGERED BIGINT NOT NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

ALTER TABLE qrtz_simple_triggers OWNER TO postgres;

CREATE TABLE qrtz_cron_triggers
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    CRON_EXPRESSION VARCHAR(120) NOT NULL,
    TIME_ZONE_ID VARCHAR(80),
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

ALTER TABLE qrtz_cron_triggers OWNER TO postgres;

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

ALTER TABLE qrtz_simprop_triggers OWNER TO postgres;

CREATE TABLE qrtz_blob_triggers
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    BLOB_DATA BYTEA NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
        REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

ALTER TABLE qrtz_blob_triggers OWNER TO postgres;

CREATE TABLE qrtz_calendars
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    CALENDAR_NAME  VARCHAR(200) NOT NULL, 
    CALENDAR BYTEA NOT NULL,
    PRIMARY KEY (SCHED_NAME,CALENDAR_NAME)
);

ALTER TABLE qrtz_calendars OWNER TO postgres;

CREATE TABLE qrtz_paused_trigger_grps
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_GROUP  VARCHAR(200) NOT NULL, 
    PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP)
);

ALTER TABLE qrtz_paused_trigger_grps OWNER TO postgres;

CREATE TABLE qrtz_fired_triggers 
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    ENTRY_ID VARCHAR(95) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    FIRED_TIME BIGINT NOT NULL,
    PRIORITY INTEGER NOT NULL,
    STATE VARCHAR(16) NOT NULL,
    JOB_NAME VARCHAR(200) NULL,
    JOB_GROUP VARCHAR(200) NULL,
    IS_NONCONCURRENT BOOL NULL,
    REQUESTS_RECOVERY BOOL NULL,
    PRIMARY KEY (SCHED_NAME,ENTRY_ID)
);

ALTER TABLE qrtz_fired_triggers OWNER TO postgres;

CREATE TABLE qrtz_scheduler_state 
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT NOT NULL,
    CHECKIN_INTERVAL BIGINT NOT NULL,
    PRIMARY KEY (SCHED_NAME,INSTANCE_NAME)
);

ALTER TABLE qrtz_scheduler_state OWNER TO postgres;

CREATE TABLE qrtz_locks
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    LOCK_NAME  VARCHAR(40) NOT NULL, 
    PRIMARY KEY (SCHED_NAME,LOCK_NAME)
);

ALTER TABLE qrtz_locks OWNER TO postgres;

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

--
-- TOC entry 1798 (class 1259 OID 716506)
-- Dependencies: 1385
-- Name: energy_consumption_capture_at_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX energy_consumption_capture_at_index ON energy_consumption USING btree (capture_at);


--
-- TOC entry 1799 (class 1259 OID 716505)
-- Dependencies: 1385
-- Name: energy_consumption_fixture_id_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX energy_consumption_fixture_id_index ON energy_consumption USING btree (fixture_id);


--
-- TOC entry 1802 (class 1259 OID 716507)
-- Dependencies: 1385
-- Name: energy_consumption_power_used_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX energy_consumption_power_used_index ON energy_consumption USING btree (power_used);

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

--
-- TOC entry 1803 (class 1259 OID 716502)
-- Dependencies: 1387
-- Name: fixture_area_id_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fixture_area_id_index ON fixture USING btree (area_id);


--
-- TOC entry 1804 (class 1259 OID 741092)
-- Dependencies: 1387
-- Name: fixture_building_id_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fixture_building_id_index ON fixture USING btree (building_id);


--
-- TOC entry 1805 (class 1259 OID 716504)
-- Dependencies: 1387
-- Name: fixture_campus_id_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fixture_campus_id_index ON fixture USING btree (campus_id);


--
-- TOC entry 1806 (class 1259 OID 716501)
-- Dependencies: 1387
-- Name: fixture_floor_id_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fixture_floor_id_index ON fixture USING btree (floor_id);


--
-- TOC entry 1810 (class 1259 OID 741114)
-- Dependencies: 1388
-- Name: fixture_group_fixture_id_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fixture_group_fixture_id_index ON fixture_group USING btree (fixture_id);


--
-- TOC entry 1811 (class 1259 OID 741115)
-- Dependencies: 1388
-- Name: fixture_group_group_id_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fixture_group_group_id_index ON fixture_group USING btree (group_id);


--
-- TOC entry 1809 (class 1259 OID 716503)
-- Dependencies: 1387
-- Name: fixture_sub_area_id_index; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fixture_sub_area_id_index ON fixture USING btree (sub_area_id);


--
-- TOC entry 1930 (class 2620 OID 795197)
-- Dependencies: 25 1374
-- Name: update_area_change; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_area_change
    AFTER UPDATE ON area
    FOR EACH ROW
    EXECUTE PROCEDURE update_location_change();


--
-- TOC entry 1931 (class 2620 OID 795195)
-- Dependencies: 1376 25
-- Name: update_building_change; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_building_change
    AFTER UPDATE ON building
    FOR EACH ROW
    EXECUTE PROCEDURE update_location_change();


--
-- TOC entry 1932 (class 2620 OID 795194)
-- Dependencies: 1379 25
-- Name: update_campus_change; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_campus_change
    AFTER UPDATE ON campus
    FOR EACH ROW
    EXECUTE PROCEDURE update_location_change();


--
-- TOC entry 1933 (class 2620 OID 795196)
-- Dependencies: 1391 25
-- Name: update_floor_change; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_floor_change
    AFTER UPDATE ON floor
    FOR EACH ROW
    EXECUTE PROCEDURE update_location_change();

--
-- TOC entry 1917 (class 2606 OID 716466)
-- Dependencies: 1418 1407 1407 1418 1852
-- Name: qrtz_blob_triggers_trigger_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


--
-- TOC entry 1918 (class 2606 OID 716471)
-- Dependencies: 1418 1852 1409 1409 1418
-- Name: qrtz_cron_triggers_trigger_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


--
-- TOC entry 1919 (class 2606 OID 716476)
-- Dependencies: 1412 1412 1411 1411 1838
-- Name: qrtz_job_listeners_job_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_job_listeners
    ADD CONSTRAINT qrtz_job_listeners_job_name_fkey FOREIGN KEY (job_name, job_group) REFERENCES qrtz_job_details(job_name, job_group);


--
-- TOC entry 1920 (class 2606 OID 716481)
-- Dependencies: 1418 1852 1416 1416 1418
-- Name: qrtz_simple_triggers_trigger_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


--
-- TOC entry 1921 (class 2606 OID 716486)
-- Dependencies: 1418 1418 1417 1417 1852
-- Name: qrtz_trigger_listeners_trigger_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_trigger_listeners
    ADD CONSTRAINT qrtz_trigger_listeners_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


--
-- TOC entry 1922 (class 2606 OID 716491)
-- Dependencies: 1418 1411 1838 1418 1411
-- Name: qrtz_triggers_job_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_job_name_fkey FOREIGN KEY (job_name, job_group) REFERENCES qrtz_job_details(job_name, job_group);

--
-- TOC entry 1938 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2010-05-28 13:36:35

--
-- PostgreSQL database dump complete
--

-- Metadata Start --

INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'Admin');
INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'Auditor');
INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'Employee');
INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'Mobile');

INSERT INTO users (id, email, "password", first_name, last_name, created_on, role_id, contact, old_passwords, location_id, location_type, approved_location_id, approved_location_type) VALUES (nextval('user_seq'), 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Administrator', '', '2009-12-12', 1, NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO module (id, name, description) VALUES (nextval('module_seq'), 'Monitor', NULL);
INSERT INTO module (id, name, description) VALUES (nextval('module_seq'), 'Administrator', NULL);
INSERT INTO module (id, name, description) VALUES (nextval('module_seq'), 'Reports', NULL);
INSERT INTO module (id, name, description) VALUES (nextval('module_seq'), 'Profile', NULL);
INSERT INTO module (id, name, description) VALUES (nextval('module_seq'), 'Manage Floor Plan', NULL);

INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 1, 1, 3);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 1, 2, 3);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 1, 3, 3);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 2, 1, 3);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 2, 2, 1);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 2, 3, 1);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 3, 1, 3);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 3, 2, 3);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 3, 3, 1);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 4, 1, 3);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 4, 2, 1);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 4, 3, 1);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 5, 1, 3);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 5, 2, 1);
INSERT INTO module_permission (id, module_id, role_id, permission) VALUES (nextval('module_permission_seq'), 5, 3, 1);

INSERT INTO permission_details (id, name, description) VALUES (1, 'none', NULL);
INSERT INTO permission_details (id, name, description) VALUES (2, 'view', NULL);
INSERT INTO permission_details (id, name, description) VALUES (3, 'edit', NULL);

INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Adak(GMT-10:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Atka(GMT-10:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Anchorage(GMT-9:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Juneau(GMT-9:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Nome(GMT-9:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Yakutat(GMT-9:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Dawson(GMT-8:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Ensenada(GMT-8:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Los_Angeles(GMT-8:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tijuana(GMT-8:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Vancouver(GMT-8:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Whitehorse(GMT-8:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Pacific(GMT-8:00) Canada', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Yukon(GMT-8:00) Canada', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'BajaNorte(GMT-8:00) Mexico', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Boise(GMT-7:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Cambridge_Bay(GMT-7:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Chihuahua(GMT-7:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Dawson_Creek(GMT-7:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Denver(GMT-7:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Edmonton(GMT-7:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Hermosillo(GMT-7:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Inuvik(GMT-7:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Mazatlan(GMT-7:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Phoenix(GMT-7:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Shiprock(GMT-7:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Yellowknife(GMT-7:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Mountain(GMT-7:00) Canada', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'BajaSur(GMT-7:00) Mexico', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Belize(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Cancun(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Chicago(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Costa_Rica(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'El_Salvador(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Guatemala(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Knox_IN(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Managua(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Menominee(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Merida(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Mexico_City(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Monterrey(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Rainy_River(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Rankin_Inlet(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Regina(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Swift_Current(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tegucigalpa(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Winnipeg(GMT-6:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Central(GMT-6:00) Canada', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'East-Saskatchewan(GMT-6:00) Canada', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Saskatchewan(GMT-6:00) Canada', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'EasterIsland(GMT-6:00) Chile', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'General(GMT-6:00) Mexico', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Atikokan(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Bogota(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Cayman(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Coral_Harbour(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Detroit(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Fort_Wayne(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Grand_Turk(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Guayaquil(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Havana(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Indianapolis(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Iqaluit(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Jamaica(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Lima(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Louisville(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Montreal(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Nassau(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'New_York(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Nipigon(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Panama(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Pangnirtung(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Port-au-Prince(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Resolute(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Thunder_Bay(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Toronto(GMT-5:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Eastern(GMT-5:00) Canada', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Caracas(GMT-4:-30) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Anguilla(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Antigua(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Aruba(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Asuncion(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Barbados(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Blanc-Sablon(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Boa_Vista(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Campo_Grande(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Cuiaba(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Curacao(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Dominica(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Eirunepe(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Glace_Bay(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Goose_Bay(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Grenada(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Guadeloupe(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Guyana(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Halifax(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'La_Paz(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Manaus(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Marigot(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Martinique(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Moncton(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Montserrat(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Port_of_Spain(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Porto_Acre(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Porto_Velho(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Puerto_Rico(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Rio_Branco(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Santiago(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Santo_Domingo(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'St_Barthelemy(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'St_Kitts(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'St_Lucia(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'St_Thomas(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'St_Vincent(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Thule(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tortola(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Virgin(GMT-4:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Palmer(GMT-4:00) Antarctica', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Bermuda(GMT-4:00) Atlantic', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Stanley(GMT-4:00) Atlantic', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Acre(GMT-4:00) Brazil', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'West(GMT-4:00) Brazil', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Atlantic(GMT-4:00) Canada', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Continental(GMT-4:00) Chile', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'St_Johns(GMT-3:-30) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Newfoundland(GMT-3:-30) Canada', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Araguaina(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Bahia(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Belem(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Buenos_Aires(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Catamarca(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Cayenne(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Cordoba(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Fortaleza(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Godthab(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Jujuy(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Maceio(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Mendoza(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Miquelon(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Montevideo(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Paramaribo(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Recife(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Rosario(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Santarem(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Sao_Paulo(GMT-3:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Rothera(GMT-3:00) Antarctica', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'East(GMT-3:00) Brazil', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Noronha(GMT-2:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'South_Georgia(GMT-2:00) Atlantic', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'DeNoronha(GMT-2:00) Brazil', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Scoresbysund(GMT-1:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Azores(GMT-1:00) Atlantic', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Cape_Verde(GMT-1:00) Atlantic', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Abidjan(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Accra(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Bamako(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Banjul(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Bissau(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Casablanca(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Conakry(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Dakar(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'El_Aaiun(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Freetown(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Lome(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Monrovia(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Nouakchott(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Ouagadougou(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Sao_Tome(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Timbuktu(GMT+0:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Danmarkshavn(GMT+0:00) America', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Canary(GMT+0:00) Atlantic', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Faeroe(GMT+0:00) Atlantic', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Faroe(GMT+0:00) Atlantic', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Madeira(GMT+0:00) Atlantic', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Reykjavik(GMT+0:00) Atlantic', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'St_Helena(GMT+0:00) Atlantic', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Belfast(GMT+0:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Dublin(GMT+0:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Guernsey(GMT+0:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Isle_of_Man(GMT+0:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Jersey(GMT+0:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Lisbon(GMT+0:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'London(GMT+0:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Algiers(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Bangui(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Brazzaville(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Ceuta(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Douala(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Kinshasa(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Lagos(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Libreville(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Luanda(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Malabo(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Ndjamena(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Niamey(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Porto-Novo(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tunis(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Windhoek(GMT+1:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Longyearbyen(GMT+1:00) Arctic', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Jan_Mayen(GMT+1:00) Atlantic', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Amsterdam(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Andorra(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Belgrade(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Berlin(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Bratislava(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Brussels(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Budapest(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Copenhagen(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Gibraltar(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Ljubljana(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Luxembourg(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Madrid(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Malta(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Monaco(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Oslo(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Paris(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Podgorica(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Prague(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Rome(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'San_Marino(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Sarajevo(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Skopje(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Stockholm(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tirane(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Vaduz(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Vatican(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Vienna(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Warsaw(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Zagreb(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Zurich(GMT+1:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Blantyre(GMT+2:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Bujumbura(GMT+2:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Cairo(GMT+2:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Gaborone(GMT+2:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Harare(GMT+2:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Johannesburg(GMT+2:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Kigali(GMT+2:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Lubumbashi(GMT+2:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Lusaka(GMT+2:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Maputo(GMT+2:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Maseru(GMT+2:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Mbabane(GMT+2:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tripoli(GMT+2:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Amman(GMT+2:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Beirut(GMT+2:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Damascus(GMT+2:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Gaza(GMT+2:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Istanbul(GMT+2:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Jerusalem(GMT+2:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Nicosia(GMT+2:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tel_Aviv(GMT+2:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Athens(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Bucharest(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Chisinau(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Helsinki(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Istanbul(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Kaliningrad(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Kiev(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Mariehamn(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Minsk(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Nicosia(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Riga(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Simferopol(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Sofia(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tallinn(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tiraspol(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Uzhgorod(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Vilnius(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Zaporozhye(GMT+2:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Addis_Ababa(GMT+3:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Asmara(GMT+3:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Asmera(GMT+3:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Dar_es_Salaam(GMT+3:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Djibouti(GMT+3:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Kampala(GMT+3:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Khartoum(GMT+3:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Mogadishu(GMT+3:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Nairobi(GMT+3:00) Africa', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Syowa(GMT+3:00) Antarctica', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Aden(GMT+3:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Baghdad(GMT+3:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Bahrain(GMT+3:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Kuwait(GMT+3:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Qatar(GMT+3:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Moscow(GMT+3:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Volgograd(GMT+3:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Antananarivo(GMT+3:00) Indian', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Comoro(GMT+3:00) Indian', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Mayotte(GMT+3:00) Indian', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tehran(GMT+3:30) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Baku(GMT+4:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Dubai(GMT+4:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Muscat(GMT+4:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tbilisi(GMT+4:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Yerevan(GMT+4:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Samara(GMT+4:00) Europe', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Mahe(GMT+4:00) Indian', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Mauritius(GMT+4:00) Indian', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Reunion(GMT+4:00) Indian', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Kabul(GMT+4:30) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Aqtau(GMT+5:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Aqtobe(GMT+5:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Ashgabat(GMT+5:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Ashkhabad(GMT+5:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Dushanbe(GMT+5:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Karachi(GMT+5:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Oral(GMT+5:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Samarkand(GMT+5:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tashkent(GMT+5:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Yekaterinburg(GMT+5:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Kerguelen(GMT+5:00) Indian', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Maldives(GMT+5:00) Indian', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Calcutta(GMT+5:30) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Colombo(GMT+5:30) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Kolkata(GMT+5:30) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Katmandu(GMT+5:45) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Mawson(GMT+6:00) Antarctica', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Vostok(GMT+6:00) Antarctica', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Almaty(GMT+6:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Bishkek(GMT+6:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Dacca(GMT+6:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Dhaka(GMT+6:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Novosibirsk(GMT+6:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Omsk(GMT+6:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Qyzylorda(GMT+6:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Thimbu(GMT+6:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Thimphu(GMT+6:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Chagos(GMT+6:00) Indian', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Rangoon(GMT+6:30) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Cocos(GMT+6:30) Indian', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Davis(GMT+7:00) Antarctica', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Bangkok(GMT+7:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Ho_Chi_Minh(GMT+7:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Hovd(GMT+7:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Jakarta(GMT+7:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Krasnoyarsk(GMT+7:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Phnom_Penh(GMT+7:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Pontianak(GMT+7:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Saigon(GMT+7:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Vientiane(GMT+7:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Christmas(GMT+7:00) Indian', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Casey(GMT+8:00) Antarctica', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Brunei(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Choibalsan(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Chongqing(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Chungking(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Harbin(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Hong_Kong(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Irkutsk(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Kashgar(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Kuala_Lumpur(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Kuching(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Macao(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Macau(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Makassar(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Manila(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Shanghai(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Singapore(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Taipei(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Ujung_Pandang(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Ulaanbaatar(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Ulan_Bator(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Urumqi(GMT+8:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Perth(GMT+8:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'West(GMT+8:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Eucla(GMT+8:45) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Dili(GMT+9:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Jayapura(GMT+9:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Pyongyang(GMT+9:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Seoul(GMT+9:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tokyo(GMT+9:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Yakutsk(GMT+9:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Adelaide(GMT+9:30) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Broken_Hill(GMT+9:30) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Darwin(GMT+9:30) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'North(GMT+9:30) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'South(GMT+9:30) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Yancowinna(GMT+9:30) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'DumontDUrville(GMT+10:00) Antarctica', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Sakhalin(GMT+10:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Vladivostok(GMT+10:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'ACT(GMT+10:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Brisbane(GMT+10:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Canberra(GMT+10:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Currie(GMT+10:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Hobart(GMT+10:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Lindeman(GMT+10:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Melbourne(GMT+10:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'NSW(GMT+10:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Queensland(GMT+10:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Sydney(GMT+10:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Tasmania(GMT+10:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Victoria(GMT+10:00) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'LHI(GMT+10:30) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Lord_Howe(GMT+10:30) Australia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Magadan(GMT+11:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'McMurdo(GMT+12:00) Antarctica', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'South_Pole(GMT+12:00) Antarctica', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Anadyr(GMT+12:00) Asia', NULL);
INSERT INTO timezone (id, name, description) VALUES (nextval('timezone_seq'), 'Kamchatka(GMT+12:00) Asia', NULL);

INSERT INTO event_type (id, "type", description) VALUES (nextval('event_type_seq'), 'Fixture Out', NULL);
INSERT INTO event_type (id, "type", description) VALUES (nextval('event_type_seq'), 'Push Profile', NULL);
INSERT INTO event_type (id, "type", description) VALUES (nextval('event_type_seq'), 'Profile Mismatch', NULL);
INSERT INTO event_type (id, "type", description) VALUES (nextval('event_type_seq'), 'Bad Profile', NULL);
INSERT INTO event_type (id, "type", description) VALUES (nextval('event_type_seq'), 'Fixture Upgrade', NULL);
INSERT INTO event_type (id, "type", description) VALUES (nextval('event_type_seq'), 'Gateway Upgrade', NULL);
INSERT INTO event_type (id, "type", description) VALUES (nextval('event_type_seq'), 'Fixture CU Failure', NULL);
INSERT INTO event_type (id, "type", description) VALUES (nextval('event_type_seq'), 'Fixture Image Checksum Failure', NULL);
INSERT INTO event_type (id, "type", description) VALUES (nextval('event_type_seq'), 'DR Condition', NULL);
INSERT INTO event_type (id, "type", description) VALUES (nextval('event_type_seq'), 'Fixture associated Group Changed', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Profile Mismatch User Action', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Bacnet', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Discovery', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Commissioning', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture Hardware Failure', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture Too Hot', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Fixture CPU Usage is High', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'EM upgrade', NULL);
INSERT INTO event_type (id, "type", description) VALUES ((select coalesce(max(id),0)+1 from event_type), 'Scheduler', NULL);

INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (1, 'Philips                                                                                                                         ', 'F17T8/TL830/ALTO', 'T8                                                                                                                              ', 1400, 1330, 17, 30000, 36000, 1, 24, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (2, 'Philips                                                                                                                         ', 'F17T8/TL835/ALTO', 'T8                                                                                                                              ', 1400, 1330, 17, 30000, 36000, 1, 24, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (3, 'Philips                                                                                                                         ', 'F17T8/TL841/ALTO', 'T8                                                                                                                              ', 1400, 1330, 17, 30000, 36000, 1, 24, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (4, 'Philips                                                                                                                         ', 'F17T8/TL850/ALTO', 'T8                                                                                                                              ', 1325, 1260, 17, 30000, 36000, 1, 24, 82, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (5, 'Philips                                                                                                                         ', 'F17T8/TL730/ALTO', 'T8                                                                                                                              ', 1325, 1260, 17, 30000, 36000, 1, 24, 78, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (6, 'Philips                                                                                                                         ', 'F17T8/TL735/ALTO', 'T8                                                                                                                              ', 1325, 1260, 17, 30000, 36000, 1, 24, 78, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (7, 'Philips                                                                                                                         ', 'F17T8/TL741/ALTO', 'T8                                                                                                                              ', 1325, 1260, 17, 30000, 36000, 1, 24, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (8, 'Philips                                                                                                                         ', 'F25T8/TL830/ALTO', 'T8                                                                                                                              ', 2225, 2115, 25, 30000, 36000, 1, 36, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (9, 'Philips                                                                                                                         ', 'F25T8/TL835/ALTO', 'T8                                                                                                                              ', 2225, 2115, 25, 30000, 36000, 1, 36, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (10, 'Philips                                                                                                                         ', 'F25T8/TL841/ALTO', 'T8                                                                                                                              ', 2225, 2115, 25, 30000, 36000, 1, 36, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (11, 'Philips                                                                                                                         ', 'F25T8/TL850/ALTO', 'T8                                                                                                                              ', 2150, 2040, 25, 30000, 36000, 1, 36, 82, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (12, 'Philips                                                                                                                         ', 'F25T8/TL730/ALTO', 'T8                                                                                                                              ', 2125, 2020, 25, 30000, 36000, 1, 36, 78, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (13, 'Philips                                                                                                                         ', 'F25T8/TL735/ALTO', 'T8                                                                                                                              ', 2125, 2020, 25, 30000, 36000, 1, 36, 78, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (14, 'Philips                                                                                                                         ', 'F25T8/TL741/ALTO', 'T8                                                                                                                              ', 2125, 2020, 25, 30000, 36000, 1, 36, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (15, 'Philips                                                                                                                         ', 'F32T8/TL830/ALTO', 'T8                                                                                                                              ', 2950, 2800, 32, 30000, 36000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (16, 'Philips                                                                                                                         ', 'F32T8/TL835/ALTO', 'T8                                                                                                                              ', 2950, 2800, 32, 30000, 36000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (17, 'Philips                                                                                                                         ', 'F32T8/TL841/ALTO', 'T8                                                                                                                              ', 2950, 2800, 32, 30000, 36000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (18, 'Philips                                                                                                                         ', 'F32T8/TL850/ALTO', 'T8                                                                                                                              ', 2850, 2700, 32, 30000, 36000, 1, 48, 82, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (19, 'Philips                                                                                                                         ', 'F32T8/TL730/ALTO', 'T8                                                                                                                              ', 2800, 2660, 32, 30000, 36000, 1, 48, 78, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (20, 'Philips                                                                                                                         ', 'F32T8/TL735/ALTO', 'T8                                                                                                                              ', 2800, 2660, 32, 30000, 36000, 1, 48, 78, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (21, 'Philips                                                                                                                         ', 'F32T8/TL741/ALTO', 'T8                                                                                                                              ', 2800, 2660, 32, 30000, 36000, 1, 48, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (22, 'Philips                                                                                                                         ', 'F32T8/TL741/ALTO', 'T8                                                                                                                              ', 2800, 2660, 32, 30000, 36000, 1, 48, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (23, 'Philips                                                                                                                         ', 'F32T8/TL750/ALTO', 'T8                                                                                                                              ', 2700, 2565, 32, 30000, 36000, 1, 48, 78, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (24, 'Philips                                                                                                                         ', 'F17T8/TL830/PLUS/ALTO', 'T8                                                                                                                              ', 1400, 1330, 17, 36000, 42000, 1, 24, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (25, 'Philips                                                                                                                         ', 'F17T8/TL835/PLUS/ALTO', 'T8                                                                                                                              ', 1400, 1330, 17, 36000, 42000, 1, 24, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (26, 'Philips                                                                                                                         ', 'F17T8/TL841/PLUS/ALTO', 'T8                                                                                                                              ', 1400, 1330, 17, 36000, 42000, 1, 24, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (27, 'Philips                                                                                                                         ', 'F17T8/TL850/PLUS/ALTO', 'T8                                                                                                                              ', 1300, 1235, 17, 36000, 42000, 1, 24, 82, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (28, 'Philips                                                                                                                         ', 'F17T8/TL865/PLUS/ALTO', 'T8                                                                                                                              ', 1275, 1210, 17, 36000, 42000, 1, 24, 85, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (29, 'Philips                                                                                                                         ', 'F25T8/TL830/PLUS/ALTO', 'T8                                                                                                                              ', 2225, 2115, 25, 36000, 42000, 1, 36, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (30, 'Philips                                                                                                                         ', 'F25T8/TL835/PLUS/ALTO', 'T8                                                                                                                              ', 2225, 2115, 25, 36000, 42000, 1, 36, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (31, 'Philips                                                                                                                         ', 'F25T8/TL841/PLUS/ALTO', 'T8                                                                                                                              ', 2225, 2115, 25, 36000, 42000, 1, 36, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (32, 'Philips                                                                                                                         ', 'F25T8/TL850/PLUS/ALTO', 'T8                                                                                                                              ', 2150, 2040, 25, 36000, 42000, 1, 36, 82, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (33, 'Philips                                                                                                                         ', 'F25T8/TL865/PLUS/ALTO', 'T8                                                                                                                              ', 2125, 2020, 25, 36000, 42000, 1, 36, 85, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (34, 'Philips                                                                                                                         ', 'F32T8/TL830/PLUS/ALTO', 'T8                                                                                                                              ', 2950, 2800, 32, 36000, 42000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (35, 'Philips                                                                                                                         ', 'F32T8/TL835/PLUS/ALTO', 'T8                                                                                                                              ', 2950, 2800, 32, 36000, 42000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (36, 'Philips                                                                                                                         ', 'F32T8/TL841/PLUS/ALTO', 'T8                                                                                                                              ', 2950, 2800, 32, 36000, 42000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (37, 'Philips                                                                                                                         ', 'F32T8/TL850/PLUS/ALTO', 'T8                                                                                                                              ', 2850, 2710, 32, 36000, 42000, 1, 48, 82, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (38, 'Philips                                                                                                                         ', 'F32T8/TL865/PLUS/ALTO', 'T8                                                                                                                              ', 2750, 2610, 32, 36000, 42000, 1, 48, 85, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (39, 'Philips                                                                                                                         ', 'F32T8/TL730/PLUS/ALTO', 'T8                                                                                                                              ', 2800, 2660, 32, 36000, 42000, 1, 48, 78, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (40, 'Philips                                                                                                                         ', 'F32T8/TL735/PLUS/ALTO', 'T8                                                                                                                              ', 2800, 2660, 32, 36000, 42000, 1, 48, 78, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (41, 'Philips                                                                                                                         ', 'F32T8/TL741/PLUS/ALTO', 'T8                                                                                                                              ', 2800, 2660, 32, 36000, 42000, 1, 48, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (42, 'Philips                                                                                                                         ', 'F32T8/TL750/PLUS/ALTO', 'T8                                                                                                                              ', 2700, 2565, 32, 36000, 42000, 1, 48, 78, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (43, 'Philips                                                                                                                         ', 'F17T8/ADV830/ALTO', 'T8                                                                                                                              ', 1500, 1450, 17, 30000, 36000, 1, 24, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (44, 'Philips                                                                                                                         ', 'F17T8/ADV835/ALTO', 'T8                                                                                                                              ', 1500, 1450, 17, 30000, 36000, 1, 24, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (45, 'Philips                                                                                                                         ', 'F17T8/ADV841/ALTO', 'T8                                                                                                                              ', 1500, 1450, 17, 30000, 36000, 1, 24, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (46, 'Philips                                                                                                                         ', 'F17T8/ADV850/ALTO', 'T8                                                                                                                              ', 1425, 1380, 17, 30000, 36000, 1, 24, 82, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (47, 'Philips                                                                                                                         ', 'F25T8/ADV830/ALTO', 'T8                                                                                                                              ', 2380, 2300, 25, 30000, 36000, 1, 36, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (48, 'Philips                                                                                                                         ', 'F25T8/ADV835/ALTO', 'T8                                                                                                                              ', 2380, 2300, 25, 30000, 36000, 1, 36, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (49, 'Philips                                                                                                                         ', 'F25T8/ADV841/ALTO', 'T8                                                                                                                              ', 2380, 2300, 25, 30000, 36000, 1, 36, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (50, 'Philips                                                                                                                         ', 'F25T8/ADV850/ALTO', 'T8                                                                                                                              ', 2275, 2210, 25, 30000, 36000, 1, 36, 82, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (51, 'Philips                                                                                                                         ', 'F32T8/ADV830/ALTO', 'T8                                                                                                                              ', 3100, 3000, 32, 30000, 36000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (52, 'Philips                                                                                                                         ', 'F32T8/ADV835/ALTO', 'T8                                                                                                                              ', 3100, 3000, 32, 30000, 36000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (53, 'Philips                                                                                                                         ', 'F32T8/ADV841/ALTO', 'T8                                                                                                                              ', 3100, 3000, 32, 30000, 36000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (54, 'Philips                                                                                                                         ', 'F32T8/ADV850/ALTO', 'T8                                                                                                                              ', 3100, 3000, 32, 30000, 36000, 1, 48, 82, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (55, 'Philips                                                                                                                         ', 'F32T8/ADV830/XEW/ALTO', 'T8                                                                                                                              ', 2500, 2425, 25, 30000, 36000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (56, 'Philips                                                                                                                         ', 'F32T8/ADV835/XEW/ALTO', 'T8                                                                                                                              ', 2500, 2425, 25, 30000, 36000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (57, 'Philips                                                                                                                         ', 'F32T8/ADV841/XEW/ALTO', 'T8                                                                                                                              ', 2500, 2425, 25, 30000, 36000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (58, 'Philips                                                                                                                         ', 'F32T8/ADV850/XEW/ALTO', 'T8                                                                                                                              ', 2400, 2330, 25, 30000, 36000, 1, 48, 85, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (59, 'Philips                                                                                                                         ', 'F32T8/ADV830/EW/ALTO', 'T8                                                                                                                              ', 2725, 2645, 28, 30000, 36000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (60, 'Philips                                                                                                                         ', 'F32T8/ADV835/EW/ALTO', 'T8                                                                                                                              ', 2725, 2645, 28, 30000, 36000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (61, 'Philips                                                                                                                         ', 'F32T8/ADV841/EW/ALTO', 'T8                                                                                                                              ', 2725, 2645, 28, 30000, 36000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (62, 'Philips                                                                                                                         ', 'F32T8/ADV850/EW/ALTO', 'T8                                                                                                                              ', 2675, 2595, 28, 30000, 36000, 1, 48, 85, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (63, 'Philips                                                                                                                         ', 'F32T8/ADV830/EW/ALTO', 'T8                                                                                                                              ', 2850, 2765, 30, 30000, 36000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (64, 'Philips                                                                                                                         ', 'F32T8/ADV835/EW/ALTO', 'T8                                                                                                                              ', 2850, 2765, 30, 30000, 36000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (65, 'Philips                                                                                                                         ', 'F32T8/ADV841/EW/ALTO', 'T8                                                                                                                              ', 2850, 2765, 30, 30000, 36000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (66, 'Philips                                                                                                                         ', 'F32T8/ADV850/EW/ALTO', 'T8                                                                                                                              ', 2800, 2715, 30, 30000, 36000, 1, 48, 85, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (67, 'Philips                                                                                                                         ', 'F32T8/TL830/XLL/ALTO', 'T8                                                                                                                              ', 2950, 2800, 32, 40000, 46000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (68, 'Philips                                                                                                                         ', 'F32T8/TL835/XLL/ALTO', 'T8                                                                                                                              ', 2950, 2800, 32, 40000, 46000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (69, 'Philips                                                                                                                         ', 'F32T8/TL841/XLL/ALTO', 'T8                                                                                                                              ', 2950, 2800, 32, 40000, 46000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (70, 'Philips                                                                                                                         ', 'F32T8/TL850/XLL/ALTO', 'T8                                                                                                                              ', 2850, 2700, 32, 40000, 46000, 1, 48, 85, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (71, 'Philips                                                                                                                         ', 'F32T8/ADV830/XLL/ALTO', 'T8                                                                                                                              ', 2400, 2330, 25, 40000, 46000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (72, 'Philips                                                                                                                         ', 'F32T8/ADV835/XLL/ALTO', 'T8                                                                                                                              ', 2400, 2330, 25, 40000, 46000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (73, 'Philips                                                                                                                         ', 'F32T8/ADV841/XLL/ALTO', 'T8                                                                                                                              ', 2400, 2330, 25, 40000, 46000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (74, 'Philips                                                                                                                         ', 'F32T8/ADV850/XLL/ALTO', 'T8                                                                                                                              ', 2350, 2280, 25, 40000, 46000, 1, 48, 85, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (75, 'Sylvania                                                                                                                        ', 'FO32/830/XPS/ECO', 'T8                                                                                                                              ', 3100, 2945, 32, 36000, 42000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (76, 'Sylvania                                                                                                                        ', 'FO32/835/XPS/ECO', 'T8                                                                                                                              ', 3100, 2945, 32, 36000, 42000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (77, 'Sylvania                                                                                                                        ', 'FO32/841/XPS/ECO', 'T8                                                                                                                              ', 3100, 2945, 32, 36000, 42000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (78, 'Sylvania                                                                                                                        ', 'FO32/850/XPS/ECO', 'T8                                                                                                                              ', 3000, 2850, 32, 36000, 42000, 1, 48, 80, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (79, 'Sylvania                                                                                                                        ', 'FO32/865/XPS/ECO', 'T8                                                                                                                              ', 2900, 2750, 32, 36000, 42000, 1, 48, 80, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (80, 'Sylvania                                                                                                                        ', 'FO17/830/XPS/ECO', 'T8                                                                                                                              ', 1400, 1330, 17, 30000, NULL, 1, 24, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (81, 'Sylvania                                                                                                                        ', 'FO17/835/XPS/ECO', 'T8                                                                                                                              ', 1400, 1330, 17, 30000, NULL, 1, 24, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (82, 'Sylvania                                                                                                                        ', 'FO17/841/XPS/ECO', 'T8                                                                                                                              ', 1400, 1330, 17, 30000, NULL, 1, 24, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (83, 'Sylvania                                                                                                                        ', 'FO25/830/XPS/ECO', 'T8                                                                                                                              ', 2200, 2090, 25, 30000, NULL, 1, 36, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (84, 'Sylvania                                                                                                                        ', 'FO25/835/XPS/ECO', 'T8                                                                                                                              ', 2200, 2090, 25, 30000, NULL, 1, 36, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (85, 'Sylvania                                                                                                                        ', 'FO25/841/XPS/ECO', 'T8                                                                                                                              ', 2200, 2090, 25, 30000, NULL, 1, 36, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (86, 'Sylvania                                                                                                                        ', 'FO32/25W/830/XP/SS/ECO', 'T8                                                                                                                              ', 2475, 2350, 25, 36000, 42000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (87, 'Sylvania                                                                                                                        ', 'FO32/25W/835/XP/SS/ECO', 'T8                                                                                                                              ', 2475, 2350, 25, 36000, 42000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (88, 'Sylvania                                                                                                                        ', 'FO32/25W/841/XP/SS/ECO', 'T8                                                                                                                              ', 2475, 2350, 25, 36000, 42000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (89, 'Sylvania                                                                                                                        ', 'FO32/25W/850/XP/SS/ECO', 'T8                                                                                                                              ', 2300, 2185, 25, 36000, 42000, 1, 48, 85, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (90, 'Sylvania                                                                                                                        ', 'FO28/830/XP/SS/ECO', 'T8                                                                                                                              ', 2725, 2590, 28, 36000, 42000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (91, 'Sylvania                                                                                                                        ', 'FO28/835/XP/SS/ECO', 'T8                                                                                                                              ', 2725, 2590, 28, 36000, 42000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (92, 'Sylvania                                                                                                                        ', 'FO28/841/XP/SS/ECO', 'T8                                                                                                                              ', 2725, 2590, 28, 36000, 42000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (93, 'Sylvania                                                                                                                        ', 'FO28/850XP/SS/ECO', 'T8                                                                                                                              ', 2600, 2470, 28, 36000, 42000, 1, 48, 80, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (94, 'Sylvania                                                                                                                        ', 'FO30/830/XP/SS/ECO', 'T8                                                                                                                              ', 2850, 2710, 30, 36000, 42000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (95, 'Sylvania                                                                                                                        ', 'FO30/835/XP/SS/ECO', 'T8                                                                                                                              ', 2850, 2710, 30, 36000, 42000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (96, 'Sylvania                                                                                                                        ', 'FO30/841/XP/SS/ECO', 'T8                                                                                                                              ', 2850, 2710, 30, 36000, 42000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (97, 'Sylvania                                                                                                                        ', 'FO30/850/XP/SS/ECO', 'T8                                                                                                                              ', 2800, 2660, 30, 36000, 42000, 1, 48, 85, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (98, 'Sylvania                                                                                                                        ', 'FO32/827/XP/ECO', 'T8                                                                                                                              ', 3000, 2850, 32, 36000, 42000, 1, 48, 85, 2700);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (99, 'Sylvania                                                                                                                        ', 'FO32/830/XP/ECO', 'T8                                                                                                                              ', 3000, 2850, 32, 36000, 42000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (100, 'Sylvania                                                                                                                        ', 'FO32/835/XP/ECO', 'T8                                                                                                                              ', 3000, 2850, 32, 36000, 42000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (101, 'Sylvania                                                                                                                        ', 'FO32/841/XP/ECO', 'T8                                                                                                                              ', 3000, 2850, 32, 36000, 42000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (102, 'Sylvania                                                                                                                        ', 'FO32/850/XP/ECO', 'T8                                                                                                                              ', 3000, 2850, 32, 36000, 42000, 1, 48, 85, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (103, 'Sylvania                                                                                                                        ', 'FO32/865/XP/ECO', 'T8                                                                                                                              ', 2850, 2708, 32, 36000, 42000, 1, 48, 85, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (104, 'Sylvania                                                                                                                        ', 'FO32/SKYWHITE/XP/ECO', 'T8                                                                                                                              ', 2650, 2518, 32, 36000, 42000, 1, 48, 88, 8000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (105, 'Sylvania                                                                                                                        ', 'FO17/827/XP/ECO', 'T8                                                                                                                              ', 1375, 1305, 17, 24000, NULL, 1, 24, 85, 2700);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (106, 'Sylvania                                                                                                                        ', 'FO17/830/XP/ECO', 'T8                                                                                                                              ', 1375, 1305, 17, 24000, NULL, 1, 24, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (107, 'Sylvania                                                                                                                        ', 'FO17/835/XP/ECO', 'T8                                                                                                                              ', 1375, 1305, 17, 24000, NULL, 1, 24, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (108, 'Sylvania                                                                                                                        ', 'FO17/841/XP/ECO', 'T8                                                                                                                              ', 1375, 1305, 17, 24000, NULL, 1, 24, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (109, 'Sylvania                                                                                                                        ', 'FO17/850/XP/ECO', 'T8                                                                                                                              ', 1375, 1305, 17, 24000, NULL, 1, 24, 85, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (110, 'Sylvania                                                                                                                        ', 'FO17/865/XP/ECO', 'T8                                                                                                                              ', 1250, 1188, 17, 24000, NULL, 1, 24, 85, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (111, 'Sylvania                                                                                                                        ', 'FO25/827/XP/ECO', 'T8                                                                                                                              ', 2175, 2065, 25, 24000, NULL, 1, 36, 85, 2700);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (112, 'Sylvania                                                                                                                        ', 'FO25/830/XP/ECO', 'T8                                                                                                                              ', 2175, 2065, 25, 24000, NULL, 1, 36, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (113, 'Sylvania                                                                                                                        ', 'FO25/835/XP/ECO', 'T8                                                                                                                              ', 2175, 2065, 25, 24000, NULL, 1, 36, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (114, 'Sylvania                                                                                                                        ', 'FO25/841/XP/ECO', 'T8                                                                                                                              ', 2175, 2065, 25, 24000, NULL, 1, 36, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (115, 'Sylvania                                                                                                                        ', 'FO25/850/XP/ECO', 'T8                                                                                                                              ', 2175, 2065, 25, 24000, NULL, 1, 36, 85, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (116, 'Sylvania                                                                                                                        ', 'FO25/865/XP/ECO', 'T8                                                                                                                              ', 2000, 1900, 25, 24000, NULL, 1, 36, 85, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (117, 'Sylvania                                                                                                                        ', 'FO40/830/XP/ECO', 'T8                                                                                                                              ', 3750, 3560, 40, 24000, NULL, 1, 60, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (118, 'Sylvania                                                                                                                        ', 'FO40/835/XP/ECO', 'T8                                                                                                                              ', 3750, 3560, 40, 24000, NULL, 1, 60, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (119, 'Sylvania                                                                                                                        ', 'FO40/841/XP/ECO', 'T8                                                                                                                              ', 3750, 3560, 40, 24000, NULL, 1, 60, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (120, 'Sylvania                                                                                                                        ', 'FO40/865/XP/ECO', 'T8                                                                                                                              ', 3650, 3468, 40, 24000, NULL, 1, 60, 85, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (121, 'Sylvania                                                                                                                        ', 'FO32/730/XP/ECO', 'T8                                                                                                                              ', 2850, 2705, 32, 36000, 42000, 1, 48, 78, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (122, 'Sylvania                                                                                                                        ', 'FO32/735/XP/ECO', 'T8                                                                                                                              ', 2850, 2705, 32, 36000, 42000, 1, 48, 78, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (123, 'Sylvania                                                                                                                        ', 'FO32/741/XP/ECO', 'T8                                                                                                                              ', 2850, 2705, 32, 36000, 42000, 1, 48, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (124, 'Sylvania                                                                                                                        ', 'FO17/735/XP/ECO', 'T8                                                                                                                              ', 1325, 1255, 17, 24000, NULL, 1, 24, 78, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (125, 'Sylvania                                                                                                                        ', 'FO17/741/XP/ECO', 'T8                                                                                                                              ', 1325, 1255, 17, 24000, NULL, 1, 24, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (126, 'Sylvania                                                                                                                        ', 'FO25/735/XP/ECO', 'T8                                                                                                                              ', 2050, 1945, 25, 24000, NULL, 1, 36, 78, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (127, 'Sylvania                                                                                                                        ', 'FO25/741/XP/ECO', 'T8                                                                                                                              ', 2050, 1945, 25, 24000, NULL, 1, 36, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (128, 'Sylvania                                                                                                                        ', 'FO32/830/ECO', 'T8                                                                                                                              ', 2950, 2802, 32, 30000, 35000, 1, 48, 85, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (129, 'Sylvania                                                                                                                        ', 'FO32/835/ECO', 'T8                                                                                                                              ', 2950, 2802, 32, 30000, 35000, 1, 48, 85, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (130, 'Sylvania                                                                                                                        ', 'FO32/841/ECO', 'T8                                                                                                                              ', 2950, 2802, 32, 30000, 35000, 1, 48, 85, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (131, 'Sylvania                                                                                                                        ', 'FO32/850/ECO', 'T8                                                                                                                              ', 2800, 2660, 32, 30000, 35000, 1, 48, 80, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (132, 'Sylvania                                                                                                                        ', 'FO17/830/ECO', 'T8                                                                                                                              ', 1350, 1240, 17, 20000, NULL, 1, 24, 82, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (133, 'Sylvania                                                                                                                        ', 'FO17/835/ECO', 'T8                                                                                                                              ', 1350, 1240, 17, 20000, NULL, 1, 24, 82, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (134, 'Sylvania                                                                                                                        ', 'FO17/841/ECO', 'T8                                                                                                                              ', 1350, 1240, 17, 20000, NULL, 1, 24, 82, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (135, 'Sylvania                                                                                                                        ', 'FO25/830/ECO', 'T8                                                                                                                              ', 2150, 1975, 25, 20000, NULL, 1, 36, 82, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (136, 'Sylvania                                                                                                                        ', 'FO25/835/ECO', 'T8                                                                                                                              ', 2150, 1975, 25, 20000, NULL, 1, 36, 82, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (137, 'Sylvania                                                                                                                        ', 'FO25/841/ECO', 'T8                                                                                                                              ', 2150, 1975, 25, 20000, NULL, 1, 36, 82, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (138, 'Sylvania                                                                                                                        ', 'FO40/830/ECO', 'T8                                                                                                                              ', 3650, 3473, 40, 20000, NULL, 1, 60, 82, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (139, 'Sylvania                                                                                                                        ', 'FO40/835/ECO', 'T8                                                                                                                              ', 3650, 3473, 40, 20000, NULL, 1, 60, 82, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (140, 'Sylvania                                                                                                                        ', 'FO40/841/ECO', 'T8                                                                                                                              ', 3650, 3473, 40, 20000, NULL, 1, 60, 82, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (141, 'Sylvania                                                                                                                        ', 'FO32/730/ECO', 'T8                                                                                                                              ', 2800, 2520, 32, 25000, NULL, 1, 48, 78, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (142, 'Sylvania                                                                                                                        ', 'FO32/735/ECO', 'T8                                                                                                                              ', 2800, 2520, 32, 25000, NULL, 1, 48, 78, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (143, 'Sylvania                                                                                                                        ', 'FO32/735/SL', 'T8                                                                                                                              ', 2716, 2444, 32, 25000, NULL, 1, 48, 78, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (144, 'Sylvania                                                                                                                        ', 'FO32/741/ECO/CVP', 'T8                                                                                                                              ', 2800, 2520, 32, 25000, NULL, 1, 48, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (145, 'Sylvania                                                                                                                        ', 'FO32/741/ECO/SL', 'T8                                                                                                                              ', 2716, 2444, 32, 25000, NULL, 1, 48, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (146, 'Sylvania                                                                                                                        ', 'FO32/741/ECO', 'T8                                                                                                                              ', 2800, 2520, 32, 25000, NULL, 1, 48, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (147, 'Sylvania                                                                                                                        ', 'FO32/750/ECO', 'T8                                                                                                                              ', 2650, 2385, 32, 25000, NULL, 1, 48, 78, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (148, 'Sylvania                                                                                                                        ', 'FO32/765/ECO', 'T8                                                                                                                              ', 2700, 2430, 32, 25000, NULL, 1, 48, 78, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (149, 'Sylvania                                                                                                                        ', 'FO13/735/ECO', 'T8                                                                                                                              ', 830, 745, 14, 20000, NULL, 1, 18, 75, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (150, 'Sylvania                                                                                                                        ', 'FO13/741/ECO', 'T8                                                                                                                              ', 830, 745, 14, 20000, NULL, 1, 18, 75, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (151, 'Sylvania                                                                                                                        ', 'FO17/730/ECO', 'T8                                                                                                                              ', 1300, 1170, 17, 20000, NULL, 1, 24, 75, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (152, 'Sylvania                                                                                                                        ', 'FO17/735/ECO', 'T8                                                                                                                              ', 1300, 1170, 17, 20000, NULL, 1, 24, 75, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (153, 'Sylvania                                                                                                                        ', 'FO17/741/ECO', 'T8                                                                                                                              ', 1300, 1170, 17, 20000, NULL, 1, 24, 75, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (154, 'Sylvania                                                                                                                        ', 'FO25/730/ECO', 'T8                                                                                                                              ', 1950, 1755, 25, 20000, NULL, 1, 36, 75, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (155, 'Sylvania                                                                                                                        ', 'FO25/735/ECO', 'T8                                                                                                                              ', 1950, 1755, 25, 20000, NULL, 1, 36, 75, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (156, 'Sylvania                                                                                                                        ', 'FO25/741/ECO', 'T8                                                                                                                              ', 1950, 1755, 25, 20000, NULL, 1, 36, 75, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (157, 'Sylvania                                                                                                                        ', 'FO40/730/ECO', 'T8                                                                                                                              ', 3500, 3150, 40, 20000, NULL, 1, 60, 75, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (158, 'Sylvania                                                                                                                        ', 'FO40/735/ECO', 'T8                                                                                                                              ', 3500, 3150, 40, 20000, NULL, 1, 60, 75, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (159, 'Sylvania                                                                                                                        ', 'FO40/741/ECO', 'T8                                                                                                                              ', 3500, 3150, 40, 20000, NULL, 1, 60, 75, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (160, 'Sylvania                                                                                                                        ', 'FO32/741ECO/SL', 'T8                                                                                                                              ', 2716, 2444, 32, 25000, NULL, 1, 48, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (161, 'Sylvania                                                                                                                        ', 'F18T8CW/K24', 'T8                                                                                                                              ', 1190, 1035, 18, 7500, NULL, 1, 24, 60, 4200);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (162, 'Sylvania                                                                                                                        ', 'F18T8CW/K24/1/12/UPC', 'T8                                                                                                                              ', 1190, 1035, 18, 7500, NULL, 1, 24, 60, 4200);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (163, 'Sylvania                                                                                                                        ', 'F18T8CW/K26', 'T8                                                                                                                              ', 1280, 1079, 18, 7500, NULL, 1, 26, 60, 4200);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (164, 'Sylvania                                                                                                                        ', 'F18T8/CW/K26PLT', 'T8                                                                                                                              ', 1280, 1079, 18, 7500, NULL, 1, 26, 60, 4200);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (165, 'Sylvania                                                                                                                        ', 'F18T8/D/K26', 'T8                                                                                                                              ', 1100, 957, 18, 7500, NULL, 1, 26, 76, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (166, 'Sylvania                                                                                                                        ', 'F18T8CW/K28', 'T8                                                                                                                              ', 1360, 1131, 18, 7500, NULL, 1, 28, 60, 4200);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (167, 'Sylvania                                                                                                                        ', 'F18T8CW/K30', 'T8                                                                                                                              ', 1400, 1200, 18, 7500, NULL, 1, 30, 60, 4200);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (168, 'Sylvania                                                                                                                        ', 'F30T8/WW', 'T8                                                                                                                              ', 2150, 1871, 30, 7500, NULL, 1, 36, 52, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (169, 'Sylvania                                                                                                                        ', 'F30T8/DWW/RP', 'T8                                                                                                                              ', 2360, 2124, 30, 7500, NULL, 1, 36, 70, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (170, 'Sylvania                                                                                                                        ', 'F30T8/N', 'T8                                                                                                                              ', 1500, 1305, 30, 7500, NULL, 1, 36, 86, 3600);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (171, 'Sylvania                                                                                                                        ', 'F30T8/CWX', 'T8                                                                                                                              ', 1550, 1349, 30, 7500, NULL, 1, 36, 87, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (172, 'Sylvania                                                                                                                        ', 'F30T8/CW', 'T8                                                                                                                              ', 2180, 1897, 30, 7500, NULL, 1, 36, 60, 4200);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (173, 'Sylvania                                                                                                                        ', 'F30T8CW 6/CS ', 'T8                                                                                                                              ', 2180, 1897, 30, 7500, NULL, 1, 36, 60, 4200);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (174, 'Sylvania                                                                                                                        ', 'F30T8/D', 'T8                                                                                                                              ', 1850, 1653, 30, 7500, NULL, 1, 36, 76, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (175, 'Sylvania                                                                                                                        ', 'FO32/735/SL', 'T8                                                                                                                              ', 2716, 2444, 32, 25000, NULL, 1, 48, 78, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (176, 'Sylvania                                                                                                                        ', 'F13T8/CW', 'T8                                                                                                                              ', 530, 461, 13, 7500, NULL, 1, 12, 60, 4200);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (177, 'Sylvania                                                                                                                        ', 'F14T8/CW', 'T8                                                                                                                              ', 685, 644, 14, 7500, NULL, 1, 15, 60, 4200);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (178, 'Sylvania                                                                                                                        ', 'F14T8/D', 'T8                                                                                                                              ', 575, 561, 14, 7500, NULL, 1, 15, 76, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (179, 'Sylvania                                                                                                                        ', 'L58W/840', 'T8                                                                                                                              ', 5200, 4785, 58, 20000, NULL, 1, 60, 80, 4000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (180, 'Sylvania                                                                                                                        ', 'L70W/21-840', 'T8                                                                                                                              ', 6550, 6026, 70, 15000, NULL, 1, 70, 80, 4000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (181, 'Sylvania                                                                                                                        ', 'F15T8/GRO/AQ/RP', 'T8                                                                                                                              ', 325, NULL, 15, 7500, NULL, 1, 18, 66, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (182, 'Sylvania                                                                                                                        ', 'F15T8/DSW/RP', 'T8                                                                                                                              ', 900, 810, 15, 7500, NULL, 1, 18, 70, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (183, 'Sylvania                                                                                                                        ', 'F15T8/D830', 'T8                                                                                                                              ', 920, 846, 15, 7500, NULL, 1, 18, 82, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (184, 'Sylvania                                                                                                                        ', 'F15T8/WW', 'T8                                                                                                                              ', 845, 735, 15, 7500, NULL, 1, 18, 52, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (185, 'Sylvania                                                                                                                        ', 'F15T8/SW/RP', 'T8                                                                                                                              ', 845, 735, 15, 7500, NULL, 1, 18, 52, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (186, 'Sylvania                                                                                                                        ', 'F15T8/D35', 'T8                                                                                                                              ', 940, 846, 15, 7500, NULL, 1, 18, 70, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (187, 'Sylvania                                                                                                                        ', 'F15T8/N', 'T8                                                                                                                              ', 560, 487, 15, 7500, NULL, 1, 18, 86, 3600);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (188, 'Sylvania                                                                                                                        ', 'F15T8/DCW/RP', 'T8                                                                                                                              ', 900, 810, 15, 7500, NULL, 1, 18, 70, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (189, 'Sylvania                                                                                                                        ', 'F15T8/CW', 'T8                                                                                                                              ', 825, 718, 15, 7500, NULL, 1, 18, 60, 4200);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (190, 'Sylvania                                                                                                                        ', 'F15T8/CW/6PK', 'T8                                                                                                                              ', 825, 718, 15, 7500, NULL, 1, 18, 60, 4200);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (191, 'Sylvania                                                                                                                        ', 'F15T8/DSGN50', 'T8                                                                                                                              ', 600, 539, 15, 7500, NULL, 1, 18, 90, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (192, 'Sylvania                                                                                                                        ', 'F15T8/D', 'T8                                                                                                                              ', 700, 653, 15, 7500, NULL, 1, 18, 76, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (193, 'Ushio                                                                                                                           ', 'F15T8WW', 'T8                                                                                                                              ', 800, NULL, 15, 5000, 6500, 1, 18, NULL, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (194, 'Ushio                                                                                                                           ', 'F15T8CW', 'T8                                                                                                                              ', 875, NULL, 15, 5000, 6500, 1, 18, NULL, 4200);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (195, 'Ushio                                                                                                                           ', 'F15T8D', 'T8                                                                                                                              ', 760, NULL, 15, 5000, 6500, 1, 18, NULL, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (196, 'Ushio                                                                                                                           ', 'F17T8/730', 'T8                                                                                                                              ', 1325, NULL, 17, 24000, 30000, 1, 24, 78, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (197, 'Ushio                                                                                                                           ', 'F17T8/735', 'T8                                                                                                                              ', 1325, NULL, 17, 24000, 30000, 1, 24, 78, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (198, 'Ushio                                                                                                                           ', 'F17T8/741', 'T8                                                                                                                              ', 1325, NULL, 17, 24000, 30000, 1, 24, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (199, 'Ushio                                                                                                                           ', 'F17T8/750', 'T8                                                                                                                              ', 1325, NULL, 17, 24000, 30000, 1, 24, 78, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (200, 'Ushio                                                                                                                           ', 'F25T8/730', 'T8                                                                                                                              ', 2125, NULL, 25, 24000, 30000, 1, 36, 78, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (201, 'Ushio                                                                                                                           ', 'F25T8/735', 'T8                                                                                                                              ', 2125, NULL, 25, 24000, 30000, 1, 36, 78, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (202, 'Ushio                                                                                                                           ', 'F25T8/741', 'T8                                                                                                                              ', 2125, NULL, 25, 24000, 30000, 1, 36, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (203, 'Ushio                                                                                                                           ', 'F25T8/750', 'T8                                                                                                                              ', 2125, NULL, 25, 24000, 30000, 1, 36, 78, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (204, 'Ushio                                                                                                                           ', 'F32T8/730', 'T8                                                                                                                              ', 2850, NULL, 32, 24000, 30000, 1, 48, 78, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (205, 'Ushio                                                                                                                           ', 'F32T8/735', 'T8                                                                                                                              ', 2850, NULL, 32, 24000, 30000, 1, 48, 78, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (206, 'Ushio                                                                                                                           ', 'F32T8/741', 'T8                                                                                                                              ', 2850, NULL, 32, 24000, 30000, 1, 48, 78, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (207, 'Ushio                                                                                                                           ', 'F32T8/750', 'T8                                                                                                                              ', 2850, NULL, 32, 24000, 30000, 1, 48, 78, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (208, 'Ushio                                                                                                                           ', 'F28T8/830', 'T8                                                                                                                              ', 2800, NULL, 28, 24000, 30000, 1, 48, 86, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (209, 'Ushio                                                                                                                           ', 'F28T8/835', 'T8                                                                                                                              ', 2800, NULL, 28, 24000, 30000, 1, 48, 86, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (210, 'Ushio                                                                                                                           ', 'F28T8/841', 'T8                                                                                                                              ', 2800, NULL, 28, 24000, 30000, 1, 48, 86, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (211, 'Ushio                                                                                                                           ', 'F17T8/830', 'T8                                                                                                                              ', 1400, NULL, 17, 24000, 30000, 1, 24, 86, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (212, 'Ushio                                                                                                                           ', 'F17T8/835', 'T8                                                                                                                              ', 1400, NULL, 17, 24000, 30000, 1, 24, 86, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (213, 'Ushio                                                                                                                           ', 'F17T8/841', 'T8                                                                                                                              ', 1400, NULL, 17, 24000, 30000, 1, 24, 86, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (214, 'Ushio                                                                                                                           ', 'F17T8/850', 'T8                                                                                                                              ', 1400, NULL, 17, 24000, 30000, 1, 24, 86, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (215, 'Ushio                                                                                                                           ', 'F25T8/830', 'T8                                                                                                                              ', 2250, NULL, 25, 24000, 30000, 1, 36, 86, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (216, 'Ushio                                                                                                                           ', 'F25T8/835', 'T8                                                                                                                              ', 2250, NULL, 25, 24000, 30000, 1, 36, 86, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (217, 'Ushio                                                                                                                           ', 'F25T8/841', 'T8                                                                                                                              ', 2250, NULL, 25, 24000, 30000, 1, 36, 86, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (218, 'Ushio                                                                                                                           ', 'F25T8/850', 'T8                                                                                                                              ', 2250, NULL, 25, 24000, 30000, 1, 36, 86, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (219, 'Ushio                                                                                                                           ', 'F32T8/830', 'T8                                                                                                                              ', 3050, NULL, 32, 24000, 30000, 1, 48, 86, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (220, 'Ushio                                                                                                                           ', 'F32T8/835', 'T8                                                                                                                              ', 3050, NULL, 32, 24000, 30000, 1, 48, 86, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (221, 'Ushio                                                                                                                           ', 'F32T8/841', 'T8                                                                                                                              ', 3050, NULL, 32, 24000, 30000, 1, 48, 86, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (222, 'Ushio                                                                                                                           ', 'F32T8/850', 'T8                                                                                                                              ', 3050, NULL, 32, 24000, 30000, 1, 48, 86, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (223, 'Ushio                                                                                                                           ', 'F32T8/960', 'T8                                                                                                                              ', 1960, NULL, 32, 24000, 30000, 1, 48, 95, 6000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (224, 'Ushio                                                                                                                           ', 'F32T8/841/HL', 'T8                                                                                                                              ', 3150, NULL, 32, 24000, 30000, 1, 48, 86, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (225, 'Ushio                                                                                                                           ', 'F32T8/850/HL', 'T8                                                                                                                              ', 3150, NULL, 32, 24000, 30000, 1, 48, 86, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (226, 'Eiko                                                                                                                            ', 'F28T8/830/ES', 'T8                                                                                                                              ', 2800, 2635, 28, 24000, 30000, 1, 48, 83, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (227, 'Eiko                                                                                                                            ', 'F28T8/835/ES', 'T8                                                                                                                              ', 2800, 2635, 28, 24000, 30000, 1, 48, 83, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (228, 'Eiko                                                                                                                            ', 'F28T8/841/ES', 'T8                                                                                                                              ', 2800, 2635, 28, 24000, 30000, 1, 48, 83, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (229, 'Eiko                                                                                                                            ', 'F28T8/850/ES', 'T8                                                                                                                              ', 2800, 2635, 28, 24000, 30000, 1, 48, 83, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (230, 'Eiko                                                                                                                            ', 'F32T8/830/ES/25W', 'T8                                                                                                                              ', 2400, 2260, 25, 24000, 30000, 1, 48, 83, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (231, 'Eiko                                                                                                                            ', 'F32T8/835/ES/25W', 'T8                                                                                                                              ', 2400, 2260, 25, 24000, 30000, 1, 48, 83, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (232, 'Eiko                                                                                                                            ', 'F32T8/841/ES/25W', 'T8                                                                                                                              ', 2400, 2260, 25, 24000, 30000, 1, 48, 83, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (233, 'Eiko                                                                                                                            ', 'F32T8/850/ES/25W', 'T8                                                                                                                              ', 2350, 2200, 25, 24000, 30000, 1, 48, 83, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (234, 'Eiko                                                                                                                            ', 'F32T8/830K/HL', 'T8                                                                                                                              ', 3100, 2915, 32, 24000, 30000, 1, 48, 83, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (235, 'Eiko                                                                                                                            ', 'F32T8/835K/HL', 'T8                                                                                                                              ', 3100, 2915, 32, 24000, 30000, 1, 48, 83, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (236, 'Eiko                                                                                                                            ', 'F32T8/841K/HL', 'T8                                                                                                                              ', 3100, 2915, 32, 24000, 30000, 1, 48, 83, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (237, 'Eiko                                                                                                                            ', 'F32T8/850K/HL', 'T8                                                                                                                              ', 3000, 2820, 32, 24000, 30000, 1, 48, 83, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (238, 'Eiko                                                                                                                            ', 'F17T8/730K', 'T8                                                                                                                              ', 1325, 1200, 17, 20000, 25000, 1, 24, 73, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (239, 'Eiko                                                                                                                            ', 'F17T8/735K', 'T8                                                                                                                              ', 1325, 1200, 17, 20000, 25000, 1, 24, 73, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (240, 'Eiko                                                                                                                            ', 'F17T8/741K', 'T8                                                                                                                              ', 1325, 1200, 17, 20000, 25000, 1, 24, 73, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (241, 'Eiko                                                                                                                            ', 'F17T8/830K', 'T8                                                                                                                              ', 1400, 1300, 17, 20000, 25000, 1, 24, 83, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (242, 'Eiko                                                                                                                            ', 'F17T8/835K', 'T8                                                                                                                              ', 1400, 1300, 17, 20000, 25000, 1, 24, 83, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (243, 'Eiko                                                                                                                            ', 'F17T8/841K', 'T8                                                                                                                              ', 1400, 1300, 17, 20000, 25000, 1, 24, 83, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (244, 'Eiko                                                                                                                            ', 'F17T8/850K', 'T8                                                                                                                              ', 1400, 1300, 17, 20000, 25000, 1, 24, 83, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (245, 'Eiko                                                                                                                            ', 'F25T8/730K', 'T8                                                                                                                              ', 2125, 1925, 25, 20000, 25000, 1, 36, 73, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (246, 'Eiko                                                                                                                            ', 'F25T8/735K', 'T8                                                                                                                              ', 2125, 1925, 25, 20000, 25000, 1, 36, 73, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (247, 'Eiko                                                                                                                            ', 'F25T8/741K', 'T8                                                                                                                              ', 2125, 1925, 25, 20000, 25000, 1, 36, 73, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (248, 'Eiko                                                                                                                            ', 'F25T8/830K', 'T8                                                                                                                              ', 2225, 2050, 25, 20000, 25000, 1, 36, 83, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (249, 'Eiko                                                                                                                            ', 'F25T8/835K', 'T8                                                                                                                              ', 2225, 2050, 25, 20000, 25000, 1, 36, 83, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (250, 'Eiko                                                                                                                            ', 'F25T8/841K', 'T8                                                                                                                              ', 2225, 2050, 25, 20000, 25000, 1, 36, 83, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (251, 'Eiko                                                                                                                            ', 'F28T8/830', 'T8                                                                                                                              ', 2800, 2635, 28, 20000, 25000, 1, 48, 83, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (252, 'Eiko                                                                                                                            ', 'F28T8/835', 'T8                                                                                                                              ', 2800, 2635, 28, 20000, 25000, 1, 48, 83, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (253, 'Eiko                                                                                                                            ', 'F28T8/841', 'T8                                                                                                                              ', 2800, 2635, 28, 20000, 25000, 1, 48, 83, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (254, 'Eiko                                                                                                                            ', 'F28T8/850', 'T8                                                                                                                              ', 2800, 2635, 28, 20000, 25000, 1, 48, 83, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (255, 'Eiko                                                                                                                            ', 'F32T8/730K', 'T8                                                                                                                              ', 2850, 2705, 32, 20000, 25000, 1, 48, 75, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (256, 'Eiko                                                                                                                            ', 'F32T8/735K', 'T8                                                                                                                              ', 2850, 2705, 32, 20000, 25000, 1, 48, 75, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (257, 'Eiko                                                                                                                            ', 'F32T8/741K', 'T8                                                                                                                              ', 2850, 2705, 32, 20000, 25000, 1, 48, 75, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (258, 'Eiko                                                                                                                            ', 'F32T8/830K', 'T8                                                                                                                              ', 2950, 2800, 32, 20000, 25000, 1, 48, 83, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (259, 'Eiko                                                                                                                            ', 'F32T8/835K', 'T8                                                                                                                              ', 2950, 2800, 32, 20000, 25000, 1, 48, 83, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (260, 'Eiko                                                                                                                            ', 'F32T8/841K', 'T8                                                                                                                              ', 2950, 2800, 32, 20000, 25000, 1, 48, 83, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (261, 'Eiko                                                                                                                            ', 'F32T8/850K', 'T8                                                                                                                              ', 2950, 2800, 32, 20000, 25000, 1, 48, 83, 5000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (262, 'Eiko                                                                                                                            ', 'F10T8/CW', 'T8                                                                                                                              ', 480, 425, 10, 7500, NULL, 1, 13.6, 60, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (263, 'Eiko                                                                                                                            ', 'F13T8/CW', 'T8                                                                                                                              ', 565, 480, 13, 7500, NULL, 1, 12, 60, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (264, 'Eiko                                                                                                                            ', 'F14T8/CW', 'T8                                                                                                                              ', 685, 580, 14, 7500, NULL, 1, 15, 60, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (265, 'Eiko                                                                                                                            ', 'F14T8/D', 'T8                                                                                                                              ', 645, 545, 14, 7500, NULL, 1, 15, 75, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (266, 'Eiko                                                                                                                            ', 'F15T8/CW', 'T8                                                                                                                              ', 825, 725, 15, 7500, NULL, 1, 18, 60, 4100);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (267, 'Eiko                                                                                                                            ', 'F15T8/WW', 'T8                                                                                                                              ', 845, 745, 15, 7500, NULL, 1, 18, 52, 3000);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (268, 'Eiko                                                                                                                            ', 'F15T8/W', 'T8                                                                                                                              ', 800, 725, 15, 7500, NULL, 1, 18, 55, 3500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (269, 'Eiko                                                                                                                            ', 'F15T8/D', 'T8                                                                                                                              ', 700, 615, 15, 7500, NULL, 1, 18, 75, 6500);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (270, 'Eiko                                                                                                                            ', 'F28T8/830/ES', 'T8                                                                                                                              ', NULL, NULL, 28, 24000, 30000, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (271, 'Eiko                                                                                                                            ', 'F28T8/835/ES', 'T8                                                                                                                              ', NULL, NULL, 28, 24000, 30000, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (272, 'Eiko                                                                                                                            ', 'F28T8/841/ES', 'T8                                                                                                                              ', NULL, NULL, 28, 24000, 30000, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (273, 'Eiko                                                                                                                            ', 'F28T8/850/ES', 'T8                                                                                                                              ', NULL, NULL, 28, 24000, 30000, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (274, 'Eiko                                                                                                                            ', 'F32T8/830/ES/25W', 'T8                                                                                                                              ', NULL, NULL, 25, 24000, 30000, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (275, 'Eiko                                                                                                                            ', 'F32T8/835/ES/25W', 'T8                                                                                                                              ', NULL, NULL, 25, 24000, 30000, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (276, 'Eiko                                                                                                                            ', 'F32T8/841/ES/25W', 'T8                                                                                                                              ', NULL, NULL, 25, 24000, 30000, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (277, 'Eiko                                                                                                                            ', 'F32T8/850/ES/25W', 'T8                                                                                                                              ', NULL, NULL, 25, 24000, 30000, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (278, 'Eiko                                                                                                                            ', 'F32T8/830K/HL', 'T8                                                                                                                              ', NULL, NULL, 32, 24000, 30000, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (279, 'Eiko                                                                                                                            ', 'F32T8/835K/HL', 'T8                                                                                                                              ', NULL, NULL, 32, 24000, 30000, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (280, 'Eiko                                                                                                                            ', 'F32T8/841K/HL', 'T8                                                                                                                              ', NULL, NULL, 32, 24000, 30000, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (281, 'Eiko                                                                                                                            ', 'F32T8/850K/HL', 'T8                                                                                                                              ', NULL, NULL, 32, 24000, 30000, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (282, 'Lunera                                                                                                                          ', 'LED', 'LED                                                                                                                             ', NULL, NULL, 32, 0, 0, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (283, 'OSRAM                                                                                                                          ', 'FP28', 'FP28                                                                                                                             ', NULL, NULL, 63, 0, 0, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "initial_lumens", "design_lumens", "energy", "life_ins_start", "life_prog_start", "diameter", "length", "cri", "color_temp") VALUES (284, 'OSRAM                                                                                                                          ', 'FP54T5HO', 'FP54T5HO                                                                                                                             ', NULL, NULL, 120, 0, 0, 1, 48, NULL, NULL);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "energy", "life_ins_start", "life_prog_start", "color_temp", "length", "diameter", "design_lumens", "cri") VALUES (285, 'GE', 'F96T8/XL/SPX41',  'T8', 59, 24000, 24000, 4100, 96, 1, 5950, 86);
INSERT INTO bulbs ("id", "manufacturer", "bulb_name", "type", "energy", "life_ins_start", "life_prog_start", "color_temp", "length", "diameter", "initial_lumens", "design_lumens", "cri") VALUES (286, 'Sylvania', 'FO96/841/XP/ECO',  'T8', 59, 18000, 18000, 4100, 96, 1, 6200, 5890, 82);

--
-- TOC entry 2011 (class 0 OID 18702)
-- Dependencies: 1731
-- Data for Name: ballasts; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (9, 50705, 'QTP 1x32T8/UNV DIM-TC                                                                                                           ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 0.88, 32, 'OSRAM                                                                                                                           ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (10, 50707, 'QTP 2x32T8/UNV DIM-TC                                                                                                           ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.88, 32, 'OSRAM                                                                                                                           ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (11, 50714, 'QTP 3x32T8/UNV DIM-TCL                                                                                                          ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 0.88, 32, 'OSRAM                                                                                                                           ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (12, 50716, 'QTP 4x32T8/UNV DIM-TCL                                                                                                          ', '120-277                                                                                                                         ', 'T8                                                              ', 4, 0.88, 32, 'OSRAM                                                                                                                           ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (13, 50726, 'QTP 2x28T5/UNV DIM-TCL                                                                                                          ', '120-277                                                                                                                         ', 'T5                                                              ', 2, 1, 28, 'OSRAM                                                                                                                           ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (17, 75379, 'GE132MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 0.88, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (18, 75379, 'GE132MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 0.88, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (19, 75380, 'GE232MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.88, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (20, 75380, 'GE232MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1.1000000000000001, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (21, 75380, 'GE232MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.88, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (22, 75380, 'GE232MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1.1000000000000001, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (23, 75381, 'GE332MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 0.88, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (24, 75381, 'GE332MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.97999999999999998, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (25, 75381, 'GE332MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 0.84999999999999998, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (26, 75381, 'GE332MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.93999999999999995, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (27, 75382, 'GE432MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 4, 0.88, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (28, 75382, 'GE432MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 0.90000000000000002, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (29, 75382, 'GE432MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 4, 0.84999999999999998, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (30, 75382, 'GE432MVPS-N-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 0.90000000000000002, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (31, 75383, 'GE232MVPS-H-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1.1799999999999999, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (32, 75383, 'GE232MVPS-H-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1.3400000000000001, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (33, 75383, 'GE232MVPS-H-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1.1499999999999999, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (34, 75383, 'GE232MVPS-H-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1.3300000000000001, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (35, 75384, 'GE332MVPS-H-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1.1799999999999999, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (36, 75384, 'GE332MVPS-H-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1.26, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (37, 75384, 'GE332MVPS-H-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1.1499999999999999, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (38, 75384, 'GE332MVPS-H-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1.25, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (39, 75385, 'GE432MVPS-H-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 4, 1.1799999999999999, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (40, 75385, 'GE432MVPS-H-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1.27, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (41, 75385, 'GE432MVPS-H-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 4, 1.1399999999999999, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (42, 75385, 'GE432MVPS-H-V03                                                                                                                 ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1.24, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (43, 75386, 'GE432MVPS-N-V03W                                                                                                                ', '120-277                                                                                                                         ', 'T8                                                              ', 4, 0.88, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (44, 75386, 'GE432MVPS-N-V03W                                                                                                                ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 0.90000000000000002, 32, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (45, 75386, 'GE432MVPS-N-V03W                                                                                                                ', '120-277                                                                                                                         ', 'T8                                                              ', 4, 0.84999999999999998, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (46, 75386, 'GE432MVPS-N-V03W                                                                                                                ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 0.90000000000000002, 28, 'GE                                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (47, NULL, 'REZ-132-SC                                                                                                                      ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1.05, 17, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (48, NULL, 'REZ-132-SC                                                                                                                      ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1.05, 25, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (49, NULL, 'REZ-132-SC                                                                                                                      ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (50, NULL, 'REZ-2S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1.05, 17, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (51, NULL, 'REZ-2S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1.05, 25, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (52, NULL, 'REZ-2S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (53, NULL, 'VEZ-3S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1.05, 17, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (54, NULL, 'VEZ-3S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1.05, 25, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (55, NULL, 'VEZ-3S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (56, NULL, 'IDA-132-SC                                                                                                                      ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1, 17, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (57, NULL, 'IDA-132-SC                                                                                                                      ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1, 25, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (58, NULL, 'IDA-132-SC                                                                                                                      ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (59, NULL, 'IDA-2S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1, 17, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (60, NULL, 'IDA-2S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1, 25, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (61, NULL, 'IDA-2S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (62, NULL, 'IDA-3S32-G                                                                                                                      ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1, 17, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (63, NULL, 'IDA-3S32-G                                                                                                                      ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1, 25, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (64, NULL, 'IDA-3S32-G                                                                                                                      ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (65, NULL, 'IDA-4S32                                                                                                                        ', '120-277                                                                                                                         ', 'T8                                                              ', 4, 0.88, 25, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (66, NULL, 'IDA-4S32                                                                                                                        ', '120-277                                                                                                                         ', 'T8                                                              ', 4, 0.88, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (67, NULL, 'ILV-2S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.88, 17, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (68, NULL, 'ILV-2S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.88, 25, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (69, NULL, 'ILV-2S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.88, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (70, NULL, 'ILV-4S32-G                                                                                                                      ', '120-277                                                                                                                         ', 'T8                                                              ', 4, 0.88, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (71, NULL, 'IZT-132-SC                                                                                                                      ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1, 17, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (72, NULL, 'IZT-132-SC                                                                                                                      ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1, 25, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (73, NULL, 'IZT-132-SC                                                                                                                      ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (74, NULL, 'IZT-2S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1, 17, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (75, NULL, 'IZT-2S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1, 25, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (76, NULL, 'IZT-2S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (77, NULL, 'IZT-3S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1, 17, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (78, NULL, 'IZT-3S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1, 25, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (79, NULL, 'IZT-3S32-SC                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (80, NULL, 'IZT-4S32                                                                                                                        ', '120-277                                                                                                                         ', 'T8                                                              ', 4, 1, 25, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (81, NULL, 'VZT-4S32-G                                                                                                                      ', '277                                                                                                                             ', 'T8                                                              ', 4, 0.88, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (82, NULL, 'VZT-4S32-HL                                                                                                                     ', '277                                                                                                                             ', 'T8                                                              ', 4, 1.1799999999999999, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (83, NULL, 'VZT-4PSP32-G                                                                                                                    ', '277                                                                                                                             ', 'T8                                                              ', 4, 0.88, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (84, NULL, 'IZT-4S32                                                                                                                        ', '120-277                                                                                                                         ', 'T8                                                              ', 4, 0.88, 32, 'Philips                                                                                                                         ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (85, NULL, 'B232PUS50-A                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 0.88, 32, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (86, NULL, 'B232PUS50-A                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.88, 32, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (87, NULL, 'B132R120S30                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 0.88, 32, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (88, NULL, 'B132R277S30                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 0.89000000000000001, 32, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (89, NULL, 'B232SR120S30                                                                                                                    ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.88, 32, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (90, NULL, 'B232SR277S30                                                                                                                    ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.88, 32, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (91, NULL, 'B332SR120S30                                                                                                                    ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 0.88, 32, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (92, NULL, 'B332SR277S30                                                                                                                    ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 0.88, 32, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (93, NULL, 'B132PUNVDV1                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1, 32, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (94, NULL, 'B132PUNVDV1                                                                                                                     ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1, 32, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (95, NULL, 'B132PUNVSV3-A                                                                                                                   ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 0.88, 32, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (96, NULL, 'B132PUNVSV3-A                                                                                                                   ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 0.84999999999999998, 25, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (97, NULL, 'B132PUNVSV3-A                                                                                                                   ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 0.84999999999999998, 17, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (98, NULL, 'B232PUNVSV3-A                                                                                                                   ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.88, 32, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (99, NULL, 'B232PUNVSV3-A                                                                                                                   ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.84999999999999998, 25, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (100, NULL, 'B232PUNVSV3-A                                                                                                                   ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.84999999999999998, 17, 'Universal Lighting                                                                                                              ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (101, NULL, 'H3D T832 C UNV 1 10                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1, 32, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (102, NULL, 'H3D T832 C UNV 1 17                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1.1699999999999999, 32, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (103, NULL, 'H3D T832 C UNV 2 10                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1, 32, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (104, NULL, 'H3D T832 C UNV 2 17                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1.1699999999999999, 32, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (105, NULL, 'H3D T832 G UNV 3 10                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1, 32, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (106, NULL, 'H3D T832 G UNV 3 17                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1.1699999999999999, 32, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (107, NULL, 'H3D T825 C UNV 1 10                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1, 25, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (108, NULL, 'H3D T825 C UNV 1 17                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1.1699999999999999, 25, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (109, NULL, 'H3D T825 C UNV 2 10                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1, 25, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (110, NULL, 'H3D T825 C UNV 2 17                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1.1699999999999999, 25, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (111, NULL, 'H3D T817 C UNV 1 10                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1, 17, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (112, NULL, 'H3D T817 C UNV 1 17                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 1.1699999999999999, 17, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (113, NULL, 'H3D T817 C UNV 2 10                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1, 17, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (114, NULL, 'H3D T817 C UNV 2 17                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 1.1699999999999999, 17, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (115, NULL, 'EC5 T817 J UNV 1                                                                                                                ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 0.84999999999999998, 17, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (116, NULL, 'EC5 T817 J UNV 2                                                                                                                ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.84999999999999998, 17, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (117, NULL, 'EC5 T825 J UNV 1                                                                                                                ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 0.84999999999999998, 25, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (118, NULL, 'EC5 T825 J UNV 2                                                                                                                ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.84999999999999998, 25, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (119, NULL, 'EC5 T832 J UNV 1                                                                                                                ', '120-277                                                                                                                         ', 'T8                                                              ', 1, 0.84999999999999998, 32, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (120, NULL, 'EC5 T832 J UNV 2                                                                                                                ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.84999999999999998, 32, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (121, NULL, 'EC5 T832 G UNV 2L                                                                                                               ', '120-277                                                                                                                         ', 'T8                                                              ', 2, 0.84999999999999998, 32, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (122, NULL, 'EC5 T832 G UNV 3L                                                                                                               ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 0.84999999999999998, 32, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (123, NULL, 'EC5 T832 G UNV 317L                                                                                                             ', '120-277                                                                                                                         ', 'T8                                                              ', 3, 1.1699999999999999, 32, 'Lutron                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (124, NULL, 'Lunera 2200                                                                                                                     ', '100-277                                                                                                                         ', 'LED                                                             ', 1, 1, 59, 'Lunera                                                                                                                          ');
INSERT INTO ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (125, NULL, 'CREE LR24                                                                                                                       ', '100-277                                                                                                                         ', 'LED                                                             ', 1, 1, 52, 'CREE                                                                                                                            ');
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

INSERT INTO ballasts (id, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (158, 'CREE CR22', '120-277', 'LED', 1, 1.00, 35, 'CREE');
INSERT INTO ballasts (id, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, wattage, manufacturer) VALUES (159, 'CREE CR24', '120-277', 'LED', 1, 1.00, 44, 'CREE');

-- Metadata End --



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
    
ALTER TABLE switch ADD CONSTRAINT fk_switch_gems_groups_id FOREIGN KEY (gems_groups_id) 
	REFERENCES gems_groups (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;

    
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

ALTER TABLE scene ADD CONSTRAINT fk_switch_id FOREIGN KEY (switch_id) 
	REFERENCES switch (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;
    
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
    
ALTER TABLE lightlevels ADD CONSTRAINT fk_switch_id FOREIGN KEY (switch_id) 
	REFERENCES switch (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE lightlevels ADD CONSTRAINT fk_scene_id FOREIGN KEY (scene_id) 
	REFERENCES scene (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE lightlevels ADD CONSTRAINT fk_fixture_id FOREIGN KEY (f_id) 
	REFERENCES fixture (id) MATCH SIMPLE 
	ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE scene_seq OWNER TO postgres;

CREATE TABLE pricing
(
  id bigint NOT NULL,
  price_level character varying(255),
  "interval" character varying(255),
  price double precision,
  day_type character varying(255),
  from_time timestamp without time zone,
  to_time timestamp without time zone,
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
  enabled character varying(100) DEFAULT 'No'::character varying,
  start_time timestamp without time zone,
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

-- DRTarget metadata start --

INSERT INTO dr_target (id, price_level, pricing, duration, target_reduction, enabled) VALUES (1, 'Off Peak', 0.085, 60, 0, 'No');
INSERT INTO dr_target (id, price_level, pricing, duration, target_reduction, enabled) VALUES (2, 'Normal', 0.105, 60, 0, 'No');
INSERT INTO dr_target (id, price_level, pricing, duration, target_reduction, enabled) VALUES (3, 'Peak', 0.150, 60, 10, 'No');
INSERT INTO dr_target (id, price_level, pricing, duration, target_reduction, enabled) VALUES (4, 'Moderate', 0.24, 60, 30, 'No');
INSERT INTO dr_target (id, price_level, pricing, duration, target_reduction, enabled) VALUES (5, 'High', 0.36, 60, 50, 'No');
INSERT INTO dr_target (id, price_level, pricing, duration, target_reduction, enabled) VALUES (6, 'Critical', 0.48, 60, 70, 'No');

-- End DRTarget metadata --

-- Pricing metadata start --

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

-- End Pricing metadata --

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

CREATE TABLE ballast_volt_power (
  id bigint NOT NULL,
  ballast_id bigint DEFAULT 0,
  volt_power_map_id bigint,
  volt double precision,
  power double precision,
  UNIQUE (volt_power_map_id, volt),
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

--
-- Insert data for default volt to power map id = 1
--
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 0, 24.60);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 0.5, 24.60);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 1, 24.95);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 1.5, 26.67);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 2, 30.27);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 2.5, 35.67);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 3, 42.36);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 3.5, 48.74);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 4, 55.17);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 4.5, 61.69);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 5, 67.73);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 5.5, 72.93);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 6, 77.66);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 6.5, 81.70);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 7, 86.74);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 7.5, 90.47);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 8, 95.16);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 8.5, 97.08);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 9, 100.03);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 9.5, 100.25);
INSERT INTO ballast_volt_power (id, volt_power_map_id, volt, power) values (nextval('ballast_volt_power_seq'), 1, 10, 102.80);
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

CREATE TABLE system_configuration (
  id bigint NOT NULL,
  name character varying,
  value character varying,
  CONSTRAINT system_configuration_pk PRIMARY KEY (id),  
  CONSTRAINT unique_system_configuration_name UNIQUE (name)
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

CREATE INDEX system_configuration_name_index ON system_configuration USING btree (name);

--
-- Insert default system configuration data 
--
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'default.metadata.areas', 'Default,Breakroom,Conference Room,Open Corridor,Closed Corridor,Egress,Lobby,Warehouse,Open Office,Private Office,Restroom,Lab,Custom1,Custom2,Standalone,Highbay');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'default.metadata.weekday', 'Monday,Tuesday,Wednesday,Thursday,Friday');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'default.metadata.weekend', 'Saturday,Sunday');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'default_utc_time_cmd_frequency', '300000');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'default_utc_time_cmd_offset', '240000');

--
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

-- End profile defaults.
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.interPacketDelay', '180');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.interBucketDelay', '4');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.plcPacketSize', '192');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.zigbeePacketSize', '64');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'commandRetryDelay', '1000');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'commandNoOfRetries', '2');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'perf.pmStatsMode', '1');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'event.outageVolts', '70');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'event.outageAmbLight', '100');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.no_multicast_targets', '20');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.no_multicast_targets', '10');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.retry_interval', '10');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.max_no_install_sensors', '100');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'default.radio_rate', '2');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.max_time', '180');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.su_app_pattern', 'su_app');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.su_firm_pattern', 'su_firm');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.gw_app_pattern', 'gw_app');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.gw_firm_pattern', 'gw_firm');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.su_20_pattern', 'su.bin');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.gw_20_pattern', 'gw.tar');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.cu_20_pattern', 'cu.bin');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.sw_20_pattern', 'sw.bin');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.multicast_inter_pkt_delay', '300');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.unicast_inter_pkt_delay', '35');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.ack_dbupdate_threads', '1');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.response_listener_threads', '1');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.pmstats_processing_threads', '1');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.validationTargetAmbLight', '9990');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.5min_table', '90');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.hourly_table', '365');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.daily_table', '3650');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'fixture.default_voltage', '277');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.su_pyc_pattern', 'su_pyc');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'upgrade.gw_pyc_pattern', 'gw_pyc');
--Bacnet configuration properties
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.vendor_id', '516');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.server_port', '47808');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.network_id', '9999');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.max_APDU_length', '1476');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.APDU_timeout', '10000');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'bacnet.device_base_instance', '400000');
--Fixture sorting path (0) Top to bottom, (1) Botton to Top, (2) Left to Right, (3) Right to Left
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'fixture.sorting.path', '0');
-- Prune ems audit table default to 1 day of history
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.emsaudit_table', '7');
-- Scaling factor on energy consumption for various volts
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.apply.scaling.factor', 'true');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.scaling.for.110v', '0.0511');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.adj.for.110v', '6.9192');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.scaling.for.277v', '1.4522');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.adj.for.277v', '12.754');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.scaling.for.240v', '4.5');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ec.adj.for.240v', '0');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'commissioning.inactivity_timeout', '900');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'perf.base_power_correction_percentage', '5');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'event.outage_detect_percentage', '10');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'event.fixture_outage_detect_watts', '7');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.validationTargetRelAmbLight', '200');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.validationMaxEnergyPercentReading', '40');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.no_multicast_retransmits', '2');
-- DR related
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'dr.service_enabled', 'false');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'dr.repeat_interval', '60000');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.default_fail_retries', '1');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.no_test_runs', '20');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.test_file', '429_su.bin');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'ssl.enabled', 'true');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'imageUpgrade.interPacketDelay_2', '20');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.multicast_inter_pkt_delay_2', '75');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'stats.temp_offset_1', '18');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'default_su_hop_count', '3');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'discovery.validationTargetRelAmbLight_2', '500');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'pmstats_process_batch_time', '2000');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.em_stats_table', '90');

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

--Added by Sreedhar 06/07
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

--Added by Yogesh 06/16
--audit log table 
CREATE TABLE ems_audit
(
	id bigint NOT NULL,
	txn_id bigint DEFAULT 0,
	device_id bigint,
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
-- adding a configuration that will decide whether bacnet configuration should be shown or not in the menu
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'menu.bacnet.show', 'false');

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

--Added by Yogesh 10/31
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

ALTER TABLE area ADD tenant_id bigint;
ALTER TABLE area ADD CONSTRAINT fk_area_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);

select setval('dr_target_seq', (select max(id) from dr_target) + 1);
select setval('pricing_seq', (select max(id) from pricing) + 1);


--- gems group fixtures
CREATE TABLE gems_group_fixture
(
  id bigint NOT NULL,
  group_id bigint NOT NULL,
  fixture_id bigint,
  need_sync bigint,
  user_action bigint,
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
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'cmd.pmstats_process_batch_size', '20');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'stats.temp_offset_2', '8');

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

CREATE TABLE ems_user_audit (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    username character varying NOT null,
    action_type character varying NOT NULL,   
	log_time timestamp without time zone not null,
    description character varying,
    ip_address character varying NOT null DEFAULT ''
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
CREATE INDEX ems_user_audit_ip_index ON ems_user_audit USING btree (ip_address);

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
  non_anonymous_password character varying(20),
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

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'gems.version.build', '0');

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

--Added by Lalit 3o-May-2012
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'sweeptimer.enable', 'false');

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

--- Added by Shilpa 27-June-2012
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'dhcp.enable', 'true');

---Added by Sampath 02-July-2012

INSERT INTO system_configuration (id, name, value) 
values ((select coalesce(max(id),0)+1 from system_configuration),'db_pruning.ems_user_audit_table', '90');

INSERT INTO system_configuration (id, name, value) 
values ((select coalesce(max(id),0)+1 from system_configuration),'db_pruning.ems_user_audit_history_table', '1825');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'rest.api.key', '');

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
ALTER TABLE em_motion_bits ADD COLUMN  motion_bits_frequency integer;


-- Added by Sharad 08/08 To extent profile model to support enhanced features  INSTALL SQL CHANGES
CREATE TABLE profile_template
(
  id bigint NOT NULL,
  name character varying(255),
  display_template boolean DEFAULT true,
  CONSTRAINT profile_template_pkey PRIMARY KEY (id)
);

ALTER TABLE profile_template OWNER TO postgres;

CREATE SEQUENCE profile_template_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE profile_template_seq OWNER TO postgres;
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

ALTER TABLE groups ADD COLUMN profile_no smallint;
ALTER TABLE groups ADD COLUMN derived_from_group bigint;
ALTER TABLE groups ADD COLUMN tenant_id bigint;
ALTER TABLE groups ADD CONSTRAINT fk_gems_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenants (id);
ALTER TABLE groups ADD COLUMN template_id bigint;
ALTER TABLE groups ADD CONSTRAINT fk_gems_template_id FOREIGN KEY (template_id) REFERENCES profile_template (id);
ALTER TABLE groups ADD COLUMN display_profile boolean DEFAULT true;
ALTER TABLE groups ADD COLUMN default_profile boolean DEFAULT true;
ALTER TABLE fixture DROP CONSTRAINT fkcdb9fa09393e967c;
ALTER TABLE fixture DROP COLUMN profile_handler_id;

--
-- Profile Enhancement 
--
-- Yogesh 27/08

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
		SELECT id INTO group_no FROM groups g WHERE g.profile_no = $1::smallint and tenant_id is null;
	ELSE
		SELECT id INTO group_no FROM groups g WHERE g.profile_no = $1::smallint and tenant_id = $2;
    END IF;
	return group_no;
END;
$$
LANGUAGE plpgsql;


INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'motionbits.enable', 'false');

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'dashboard_sso', 'false');

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
    group_no integer,
    CONSTRAINT motion_bits_scheduler_group_id_fkey FOREIGN KEY (group_id)
      REFERENCES gems_groups (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE motion_bits_scheduler OWNER TO postgres;
ALTER TABLE motion_bits_scheduler ADD COLUMN  display_name character varying;
ALTER TABLE ONLY motion_bits_scheduler
    ADD CONSTRAINT motion_bits_scheduler_pkey PRIMARY KEY (id);
    

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

--- Added on 22/10/12, YGC
INSERT INTO wds_model_type (id, name, no_of_buttons) values (1, 'WDS4B1S', 4);

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
    scene_toggle_order integer,
    button_manip_action bigint,
    CONSTRAINT button_manipulation_pkey PRIMARY KEY (id),
    CONSTRAINT fk_button_map_id FOREIGN KEY (button_map_id)
      REFERENCES button_map (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE public.button_manipulation OWNER TO postgres;


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
    wds_no integer,
    association_state integer default 0,
    version character varying(50),
    upgrade_status character varying(20),
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

CREATE SEQUENCE wds_no_seq INCREMENT BY 1 MAXVALUE 999999 NO MINVALUE CACHE 1;
ALTER TABLE public.wds_no_seq OWNER TO postgres;
