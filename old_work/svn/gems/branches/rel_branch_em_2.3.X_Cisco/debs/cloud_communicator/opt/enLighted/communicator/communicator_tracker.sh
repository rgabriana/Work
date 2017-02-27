#!/bin/bash

export EMS_MODE_FILE="/var/lib/tomcat6/Enlighted/emsmode"

{
if [ -f ${EMS_MODE_FILE} ]
then
    emsmode=$(head -n 1 ${EMS_MODE_FILE})
    if [[ $emsmode =~ "UPGRADE_RESTORE" ]]
    then
        exit 0
    fi
fi

process=$(ps -ef | grep -E "em_cloud_communicator\.jar")
if [[ $process =~ "em_cloud_communicator.jar" ]]
then
    echo "communicator is running"      
else
    echo "Starting communicator job"
    java -jar /opt/enLighted/communicator/em_cloud_communicator.jar &
fi

}
