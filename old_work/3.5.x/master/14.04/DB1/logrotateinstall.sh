#!/bin/bash -x
#####################################################
# logrotateinstall.sh 
#####################################################
set -e 

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

## ROOT CHECK ##
if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi

source /tmp/master_cloud_DB1_setup.config

## DIR CHECK ##
if [ ! -d $src_dir/logrotate ];then
  echo "source directory does not exists.  Please make sure you run the get_logrotate_source.sh script before continuing.  Thank you."
  exit
fi

for((i=0;i<${#LOGROTATE[@]};i++))
do
SOURCE=${LOGROTATE[$i]}
DESTINATION=`echo $SOURCE | sed -e "s!$MASTER_OS_SRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
  if [ ! -f $DIR/$FILE ] || [ $(grep -s -c $(shasum $DIR/$FILE|awk -F " " '{print $1}') $SUMFILE) = "0" ]; then 

    echo "$FILE is missing or out of date.  Updatng $FILE"

    if [ -f $DESTINATION ]; then
      
      echo "Creating backup of $FILE"
      cp -v $DESTINATION $DESTINATION.installbk.$(date +%d%b%y_%T)

    fi

    cp -v $src_dir/logrotate/$FILE $DESTINATION
    chmod 644 $DIR/$FILE
    chown root:root $DIR/$FILE

  else 

    echo "$FILE is up to date"

  fi

done
