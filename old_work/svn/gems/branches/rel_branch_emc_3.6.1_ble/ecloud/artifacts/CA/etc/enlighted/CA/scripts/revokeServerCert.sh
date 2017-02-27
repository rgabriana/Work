#!/bin/sh
CA_HOME=/etc/enlighted/CA/ssl
DOMAIN_NAME=$(hostname | cut -d"." -f2- | tr -d '[[:space:]]')

if [ $# = 0 ]; then 
        echo "Please provide UUID" 
        exit 
fi 

#Revoking existing certificate
echo "Revoking $1 certificate"
openssl ca -config $CA_HOME/openssl.my.cnf -policy policy_match -passin pass:enlighted -revoke $CA_HOME/certs/$1.$DOMAIN_NAME.pem 

$CA_HOME/../scripts/generateCRL.sh
