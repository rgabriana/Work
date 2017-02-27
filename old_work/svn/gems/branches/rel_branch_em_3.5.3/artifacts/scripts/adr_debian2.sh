#!/bin/bash
# Author: Siddharth
# Version 0.1
source /etc/environment

if [ -d debian_adr2 ]
then
        echo "Recreating debian_adr2 directory"
        rm -rf debian_adr2
        mkdir debian_adr2
else
        echo "Creating debian_adr2 directory"
        mkdir debian_adr2
fi
chmod -R 755 debs2/adr/*
echo  "Copying files to debian_adr2 directory!!"
rsync -a --exclude='.svn' debs2/adr/* debian_adr2

echo  "Copying adr.jar to debian_adr2/tmp/adr/opt/enLighted/adr directory!!"
cp adr/target/adr.jar debian_adr2/tmp/adr/opt/enLighted/adr
echo  "Copying ADRInstall.sql to debian_adr2/tmp/adr/opt/enLighted/adr directory!!"
cp adr/artifacts/sql/ADRInstall.sql debian_adr2/tmp/adr/opt/enLighted/adr
echo "Creating debian package!!!!!"
dpkg-deb -b debian_adr2 debian_adr2/adr.deb

#COMM_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)

#mv debian_adr/adr.deb debian_adr/${COMM_REV}_adr.deb

