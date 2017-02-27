#!/bin/bash
# Database details
PGUSER="postgres"

#select list of DB name to iterate on.
dbArray=($(psql -U postgres -A -t -c "SELECT datname FROM pg_database WHERE datistemplate = false and datname != 'postgres';
"))

for db in "${dbArray[@]}"
do
	#make sure you have ran runUpgradeOnAllDB.sh to make procedures part of bd schema. 
    # place procedure name with space as delimiter,
		array=( "pruneenergy()"  "prunehourlyenergy()"  "prunedailyenergy()")
		for i in "${array[@]}"
		do
			psql -U ${PGUSER} -d $db -c "select $i ;" 
		done
done
