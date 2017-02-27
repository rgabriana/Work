--
-- PostgreSQL adr schema
--

-- Started on 2010-05-28 13:36:34

SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 1937 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'Standard public schema';


--
-- TOC entry 367 (class 2612 OID 16386)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

DROP PROCEDURAL LANGUAGE IF EXISTS plpgsql CASCADE ;
CREATE PROCEDURAL LANGUAGE plpgsql;


SET search_path = public, pg_catalog;

CREATE TABLE adr_target
(
  id bigint NOT NULL,
  dr_identifier character varying(255) NOT NULL,
  dr_status character varying(63),
  operation_mode_value character varying(63),
  start_time timestamp without time zone,
  end_time timestamp without time zone,
  load_amount double precision,
  price_absolute double precision,
  price_relative double precision,
  CONSTRAINT dr_target_pkey PRIMARY KEY (id)
);

ALTER TABLE adr_target OWNER TO postgres;

CREATE SEQUENCE adr_target_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999999999
  START 1
  CACHE 1;

ALTER TABLE adr_target_seq OWNER TO postgres;

CREATE INDEX dr_identifier_index ON adr_target USING btree (lower(dr_identifier::text));


create table adr_configuration (
  id bigint NOT NULL,
  name character varying,
  val character varying,
  CONSTRAINT adr_configuration_pk PRIMARY KEY (id),  
  CONSTRAINT unique_adr_configuration_name UNIQUE (name)
);

ALTER TABLE adr_configuration OWNER TO postgres;

CREATE TABLE dr_event (
    id bigint NOT NULL,
    event_id character varying,
    modification_number bigint,
    priority bigint,
    market_context character varying,
    test_event character varying,
    vtn_comment character varying,
    event_status character varying,
    creation_date_time timestamp without time zone,
    start_date_time timestamp without time zone,
    event_duration character varying,
    start_after character varying,
    notification_duration character varying,
    ramp_up_duration character varying,
    recovery_duration character varying,
    opt_type character varying,
    vtn_id character varying,
    request_id character varying,
    CONSTRAINT dr_event_pk PRIMARY KEY (id)
);

Alter table dr_event add constraint unique_event_id UNIQUE (event_id);

ALTER TABLE dr_event OWNER TO postgres;


CREATE TABLE dr_event_signal (
    id bigint NOT NULL,
    dr_event_id bigint not null,
    signal_name character varying,
    signal_id character varying,
    signal_type character varying,
    current_payload_value double precision,
    CONSTRAINT dr_event_signal_pk PRIMARY KEY (id),
    CONSTRAINT fk_dr_event_signal_dr_event FOREIGN KEY (dr_event_id) REFERENCES dr_event (id)
);

ALTER TABLE dr_event_signal OWNER TO postgres;

CREATE TABLE dr_event_signal_interval (
    id bigint NOT NULL,
    dr_event_signal_id bigint not null,
    interval_duration character varying,
    uid character varying,
    payload_value double precision,
    CONSTRAINT dr_event_signal_interval_pk PRIMARY KEY (id),
    CONSTRAINT fk_dr_event_signal_interval_dr_event_signal FOREIGN KEY (dr_event_signal_id) REFERENCES dr_event_signal (id)
);

ALTER TABLE dr_event_signal_interval OWNER TO postgres;

CREATE SEQUENCE dr_event_signal_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999999999
  START 1
  CACHE 1;

ALTER TABLE dr_event_signal_seq OWNER TO postgres;

CREATE SEQUENCE dr_event_signal_interval_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 999999999999999999
  START 1
  CACHE 1;

ALTER TABLE dr_event_signal_interval_seq OWNER TO postgres;
