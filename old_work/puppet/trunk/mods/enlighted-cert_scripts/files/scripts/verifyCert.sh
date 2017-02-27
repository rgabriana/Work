#!/bin/sh
## This file is being managed by puppet ##
CA_HOME=/etc/enlighted/CA/ssl

if [ $# = 0 ]; then
	echo "Please provide client certificate name"
	exit
fi
openssl verify -CAfile $CA_HOME/certs/enlca.crt $CA_HOME/certs/$1.pem
