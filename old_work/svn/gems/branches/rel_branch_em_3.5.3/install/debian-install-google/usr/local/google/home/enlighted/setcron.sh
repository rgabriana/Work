#!/bin/bash
source /etc/environment
if [ ! -f /var/spool/cron/crontabs/root ]
then
    echo "#" > $ENLIGHTED_HOME/root
    crontab -u root $ENLIGHTED_HOME/root
    rm -f $ENLIGHTED_HOME/root
    echo "new cron created for root user"
fi

cronExists=$(grep 'ems_heartbeat_tracker\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "ems_heartbeat_tracker" ]]
then
    echo "EM Tracker Cron is already set"
    sudo sed -i '/ems_heartbeat_tracker/d' /var/spool/cron/crontabs/root
    echo "*/30 * * * * $ENLIGHTED_HOME/scripts/ems_heartbeat_tracker.sh" >> /var/spool/cron/crontabs/root
    echo "EM Tracker is set to run every 30 minutes"
else
    echo "*/30 * * * * $ENLIGHTED_HOME/scripts/ems_heartbeat_tracker.sh" >> /var/spool/cron/crontabs/root
    echo "EM Tracker is set to run every 30 minutes"
fi

cronExists=$(grep 'ems_memory_tracker\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "ems_memory_tracker" ]]
then
    echo "EM Memory Tracker Cron is already set"
else
    echo "*/15 * * * * $ENLIGHTED_HOME/scripts/ems_memory_tracker.sh" >> /var/spool/cron/crontabs/root
    echo "EM Memory Tracker is set to run every 15 minutes"
fi

cronExists=$(grep 'dailybackup\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "dailybackup" ]]
then
    echo "EM Database Daily Backup Cron is already set"
    sudo sed -i '/dailybackup/d' /var/spool/cron/crontabs/root
    echo "0 1,4 * * * $OPT_ENLIGHTED/DB/dailybackup.sh" >> /var/spool/cron/crontabs/root
    echo "EM Database Daily Backup Cron is set to attempt backup twice at 1AM and 4AM."
else
    echo "0 1,4 * * * $OPT_ENLIGHTED/DB/dailybackup.sh" >> /var/spool/cron/crontabs/root
    echo "EM Database Daily Backup Cron is set to attempt backup twice at 1AM and 4AM"
fi

cronExists=$(grep 'ems_custom_logrotate\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "ems_custom_logrotate" ]]
then
    echo "EM Tracker Cron is already set"
    sudo sed -i '/ems_custom_logrotate/d' /var/spool/cron/crontabs/root
    echo "*/15 * * * * $ENLIGHTED_HOME/scripts/ems_custom_logrotate.sh" >> /var/spool/cron/crontabs/root
    echo "EM log rotate check is set to run every 15 minutes"
else
    echo "*/15 * * * * $ENLIGHTED_HOME/scripts/ems_custom_logrotate.sh" >> /var/spool/cron/crontabs/root
    echo "EM log rotate check is set to run every 15 minutes"
fi

