#!/bin/bash

####################################################
#Database dump, logs directory and version file of
#a particular fixture are archived, commpressed
#in the Enlighted/support directory which then can
#be exported to the GUI
####################################################

if [ $# -lt 1 ] ; then
  echo "Usage: fixture_log.sh fixture_name"
  exit 1
fi

fixture_name=$1
echo "fixture name = $fixture_name"

# Database details
PGUSER="postgres"
PGDATABASE="ems"

TOMCAT_PATH=/var/lib/tomcat6

current_time=`date '+%m%d%Y%H%M%S'`
echo "Current time = $current_time"

cd $TOMCAT_PATH/Enlighted/support
mkdir $current_time
cd $current_time

#retrieve the fixture id
query="select id from fixture where fixture_name='"$fixture_name"'"
fixture_id=`psql -X -U$PGUSER -P t $PGDATABASE -c "$query"`;
fixture_id=`echo $fixture_id | tr -d ' '`
echo "fixture id = $fixture_id"

echo "backing up the database for the fixture $fixture_name"
#backup the events data
psql -U$PGUSER $PGDATABASE -c "select * from events_and_fault where fixture_id = $fixture_id" > events_$fixture_id.txt

#backup the fixture data
psql -U$PGUSER $PGDATABASE -c "select * from fixture where id = $fixture_id" > fixture_$fixture_id.txt

#backup the energy consumption data
psql -U$PGUSER $PGDATABASE -c "select * from energy_consumption where fixture_id = $fixture_id" > energy_$fixture_id.txt

#backup the logs directory
echo "backing up the logs directory for fixture $fixture_name ..."
grep " $fixture_id:" $TOMCAT_PATH/logs/* > $fixture_id.log
grep " $fixture_name" $TOMCAT_PATH/logs/* > $fixture_name.log

#backup version file
cp -f $TOMCAT_PATH/webapps/ems/META-INF/EMSMANIFEST.MF .

cd ..
echo "archiving/compressing the files..."
archive_name="enLighted_$fixture_name"_"$current_time".tar
echo $archive_name
#tar zcvf enLighted_$fixture_name_$current_time.tar $current_time/*
tar zcvf $archive_name $current_time/*

#gzip enLighted_$fixture_name_$current_time.tar
gzip $archive_name

#cd $TOMCAT_PATH/Enlighted/support
rm -rf $current_time

echo "finished generating troubleshooting file for fixture $fixture_name"

