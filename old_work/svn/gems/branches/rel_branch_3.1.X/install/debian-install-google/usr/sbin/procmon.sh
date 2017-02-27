#!/bin/sh

PROG_NAME="bacnetd"

pgrep ${PROG_NAME} >/dev/null
status=$?

if [ ${status} -ne 0 ]
then
    logger -p 2 -t procmon "Process ${PROG_NAME} is not running."
    /var/lib/tomcat6/Enlighted/bacnet start
    status=$?
    if [ ${status} -eq 0 ]
    then
        logger -p 6 -t procmon "Process ${PROG_NAME} started successfully."
    else
        logger -p 2 -t procmon "Process ${PROG_NAME} failed to start."
    fi
fi

exit 0

