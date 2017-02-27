sleep 10
echo "save-energy" | sudo -S /etc/init.d/tomcat6 stop
sleep 10
echo "################################################################"
echo "Cleaning /var/lib/tomcat6/webapps/ directory !!!!"
echo "save-energy" | sudo -S rm -rf /var/lib/tomcat6/webapps/em_cloud_instance
echo "save-energy" | sudo -S rm -rf /var/lib/tomcat6/webapps/em_cloud_instance.war
echo "save-energy" | sudo -S rm -rf /var/lib/tomcat6/work/Catalina/localhost/em_cloud_instance
echo "################################################################"
echo "Copying the em_cloud_instance.war.............."
echo "save-energy"| sudo -S cp em_cloud_instance.war /var/lib/tomcat6/webapps
echo "################################################################"
echo "save-energy" | sudo -S /etc/init.d/tomcat6 start
sleep 120
sudo service apache2 restart
sleep 60

