#!/bin/bash
source /etc/environment
# Author: Sanjeev
# Version 3.6.1
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
	echo "***********************setting ENLIGHTED_HOME hardcoded to /usr/local/google/home/enlighted as it is not still set************"
        export ENLIGHTED_HOME="/usr/local/google/home/enlighted"
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


chmod -R 755 install/debian-install-google/*
echo "Copying the debian files to debian_ems_google directory!!!!"
rsync -a --exclude='.svn' install/debian-install-google/* debian_ems_google/
cp ems/target/ems.war debian_ems_google$ENL_APP_HOME/webapps/ems.war
cp ems_common/enl_utils/target/enl_utils.jar debian_ems_google$ENL_APP_HOME/Enlighted/enl_utils.jar
cp ems/artifacts/sql/migration/From_2.0_To_3.0/upgradeSQL.sql debian_ems_google$ENLIGHTED_HOME/
cp ems/artifacts/sql/migration/From_2.0_To_3.0/sppa.sql debian_ems_google$ENLIGHTED_HOME/

#Adding bacnet libraries
cp ../BACNET2/stack-0.8.0/demo/gateway/bacnetLighting debian_ems_google$ENL_APP_HOME/Enlighted/bacnet/bacnetLighting
cp ../BACNET2/libs/amd64/* debian_ems_google/usr/lib/

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
rsync -a --exclude='.svn' em_mgmt_google/* debian_mgmt_google$EM_MGMT_BASE/em_mgmt/
cd debian_mgmt_google$EM_MGMT_BASE/em_mgmt/
chmod -R 766 locale
django-admin.py compilemessages
cd ../../../../
svn info | grep 'Last Changed Rev' | sed 's/^.*Rev/Revision/' > debian_mgmt_google$EM_MGMT_BASE/em_mgmt/management/templatetags/svninfo.txt
dpkg-deb -b debian_mgmt_google debian_mgmt_google/em_mgmt_google.deb 
#################################################################
if [ -d debian_cloud_communicator_google ]
then
        echo "Recreating debian_cloud_communicator_google directory"
        rm -rf debian_cloud_communicator_google
        mkdir debian_cloud_communicator_google
else
        echo "Creating debian_cloud_communicator_google directory"
        mkdir debian_cloud_communicator_google
fi
chmod -R 755 debs/cloud_communicator_google/*
echo  "Copying files to debian_cloud_communicator_google directory!!"
rsync -a --exclude='.svn' debs/cloud_communicator_google/* debian_cloud_communicator_google

echo  "Copying em_cloud_communicator.jar to debian_cloud_communicator_google$OPT_ENLIGHTED/communicator directory!!"
cp em_cloud_communicator/target/em_cloud_communicator.jar debian_cloud_communicator_google$OPT_ENLIGHTED/communicator

echo "Creating debian package!!!!!"
dpkg-deb -b debian_cloud_communicator_google debian_cloud_communicator_google/cloud_communicator_google.deb
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
cp debian_mgmt_google/em_mgmt_google.deb google/GoogleInstall/debs/${GEMS_REV}_em_mgmt.deb
cp debian_ems_google/enLighted_google.deb google/GoogleInstall/debs/${GEMS_REV}_enLighted.deb
cp debian_cloud_communicator_google/cloud_communicator_google.deb google/GoogleInstall/debs/${GEMS_REV}_cloud_communicator.deb

# Copy DB related files into GoogleInstall dir
cp ems/artifacts/sql/migration/From_2.0_To_3.0/upgradeSQL.sql google/GoogleInstall/
cp ems/artifacts/sql/migration/From_2.0_To_3.0/sppa.sql google/GoogleInstall/
cp install/debian-install-google$ENLIGHTED_HOME/InstallSQL.sql google/GoogleInstall/

# Copy the upgrade_run.sh script from em_all
cp debs/em_all/home/enlighted/upgrade_run.sh google/GoogleInstall
#sed -e '0,/BEGIN GOOGLE PREREQ DECLARATION/d' -e '/END GOOGLE PREREQ DECLARATION/,$d' -e 's/#/sudo apt-get -y install /' google/GoogleInstall/upgrade_run.sh >> google/GoogleInstall/prereqs

# Generate a enl specific sudo extension
sed -e '0,/BEGIN SERVICE SPECIFIC USER SECTION/d' debs/em_mgmt_new/home/enlighted/apache2/installation/sudoers > google/GoogleInstall/enl_sudo
chmod 440 google/GoogleInstall/enl_sudo

# Add a rev number file
echo ${GEMS_REV} > google/GoogleInstall/version.txt
chmod 644 google/GoogleInstall/version.txt

cd google
tar czf ${GEMS_REV}_GoogleInstall.tgz GoogleInstall
cd ..
