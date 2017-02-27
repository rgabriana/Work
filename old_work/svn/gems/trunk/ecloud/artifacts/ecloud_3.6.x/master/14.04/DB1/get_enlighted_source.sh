#!/bin/bash -x
################################################
# get_enlighted_source.sh
################################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi


if [ ! -d $src_dir/ENL_files ]; then
  echo "$(date): Source dir does not exists, creating $src_dir/ENL_files"
  mkdir -pv $src_dir/ENL_files
fi

source $src_dir/$SETUP_CONFIG 

for((i=0;i<${#UTILS[@]};i++))
do
  echo $i
  SOURCE=${UTILS[$i]}
  DESTINATION=$(echo $SOURCE | sed -e "s!$MASTER_OS_SRC!!")
  DIR=`dirname $DESTINATION`
  FILE=`basename $DESTINATION`
  $XFER --output-document=$src_dir/ENL_files/$FILE $SOURCE

done
