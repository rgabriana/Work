#!/bin/bash

set -e

workdir=`pwd`

redefineSH() {
    local prevdir=`pwd`
    cd /bin
    sudo mv sh sh.orig
    sudo ln -s bash sh
    cd $prevdir
}

setUpFiles() {
    local prevdir=`pwd`
    cd $workdir
    cp upgrade_run.sh $HOME/upgrade_run.sh
    cp -r debs $HOME/.
    chmod +x $HOME/upgrade_run.sh
    cd $prevdir
}

installOracleJava8() {
    sudo add-apt-repository -y ppa:webupd8team/java
    sudo apt-get update
    echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
    sudo apt-get -y install oracle-java8-installer
    sudo apt-get -y install oracle-java8-set-default
}

installPreReqs() {
    local prevdir=`pwd`
    cd $workdir
    sh prereqs
    cd $prevdir
}

configurePG() {
    local prevdir=`pwd`
    cd $workdir
    sudo cp pg_hba.conf postgresql.conf /etc/postgresql/9.3/main/.
    sudo service postgresql restart
    psql -Upostgres -c "alter user postgres with password 'postgres'" postgres
    psql -Upostgres -f ascii.sql
    cd $prevdir
}

sudoConfig() {
    local prevdir=`pwd`
    cd $workdir
    chmod 440 enl_sudo
    sudo cp enl_sudo /etc/sudoers.d/.
    sudo goobuntu-config set custom_etc_sudoers_d true
    cd $prevdir
}

installSoftware() {
    local prevdir=`pwd`
    cd $workdir
    createdb -Upostgres ems
    psql -Upostgres -P pager=off -f InstallSQL.sql ems
    psql -Upostgres -P pager=off -f upgradeSQL.sql ems
    sudo GOOGLE_INSTALL=$workdir $HOME/upgrade_run.sh em_all.deb $HOME 0.0.0.0 /var/www/em_mgmt/em_mgmt 127.0.0.1 F T
    cd $prevdir
}

disableDHCP() {
    psql -Upostgres -c "update system_configuration set value='false' where name='dhcp.enable'" ems
}

disableHealthMonitor() {
    psql -Upostgres -c "update system_configuration set value=0 where name='enable.cloud.communication'" ems
}

enableRequiredApacheModules() {
    sudo ln -s /etc/apache2/mods-available/headers.* /etc/apache2/mods-enabled/
    sudo ln -s /etc/apache2/mods-available/mime_magic.* /etc/apache2/mods-enabled/
    sudo ln -s /etc/apache2/mods-available/mpm.* /etc/apache2/mods-enabled/
    sudo ln -s /etc/apache2/mods-available/proxy.* /etc/apache2/mods-enabled/
    sudo ln -s /etc/apache2/mods-available/proxy_http.* /etc/apache2/mods-enabled/
    sudo ln -s /etc/apache2/mods-available/rewrite.* /etc/apache2/mods-enabled/
    sudo ln -s /etc/apache2/mods-available/socache_shmcb.* /etc/apache2/mods-enabled/
    sudo ln -s /etc/apache2/mods-available/ssl.* /etc/apache2/mods-enabled/
}

restartServices() {
    #sudo service apache2 restart
    sudo service tomcat6 restart
}

createEnlighted() {
    useradd -s /bin/bash -d /usr/local/google/home/enlighted enlighted
    mkdir -p /usr/local/google/home/enlighted
    chown enlighted:enlighted /usr/local/google/home/enlighted
}


copyApacheRewriteFileAndSecurityKey() {
    sudo mkdir /etc/apache2/ssl
    sudo cp apache.* /etc/apache2/ssl
    sudo cp rewrite_prg.pl /etc/apache2
    sudo cp 000-default.conf /etc/apache2/sites-available/.
}

createEnlighted
redefineSH
setUpFiles
installOracleJava8
installPreReqs
configurePG
installSoftware
disableDHCP
disableHealthMonitor
sudoConfig
copyApacheRewriteFileAndSecurityKey
enableRequiredApacheModules
restartServices
