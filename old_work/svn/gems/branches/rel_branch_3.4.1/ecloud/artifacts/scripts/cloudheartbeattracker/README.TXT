Cloud HeartBeat Tracker will monitor apache and tomcat server status every 5 minute and will restart if any one is down more than 10 minutes.

Cloud HeartBeat Tracker Installation Notes:
-------------------------------------------

1. Copy cloud_heartbeat_tracker.sh script into /home/enlighted/utils/monitoring/

2. Change the permission as executable and chnage owner to enlighted
   sudo chmod 777 cloud_heartbeat_tracker.sh
   sudo chown enlighted:enlighted cloud_heartbeat_tracker.sh
   
3. create directory as 'cloud' into /var/lib/tomcat6/webapps

4. Copy heartbeat.jsp into /var/lib/tomcat6/webapps/cloud/

5. Change the permission as executable and chnage owner to tomcat6
   sudo chmod 777 heartbeat.jsp
   sudo chown tomcat6:tomcat6 heartbeat.jsp
   
6. add below entry into sudoers file located in /etc/sudoers
   /etc/init.d/tomcat6
   
7. add cron entry to execute the above script to run every 5 min as below
   sudo crontab -e
   */5 * * * * /home/enlighted/utils/monitoring/cloud_heartbeat_tracker.sh > /home/enlighted/cm.log 2>&1
   
8. Restart the cron job
   sudo service cron restart

9. You can check current status in the cm.log file
   tail -f ~/cm.log