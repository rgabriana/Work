#!/bin/bash
export LOG_DIR="/var/lib/tomcat6/Enlighted/adminlogs"
mkdir -p $LOG_DIR
echo "" > $LOG_DIR/backuprestore.log
echo "" > $LOG_DIR/backuprestore_error.log

{
# --------------
# This is a backup restore script to be called by the GUI.
# --------------


# Input parameters
export OPERATION="$1"
export FILENAME=$2
export BKFILEPATH=$3

# TOMCAT INSTALLATION path
export TOMCAT_PATH=$4

LOG_DIR=${TOMCAT_PATH}/../Enlighted/adminlogs
# Working Directory
export WORKINGDIRECTORY="${TOMCAT_PATH}/../Enlighted/bkRestoreFolderTemp"

# Fail over in case usb isn't connected location
export FAIL_OVER_BACKUP_DIR="/opt/enLighted/DB/DBBK"

#Daily backup file name
export DAILY_EC_BACKUP_FILE="daily_ec_dump.backup"

#schema backup file name
export EC_SCHEMA_BACKUP_FILE="ec_schema_dump.backup"

#Drop Energy Consumption Index And Contraint File
export DROPECINDEX="${TOMCAT_PATH}/ems_mgmt/adminscripts/drop_ec_index_and_constraint.sql"

#Add Energy Consumption Index And Contraint File
export ADDECINDEX="${TOMCAT_PATH}/ems_mgmt/adminscripts/add_ec_index_and_constraint.sql"

#upgradeSQL path
export UPGRADE_SQL_PATH="/home/enlighted/upgradeSQL.sql"

# Database details
export POSTGRESHOST="localhost"
export POSTGRESUSER="postgres"
export POSTGRESDATABASE="ems"
export today=$(date '+%m'-'%d'-'%Y'_'%H'-'%M')
export yesterday=$(date --d '1 day ago' '+%m'-'%d'-'%Y')

#Error codes
export DB_BACKUP_FAIL=1
export DB_RESTORE_FAIL=2
export DB_CREATION_FAIL=3
export DB_BACKUPFILE_DELETE_FAIL=4
export SANITY_PASS=5
export SANITY_FAIL=6
export EMS_STOP_RETRY_ATTEMPTS=3

#OPERATION NAMES
export BACKUP_OP="backup"
export DO_RESTORE="dorestore" 


#Application rnning states
RUNNING=running
STOPPED=stopped



# --- Print usage function ---
usage () {

	echo "Usage:"
	echo "$(basename $0) <backup|restore> <filename>"
}

# --- Backup Function ---
backupfunction() {
    
    USBMEDIA=$(df -k| grep media|tr -s ' ' ' '|cut -f6 -d' '|head -1)

    # USB stick location
    BACKUP_DIR="${USBMEDIA}/dbbackup"
    if [ ! -d "$BACKUP_DIR" ]
    then
        BACKUP_DIR="/media/USB1/dbbackup"
    fi
    if [ ! -d "$BACKUP_DIR" ]
    then
        BACKUP_DIR=$FAIL_OVER_BACKUP_DIR
        copy=0
    else
        copy=1
    fi

	backupfile=$1

	cd ${BACKUP_DIR}

    echo "*** Starting database dump ***" >> $LOG_DIR/backuprestore_error.log

    if [ -f $FAIL_OVER_BACKUP_DIR/$DAILY_EC_BACKUP_FILE ]
    then
        echo "*** Copying  energy consumption daily backup to destination folder ***" >> $LOG_DIR/backuprestore_error.log
        if [[ $copy =~ "1" ]] 
        then
            cp $FAIL_OVER_BACKUP_DIR/$DAILY_EC_BACKUP_FILE  ./
        fi
        echo "*** Generating schema and data backup without energy consumption data in destination folder ***" >> $LOG_DIR/backuprestore_error.log
        /usr/bin/pg_dump -i -U $POSTGRESUSER -h $POSTGRESHOST -v -F c -s -t energy_consumption -t energy_consumption_hourly -t energy_consumption_daily -f  "$EC_SCHEMA_BACKUP_FILE" $POSTGRESDATABASE
        /usr/bin/pg_dump -i -U $POSTGRESUSER -h $POSTGRESHOST -v -b -F c -T energy_consumption -T energy_consumption_hourly -T energy_consumption_daily -f  "${BACKUP_DIR}/${backupfile}_$today.backup" $POSTGRESDATABASE

    else
        echo "*** Daily energy consumption backup is not there. Generating complete backup. ***" >> $LOG_DIR/backuprestore_error.log
        /usr/bin/pg_dump -i -U $POSTGRESUSER -h $POSTGRESHOST -F c -s -t energy_consumption -t energy_consumption_hourly -t energy_consumption_daily -f  "$EC_SCHEMA_BACKUP_FILE" $POSTGRESDATABASE
        /usr/bin/pg_dump -i -U $POSTGRESUSER -h $POSTGRESHOST -v -b -F c -f  "${BACKUP_DIR}/${backupfile}_$today.backup" $POSTGRESDATABASE
    fi

	if [ $? -ne 0 ]
	then
        echo "ERROR: Database dump was not successful."
        if [ -f $DAILY_EC_BACKUP_FILE ]
        then
            rm -f $DAILY_EC_BACKUP_FILE
        fi
		return $DB_BACKUP_FAIL
	fi
    echo "step1;"
    echo "*** Database dump successful ***" >> $LOG_DIR/backuprestore_error.log

    echo "*** Getting current implementation version ***" >> $LOG_DIR/backuprestore_error.log
    manifestfile=$TOMCAT_PATH/ems/META-INF/MANIFEST.MF
    if [ ! -f $manifestfile ]
    then
        echo "ERROR: Manifest file, holding current implementation version, is missing."
        rm -f ${backupfile}_$today.backup $EC_SCHEMA_BACKUP_FILE
        if [[ $copy =~ "1" ]] 
        then
            if [ -f $DAILY_EC_BACKUP_FILE ]
            then
                rm -f $DAILY_EC_BACKUP_FILE
            fi
        fi
        return $DB_BACKUP_FAIL
	fi
    cp $manifestfile ${BACKUP_DIR}/
    echo "step2;"

    echo "*** Creating archive file ***" >> $LOG_DIR/backuprestore_error.log
    if [ -f $DAILY_EC_BACKUP_FILE ]
    then
    	tar -zcvf ${backupfile}_$today.tar.gz ${backupfile}_$today.backup $DAILY_EC_BACKUP_FILE MANIFEST.MF $EC_SCHEMA_BACKUP_FILE
    else 
        tar -zcvf ${backupfile}_$today.tar.gz ${backupfile}_$today.backup MANIFEST.MF 
    fi
	tarstatus=$?

	rm -f ${backupfile}_$today.backup $EC_SCHEMA_BACKUP_FILE
    rm -f MANIFEST.MF
    if [[ $copy =~ "1" ]] 
    then
        if [ -f $DAILY_EC_BACKUP_FILE ]
        then
            rm -f $DAILY_EC_BACKUP_FILE
        fi
    fi

	if [ $tarstatus -ne 0 ]
	then
        echo "ERROR: Failed to create an acrhive file."
		return $DB_BACKUP_FAIL
	fi
    echo "step3;"
	return 0	
}

# --- Restore function ---
restore() {
    tarfile=$1
	path=$2
    managerUser="admin"
    managerPass="admin"

	cd $WORKINGDIRECTORY
    echo "*** Starting database restore ***" >> $LOG_DIR/backuprestore_error.log
    echo "*** Getting current implementation version ***" >> $LOG_DIR/backuprestore_error.log

    manifestfile=$TOMCAT_PATH/ems/META-INF/MANIFEST.MF
    if [ ! -f $manifestfile ]
    then
        echo "ERROR: Manifest file, holding current implemenation version, is missing."
        return $DB_RESTORE_FAIL
	fi
    
		
	versiononsystem=$(cat $manifestfile | grep Implementation-Version | sed -re 's/^.+: //')
	
	sudo cp $path/$tarfile .
	
    echo "*** Extracting data from backup file ***" >> $LOG_DIR/backuprestore_error.log
	tar -zvxf $tarfile 
        
	backupfile=$(echo $tarfile | sed "s/.tar.gz/.backup/g")
    if [ ! -f $backupfile ]
    then
        echo "ERROR: $backupfile does not exist. Please take a dump and gzip it with the same name as that of the tar ball name. "
        return $DB_RESTORE_FAIL
    else
		echo "*** $backupfile exists ***" >> $LOG_DIR/backuprestore_error.log
	fi
    
    echo "*** Checking versions ***" >> $LOG_DIR/backuprestore_error.log
	if [ -f MANIFEST.MF ]
	then
		versiononbackupfile=$(cat MANIFEST.MF | grep Implementation-Version | sed -re 's/^.+: //')
		if [ $versiononsystem == $versiononbackupfile ]
		then
			echo "*** Versions are compatible ***" >> $LOG_DIR/backuprestore_error.log
		else
			echo "*** System version: ${versiononsystem} ***" >> $LOG_DIR/backuprestore_error.log
			echo "*** Backup File version: ${versiononbackupfile} ***" >> $LOG_DIR/backuprestore_error.log
            echo "*** Versions are not same. Still Continuing.. ***" >> $LOG_DIR/backuprestore_error.log
		fi
	else
		echo "*** Invalid backup file. Manifest file doesn't exist. But still proceeding. ***"  >> $LOG_DIR/backuprestore_error.log

	fi

    echo "step1;"
    echo "*** Check if EMS is running ***" >> $LOG_DIR/backuprestore_error.log
    managerList=$(wget --no-check-certificate https://$managerUser:$managerPass@localhost/manager/list -O - -q)
    echo "*** ***" >> $LOG_DIR/backuprestore_error.log
    echo $managerList >> $LOG_DIR/backuprestore_error.log
    echo "*** ***" >> $LOG_DIR/backuprestore_error.log
    if [[ $managerList =~ "ems:running" ]]
    then
        echo "*** EMS is running ***" >> $LOG_DIR/backuprestore_error.log
        echo "*** Stopping EMS ***" >> $LOG_DIR/backuprestore_error.log
        stopStatus=$(wget --no-check-certificate https://$managerUser:$managerPass@localhost/manager/stop?path=/ems -O - -q)
        sleep 10
        echo "*** ***" >> $LOG_DIR/backuprestore_error.log
        echo $stopStatus >> $LOG_DIR/backuprestore_error.log
        echo "*** ***" >> $LOG_DIR/backuprestore_error.log

        if [[ $stopStatus =~ "OK" ]]
        then
            echo "*** EMS is down ***" >> $LOG_DIR/backuprestore_error.log
        else
            echo "ERROR: Some problem while stopping ems. Please try again after some time."
            return $DB_RESTORE_FAIL
        fi
        
    else
        echo "*** EMS is not running ***" >> $LOG_DIR/backuprestore_error.log
    fi
    echo "step2;"
    
    echo "*** Dropping the existing database [$POSTGRESDATABASE] that exists ***" >> $LOG_DIR/backuprestore_error.log
    dropdb -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE
    if [ $? -eq 0 ]
    then
	    #Adding a sleep, so that sytem can become stable, hdd activity etc
	    sleep 2 
	    echo "*** Database has been successfully dropped..Attempting to create $postgresDATABASE ***" >> $LOG_DIR/backuprestore_error.log
	    createdb -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE
	    if [ $? -ne 0 ]
	    then
            echo "ERROR: There is some error while creating $postgresDATABASE database."
		    return $DB_CREATION_FAIL
        else
            echo "*** Database [$POSTGRESDATABASE] has been successfully created. ***" >> $LOG_DIR/backuprestore_error.log
	    fi
    else

        echo "*** Internal error while dropping database $postgresDATABASE. Starting EMS application again. ***" >> $LOG_DIR/backuprestore_error.log
        startStatus=$(wget --no-check-certificate https://$managerUser:$managerPass@localhost/manager/start?path=/ems -O - -q)     
		sleep 15
        if [[ $startStatus =~ "OK" ]]
        then
           echo "*** Ems is up again ***" >> $LOG_DIR/backuprestore_error.log
        else
           echo "*** Internal issue while starting up EMS ***" >> $LOG_DIR/backuprestore_error.log
        fi
        echo "ERROR: There was some error while dropping ems database." 
		return $DB_RESTORE_FAIL	
    fi
    echo "step3;"

    echo "*** Starting with restore process ***" >> $LOG_DIR/backuprestore_error.log

    if [ -f $WORKINGDIRECTORY/$DAILY_EC_BACKUP_FILE ]
    then
        echo "*** Restore the schema and setup data. ***" >> $LOG_DIR/backuprestore_error.log
        pg_restore -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -F c -v <  $backupfile
        if [ $? -eq 0 ]
        then
            echo "*** Schema and setup data is restored. Continuing with energy consumption data backup. ***" >> $LOG_DIR/backuprestore_error.log
        else
            echo "ERROR: Internal error while restoring database $postgresDATABASE. This is a very critical issue. Please raise an alarm."
            return $DB_RESTORE_FAIL	
        fi
        pg_restore -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -F c -v -s <  $EC_SCHEMA_BACKUP_FILE
        
        if [ $? -eq 0 ]
        then
            echo "*** Dropping Energy Consumption constraints. ***" >> $LOG_DIR/backuprestore_error.log
            psql -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE <  $DROPECINDEX
            if [ $? -eq 0 ]
            then
                echo "*** Energy consumption indexes and contraints dropped. ***" >> $LOG_DIR/backuprestore_error.log
                echo "*** Restoring energy consumption data. This might take a long time depending on the data size. Please be patient. ***" >> $LOG_DIR/backuprestore_error.log
                pg_restore -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -F c -v -a <  $DAILY_EC_BACKUP_FILE
                if [ $? -eq 0 ]
                then
                    echo "*** Energy consumption data restored. ***" >> $LOG_DIR/backuprestore_error.log
                    echo "*** Adding energy consumption indexes and constraints. This might take a long time depending on the data size. Please be patient. ***" >> $LOG_DIR/backuprestore_error.log
                    psql -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE <  $ADDECINDEX
                    if [ $? -eq 0 ]
                    then
                        echo "*** Energy consumption indexed and constraints restored. ***" >> $LOG_DIR/backuprestore_error.log
                    else
                        echo "ERROR: Internal error while restoring database $postgresDATABASE. This is a very critical issue. Please raise an alarm."
                        return $DB_RESTORE_FAIL	
                    fi
                else
                    echo "ERROR: Internal error while restoring database $postgresDATABASE. This is a very critical issue. Please raise an alarm."
                    return $DB_RESTORE_FAIL	
                fi
            else
                echo "ERROR: Internal error while restoring database $postgresDATABASE. This is a very critical issue. Please raise an alarm."
                return $DB_RESTORE_FAIL	
            fi
        else
            echo "ERROR: Internal error while restoring database $postgresDATABASE. This is a very critical issue. Please raise an alarm."
            return $DB_RESTORE_FAIL	
        fi
    else
        $echo "*** Restoring complete data. This might take a long time depending on the data size. Please be patient.***" >> $LOG_DIR/backuprestore_error.log
        pg_restore -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -F c -v <  $backupfile
        if [ $? -eq 0 ]
        then
            echo "*** Restoration complete. ***" >> $LOG_DIR/backuprestore_error.log
        else
            echo "ERROR: Internal error while restoring database $postgresDATABASE. This is a very critical issue. Please raise an alarm."
            return $DB_RESTORE_FAIL	
        fi
    fi


    echo "*** Database is successfully restored. ***" >> $LOG_DIR/backuprestore_error.log
    echo "step4;"

    if [ -f $WORKINGDIRECTORY/$DAILY_EC_BACKUP_FILE ]
    then
        rm -f "$WORKINGDIRECTORY/$DAILY_EC_BACKUP_FILE"
    fi
    if [ -f $WORKINGDIRECTORY/$FILENAME ]
    then
        rm -f "$WORKINGDIRECTORY/$FILENAME"
    fi
    backupfile=$(echo $FILENAME | sed "s/.tar.gz/.backup/g")
    if [ -f $WORKINGDIRECTORY/$backupfile ]
    then
        rm -f "$WORKINGDIRECTORY/$backupfile"
    fi
    if [ -f $WORKINGDIRECTORY/$EC_SCHEMA_BACKUP_FILE ]
    then
        rm -f "$WORKINGDIRECTORY/$EC_SCHEMA_BACKUP_FILE"
    fi
    if [ -f "$WORKINGDIRECTORY/MANIFEST.MF" ]
    then
        rm -f "$WORKINGDIRECTORY/MANIFEST.MF"
    fi

    echo "*** Starting the ems application again. ***" >> $LOG_DIR/backuprestore_error.log
    startStatus=$(wget --no-check-certificate https://$managerUser:$managerPass@localhost/manager/start?path=/ems -O - -q)     
	sleep 15
    if [[ $startStatus =~ "OK" ]]
    then
       echo "*** Ems is up again ***" >> $LOG_DIR/backuprestore_error.log
    else
       echo "ERROR: Internal issue while starting up EMS. Please try restarting the server."
       return $DB_RESTORE_FAIL	
    fi

    echo "step5;"
    return 0;
}

# ----- Main -----

mkdir -p ${WORKINGDIRECTORY}

if [ "$OPERATION" == "backup" ]
then
        #echo "Starting back up on $FAIL_OVER_BACKUP_DIR "
    echo "EMS_BACKUP_STARTED"
	backupfunction $FILENAME
	if [ $? -ne 0 ]
	then
        echo "*** Backup was not successful ***" >> $LOG_DIR/backuprestore_error.log
        echo "*** EXIT ***" >> $LOG_DIR/backuprestore_error.log
		exit $DB_BACKUP_FAIL
	else
        echo "*** Backup successful ***" >> $LOG_DIR/backuprestore_error.log
        echo "*** EXIT ***" >> $LOG_DIR/backuprestore_error.log
		exit 0
	fi
fi

#Restore operation
if [ $OPERATION == "${DO_RESTORE}" ]
then
    echo "EMS_RESTORE_STARTED"
	restore $FILENAME $BKFILEPATH
	retcode=$?
    if [ -f $WORKINGDIRECTORY/$DAILY_EC_BACKUP_FILE ]
    then
        rm -f "$WORKINGDIRECTORY/$DAILY_EC_BACKUP_FILE"
    fi
    if [ -f $WORKINGDIRECTORY/$FILENAME ]
    then
        rm -f "$WORKINGDIRECTORY/$FILENAME"
    fi
    backupfile=$(echo $FILENAME | sed "s/.tar.gz/.backup/g")
    if [ -f $WORKINGDIRECTORY/$backupfile ]
    then
        rm -f "$WORKINGDIRECTORY/$backupfile"
    fi
    if [ -f $WORKINGDIRECTORY/$EC_SCHEMA_BACKUP_FILE ]
    then
        rm -f "$WORKINGDIRECTORY/$EC_SCHEMA_BACKUP_FILE"
    fi
    if [ -f "$WORKINGDIRECTORY/MANIFEST.MF" ]
    then
        rm -f "$WORKINGDIRECTORY/MANIFEST.MF"
    fi
	if [ $retcode -ne 0 ]
    then
		echo "*** Restore was not successful ***" >> $LOG_DIR/backuprestore_error.log
		echo "*** EXIT ***" >> $LOG_DIR/backuprestore_error.log
        exit $retcode
    else
		echo "*** Restore was successful ***" >> $LOG_DIR/backuprestore_error.log
		echo "*** EXIT ***" >> $LOG_DIR/backuprestore_error.log
        exit 0
    fi
fi

} > $LOG_DIR/backuprestore.log 2>> $LOG_DIR/backuprestore_error.log