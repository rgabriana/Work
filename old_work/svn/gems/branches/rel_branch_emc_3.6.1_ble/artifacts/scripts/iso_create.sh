#!/bin/bash
# Author: Siddharth
source /etc/environment
if [ -d ISO_Build_Dir ]
then
	echo "Recreating ISO_Build_Dir directory !!!!"
        echo "enLightedTomcat" | sudo -S rm -rf ISO_Build_Dir
	mkdir ISO_Build_Dir
else 
	echo "Creating ISO_Build_Dir directory !!!!!!"
	mkdir ISO_Build_Dir
fi
cd ISO_Build_Dir

ln -s /home/bldadm/workspace/downloads/tomcat6-admin_6.0.24-2ubuntu1_all.deb tomcat6-admin_6.0.24-2ubuntu1_all.deb
ln -s /home/bldadm/workspace/downloads/base.iso base.iso

cp ../artifacts/scripts/buildiso.sh .
cp ../artifacts/scripts/buildiso2.sh .
cp ../install/debian-install$ENLIGHTED_HOME/sudoers .
cp ../debian_sys/*_em_system.deb em_system.deb
cp ../debian_mgmt/*_em_mgmt.deb em_mgmt.deb
cp ../debian_ems/*_enLighted.deb enLighted.deb
echo "enLightedTomcat" | sudo -S  ./buildiso.sh 
