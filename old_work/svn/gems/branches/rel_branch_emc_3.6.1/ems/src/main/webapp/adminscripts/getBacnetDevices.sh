#!/bin/bash
source /etc/environment

BACNET_IP_PORT=47809
export BACNET_IP_PORT

BACNET_BBMD_PORT=47808
export BACNET_BBMD_PORT

BACNET_BBMD_ADDRESS=localhost
export BACNET_BBMD_ADDRESS

devices=`/bin/bash -c $ENL_TOMCAT_HOME/Enlighted/bacnet/tools/bacwi | grep -E "^[[:space:]]*[0-9]+[[:space:]]+[a-zA-Z0-9:]+[[:space:]]+$1[[:space:]]+" | sed 's/^[[:space:]]*\([0-9]*\).*/\1/'  | grep -E "^[0-9]+$" | sort -n`
echo $devices
