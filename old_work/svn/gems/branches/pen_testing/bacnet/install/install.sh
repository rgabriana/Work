#!/bin/sh

set -x

BACNETD_HOME_DIR="/var/lib/bacnet"
BACNETD_PATH="/usr/sbin"
INITD_PATH="/etc/init.d"

cwd=$(pwd)

echo "Starting install of bacnet daemon. "

if [ ! -d ${BACNETD_HOME_DIR} ]
then
    echo "Creating ${BACNETD_HOME_DIR} directory."
    mkdir -p ${BACNETD_HOME_DIR}
else
    echo "Directory ${BACNETD_HOME_DIR} exists."
fi

echo "Setting ownership of ${BACNETD_HOME_DIR} directory."
chown enlighted:enlighted ${BACNETD_HOME_DIR}

# Setup libroxml library.
LIBROXML_SO_FILE="/usr/lib/libroxml.so"

if [ ! -f ${LIBROXML_SO_FILE} ]
then
    echo "Installing libroxml library."
    tar -xvzf libroxml-2.2.2.tar.gz
    cd libroxml-2.2.2
    make
    make install
    cd ${cwd}
else
    echo "libroxml library exists."
fi

# Install curl.
if ! dpkg -l curl | grep 7.19.7
then
    echo "Installing curl package."
    dpkg -i curl_7.19.7-1ubuntu1.1_i386.deb
else
    echo "curl package exists."
fi

# Copy bacnet daemon config files to home directory.
echo "Copying bacnetd files to ${BACNET_HOME_DIR}."
cp -pvf ./bacnet.conf  ${BACNETD_HOME_DIR}/bacnet.conf
cp -pvf ./bacnet_objects.cfg ${BACNETD_HOME_DIR}/bacnet_objects.cfg
cp -pvf ./procmon.cron ${BACNETD_HOME_DIR}/procmon.cron

echo "Setting ownership of files in ${BACNETD_HOME_DIR} directory."
chown enlighted:enlighted ${BACNETD_HOME_DIR}/*

# Copy bacnet daemon binary and other files.
echo "Copying bacnetd init files to ${INITD_PATH}."
cp -pvf ./bacnet  ${INITD_PATH}/bacnet
cp -pvf ./procmon.sh  ${BACNETD_PATH}/procmon.sh
cp -pvf ./bacnetd ${BACNETD_PATH}/bacnetd

# Set uid and gid of bacnet daemon for this user.
echo "Setting uid and gid of bacnetd program."
chmod u+s ${BACNETD_PATH}/bacnetd
chmod g+s ${BACNETD_PATH}/bacnetd

# Setup init scripts for bacnet.
echo "Setting init scripts for bacnet."
cd ${INITD_PATH}
update-rc.d bacnet defaults 99
update-rc.d bacnet enable 2 3 4 5
cd ${cwd}

echo "Bacnet daemon install done."

echo "Setting UEM keys"
psql -U postgres ems -c "update system_configuration set value = '498325de3cb4a7d966e21317e65d72059824982' where name = 'uem.apikey'"
psql -U postgres ems -c "update system_configuration set value = '7916298ddca0409b4ff2488f4ba4418e7f689b4e' where name = 'uem.secretkey'"
echo "UEM keys setup complete"


echo "Starting bacnet service."
${INITD_PATH}/bacnet start
