#!/bin/bash


verifyChecksum() {
s=$(md5sum ${1}|cut -d' ' -f1);
if [ "${s}" != "${2}" ];
then
        return 1;
else
        return 0;
fi;
}

HOME=/home/bldadm
PASS="save-energy"
if [ ! -z "$1" ]
then
        HOME="$1"
fi

if [ ! -z "$2" ]
then
        PASS="$2"
fi

ARTIFACTS=artifacts/scripts/1404
if [ ! -z "$3" ]
then
        ARTIFACTS="$3"
fi

CURR_DIR=`pwd`
EM_DEB=debian_em_all2
DEB_SH=em_mgmt/adminscripts
TOMCAT_DEBIAN=debian_tomcat8

mkdir newiso
mkdir iso
if [  -f $UBUISO ];
then 
	if verifyChecksum $HOME/download/ubuntu-14.04.3-server-i386.iso "352009d5b44f0e97c9558919f0147c0c";
	then
		echo "md5sums match not downloading download"
	else
		cd $HOME/download/
		rm ubuntu-14.04.3-server-i386.iso
		wget http://releases.ubuntu.com/14.04.3/ubuntu-14.04.3-server-i386.iso
	fi
else
	cd $HOME/download
	wget http://releases.ubuntu.com/14.04.3/ubuntu-14.04.3-server-i386.iso
fi

cd $CURR_DIR
##Clean the iso directory
if [ -d "ISO_Build_Dir" ];
then
	rm -rf ISO_Build_Dir/*
else
	mkdir ISO_Build_Dir
fi

echo "$PASS" | sudo -S mount -o loop $HOME/download/ubuntu-14.04.3-server-i386.iso iso
sudo cp -r ./iso/* ./newiso/
sudo cp -r ./iso/.disk/ ./newiso/
sudo cp $ARTIFACTS/lang newiso/isolinux/.
sudo cp $ARTIFACTS/txt.cfg newiso/isolinux/.
sudo apt-get -y -d install openssh-server openssl ntp apache2 libapache2-mod-wsgi libapache2-mod-php5 oracle-java8-installer software-properties-common postgresql-9.4 postgresql-plperl-9.4 postgresql-pltcl-9.4 python-paramiko curl tcl isc-dhcp-server php5 php5-pgsql python-django python-setuptools python-django-piston gettext ssh-askpass bind9 zip usbmount lftp uuid
sudo mkdir -p newiso/pool/extras/binary-i386

##JAVA-SECUTIRY PATCHED VERSION
#java_debian_file=$(find $HOME/download/ -type f ! -path "*/.svn**" -path "*oracle-java*i386.deb" | sort -nr | head -n 1)
java_debian_file=$(ls -t `find $HOME/download/ -type f ! -path "*/.svn**" -path "*oracle-java*i386.deb"` | head -n 1)
#echo Copy oracle-java8-jdk_8u66_i386.deb to /var/cache/apt/archives/
#sudo cp $HOME/download/oracle-java8-jdk_8u66_i386.deb /var/cache/apt/archives/
echo Copy $java_debian_file to /var/cache/apt/archives/
sudo cp $java_debian_file /var/cache/apt/archives/

sudo rm -rf newiso/preseed/*
echo coping tomcat8.deb to /newiso/preseed/
sudo cp $TOMCAT_DEBIAN/tomcat8_i386.deb newiso/preseed/tomcat8.deb
sudo cp $ARTIFACTS/* newiso/preseed/.
#EM Debs Coping
echo coping em_all.deb and debian_upgrade.sh to /newiso/preseed/
sudo cp $EM_DEB/*em_all.deb newiso/preseed/em_all.deb
sudo cp $DEB_SH/debian_upgrade.sh newiso/preseed/debian_upgrade.sh
sudo cp /var/cache/apt/archives/*.deb newiso/pool/extras/.
sudo mkdir -p newiso/dists/stable/extras/binary-i386
cd newiso
sudo apt-ftparchive packages ./pool/extras/ > ../Packages
cd ..
sudo gzip -c Packages | tee Packages.gz > /dev/null
sudo mv Packages* newiso/dists/stable/extras/binary-i386/
cd newiso
sudo rm -rf md5sum.txt
sudo md5sum `find ! -name “md5sum.txt” ! -path “./isolinux/*” -follow -type f` > md5sum.txt
cd ..
sudo apt-get -y install mkisofs
sudo mkisofs -J -l -b isolinux/isolinux.bin -no-emul-boot -boot-load-size 4 -boot-info-table -z -iso-level 4 -c isolinux/isolinux.cat -o ./Enlighted_EM_Server1404_Base_32Bit.iso -joliet-long newiso/
GEMS_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)
dt="$((`date '+%Y%m%d%H%M%S'`))"
sudo mv Enlighted_EM_Server1404_Base_32Bit.iso ISO_Build_Dir/ENL_release_1404_32Bit_${GEMS_REV}_${dt}.iso

# clean up
sudo umount iso
sudo rm -rf iso
sudo rm -rf newiso
sudo rm -rf /var/cache/apt/archives/oracle-java8-jdk_8u66_i386.deb
