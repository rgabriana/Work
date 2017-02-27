sleep 10
echo "save-energy" | sudo -S /etc/init.d/tomcat6 stop
sleep 10
echo "save-energy" | sudo -S /etc/init.d/postgresql-8.4 restart
sleep 10
echo "################################################################"
echo "Cleaning /var/lib/tomcat6/webapps/ directory !!!!"
echo "save-energy" | sudo -S rm -rf /var/lib/tomcat6/webapps/ecloud
echo "save-energy" | sudo -S rm -rf /var/lib/tomcat6/webapps/ecloud.war
echo "save-energy" | sudo -S rm -rf /var/lib/tomcat6/work/Catalina/localhost/ecloud
echo "################################################################"
echo "Copying the ecloud.war.............."
echo "save-energy" | sudo -S cp ecloud.war /var/lib/tomcat6/webapps
echo "################################################################"
echo "Starting Database upgrade...."
psql -U postgres ems < ecloud_upgrade.sql
echo "################################################################"
echo "save-energy" | sudo -S /etc/init.d/tomcat6 start
sleep 120
sudo service apache2 restart
sleep 60


