#!/bin/bash
export LOG_DIR="/var/lib/tomcat6/Enlighted/adminlogs"
export LOG_HISTORY_DIR="/var/lib/tomcat6/Enlighted/adminlogs/history"
mkdir -p $LOG_HISTORY_DIR
export EMS_MODE_FILE="/var/lib/tomcat6/Enlighted/emsmode"
export TOMCAT_PATH="/var/lib/tomcat6/webapps"
export upgradeBackupDir="/var/lib/tomcat6/Enlighted/tempExtract"
mkdir -p $LOG_DIR
# Input parameters
export OPERATION="$1"
export FILENAME=$2
export BKFILEPATH=$3
export APP_ROOT=$4
export IP_ADDRESS=$5
export FORCE=$6
export manifestfile=$TOMCAT_PATH/ems/META-INF/MANIFEST.MF
versionstring1=$(cat $manifestfile | grep Implementation-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
versionstring2=$(cat $manifestfile | grep Build-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
versionstringnumber="$versionstring1-$versionstring2" 
export BACKUP_VERSION=$versionstringnumber

logDir="dbbackup"
if [ "${OPERATION}" == "restore" ]
then
    logDir="dbrestore"
fi

if [ -d "${LOG_HISTORY_DIR}/${logDir}" ]
then
    content=$(find "${LOG_HISTORY_DIR}/${logDir}" -type f)  
    if [ ! -z "${content}" ]
    then
        opTime=$(head -1 "${LOG_HISTORY_DIR}"/last_"${logDir}"_time)
        cd ${LOG_HISTORY_DIR}
        tar -czf ${logDir}_${opTime}.tar.gz ${logDir}
        rm -rf ${LOG_HISTORY_DIR}/${logDir}
    fi
fi

mkdir -p ${LOG_HISTORY_DIR}/${logDir}
now=$(date '+%Y'-'%m'-'%d'_'%H'-'%M'-'%S')
echo "$now" > $LOG_HISTORY_DIR/last_${logDir}_time

echo "" > $LOG_DIR/backuprestore.log
echo "" > $LOG_DIR/backuprestore_error.log

{
# --------------
# This is a backup restore script to be called by the GUI.
# --------------

status="0"
auditid="0"

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
export DROPECINDEX="${APP_ROOT}/../adminscripts/drop_ec_index_and_constraint.sql"

#Add Energy Consumption Index And Contraint File
export ADDECINDEX="${APP_ROOT}/../adminscripts/add_ec_index_and_constraint.sql"

export AUDITLOGSCRIPT="$APP_ROOT/../adminscripts/auditlogs.sh"

#upgradeSQL path
export UPGRADE_SQL_PATH="/home/enlighted/upgradeSQL.sql"

# Database details
export POSTGRESHOST="localhost"
export POSTGRESUSER="postgres"
export POSTGRESDATABASE="ems"
export today=$(date '+%Y'-'%m'-'%d'_'%H'-'%M')
export yesterday=$(date --d '1 day ago' '+%m'-'%d'-'%Y')

#Error codes
export DB_BACKUP_FAIL=1

# --- Backup Function ---
backupfunction() {
    
    BACKUP_DIR=$FAIL_OVER_BACKUP_DIR
	backupfile=$1

	cd ${BACKUP_DIR}

    echo "*** Starting database dump ***" >> $LOG_DIR/backuprestore_error.log   
   	##  Temporary fix. Always taking complete dump. Uncomment following lines to speed up backup after fixing daily backup run.
    latestbackupfile=$(find -name $DAILY_EC_BACKUP_FILE -mtime -1)
    if [ -f "$DAILY_EC_BACKUP_FILE" ]
    then
        if [[ "$latestbackupfile" =~ "$DAILY_EC_BACKUP_FILE" ]]
        then
            echo "*** Energy consumption daily backup exists ***" >> $LOG_DIR/backuprestore_error.log
            echo "*** Generating schema and data backup without energy consumption data ***" >> $LOG_DIR/backuprestore_error.log
            /usr/bin/pg_dump -i -U $POSTGRESUSER -h $POSTGRESHOST -v -F c -s -t energy_consumption -t energy_consumption_hourly -t energy_consumption_daily -f  "$EC_SCHEMA_BACKUP_FILE" $POSTGRESDATABASE
            /usr/bin/pg_dump -i -U $POSTGRESUSER -h $POSTGRESHOST -v -b -F c -T energy_consumption -T energy_consumption_hourly -T energy_consumption_daily -f  "${BACKUP_DIR}/${backupfile}_${BACKUP_VERSION}_$today.backup" $POSTGRESDATABASE

        else
            echo "*** Daily energy consumption backup is not there for today. Generating complete backup. ***" >> $LOG_DIR/backuprestore_error.log
            /usr/bin/pg_dump -i -U $POSTGRESUSER -h $POSTGRESHOST -v -b -F c -f  "${BACKUP_DIR}/${backupfile}_${BACKUP_VERSION}_$today.backup" $POSTGRESDATABASE
        fi
    else
        echo "*** Daily energy consumption backup is not there for today. Generating complete backup. ***" >> $LOG_DIR/backuprestore_error.log
        /usr/bin/pg_dump -i -U $POSTGRESUSER -h $POSTGRESHOST -v -b -F c -f  "${BACKUP_DIR}/${backupfile}_${BACKUP_VERSION}_$today.backup" $POSTGRESDATABASE
    fi

	if [ $? -ne 0 ]
	then
        echo "ERROR: Database dump was not successful."
		return $DB_BACKUP_FAIL
	fi

    echo "step1;"
    echo "*** Database dump successful ***" >> $LOG_DIR/backuprestore_error.log

    echo "*** Getting current implementation version ***" >> $LOG_DIR/backuprestore_error.log
    manifestfile=$TOMCAT_PATH/ems/META-INF/MANIFEST.MF
    if [ ! -f $manifestfile ]
    then
        echo "ERROR: Manifest file, holding current implementation version, is missing."
        rm -f ${backupfile}_${BACKUP_VERSION}_$today.backup $EC_SCHEMA_BACKUP_FILE
        return $DB_BACKUP_FAIL
	fi
    cp $manifestfile ${BACKUP_DIR}/
    echo "step2;"

    echo "*** Creating archive file ***" >> $LOG_DIR/backuprestore_error.log
##  Temporary fix. Always taking complete dump. Uncomment following lines to speed up backup after fixing daily backup run.
    if [ -f "$DAILY_EC_BACKUP_FILE" ]
    then
        if [[ "$latestbackupfile" =~ "$DAILY_EC_BACKUP_FILE" ]]
        then
        	tar -zcvf ${backupfile}_${BACKUP_VERSION}_$today.tar.gz ${backupfile}_${BACKUP_VERSION}_$today.backup $DAILY_EC_BACKUP_FILE MANIFEST.MF $EC_SCHEMA_BACKUP_FILE
        else 
           tar -zcvf ${backupfile}_${BACKUP_VERSION}_$today.tar.gz ${backupfile}_${BACKUP_VERSION}_$today.backup MANIFEST.MF 
        fi
    else 
       tar -zcvf ${backupfile}_${BACKUP_VERSION}_$today.tar.gz ${backupfile}_${BACKUP_VERSION}_$today.backup MANIFEST.MF 
    fi
	tarstatus=$?

	rm -f ${backupfile}_${BACKUP_VERSION}_$today.backup $EC_SCHEMA_BACKUP_FILE
    rm -f MANIFEST.MF

	if [ $tarstatus -ne 0 ]
	then
        echo "ERROR: Failed to create an acrhive file."
		return $DB_BACKUP_FAIL
	fi

    if [ "$FORCE" == "F" ]
    then
        cp "${backupfile}_${BACKUP_VERSION}_$today.tar.gz" "$upgradeBackupDir/"
    fi
    
    USBMEDIA=$(df -k | grep media | tr -s ' ' ' '| cut -f6 -d' ' | head -1)
    if [ -d "$USBMEDIA" ]
    then
        echo "*** USB media $USBMEDIA/ is detected. Attempting to move backup from server to usb. ***" >> $LOG_DIR/backuprestore_error.log
        SPACE_AVAILABLE=$(df -B 1 | grep "${USBMEDIA}" | tr -s ' ' ' ' | cut -d" " -f4)
        BACKUP_FILE_SIZE=$(ls -l "${backupfile}_${BACKUP_VERSION}_$today.tar.gz"  | tr -s ' ' ' ' | cut -d" " -f5)
        
        if [ "${BACKUP_FILE_SIZE}" -lt "${SPACE_AVAILABLE}" ]
        then
            if [ ! -d "${USBMEDIA}/dbbackup" ]
            then
	            mkdir "${USBMEDIA}/dbbackup"
            fi
            sudo cp ${backupfile}_${BACKUP_VERSION}_$today.tar.gz "${USBMEDIA}/dbbackup/"
            if [ $? -ne 0 ]
            then
                echo "*** Failed to move backup file ${backupfile}_${BACKUP_VERSION}_$today.tar.gz from server to $USBMEDIA/dbbackup ***" >> $LOG_DIR/backuprestore_error.log
            else
                rm ${backupfile}_${BACKUP_VERSION}_$today.tar.gz
                echo "*** Moved backup file ${backupfile}_${BACKUP_VERSION}_$today.tar.gz from server to $USBMEDIA/dbbackup ***" >> $LOG_DIR/backuprestore_error.log
            fi
        else
            echo "*** Not enough space available to move backup on $USBMEDIA/. It is saved on server. ***" >> $LOG_DIR/backuprestore_error.log
        fi
    fi

    echo "step3;"
	return 0	
}

# --- Restore function ---
restore() {

    tarfile=$1
	path=$2

	cd $WORKINGDIRECTORY
    
    if [ $path != "/var/lib/tomcat6/Enlighted/bkRestoreFolderTemp" ]
    then
        rm -rf ./*
        sudo cp $path/$tarfile ./
    fi

    echo "*** Starting database restore ***" >> $LOG_DIR/backuprestore_error.log

    if [ ! -f "$tarfile" ]
    then
        echo "ERROR: Backup file no longer exists."
        status="2"
        return
    fi
	
    echo "*** Extracting data from backup file ***" >> $LOG_DIR/backuprestore_error.log
	tar -zvxf $tarfile

    if [ "$FORCE" != "F" ]
    then
        #Client side warning about system upgrade.
        sleep 30

        echo "*** Getting current implementation version ***" >> $LOG_DIR/backuprestore_error.log

        manifestfile=$TOMCAT_PATH/ems/META-INF/MANIFEST.MF
        if [ ! -f $manifestfile ]
        then
            echo "ERROR: Manifest file, holding current implemenation version, is missing. "
            status="2"
            return
	    fi
    
		
	version1=$(cat $manifestfile | grep Implementation-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
        version2=$(cat $manifestfile | grep Build-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
        versiononsystem="$version1.$version2"

        if [ -f MANIFEST.MF ] 
        then
            backupmanifest="MANIFEST.MF"
            backuptype="compressed"
            if [ -f "$DAILY_EC_BACKUP_FILE" ]
            then
                backupmode="split"
            else
                backupmode="single"
            fi
        fi

        if [ -f EMSMANIFEST.MF ] 
        then
            backupmanifest="EMSMANIFEST.MF"
            backuptype="sql"
        fi
    
        echo "*** Checking versions ***" >> $LOG_DIR/backuprestore_error.log
	    if [ -f "${backupmanifest}" ]
	    then
            version1=$(cat ${backupmanifest} | grep Implementation-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
            version2=$(cat ${backupmanifest} | grep Build-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
            versiononbackupfile="$version1.$version2"
		    if [ "$versiononsystem" == "$versiononbackupfile" ]
		    then
			    echo "*** Versions are compatible ***" >> $LOG_DIR/backuprestore_error.log
		    else
			    echo "*** System version: ${versiononsystem} ***" >> $LOG_DIR/backuprestore_error.log
			    echo "*** Backup File version: ${versiononbackupfile} ***" >> $LOG_DIR/backuprestore_error.log
                echo "ERROR: Cannot continue with the restore. EM application and backup file must be on the same version."
                status="2"
                return
		    fi
	    else
            echo "ERROR: Invalid backup file. Backup bundle is not versioned."
            status="2"
            return
	    fi

    else
        if [ -f MANIFEST.MF ] 
        then
            backuptype="compressed"
            if [ -f "$DAILY_EC_BACKUP_FILE" ]
            then
                backupmode="split"
            else
                backupmode="single"
            fi
        fi

        if [ -f EMSMANIFEST.MF ] 
        then
            backuptype="sql"
        fi
    fi

    if [ "$backuptype" == "compressed" ]
    then
        backupfile=$(echo $tarfile | sed "s/.tar.gz/.backup/g")
    else
	    backupfile=$(echo $tarfile | sed "s/.tar.gz/.sql/g")
    fi
    

    if [ ! -f $backupfile ]
    then
        echo "ERROR: $backupfile does not exist. Please take a dump and gzip it with the same name as that of the tar ball name. "
        status="2"
        return
    else
		echo "*** $backupfile exists ***" >> $LOG_DIR/backuprestore_error.log
	fi
    
    echo "step1;"

    echo "*** Shutdown EM application if it is running ***" >> $LOG_DIR/backuprestore_error.log
    stopStatus=$(sudo /etc/init.d/tomcat6 stop N)
    if [[ "$stopStatus" =~ "done" ]]
    then
        echo "*** EM is down ***" >> $LOG_DIR/backuprestore_error.log
        echo "*** Killing any active sessions left on database ***" >> $LOG_DIR/backuprestore_error.log
        psql -h $POSTGRESHOST -U $POSTGRESUSER -c "SELECT pg_terminate_backend(pg_stat_activity.procpid) from pg_stat_activity where pg_stat_activity.datname = '${POSTGRESDATABASE}';"
        if [ $? -eq 0 ]
        then
            echo "*** Sessions destroyed ***" >> $LOG_DIR/backuprestore_error.log
        fi
    else
        echo "ERROR: Some problem while stopping ems. Please try again after some time."
        status="1"
        return
    fi

    echo "step2;"
    
    echo "*** Dropping the existing database [$POSTGRESDATABASE] that exists ***" >> $LOG_DIR/backuprestore_error.log
    dropdb -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE
    if [ $? -eq 0 ]
    then
	    #Adding a sleep, so that sytem can become stable, hdd activity etc
	    sleep 2 
	    echo "*** Database has been successfully dropped. Attempting to create $POSTGRESDATABASE again. ***" >> $LOG_DIR/backuprestore_error.log
	    createdb -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE
	    if [ $? -ne 0 ]
	    then
            echo "ERROR: There is some error while creating $POSTGRESDATABASE database."
            status="1"
		    return
        else
            echo "*** Database [$POSTGRESDATABASE] has been successfully created. ***" >> $LOG_DIR/backuprestore_error.log
	    fi
    else

        echo "*** Internal error while dropping database $POSTGRESDATABASE. Starting EM application again. ***" >> $LOG_DIR/backuprestore_error.log
        echo "ERROR: There was some error while dropping ems database." 
		status="1"
        return
    fi
    echo "step3;"

    echo "*** Starting with restore process ***" >> $LOG_DIR/backuprestore_error.log

    if [ "$backuptype" == "compressed" ]
    then
        if [ "$backupmode" == "split" ]
        then
            echo "*** Restore the schema and setup data. ***" >> $LOG_DIR/backuprestore_error.log
            pg_restore -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -F c -v <  $backupfile
            if [ $? -eq 0 ]
            then
                echo "*** Schema and setup data is restored. Continuing with energy consumption data backup. ***" >> $LOG_DIR/backuprestore_error.log
            else
                echo "ERROR: Internal error while restoring database $postgresDATABASE. This is a very critical issue. Please raise an alarm."
                status="1"
                return
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
                            echo "ERROR: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
                            status="1"
                            return
                        fi
                    else
                        echo "ERROR: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
                        status="1"
                        return
                    fi
                else
                    echo "ERROR: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
                    status="1"
                    return
                fi
            else
                echo "ERROR: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
                status="1"
                return
            fi
        else
            echo "*** Restoring complete data. This might take a long time depending on the data size. Please be patient.***" >> $LOG_DIR/backuprestore_error.log
            pg_restore -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -F c -v <  $backupfile
            if [ $? -eq 0 ]
            then
                echo "*** Restore complete. ***" >> $LOG_DIR/backuprestore_error.log
            else
                echo "ERROR: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
                status="1"
                return
            fi
        fi

    else
        echo "*** Restoring complete data. This might take a long time depending on the data size. Please be patient.***" >> $LOG_DIR/backuprestore_error.log
        psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE <  $backupfile >> $LOG_DIR/backuprestore_error.log
        if [ $? -eq 0 ]
        then
            echo "*** Restoration complete. ***" >> $LOG_DIR/backuprestore_error.log
        else
            echo "ERROR: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
            status="1"
            return
        fi
    fi

	echo "*** Performing an additional step of setting up database. Running $UPGRADE_SQL_PATH file. ***" >> $LOG_DIR/backuprestore_error.log
	if [ ! -f $UPGRADE_SQL_PATH ]
	then
		echo "*** Upgrade SQL file not present on system. Please run it manually. ***" >> $LOG_DIR/backuprestore_error.log
	else
		psql -U $POSTGRESUSER -h $POSTGRESHOST $POSTGRESDATABASE -f $UPGRADE_SQL_PATH
		if [ $? -ne 0 ]
		then
			echo "Failed to run $UPGRADE_SQL_PATH file. Still proceeding.." >> $LOG_DIR/backuprestore_error.log
        else
            echo "Database is successfully upgraded." >> $LOG_DIR/backuprestore_error.log
		fi
	fi

    echo "*** Database is successfully restored. ***" >> $LOG_DIR/backuprestore_error.log
    echo "step4;"

    rm -rf ./*

    return 0;
}

startTomcat() {
    startStatus=$(sudo /etc/init.d/tomcat6 start N)
    if [[ "$startStatus" =~ "done" ]]
    then
        echo "*** Tomcat6 service is running. EM application should be up again ***" >> $LOG_DIR/backuprestore_error.log
        echo "*** NOTE: If you are not able to access EM application in another 2-3 minutes, please raise an alarm ***" >> $LOG_DIR/backuprestore_error.log
        return
    fi
    status="1"
    echo "*** ERROR: TOMCAT6 RESTART FAILED. Please do it manually ***" >> $LOG_DIR/backuprestore_error.log
}

# ----- Main -----

mkdir -p ${WORKINGDIRECTORY}

#Backup operation
if [ "$OPERATION" == "backup" ]
then
    auditinsert=$($AUDITLOGSCRIPT "add" "EM Management" "EM Database backup process is initiated to create bundle file $FILENAME. Processing..." "$IP_ADDRESS")
    if [[ $auditinsert =~ "INSERT 0 1" ]]
    then
        auditid=$(echo $auditinsert | cut -d":" -f2)
    fi

    echo "EMS_BACKUP_RESTORE_STARTED"

    if [ "$FORCE" != "F" ]
    then
        mode=$(checkandsetemmode.sh "NORMAL:BACKUP:$FILENAME")
    else
        mode="S"
    fi

    if [ $mode == "S" ]
    then

	    backupfunction $FILENAME
        retstate=$?
        if [ "$FORCE" != "F" ]
        then
            mode=$(checkandsetemmode.sh "NORMAL")
        fi
    else
       retstate=1
       echo "ERROR: Cannot continue with backup. There is already an ongoing process doing some critical work. Please try again after some time."
    fi
	if [ $retstate -ne 0 ]
	then
        updateoutput=$($AUDITLOGSCRIPT "update" "" " Failed." "" "$auditid")
        echo "*** Backup was not successful ***" >> $LOG_DIR/backuprestore_error.log
        echo "*** EXIT ***" >> $LOG_DIR/backuprestore_error.log
		exit $DB_BACKUP_FAIL
	else
        updateoutput=$($AUDITLOGSCRIPT "update" "" " Successful." "" "$auditid")
        echo "*** Backup successful ***" >> $LOG_DIR/backuprestore_error.log
        echo "*** EXIT ***" >> $LOG_DIR/backuprestore_error.log
	fi
fi

#Restore operation
if [ $OPERATION == "restore" ]
then
    
    restoredate=$(date "+%Y-%m-%d %H:%M:%S")
    echo "EMS_BACKUP_RESTORE_STARTED"

    if [ "$FORCE" != "F" ]
    then
        mode=$(checkandsetemmode.sh "UPGRADE_RESTORE:$FILENAME")
    else
        echo "*** It seems database restore was interuppted during upgrade process due to unknown reasons. Trying to redo the restoration. ***" >> $LOG_DIR/backuprestore_error.log
        mode="S"
    fi

    if [ "$FORCE" == "R" ]
    then
        mode="S"
        echo "*** NOTE: It seems that database restore was interuppted due to system failure. Trying to re-initiate the restore process using $FILENAME. ***" >> $LOG_DIR/backuprestore_error.log
    fi


    if [ $mode == "S" ]
    then

        if [ "$FORCE" != "F" ]
        then
            sed -i 's/\(<span\ id="maintenance">\)[a-zA-Z0-9]*\(<\/span>\)/\1Y\2/' /var/lib/tomcat6/webapps/ROOT/heartbeat.html
        fi
	    restore $FILENAME $BKFILEPATH
        if [ "$FORCE" != "F" -a "$status" != "2" ]
        then
            startTomcat
        fi
        if [ "$status" = "0" ]
        then
            echo "step5;"
        fi
        if [ "$FORCE" != "F" ]
        then
            sed -i 's/\(<span\ id="maintenance">\)[a-zA-Z0-9]*\(<\/span>\)/\1N\2/' /var/lib/tomcat6/webapps/ROOT/heartbeat.html
            mode=$(checkandsetemmode.sh "NORMAL")
        fi
        rm -rf "$WORKINGDIRECTORY/*"
    else
       status="1"
       echo "ERROR: Cannot continue with restore. There is already an ongoing process doing some critical work. Please try again after some time."
    fi

    if [ "$FORCE" != "F" ]
    then

        auditinsert=$($AUDITLOGSCRIPT "add" "EM Management Upgrade/Restore" "EM Database restore process is initiated using file $FILENAME. Processing..." "$IP_ADDRESS" "" "$restoredate" )
        if [[ $auditinsert =~ "INSERT 0 1" ]]
        then
            auditid=$(echo $auditinsert | cut -d":" -f2)
        fi

	    if [ $status != "0" ]
        then
            updateoutput=$($AUDITLOGSCRIPT "update" "" " Failed." "" "$auditid")
		    echo "*** Restore was not successful ***" >> $LOG_DIR/backuprestore_error.log
		    echo "*** EXIT ***" >> $LOG_DIR/backuprestore_error.log
        else
            updateoutput=$($AUDITLOGSCRIPT "update" "" " Successful." "" "$auditid")
		    echo "*** Restore was successful ***" >> $LOG_DIR/backuprestore_error.log
		    echo "*** EXIT ***" >> $LOG_DIR/backuprestore_error.log
        fi
    fi
fi

} > $LOG_DIR/backuprestore.log 2>> $LOG_DIR/backuprestore_error.log


cp $LOG_DIR/backuprestore.log ${LOG_HISTORY_DIR}/${logDir}
cp $LOG_DIR/backuprestore_error.log ${LOG_HISTORY_DIR}/${logDir}

opTime=$(head -1 "${LOG_HISTORY_DIR}"/last_"${logDir}"_time)
cd ${LOG_HISTORY_DIR}
tar -czf ${logDir}_${opTime}.tar.gz ${logDir}
rm -rf ${LOG_HISTORY_DIR}/${logDir}
exit 0
