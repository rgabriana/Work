#!/bin/bash -x
######################################
# get_enlighted_source.sh
######################################
set -e 

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

## ROOT CHECK ##

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi

## DIR CHECK ##

source /tmp/master_cloud_DB1_setup.config

if [ ! -d $src_dir/enlighted ]; then
  echo "$(date): Source dir does not exists, creating $src_dir/enlighted"
  mkdir -pv $src_dir/enlighted
fi

for((i=0;i<${#ENLIGHTED[@]};i++))

do
  SOURCE=${ENLIGHTED[$i]}
  DESTINATION=`echo ${SOURCE} | sed -e "s!${MASTER_OS_SRC}!!"`
  DIR=`dirname $DESTINATION`
  FILE=`basename $DESTINATION`
  if [ ! -f $src_dir/enlighted/$FILE ] || [ $(grep -s -c $(shasum $src_dir/enlighted/$FILE|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
    echo "$FILE is missing or out of date.  Downloading updated $FILE"
    $XFER --output-document=$src_dir/enlighted/${FILE} $SOURCE
  else 
    echo "$FILE is up to date"
  fi
done
