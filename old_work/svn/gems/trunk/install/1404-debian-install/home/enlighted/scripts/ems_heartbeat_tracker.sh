#!/bin/bash

export EMS_MODE_FILE="/var/lib/tomcat6/Enlighted/emsmode"

{
logger -p 6 -t ems_heartbeat_tracker "Checking connectivity to EM"

if [ -f ${EMS_MODE_FILE} ]
then
    emsmode=$(head -n 1 ${EMS_MODE_FILE})
    if [[ $emsmode =~ "UPGRADE_RESTORE" ]]
    then
        exit 0
    fi
    if [[ $emsmode =~ "TOMCAT_SHUTDOWN" ]]
    then
        exit 0
    fi
else
    echo "NORMAL" > $EMS_MODE_FILE
    chmod 777 "$EMS_MODE_FILE"
    exit
fi

pingStatus=$(wget --no-check-certificate --timeout=10 --tries=3 http://localhost:9090/ems/heartbeat.jsp 2>&1)
rm -f heartbeat.jsp*
if [[ $pingStatus =~ "200 OK" ]]
then
    exit 0
else
    sleep 300
    pingStatus=$(wget --no-check-certificate --timeout=10 --tries=3 http://localhost:9090/ems/heartbeat.jsp 2>&1)
    rm -f heartbeat.jsp*
    if [[ $pingStatus =~ "200 OK" ]]
    then
        exit 0
    else
       sleep 300
	    pingStatus=$(wget --no-check-certificate --timeout=10 --tries=3 http://localhost:9090/ems/heartbeat.jsp 2>&1)
	    rm -f heartbeat.jsp*
	    if [[ $pingStatus =~ "200 OK" ]]
	    then
	        exit 0
	    else
	        emsmode=$(head -n 1 ${EMS_MODE_FILE})
	        if [[ $emsmode =~ "UPGRADE_RESTORE" ]]
	        then
	            exit 0
	        fi
	        if [[ $emsmode =~ "TOMCAT_SHUTDOWN" ]]
	        then
	            exit 0
	        fi
	        /etc/init.d/tomcat6 restart
	        logger -p 6 -t ems_heartbeat_tracker "Tomcat rebooted as connectivity to EM is lost"
	    fi
    fi
fi

}