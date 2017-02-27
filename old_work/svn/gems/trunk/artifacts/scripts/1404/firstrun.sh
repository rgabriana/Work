# !/bin/bash

# Adding EM host openssh key file 
mv /opt/enLighted/tmp/1004_ssh_host_* /etc/ssh/
sudo chmod 600 /etc/ssh/1004_ssh_host_dsa_key
sudo chmod 600 /etc/ssh/1004_ssh_host_rsa_key
mv /opt/enLighted/tmp/sshd_config /etc/ssh/

# ISC DHCP server and networking settings
mv /opt/enLighted/tmp/isc-dhcp-server /etc/init.d/isc-dhcp-server
mv /opt/enLighted/tmp/interfaces /etc/network/interfaces

# set the PGPORT and PGPASSWORD environment variables in bashrc
echo "export PGPORT=5433" >> /etc/bash.bashrc
echo "export PGPORT=5433" >> /etc/profile
echo "export PGPASSWORD=postgres" >> /etc/bash.bashrc
echo "export PGPASSWORD=postgres" >> /etc/profile
echo "JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-x64" >> /etc/profile
#echo "JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-x64" >> /etc/default/tomcat6
echo "export PGPORT=5433" >> /etc/apache2/envvars
# add enetry in to resolv.conf for ping gems.enlightedcustomer.net
echo "nameserver 127.0.0.1" >> /etc/resolvconf/resolv.conf.d/head
#Bind Configuration files
sudo rm -rf /etc/bind/
sudo rm -rf /var/lib/bind/
sudo tar xvf /opt/enLighted/tmp/etc-bind.bk.tar -C /
sudo tar xvf /opt/enLighted/tmp/var-bind.bk.tar -C /
sudo cp /opt/enLighted/tmp/named.conf.options /etc/bind/
chmod u+s `which ping`
echo "/usr/bin/dhcpscript ux," >> /etc/apparmor.d/usr.sbin.dhcpd
# copy the pg_hba.conf file to postgres config folder
mv /opt/enLighted/tmp/pg_hba.conf /etc/postgresql/9.4/main/pg_hba.conf
# set the postgres user password
sudo -u postgres psql postgres < /opt/enLighted/tmp/psqlpassword.sql
# create the ssl folder in apache and copy the key and pem files
mkdir -p /etc/apache2/ssl
mkdir -p /home/enlighted/django_cache
mv /opt/enLighted/tmp/apache* /etc/apache2/ssl/.
mv /opt/enLighted/tmp/rewrite_prg.pl /etc/apache2/.
mv /opt/enLighted/tmp/000-default.conf /etc/apache2/sites-available/.
if [ `echo $?` -eq 0 ]
then
	echo "Successfully copied" >/home/enlighted/a.log
fi
sudo bash /success.sh
sudo cp /home/enlighted/.pgpass /var/www/.
sudo chown www-data:www-data /var/www/.pgpass
sudo rm /bin/sh
sudo ln -s /bin/bash /bin/sh
sudo dpkg -i /opt/enLighted/tmp/tomcat8.deb
sudo ln -s /sbin/ifconfig /usr/bin/ifconfig
echo "tomcat ALL=NOPASSWD: /usr/bin/ifconfig" >> /etc/sudoers
sudo chown -R tomcat:tomcat /opt/tomcat/
#sudo update-rc.d tomcat defaults
#sudo update-rc.d tomcat defaults 99
#sudo update-rc.d tomcat enable 2 3 4 5
sudo chown -R tomcat:tomcat /opt/tomcat/
sudo sh /opt/enLighted/tmp/setAllEMEnvironment.sh
sudo sh /opt/enLighted/tmp/hostname.sh 

# USB mounting
sudo sed -i 's/MOUNTOPTIONS="/MOUNTOPTIONS="user,umask=000,/' /etc/usbmount/usbmount.conf

# Services etc
sudo service postgresql restart
sudo sh /opt/enLighted/tmp/debian_upgrade.sh /opt/enLighted/tmp/em_all.deb
sudo update-grub
sudo rm -rf /opt/enLighted/tmp/*
sudo rm -rf /apache-tomcat-8.0.26.tar.gz
sudo rm -rf /tomcat*
rm $0
shutdown -r now

