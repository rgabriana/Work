#!/bin/bash
# Author: Sachin

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

ln -s /home/bldadm/workspace/downloads/EnlightedServer1404_Base.iso EnlightedServer1404_Base.iso

cp ../artifacts/scripts/buildiso_1404.sh .
cp ../artifacts/scripts/buildiso2_1404.sh .
cp ../install/debian-install/home/enlighted/sudoers .
cp ../debian_sys/*_em_system.deb em_system.deb
cp ../debian_mgmt/*_em_mgmt.deb em_mgmt.deb
cp ../debian_ems/*_enLighted.deb enLighted.deb
echo "enLightedTomcat" | sudo -S  ./buildiso_1404.sh 
