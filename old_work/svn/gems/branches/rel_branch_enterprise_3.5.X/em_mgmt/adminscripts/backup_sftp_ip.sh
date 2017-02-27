#!/bin/bash
{


	backupSftpIpSelected=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.ip'" | grep value | cut -d " " -f3)
	echo "$backupSftpIpSelected"
	
}