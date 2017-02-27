#!/bin/bash

sudo service ntp stop

. /etc/default/ntpdate

/usr/sbin/ntpdate $NTPSERVERS