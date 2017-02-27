#!/bin/bash

export JAVA_HOME=/opt/enLighted/adr/jre1.7.0_21

{
adrProcess=$(ps -ef | grep -E "adr\.jar")
if [[ $adrProcess =~ "adr.jar" ]]
then
    echo "ADR is running"      
else
    echo "Starting ADR job"
    /opt/enLighted/adr/jre1.7.0_21/bin/java -Djava.util.logging.config.file=/opt/enLighted/adr/logging.properties -jar /opt/enLighted/adr/adr.jar &
fi

}
