#!/bin/sh

newmode=$1
currentmode=$(head -n 1 /var/lib/tomcat6/Enlighted/emsmode)
if [ "$newmode" != "NORMAL" ] 
then
    if [ "$currentmode" == "NORMAL"  -o "$currentmode" == "TOMCAT_SHUTDOWN" ]
    then
        if [ "$newmode" == "NORMAL:IMAGE_UPGRADE" ]
        then
            echo "$newmode:1:" > /var/lib/tomcat6/Enlighted/emsmode
            echo "S"
        else
            echo "$newmode" > /var/lib/tomcat6/Enlighted/emsmode
            echo "S"
        fi
    else
        if [ "$newmode" == "NORMAL:IMAGE_UPGRADE" ]
        then
            if [[ "$currentmode" =~ "NORMAL:IMAGE_UPGRADE:" ]]
            then
                oldnoofimgupd=$(head -n 1 /var/lib/tomcat6/Enlighted/emsmode | cut -d":" -f3)
                newnoofimgupd=$(expr $oldnoofimgupd + 1)
                sed -i 's/\(NORMAL:IMAGE_UPGRADE:\)[0-9]*/\1'$newnoofimgupd'/' /var/lib/tomcat6/Enlighted/emsmode
                echo "S"
            else
                echo "$currentmode"
            fi
        else
            echo "$currentmode"
        fi
    fi
else
    if [[ "$currentmode" =~ "NORMAL:IMAGE_UPGRADE:" ]]
    then
        oldnoofimgupd=$(head -n 1 /var/lib/tomcat6/Enlighted/emsmode | cut -d":" -f3)
        newnoofimgupd=$(expr $oldnoofimgupd - 1)
        if [ $newnoofimgupd == "0" ]
        then
            echo "NORMAL" > /var/lib/tomcat6/Enlighted/emsmode
            echo "S"
        else
            sed -i 's/\(NORMAL:IMAGE_UPGRADE:\)[0-9]*/\1'$newnoofimgupd'/' /var/lib/tomcat6/Enlighted/emsmode
            echo "S"
        fi
    else
        echo "NORMAL" > /var/lib/tomcat6/Enlighted/emsmode
        echo "S"
    fi
fi
