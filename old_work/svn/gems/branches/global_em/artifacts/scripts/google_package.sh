#!/bin/bash
# Author: Sanjeev
# Version 3.2.0

if [ ! -d debian_ems_google ]
then
        echo "Creating debian_ems_google directory!!!!"
        mkdir debian_ems_google
else
        echo "deleting the existing debian_ems_google directory!!!!!!"
        rm -rf debian_ems_google
        echo "Re-creating debian_ems_google directory!!!!"
        mkdir debian_ems_google
fi
chmod -R 755 install/debian-install-google/*
echo "Copying the debian files to debian_ems_google directory!!!!"
rsync -a --exclude='.svn' install/debian-install-google/* debian_ems_google/
cp ems/target/ems.war debian_ems_google/var/lib/tomcat6/webapps/ems.war
cp ems/artifacts/sql/migration/From_2.0_To_3.0/upgradeSQL.sql debian_ems_google/usr/local/google/home/enlighted/
cp ems/artifacts/sql/migration/From_2.0_To_3.0/sppa.sql debian_ems_google/usr/local/google/home/enlighted/
cp ../BACNET/demo/gateway/bacnetd debian_ems_google/usr/sbin/bacnetd
dpkg-deb -b debian_ems_google debian_ems_google/enLighted_google.deb
#################################################################
if [ ! -d debian_mgmt_google ]
then
        echo "Creating debian directory!!!!"
        mkdir debian_mgmt_google
else
        echo "deleting the existing debian directory!!!!!!"
        rm -rf debian_mgmt_google
        echo "Re-creating debian directory!!!!"
        mkdir debian_mgmt_google
fi
chmod -R 755 debs/em_mgmt_new_google/*
echo "Copying the debian files to debian_mgmt_google directory!!!!"
rsync -a --exclude='.svn' debs/em_mgmt_new_google/* debian_mgmt_google/
rsync -a --exclude='.svn' em_mgmt_google/* debian_mgmt_google/var/www/em_mgmt/
cd debian_mgmt_google/var/www/em_mgmt/
django-admin.py compilemessages
cd ../../../../
svn info | grep 'Last Changed Rev' | sed 's/^.*Rev/Revision/' > debian_mgmt_google/var/www/em_mgmt/management/templatetags/svninfo.txt
dpkg-deb -b debian_mgmt_google debian_mgmt_google/em_mgmt_google.deb 
#################################################################
if [ ! -d debian_sys_google ]
then
        echo "Creating debian directory!!!!"
        mkdir debian_sys_google
else
        echo "deleting the existing debian directory!!!!!!"
        rm -rf debian_sys_google
        echo "Re-creating debian directory!!!!"
        mkdir debian_sys_google
fi
chmod -R 755 debs/system_google/em_system/*
echo "Copying the debian files to debian_sys_google directory!!!!"
rsync -a --exclude='.svn' debs/system_google/em_system/* debian_sys_google/
dpkg-deb -b debian_sys_google debian_sys_google/em_system_google.deb
#################################################################
if [ -d debian_em_uem_communicator_google ]
then
        echo "Recreating debian_em_uem_communicator_google directory"
        rm -rf debian_em_uem_communicator_google
        mkdir debian_em_uem_communicator_google
else
        echo "Creating debian_em_uem_communicator_google directory"
        mkdir debian_em_uem_communicator_google
fi
chmod -R 755 debs/em_uem_communicator_google/*
echo  "Copying files to debian_em_uem_communicator_google directory!!"
rsync -a --exclude='.svn' debs/em_uem_communicator_google/* debian_em_uem_communicator_google

echo  "Copying em_uem_communicator.jar to debian_em_uem_communicator/opt/enLighted/uem/communicator directory!!"
cp em_uem_communicator/target/em_uem_communicator.jar debian_em_uem_communicator_google/opt/enLighted/uem/communicator
cp em_uem_communicator/artifacts/log4j.properties debian_em_uem_communicator_google/opt/enLighted/uem/communicator
cp em_uem_communicator/artifacts/start.sh debian_em_uem_communicator_google/opt/enLighted/uem/communicator
echo "Creating debian package!!!!!"
dpkg-deb -b debian_em_uem_communicator_google debian_em_uem_communicator_google/em_uem_communicator_google.deb
GEMS_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)
if [ -d google ]
then
        echo "Recreating GoogleInstall directory"
        rm -rf google
        mkdir -p google/GoogleInstall
else
        echo "Creating GoogleInstall directory"
        mkdir -p google/GoogleInstall
fi
echo  "Copying files to GoogleInstall directory!!"
rsync -a --exclude='.svn' install/google/* google/GoogleInstall/
cp debian_sys_google/em_system_google.deb google/GoogleInstall/debs/${GEMS_REV}_em_system.deb
cp debian_mgmt_google/em_mgmt_google.deb google/GoogleInstall/debs/${GEMS_REV}_em_mgmt.deb
cp debian_ems_google/enLighted_google.deb google/GoogleInstall/debs/${GEMS_REV}_enLighted.deb
cp debian_em_uem_communicator_google/em_uem_communicator_google.deb google/GoogleInstall/debs/${GEMS_REV}_em_uem_communicator.deb

# Copy DB related files into GoogleInstall dir
cp ems/artifacts/sql/migration/From_2.0_To_3.0/upgradeSQL.sql google/GoogleInstall/
cp ems/artifacts/sql/migration/From_2.0_To_3.0/sppa.sql google/GoogleInstall/
cp install/debian-install-google/usr/local/google/home/enlighted/InstallSQL.sql google/GoogleInstall/

# Copy the upgrade_run.sh script from em_all
cp debs/em_all/home/enlighted/upgrade_run.sh google/GoogleInstall
sed -e '0,/BEGIN GOOGLE PREREQ DECLARATION/d' -e '/END GOOGLE PREREQ DECLARATION/,$d' -e 's/#/sudo apt-get -y install /' google/GoogleInstall/upgrade_run.sh >> google/GoogleInstall/prereqs

# Generate a enl specific sudo extension
sed -e '0,/BEGIN SERVICE SPECIFIC USER SECTION/d' debs/em_mgmt_new/home/enlighted/apache2/installation/sudoers > google/GoogleInstall/enl_sudo
chmod 440 google/GoogleInstall/enl_sudo

# Add a rev number file
echo ${GEMS_REV} > google/GoogleInstall/version.txt
chmod 644 google/GoogleInstall/version.txt

cd google
tar czf ${GEMS_REV}_GoogleInstall.tgz GoogleInstall
cd ..
