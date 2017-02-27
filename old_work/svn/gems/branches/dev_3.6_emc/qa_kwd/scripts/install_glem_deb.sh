sleep 10
echo "save-energy" | sudo -S /etc/init.d/tomcat6 stop
sleep 10
echo "save-energy" | sudo -S /etc/init.d/postgresql-8.4 restart
sleep 10
echo "*****************************************************"
echo "Deleting the older database..."
echo "save-energy"| sudo -S dropdb -U postgres emscloud
echo "-----------------------------------------------------"
echo "Installing the glem/orchestrator Master on Server..."
sleep 2s
echo "save-energy"| sudo -S  dpkg -i --force-overwrite *orchestrator.deb
echo "Installation complete..."
echo "----------------------------------------------------"
echo "*****************************************************"

