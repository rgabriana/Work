#!/bin/sh

CA_HOME=/etc/enlighted/CA/ssl

$CA_HOME/../scripts/revokeServerCert.sh $1

$CA_HOME/../scripts/generateServercert.sh $1
