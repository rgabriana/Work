#!/bin/bash -x
################################################
# get_psql_source.sh
################################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi


if [ ! -d $src_dir/psql ]; then
  echo "$(date): Source dir does not exists, creating $src_dir/psql"
  mkdir -pv $src_dir/psql
fi

source $src_dir/$SETUP_CONFIG

SOURCE=$SQL_FILE
FILE=`echo $SQL_FILE | sed -e "s!$MASTER_OS_SRC!!"`

if [ ! -f $src_dir/psql/$FILE ] || [ $(grep -s -c $(shasum $src_dir/psql/$FILE | awk -F " " '{print $1}') $SUMFILE) = 0 ]; then 

  echo "$FILE is missing or out of date.  Updating $FILE" 

  $XFER --output-document=$src_dir/psql/$FILE $SOURCE

else

  echo "$FILE is up to date"

fi 

psql -Upostgres emscloud < $src_dir/psql/$FILE
