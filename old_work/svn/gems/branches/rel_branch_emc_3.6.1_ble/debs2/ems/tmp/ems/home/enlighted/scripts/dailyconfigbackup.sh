#!/bin/bash
source /etc/environment
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
backupdir=$OPT_ENLIGHTED/config_backup

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
        sudo mkdir -p $backupdir
fi

export LOG_DIR="$ENL_APP_HOME/Enlighted/adminlogs"
mkdir -p $LOG_DIR

echo "***STARTING NEW CONFIG BACK UP****************" >$LOG_DIR/daily_config_backup.log

macid=`/sbin/ifconfig | grep -E $network".*HWaddr" | sed 's/^.*HWaddr\ *//'`
macid=`echo $macid | sed -e 's/://g'`
t=`date +%F-%s`
echo "Current Date time is:"$t >>$LOG_DIR/daily_config_backup.log
backupfile=$backupdir/ENL_all_config_backup_$macid-$t.tar.gz
echo "backup directory is:"$backupdir": Network passed is:"$network": MacID:"$macid": BackupFileName:"$backupfile":" >>$LOG_DIR/daily_config_backup.log
arrayname=( $(for x in `find $backupdir -maxdepth 1 -name "ENL_all_config_backup_*.tar.gz" -type f`; do echo $x;done;) )


#sudo tar -cvpzf $backupfile /etc/customLogrotate.* /etc/network/interfaces /etc/timezone /etc/default/ntpdate /etc/ntp.conf /etc/iptables.rules /etc/sudoers $ENL_APP_HOME/Enlighted $ENL_APP_HOME/conf $ENL_APP_HOME/webapps/ems.war $OPT_ENLIGHTED /var/www/em_mgmt /bin/authadmin.sh /bin/checkandsetemmode.sh $ENLIGHTED_HOME/insertGateway.php $ENLIGHTED_HOME/discovergateway.sh $ENLIGHTED_HOME/InstallSQL.sql /etc/tomcat6/context.xml /etc/apache2/sites-enabled/000-default /etc/apache2/ssl /etc/apache2/mods-enabled/proxy.conf /etc/apache2/rewrite_prg.pl --exclude=$backupdir	


if [ `psql -q -U postgres -h localhost  -t -c "select count(*) from pg_database where datname='adr'"` -eq 1 ]
then
	## Logic to create the backup of adr db in the folder $OPT_ENLIGHTED/adr/DB
	adrdbdir=$OPT_ENLIGHTED/adr/DB
	if [ ! -d $adrdbdir ]
	then
	        mkdir -p $adrdbdir
	fi
	arraynameadr=( $(for x in `find $adrdbdir -maxdepth 1 -name "*" -type f`; do echo $x;done;) )
	DAILY_BACKUP_FILE=$OPT_ENLIGHTED/adr/DB/adr_$macid-$t.backup
	adrbackupfile=$OPT_ENLIGHTED/adr/DB/ADR_db_backup_$macid-$t.tar.gz
	/usr/bin/pg_dump -i -U postgres -h localhost -b -F c -f  "${DAILY_BACKUP_FILE}" adr
	tar -zcvf $adrbackupfile DAILY_BACKUP_FILE
	for element in $(seq 0 $((${#arraynameadr[@]} - 1)))
    do
                    echo " Removing adr :${arraynameadr[$element]}" >>$LOG_DIR/daily_config_backup.log
                    sudo rm -rf ${arraynameadr[$element]}
    done
    echo "Backup of adr db is sucessful. " >>$LOG_DIR/daily_config_backup.log
fi


##sudo tar -cvpzf $backupfile $OPT_ENLIGHTED/adr $OPT_ENLIGHTED/DB $OPT_ENLIGHTED/communicator /etc/customLogrotate.conf /etc/customLogrotate.* /etc/network/interfaces /etc/timezone /etc/default/ntpdate /etc/iptables.rules /etc/sudoers $ENL_APP_HOME/Enlighted /etc/apache2/sites-enabled/000-default /etc/apache2/ssl /etc/apache2/mods-enabled/proxy.conf /etc/apache2/rewrite_prg.pl --exclude=$OPT_ENLIGHTED/adminlogs --exclude=$OPT_ENLIGHTED/DB/DBBK --exclude=$OPT_ENLIGHTED/communicator/*.sh --exclude=$OPT_ENLIGHTED/communicator/*.log --exclude=$OPT_ENLIGHTED/communicator/*.jar --exclude=$ENL_APP_HOME/Enlighted/adminlogs --exclude=$ENL_APP_HOME/Enlighted/UpgradeImages --exclude=$ENL_APP_HOME/Enlighted/UpgradeImages --exclude=$ENL_APP_HOME/Enlighted/tempExtract --exclude=$ENL_APP_HOME/Enlighted/UpgradeImages/bkRestoreFolderTemp  --exclude=**/*.sh --exclude=**/*.log --exclude=**/*.sh --exclude=**/*.jar --exclude=**/*.deb   
sudo tar -cvpzf $backupfile  $OPT_ENLIGHTED/communicator /etc/network/interfaces /etc/timezone /etc/default/ntpdate /etc/ntp.conf /etc/default/isc-dhcp-server /etc/iptables.rules /etc/sudoers $ENL_APP_HOME/Enlighted --exclude=$ENL_APP_HOME_RELATIVE/Enlighted/cloudServerInfo.xml --exclude=$ENL_APP_HOME_RELATIVE/Enlighted/emsmode --exclude=$ENL_APP_HOME_RELATIVE/Enlighted/ems_log4j --exclude=$ENL_APP_HOME_RELATIVE/Enlighted/UpgradeImages --exclude=$ENL_APP_HOME_RELATIVE/Enlighted/bacnet/bacnetLighting --exclude=$ENL_APP_HOME_RELATIVE/Enlighted/tempExtract --exclude=$ENL_APP_HOME_RELATIVE/Enlighted/bkRestoreFolderTemp --exclude=opt/enLighted/DB/DBBK --exclude=$ENL_APP_HOME_RELATIVE/Enlighted/adminlogs  --exclude=opt/enLighted/communicator/syncdata --exclude=opt/enLighted/communicator/last_communication_time --exclude=**/*.sh --exclude=**/*.log --exclude=**/*.log.* --exclude=**/*.jar --exclude=**/*.deb  --exclude=**/*.pl --exclude=**/*.bin
## /etc/network/interfaces removed /etc/fstab and also /etc/groups $ENLIGHTED_HOME/.pgpass
status=`echo $?`
if [ $status -ne 0  ]
then
        echo "Backup of configuration files is not sucessful. Status of tar command is "$status >>$LOG_DIR/daily_config_backup.log
fi

for element in $(seq 0 $((${#arrayname[@]} - 1)))
do
                echo " Removing :${arrayname[$element]}" >>$LOG_DIR/daily_config_backup.log
                sudo rm -rf ${arrayname[$element]}
done

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
	sudo cp $backupfile $outputdir
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
##sudo $OPT_ENLIGHTED/DB/dailybackup.sh



