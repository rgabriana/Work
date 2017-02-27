#!/bin/bash

sudo /etc/init.d/dhcp3-server stop
dhcpStatus=`sudo /etc/init.d/dhcp3-server status | grep -i not`
if [ -n "$dhcpStatus" ];
then
	sudo ifdown eth1
	ethStatus=`ifconfig | grep -i eth1`
	if [ -n "$ethStatus" ];
	then
		exit 1
	else
		exit 0
	fi
else
	exit 1
fi

exit 0
