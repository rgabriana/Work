#!/bin/bash
source /etc/environment

export EMS_MODE_FILE="$ENL_APP_HOME/Enlighted/emsmode"

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
    current_date=$(date +%s%N | cut -b1-13)
    echo $current_date
    last_communication_time=$(cat ${OPT_ENLIGHTED}/communicator/last_communication_time | head -n 1)
    timeout_last_communication=$(expr $last_communication_time + 1000 \* 60 \* 60 \* 2)
    echo $timeout_last_communication
    if [ ${current_date} -ge ${timeout_last_communication} ]
    then
        echo "communicator is not able to successfully communicate with cloud server for a while. killing the process and restarting again"
        ps -ef | grep em_cloud_communicator.jar | grep -v grep | head -n 1 | awk -v OFS=" " '$1=$1' | cut -d" " -f2 | xargs kill -9
        process=$(ps -ef | grep -E "em_cloud_communicator\.jar")
        if [[ ! $process =~ "em_cloud_communicator.jar" ]]
        then
            java -jar ${OPT_ENLIGHTED}/communicator/em_cloud_communicator.jar &
        fi
    else
        echo "communicator is running"          
    fi      
else
    echo "Starting communicator job"
    java -jar ${OPT_ENLIGHTED}/communicator/em_cloud_communicator.jar &
fi

}
