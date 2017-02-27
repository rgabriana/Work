sleep 2
cd /home/enlighted/deploy/
echo "Stoping tomcat service......"
echo "save-energy" | sudo -S /etc/init.d/tomcat6 stop
sleep 10
echo "Deleteing old em_cloud_instance.war and Copying the new em_cloud_instance.war in webapps folder.............."
echo "save-energy" | sudo -S rm -rf /var/lib/tomcat6/webapps/em_cloud_instance*
echo "save-energy" | sudo -S rm -rf /var/lib/tomcat6/work/Catalina/localhost/em_cloud_instance
echo "save-energy" | sudo -S cp em_cloud_instance.war /var/lib/tomcat6/webapps
echo "Starting tomcat service......"
echo "save-energy" | sudo -S /etc/init.d/tomcat6 start
sleep 120
echo "Restarting apache service......"
sudo service apache2 restart
sleep 60
