#!/bin/bash

export DEFAULT_TOMCAT_PROP_FILE="/var/lib/tomcat6/Enlighted/tomcat.properties"
export EMS_MODE_FILE="/var/lib/tomcat6/Enlighted/emsmode"

{
logger -p 6 -t emsdashboard_heartbeat_tracker "Checking connectivity to EMS Dashboard"

if [ -f ${EMS_MODE_FILE} ]
then
    emsmode=$(head -n 1 ${EMS_MODE_FILE})
    if [[ $emsmode =~ "TOMCAT_SHUTDOWN" ]]
    then
        exit 0
    fi
else
    echo "NORMAL" > $EMS_MODE_FILE
    chmod 777 "$EMS_MODE_FILE"
    exit
fi

managerUser="admin"
managerPass="admin"
userpass=$managerUser":"$managerPass
if [ -f ${DEFAULT_TOMCAT_PROP_FILE} ]
then
    userpass=$(head -n 1 ${DEFAULT_TOMCAT_PROP_FILE})
fi

managerList=$(wget --no-check-certificate https://$userpass@localhost/manager/list -O - -q)

if [[ $managerList =~ "ems_dashboard:running" ]]
then
    pingStatus=$(wget --no-check-certificate --timeout=120 --tries=3 https://localhost/ems_dashboard/heartbeat.jsp 2>&1)
    if [[ $pingStatus =~ "200 OK" ]]
    then
        exit 0
    else
        emsmode=$(head -n 1 ${EMS_MODE_FILE})
        if [[ $emsmode =~ "TOMCAT_SHUTDOWN" ]]
        then
            exit 0
        fi
        /etc/init.d/tomcat6 restart
        logger -p 6 -t emsdashboard_heartbeat_tracker "Tomcat rebooted as connectivity to EMS Dashboard is lost"
    fi        
else
    pingStatus=$(wget --no-check-certificate --timeout=120 --tries=3 https://localhost/ems_dashboard/heartbeat.jsp 2>&1)
    if [[ $pingStatus =~ "200 OK" ]]
    then
        exit
    else
        emsmode=$(head -n 1 ${EMS_MODE_FILE})
        if [[ $emsmode =~ "TOMCAT_SHUTDOWN" ]]
        then
            exit 0
        fi
        /etc/init.d/tomcat6 restart
        logger -p 6 -t emsdashboard_heartbeat_tracker "Tomcat rebooted as connectivity to EMS Dashboard is lost"
    fi
fi

}
