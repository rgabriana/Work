#!/bin/bash
arg=0
if [ -z "$1" ]
  then
    arg=1
fi

if [ -z "$2" ]
  then
    arg=1
fi
if [ $arg -gt 0 ]
  then
        echo "Arguments not proper.."
        echo "Please specify arguments in the fashion UNIQUE_PROCESS_NAME INTERVAL_TIME_SINCE_START(seconds)"
        echo "sh kill_processes.sh /home/enlighted/utils/generateSPPACerts.php 30"
        echo "Exiting now....."
        exit 1
fi
 
for pid in $(ps -ef | grep $1 | awk '{print $2}' | cut -d' ' -f1); 
  do  
    for time in $(ps -ef | grep $1 | grep $pid | awk 'NR==1{print $5}' | cut -d' ' -f1); 
    do          
        for time_elapsed in $(ps -eo pid,etime,command | grep $pid | grep -v grep | awk '{print $2}' | awk -F : '{ printf("%.0f\n", $1*60*60+$2*60+($3)); }');
        do 
            if [ $time_elapsed -gt $2 ]; then
                echo "killing process:$pid:time elapsed is:$time_elapsed";
                sudo kill -9 $pid
            fi
        done;
    done; 
  done;


