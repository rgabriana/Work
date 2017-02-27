#!/bin/sh
CA_HOME=../ssl
DOMAIN_NAME=enlightedcloud.net

openssl req -passout pass:enlighted -config openssl.my.cnf -new -x509 -extensions v3_ca -keyout $CA_HOME/private/enlca.key -out $CA_HOME/certs/enlca.crt -days 9125 \
        -subj "/C=US/ST=California/L=Mountain View/O=enLighted Inc/CN=www.$DOMAIN_NAME/emailAddress=support@$DOMAIN_NAME/subjectAltName=DNS:www.$DOMAIN_NAME"

keytool -import -file $CA_HOME/certs/enlca.crt -alias ENLCA -keystore $CA_HOME/pfx/enlighted.ts -storepass enlighted -noprompt
