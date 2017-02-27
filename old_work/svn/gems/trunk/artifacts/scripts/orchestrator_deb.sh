#!/bin/bash
# Author: Kushal

if [ ! -d debian_orc ]
then
        echo "Creating debian_orc directory!!!!"
        mkdir debian_orc
else
        echo "deleting the existing debian_orc directory!!!!!!"
        rm -rf debian_orc
        echo "Re-creating debian_orc directory!!!!"
        mkdir debian_orc
fi
chmod -R 755 debs/orchestrator/*
echo "Copying the debian files to debian_orc directory!!!!"
rsync -a --exclude='.svn' debs/orchestrator/* debian_orc/

cp ecloud/target/ecloud.war debian_orc/var/lib/tomcat6/webapps/ecloud.war
cp ecloud/artifacts/sql/ecloud_install.sql debian_orc/var/lib/tomcat6/Enlighted/upgradeEnlFiles/
cp ecloud/artifacts/sql/ecloud_upgrade.sql debian_orc/var/lib/tomcat6/Enlighted/upgradeEnlFiles/
dpkg-deb -b debian_orc debian_orc/orchestrator.deb
#################################################################
GEMS_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)
svn info | grep 'Last Changed Rev' | sed 's/^.*Rev/CurrentRevision/' >> debian_orc/DEBIAN/control
echo "ValidationKey: enLighted" >> debian_orc/DEBIAN/control
mv debian_orc/orchestrator.deb debian_orc/${GEMS_REV}_orchestrator.deb
