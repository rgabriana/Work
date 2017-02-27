#!/bin/bash

if [ -d debian_core_system ]
then
        echo "Recreating debian_core_system directory"
        rm -rf debian_core_system
        mkdir debian_core_system
else
        echo "Creating debian_core_system directory"
        mkdir debian_core_system
fi
chmod -R 755 debs/cloud_system/core_system/*

echo  "Copying files to debian_core_system directory!!"
rsync -a --exclude='.svn' debs/cloud_system/core_system/* debian_core_system

echo "pulling artifacts.."
cp ecloud/artifacts/sql/ecloud_install.sql debian_core_system/home/enlighted/core_file/postgresql/

echo "Creating debian package!!!!!"
dpkg-deb -b  debian_core_system  debian_core_system/core_system.deb

COMM_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)
cp ecloud/artifacts/scripts/system_setup_script.sh debian_core_system/home/enlighted/core_file/${COMM_REV}_system_setup_script.sh
mv debian_core_system/core_system.deb debian_core_system/${COMM_REV}_core_system.deb


