sleep 2
export REPLICA_CLOUD_PASSWORD="$1"
cd /home/enlighted/deploy/
echo "Stoping tomcat service......"
echo "$REPLICA_CLOUD_PASSWORD" | sudo -S /etc/init.d/tomcat6 stop
sleep 10
echo "Copying the old ecloud_instance.war in /home/enlighted/apps folder.... "
echo "$REPLICA_CLOUD_PASSWORD" | sudo -S mkdir -p /home/enlighted/apps
export ECLOUDINSTANCE_DIRECTORY="/var/lib/tomcat6/webapps/em_cloud_instance"
if [ -d "$ECLOUDINSTANCE_DIRECTORY" ]; then
	ECLOUDINSTANCE_OLD_BUILD_VERSION=$(cat /var/lib/tomcat6/webapps/em_cloud_instance/META-INF/MANIFEST.MF | grep 'Build-Version' | cut -f2 -d " ")
	echo "$REPLICA_CLOUD_PASSWORD" | sudo -S cp /var/lib/tomcat6/webapps/em_cloud_instance.war /home/enlighted/apps/em_cloud_instance.war."$ECLOUDINSTANCE_OLD_BUILD_VERSION"
fi
echo "Deleting old em_cloud_instance.war and Copying the new em_cloud_instance.war in webapps folder.............."
echo "$REPLICA_CLOUD_PASSWORD" | sudo -S rm -rf /var/lib/tomcat6/webapps/em_cloud_instance*
echo "$REPLICA_CLOUD_PASSWORD" | sudo -S rm -rf /var/lib/tomcat6/work/Catalina/localhost/em_cloud_instance
echo "$REPLICA_CLOUD_PASSWORD" | sudo -S cp em_cloud_instance.war /var/lib/tomcat6/webapps
echo "Starting tomcat service......"
echo "$REPLICA_CLOUD_PASSWORD" | sudo -S /etc/init.d/tomcat6 start
sleep 120
echo "Restarting apache service......"
sudo service apache2 restart
sleep 60
