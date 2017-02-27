#!/bin/bash -x
set -e

. /etc/enlighted/CA/scripts/cert.config

if [ $# = 0 ]; then
	echo "Please provide client certificate name"
	exit
fi

openssl x509 -noout -in $CA_HOME/certs/$1.pem -dates
