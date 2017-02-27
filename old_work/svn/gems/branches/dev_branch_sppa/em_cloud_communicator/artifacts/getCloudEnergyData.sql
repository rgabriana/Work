CREATE OR REPLACE FUNCTION getCloudEnergyData(fromId bigint , emInstance bigint) RETURNS VOID  AS $$
DECLARE 
    output varchar;
BEGIN

EXECUTE 'COPY (SELECT id, min_temperature, max_temperature, avg_temperature, light_on_seconds, 
       light_min_level, light_max_level, light_avg_level, light_on, 
       light_off, power_used, occ_in, occ_out, occ_count, dim_percentage, 
       dim_offset, bright_percentage, bright_offset, capture_at, fixture_id, 
       price, "cost", base_power_used, base_cost, saved_power_used, 
       saved_cost, occ_saving, tuneup_saving, ambient_saving, manual_saving, 
       zero_bucket, avg_volts, curr_state, motion_bits, power_calc, 
       energy_cum, energy_calib, min_volts, max_volts, energy_ticks, 
       last_volts, saving_type, cu_status, last_temperature, '||$2 ||'AS em_instance_id  from energy_consumption where id > ' || $1 || ' order by id) TO ''/home/enlighted/clouddata/final_cloud_energy_consumption'' with delimiter ''~'' '; 

END;
$$ LANGUAGE plpgsql;

