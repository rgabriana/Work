#!/bin/bash
stat=3
if [ -f "/etc/init.d/dhcp3-server" ] 
then
	##1004
	sudo /etc/init.d/dhcp3-server status 2>&1>/dev/null
	stat=`echo $?`
else
	##1404
	log_status=`sudo service isc-dhcp-server status`
	stat=`echo $?`
	if [ "$stat" -ne "0" ]
	then
		echo "Problem in retrieving status of isc-dhcp-server $log_status"
	fi
	if [[ "$log_status" =~ "start" ]]
	then
		stat=0
	else
		stat=1
fi

if [ "$stat" -eq 0 ]
then
	echo "is running"
else
	echo "is not running"
fi
fi