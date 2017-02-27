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
