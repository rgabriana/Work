#!/bin/sh
source /etc/environment
newmode=$1
currentmode=$(head -n 1 $ENL_APP_HOME/Enlighted/emsmode)
if [ "$newmode" != "NORMAL" ] 
then
    if [ "$currentmode" == "NORMAL"  -o "$currentmode" == "TOMCAT_SHUTDOWN" ]
    then
        if [ "$newmode" == "NORMAL:IMAGE_UPGRADE" ]
        then
            echo "$newmode:1:" > $ENL_APP_HOME/Enlighted/emsmode
            echo "S"
        else
            echo "$newmode" > $ENL_APP_HOME/Enlighted/emsmode
            echo "S"
        fi
    else
        if [ "$newmode" == "NORMAL:IMAGE_UPGRADE" ]
        then
            if [[ "$currentmode" =~ "NORMAL:IMAGE_UPGRADE:" ]]
            then
                oldnoofimgupd=$(head -n 1 $ENL_APP_HOME/Enlighted/emsmode | cut -d":" -f3)
                newnoofimgupd=$(expr $oldnoofimgupd + 1)
                sed -i 's/\(NORMAL:IMAGE_UPGRADE:\)[0-9]*/\1'$newnoofimgupd'/' $ENL_APP_HOME/Enlighted/emsmode
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
        oldnoofimgupd=$(head -n 1 $ENL_APP_HOME/Enlighted/emsmode | cut -d":" -f3)
        newnoofimgupd=$(expr $oldnoofimgupd - 1)
        if [ $newnoofimgupd == "0" ]
        then
            echo "NORMAL" > $ENL_APP_HOME/Enlighted/emsmode
            echo "S"
        else
            sed -i 's/\(NORMAL:IMAGE_UPGRADE:\)[0-9]*/\1'$newnoofimgupd'/' $ENL_APP_HOME/Enlighted/emsmode
            echo "S"
        fi
    else
        echo "NORMAL" > $ENL_APP_HOME/Enlighted/emsmode
        echo "S"
    fi
fi
