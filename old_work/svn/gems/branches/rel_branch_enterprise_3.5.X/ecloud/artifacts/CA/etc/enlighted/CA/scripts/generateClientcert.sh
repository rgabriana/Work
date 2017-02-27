#!/bin/sh
CA_HOME=../ssl
DOMAIN_NAME=enlightedcloud.net

if [ $# = 0 ]; then
	echo "Please provide client certificate name"
	exit
fi

#Request new client certificate
echo "Generating $1 certificate"
openssl req -new -nodes -days 365 \
	-subj "/C=US/ST=California/L=Mountain View/O=enLighted Inc/CN=$1.$DOMAIN_NAME/emailAddress=support@$DOMAIN_NAME" \
	-keyout $CA_HOME/private/$1.key -config openssl.my.cnf -out $CA_HOME/csr/$1.csr

#sign certificate
echo "Signing $1 certificate"
openssl ca -batch -passin "pass:enlighted" -out $CA_HOME/certs/$1.pem -days 365 -config openssl.my.cnf -infiles $CA_HOME/csr/$1.csr

#Make pkcs12 compilant
echo "Creating PKCS12 compilant certificate"
openssl pkcs12 -passout pass:$1 -export -in $CA_HOME/certs/$1.pem -inkey $CA_HOME/private/$1.key -out $CA_HOME/pfx/$1.pfx

#Regenerate CRL
echo "Generating CRL"
openssl ca -config openssl.my.cnf -passin "pass:enlighted" -gencrl -out $CA_HOME/crl/enlca.crl
