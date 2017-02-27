#!/bin/bash -x
#########################################
# Title: Apache2 install script 	#
# usage: apache2install.sh		#
#########################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

# Root check
if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this scripts"
  exit
fi

source $src_dir/$SETUP_CONFIG

## Source check ##

if [ ! -d $src_dir/apache2 ]; then
  echo "The source directory $src_dir/apache2 is missing.  You must first run the get_apache2_source script"
  exit
fi

for ((a=0;a<${#A2[@]};a++))
do 
SOURCE=${A2[$a]}
DESTINATION=`echo $SOURCE |  sed -e "s!$MASTER_OS_SRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`

  if [ ! -f $DESTINATION ] || [ $(grep -s -c $(shasum $DESTINATION) $src_dir/$SUMFILE) = 0 ]; then 

  echo "$FILE is missing or out of date"

    if [ -f $DESTINATION ]; then 
      echo "Creating backup of $FILE"
      cp -v $DESTINATION $DESTINATION.patchbk.$(date +%d%b%Y_%T)
    fi
 
  echo "Updating $FILE"
  cp -v $src_dir/apache2/$FILE $DESTINATION
  
  fi 
done

service apache2 restart
