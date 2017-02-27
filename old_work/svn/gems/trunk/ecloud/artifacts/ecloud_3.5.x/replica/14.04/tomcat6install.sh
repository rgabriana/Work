#!/bin/bash -x
#################################
# Title: Tomcat6 install script	#
# Usage:  tomcat6install.sh	#
#################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

# Root check
if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this scripts"
  exit
fi

# Source check

if [ ! -d $src_dir/tomcat6 ]; then 
  echo "The source directory $src_dir/tomcat6 is missing.  You must first run the get_tomcat6_source script"
  exit
fi

source $src_dir$SETUP_CONFIG
# Target Folders

TC_home=/var/lib/tomcat6

mkdir -pv $TC_home/Enlighted/logs
mkdir -pv $TC_home/Enlighted/ems_log4j

# Install needed packages
apt-get --assume-yes update
PKG[0]=tcl8.5
PKG[1]=openjdk-6-jdk
PKG[2]=openjdk-6-jre
PKG[3]=openjdk-6-jre-lib
PKG[4]=openjdk-6-jre-headless
PKG[5]=sshpass
PKG[6]=postgresql-client
PKG[7]=postgresql-client-common
PKG[8]=libclass-singleton-perl
PKG[9]=liblist-moreutils-perl
PKG[10]=libparams-validate-perl
PKG[11]=libdatetime-locale-perl
PKG[12]=libdatetime-timezone-perl
PKG[13]=libdatetime-perl
PKG[14]=libdbd-pg-perl
PKG[15]=tomcat6
PKG[16]=tomcat6-admin

for((t=0;t<${#PKG[@]};t++))

do
  if [ $(dpkg -s ${PKG[$t]} 2> /dev/null|grep -c -s 'Status: install ok installed') = 0 ]; then
    apt-get --assume-yes install ${PKG[$t]}
  else
    echo "${PKG[$t]} already installed"
  fi
done

for ((t=0;t<${#TC6[@]};t++))
do 
SOURCE=${TC6[$t]}
DESTINATION=`echo $SOURCE | sed -e "s!$REPLICA_OS_SRC!!"`
DIR=`dirname $DESTINATION`
FILE=`basename $DESTINATION`

  if [ ! -f $DIR/$FILE ] || [ $(grep -s -c $(shasum $DIR/$FILE|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then 

  echo "$FILE is missing or out of date.  Updating $FILE"

    if [ -f $DESTINATION ]; then

      echo "Creating $FILE backup"
      cp -v $DESTINATION $DIR/$FILE.installbk.$(date +%d%b%Y_%T)

    fi

    cp -v $src_dir/tomcat6/$FILE $DESTINATION
    chown -v ${TCO[$t]} $DESTINATION
    chmod -v ${TCP[$t]} $DESTINATION

  else

  echo "$FILE is up to date"

  fi

done

if [ ! -f /etc/sudoers.d/enlighted ]; then
  tc6sudo=$(mktemp)
  echo "enlighted ALL=NOPASSWD: /etc/init.d/apache2" > $tc6sudo 
  chmod -v 0440 $tc6sudo
  if [ $(visudo -c -f $tc6sudo|grep -c -s 'parsed ok') = "1" ];then 
    cp -pv $tc6sudo /etc/sudoers.d/enlighted
  else
    echo "please verify the sudoers file for tomcat6 using the command "visudo -f /etc/sudoers.d/tomcat6""
  fi
else
  echo "sudoers file exists"
fi 

# Restart Tomcat services 

service tomcat6 restart
