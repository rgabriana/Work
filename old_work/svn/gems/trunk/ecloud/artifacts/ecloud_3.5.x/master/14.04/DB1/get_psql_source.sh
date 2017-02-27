#!/bin/bash -x
#################################################
# get_psql_source.sh
#################################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

## ROOT CHECK ##
if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi

## DIR CHECK ##
if [ ! -d $src_dir/psql ]; then
  echo "$(date): Source dir does not exists, creating $src_dir/psql"
  mkdir -pv $src_dir/psql
fi

source /tmp/master_cloud_DB1_setup.config

## XFER ##
for((i=0;i<${#PSQL[@]};i++))
do
  SOURCE=${PSQL[$i]}
  DESTINATION=`echo ${SOURCE} | sed -e "s!${MASTER_OS_SRC}!!"`
  DIR=`dirname $DESTINATION`
  FILE=`basename $DESTINATION`
  if [ ! -f $src_dir/psql/$FILE ] || [ $(grep -s -c $(shasum $src_dir/psql/$FILE|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
    echo "$FILE is missing or out of date.  Downloading $FILE"
    $XFER --output-document=$src_dir/psql/$FILE $SOURCE
  else
    echo "$FILE file is up to date"
  fi
done
