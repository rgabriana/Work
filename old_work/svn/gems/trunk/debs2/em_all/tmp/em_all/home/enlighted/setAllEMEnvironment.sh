#!/bin/bash
function generic_insert_update_key_value_conf(){
	arg=0
	if [ -z "$1" ]
	  then
	        arg=1
	fi
	if [ -z "$2" ]
	then
	        arg=1
	fi
	if [ -z "$3" ]
	then
	        arg=1
	fi
	if [ $arg -gt 0 ]
	  then
	                echo "Arguments not proper.."
	                echo "Please specify arguments in the fashion : key value file_path"
	                echo "generic_insert_update_key_value_conf.sh ENL_APP_HOME= /opt/tomcat /etc/environment"
	                echo "Exiting Now.."
	                exit 1
	fi
	export=1
	if [ -z "$4" ]
	then
	        export=0
	fi

	 if [  $export -eq 1 ] && [ $4 -eq 2  ]
         then
		sudo sed -i "/$1/d" $3
		sudo sed -i '$ a '$1'"'$2'"' $3
		sudo sed -i '$ a export '$1'"'$2'"' $3
	else 
		cnt=`sudo cat $3 | grep $1 | wc -l`
		if [ $cnt -gt 0 ]
		then
			## Replace the line
			if [ $export -eq 0 ]
			then
				sudo sed -i '/'$1'/c\'$1'"'$2'"' $3
			else
				sudo sed -i '/'$1'/c\export '$1'"'$2'"' $3
			fi
		else
			## insert line at the end of file
			if [ $export -eq 0 ]
			then
				sudo sed -i '$ a '$1'"'$2'"' $3
			else
				sudo sed -i '$ a export '$1'"'$2'"' $3
			fi
		fi
	fi
	export $1$2
}

ARCH=`uname -m`
is64=0
if [[ "$ARCH" =~ "_64" ]]
then
	is64=1
fi
#####BELOW FOR 14.04 ###################
if [ $is64 -eq 1 ];
then
	generic_insert_update_key_value_conf JAVA_HOME= "/usr/lib/jvm/jdk-8-oracle-x64/jre" /etc/environment 1
else
	generic_insert_update_key_value_conf JAVA_HOME= "/usr/lib/jvm/jdk-8-oracle-i586/jre" /etc/environment 1
fi
generic_insert_update_key_value_conf ENL_APP_HOME= /opt/tomcat /etc/environment 2
generic_insert_update_key_value_conf ENL_APP_HOME= /opt/tomcat /etc/apache2/envvars 1
generic_insert_update_key_value_conf ENL_TOMCAT_HOME= /opt/tomcat /etc/environment 1
generic_insert_update_key_value_conf ENL_TOMCAT_HOME= /opt/tomcat /etc/apache2/envvars 1
generic_insert_update_key_value_conf ENL_APP_HOME_RELATIVE= opt/tomcat /etc/environment 1
generic_insert_update_key_value_conf TOMCAT_LOG= /opt/tomcat/logs /etc/environment 1
generic_insert_update_key_value_conf TOMCAT_USER= tomcat /etc/environment 1
generic_insert_update_key_value_conf TOMCAT_SUDO_SERVICE= tomcat /etc/environment 1
generic_insert_update_key_value_conf EM_MGMT_HOME= "/var/www/em_mgmt" /etc/environment 1
generic_insert_update_key_value_conf TOMCAT_SERVICE= "/etc/init.d/tomcat" /etc/environment 1
generic_insert_update_key_value_conf OPT_ENLIGHTED= /opt/enLighted /etc/environment 1
generic_insert_update_key_value_conf OPT_ENLIGHTED= /opt/enLighted /etc/apache2/envvars 1
generic_insert_update_key_value_conf ENLIGHTED_HOME= /home/enlighted /etc/environment 1
generic_insert_update_key_value_conf ENLIGHTED_HOME= /home/enlighted /etc/apache2/envvars 1
generic_insert_update_key_value_conf EM_MGMT_BASE= /var/www /etc/environment 1
generic_insert_update_key_value_conf EM_MGMT_BASE= /var/www /etc/apache2/envvars 1
generic_insert_update_key_value_conf ENL_APACHE_HOME= /etc/apache2 /etc/environment 1
generic_insert_update_key_value_conf ENL_APACHE_HOME= /etc/apache2 /etc/apache2/envvars 1
generic_insert_update_key_value_conf PGPORT= 5433 /etc/environment 1
generic_insert_update_key_value_conf UPGRADE_RUN_PATH= /tmp/em_all/home/enlighted  /etc/environment 1

source /etc/environment



