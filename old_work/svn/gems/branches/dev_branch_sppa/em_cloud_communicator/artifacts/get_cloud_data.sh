#!/bin/bash

WORKING_DIR="/home/enlighted/clouddata"
{

cd $WORKING_DIR

prepareData() {

    old="$1"
    new="$2"
    out="$3"
    difffile="$4"
    finalout="$5"

    rm $out $new $difffile $finalout

    cp "/tmp/$new" ./
    sed -i 's/\\~/RCDLMTRENCODE/g' $new

    if [ ! -f "$old" ]
    then
        touch $old
    fi

    oldlines=$(wc -l "$old" | cut -d" " -f1)
    newlines=$(wc -l "$new" | cut -d" " -f1)

    oldlinecount=1
    newlinecount=1

    while [ "$oldlinecount" -le "$oldlines" ]
    do
        oldid=$(head -n "$oldlinecount" "$old" | tail -n 1 | cut -d"~" -f1)
        if [ "$newlinecount" -le "$newlines" ]
        then
            newrecord=$(head -n "$newlinecount" "$new" | tail -n 1)
            newid=$(echo "$newrecord" | cut -d"~" -f1)
            if [ "$oldid" == "$newid" ]
            then
                echo "$newrecord" >> "$out"
                oldlinecount=`expr $oldlinecount + 1`            
                newlinecount=`expr $newlinecount + 1`
            else
                echo "" >> "$out"
                oldlinecount=`expr $oldlinecount + 1`
            fi
        else
            echo "" >> "$out"
            oldlinecount=`expr $oldlinecount + 1`
        fi
    done

    while [ "$newlinecount" -le "$newlines" ]
    do
        newrecord=$(head -n "$newlinecount" "$new" | tail -n 1)
        echo "$newrecord" >> "$out"
        newlinecount=`expr $newlinecount + 1`
    done

    diff -y $old $out > $difffile

    insert=$(grep -n "^[^~]*>" $difffile  | sed 's/^\([0-9]*\):.*$/\1/')
    if [ ! -z "$insert" ]
    then
        numberoflines=$(echo "$insert" | wc -l)
        readline=1
        while [ $readline -le $numberoflines ]
        do
            procline=$(echo "$insert" | head -n $readline | tail -n 1)
            head -n $procline $out | tail -n 1  | sed 's/^/INSERT~/' >> $finalout
            readline=`expr $readline + 1`
        done
    fi

    update=$(grep -n "^[0-9].*|[^a-zA-Z]*~" $difffile  | sed 's/^\([0-9]*\):.*$/\1/')
    if [ ! -z "$update" ]
    then
        numberoflines=$(echo "$update" | wc -l)
        readline=1
        while [ $readline -le $numberoflines ]
        do
            procline=$(echo "$update" | head -n $readline | tail -n 1)
            head -n $procline $out | tail -n 1 | sed 's/^/UPDATE~/' >> $finalout
            readline=`expr $readline + 1`
        done
    fi

    delete=$(grep -n "^[0-9].*|[^a-zA-Z0-9~]*$" $difffile  | sed 's/^\([0-9]*\):.*$/\1/')
    if [ ! -z "$delete" ]
    then
        numberoflines=$(echo "$delete" | wc -l)
        readline=1
        while [ $readline -le $numberoflines ]
        do
            procline=$(echo "$delete" | head -n $readline | tail -n 1)
            head -n $procline $old | tail -n 1 | sed 's/^/DELETE~/' >> $finalout
            readline=`expr $readline + 1`
        done
    fi

}

generatedata=$(psql -U postgres ems -c "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE READ ONLY; select getCloudData();")

.  /var/lib/tomcat6/Enlighted/communicator.properties 
generatedata=$(psql -U postgres ems -c "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE READ ONLY; select getCloudEnergyData($lastEnergyConsumptionId ,$emInstaceId);")

prepareData "old_cloud_company" "new_cloud_company" "out_cloud_company" "diff_cloud_company" "final_cloud_company"
prepareData "old_cloud_campus" "new_cloud_campus" "out_cloud_campus" "diff_cloud_campus" "final_cloud_campus"
prepareData "old_cloud_building" "new_cloud_building" "out_cloud_building" "diff_cloud_building" "final_cloud_building"
prepareData "old_cloud_floor" "new_cloud_floor" "out_cloud_floor" "diff_cloud_floor" "final_cloud_floor"
prepareData "old_cloud_area" "new_cloud_area" "out_cloud_area" "diff_cloud_area" "final_cloud_area"
prepareData "old_cloud_fixture" "new_cloud_fixture" "out_cloud_fixture" "diff_cloud_fixture" "final_cloud_fixture"
prepareData "old_cloud_gateway" "new_cloud_gateway" "out_cloud_gateway" "diff_cloud_gateway" "final_cloud_gateway"

}

