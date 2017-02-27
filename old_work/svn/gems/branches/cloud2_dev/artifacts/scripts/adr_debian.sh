#!/bin/bash
# Author: Siddharth
# Version 0.1

if [ -d debian_adr ]
then
        echo "Recreating debian_adr directory"
        rm -rf debian_adr
        mkdir debian_adr
else
        echo "Creating debian_adr directory"
        mkdir debian_adr
fi
chmod -R 755 debs/adr/*
echo  "Copying files to debian_adr directory!!"
rsync -a --exclude='.svn' debs/adr/* debian_adr

echo  "Copying adr.jar to debian_adr/opt/enLighted/adr directory!!"
cp adr/target/adr.jar debian_adr/opt/enLighted/adr
echo  "Copying ADRInstall.sql to debian_adr/opt/enLighted/adr directory!!"
cp adr/artifacts/sql/ADRInstall.sql debian_adr/opt/enLighted/adr
echo "Creating debian package!!!!!"
dpkg-deb -b debian_adr debian_adr/adr.deb

#COMM_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)

#mv debian_adr/adr.deb debian_adr/${COMM_REV}_adr.deb

