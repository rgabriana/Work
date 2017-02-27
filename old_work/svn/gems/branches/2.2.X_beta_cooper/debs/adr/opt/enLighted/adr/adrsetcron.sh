#!/bin/bash

if [ ! -f /var/spool/cron/crontabs/root ]
then
    echo "#" > /home/enlighted/Desktop/root
    crontab -u root /home/enlighted/Desktop/root
    rm -f /home/enlighted/Desktop/root
    echo "new cron created for root user"
fi

cronExists=$(grep -E "@reboot[\ ]+java[\ ]+-Djava\.util\.logging\.config\.file=\/opt\/enLighted\/adr\/logging\.properties[\ ]+-jar[\ ]+\/opt\/enLighted\/adr\/adr\.jar" /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "reboot" ]]
then
    echo "ADR Startup Cron is already set"
else
    cronExists=$(grep -E "@reboot[\ ]+java[\ ]+-jar[\ ]+\/opt\/enLighted\/adr\/adr\.jar" /var/spool/cron/crontabs/root)
    if [[ "$cronExists" =~ "reboot" ]]
    then
        sed -i 's/\(@reboot[\ ]\{1,\}java[\ ]\{1,\}\)\(-jar[\ ]\{1,\}\/opt\/enLighted\/adr\/adr\.jar\)/\1-Djava.util.logging.config.file=\/opt\/enLighted\/adr\/logging.properties\ \2/' /var/spool/cron/crontabs/root
    else
        echo "@reboot java -Djava.util.logging.config.file=/opt/enLighted/adr/logging.properties -jar /opt/enLighted/adr/adr.jar" >> /var/spool/cron/crontabs/root
    fi
    echo "ADR Startup Cron is set"
fi

cronExists=$(grep -E "adr_tracker\.sh" /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "adr_tracker" ]]
then
    echo "ADR Monitoring Cron is already set"
else
    echo "*/15 * * * * /opt/enLighted/adr/adr_tracker.sh" >> /var/spool/cron/crontabs/root
    echo "ADR Monitoring Cron is set to run every 15 minutes"
fi
