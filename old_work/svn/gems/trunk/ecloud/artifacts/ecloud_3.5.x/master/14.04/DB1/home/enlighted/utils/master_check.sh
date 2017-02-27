#!/bin/bash
###################################################
#  name:	master_check.sh			  #
#  editor:	Rolando R. Gabriana Jr.           #
#  contact: 	rolando.gabriana@enlightedinc.com #
#  date edited: October 14, 2015                  #
###################################################
#  purpose:                                       #
#  Script created to perform a check if the \     #
#  master_dail_backup.sh script is already \      #
#  running.                                       #
###################################################

# Check process for other backups running

src_dir=/home/enlighted
backup=master_daily_backup.sh
result=`ps -ef | grep -v grep | grep -c $backup`

echo "exit code: $result"

if [ $result -gt 0 ]; then
    echo "`date`: $backup is running, everything is fine"
        exit 0
else
    echo "`date`: $backup is not running"
fi

bash $src_dir/$backup >> $src_dir/backup.log 2>&1  

