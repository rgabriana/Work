#!/bin/bash -x
#####################################################
# enlightedcainstall.sh
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
if [ ! -d $src_dir/enlightedca ]; then
  echo "$src_dir/enlightedca directory does not exists.  Please make sure you run the get_enlightedca_source.sh script before continuing.  Thank you."
  exit
fi

for((i=0;i<${#ENLIGHTEDCA[@]};i++))
do
SOURCE=${ENLIGHTEDCA[$i]}
DESTINATION=`echo ${SOURCE} | sed -e "s!${MASTER_OS_SRC}!!"`
DIR=`dirname ${DESTINATION}`
FILE=`basename ${DESTINATION}`
  if [ ! -d ${DIR} ] || [ ! -f $DESTINATION ] || [ $(grep -s -c $(shasum $DESTINATION|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then

    echo "$FILE is missing or out of date.  Updating $FILE"
    mkdir -pv ${DIR}

    if [ -f $DESTINATION ]; then

      echo "Creating Backup"
      cp -v $DESTINATION $DESTINATION.installbk.$(date +%d%b%Y_%T)

    fi

    cp -v ${src_dir}/enlightedca/${FILE} $DESTINATION
    chown -v enlighted:enlighted $DESTINATION
    chown -vR enlighted:enlighted $DIR
    chmod -Rv 755 ${DIR}

  fi
done
