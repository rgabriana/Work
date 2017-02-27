--
-- PostgreSQL database dump
--

-- Started on 2010-05-28 13:36:34

SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

CREATE OR REPLACE FUNCTION general_wal_trigger() RETURNS trigger AS $$
switch $TG_op {
INSERT {

    set isFirst 0
    set statement "insert into "
    append statement "$TG_table_name "
    set col_names "("
    set col_values "("

    foreach field $TG_relatts {
        if { [string length $field] != 0 } {
            set isNull 0
            set field_val [lindex [array get NEW $field] 1]
            if { [string length $field_val] == 0 } {
                set isNull 1
            }

            if { $isFirst == 0 } {
                set isFirst 1
            } else {
                append col_names ", "
                append col_values ", "
            }
            append col_names "$field"
            if { $isNull == 0 } {
                append col_values {E''}
                regsub -all {['\\]} $field_val {&&&&} field_val
                append col_values "$field_val"
                append col_values {''}
            } else {
                append col_values "null"
            }
        }                     
    }
    append statement "$col_names" ") values "
    append statement "$col_values" ");"
    spi_exec "insert into wal_logs (id, creation_time, action, table_name, record_id, sql_statement) values (nextval('wal_logs_seq'), current_timestamp, 'INSERT', '$TG_table_name' , $NEW(id), E'$statement' )"
}
UPDATE {
    set isFirst 0
    set statement "update "
    append statement "$TG_table_name"
    append statement " set "
    foreach field $TG_relatts {
        if { [string length $field] != 0 } {
            set isNull 0
            set field_val [lindex [array get NEW $field] 1]
            if { [string length $field_val] == 0 } {
                set isNull 1
            }

            if { $isFirst == 0 } {
                set isFirst 1
            } else {
                append statement ", "
            }
            append statement "$field" " = " 
            if { $isNull == 0 } {
                append statement {E''}
                regsub -all {['\\]} $field_val {&&&&} field_val
                append statement "$field_val"
                append statement {''}
            } else {
                append statement "null"
            }                     
        }
    }
    append statement " where id = $NEW(id) ;"
    spi_exec "insert into wal_logs (id, creation_time, action, table_name, record_id, sql_statement) values (nextval('wal_logs_seq'), current_timestamp, 'UPDATE', '$TG_table_name' , $NEW(id), E'$statement' )"
}
DELETE {
    set statement "delete from "
    append statement "$TG_table_name" " where id = " "$OLD(id)" ";"
    spi_exec "insert into wal_logs (id, creation_time, action, table_name, record_id, sql_statement) values (nextval('wal_logs_seq'), current_timestamp, 'DELETE', '$TG_table_name' , $OLD(id), E'$statement' )"
}
}
return OK
$$ LANGUAGE pltcl;

CREATE OR REPLACE FUNCTION addTriggers() RETURNS character varying
AS $$
DECLARE 
    output character varying;
    rec character varying;
BEGIN	
    output := '';
    FOR rec IN (SELECT table_name FROM information_schema.tables WHERE table_schema='public')
    LOOP
       if rec != 'energy_consumption' AND rec != 'energy_consumption_hourly' AND rec != 'energy_consumption_daily' AND rec != 'wal_logs' then
        	EXECUTE  'DROP TRIGGER IF EXISTS ' || rec || '_wal_trigger' || ' ON ' ||    rec   ;
            EXECUTE 'CREATE TRIGGER ' || rec || '_wal_trigger' || ' AFTER INSERT OR UPDATE OR DELETE ON ' ||    rec::regclass || ' FOR EACH ROW EXECUTE PROCEDURE general_wal_trigger()';
        end if;
        if rec = 'energy_consumption' OR rec = 'energy_consumption_hourly' OR rec = 'energy_consumption_daily' then
        EXECUTE  'DROP TRIGGER IF EXISTS ' || rec || '_wal_trigger' || ' ON ' ||    rec ;
        EXECUTE 'CREATE TRIGGER ' || rec || '_wal_trigger' || ' AFTER INSERT OR UPDATE ON ' ||    rec::regclass || ' FOR EACH ROW EXECUTE PROCEDURE general_wal_trigger()';
        end if;
    END LOOP;
    RETURN output;   
END;
$$ LANGUAGE plpgsql; 

select addTriggers();


CREATE TABLE wal_logs
(
  id bigint NOT NULL,
  creation_time timestamp without time zone,
  action character varying,
  table_name character varying,
  record_id bigint,
  sql_statement character varying,
  CONSTRAINT wal_logs_pk PRIMARY KEY (id)
);

ALTER TABLE wal_logs OWNER TO postgres;

CREATE SEQUENCE wal_logs_seq
  INCREMENT 1
  MINVALUE 1
  NO MAXVALUE
  START 1
  CACHE 1;
  
ALTER TABLE wal_logs_seq OWNER TO postgres;