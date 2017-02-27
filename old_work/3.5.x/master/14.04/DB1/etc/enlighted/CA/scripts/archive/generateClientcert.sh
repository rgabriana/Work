#!/bin/sh
. /etc/enlighted/CA/scripts/cert.config

if [ $# = 0 ]; then
	echo "Please provide client certificate name"
	exit
fi

#Request new client certificate
echo "Generating $1 certificate"
openssl req -new -nodes -days 7300 \
	-subj "/C=US/ST=California/L=Mountain View/O=enLighted Inc/CN=$1.$DOMAIN_NAME/emailAddress=support@$DOMAIN_NAME" \
	-keyout $CA_HOME/private/$1.key -config $CA_SCRIPTS/openssl.my.cnf -out $CA_HOME/csr/$1.csr

#sign certificate
echo "Signing $1 certificate"
openssl ca -batch -passin "pass:enlighted" -out $CA_HOME/certs/$1.pem -days 7300 -config $CA_SCRIPTS/openssl.my.cnf -infiles $CA_HOME/csr/$1.csr

#Make pkcs12 compliant
echo "Creating PKCS12 compliant certificate"
openssl pkcs12 -passout pass:$1 -export -in $CA_HOME/certs/$1.pem -inkey $CA_HOME/private/$1.key -out $CA_HOME/pfx/$1.pfx

#Regenerate CRL
echo "Generating CRL"
openssl ca -config $CA_SCRIPTS/openssl.my.cnf -passin "pass:enlighted" -gencrl -out $CA_HOME/crl/enlca.crl
