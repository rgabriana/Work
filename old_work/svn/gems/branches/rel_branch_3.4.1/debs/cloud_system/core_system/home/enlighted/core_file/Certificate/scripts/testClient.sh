#!/bin/sh
CA_HOME=../ssl

if [ $# != 4 ]; then
	echo "Usage: testClient.sh <clientcertkey> <clientcertificate> <certpass> <url>"
	exit
fi
curl -v --cacert $CA_HOME/certs/enlca.crt --key $1 --cert $2:$3 $4

#./testClient.sh  ../ca/enlighted/private/em_50_100.key ../ca/enlighted/certs/em_50_100.pem em_50_100 https://080027ecf8d1.enlightedcloud.net/test.php
