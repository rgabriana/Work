#!/bin/bash -x

source /etc/enlighted/CA/scripts/cert.config

if [ -f $CA_SCRIPTS/openssl.my.cnf ]; then
  mkdir -m 0755 -p $CA_HOME/{certs,crl,csr,doc,newcerts,private,pfx}
  cp -v $CA_SCRIPTS/openssl.my.cnf $CA_HOME/
else
  echo "Please ensure that openssl.my.cnf exists in your current directory!"
  exit 1
fi

if [ -f $CA_HOME/serial ]; then
  echo "found an existing file, creating a backup"
  cp -v $CA_HOME/serial $CA_HOME/serial.installbk.$(date +%d%b%Y_%T)
  echo "01" > $CA_HOME/serial
else
  echo "01" > $CA_HOME/serial
fi

if [ -f $CA_HOME/index.txt ]; then
  echo "found an existing file, creating a backup"
  cp -v $CA_HOME/index.txt $CA_HOME/index.txt.installbk.$(date +%d%b%Y_%T)
  echo "" > $CA_HOME/index.txt
else
  echo "" >  $CA_HOME/index.txt 
fi

if [ -f $CA_HOME/crlnumber ]; then
  echo "found an existing file, creating a backup"
  cp -v $CA_HOME/crlnumber $CA_HOME/clrnumber.installbk.$(date +%d%b%Y_%T)
  echo "01" > $CA_HOME/crlnumber
else 
  echo "01" > $CA_HOME/crlnumber
fi
