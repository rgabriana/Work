#!/bin/bash
# This command will take the following parameter
# $1 = Is NTP Enabled
# $2 = List of NTP Servers
# #3 = New Server Time
# If $1 is Y then the ntp is setup else $3 is used to set the system current time.
if [ $1 = "Y" ]; then
        echo "NTPDATE_USE_NTP_CONF=no" > /tmp/ntpdefault
        echo "NTPSERVERS=\"$2\"" >> /tmp/ntpdefault
        echo "NTPOPTIONS=\"\"" >> /tmp/ntpdefault
        sudo cp /tmp/ntpdefault /etc/default/ntpdate
else
        echo "NTPDATE_USE_NTP_CONF=yes" > /tmp/ntpdefault
        echo "NTPSERVERS=\"\"" >> /tmp/ntpdefault
        echo "NTPOPTIONS=\"\"" >> /tmp/ntpdefault
        sudo cp /tmp/ntpdefault /etc/default/ntpdate
        sudo rm /etc/ntp.conf

        sudo date $3
        sudo hwclock -w
fi
