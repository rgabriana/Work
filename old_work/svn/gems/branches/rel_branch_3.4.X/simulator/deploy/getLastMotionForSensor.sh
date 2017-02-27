#!/bin/bash

export filename=$1
export folder="./sensormotions"

output="78"
if [ -f $folder/$filename ]
then
    output=$(head -n 1 $folder/$filename)
    if [ -z $output ]
    then 
        output="78"
    else 
        sed -i -e "1d" $folder/$filename
    fi
fi

echo $output
