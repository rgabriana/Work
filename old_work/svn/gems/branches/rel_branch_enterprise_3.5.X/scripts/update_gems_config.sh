#!/bin/bash

sed '/^\#/d' gems.properties > gems_temp.properties
param_name=""
value=""
for line in $(< gems_temp.properties)   
do  
	#echo $line
	param_name=`echo $line | cut -d= -f1`
	#echo $param_name
	value=`echo $line | cut -d= -f2`
	echo "set $param_name to $value"

	echo "UPDATE system_configuration SET value = '$value' WHERE name = '$param_name';" >> db_temp.sql
done   

psql -Upostgres ems < db_temp.sql
rm -rf db_temp.sql
rm -rf gems_temp.properties

