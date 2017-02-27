#!/bin/bash
####################################
# install script for the PXE server
# usage: bash -xc "$(wget -O - -q --user=<repo_username> --password=<repo_password> https://pl3.projectlocker.com/enlightedinc/Gems/svn/gems/trunk/ecloud/artifacts/pxeserver/pxeinstaller.sh)"
####################################
set -e 

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

#  Root Check #

if [ `id -u` != "0" ]; then 
  echo "you need root priveleges to execute this script"
  exit 
fi 

#  Environment Variables  #
while getopts :u:p: opt; do 
  case $opt in 
        u) username=$OPTARG
        ;;
        p) password=$OPTARG
        ;;
        *) :
        ;;
  esac
done

if [ -z "$*" ]; then
        read -p "please enter your username for the source server " username
        read -p "please enter your password for the source server " password
fi

export src_dir=/tmp
export LOG_DIR=/var/log/$(hostname)_setup
export WGET_LOG=pxe_server_wget.log
export PXESRC="https://pl3.projectlocker.com/enlightedinc/Gems/svn/gems/trunk/ecloud/artifacts/pxeserver"
export SECURITY_SRC="https://pl3.projectlocker.com/enlightedinc/Gems/svn/gems/trunk/ecloud/artifacts/tools/security"
export XFER="wget --timestamping --verbose --debug --append-output=$LOG_DIR/$WGET_LOG --inet4-only --user=$username --password=$password"
export PXECFG=pxeinstaller.config
export SUMFILE=PXE.sum 
export MD5FILE=UBUNTU.sum

## Logging ##
if [ ! -d $LOG_DIR ]; then 
  mkdir -pv $LOG_DIR
fi

chmod 755 $LOG_DIR

$XFER --output-document=$src_dir/$SUMFILE $PXESRC/PXE.sum 
$XFER --output-document=$src_dir/$MD5FILE $PXESRC/UBUNTU.sum 

if [ ! -f $src_dir/$PXECFG ] || [ $(grep -s -c $(shasum $src_dir/$PXECFG|awk -F " " '{print $1}') $src_dir/$SUMFILE) = 0 ]; then
  echo "$PXECFG file is missing or out of date.  Downloading latest $PXECFG"
  $XFER --output-document=/$src_dir/$PXECFG $PXESRC/pxeinstaller.config
else
  echo "$PXECFG is up to date"
fi 

source $src_dir/$PXECFG

## Download install scripts ##

for((x=0;x<${#INSTALL_SCRIPT[@]};x++))
do 
FILE=${INSTALL_SCRIPT[$x]}
  if [ ! -x $src_dir/$FILE ] || [ $(grep -s -c $(shasum $src_dir/$FILE|awk -F " " '{print $1}') $src_dir/$SUMFILE) = 0 ]; then 
    echo "$FILE is missing, not executable or out of date.  Updating $FILE"
    $XFER --output-document=$src_dir/$FILE $PXESRC/$FILE
    chmod -v 755 $src_dir/$FILE
  else
    echo "$FILE is up to date"
  fi
done

## Execute install scripts

for((x=0;x<${#INSTALL_SCRIPT[@]};x++))
  do
  SRC_FILE=$SRC_DIR/${INSTALL_SCRIPT[$x]}
  FILE=$(basename $SRC_FILE)
    echo "executing $FILE"

    if [ ! -f $LOG_DIR/$FILE.$x.log ]; then 
      echo " " > $LOG_DIR/$FILE.$x.log
    fi 

    bash -x $src_dir/$FILE > $LOG_DIR/$FILE.$x.log 2>&1

  done

service tftpd-hpa restart
service isc-dhcp-server restart
service apache2 restart

service tftpd-hpa status
service isc-dhcp-server status
service apache2 status

echo "PXE SERVER SETUP COMPLETE."
