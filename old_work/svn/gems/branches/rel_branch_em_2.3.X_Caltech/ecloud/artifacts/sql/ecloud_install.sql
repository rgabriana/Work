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
    sppa_price double precision default 0.08
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
    block_energy_remaining numeric(19,2),
    sppa_bill_enabled boolean default false
);


ALTER TABLE public.em_instance OWNER TO postgres;

ALTER TABLE ONLY em_instance
    ADD CONSTRAINT em_instance_pk PRIMARY KEY (id);

ALTER TABLE ONLY em_instance
    ADD CONSTRAINT em_instance_customer_fk FOREIGN KEY (customer_id) REFERENCES customer(id);
ALTER TABLE ONLY em_instance
    ADD CONSTRAINT em_instance_replica_server_fk FOREIGN KEY (replica_server_id) REFERENCES replica_server(id);
    
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

