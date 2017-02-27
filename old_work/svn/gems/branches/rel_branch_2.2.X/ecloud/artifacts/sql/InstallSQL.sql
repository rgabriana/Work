
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
    phone character varying
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


ALTER TABLE public.user_seq OWNER TO postgres;

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
    name character varying,
    mac_id character varying NOT NULL,
    version character varying,
    last_connectivity_at timestamp without time zone,
    active boolean default false
);


ALTER TABLE public.em_instance OWNER TO postgres;

ALTER TABLE ONLY em_instance
    ADD CONSTRAINT em_instance_pk PRIMARY KEY (id);

ALTER TABLE ONLY em_instance
    ADD CONSTRAINT em_instance_customer_fk FOREIGN KEY (customer_id) REFERENCES customer(id);
    
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

