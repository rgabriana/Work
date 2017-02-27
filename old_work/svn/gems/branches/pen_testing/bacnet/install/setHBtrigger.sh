#!/bin/bash
if [ $# -ne 1 ]; 
    then echo "Usage: setHBtrigger.sh <EM mac id>"
    exit 1
fi
psql -U postgres ems -c "select sensor_id from fixture where state='COMMISSIONED'" >sensors.txt
sed -f toxml.sed sensors.txt > sensors.xml
touch .done
while [ `wc -l .done | awk '{print \$1}'` -lt 1 ]; do
curl --user admin:admin  -k -X POST -H "Content-Type: application/xml" -d @sensors.xml https://$1/ems/services/uem/setPeriodicAndRealTimeHB/8/0/5
sleep 10
done
exit 0

