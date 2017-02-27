#!/bin/sh
## This file is being managed by puppet ##
CA_HOME=/etc/enlighted/CA/ssl
DOMAIN_NAME=enlightedcloud.net

if [ $# = 0 ]; then 
        echo "Please provide server UUID" 
        exit 
fi 

#Signing certificate
echo "Signing $1 certificate"
openssl ca -config $CA_HOME/openssl.my.cnf -policy policy_match -batch -out $CA_HOME/certs/$1.$DOMAIN_NAME.pem -infiles $CA_HOME/csr/$1.$DOMAIN_NAME.csr

#Make pkcs12 compilant
echo "Creating PKCS12 compilant certificate"
openssl pkcs12 -export -in $CA_HOME/certs/$1.$DOMAIN_NAME.pem -inkey $CA_HOME/private/$1.$DOMAIN_NAME.key -out $CA_HOME/pfx/$1.$DOMAIN_NAME.pfx
 
