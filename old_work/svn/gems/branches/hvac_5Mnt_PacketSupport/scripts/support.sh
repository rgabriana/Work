#!/bin/bash

####################################################
#Database dump, logs directory and version file are
#archived, compressed in the Enlighted/support
#directory which then can be exported to the GUI
####################################################

# Database details
PGHOST="localhost"
PGUSER="postgres"
PGDATABASE="ems"

TOMCAT_PATH=/var/lib/tomcat6

current_time=`date '+%m%d%Y%H%M%S'`
echo "Current time = $current_time"

cd $TOMCAT_PATH/Enlighted/support
mkdir $current_time
cd $current_time

#backup the logs directory
echo "backing up the logs directory..."
tar zcvf logs.tar $TOMCAT_PATH/logs/*

#backup the database
echo "backing up the database..."
/usr/bin/pg_dump -i -U $PGUSER -h $PGHOST -b -f "database_backup.sql" $PGDATABASE

#backup version file
cp -f $TOMCAT_PATH/webapps/ems/META-INF/EMSMANIFEST.MF .

cd ..
echo "archiving/compressing the files..."
tar zcvf enLighted_support_$current_time.tar $current_time/*

gzip enLighted_support_$current_time.tar

cd $TOMCAT_PATH/Enlighted/support
rm -rf $current_time

echo "finished generating troubleshooting file enLighted_support_$current_time.tar.gz"

