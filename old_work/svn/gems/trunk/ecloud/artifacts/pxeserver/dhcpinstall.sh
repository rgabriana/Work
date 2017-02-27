#!/bin/bash -x
###################
# dhcpinstall.sh #
###################

set -e 

# Root Check #

if [ `id -u` != 0 ]; then 
  echo "You need administrator priveleges to execute this program"
  exit
fi 

# Source check

source $src_dir/$PXECFG

if [ ! -d $SRC_DIR/dhcp ]; then 
  echo "The source directory $SRC_DIR/dhcp is missing.  You must first run the get_tftp_source.sh script"
  exit
fi

apt-get -y update

PKG[0]=isc-dhcp-server

for((p=0;p<${#PKG[@]};p++))
do 
  if [ $(dpkg -s ${PKG[$p]} 2> /dev/null|grep -s -c 'Status: install ok installed') = 0 ]; then 
    apt-get --assume-yes install ${PKG[$p]}
  else
    echo "${PKG[$p]} already installed"
  fi 
done

# Dir and file check #
for((t=0;t<${#DHCP[@]};t++))
do 
SRC=$SRC_DIR/dhcp
SOURCE=${DHCP[$t]}
DESTINATION=`echo $SOURCE | sed -e "s!$PXESRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`

  if [ ! -f $DIR/$FILE ] || [ $(grep -s -c $(shasum $DIR/$FILE|awk -F " " '{print $1}') $src_dir/$SUMFILE) = 0 ]; then
    echo "$FILE is missing or is out of date.  Updating $FILE"
    echo "Creating $FILE backups"
    cp -v $DIR/$FILE $DIR/$FILE.installbk.$(date +%d%b%Y_%T)
    cp -v $SRC/$FILE $DIR/$FILE
    chown -v ${DHCPO[$t]} $DIR/$FILE
    chmod -v ${DHCPP[$t]} $DIR/$FILE
  else
    echo "$FILE is up to date"
  fi 
done