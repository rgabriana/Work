#!/bin/bash
# Author: Siddharth
# Version 0.1
source /etc/environment

if [ ! -z "$1" ]
then
	echo "***********************setting ENL_APP_HOME from passed argument to shell************"
	export ENL_APP_HOME="$1"
fi
if [ -z "$ENL_APP_HOME"  ]
then
	echo "***********************setting ENL_APP_HOME hardcoded to /var/lib/tomcat6 as it is not still set************"
        export ENL_APP_HOME="/var/lib/tomcat6"
fi


if [ ! -z "$2" ]
then
	echo "***********************setting ENLIGHTED_HOME from passed argument to shell************"
	export ENLIGHTED_HOME="$2"
fi
if [ -z "$ENLIGHTED_HOME"  ]
then
	echo "***********************setting ENLIGHTED_HOME hardcoded to /home/enlighted as it is not still set************"
        export ENLIGHTED_HOME="/home/enlighted"
fi

if [ ! -z "$3" ]
then
	echo "***********************setting EM_MGMT_BASE from passed argument to shell************"
	export EM_MGMT_BASE="$3"
fi
if [ -z "$EM_MGMT_BASE"  ]
then
	echo "***********************setting EM_MGMT_BASE hardcoded to /var/wwww as it is not still set************"
        export EM_MGMT_BASE="/var/www"
fi


if [ ! -z "$4" ]
then
	echo "***********************setting OPT_ENLIGHTED from passed argument to shell************"
	export OPT_ENLIGHTED="$4"
fi
if [ -z "$OPT_ENLIGHTED"  ]
then
	echo "***********************setting OPT_ENLIGHTED hardcoded to /opt/enLighted as it is not still set************"
        export OPT_ENLIGHTED="/opt/enLighted"
fi

if [ -d debian_adr ]
then
        echo "Recreating debian_adr directory"
        rm -rf debian_adr
        mkdir debian_adr
else
        echo "Creating debian_adr directory"
        mkdir debian_adr
fi
chmod -R 755 debs/adr/*
echo  "Copying files to debian_adr directory!!"
rsync -a --exclude='.svn' debs/adr/* debian_adr

echo  "Copying adr.jar to debian_adr$OPT_ENLIGHTED/adr directory!!"
cp adr/target/adr.jar debian_adr$OPT_ENLIGHTED/adr
echo  "Copying ADRInstall.sql to debian_adr$OPT_ENLIGHTED/adr directory!!"
cp adr/artifacts/sql/ADRInstall.sql debian_adr$OPT_ENLIGHTED/adr
echo "Creating debian package!!!!!"
dpkg-deb -b debian_adr debian_adr/adr.deb

#COMM_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)

#mv debian_adr/adr.deb debian_adr/${COMM_REV}_adr.deb

