# !/bin/bash

ln -s /etc/apache2/mods-available/alias.* /etc/apache2/mods-enabled/.
ln -s /etc/apache2/mods-available/authz_core.load /etc/apache2/mods-enabled/.
ln -s /etc/apache2/mods-available/authz_host.load /etc/apache2/mods-enabled/.
ln -s /etc/apache2/mods-available/headers.load /etc/apache2/mods-enabled/headers.load
ln -s /etc/apache2/mods-available/mime.* /etc/apache2/mods-enabled/.
ln -s /etc/apache2/mods-available/mime_magic.* /etc/apache2/mods-enabled/.
ln -s /etc/apache2/mods-available/mpm.* /etc/apache2/mods-enabled/.
ln -s /etc/apache2/mods-available/proxy.* /etc/apache2/mods-enabled/.
ln -s /etc/apache2/mods-available/proxy_http.load /etc/apache2/mods-enabled/.
ln -s /etc/apache2/mods-available/rewrite.load /etc/apache2/mods-enabled/rewrite.load
ln -s /etc/apache2/mods-available/setenvif.* /etc/apache2/mods-enabled/.
ln -s /etc/apache2/mods-available/ssl.conf /etc/apache2/mods-enabled/ssl.conf
ln -s /etc/apache2/mods-available/ssl.load /etc/apache2/mods-enabled/ssl.load
ln -s /etc/apache2/mods-available/socache_shmcb.load /etc/apache2/mods-enabled/socache_shmcb.load
# Disable SSLv3 protocol which has the POODLE exploit
sed -i s/SSLProtocol\ all/SSLProtocol\ TLSv1\ TLSv1.1\ TLSv1.2/g /etc/apache2/mods-available/ssl.conf
# now change the ports for postgres and tomcat

sed -i s/5432/5433/g /etc/postgresql/9.3/main/postgresql.conf
echo "bytea_output = escape" >> /etc/postgresql/9.3/main/postgresql.conf

sed -i s/8080/9090/g /etc/tomcat6/server.xml

sed -i s/INTERFACES=\"\"/INTERFACES=\"eth1\"/g /etc/default/isc-dhcp-server

sed -i '4i\/opt\/enlighted\/firstrun.sh' /etc/rc.local
