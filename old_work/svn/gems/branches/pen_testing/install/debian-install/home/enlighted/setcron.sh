#!/bin/bash

if [ ! -f /var/spool/cron/crontabs/root ]
then
    echo "#" > /home/enlighted/Desktop/root
    crontab -u root /home/enlighted/Desktop/root
    rm -f /home/enlighted/Desktop/root
    echo "new cron created for root user"
fi

cronExists=$(grep 'ems_heartbeat_tracker\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "ems_heartbeat_tracker" ]]
then
    echo "EM Tracker Cron is already set"
    sudo sed -i '/ems_heartbeat_tracker/d' /var/spool/cron/crontabs/root
    echo "*/30 * * * * /home/enlighted/scripts/ems_heartbeat_tracker.sh" >> /var/spool/cron/crontabs/root
    echo "EM Tracker is set to run every 30 minutes"
else
    echo "*/30 * * * * /home/enlighted/scripts/ems_heartbeat_tracker.sh" >> /var/spool/cron/crontabs/root
    echo "EM Tracker is set to run every 30 minutes"
fi

cronExists=$(grep 'ems_memory_tracker\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "ems_memory_tracker" ]]
then
    echo "EM Memory Tracker Cron is already set"
else
    echo "*/15 * * * * /home/enlighted/scripts/ems_memory_tracker.sh" >> /var/spool/cron/crontabs/root
    echo "EM Memory Tracker is set to run every 15 minutes"
fi

cronExists=$(grep 'dailybackup\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "dailybackup" ]]
then
    echo "EM Database Daily Backup Cron is already set"
    sudo sed -i '/dailybackup/d' /var/spool/cron/crontabs/root
    echo "0 1,4 * * * /opt/enLighted/DB/dailybackup.sh" >> /var/spool/cron/crontabs/root
    echo "EM Database Daily Backup Cron is set to attempt backup twice at 1AM and 4AM."
else
    echo "0 1,4 * * * /opt/enLighted/DB/dailybackup.sh" >> /var/spool/cron/crontabs/root
    echo "EM Database Daily Backup Cron is set to attempt backup twice at 1AM and 4AM"
fi

cronExists=$(grep 'ems_custom_logrotate\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "ems_custom_logrotate" ]]
then
    echo "EM Tracker Cron is already set"
    sudo sed -i '/ems_custom_logrotate/d' /var/spool/cron/crontabs/root
    echo "*/15 * * * * /home/enlighted/scripts/ems_custom_logrotate.sh" >> /var/spool/cron/crontabs/root
    echo "EM log rotate check is set to run every 15 minutes"
else
    echo "*/15 * * * * /home/enlighted/scripts/ems_custom_logrotate.sh" >> /var/spool/cron/crontabs/root
    echo "EM log rotate check is set to run every 15 minutes"
fi

cronExists=$(grep 'ems_ntp\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "ems_ntp" ]]
then
     echo "EM NTP Cron is already set."
else
	echo "50 0,3 * * * /home/enlighted/scripts/ems_ntp.sh" >> /var/spool/cron/crontabs/root
    echo "EM NTP Time sync is set to run every day at 00:50 a.m. and 03:50 a.m."
fi

cronExists=$(grep 'dailyconfigbackup\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "dailyconfigbackup" ]]
then
     echo "dailyconfigbackup Cron is already set."
else
	echo "30 0,3 * * * sh /home/enlighted/scripts/dailyconfigbackup.sh " >> /var/spool/cron/crontabs/root
    echo "dailyconfigbackup is set to run every day at 00:30 a.m. and 03:30 a.m."
fi
