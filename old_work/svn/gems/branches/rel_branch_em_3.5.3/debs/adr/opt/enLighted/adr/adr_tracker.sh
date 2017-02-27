#!/bin/bash
source /etc/environment
export ENL_APP_HOME=$ENL_APP_HOME
export OPT_ENLIGHTED=$OPT_ENLIGHTED
export ENLIGHTED_HOME=$ENLIGHTED_HOME
export JAVA_HOME=$OPT_ENLIGHTED/adr/jre1.7.0_21

{
adrProcess=$(ps -ef | grep -E "adr\.jar")
if [[ $adrProcess =~ "adr.jar" ]]
then
    echo "ADR is running"      
else
    echo "Starting ADR job"
    $OPT_ENLIGHTED/adr/jre1.7.0_21/bin/java -Djava.util.logging.config.file=$OPT_ENLIGHTED/adr/logging.properties -jar $OPT_ENLIGHTED/adr/adr.jar &
fi

}
