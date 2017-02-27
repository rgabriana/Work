#!/bin/bash
# Database details
PGUSER="postgres"
PGDATABASE="emscloud"

array=( "pruneemstats()" )
for i in "${array[@]}"
do
	psql -U ${PGUSER} -d ${PGDATABASE} -c "select $i ;" 
done
