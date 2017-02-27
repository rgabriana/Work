#!/bin/bash -x
######################################
# get_enlightedca_source.sh
######################################
set -e 

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

## ROOT CHECK ##

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi

## DIR CHECK ##
if [ ! -d $src_dir/enlightedca ]; then
  echo "$(date): Source dir does not exists, creating $src_dir/enlightedca"
  mkdir -pv $src_dir/enlightedca
fi

source /tmp/master_cloud_DB1_setup.config

for((i=0;i<${#ENLIGHTEDCA[@]};i++))
do
  SOURCE=${ENLIGHTEDCA[$i]}
  DESTINATION=`echo ${SOURCE} | sed -e "s!${MASTER_OS_SRC}!!"`
  DIR=`dirname ${DESTINATION}`
  FILE=`basename ${DESTINATION}`
  if [ ! -f $DESTINATION ] || [ $(grep -s -c $(shasum $DESTINATION | awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
    echo "$FILE is missing or it's out of date.  Downloading $FILE"
    $XFER --output-document=$src_dir/enlightedca/${FILE} $SOURCE
  else
    echo "$FILE is up to date"
  fi
done
