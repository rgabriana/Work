#!/bin/bash
source /etc/environment

cronfile="/var/spool/cron/crontabs/root"

if [ ! -f ${cronfile} ]
then
    echo "#" > $ENLIGHTED_HOME/Desktop/root
    crontab -u root $ENLIGHTED_HOME/Desktop/root
    rm -f $ENLIGHTED_HOME/Desktop/root
    echo "new cron created for root user"
fi

cronExists=$(grep -E "^@reboot\s*\/bin\/bash\s*.*\/adr\/adr_tracker\.sh" ${cronfile})
if [[ "$cronExists" =~ "adr_tracker" ]]
then
    echo "startup adr tracker cron is already set"
else
    cronExists=$(grep -E "@reboot\s*.*\/adr\/adr_tracker\.sh" ${cronfile})
    if [[ "$cronExists" =~ "adr_tracker" ]]
    then
        echo "update startup adr tracker cron"
        sed -i 's/^\(@reboot\)\([[:space:]]*.*\/adr\/adr_tracker\.sh.*\)/\1\ \/bin\/bash\ \2/' ${cronfile}
    else
        echo "set startup adr tracker cron"
        echo "@reboot /bin/bash ${OPT_ENLIGHTED}/adr/adr_tracker.sh" >> ${cronfile}
    fi
fi

cronExists=$(grep -E "^.*\*\s*\/bin\/bash\s*.*\/adr\/adr_tracker\.sh" ${cronfile})
if [[ "$cronExists" =~ "adr_tracker" ]]
then
    echo "adr tracker cron is already set"
else
    cronExists=$(grep -E "^.*\*\s*.*\/adr\/adr_tracker\.sh" ${cronfile})
    if [[ "$cronExists" =~ "adr_tracker" ]]
    then
        echo "update adr tracker cron"
        sed -i 's/^\(.*\*\)\([[:space:]]*.*\/adr\/adr_tracker\.sh.*\)/\1\ \/bin\/bash\ \2/' ${cronfile}
    else
        echo "set adr tracker cron"
        echo "*/15 * * * * /bin/bash  ${OPT_ENLIGHTED}/adr/adr_tracker.sh" >> ${cronfile}
    fi
fi

