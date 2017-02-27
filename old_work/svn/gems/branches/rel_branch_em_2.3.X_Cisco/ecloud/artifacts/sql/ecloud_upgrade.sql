DROP PROCEDURAL LANGUAGE IF EXISTS plpgsql CASCADE ;
CREATE PROCEDURAL LANGUAGE plpgsql;

alter table em_instance alter COLUMN  name drop NOT NULL;
alter table em_instance alter COLUMN  customer_id drop NOT NULL;
alter table em_instance add column last_connectivity_at timestamp without time zone;
alter table em_instance add column active boolean default false;
alter table em_instance add column time_zone character varying(255) default 'UTC';
alter table users add column salt character varying(255) NOT NULL default 'randomsalt';

update em_instance set active = true where active is null;
update users set (password, salt) = ('7c0345648dfb5b05cf5bedcf2c14b37f63f79421','randomsalt') where email = 'admin';

alter table em_instance add column contact_name character varying(255);
alter table em_instance add column contact_email character varying(255);
alter table em_instance add column address character varying(255);
alter table em_instance add column contact_phone character varying(255);
alter table em_instance add column latest_em_state_id bigint ;

alter table em_state alter COLUMN database_status TYPE character varying(255);

alter table em_instance add column sppa_enabled boolean default false;

update em_instance set sppa_enabled = false where sppa_enabled is null;

CREATE SEQUENCE replica_server_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.replica_server_seq OWNER TO postgres;


CREATE TABLE replica_server (
    id bigint NOT NULL,
    ip character varying NOT NULL
);


ALTER TABLE public.replica_server OWNER TO postgres;

ALTER TABLE ONLY replica_server
    ADD CONSTRAINT replica_server_pk PRIMARY KEY (id);
    
alter table em_instance add column replica_server_id bigint;
ALTER TABLE ONLY em_instance
ADD CONSTRAINT em_instance_replica_server_fk FOREIGN KEY (replica_server_id) REFERENCES replica_server(id);
    
CREATE TABLE upgrades
(
  id bigint NOT NULL,
  "location" character varying(255) NOT NULL,
  "name" character varying(255) NOT NULL,
  "type" character varying(255) NOT NULL,
  CONSTRAINT upgrades_pkey PRIMARY KEY (id)
);

alter table replica_server add column name character varying(255);
alter table replica_server add column uid character varying(255);
alter table replica_server add column internal_ip character varying(255);
alter table replica_server add column mac_id character varying(255);

update replica_server set mac_id = uid where mac_id is null;

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
    
ALTER TABLE em_state  RENAME COLUMN number_of_attempts TO failed_attempts ;
    
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

alter table em_instance add column sppa_price double precision default 0.08;

alter table customer add column sppa_price double precision default 0.08;

alter table em_instance add column cert_start_date timestamp without time zone;

alter table em_instance add column cert_end_date timestamp without time zone;

--04/26 sree

alter table em_instance add column tax_rate double precision default 8.25;

alter table em_instance add column block_purchase_energy numeric(19,2);

alter table em_instance add column block_energy_remaining numeric(19,2);

alter table sppa_bill add column tax double precision;

alter table sppa_bill add column block_energy_remaining numeric(19,2);

alter table sppa_bill add column block_term_remaining bigint;

alter table em_instance add column sppa_bill_enabled boolean default false;

update em_instance set sppa_bill_enabled = 'FALSE' where sppa_bill_enabled is null;
