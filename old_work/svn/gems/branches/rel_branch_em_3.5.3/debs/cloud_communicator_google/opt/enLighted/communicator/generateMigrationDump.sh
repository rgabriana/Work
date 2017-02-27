#!/bin/bash 
source /etc/environment
pg_dump -U postgres ems -F c -v -b  -T 'energy_consumption' -T 'energy_consumption_daily' -T 'energy_consumption_hourly' -T 'em_motion_bits' -T 'plugload_energy_consumption' -T 'plugload_energy_consumption_daily' -T 'plugload_energy_consumption_hourly' -f "$ENLIGHTED_HOME/clouddata/sppa_migration.sql" ;
pg_dump -U postgres ems -F c -s -t 'energy_consumption' -t 'energy_consumption_daily' -t 'energy_consumption_hourly' -t 'em_motion_bits' -t 'plugload_energy_consumption' -t 'plugload_energy_consumption_daily' -t 'plugload_energy_consumption_hourly' -f "$ENLIGHTED_HOME/clouddata/sppa_migration_energy.sql" ;
