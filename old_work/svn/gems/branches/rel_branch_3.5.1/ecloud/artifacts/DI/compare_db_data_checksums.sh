#!/bin/sh

em_db_name=ems
rp_db_name=$1

#Check and create test directory

if [ $1 != "" ]; then
  test_directory=db_compare_test_$rp_db_name
else
  test_directory=db_compare_test_ems
fi

if [ -d "$test_directory" ]; then
	echo "Deleting test directory"
	sudo rm -rf $test_directory
fi

echo "Creating test directory"
mkdir $test_directory
chmod 777 $test_directory

cd $test_directory

current_dir_path=$(pwd)
echo "$current_dir_path"

if [ $1 != " " ]
then

  #Create replica tables csv
  echo "Creating Replica DB table list"
  psql -U postgres $rp_db_name -t -c "COPY (SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name NOT IN ('energy_consumption','energy_consumption_daily','energy_consumption_hourly') ORDER BY table_name) TO '$current_dir_path/rp_table_list.csv' WITH CSV;"

  echo "Creating Replica table csv"
  while read line; do
       echo "$line";
	psql -U postgres $rp_db_name -t -c "COPY (SELECT * FROM $line ORDER BY  id ASC) TO '$current_dir_path/em_$line.csv' WITH CSV;"
  done < $current_dir_path/rp_table_list.csv

else

  #Create em tables csv
  echo "Creating EM DB table list"
  psql -U postgres $em_db_name -t -c "COPY (SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name NOT IN ('energy_consumption','energy_consumption_daily','energy_consumption_hourly') ORDER BY table_name) TO '$current_dir_path/em_table_list.csv' WITH CSV;"

  echo "Creating EM table csv"
  while read line; do 
	echo "$line";
	psql -U postgres $em_db_name -t -c "COPY (SELECT * FROM $line ORDER BY  id ASC) TO '$current_dir_path/em_$line.csv' WITH CSV;"
  done < $current_dir_path/em_table_list.csv
fi


