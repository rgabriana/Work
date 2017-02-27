#!/bin/bash
source /etc/environment

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
    sudo apt-get update
    sudo apt-get -y install binutils java-common
    echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
    sudo dpkg -i debs/oracle-java8-installer_8u25+8u6arm-1~webupd8~1_all.deb
}

installTomcat8() {
    sudo dpkg -i debs/tomcat8_amd64.deb
    sudo cp v2/tomcat.conf /etc/init/
}

installPreReqs() {
    local prevdir=`pwd`
    cd $workdir
    sh v2/prereqs
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
    sudo GOOGLE_INSTALL=$workdir $HOME/upgrade_run.sh em_all.deb $HOME 0.0.0.0 $EM_MGMT_BASE/em_mgmt/em_mgmt 127.0.0.1 F T 1
    sudo cp v2/server.xml $ENL_TOMCAT_HOME/conf/
    cd $prevdir
}

disableDHCP() {
    psql -Upostgres -c "update system_configuration set value='false' where name='dhcp.enable'" ems
}

disableHealthMonitor() {
    psql -Upostgres -c "update system_configuration set value=0 where name='enable.cloud.communication'" ems
}

enableRequiredApacheModules() {
    sudo ln -s $ENL_APACHE_HOME/mods-available/headers.* $ENL_APACHE_HOME/mods-enabled/
    sudo ln -s $ENL_APACHE_HOME/mods-available/mime_magic.* $ENL_APACHE_HOME/mods-enabled/
    sudo ln -s $ENL_APACHE_HOME/mods-available/mpm.* $ENL_APACHE_HOME/mods-enabled/
    sudo ln -s $ENL_APACHE_HOME/mods-available/proxy.* $ENL_APACHE_HOME/mods-enabled/
    sudo ln -s $ENL_APACHE_HOME/mods-available/proxy_http.* $ENL_APACHE_HOME/mods-enabled/
    sudo ln -s $ENL_APACHE_HOME/mods-available/rewrite.* $ENL_APACHE_HOME/mods-enabled/
    sudo ln -s $ENL_APACHE_HOME/mods-available/socache_shmcb.* $ENL_APACHE_HOME/mods-enabled/
    sudo ln -s $ENL_APACHE_HOME/mods-available/ssl.* $ENL_APACHE_HOME/mods-enabled/
}

configureApacheWith2GBMemory() {
    sudo sed -i s/Xmx128m/Xmx2048m/g $TOMCAT_CONF
}
restartServices() {
    #sudo service apache2 restart
    sudo cp $ENLIGHTED_HOME/.pgpass $EM_MGMT_BASE
    sudo chown www-data:www-data $EM_MGMT_BASE/.pgpass
    sudo chmod 0600 $EM_MGMT_BASE/.pgpass
    sudo service $TOMCAT_SUDO_SERVICE restart
}

createEnlighted() {
    useradd -s /bin/bash -d $ENLIGHTED_HOME enlighted
    mkdir -p $ENLIGHTED_HOME
    chown enlighted:enlighted $ENLIGHTED_HOME
}


copyApacheRewriteFileAndSecurityKey() {
    sudo mkdir $ENL_APACHE_HOME/ssl
    sudo cp apache.* $ENL_APACHE_HOME/ssl
    sed -i "s|/var/lib/tomcat6|$ENL_APP_HOME|g" rewrite_prg.pl
    sudo cp rewrite_prg.pl $ENL_APACHE_HOME
    sudo cp v2/000-default.conf $ENL_APACHE_HOME/sites-available/.
}

setEnvironmentVariables(){
	sudo sh v2/setAllEMEnvironment.sh
}

setEnvironmentVariables
source /etc/environment
createEnlighted
redefineSH
setUpFiles
installOracleJava8
installTomcat8
installPreReqs
configurePG
installSoftware
disableDHCP
disableHealthMonitor
sudoConfig
copyApacheRewriteFileAndSecurityKey
enableRequiredApacheModules
configureApacheWith2GBMemory
restartServices
