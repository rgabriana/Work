#!/bin/sh
CA_HOME=/etc/enlighted/CA/ssl
DOMAIN_NAME=$(hostname | cut -d"." -f2- | tr -d '[[:space:]]')

if [ $# = 0 ]; then
	echo "Please provide client certificate name"
	exit
fi

#Request new client certificate
echo "Generating $1 certificate"
openssl req -new -nodes -days 7500 \
	-subj "/C=US/ST=California/L=Mountain View/O=enLighted Inc/CN=$1.$DOMAIN_NAME/emailAddress=support@$DOMAIN_NAME" \
	-keyout $CA_HOME/private/$1.key -sha256 -config $CA_HOME/openssl.my.cnf -out $CA_HOME/csr/$1.csr

#sign certificate
echo "Signing $1 certificate"
openssl ca -batch -passin "pass:enlighted" -out $CA_HOME/certs/$1.pem -days 7500 -config $CA_HOME/openssl.my.cnf -md sha256 -infiles $CA_HOME/csr/$1.csr

#Make pkcs12 compilant
echo "Creating PKCS12 compilant certificate"
openssl pkcs12 -passout pass:$1 -export -in $CA_HOME/certs/$1.pem -inkey $CA_HOME/private/$1.key -out $CA_HOME/pfx/$1.pfx

#Regenerate CRL
echo "Generating CRL"
openssl ca -config $CA_HOME/openssl.my.cnf -passin "pass:enlighted" -gencrl -out $CA_HOME/crl/enlca.crl
