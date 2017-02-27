#!/bin/bash
# Author: Siddharth
# Version 0.1

if [ -d debian_dashboard ]
then
        echo "Recreating debian_dashboard directory"
        rm -rf debian_dashboard
        mkdir debian_dashboard
else
        echo "Creating debian_dashboard directory"
        mkdir debian_dashboard
fi
chmod -R 755 debs/em_dashboard/*
echo  "Copying files to debian_dashboard directory!!"
rsync -a --exclude='.svn' debs/em_dashboard/* debian_dashboard

echo  "Copying ems_dashboard.war to debian_dashboard/opt/enLighted/dashboard directory!!"
cp ems_dashboard/target/ems_dashboard.war debian_dashboard/var/lib/tomcat6/webapps
cp ems_dashboard/sql/UpgradeSQL.sql debian_dashboard/home/enlighted/

echo "Creating debian package!!!!!"
dpkg-deb -b debian_dashboard debian_dashboard/ems_dashboard.deb

#COMM_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)

