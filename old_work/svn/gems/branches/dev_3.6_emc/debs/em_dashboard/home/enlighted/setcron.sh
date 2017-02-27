#!/bin/bash

if [ ! -f /var/spool/cron/crontabs/root ]
then
    echo "#" > /home/enlighted/Desktop/root
    crontab -u root /home/enlighted/Desktop/root
    rm -f /home/enlighted/Desktop/root
    echo "new cron created for root user"
fi

cronExists=$(grep 'emsdashboard_heartbeat_tracker\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "emsdashboard_heartbeat_tracker" ]]
then
    echo "EMS Dashboard Tracker Cron is already set"
else
    echo "*/15 * * * * /home/enlighted/scripts/emsdashboard_heartbeat_tracker.sh" >> /var/spool/cron/crontabs/root
    echo "EMS Dashboard Tracker is set to run every 15 minutes"
fi

cronExists=$(grep 'emsdashboard_memory_tracker\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "emsdashboard_memory_tracker" ]]
then
    echo "EMS Dashboard Memory Tracker Cron is already set"
else
    echo "*/15 * * * * /home/enlighted/scripts/emsdashboard_memory_tracker.sh" >> /var/spool/cron/crontabs/root
    echo "EMS Dashboard Memory Tracker is set to run every 15 minutes"
fi
