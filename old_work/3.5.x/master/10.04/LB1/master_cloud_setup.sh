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
#/ecloud_3.5.x\								#
#/master\								#
#/10.04\								#
#/LB1/\									#
#/master_cloud_setup.sh)"						#
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

export SRC_LOG=/tmp/master_setup.log
export XFER="wget --timestamping --verbose --debug --append-output=$SRC_LOG --inet4-only --user=$username --password=$password"
export src_dir=/tmp
export MASTER_OS_SRC=https://pl3.projectlocker.com/enlightedinc/Gems/svn/gems/trunk/ecloud/artifacts/ecloud_3.5.x/master/10.04/LB1
export SECURITY_SRC=https://pl3.projectlocker.com/enlightedinc/Gems/svn/gems/trunk/ecloud/artifacts/tools/security
export SETUP_CONFIG=master_cloud_setup.config
export SUMFILE=$src_dir/LB1_3.5.x_10.04.sum
export LOG_DIR=/var/log/enlighted/SETUP

# Cleanup old logs

rm -vf $src_dir/*.log

$XFER --output-document=$SUMFILE $MASTER_OS_SRC/LB1_3.5.x_10.04.sum

if [ ! -f $SETUP_CONFIG ] || [ $(grep -s -c $(shasum $SETUP_CONFIG|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
  echo "$(basename $SETUP_CONFIG) is missing or out of date.  Downloading updated $(basename $SETUP_CONFIG)"
  $XFER --output-document=$SETUP_CONFIG $MASTER_OS_SRC/master_cloud_setup.config
else
  echo "$(basename $SETUP_CONFIG) is up to date."
fi

source $SETUP_CONFIG

# SSL folder check 

if [ ! -d $ENLCADIR ]; then 
  mkdir -pv $ENLCADIR
fi
  chmod -vR $ENLCADIRP /etc/enlighted

# Script check

echo "checking for install scripts"

for((i=0;i<${#INSTALL_SCRIPT[@]};i++))
do 
  if [ ! -x $src_dir/${INSTALL_SCRIPT[$i]} ] || [ $(grep -s -c $(shasum $src_dir/${INSTALL_SCRIPT[$i]}|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then 
    echo "${INSTALL_SCRIPT[$i]} file is missing or is not executable.  Gathering latest ${INSTALL_SCRIPT[$i]} from repo please wait."
    $XFER --output-document=$src_dir/${INSTALL_SCRIPT[$i]} $MASTER_OS_SRC/${INSTALL_SCRIPT[$i]}
    chmod -v 0755 $src_dir/${INSTALL_SCRIPT[$i]}
  else
    echo "${INSTALL_SCRIPT[$i]} file is up to date"
  fi 
done

for ((i=0;i<${#INSTALL_SCRIPT[@]};i++))
do
  echo "Executing ${INSTALL_SCRIPT[$i]} script"
  if [ ! -d $LOG_DIR ]; then
    mkdir -pv $LOG_DIR
  fi 
  bash -x $src_dir/${INSTALL_SCRIPT[$i]} | tee $LOG_DIR/${INSTALL_SCRIPT[$i]}.log
done
chown -vR $ENLCADIRO /etc/enlighted

apt-get --assume-yes autoclean
apt-get --assume-yes autoremove

echo "Master LB1 installation complete.  Don't forget to restore the 000-default-em from backup"
