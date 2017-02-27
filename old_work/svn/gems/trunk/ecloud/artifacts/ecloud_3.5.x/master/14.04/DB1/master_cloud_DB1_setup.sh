#!/bin/bash -x
#########################################################################
# Title: Set-up script for installing enlighted cloud master server	#
# usage:								#
# bash -xc "$(wget -O - -q --user=<repo_username> --password=<repo_password> https://pl3.projectlocker.com/enlightedinc/Gems/svn/gems/trunk/ecloud/artifacts/ecloud_3.5.x/master/14.04/DB1/master_cloud_DB1_setup.sh)"
#########################################################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

# Root check

if [ `id -u` != "0" ]; then
 echo "you need root priveleges to execute this scripts"
 exit
fi

# Environment variables

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

export SRC_LOG=/tmp/master_setup.log
export src_dir=/tmp
export XFER="wget -O - --timestamping --debug --append-output=$SRC_LOG --inet4-only --user=$username --password=$password"
export MASTER_OS_SRC=https://pl3.projectlocker.com/enlightedinc/Gems/svn/gems/trunk/ecloud/artifacts/ecloud_3.5.x/master/14.04/DB1
export SECURITY_SRC=https://pl3.projectlocker.com/enlightedinc/Gems/svn/gems/trunk/ecloud/artifacts/tools/security
export SUMFILE=$src_dir/DB1_3.5.x_14.04.sum
## Download the checksum file

$XFER --output-document=$src_dir/DB1_3.5.x_14.04.sum $MASTER_OS_SRC/DB1_3.5.x_14.04.sum

## verify the config files are present and that it's the most recent version.

if [ ! -f master_cloud_DB1_setup.config ] || [ $(grep -s -c $(shasum $src_dir/master_cloud_DB1_setup.config|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
  $XFER --output-document=$src_dir/master_cloud_DB1_setup.config $MASTER_OS_SRC/master_cloud_DB1_setup.config
fi

source $src_dir/master_cloud_DB1_setup.config

rm -vf $src_dir/*.log
# Check for disk partitions

if [ $(mount | grep --no-messages --count --word-regexp '/var/lib/postgresql') = 1 ]; then 
  echo "un-mounting /var/lib/postgresql"
  umount /var/lib/postgresql
fi

if [ $(dpkg -s postgresql 2> /dev/null|grep -c -s 'Status: install ok installed') = 1 ]; then 
  echo "Please uninstall the following packages before procedding:"
  echo "$(dpkg -l postgresql*)"
  echo "Thank you"
  exit
fi

if [ $(lshw -class disk|grep --no-messages --count --word-regexp "logical name: /dev/sdb") = 1 ]; then 

    if [ $(lvscan | grep --no-messages --count --word-regexp '/dev/psql/psql') = 1 ]; then 
      echo "removing psql logical volume"
      lvremove --force --verbose /dev/psql/psql
    fi

    if [ $(vgscan|grep --no-messages --word-regexp --count 'psql') = 1 ]; then 
      echo "removing existing psql volume groups"
      vgremove --force --force --verbose psql
    fi

    if [ $(pvscan|grep --no-messages --word-regexp --count 'PV /dev/sdb1') = 1 ]; then 
      echo "Wiping existing physical volumes"
      pvremove --force --force --yes --verbose /dev/sdb1
    fi

  echo -e 'd\nn\n\n\n\n\nw'|fdisk /dev/sdb | partprobe 
  sleep 5s

  echo "Setting Up LVMs"
  pvcreate --force --force --yes --verbose /dev/sdb1
  vgcreate --verbose --yes --force --force psql /dev/sdb1
  lvcreate --extents 100%FREE --name psql --zero n psql
  sleep 5s 
  mkfs.ext4 /dev/psql/psql

  if [ ! -d /var/lib/postgresql ]; then 
    mkdir -pv /var/lib/postgresql
  fi

  mount /dev/psql/psql /var/lib/postgresql
  echo -e "/dev/mapper/psql-psql\t/var/lib/postgresql\text4\terrors=remount-ro\t0\t1" >> /etc/fstab
else 

  echo "System cannot find second physical drive for the postgres installation.  Please verify the second partition is available"
  exit

fi 



# Verify the install scripts are executable and that they are at the latest version

echo "checking for install scripts"

for((i=0;i<${#INSTALL_SCRIPT[@]};i++))
do 
  if [ ! -x $src_dir/${INSTALL_SCRIPT[$i]} ] || [ $(grep -s -c $(shasum $src_dir/${INSTALL_SCRIPT[$i]}|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then 
    echo "${INSTALL_SCRIPT[$i]} file is missing, is not executable or wrong version.  Gathering latest ${INSTALL_SCRIPT[$i]} from repo please wait."
    $XFER --output-document=$src_dir/${INSTALL_SCRIPT[$i]}\
 $MASTER_OS_SRC/${INSTALL_SCRIPT[$i]}
    chmod -v 0755 $src_dir/${INSTALL_SCRIPT[$i]}
  else
    echo "Found ${INSTALL_SCRIPT[$i]} file"
  fi 
done

# Start installing

for ((i=0;i<${#INSTALL_SCRIPT[@]};i++))
do
  echo "executing ${INSTALL_SCRIPT[$i]}" 
  bash -x $src_dir/${INSTALL_SCRIPT[$i]} >> $src_dir/${INSTALL_SCRIPT[$i]}.log 2>&1
done

apt-get --assume-yes autoclean
apt-get --assume-yes autoremove

service auditd restart

echo "Master DB1 installation complete.  Don't forget to restore the entire EMS cloud database from backup."
