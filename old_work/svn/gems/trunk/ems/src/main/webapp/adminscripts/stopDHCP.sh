#!/bin/bash

if [ -f "/etc/init.d/dhcp3-server" ] 
then
	. /etc/default/dhcp3-server
	sudo /etc/init.d/dhcp3-server stop
	dhcpStatus=`sudo /etc/init.d/dhcp3-server status | grep -i not`
	if [ -n "$dhcpStatus" ];
	then
		 network="$INTERFACES"
		if [ -z $network  ]
		then
			sudo ifdown $network
			ethStatus=`echo $?`
			exit $ethStatus
		else
			exit 0
		fi
	else
		exit 1
	fi
	
	exit 0
else
	. /etc/default/isc-dhcp-server
	sudo service isc-dhcp-server stop
	dhcpStatus=`sudo service isc-dhcp-server status | grep -i stop`
	if [ -n "$dhcpStatus" ];
	then
		 network="$INTERFACES"
		if [ -z $network  ]
		then
			sudo ifdown $network
			ethStatus=`echo $?`
			exit $ethStatus
		else
			exit 0
		fi
	else
		exit 1
	fi
	
	exit 0
fi
