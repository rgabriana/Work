#!/bin/bash
source /etc/environment
export ENCRYPTDECRYPTSCRIPT="$EM_MGMT_HOME/adminscripts/encryptdecryptstring.sh"

backupSftpUsername=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.username'" | grep value | cut -d " " -f3)
backupSftpEncryptedPassword=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.password'" | grep value | cut -d " " -f3)
backupSftpIp=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.ip'" | grep value | cut -d " " -f3)
backupSftpDirectory=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.directory'" | grep value | cut -d " " -f3)
backupSftpPassword=$($ENCRYPTDECRYPTSCRIPT "decrypt" $backupSftpEncryptedPassword)

OLD_IFS=$IFS
IFS=$'\n'
									      #<size>#<Timestamp>#<filename>			
	if [ ! -z $backupSftpUsername ] && [ ! -z $backupSftpPassword ] && [ ! -z $backupSftpIp ] && [ ! -z $backupSftpDirectory ]
	then
		lftp -c "open sftp://${backupSftpUsername}:${backupSftpPassword}@${backupSftpIp}; set net:max-retries 2;set net:persist-retries 2;set net:reconnect-interval-base 2;set net:reconnect-interval-max 2;set net:reconnect-interval-multiplier 2;set net:timeout 2;set ftp:list-options -a;cd ${backupSftpDirectory} ; ls -l | sort -k6M -k7 -k8 | tail -11; bye" > /tmp/sftpfilelist.txt 2>&1
		Sftplistcommandoutputfile="/tmp/sftpfilelist.txt"
		if grep -q "failed" "$Sftplistcommandoutputfile" || grep -q "error" "$Sftplistcommandoutputfile"
		then
			echo ""
		else
			listoffiles=$(awk 'BEGIN { OFS="#" }; {print $5,$6" "$7" "$8,$9}' /tmp/sftpfilelist.txt | grep ".tar.gz")
			for file in $listoffiles
			do
				echo $file
			done
		fi
	else
		echo ""
	fi
IFS=$OLD_IFS