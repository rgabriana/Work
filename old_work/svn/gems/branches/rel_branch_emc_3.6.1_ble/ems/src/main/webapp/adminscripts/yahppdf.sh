#!/bin/bash
source /etc/environment
#wkhtmltopdfpath=`which wkhtmltopdf`
#xvfb-run -a -s "-screen 0 640x480x16" $wkhtmltopdfpath "$@"

toreplace=$( echo "$ENL_APP_HOME/webapps/ems/" | sed 's/\//\\\//g')
sed -i 's/;jsessionid\(.*"\)/"/g' $1
#sed -i "s/\/ems\//$toreplace/g" $1

#xvfb-run -a $wkhtmltopdfpath $@

java -jar $ENL_APP_HOME/Enlighted/enl_utils.jar feature=genpdf input=$1 output=$2 header=$3 footer=$4