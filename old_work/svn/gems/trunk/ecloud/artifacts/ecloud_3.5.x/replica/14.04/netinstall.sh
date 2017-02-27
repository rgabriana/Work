#!/bin/bash -x
#####################################################
# TITLE: netinstall.sh 
# PURPOSE: The purpose of this script is to setup the network config of the replica server
# NOTES: -NONE-
#####################################################
set -e 

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

## ROOT CHECK ##
if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi

source $src_dir/$SETUP_CONFIG

## DIR CHECK ##
if [ ! -d $src_dir/netinstall ];then
  echo "$src_dir/netinstall directory does not exists.  Please make sure you run the get_netinstall_source.sh script before continuing.  Thank you."
  exit
fi

for((i=0;i<${#NET[@]};i++))
do
SOURCE=${NET[$i]}
DESTINATION=`echo $SOURCE | sed -e "s!$REPLICA_OS_SRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
  if [ ! -f $DIR/$FILE ] || [ $(grep -s -c $(shasum $DIR/$FILE|awk -F " " '{print $1}') $SUMFILE) = "0" ]; then 

    echo "$FILE is missing or out of date.  Updatng $FILE"

    if [ -f $DESTINATION ]; then
      
      echo "Creating backup of $FILE"
      cp -v $DESTINATION $DESTINATION.installbk.$(date +%d%b%y_%T)

    fi

    cp -v $src_dir/netinstall/$FILE $DESTINATION
    chmod -c ${NETP[$i]} $DIR/$FILE
    chown -c ${NETO[$i]} $DIR/$FILE

  else 

    echo "$FILE is up to date"

  fi

done
