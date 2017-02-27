#!/bin/bash
# This command will take the following parameter
# $1 = Is NTP Enabled
# $2 = List of NTP Servers
# #3 = New Server Time
# If $1 is Y then the ntp is setup else $3 is used to set the system current time.
sudo rm -rf  /tmp/ntpdefault
if [ $1 = "Y" ]; then
        echo "NTPDATE_USE_NTP_CONF=no" > /tmp/ntpdefault
        echo "NTPSERVERS=$2" >> /tmp/ntpdefault
        echo "NTPOPTIONS=\"\"" >> /tmp/ntpdefault
        sudo cp /tmp/ntpdefault /etc/default/ntpdate
        
        sudo cp /etc/ntp.conf /tmp/ntp.conf
		sudo sed  -i '/^server/d' /etc/ntp.conf
		#sudo sed -i '/ntp server as a fallback/i dhanesh-dhanesh' /etc/ntp.conf
		#stringarray=($2)
		stringarray1=$(echo $2 | tr "\"" "'")
        stringarray1=$(echo $stringarray1 | tr "'" " ")
        stringarray=($stringarray1)
		for i in "${stringarray[@]}"
		    do
		      :
		      # do whatever on $i
		       sudo sed -i '/ntp server as a fallback/i server '$i /etc/ntp.conf
		    done
else
        echo "NTPDATE_USE_NTP_CONF=yes" > /tmp/ntpdefault
        echo "NTPSERVERS=\"\"" >> /tmp/ntpdefault
        echo "NTPOPTIONS=\"\"" >> /tmp/ntpdefault
        sudo cp /tmp/ntpdefault /etc/default/ntpdate
        #sudo rm /etc/ntp.conf
        
        sudo cp /etc/ntp.conf /tmp/ntp.conf
		sudo sed  -i '/^server/d' /etc/ntp.conf
				    

        sudo date $3
        sudo hwclock --systohc --utc
fi
