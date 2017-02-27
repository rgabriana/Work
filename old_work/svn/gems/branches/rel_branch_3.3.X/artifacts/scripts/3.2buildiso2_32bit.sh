#!/bin/bash
mount -t proc none /proc
mount -t sysfs none /sys
mount -t devpts none /dev/pts
export HOME=/root
export LC_ALL=C
dbus-uuidgen > /var/lib/dbus/machine-id
dpkg-divert --local --rename --add /sbin/initctl
ln -s /bin/true /sbin/initctl
 
mkdir home/enlighted
sudo dpkg -D2000 -i /tmp/em_all.deb
echo "/home/enlighted/upgrade_run.sh em_all.deb /home/enlighted 0.0.0.0 /var/www/em_mgmt/em_mgmt 127.0.0.1 F T" >> /success.sh
sudo rm -rf ~/.bash_history
sudo rm /var/lib/dbus/machine-id
sudo rm /sbin/initctl
sudo dpkg-divert --rename --remove /sbin/initctl
sudo umount /proc
sudo umount /sys
sudo umount /dev/pts
sudo rm -f /var/log/NLSS*
sudo rm -rf /tmp/*
exit
