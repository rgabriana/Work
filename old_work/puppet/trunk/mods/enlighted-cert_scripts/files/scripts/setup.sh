#!/bin/sh
## This file is being managed by puppet ##
./makeCA.sh
./generateCAcert.sh
./generateServercert.sh master.enlightedcloud.net
./generateCRL.sh
