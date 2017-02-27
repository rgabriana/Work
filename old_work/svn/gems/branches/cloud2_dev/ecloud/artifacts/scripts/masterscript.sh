sleep 2
cd /home/enlighted/deploy/
echo "Stoping tomcat service......"
echo "save-energy" | sudo -S /etc/init.d/tomcat6 stop
sleep 10
echo "Starting Database upgrade...."
psql -U postgres emscloud < ecloud_upgrade.sql
sleep 10
echo "Deleting old eloud.war and Copying the new ecloud.war in webapps folder.............."
echo "save-energy" | sudo -S rm -rf /var/lib/tomcat6/webapps/ecloud*
echo "save-energy" | sudo -S rm -rf /var/lib/tomcat6/work/Catalina/localhost/ecloud
echo "save-energy" | sudo -S cp ecloud.war /var/lib/tomcat6/webapps
echo "Starting tomcat service......"
echo "save-energy" | sudo -S /etc/init.d/tomcat6 start
sleep 120
echo "Restarting apache service......"
sudo service apache2 restart
sleep 60
