#!/bin/bash

export LOG_DIR="/var/lib/tomcat6/Enlighted/adminlogs"
export EMS_MODE_FILE="/var/lib/tomcat6/Enlighted/emsmode"
mkdir -p $LOG_DIR

LOGS_ERR=$LOG_DIR/upgradegems_error.log
LOGS=$LOG_DIR/upgradegems.log
LOGS_DB=$LOG_DIR/dbupgrade.log

export upgradeBackupDir="/var/lib/tomcat6/Enlighted/tempExtract"
export tomcatwebapp="/var/lib/tomcat6/webapps"
export communicatorjar="/opt/enLighted/communicator/em_cloud_communicator.jar"
mkdir -p $upgradeBackupDir

########################################################################################################################
# NOTE: This script is meant to work for normal installs and also for Google's special package. When running for       #
# Google's special package, the variable $GOOGLE_INSTALL will be defined to allow us to get the rev num in a different #
# way than the normal query of the em_all.deb file.                                                                    #
########################################################################################################################
DEBIAN_FILE_NAME=$(echo "$1")
if [ -z "$GOOGLE_INSTALL" ];then
    DEBIAN_REV_NUMBER=$(dpkg-deb -f "$DEBIAN_FILE_NAME" CurrentRevision)
else
    DEBIAN_REV_NUMBER=`cat $GOOGLE_INSTALL/version.txt`
fi

workingDirectory=$2

APP_ROOT=$4
if [ -z "$APP_ROOT" ]
then
    APP_ROOT="/var/www/em_mgmt/em_mgmt"
fi

IP_ADDRESS=$5
if [ -z "$IP_ADDRESS" ]
then
    IP_ADDRESS="127.0.0.1"
fi

FORCE=$6
FRESH_INSTALL=$7

if [ "$FORCE" == "F" ]
then
    LOGS_ERR=$LOG_DIR/recover_error.log
    LOGS=$LOG_DIR/recover.log
fi

AUDITLOGSCRIPT="$APP_ROOT/../adminscripts/auditlogs.sh"
BACKUPSCRIPT="$APP_ROOT/../adminscripts/backuprestoreguiaction.sh"

if [ "$FORCE" != "F" ]
then
    ps -ef | grep postgresql | grep -v grep > /dev/null
    if [ $? -eq 0 ]
    then
        if [ `psql -q -U postgres -h localhost  -t -c "select count(*) from pg_database where datname='ems'"` -eq 1 ]
        then
            rm -f $upgradeBackupDir/beforeUpgradeDBBackup*
            rm -f $upgradeBackupDir/ems_dump*

            echo "*** Taking database backup ***"  >> $LOGS_ERR
            if [ -f "$BACKUPSCRIPT" ]
            then
                /bin/bash $BACKUPSCRIPT "backup" "beforeUpgradeDBBackup" "/opt/enLighted/DB/DBBK" "$APP_ROOT" "$IP_ADDRESS" "F"
                backupexists=$(ls $upgradeBackupDir/beforeUpgradeDBBackup*tar.gz)
                if [ -z "$backupexists" ]
                then
                    echo "UPGRADE ERROR: Database backup was not successful. Cannot continue with upgrade."
                    exit 2
                fi
            else
                POSTGRESUSER=postgres
                POSTGRESHOST=localhost
                POSTGRESDATABASE=ems
                /usr/bin/pg_dump -i -U $POSTGRESUSER -h $POSTGRESHOST -F c -b -f  "$upgradeBackupDir/beforeUpgradeDBBackup.backup" $POSTGRESDATABASE
                if [ $? -ne 0 ]
                then
                    echo "UPGRADE ERROR: Database backup was not successful. Cannot continue with upgrade."
                    exit 2
                fi
            fi
        else
            echo "*** First time install ***"  >> $LOGS_ERR
        fi
    else
        echo "UPGRADE ERROR: Postgres not running. Cannot take database backup. Stopping upgrade process."
        exit 2
    fi

    echo "*** Taking application backup ***" >> $LOGS_ERR
    rm -f $upgradeBackupDir/*.war
    rm -f $upgradeBackupDir/*.jar

    cp /var/www/em_mgmt/adminscripts/debian_upgrade.sh "$upgradeBackupDir/debian_upgrade.sh"
    cp /var/www/em_mgmt/adminscripts/backuprestoreguiaction.sh "$upgradeBackupDir/backuprestoreguiaction.sh"
    cp /var/www/em_mgmt/adminscripts/recover.sh "$upgradeBackupDir/recover.sh"

    lastUpgradeDeb=$(ls -lt $upgradeBackupDir/*.deb | head -n 1)
    if [[ "$lastUpgradeDeb" =~ "em_all.deb" ]]
    then
        echo "*** Application backup successful ***"  >> $LOGS_ERR
    else
        cp $communicatorjar "$upgradeBackupDir/"
        cp $tomcatwebapp/*.war "$upgradeBackupDir/"
        echo "*** Application backup successful ***"  >> $LOGS_ERR
    fi
fi

if [ "$FRESH_INSTALL" != "T" ]
then
	echo "step2;"

	echo "*** Stopping tomcat6 service ***"
	stopStatus=$(sudo /etc/init.d/tomcat6 stop N)
	if [[ "$stopStatus" =~ "done" ]]
	then
    	echo "*** Tomcat6 is down ***";
	else
    	echo "UPGRADE ERROR: Failed to stop tomcat6 service. Exit upgrade process."
    	exit 1
	fi
fi

########################################################################################################################
# NOTE: This script is meant to work for normal installs and also for Google's special package. When running for       #
# Google's special package, the variable $GOOGLE_INSTALL will be defined to allow us to specify the right debs         #
# directory.                                                                                                           #
########################################################################################################################
if [ -z "$GOOGLE_INSTALL" ];then
    cd /home/enlighted/debs
else
    cd $HOME/debs/
fi

echo "startemupgrade;"

echo "*** Upgrading EM App... ***" >> $LOGS_ERR
sudo dpkg -i --force-overwrite ./*_enLighted.deb
if [ $? -eq 0 ]
then
    echo "step3;"
    echo "*** EM App upgraded successfully ***" >> $LOGS_ERR
else
    echo "UPGRADE ERROR: EM App upgrade failed. Exit upgrade process and bringing up tomcat server."
    exit 3
fi

echo "*** Upgrading EM System... ***" >> $LOGS_ERR
sudo dpkg -i --force-overwrite ./*_em_system.deb
if [ $? -eq 0 ]
then
    echo "step4;"
    echo "*** EM System upgraded successfully ***" >> $LOGS_ERR
else
    echo "UPGRADE ERROR: EM System upgrade failed. Exit upgrade process and bringing up tomcat server."
    exit 3
fi

echo "*** Upgrading Em Uem Communicator... ***" >> $LOGS_ERR
sudo dpkg -i --force-overwrite ./*_em_uem_communicator.deb
if [ $? -eq 0 ]
then
    echo "*** EM UEM Communicator upgraded successfully ***" >> $LOGS_ERR
else
    echo "UPGRADE ERROR: EM UEM Communicator upgrade failed. Exit upgrade process and bringing up tomcat server."
    exit 3
fi

########################################################################################################################
# NOTE: This script is meant to work for normal installs and also for Google's special package. When running for       #
# Google's special package, the variable $GOOGLE_INSTALL will be defined to allow us to skip the section below.        #
# IF YOU CHANGE THE MISSING LIBRARIES IN THE SKIPPED SECTION, YOU NEED TO MODIFY THE COMMENT BELOW WHICH DECLARES THE  #
# PREREQUISITES IN THE FORM OF APT PACKAGES FOR 12.04 (SO THAT THEY CAN BE INSTALLED USING APT-GET BY THE GOOGLE       #
# INSTALLER.                                                                                                           #
########################################################################################################################
# BEGIN GOOGLE PREREQ DECLARATION
# python-dev libapr1 libaprutil1 libaprutil1-dbd-sqlite3 libaprutil1-ldap apache2.2-bin apache2-utils apache2.2-common apache2-mpm-worker apache2 libk5crypto3 libkrb5support0 libkrb5-3 libgssapi-krb5-2 libldap-2.4-2 libpq5 libldap2-dev libexpat1-dev libdb4.8-dev libpcrecpp0 libpcre3-dev uuid-dev libapr1-dev libsqlite3-dev zlib1g-dev libssl-dev libgssrpc4 libkdb5-6 libkadm5srv-mit8 libkadm5clnt-mit8 comerr-dev krb5-multidev libkrb5-dev libpq-dev mysql-common libmysqlclient18 libmysqlclient-dev libaprutil1-dev apache2-threaded-dev pmount usbmount tcl8.5 postgresql-pltcl-9.1 postgresql-plperl-9.1 libapache2-mod-wsgi libxml2
# END GOOGLE PREREQ DECLARATION
if [ -z "$GOOGLE_INSTALL" ];then
    echo "*** Upgrading EM Communicator... ***" >> $LOGS_ERR
    sudo dpkg -i --force-overwrite ./*_em_cloud_communicator.deb
    if [ $? -eq 0 ]
    then
        echo "step5;"
        echo "*** EM Communicator upgraded successfully ***" >> $LOGS_ERR
    else
        echo "UPGRADE ERROR: EM Communicator upgrade failed. Exit upgrade process and bringing up tomcat server."
        exit 3
    fi
    
    ## Install missing libraries
    echo "*** Installing missging libraries. ***" >> $LOGS_ERR
    #python2.6-dev
    check=$(dpkg -l python2.6-dev | grep "python2.6-dev" | grep "2.6.5" )
    if [[ "$check" =~ "python2.6-dev" ]]
        then
            echo "*** python2.6-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling python2.6-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/python2.6-dev_2.6.5-1ubuntu6_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install python2.6-dev successfully."
                exit 3
            fi
    fi
    
    #python-dev
    check=$(dpkg -l python-dev | grep "python-dev" | grep "2.6.5" )
    if [[ "$check" =~ "python-dev" ]]
        then
            echo "*** python-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling python-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/python-dev_2.6.5-0ubuntu1_all.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install python-dev successfully."
                exit 3
            fi
    fi
    
    #libapr1
    check=$(dpkg -l libapr1 | grep "libapr1" | grep "1.3.8")
    if [[ "$check" =~ "libapr1" ]]
        then
            echo "*** libapr1 is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libapr1... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libapr1_1.3.8-1build1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libapr1 successfully."
                exit 3
            fi
    fi
    
    #libaprutil1
    check=$(dpkg -l libaprutil1 | grep "libaprutil1" | grep "1.3.9")
    if [[ "$check" =~ "libaprutil1" ]]
        then
            echo "*** libaprutil1 is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libaprutil1... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libaprutil1_1.3.9+dfsg-3build1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libaprutil1 successfully."
                exit 3
            fi
    fi
    
    #libaprutil1-dbd-sqlite3
    check=$(dpkg -l libaprutil1-dbd-sqlite3 | grep "libaprutil1-dbd-sqlite3" | grep "1.3.9")
    if [[ "$check" =~ "libaprutil1-dbd-sqlite3" ]]
        then
            echo "*** libaprutil1-dbd-sqlite3 is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libaprutil1-dbd-sqlite3... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libaprutil1-dbd-sqlite3_1.3.9+dfsg-3build1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libaprutil1-dbd-sqlite3 successfully."
                exit 3
            fi
    fi
    
    #libaprutil1-ldap
    check=$(dpkg -l libaprutil1-ldap | grep "libaprutil1-ldap" | grep "1.3.9" )
    if [[ "$check" =~ "libaprutil1-ldap" ]]
        then
            echo "*** libaprutil1-ldap is already installed ***" >> $LOGS_ERR
        else
    	    sudo dpkg -i --force-overwrite ./archives/libaprutil1-ldap_1.3.9+dfsg-3build1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libaprutil1-ldap successfully."
                exit 3
            fi
    fi
    
    #apache2.2-bin
    check=$(dpkg -l apache2.2-bin | grep "apache2.2-bin" | grep "2.2.14" )
    if [[ "$check" =~ "apache2.2-bin" ]]
        then
            echo "*** apache2.2-bin is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling apache2.2-bin... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/apache2.2-bin_2.2.14-5ubuntu8_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install apache2.2-bin successfully."
                exit 3
            fi
    fi
    
    #apache2-utils
    check=$(dpkg -l apache2-utils | grep "apache2-utils" | grep "2.2.14")
    if [[ "$check" =~ "apache2-utils" ]]
        then
            echo "*** apache2-utils is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling apache2-utils... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/apache2-utils_2.2.14-5ubuntu8_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install apache2-utils successfully."
                exit 3
            fi
    fi
    
    #apache2.2-common
    check=$(dpkg -l apache2.2-common | grep "apache2.2-common" | grep "2.2.14" )
    if [[ "$check" =~ "apache2.2-common" ]]
        then
            echo "*** apache2.2-common is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling apache2.2-common... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/apache2.2-common_2.2.14-5ubuntu8_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install apache2.2-common successfully."
                exit 3
            fi
    fi
    
    #apache2-mpm-worker
    check=$(dpkg -l apache2-mpm-worker | grep "apache2-mpm-worker" | grep "2.2.14")
    if [[ "$check" =~ "apache2-mpm-worker" ]]
        then
            echo "*** apache2-mpm-worker is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling apache2-mpm-worker... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/apache2-mpm-worker_2.2.14-5ubuntu8_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install apache2-mpm-worker successfully."
                exit 3
            fi
    fi
    
    #apache2
    check=$(dpkg -l apache2 | grep "apache2" | grep "2.2")
    if [[ "$check" =~ "apache2" ]]
        then
            echo "*** apache2 is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling apache2... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/apache2_2.2.14-5ubuntu8_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install apache2 successfully."
                exit 3
            fi
    fi
    
    #libk5crypto3
    check=$(dpkg -l libk5crypto3  | grep libk5crypto3)
    if [[ "$check" =~ "1.8.1+dfsg-2ubuntu0.2" ]]
        then
            echo "*** Intalling libk5crypto3... ***" >> $LOGS_ERR
    		sudo dpkg -i --force-overwrite ./archives/libk5crypto3_1.8.1+dfsg-2_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libk5crypto3 successfully."
                exit 3
            fi
        else
    	    echo "*** libk5crypto3 is already installed ***" >> $LOGS_ERR
    fi
    
    #libkrb5support0
    check=$(dpkg -l libkrb5support0 | grep libkrb5support0)
    if [[ "$check" =~ "1.8.1+dfsg-2ubuntu0.2" ]]
        then
            echo "*** Intalling libkrb5support0... ***" >> $LOGS_ERR
    		sudo dpkg -i --force-overwrite ./archives/libkrb5support0_1.8.1+dfsg-2_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libkrb5support0 successfully."
                exit 3
            fi
        else
    	    echo "*** libkrb5support0 is already installed ***" >> $LOGS_ERR
    fi
    
    #libkrb5-3
    check=$(dpkg -l libkrb5-3 | grep libkrb5-3)
    if [[ "$check" =~ "1.8.1+dfsg-2ubuntu0.2" ]]
        then
            echo "*** Intalling libkrb5-3... ***" >> $LOGS_ERR
            sudo dpkg -i --force-overwrite ./archives/libkrb5-3_1.8.1+dfsg-2_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libkrb5-3 successfully."
                exit 3
            fi
        else
    		echo "*** libkrb5-3 is already installed ***" >> $LOGS_ERR
    fi
    
    #libgssapi-krb5-2
    check=$(dpkg -l libgssapi-krb5-2 | grep libgssapi-krb5-2)
    if [[ "$check" =~ "1.8.1+dfsg-2ubuntu0.2" ]]
        then
            echo "*** Intalling libgssapi-krb5-2... ***" >> $LOGS_ERR
    		sudo dpkg -i --force-overwrite ./archives/libgssapi-krb5-2_1.8.1+dfsg-2_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libgssapi-krb5-2 successfully."
                exit 3
            fi
        else
    	    echo "*** libgssapi-krb5-2 is already installed ***" >> $LOGS_ERR
    fi
    
    #libldap-2.4-2
    check=$(dpkg -l libldap-2.4-2 | grep libldap-2.4-2)
    if [[ "$check" =~ "2.4.21-0ubuntu5.3" ]]
        then
            echo "*** Intalling libldap-2.4-2... ***" >> $LOGS_ERR
    		sudo dpkg -i --force-overwrite ./archives/libldap-2.4-2_2.4.21-0ubuntu5_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libldap-2.4-2 successfully."
                exit 3
            fi
        else
    	    echo "*** libldap-2.4-2 is already installed ***" >> $LOGS_ERR
    fi
    
    #libpq5
    check=$(dpkg -l libpq5 | grep libpq5)
    if [[ "$check" =~ "8.4.6-0ubuntu10.04" ]]
        then
            echo "*** Intalling libpq5... ***" >> $LOGS_ERR
    		sudo dpkg -i --force-overwrite ./archives/libpq5_8.4.3-1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libpq5 successfully."
                exit 3
            fi
        else
    	    echo "*** libpq5 is already installed ***" >> $LOGS_ERR
    fi
    
    #libldap2-dev
    check=$(dpkg -l libldap2-dev | grep "libldap2-dev" | grep "2.4.21")
    if [[ "$check" =~ "libldap2-dev" ]]
        then
            echo "*** libldap2-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libldap2-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libldap2-dev_2.4.21-0ubuntu5_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libldap2-dev successfully."
                exit 3
            fi
    fi
    
    #libexpat1-dev
    check=$(dpkg -l libexpat1-dev | grep "libexpat1-dev" | grep "2.0.1")
    if [[ "$check" =~ "libexpat1-dev" ]]
        then
            echo "*** libexpat1-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libexpat1-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libexpat1-dev_2.0.1-7ubuntu1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libexpat1-dev successfully."
                exit 3
            fi
    fi
    
    #libdb4.8-dev
    check=$(dpkg -l libdb4.8-dev | grep "libdb4.8-dev" | grep "4.8.24")
    if [[ "$check" =~ "libdb4.8-dev" ]]
        then
            echo "*** libdb4.8-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libdb4.8-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libdb4.8-dev_4.8.24-1ubuntu1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libdb4.8-dev successfully."
                exit 3
            fi
    fi
    
    #libpcrecpp0
    check=$(dpkg -l libpcrecpp0 | grep "libpcrecpp0" | grep "7.8")
    if [[ "$check" =~ "libpcrecpp0" ]]
        then
            echo "*** libpcrecpp0 is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libpcrecpp0... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libpcrecpp0_7.8-3build1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libpcrecpp0 successfully."
                exit 3
            fi
    fi
    
    #libpcre3-dev
    check=$(dpkg -l libpcre3-dev | grep "libpcre3-dev" | grep "7.8")
    if [[ "$check" =~ "libpcre3-dev" ]]
        then
            echo "*** libpcre3-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libpcre3-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libpcre3-dev_7.8-3build1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libpcre3-dev successfully."
                exit 3
            fi
    fi
    
    #uuid-dev
    check=$(dpkg -l uuid-dev | grep "uuid-dev" | grep "2.17.2")
    if [[ "$check" =~ "uuid-dev" ]]
        then
            echo "*** uuid-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling uuid-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/uuid-dev_2.17.2-0ubuntu1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install uuid-dev successfully."
                exit 3
            fi
    fi
    
    #libapr1-dev
    check=$(dpkg -l libapr1-dev | grep "libapr1-dev" | grep "1.3.8")
    if [[ "$check" =~ "libapr1-dev" ]]
        then
            echo "*** libapr1-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libapr1-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libapr1-dev_1.3.8-1build1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libapr1-dev successfully."
                exit 3
            fi
    fi
    
    #libsqlite3-dev
    check=$(dpkg -l libsqlite3-dev | grep "libsqlite3-dev" | grep "3.6.22")
    if [[ "$check" =~ "libsqlite3-dev" ]]
        then
            echo "*** libsqlite3-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libsqlite3-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libsqlite3-dev_3.6.22-1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libsqlite3-dev successfully."
                exit 3
            fi
    fi
    
    #zlib1g-dev
    check=$(dpkg -l zlib1g-dev | grep "zlib1g-dev" | grep "1.2.3.3")
    if [[ "$check" =~ "zlib1g-dev" ]]
        then
            echo "*** zlib1g-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling zlib1g-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/zlib1g-dev_1%3a1.2.3.3.dfsg-15ubuntu1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install zlib1g-dev successfully."
                exit 3
            fi
    fi
    
    #libssl-dev
    check=$(dpkg -l libssl-dev | grep "libssl-dev" | grep "0.9.8")
    if [[ "$check" =~ "libssl-dev" ]]
        then
            echo "*** libssl-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libssl-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libssl-dev_0.9.8k-7ubuntu8_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libssl-dev successfully."
                exit 3
            fi
    fi
    
    #libgssrpc4
    check=$(dpkg -l libgssrpc4 | grep "libgssrpc4" | grep "1.8.1")
    if [[ "$check" =~ "libgssrpc4" ]]
        then
            echo "*** libgssrpc4 is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libgssrpc4... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libgssrpc4_1.8.1+dfsg-2_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libgssrpc4 successfully."
                exit 3
            fi
    fi
    
    #libkdb5-4
    check=$(dpkg -l libkdb5-4 | grep "libkdb5-4" | grep "1.8.1")
    if [[ "$check" =~ "libkdb5-4" ]]
        then
            echo "*** libkdb5-4 is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libkdb5-4... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libkdb5-4_1.8.1+dfsg-2_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libkdb5-4 successfully."
                exit 3
            fi
    fi
    
    #libkadm5srv-mit7
    check=$(dpkg -l libkadm5srv-mit7 | grep "libkadm5srv-mit7" | grep "1.8.1")
    if [[ "$check" =~ "libkadm5srv-mit7" ]]
        then
            echo "*** libkadm5srv-mit7 is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libkadm5srv-mit7... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libkadm5srv-mit7_1.8.1+dfsg-2_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libkadm5srv-mit7 successfully."
                exit 3
            fi
    fi
    
    #libkadm5clnt-mit7
    check=$(dpkg -l libkadm5clnt-mit7 | grep "libkadm5clnt-mit7" | grep "1.8.1")
    if [[ "$check" =~ "libkadm5clnt-mit7" ]]
        then
            echo "*** libkadm5clnt-mit7 is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libkadm5clnt-mit7... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libkadm5clnt-mit7_1.8.1+dfsg-2_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libkadm5clnt-mit7 successfully."
                exit 3
            fi
    fi
    
    #comerr-dev
    check=$(dpkg -l comerr-dev | grep "comerr-dev" | grep "2.1")
    if [[ "$check" =~ "comerr-dev" ]]
        then
            echo "*** comerr-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling comerr-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/comerr-dev_2.1-1.41.11-1ubuntu2_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install comerr-dev successfully."
                exit 3
            fi
    fi
    
    #krb5-multidev
    check=$(dpkg -l krb5-multidev | grep "krb5-multidev" | grep "1.8.1")
    if [[ "$check" =~ "krb5-multidev" ]]
        then
            echo "*** krb5-multidev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling krb5-multidev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/krb5-multidev_1.8.1+dfsg-2_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install krb5-multidev successfully."
                exit 3
            fi
    fi
    
    #libkrb5-dev
    check=$(dpkg -l libkrb5-dev | grep "libkrb5-dev" | grep "1.8.1" )
    if [[ "$check" =~ "libkrb5-dev" ]]
        then
            echo "*** libkrb5-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libkrb5-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libkrb5-dev_1.8.1+dfsg-2_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libkrb5-dev successfully."
                exit 3
            fi
    fi
    
    #libpq-dev
    check=$(dpkg -l libpq-dev | grep "libpq-dev" | grep "8.4.3")
    if [[ "$check" =~ "libpq-dev" ]]
        then
            echo "*** libpq-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libpq-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libpq-dev_8.4.3-1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libpq-dev successfully."
                exit 3
            fi
    fi
    
    #mysql-common
    check=$(dpkg -l mysql-common | grep "mysql-common" | grep "5.1.4")
    if [[ "$check" =~ "mysql-common" ]]
        then
            echo "*** mysql-common is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling mysql-common... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/mysql-common_5.1.41-3ubuntu12_all.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install mysql-common successfully."
                exit 3
            fi
    fi
    
    #libmysqlclient16
    check=$(dpkg -l libmysqlclient16 | grep "libmysqlclient16" | grep "5.1.4")
    if [[ "$check" =~ "libmysqlclient16" ]]
        then
            echo "*** libmysqlclient16 is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libmysqlclient16... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libmysqlclient16_5.1.41-3ubuntu12_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libmysqlclient16 successfully."
                exit 3
            fi
    fi
    
    #libmysqlclient-dev
    check=$(dpkg -l libmysqlclient-dev | grep "libmysqlclient-dev" | grep "5.1.4")
    if [[ "$check" =~ "libmysqlclient-dev" ]]
        then
            echo "*** libmysqlclient-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libmysqlclient-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libmysqlclient-dev_5.1.41-3ubuntu12_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libmysqlclient-dev successfully."
                exit 3
            fi
    fi
    
    #libaprutil1-dev
    check=$(dpkg -l libaprutil1-dev | grep "libaprutil1-dev" | grep "1.3.9")
    if [[ "$check" =~ "libaprutil1-dev" ]]
        then
            echo "*** libaprutil1-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling libaprutil1-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/libaprutil1-dev_1.3.9+dfsg-3build1_i386.deb;
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install libaprutil1-dev successfully."
                exit 3
            fi
    fi
    
    #apache2-threaded-dev
    check=$(dpkg -l apache2-threaded-dev | grep "apache2-threaded-dev" | grep "2.2.14")
    if [[ "$check" =~ "apache2-threaded-dev" ]]
        then
            echo "*** apache2-threaded-dev is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling apache2-threaded-dev... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/apache2-threaded-dev_2.2.14-5ubuntu8_i386.deb
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install apache2-threaded-dev successfully."
                exit 3
            fi
    fi
    
    #pmount
    check=$(dpkg -l pmount | grep "pmount" | grep "0.")
    if [[ "$check" =~ "pmount" ]]
        then
            echo "*** pmount is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling pmount... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/pmount_0.9.20-2_i386.deb
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install pmount successfully."
                exit 3
            fi
    fi
    
    #usbmount
    check=$(dpkg -l usbmount | grep "usbmount" | grep "0.")
    if [[ "$check" =~ "usbmount" ]]
        then
            echo "*** usbmount is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling usbmount... ***" >> $LOGS_ERR
    	    sudo dpkg -i --force-overwrite ./archives/usbmount_0.0.19.1ubuntu1_all.deb
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install usbmount successfully."
                exit 3
            fi
    fi
    
    #tcl
    check=$(dpkg -l tcl8.5 | grep "tcl8.5")
    if [[ "$check" =~ "tcl8.5" ]]
        then
            echo "*** tcl8.5 is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling tcl8.5 ... ***" >> $LOGS_ERR
                sudo dpkg -i --force-overwrite ./archives/tcl8.5_8.5.8-2_i386.deb
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install tcl8.5 successfully."
                exit 3
            fi
    fi
    
    #postgresql-pltcl-8.4
    check=$(dpkg -l postgresql-pltcl-8.4 | grep "postgresql-pltcl-8.4")
    if [[ "$check" =~ "postgresql-pltcl-8.4" ]]
        then
            echo "*** postgresql-pltcl-8.4 is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling postgresql-pltcl-8.4 ... ***" >> $LOGS_ERR
                sudo dpkg -i --force-overwrite ./archives/postgresql-pltcl-8.4_8.4.3-1_i386.deb
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install postgresql-pltcl-8.4 successfully."
                exit 3
            fi
    fi
    
    #postgresql-plperl-8.4
    check=$(dpkg -l postgresql-plperl-8.4 | grep "postgresql-plperl-8.4")
    if [[ "$check" =~ "postgresql-plperl-8.4" ]]
        then
            echo "*** postgresql-plperl-8.4 is already installed ***" >> $LOGS_ERR
        else
            echo "*** Intalling postgresql-plperl-8.4 ... ***" >> $LOGS_ERR
                sudo dpkg -i --force-overwrite ./archives/postgresql-plperl-8.4_8.4.16-0_i386.deb
            if [ $? -ne 0 ]
            then
                echo "UPGRADE ERROR: EM Management upgrade failed. Could not install postgresql-plperl-8.4 successfully."
                exit 3
            fi
    fi
fi

echo "*** Upgrading EM Management... ***" >> $LOGS_ERR
sudo dpkg -i --force-overwrite ./*_em_mgmt.deb
if [ $? -eq 0 ]
then
    echo "*** EM Management upgraded successfully ***" >> $LOGS_ERR
	if [ "$FRESH_INSTALL" != "T" ]
	then
					echo "step6;"

						ps -ef | grep postgresql | grep -v grep > /dev/null
						if [ $? -eq 0 ]
						then
								if [ `psql -q -U postgres -h localhost  -t -c "select count(*) from pg_database where datname='ems'"` -eq 1 ]
								then
										UPGRADESQLPATH=~enlighted/upgradeSQL.sql
                                        echo "*** Upgrading database... ***" >> $LOGS_ERR
										DBUSER=postgres
										DBHOST=localhost
										DB=ems

                                        cp ~enlighted/upgradeSQL.sql /var/lib/tomcat6/Enlighted/tempExtract/${DEBIAN_REV_NUMBER}_upgradeSQL.sql

                                        if [ `psql -q -U postgres ems -h localhost  -t -c "select 1 from system_configuration where name = 'cloud.communicate.type' and value = '2'"` -eq 1 ] 
                                        then
                                            newid=$(/usr/bin/psql -x -U $DBUSER $DB -h $DBHOST -c "select nextval('wal_logs_seq')" | grep nextval | cut -d " " -f3)
                                            /usr/bin/psql -U $DBUSER $DB -h $DBHOST -c "insert into wal_logs (id, creation_time , action, table_name, sql_statement) values ($newid, current_timestamp, 'UPGRADE', '/var/lib/tomcat6/Enlighted/tempExtract/${DEBIAN_REV_NUMBER}_upgradeSQL.sql', '')" >> $LOGS_DB 2>> $LOGS_DB
                                        fi

										echo "startdbupgrade;"

									    psql -U $DBUSER -h $DBHOST $DB < $UPGRADESQLPATH >> $LOGS_DB 2>> $LOGS_DB

                                        if [ `psql -q -U postgres ems -h localhost  -t -c "select 1 from system_configuration where name = 'cloud.communicate.type' and value = '2'"` -eq 1 ] 
                                        then
                                            psql -U $DBUSER -h $DBHOST $DB < ~enlighted/sppa.sql >> $LOGS_DB 2>> $LOGS_DB
                                        fi
                                        
										if [ $? -eq 0 ]
                                        then
										    echo "*** Database upgrade completed successfully ***" >> $LOGS_ERR
                                        else
									        echo "UPGRADE ERROR: Database was not upgraded succesfully. Could not continue with the upgrade."
											exit 3
										fi
								else 
										echo "UPGRADE ERROR: Database was not upgraded succesfully. Could not continue with the upgrade."
										exit 3
								fi
						else
										echo "UPGRADE ERROR: database server is not running. Could not continue with the upgrade."
										exit 3
						fi

						echo "step7;"

					sleep 10
					echo "*** Restarting Apache2 server ***" >> $LOGS_ERR
					startapache=$(sudo /etc/init.d/apache2 restart)
					if [[ "$startapache" =~ "done" ]]
					then
							 echo "*** Apache2 service is up. ***" >> $LOGS_ERR
					else
							echo "UPGRADE ERROR: Failed to start Apache2 service. Contact enlighted admin."
							exit 3
					fi
	fi
else
    echo "UPGRADE ERROR: EM Management upgrade failed. Exit upgrade process and bringing up tomcat server."
    exit 3
fi
