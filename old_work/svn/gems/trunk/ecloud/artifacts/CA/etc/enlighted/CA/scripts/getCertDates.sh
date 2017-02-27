#!/bin/sh
CA_HOME=/etc/enlighted/CA/ssl

if [ $# = 0 ]; then
	echo "Please provide client certificate name"
	exit
fi

openssl x509 -noout -in $CA_HOME/certs/$1.pem -dates

$CA_HOME/../scripts/verifyCert.sh $1
