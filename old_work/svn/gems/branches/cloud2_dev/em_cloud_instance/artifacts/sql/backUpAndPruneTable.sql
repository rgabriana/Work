CREATE PROCEDURAL LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION backUpAndPruneTable( table_name character varying , days_data_to_keep numeric , column_name character varying) RETURNS VARCHAR
    AS $$
DECLARE 
	no_days numeric;
	no_days_text text;
	no_days_time timestamp;	
	from_timeStamp timestamp ;
	tm timestamp = now();
	new_table_name character varying;
BEGIN
	no_days= days_data_to_keep;
	no_days_text = no_days || ' day';
	no_days_time = tm - no_days_text::interval;
	EXECUTE 'SELECT min('||column_name||') from '|| table_name||';'  INTO from_timeStamp ;
	new_table_name = table_name||'_from_'||to_char(from_timeStamp,'DD_Mon_YYYY')||'_to_'||to_char(no_days_time,'DD_Mon_YYYY');
	EXECUTE 'CREATE TABLE '||new_table_name|| ' AS SELECT * FROM '||table_name||' where '||column_name::text|| ' < '||chr(39)|| no_days_time ||chr(39)||' ;' ;
	EXECUTE 'DELETE FROM '||table_name||' WHERE '|| column_name||' < '||chr(39)||no_days_time::text||chr(39)||' ;';
	RETURN new_table_name ;
END;
$$
LANGUAGE plpgsql;

ALTER FUNCTION public.backUpAndPruneTable(table_name character varying , days_data_to_keep numeric , column_name character varying) OWNER TO postgres;

