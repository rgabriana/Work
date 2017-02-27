#!/bin/bash

#######################  SCRIPT TO RUN AT SOURCE TO GENERATE A TAR FILE #####################################

arg=0
network=''
if [ -z "$1" ]
then
        #arg=1
	network=`echo $(psql -q -U postgres -d ems -h localhost -p 5433 -t -c"select interface_name from network_interface_mapping n, network_types nt, network_settings ns where n.network_type_id=nt.id and n.network_settings_id=ns.id and nt.name='Corporate'" sed 's,^ *,,; s, *$,,')`

else
	network=$1
fi
backupdir=/opt/enLighted/config_backup

if [ ! -z "$2" ]
then
        backupdir=$2
fi

if [ $arg -gt 0 ]
  then
                echo "Arguments not proper.."
                echo "Please specify arguments in the fashion : network_interface_name"
                echo "create_config_backup.sh eth0"
                echo "Exiting Now.."
                exit 1
fi
if [ ! -d $backupdir ]
then
        mkdir -p $backupdir
fi

export LOG_DIR="/var/lib/tomcat6/Enlighted/adminlogs"
mkdir -p $LOG_DIR

echo "***STARTING NEW CONFIG BACK UP****************" >$LOG_DIR/daily_config_backup.log

macid=`ifconfig | grep -E $network".*HWaddr" | sed 's/^.*HWaddr\ *//'`
macid=`echo $macid | sed -e 's/://g'`
t=`date +%F-%s`
echo "Current Date time is:"$t >>$LOG_DIR/daily_config_backup.log
backupfile=$backupdir/ENL_all_config_backup_$macid-$t.tar.gz
echo "backup directory is:"$backupdir": Network passed is:"$network": MacID:"$macid": BackupFileName:"$backupfile":" >>$LOG_DIR/daily_config_backup.log
arrayname=( $(for x in `find $backupdir -maxdepth 1 -name "ENL_all_config_backup_*.tar.gz" -type f`; do echo $x;done;) )


#sudo tar -cvpzf $backupfile /etc/customLogrotate.* /etc/network/interfaces /etc/timezone /etc/default/ntpdate /etc/iptables.rules /etc/sudoers /var/lib/tomcat6/Enlighted /var/lib/tomcat6/conf /var/lib/tomcat6/webapps/ems.war /opt/enLighted /var/www/em_mgmt /bin/authadmin.sh /bin/checkandsetemmode.sh /home/enlighted/insertGateway.php /home/enlighted/discovergateway.sh /home/enlighted/InstallSQL.sql /etc/tomcat6/context.xml /etc/apache2/sites-enabled/000-default /etc/apache2/ssl /etc/apache2/mods-enabled/proxy.conf /etc/apache2/rewrite_prg.pl --exclude=$backupdir	


if [ `psql -q -U postgres -h localhost  -t -c "select count(*) from pg_database where datname='adr'"` -eq 1 ]
then
	## Logic to create the backup of adr db in the folder /opt/enLighted/adr/DB
	adrdbdir=/opt/enLighted/adr/DB
	if [ ! -d $adrdbdir ]
	then
	        mkdir -p $adrdbdir
	fi
	arraynameadr=( $(for x in `find $adrdbdir -maxdepth 1 -name "*" -type f`; do echo $x;done;) )
	DAILY_BACKUP_FILE=/opt/enLighted/adr/DB/adr_$macid-$t.backup
	adrbackupfile=/opt/enLighted/adr/DB/ADR_db_backup_$macid-$t.tar.gz
	/usr/bin/pg_dump -i -U postgres -h localhost -b -F c -f  "${DAILY_BACKUP_FILE}" adr
	tar -zcvf $adrbackupfile DAILY_BACKUP_FILE
	for element in $(seq 0 $((${#arraynameadr[@]} - 1)))
    do
                    echo " Removing adr :${arraynameadr[$element]}" >>$LOG_DIR/daily_config_backup.log
                    sudo rm -rf ${arraynameadr[$element]}
    done
    echo "Backup of adr db is sucessful. " >>$LOG_DIR/daily_config_backup.log
fi


##sudo tar -cvpzf $backupfile /opt/enLighted/adr /opt/enLighted/DB /opt/enLighted/communicator /etc/customLogrotate.conf /etc/customLogrotate.* /etc/network/interfaces /etc/timezone /etc/default/ntpdate /etc/iptables.rules /etc/sudoers /var/lib/tomcat6/Enlighted /etc/apache2/sites-enabled/000-default /etc/apache2/ssl /etc/apache2/mods-enabled/proxy.conf /etc/apache2/rewrite_prg.pl --exclude=/opt/enLighted/adminlogs --exclude=/opt/enLighted/DB/DBBK --exclude=/opt/enLighted/communicator/*.sh --exclude=/opt/enLighted/communicator/*.log --exclude=/opt/enLighted/communicator/*.jar --exclude=/var/lib/tomcat6/Enlighted/adminlogs --exclude=/var/lib/tomcat6/Enlighted/UpgradeImages --exclude=/var/lib/tomcat6/Enlighted/UpgradeImages --exclude=/var/lib/tomcat6/Enlighted/tempExtract --exclude=/var/lib/tomcat6/Enlighted/UpgradeImages/bkRestoreFolderTemp  --exclude=**/*.sh --exclude=**/*.log --exclude=**/*.sh --exclude=**/*.jar --exclude=**/*.deb   
sudo tar -cvpzf $backupfile /opt/enLighted/adr /opt/enLighted/DB /opt/enLighted/communicator /etc/customLogrotate.conf /etc/customLogrotate.* /etc/network/interfaces /etc/timezone /etc/default/ntpdate /etc/iptables.rules /etc/sudoers /var/lib/tomcat6/Enlighted /etc/apache2/sites-enabled/000-default /etc/apache2/ssl /etc/apache2/mods-enabled/proxy.conf /etc/apache2/rewrite_prg.pl --exclude=var/lib/tomcat6/Enlighted/tempExtract --exclude=var/lib/tomcat6/Enlighted/bkRestoreFolderTemp --exclude=opt/enLighted/DB/DBBK --exclude=var/lib/tomcat6/Enlighted/adminlogs  --exclude=**/*.sh --exclude=**/*.log --exclude=**/*.jar --exclude=**/*.deb  --exclude=**/*.pl --exclude=**/*.bin
## /etc/network/interfaces removed /etc/fstab and also /etc/groups /home/enlighted/.pgpass
status=`echo $?`
if [ $status -eq 0  ]
then
        for element in $(seq 0 $((${#arrayname[@]} - 1)))
        do
                        echo " Removing :${arrayname[$element]}" >>$LOG_DIR/daily_config_backup.log
                        sudo rm -rf ${arrayname[$element]}
        done
else
        echo "Backup of configuration files is not sucessful. Status of tar command is "$status >>$LOG_DIR/daily_config_backup.log
fi


####Check whether the backup file should go to media or should remain in the directory only
USBMEDIA=$(df -k | grep media | tr -s ' ' ' '| cut -f6 -d' ' | head -1)
SPACE_AVAILABLE=$(df -B 1 | grep "${USBMEDIA}" | tr -s ' ' ' ' | cut -d" " -f4)
BACKUP_FILE_SIZE=$(ls -l "${backupfile}"  | tr -s ' ' ' ' | cut -d" " -f5)

outputdir=""

#checking for USBMEDIA. if dbdbackup exists with in USB media then take back up there, else create the folder and then backup 
if [ -d "${USBMEDIA}" ]
then
	echo "*****USB media: ${USBMEDIA} found with free space of ${SPACE_AVAILABLE}" >>$LOG_DIR/daily_config_backup.log
	echo "*****Backup file size  ${BACKUP_FILE_SIZE}" >>$LOG_DIR/daily_config_backup.log
	echo "" >>$LOG_DIR/daily_config_backup.log
	mkdir -p "${USBMEDIA}/config_backup"
	outputdir="${USBMEDIA}/config_backup"    
fi

if [ -d "${outputdir}" ]
then
	arraynameUSB=( $(for x in `find $outputdir -maxdepth 1 -name "ENL_all_config_backup_$macid*.tar.gz" -type f`; do echo $x;done;) )
	cp $backupfile $outputdir
	for element in $(seq 0 $((${#arraynameUSB[@]} - 1)))
	do
	     echo " Removing :${arraynameUSB[$element]}" >>$LOG_DIR/daily_config_backup.log
	     sudo rm -rf ${arraynameUSB[$element]}
	done
	echo "done backing up the configuration file:"$outputdir/ENL_all_config_backup_$macid-$t.tar.gz":" >>$LOG_DIR/daily_config_backup.log
else
	echo "done backing up the configuration file:"$backupfile":" >>$LOG_DIR/daily_config_backup.log
fi

###Comment out following line which is added to take the db backup with this.
##sudo /opt/enLighted/DB/dailybackup.sh



