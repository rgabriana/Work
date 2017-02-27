#!/bin/bash 
source /etc/environment
pg_dump -U postgres ems -F c -v -b  -T 'energy_consumption*'  -f "$ENLIGHTED_HOME/clouddata/sppa_migration.sql" ;
pg_dump -U postgres ems -F c -s -t 'energy_consumption*' -f "$ENLIGHTED_HOME/clouddata/sppa_migration_energy.sql" ;
