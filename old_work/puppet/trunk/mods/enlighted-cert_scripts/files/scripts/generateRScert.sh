#!/bin/sh
## This file is being managed by puppet ##
CA_HOME=/etc/enlighted/CA/ssl
DOMAIN_NAME=enlightedcloud.net

if [ $# = 0 ]; then 
        echo "Please provide server UUID" 
        exit 
fi 

#Request new certificate
echo "Generating $1 certificate"
openssl req -config $CA_HOME/openssl.my.cnf -new -nodes -keyout $CA_HOME/private/$1.$DOMAIN_NAME.key -out $CA_HOME/csr/$1.$DOMAIN_NAME.csr -days 7300 \
	-subj "/C=US/ST=California/L=Mountain View/O=enLighted Inc/CN=$1.$DOMAIN_NAME/emailAddress=support@$DOMAIN_NAME/subjectAltName=DNS:$1.$DOMAIN_NAME"

#Signing certificate
echo "Signing $1 certificate"
openssl ca -config $CA_HOME/openssl.my.cnf -policy policy_match -batch -out $CA_HOME/certs/$1.$DOMAIN_NAME.pem -infiles $CA_HOME/csr/$1.$DOMAIN_NAME.csr

#Make pkcs12 compilant
echo "Creating PKCS12 compilant certificate"
openssl pkcs12 -export -in $CA_HOME/certs/$1.$DOMAIN_NAME.pem -inkey $CA_HOME/private/$1.$DOMAIN_NAME.key -out $CA_HOME/pfx/$1.$DOMAIN_NAME.pfx
 
#Regenerate CRL
echo "Generating CRL"
openssl ca -config $CA_HOME/openssl.my.cnf -gencrl -out $CA_HOME/crl/enlca.crl

