#!/bin/bash -x
#########################
# kernelinstall.sh	#
#########################

set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

# Root check

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this scripts"
  exit
fi

# Source check

if [ ! -d $src_dir/kernel ]; then
  echo "The source directory $src_dir/kernel is missing.  You must first run the get_kernel_source script"
  exit
fi

source $src_dir/master_cloud_setup.config

for((k=0;k<${#KERNEL[@]};k++))
do
SOURCE=${KERNEL[$k]}
DESTINATION=`echo $SOURCE | sed -e "s!$SECURITY_SRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
  if [ ! -f $DIR/$FILE ] || [ $(grep -s -c $(shasum $DIR/$FILE|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
  echo "$FILE not found or it's out of date.  Updating $FILE"

    if [ -f $DESTINATION ]; then

      echo "Creating backup of $FILE"
      cp -v $DIR/$FILE $DIR/$FILE.installbk.$(date +%d%b%Y_%T)

    fi

  echo "Updating $FILE"
    cp -v $src_dir/kernel/$FILE $DIR/$FILE
    chown -v ${KERNELO[$k]} $DIR/$FILE
    chmod -v ${KERNELP[$k]} $DIR/$FILE
else
  echo "$FILE is up to date"
fi
done 
