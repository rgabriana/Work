sleep 10
echo "enLightedCloud" | sudo -S /etc/init.d/tomcat6 stop
sleep 10
echo "enLightedCloud" | sudo -S /etc/init.d/postgresql-8.4 restart
sleep 10
echo "################################################################"
echo "Cleaning /var/lib/tomcat6/webapps/ directory !!!!"
echo "enLightedCloud" | sudo -S rm -rf /var/lib/tomcat6/webapps/ecloud
echo "enLightedCloud" | sudo -S rm -rf /var/lib/tomcat6/webapps/ecloud.war
echo "enLightedCloud" | sudo -S rm -rf /var/lib/tomcat6/work/Catalina/localhost/ecloud
echo "################################################################"
echo "Copying the ecloud.war.............."
echo "enLightedCloud" | sudo -S cp ecloud.war /var/lib/tomcat6/webapps
echo "################################################################"
echo "Starting Database upgrade...."
psql -U postgres emscloud < ecloud_upgrade.sql
echo "################################################################"
echo "enLightedCloud" | sudo -S /etc/init.d/tomcat6 start
sleep 120
sudo service apache2 restart
sleep 60


