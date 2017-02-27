#!/bin/bash -x
set -e
source /etc/enlighted/CA/scripts/cert.config

if [ $# = 0 ]; then 
        echo "Please provide UUID" 
        exit 
fi 

#Revoking existing certificate
echo "Revoking $1 certificate"
openssl ca -config $CA_SCRIPTS/openssl.my.cnf -policy policy_match -passin pass:enlighted -revoke $CA_HOME/certs/$1.pem

xcode=`echo $?`

if [ "$xcode" = "0" ]; then 
  echo "Removing $1 keys and certs"
  find $CA_HOME -type f -name "$1*" -exec rm -vf '{}' \;

  else 

  echo "searching for $1 certs and keys"
  find $CA_HOME -type f -name "$1*" -exec rm -vi '{}' \;

fi

exit
