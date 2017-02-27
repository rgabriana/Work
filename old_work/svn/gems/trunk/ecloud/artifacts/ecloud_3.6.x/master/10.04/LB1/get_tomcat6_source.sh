#!/bin/bash -x 
#########################################################################
# Author: Rolando R. Gabriana Jr. 					#
#  Get source files for tomcat6 configs	\				#
#  for ubuntu 10.04 and enlighted version 3.5.x				#
# usage: bash -x get_tomcat6_source.sh > <install_log_file> 2>&1	# 
#########################################################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

# Root check
if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this scripts"
  exit
fi

# Create download folder 

if [ ! -d $src_dir/tomcat6 ]; then
  echo "$(date): Source dir doesn't exists, creating $src_dir/tomcat6" 
  mkdir -pv $src_dir/tomcat6
fi

source $src_dir/$SETUP_CONFIG


for((i=0;i<${#TC6[@]};i++))
do
 
  SOURCE=${TC6[$i]}
  DESTINATION=`echo $SOURCE|sed -e "s!${MASTER_OS_SRC}!!"`
  DIR=`dirname $DESTINATION`
  FILE=`basename $DESTINATION`
  
  if [ ! -f $src_dir/$FILE ] || [ $(grep -s -c $(shasum $src_dir/$FILE | awk -F " " '{print $1}') $src_dir/$SUMFILE) = 0 ]; then 

    echo "$FILE is missing or out of date.  Updating $FILE"
    $XFER --output-document=$src_dir/tomcat6/${FILE} $SOURCE

  else
  
    echo "$FILE is up to date" 

  fi
done
