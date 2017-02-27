#!/bin/sh
echo "started"
while [ true ]
do
java -jar ems_communicator.jar /home/enlighted/Desktop/urls.properties SendDashBoardDetailsHourlyData
sleep 60
done
