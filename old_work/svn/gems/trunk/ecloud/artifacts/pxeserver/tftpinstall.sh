#!/bin/bash -x
###################
# tftpinstall.sh #
###################

set -e 

# Root Check #

if [ `id -u` != 0 ]; then 
  echo "You need administrator priveleges to execute this program"
  exit
fi 

# Source check

source $src_dir/$PXECFG
if [ ! -d $SRC_DIR/tftp ]; then 
  echo "The source directory $SRC_DIR/tftp is missing.  You must first run the get_tftp_source.sh script"
  exit
fi


apt-get -y update

PKG[0]=inetutils-inetd
PKG[1]=tftpd-hpa

for((p=0;p<${#PKG[@]};p++))
do 
  if [ $(dpkg -s ${PKG[$p]} 2> /dev/null|grep -s -c 'Status: install ok installed') = 0 ]; then 
    apt-get --assume-yes install ${PKG[$p]}
  else
    echo "${PKG[$p]} already installed"
  fi 
done
#ISO Checking
# Dir and file check #
for((u=0;u<${#UBUNTU[@]};u++))
do
  ISO_SRC=`echo ${UBUNTU[$u]}`
  ISO=`echo $ISO_SRC| awk -F '/' '{print $NF}'`

  if [ ! -d $ISO_DIR ];then
    mkdir -pv $ISO_DIR
  fi

  if [ ! -f $ISO_DIR/$ISO ] || [ $(grep -s -c $(md5sum $ISO_DIR/$ISO|awk -F " " '{print $1}') $src_dir/$MD5FILE) = 0 ]; then
    echo "$ISO is missing.  Downloading $ISO"
    wget --output-document=$ISO_DIR/$ISO $ISO_SRC
    chmod 755 -vR $ISO_DIR/$ISO
  fi

TFTP_DIR=/var/lib/tftpboot/ubuntu
HTML_DIR=/var/www/ubuntu

while [ $(mount | grep /media/cdrom |wc -l) -gt 0 ]; do 
  umount /media/cdrom
done
	
mount $ISO_DIR/$ISO /media/cdrom
    
OS_VERSION=`grep DISKNAME /media/cdrom/README.diskdefines | awk -F " " '{print $4}'|awk -F "." '{print $1"."$2}'`
	
  if [ ! -d $TFTP_DIR/$OS_VERSION ]; then 
    echo "$TFTP_DIR/$OS_VERSION directory is missing.  Creating directory"
    mkdir -pv $TFTP_DIR/$OS_VERSION
  fi

  if [ ! -d $HTML_DIR/$OS_VERSION ]; then        
    mkdir -pv $HTML_DIR/$OS_VERSION
  fi

  cp -var /media/cdrom/* $HTML_DIR/$OS_VERSION
  cp -var /media/cdrom/install/netboot/* /var/lib/tftpboot/
  umount /media/cdrom
done
    
for((t=0;t<${#TFTP[@]};t++))
do 
SRC=$SRC_DIR/tftp
SOURCE=${TFTP[$t]}
DESTINATION=`echo $SOURCE | sed -e "s!$PXESRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
 
  if [ ! -f $DIR/$FILE ] || [ $(grep -s -c $(shasum $DIR/$FILE|awk -F " " '{print $1}') $src_dir/$SUMFILE) = 0 ]; then
    echo "$FILE is missing or is out of date.  Updating $FILE"
      if [ -f $DIR/$FILE ]; then 
        echo "Creating $FILE backups"
        cp -v $DIR/$FILE $DIR/$FILE.installbk.$(date +%d%b%Y_%T)
      fi

    cp -v $SRC/$FILE $DIR/$FILE
    chown -v ${TFTPO[$t]} $DIR/$FILE
    chmod -v ${TFTPP[$t]} $DIR/$FILE

  else

    echo "$FILE is up to date"

  fi 
done
update-inetd --enable BOOT
