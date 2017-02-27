sleep 10
echo "save-energy" | sudo -S /etc/init.d/tomcat6 stop
sleep 10
echo "save-energy" | sudo -S /etc/init.d/postgresql-8.4 restart
sleep 10
echo "################################################################"
echo "Cleaning /var/lib/tomcat6/webapps/ directory !!!!"
echo "save-energy" | sudo -s rm -rf /media/*
echo "save-energy" | sudo -S rm -rf /var/lib/tomcat6/work/Catalina/localhost/ems*
echo "################################################################"
echo "Dropping the DB .................."
echo "save-energy" | sudo -S -u postgres dropdb ems
echo "################################################################"
echo "Creating the DB .................."
echo "save-energy" | sudo -S -u postgres createdb ems
echo "################################################################"
echo "Running InstallSQL.sql...."
psql -U postgres ems < InstallSQL.sql
echo "################################################################"
echo "Running SingleDebian em_all.deb!!!!"
echo "save-energy" | sudo bash *_debian_upgrade.sh  *_em_all.deb  /home/enlighted/Desktop/latest2.1SingleDebian/  0.0.0.0  /var/www/em_mgmt/em_mgmt  127.0.0.1
sleep 300
echo "################################################################"
echo "save-energy" | sudo -S /etc/init.d/tomcat6 start
sleep 120
sudo a2dismod ssl
sudo a2enmod ssl
sudo service apache2 restart
sleep 60
echo "save-energy" | sudo rm -rf /media/*
sleep 10
