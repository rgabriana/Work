#!/bin/bash
{
AVAHI_PKG_STATUS=`dpkg -s avahi-autoipd | grep Status`
if [ "$AVAHI_PKG_STATUS" == "Status: install ok installed" ]; then
	logger -p 6 -t em_system "Removing avahi package"
	dpkg -r avahi-autoipd
	dpkg --purge avahi-autoipd
	AVAHI_PKG_STATUS=`dpkg -s avahi-autoipd | grep Status`
	if [ "$AVAHI_PKG_STATUS" == "Status: unknown ok not-installed" ]; then
		logger -p 6 -t em_system "avahi package removed"
	fi
else
	logger -p 6 -t em_system "avahi package not present"
fi
}
