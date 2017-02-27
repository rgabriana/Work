#!/bin/sh
CA_HOME=/etc/enlighted/CA/ssl

if [ $# = 0 ]; then
	echo "Please provide client certificate name"
	exit
fi

tmpfile=$(mktemp)
cat $CA_HOME/certs/enlca.crt $CA_HOME/crl/enlca.crl > $tmpfile
openssl verify -crl_check -CAfile $tmpfile $CA_HOME/certs/$1.pem
rm $tmpfile
