#!/bin/bash
{


	backupUsbPathSelected=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.usb.path'" | grep value | cut -d " " -f3)
	echo "$backupUsbPathSelected"
	
}