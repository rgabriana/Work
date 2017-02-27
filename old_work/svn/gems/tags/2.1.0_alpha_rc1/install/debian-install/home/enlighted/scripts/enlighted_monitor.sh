!/bin/bash
#declare out of memory string

if grep 'java.lang.OutOfMemoryError' /var/lib/tomcat6/logs/catalina.out
 then
   service tomcat6 stop
   sleep 30
   timeString=$(date +"%m-%d-%y-%T")
   mv /var/lib/tomcat6/logs/catalina.out /var/lib/tomcat6/logs/catalina.out_$timeString
   rm -rf /var/lib/tomcat6/work/Catalina/localhost/ems
   service tomcat6 start
fi
