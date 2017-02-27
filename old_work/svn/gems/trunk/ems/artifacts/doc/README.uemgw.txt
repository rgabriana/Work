1) Insert UEM gateway, just like yet another enlighted gateway using insertGateway.php
	php insertGateway.php --ip=127.0.0.1 --mac=68:54:f5:xx:xx:xx --mask=255.255.255.0
2) Discover this gateway on one of the floors
3) Update the gateway port and gatewayType params in the database manually from console, as currently we don't have support for commissioning UEM gateway from UI.
4) So from the psql console> update gateway set gateway_type=5, port=8085, commissioned='t' where id=?
5) Restart tomcat and you should have a connection established with UEM gateway if this gateway is running

OR

1) Call a get webservice https://hostname/ems/services/uem/add/{host}/{port}/{key}

You should get Success if uem is not already configured and
 it should automatically start forwarding the packets.