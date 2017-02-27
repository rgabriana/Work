#!/bin/bash -x 
#######################################
# Get source files for kernel configs #
#######################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

#Root check

if [ `id -u` != "0" ]; then 
  echo "you need root priveleges to execute this script"
  exit
fi

# Create download folder 

if [ ! -d $src_dir/kernel ]; then
  echo "$(date): Source dir is missing, creating $src_dir/kernel" 
  mkdir -pv $src_dir/kernel
fi

source $src_dir/$SETUP_CONFIG

for ((k=0;k<${#KERNEL[@]};k++))
do
SOURCE=${KERNEL[$k]}
DESTINATION=`echo $SOURCE | sed -e "s!$SECURITY_SRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
  if [ ! -f $src_dir/kernel/$FILE ] || [ $(grep -s -c $(shasum $src_dir/kernel/$FILE|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then 
    echo "$FILE is missing or out of date.  Downloading new $FILE."
    $XFER --output-document=$src_dir/kernel/$FILE ${KERNEL[$i]}
  else 
    echo "$FILE is up to date"
 fi
done
