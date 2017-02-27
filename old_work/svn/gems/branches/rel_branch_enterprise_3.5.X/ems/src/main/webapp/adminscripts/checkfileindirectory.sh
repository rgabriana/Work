#!/bin/bash


OLD_IFS=$IFS
IFS=$'\n'
if [ ! -z "$1" ]
then

        if [ -z "$2" ]
        then
                listoffiles=$(sudo /usr/bin/find $1 -type f '-ls' |  awk '{for (i = 1; i < 11; i++) $i = ""; sub(/^ */, ""); print}' )
                for file in $listoffiles
                do
                        echo "$file"
                done
        else
                listoffiles=$(sudo /usr/bin/find $1 -type f '-ls' |  awk '{for (i = 1; i < 11; i++) $i = ""; sub(/^ */, ""); print}' | grep $2 )
                for file in $listoffiles
                do
                        echo "$file"
                done
        fi
fi
IFS=$OLD_IFS