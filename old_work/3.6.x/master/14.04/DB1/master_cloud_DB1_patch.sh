#!/bin/bash -x
#########################################################################
# Title: Set-up script for installing enlighted cloud master server	#
# usage:								#
# bash -xc\								#
# "$(wget\								#
# -O - -q\								#
# --user=<repo_username>\						#
# --password=<repo_password>\						#
# https://pl3.projectlocker.com\					#
#/enlightedinc\								#
#/Gems\									#
#/svn\									#
#/gems\									#
#/trunk\								#
#/ecloud\								#
#/artifacts\								#
#/ecloud_3.6.x\								#
#/master\								#
#/14.04\								#
#/DB1/\									#
#/master_cloud_DB1_setup.sh)"						#
#########################################################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

# Root check

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this scripts"
  exit
fi

# Environment variables

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
export LOG_DIR=/var/log/enlighted/patch_3.6.x
export SRC_LOG=master_setup.log
export XFER="wget -O - --debug --append-output=$LOG_DIR/$SRC_LOG --inet4-only --user=$username --password=$password"
export MASTER_OS_SRC=https://pl3.projectlocker.com/enlightedinc/Gems/svn/gems/trunk/ecloud/artifacts/ecloud_3.6.x/master/14.04/DB1
export SECURITY_SRC=https://pl3.projectlocker.com/enlightedinc/Gems/svn/gems/trunk/ecloud/artifacts/tools/security
export SUMFILE=DB1_3.6.x_14.04.sum 
export SETUP_CONFIG=master_cloud_DB1_patch.config

## LOGGING ##

if [ ! -d $LOG_DIR ]; then
        echo "$LOG_DIR is missing creating $LOG_DIR directory" 
        mkdir -pv $LOG_DIR
fi

rm -vf $LOG_DIR/$SRC_LOG

## Download checksum ##

$XFER --output-document=$src_dir/$SUMFILE $MASTER_OS_SRC/$SUMFILE

## Config check ##

if [ ! -f $src_dir/$SETUP_CONFIG ] || [ $(grep -s -c $(shasum $src_dir/$SETUP_CONFIG | awk -F " " '{print $1}') $src_dir/$SUMFILE) = 0 ]; then

  echo "$SETUP_CONFIG is missing or out of date.  Updating file"
  $XFER --output-document=$src_dir/$SETUP_CONFIG $MASTER_OS_SRC/$SETUP_CONFIG

else 

  echo "$SETUP_CONFIG is up to date"

fi

source $src_dir/$SETUP_CONFIG

# Script check

echo "checking for installed scripts"

## INSTALL SCRIPT ##

for((i=0;i<${#INSTALL_SCRIPT[@]};i++))
do

  if [ ! -x $src_dir/${INSTALL_SCRIPT[$i]} ] || [ $(grep -s -c $(shasum $src_dir/${INSTALL_SCRIPT[$i]} | awk -F " " '{print $1}') $src_dir/$SUMFILE) = 0 ]; then
    
    echo "${INSTALL_SCRIPT[$i]} file is missing or is not executable.  Gathering latest ${INSTALL_SCRIPT[$i]} from repo please wait."
    $XFER --output-document=$src_dir/${INSTALL_SCRIPT[$i]} $MASTER_OS_SRC/${INSTALL_SCRIPT[$i]}
    chmod -v 0755 $src_dir/${INSTALL_SCRIPT[$i]}

  else

    echo "${INSTALL_SCRIPT[$i]} file is up to date"

  fi

done

for((i=0;i<${#INSTALL_SCRIPT[@]};i++))

do

  if [ ! -f $LOG_DIR/${INSTALL_SCRIPT[$i]}.log ]; then 
    echo "" > $LOG_DIR/${INSTALL_SCRIPT[$i]}.log
  fi

  chmod -v 644 $LOG_DIR/${INSTALL_SCRIPT[$i]}.log
  echo "Executing ${INSTALL_SCRIPT[$i]} script"
  bash -x $src_dir/${INSTALL_SCRIPT[$i]} > $LOG_DIR/${INSTALL_SCRIPT[$i]}.log 2>&1

done

apt-get --assume-yes autoclean
apt-get --assume-yes autoremove


echo "Master DB1 patch complete.  Log files can be found at $LOG_DIR"
