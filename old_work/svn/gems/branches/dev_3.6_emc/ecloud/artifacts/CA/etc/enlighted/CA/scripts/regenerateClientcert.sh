#!/bin/sh

CA_HOME=/etc/enlighted/CA/ssl

$CA_HOME/../scripts/revokeClientCert.sh $1

$CA_HOME/../scripts/generateClientcert.sh $1

 
