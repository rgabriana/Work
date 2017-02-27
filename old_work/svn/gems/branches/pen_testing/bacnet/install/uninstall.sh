#!/bin/sh

set -x

BACNETD_HOME_DIR="/var/lib/bacnet"
BACNETD_PATH="/usr/sbin"
INITD_PATH="/etc/init.d"

cwd=$(pwd)

echo "Starting uninstall of bacnet daemon. "

if [ -d ${BACNETD_HOME_DIR} ]
then
    echo "Removing ${BACNETD_HOME_DIR} directory."
    rm -rf ${BACNETD_HOME_DIR}
fi

if [ -f ${INITD_PATH}/bacnet ]
then
    echo "Stopping bacnet init service."
    ${INITD_PATH}/bacnet stop
fi

rm -f ${BACNETD_PATH}/procmon.sh >/dev/null
rm -f ${BACNETD_PATH}/bacnetd >/dev/null
rm -f ${INITD_PATH}/bacnet >/dev/null

echo "Removing init scripts for bacnet."
cd ${INITD_PATH}
update-rc.d bacnet remove
cd ${cwd}

echo "Bacnet daemon uninstall done."
