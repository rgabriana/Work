#!/bin/bash

cronExists=$(grep 'ems_communicator\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "ems_communicator" ]]
then
    echo "EM Communicator Cron is already set"
else
    echo "30 * * * * /opt/enLighted/communicator/scripts/ems_communicator.sh" >> /var/spool/cron/crontabs/root
    echo "EM Communicator is set to run every  1 hour"
	service cron restart
fi