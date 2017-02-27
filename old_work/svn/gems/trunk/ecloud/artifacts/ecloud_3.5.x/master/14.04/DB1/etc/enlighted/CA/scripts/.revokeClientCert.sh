#!/bin/sh
CA_HOME=../ssl
DOMAIN_NAME=enlightedcloud.net

if [ $# = 0 ]; then 
        echo "Please provide UUID" 
        exit 
fi 

#Revoking existing certificate
echo "Revoking $1 certificate"
openssl ca -config openssl.my.cnf -policy policy_match -passin pass:enlighted -revoke $CA_HOME/certs/$1.pem 
