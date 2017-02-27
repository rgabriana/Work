#!/bin/bash -x
#########################
# Title: userinstall.sh	
# Purpopse: The purpose of this script is to create the user needed for the server
# Notes:
# - User accounts to be created needs to be added at the $UserAccts section of the 
#   replica_server_setup_script.config file. 
# - Users public keys are controlled at the respective folder in $SECURITY_SRC folder
#########################

set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

# Root check

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this scripts"
  exit
fi

source $src_dir$SETUP_CONFIG

for User in ${UserAccts}; do
## USER Setup ##
   if [ $(grep -s -c $User /etc/passwd) = 0 ]; then 
     echo "creating user $User"
     useradd -m -s /bin/bash -d /home/$User $User
   else
     echo "$User exists"
   fi 
## KEY Setup ##
   mkdir -pv /home/$User/.ssh
   key[0]=$SECURITY_SRC/home/$User/.ssh/authorized_keys
#   key[1]=$SECURITY_SRC/home/$User/.ssh/known_hosts
   for ((i=0;i<${#key[@]};i++)); do 
     SOURCE=${key[$i]}
     DESTINATION=`echo $SOURCE | sed -e "s!$SECURITY_SRC!!"`
     FILE=`basename $DESTINATION`
     DIR=`dirname $DESTINATION`
     if [ ! -f $DESTINATION ] || [ $(grep -s -c $(shasum $DESTINATION|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then
       $XFER --output-document=$DESTINATION $SOURCE
     fi
     chmod -v 0700 /home/$User/.ssh
     chmod -v 0600 /home/$User/.ssh/$FILE
     chown -R -v $User:$User /home/$User/.ssh
   done
done

for group in ${Ops_group}; do 
  usermod -G ops $group
done
