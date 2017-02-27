#!/bin/bash

BACKUP_DIR="/opt/enLighted/DB/DBBK/"
BACKUP_FILE="daily_ec_dump.backup"
# Database details
PGHOST="localhost"
PGUSER="postgres"
PGDATABASE="ems"

cd $BACKUP_DIR

if [ -f $BACKUP_FILE ] 
then
    backupfile=$(find -name $BACKUP_FILE -mtime +1)
    if [[ ! $backupfile =~ $BACKUP_FILE ]]
    then
        exit
    fi
fi

/usr/bin/pg_dump -i -U $PGUSER -h $PGHOST -F c -a -t energy_consumption -t energy_consumption_hourly -t energy_consumption_daily -f "temp.backup" $PGDATABASE


if [ $? -eq 0 ]
then
    mv "temp.backup" "$BACKUP_FILE"
fi

if [ -f "temp.backup" ]
then
    rm -f temp.backup
fi



