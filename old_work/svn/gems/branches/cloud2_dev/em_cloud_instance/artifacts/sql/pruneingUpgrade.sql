CREATE PROCEDURAL LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION pruneenergy() RETURNS void
    AS $$
DECLARE 
	no_days numeric;
	no_days_text text;
	no_days_time timestamp;	
	tm timestamp = now();

BEGIN
	no_days=7 ;

	no_days_text = no_days || ' day';
	no_days_time = tm - no_days_text::interval;
	
	DELETE FROM energy_consumption WHERE capture_at < no_days_time;
END;
$$
LANGUAGE plpgsql;

ALTER FUNCTION public.pruneenergy() OWNER TO postgres;

CREATE OR REPLACE FUNCTION prunehourlyenergy() RETURNS void
    AS $$
DECLARE 
	no_days numeric;
	no_days_text text;
	no_days_time timestamp;	
	tm timestamp = now();

BEGIN
	no_days=7 ;

	no_days_text = no_days || ' day';
	no_days_time = tm - no_days_text::interval;
	
	DELETE FROM energy_consumption_hourly WHERE capture_at < no_days_time;
END;
$$
LANGUAGE plpgsql;

ALTER FUNCTION public.prunehourlyenergy() OWNER TO postgres;

CREATE OR REPLACE FUNCTION prunedailyenergy() RETURNS void
    AS $$
DECLARE 
	no_days numeric;
	no_days_text text;
	no_days_time timestamp;	
	tm timestamp = now();

BEGIN
	no_days=7 ;

	no_days_text = no_days || ' day';
	no_days_time = tm - no_days_text::interval;
	
	DELETE FROM energy_consumption_daily WHERE capture_at < no_days_time;
END;
$$
LANGUAGE plpgsql;

ALTER FUNCTION public.prunedailyenergy() OWNER TO postgres;