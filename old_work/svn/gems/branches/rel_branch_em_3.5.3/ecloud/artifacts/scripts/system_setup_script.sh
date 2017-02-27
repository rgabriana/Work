#!/bin/bash -x
#DNS Check
read -p "Give FQDN for this server (eg. in-pu-r-12343.enlightedcloud.net) : " answer
ip="$(ifconfig | grep -A 1 'eth0' | tail -1 | cut -d ':' -f 2 | cut -d ' ' -f 1)"
check="$(nslookup $answer | grep $ip)" 
echo "server ip : $ip"
echo "DNS address : $check"
if [ ! "$check" ] ; then
echo "DNS ip and server ip do not match. Contact Admin before going ahead. Exiting the process..."
exit 1 
fi
file=/tmp/installInfo.properties
if [ ! -e "$file" ] ; then
	sudo touch /tmp/installInfo.properties
	sudo chown enlighted:enlighted /tmp/installInfo.properties
	sudo chmod 777 /tmp/installInfo.properties
fi
sudo echo "dns=$answer"  >> /tmp/installInfo.properties

# set hostname
sethostname=${answer%%.*}
sudo hostname $sethostname
echo "hostname set to $sethostname"
sudo echo "127.0.0.1 $sethostname"  >> /etc/hosts

#Check and install all packages
check_install_packages() {
DEPS="apache2 postgresql-9.2 openjdk-6-jdk openjdk-6-jre tomcat6 tomcat6-admin libapache2-mod-php5 php5-cli sshpass libssh2-php php5-pgsql ufw"
for i in $DEPS ; do
    sudo dpkg-query -W -f='${Package}\n' | grep ^$i$ 
    if [ $? != 0 ] ; then
        echo "Installing package $i ...."
        sudo apt-get --yes install $i -y > /dev/null
        sudo dpkg-query -W -f='${Package}\n' | grep ^$i$ 
         if [ $? != 0 ] ; then
                echo "$i package installation not successful :("
        else
                echo "$i package installation is successful :)"
        fi
    else
        echo "Package $i already installed ...."
    fi
done  
}

#start
echo "Installing prerequisites..."
 sudo dpkg-query -W -f='${Package}\n' | grep python-software-properties
    if [ $? != 0 ] ; then
        echo "Installing package python-software-properties ...."
        sudo apt-get --yes install python-software-properties 
    fi 
sudo add-apt-repository ppa:pitti/postgresql  
echo "Updating repo...."
sudo apt-get update
echo "Package installation starting .."
check_install_packages
echo "PACKAGE INSTALLATION DONE. :)"
echo "Installing Core System debian"
sudo dpkg -i *_core_system.deb
echo "CORE SYSTEM INSTALLATION DONE. :)" 
read -p "Is this Master Server  [yes or no] : " answer
	case $answer in

	  [yY]|[yY][Ee][Ss] )
		echo "Master server installation started...."
		sudo dpkg -i *_master.deb
		echo "MASTER SERVER INSTALLATION COMPLETE. :)"
		;;

	[nN]|[Nn][Oo] )
		echo "Replica server installation started...."
		sudo dpkg -i *_replica.deb
		echo "REPLICA SERVER INSTALLATION COMPLETE. :)"
		;;
	*)
		echo"Invalid option!!! Try again..."
		echo "INSTALLATION FAILED :("
	esac



