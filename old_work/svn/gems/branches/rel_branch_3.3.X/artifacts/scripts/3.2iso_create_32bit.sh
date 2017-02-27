#!/bin/bash
# Author: Yogesh , Sachin K

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
 
ln -s /home/bldadm/workspace/downloads/EnlightedServer1404_32bit__Base.iso EnlightedServer1404_32bit__Base.iso

cp ../artifacts/scripts/3.2buildiso_32bit.sh .
cp ../artifacts/scripts/3.2buildiso2_32bit.sh .
cp ../install/debian-install/home/enlighted/sudoers .
cp ../debian_em_all/*_em_all.deb em_all.deb
chmod +x *.sh
echo "enlighted" | sudo -S  ./3.2buildiso_32bit.sh 
