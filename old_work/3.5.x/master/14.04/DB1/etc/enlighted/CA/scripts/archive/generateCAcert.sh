#!/bin/bash
set -e

. /etc/enlighted/CA/scripts/cert.config

if [ -f $CA_HOME/pfx/enlighted.ts ]; then
  echo "WARNING: there is already an existing trust store in $CA_HOME/pfx folder."
  read -p "Are you sure you want to continue? (HINT: continuing means you are breaking your existing certificate chain.) yes or no? " ANSWER
  if [[ ! $ANSWER =~ ^([Yy][Ee][Ss]|[Yy])$ ]]; then
  exit
  fi
fi
openssl req\
 -passout\
 pass:enlighted\
 -config $CA_SCRIPTS/openssl.my.cnf\
 -new\
 -x509\
 -extensions v3_ca\
 -keyout $CA_HOME/private/enlca.key\
 -out $CA_HOME/certs/enlca.crt -days 9125 \
 -subj "/C=US\
/ST=California/\
L=Mountain View/\
O=enLighted Inc/\
CN=www.$DOMAIN_NAME/\
emailAddress=support@$DOMAIN_NAME/\
subjectAltName=DNS:www.$DOMAIN_NAME"
###########################################################################################
keytool\
 -import\
 -file $CA_HOME/certs/enlca.crt\
 -alias ENLCA\
 -keystore $CA_HOME/pfx/enlighted.ts\
 -storepass enlighted\
 -noprompt
