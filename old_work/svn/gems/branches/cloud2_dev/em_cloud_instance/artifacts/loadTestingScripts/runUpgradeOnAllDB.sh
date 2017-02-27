#!/bin/bash
# Database details
PGUSER="postgres"

#select list of DB name to iterate on.
dbArray=($(psql -U postgres -A -t -c "SELECT datname FROM pg_database WHERE datistemplate = false and datname != 'postgres';
"))
for db in "${dbArray[@]}"
do
		# run upgade spcript to place  
		psql -U ${PGUSER} -d $db < /home/enlighted/pruneingUpgrade.sql
done