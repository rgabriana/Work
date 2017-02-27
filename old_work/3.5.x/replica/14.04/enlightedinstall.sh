#!/bin/bash -x
#####################################################
# TITLE:  enlightedinstall.sh 
# PURPOSE:  The purpose of this script is to setup enlighted utilities and profile
# for the Replica server.
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
if [ ! -d $src_dir/enlighted ];then
  echo "$src_dir/enlighted source directory does not exists.  Please make sure you run the get_enlighted_source.sh script before continuing.  Thank you."
  exit
fi

for((i=0;i<${#ENLIGHTED[@]};i++))
do
  SOURCE=${ENLIGHTED[$i]}
  DESTINATION=`echo ${SOURCE} | sed -e "s!${MASTER_OS_SRC}!!"`
  DIR=`dirname ${DESTINATION}`
  FILE=`basename ${DESTINATION}`
  if [ ! -d ${DIR} ];then 
     mkdir -pv ${DIR}
  fi
  if [ ! -f $DIR/$FILE ] || [ $(grep -s -c $(shasum $DIR/$FILE|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then 
     cp -v ${src_dir}/enlighted/${FILE} ${DIR}/${FILE}
     chown -v enlighted:enlighted ${DIR}/${FILE}
     chmod -v 644 ${DIR}/${FILE}
     chmod -v 755 ${DIR}
  fi
done

echo '* 2 * * * /home/enlighted/replica_check.sh >> /var/log/enlighted/backup.log 2>&1' > /var/spool/cron/crontabs/enlighted
chmod -v 600 /var/spool/cron/crontabs/enlighted
chown -v enlighted:crontab /var/spool/cron/crontabs/enlighted
