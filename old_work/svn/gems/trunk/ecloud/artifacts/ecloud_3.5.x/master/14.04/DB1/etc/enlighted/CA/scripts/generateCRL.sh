#!/bin/bash -x 
set -e
source /etc/enlighted/CA/scripts/cert.config

openssl ca -config $CA_SCRIPTS/openssl.my.cnf -passin "pass:enlighted" -gencrl -out $CA_HOME/crl/enlca.crl
