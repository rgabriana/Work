#!/bin/bash
{


	backupSftpUsername=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.username'" | grep value | cut -d " " -f3)
	echo "$backupSftpUsername"
	
}