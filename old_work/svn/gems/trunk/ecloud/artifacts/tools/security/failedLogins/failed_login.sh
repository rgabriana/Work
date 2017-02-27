#!/bin/bash
set -e 
source /opt/failed_logins.config
failed_list=$(mktemp)

## whitelist ##
awk -F " " '$6 ~ /Accepted/'
grep -i 'failed.*password' $flog | while read line
do
  ip=`echo $line | awk -F " " '{print $10}'`

done






| rev |awk -F " " '{print $4}' | rev |sort|uniq -c|sort -n | awk -F " " '$1 > 5' | awk -F " " '{print $2}'|sort -n


