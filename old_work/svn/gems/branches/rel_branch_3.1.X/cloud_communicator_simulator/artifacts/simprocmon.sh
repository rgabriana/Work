#!/bin/sh

COMPID=`ps auxwww | grep -i "DNAME=2261" | grep -v grep | awk '{print $2}'`
if [ -z "$COMPID" ]; then

        logger -p 6 -t simprocmon "COM 2261 is down"
        echo "Starting COM 2261"
        cd ~/simulator/2261/
        java -jar -DNAME=2261 ~/simulator/cloudcommunicatorsimulator.jar  > /dev/null 2>&1 &
        sleep 2
else
        logger -p 6 -t simprocmon "COM 2261 is running"
fi
