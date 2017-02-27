#!/bin/sh
CA_HOME=../ssl

openssl ca -config openssl.my.cnf -passin "pass:enlighted" -gencrl -out $CA_HOME/crl/enlca.crl
