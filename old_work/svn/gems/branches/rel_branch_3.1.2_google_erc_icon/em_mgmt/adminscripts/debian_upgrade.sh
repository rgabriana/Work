#!/bin/bash

export LOG_DIR="/var/lib/tomcat6/Enlighted/adminlogs"
export LOG_HISTORY_DIR="/var/lib/tomcat6/Enlighted/adminlogs/history"

mkdir -p $LOG_HISTORY_DIR
mkdir -p $LOG_DIR


if [ -d "${LOG_HISTORY_DIR}/upgrade" ]
then
    content=$(find "${LOG_HISTORY_DIR}/upgrade" -type f)  
    if [ ! -z "${content}" ]
    then
        opTime=$(head -1 "${LOG_HISTORY_DIR}"/last_upgrade_time)
        cd ${LOG_HISTORY_DIR}
        tar -czf upgrade_${now}.tar.gz upgrade
        rm -rf ${LOG_HISTORY_DIR}/upgrade
    fi
fi

mkdir -p ${LOG_HISTORY_DIR}/upgrade
now=$(date '+%Y'-'%m'-'%d'_'%H'-'%M'-'%S')
echo "$now" > $LOG_HISTORY_DIR/last_upgrade_time

export EMS_MODE_FILE="/var/lib/tomcat6/Enlighted/emsmode"
export upgradeBackupDir="/var/lib/tomcat6/Enlighted/tempExtract"
export tomcatwebapp="/var/lib/tomcat6/webapps"
export communicatorjar="/opt/enLighted/communicator/ems_communicator.jar"
mkdir -p $upgradeBackupDir

DEBIAN_FILE_NAME=$(echo "$1" | sed 's/#/\ /g')
workingDirectory=$2
status="0"

if [ -z "$3" ]
then
    EM_REV_NUMBER="0"
else
    EM_REV_NUMBER=$(echo "$3" | cut -d '.' -f4)
fi

APP_ROOT=$4
if [ -z "$APP_ROOT" ]
then
    APP_ROOT="/var/www/em_mgmt/em_mgmt"
fi

IP_ADDRESS=$5
if [ -z "$IP_ADDRESS" ]
then
    IP_ADDRESS="127.0.0.1"
fi

#validate the no. of groups
ps -ef | grep postgresql | grep -v grep > /dev/null
if [ $? -eq 0 ]
then
  # verify database exists
  if [ `psql -q -U postgres -h localhost  -t -c "select count(*) from pg_database where datname='ems'"` -eq 1 ]
  then
    # verify that switch_fixtures table exists (v2.1)
    if [ `psql -q -U postgres -h localhost ems -t -c "select count(*) from pg_tables where tablename = 'switch_fixtures'"` -eq 1 ]
    then
      #check no. of groups for each sensor
      if [ `psql -q -U postgres -h localhost ems -t -c "select count(*) from (select fixture_id, count(*) from (select id, fixture_id, group_id from gems_group_fixture union select id, fixture_id, switch_id from switch_fixtures) AS sq group by fixture_id having count(*) > 10) AS sq2"` -gt 0 ]
      then
        echo "Upgrade cannot proceed as there are sensors which would be part of more than 10 groups after upgrade."
        exit 1;
      fi
    fi
  fi
fi

FORCE=$6

LOGS_ERR=$LOG_DIR/upgradegems_error.log
LOGS=$LOG_DIR/upgradegems.log
LOGS_DB=$LOG_DIR/dbupgrade.log

if [ "$FORCE" == "F" ]
then
    LOGS_ERR=$LOG_DIR/recover_error.log
    LOGS=$LOG_DIR/recover.log
fi

echo "" > $LOGS_ERR
echo "" > $LOGS

touch $LOG_DIR/backuprestore.log
touch $LOG_DIR/backuprestore_error.log
touch $LOG_DIR/recover_error.log
touch $LOG_DIR/recover.log
touch $LOG_DIR/temprecover.log
touch $LOG_DIR/dbupgrade.log
touch $LOG_DIR/dailybackup.log
touch $LOG_DIR/history/last_upgrade_time
touch $LOG_DIR/history/last_dbbackup_time
touch $LOG_DIR/history/last_dbrestore_time

AUDITLOGSCRIPT="$APP_ROOT/../adminscripts/auditlogs.sh"
BACKUPSCRIPT="$APP_ROOT/../adminscripts/backuprestoreguiaction.sh"

{

upgradeAll() {
    
    cd $workingDirectory
    
    DEBIAN_REV_NUMBER=$(dpkg-deb -f "$DEBIAN_FILE_NAME" CurrentRevision)

    DEBIAN_VALIDATION_VALUE=$(dpkg-deb -f "$DEBIAN_FILE_NAME" ValidationKey)

    if [ "$FORCE" != "F" ]
    then
        if [ -z "$DEBIAN_VALIDATION_VALUE" ]
        then
            echo "UPGRADE ERROR: Cannot upgrade to lower version"
            status="2"
            return
        fi

        if [ $DEBIAN_VALIDATION_VALUE != "enLighted" ]
        then
            echo "UPGRADE ERROR: The debian package is not valid."
            status="2"
            return
        fi	
        
        if [ -z "$DEBIAN_REV_NUMBER" ]
        then
            echo "UPGRADE ERROR: Cannot upgrade to lower version"
            status="2"
            return
        fi
        
        if [ $DEBIAN_REV_NUMBER -lt $EM_REV_NUMBER ]
        then
            echo "UPGRADE ERROR: Cannot upgrade to lower version"
            status="2"
            return
        fi
    fi
    
    echo "*** Preparing all EM components for upgrade ***" >> $LOGS_ERR

    #Client side warning about system upgrade.
    sleep 30

    sudo dpkg -i --force-overwrite "$DEBIAN_FILE_NAME"
    if [ $? -eq 0 ]
    then
        echo "step1;"
	    echo "*** Upgrading individual EM components ***" >> $LOGS_ERR
    else
	    echo "UPGRADE ERROR: Failed to prepare all EM components. Exit upgrade process."
        status="2"
        return
    fi
    
    /home/enlighted/upgrade_run.sh "$DEBIAN_FILE_NAME" "$2" "$3" "$4" "$5" "$6"
    status="$?"

    if [ "$status" -eq 3 ]
    then
        status="1"
        rollback="Y"
    fi

}

startTomcat() {
    startStatus=$(sudo /etc/init.d/tomcat6 start N)
    if [[ "$startStatus" =~ "done" ]]
    then
        echo "*** Tomcat6 service is running. EM application should be up again ***" >> $LOGS_ERR
        echo "*** NOTE: If you are not able to access EM application in another 2-3 minutes, please raise an alarm ***" >> $LOGS_ERR
        return
    fi
    status="1"
    echo "*** UPGRADE ERROR: TOMCAT6 RESTART FAILED. Please do it manually ***" >> $LOGS_ERR
}

upgradedate=""
mode="S"
if [ "$FORCE" != "F" ]
then
    if [ -f "$AUDITLOGSCRIPT" ]
    then
        mode=$(checkandsetemmode.sh "UPGRADE_RESTORE:$DEBIAN_FILE_NAME")
        auditinsert=$($AUDITLOGSCRIPT "add" "EM Management Upgrade/Restore" "EM upgrade process is initiated using $DEBIAN_FILE_NAME. Processing..." "$IP_ADDRESS")
        if [[ $auditinsert =~ "INSERT 0 1" ]]
        then
            auditid=$(echo $auditinsert | cut -d":" -f2)
        fi
    else
        upgradedate=$(date "+%Y-%m-%d %H:%M:%S")
        echo "UPGRADE_RESTORE:$DEBIAN_FILE_NAME" > $EMS_MODE_FILE
    fi
fi

echo "EMS_UPGRADE_STARTED"

if [ $mode == "S" ]
then
    
    if [ "$FORCE" != "F" ]
    then
        sed -i 's/\(<span\ id="maintenance">\)[a-zA-Z0-9]*\(<\/span>\)/\1Y\2/' /var/lib/tomcat6/webapps/ROOT/heartbeat.jsp
    fi

    upgradeAll

    if [ "$status" == "0" ]
    then
        if [ "$FORCE" != "F" ]
        then
            startTomcat            
            sleep 60
            rm -f $upgradeBackupDir/*_em_all.deb
            cd $workingDirectory
            cp $DEBIAN_FILE_NAME $upgradeBackupDir/
            cp /var/www/em_mgmt/adminscripts/debian_upgrade.sh "$upgradeBackupDir/debian_upgrade.sh"
            cp /var/www/em_mgmt/adminscripts/backuprestoreguiaction.sh "$upgradeBackupDir/backuprestoreguiaction.sh"
            cp /var/www/em_mgmt/adminscripts/recover.sh "$upgradeBackupDir/recover.sh"
            echo "*** NOTE: STARTING DATABASE BACKUP on new version with recent changes. You may track the PROGRESS ON BACKUP/RESTORE page. ***"  >> $LOGS_ERR
        fi
        echo "step8;"
    fi

    if [ "$FORCE" != "F" ]
    then
        if [ "$status" == "1" -a "$rollback" == "Y" ]
        then
            echo "*** NOTE: System is trying to recover itself.  ***"  >> $LOGS_ERR
            $upgradeBackupDir/recover.sh "FROM_UPGRADE"
        else
            sed -i 's/\(<span\ id="maintenance">\)[a-zA-Z0-9]*\(<\/span>\)/\1N\2/' /var/lib/tomcat6/webapps/ROOT/heartbeat.jsp

            if [ -f "$AUDITLOGSCRIPT" ]
            then
                mode=$(checkandsetemmode.sh "NORMAL")
            else
                echo "NORMAL" > $EMS_MODE_FILE
            fi
        fi

        if [ "$status" == "0" ]
        then
            if [ -f "$BACKUPSCRIPT" ]
            then
                /bin/bash $BACKUPSCRIPT "backup" "afterUpgradeDBBackup" "/opt/enLighted/DB/DBBK" "$APP_ROOT" "$IP_ADDRESS" &
            fi
        fi
    fi

else
   status="1"
   echo "UPGRADE ERROR: Cannot continue with upgrade. There is already an ongoing process doing some critical work. Please try again after some time."
fi

if [ "$FORCE" != "F" ]
then
    if [ -f "$AUDITLOGSCRIPT" ]
    then
        if [ "$status" == "0" ]
        then
            if [ -z "$upgradedate" ]
            then
                updateoutput=$($AUDITLOGSCRIPT "update" "" " Successful." "" "$auditid")
            else
                auditinsert=$($AUDITLOGSCRIPT "add" "EM Management Upgrade/Restore" "EM upgrade process is initiated using $DEBIAN_FILE_NAME. Processing... Successful." "$IP_ADDRESS" "" "$upgradedate" )
            fi
        else
            if [ -z "$upgradedate" ]
            then
                updateoutput=$($AUDITLOGSCRIPT "update" "" " Failed." "" "$auditid")
            else 
                auditinsert=$($AUDITLOGSCRIPT "add" "EM Management Upgrade/Restore" "EM upgrade process is initiated using $DEBIAN_FILE_NAME. Processing... Failed." "$IP_ADDRESS" "" "$upgradedate" )
            fi
        fi
    fi
fi
    
} > $LOGS 2>> $LOGS_ERR

cp $LOGS ${LOG_HISTORY_DIR}/upgrade/
cp $LOGS_ERR ${LOG_HISTORY_DIR}/upgrade/
cp $LOGS_DB ${LOG_HISTORY_DIR}/upgrade/

opTime=$(head -1 "${LOG_HISTORY_DIR}"/last_upgrade_time)
cd ${LOG_HISTORY_DIR}
tar -czf upgrade_${opTime}.tar.gz upgrade
rm -rf ${LOG_HISTORY_DIR}/upgrade
