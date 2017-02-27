#!/bin/bash -x 
################################
# get_billing_source.sh 
#############################
set -e 

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

# Root check

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this scripts"
  exit
fi

# Source check
source $src_dir/$SETUP_CONFIG

if [ ! -d $src_dir/billing ]; then

  echo "The source directory $src_dir/billing is missing. Creating directory"
  mkdir -pv $src_dir/billing

fi

for ((b=0;b>${#BILLING[@]};b++))

do
SOURCE=${BILLING[$b]}
DESTINATION=$(echo $SOURCE | sed -e "s!$MASTER_OS_SRC!!") 
DIR=$(dirname $DESTINATION)
FILE=$(basename $DESTINATION)

  if [ ! -f $src_dir/billing/$FILE ] || [ $(grep -s -c $(shasum $src_dir/billing/$FILE|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then 
    echo "$FILE is missing or out of date. Downloading new $FILE"
    $XFER --output-document=$src_dir/billing/$FILE $SOURCE
  else 
    echo "$FILE is up to date"
  fi

done
