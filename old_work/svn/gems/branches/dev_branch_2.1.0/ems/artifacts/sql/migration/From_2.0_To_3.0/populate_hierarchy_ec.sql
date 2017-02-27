CREATE OR REPLACE FUNCTION prepopulate_hierarchy_ec_hourly(todate timestamp with time zone) RETURNS void
    AS $$
DECLARE 
	hier_rec hier_hour_record;
	company_rec company_hour_record;
BEGIN

--floor aggregation
FOR hier_rec IN (
	SELECT floor_id, SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(ec.avg_temperature) AS avg_temp, SUM(base_power_used) AS base_power, sum(base_cost) AS base_cost, SUM(saved_power_used) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_saving) AS occ_saving, SUM(ambient_saving) AS amb_saving, SUM(tuneup_saving) AS tune_saving, SUM(manual_saving) AS manual_saving, AVG(price) AS price, min(price) AS min_price, max(price) AS max_price, sum(power_used) AS avg_load, count(*) AS no_of_rec
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
	SELECT building_id, SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(ec.avg_temperature) AS avg_temp, SUM(base_power_used) AS base_power, sum(base_cost) AS base_cost, SUM(saved_power_used) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_saving) AS occ_saving, SUM(ambient_saving) AS amb_saving, SUM(tuneup_saving) AS tune_saving, SUM(manual_saving) AS manual_saving, AVG(price) AS price, min(price) AS min_price, max(price) AS max_price, sum(power_used) AS avg_load, count(*) AS no_of_rec
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
	SELECT campus_id, SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(ec.avg_temperature) AS avg_temp, SUM(base_power_used) AS base_power, sum(base_cost) AS base_cost, SUM(saved_power_used) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_saving) AS occ_saving, SUM(ambient_saving) AS amb_saving, SUM(tuneup_saving) AS tune_saving, SUM(manual_saving) AS manual_saving, AVG(price) AS price, min(price) AS min_price, max(price) AS max_price, sum(power_used) AS avg_load, count(*) AS no_of_rec
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
	SELECT SUM(power_used) AS agg_power, sum(cost) AS agg_cost, min(min_temperature) AS min_temp, max(max_temperature) AS max_temp, avg(ec.avg_temperature) AS avg_temp, SUM(base_power_used) AS base_power, sum(base_cost) AS base_cost, SUM(saved_power_used) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_saving) AS occ_saving, SUM(ambient_saving) AS amb_saving, SUM(tuneup_saving) AS tune_saving, SUM(manual_saving) AS manual_saving, AVG(price) AS price, min(price) AS min_price, max(price) AS max_price, sum(power_used) AS avg_load, count(*) AS no_of_rec
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

CREATE OR REPLACE FUNCTION prepopulate_hierarchy_ec_daily(todate timestamp with time zone) RETURNS void
    AS $$
DECLARE 
	hier_rec hier_daily_record;
	company_rec company_daily_record;
	price_calc numeric;
BEGIN

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

END;
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION prepopulate_hierarchy_ec(fromdate timestamp with time zone, todate timestamp with time zone) RETURNS void
AS $$
DECLARE
	curr_time timestamp;	
BEGIN

	UPDATE energy_consumption_hourly set avg_load = power_used WHERE capture_at > now() - interval '1 day';
	curr_time = fromdate;
	WHILE curr_time < todate LOOP
		PERFORM prepopulate_hierarchy_ec_hourly(curr_time);	
	   	curr_time = curr_time + interval '1 hour';		
	END LOOP;	

	curr_time = fromdate;
	WHILE curr_time < todate LOOP
		PERFORM prepopulate_hierarchy_ec_daily(curr_time);	
	    	curr_time = curr_time + interval '1 day';		
	END LOOP;	

END;
$$
LANGUAGE plpgsql;

