#!/bin/bash -x
set -e

source /etc/enlighted/CA/scripts/cert.config

if [ $# = 0 ]; then 
        echo "Please provide server UUID" 
        exit 
fi 

#Signing certificate
echo "Signing $1 certificate"
openssl ca -config $CA_SCRIPTS/openssl.my.cnf -policy policy_match -passin pass:enlighted -batch -out $CA_HOME/certs/$1.$DOMAIN_NAME.pem -infiles $CA_HOME/csr/$1.$DOMAIN_NAME.csr

#Make pkcs12 compliant
echo "Creating PKCS12 compliant certificate"
openssl pkcs12 -passout pass:$1 -export -in $CA_HOME/certs/$1.$DOMAIN_NAME.pem -inkey $CA_HOME/private/$1.$DOMAIN_NAME.key -out $CA_HOME/pfx/$1.$DOMAIN_NAME.pfx
 
