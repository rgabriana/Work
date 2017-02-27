#!/bin/bash

export POSTGRESHOST="localhost"
export POSTGRESUSER="postgres"
export POSTGRESDATABASE="ems"

export upgradeBackupDir="/var/lib/tomcat6/Enlighted/tempExtract"
export tomcatwebapp="/var/lib/tomcat6/webapps"
export communicatorjar="/opt/enLighted/communicator/ems_communicator.jar"
export LOG_DIR="/var/lib/tomcat6/Enlighted/adminlogs"
export EMS_MODE_FILE="/var/lib/tomcat6/Enlighted/emsmode"

BACKUPSCRIPT="/var/lib/tomcat6/Enlighted/tempExtract/backuprestoreguiaction.sh"
UPGRADESCRIPT="/var/lib/tomcat6/Enlighted/tempExtract/debian_upgrade.sh"

AUDITLOGSCRIPT="/var/www/em_mgmt/adminscripts/auditlogs.sh"

recovermode="$1"

echo "" > $LOG_DIR/temprecover.log
chmod 777 $LOG_DIR/temprecover.log

{

currentmode=$(head -n 1 $EMS_MODE_FILE)

if [[ $currentmode =~ "UPGRADE_RESTORE" ]]
then
    if [[ $currentmode =~ "em_all.deb" ]]
    then
        emupgrade=$(grep startemupgrade /var/lib/tomcat6/Enlighted/adminlogs/upgradegems.log)
        dbupgrade=$(grep startdbupgrade /var/lib/tomcat6/Enlighted/adminlogs/upgradegems.log)
        if [[ "$emupgrade" =~ "startemupgrade" ]]
        then
            echo "*** Upgrade failed due to unexpected error. Trying to rollback to previous steady state. This might take upto 20-30 minutes. ***" >> /var/lib/tomcat6/Enlighted/adminlogs/upgradegems_error.log
            filename=$(ls $upgradeBackupDir/*em_all.deb | head -n 1 | cut -d"/" -f7)
            if [ ! -z "$filename" ]
            then
                /bin/bash $upgradeBackupDir/debian_upgrade.sh $filename $upgradeBackupDir/ 0.0.0.0 /var/www/em_mgmt/em_mgmt 127.0.0.1 F

                if [[ "$dbupgrade" =~ "startdbupgrade" ]]
                then
                    echo "*** Restoring the database. This might take a long time depending on the size of data. Please be patient. ***" >> /var/lib/tomcat6/Enlighted/adminlogs/upgradegems_error.log
                    filename=$(ls $upgradeBackupDir/beforeUpgradeDBBackup*tar.gz | head -n 1 | cut -d"/" -f7)
                    if [ ! -z "$filename" ]
                    then
                        /bin/bash $upgradeBackupDir/backuprestoreguiaction.sh "restore" $filename $upgradeBackupDir /var/www/em_mgmt/em_mgmt 127.0.0.1 "F"
                    fi
                fi

                sudo /etc/init.d/tomcat6 restart

                echo "NOTE: Please check if your em is restored to previous version and your data is in order. If not, contact enlighted admin." >> /var/lib/tomcat6/Enlighted/adminlogs/upgradegems_error.log
                echo "ERROR: Upgrade failed" >> /var/lib/tomcat6/Enlighted/adminlogs/upgradegems.log
            else
                echo "*** Backup doesn't exist ***" >> /var/lib/tomcat6/Enlighted/adminlogs/upgradegems_error.log
                echo "ERROR: Upgrade failed" >> /var/lib/tomcat6/Enlighted/adminlogs/upgradegems.log
            fi
        fi
        
        if [ -f "$AUDITLOGSCRIPT" -a "$recovermode" != "FROM_UPGRADE" ]
        then
            auditinsert=$($AUDITLOGSCRIPT "add" "EM Management Upgrade/Restore" "EM upgrade process was interrupted because of system failure. System failure recovery process has tried to rollback the em to previous stable state." "127.0.0.1")
        fi

        sed -i 's/\(<span\ id="maintenance">\)[a-zA-Z0-9]*\(<\/span>\)/\1N\2/' /var/lib/tomcat6/webapps/ROOT/heartbeat.jsp

        if [ -f "/bin/checkandsetemmode.sh" ]
        then
            mode=$(checkandsetemmode.sh "NORMAL")
        else
            echo "NORMAL" > $EMS_MODE_FILE
        fi

    else
        filename=$(head -n 1 $EMS_MODE_FILE | cut -d":" -f2)

        if [ -f "$AUDITLOGSCRIPT" -a "$recovermode" != "FROM_UPGRADE" ]
        then
            auditinsert=$($AUDITLOGSCRIPT "add" "EM Management Upgrade/Restore" "EM Database restore process was interuppted due to system failure. It is re-initiated on system start-up using file $filename." "127.0.0.1" )
        fi

        /bin/bash /var/www/em_mgmt/adminscripts/backuprestoreguiaction.sh "restore" $filename /var/lib/tomcat6/Enlighted/bkRestoreFolderTemp /var/www/em_mgmt/em_mgmt 127.0.0.1 "R"

        sudo /etc/init.d/tomcat6 restart

        if [ -f "$AUDITLOGSCRIPT" -a "$recovermode" != "FROM_UPGRADE" ]
        then
            auditinsert=$($AUDITLOGSCRIPT "add" "EM Management Upgrade/Restore" "EM Database restore process was interuppted due to system failure. It was re-initiated on system start-up using file $filename." "127.0.0.1" )
        fi

        sed -i 's/\(<span\ id="maintenance">\)[a-zA-Z0-9]*\(<\/span>\)/\1N\2/' /var/lib/tomcat6/webapps/ROOT/heartbeat.jsp

        if [ -f "/bin/checkandsetemmode.sh" ]
        then
            mode=$(checkandsetemmode.sh "NORMAL")
        else
            echo "NORMAL" > $EMS_MODE_FILE
        fi
    fi 

else 
    if [[ $currentmode =~ "NORMAL:BACKUP" ]]
    then
        if [ -f "$AUDITLOGSCRIPT" -a "$recovermode" != "FROM_UPGRADE" ]
        then
            auditinsert=$($AUDITLOGSCRIPT "add" "EM Management" "EM database backup process was interrupted because of system failure." "127.0.0.1")
        fi
    fi

    sed -i 's/\(<span\ id="maintenance">\)[a-zA-Z0-9]*\(<\/span>\)/\1N\2/' /var/lib/tomcat6/webapps/ROOT/heartbeat.jsp

    emsmode=$(head -n 1 $EMS_MODE_FILE)
    while [[ "$emsmode" =~ "NORMAL:" ]]
    do
        if [ -f "/bin/checkandsetemmode.sh" ]
        then
            mode=$(checkandsetemmode.sh "NORMAL")
        else
            echo "NORMAL" > $EMS_MODE_FILE
        fi
        emsmode=$(head -n 1 $EMS_MODE_FILE)
    done
    mode=$(checkandsetemmode.sh "NORMAL")

fi

} > $LOG_DIR/temprecover.log 2>> $LOG_DIR/temprecover.log


