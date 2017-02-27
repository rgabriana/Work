#!/bin/bash
#################################################
#  name: replica_check.sh                       #
#  editor: Rolando R. Gabriana Jr.              #
#  contact: rolando.gabriana@enlightedinc.com   #
#  date edited: October 14, 2015                #
#################################################
#  purpose:                                     #
#  Script created to perform a check if the \   #
#  replica_daily_backups.sh script is already \ #
#  running.                                     #
#################################################
src_dir=/home/enlighted
backup=replica_daily_backups.sh

setup_logrotate() {
LOGROTATE_CONF_EXISTS=false;
if [ -f /etc/logrotate.d/replica_dbdbackup ]
then
	if [ -s /etc/logrotate.d/replica_dbdbackup ]
	then
		LOGROTATE_CONF_EXISTS=true;
	else
		LOGROTATE_CONF_EXISTS=false;
	fi
else
	LOGROTATE_CONF_EXISTS=false;
fi

if [ $LOGROTATE_CONF_EXISTS = false ]
then		
	echo "$src_dir/backup.log {
	    rotate 12
	    size 50M
		monthly
		missingok
		compress
		delaycompress
		notifempty
		copytruncate
	}" > /etc/logrotate.d/replica_dbdbackup
fi
}

# first perform log rotation
setup_logrotate

# Check process for other backups running
result=`ps -ef | grep -v grep | grep -c $backup`

echo "exit code: $result" >> $src_dir/backup.log

if [ $result -gt 0 ]; then
    echo "`date`: $backup is running, everything is fine" >> $src_dir/backup.log
        exit 0
else
    echo "`date`: $backup is not running" >> $src_dir/backup.log
fi



# now run the actual backup script
bash $src_dir/$backup >> $src_dir/backup.log 2>&1 
