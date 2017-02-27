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


INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'browsing.supported.version', '3.0.0');

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
INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'db_pruning.emstats_table', '90');

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
  
  INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'browsing.show', 'false');
