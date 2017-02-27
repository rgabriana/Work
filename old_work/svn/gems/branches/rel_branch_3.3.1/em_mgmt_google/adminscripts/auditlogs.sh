#!/bin/bash

export POSTGRESHOST="localhost"
export POSTGRESUSER="postgres"
export POSTGRESDATABASE="ems"
export AUDITTABLE="ems_user_audit"
export AUDITSEQ="ems_user_audit_seq"

{


# Input parameters
export OPERATION="$1"
export ACTION="$2"
export DESCRIPTION="$3"
export IP="$4"
export ID="$5"
export TIME=$6

addAudit() {
    newid=$(/usr/bin/psql -x -U $POSTGRESUSER $POSTGRESDATABASE -c "select nextval('$AUDITSEQ')" | grep nextval | cut -d " " -f3)
    echo "ID:$newid:"
    if [ ! -z "$TIME" ]
    then
        /usr/bin/psql -U $POSTGRESUSER $POSTGRESDATABASE -c "insert into $AUDITTABLE(id, user_id ,username, action_type, log_time, description,ip_address) values ($newid, 1, 'admin', '$ACTION', '$TIME', '$DESCRIPTION' , '$IP')"
    else
        /usr/bin/psql -U $POSTGRESUSER $POSTGRESDATABASE -c "insert into $AUDITTABLE(id, user_id ,username, action_type, log_time, description,ip_address) values ($newid, 1, 'admin', '$ACTION', current_timestamp, '$DESCRIPTION' , '$IP')"
    fi

}

modifyAudit() {
    /usr/bin/psql -U $POSTGRESUSER $POSTGRESDATABASE -c "update $AUDITTABLE set description = description || '$DESCRIPTION' where id = $ID"
}

if [ $OPERATION == "add" ]
then
    addAudit
else
    modifyAudit
fi

}


