#!/bin/bash -x
#################################################
# get_sudoers_source.sh
#################################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

## ROOT CHECK ##

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi

## SOURCE CHECK ##

if [ ! -d $src_dir/sudoers ]; then
  echo "$(date): $src_dir/sudoers does not exists, creating $src_dir/sudoers"
  mkdir -pv $src_dir/sudoers
fi

source $src_dir/$PXECFG

for((i=0;i<${#SUDOERS[@]};i++))

do
  SOURCE=${SUDOERS[$i]}
  DESTINATION=`echo $SOURCE | sed -e "s!$SECURITY_SRC!!"`
  DIR=`dirname $DESTINATION`
  FILE=`basename $DESTINATION`
  if [ ! -f $src_dir/sudoers/$FILE ] || [ $(grep -s -c $(shasum $src_dir/sudoers/$FILE|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
    echo "$FILE is missing or out of date.  Updating $FILE"
      $XFER --output-document=$src_dir/sudoers/$FILE $SOURCE
    else
      echo "$FILE is up to date"
  fi
done
