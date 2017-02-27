#!/bin/bash
# Author: Siddharth
# Version 0.1

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
chmod -R 755 install/debian-install/*
echo "Copying the debian files to debian_ems directory!!!!"
#cp -R install/debian-install/* debian_ems
rsync -a --exclude='.svn' install/debian-install/* debian_ems/
cp ems/target/ems.war debian_ems/var/lib/tomcat6/webapps/ems.war
cp ems/artifacts/sql/migration/From_2.0_To_3.0/upgradeSQL.sql debian_ems/home/enlighted/
cp ../BACNET/demo/gateway/bacnetd debian_ems/usr/sbin/bacnetd
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
chmod -R 755 debs/em_mgmt/*
echo "Copying the debian files to debian_mgmt directory!!!!"
#cp -R debs/em_mgmt/* debian_mgmt
rsync -a --exclude='.svn' debs/em_mgmt/* debian_mgmt/
#cp ems_mgmt/target/ems_mgmt.war debian_mgmt/var/lib/tomcat6/webapps/ems_mgmt.war
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
#cp -R debs/system/em_system/* debian_sys
rsync -a --exclude='.svn' debs/system/em_system/* debian_sys/
dpkg-deb -b debian_sys debian_sys/em_system.deb
#################################################################
GEMS_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)
mv debian_ems/enLighted.deb debian_ems/${GEMS_REV}_enLighted.deb
mv debian_mgmt/em_mgmt.deb debian_mgmt/${GEMS_REV}_em_mgmt.deb
mv debian_sys/em_system.deb debian_sys/${GEMS_REV}_em_system.deb
mv ems/target/ems.war ems/target/${GEMS_REV}_ems.war
#mv ems_mgmt/target/ems_mgmt.war ems_mgmt/target/${GEMS_REV}_ems_mgmt.war
#################################################################
