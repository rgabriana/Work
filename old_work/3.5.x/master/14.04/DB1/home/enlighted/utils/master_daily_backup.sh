#!/bin/bash
echo "`date`: Initialize backup"
backup_directory="/home/enlighted/backups"
mkdir -p ${backup_directory}
cd "${backup_directory}"
DAILY_BACKUP_PREFIX="daily_dump"
rm -f ${DAILY_BACKUP_PREFIX}*backup
rm -f CLOUD_BACKUP_VERSION


export manifestfile=/var/lib/tomcat6/webapps/ecloud/META-INF/MANIFEST.MF
versionstring1=$(cat $manifestfile | grep Implementation-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
versionstring2=$(cat $manifestfile | grep Build-Version | sed -re 's/^.+: ([0-9.]+).*/\1/')
versionstringnumber="$versionstring1.$versionstring2" 
export BACKUP_VERSION=$versionstringnumber


today=$(date '+%m'-'%d'-'%Y'_'%H'-'%M'-'%S')

DAILY_BACKUP_FILE="${DAILY_BACKUP_PREFIX}_${BACKUP_VERSION}_$today.backup"
DAILY_TEMP_TAR="${DAILY_BACKUP_PREFIX}.temptartz"



echo ${BACKUP_VERSION} > CLOUD_BACKUP_VERSION

pg_dump -U DB1 emscloud -f "${DAILY_BACKUP_FILE}"

if [ $? -eq 0 ]
then
    tar -zcvf "${DAILY_TEMP_TAR}" $DAILY_BACKUP_FILE CLOUD_BACKUP_VERSION
    sleep 2
    rm "$DAILY_BACKUP_FILE" CLOUD_BACKUP_VERSION
else
    exit
fi

backups_no=$(ls -l $DAILY_BACKUP_PREFIX*.tar.gz | wc -l )
while [ "${backups_no}" -gt 2 ]
do
    REMOVE_FILE=$(ls -ltr --time-style="+%Y-%m-%d %H:%M:%S" | grep $DAILY_BACKUP_PREFIX.*tar.gz | head -n 1 | tr -s ' ' ' ' | cut -d" " -f8)
    if [ ! -z $REMOVE_FILE ]
    then
        rm -f "${REMOVE_FILE}"
        backups_no=$(ls -l $DAILY_BACKUP_PREFIX*.tar.gz | wc -l )
    else
        break
    fi
done

mv $DAILY_TEMP_TAR "${DAILY_BACKUP_PREFIX}_${BACKUP_VERSION}_$today.tar.gz"
echo "`date`: backup complete"
#rsync --delete -azvv /home/enlighted/backups/ enlighted@192.168.2.223:/home/enlighted/backups
########################################
# This file is being managed by puppet #
########################################
