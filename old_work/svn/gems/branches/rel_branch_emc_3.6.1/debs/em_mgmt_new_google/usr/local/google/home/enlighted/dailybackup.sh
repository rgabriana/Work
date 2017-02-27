#!/bin/bash
source /etc/environment

export LOG_DIR="$ENL_APP_HOME/Enlighted/adminlogs"
mkdir -p $LOG_DIR

BACKUP_DIR="$OPT_ENLIGHTED/DB/DBBK"
EC_DAILY_BACKUP_FILE="daily_ec_dump.backup"
EC_DAILY_SCHEMA_BACKUP_FILE="ec_schema_dump.backup"
backupprefix=$($EM_MGMT_BASE/em_mgmt/adminscripts/generate_backup_prefix.sh)
DAILY_BACKUP_PREFIX="daily_dump"
TOMCAT_PATH="$ENL_APP_HOME/webapps"
today=$(date '+%m'-'%d'-'%Y'_'%H'-'%M'-'%S')
today_secs=$(date +%s)
lockfile="dailybackup.pid"

# first check if there is enough disk space, i.e. Disk Space Used is < 85%

DISK_SPACE_FREE_PERCENTAGE=`df -hl $BACKUP_DIR | awk '/^\/dev\/sd[ab]/ { sum+=$5 } END { print sum }'`

if [ $DISK_SPACE_FREE_PERCENTAGE -gt 84 ] 
then
	echo "Disk space is low >= 85% used, backup aborted..."  >> $LOG_DIR/dailybackup.log
	exit
fi 

# disk check complete proceed


echo "">>$LOG_DIR/dailybackup.log
echo "*****starting the dailybackup script" >>$LOG_DIR/dailybackup.log
echo "*****daily backup started at ${today}" >>$LOG_DIR/dailybackup.log


export manifestfile=$TOMCAT_PATH/ems/META-INF/MANIFEST.MF
versionstring1=$(cat $manifestfile | grep Implementation-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
versionstring2=$(cat $manifestfile | grep Build-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
versionstringnumber="$versionstring1-$versionstring2" 
export BACKUP_VERSION=$versionstringnumber

echo "*****Taking a backup with version number ${BACKUP_VERSION}" >>$LOG_DIR/dailybackup.log

DAILY_BACKUP_FILE="${backupprefix}${DAILY_BACKUP_PREFIX}_${BACKUP_VERSION}_$today.backup"
DAILY_BACKUP_TAR="${backupprefix}${DAILY_BACKUP_PREFIX}_${BACKUP_VERSION}.temptargz"

# Database details
PGHOST="localhost"
PGUSER="postgres"
PGDATABASE="ems"

##USB Mount Fix to remove unwanted symlink
if [ -d /media/usb ]
then
    rm -f /media/usb
fi

cd $BACKUP_DIR

if [ ! -f "$lockfile" ]
then
        echo "" > "$lockfile"
else
        dailybackuppid=$(head -n 1 "$lockfile")
        processRunning=$(ps -fp $dailybackuppid)
        if [[ "$processRunning" =~ "$dailybackuppid" ]]
        then
                echo "db daily backup already in progress. exiting." >> $LOG_DIR/dailybackup.log
                exit
        fi
fi
echo $$ > "$lockfile"

if [ -f $DAILY_BACKUP_TAR ] 
then
	
	echo "*****checking for any system generated daily backups in the last 12 hours" >>$LOG_DIR/dailybackup.log
    backupfile=$(find -name $DAILY_BACKUP_TAR -mmin -720)
    if [[ "$backupfile" =~ "$DAILY_BACKUP_TAR" ]]
    then
    	echo "db backup found in the last 12 hours" >>$LOG_DIR/dailybackup.log
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

USBMEDIA=$(df -k | grep media | tr -s ' ' ' '| cut -f6 -d' ' | head -1)
SPACE_AVAILABLE=$(df -B 1 | grep "${USBMEDIA}" | tr -s ' ' ' ' | cut -d" " -f4)
BACKUP_FILE_SIZE=$(ls -l "${DAILY_BACKUP_TAR}"  | tr -s ' ' ' ' | cut -d" " -f5)

outputdir=""

#checking for USBMEDIA. if dbdbackup exists with in USB media then take back up there, else create the folder and then backup 
if [ -d "${USBMEDIA}" ]
then
	echo "*****USB media: ${USBMEDIA} found with free space of ${SPACE_AVAILABLE}" >>$LOG_DIR/dailybackup.log
	echo "*****Backup file size  ${BACKUP_FILE_SIZE}" >>$LOG_DIR/dailybackup.log
	echo "" >>$LOG_DIR/dailybackup.log
	mkdir "${USBMEDIA}/dbbackup"
	outputdir="${USBMEDIA}/dbbackup"    
fi



#echo $USBMEDIA
#echo $SPACE_AVAILABLE
#echo $BACKUP_FILE_SIZE

if [ -d "${USBMEDIA}/dbbackup" ]
then
    while [ "${BACKUP_FILE_SIZE}" -ge "${SPACE_AVAILABLE}" ]
    	do
        echo "*****backup file size greater than space available in USB. Trying to remove the older backups in USB" >>$LOG_DIR/dailybackup.log
        REMOVE_FILE=$(ls -ltr --time-style="+%Y-%m-%d %H:%M:%S" "${USBMEDIA}/dbbackup" | grep "tar.gz" | head -n 1 | tr -s ' ' ' ' | cut -d" " -f8)

        
        
        if [ ! -z $REMOVE_FILE ]
        then
            echo "*****${REMOVE_FILE} is the oldest back up" >>$LOG_DIR/dailybackup.log
            rm -f $USBMEDIA/dbbackup/"${REMOVE_FILE}"
            SPACE_AVAILABLE=$(df -B 1 | grep "${USBMEDIA}" | tr -s ' ' ' ' | cut -d" " -f4)
            echo "*****Amount of space available after removing the oldest back up ${SPACE_AVAILABLE}" >>$LOG_DIR/dailybackup.log
        else
            echo "*****not enough space in USB drive even after deleting the older backup files.Please clear any non-enlighted files " >> $LOG_DIR/dailybackup.log
            echo "*****Back up will be done in the server since there is no space in USB" >> $LOG_DIR/dailybackup.log
            echo "" >>$LOG_DIR/dailybackup.log
            outputdir=""
            break
        fi

    done
fi


if [ -d "${outputdir}" ]
then
    cp $DAILY_BACKUP_TAR $outputdir/"${backupprefix}${DAILY_BACKUP_PREFIX}_${BACKUP_VERSION}_$today.tar.gz"
    echo "*****copied ${backupprefix}${DAILY_BACKUP_PREFIX}_${BACKUP_VERSION}_$today.tar.gz onto ${outputdir}" >>$LOG_DIR/dailybackup.log   
else
    backups_no=$(ls -l *$DAILY_BACKUP_PREFIX*.tar.gz | wc -l )
    while [ "${backups_no}" -gt 2 ]
    do
        REMOVE_FILE=$(ls -ltr --time-style="+%Y-%m-%d %H:%M:%S" "$BACKUP_DIR" | grep .*$DAILY_BACKUP_PREFIX.*tar.gz | head -n 1 | tr -s ' ' ' ' | cut -d" " -f8)
        if [ ! -z $REMOVE_FILE ]
        then
            rm -f $BACKUP_DIR/"${REMOVE_FILE}"
            backups_no=$(ls -l *$DAILY_BACKUP_PREFIX*.tar.gz | wc -l )
        else
            break
        fi
    done
    cp $DAILY_BACKUP_TAR $BACKUP_DIR/"${backupprefix}${DAILY_BACKUP_PREFIX}_${BACKUP_VERSION}_$today.tar.gz"
fi

backup_end_time=$(date '+%m'-'%d'-'%Y'_'%H'-'%M'-'%S')
backup_end_secs=$(date +%s)
echo ${backup_end_time} >>$LOG_DIR/dailybackup.log
difference=$(($backup_end_secs - $today_secs))
difference_hrs=$(($difference / 3600))
difference_min=$((($difference % 3600)/60))
difference_sec=$((($difference % 3600)%60))
echo "*****daily backup started at ${today} , ended at  ${backup_end_time}" >>$LOG_DIR/dailybackup.log
echo "*****backup completed in ${difference_hrs} hours  ${difference_min} minutes and ${difference_sec} seconds">>$LOG_DIR/dailybackup.log
