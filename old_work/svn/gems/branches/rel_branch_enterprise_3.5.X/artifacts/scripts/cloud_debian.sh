#!/bin/bash
# Author: Kushal

if [ ! -d debian_master ]
then
        echo "Creating debian_master directory!!!!"
        mkdir debian_master
else
        echo "deleting the existing debian_master directory!!!!!!"
        rm -rf debian_master
        echo "Re-creating debian_master directory!!!!"
        mkdir debian_master
fi

GEMS_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)
echo  "Copying files to debian_master directory!!"
rsync -a --exclude='.svn' debs/master/* debian_master/
svn info | grep 'Last Changed Rev' | sed 's/^.*Rev/CurrentRevision/' >> debian_master/DEBIAN/control
echo "ValidationKey: enLighted" >> debian_master/DEBIAN/control

cp ecloud/target/ecloud.war debian_master/home/enlighted/upgrade/
cp ecloud/artifacts/sql/ecloud_upgrade.sql debian_master/home/enlighted/upgrade/
cp ecloud/artifacts/scripts/monitoring/monitor_servers.py debian_master/home/enlighted/upgrade/
cp ecloud/artifacts/scripts/monitoring/monitor_em.py debian_master/home/enlighted/upgrade/

echo "Creating debian package!!!!!"
dpkg-deb -b debian_master debian_master/master.deb
mv debian_master/master.deb debian_master/${GEMS_REV}_master.deb



if [ ! -d debian_replica ]
then
        echo "Creating debian_replica directory!!!!"
        mkdir debian_replica
else
        echo "deleting the existing debian_replica directory!!!!!!"
        rm -rf debian_replica
        echo "Re-creating debian_replica directory!!!!"
        mkdir debian_replica
fi

echo  "Copying files to debian_replica directory!!"
rsync -a --exclude='.svn' debs/replica/* debian_replica/
svn info | grep 'Last Changed Rev' | sed 's/^.*Rev/CurrentRevision/' >> debian_replica/DEBIAN/control
echo "ValidationKey: enLighted" >> debian_replica/DEBIAN/control

cp em_cloud_instance/target/em_cloud_instance.war debian_replica/home/enlighted/upgrade/

echo "Creating debian package!!!!!"
dpkg-deb -b debian_replica debian_replica/replica.deb
mv debian_replica/replica.deb debian_replica/${GEMS_REV}_replica.deb

