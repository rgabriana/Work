#!/bin/bash
# Author: Siddharth
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

cp ../../script/buildiso_dashboard.sh .
cp ../../script/buildiso2_dashboard.sh .
cp ../install/debian-install/home/enlighted/sudoers .

cp ../debian_dashboard/ems_dashboard.deb ems_dashboard.deb

echo "enLightedTomcat" | sudo -S  ./buildiso_dashboard.sh 

