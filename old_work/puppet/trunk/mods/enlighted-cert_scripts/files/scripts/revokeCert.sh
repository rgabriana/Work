#!/bin/sh
## This file is being managed by puppet ##
CA_HOME=/etc/enlighted/CA/ssl
DOMAIN_NAME=enlightedcloud.net

if [ $# = 0 ]; then 
        echo "Please provide UUID" 
        exit 
fi 

#Revoking existing certificate
echo "Revoking $1 certificate"
openssl ca -config $CA_HOME/openssl.my.cnf -policy policy_match -revoke $CA_HOME/certs/$1.$DOMAIN_NAME.pem 
