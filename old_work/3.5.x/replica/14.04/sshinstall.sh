#!/bin/bash -x
#################################################
# sshinstall.sh
#################################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

## ROOT CHECK ##

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi

## SOURCE CHECK ##
source $src_dir/$SETUP_CONFIG

if [ ! -d $src_dir/ssh ]; then
  echo "$(date): $src_dir/ssh not exists, Please first run the get_ssh_source.sh script."
  exit 1
fi

for((i=0;i<${#SSHARRAY[@]};i++))

do

  SOURCE=${SSHARRAY[$i]}
  DESTINATION=`echo $SOURCE | sed -e "s!$SECURITY_SRC!!"|sed -e "s!$REPLICA_OS_SRC!!"` 
  DIR=`dirname $DESTINATION`
  FILE=`basename $DESTINATION`

  if [ ! -f $DESTINATION ] || [ $(grep -s -c $(shasum $DESTINATION|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then

    echo "$FILE is not up to date.  Updating $FILE"

    if [ -f $DESTINATION ]; then 
    
      echo "Creating $FILE backup"
      /bin/cp -v $DESTINATION $DESTINATION.installbk.$(date +%d%b%Y_%T)

    fi

    echo "Updating $FILE"
    /bin/cp -v $src_dir/ssh/$FILE $DESTINATION
    /bin/chown -v ${SSHOWNER[$i]} $DESTINATION
    /bin/chmod -v ${SSHPERMISSIONS[$i]} $DESTINATION

  else 

    echo "$FILE is up to date"

  fi

done
