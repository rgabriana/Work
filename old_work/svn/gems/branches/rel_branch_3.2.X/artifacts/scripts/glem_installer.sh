#!/bin/bash
# Author : Sachin K

sudo apt-get update
sudo apt-get -y install binutils java-common
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
sudo dpkg -i oracle-java8-installer_8u25+8u6arm-1~webupd8~1_all.deb
sudo apt-get -y install apache2
sudo apt-get -y install tomcat6 tomcat6-admin
sudo apt-get -y install postgresql
sudo dpkg -i --force-overwrite *orchestrator.deb

