#!/bin/bash

if [ -f "/etc/init.d/dhcp3-server" ]
then
	. /etc/default/dhcp3-server
else
	. /etc/default/isc-dhcp-server
fi
network="$INTERFACES"
if [ ! -z "$network" ]
then
	sudo ifup eth1
	ethStatus=`echo $?` 
	exit $ethStatus
fi
exit 0
