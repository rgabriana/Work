#!/bin/sh

CA_HOME=/etc/enlighted/CA/scripts

$CA_HOME/makeCA.sh
$CA_HOME/generateCAcert.sh
$CA_HOME/generateCRL.sh
