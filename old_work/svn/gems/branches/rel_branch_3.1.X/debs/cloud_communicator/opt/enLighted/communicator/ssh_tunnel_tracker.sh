#!/bin/bash
. /opt/enLighted/communicator/config.properties
cloudServerXml=/var/lib/tomcat6/Enlighted/cloudServerInfo.xml
cloudServerIp=(`sed -e 's,.*<domain>\([^<]*\)</domain>.*,\1,g' $cloudServerXml`)

if [ "`ps -eaf | grep ssh | grep localhost | grep $cloudServerIp | grep 22| grep $remoteSshTunnelingPort | grep -v grep`" ] ;
then
 echo "SSH tunnel is up"
 if [ $sshTunnelOn == "false" ]
 then
	 echo "shutting down the tunnel as config setting is false"
         pkill -f ":localhost:22 enlighted@${cloudServerIp}"
 fi
else
	if [ $sshTunnelOn == "true" ]
	then
 		echo "SSH tunnel is down restarting the tunnel"
 		ssh -o "StrictHostKeyChecking no" -o "ServerAliveInterval 300" -o "ServerAliveCountMax 1" -o "ExitOnForwardFailure=yes" -i ${keyPath}  -N -f -q -R ${remoteSshTunnelingPort}:localhost:22 enlighted@${cloudServerIp}
 	fi
fi
