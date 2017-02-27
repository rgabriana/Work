#!/bin/bash


source /etc/environment

cronfile="/var/spool/cron/crontabs/root"

if [ ! -f ${cronfile} ]
then
    echo "#" >  ${ENLIGHTED_HOME}/root
    crontab -u root  ${ENLIGHTED_HOME}/root
    rm -f  ${ENLIGHTED_HOME}/root
    echo "new cron created for root user"
fi

cronExists=$(grep -E "^.*\*\s*\/bin\/bash\s*.*\/scripts\/ems_heartbeat_tracker\.sh" ${cronfile})
if [[ "$cronExists" =~ "ems_heartbeat_tracker" ]]
then
    echo "ems heartbeat tracker cron is already set"
else
    cronExists=$(grep -E "^.*\*\s*.*\/scripts\/ems_heartbeat_tracker\.sh" ${cronfile})
    if [[ "$cronExists" =~ "ems_heartbeat_tracker" ]]
    then
        echo "update ems heartbeat tracker cron"
        sudo sed -i '/ems_heartbeat_tracker/d' ${cronfile}        
        echo "*/30 * * * * /bin/bash  ${ENLIGHTED_HOME}/scripts/ems_heartbeat_tracker.sh" >> ${cronfile}
    else
        echo "set new heartbeat tracker cron"
        echo "*/30 * * * * /bin/bash  ${ENLIGHTED_HOME}/scripts/ems_heartbeat_tracker.sh" >> ${cronfile}
    fi
fi


cronExists=$(grep -E "^.*\*\s*\/bin\/bash\s*.*\/scripts\/ems_memory_tracker\.sh" ${cronfile})
if [[ "$cronExists" =~ "ems_memory_tracker" ]]
then
    echo "ems memory tracker cron is already set"
else
    cronExists=$(grep -E "^.*\*\s*.*\/scripts\/ems_memory_tracker\.sh" ${cronfile})
    if [[ "$cronExists" =~ "ems_memory_tracker" ]]
    then
        echo "update ems memory tracker cron"
        sed -i 's/^\(.*\*\)\([[:space:]]*.*\/scripts\/ems_memory_tracker\.sh.*\)/\1\ \/bin\/bash\ \2/' ${cronfile}
    else
        echo "set new memory tracker cron"
        echo "*/15 * * * * /bin/bash  ${ENLIGHTED_HOME}/scripts/ems_memory_tracker.sh" >> ${cronfile}
    fi
fi

cronExists=$(grep -E "^.*\*\s*\/bin\/bash\s*.*\/DB\/dailybackup\.sh" ${cronfile})
if [[ "$cronExists" =~ "dailybackup" ]]
then
    echo "dailybackup cron is already set"
else
    cronExists=$(grep -E "^.*\*\s*.*\/DB\/dailybackup\.sh" ${cronfile})
    if [[ "$cronExists" =~ "dailybackup" ]]
    then
        echo "update dailybackup cron"
        sudo sed -i '/dailybackup/d' ${cronfile}
        echo "0 1,4 * * * /bin/bash ${OPT_ENLIGHTED}/DB/dailybackup.sh" >> ${cronfile}
    else
        echo "set dailybackup cron"
        echo "0 1,4 * * * /bin/bash ${OPT_ENLIGHTED}/DB/dailybackup.sh" >> ${cronfile}
    fi
fi


cronExists=$(grep -E "^.*\*\s*\/bin\/bash\s*.*\/scripts\/ems_custom_logrotate\.sh" ${cronfile})
if [[ "$cronExists" =~ "ems_custom_logrotate" ]]
then
    echo "custom logrotate cron is already set"
else
    cronExists=$(grep -E "^.*\*\s*.*\/scripts\/ems_custom_logrotate\.sh" ${cronfile})
    if [[ "$cronExists" =~ "ems_custom_logrotate" ]]
    then
        echo "update custom logrotate cron"
        sudo sed -i '/ems_custom_logrotate/d' ${cronfile}
        echo "*/15 * * * * /bin/bash  ${ENLIGHTED_HOME}/scripts/ems_custom_logrotate.sh" >> ${cronfile}
    else
        echo "set custom logrotate cron"
        echo "*/15 * * * * /bin/bash  ${ENLIGHTED_HOME}/scripts/ems_custom_logrotate.sh" >> ${cronfile}
    fi
fi

cronExists=$(grep -E "^.*\*\s*\/bin\/bash\s*.*\/scripts\/ems_ntp\.sh" ${cronfile})
if [[ "$cronExists" =~ "ems_ntp" ]]
then
    echo "ntp cron is already set"
else
    cronExists=$(grep -E "^.*\*\s*.*\/scripts\/ems_ntp\.sh" ${cronfile})
    if [[ "$cronExists" =~ "ems_ntp" ]]
    then
        echo "update ntp cron"
        sed -i 's/^\(.*\*\)\([[:space:]]*.*\/scripts\/ems_ntp\.sh.*\)/\1\ \/bin\/bash\ \2/' ${cronfile}
    else
        echo "set ntp cron"
        echo "50 0,3 * * * /bin/bash  ${ENLIGHTED_HOME}/scripts/ems_ntp.sh" >> ${cronfile}
    fi
fi

cronExists=$(grep -E "^.*\*\s*\/bin\/bash\s*.*\/scripts\/dailyconfigbackup\.sh" ${cronfile})
if [[ "$cronExists" =~ "dailyconfigbackup" ]]
then
    echo "dailyconfigbackup cron is already set"
else
    cronExists=$(grep -E "^.*\*\s*sh\s*.*\/scripts\/dailyconfigbackup\.sh" ${cronfile})
    if [[ "$cronExists" =~ "dailyconfigbackup" ]]
    then
        echo "update dailyconfigbackup cron"
        sed -i 's/^\(.*\*[[:space:]]*\)sh\([[:space:]]*.*\/scripts\/dailyconfigbackup\.sh.*\)/\1\ \/bin\/bash\ \2/' ${cronfile}
    else
        echo "set dailyconfigbackup cron"
        echo "30 0,3 * * * /bin/bash  ${ENLIGHTED_HOME}/scripts/dailyconfigbackup.sh" >> ${cronfile}
    fi
fi

cronExists=$(grep -E "^@reboot\s*sleep.*;\s*\/bin\/bash\s*.*\/scripts\/ems_ntp\.sh" ${cronfile})
if [[ "$cronExists" =~ "ems_ntp" ]]
then
    echo "startup ntp cron is already set"
else
    cronExists=$(grep -E "@reboot\s*sleep.*;\s*sh\s*.*\/scripts\/ems_ntp\.sh" ${cronfile})
    if [[ "$cronExists" =~ "ems_ntp" ]]
    then
        echo "update startup ntp cron"
        sed -i 's/^\(@reboot[[:space:]]*sleep.*;[[:space:]]*\)sh\([[:space:]]*.*\/scripts\/ems_ntp\.sh.*\)/\1\ \/bin\/bash\ \2/' ${cronfile}
    else
        echo "set startup ntp cron"
        echo "@reboot sleep 60; /bin/bash ${ENLIGHTED_HOME}/scripts/ems_ntp.sh" >> ${cronfile}
    fi
fi

