#!/bin/bash -x
set -e
source /etc/enlighted/CA/scripts/cert.config

if [ $# = 0 ]; then 
        echo "Please provide UUID" 
        exit 
fi 

#Revoking existing certificate
echo "Revoking $1 certificate"
openssl ca -config $CA_SCRIPTS/openssl.my.cnf -policy policy_match -passin pass:enlighted -revoke $CA_HOME/certs/$1.$DOMAIN_NAME.pem 
