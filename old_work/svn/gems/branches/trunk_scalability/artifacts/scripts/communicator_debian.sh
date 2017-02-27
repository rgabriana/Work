#!/bin/bash
# Author: Siddharth
# Version 0.1


if [ -d debian_communicator ]
then
        echo "Recreating debian_communicator directory"
        rm -rf debian_communicator
        mkdir debian_communicator
else
        echo "Creating debian_communicator directory"
        mkdir debian_communicator
fi
chmod -R 755 debs/communicator/*
echo  "Copying files to debian_communicator directory!!"
rsync -a --exclude='.svn' debs/communicator/* debian_communicator

echo  "Copying ems_communicator.jar to debian_communicator/opt/enLighted/communicator directory!!"
cp ems_communicator/target/ems_communicator.jar debian_communicator/opt/enLighted/communicator
echo "Creating debian package!!!!!"
dpkg-deb -b debian_communicator debian_communicator/ems_communicator.deb

#COMM_REV=$(svn info | grep 'Last Changed Rev' | cut -d ' ' -f4)

#mv debian_communicator/ems_communicator.deb debian_communicator/${COMM_REV}_ems_communicator.deb


