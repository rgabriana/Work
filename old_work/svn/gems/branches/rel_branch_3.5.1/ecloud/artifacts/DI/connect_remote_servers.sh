#! /bin/bash

# Connecting to EM server
# Command line arguments
# 1-"--checksum OR --data", 2-EM Server IP, 3-Replica Server IP, 4-EM Server Username, 5-Replica Server UserName, 6-EM_Replica_Instance

em_data_dir=db_compare_test_ems
replica_data_dir=db_compare_test_$6
file1="./$em_data_dir/em_table_list.csv"
file2="./$replica_data_dir/rp_table_list.csv"

if [ $1 = "--data" ]
then

  echo ---------Copying compare script on EM Server Enter Password--------------
  scp compare_db_data_checksums.sh $4@$2:/home/$4/
  echo ---------Running Compare Script on EM Server Enter Password---------------
  ssh $4@$2 "cd /home/$4/;pwd;ls -l;rm -r test; mkdir test; chmod 777 test; cd test;cp /home/$4/compare_db_data_checksums.sh .; chmod 777 sh compare_db_data_checksums.sh; sh compare_db_data_checksums.sh; zip -r $em_data_dir.zip $em_data_dir/"
  echo ---------Copying generated checksums and data from EM Server Enter Password---------------
  scp -r $4@$2:/home/$4/test/$em_data_dir.zip .
  unzip $em_data_dir
  # Connecting to Replica server
  echo ---------Copying compare script on Replica Server Enter Password---------------
  scp compare_db_data_checksums.sh $5@$3:/home/$5/
  echo ---------Running Compare Script on Replica Server Enter Password---------------
  ssh $5@$3 "cd /home/$5/;pwd;ls -l;rm -r test; mkdir test; chmod 777 test; cd test;cp /home/$5/compare_db_data_checksums.sh .; chmod 777 sh compare_db_data_checksums.sh; sh compare_db_data_checksums.sh $6; zip -r $replica_data_dir.zip $replica_data_dir/"
  echo ---------Copying generated checksums and data from Replica Server Enter Password---------------
  scp -r $5@$3:/home/$5/test/$replica_data_dir.zip .  
  unzip $replica_data_dir
  #Comparing DB Schemas
  echo ---------------Comparing DB schemas------------------
  DIFF=$(diff $file1 $file2)
  if [ $? != 1 ]    
  then
    echo "DB Schemas are same"
  else
    echo "DB Schemas are different"
  fi
fi




if [ $1 = "--checksum" ]
then
  echo --------------------Checking if the directories containing data files exist--------------------
  if [ -d "$em_data_dir" ] && [ -d "$replica_data_dir" ]
  then
  echo "Data dump available--- Comparing checksums"
  #Delete the testresults directory if it exists
  rm -rf testresults
  mkdir testresults
  chmod 777 testresults
     while read line; do 
        em_file="./$em_data_dir/em_$line.csv"
        replica_file="./$replica_data_dir/em_$line.csv"
        if [ -f $em_file ]
        then
          md5_em_file=`md5sum $em_file | cut -c -32`
          md5_replica_file=`md5sum $replica_file | cut -c -32`
          echo $md5_em_file
          echo $md5_replica_file
          if [ $md5_em_file = $md5_replica_file ]
          then
            echo "$line -> $md5_em_file $md5_replica_file  Md5s match" >> ./testresults/checksum_matches.txt
          else
            echo "$line -> $md5_em_file $md5_replica_file Either one of the files don't exist OR the Md5s dont match" >> ./testresults/checksum_mismatches.txt
          fi
        fi
     done < ./$em_data_dir/em_table_list.csv
  else
  echo "Collect the data dump"
  fi
fi















