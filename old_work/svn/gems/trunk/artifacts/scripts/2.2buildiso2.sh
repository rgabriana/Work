#!/bin/bash
source /etc/environment
mount -t proc none /proc
mount -t sysfs none /sys
mount -t devpts none /dev/pts
export HOME=/root
export LC_ALL=C
dbus-uuidgen > /var/lib/dbus/machine-id
dpkg-divert --local --rename --add /sbin/initctl
ln -s /bin/true /sbin/initctl

mkdir home/enlighted
sudo dpkg -i /tmp/tomcat6-admin_6.0.24-2ubuntu1_all.deb
sudo dpkg -D2000 -i /tmp/em_all.deb
echo "$ENLIGHTED_HOME/upgrade_run.sh em_all.deb $ENLIGHTED_HOME 0.0.0.0 $EM_MGMT_BASE/em_mgmt/em_mgmt 127.0.0.1 F T" >> /success.sh
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
