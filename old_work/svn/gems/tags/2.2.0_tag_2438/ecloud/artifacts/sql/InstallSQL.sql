
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


CREATE TABLE em_instance (
    id bigint NOT NULL,
    customer_id bigint NOT NULL,
    name character varying NOT NULL,
    mac_id character varying NOT NULL,
    version character varying,
    security_key character varying,
    time_zone character varying,
    last_connectivity_at timestamp without time zone,
    latitude  numeric(4,2),
    longitude numeric(4,2)   
);


ALTER TABLE public.em_instance OWNER TO postgres;

ALTER TABLE ONLY em_instance
    ADD CONSTRAINT em_instance_pk PRIMARY KEY (id);

ALTER TABLE ONLY em_instance
    ADD CONSTRAINT em_instance_customer_fk FOREIGN KEY (customer_id) REFERENCES customer(id);


   



    

 
    
    


    
    
    

    

    

    
    
  --
-- Cloud side Facility
--

--
-- Name: campus; Type: TABLE; Owner: postgres; Tablespace: 
--

CREATE TABLE cloud_campus
(
  id bigint NOT NULL,
  customer_id bigint not null,
  name character varying(256) NOT NULL,
  "location" character varying(256),
  zipcode character varying(16),
  CONSTRAINT cloud_campus_pk PRIMARY KEY (id)
  

);

ALTER TABLE cloud_campus OWNER TO postgres;

ALTER TABLE ONLY cloud_campus
    ADD CONSTRAINT customer_campus_fk FOREIGN KEY (customer_id) REFERENCES customer(id);




--
-- Name: campus_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE cloud_campus_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE cloud_campus_seq OWNER TO postgres;




--
-- Name: building; Type: TABLE; Owner: postgres;
--

CREATE TABLE cloud_building
(
  id bigint NOT NULL,
  name character varying(256) NOT NULL,
  cloud_campus_id bigint NOT NULL,
  CONSTRAINT building_pk PRIMARY KEY (id),
  CONSTRAINT fkaaba12b4108db0ef FOREIGN KEY (cloud_campus_id)
      REFERENCES cloud_campus (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
 
);

ALTER TABLE cloud_building OWNER TO postgres;



--
-- Name: building_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE cloud_building_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE cloud_building_seq OWNER TO postgres;



--
-- Name: floor; Type: TABLE; Owner: postgres;
--

CREATE TABLE cloud_floor
(
  id bigint NOT NULL,
  name character varying(128) NOT NULL,
  description character varying(512),
  cloud_building_id bigint,
  floorplan_url character varying(255),
  plan_map_id bigint,
  no_installed_sensors integer DEFAULT 0,
  no_installed_fixtures integer DEFAULT 0,
  floor_plan_uploaded_time timestamp without time zone,
  CONSTRAINT floor_pk PRIMARY KEY (id),
  CONSTRAINT fk5d0240cd3a6d38f FOREIGN KEY (cloud_building_id)
      REFERENCES cloud_building (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE cloud_floor OWNER TO postgres;



--
-- Name: floor_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE cloud_floor_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE cloud_floor_seq OWNER TO postgres;




  
    
--
-- EM Side facility 
--    
    
--
-- Name: campus; Type: TABLE; Owner: postgres; Tablespace: 
--

CREATE TABLE em_campus
(
  id bigint NOT NULL,
  cloud_campus_id bigint ,
  em_instance_id bigint not null,
  em_remote_id bigint ,
  name character varying(256) NOT NULL,
  "location" character varying(256),
  zipcode character varying(16),
  CONSTRAINT campus_pk PRIMARY KEY (id)
  

);

ALTER TABLE em_campus OWNER TO postgres;


ALTER TABLE ONLY em_campus
    ADD CONSTRAINT cloud_campus_fk FOREIGN KEY (cloud_campus_id) REFERENCES cloud_campus(id);
 ALTER TABLE ONLY em_campus
    ADD CONSTRAINT em_instance_campus_fk FOREIGN KEY (em_instance_id) REFERENCES em_instance(id);




--
-- Name: campus_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE em_campus_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE em_campus_seq OWNER TO postgres;



--
-- Name: building; Type: TABLE; Owner: postgres;
--

CREATE TABLE em_building
(
  id bigint NOT NULL,
  cloud_building_id bigint,
  em_remote_id bigint,
  em_campus_id bigint,
  name character varying(256) NOT NULL,
  CONSTRAINT em_building_pk PRIMARY KEY (id),
  CONSTRAINT fkaaba12b4108db0ef FOREIGN KEY (em_campus_id)
      REFERENCES em_campus (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
 
);

ALTER TABLE em_building OWNER TO postgres;

ALTER TABLE ONLY em_building
    ADD CONSTRAINT cloud_building_fk FOREIGN KEY (cloud_building_id) REFERENCES cloud_building(id);

--
-- Name: building_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE em_building_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE em_building_seq OWNER TO postgres;



--
-- Name: floor; Type: TABLE; Owner: postgres;
--

CREATE TABLE em_floor
(
  id bigint NOT NULL,
  cloud_floor_id bigint,
  em_remote_id bigint,
  em_building_id bigint,
  name character varying(128) NOT NULL,
  description character varying(512),
  floorplan_url character varying(255),
  plan_map_id bigint,
  no_installed_sensors integer DEFAULT 0,
  no_installed_fixtures integer DEFAULT 0,
  floor_plan_uploaded_time timestamp without time zone,
  CONSTRAINT em_floor_pk PRIMARY KEY (id),
  CONSTRAINT fk5d0240cd3a6d38f FOREIGN KEY (em_building_id)
      REFERENCES em_building (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE em_floor OWNER TO postgres;

ALTER TABLE ONLY em_floor
    ADD CONSTRAINT cloud_floor_fk FOREIGN KEY (cloud_floor_id) REFERENCES cloud_floor(id);


--
-- Name: floor_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE em_floor_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE em_floor_seq OWNER TO postgres;







--
-- Name:Device; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--    
  CREATE SEQUENCE device_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;  
    
 ALTER TABLE public.device_seq OWNER TO postgres;
    
 CREATE TABLE device (
    id bigint NOT NULL,
    em_local_id bigint NOT NULL,
    name character varying NOT NULL,
    em_instance_facility_id bigint NOT NULL,
    em_floor_id bigint NOT NULL, 
    cloud_floor_id bigint NOT NULL, 
    device_type character varying ,
    x bigint,
    y bigint ,
    mac_address character varying,
    device_version character varying
);
   
    
ALTER TABLE public.device OWNER TO postgres;

ALTER TABLE ONLY device
    ADD CONSTRAINT device_pk PRIMARY KEY (id);
    
 ALTER TABLE ONLY device
    ADD CONSTRAINT floor_id_device_fk FOREIGN KEY (em_floor_id) REFERENCES em_floor(id);
    
 ALTER TABLE ONLY device
    ADD CONSTRAINT cloud_floor_id_device_fk FOREIGN KEY (cloud_floor_id) REFERENCES  cloud_floor(id);

--
-- Name:gateway; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--
CREATE SEQUENCE gateway_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.gateway_seq OWNER TO postgres;


CREATE TABLE gateway (
	  id bigint NOT NULL,
	  ip_address character varying(255),
	  snap_address character varying, 
	  no_of_commissioned_sensors integer default 0,
	  boot_loader_version character varying(50),
	  firmware_version character varying(50)
	
);


ALTER TABLE public.gateway OWNER TO postgres;

ALTER TABLE ONLY gateway
    ADD CONSTRAINT gateway_pk PRIMARY KEY (id);
    
 ALTER TABLE ONLY gateway
    ADD CONSTRAINT inheritance_gateway_join_fk FOREIGN KEY (id) REFERENCES device(id);
    

--
-- Name:sensor; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--
CREATE SEQUENCE fixture_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.fixture_seq OWNER TO postgres;


CREATE TABLE fixture (
      id bigint NOT NULL,
      snap_address character varying,  
      gateway_id bigint NOT NULL,
  	  firmware_version character varying(20),
	  bootloader_version character varying(20),
	  state character varying,
	  cu_version character varying(20),
	  version_synced integer DEFAULT 0,
	  bulbs_last_service_date date,
	  ballast_last_service_date date,
	  ip_address character varying(255),
	  baseline_power numeric(19,2) DEFAULT 0
    
);


ALTER TABLE public.fixture OWNER TO postgres;

ALTER TABLE ONLY fixture
    ADD CONSTRAINT fixture_pk PRIMARY KEY (id);

 ALTER TABLE ONLY fixture
    ADD CONSTRAINT inheritance_fixture_join_fk FOREIGN KEY (id) REFERENCES device(id);

ALTER TABLE ONLY fixture
    ADD CONSTRAINT gateway_id_fk FOREIGN KEY (gateway_id) REFERENCES gateway(id);

    
--
-- Adding Default values
--


INSERT INTO users (id, email, "password", first_name, last_name, created_on, role_type,status) VALUES (nextval('user_seq'), 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Administrator', '', '2012-01-19', 'Admin','A');

