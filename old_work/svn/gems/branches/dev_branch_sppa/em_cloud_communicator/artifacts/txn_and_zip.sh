#!/bin/bash

op="$1"
txn="$2"


if [ -f "/var/lib/tomcat6/Enlighted/CloudDataManifest.MF" ]
then
    if [ "$op" == "new" ]
    then
        currentrecord=$(cat /var/lib/tomcat6/Enlighted/CloudDataManifest.MF | grep CurrentRecordNumber | cut -d" " -f2)
        currenttxn=$(cat /var/lib/tomcat6/Enlighted/CloudDataManifest.MF | grep CurrentTransactionNumber | cut -d" " -f2)
        currenttxnstat=$(cat /var/lib/tomcat6/Enlighted/CloudDataManifest.MF | grep CurrentTransactionStatus | cut -d" " -f2)
        if [ "$currenttxnstat" == "Y" ]
        then
            newrecord=$(expr $currentrecord + 1)
        else 
            exit
        fi

        rm -f /tmp/em_core*
	 rm -f /tmp/em_energy*
	  rm -f /tmp/final_cloud_energy_consumption

        cd /tmp/
        tar -cvzf "em_core_$newrecord.tar.gz" json.file > /dev/null
	mv /home/enlighted/clouddata/final_cloud_energy_consumption . 
	name="em_energy.tar.gz"
	tar -cvzf  "$name"  final_cloud_energy_consumption > /dev/null
        
        sed -i 's/\(CurrentRecordNumber: \).*/\1'$newrecord'/' /var/lib/tomcat6/Enlighted/CloudDataManifest.MF
        sed -i 's/\(CurrentTransactionNumber: \).*/\10/' /var/lib/tomcat6/Enlighted/CloudDataManifest.MF
        sed -i 's/\(CurrentTransactionStatus: \).*/\1N/' /var/lib/tomcat6/Enlighted/CloudDataManifest.MF

        echo "em_core_$newrecord.tar.gz"

    else
        if [ "$op" == "updatetxn" ]
        then
            sed -i 's/\(CurrentTransactionNumber: \).*/\1'$txn'/' /var/lib/tomcat6/Enlighted/CloudDataManifest.MF
        else
            if [ "$op" == "reset" ]
            then
                sed -i 's/\(CurrentTransactionStatus: \).*/\1Y/' /var/lib/tomcat6/Enlighted/CloudDataManifest.MF
            fi
        fi
    fi

else 
    exit
fi
