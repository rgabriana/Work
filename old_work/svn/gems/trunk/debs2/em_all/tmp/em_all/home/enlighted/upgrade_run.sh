#!/bin/bash
source /etc/environment

export LOG_DIR="$ENL_APP_HOME/Enlighted/adminlogs"
export EMS_MODE_FILE="$ENL_APP_HOME/Enlighted/emsmode"
mkdir -p $LOG_DIR

LOGS_ERR=$LOG_DIR/upgradegems_error.log
LOGS=$LOG_DIR/upgradegems.log
LOGS_DB=$LOG_DIR/dbupgrade.log

export upgradeBackupDir="$ENL_APP_HOME/Enlighted/tempExtract"
export tomcatwebapp="$ENL_APP_HOME/webapps"
export communicatorjar="$OPT_ENLIGHTED/communicator/em_cloud_communicator.jar"
mkdir -p $upgradeBackupDir

########################################################################################################################
# NOTE: This script is meant to work for normal installs and also for Google's special package. When running for       #
# Google's special package, the variable $GOOGLE_INSTALL will be defined to allow us to get the rev num in a different #
# way than the normal query of the em_all.deb file.                                                                    #
########################################################################################################################
DEBIAN_FILE_NAME=$(echo "$1")
if [ -z "$GOOGLE_INSTALL" ];then
    DEBIAN_REV_NUMBER=$(dpkg-deb -f "$DEBIAN_FILE_NAME" CurrentRevision)
else
    DEBIAN_REV_NUMBER=`cat $GOOGLE_INSTALL/version.txt`
fi

workingDirectory=$2

APP_ROOT=$4
if [ -z "$APP_ROOT" ]
then
    APP_ROOT="$EM_MGMT_BASE/em_mgmt/em_mgmt"
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

echo "ENL_APP_HOME IS $ENL_APP_HOME" >> $LOGS_ERR
AUDITLOGSCRIPT="$APP_ROOT/../adminscripts/auditlogs.sh"
BACKUPSCRIPT="$APP_ROOT/../adminscripts/backuprestoreguiaction.sh"



if [ "$FRESH_INSTALL" != "T" ]
then
	echo "step2;"

	echo "*** Stopping $TOMCAT_SUDO_SERVICE service ***"
	stopStatus=$(sudo $TOMCAT_SERVICE stop)
	if [[ "$stopStatus" =~ "done" ]]
	then
    	echo "*** Tomcat is down ***";
	else
    	echo "UPGRADE ERROR: Failed to stop tomcat service. Exit upgrade process."
    	exit 1
	fi
fi


cd /tmp/em_all/home/enlighted/debs

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



#################################################################################################################
### IMP NOTE: Please change and remove the following echo statements if you decided ##################################
### in future to do something in this step4. This is because our upgradegem.log checks these words to show the 
### upgradation status ##
###################################    START  ####################################################
echo "step4;"
echo "*** EM System upgraded successfully ***" >> $LOGS_ERR
###################################    END  ####################################################

echo "*** Uninstalling avahi packages ***" >> $LOGS_ERR
if [ -z "$GOOGLE_INSTALL" ];then
    /tmp/em_all/home/enlighted/avahiremoval.sh
else
    /usr/local/google$ENLIGHTED_HOME/scripts/avahiremoval.sh
fi
 
####################################################################################################
####################Upgrading communicator STARTS #############################
####################################################################################################
echo "*** Upgrading EM Communicator... ***" >> $LOGS_ERR
sudo dpkg -i --force-overwrite ./*_cloud_communicator.deb
if [ $? -eq 0 ]
then
    echo "step5;"
    echo "*** EM Communicator upgraded successfully ***" >> $LOGS_ERR
else
    echo "UPGRADE ERROR: EM Communicator upgrade failed. Exit upgrade process and bringing up tomcat server."
    exit 3
fi

####################################################################################################
####################Upgrading communicator ENDS #############################
####################################################################################################

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
										UPGRADESQLPATH=/tmp/ems/home/enlighted/upgradeSQL.sql
                                        echo "*** Upgrading database... ***" >> $LOGS_ERR
										DBUSER=postgres
										DBHOST=localhost
										DB=ems

                                        cp /tmp/ems/home/enlighted/upgradeSQL.sql $ENL_APP_HOME/Enlighted/tempExtract/${DEBIAN_REV_NUMBER}_upgradeSQL.sql

                                        if [ `psql -q -U postgres ems -h localhost  -t -c "select count(*) from system_configuration where name = 'cloud.communicate.type' and value = '2'"` -gt 0 ] 
                                        then
                                            newid=$(/usr/bin/psql -x -U $DBUSER $DB -h $DBHOST -c "select nextval('wal_logs_seq')" | grep nextval | cut -d " " -f3)
                                            /usr/bin/psql -U $DBUSER $DB -h $DBHOST -c "insert into wal_logs (id, creation_time , action, table_name, sql_statement) values ($newid, current_timestamp, 'UPGRADE', '$ENL_APP_HOME/Enlighted/tempExtract/${DEBIAN_REV_NUMBER}_upgradeSQL.sql', '')" >> $LOGS_DB 2>> $LOGS_DB
                                        fi

										echo "startdbupgrade;"

									    psql -U $DBUSER -h $DBHOST $DB < $UPGRADESQLPATH >> $LOGS_DB 2>> $LOGS_DB

                                        if [ `psql -q -U postgres ems -h localhost  -t -c "select count(*) from system_configuration where name = 'cloud.communicate.type' and value = '2'"` -gt 0 ] 
                                        then
                                            psql -U $DBUSER -h $DBHOST $DB < /tmp/ems/home/enlighted/sppa.sql >> $LOGS_DB 2>> $LOGS_DB
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
                    sudo /etc/init.d/apache2 reload
                                        echo "*** Please wait..Forece reloading Apache2 server ***" >> $LOGS_ERR
                    sleep 5
					startapache=$(sudo /etc/init.d/apache2 status)
					if [[ "$startapache" =~ "is running" ]]
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
