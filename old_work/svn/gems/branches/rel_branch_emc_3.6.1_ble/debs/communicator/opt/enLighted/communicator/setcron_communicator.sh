#!/bin/bash
source /etc/environment
cronExists=$(grep 'ems_communicator\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "ems_communicator" ]]
then
    echo "EM Communicator Cron is already set"
else
    echo "30 * * * * $OPT_ENLIGHTED/communicator/scripts/ems_communicator.sh" >> /var/spool/cron/crontabs/root
    echo "EM Communicator is set to run every  1 hour"
	service cron restart
fi
