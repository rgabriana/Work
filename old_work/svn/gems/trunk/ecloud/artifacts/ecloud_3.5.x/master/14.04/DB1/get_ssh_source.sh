#!/bin/bash -x
#################################################
# get_ssh_source.sh
#################################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

## ROOT CHECK ##

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi

## SOURCE CHECK ##

if [ ! -d $src_dir/ssh ]; then
  echo "$(date): $src_dir/ssh does not exists, creating $src_dir/ssh"
  mkdir -pv $src_dir/ssh
fi

source /tmp/master_cloud_DB1_setup.config

for((i=0;i<${#SSHARRAY[@]};i++))
do
  SOURCE=${SSHARRAY[$i]}
  DESTINATION=`echo $SOURCE | sed -e "s!$SECURITY_SRC!!"| sed -e "s!$MASTER_OS_SRC!!"`
  DIR=`dirname $DESTINATION`
  FILE=`basename $DESTINATION`
  if [ ! -f $src_dir/ssh/$FILE ] || [ $(grep -s -c $(shasum $src_dir/ssh/$FILE|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
    echo "$FILE is missing or out of date.  Updating $FILE"
      $XFER --output-document=$src_dir/ssh/$FILE $SOURCE
    else
      echo "$FILE is up to date"
  fi
done
