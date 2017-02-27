#!/bin/bash -x
set -e 

source /etc/enlighted/CA/scripts/cert.config

openssl req -passout pass:enlighted -config $CA_SCRIPTS/openssl.my.cnf -new -x509 -extensions v3_ca -keyout $CA_HOME/private/enlca.key -out $CA_HOME/certs/enlca.crt -days 9125 \
        -subj "/C=US/ST=California/L=Mountain View/O=enLighted Inc/CN=www.$DOMAIN_NAME/emailAddress=support@$DOMAIN_NAME/subjectAltName=DNS:www.$DOMAIN_NAME"

keytool -import -file $CA_HOME/certs/enlca.crt -alias ENLCA -keystore $CA_HOME/pfx/enlighted.ts -storepass enlighted -noprompt
