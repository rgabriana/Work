#!/bin/bash
# Database details
PGUSER="postgres"
PGDATABASE="emscloud"

# place procedure name with space as delimiter
array=( "pruneemstats()" )
for i in "${array[@]}"
do
	psql -U ${PGUSER} -d ${PGDATABASE} -c "PERFORM $i ;" 
done

