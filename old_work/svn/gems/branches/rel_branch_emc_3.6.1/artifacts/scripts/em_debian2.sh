#!/bin/bash
# Author: Kushal
# Version 3.5

ARCH=$1
if [ ! -d debian_ems2 ]
then
        echo "Creating debian_ems2 directory!!!!"
        mkdir debian_ems2
else
        echo "deleting the existing debian_ems2 directory!!!!!!"
        rm -rf debian_ems2
        echo "Re-creating debian_ems2 directory!!!!"
        mkdir debian_ems2
fi
chmod -R 755 debs2/ems/*
echo "Copying the debian files to debian_ems2 directory!!!!"
rsync -a --exclude='.svn' debs2/ems/* debian_ems2/
cp ems/target/ems.war debian_ems2/tmp/ems/var/lib/tomcat6/webapps/ems.war
cp ems_common/enl_utils/target/enl_utils.jar debian_ems2/tmp/ems/home/enlighted/enl_utils.jar
cp ems/artifacts/sql/migration/From_2.0_To_3.0/upgradeSQL.sql debian_ems2/tmp/ems/home/enlighted/
cp ems/artifacts/sql/migration/From_2.0_To_3.0/sppa.sql debian_ems2/tmp/ems/home/enlighted/
#Adding bacnet libraries
cp ../BACNET2/stack-0.8.0/demo/gateway/bacnetLighting debian_ems2/tmp/ems/var/lib/tomcat6/Enlighted/bacnet/bacnetLighting
if [ "$ARCH" = "a32" ]; then
    cp ../BACNET2/libs/i386/* debian_ems2/tmp/ems/usr/lib/
else
    cp ../BACNET2/libs/amd64/* debian_ems2/tmp/ems/usr/lib/
fi
if [ "$ARCH" = "a32" ]; then
    cp artifacts/scripts/1404/bacnet/tools/x86/* debian_ems2/tmp/ems/home/enlighted/bacnet/tools/
else
    cp artifacts/scripts/1404/bacnet/tools/x64/* debian_ems2/tmp/ems/home/enlighted/bacnet/tools/
fi
dpkg-deb -b debian_ems2 debian_ems2/enLighted.deb

#################################################################
##TOMCAT DEBIAN STARTS
#################################################################
if [ ! -d debian_tomcat8 ]
then
        echo "Creating debian_tomcat8 directory!!!!"
        mkdir debian_tomcat8
else
        echo "deleting the existing debian_tomcat8 directory!!!!!!"
        rm -rf debian_tomcat8
        echo "Re-creating debian_tomcat8 directory!!!!"
        mkdir debian_tomcat8
fi
chmod -R 755 debs2/tomcat8/*
echo "Copying the debian files to debian_tomcat directory!!!!"
rsync -a --exclude='.svn' debs2/tomcat8/* debian_tomcat8/
if [ "$ARCH" = "a32" ]; then
	cp debian_tomcat8/src/apache-tomcat-8.0.26.tar.gz debian_tomcat8/tomcat8_x86/
	dpkg-deb -b debian_tomcat8/tomcat8_x86/ debian_tomcat8/tomcat8_i386.deb
else
	cp debian_tomcat8/src/apache-tomcat-8.0.26.tar.gz debian_tomcat8/tomcat8_x64/
        dpkg-deb -b debian_tomcat8/tomcat8_x64/ debian_tomcat8/tomcat8_amd64.deb
fi
### TOMCAT DEBIAN CREATION ENDS


#################################################################
if [ ! -d debian_mgmt2 ]
then
        echo "Creating debian directory!!!!"
        mkdir debian_mgmt2
else
        echo "deleting the existing debian directory!!!!!!"
        rm -rf debian_mgmt2
        echo "Re-creating debian directory!!!!"
        mkdir debian_mgmt2
fi
chmod -R 755 debs2/em_mgmt_new/*
echo "Copying the debian files to debian_mgmt2 directory!!!!"
rsync -a --exclude='.svn' debs2/em_mgmt_new/* debian_mgmt2/
rsync -a --exclude='.svn' em_mgmt/* debian_mgmt2/tmp/em_mgmt_new/var/www/em_mgmt/
cd debian_mgmt2/tmp/em_mgmt_new/var/www/em_mgmt/
chmod -R 766 locale
django-admin.py compilemessages
cd ../../../../../../
svn info | grep 'Last Changed Rev' | sed 's/^.*Rev/Revision/' > debian_mgmt2/tmp/em_mgmt_new/var/www/em_mgmt/management/templatetags/svninfo.txt
dpkg-deb -b debian_mgmt2 debian_mgmt2/em_mgmt.deb 

#################################################################
if [ -d debian_cloud_communicator2 ]
then
        echo "Recreating debian_cloud_communicator2 directory"
        rm -rf debian_cloud_communicator2
        mkdir debian_cloud_communicator2
else
        echo "Creating debian_cloud_communicator2 directory"
        mkdir debian_cloud_communicator2
fi
chmod -R 755 debs2/cloud_communicator/*
echo  "Copying files to debian_cloud_communicator2 directory!!"
rsync -a --exclude='.svn' debs2/cloud_communicator/* debian_cloud_communicator2/

echo  "Copying em_cloud_communicator.jar to debian_cloud_communicator2/tmp/cloud_communicator/opt/enLighted/communicator directory!!"
cp em_cloud_communicator/target/em_cloud_communicator.jar debian_cloud_communicator2/tmp/cloud_communicator/opt/enLighted/communicator
echo "Creating debian package!!!!!"
dpkg-deb -b debian_cloud_communicator2 debian_cloud_communicator2/em_cloud_communicator.deb
#################################################################

#################################################################
GEMS_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)
mv debian_ems2/enLighted.deb debian_ems2/${GEMS_REV}_enLighted.deb
mv debian_mgmt2/em_mgmt.deb debian_mgmt2/${GEMS_REV}_em_mgmt.deb
mv debian_cloud_communicator2/em_cloud_communicator.deb debian_cloud_communicator2/${GEMS_REV}_em_cloud_communicator.deb
#################################################################
if [ -d debian_em_all2 ]
then
        echo "Recreating debian_em_all2 directory"
        rm -rf debian_em_all2
        mkdir debian_em_all2
else
        echo "Creating debian_em_all2 directory"
        mkdir debian_em_all2
fi
echo  "Copying files to debian_em_all2 directory!!"
rsync -a --exclude='.svn' debs2/em_all/* debian_em_all2/

if [ "$ARCH" = "a32" ]; then
    cp debian_em_all2/tmp/em_all/etc/apt/sources.list.32 debian_em_all2/tmp/em_all/etc/apt/sources.list
else
    cp debian_em_all2/tmp/em_all/etc/apt/sources.list.64 debian_em_all2/tmp/em_all/etc/apt/sources.list
fi


svn info | grep 'Last Changed Rev' | sed 's/^.*Rev/CurrentRevision/' >> debian_em_all2/DEBIAN/control
echo "ValidationKey: enLighted" >> debian_em_all2/DEBIAN/control
cp debian_ems2/${GEMS_REV}_enLighted.deb debian_em_all2/tmp/em_all/home/enlighted/debs/
cp debian_mgmt2/${GEMS_REV}_em_mgmt.deb debian_em_all2/tmp/em_all/home/enlighted/debs/
cp debian_cloud_communicator2/${GEMS_REV}_em_cloud_communicator.deb debian_em_all2/tmp/em_all/home/enlighted/debs/
cp -R mobile/Deployables/release/emsMobile* debian_em_all2/tmp/em_all/var/lib/tomcat6/webapps/ROOT/
echo "Creating debian package!!!!!"
dpkg-deb -b debian_em_all2 debian_em_all2/em_all.deb
mv debian_em_all2/em_all.deb debian_em_all2/${GEMS_REV}_${ARCH}_em_all.deb
cp em_mgmt/adminscripts/debian_upgrade.sh debian_em_all2/${GEMS_REV}_${ARCH}_debian_upgrade.sh
