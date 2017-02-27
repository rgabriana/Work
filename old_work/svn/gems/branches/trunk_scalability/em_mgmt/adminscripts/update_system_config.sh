#!/bin/bash

{


# Input parameters
export OPERATION="$1"
export VALUE="$2"
export NAME="$3"




updateCloudCommunicationFlag() {
    curl -d "<root><request><messageType>1</messageType><body><loginRequest><appIp>EMS COMMUNICATOR</appIp></loginRequest></body></request></root>" -c /tmp/sc_cookie.tmp -k https://localhost/ems/wsaction.action
    curl -v -X POST -H"Content-Type: application/xml" -b /tmp/sc_cookie.tmp -d "<systemConfiguration><name>${NAME}</name><value>${VALUE}</value></systemConfiguration>" -k https://localhost/ems/services/systemconfig/edit
}

if [ $OPERATION == "update" ]
then
    updateCloudCommunicationFlag
fi

}


