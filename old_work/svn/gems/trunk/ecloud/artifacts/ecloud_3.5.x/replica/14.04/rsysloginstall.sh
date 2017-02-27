#!/bin/bash -x
#####################################################
# rsysloginstall.sh 
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
if [ ! -d $src_dir/rsyslog ];then
  echo "$src_dir directory does not exists.  Please make sure you run the get_rsyslog_source.sh script before continuing.  Thank you."
  exit
fi

for((i=0;i<${#RSYSLOG[@]};i++))
do
SOURCE=${RSYSLOG[$i]}
DESTINATION=`echo ${SOURCE} | sed -e "s!${REPLICA_OS_SRC}!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
if [ ! -f $DIR/$FILE ] || [ $(grep -s -c $(shasum $DIR/$FILE|awk -F " " '{print $1}') $SUMFILE) = "0" ]; then

  echo "$FILE is missing or out of date.  Updating $FILE"

  if [ -f $DESTINATION ]; then 

    echo "creating backup of $FILE"
    cp -v $DIR/$FILE $DIR/$FILE.installbk.$(date +%d%b%Y_%T)

  fi

  cp -v ${src_dir}/rsyslog/${FILE} ${DIR}/${FILE}
  chmod -v ${RSYSLOGP[$i]} $DIR/$FILE
  chown -v ${RSYSLOGO[$i]} $DIR/$FILE

else

  echo "$FILE is up to date"

fi

done
