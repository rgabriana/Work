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

updateSoftware() {
    local prevdir=`pwd`
    sudo cp -a $ENL_APACHE_HOME/ssl $ENL_APACHE_HOME/ssl.pre_upgrade
    cd $workdir
    sudo GOOGLE_INSTALL=$workdir $HOME/upgrade_run.sh em_all.deb $HOME 0.0.0.0 $EM_MGMT_BASE/em_mgmt/em_mgmt 127.0.0.1 F T 1
    psql -Upostgres -P pager=off -f upgradeSQL.sql ems    
    sudo rm -rf $ENL_APACHE_HOME/ssl
    sudo mv $ENL_APACHE_HOME/ssl.pre_upgrade $ENL_APACHE_HOME/ssl
    cd $prevdir
}

disableDHCP() {
    psql -Upostgres -c "update system_configuration set value='false' where name='dhcp.enable'" ems
}

disableHealthMonitor() {
    psql -Upostgres -c "update system_configuration set value=0 where name='enable.cloud.communication'" ems
}

restartServices() {
    sudo service apache2 restart
    sudo service $TOMCAT_SUDO_SERVICE restart
}

configureTomcat() {
    sed -e 's_/usr/lib/j2sdk1.5-ibm_/usr/lib/j2sdk1.5-ibm /usr/local/buildtools/java/jdk /usr/lib/jvm/java-6-openjdk-amd64/jre_' $TOMCAT_SERVICE > $TOMCAT_SERVICE.new
    mv $TOMCAT_SERVICE.new $TOMCAT_SERVICE
    chmod +x $TOMCAT_SERVICE
}

createEnlighted() {
    useradd -s /bin/bash -d $ENLIGHTED_HOME enlighted
    mkdir -p $ENLIGHTED_HOME
    chown enlighted:enlighted $ENLIGHTED_HOME
}

setEnvironmentVariables(){
	sudo sh ./setAllEMEnvironment.sh
}

setEnvironmentVariables
source /etc/environment
setUpFiles
configurePG
updateSoftware
configureTomcat
disableDHCP
sudoConfig
disableHealthMonitor
restartServices