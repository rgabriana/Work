#!/bin/bash 
###########################################
# Title: Database_pickup.sh
# Author: Rolando Gabriana
# usage: 
# database_pickup.sh <mac id>||<mac_id list file>
###################################################
#set -e
# Check to see if the input is a file or a string
DB_list=$(mktemp)

read -p  "Please provide a mac address (XX:XX:XX:XX:XX:XX)  " macid

  psql -Upostgres emscloud --host=db1 --tuples-only --no-align --command="
    SELECT
      em_instance.mac_id, 
      em_instance.database_name, 
      replica_server.ip,
      em_instance.address,
      em_instance.name 
    FROM
      em_instance
    LEFT JOIN 
      replica_server on 
      em_instance.replica_server_id=replica_server.id
    WHERE
      em_instance.mac_id like '$macid' AND
      em_instance.replica_server_id=replica_server.id
    ;
    " > $DB_list
echo "Please verify the following info is correct"
echo "MAC ID	|  Database Name  |	Replica Server		|	Address		|	Name"
cat $DB_list
read -p "is this accurate? (yes/no) " q

if [ $q != "yes" ]; then
  echo "please verify your mac address and try again good bye"
  exit
fi 

mac_id=`awk -F "|" '{print $1}' $DB_list`
db_name=`awk -F "|" '{print $2}' $DB_list`
replica=`awk -F "|" '{print $3}' $DB_list`
name=`awk -F "|" '{print $5}' $DB_list`
location=/home/enlighted/backups
target=/home/sales_eng/database_pickup/$mac_id/$db_name/

mkdir -p $target 

scp $replica:$location/$db_name/* $target

rm $DB_list

echo "Your database is ready for you at $target"

exit 0 
