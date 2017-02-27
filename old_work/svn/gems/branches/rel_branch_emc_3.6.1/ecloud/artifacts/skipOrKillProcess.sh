#!/bin/bash

process="/home/enlighted/utils/generateSPPACerts.php"
lockfile="/home/enlighted/utils/generateSPPACerts.pid"
timeout=1000
newPid=$1
logsFile="/home/enlighted/utils/gencerts.log"

output=0


skipKill() {

    for pid in $(ps -ef | grep $process | grep -v grep | awk '{print $2}' | cut -d' ' -f1);
    do
        time_elapsed=$(ps -p "$pid" -o etime= | awk 'BEGIN { FS = ":" } { if (NF == 2) { print $1*60 + $2 } else if (NF == 3) { split($1, a, "-"); if (a[2] != "" ) { print ((a[1]*24+a[2])*60 + $2) * 60 + $3; } else { print ($1*60 + $2) * 60 + $3; } } }' )
        if [ $time_elapsed -gt $timeout ]; then
            kill -9 $pid
        fi
    done;


    if [ ! -f "$lockfile" ]
    then
            echo "" > "$lockfile"
    else
            oldpid=$(head -n 1 "$lockfile")
            if [ -z $oldpid ]
            then
                echo $newPid > "$lockfile"
                output=1
            else
                processRunning=$(ps -fp $oldpid)
                if [[ "$processRunning" =~ "$oldpid" ]]
                then
                    echo "another instance with pid = $oldpid already in progress."
                    output=0
                else
                    echo $newPid > "$lockfile"
                    output=1
                fi
            fi
    fi


}

skipKill >> $logsFile 2>&1
echo $output



