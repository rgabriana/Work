#!/bin/sh
## This file is being managed by puppet ##
CA_HOME=/etc/enlighted/CA/ssl

openssl ca -config $CA_HOME/openssl.my.cnf -gencrl -out $CA_HOME/crl/enlca.crl
