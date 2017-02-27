#!/bin/bash
# USB stick location
USB_BACKUP_DIR="/media/184F-549E"
# Fail over in case usb isn't connected location
FAIL_OVER_BACKUP_DIR="/opt/enLighted/DB/DBBK"
# Database details
PGHOST="localhost"
PGUSER="postgres"
PGDATABASE="ems"
today=`date '+%m'-'%d'-'%Y'`
yesterday=`date --date='1 day ago' '+%m'-'%d'-'%Y'`
if [ -d "$USB_BACKUP_DIR" ]; then
	/usr/bin/pg_dump -U $PGUSER $PGDATABASE | gzip > $USB_BACKUP_DIR/enLighted_$today.gz
	if [ $? == 0 ]; then
		rm -f $USB_BACKUP_DIR/enLighted_$yesterday.gz
	fi
else
	/usr/bin/pg_dump -U $PGUSER $PGDATABASE | gzip > $FAIL_OVER_BACKUP_DIR/enLighted_$today.gz
	if [ $? == 0 ]; then
		rm -f $FAIL_OVER_BACKUP_DIR/enLighted_$yesterday.gz
	fi
fi
