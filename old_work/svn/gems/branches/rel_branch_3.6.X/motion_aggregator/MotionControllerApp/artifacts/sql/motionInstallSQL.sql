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

INSERT INTO system_configuration (id, name, value) values ((select coalesce(max(id),0)+1 from system_configuration), 'motion.udp.port', '8084');

CREATE TABLE motion_packets (
  id bigint NOT NULL,
  fixture_id bigint,
  blob_id character varying,
  local_x integer,
  local_y integer,
  global_x integer,
  global_y integer,
  capture_at timestamp without time zone,
  CONSTRAINT motion_packets_pk PRIMARY KEY (id)  
);

ALTER TABLE motion_packets OWNER TO postgres;
ALTER TABLE ONLY motion_packets 
    ADD CONSTRAINT fixture_id_fk FOREIGN KEY(fixture_id) REFERENCES fixture(id);

--
-- Name: system_configuration_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE motion_packets_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE motion_packets_seq OWNER TO postgres;

CREATE TABLE fixture (
  id bigint NOT NULL,
  mac character varying,
  x integer,
  y integer,
  floor_id bigint,
  CONSTRAINT fixture_pk PRIMARY KEY (id)  
);

ALTER TABLE fixture OWNER TO postgres;

--
-- Name: system_configuration_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE fixture_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE fixture_seq OWNER TO postgres;

ALTER TABLE ONLY motion_packets 
    ADD CONSTRAINT fixture_id_fk FOREIGN KEY(fixture_id) REFERENCES fixture(id);
    
 CREATE TABLE floor (
  id bigint NOT NULL,
  name character varying,
  width integer,
  height integer,
  CONSTRAINT floor_pk PRIMARY KEY (id)  
);

ALTER TABLE floor OWNER TO postgres;

--
-- Name: system_configuration_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE floor_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE floor_seq OWNER TO postgres;

ALTER TABLE ONLY fixture 
    ADD CONSTRAINT floor_id_fk FOREIGN KEY(floor_id) REFERENCES floor(id);
    
CREATE TABLE display_data (
  id bigint NOT NULL,
  message text,
  CONSTRAINT display_data_pk PRIMARY KEY (id)  
);

ALTER TABLE display_data OWNER TO postgres;

--
-- Name: system_configuration_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE display_data_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE display_data_seq OWNER TO postgres;
    
