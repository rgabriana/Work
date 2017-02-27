#!/bin/bash

export POSTGRESHOST="localhost"
export POSTGRESUSER="postgres"
export POSTGRESDATABASE="ems"
export SYSTEMTABLE="system_configuration"



{


# Input parameters
export OPERATION="$1"
export VALUE="$2"
export NAME="$3"




updateCloudCommunicationFlag() {
    /usr/bin/psql -U $POSTGRESUSER $POSTGRESDATABASE -h $POSTGRESHOST -c "update $SYSTEMTABLE set value = '$VALUE' where name = '$NAME'"
}

if [ $OPERATION == "update" ]
then
    updateCloudCommunicationFlag
fi

}


