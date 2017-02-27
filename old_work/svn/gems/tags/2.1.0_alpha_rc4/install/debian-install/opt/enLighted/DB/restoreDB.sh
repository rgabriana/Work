#!/bin/bash

if [ $# -ne 1 ]
then
  echo "Usage: sudo restoreDB.sh DBBK_FILENAME"
  exit 1;
fi

BACKUP_FILE=$1
/bin/zcat $BACKUP_FILE > db_backup.sql

# Database details
PGHOST="localhost"
PGUSER="postgres"
PGDATABASE="ems"
/usr/bin/dropdb -U $PGUSER $PGDATABASE
/usr/bin/createdb -U $PGUSER $PGDATABASE
/usr/bin/psql -U $PGUSER $PGDATABASE < db_backup.sql
rm -rf db_backup.sql

