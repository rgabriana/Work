#!/bin/bash

{
pingStatus=$(wget --no-check-certificate --timeout=10 --tries=3 http://localhost/cloud/heartbeat.jsp 2>&1)
echo $pingStatus
rm -f heartbeat.jsp*
if [[ $pingStatus =~ "200 OK" ]]
then
  echo 'SUCCESS 0'
  exit 0
else
    sleep 300s
    echo 'waiting for 5 more minutes and checking connectivity again.... count = 1'
    pingStatus=$(wget --no-check-certificate --timeout=10 --tries=3 http://localhost/cloud/heartbeat.jsp 2>&1)
    rm -f heartbeat.jsp*
    if [[ $pingStatus =~ "200 OK" ]]
    then
       echo 'SUCCESS 1'
       exit 0
    else
       sleep 300s
       echo 'waiting for 5 more minutes and checking connectivity again.... count = 2'
	   pingStatus=$(wget --no-check-certificate --timeout=10 --tries=3 http://localhost/cloud/heartbeat.jsp 2>&1)
	   rm -f heartbeat.jsp*
	   if [[ $pingStatus =~ "200 OK" ]]
	   then
               echo 'SUCCESS 2'
	       exit 0
	    else

               TPID=`ps -ef | grep tomcat6 | grep java | awk '{print $2}'`
		if [[ ! -z "$TPID" && $TPID -gt 0 ]]
		then
		   echo 'Tomcat6 is running Okay'
		else
		    starttomcat=$(sudo /etc/init.d/tomcat6 restart)
			if [[ "$starttomcat" =~ "done" ]]
			then
					 echo "*** Tomcat service is up. ***"
			else
					echo "Failed to start Tomcat service. Contact enlighted admin."
					exit 3
			fi
		fi
		PGREP="/usr/bin/pgrep"
		HTTPD="apache2"
		$PGREP ${HTTPD}
		if [ $? -ne 0 ] # if apache not running
		then
			startapache=$(sudo /etc/init.d/apache2 restart)
			if [[ "$startapache" =~ "done" ]]
			then
					 echo "*** Apache2 service is up. ***"
			else
					echo "Failed to start Apache2 service. Contact enlighted admin."
					exit 3
			fi
		 fi
	    fi
   fi
fi
}