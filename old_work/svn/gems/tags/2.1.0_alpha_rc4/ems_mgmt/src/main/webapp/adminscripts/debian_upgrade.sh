#!/bin/sh

LOG_DIR=/var/lib/tomcat6/Enlighted/adminlogs
{
	UPGRADE_COMPLETE=10
	DEBIAN_FILE_NAME=$1
	TOMCAT_PATH=$3
	workingDirectory=${TOMCAT_PATH}/Enlighted/UpgradeImages
	
	cd $workingDirectory
	#--
	echo "Upgrading software .."
	sudo dpkg -i --force-overwrite $DEBIAN_FILE_NAME

	if [ $? -eq 0 ]
	then
		echo "Software was upgraded succesfully .."
		echo "##$UPGRADE_COMPLETE"
		sleep 15
		exit 0
	else
		echo "Error upgrading software .."
		echo "-EXIT-"
		exit 1	
	fi
} > $LOG_DIR/upgradegems.log 2> $LOG_DIR/upgradegems_error.log
