#!/bin/bash
source /etc/environment
export DEFAULT_TOMCAT_PROP_FILE="$ENL_APP_HOME/Enlighted/tomcat.properties"

{
logger -p 6 -t ems_memory_tracker "Checking out of memory condition for EM"
managerUser="admin"
managerPass="admin"
userpass=$managerUser":"$managerPass
if [ -f ${DEFAULT_TOMCAT_PROP_FILE} ]
then
    userpass=$(head -n 1 ${DEFAULT_TOMCAT_PROP_FILE})
fi

memTrackerRunning=$(ps aux | grep '[g]rep java.lang.OutOfMemoryError')
hasError=$(grep 'java.lang.OutOfMemoryError' $ENL_APP_HOME/logs/catalina.out)

if [ -n "$memTrackerRunning" ]

then
   logger -p 6 -t ems_memory_tracker  "memory tracker running"
else
   logger -p 6 -t ems_memory_tracker  "memory tracker not running"	
   if [ -n "$hasError" ]
   then
    service $TOMCAT_SUDO_SERVICE stop
    logger -p 6 -t ems_memory_tracker "Tomcat stopped"	
    sleep 30
    managerList=$(wget --no-check-certificate https://$userpass@localhost/manager/list -O - -q)
    while [ -n "$managerList" ]
    do
        sleep 30
        managerList=$(wget --no-check-certificate https://$userpass@localhost/manager/list -O - -q)
    done
    timeString=$(date +"%m-%d-%y-%T")
    mv $ENL_APP_HOME/logs/catalina.out $ENL_APP_HOME/logs/catalina.out_$timeString
    rm -rf $ENL_APP_HOME/work/Catalina/localhost/ems
    service $TOMCAT_SUDO_SERVICE start
    logger -p 6 -t ems_memory_tracker "Tomcat rebooted as out of memory detected"
   fi	
fi
}