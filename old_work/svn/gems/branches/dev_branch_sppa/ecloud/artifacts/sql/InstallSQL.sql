
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

CREATE SEQUENCE user_seq
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

	private long id;
	private String version;
	private Customer customer;
	private String name;
	private String macId;
	private String timeZone;
	private Date lastConnectivityAt;
	private String databaseName;
	
CREATE TABLE em_instance (
    id bigint NOT NULL,
    customer_id bigint NOT NULL,
    name character varying NOT NULL,
    mac_id character varying NOT NULL,
    version character varying,
    security_key character varying,
    time_zone character varying,
    last_connectivity_at timestamp without time zone,
    database_name character varying  
);


ALTER TABLE public.em_instance OWNER TO postgres;

ALTER TABLE ONLY em_instance
    ADD CONSTRAINT em_instance_pk PRIMARY KEY (id);

ALTER TABLE ONLY em_instance
    ADD CONSTRAINT em_instance_customer_fk FOREIGN KEY (customer_id) REFERENCES customer(id);
    
    
INSERT INTO users (id, email, "password", first_name, last_name, created_on, role_type,status) VALUES (nextval('users_seq'), 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Administrator', '', '2012-01-19', 'Admin','A');

