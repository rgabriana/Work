#!/bin/bash
source /etc/environment
. /etc/lsb-release
current_os=$DISTRIB_RELEASE
min_os_ver=10.04
flag_os_greater_than_1004=$(echo $current_os $min_os_ver | awk '{if ($1 > $2) print 1; else print 0}')

export LOG_DIR="$ENL_APP_HOME/Enlighted/adminlogs"
export LOG_HISTORY_DIR="$ENL_APP_HOME/Enlighted/adminlogs/history"
mkdir -p $LOG_HISTORY_DIR
export EMS_MODE_FILE="$ENL_APP_HOME/Enlighted/emsmode"
export TOMCAT_PATH="$ENL_APP_HOME/webapps"
export upgradeBackupDir="$ENL_APP_HOME/Enlighted/tempExtract"
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
export FAIL_OVER_BACKUP_DIR="$OPT_ENLIGHTED/DB/DBBK"

#Daily backup file name
export DAILY_EC_BACKUP_FILE="daily_ec_dump.backup"

#schema backup file name
export EC_SCHEMA_BACKUP_FILE="ec_schema_dump.backup"

#Drop Energy Consumption Index And Contraint File
export DROPECINDEX="${APP_ROOT}/../adminscripts/drop_ec_index_and_constraint.sql"
export DROPPLECINDEX="${APP_ROOT}/../adminscripts/drop_plec_index_and_constraint.sql"

#Add Energy Consumption Index And Contraint File
export ADDECINDEX="${APP_ROOT}/../adminscripts/add_ec_index_and_constraint.sql"
export ADDPLECINDEX="${APP_ROOT}/../adminscripts/add_plec_index_and_constraint.sql"

export AUDITLOGSCRIPT="$APP_ROOT/../adminscripts/auditlogs.sh"

export ENCRYPTDECRYPTSCRIPT="$APP_ROOT/../adminscripts/encryptdecryptstring.sh"

#upgradeSQL path
export UPGRADE_SQL_PATH="$ENLIGHTED_HOME/upgradeSQL.sql"
export SPPA_ENABLE_SQL="$ENLIGHTED_HOME/sppa.sql"

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
            /usr/bin/pg_dump -i -U $POSTGRESUSER -h $POSTGRESHOST -v -F c -s -t energy_consumption -t energy_consumption_hourly -t energy_consumption_daily -t plugload_energy_consumption -t plugload_energy_consumption_hourly -t plugload_energy_consumption_daily -f  "$EC_SCHEMA_BACKUP_FILE" $POSTGRESDATABASE
            /usr/bin/pg_dump -i -U $POSTGRESUSER -h $POSTGRESHOST -v -b -F c -T energy_consumption -T energy_consumption_hourly -T energy_consumption_daily -T plugload_energy_consumption -T plugload_energy_consumption_hourly -T plugload_energy_consumption_daily -f  "${BACKUP_DIR}/${backupfile}_${BACKUP_VERSION}_$today.backup" $POSTGRESDATABASE

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
        echo "ERROR:: Database dump was not successful."
		return $DB_BACKUP_FAIL
	fi

    echo "step1;"
    echo "*** Database dump successful ***" >> $LOG_DIR/backuprestore_error.log
	echo "***Taking Configuration Files Backup***********" >> $LOG_DIR/backuprestore_error.log
	sh $ENLIGHTED_HOME/scripts/dailyconfigbackup.sh >> $LOG_DIR/backuprestore_error.log
	echo "***FINISHED with Configuration Files Backup***********" >> $LOG_DIR/backuprestore_error.log
	configBackupFile=`echo $(find $OPT_ENLIGHTED/config_backup/ENL_all_config_backup*)`
	adrBackupFile=`echo $(find $OPT_ENLIGHTED/adr/DB/ADR_db_backup)`

    echo "*** Getting current implementation version ***" >> $LOG_DIR/backuprestore_error.log
    manifestfile=$TOMCAT_PATH/ems/META-INF/MANIFEST.MF
    if [ ! -f $manifestfile ]
    then
        echo "ERROR:: Manifest file, holding current implementation version, is missing."
        rm -f ${backupfile}_${BACKUP_VERSION}_$today.backup $EC_SCHEMA_BACKUP_FILE
        return $DB_BACKUP_FAIL
	fi
    cp $manifestfile ${BACKUP_DIR}/
    echo "step2;"

    echo "*** Creating archive file ***" >> $LOG_DIR/backuprestore_error.log
##  Temporary fix. Always taking complete dump. Uncomment following lines to speed up backup after fixing daily backup run.
	tarFiles=""
    if [ -f "$DAILY_EC_BACKUP_FILE" ]
    then
        if [[ "$latestbackupfile" =~ "$DAILY_EC_BACKUP_FILE" ]]
        then
        	tarFiles=`echo "${backupfile}_${BACKUP_VERSION}_$today.backup $DAILY_EC_BACKUP_FILE MANIFEST.MF $EC_SCHEMA_BACKUP_FILE"`
        else
        	tarFiles=`echo "${backupfile}_${BACKUP_VERSION}_$today.backup MANIFEST.MF"`
        fi
    else
    	tarFiles=`echo "${backupfile}_${BACKUP_VERSION}_$today.backup MANIFEST.MF"`
    fi
    if [ -f "$adrBackupFile" ]
	then
			cp $adrBackupFile .
        	adrBackupFile=${adrBackupFile##*/}
	        tarFiles=`echo "$tarFiles $adrBackupFile"`
	fi
	if [ -f "$configBackupFile" ]
	then
			cp $configBackupFile .
        	configBackupFile=${configBackupFile##*/}
	        tarFiles=`echo "$tarFiles $configBackupFile"`
	fi

	tar -zcvf ${backupfile}_${BACKUP_VERSION}_$today.tar.gz $tarFiles

	tarstatus=$?

	rm -f ${backupfile}_${BACKUP_VERSION}_$today.backup $EC_SCHEMA_BACKUP_FILE
    rm -f MANIFEST.MF
	if [ -f "$adrBackupFile" ]
	then
		rm -rf $adrBackupFile
	fi
	if [ -f "$configBackupFile" ]
	then
		rm -rf $configBackupFile
	fi

	if [ $tarstatus -ne 0 ]
	then
        echo "ERROR:: Failed to create an acrhive file."
		return $DB_BACKUP_FAIL
	fi

    if [ "$FORCE" == "F" ]
    then
        cp "${backupfile}_${BACKUP_VERSION}_$today.tar.gz" "$upgradeBackupDir/"
    fi

# cap maximum number of backups at 5 and delete older ones, bug #EM-848
# cap it at 2 bug EM-997
    backups_no=$(ls -l *.tar.gz | wc -l )

    while [ "${backups_no}" -gt 2 ]
    do
        REMOVE_FILE=$(ls -ltr --time-style="+%Y-%m-%d %H:%M:%S"  *.tar.gz | head -n 1 | tr -s ' ' ' ' | cut -d" " -f8)
        if [ ! -z $REMOVE_FILE ]
        then
            rm -f "${REMOVE_FILE}"
            backups_no=$(ls -l *.tar.gz | wc -l )
        else
            break
        fi
    done
# cap maximum number of backups at 5 and delete older ones, bug #EM-848
# cap it at 2 bug EM-997
    BACKUP_OPTION_USB="usb"
    BACKUP_OPTION_SFTP="sftp"

    BACKUP_OPTION_SELECTED=$(/usr/bin/psql -x -U $POSTGRESUSER $POSTGRESDATABASE -h $POSTGRESHOST -c "select value from system_configuration where name ='backup.option.selected'" | grep value | cut -d " " -f3)

    BACKUP_USB_PATH=$(/usr/bin/psql -x -U $POSTGRESUSER $POSTGRESDATABASE -h $POSTGRESHOST -c "select value from system_configuration where name ='backup.usb.path'" | grep value | cut -d " " -f3)

    if [ "${BACKUP_OPTION_SELECTED}" == "${BACKUP_OPTION_USB}" ]
    then
    	echo "***** usb option selected **********" >>$LOG_DIR/backuprestore_error.log
    	#USBMEDIA=$(df -k | grep media | tr -s ' ' ' '| cut -f6 -d' ' | head -1)

    	USBMEDIACONNECTED=$(df -k | grep media | tr -s ' ' ' '| cut -f6 -d' ')

    	if [[ "$USBMEDIACONNECTED" =~ "$BACKUP_USB_PATH" ]]
	    then
	    	USBMEDIA="${BACKUP_USB_PATH}"
	    	SPACE_AVAILABLE=$(df -B 1 | grep "${USBMEDIA}" | tr -s ' ' ' ' | cut -d" " -f4)
		else
			echo "*** External Storage device not found at the path ${BACKUP_USB_PATH} .Backup file is saved on EM server. ********" >>$LOG_DIR/backuprestore_error.log
			USBMEDIA=""
		fi

	    if [ -d "$USBMEDIA" ]
	    then
	        echo "*** USB media $USBMEDIA/ is detected. Attempting to move backup from server to usb. ***" >> $LOG_DIR/backuprestore_error.log
	        SPACE_AVAILABLE=$(df -B 1 | grep "${USBMEDIA}" | tr -s ' ' ' ' | cut -d" " -f4)
	        BACKUP_FILE_SIZE=$(ls -l "${backupfile}_${BACKUP_VERSION}_$today.tar.gz"  | tr -s ' ' ' ' | cut -d" " -f5)

	        if [ "${BACKUP_FILE_SIZE}" -lt "${SPACE_AVAILABLE}" ]
	        then
	            if [ ! -d "${USBMEDIA}/dbbackup" ]
	            then
		            sudo mkdir "${USBMEDIA}/dbbackup"
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
    else
    	echo "***** sftp option selected **********" >>$LOG_DIR/backuprestore_error.log
    	backupSftpUsername=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.username'" | grep value | cut -d " " -f3)
    	backupSftpEncryptedPassword=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.password'" | grep value | cut -d " " -f3)
    	backupSftpIp=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.ip'" | grep value | cut -d " " -f3)
    	backupSftpDirectory=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.directory'" | grep value | cut -d " " -f3)
    	backupSftpPassword=$($ENCRYPTDECRYPTSCRIPT "decrypt" $backupSftpEncryptedPassword)

    	if [ ! -z $backupSftpUsername ] && [ ! -z $backupSftpPassword ] && [ ! -z $backupSftpIp ] && [ ! -z $backupSftpDirectory ]
		then
			echo "******* SFTP remote backup start *******" >>$LOG_DIR/backuprestore_error.log
			#lftp sftp://${backupSftpUsername}:${backupSftpPassword}@${backupSftpIp}:${backupSftpDirectory}  -e "put ${SFTP_BACKUP_FILE_NAME}; bye"
			echo "Uploading backup file ${backupfile}_${BACKUP_VERSION}_$today.tar.gz onto sftp server" >>$LOG_DIR/backuprestore_error.log
			Sftpuploadcommandoutput=$(lftp sftp://${backupSftpUsername}:${backupSftpPassword}@${backupSftpIp}  -e "set net:max-retries 2;set net:persist-retries 2;set net:reconnect-interval-base 2;set net:reconnect-interval-max 2;set net:reconnect-interval-multiplier 2;set net:timeout 2;set ftp:list-options -a; cd ${backupSftpDirectory}; put ${backupfile}_${BACKUP_VERSION}_$today.tar.gz; bye" 2>&1)
			#echo "**** ${Sftpuploadcommandoutput} *******" >>$LOG_DIR/backuprestore_error.log
			if [[ ( "$Sftpuploadcommandoutput" =~ "failed" ) || ( "$Sftpuploadcommandoutput" =~ "error" ) ]]
		    then
		    	echo "******* Sftp remote backup failed.Please check Sftp server settings. Backup file is saved on EM server *******" >>$LOG_DIR/backuprestore_error.log
			else
				echo "******* Sftp remote backup successful *******" >>$LOG_DIR/backuprestore_error.log
		    fi
		else
			echo "****** Sftp backup failed. Please check the Sftp server settings.Backup file is saved on EM server ******" >>$LOG_DIR/backuprestore_error.log
		fi

    	#sh /opt/enLighted/DB/sftpbackup.sh "${backupfile}_${BACKUP_VERSION}_$today.tar.gz" >>$LOG_DIR/backuprestore_error.log
    fi

    echo "step3;"
	return 0
}


function generic_insert_update_key_value_conf(){
        arg=0
        if [ -z "$1" ]
          then
                arg=1
        fi
        if [ -z "$2" ]
        then
                arg=1
        fi
        if [ -z "$3" ]
        then
                arg=1
        fi
        if [ $arg -gt 0 ]
          then
                        echo "Arguments not proper.."
                        echo "Please specify arguments in the fashion : key value file_path"
                        echo "generic_insert_update_key_value_conf.sh ENL_APP_HOME= /opt/tomcat /etc/environment"
                        echo "Exiting Now.."
                        exit 1
        fi

        sudo sed -i "/$1/d" $3
        sudo sed -i "$ a $1$2" $3
}
containsElement () {
  local e
  for e in "${@:2}"; do [[ "$e" == "$1" ]] && return 0; done
  return 1
}
replaceWithNewEnvVarValues(){
        #sed -i "s/export//g" test.sh
        #sed -e "s/\.\/var\/lib\/tomcat6/$escape_enl_app_home/g"
        actualReplaceWithNewEnvVarValues "/var/lib/tomcat6" "$ENL_APP_HOME" $1
         actualReplaceWithNewEnvVarValues "/opt/enLighted" "$OPT_ENLIGHTED" $1
         actualReplaceWithNewEnvVarValues "var/lib/tomcat6" "$ENL_APP_HOME_RELATIVE" $1
         actualReplaceWithNewEnvVarValues "/var/log/tomcat6" "$TOMCAT_LOG" $1
         actualReplaceWithNewEnvVarValues "/usr/lib/jvm/java-6-openjdk/jre" "$JAVA_HOME" $1
         actualReplaceWithNewEnvVarValues "tomcat6" "$TOMCAT_USER" $1
         actualReplaceWithNewEnvVarValues "/var/www" "$EM_MGMT_BASE" $1
         actualReplaceWithNewEnvVarValues "/etc/init.d/tomcat6" "$TOMCAT_SERVICE" $1
         actualReplaceWithNewEnvVarValues "/home/enlighted" "$ENLIGHTED_HOME" $1
         actualReplaceWithNewEnvVarValues "/etc/apache2" "$ENL_APACHE_HOME" $1
}

actualReplaceWithNewEnvVarValues(){
        strToReplace=$(echo "$1" | sed 's/\//\\\//g')
        strReplacedBy=$(echo "$2" | sed 's/\//\\\//g')
        sudo sed -i "s/$strToReplace/$strReplacedBy/g" $3
}


# --- Restore function ---
restore() {

    tarfile=$1
	path=$2

	cd $WORKINGDIRECTORY

    if [ $path != "$ENL_APP_HOME/Enlighted/bkRestoreFolderTemp" ]
    then
        rm -rf ./*
        sudo cp $path/$tarfile ./
    fi

    echo "*** Starting database restore ***" >> $LOG_DIR/backuprestore_error.log

    if [ ! -f "$tarfile" ]
    then
        echo "ERROR:: Backup file no longer exists."
        status="2"
        return
    fi

    echo "*** Extracting data from backup file ***" >> $LOG_DIR/backuprestore_error.log
	tar -zvxf $tarfile

	numversiononsystem=-1
	numversiononbackupfile=0
    if [ "$FORCE" != "F" ]
    then
        #Client side warning about system upgrade.
        sleep 30

        echo "*** Getting current implementation version ***" >> $LOG_DIR/backuprestore_error.log

        manifestfile=$TOMCAT_PATH/ems/META-INF/MANIFEST.MF
        if [ ! -f $manifestfile ]
        then
            echo "ERROR:: Manifest file, holding current implemenation version, is missing. "
            status="2"
            return
	    fi


	    version1=$(cat $manifestfile | grep Implementation-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
        version2=$(cat $manifestfile | grep Build-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
        versiononsystem="$version1.$version2"
        versionsystemint="$version1$version2"
        versionsystemint=$(echo "${versionsystemint}")
        versionsystemint=$(echo $versionsystemint | sed "s/\.//g")

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

        if [ -f CLOUD_BACKUP_VERSION ]
        then
            backupmanifest="CLOUD_BACKUP_VERSION"
            backuptype="compressedsql"
			str=`grep "FORMAT:CSV_SPLIT" CLOUD_BACKUP_VERSION`
	    	if [ $str == "FORMAT:CSV_SPLIT" ]
	    	then
				backupformat="csv_split"
		    else
				backupformat="simple"
	    	fi
        fi

        echo "*** Checking versions ***" >> $LOG_DIR/backuprestore_error.log
	    if [ -f "${backupmanifest}" ]
	    then
            if [ "$backupmanifest" == "CLOUD_BACKUP_VERSION" ]
            then
                versiononbackupfile=$(head -n 1 "$backupmanifest")
            else
                version1=$(cat ${backupmanifest} | grep Implementation-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
                version2=$(cat ${backupmanifest} | grep Build-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
                versiononbackupfile="$version1.$version2"
                versiononbackupfileint="$version1$version2"
                versiononbackupfileint=$(echo "${versiononbackupfileint}")
                versiononbackupfileint=$(echo $versiononbackupfileint | sed "s/\.//g")
            fi

			numversiononsystem=`echo "$versiononsystem" | sed 's/\.//g'`
			numversiononbackupfile=`echo "$versiononbackupfile" | sed 's/\.//g'`
		    if [ "$numversiononsystem" -ge "$numversiononbackupfile" ]
		    then
			    echo "*** Versions are compatible ***" >> $LOG_DIR/backuprestore_error.log
   			    echo "*** System version: ${versiononsystem} ***"
   			    echo "*** Backup File version: ${versiononbackupfile} ***"
		    else
			    echo "*** System version: ${versiononsystem} ***" >> $LOG_DIR/backuprestore_error.log
			    echo "*** Backup File version: ${versiononbackupfile} ***" >> $LOG_DIR/backuprestore_error.log
		        #echo "*** System version int: ${versionsystemint} ***" >> $LOG_DIR/backuprestore_error.log
		        #echo "*** Backup File version int: ${versiononbackupfileint} ***" >> $LOG_DIR/backuprestore_error.log
				 echo "ERROR:: Cannot continue with the restore. EM application and backup file must be on the same version , or backup file's version should be lesser than Em Application version"
     		     status="2"
       		     return
		   fi
   		else
            echo "ERROR:: Invalid backup file. Backup bundle is not versioned."
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

    if [ "$backuptype" == "compressed" -o  "$backuptype" == "compressedsql" ]
    then
        backupfile=$(echo $tarfile | sed "s/.tar.gz/.backup/g")
    else
	    backupfile=$(echo $tarfile | sed "s/.tar.gz/.sql/g")
    fi


    if [ ! -f $backupfile ]
    then
        echo "ERROR:: $backupfile does not exist. Please take a dump and gzip it with the same name as that of the tar ball name. "
        status="2"
        return
    else
		echo "*** $backupfile exists ***" >> $LOG_DIR/backuprestore_error.log
	fi

    echo "step1;"

    echo "*** Shutdown EM application if it is running ***" >> $LOG_DIR/backuprestore_error.log
    stopStatus=$(sudo $TOMCAT_SERVICE stop N)
    if [[ "$stopStatus" =~ "done" ]]
    then
        echo "*** EM is down ***" >> $LOG_DIR/backuprestore_error.log
        echo "*** Killing any active sessions left on database ***" >> $LOG_DIR/backuprestore_error.log
        if [ "$flag_os_greater_than_1004" -gt 0 ]
        then
            psql -h $POSTGRESHOST -U $POSTGRESUSER -c "SELECT pg_terminate_backend(pg_stat_activity.pid) from pg_stat_activity where pg_stat_activity.datname = '${POSTGRESDATABASE}';"
        else
            psql -h $POSTGRESHOST -U $POSTGRESUSER -c "SELECT pg_terminate_backend(pg_stat_activity.procpid) from pg_stat_activity where pg_stat_activity.datname = '${POSTGRESDATABASE}';"
        fi
        if [ $? -eq 0 ]
        then
            echo "*** Sessions destroyed ***" >> $LOG_DIR/backuprestore_error.log
        fi
    else
        echo "ERROR:: Some problem while stopping ems. Please try again after some time."
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
            echo "ERROR:: There is some error while creating $POSTGRESDATABASE database."
            status="1"
		    return
        else
            echo "*** Database [$POSTGRESDATABASE] has been successfully created. ***" >> $LOG_DIR/backuprestore_error.log
	    fi
    else

        echo "*** Internal error while dropping database $POSTGRESDATABASE. Starting EM application again. ***" >> $LOG_DIR/backuprestore_error.log
        echo "ERROR:: There was some error while dropping ems database."
		status="1"
        return
    fi
    echo "step3;"

    echo "*** Starting with restore process ***" >> $LOG_DIR/backuprestore_error.log

    if [ "$backuptype" == "compressed" ]
    then
        # Restoring from a "local" backup (NOT a Cloud backup).
        if [ "$backupmode" == "split" ]
        then
            # The Energy Consumption tables are split from the Meta-data tables.
            echo "*** Restore the schema and setup data. ***" >> $LOG_DIR/backuprestore_error.log
            if [ "$flag_os_greater_than_1004" -gt 0 ]
            then
                pg_restore -l "$backupfile" | grep -v "plpgsql" > /tmp/restore_elements
                pg_restore -L /tmp/restore_elements -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE $backupfile >> $LOG_DIR/backuprestore_error.log
            else
                pg_restore -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -F c -v <  $backupfile >> $LOG_DIR/backuprestore_error.log
            fi
            if [ $? -eq 0 ]
            then
                echo "*** Schema and setup data is restored. Continuing with energy consumption data backup. ***" >> $LOG_DIR/backuprestore_error.log
            else
                echo "ERROR:: Internal error while restoring database $postgresDATABASE. This is a very critical issue. Please raise an alarm."
                status="1"
                return
            fi
            if [ "$flag_os_greater_than_1004" -gt 0 ]
            then
                pg_restore -l "$EC_SCHEMA_BACKUP_FILE" | grep -v "plpgsql" > /tmp/restore_elements
                pg_restore -L /tmp/restore_elements -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE $EC_SCHEMA_BACKUP_FILE >> $LOG_DIR/backuprestore_error.log
            else
                pg_restore -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -F c -v -s <  $EC_SCHEMA_BACKUP_FILE >> $LOG_DIR/backuprestore_error.log
            fi

            if [ $? -eq 0 ]
            then
                echo "*** Dropping Energy Consumption constraints. ***" >> $LOG_DIR/backuprestore_error.log
                psql -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE <  $DROPPLECINDEX
                psql -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE <  $DROPECINDEX
                # Remove all "WAL" triggers before restoring data
                psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -c "select removeAllTriggers();"
                if [ $? -eq 0 ]
                then
                    echo "*** Energy consumption indexes and constraints dropped. ***" >> $LOG_DIR/backuprestore_error.log
                    echo "*** Restoring energy consumption data. This might take a long time depending on the data size. Please be patient. ***" >> $LOG_DIR/backuprestore_error.log
                    if [ "$flag_os_greater_than_1004" -gt 0 ]
                    then
                        pg_restore -l "$DAILY_EC_BACKUP_FILE" | grep -v "plpgsql" > /tmp/restore_elements
                		pg_restore -L /tmp/restore_elements -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE $DAILY_EC_BACKUP_FILE >> $LOG_DIR/backuprestore_error.log
                    else
                        pg_restore -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -F c -v -a <  $DAILY_EC_BACKUP_FILE >> $LOG_DIR/backuprestore_error.log
                    fi
                    if [ $? -eq 0 ]
                    then
                        echo "*** Energy consumption data restored. ***" >> $LOG_DIR/backuprestore_error.log
                        echo "*** Adding energy consumption indexes and constraints. This might take a long time depending on the data size. Please be patient. ***" >> $LOG_DIR/backuprestore_error.log
                        psql -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE <  $ADDPLECINDEX
                        psql -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE <  $ADDECINDEX
                        if [ $? -eq 0 ]
                        then
                            echo "*** Energy consumption indexed and constraints restored. ***" >> $LOG_DIR/backuprestore_error.log
                        else
                            echo "ERROR:: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
                            status="1"
                            return
                        fi
                    else
                        echo "ERROR:: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
                        status="1"
                        return
                    fi
                else
                    echo "ERROR:: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
                    status="1"
                    return
                fi
            else
                echo "ERROR:: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
                status="1"
                return
            fi
        else
            # Restore a simple local backup (EC and Meta-data tables are in the same backup file).
            echo "*** Restoring complete data. This might take a long time depending on the data size. Please be patient.***" >> $LOG_DIR/backuprestore_error.log
            if [ "$flag_os_greater_than_1004" -gt 0 ]
            then
                pg_restore -l "$backupfile" | grep -v "plpgsql" > /tmp/restore_elements
                pg_restore -L /tmp/restore_elements -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE $backupfile >> $LOG_DIR/backuprestore_error.log
            else
                pg_restore -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -F c -v <  $backupfile >> $LOG_DIR/backuprestore_error.log
            fi
            if [ $? -eq 0 ]
            then
                echo "*** Restore complete. ***" >> $LOG_DIR/backuprestore_error.log
            else
                echo "ERROR:: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
                status="1"
                return
            fi
        fi

    else
        # Restoring from a Cloud backup
        echo "*** Restoring complete data. This might take a long time depending on the data size. Please be patient.***" >> $LOG_DIR/backuprestore_error.log
        psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE < $EM_MGMT_BASE/em_mgmt/adminscripts/cloud_backup_before_restore.sql >> $LOG_DIR/backuprestore_error.log
        if [ "$backuptype" == "compressedsql" ]
        then
            # Restore from a compressed backup
			if [ "$backupformat" == "csv_split" ]
        	then
        	    # The EC and Meta-Data are split
	            echo "*** Restore the schema and setup data. ***" >> $LOG_DIR/backuprestore_error.log
                    gunzip -c $backupfile | psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE >> $LOG_DIR/backuprestore_error.log
	            if [ $? -eq 0 ]
	            then
	                echo "*** Setup data is restored. Continuing with energy consumption data backup. ***" >> $LOG_DIR/backuprestore_error.log
	            else
	                echo "ERROR:: Internal error while restoring database $postgresDATABASE. This is a very critical issue. Please raise an alarm."
	                status="1"
	                return
	            fi
	            psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE  <  $EC_SCHEMA_BACKUP_FILE
	            if [ $? -eq 0 ]
	            then
	                echo "*** Dropping Energy Consumption constraints. ***" >> $LOG_DIR/backuprestore_error.log
	                psql -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE < $DROPECINDEX
	                psql -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE < $DROPPLECINDEX
	                # Note: Because we are restoring from the cloud, we do not need to drop the WAL triggers
	                if [ $? -eq 0 ]
	                then
	                    echo "*** Energy consumption indexes and contraints dropped. ***" >> $LOG_DIR/backuprestore_error.log
	                    echo "*** Restoring energy consumption data. This might take a long time depending on the data size. Please be patient. ***" >> $LOG_DIR/backuprestore_error.log
#*************************************************************** begin csv restore **********************************************
						echo "*** Begin restoring Energy consumption data ***" >> $LOG_DIR/backuprestore_error.log
                        if [ -d EC_CSV ]
                        then
						    for fgz in EC_CSV/*
						    do
                                gunzip $fgz
                                f=`echo $fgz | cut -d'.' -f1`
							    flines=`wc -l $f | cut -d' ' -f1`
							    if [ $flines -eq 1 ]
							    then
								    echo "Ignoring empty file $f " >> $LOG_DIR/backuprestore_error.log
							    else
								    echo "Processing file $f " >> $LOG_DIR/backuprestore_error.log
								    header=$(head -n 1 $f)
								    head -n 1 $f > EC_CSV/tmp_file
								    sed -i '1d' $f
								    psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -c "\\copy energy_consumption (${header}) from ${f} CSV"
								    if [ $? -ne 0 ]
								    then
									    echo "Error importing file $f " >> $LOG_DIR/backuprestore_error.log
								    fi
								    cat $f >> EC_CSV/tmp_file
								    mv EC_CSV/tmp_file $f
							    fi
						    done
                        fi
						echo "*** Begin restoring Energy consumption Daily data ***" >> $LOG_DIR/backuprestore_error.log
                        if [ -d ECD_CSV ]
                        then
						    for fgz in ECD_CSV/*
						    do
							    gunzip $fgz
							    f=`echo $fgz | cut -d'.' -f1`
							    flines=`wc -l $f | cut -d' ' -f1`
							    if [ $flines -eq 1 ]
							    then
								    echo "Ignoring empty file $f " >> $LOG_DIR/backuprestore_error.log
							    else
								    echo "Processing file $f " >> $LOG_DIR/backuprestore_error.log
								    header=$(head -n 1 $f)
								    head -n 1 $f > ECD_CSV/tmp_file
								    sed -i '1d' $f
								    psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -c "\\copy energy_consumption_daily ($header) from $f CSV"
								    if [ $? -ne 0 ]
								    then
									    echo "Error importing file $f " >> $LOG_DIR/backuprestore_error.log
								    fi
								    cat $f >> ECD_CSV/tmp_file
								    mv ECD_CSV/tmp_file $f
							    fi
						    done
                        fi
						echo "*** Begin restoring Energy consumption Hourly data ***" >> $LOG_DIR/backuprestore_error.log
                        if [ -d ECH_CSV ]
                        then
						    for fgz in ECH_CSV/*
						    do
							    gunzip $fgz
							    f=`echo $fgz | cut -d'.' -f1`
							    flines=`wc -l $f | cut -d' ' -f1`
							    if [ $flines -eq 1 ]
							    then
								    echo "Ignoring empty file $f " >> $LOG_DIR/backuprestore_error.log
							    else
								    echo "Processing file $f " >> $LOG_DIR/backuprestore_error.log
								    header=$(head -n 1 $f)
								    head -n 1 $f > ECH_CSV/tmp_file
								    sed -i '1d' $f
								    psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -c "\\copy energy_consumption_hourly ($header) from $f CSV"
								    if [ $? -ne 0 ]
								    then
									    echo "Error importing file $f " >> $LOG_DIR/backuprestore_error.log
								    fi
								    cat $f >> ECH_CSV/tmp_file
								    mv ECH_CSV/tmp_file $f
							    fi
						    done
                        fi

						echo "*** Begin restoring plugload_ Energy consumption data ***" >> $LOG_DIR/backuprestore_error.log
                        if [ -d PLUGLOAD_EC_CSV ]
                        then
						    for fgz in PLUGLOAD_EC_CSV/*
						    do
							    gunzip $fgz
							    f=`echo $fgz | cut -d'.' -f1`
							    flines=`wc -l $f | cut -d' ' -f1`
							    if [ $flines -eq 1 ]
							    then
								    echo "Ignoring empty file $f " >> $LOG_DIR/backuprestore_error.log
							    else
								    echo "Processing file $f " >> $LOG_DIR/backuprestore_error.log
								    header=$(head -n 1 $f)
								    head -n 1 $f > PLUGLOAD_EC_CSV/tmp_file
								    sed -i '1d' $f
								    psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -c "\\copy plugload_energy_consumption (${header}) from ${f} CSV"
								    if [ $? -ne 0 ]
								    then
									    echo "Error importing file $f " >> $LOG_DIR/backuprestore_error.log
								    fi
								    cat $f >> PLUGLOAD_EC_CSV/tmp_file
								    mv PLUGLOAD_EC_CSV/tmp_file $f
							    fi
						    done
                        fi
						echo "*** Begin restoring plugload_ Energy consumption Daily data ***" >> $LOG_DIR/backuprestore_error.log
                        if [ -d PLUGLOAD_ECD_CSV ]
                        then
						    for fgz in PLUGLOAD_ECD_CSV/*
						    do
							    gunzip $fgz
							    f=`echo $fgz | cut -d'.' -f1`
							    flines=`wc -l $f | cut -d' ' -f1`
							    if [ $flines -eq 1 ]
							    then
								    echo "Ignoring empty file $f " >> $LOG_DIR/backuprestore_error.log
							    else
								    echo "Processing file $f " >> $LOG_DIR/backuprestore_error.log
								    header=$(head -n 1 $f)
								    head -n 1 $f > PLUGLOAD_ECD_CSV/tmp_file
								    sed -i '1d' $f
								    psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -c "\\copy plugload_energy_consumption_daily ($header) from $f CSV"
								    if [ $? -ne 0 ]
								    then
									    echo "Error importing file $f " >> $LOG_DIR/backuprestore_error.log
								    fi
								    cat $f >> PLUGLOAD_ECD_CSV/tmp_file
								    mv PLUGLOAD_ECD_CSV/tmp_file $f
							    fi
						    done
                        fi
						echo "*** Begin restoring plugload_ Energy consumption Hourly data ***" >> $LOG_DIR/backuprestore_error.log
                        if [ -d PLUGLOAD_ECH_CSV ]
                        then
						    for fgz in PLUGLOAD_ECH_CSV/*
						    do
							    gunzip $fgz
							    f=`echo $fgz | cut -d'.' -f1`
							    flines=`wc -l $f | cut -d' ' -f1`
							    if [ $flines -eq 1 ]
							    then
								    echo "Ignoring empty file $f " >> $LOG_DIR/backuprestore_error.log
							    else
								    echo "Processing file $f " >> $LOG_DIR/backuprestore_error.log
								    header=$(head -n 1 $f)
								    head -n 1 $f > PLUGLOAD_ECH_CSV/tmp_file
								    sed -i '1d' $f
								    psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE -c "\\copy plugload_energy_consumption_hourly ($header) from $f CSV"
								    if [ $? -ne 0 ]
								    then
									    echo "Error importing file $f " >> $LOG_DIR/backuprestore_error.log
								    fi
								    cat $f >> PLUGLOAD_ECH_CSV/tmp_file
								    mv PLUGLOAD_ECH_CSV/tmp_file $f
							    fi
						    done
                        fi

#*************************************************************** end csv restore **********************************************
	                    if [ $? -eq 0 ]
	                    then
	                        echo "*** Energy consumption data restored. ***" >> $LOG_DIR/backuprestore_error.log
	                        echo "*** Adding energy consumption indexes and constraints. This might take a long time depending on the data size. Please be patient. ***" >> $LOG_DIR/backuprestore_error.log
	                        psql -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE <  $ADDECINDEX
	                        psql -h $POSTGRESHOST -U $POSTGRESUSER $POSTGRESDATABASE <  $ADDPLECINDEX
	                        if [ $? -eq 0 ]
	                        then
	                            echo "*** Energy consumption indexed and constraints restored. ***" >> $LOG_DIR/backuprestore_error.log
	                        else
	                            echo "ERROR:: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
	                            status="1"
	                            return
	                        fi
	                    else
	                        echo "ERROR:: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
	                        status="1"
	                        return
	                    fi
	                else
	                    echo "ERROR:: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
	                    status="1"
	                    return
	                fi
	            else
	                echo "ERROR:: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
	                status="1"
	                return
	            fi
			else
            	gunzip -c $backupfile | psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE >> $LOG_DIR/backuprestore_error.log
			fi
            restoreStatus=$?
        else
            # Single backup file containing uncompressed SQL.
            psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE <  $backupfile >> $LOG_DIR/backuprestore_error.log
            restoreStatus=$?
        fi

        # Do cleanup after restoring from the cloud
        psql -h $POSTGRESHOST -U $POSTGRESUSER -d $POSTGRESDATABASE < $EM_MGMT_BASE/em_mgmt/adminscripts/cloud_backup_after_restore.sql >> $LOG_DIR/backuprestore_error.log

        if [ "${restoreStatus}" -eq 0 ]
        then
            echo "*** Restoration complete. ***" >> $LOG_DIR/backuprestore_error.log
        else
            echo "ERROR:: Internal error while restoring database $POSTGRESDATABASE. This is a very critical issue. Please raise an alarm."
            status="1"
            return
        fi
    fi

    # Check if we need to upgrade the database
	if [ "$numversiononsystem" -gt "$numversiononbackupfile" ]
	then
	    # Need to upgrade the version of the database to match the version of the current system
		if [ ! -f $UPGRADE_SQL_PATH ]
		then
    		echo "*** Upgrade SQL file not present on system. Please run it manually. ***" >> $LOG_DIR/backuprestore_error.log
		else
	       	echo "*** Performing an additional step of setting up database. Running $UPGRADE_SQL_PATH file. ***" >> $LOG_DIR/backuprestore_error.log
		    psql -U $POSTGRESUSER -h $POSTGRESHOST $POSTGRESDATABASE -f $UPGRADE_SQL_PATH

            if [ $? -ne 0 ]
		    then
				echo "*** Failed to run $UPGRADE_SQL_PATH file. Still proceeding.." >> $LOG_DIR/backuprestore_error.log
				status="1"
        		return
		    else
		        echo "*** Database is successfully upgraded." >> $LOG_DIR/backuprestore_error.log
		    fi
		fi
	fi

    # If EM is Cloud enabled, then ensure the WAL triggers are restored.
    if [ `psql -q -U $POSTGRESUSER -h $POSTGRESHOST $POSTGRESDATABASE -t -c "select 1 from system_configuration where name = 'cloud.communicate.type' and value = '2'"` -eq 1 ]
    then
        psql -U $POSTGRESUSER -h $POSTGRESHOST $POSTGRESDATABASE -c "select addTriggers();" >> $LOG_DIR/backuprestore_error.log
    fi

    echo "*** Database is successfully restored. ***" >> $LOG_DIR/backuprestore_error.log
    echo "step4;"

	echo "*** Overwriting configuration files ***" >> $LOG_DIR/backuprestore_error.log
	configBackupFileToRestore=`echo $(find ENL_all_config_backup*)`
	if [ -f "$configBackupFileToRestore" ]
	then
				echo "*** configBackupFileToRestore is $configBackupFileToRestore ***" >> $LOG_DIR/backuprestore_error.log
				sudo rm -rf /tmp/backuprestore
                mkdir /tmp/backuprestore
                sudo tar -xf $configBackupFileToRestore -C /tmp/backuprestore
                ##Check where this is 1004 or 1404
                is1004=false
                if [ -d "/tmp/backuprestore/var/lib/tomcat6"  ];
                then
                        is1004=true
                fi
                escape_enl_app_home=$(echo "$ENL_APP_HOME" | sed 's/\//\\\//g')
                CURR_D=`pwd`
                cd /tmp/backuprestore
                farray_Not_To_Copy=("/tmp/backuprestore/etc/sudoers" "/tmp/backuprestore/etc/default/dhcp3-server" )

                isMigrationFrom1004To1404=false;
                if $is1004; then
                        if [ $flag_os_greater_than_1004 -gt 0 ]
                        then
                                isMigrationFrom1004To1404=true
                        fi
                fi
                if $isMigrationFrom1004To1404;
                then

                	if [ -f "/tmp/backuprestore/etc/default/dhcp3-server" ]
                	then
                		. /tmp/backuprestore/etc/default/dhcp3-server
						sudo cp /etc/default/isc-dhcp-server /tmp/backuprestore/etc/default/isc-dhcp-server
						generic_insert_update_key_value_conf INTERFACES= "\"$INTERFACES\"" /tmp/backuprestore/etc/default/isc-dhcp-server
					fi
                fi
                for x in `find .`;
                do
                		boolToCopy=true
                        if [ -f "$x" ];
                        then
                                if $is1004; then
                                        cmd=$(ls -ltr $x | grep "var/lib/tomcat6")
                                        if [ $? -eq 0  ];
                                        then
                                                fileNew=`echo "$x" | sed -e "s/\.\/var\/lib\/tomcat6/$escape_enl_app_home/g"`
                                        else
                                        		filePath=`readlink -f $x`
                                        		containsElement "$filePath" "${farray_Not_To_Copy[@]}"
                                                if [ "$?" -eq 0  ];
                                                then
                                                        boolToCopy=false
                                                fi
                                                fileNew=`echo "$x" |  sed -e "s/\.\//\//"`
                                        fi
                                        if $boolToCopy; then
                                                if $isMigrationFrom1004To1404;
                                                then
                                                        ##############################restoring from 1004 to 14.04#################################
                                                        f=`readlink -f $x`
                                                        replaceWithNewEnvVarValues $f
                                                fi
                                        fi
                                else
                                        fileNew=`echo "$x" |  sed -e "s/\.\//\//"`
                                fi
                                if $boolToCopy; then
                                	echo "copying $x $fileNew" >> $LOG_DIR/backuprestore_error.log
                                	sudo cp $x $fileNew
                                fi
                        fi
                done;
                cd $CURR_D

				sudo dpkg-reconfigure --frontend noninteractive tzdata
				echo "*** System files are restored for RMA ***" >> $LOG_DIR/backuprestore_error.log
	else
				echo "*** Config backup file does not exists ***" >> $LOG_DIR/backuprestore_error.log
	fi
    rm -rf ./*



    return 0;
}

startTomcat() {
    startStatus=$(sudo $TOMCAT_SERVICE start N)
    if [[ "$startStatus" =~ "done" ]]
    then
        echo "*** Tomcat service is running. EM application should be up again ***" >> $LOG_DIR/backuprestore_error.log
        echo "*** NOTE: If you are not able to access EM application in another 2-3 minutes, please raise an alarm ***" >> $LOG_DIR/backuprestore_error.log
        return
    fi
    status="1"
    echo "*** ERROR:: TOMCAT RESTART FAILED. Please do it manually ***" >> $LOG_DIR/backuprestore_error.log
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
       echo "ERROR:: Cannot continue with backup. There is already an ongoing process doing some critical work. Please try again after some time."
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
            sed -i 's/\(<span\ id="maintenance">\)[a-zA-Z0-9]*\(<\/span>\)/\1Y\2/' $ENL_APP_HOME/webapps/ROOT/heartbeat.jsp
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
            sed -i 's/\(<span\ id="maintenance">\)[a-zA-Z0-9]*\(<\/span>\)/\1N\2/' $ENL_APP_HOME/webapps/ROOT/heartbeat.jsp
            mode=$(checkandsetemmode.sh "NORMAL")
        fi
        rm -rf "$WORKINGDIRECTORY/*"
    else
       status="1"
       echo "ERROR:: Cannot continue with restore. There is already an ongoing process doing some critical work. Please try again after some time."
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
