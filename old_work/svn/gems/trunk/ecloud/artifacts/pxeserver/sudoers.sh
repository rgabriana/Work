#!/bin/bash -x
#################################################
# sudoers.sh
#################################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

## ROOT CHECK ##

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi

## SOURCE CHECK ##

source $src_dir/$PXECFG

if [ ! -d $src_dir/sudoers ]; then
  echo "$(date): $src_dir/sudoers not exists, Please first run the get_sudoers_source.sh script."
  exit 1
fi

for((i=0;i<${#SUDOERS[@]};i++))

do

  SOURCE=${SUDOERS[$i]}
  DESTINATION=`echo $SOURCE | sed -e "s!$SECURITY_SRC!!"` 
  DIR=`dirname $DESTINATION`
  FILE=`basename $DESTINATION`

  if [ ! -f $DESTINATION ] || [ $(grep -s -c $(shasum $DESTINATION|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then

    echo "$FILE is not up to date.  Updating $FILE"

    if [ -f $DESTINATION ]; then 
    
      echo "Creating $FILE backup"
      /bin/cp -v $DESTINATION $DESTINATION.installbk.$(date +%d%b%Y_%T)

    fi

    echo "Updating $FILE"
    /bin/cp -v $src_dir/sudoers/$FILE $DESTINATION
    /bin/chown -v ${SUDOERSO[$i]} $DESTINATION
    /bin/chmod -v ${SUDOERSP[$i]} $DESTINATION

  else 

    echo "$FILE is up to date"

  fi

done
