#!/bin/bash

if [ -d debian_replica ]
then
        echo "Recreating debian_replica directory"
        rm -rf debian_replica
        mkdir debian_replica
else
        echo "Creating debian_replica directory"
        mkdir debian_replica
fi
chmod -R 755 debs/cloud_system/replica_system/*
echo  "Copying files to debian_replica directory!!"
rsync -a --exclude='.svn' debs/cloud_system/replica_system/* debian_replica

echo  "Copying war to debian_replica/home/enlighted/replica_files/ !!"
cp em_cloud_instance/target/em_cloud_instance.war debian_replica/home/enlighted/replica_files/
echo  "Copying other artifacts to debian_replica/home/enlighted/replica_files/"
cp em_cloud_instance/artifacts/connection_config.properties debian_replica/home/enlighted/replica_files/
rsync -a --exclude='.svn'  em_cloud_instance/src/Enlighted/ems_log4j debian_replica/home/enlighted/replica_files/


echo "Creating debian package!!!!!"
dpkg-deb -b debian_replica debian_replica/replica.deb

COMM_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)

mv debian_replica/replica.deb debian_replica/${COMM_REV}_replica.deb

