#!/bin/bash
source /etc/environment
# Author: Kushal
# Version 2.2

if [ ! -d debian_ems ]
then
        echo "Creating debian_ems directory!!!!"
        mkdir debian_ems
else
        echo "deleting the existing debian_ems directory!!!!!!"
        rm -rf debian_ems
        echo "Re-creating debian_ems directory!!!!"
        mkdir debian_ems
fi
#if [ ! -z "$1" ]
#then
#	echo "***********************setting ENL_ENV_SHELL from passed argument to shell************"
#	export ENL_ENV_SHELL="$1"
#fi
#if [ -z "$ENL_APP_HOME"  ]
#then
#	echo "***********************setting ENL_ENV_SHELL hardcoded to install/debian-install/home/enlighted/scripts/setAllEMEnvironment.sh as it is not still set************"
#        export ENL_ENV_SHELL="install/debian-install/home/enlighted/scripts/setAllEMEnvironment.sh"
#fi

#echo "***********run the env shell script first $ENL_ENV_SHELL******"
#sh $ENL_ENV_SHELL
#source /etc/environment

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


chmod -R 755 install/debian-install/*
echo "Copying the debian files to debian_ems directory!!!!"
rsync -a --exclude='.svn' install/debian-install/* debian_ems/
cp ems/target/ems.war debian_ems$ENL_APP_HOME/webapps/ems.war
cp ems/artifacts/sql/migration/From_2.0_To_3.0/upgradeSQL.sql debian_ems$ENLIGHTED_HOME/
cp ems/artifacts/sql/migration/From_2.0_To_3.0/sppa.sql debian_ems$ENLIGHTED_HOME/
#Adding bacnet libraries
cp ../BACNET2/stack-0.8.0/demo/gateway/bacnetLighting debian_ems$ENL_APP_HOME/Enlighted/bacnet/bacnetLighting
cp ../BACNET2/libs/i386/* debian_ems/usr/lib/
dpkg-deb -b debian_ems debian_ems/enLighted.deb
#################################################################
if [ ! -d debian_mgmt ]
then
        echo "Creating debian directory!!!!"
        mkdir debian_mgmt
else
        echo "deleting the existing debian directory!!!!!!"
        rm -rf debian_mgmt
        echo "Re-creating debian directory!!!!"
        mkdir debian_mgmt
fi
chmod -R 755 debs/em_mgmt_new/*
echo "Copying the debian files to debian_mgmt directory!!!!"
rsync -a --exclude='.svn' debs/em_mgmt_new/* debian_mgmt/
rsync -a --exclude='.svn' em_mgmt/* debian_mgmt$EM_MGMT_BASE/em_mgmt/
cd debian_mgmt$EM_MGMT_BASE/em_mgmt/
django-admin.py compilemessages
cd ../../../../
svn info | grep 'Last Changed Rev' | sed 's/^.*Rev/Revision/' > debian_mgmt$EM_MGMT_BASE/em_mgmt/management/templatetags/svninfo.txt
dpkg-deb -b debian_mgmt debian_mgmt/em_mgmt.deb 
#################################################################
if [ ! -d debian_sys ]
then
        echo "Creating debian directory!!!!"
        mkdir debian_sys
else
        echo "deleting the existing debian directory!!!!!!"
        rm -rf debian_sys
        echo "Re-creating debian directory!!!!"
        mkdir debian_sys
fi
chmod -R 755 debs/system/em_system/*
echo "Copying the debian files to debian_sys directory!!!!"
rsync -a --exclude='.svn' debs/system/em_system/* debian_sys/
dpkg-deb -b debian_sys debian_sys/em_system.deb
#################################################################
if [ -d debian_cloud_communicator ]
then
        echo "Recreating debian_cloud_communicator directory"
        rm -rf debian_cloud_communicator
        mkdir debian_cloud_communicator
else
        echo "Creating debian_cloud_communicator directory"
        mkdir debian_cloud_communicator
fi
chmod -R 755 debs/cloud_communicator/*
echo  "Copying files to debian_cloud_communicator directory!!"
rsync -a --exclude='.svn' debs/cloud_communicator/* debian_cloud_communicator

echo  "Copying em_cloud_communicator.jar to debian_cloud_communicator$OPT_ENLIGHTED/communicator directory!!"
cp em_cloud_communicator/target/em_cloud_communicator.jar debian_cloud_communicator$OPT_ENLIGHTED/communicator
echo "Creating debian package!!!!!"
dpkg-deb -b debian_cloud_communicator debian_cloud_communicator/em_cloud_communicator.deb
#################################################################
GEMS_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)
mv debian_ems/enLighted.deb debian_ems/${GEMS_REV}_enLighted.deb
mv debian_mgmt/em_mgmt.deb debian_mgmt/${GEMS_REV}_em_mgmt.deb
mv debian_sys/em_system.deb debian_sys/${GEMS_REV}_em_system.deb
mv debian_cloud_communicator/em_cloud_communicator.deb debian_cloud_communicator/${GEMS_REV}_em_cloud_communicator.deb
#################################################################
if [ -d debian_em_all ]
then
        echo "Recreating debian_em_all directory"
        rm -rf debian_em_all
        mkdir debian_em_all
else
        echo "Creating debian_em_all directory"
        mkdir debian_em_all
fi
echo  "Copying files to debian_em_all directory!!"
rsync -a --exclude='.svn' debs/em_all/* debian_em_all/

echo "Copying setAllEMEnvironment.sh file to the debian"
cp install/debian-install$ENLIGHTED_HOME/scripts/setAllEMEnvironment.sh debian_em_all$ENLIGHTED_HOME/
echo "Copying InstallSQL.sql file to the debian"
cp install/debian-install$ENLIGHTED_HOME/InstallSQL.sql debian_em_all$ENLIGHTED_HOME/


svn info | grep 'Last Changed Rev' | sed 's/^.*Rev/CurrentRevision/' >> debian_em_all/DEBIAN/control
echo "ValidationKey: enLighted" >> debian_em_all/DEBIAN/control
cp debian_cloud_communicator/${GEMS_REV}_em_cloud_communicator.deb debian_em_all$ENLIGHTED_HOME/debs/
cp debian_sys/${GEMS_REV}_em_system.deb debian_em_all$ENLIGHTED_HOME/debs/
cp debian_mgmt/${GEMS_REV}_em_mgmt.deb debian_em_all$ENLIGHTED_HOME/debs/
cp debian_ems/${GEMS_REV}_enLighted.deb debian_em_all$ENLIGHTED_HOME/debs/
cp -R mobile/Deployables/release/emsMobile* debian_em_all$ENL_APP_HOME/webapps/ROOT/
echo "Creating debian package!!!!!"
dpkg-deb -b debian_em_all debian_em_all/em_all.deb
mv debian_em_all/em_all.deb debian_em_all/${GEMS_REV}_em_all.deb
cp em_mgmt/adminscripts/debian_upgrade.sh debian_em_all/${GEMS_REV}_debian_upgrade.sh
