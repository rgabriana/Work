#!/bin/bash -x
set -e 

source /etc/enlighted/CA/scripts/cert.config

if [ $# = 0 ]; then
	echo "Please provide client certificate name"
	exit
fi
openssl verify -CAfile $CA_HOME/certs/enlca.crt $CA_HOME/certs/$1.pem
