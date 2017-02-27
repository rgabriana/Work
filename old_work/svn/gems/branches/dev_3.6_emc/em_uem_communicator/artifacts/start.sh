#!/bin/sh
source /etc/environment
uemEnable=$(psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='uem.enable'" | grep value | cut -d " " -f3)
uemPacketForwarding=$(psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='uem.pkt.forwarding.enable'" | grep value | cut -d " " -f3)

if [ "${uemEnable}" == "1" -a "${uemPacketForwarding}" == "1" ]
then


    cd $OPT_ENLIGHTED/uem/communicator/

    logger -p 6 -t em_uem_communicator_tracker "Checking out memory condition for EM_UEM_COMMUNICATOR."

    memTrackerRunning=$(ps aux | grep '[g]rep java.lang.OutOfMemoryError em_uem_communicator.log')
    hasError=$(grep 'java.lang.OutOfMemoryError' em_uem_communicator.log)

    if [ -n "$memTrackerRunning" ]
    then
       logger -p 6 -t em_uem_communicator_tracker  "em uem communicator memory tracker running."
    else
       logger -p 6 -t em_uem_communicator_tracker  "em uem communicator memory tracker not running."	
       if [ -n "$hasError" ]
       then
        logger -p 6 -t em_uem_communicator_tracker "Found OutOfMemoryError. Killing em uem communicator."
        EMUEMCOMPID=`ps auxwww | grep -i EMUEMCOM | grep -v grep | awk '{print $2}'`
        kill -9 $EMUEMCOMPID    
       fi
    fi


    EMUEMCOMPID=`ps auxwww | grep -i EMUEMCOM | grep -v grep | awk '{print $2}'`

    if [ -z "$EMUEMCOMPID" ]; then
            logger -p 6 -t em_uem_communicator_tracker "em uem communicator is down. Restarting it."
            echo "Starting em uem communicator."
            java -Dlog4j.configuration=file:log4j.properties -DNAME=EMUEMCOM -jar em_uem_communicator.jar  > em_uem_communicator.log 2>&1 &
            sleep 3
    else
            logger -p 6 -t em_uem_communicator_tracker "em uem communicator is running."
    fi

fi



