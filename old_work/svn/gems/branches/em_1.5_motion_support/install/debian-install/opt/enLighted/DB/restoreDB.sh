#!/bin/bash

if [ $# -ne 1 ]
then
  echo "Usage: sudo restoreDB.sh DBBK_FILENAME"
  exit 1;
fi

BACKUP_FILE=$1
TMP_BACKUP_DIR=/tmp/sys_db_backup
BACNET_DB_FILE=bacnet.db

if [ -d ${TMP_BACKUP_DIR} ]
then
    # Remove sql and bacnet db files.
    rm -f ${TMP_BACKUP_DIR}/*.sql
    rm -f ${TMP_BACKUP_DIR}/${BACNET_DB_FILE}
else
    mkdir -p ${TMP_BACKUP_DIR}
fi

# untar the zipped file to backup directory.
tar -C ${TMP_BACKUP_DIR} -xzf $BACKUP_FILE

# Copy psql file.
cat ${TMP_BACKUP_DIR}/*.sql > db_backup.sql

# Copy bacnet db file.
cp ${TMP_BACKUP_DIR}/${BACNET_DB_FILE} /var/lib/tomcat6/Enlighted

# Database details
PGHOST="localhost"
PGUSER="postgres"
PGDATABASE="ems"
/usr/bin/dropdb -U $PGUSER $PGDATABASE
/usr/bin/createdb -U $PGUSER $PGDATABASE
/usr/bin/psql -U $PGUSER $PGDATABASE < db_backup.sql
rm -rf db_backup.sql

