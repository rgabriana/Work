#!/bin/bash -x 
#################################################
# Title: replica_server_setup_script.sh		
# Purpose: This is script is written to be	
# executed on a base OS of the REPLICA server	
# Still needs to be done:		
# -Need figure out how to implement network 
#  information for different replica
#################################################
set -e 
# $Header$

export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# Root Check 

if [ `id -u` != 0 ]; then 
	echo "You need admin priveleges to run this script"
	exit
fi 

# Environment Variables 

while getopts :u:p: opt; do 
  case $opt in 
        u) username=$OPTARG
        ;;
        p) password=$OPTARG
        ;;
        *) :
        ;;
  esac
done

if [ -z "$*" ]; then
        read -p "please enter your username for the source server " username
        read -p "please enter your password for the source server " password
fi

export src_dir=/tmp
export SRC_LOG=/tmp/master_setup.log
export XFER="wget --timestamping --verbose --debug --append-output=$SRC_LOG --inet4-only --user=$username --password=$password"
export REPLICA_OS_SRC=https://pl3.projectlocker.com/enlightedinc/Gems/svn/gems/trunk/ecloud/artifacts/ecloud_3.5.x/replica/14.04
export SECURITY_SRC=https://pl3.projectlocker.com/enlightedinc/Gems/svn/gems/trunk/ecloud/artifacts/tools/security
export SETUP_CONFIG=replica_server_setup_script.config
export SUMFILE=REPLICA_3.5.x_14.04.sum 



#Download the checksum file

$XFER --output-document=$src_dir/$SUMFILE $REPLICA_OS_SRC/$SUMFILE

## verify the config files are present and that it's the most recent version.

if [ ! -f $src_dir/$SETUP_CONFIG ] || [ $(grep -s -c $(shasum $src_dir/$SETUP_CONFIG|awk -F " " '{print $1}') $src_dir/$SUMFILE) = 0 ]; then
	$XFER --output-document=$src_dir/$SETUP_CONFIG $REPLICA_OS_SRC/$SETUP_CONFIG
fi

source $src_dir/$SETUP_CONFIG

rm -vf $src_dir/*.log

# Check for disk partitions

## Database Partition ##

if [ $(lshw -class disk | grep --no-messages --count --word-regexp 'logical name: /dev/sdb') = 1 ]; then 
## Check mount ##
	if [ $(mount | grep --no-messages --count --word-regexp '/var/lib/postgresql') = 1 ]; then 
		echo "unmounting database parttion" 
		umount /var/lib/postgresql
	fi 
## Check Logical Volumes ##
	if [ $(lvscan | grep --no-messages --count --word-regexp '/dev/psql/psql') = 1 ]; then 
		echo "removing database logical volume"
		lvremove --force --verbose /dev/psql/psql
	fi
## Check Volume Groups ##
	if [ $(vgscan | grep --no-messages --count --word-regexp 'psql') = 1 ]; then 
		echo "removing database volume groups"
		vgremove --force --force --verbose psql 
	fi
## Check Physical Volumes ##
	if [ $(pvscan | grep --no-messages --count --word-regexp ' PV /dev/sdb1') = 1 ]; then
		echo "wiping database physical volumes"
		pvremove --force --force --yes --verbose /dev/sdb1
	fi
## FDISK ##
	echo -e 'd\nn\n\n\n\n\nw' | fdisk /dev/sdb | partprobe
	sleep 5s
## Create LVM ##
	echo "Creating database LVM"
	pvcreate --force --force --yes --verbose /dev/sdb1
	vgcreate --verbose --yes --force --force psql /dev/sdb1
	lvcreate --extents 100%FREE --name psql --zero n psql
	sleep 5s
## EXT4 ##
	mkfs.ext4 /dev/psql/psql
## MOUNT ##
	if [ ! -d /var/lib/postgresql ]; then
		echo "Creating database folder"
		mkdir -pv /var/lib/postgresql
	fi

	mount /dev/psql/psql /var/lib/postgresql

else 

	echo "WARNING: system cannot find database partition.  Please be sure you have the partition available." 
	#exit
fi

## Backup Partition ##

if [ $(lshw -class disk | grep --no-messages --count --word-regexp 'logical name: /dev/sdc') = 1 ]; then 
## Check mount ##
	if [ $(mount | grep --no-messages --count --word-regexp '/home/enlighted/backups') = 1 ]; then 
		echo "unmounting backup partitions"
		umount /home/enlighted/backups
	fi
## Check Logical Volume ##
	if [ $(lvscan | grep --no-messages --count --word-regexp '/dev/backups/backups') = 1 ]; then 
        	echo "removing backups logical volume"
                lvremove --force --verbose /dev/backups/backups
	fi
## Check Volume group ##
        if [ $(vgscan | grep --no-messages --count --word-regexp 'backups') = 1 ]; then
                echo "removing backups volume groups"
                vgremove --force --force --verbose backups
        fi
## Check Physical Volume ##
        if [ $(pvscan | grep --no-messages --count --word-regexp ' PV /dev/sdc1') = 1 ]; then
                echo "wiping backups physical volumes"
                pvremove --force --force --yes --verbose /dev/sdc1
        fi
## FDISK ##
	echo -e 'd\nn\n\n\n\n\nw' | fdisk /dev/sdc | partprobe
	sleep 5s 
## Create LVM ##
	echo "Setting Up LVMs"
	pvcreate --force --force --yes --verbose /dev/sdc1
	vgcreate --verbose --yes --force --force backups /dev/sdc1
	lvcreate --extents 100%FREE --name backups --zero n backups
	sleep 5s
## EXT4 ##
	mkfs.ext4 /dev/backups/backups
## MOUNT ##
	if [ ! -d /home/enlighted/backups ]; then 
		echo "Creating backups folder"
		mkdir -pv /home/enlighted/backups
	fi

	mount /dev/backups/backups /home/enlighted/backups

else

	echo "WARNING: System cannot find third physical drive for the postgres installation.  Please verify the second partition is available"

fi

## Script Check ##
for((i=0;i<${#INSTALL_SCRIPT[@]};i++)); do 
SOURCE=${INSTALL_SCRIPT[$i]}
FILE=$SOURCE
	if [ ! -f $src_dir/$FILE ] || [ $(grep -s -c $(shasum $src_dir/$FILE|awk -F " " '{print $1}') $src_dir/$SUMFILE) = 0 ]; then 

		echo "$FILE is missing or out of date. Updating $FILE"
		$XFER --output-document=$src_dir/$FILE $REPLICA_OS_SRC/$FILE
	else
		echo "$FILE is up to date"
	fi 

done

## Execute scripts

for((i=0;i<${#INSTALL_SCRIPT[@]};i++)); do 
SOURCE=${INSTALL_SCRIPT[$i]}
FILE=$SOURCE
LOG_DIR=/var/log/enlighted/SETUP

	if [ ! -d $LOG_DIR ]; then
		echo "Creating $LOG_DIR" 
		mkdir -pv $LOG_DIR
		chmod 0755 $LOG_DIR
	fi

	echo "Executing $FILE"
	bash -x $src_dir/$FILE | tee $LOG_DIR/$FILE.log
done

echo "Replica Server installation complete."
