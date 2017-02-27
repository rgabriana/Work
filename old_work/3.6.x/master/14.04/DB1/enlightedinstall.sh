#!/bin/bash -x
#########################################
# enlightedinstall.sh
#########################################
set -e

PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/sbin:/usr/local/bin

# Root check

if [ `id -u` != "0" ]; then
  echo "you need root priveleges to execute this scripts"
  exit
fi

# Source check 
if [ ! -d $src_dir/ENL_files ]; then
  echo "The source directory $src_dir/ENL_files is missing.  You must first run the get_enlighted_source script"
  exit
fi

source $src_dir/$SETUP_CONFIG

for((i=0;i<${#UTILS[@]};i++))

do 

  SOURCE=${UTILS[$i]}
  DESTINATION=`echo $SOURCE | sed -e "s!${MASTER_OS_SRC}!!"`  
  DIR=`dirname $DESTINATION`
  FILE=`basename $DESTINATION`

  if [ ! -f $DESTINATION ] || [ $(grep -s -c $(shasum $DESTINATION|awk -F " " '{print $1}') $SUMFILE) = 0 ]; then

    echo "$FILE is missing or out of date"

      if [ -f $DESTINATION ]; then

        echo "Creating $FILE backups"
        cp -v $DESTINATION $DESTINATION.patchbk.$(date +%d%b%Y_%T)

      fi 

    echo "Updating $FILE"
    cp -v $src_dir/ENL_files/$FILE $DESTINATION
    chown -v enlighted:enlighted $DESTINATION
    chmod -v 755 $DESTINATION

  fi 

done

chmod -v 644 /etc/logrotate.d/gencerts
chown -v root:root /etc/logrotate.d/gencerts

echo 'PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin' > /tmp/enlighted-cron
echo '*/5 * * * * php /home/enlighted/utils/generateSPPACerts.php >> /home/enlighted/utils/gencerts.log 2>&1' >> /tmp/enlighted-cron
crontab -u enlighted /tmp/enlighted-cron
#chmod -v 600 /var/spool/cron/crontabs/enlighted
#chown -v enlighted:root /var/spool/cron/crontabs/enlighted
