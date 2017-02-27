#!/bin/bash
source /etc/environment

cronfile="/var/spool/cron/crontabs/root"

if [ ! -f ${cronfile} ]
then
    echo "#" > ${ENLIGHTED_HOME}/Desktop/root
    crontab -u root ${ENLIGHTED_HOME}/Desktop/root
    rm -f ${ENLIGHTED_HOME}/Desktop/root
    echo "new cron created for root user"
fi

sed -i 's/30.*ems_communicator\.sh//' ${cronfile}

cronExists=$(grep -E "^@reboot\s*\/bin\/bash\s*java\s*-jar\s*.*\/communicator\/em_cloud_communicator\.jar" ${cronfile})
if [[ "$cronExists" =~ "em_cloud_communicator" ]]
then
    echo "startup cloud communicator cron is already set"
else
    cronExists=$(grep -E "@reboot\s*java\s*-jar\s*.*\/communicator\/em_cloud_communicator\.jar" ${cronfile})
    if [[ "$cronExists" =~ "em_cloud_communicator" ]]
    then
        echo "update startup cloud communicator cron"
        sed -i 's/^\(@reboot[[:space:]]*\)\(java[[:space:]]*-jar[[:space:]]*.*\/communicator\/em_cloud_communicator\.jar.*\)/\1\ \/bin\/bash\ \2/' ${cronfile}
    else
        echo "set startup cloud communicator cron"
        echo "@reboot /bin/bash java -jar ${OPT_ENLIGHTED}/communicator/em_cloud_communicator.jar" >> ${cronfile}
    fi
fi


cronExists=$(grep -E "^.*\*\s*\/bin\/bash\s*.*\/communicator\/communicator_tracker\.sh" ${cronfile})
if [[ "$cronExists" =~ "communicator_tracker" ]]
then
    echo "communicator tracker cron is already set"
else
    cronExists=$(grep -E "^.*\*\s*.*\/communicator\/communicator_tracker\.sh" ${cronfile})
    if [[ "$cronExists" =~ "communicator_tracker" ]]
    then
        echo "update communicator tracker cron"
        sed -i 's/^\(.*\*\)\([[:space:]]*.*\/communicator\/communicator_tracker\.sh.*\)/\1\ \/bin\/bash\ \2/' ${cronfile}
    else
        echo "set communicator tracker cron"
        echo "*/5 * * * * /bin/bash ${OPT_ENLIGHTED}/communicator/communicator_tracker.sh" >> ${cronfile}
    fi
fi

cronExists=$(grep -E "^.*\*\s*\/bin\/bash\s*.*\/communicator\/ssh_tunnel_tracker\.sh" ${cronfile})
if [[ "$cronExists" =~ "ssh_tunnel_tracker" ]]
then
    echo "ssh tunnel tracker cron is already set"
else
    cronExists=$(grep -E "^.*\*\s*.*\/communicator\/ssh_tunnel_tracker\.sh" ${cronfile})
    if [[ "$cronExists" =~ "ssh_tunnel_tracker" ]]
    then
        echo "update ssh tunnel tracker cron"
        sudo sed -i '/ssh_tunnel_tracker/d' ${cronfile}
        echo "*/1 * * * * /bin/bash ${OPT_ENLIGHTED}/communicator/ssh_tunnel_tracker.sh" >> ${cronfile}
    else
        echo "set ssh tunnel tracker cron"
        echo "*/1 * * * * /bin/bash ${OPT_ENLIGHTED}/communicator/ssh_tunnel_tracker.sh" >> ${cronfile}
    fi
fi

