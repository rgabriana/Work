create language pltcl;

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
       if rec != 'energy_consumption' AND rec != 'energy_consumption_hourly' AND rec != 'energy_consumption_daily' AND rec != 'wal_logs' AND rec != 'cloud_config' then
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

CREATE TABLE cloud_config (
  id bigint NOT NULL,
  name character varying,
  val character varying,
  CONSTRAINT cloud_config_pk PRIMARY KEY (id),  
  CONSTRAINT unique_cloud_config_name UNIQUE (name)
);

ALTER TABLE cloud_config OWNER TO postgres;

insert into cloud_config (id, name, val) values (1, 'lastWalSyncId', '-99');

--12/26/2012 Sree
ALTER TABLE event_type ADD COLUMN severity smallint;
ALTER TABLE event_type ADD COLUMN active smallint;

UPDATE event_type SET severity = 2, active = 1 WHERE type = 'Fixture Out';
UPDATE event_type SET severity = 5, active = 0 WHERE type = 'Push Profile';
UPDATE event_type SET severity = 4, active = 0 WHERE type = 'Profile Mismatch';
UPDATE event_type SET severity = 3, active = 1 WHERE type = 'Bad Profile';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Fixture Upgrade';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Gateway Upgrade';
UPDATE event_type SET severity = 2, active = 1 WHERE type = 'Fixture CU Failure';
UPDATE event_type SET severity = 3, active = 1 WHERE type = 'Fixture Image Checksum Failure';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'DR Condition';
UPDATE event_type SET severity = 5, active = 0 WHERE type = 'Fixture associated Group Changed';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Bacnet';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Discovery';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'Commissioning';
UPDATE event_type SET severity = 4, active = 0 WHERE type = 'Profile Mismatch User Action';
UPDATE event_type SET severity = 2, active = 1 WHERE type = 'Fixture Hardware Failure';
UPDATE event_type SET severity = 3, active = 1 WHERE type = 'Fixture Too Hot';
UPDATE event_type SET severity = 3, active = 1 WHERE type = 'Fixture CPU Usage is High';
UPDATE event_type SET severity = 2, active = 1 WHERE type = 'Gateway configuration error';
UPDATE event_type SET severity = 3, active = 1 WHERE type = 'Erroneous Energy Reading';
UPDATE event_type SET severity = 2, active = 1 WHERE type = 'Gateway Connection Failure';
UPDATE event_type SET severity = 5, active = 1 WHERE type = 'EM upgrade';
UPDATE event_type SET severity = 5, active = 0 WHERE type = 'Scheduler';

