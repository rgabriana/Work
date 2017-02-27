#!/bin/bash -x
######################################
# get_logrotate_source.sh
######################################
set -e 

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

## ROOT CHECK ##

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi

## DIR CHECK ##
if [ ! -d $src_dir/logrotate ]; then
  echo "$(date): Source dir does not exists, creating $src_dir/logrotate"
  mkdir -pv $src_dir/logrotate
fi

source $src_dir/$SETUP_CONFIG

for((l=0;l<${#LOGROTATE[@]};l++))
do
SOURCE=${LOGROTATE[$l]}
DESTINATION=`echo $SOURCE | sed -e "s!$MASTER_OS_SRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
  if [ ! -f $src_dir/logrotate/$FILE ] || [ $(grep -s -c $(shasum $src_dir/logrotate/$FILE|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
    echo "$FILE is missing or out of date.  Downloading new $FILE"
    $XFER --output-document=$src_dir/logrotate/$FILE $SOURCE
  else 
    echo "$FILE is up to date"
  fi
done
