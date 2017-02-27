#!/bin/sh
CA_HOME=../ssl

if [ $# = 0 ]; then
	echo "Please provide client certificate name"
	exit
fi
openssl verify -CAfile $CA_HOME/certs/enlca.crt $CA_HOME/certs/$1.pem
