#!/bin/sh

./makeCA.sh
./generateCAcert.sh
./generateServercert.sh us-tx-m-p-6854f521b058
./generateCRL.sh
