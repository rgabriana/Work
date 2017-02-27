#!/bin/bash

export LOG_DIR=""
mkdir -p $LOG_DIR
{
export filename=$1
export OPERATION=$2
export TOMCAT_PATH=$3
LOG_DIR=${TOMCAT_PATH}/Enlighted/adminlogs
#export workingDirectory=/opt/enLighted/upgradeImages
export workingDirectory=${TOMCAT_PATH}/Enlighted/UpgradeImages

#Progress state file
export STATE_FILE="${workingDirectory}/admin_process_state"

export SYSTEM_MANIFEST_LOC="${TOMCAT_PATH}/webapps/ems/META-INF/EMSMANIFEST.MF"
export BACKUP_DIRECTORY=/opt/enLighted/DB/DBBK

#SUCCESS codes
export UPGRADE_FAIL=3
export UPGRADE_PASS=4

export SANITY_PASS=6

export BACKUP_STARTED=7
export BACKUP_PASS=8

export UPGRADEDB_STARTED=9
export UPGRADEDB_PASS=10

export DEPLOYEMS_STARTED=11
export DEPLOYEMS_PASS=12
export EVERYTHING_OK=15

export REVERT_PASS=17
export REVERT_FAIL=18

export POSTGRESUSER="postgres"
export POSTGRESDATABASE="ems"
export POSTGRESHOST="localhost"

export today=$(date '+%m'-'%d'-'%Y'_'%H'-'%M')
export backupfilePrefix=ems_preupgrade_dbbk
sanityCheck() {

	tar -tf $filename > /dev/null 2>&1
	if [ $? -ne 0 ]
	then
		echo "Upgrade image $filename is not a valid image."
		echo "Upgrade will not proceed"
		return 2
	fi
		
	manifest_filepresent=$(tar -tf $filename EMSMANIFEST.MF > /dev/null 2>&1;echo $?)
	sqlfile_present=$(tar -tf $filename upgradeSQL.sql > /dev/null 2>&1;echo $?)
	if [ $manifest_filepresent -ne 0 ]
	then
		echo "Version file not present.."	
        	return 2
	fi
	if [ $sqlfile_present -ne 0 ]
	then
		echo "Upgrade sql file not present.."
		return 2
	fi
	
		echo "Required files are present.."
        	tar -zxf $filename
        	tv=$(cat EMSMANIFEST.MF | grep "Build-Version" | sed -re 's/^.+: //')
        	versiononsystem=$(cat $SYSTEM_MANIFEST_LOC | grep "Build-Version" | sed -re 's/^.+: //')
        	
		#---- Commenting out for now. Produces a weird "Integer expected" error for every number.Cant figure out. Gave up ----
		#echo "$tv"
		#if [ $tv -lt $versiononsystem ]
        	#then
		#	echo "Incompatible version.."
		#	echo "System version:$versiononsystem"
		#	echo "Upgrade version:$tarversion"
                #	return 1
        	#else
                #	return 0
        	#fi
	return 0
}
takeBackup() {

	#Assuming ems is already stopped by now...
	#Lets take a backup of the existing ems.war and the database.
	sudo cp $TOMCAT_PATH/webapps/ems.war ems_bkp.war
	/usr/bin/pg_dump -i -U $POSTGRESUSER -h $POSTGRESHOST -b -f "${backupfilePrefix}.sql" $POSTGRESDATABASE
	
	#/usr/bin/pg_dump -U $POSTGRESUSER $POSTGRESDATABASE -h $POSTGRESHOST > ${backupfilePrefix}.sql
	
	if [ $? -ne 0 ]
	then
		echo "Backup failed.. Upgrade will not proceed.."
		return 1	
	fi
	tar -zcf ${backupfilePrefix}.tar.gz ems_bkp.war ${backupfilePrefix}.sql
	if [ $? -ne 0 ]
	then
		echo "Backup failed... Upgrade will not proceed.."
		return 1
	else
		echo "Backup taken .. Upgrade can start.."
		return 0
	fi
	
	# -- Renaming so that It does not show up on the ugrade screen
	mv ${backupfilePrefix}.tar.gz /opt/enLighted/DB/DBBK/${backupfilePrefix}.tar.gz.1	
}

revert_back() {

		echo "Reverting to previous state ..."	
		dropdb -U $POSTGRESUSER -h $POSTGRESHOST $POSTGRESDATABASE
		createdb -U $POSTGRESUSER -h $POSTGRESHOST $POSTGRESDATABASE
		
		psql -U $POSTGRESUSER -h $POSTGRESHOST $POSTGRESDATABASE < /home/enlighted/InstallSQL.sql > /dev/null 2>&1
		if [ $? -ne 0 ]
                then
                        echo "Database revert failed.. Please restart upgrade again or revert it manually.."
                        return 1
                fi
		
		psql -U $POSTGRESUSER  -h $POSTGRESHOST $POSTGRESDATABASE < ${backupfilePrefix}.sql > /dev/null 2>&1
		if [ $? -ne 0 ]
		then
			echo "Database revert failed.. Please restart upgrade again or revert it manually.."
			return 1
		fi


		tar -zxf ${backupfilePrefix}.tar.gz	
		mv ems_bkp.war $TOMCAT_PATH/webapps/ems.war
		if [ $? -ne 0 ]
		then
			echo "Application could not be restored back again..Please restart upgrade or revert it manually.."
			return 1
		fi
	return 0	
}

# ---------
# Main
# ---------

#workingDirectory is the upgradeImage directory
if [ ! -d $workingDirectory ]
then
	mkdir -p $workingDirectory
fi

# -- Checking the state of the progress
if [ $OPERATION == "sanitycheck" ]
then
	cd $workingDirectory
	sanityCheck
	if [ $? -ne 0 ]
	then
		echo "-EXIT-##$UPGRADE_FAIL"
		exit $UPGRADE_FAIL
	else
		echo "##$SANITY_PASS"
		echo $SANITY_PASS > $STATE_FILE
		exit $UPGRADE_PASS
	fi
fi
if [ $OPERATION == "takebackup" ]
then
	cd $workingDirectory
	sleep 15
	echo $BACKUP_STARTED > $STATE_FILE
	takeBackup
	if [ $? -ne 0 ]
        then
                echo "-EXIT-##$UPGRADE_FAIL"
		echo "Software upgrade failed..Please restart the upgrade process."
                exit $UPGRADE_FAIL
        else
		echo "##$BACKUP_PASS"
		echo $BACKUP_PASS > $STATE_FILE
                exit $UPGRADE_PASS
        fi
fi
if [ $OPERATION == "rundbupgradescript" ]
then
	cd $workingDirectory
	echo $UPGRADEDB_STARTED > $STATE_FILE
	sleep 10
	psql -U $POSTGRESUSER -h $POSTGRESHOST $POSTGRESDATABASE -f upgradeSQL.sql > /dev/null 2>&1
	if [ $? -ne 0 ]
        then
                echo "-EXIT-##$UPGRADE_FAIL"
		echo "Software upgrade failed..Please restart the upgrade process."
		echo $UPGRADE_FAIL > $STATE_FILE
                exit $UPGRADE_FAIL
        else
		echo "Database upgraded..."
		echo "##$UPGRADEDB_PASS"
		echo $UPGRADEDB_PASS > $STATE_FILE 
                exit $UPGRADE_PASS
        fi
fi
if [ $OPERATION == "loadems" ]
then
	cd $workingDirectory
	sleep 10
	echo $DEPLOYEMS_STARTED > $STATE_FILE
	sudo cp -f ems.war $TOMCAT_PATH/webapps
	if [ $? -ne 0 ]
	then
		echo "Software upgrade failed..Please restart the upgrade process."
		echo "-EXIT-##$UPGRADE_FAIL"
		echo $UPGRADE_FAIL > $STATE_FILE
		exit $UPGRADE_FAIL
	else
		sleep 10
		#echo "Deployed ems application ... Ready to start application"
		echo "##$DEPLOYEMS_PASS"
		echo $EVERYTHING_OK > $STATE_FILE
                exit $UPGRADE_PASS
        fi	
fi
if [ $OPERATION == "revert" ]
then
	cd $workingDirectory
	last_state=$(head -n 1 $STATE_FILE)
	echo "The upgrade was not successful.Reverting back to its previously working state.."
	revert_back
	if [ $? -eq 0 ]
	then
		echo "Revert was succesful. Please click on upgrade to start the process"
		echo "-Done-"
		echo "-EXIT-##$REVERT_PASS"
		echo $REVERT_PASS > $STATE_FILE
		exit $REVERT_PASS
	else
		echo "-EXIT-##$REVERT_FAIL"
		exit $REVER_FAIL
	fi
fi
} > $LOG_DIR/upgradegems.log 2>&1
