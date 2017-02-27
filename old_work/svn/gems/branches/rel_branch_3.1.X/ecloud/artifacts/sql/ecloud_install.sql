DROP PROCEDURAL LANGUAGE IF EXISTS plpgsql CASCADE ;
CREATE PROCEDURAL LANGUAGE plpgsql;
--
-- Name:customer; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--
    
CREATE SEQUENCE customer_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.customer_seq OWNER TO postgres;


CREATE TABLE customer (
    id bigint NOT NULL,
    name character varying NOT NULL,
    address character varying NOT NULL,
    email character varying,
    phone character varying,
    sppa_price double precision default 0.08,
    prev_amt_due double precision DEFAULT 0.0,
    last_bill_gen_date timestamp without time zone
);

ALTER TABLE public.customer OWNER TO postgres;

ALTER TABLE ONLY customer
    ADD CONSTRAINT customer_pk PRIMARY KEY (id);



--
-- Name: user_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE users_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.users_seq OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE users (
    id bigint NOT NULL,
    email character varying NOT NULL,
    password character varying NOT NULL,
    salt character varying NOT NULL,
    first_name character varying,
    last_name character varying,
    created_on date,
    role_type character varying,
    status character varying,
    customer_id bigint 
);


ALTER TABLE public.users OWNER TO postgres;


ALTER TABLE ONLY users
    ADD CONSTRAINT users_pk PRIMARY KEY (id); 
    
ALTER TABLE ONLY users
    ADD CONSTRAINT users_customer_fk FOREIGN KEY (customer_id) REFERENCES customer(id);


--
-- Name:replica_server; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--
    
CREATE SEQUENCE replica_server_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.replica_server_seq OWNER TO postgres;


CREATE TABLE replica_server (
    id bigint NOT NULL,
    ip character varying NOT NULL,
    name character varying,
    uid character varying,
    internal_ip character varying,
    mac_id character varying
);


ALTER TABLE public.replica_server OWNER TO postgres;

ALTER TABLE ONLY replica_server
    ADD CONSTRAINT replica_server_pk PRIMARY KEY (id);
    
--
-- Name:em_instance; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--
CREATE SEQUENCE em_instance_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.em_instance_seq OWNER TO postgres;

CREATE TABLE em_instance (
    id bigint NOT NULL,
    customer_id bigint,
    replica_server_id bigint,
    name character varying,
    mac_id character varying NOT NULL,
    version character varying,
    last_connectivity_at timestamp without time zone,
    active boolean default false,
    time_zone character varying,
    contact_name character varying,
    contact_email character varying,
    address character varying,
    contact_phone character varying,
    sppa_enabled boolean default false,
    latest_em_state_id bigint,
    sppa_price double precision default 0.08,
    cert_start_date timestamp without time zone,
    cert_end_date timestamp without time zone,
    tax_rate double precision default 8.25,
    block_purchase_energy numeric(19,2),
    block_energy_consumed numeric(19,2),
    sppa_bill_enabled boolean default false,
    total_billed_no_of_days bigint,
    geo_location character varying,
    latest_em_health_monitor_id bigint
);


ALTER TABLE public.em_instance OWNER TO postgres;

ALTER TABLE ONLY em_instance
    ADD CONSTRAINT em_instance_pk PRIMARY KEY (id);

ALTER TABLE ONLY em_instance
    ADD CONSTRAINT em_instance_customer_fk FOREIGN KEY (customer_id) REFERENCES customer(id);
ALTER TABLE ONLY em_instance
    ADD CONSTRAINT em_instance_replica_server_fk FOREIGN KEY (replica_server_id) REFERENCES replica_server(id);
ALTER TABLE ONLY em_instance
    ADD CONSTRAINT em_instance_health_monitor_fk FOREIGN KEY (latest_em_health_monitor_id) REFERENCES em_health_monitor(id);
    
CREATE TABLE em_stats
( 
  id bigint NOT NULL,
  em_instance_id bigint,
  capture_at timestamp without time zone,
  active_thread_count integer,
  gc_count bigint,
  gc_time bigint,
  heap_used numeric(19,2),
  non_heap_used numeric(19,2),
  sys_load numeric(19,2),
  cpu_percentage numeric(19,2),
  is_em_accessible BOOLEAN DEFAULT '0' NOT NULL,
  CONSTRAINT em_stats_pkey PRIMARY KEY(id)
);

ALTER TABLE ONLY em_stats
    ADD CONSTRAINT em_instance_fk FOREIGN KEY (em_instance_id) REFERENCES em_instance(id);
    
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

INSERT INTO users (id, email, "password", salt, first_name, last_name, created_on, role_type,status) VALUES (nextval('users_seq'), 'admin', '7c0345648dfb5b05cf5bedcf2c14b37f63f79421','randomsalt', 'Administrator', '', '2012-01-19', 'Admin','A');

CREATE TABLE upgrades
(
  id bigint NOT NULL,
  "location" character varying(255) NOT NULL,
  "name" character varying(255) NOT NULL,
  "type" character varying(255) NOT NULL,
  CONSTRAINT upgrades_pkey PRIMARY KEY (id)
);


CREATE TABLE em_tasks
(
  id bigint NOT NULL,
  em_instance_id bigint not null,
  task_code character varying(30) not null,
  task_status character varying(30) not null,
  progress_status character varying(50),
  parameters character varying(3000),
  priority character varying(30),
  start_time timestamp without time zone,
  offset_time timestamp without time zone,
  number_of_attempts integer,
  CONSTRAINT em_tasks_pkey PRIMARY KEY (id)
);

ALTER TABLE em_tasks OWNER TO postgres;

ALTER TABLE ONLY em_tasks
    ADD CONSTRAINT em_tasks_em_instance_fk FOREIGN KEY (em_instance_id) REFERENCES em_instance(id);
    
CREATE SEQUENCE em_tasks_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE em_tasks_seq OWNER TO postgres;


CREATE TABLE em_state
(
  id bigint NOT NULL,
  em_instance_id bigint not null,
  em_status character varying(30) default 'CALL_HOME',
  database_status character varying(255) default 'NOT_MIGRATED',
   failed_attempts integer default 0,
  set_time timestamp without time zone,
  log character varying(3000) default 'SPPA not enabled',
  CONSTRAINT em_state_pkey PRIMARY KEY (id)
);

ALTER TABLE em_state OWNER TO postgres;

ALTER TABLE ONLY em_state
    ADD CONSTRAINT em_state_em_instance_fk FOREIGN KEY (em_instance_id) REFERENCES em_instance(id);
    
CREATE SEQUENCE em_state_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE em_state_seq OWNER TO postgres;

CREATE OR REPLACE FUNCTION em_state_change() RETURNS "trigger" AS $$ 
	BEGIN
		update em_instance set latest_em_state_id = new.id where id = new.em_instance_id ;
		RETURN new ;
	END
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_em_state_change AFTER INSERT OR UPDATE ON em_state
  FOR EACH ROW EXECUTE PROCEDURE em_state_change();

CREATE TABLE sppa_bill (
  id bigint NOT NULL,
  bill_start_date timestamp without time zone,
  bill_end_date timestamp without time zone,
  bill_creation_time timestamp without time zone,
  no_of_days integer,
  em_instance_id bigint,
  baseline_energy numeric(19,2),
  consumed_energy numeric(19,2),
  base_cost double precision,
  sppa_cost double precision,
  saved_cost double precision,
  tax double precision,
  block_energy_remaining numeric(19,2),
  block_term_remaining bigint,
  customer_bill_id bigint,
  CONSTRAINT sppa_bill_pkey PRIMARY KEY (id)
);

ALTER TABLE public.sppa_bill OWNER TO postgres;
    
CREATE SEQUENCE sppa_bill_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.sppa_bill_seq OWNER TO postgres;

CREATE TABLE customer_sppa_bill (
  id bigint NOT NULL,
  customer_id bigint,
  bill_start_date timestamp without time zone,
  bill_end_date timestamp without time zone,
  bill_creation_time timestamp without time zone,
  no_of_days integer,
  baseline_energy numeric(19,2),
  consumed_energy numeric(19,2),
  base_cost double precision,
  sppa_cost double precision,
  saved_cost double precision,
  bill_status integer,
  current_charges double precision,
  total_amt_due double precision,
  amount_received double precision,
  prevamtdue double precision,

  CONSTRAINT customer_sppa_bill_pkey PRIMARY KEY (id)
);

ALTER TABLE public.customer_sppa_bill OWNER TO postgres;

CREATE SEQUENCE customer_sppa_bill_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.customer_sppa_bill_seq OWNER TO postgres;

ALTER TABLE sppa_bill ADD CONSTRAINT fk_customer_sppa_bill FOREIGN KEY (customer_bill_id) REFERENCES customer_sppa_bill (id);

--
-- ADDING QUARTZ TABLES
--
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

ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_job_name_fkey FOREIGN KEY (job_name, job_group) REFERENCES qrtz_job_details(job_name, job_group);
    
--
-- ADDING QUARTZ TABLES - ENDS
--
    
    
ALTER TABLE em_instance ADD COLUMN open_tunnel_to_cloud boolean default false;
ALTER TABLE em_instance ADD COLUMN browse_enabled_from_cloud boolean default false ;
ALTER TABLE em_instance ADD COLUMN tunnel_port integer default 0;
ALTER TABLE em_instance ADD COLUMN browsable_link character varying ;
ALTER TABLE em_instance ADD COLUMN open_ssh_tunnel_to_cloud boolean default false;
ALTER TABLE em_instance ADD COLUMN ssh_tunnel_port integer default 0;



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


INSERT INTO system_configuration (id, name, value) values (nextval('system_configuration_seq'), 'browsing.supported.version', '3.1.0');

CREATE TABLE user_customers
(
  id bigint NOT NULL,
  user_id bigint NOT NULL,
  customer_id bigint NOT NULL,
  CONSTRAINT user_customers_pkey PRIMARY KEY (id),
  CONSTRAINT user_customers_customer_id_fkey FOREIGN KEY (customer_id)
      REFERENCES customer (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT user_customers_user_id_fkey FOREIGN KEY (user_id)
      REFERENCES users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE public.user_customers OWNER TO postgres;

CREATE SEQUENCE user_customers_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 38
  CACHE 1;
ALTER TABLE public.user_customers_seq OWNER TO postgres;



-- Name: pruneemstats function to prune em_stats table
INSERT INTO system_configuration (id, name, value) values (nextval('system_configuration_seq'), 'db_pruning.emstats_table', '90');

CREATE OR REPLACE FUNCTION pruneemstats() RETURNS void
    AS $$
DECLARE 
	no_days numeric;
	no_days_text text;
	no_days_time timestamp;	
	tm timestamp = now();

BEGIN
	SELECT value INTO no_days
	FROM system_configuration
	WHERE name = 'db_pruning.emstats_table';

	no_days_text = no_days || ' day';
	no_days_time = tm - no_days_text::interval;
	
	DELETE FROM em_stats WHERE capture_at < no_days_time;
END;
$$
LANGUAGE plpgsql;

ALTER FUNCTION public.pruneemstats() OWNER TO postgres;

CREATE TABLE bill_payments (
  id bigint NOT NULL,
  customer_id bigint,
  payment_date timestamp without time zone,  
  payment_amount double precision,  
  CONSTRAINT bill_payments_pkey PRIMARY KEY (id)
);

ALTER TABLE public.bill_payments OWNER TO postgres;

CREATE SEQUENCE bill_payments_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.bill_payments_seq OWNER TO postgres;

CREATE TABLE site (
  id bigint NOT NULL,
  customer_id bigint,
  geo_location character varying,
  name character varying,
  sppa_price double precision default 0.08,
  tax_rate double precision default 8.25,
  block_purchase_energy numeric(19,2),
  block_energy_consumed numeric(19,2),
  total_billed_no_of_days bigint,
  po_number character varying,
  bill_start_date timestamp without time zone,
  CONSTRAINT site_pkey PRIMARY KEY (id)
);

ALTER TABLE public.site OWNER TO postgres;

CREATE SEQUENCE site_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.site_seq OWNER TO postgres;

CREATE TABLE em_site (
  id bigint NOT NULL,
  em_id bigint,
  site_id bigint,
  CONSTRAINT em_site_pkey PRIMARY KEY (id)
);

ALTER TABLE public.em_site OWNER TO postgres;

CREATE SEQUENCE em_site_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.em_site_seq OWNER TO postgres;

CREATE SEQUENCE EM_HEALTH_MONITOR_SEQ
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.EM_HEALTH_MONITOR_SEQ OWNER TO postgres;

CREATE TABLE EM_HEALTH_MONITOR (
    id bigint NOT NULL,
    em_instance_id bigint NOT NULL,
    capture_at timestamp without time zone,
    gateways_critical int,
    gateways_under_observation int,
    gateways_total int,
    sensors_critial int,
    sensors_under_observation int,
    sensors_total int
);

ALTER TABLE public.EM_HEALTH_MONITOR OWNER TO postgres;

ALTER TABLE ONLY EM_HEALTH_MONITOR
    ADD CONSTRAINT EM_HEALTH_MONITOR_PK PRIMARY KEY (id);
ALTER TABLE ONLY EM_HEALTH_MONITOR
    ADD CONSTRAINT EM_HM_EM_INSTANCe_FK FOREIGN KEY (em_instance_id) REFERENCES em_instance(id);

CREATE OR REPLACE FUNCTION em_health_monitor_change() RETURNS "trigger" AS $$ 
	BEGIN
		update em_instance set latest_em_health_monitor_id = new.id where id = new.em_instance_id ;
		RETURN new ;
	END
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_em_health_monitor_change AFTER INSERT OR UPDATE ON EM_HEALTH_MONITOR
  FOR EACH ROW EXECUTE PROCEDURE em_health_monitor_change();
  
  INSERT INTO system_configuration (id, name, value) values (nextval('system_configuration_seq'), 'browsing.show', 'false');

--sree 02/10/2014
ALTER TABLE em_instance ADD COLUMN em_commissioned_date timestamp without time zone;
ALTER TABLE em_instance ADD COLUMN no_of_emergency_fixtures int;
ALTER TABLE em_instance ADD COLUMN emergency_fixtures_guideline_load numeric(19,2);
ALTER TABLE em_instance ADD COLUMN emergency_fixtures_load numeric(19,2);

ALTER TABLE sppa_bill ADD COLUMN emergency_baseline_energy numeric(19,2);
ALTER TABLE sppa_bill ADD COLUMN emergency_consumed_energy numeric(19,2);

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

CREATE TABLE floor_energy_consumption_hourly
(  
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
  avg_temp double precision,
  min_temp double precision,
  max_temp double precision,
  avg_amb double precision,
  min_amb double precision,
  max_amb double precision,
  motion_events bigint,
  no_of_records integer
);

ALTER TABLE floor_energy_consumption_hourly OWNER TO postgres;

CREATE INDEX floor_energy_consumption_hourly_capture_at_index ON floor_energy_consumption_hourly USING btree (capture_at);

CREATE INDEX floor_energy_consumption_hourly_level_id_index ON floor_energy_consumption_hourly USING btree (level_id);

CREATE TABLE floor_energy_consumption_daily
(  
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
  avg_temp double precision,
  min_temp double precision,
  max_temp double precision,
  avg_amb double precision,
  min_amb double precision,
  max_amb double precision,
  motion_events bigint,
  no_of_records integer
);

ALTER TABLE floor_energy_consumption_daily OWNER TO postgres;

CREATE INDEX floor_energy_consumption_daily_capture_at_index ON floor_energy_consumption_daily USING btree (capture_at);

CREATE INDEX floor_energy_consumption_daily_level_id_index ON floor_energy_consumption_daily USING btree (level_id);

CREATE TABLE bld_energy_consumption_hourly
(  
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
  avg_temp double precision,
  min_temp double precision,
  max_temp double precision,
  avg_amb double precision,
  min_amb double precision,
  max_amb double precision,
  motion_events bigint,
  no_of_records integer
);

ALTER TABLE bld_energy_consumption_hourly OWNER TO postgres;

CREATE INDEX bld_energy_consumption_hourly_capture_at_index ON bld_energy_consumption_hourly USING btree (capture_at);

CREATE INDEX bld_energy_consumption_hourly_level_id_index ON bld_energy_consumption_hourly USING btree (level_id);

CREATE TABLE bld_energy_consumption_daily
(  
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
  avg_temp double precision,
  min_temp double precision,
  max_temp double precision,
  avg_amb double precision,
  min_amb double precision,
  max_amb double precision,
  motion_events bigint,
  no_of_records integer
);

ALTER TABLE bld_energy_consumption_daily OWNER TO postgres;

CREATE INDEX bld_energy_consumption_daily_capture_at_index ON bld_energy_consumption_daily USING btree (capture_at);

CREATE INDEX bld_energy_consumption_daily_level_id_index ON bld_energy_consumption_daily USING btree (level_id);

CREATE TABLE campus_energy_consumption_hourly
(  
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
  avg_temp double precision,
  min_temp double precision,
  max_temp double precision,
  avg_amb double precision,
  min_amb double precision,
  max_amb double precision,
  motion_events bigint,
  no_of_records integer
);

ALTER TABLE campus_energy_consumption_hourly OWNER TO postgres;

CREATE INDEX campus_energy_consumption_hourly_capture_at_index ON campus_energy_consumption_hourly USING btree (capture_at);

CREATE INDEX campus_energy_consumption_hourly_level_id_index ON campus_energy_consumption_hourly USING btree (level_id);

CREATE TABLE campus_energy_consumption_daily
( 
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
  avg_temp double precision,
  min_temp double precision,
  max_temp double precision,
  avg_amb double precision,
  min_amb double precision,
  max_amb double precision,
  motion_events bigint,
  no_of_records integer
);

ALTER TABLE campus_energy_consumption_daily OWNER TO postgres;

CREATE INDEX campus_energy_consumption_daily_capture_at_index ON campus_energy_consumption_daily USING btree (capture_at);

CREATE INDEX campus_energy_consumption_daily_level_id_index ON campus_energy_consumption_daily USING btree (level_id);

CREATE TABLE organization_energy_consumption_hourly
(  
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
  avg_temp double precision,
  min_temp double precision,
  max_temp double precision,
  avg_amb double precision,
  min_amb double precision,
  max_amb double precision,
  motion_events bigint,
  no_of_records integer
);

ALTER TABLE organization_energy_consumption_hourly OWNER TO postgres;

CREATE INDEX organization_energy_consumption_hourly_capture_at_index ON organization_energy_consumption_hourly USING btree (capture_at);

CREATE INDEX organization_energy_consumption_hourly_level_id_index ON organization_energy_consumption_hourly USING btree (level_id);

CREATE TABLE organization_energy_consumption_daily
(  
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
  avg_temp double precision,
  min_temp double precision,
  max_temp double precision,
  avg_amb double precision,
  min_amb double precision,
  max_amb double precision,
  motion_events bigint,
  no_of_records integer
);

ALTER TABLE organization_energy_consumption_daily OWNER TO postgres;

CREATE INDEX organization_energy_consumption_daily_capture_at_index ON organization_energy_consumption_daily USING btree (capture_at);

CREATE INDEX organization_energy_consumption_daily_level_id_index ON organization_energy_consumption_daily USING btree (level_id);

CREATE TABLE floor_energy_consumption_15min
(  
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
  avg_temp double precision,
  min_temp double precision,
  max_temp double precision,
  avg_amb double precision,
  min_amb double precision,
  max_amb double precision,
  motion_events bigint,
  no_of_records integer
);

ALTER TABLE floor_energy_consumption_15min OWNER TO postgres;

CREATE INDEX floor_energy_consumption_15min_capture_at_index ON floor_energy_consumption_15min USING btree (capture_at);

CREATE INDEX floor_energy_consumption_15min_level_id_index ON floor_energy_consumption_15min USING btree (level_id);

CREATE TABLE bld_energy_consumption_15min
(  
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
  avg_temp double precision,
  min_temp double precision,
  max_temp double precision,
  avg_amb double precision,
  min_amb double precision,
  max_amb double precision,
  motion_events bigint,
  no_of_records integer
);

ALTER TABLE bld_energy_consumption_15min OWNER TO postgres;

CREATE INDEX bld_energy_consumption_15min_capture_at_index ON bld_energy_consumption_15min USING btree (capture_at);

CREATE INDEX bld_energy_consumption_15min_level_id_index ON bld_energy_consumption_15min USING btree (level_id);

CREATE TABLE campus_energy_consumption_15min
(  
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
  avg_temp double precision,
  min_temp double precision,
  max_temp double precision,
  avg_amb double precision,
  min_amb double precision,
  max_amb double precision,
  motion_events bigint,
  no_of_records integer
);

ALTER TABLE campus_energy_consumption_15min OWNER TO postgres;

CREATE INDEX campus_energy_consumption_15min_capture_at_index ON campus_energy_consumption_15min USING btree (capture_at);

CREATE INDEX campus_energy_consumption_15min_level_id_index ON campus_energy_consumption_15min USING btree (level_id);

CREATE TABLE organization_energy_consumption_15min
(  
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
  avg_temp double precision,
  min_temp double precision,
  max_temp double precision,
  avg_amb double precision,
  min_amb double precision,
  max_amb double precision,
  motion_events bigint,
  no_of_records integer
);

ALTER TABLE organization_energy_consumption_15min OWNER TO postgres;

CREATE INDEX organization_energy_consumption_15min_capture_at_index ON organization_energy_consumption_15min USING btree (capture_at);

CREATE INDEX organization_energy_consumption_15min_level_id_index ON organization_energy_consumption_15min USING btree (level_id);

-- schema changes related to energy aggregation and sync

ALTER TABLE floor_energy_consumption_15min
  ADD CONSTRAINT floor_energy_consumption_15min_pkey PRIMARY KEY (cust_id, level_id, capture_at);
ALTER TABLE floor_energy_consumption_hourly
  ADD CONSTRAINT floor_energy_consumption_hourly_pkey PRIMARY KEY (cust_id, level_id, capture_at);
ALTER TABLE floor_energy_consumption_daily
  ADD CONSTRAINT floor_energy_consumption_daily_pkey PRIMARY KEY (cust_id, level_id, capture_at);
ALTER TABLE bld_energy_consumption_15min
  ADD CONSTRAINT bld_energy_consumption_15min_pkey PRIMARY KEY (cust_id, level_id, capture_at);
ALTER TABLE bld_energy_consumption_hourly
  ADD CONSTRAINT bld_energy_consumption_hourly_pkey PRIMARY KEY (cust_id, level_id, capture_at);
ALTER TABLE bld_energy_consumption_daily
  ADD CONSTRAINT bld_energy_consumption_daily_pkey PRIMARY KEY (cust_id, level_id, capture_at);
ALTER TABLE campus_energy_consumption_15min
  ADD CONSTRAINT campus_energy_consumption_15min_pkey PRIMARY KEY (cust_id, level_id, capture_at);
ALTER TABLE campus_energy_consumption_hourly
  ADD CONSTRAINT campus_energy_consumption_hourly_pkey PRIMARY KEY (cust_id, level_id, capture_at);
ALTER TABLE campus_energy_consumption_daily
  ADD CONSTRAINT campus_energy_consumption_daily_pkey PRIMARY KEY (cust_id, level_id, capture_at);
ALTER TABLE organization_energy_consumption_15min
  ADD CONSTRAINT organization_energy_consumption_15min_pkey PRIMARY KEY (cust_id, level_id, capture_at);
ALTER TABLE organization_energy_consumption_hourly
  ADD CONSTRAINT organization_energy_consumption_hourly_pkey PRIMARY KEY (cust_id, level_id, capture_at);
ALTER TABLE organization_energy_consumption_daily
  ADD CONSTRAINT organization_energy_consumption_daily_pkey PRIMARY KEY (cust_id, level_id, capture_at);
  
INSERT INTO system_configuration (id, name, value) VALUES (nextval('system_configuration_seq'), 'FEATURE_ENERGY_AGGREGATION', 'false');
INSERT INTO system_configuration (id, name, value) VALUES (nextval('system_configuration_seq'), 'SYNC.FLOOR.ENERGY.CRON', '0 8/15 * 1/1 * ? *');

CREATE TABLE plan_map (
    id bigint NOT NULL,
    plan_map bytea NOT NULL,
    name character varying(64)
);

ALTER TABLE public.plan_map OWNER TO postgres;
ALTER TABLE ONLY plan_map ADD CONSTRAINT plan_map_pk PRIMARY KEY (id);
CREATE SEQUENCE plan_map_seq  START WITH 1 INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;
ALTER TABLE public.plan_map_seq OWNER TO postgres;

ALTER TABLE facility ADD COLUMN plan_map_id bigint ;
ALTER TABLE ONLY facility ADD CONSTRAINT plan_map_facility_fk FOREIGN KEY (plan_map_id) REFERENCES plan_map(id);
ALTER TABLE ONLY facility ADD CONSTRAINT customer_facility_fk FOREIGN KEY (customer_id) REFERENCES customer(id);
ALTER TABLE ONLY facility ADD CONSTRAINT facility_parent_id_facility_fk FOREIGN KEY (parent_id) REFERENCES facility(id);

ALTER TABLE ONLY facility_em_mapping ADD CONSTRAINT em_instance_facility_em_mapping_fk FOREIGN KEY (em_id) REFERENCES em_instance(id);
ALTER TABLE ONLY facility_em_mapping ADD CONSTRAINT facility_facility_em_mapping_fk FOREIGN KEY (facility_id) REFERENCES facility(id);
ALTER TABLE facility_em_mapping ADD COLUMN em_facility_path character varying(128);


 CREATE TABLE em_last_ec_synctime (
    id bigint NOT NULL,
    em_id bigint NOT NULL,
    last_sync_at timestamp without time zone
);

ALTER TABLE public.em_last_ec_synctime OWNER TO postgres;
ALTER TABLE ONLY em_last_ec_synctime ADD CONSTRAINT em_last_ec_synctime_pk PRIMARY KEY (id);    
ALTER TABLE ONLY em_last_ec_synctime ADD CONSTRAINT em_instance_em_last_ec_synctime_fk FOREIGN KEY (em_id) REFERENCES em_instance(id);

CREATE SEQUENCE em_last_ec_synctime_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE em_last_ec_synctime_seq OWNER TO postgres;

CREATE OR REPLACE FUNCTION on_em_insert_update_em_lastSync() RETURNS "trigger" AS $$ 
	BEGIN
		insert into em_last_ec_synctime(id,em_id) values(nextval('em_last_ec_synctime_seq'), new.id);
		RETURN new ;
	END
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_em_last_ec_synctime AFTER INSERT ON em_instance
  FOR EACH ROW EXECUTE PROCEDURE on_em_insert_update_em_lastSync();
  
  CREATE TABLE em_facility
(
	id bigint NOT NULL,
	em_id bigint,
	em_facility_type character varying(32),
  	em_facility_id bigint,
  	em_facility_name character varying(64),
	CONSTRAINT em_facility_pkey PRIMARY KEY (id)
);

ALTER TABLE em_facility OWNER TO postgres;

ALTER TABLE em_facility ADD CONSTRAINT em_facility_em_instance_fk FOREIGN KEY (em_id)
      REFERENCES em_instance (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
      
CREATE UNIQUE INDEX unique_em_facility ON em_facility USING btree (em_id, em_facility_type, em_facility_id);      

CREATE SEQUENCE em_facility_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE em_facility_seq OWNER TO postgres;

CREATE OR REPLACE FUNCTION updateUnassignedEmLastSynctime()
RETURNS void AS
$$
DECLARE
  hs_row record;
BEGIN
  FOR hs_row IN select em.id from em_instance em  LEFT OUTER JOIN em_last_ec_synctime el  on em.id = el.em_id where el.em_id is null 
  LOOP
	insert into em_last_ec_synctime(id,em_id) values(nextval('em_last_ec_synctime_seq'), hs_row.id);
  END LOOP;
END;
$$ LANGUAGE plpgsql;

select updateUnassignedEmLastSynctime() ;

DROP TYPE IF EXISTS energy_record;

CREATE TYPE energy_record AS (
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
	peak_amb numeric,
	min_amb numeric,
	price numeric,
	avg_amb numeric,
	motion_events integer
);


CREATE OR REPLACE FUNCTION aggregatedailyenergydataforfloor(todate timestamp without time zone, floor_id bigint) RETURNS void
    AS $$
DECLARE 
  cus_id bigint;
  l_id bigint;
  rec energy_record;
BEGIN
	RAISE NOTICE 'Enering aggregatedailyenergydataforfloor floord_id = %',floor_id;
	select level_id INTO l_id from floor_energy_consumption_daily where capture_at = todate and level_id = floor_id;
	select customer_id INTO cus_id from facility where id = floor_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM floor_energy_consumption_hourly as ec
	WHERE ec.capture_at <= todate and ec.capture_at > toDate - interval '1 day' and ec.level_id = floor_id GROUP BY ec.level_id)
	LOOP
	
	IF l_id IS NULL THEN
		RAISE NOTICE 'Inserting daily floor aggr at %',todate;
		INSERT INTO floor_energy_consumption_daily (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, floor_id, rec.agg_power, rec.price, rec.agg_cost, todate, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE NOTICE 'Updating daily floor aggr at %',todate;
		UPDATE floor_energy_consumption_daily SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=todate, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=todate and level_id=floor_id;
	END IF;
	END LOOP;
	
END
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aggregatehourlyenergydataforfloor(todate timestamp without time zone, floor_id bigint) RETURNS void
    AS $$
DECLARE 
  cus_id bigint;
  l_id bigint;
  rec energy_record;
BEGIN
	RAISE NOTICE 'Enering aggregatehourlyenergydataforfloor floord_id = %',floor_id;
	--Update aggregated energy data for the building
	select level_id INTO l_id from floor_energy_consumption_hourly where capture_at = todate and level_id = floor_id;
	select customer_id INTO cus_id from facility where id = floor_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM floor_energy_consumption_15min as ec
	WHERE ec.capture_at <= todate and ec.capture_at > toDate - interval '1 hour' and ec.level_id = floor_id GROUP BY ec.level_id)
	LOOP
	
	IF l_id IS NULL THEN
		RAISE NOTICE 'Inserting hourly floor aggr at %',todate;
		INSERT INTO floor_energy_consumption_hourly (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, floor_id, rec.agg_power, rec.price, rec.agg_cost, todate, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE NOTICE 'Updating hourly floor aggr at %',todate;
		UPDATE floor_energy_consumption_hourly SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=todate, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=todate and level_id=floor_id;
	END IF;
	END LOOP;
	IF(date_part('hour', toDate) = 0) THEN
		PERFORM aggregatedailyenergydataforfloor(toDate, floor_id);
	END IF;
END
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aggregatedailyenergydataforbldg(todate timestamp without time zone, bldg_id bigint) RETURNS void
    AS $$
DECLARE 
  cus_id bigint;
  l_id bigint;
  rec energy_record;
BEGIN
	RAISE NOTICE 'Enering aggregatedailyenergydataforbldg bldg_id = %',bldg_id;
	--Update aggregated energy data for the building
	select level_id INTO l_id from bld_energy_consumption_daily where capture_at = todate and level_id = bldg_id;
	select customer_id INTO cus_id from facility where id = bldg_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM bld_energy_consumption_hourly as ec
	WHERE ec.capture_at <= todate and ec.capture_at > toDate - interval '1 day' and ec.level_id = bldg_id GROUP BY ec.level_id)
	LOOP
	
	IF l_id IS NULL THEN
		RAISE NOTICE 'Inserting daily bldg aggr at %',todate;
		INSERT INTO bld_energy_consumption_daily (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, bldg_id, rec.agg_power, rec.price, rec.agg_cost, todate, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE NOTICE 'Updating daily bldg aggr at %',todate;
		UPDATE bld_energy_consumption_daily SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=todate, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=todate and level_id=bldg_id;
	END IF;
	END LOOP;
	
END
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aggregatehourlyenergydataforbldg(todate timestamp without time zone, bldg_id bigint) RETURNS void
    AS $$
DECLARE 
  cus_id bigint;
  l_id bigint;
  rec energy_record;
BEGIN
	RAISE NOTICE 'Enering aggregatehourlyenergydataforbldg bldg_id = %',bldg_id;
	--Update aggregated energy data for the building
	select level_id INTO l_id from bld_energy_consumption_hourly where capture_at = todate and level_id = bldg_id;
	select customer_id INTO cus_id from facility where id = bldg_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM bld_energy_consumption_15min as ec
	WHERE ec.capture_at <= todate and ec.capture_at > toDate - interval '1 hour' and ec.level_id = bldg_id GROUP BY ec.level_id)
	LOOP
	
	IF l_id IS NULL THEN
		RAISE NOTICE 'Inserting hourly bldg aggr at %',todate;
		INSERT INTO bld_energy_consumption_hourly (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, bldg_id, rec.agg_power, rec.price, rec.agg_cost, todate, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE NOTICE 'Updating hourly bldg aggr at %',todate;
		UPDATE bld_energy_consumption_hourly SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=todate, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=todate and level_id=bldg_id;
	END IF;
	END LOOP;
END
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aggregate15minenergydataforbldg(todate timestamp without time zone, bldg_id bigint) RETURNS void
    AS $$
DECLARE 
  cus_id bigint;
  data_captured_at timestamp without time zone;
  l_id bigint;
  rec energy_record;
  hour_part timestamp without time zone;
BEGIN
	RAISE NOTICE 'Enering aggregate15minenergydataforbldg bldg_id = %',bldg_id;
	data_captured_at = date_trunc('minute', todate);
	RAISE NOTICE 'Data captured at is %',data_captured_at;

	--Update aggregated energy data for the building
	select level_id INTO l_id from bld_energy_consumption_15min where capture_at = data_captured_at and level_id = bldg_id;
	select customer_id INTO cus_id from facility where id = bldg_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM floor_energy_consumption_15min as ec, facility f
	WHERE ec.capture_at = data_captured_at and ec.level_id = f.id and f.parent_id = bldg_id GROUP BY f.parent_id, ec.capture_at)
	LOOP
	
	RAISE NOTICE 'In the loop';
	IF l_id IS NULL THEN
		RAISE NOTICE 'Inserting 15min bldg aggr at %',todate;
		INSERT INTO bld_energy_consumption_15min (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, bldg_id, rec.agg_power, rec.price, rec.agg_cost, data_captured_at, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE NOTICE 'Updating 15min bldg aggr at %',todate;
		UPDATE bld_energy_consumption_15min SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=data_captured_at, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=data_captured_at and level_id=bldg_id;
	END IF;
	END LOOP;
	
	if(date_part('minute', data_captured_at) = 0) THEN
		PERFORM aggregatehourlyenergydataforbldg(data_captured_at, bldg_id);
	END IF;
	
	IF(date_part('hour', data_captured_at) = 0) THEN
		PERFORM aggregatedailyenergydataforbldg(date_trunc('hour', data_captured_at), bldg_id);
	END IF;
END
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aggregatedailyenergydataforcampus(todate timestamp without time zone, campus_id bigint) RETURNS void
    AS $$
DECLARE 
  cus_id bigint;
  l_id bigint;
  rec energy_record;
BEGIN
	RAISE NOTICE 'Enering aggregatedailyenergydataforcampus, campus_id = %',campus_id;
	--Update aggregated energy data for the building
	select level_id INTO l_id from campus_energy_consumption_daily where capture_at = todate and level_id = campus_id;
	select customer_id INTO cus_id from facility where id = campus_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM campus_energy_consumption_hourly as ec
	WHERE ec.capture_at <= todate and ec.capture_at > toDate - interval '1 day' and ec.level_id = campus_id GROUP BY ec.level_id)
	LOOP
	
	IF l_id IS NULL THEN
		RAISE NOTICE 'Inserting daily campus aggr at %',todate;
		INSERT INTO campus_energy_consumption_daily (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, campus_id, rec.agg_power, rec.price, rec.agg_cost, todate, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE NOTICE 'Updating daily campus aggr at %',todate;
		UPDATE campus_energy_consumption_daily SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=todate, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=todate and level_id=campus_id;
	END IF;
	END LOOP;
	
END
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aggregatehourlyenergydataforcampus(todate timestamp without time zone, campus_id bigint) RETURNS void
    AS $$
DECLARE 
  cus_id bigint;
  l_id bigint;
  rec energy_record;
BEGIN
	RAISE NOTICE 'Enering aggregatehourlyenergydataforcampus, campus_id = %',campus_id;
	--Update aggregated energy data for the building
	select level_id INTO l_id from campus_energy_consumption_hourly where capture_at = todate and level_id = campus_id;
	select customer_id INTO cus_id from facility where id = campus_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM campus_energy_consumption_15min as ec
	WHERE ec.capture_at <= todate and ec.capture_at > toDate - interval '1 hour' and ec.level_id = campus_id GROUP BY ec.level_id)
	LOOP
	
	IF l_id IS NULL THEN
		RAISE NOTICE 'Inserting hourly campus aggr at %',todate;
		INSERT INTO campus_energy_consumption_hourly (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, campus_id, rec.agg_power, rec.price, rec.agg_cost, todate, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE NOTICE 'Inserting hourly campus aggr at %',todate;
		UPDATE campus_energy_consumption_hourly SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=todate, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=todate and level_id=campus_id;
	END IF;
	END LOOP;
	
END
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aggregate15minenergydataforcampus(todate timestamp without time zone, campus_id bigint) RETURNS void
    AS $$
DECLARE 
  cus_id bigint;
  data_captured_at timestamp without time zone;
  l_id bigint;
  rec energy_record;
BEGIN
	RAISE NOTICE 'Enering aggregate15minenergydataforcampus, campus_id = %',campus_id;
	data_captured_at = date_trunc('minute', todate);

	select customer_id INTO cus_id from facility where id = campus_id;
	
	--Update aggregated energy data for the campus
	select level_id INTO l_id from campus_energy_consumption_15min where capture_at = data_captured_at and level_id = campus_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM bld_energy_consumption_15min as ec, facility f
	WHERE ec.capture_at = data_captured_at and ec.level_id = f.id and f.parent_id = campus_id GROUP BY f.parent_id, ec.capture_at)
	LOOP
	
	IF l_id IS NULL THEN
		RAISE NOTICE 'Inserting 15min campus aggr at %',data_captured_at;
		INSERT INTO campus_energy_consumption_15min (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, campus_id, rec.agg_power, rec.price, rec.agg_cost, data_captured_at, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE NOTICE 'Updating 15min campus aggr at %',data_captured_at;
		UPDATE campus_energy_consumption_15min SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=data_captured_at, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=data_captured_at and level_id=campus_id;
	END IF;
	END LOOP;
	IF(date_part('minute', data_captured_at) = 0) THEN
		PERFORM aggregatehourlyenergydataforcampus(data_captured_at, campus_id);
	END IF;

	IF(date_part('hour', data_captured_at) = 0) THEN
		PERFORM aggregatedailyenergydataforcampus(date_trunc('hour', toDate), campus_id);
	END IF;
END
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aggregatedailyenergydataforcompany(todate timestamp without time zone, company_id bigint) RETURNS void
    AS $$
DECLARE 
  cus_id bigint;
  l_id bigint;
  rec energy_record;
BEGIN
	RAISE NOTICE 'Enering aggregatedailyenergydataforcompany, company_id = %',company_id;
	--Update aggregated energy data for the building
	select level_id INTO l_id from organization_energy_consumption_daily where capture_at = todate and level_id = company_id;
	select customer_id INTO cus_id from facility where id = company_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM organization_energy_consumption_hourly as ec
	WHERE ec.capture_at <= todate and ec.capture_at > toDate - interval '1 day' and ec.level_id = company_id GROUP BY ec.level_id)
	LOOP
	
	IF l_id IS NULL THEN
		RAISE NOTICE 'Inserting daily company aggr at %',todate;
		INSERT INTO organization_energy_consumption_daily (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, company_id, rec.agg_power, rec.price, rec.agg_cost, todate, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE NOTICE 'Updating daily company aggr at %',todate;
		UPDATE organization_energy_consumption_daily SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=todate, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=todate and level_id=company_id;
	END IF;
	END LOOP;
	
END
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aggregatehourlyenergydataforcompany(todate timestamp without time zone, company_id bigint) RETURNS void
    AS $$
DECLARE 
  cus_id bigint;
  l_id bigint;
  rec energy_record;
BEGIN
	RAISE NOTICE 'Enering aggregatehourlyenergydataforcompany, company_id = %',company_id;
	--Update aggregated energy data for the building
	select level_id INTO l_id from organization_energy_consumption_hourly where capture_at = todate and level_id = company_id;
	select customer_id INTO cus_id from facility where id = company_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM organization_energy_consumption_15min as ec
	WHERE ec.capture_at <= todate and ec.capture_at > toDate - interval '1 hour' and ec.level_id = company_id GROUP BY ec.level_id)
	LOOP
	
	IF l_id IS NULL THEN
		RAISE NOTICE 'Inserting hourly company aggr at %',todate;
		INSERT INTO organization_energy_consumption_hourly (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, company_id, rec.agg_power, rec.price, rec.agg_cost, todate, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE NOTICE 'Updating hourly company aggr at %',todate;
		UPDATE organization_energy_consumption_hourly SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=todate, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=todate and level_id=company_id;
	END IF;
	END LOOP;
	
END
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aggregate15minenergydataforcompany(todate timestamp without time zone, company_id bigint) RETURNS void
    AS $$
DECLARE 
  cus_id bigint;
  data_captured_at timestamp without time zone;
  l_id bigint;
  rec energy_record;
BEGIN
	RAISE NOTICE 'Enering aggregate15minenergydataforcompany, company_id = %',company_id;
	data_captured_at = date_trunc('minute', todate);

	--Update aggregated energy data for the company
	select customer_id INTO cus_id from facility where id = company_id;
	select level_id INTO l_id from organization_energy_consumption_15min where capture_at = data_captured_at and level_id = company_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM campus_energy_consumption_15min as ec, facility f
	WHERE ec.capture_at = data_captured_at and ec.level_id = f.id and f.parent_id = company_id GROUP BY f.parent_id, ec.capture_at)
	LOOP
	
	IF l_id IS NULL THEN
		RAISE NOTICE 'Inserting 15min company aggr at %',data_captured_at;
		INSERT INTO organization_energy_consumption_15min (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, company_id, rec.agg_power, rec.price, rec.agg_cost, data_captured_at, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE NOTICE 'Updating 15min company aggr at %',data_captured_at;
		UPDATE organization_energy_consumption_15min SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=data_captured_at, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=data_captured_at and level_id=company_id;
	END IF;
	END LOOP;
	IF(date_part('minute', data_captured_at) = 0) THEN
		PERFORM aggregatehourlyenergydataforcompany(data_captured_at, company_id);
	END IF;

	IF(date_part('hour', data_captured_at) = 0) THEN
		PERFORM aggregatedailyenergydataforcompany(date_trunc('hour', toDate), company_id);
	END IF;
END
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION aggregateenergydata() RETURNS trigger
    AS $$
DECLARE 
  floor_id bigint;
  bldg_id bigint;
  campus_id bigint;
  company_id bigint;
  cus_id bigint;
  data_captured_at timestamp without time zone;
  l_id bigint;
  rec energy_record;
  date_hour_boundary timestamp without time zone;
  date_day_boundary timestamp without time zone;
BEGIN
	RAISE WARNING 'Entering the trigger...';
	floor_id = NEW.level_id;
	cus_id = NEW.cust_id;
	
	data_captured_at = date_trunc('minute', NEW.capture_at);
	RAISE NOTICE 'Data captured at is %',data_captured_at;

	IF(date_part('minute', data_captured_at) = 15) THEN
		date_hour_boundary = data_captured_at + interval '45 minutes';
	ELSEIF(date_part('minute', data_captured_at) = 30) THEN
		date_hour_boundary = data_captured_at + interval '30 minutes';
	ELSEIF(date_part('minute', data_captured_at) = 45) THEN
		date_hour_boundary = data_captured_at + interval '15 minutes';
	ELSE
		date_hour_boundary = data_captured_at;
	END IF;

	RAISE NOTICE 'Hourly boundary is %',date_hour_boundary;

	IF(date_part('hour', date_hour_boundary) = 0) THEN
		date_day_boundary = date_hour_boundary;
	ELSE
		date_day_boundary = date_trunc('day', date_hour_boundary) + interval '1 day';
	END IF;
	
	RAISE NOTICE 'Daily boundary is %',date_day_boundary;

	if(TG_OP = 'UPDATE') THEN
		-- update hourly and daily data for this floor
		PERFORM aggregatehourlyenergydataforfloor(date_hour_boundary, floor_id);
		PERFORM aggregatedailyenergydataforfloor(date_day_boundary, floor_id);
	ELSEIF (TG_OP = 'INSERT') THEN
		IF(date_part('minute', data_captured_at) = 0) THEN
			PERFORM aggregatehourlyenergydataforfloor(data_captured_at, floor_id);
			IF(date_part('hour', data_captured_at) = 0) THEN
				PERFORM aggregatedailyenergydataforfloor(date_trunc('hour', data_captured_at), floor_id);
			END IF;
		END IF;
	END IF;
	
	--Update aggregated energy data for the building
	select parent_id INTO bldg_id from facility where id = floor_id and customer_id=cus_id;
	select level_id INTO l_id from bld_energy_consumption_15min where capture_at = data_captured_at and level_id = bldg_id;
	
	RAISE NOTICE 'Value of cus_id is %',cus_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM floor_energy_consumption_15min as ec, facility f
	WHERE ec.capture_at = data_captured_at and ec.level_id = f.id and f.parent_id = bldg_id GROUP BY f.parent_id, ec.capture_at)
	LOOP
	
	RAISE NOTICE 'In the loop';
	IF l_id IS NULL THEN
		RAISE WARNING 'Inserting bldg_energy_cons ...';
		INSERT INTO bld_energy_consumption_15min (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, bldg_id, rec.agg_power, rec.price, rec.agg_cost, data_captured_at, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE WARNING 'Updating bldg_energy_cons capture_at...%',data_captured_at;
		UPDATE bld_energy_consumption_15min SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=data_captured_at, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=data_captured_at and level_id=bldg_id;
	END IF;
	END LOOP;

	if(TG_OP = 'UPDATE') THEN
		PERFORM aggregatehourlyenergydataforbldg(date_hour_boundary, bldg_id);
		PERFORM aggregatedailyenergydataforbldg(date_day_boundary, bldg_id);
	ELSEIF (TG_OP = 'INSERT') THEN
		IF(date_part('minute', data_captured_at) = 0) THEN
			PERFORM aggregatehourlyenergydataforbldg(data_captured_at, bldg_id);
			IF(date_part('hour', data_captured_at) = 0) THEN
				PERFORM aggregatedailyenergydataforbldg(date_trunc('hour', data_captured_at), bldg_id);
			END IF;
		END IF;
	END IF;

	--Update aggregated energy data for the campus
	select parent_id INTO campus_id from facility where id = bldg_id and customer_id=cus_id;
	select level_id INTO l_id from campus_energy_consumption_15min where capture_at = data_captured_at and level_id = campus_id;
	
	RAISE NOTICE 'Value of cus_id is %',cus_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM bld_energy_consumption_15min as ec, facility f
	WHERE ec.capture_at = data_captured_at and ec.level_id = f.id and f.parent_id = campus_id GROUP BY f.parent_id, ec.capture_at)
	LOOP
	
	RAISE NOTICE 'In the loop';
	IF l_id IS NULL THEN
		RAISE WARNING 'Inserting campus_energy_cons ...';
		INSERT INTO campus_energy_consumption_15min (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, campus_id, rec.agg_power, rec.price, rec.agg_cost, data_captured_at, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE WARNING 'Updating campus_energy_cons capture_at...%',data_captured_at;
		UPDATE campus_energy_consumption_15min SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=data_captured_at, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=data_captured_at and level_id=campus_id;
	END IF;
	END LOOP;

	if(TG_OP = 'UPDATE') THEN
		PERFORM aggregatehourlyenergydataforcampus(date_hour_boundary, campus_id);
		PERFORM aggregatedailyenergydataforcampus(date_day_boundary, campus_id);
	ELSEIF (TG_OP = 'INSERT') THEN
		IF(date_part('minute', data_captured_at) = 0) THEN
			PERFORM aggregatehourlyenergydataforcampus(data_captured_at, campus_id);
			IF(date_part('hour', data_captured_at) = 0) THEN
				PERFORM aggregatedailyenergydataforcampus(date_trunc('hour', data_captured_at), campus_id);
			END IF;
		END IF;
	END IF;

	--Update aggregated energy data for the company
	select parent_id INTO company_id from facility where id = campus_id and customer_id=cus_id;
	select level_id INTO l_id from organization_energy_consumption_15min where capture_at = data_captured_at and level_id = company_id;
	
	FOR rec IN (
	SELECT SUM(energy) AS agg_power, sum(cost) AS agg_cost, min(min_temp) AS min_temp, max(max_temp) AS max_temp, avg(avg_temp) AS avg_temp, SUM(base_energy) AS base_power, sum(base_cost) AS base_cost, SUM(saved_energy) AS saved_power, sum(saved_cost) AS  saved_cost, SUM(occ_savings) AS occ_saving, SUM(ambient_savings) AS amb_saving, SUM(tuneup_savings) AS tune_saving, SUM(manual_savings) AS manual_saving, max(max_amb) AS peak_amb, min(min_amb) AS min_amb, avg(price) AS price, avg(avg_amb) AS avg_amb, sum(motion_events) AS motion_events
	FROM campus_energy_consumption_15min as ec, facility f
	WHERE ec.capture_at = data_captured_at and ec.level_id = f.id and f.parent_id = company_id GROUP BY f.parent_id, ec.capture_at)
	LOOP
	
	RAISE NOTICE 'In the loop';
	IF l_id IS NULL THEN
		RAISE WARNING 'Inserting campus_energy_cons ...';
		INSERT INTO organization_energy_consumption_15min (cust_id, level_id, energy, price, cost, capture_at, min_temp, max_temp, avg_temp, base_energy, base_cost, saved_energy, saved_cost, occ_savings, ambient_savings, tuneup_savings, manual_savings, max_amb, min_amb, avg_amb, motion_events) VALUES (cus_id, company_id, rec.agg_power, rec.price, rec.agg_cost, data_captured_at, rec.min_temp, rec.max_temp, round(rec.avg_temp), rec.base_power, rec.base_cost, rec.saved_power, rec.saved_cost, rec.occ_saving, rec.amb_saving, rec.tune_saving, rec.manual_saving, rec.peak_amb, rec.min_amb, rec.avg_amb, rec.motion_events);
	ELSE
		RAISE WARNING 'Updating campus_energy_cons capture_at...%',data_captured_at;
		UPDATE organization_energy_consumption_15min SET energy = rec.agg_power, price=rec.price, cost=rec.agg_cost, capture_at=data_captured_at, min_temp = rec.min_temp, max_temp=rec.max_temp, avg_temp=rec.avg_temp, base_energy=rec.base_power, base_cost = rec.base_cost, saved_energy=rec.saved_power, saved_cost=rec.saved_cost, occ_savings=rec.occ_saving, ambient_savings=rec.amb_saving, tuneup_savings=rec.tune_saving, manual_savings=rec.manual_saving, max_amb = rec.peak_amb, min_amb=rec.min_amb, avg_amb=rec.avg_amb, motion_events = rec.motion_events where capture_at=data_captured_at and level_id=company_id;
	END IF;
	END LOOP;

	if(TG_OP = 'UPDATE') THEN
		PERFORM aggregatehourlyenergydataforcompany(date_hour_boundary, company_id);
		PERFORM aggregatedailyenergydataforcompany(date_day_boundary, company_id);
	ELSEIF (TG_OP = 'INSERT') THEN
		IF(date_part('minute', data_captured_at) = 0) THEN
			PERFORM aggregatehourlyenergydataforcompany(data_captured_at, company_id);
			IF(date_part('hour', data_captured_at) = 0) THEN
				PERFORM aggregatedailyenergydataforcompany(date_trunc('hour', data_captured_at), company_id);
			END IF;
		END IF;
	END IF;

	RETURN NULL;

END
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS aggr_energy_data_trigger on floor_energy_consumption_15min;
CREATE TRIGGER aggr_energy_data_trigger AFTER INSERT or UPDATE ON floor_energy_consumption_15min FOR EACH ROW EXECUTE PROCEDURE aggregateenergydata();



-- END schema changes related to energy aggregation and sync

ALTER TABLE site ADD COLUMN square_foot numeric(19,2);
ALTER TABLE site ADD COLUMN region character varying(20);

ALTER TABLE em_instance DROP COLUMN block_purchase_energy;
ALTER TABLE em_instance DROP COLUMN geo_location;
ALTER TABLE em_instance DROP COLUMN sppa_price;
ALTER TABLE customer DROP COLUMN sppa_price;

SELECT setval('site_seq', (SELECT max(id)+1 FROM site));
SELECT setval('em_site_seq', (SELECT max(id)+1 FROM em_site));

CREATE TYPE customer_record AS (
		customerId bigint ,
		customerName character varying
);

CREATE OR REPLACE FUNCTION fillFacilityFromCustomers() RETURNS void AS 
$BODY$
DECLARE 
	rec customer_record;
	facilityCustomerId bigint;
BEGIN
	FOR rec IN (
	SELECT id as customerId,name as customerName from customer) 
	LOOP 
	  SELECT f.customer_id INTO facilityCustomerId from facility f where rec.customerId = f.customer_id and f.type = 1 LIMIT 1 ;
	  IF (facilityCustomerId IS NULL) THEN
		INSERT INTO facility(id,name,type,customer_id) VALUES (nextval('facility_seq'),rec.customerName,1,rec.customerId);
	  END IF;
	END LOOP;

END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;

select fillFacilityFromCustomers();

ALTER TABLE site ADD COLUMN estimated_burn_hours integer default 0;
UPDATE site set estimated_burn_hours = 0 where estimated_burn_hours is null;

CREATE TABLE site_anomalies (
  id bigint NOT NULL,
  geo_location character varying,
  report_date timestamp without time zone,
  start_date timestamp without time zone,
  end_date timestamp without time zone,
  issue character varying(50),
  details character varying,
  CONSTRAINT id_pkey PRIMARY KEY (id),
  CONSTRAINT site_anomaly_unique_constraint UNIQUE(geo_location, report_date, issue)  
);
ALTER TABLE public.site_anomalies OWNER TO postgres;

CREATE SEQUENCE site_anomalies_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.site_anomalies_seq OWNER TO postgres;

INSERT INTO system_configuration (id, name, value) VALUES (nextval('system_configuration_seq'), 'VALIDATE.SITE.ANOMALY.CRON', '0 0 2 * * ?');

INSERT INTO system_configuration (id, name, value) VALUES (nextval('system_configuration_seq'), 'MetaDataServer.IP', 'localhost');
