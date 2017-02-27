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

installPreReqs() {
    local prevdir=`pwd`
    cd $workdir
    sh prereqs
    cd $prevdir
}

configurePG() {
    local prevdir=`pwd`
    cd $workdir
    sudo cp pg_hba.conf postgresql.conf /etc/postgresql/9.1/main/.
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

restartServices() {
    sudo service apache2 restart
    sudo service tomcat6 restart
}

configureTomcat() {
    sed -e 's_/usr/lib/j2sdk1.5-ibm_/usr/lib/j2sdk1.5-ibm /usr/local/buildtools/java/jdk /usr/lib/jvm/java-6-openjdk-amd64/jre_' /etc/init.d/tomcat6 > /etc/init.d/tomcat6.new
    mv /etc/init.d/tomcat6.new /etc/init.d/tomcat6
    chmod +x /etc/init.d/tomcat6
}

createEnlighted() {
    useradd -s /bin/bash -d /usr/local/google/home/enlighted enlighted
    mkdir -p /usr/local/google/home/enlighted
    chown enlighted:enlighted /usr/local/google/home/enlighted
}

createEnlighted
redefineSH
setUpFiles
installPreReqs
configurePG
installSoftware
configureTomcat
disableDHCP
disableHealthMonitor
sudoConfig
restartServices
