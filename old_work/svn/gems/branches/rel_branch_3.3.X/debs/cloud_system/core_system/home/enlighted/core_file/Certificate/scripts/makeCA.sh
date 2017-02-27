#!/bin/sh
CA_HOME=../ssl

if [ -f openssl.my.cnf ]; then
	mkdir -m 0755 -p $CA_HOME
	mkdir -m 0755 \
		$CA_HOME/certs \
		$CA_HOME/crl \
		$CA_HOME/csr \
		$CA_HOME/doc \
		$CA_HOME/newcerts \
		$CA_HOME/private \
		$CA_HOME/pfx
	touch $CA_HOME/serial 
	echo '01' > $CA_HOME/serial
	touch $CA_HOME/index.txt 
	touch $CA_HOME/crlnumber
	echo '01' > $CA_HOME/crlnumber
	cp openssl.my.cnf $CA_HOME/
else
	echo "Please ensure that openssl.my.cnf exists in your current directory!"
	exit
fi

