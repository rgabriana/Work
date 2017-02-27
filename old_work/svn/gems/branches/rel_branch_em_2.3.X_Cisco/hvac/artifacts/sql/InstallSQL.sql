CREATE PROCEDURAL LANGUAGE plpgsql;

CREATE TABLE users
(
  id bigint NOT NULL,
  email character varying NOT NULL,
  "password" character varying NOT NULL,
  role_id bigint,
  status character varying,
  CONSTRAINT users_pk PRIMARY KEY (id),
  CONSTRAINT unique_users_name UNIQUE (email)
);

ALTER TABLE users OWNER TO postgres;

CREATE SEQUENCE user_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE user_seq OWNER TO postgres;


CREATE TABLE roles
(
  id bigint NOT NULL,
  name character varying,
  CONSTRAINT role_pk PRIMARY KEY (id),
  CONSTRAINT unique_roles_name UNIQUE (name)
);

ALTER TABLE roles OWNER TO postgres;

--
-- Name: role_seq; Type: SEQUENCE; Owner: postgres
--

CREATE SEQUENCE role_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE role_seq OWNER TO postgres;

CREATE SEQUENCE zones_seq
   INCREMENT 1
   START 1;
ALTER TABLE zones_seq OWNER TO postgres;

CREATE TABLE zones
( id bigint not null,
  name character varying NOT NULL
);

ALTER TABLE public.zones OWNER TO postgres;

ALTER TABLE ONLY zones ADD CONSTRAINT zones_pk PRIMARY KEY (id);

CREATE SEQUENCE zones_sensor_seq
   INCREMENT 1
   START 1;
ALTER TABLE zones_sensor_seq OWNER TO postgres;


CREATE TABLE sensor (
  id bigint not null,
  name character varying(50),
  mac_address character varying(50),
  last_occupancy_seen integer,
  current_dim_level integer,
  outage_flag boolean,
  last_status_time timestamp without time zone,
  avg_temperature numeric,
  avg_ambient_light integer,  
  CONSTRAINT sensor_pk PRIMARY KEY (id),
  CONSTRAINT unique_sensor_mac_address UNIQUE (mac_address)
);

ALTER TABLE sensor OWNER TO postgres;

CREATE SEQUENCE sensor_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE sensor_seq OWNER TO postgres;

CREATE TABLE zones_sensor
(
  id bigint NOT NULL,
  zone_id bigint NOT NULL,
  sensor_id bigint NOT NULL
);

ALTER TABLE zones_sensor OWNER TO postgres;


ALTER TABLE zones_sensor ADD CONSTRAINT zones_sensor_pk PRIMARY KEY (id);
ALTER TABLE zones_sensor ADD CONSTRAINT fk_zones_sensor_zones FOREIGN KEY (zone_id) REFERENCES zones (id);
ALTER TABLE zones_sensor ADD CONSTRAINT fk_zones_sensor_sensor FOREIGN KEY (sensor_id) REFERENCES sensor (id);
ALTER TABLE zones_sensor ADD CONSTRAINT zone_sensor_unique UNIQUE (zone_id,sensor_id);

create table hvac_configuration (
	id bigint not null,
	name character varying(128),
	value character varying(128),
	CONSTRAINT hvac_configuration_pk PRIMARY KEY (id),
    CONSTRAINT unique_hvac_configuration_name UNIQUE (name) 
);

ALTER TABLE hvac_configuration OWNER TO postgres;

CREATE SEQUENCE hvac_configuration_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE hvac_configuration_seq OWNER TO postgres;

INSERT INTO users (id, email, "password", role_id, status) VALUES (nextval('user_seq'), 'admin', '21232f297a57a5a743894a0e4a801fc3', 1, 'ACTIVE');

INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'Admin');
INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'Auditor');
INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'Employee');
INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'Mobile');
INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'FacilitiesAdmin');
INSERT INTO roles (id, name) VALUES (nextval('role_seq'), 'TenantAdmin');


CREATE TABLE sensor_history
(
  id bigint not null,
  mac_address character varying,
  capture_at timestamp without time zone,
  motion_bits bigint,
  avg_temperature numeric,
  avg_ambient_light integer,
  zero_bucket smallint,
  power_used numeric(19,2),
  base_power_used numeric(19,2),
  CONSTRAINT sensor_history_pk PRIMARY KEY (id),
  CONSTRAINT sensor_history_unique_key UNIQUE (capture_at, mac_address)
);

INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'delay.period.in.seconds', '60');
INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'em.host', 'localhost');
INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'em.username', 'admin');
INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'em.password', 'a1');

INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'enable.temperature', 'false');
INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'enable.motionbits', 'false');
INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'enable.ambient', 'false');
INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'enable.power', 'false');
INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'enable.dimlevel', 'false');
INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'enable.outage', 'false');
INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'enable.timesincelastoccupancy', 'false');
INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'enable.stats.polling', 'false');

INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'em.polling.interval.in.seconds', '300');

INSERT INTO hvac_configuration (id, name, value) values (nextval('hvac_configuration_seq'), 'em.stats.polling.interval.in.seconds', '5');



CREATE OR REPLACE FUNCTION update_recent_sensor_params() RETURNS "trigger" AS $$
  BEGIN
    IF tg_op = 'INSERT' THEN
    	UPDATE sensor set avg_ambient_light = new.avg_ambient_light, avg_temperature = new.avg_temperature where mac_address = new.mac_address;
    END IF;
    RETURN new;
  END
$$ LANGUAGE plpgsql;

CREATE TRIGGER insert_sensor_history AFTER INSERT ON sensor_history
  FOR EACH ROW EXECUTE PROCEDURE update_recent_sensor_params();