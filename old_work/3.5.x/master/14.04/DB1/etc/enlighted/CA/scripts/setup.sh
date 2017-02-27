#!/bin/bash -x
set -e 

echo "Executing makeCA.sh script"
bash -x /etc/enlighted/CA/scripts/makeCA.sh
echo "Executing generateCAcert.sh script" 
bash -x /etc/enlighted/CA/scripts/generateCAcert.sh
echo "Executiing generateServercert.sh script"
bash -x /etc/enlighted/CA/scripts/generateServercert.sh us-tx-m-p-6854f5fff88e
echo "Executiing generateCRL.sh script"
bash -x /etc/enlighted/CA/scripts/generateCRL.sh
