#!/bin/bash

. /etc/lsb-release
current_os=$DISTRIB_RELEASE
ARCH=`uname -m`
ARCH_BIT="32"
if [[ "$ARCH" =~ "_64" ]]
then
        ARCH_BIT="64"
fi

#echo "{\"os\":{\"arch\":\"$ARCH_BIT\",\"version\":\"$current_os\"}}"
echo "{\"arch\":\"$ARCH_BIT\",\"version\":\"$current_os\"}"