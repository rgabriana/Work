#!/bin/bash -x
#####################################################
# auditinstall.sh
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
if [ ! -d $src_dir/audit ];then
  echo "$src_dir/audit directory does not exists.  Please make sure you run the get_audit_source.sh script before continuing.  Thank you."
  exit
fi

apt-get -y update

if [ $(dpkg -s auditd 2> /dev/null | grep -s -c 'Status: install ok installed') = 0 ]; then

  apt-get --assume-yes install\
    auditd

else

  echo "Auditd package installed"

fi

for((i=0;i<${#AUDIT[@]};i++))
do
SOURCE=${AUDIT[$i]}
DESTINATION=`echo $SOURCE | sed -e "s!$SECURITY_SRC!!" | sed -e "s!$MASTER_OS_SRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
  if [ ! -f $DESTINATION ] || [ $(grep -s -c $(shasum $DESTINATION|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
    echo "$FILE is missing or out of date.  Updating $FILE"

    if [ -f $DESTINATION ]; then
      echo "Creating backups"
      cp -v $DESTINATION $DESTINATION.installbk.$(date +%d%b%Y_%T)
    fi

    echo "Updating file"
    cp -v $src_dir/audit/$FILE $DESTINATION
    chown -v ${AUDITO[$i]} $DESTINATION
    chmod -v ${AUDITP[$i]} $DESTINATION
    chmod -v 750 /etc/audisp
  else

    echo "$FILE is up to date"
  fi
done
