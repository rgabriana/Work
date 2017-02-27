#!/bin/sh
## This file is being managed by puppet ##
CA_HOME=/etc/enlighted/CA/ssl
DOMAIN_NAME=enlightedcloud.net

if [ $# = 0 ]; then
	echo "Please provide client certificate name"
	exit
fi

#Request new client certificate
echo "Generating $1 certificate"
openssl req -new -nodes -days 7300 \
	-subj "/C=US/ST=California/L=Mountain View/O=enLighted Inc/CN=$1.$DOMAIN_NAME/emailAddress=support@$DOMAIN_NAME" \
	-keyout $CA_HOME/private/$1.key -config $CA_HOME/openssl.my.cnf -out $CA_HOME/csr/$1.csr

#sign certificate
echo "Signing $1 certificate"
openssl ca -batch -out $CA_HOME/certs/$1.pem -days 7300 -config $CA_HOME/openssl.my.cnf -infiles $CA_HOME/csr/$1.csr

#Make pkcs12 compilant
echo "Creating PKCS12 compilant certificate"
openssl pkcs12 -export -in $CA_HOME/certs/$1.pem -inkey $CA_HOME/private/$1.key -out $CA_HOME/pfx/$1.pfx

#Regenerate CRL
echo "Generating CRL"
openssl ca -config $CA_HOME/openssl.my.cnf -gencrl -out $CA_HOME/crl/enlca.crl
