#!/bin/sh
CA_HOME=../ssl
DOMAIN_NAME=enlightedcloud.net

if [ $# = 0 ]; then
	echo "Please provide client certificate name"
	exit
fi

openssl x509 -noout -in $CA_HOME/certs/$1.pem -dates
