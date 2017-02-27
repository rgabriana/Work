# !/bin/bash

cp /opt/enlighted/interfaces /etc/network/interfaces

# set bash as the default shell instead of dash, dash causes a lot of our scripts to fail
# basically we point /bin/sh to /bin/bash instead of /bin/dash
sudo rm /bin/sh
sudo ln -s /bin/bash /bin/sh

# copy the ssh_host* keys to /etc/ssh folder these are required by the gateway 
# to authenticate against the EM for passwordless scp during Image Upgrade
sudo cp /opt/enlighted/ssh_host* /etc/ssh

# set the PGPORT and PGPASSWORD environment variables in bashrc
echo "export PGPORT=5433" >> /etc/bash.bashrc
echo "export PGPORT=5433" >> /etc/profile
echo "export PGPASSWORD=postgres" >> /etc/bash.bashrc
echo "export PGPASSWORD=postgres" >> /etc/profile
echo "JAVA_HOME=/usr/lib/jvm/java-8-oracle" >> /etc/profile
echo "JAVA_HOME=/usr/lib/jvm/java-8-oracle" >> /etc/default/tomcat6
sudo sed -i s/128m/1024m/g /etc/default/tomcat6 

echo "export PGPORT=5433" >> /etc/apache2/envvars
sudo cp /home/enlighted/.pgpass /var/www/.
sudo chown www-data:www-data /var/www/.pgpass
# set the postgres user password
sudo -u postgres psql postgres < /opt/enlighted/psqlpassword.sql

# copy the pg_hba.conf file to postgres config folder
cp /opt/enlighted/pg_hba.conf /etc/postgresql/9.3/main/pg_hba.conf

# create the ssl folder in apache and copy the key and pem files
mkdir -p /etc/apache2/ssl
cp /opt/enlighted/apache* /etc/apache2/ssl/.
cp /opt/enlighted/rewrite_prg.pl /etc/apache2/.
cp /opt/enlighted/000-default.conf /etc/apache2/sites-available/.

# update bind 9 related files
#Bind Configuration files
sudo rm -rf /etc/bind/
sudo rm -rf /var/lib/bind/
sudo tar xvf /opt/enlighted/etc-bind.bk.tar -C /
sudo tar xvf /opt/enlighted/var-bind.bk.tar -C /

sudo update-rc.d apparmor disable

# set up grub to ignore the network adapters names, basically not to use the USB NIC on the dell optiplex 9020 as 'em1' instead of eth1
sudo sed -i s/GRUB_CMDLINE_LINUX_DEFAULT=\"quiet\ splash\"/GRUB_CMDLINE_LINUX_DEFAULT=\"net.ifnames=0\ biosdevname=0\ quiet\ splash\"/g /etc/default/grub
sudo update-grub

sudo bash /success.sh
rm $0
shutdown -r now
