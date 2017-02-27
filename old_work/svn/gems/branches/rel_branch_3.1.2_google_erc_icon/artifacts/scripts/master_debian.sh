#!/bin/bash

if [ -d debian_master ]
then
        echo "Recreating debian_master directory"
        rm -rf debian_master
        mkdir debian_master
else
        echo "Creating debian_master directory"
        mkdir debian_master
fi
chmod -R 755 debs/cloud_system/master_system/*
echo  "Copying files to debian_master directory!!"
rsync -a --exclude='.svn' debs/cloud_system/master_system/* debian_master

echo  "Copying war to debian_master/home/enlighted/master_files/ !!"
cp ecloud/target/ecloud.war debian_master/home/enlighted/master_files/
echo  "Copying other artifacts to debian_master/home/enlighted/master_files/"
cp ecloud/artifacts/server/tomcat/context.xml debian_master/home/enlighted/master_files/
cp ecloud/artifacts/scripts/setCron.sh debian_master/home/enlighted/master_files/ 
cp ecloud/artifacts/scripts/pruneTables.sh debian_master/home/enlighted/master_files/
cp ecloud/artifacts/sql/ecloud_upgrade.sql debian_master/home/enlighted/master_files/
echo "Creating debian package!!!!!"
dpkg-deb -b debian_master debian_master/master.deb

COMM_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)

mv debian_master/master.deb debian_master/${COMM_REV}_master.deb

