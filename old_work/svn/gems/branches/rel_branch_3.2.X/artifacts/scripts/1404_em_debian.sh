#!/bin/bash
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
chmod -R 755 install/1404-debian-install/*
chmod 0600 install/1404-debian-install/home/enlighted/.pgpass
echo "enlighted" | sudo -S chmod 0600 install/1404-debian-install/root/.pgpass
echo "Copying the debian files to debian_ems directory!!!!"
# making sure that we copy over the InstallSQL.sql file from the install/debian-install folder since it will change frequently
cp install/debian-install/home/enlighted/InstallSQL.sql install/1404-debian-install/home/enlighted/.
rsync -a --exclude='.svn' install/1404-debian-install/* debian_ems/
cp ems/target/ems.war debian_ems/var/lib/tomcat6/webapps/ems.war
cp ems/artifacts/sql/migration/From_2.0_To_3.0/upgradeSQL.sql debian_ems/home/enlighted/
cp ems/artifacts/sql/migration/From_2.0_To_3.0/sppa.sql debian_ems/home/enlighted/
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
chmod -R 755 debs/1404_em_mgmt_new/*
echo "Copying the debian files to debian_mgmt directory!!!!"
rsync -a --exclude='.svn' debs/1404_em_mgmt_new/* debian_mgmt/
rsync -a --exclude='.svn' em_mgmt/* debian_mgmt/var/www/em_mgmt/
cd debian_mgmt/var/www/em_mgmt/
django-admin compilemessages
cd ../../../../
svn upgrade
svn info | grep 'Last Changed Rev' | sed 's/^.*Rev/Revision/' > debian_mgmt/var/www/em_mgmt/management/templatetags/svninfo.txt
dpkg-deb -b debian_mgmt debian_mgmt/em_mgmt.deb
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
chmod -R 755 debs/1404_cloud_communicator/*
echo  "Copying files to debian_cloud_communicator directory!!"
rsync -a --exclude='.svn' debs/1404_cloud_communicator/* debian_cloud_communicator

echo  "Copying em_cloud_communicator.jar to debian_cloud_communicator/opt/enLighted/communicator directory!!"
cp em_cloud_communicator/target/em_cloud_communicator.jar debian_cloud_communicator/opt/enLighted/communicator
echo "Creating debian package!!!!!"
dpkg-deb -b debian_cloud_communicator debian_cloud_communicator/em_cloud_communicator.deb
#################################################################
GEMS_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)
mv debian_ems/enLighted.deb debian_ems/${GEMS_REV}_enLighted.deb
mv debian_mgmt/em_mgmt.deb debian_mgmt/${GEMS_REV}_em_mgmt.deb
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
rsync -a --exclude='.svn' debs/1404_em_all/* debian_em_all/
svn info | grep 'Last Changed Rev' | sed 's/^.*Rev/CurrentRevision/' >> debian_em_all/DEBIAN/control
echo "ValidationKey: enLighted" >> debian_em_all/DEBIAN/control
cp debian_cloud_communicator/${GEMS_REV}_em_cloud_communicator.deb debian_em_all/home/enlighted/debs/
cp debian_mgmt/${GEMS_REV}_em_mgmt.deb debian_em_all/home/enlighted/debs/
cp debian_ems/${GEMS_REV}_enLighted.deb debian_em_all/home/enlighted/debs/
cp -R mobile/Deployables/release/emsMobile* debian_em_all/var/lib/tomcat6/webapps/ROOT/

echo "Creating debian package!!!!!"
dpkg-deb -b debian_em_all debian_em_all/em_all.deb
mv debian_em_all/em_all.deb debian_em_all/${GEMS_REV}_1404_em_all.deb
cp em_mgmt/adminscripts/1404_debian_upgrade.sh debian_em_all/${GEMS_REV}_1404_debian_upgrade.sh
