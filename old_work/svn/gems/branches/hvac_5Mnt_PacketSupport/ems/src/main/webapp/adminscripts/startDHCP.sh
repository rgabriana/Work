#!/bin/bash
sudo ifup eth1
ethStatus=`ifconfig | grep -i eth1` 
if [ -n "$ethStatus" ];
then
	sudo /etc/init.d/dhcp3-server start
	dhcpStatus=`sudo /etc/init.d/dhcp3-server status | grep -i not`
	if [ -n "$dhcpStatus" ];
	then
		exit 1
	else
		exit 0
	fi
else
	exit 1
fi

exit 0
