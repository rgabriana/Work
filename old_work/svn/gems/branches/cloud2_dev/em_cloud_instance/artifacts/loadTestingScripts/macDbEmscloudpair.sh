#!/bin/bash
#This script is ran on emscloud database
hexchars="0123456789ABCDEF"
pgDbName=150
rm -rf randomMacDB.txt
while [ $pgDbName -le 201 ]
do
end=$( for i in {1..6} ; do echo -n ${hexchars:$(( $RANDOM % 16 )):1} ; done | sed -e 's/\(..\)/:\1/g' )
echo "00:60:2F$end|em_100_$pgDbName"   >> randomMacDB.txt
psql -U postgres -d emscloud -c "insert into em_instance (id,mac_id, database_name)values($pgDbName,'00:60:2F$end','em_100_$pgDbName') ;"
pgDbName=$(($pgDbName + 1))
done
