#!/bin/bash
export DEFAULT_TOMCAT_PROP_FILE="/var/lib/tomcat6/Enlighted/tomcat.properties"

{
logger -p 6 -t ems_memory_tracker "Checking out of memory condition for EM"
managerUser="admin"
managerPass="admin"
userpass=$managerUser":"$managerPass
if [ -f ${DEFAULT_TOMCAT_PROP_FILE} ]
then
    userpass=$(head -n 1 ${DEFAULT_TOMCAT_PROP_FILE})
fi

hasError=$(grep 'java.lang.OutOfMemoryError' /var/lib/tomcat6/logs/catalina.out)
if [ -n "$hasError" ]
then
    service tomcat6 stop
    sleep 30
    managerList=$(wget --no-check-certificate https://$userpass@localhost/manager/list -O - -q)
    while [ -n "$managerList" ]
    do
        sleep 30
        managerList=$(wget --no-check-certificate https://$userpass@localhost/manager/list -O - -q)
    done
    timeString=$(date +"%m-%d-%y-%T")
    mv /var/lib/tomcat6/logs/catalina.out /var/lib/tomcat6/logs/catalina.out_$timeString
    rm -rf /var/lib/tomcat6/work/Catalina/localhost/ems
    service tomcat6 start
    logger -p 6 -t ems_memory_tracker "Tomcat rebooted as out of memory detected"
fi
}
