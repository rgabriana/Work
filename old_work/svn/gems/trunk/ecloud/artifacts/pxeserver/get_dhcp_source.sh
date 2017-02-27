#!/bin/bash -x
######################
# get_dhcp_source.sh #
######################

set -e 

# Root Check #

if [ `id -u` != 0 ]; then 
  echo "You need administrator priveleges to execute this program"
  exit
fi 

source $src_dir/$PXECFG
# Dir and file check #
for((t=0;t<${#DHCP[@]};t++))
do 
SRC=$SRC_DIR/dhcp
SOURCE=${DHCP[$t]}
DESTINATION=`echo $SOURCE | sed -e "s!$PXESRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`

  if [ ! -d $SRC ]; then
    echo "source folder is missing"
    mkdir -pv $SRC 
  fi 

  if [ ! -f $SRC/$FILE ] || [ $(grep -s -c $(shasum $SRC/$FILE | awk -F " " '{print $1}') $src_dir/$SUMFILE) = 0 ]; then
    echo "$FILE is missing or out of date.  Updating $FILE"
    $XFER --output-document=$SRC/$FILE $SOURCE
  else 
    echo "$FILE is up to date"
 fi 
done
