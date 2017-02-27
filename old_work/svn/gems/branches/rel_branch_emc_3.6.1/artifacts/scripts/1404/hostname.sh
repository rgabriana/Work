#!/bin/bash
interface=`ls /sys/class/net | head -1`
MAC=$(ip link show $interface | awk '/ether/ {print $2}')
if [ -z "$MAC"  ]
then
	uuid=`uuid | cut -d '-' -f5`
	echo enlightedinc-$uuid | sed -e 's/://g' > /etc/hostname
	echo 127.0.0.1 enlightedinc-$uuid localhost enlighted | sed -e 's/://g' > /etc/hosts
else
	echo enlightedinc-$MAC | sed -e 's/://g' > /etc/hostname
	echo 127.0.0.1 enlightedinc-$MAC localhost enlighted | sed -e 's/://g' > /etc/hosts
fi


# EndOfFile