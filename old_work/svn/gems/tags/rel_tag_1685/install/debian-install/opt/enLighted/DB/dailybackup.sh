#!/bin/bash
# USB stick location
USBMEDIA=$(df -k| grep media|tr -s ' ' ' '|cut -f6 -d' '|head -1)
USB_BACKUP_DIR="${USBMEDIA}/dbbackup"
# Fail over in case usb isn't connected location
FAIL_OVER_BACKUP_DIR="/opt/enLighted/DB/DBBK"
# Database details
PGHOST="localhost"
PGUSER="postgres"
PGDATABASE="ems"
today=`date '+%m'-'%d'-'%Y'`

#TOMCAT PATH (subject to change)
TOMCAT_PATH=/var/lib/tomcat6
yesterday=`date --date='1 day ago' '+%m'-'%d'-'%Y'`

#-- Archive name
TAR_ARCHIVE=enLighted_$today.tar.gz

#-- Bacnet database file (this is not postgres db but a simple text file)
BACNET_DB_FILE=bacnet.db

# -- Clean up Method
cleanUp() {
	
	rm -f EMSMANIFEST.MF
	rm -f enLighted_$today.sql
	rm -f ${BACNET_DB_FILE}
	rm -f $TAR_ARCHIVE
}

archiveBackup() {
	
	folder=$1
	
	cp -f $TOMCAT_PATH/webapps/ems/META-INF/EMSMANIFEST.MF .
	cp -f $TOMCAT_PATH/Enlighted/${BACNET_DB_FILE} .
    # Tar all files in this directory.
	tar -zcf $TAR_ARCHIVE *
	cp -f $TAR_ARCHIVE $folder
}

if [ -d "$USBMEDIA" ]; then
	if [ ! -d "$USB_BACKUP_DIR" ]; then
		mkdir "$USB_BACKUP_DIR"
	fi
else
	echo "usb not present"
fi

if [ -d "$USB_BACKUP_DIR" ]; then
	
	#-- Include the manifest file also for version checking.
			
	/usr/bin/pg_dump -i -U $PGUSER -h $PGHOST -b -f "enLighted_$today.sql" $PGDATABASE
	#/usr/bin/pg_dump -U $PGUSER $PGDATABASE | gzip > $USB_BACKUP_DIR/enLighted_$today.gz
	if [ $? == 0 ]; then
		rm -f $USB_BACKUP_DIR/enLighted_$yesterday.tar.gz
		archiveBackup $USB_BACKUP_DIR
		cleanUp
	fi
else
	/usr/bin/pg_dump -i -U $PGUSER -h $PGHOST -b -f "enLighted_$today.sql" $PGDATABASE
	#/usr/bin/pg_dump -U $PGUSER $PGDATABASE | gzip > $FAIL_OVER_BACKUP_DIR/enLighted_$today.gz
	if [ $? == 0 ]; then
		rm -f $FAIL_OVER_BACKUP_DIR/enLighted_$yesterday.tar.gz
		archiveBackup $FAIL_OVER_BACKUP_DIR
		cleanUp
	fi
fi
