#!/bin/bash -x
#########################################
# Title: Apache2 install script 	#
# usage: apache2install.sh		#
#########################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

# Root check
if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this scripts"
  exit
fi

source $src_dir/$SETUP_CONFIG

# Source check 
if [ ! -d $src_dir/apache2 ]; then
  echo "The source directory $src_dir/apache2 is missing.  You must first run the get_apache2_source script"
  exit
fi

# APT check (sources list)
if [ ! -d $src_dir/apt ]; then 
  mkdir -pv $src_dir/apt
fi 

for((a=0;a<${#APT[@]};a++))
do 
SOURCE=${APT[$a]}
DESTINATION=`echo $SOURCE | sed -e "s!$REPLICA_OS_SRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
  if [ ! -f $DESTINATION ] || [ $(grep -s -c $(shasum $DESTINATION|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then

    echo "$FILE is missing or out of date.  Downloading updated $FILE"
    $XFER --output-document=$src_dir/apt/$FILE $SOURCE

    if [ -f $DESTINATION ]; then

      echo "Backing up existing file"
      cp -v $DESTINATION $DESTINATION.installbk.$(date +%d%b%Y_%T)

    fi

    cp -v $src_dir/apt/$FILE $DESTINATION
    chown -v ${APTO[$a]} $DESTINATION
    chmod -v ${APTP[$a]} $DESTINATION

  else

    echo "$FILE is up to date"

  fi

done

# Install needed packages
apt-get --assume-yes update
PKG[0]=apache2
PKG[1]=apache2-mpm-worker
PKG[2]=python-software-properties
PKG[3]=php5
PKG[4]=libssh2-php
PKG[5]=php5-pgsql
PKG[6]=python-psycopg2
PKG[7]=python-dateutil

for((p=0;p<${#PKG[@]};p++))
do
  if [ $(dpkg -s ${PKG[$p]} 2> /dev/null|grep -c -s 'Status: install ok installed') = 0 ]; then
    apt-get --assume-yes install ${PKG[$p]}
  else
    echo "${PKG[$p]} already installed"
  fi
done

# Configs

apache2_dir=/etc/apache2
enlighted_dir=/home/enlighted

for ((i=0;i<${#A2[@]};i++))
do 
SOURCE=${A2[$i]}
DESTINATION=`echo $SOURCE | sed -e "s!$REPLICA_OS_SRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`

  if [ ! -f $DIR/$FILE ] || [ $(grep -s -c $(shasum $DIR/$FILE|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then

    echo "$FILE is missing or is out of date."

    if [ -f $DESTINATION ]; then
    
      echo "creating backup $FILE"
      cp -pv $DIR/$FILE $DIR/$FILE.old.$(date +%d%b%Y_%T)

    fi

    cp -v $src_dir/apache2/$FILE $DIR/$FILE
    chown -v ${AO[$i]} $DIR/$FILE
    chmod -v ${AP[$i]} $DIR/$FILE

  else

    echo "$FILE is up to date"

  fi 

done

#a2ensite default

echo "enable selected modules"

a2enmod ssl proxy proxy_http proxy_ajp rewrite

# Restart Apache2

service apache2 stop
