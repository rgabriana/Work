#!/bin/sh
CA_HOME=/etc/enlighted/CA/ssl

if [ $# = 0 ]; then 
        echo "Please provide client certificate name" 
        exit 
fi 

#Revoking existing certificate
echo "Revoking $1 certificate"
openssl ca -config $CA_HOME/openssl.my.cnf -policy policy_match -passin pass:enlighted -revoke $CA_HOME/certs/$1.pem

$CA_HOME/../scripts/generateCRL.sh 
