#!/bin/sh
CA_HOME=/etc/enlighted/CA/ssl

openssl ca -config openssl.my.cnf -passin "pass:enlighted" -gencrl -out $CA_HOME/crl/enlca.crl
