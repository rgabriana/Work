#!/bin/bash -x
######################################
# get_audit_source.sh
######################################
set -e 

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

## ROOT CHECK ##

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi

## DIR CHECK ##
if [ ! -d $src_dir/audit ]; then
  echo "$(date): Source dir does not exists, creating $src_dir/audit"
  mkdir -pv $src_dir/audit
fi

source /tmp/master_cloud_DB1_setup.config

for((i=0;i<${#AUDIT[@]};i++))
do
SOURCE=${AUDIT[$i]}
DESTINATION=`echo $SOURCE | sed -e "s!$SECURITY_SRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
  if [ ! -f $src_dir/audit/$FILE ] || [ $(grep -s -c $(shasum $src_dir/audit/$FILE | awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
    echo "$FILE is missing or out of date. Downloading updated $FILE"
    $XFER --output-document=$src_dir/audit/$FILE $SOURCE
  else
    echo "$FILE is up to date"
  fi
done

## APT ##

if [ ! -f /etc/apt/sources.list ] || [ $(grep -s -c $(shasum /etc/apt/sources.list|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then

  if [ -f /etc/apt/sources.list ]; then

    echo "Creating backup of the sources.list" 
    cp -v /etc/apt/sources.list /etc/apt/sources.list.installbk.$(date +%d%b%Y_%T)

  fi

  echo "files is missing or out of date. Updating file"
  $XFER --output-document=/etc/apt/sources.list $MASTER_OS_SRC/etc/apt/sources.list

fi
