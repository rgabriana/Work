#!/bin/bash -x
######################################
# get_rsyslog_source.sh
######################################
set -e 

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

## ROOT CHECK ##

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi

## DIR CHECK ##
if [ ! -d $src_dir/rsyslog ]; then
  echo "$(date): Source dir does not exists, creating $src_dir/rsyslog"
  mkdir -pv $src_dir/rsyslog
fi

source $src_dir/$SETUP_CONFIG

for((r=0;r<${#RSYSLOG[@]};r++))
do
SOURCE=${RSYSLOG[$r]}
DESTINATION=`echo $SOURCE | sed -e "s!${REPLICA_OS_SRC}!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
  if [ ! -f $src_dir/rsyslog/$FILE ] || [ $(grep -s -c $(shasum $src_dir/rsyslog/$FILE|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
    echo "$FILE is missing or out of date.  Downloading updated $FILE"
    $XFER --output-document=$src_dir/rsyslog/${FILE} $SOURCE
  else
    echo "$FILE is up to date"
  fi
done
