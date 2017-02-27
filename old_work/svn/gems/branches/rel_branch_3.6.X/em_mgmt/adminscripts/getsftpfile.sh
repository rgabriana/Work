#!/bin/bash
source /etc/environment
export BACKUP_SFTP_FILE_NAME=$1
export ENCRYPTDECRYPTSCRIPT="$EM_MGMT_HOME/adminscripts/encryptdecryptstring.sh"

backupSftpUsername=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.username'" | grep value | cut -d " " -f3)
backupSftpEncryptedPassword=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.password'" | grep value | cut -d " " -f3)
backupSftpIp=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.ip'" | grep value | cut -d " " -f3)
backupSftpDirectory=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.sftp.directory'" | grep value | cut -d " " -f3)
backupSftpPassword=$($ENCRYPTDECRYPTSCRIPT "decrypt" $backupSftpEncryptedPassword)
Sftpgetfilecommandoutput=$(lftp sftp://${backupSftpUsername}:${backupSftpPassword}@${backupSftpIp}  -e "set net:max-retries 2;set net:persist-retries 2;set net:reconnect-interval-base 2;set net:reconnect-interval-max 2;set net:reconnect-interval-multiplier 2;set net:timeout 2;set ftp:list-options -a; cd ${backupSftpDirectory}; get ${BACKUP_SFTP_FILE_NAME} -o /tmp/${BACKUP_SFTP_FILE_NAME}; bye" 2>&1)
