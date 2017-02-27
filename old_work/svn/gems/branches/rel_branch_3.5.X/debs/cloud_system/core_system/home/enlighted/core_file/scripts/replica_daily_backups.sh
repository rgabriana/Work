#!/bin/bash

backup() {
    db="$1"
    backup_directory="/home/enlighted/backups/${db}"
    mkdir -p ${backup_directory}
    cd "${backup_directory}"
    DAILY_BACKUP_PREFIX="daily_dump"
    rm -f ${DAILY_BACKUP_PREFIX}*backup
    rm -f CLOUD_BACKUP_VERSION

    BACKUP_VERSION=$(psql -x -U postgres ${db} -c "select value from system_configuration where name like 'gems.version.build'" | grep value | cut -d " " -f3)
    today=$(date '+%m'-'%d'-'%Y'_'%H'-'%M'-'%S')

    DAILY_BACKUP_FILE="${DAILY_BACKUP_PREFIX}_${BACKUP_VERSION}_$today.backup"
    DAILY_TEMP_TAR="${DAILY_BACKUP_PREFIX}.temptartz"

    echo ${BACKUP_VERSION} > CLOUD_BACKUP_VERSION

    pg_dump -U postgres ${db} | gzip > "${DAILY_BACKUP_FILE}"

    if [ $? -eq 0 ]
    then
        tar -zcvf "${DAILY_TEMP_TAR}" $DAILY_BACKUP_FILE CLOUD_BACKUP_VERSION
        sleep 2
        rm "$DAILY_BACKUP_FILE" CLOUD_BACKUP_VERSION
    else
        exit
    fi

    backups_no=$(ls -l $DAILY_BACKUP_PREFIX*.tar.gz | wc -l )
    while [ "${backups_no}" -gt 1 ]
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
}

mkdir -p /home/enlighted/backups
psql -x -U postgres -c "select datname from pg_database where datname like 'em_%' order by datname" | grep datname | cut -d" " -f3 > "/home/enlighted/backups/alldbs"

while read line           
do           
    backup "$line"           
done < /home/enlighted/backups/alldbs

