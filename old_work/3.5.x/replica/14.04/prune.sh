#!/bin/bash

##To be run every 15 mins to prune failed uncompressed syncdata as root user
## */15 * * * * //var/lib/tomcat6/Enlighted/prune.sh
cd /var/lib/tomcat6/Enlighted/syncdata/
files=$(ls -t data_*_*)
array=(${files//:/ })
macs=()
index=1
for file in "${array[@]}"
do
    echo "$file"
    mac=$(echo "$file" | cut -d"_" -f2)
    exists="0"
    for key in "${macs[@]}"
    do
        if [ "${key}" == "${mac}" ]
        then
            exists="1"
        fi    
    done
    if [ ${exists} == "0" ]
    then
        macs[index++]="$mac"
    else
        echo "deleting $file"
        rm -f "$file"
    fi
done


