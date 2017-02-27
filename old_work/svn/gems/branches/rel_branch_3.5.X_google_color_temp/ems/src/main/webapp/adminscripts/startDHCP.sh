#!/bin/bash

. /etc/default/dhcp3-server
network="$INTERFACES"
if [ ! -z "$network" ]
then
	sudo ifup eth1
	ethStatus=`echo $?` 
	exit $ethStatus
fi
exit 0
