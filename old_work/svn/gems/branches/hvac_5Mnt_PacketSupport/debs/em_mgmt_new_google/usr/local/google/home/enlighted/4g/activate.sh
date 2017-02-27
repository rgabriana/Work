#!/bin/bash

set -e

cwd=`pwd`
instdir=/usr/local/google/home/enlighted/4g

if [ -z "$1" ];then
    echo "You need to provide a trigger argument:"
    echo "$0 [schedule | health]"
    exit 1
fi

trigger="$1"
if [ "$trigger" != "schedule" -a "$trigger" != "health" ];then
    echo "Trigger argument not valid:"
    echo "$0 [schedule | health]"
    exit 1
fi

create_crontab_entry() {
    echo "Creating cron job..."
    local schedule
    local command
    if [ "$trigger" == "schedule" ];then
        schedule="3 0 * * *"
        command="$instdir/reboot_4g.py"
    elif [ "$trigger" == "health" ];then
        schedule="3,33 * * * *"
        command="$instdir/check_4g_health.py"
    fi

    echo "    Creating entry $schedule $command"
    cat <(fgrep -v $command <(sudo crontab -l)) <(echo "$schedule $command") | sudo crontab -
    echo "Done"
}

set_up_log_rotation() {
    echo "Configuring log rotation..."
    cd $instdir
    cp 4g_health_logrotate /etc/customLogrotate.d/4g_health
    chown root:root /etc/customLogrotate.d/4g_health
    chmod 644 /etc/customLogrotate.d/4g_health
    cd $cwd
    echo "Done"
}

create_crontab_entry
set_up_log_rotation
