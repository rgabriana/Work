insert into cloud_config values((select max(id)+1 from cloud_config),'plugload_ecd_last_capture_at',-1);
CREATE OR REPLACE FUNCTION cloud_config_update_plugload_ecd_last_capture_at() RETURNS trigger AS 
$BODY$
DECLARE
cc_date integer;
new_capture_at integer ;
BEGIN
	select val::integer into cc_date from cloud_config where name = 'plugload_ecd_last_capture_at';
        new_capture_at=to_char(new.capture_at,'YYYYMMDD')::integer;
	IF new_capture_at < cc_date THEN
	  update cloud_config set val = new_capture_at  where name = 'plugload_ecd_last_capture_at';
	END IF;

 	RETURN new;
END
$BODY$
LANGUAGE plpgsql VOLATILE;

create trigger plugload_ecd_cc_update after insert or update on plugload_energy_consumption_daily for each row execute procedure cloud_config_update_plugload_ecd_last_capture_at();
