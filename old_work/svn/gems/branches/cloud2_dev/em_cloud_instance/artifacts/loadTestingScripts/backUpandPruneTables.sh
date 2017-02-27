#!/bin/bash
# Database details
PGUSER="postgres"
USER="enlighted"
sqlFileName=/home/enlighted/energyDataBackup/

# run the upgradesql so that eacch database have our pruning procedure
. /home/enlighted/scripts/runPruneBackUpUpgradeOnAllDB.sh

sleep 10 

#select list of DB name to iterate on.
dbArray=($(psql -U postgres -A -t -c "SELECT datname FROM pg_database WHERE datistemplate = false and datname != 'postgres';
"))

#make sure you have ran runUpgradeOnAllDB.sh to make procedures part of bd schema. 
# place procedure name with space as delimiter, these tables will be pruned. usage backUpAndPruneTable(table_name , no_of_days , Column_Name)
array=( "backUpAndPruneTable('energy_consumption' ,90 , 'capture_at')"  "backUpAndPruneTable('energy_consumption_hourly' ,90 , 'capture_at')"  "backUpAndPruneTable('energy_consumption_daily' ,90 , 'capture_at')")
		
for db in "${dbArray[@]}"
do
		for i in "${array[@]}"
		do
	lll		echo "---------------------starting back up and pruneing for $db--------------------------------------------"
			table_name=($(psql -U ${PGUSER} -d $db -At -c "select $i ;" ))
			#sleep 10
			if test -n "$table_name"; then
				if [ ! -d $sqlFileName$db/ ]; then
				    echo "---------------------Creating folder for new DB $db-----------------"
					mkdir -p $sqlFileName$db/
					chown -R ${USER}:${USER} $sqlFileName
				fi
				echo "---------------------Taking dump for $table_name---------------------"	
				pg_dump -U ${PGUSER} $db  -F c -t $table_name -f '/tmp/'$table_name.sql ;
				#sleep 10
				#move file from temporary place to different place
				mv /tmp/$table_name.sql $sqlFileName$db/$table_name.sql
				chown  ${USER}:${USER} $sqlFileName$db/$table_name.sql
				chmod 777 $sqlFileName$db/$table_name.sql
				#sleep 10
				echo "---------------------cleaning up------------------------------------------"	
				psql -U ${PGUSER} -d $db -c "drop table $table_name;"
			fi		
		done
done
#Replace the remote host name and do rsync to backupserver make sure you have proper folder present on back up server
 rsync -e "ssh -i /home/enlighted/.ssh/id_rsa" -avz $sqlFileName enlighted@us-tx-m-d-6854f58317c9.enlightedcloud.net:$sqlFileName
 sleep 10
#delete the dir on local server as you do not need it. 
#rm -rf $sqlFileName
