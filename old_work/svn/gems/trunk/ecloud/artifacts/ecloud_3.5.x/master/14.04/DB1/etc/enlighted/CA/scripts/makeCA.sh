#!/bin/bashi -x 
set -e

source /etc/enlighted/CA/scripts/cert.config

if [ -f $CA_SCRIPTS/openssl.my.cnf ]; then
	mkdir -vm 0755 -p $CA_HOME
	mkdir -vm 0755 \
		$CA_HOME/{certs,crl,csr,doc,newcerts,private,pfx} 
		#$CA_HOME/crl \
		#$CA_HOME/csr \
		#$CA_HOME/doc \
		#$CA_HOME/newcerts \
		#$CA_HOME/private \
		#$CA_HOME/pfx
echo "Creating Serial list $CA_HOME/serial"
	touch $CA_HOME/serial 
	echo '01' > $CA_HOME/serial
echo "Creating index list $CA_HOME/index.txt"
	touch $CA_HOME/index.txt 
echo "Creating CRL list $CA_HOME/crlnumber"
	touch $CA_HOME/crlnumber
	echo '01' > $CA_HOME/crlnumber
echo "Copying config file to $CA_HOME openssl.my.cnf"
	cp -v $CA_SCRIPTS/openssl.my.cnf $CA_HOME/
else
	echo "Please ensure that openssl.my.cnf exists in your current directory!"
	exit
fi
