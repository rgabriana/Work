#!/bin/bash -x 
###########################
# get_netinstall_source.sh
###########################

set -e 

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

# Root check
if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this scripts"
  exit
fi

source $src_dir/$SETUP_CONFIG

# FILES and DIR checks

if [ ! -d $src_dir/netinstall ]; then
  echo "$(date); $src_dir/net does not exists, creating $src_dir/netinstall"
  mkdir -pv $src_dir/netinstall
fi

for ((n=0;n<${#NET[@]};n++))

do

SOURCE=${NET[$n]}
DESTINATION=`echo $SOURCE | sed -e "s!$MASTER_OS_SRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
  if [ ! -f $src_dir/netinstall/$FILE ] || [ $(grep -s -c $(shasum $src_dir/netinstall/$FILE | awk -F " " '{print $1}') $SUMFILE) = 0]; then
    echo "$FILE is missing or out of date. Updating $FILE"
    $XFER --output-document=$src_dir/netinstall/$FILE $SOURCE
  else
    echo "$FILE is up to date"
  fi
done

