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

## Stop tomcat service ## 

sudo service tomcat6 stop 

## Check for signatures ##
NEW_SIG=`md5sum $src_dir/tomcat6/ecloud.war | awk -F " " '{print $1}'`
OLD_SIG=`md5sum $TC_home/webapps/ecloud.war | awk -F " " '{print $1}'`

if [ "$OLD_SIG" != "$NEW_SIG" ];then 
  rm -vfR $TC_home/webapps/ecloud
  mv -v $TC_home/webapps/ecloud.war $src_dir/tomcat6/ecloud.war.patchbk.$(date +%d%b%Y_%T)
  rm -vfR $TC_home/work/Catalina/localhost/ecloud
  cp -v $src_dir/tomcat6/ecloud.war $TC_home/webapps/
else 
  echo "war file is up to date"
fi

sudo service tomcat6 restart
