#!/bin/bash
#########################################
# replica_patch.sh
#########################################
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

export PATCH_LOG=/tmp/replica_patch.log
export XFER="wget --debug --append-output=$PATCH_LOG --inet4-only --user=$username --password=$password"
export src_dir=/tmp
export REPLICA_OS_SRC=https://pl3.projectlocker.com/enlightedinc/Gems/svn/gems/trunk/ecloud/artifacts/ecloud_3.6.x/REPLICA/14.04
export SUMFILE=REPLICA_3.6.x_14.04.sum 
export SETUP_CONFIG=replica_patch.config

# Cleanup old logs

rm -vf $src_dir/*.log

$XFER --output-document=$SUMFILE $REPLICA_OS_SRC/$SUMFILE 

if [ ! -f $src_dir/$SETUP_CONFIG ] || [ $(grep -s -c $(shasum $src_dir/$SETUP_CONFIG|awk -F " " '{print $1}') $src_dir/$SUMFILE) = 0 ]; then
  echo "$SETUP_CONFIG is missing or out of date.  Downloading updated $SETUP_CONFIG"
  $XFER --output-document=/$src_dir/$SETUP_CONFIG $REPLICA_OS_SRC/$SETUP_CONFIG
else
  echo "$SETUP_CONFIG is up to date."
fi

source $src_dir/$SETUP_CONFIG

if [ ! -f $src_dir/$PATCH_FILE ] || [ $(grep -s -c $(shasum $src_dir/$PATCH_FILE|awk -F " " '{print $1}') $src_dir/$SUMFILE) = 0 ]; then
  echo "$PATCH_FILE file is missing or is not executable.  Gathering latest $PATCH_FILE from repo please wait."
  $XFER --output-document=$src_dir/$PATCH_FILE $REPLICA_OS_SRC/$PATCH_FILE
else
  echo "$PATCH_FILE file is up to date"
fi

  echo "Patching $(hostname)"
  dpkg -i --force-overwrite $src_dir/$PATCH_FILE
echo "Patch Complete"
