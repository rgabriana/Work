#!/bin/bash
set -e 

source /etc/enlighted/CA/scripts/cert.config
setup_log=/var/log/enlighted/CAsetup.log
echo '*****************Initiating CA setup*******************'
bash -x $CA_SCRIPTS/makeCA.sh 2>&1 > $setup_log
echo '*****************Generating CA certificate*************'
bash -x $CA_SCRIPTS/generateCAcert.sh 2>&1 > $setup_log
echo '*****************Generating Server Certificate*********'
bash -x $CA_SCRIPTS/generateServercert.sh $(hostname) 2>&1 > $setup_log
echo '****************Generating CRL list********************'
bash -x $CA_SCRIPTS/generateCRL.sh 2>&1 > $setup_log
echo '****************Generating RS certificate**************'
bash -x $CA_SCRIPTS/generateRScert.sh $(hostname) 2>&1 > $setup_log
