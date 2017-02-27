#!/bin/bash -x
#################################################
# psqlinstall.sh
#################################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

## ROOT CHECK ##

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this script"
  exit
fi

## SOURCE CHECK ##

source /tmp/master_cloud_DB1_setup.config

if [ -f /etc/default/locale ]; then 
  cp -v /etc/default/locale /etc/default/locale.installbk.$(date +%d%b%Y_%T)
  $XFER --output-document=/etc/default/locale $MASTER_OS_SRC/etc/default/locale
  locale-gen en_US
  sudo dpkg-reconfigure locales
fi 

#LOCALE=`locale -a | grep -s -x -c en_US`

## Locale check ##

#if [ "$LOCALE" = "0" ]; then
#  echo "System locale is set to $(locale -a|tr -s \\n ' ') please make sure that your locale settings include the following en_US, en_US.iso88591"
#  exit
#fi

## Package List ##

PKG[0]=postgresql
PKG[1]=postgresql-plperl-9.3
PKG[2]=postgresql-pltcl-9.3
PKG[3]=tcl8.5
PKG[4]=python-software-properties
PKG[5]=php5
PKG[6]=libssh2-php
PKG[7]=php5-pgsql
PKG[8]=vim
PKG[9]=python-psycopg2
PKG[10]=python-dateutil
PKG[11]=openjdk-6-jdk
PKG[12]=nagios-plugins-common
PKG[13]=nagios-plugins-basic

if [ ! -d $src_dir/psql ]; then
  echo "$(date): $src_dir/psql does not exists.  Please run the get_psql_source.sh script first."
  exit 1
fi

## install package ##

apt-get -y update 
for((a=0;a<${#PKG[@]};a++))
do 
  if [ $(dpkg -s ${PKG[$a]} 2> /dev/null|grep -c -s 'Status: install ok installed') = 0 ]; then
  apt-get -y install ${PKG[$a]}
  else
    echo "${PKG[$a]} already installed"
  fi 
done 

## install files ##

for((i=0;i<${#PSQL[@]};i++))
do
SOURCE=${PSQL[$i]}
DESTINATION=`echo ${SOURCE} | sed -e "s!${MASTER_OS_SRC}!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`
  if [ ! -f $DESTINATION ] || [ $(grep -s -c $(shasum $DESTINATION|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
    echo "$FILE is missing or not up to date.  Updating $FILE"

    if [ -f $DESTINATION ]; then 

      echo "Creating $FILE backup"
      /bin/cp -v $DESTINATION $DESTINATION.installbk.$(date +%d%b%Y_%T)

    fi

    echo "Updating $FILE"
    /bin/cp -v ${src_dir}/psql/${FILE} $DESTINATION
    /bin/chown -v ${PSQLO[$i]} $DESTINATION
    /bin/chmod -v ${PSQLP[$i]} $DESTINATION
  else
  
    echo "$FILE is up to date"

  fi

done

service postgresql restart

echo "Creating database backup"
pg_dumpall --username=postgres --clean --file=$src_dir/psql/DBBKUP_$(date +%d%b%Y_%T)
dropdb --username=postgres --if-exists emscloud
createdb --username=postgres --encoding=LATIN1 emscloud

SQL='psql --username=postgres --dbname=emscloud'
$SQL --file=/var/lib/postgresql/9.3/main/ecloud_install.sql
$SQL --file=/var/lib/postgresql/9.3/main/ecloud_upgrade.sql
