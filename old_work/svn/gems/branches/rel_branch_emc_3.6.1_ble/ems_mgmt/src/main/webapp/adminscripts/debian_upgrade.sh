#!/bin/bash
source /etc/environment
export LOG_DIR="$ENL_APP_HOME/Enlighted/adminlogs"
export DEFAULT_TOMCAT_PROP_FILE="$ENL_APP_HOME/Enlighted/tomcat.properties"
export EMS_MODE_FILE="$ENL_APP_HOME/Enlighted/emsmode"
export POSTGRESHOST="localhost"
export POSTGRESUSER="postgres"
export POSTGRESDATABASE="ems"
mkdir -p $LOG_DIR
echo "" > $LOG_DIR/upgradegems_error.log
echo "" > $LOG_DIR/upgradegems.log


{

DEBIAN_FILE_NAME=$(echo "$1" | sed 's/#/\ /g')
workingDirectory=$2
managerUser="admin"
managerPass="admin"

userpass=$managerUser":"$managerPass
if [ -f ${DEFAULT_TOMCAT_PROP_FILE} ]
then
    userpass=$(head -n 1 ${DEFAULT_TOMCAT_PROP_FILE})
fi

upgradegems() {

    echo "EMS_UPGRADE_STARTED"

	cd $workingDirectory

	echo "*** Starting EMS upgrade ***" >> $LOG_DIR/upgradegems_error.log
    echo "*** Check if EMS is running ***" >> $LOG_DIR/upgradegems_error.log
    managerList=$(wget --no-check-certificate https://$userpass@localhost/manager/list -O - -q)
    echo "*** ***" >> $LOG_DIR/upgradegems_error.log
    echo $managerList >> $LOG_DIR/upgradegems_error.log
    echo "*** ***" >> $LOG_DIR/upgradegems_error.log
    if [[ $managerList =~ "ems:running" ]]
    then
        echo "*** EMS is running ***" >> $LOG_DIR/upgradegems_error.log
        echo "*** Stopping EMS ***" >> $LOG_DIR/upgradegems_error.log

        echo "UPGRADE_RESTORE" > $EMS_MODE_FILE

        stopStatus=$(wget --no-check-certificate https://$userpass@localhost/manager/stop?path=/ems -O - -q)
        sleep 10
        echo "*** ***" >> $LOG_DIR/upgradegems_error.log
        echo $stopStatus >> $LOG_DIR/upgradegems_error.log
        echo "*** ***" >> $LOG_DIR/upgradegems_error.log

        if [[ $stopStatus =~ "OK" ]]
        then
            echo "*** EMS is down ***" >> $LOG_DIR/upgradegems_error.log
            echo "*** Killing any active sessions left on database ***" >> $LOG_DIR/upgradegems_error.log
            psql -h $POSTGRESHOST -U $POSTGRESUSER -c "SELECT pg_terminate_backend(pg_stat_activity.procpid) from pg_stat_activity where pg_stat_activity.datname = '${POSTGRESDATABASE}';"
            if [ $? -eq 0 ]
            then
                echo "*** Sessions destroyed ***" >> $LOG_DIR/upgradegems_error.log
            else
                echo "*** Could not destroy database sessions successfully. Restarting ems application. Please try again later. ****" >> $LOG_DIR/upgradegems_error.log
                restartTomcat
                echo "ERROR: Could not destroy database sessions successfully. Restarting ems application. Please try again later."
                return
            fi
        else
            echo "ERROR: Some problem while stopping ems. Please try again."
            return
        fi
        
    else
        echo "*** EMS is not running ***" >> $LOG_DIR/upgradegems_error.log
    fi
    echo "step1;"
	sudo dpkg -i --force-overwrite "$DEBIAN_FILE_NAME"

	if [ $? -eq 0 ]
	then
		echo "*** UPGRADE SUCCESSFUL. Starting up upgraded EMS. ***" >> $LOG_DIR/upgradegems_error.log
        echo "step2;"
        restartTomcat
        echo "step3;"
	else
		echo "*** UPGRADE UNSUCCESSFUL. Starting up existing EMS again. ***" >> $LOG_DIR/upgradegems_error.log
        restartTomcat
        echo "ERROR: Upgrade was unsuccessful. Please check the logs for more details."
	fi

}

restartTomcat() {

    echo "TomcatRestart;"
    echo "*** Restarting tomcat service. PLEASE DO NOT CLOSE/REFRESH THE PAGE or you might not be able to check the status. ***" >> $LOG_DIR/upgradegems_error.log
    sleep 60
    stopStatus=$(sudo $TOMCAT_SERVICE stop)
    #echo "*** $stopStatus ***"  >> $LOG_DIR/upgradegems_error.log
    if [[ "$stopStatus" =~ "done" ]]
    then
        startStatus=$(sudo $TOMCAT_SERVICE start)
        #echo "*** $startStatus ***"  >> $LOG_DIR/upgradegems_error.log
        if [[ "$startStatus" =~ "done" ]]
        then
            sleep 30
            echo "TomcatRestartSuccess";
            echo "*** Tomcat restart successful. Ems application should be up again. ***" >> $LOG_DIR/upgradegems_error.log
            echo "*** NOTE: If you are not able to access ems application in another 2-3 minutes, please raise an alarm. ***" >> $LOG_DIR/upgradegems_error.log
            return
        fi
    fi
    echo "TomcatRestartFailed;"
    echo "*** TOMCAT RESTART FAILED. PLEASE DO IT MANUALLY. ***" >> $LOG_DIR/upgradegems_error.log	

}
    upgradegems
    echo "NORMAL" > $EMS_MODE_FILE
    
} > $LOG_DIR/upgradegems.log 2>> $LOG_DIR/upgradegems_error.log
