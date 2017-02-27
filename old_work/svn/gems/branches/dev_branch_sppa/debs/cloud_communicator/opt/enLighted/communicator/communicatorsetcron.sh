#!/bin/bash

if [ ! -f /var/spool/cron/crontabs/root ]
then
    echo "#" > /home/enlighted/Desktop/root
    crontab -u root /home/enlighted/Desktop/root
    rm -f /home/enlighted/Desktop/root
    echo "new cron created for root user"
fi

sed -i 's/30.*ems_communicator\.sh//' /var/spool/cron/crontabs/root

cronExists=$(grep -E "em_cloud_communicator\.jar" /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "em_cloud_communicator" ]]
then
    echo "Communicator Startup Cron is already set"
else
    echo "@reboot java -Djava.util.logging.config.file=/opt/enLighted/communicator/logging.properties -jar /opt/enLighted/communicator/em_cloud_communicator.jar" >> /var/spool/cron/crontabs/root
fi

cronExists=$(grep -E "communicator_tracker\.sh" /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "communicator_tracker" ]]
then
    echo "Communicator Monitoring Cron is already set"
else
    echo "*/15 * * * * /opt/enLighted/communicator/communicator_tracker.sh" >> /var/spool/cron/crontabs/root
    echo "Communicator Monitoring Cron is set to run every 15 minutes"
fi
