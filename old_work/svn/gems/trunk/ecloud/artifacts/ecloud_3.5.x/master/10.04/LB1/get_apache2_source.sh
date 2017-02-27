#!/bin/bash -x 
#################################################
# Author: Rolando R. Gabriana Jr. 		#
#  Get source files for apache2 configs	\	#
#  for ubuntu 10.04 and enlighted version 3.5.x	#
# usage: bash get_apache2_source.sh		# 
#################################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

#Root check

if [ `id -u` != "0" ]; then 
  echo "you need root priveleges to execute this script"
  exit
fi

# Create download folder 

source $src_dir/$SETUP_CONFIG

if [ ! -d $src_dir/apache2 ]; then
  echo "$(date): Source dir does not exists, creating $src_dir/apache2" 
  mkdir -pv $src_dir/apache2
fi

for ((i=0;i<${#A2[@]};i++))
do 
SOURCE=${A2[$i]}
DESTINATION=`echo $SOURCE | sed -e "s!$MASTER_OS_SRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
  if [ ! -f $src_dir/apache2/$FILE ] || [ $(grep -s -c $(shasum $src_dir/apache2/$FILE|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
    echo "$FILE not foud or out of date.  Updating $FILE"  
    $XFER --output-document=$src_dir/apache2/$FILE $SOURCE
  else
    echo "$FILE is up to date" 
  fi
done
