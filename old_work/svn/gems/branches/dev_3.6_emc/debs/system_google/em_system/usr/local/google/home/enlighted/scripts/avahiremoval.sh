#!/bin/bash
{
AVAHI_PKG_STATUS=$(dpkg -l avahi-autoipd | grep "avahi-autoipd")
if [[ "$AVAHI_PKG_STATUS" =~ "0.6.25" ]]
then
	logger -p 6 -t em_system "Removing avahi-autoipd package"
	sudo dpkg -r avahi-autoipd
	sudo dpkg --purge avahi-autoipd
else
	logger -p 6 -t em_system "avahi-autoipd package not present"
fi

AVAHI_PKG_STATUS=$(dpkg -l avahi-daemon | grep "avahi-daemon")
if [[ "$AVAHI_PKG_STATUS" =~ "0.6.25" ]]
then
	logger -p 6 -t em_system "Removing avahi-daemon package"
    sudo dpkg -r telepathy-salut
    sudo dpkg --purge telepathy-salut
    sudo dpkg -r libnss-mdns
    sudo dpkg --purge libnss-mdns
    sudo dpkg -r avahi-utils
    sudo dpkg --purge avahi-utils
	sudo dpkg -r avahi-daemon
	sudo dpkg --purge avahi-daemon
else
	logger -p 6 -t em_system "avahi-daemon package not present"
fi
}
