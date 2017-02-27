sleep 2
export MASTER_CLOUD_PASSWORD="$1"
echo "Stoping tomcat service......"
echo "$MASTER_CLOUD_PASSWORD" | sudo -S /etc/init.d/tomcat6 stop
sleep 10
echo "creating folder /var/lib/tomcat6/Enlighted if not present and giving it the permissions tomcat6:tomcat6 ...."
echo "$MASTER_CLOUD_PASSWORD" | sudo -S mkdir -p /var/lib/tomcat6/Enlighted
echo "$MASTER_CLOUD_PASSWORD" | sudo chown tomcat6:tomcat6 /var/lib/tomcat6/Enlighted
cd /home/enlighted/deploy/
echo "Starting Database upgrade...."
psql -U postgres emscloud < ecloud_upgrade.sql
sleep 10
echo "Copying the old ecloud.war in /home/enlighted/apps folder.... "
echo "$MASTER_CLOUD_PASSWORD" | sudo -S mkdir -p /home/enlighted/apps
export ECLOUD_DIRECTORY="/var/lib/tomcat6/webapps/ecloud"
if [ -d "$ECLOUD_DIRECTORY" ]; then
	ECLOUD_OLD_BUILD_VERSION=$(cat /var/lib/tomcat6/webapps/ecloud/META-INF/MANIFEST.MF | grep 'Build-Version' | cut -f2 -d " ")
	echo "$MASTER_CLOUD_PASSWORD" | sudo -S cp /var/lib/tomcat6/webapps/ecloud.war /home/enlighted/apps/ecloud.war."$ECLOUD_OLD_BUILD_VERSION"
fi
echo "Deleting old eloud.war and Copying the new ecloud.war in webapps folder.............."
echo "$MASTER_CLOUD_PASSWORD" | sudo -S rm -rf /var/lib/tomcat6/webapps/ecloud*
echo "$MASTER_CLOUD_PASSWORD" | sudo -S rm -rf /var/lib/tomcat6/work/Catalina/localhost/ecloud
echo "$MASTER_CLOUD_PASSWORD" | sudo -S cp ecloud.war /var/lib/tomcat6/webapps
echo "Starting tomcat service......"
echo "$MASTER_CLOUD_PASSWORD" | sudo -S /etc/init.d/tomcat6 start
sleep 120
echo "Restarting apache service......"
sudo service apache2 restart
sleep 60
