#!/bin/bash

{
adrProcess=$(ps -ef | grep -E "adr\.jar")
if [[ $adrProcess =~ "adr.jar" ]]
then
    echo "ADR is running"      
else
    echo "Starting ADR job"
    java -jar /opt/enLighted/adr/adr.jar &
fi

}
