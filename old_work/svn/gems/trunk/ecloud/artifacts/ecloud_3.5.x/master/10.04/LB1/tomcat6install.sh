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

source $src_dir/$SETUP_CONFIG
# Target Folders

TC_home=/var/lib/tomcat6


# Install needed packages
apt-get --assume-yes update
PKG[0]=tomcat6
PKG[1]=tomcat6-admin
PKG[2]=tcl8.5
PKG[3]=openjdk-6-jdk
PKG[4]=openjdk-6-jre
PKG[5]=openjdk-6-jre-lib
PKG[6]=openjdk-6-jre-headless
PKG[7]=sshpass
PKG[8]=postgresql-client
PKG[9]=postgresql-client-common
PKG[10]=libclass-singleton-perl
PKG[11]=liblist-moreutils-perl
PKG[12]=libparams-validate-perl
PKG[13]=libdatetime-locale-perl
PKG[14]=libdatetime-timezone-perl
PKG[15]=libdatetime-perl
PKG[16]=libdbd-pg-perl

for((t=0;t<${#PKG[@]};t++))

do
  if [ $(dpkg -s ${PKG[$t]} 2> /dev/null|grep -c -s 'Status: install ok installed') = 0 ]; then
    apt-get --assume-yes install ${PKG[$t]}
  else
    echo "${PKG[$t]} already installed"
  fi
done

# Configs 

apache2_dir=/etc/apache2

echo "Enlighted EM web config"

if [ $(dpkg -s  apache2 2> /dev/null|grep -c -s 'Status: install ok installed') = 0 ]; then
  echo "Please install Apache2 before continuing."
  exit 1
fi

if [ ! -f $apache2_dir/sites-enabled/000-default-em ]; then
  echo "000-default-em file is missing or doesn't exists.  Creating file"
  touch $apache2_dir/sites-enabled/000-default-em
fi

chmod -v 664 $apache2_dir/sites-enabled/000-default-em
chown -v tomcat6:tomcat6 $apache2_dir/sites-enabled/000-default-em

enlighted_TC=$TC_home/Enlighted
mkdir -pv $enlighted_TC

echo 'ecloudIp='`ifconfig eth0 | grep "inet addr" | awk -F : '{print $2}' | sed 's/Bcast//g'` | tee $enlighted_TC/config.properties

for ((t=0;t<${#TC6[@]};t++))
do 
SOURCE=${TC6[$t]}
DESTINATION=`echo $SOURCE | sed -e "s!$MASTER_OS_SRC!!"`
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
