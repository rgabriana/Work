#!/bin/bash

export LOG_DIR="/var/lib/tomcat6/Enlighted/adminlogs"
export EMS_MODE_FILE="/var/lib/tomcat6/Enlighted/emsmode"
mkdir -p $LOG_DIR

LOGS_ERR=$LOG_DIR/upgradegems_error.log
LOGS=$LOG_DIR/upgradegems.log
LOGS_DB=$LOG_DIR/dbupgrade.log

export upgradeBackupDir="/var/lib/tomcat6/Enlighted/tempExtract"
export tomcatwebapp="/var/lib/tomcat6/webapps"
export communicatorjar="/opt/enLighted/communicator/em_cloud_communicator.jar"
mkdir -p $upgradeBackupDir

DEBIAN_FILE_NAME=$(echo "$1")
DEBIAN_REV_NUMBER=$(dpkg-deb -f "$DEBIAN_FILE_NAME" CurrentRevision)
workingDirectory=$2

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

FORCE=$6
FRESH_INSTALL=$7

if [ "$FORCE" == "F" ]
then
    LOGS_ERR=$LOG_DIR/recover_error.log
    LOGS=$LOG_DIR/recover.log
fi

AUDITLOGSCRIPT="$APP_ROOT/../adminscripts/auditlogs.sh"
BACKUPSCRIPT="$APP_ROOT/../adminscripts/backuprestoreguiaction.sh"

if [ "$FORCE" != "F" ]
then
    ps -ef | grep postgresql | grep -v grep > /dev/null
    if [ $? -eq 0 ]
    then
        if [ `psql -q -U postgres -h localhost  -t -c "select count(*) from pg_database where datname='ems'"` -eq 1 ]
        then
            rm -f $upgradeBackupDir/beforeUpgradeDBBackup*
            rm -f $upgradeBackupDir/ems_dump*

            echo "*** Taking database backup ***"  >> $LOGS_ERR
            if [ -f "$BACKUPSCRIPT" ]
            then
                /bin/bash $BACKUPSCRIPT "backup" "beforeUpgradeDBBackup" "/opt/enLighted/DB/DBBK" "$APP_ROOT" "$IP_ADDRESS" "F"
                backupexists=$(ls $upgradeBackupDir/beforeUpgradeDBBackup*tar.gz)
                if [ -z "$backupexists" ]
                then
                    echo "UPGRADE ERROR: Database backup was not successful. Cannot continue with upgrade."
                    exit 2
                fi
            else
                POSTGRESUSER=postgres
                POSTGRESHOST=localhost
                POSTGRESDATABASE=ems
                /usr/bin/pg_dump -i -U $POSTGRESUSER -h $POSTGRESHOST -F c -b -f  "$upgradeBackupDir/beforeUpgradeDBBackup.backup" $POSTGRESDATABASE
                if [ $? -ne 0 ]
                then
                    echo "UPGRADE ERROR: Database backup was not successful. Cannot continue with upgrade."
                    exit 2
                fi
            fi
        else
            echo "*** First time install ***"  >> $LOGS_ERR
        fi
    else
        echo "UPGRADE ERROR: Postgres not running. Cannot take database backup. Stopping upgrade process."
        exit 2
    fi

    echo "*** Taking application backup ***" >> $LOGS_ERR
    rm -f $upgradeBackupDir/*.war
    rm -f $upgradeBackupDir/*.jar

    cp /var/www/em_mgmt/adminscripts/debian_upgrade.sh "$upgradeBackupDir/debian_upgrade.sh"
    cp /var/www/em_mgmt/adminscripts/backuprestoreguiaction.sh "$upgradeBackupDir/backuprestoreguiaction.sh"
    cp /var/www/em_mgmt/adminscripts/recover.sh "$upgradeBackupDir/recover.sh"

    lastUpgradeDeb=$(ls -lt $upgradeBackupDir/*.deb | head -n 1)
    if [[ "$lastUpgradeDeb" =~ "em_all.deb" ]]
    then
        echo "*** Application backup successful ***"  >> $LOGS_ERR
    else
        cp $communicatorjar "$upgradeBackupDir/"
        cp $tomcatwebapp/*.war "$upgradeBackupDir/"
        echo "*** Application backup successful ***"  >> $LOGS_ERR
    fi
fi

if [ "$FRESH_INSTALL" != "T" ]
then
	echo "step2;"

	echo "*** Stopping tomcat6 service ***"
	stopStatus=$(sudo /etc/init.d/tomcat6 stop N)
	if [[ "$stopStatus" =~ "done" ]]
	then
    	echo "*** Tomcat6 is down ***";
	else
    	echo "UPGRADE ERROR: Failed to stop tomcat6 service. Exit upgrade process."
    	exit 1
	fi
fi

cd $HOME/debs/

echo "startemupgrade;"

echo "*** Upgrading EM App... ***" >> $LOGS_ERR
sudo dpkg -i --force-overwrite ./*_enLighted.deb
if [ $? -eq 0 ]
then
    echo "step3;"
    echo "*** EM App upgraded successfully ***" >> $LOGS_ERR
else
    echo "UPGRADE ERROR: EM App upgrade failed. Exit upgrade process and bringing up tomcat server."
    exit 3
fi


echo "*** Upgrading EM Communicator... ***" >> $LOGS_ERR
sudo dpkg -i --force-overwrite ./*_em_cloud_communicator.deb
if [ $? -eq 0 ]
then
    echo "step5;"
    echo "*** EM Communicator upgraded successfully ***" >> $LOGS_ERR
else
    echo "UPGRADE ERROR: EM Communicator upgrade failed. Exit upgrade process and bringing up tomcat server."
    exit 3
fi

echo "*** Upgrading EM Management... ***" >> $LOGS_ERR
sudo dpkg -i --force-overwrite ./*_em_mgmt.deb
if [ $? -eq 0 ]
then
    echo "*** EM Management upgraded successfully ***" >> $LOGS_ERR
	if [ "$FRESH_INSTALL" != "T" ]
	then
					echo "step6;"

						ps -ef | grep postgresql | grep -v grep > /dev/null
						if [ $? -eq 0 ]
						then
								if [ `psql -q -U postgres -h localhost  -t -c "select count(*) from pg_database where datname='ems'"` -eq 1 ]
								then
										#--- Path of upgrade sql.. Assuming it'll have been copied by now..
										UPGRADESQLPATH=$HOME/upgradeSQL.sql

                                        echo "*** Upgrading database... ***" >> $LOGS_ERR
										DBUSER=postgres
										DBHOST=localhost
										DB=ems

                                        cp /usr/local/google/home/enlighted/upgradeSQL.sql /var/lib/tomcat6/Enlighted/tempExtract/${DEBIAN_REV_NUMBER}_upgradeSQL.sql

                                        if [ `psql -q -U postgres ems -h localhost  -t -c "select 1 from system_configuration where name = 'cloud.communicate.type' and value = '2'"` -eq 1 ] 
                                        then
                                            newid=$(/usr/bin/psql -x -U $DBUSER $DB -h $DBHOST -c "select nextval('wal_logs_seq')" | grep nextval | cut -d " " -f3)
                                            /usr/bin/psql -U $DBUSER $DB -h $DBHOST -c "insert into wal_logs (id, creation_time , action, table_name, sql_statement) values ($newid, current_timestamp, 'UPGRADE', '/var/lib/tomcat6/Enlighted/tempExtract/${DEBIAN_REV_NUMBER}_upgradeSQL.sql', '')" >> $LOGS_DB 2>> $LOGS_DB
                                        fi

										echo "startdbupgrade;"

									    psql -U $DBUSER -h $DBHOST $DB < $UPGRADESQLPATH >> $LOGS_DB 2>> $LOGS_DB

                                        if [ `psql -q -U postgres ems -h localhost  -t -c "select 1 from system_configuration where name = 'cloud.communicate.type' and value = '2'"` -eq 1 ] 
                                        then
                                            psql -U $DBUSER -h $DBHOST $DB < /usr/local/google/home/enlighted/sppa.sql >> $LOGS_DB 2>> $LOGS_DB
                                        fi
                                        
										if [ $? -eq 0 ]
                                        then
										    echo "*** Database upgrade completed successfully ***" >> $LOGS_ERR
                                        else
									        echo "UPGRADE ERROR: Database was not upgraded succesfully. Could not continue with the upgrade."
											exit 3
										fi
								else 
										echo "UPGRADE ERROR: Database was not upgraded succesfully. Could not continue with the upgrade."
										exit 3
								fi
						else
										echo "UPGRADE ERROR: database server is not running. Could not continue with the upgrade."
										exit 3
						fi

						echo "step7;"

					sleep 10
					echo "*** Restarting Apache2 server ***" >> $LOGS_ERR
					startapache=$(sudo /etc/init.d/apache2 restart)
					if [[ "$startapache" =~ "done" ]]
					then
							 echo "*** Apache2 service is up. ***" >> $LOGS_ERR
					else
							echo "UPGRADE ERROR: Failed to start Apache2 service. Contact enlighted admin."
							exit 3
					fi
	fi
else
    echo "UPGRADE ERROR: EM Management upgrade failed. Exit upgrade process and bringing up tomcat server."
    exit 3
fi
