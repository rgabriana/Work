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
DESTINATION=`echo $SOURCE | sed -e "s!$MASTER_OS_SRC!!"`
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
PKG[8]=postgresql-client-8.4
PKG[9]=postgresql-client-common
PKG[10]=vim

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
DESTINATION=`echo $SOURCE | sed -e "s!$MASTER_OS_SRC!!"`
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

a2ensite default

echo "disable all previously enabled modules"
  rm -vf $apache2_dir/mods-enabled/*

echo "enable selected modules"

MOD[0]=alias
MOD[1]=auth_basic
MOD[2]=authn_file
MOD[3]=authz_default
MOD[4]=authz_host
MOD[5]=authz_user
MOD[6]=deflate
MOD[7]=env
MOD[8]=mime
MOD[9]=negotiation
MOD[10]=proxy
MOD[11]=proxy_http
MOD[12]=proxy_balancer
MOD[13]=reqtimeout
MOD[14]=rewrite
MOD[15]=setenvif
MOD[16]=ssl
MOD[17]=status
MOD[18]=version

for((m=0;m<${#MOD[@]};m++))
do
if [ ! -L ${MOD[$m]} ]; then
  echo "enabling ${MOD[$m]}"
a2enmod ${MOD[$m]}
  else "${MOD[$m]} already enabled"
fi
done

# turn on ExtendedStatus
TEST=`cat ${apache2_dir}/apache2.conf | grep 'ExtendedStatus On' | wc -l`

if [ "$TEST" -ne "1" ]; then
  echo "ExtendedStatus On" >> ${apache2_dir}/apache2.conf
fi

# Restart Apache2

service apache2 stop
