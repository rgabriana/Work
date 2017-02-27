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
UPDATE USERS set role_id=3 where role_id >=4;
DELETE FROM ROLEs where id >= 4;

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


--- gems_group_type
CREATE TABLE gems_group_type
(
  id bigint NOT NULL,
  group_type character varying NOT NULL,
  group_no integer,
  CONSTRAINT gems_group_type_pk PRIMARY KEY (id)
);
ALTER TABLE gems_group_type OWNER TO postgres;

CREATE SEQUENCE gems_group_type_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


--- gems_groups
CREATE TABLE gems_groups
(
  id bigint NOT NULL,
  group_name character varying NOT NULL,
  group_type bigint,
  description character varying,
  company_id bigint,
  CONSTRAINT gems_groups_pkey PRIMARY KEY (id),
  CONSTRAINT fk_gems_groups_company FOREIGN KEY (company_id)
      REFERENCES company (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT gems_groups_group_type_fkey FOREIGN KEY (group_type)
      REFERENCES gems_group_type (id) MATCH SIMPLE
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


--- Add defaults
INSERT INTO gems_group_type (id, group_type, group_no) VALUES (1, 'MotionGroup', 10001);

--- Ldap based authentication config
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'auth.auth_type', 'DATABASE');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'auth.ldap_url', null);
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'auth.ldap_auth_type', 'simple');
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'auth.ldap_security_principal', null);
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'auth.ldap_allow_non_ems_users', 'false');
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
	day_type := wd.type from fixture f, profile_configuration pc, profile_handler pr, weekday wd 
	             where wd.profile_configuration_id = pc.id and 
	                   pc.id = pr.profile_configuration_id and 	                   
	                   pr.id = f.profile_handler_id and
	                   wd.short_order = $2 and f.id = $1;	             
	 
	 quadrant_type := CASE when (currentMinutes <= getDayQuadrantForGivenTime(morning_time)) then 'morning'
	                             when (currentMinutes > getDayQuadrantForGivenTime(morning_time)  and currentMinutes <= getDayQuadrantForGivenTime(day_time)) then 'day'
	                             when (currentMinutes > getDayQuadrantForGivenTime(day_time)  and currentMinutes <= getDayQuadrantForGivenTime(evening_time)) then 'evenning'
	                             else 'night' 
	                     END
	                             from  fixture f, profile_configuration pc, profile_handler pr where pc.id = pr.profile_configuration_id and 
	                             f.profile_handler_id = pr.id  and f.id = $1;    
	 
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

--- Set the correct seq for the types table (Yogesh)
SELECT setval('gems_group_type_seq', max(id)) from gems_group_type;