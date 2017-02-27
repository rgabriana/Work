#!/bin/sh
## This file is being managed by puppet ##
CA_HOME=/etc/enlighted/CA/ssl

if [ $# != 4 ]; then
	echo "Usage: testClient.sh <clientcertkey> <clientcertificate> <certpass> <url>"
	exit
fi
curl -v --cacert $CA_HOME/certs/enlca.crt --key $1 --cert $2:$3 $4
