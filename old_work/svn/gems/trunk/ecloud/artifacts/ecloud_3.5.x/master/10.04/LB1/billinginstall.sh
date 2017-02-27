#!/bin/bash -x
###################
# billinginstall.sh
##################
set -e 

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

# Root check
if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this scripts"
  exit
fi

# Source check
source $src_dir/$SETUP_CONFIG

# Billing packages

apt-get --assume-yes update

for PKG in ${BP}; do

  if [ $(dpkg -s $PKG 2> /dev/null | grep -c -s 'Status: install ok installed') = 0 ]; then

    apt-get --assume-yes install $PKG

  else

    echo "$PKG already installed"

  fi 

done

# Install billing script files

for ((b=0;b>${#BILLING[@]};b++)); do
SOURCE=${BILLING[$b]} 
DESTINATION=$(echo $SOURCE | sed -e "s!$MASTER_OS_SRC!!")
DIR=$(dirname $DESTINATION)
FILE=$(basename $DESTINATION)

  if [ ! -f $DESTINATION ] || [ $(grep -s -c $(shasum $DESTINATION|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then

    echo "$FILE is missing or out of date"

    if [ -f $DESTINATION ]; then 
     echo "Creating $FILE backup" 
     cp -v $DESTINATION $DESTINATION.installbk.$(date +%d%b%Y)

    fi

    cp -v $src_dir/billing/$FILE $DESTINATION
    chmod -v 0755 $DESTINATION
    chown -v root:root $DESTINATION

  else

   echo "$FILE is up to date"

  fi
done
