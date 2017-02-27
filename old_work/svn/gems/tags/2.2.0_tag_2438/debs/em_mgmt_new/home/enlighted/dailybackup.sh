#!/bin/bash

BACKUP_DIR="/opt/enLighted/DB/DBBK"
EC_DAILY_BACKUP_FILE="daily_ec_dump.backup"
EC_DAILY_SCHEMA_BACKUP_FILE="ec_schema_dump.backup"
DAILY_BACKUP_PREFIX="daily_dump"
TOMCAT_PATH="/var/lib/tomcat6/webapps"
today=$(date '+%m'-'%d'-'%Y'_'%H'-'%M')
DAILY_BACKUP_FILE="${DAILY_BACKUP_PREFIX}_$today.backup"
DAILY_BACKUP_TAR="${DAILY_BACKUP_PREFIX}.temptargz"

# Database details
PGHOST="localhost"
PGUSER="postgres"
PGDATABASE="ems"


cd $BACKUP_DIR

if [ -f $DAILY_BACKUP_TAR ] 
then
    backupfile=$(find -name $DAILY_BACKUP_TAR -mtime -1)
    if [[ "$backupfile" =~ "$DAILY_BACKUP_TAR" ]]
    then
        exit
    fi
fi

/usr/bin/pg_dump -i -U $PGUSER -h $PGHOST -F c -a -t energy_consumption -t energy_consumption_hourly -t energy_consumption_daily -f "temp.backup" $PGDATABASE


if [ $? -eq 0 ]
then
    mv "temp.backup" "$EC_DAILY_BACKUP_FILE"
else
    exit
fi

if [ -f "temp.backup" ]
then
    rm -f temp.backup
fi


/usr/bin/pg_dump -i -U $PGUSER -h $PGHOST -F c -s -t energy_consumption -t energy_consumption_hourly -t energy_consumption_daily -f  "$EC_DAILY_SCHEMA_BACKUP_FILE" $PGDATABASE
/usr/bin/pg_dump -i -U $PGUSER -h $PGHOST -b -F c -T energy_consumption -T energy_consumption_hourly -T energy_consumption_daily -f  "${DAILY_BACKUP_FILE}" $PGDATABASE


if [ $? -eq 0 ]
then
    manifestfile=$TOMCAT_PATH/ems/META-INF/MANIFEST.MF
    if [ -f $manifestfile ]
    then
        cp $manifestfile ./
        tar -zcvf temp.tar.gz $DAILY_BACKUP_FILE $EC_DAILY_BACKUP_FILE MANIFEST.MF $EC_DAILY_SCHEMA_BACKUP_FILE
    else
        tar -zcvf temp.tar.gz $DAILY_BACKUP_FILE $EC_DAILY_BACKUP_FILE $EC_DAILY_SCHEMA_BACKUP_FILE
	fi
    if [ $? -eq 0 ]
    then
        mv "temp.tar.gz" "$DAILY_BACKUP_TAR"
    else
        rm -f $DAILY_BACKUP_FILE
        rm -f $EC_DAILY_SCHEMA_BACKUP_FILE
        exit
    fi

    if [ -f "temp.tar.gz" ]
    then
        rm -f temp.tar.gz
    fi
else
    rm -f $DAILY_BACKUP_FILE
    rm -f $EC_DAILY_SCHEMA_BACKUP_FILE
    exit
fi

rm -f $DAILY_BACKUP_FILE
rm -f $EC_DAILY_SCHEMA_BACKUP_FILE
rm -f MANIFEST.MF

USBMEDIA=$(df -k | grep media | grep 002A  | tr -s ' ' ' '| cut -f6 -d' ' | head -1)
SPACE_AVAILABLE=$(df -B 1 | grep "${USBMEDIA}" | tr -s ' ' ' ' | cut -d" " -f4)
BACKUP_FILE_SIZE=$(ls -l "${DAILY_BACKUP_TAR}"  | tr -s ' ' ' ' | cut -d" " -f5)

outputdir=""
if [ -d "${USBMEDIA}" ]
then
    outputdir="$USBMEDIA"
fi

if [ -d "$USBMEDIA/dbbackup/" ]
then
    outputdir="$USBMEDIA/dbbackup"
fi 

#echo $USBMEDIA
#echo $SPACE_AVAILABLE
#echo $BACKUP_FILE_SIZE

if [ -d "${USBMEDIA}/dbbackup" ]
then
    while [ "${BACKUP_FILE_SIZE}" -ge "${SPACE_AVAILABLE}" ]
    do
        REMOVE_FILE=$(ls -ltr --time-style="+%Y-%m-%d %H:%M:%S" "${USBMEDIA}/dbbackup" | grep "tar.gz" | head -n 1 | tr -s ' ' ' ' | cut -d" " -f8)
        if [ ! -z $REMOVE_FILE ]
        then
            rm -f $USBMEDIA/dbbackup/"${REMOVE_FILE}"
            SPACE_AVAILABLE=$(df -B 1 | grep "${USBMEDIA}" | tr -s ' ' ' ' | cut -d" " -f4)
            #echo $SPACE_AVAILABLE
        else
            break
        fi
    done
fi

if [ -d "${USBMEDIA}" ]
then
    while [ "${BACKUP_FILE_SIZE}" -ge "${SPACE_AVAILABLE}" ]
    do
        REMOVE_FILE=$(ls -ltr --time-style="+%Y-%m-%d %H:%M:%S" "${USBMEDIA}" | grep "tar.gz" | head -n 1 | tr -s ' ' ' ' | cut -d" " -f8)
        if [ ! -z $REMOVE_FILE ]
        then
            rm -f $USBMEDIA/"${REMOVE_FILE}"
            SPACE_AVAILABLE=$(df -B 1 | grep "${USBMEDIA}" | tr -s ' ' ' ' | cut -d" " -f4)
            #echo $SPACE_AVAILABLE
        else
            outputdir=""
            break
        fi
    done
fi

if [ -d "${outputdir}" ]
then
    cp $DAILY_BACKUP_TAR $outputdir/"${DAILY_BACKUP_PREFIX}_$today.tar.gz"
else
    backups_no=$(ls -l $DAILY_BACKUP_PREFIX*.tar.gz | wc -l )
    while [ "${backups_no}" -gt 4 ]
    do
        REMOVE_FILE=$(ls -ltr --time-style="+%Y-%m-%d %H:%M:%S" "$BACKUP_DIR" | grep $DAILY_BACKUP_PREFIX.*tar.gz | head -n 1 | tr -s ' ' ' ' | cut -d" " -f8)
        if [ ! -z $REMOVE_FILE ]
        then
            rm -f $BACKUP_DIR/"${REMOVE_FILE}"
            backups_no=$(ls -l $DAILY_BACKUP_PREFIX*.tar.gz | wc -l )
        else
            break
        fi
    done
    cp $DAILY_BACKUP_TAR $BACKUP_DIR/"${DAILY_BACKUP_PREFIX}_$today.tar.gz"
fi


