#!/bin/bash
{


	backupSftpDirectory=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.directory'" | grep value | cut -d " " -f3)
	echo "$backupSftpDirectory"
	
}