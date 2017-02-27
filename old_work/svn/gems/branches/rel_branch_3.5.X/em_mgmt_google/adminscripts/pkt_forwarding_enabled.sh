#!/bin/bash
{


	uemPktForwardingEnableValue=$(/usr/bin/psql -x -U postgres ems -c "select value from system_configuration where name ='uem.pkt.forwarding.enable'" | grep value | cut -d " " -f3)
	echo "$uemPktForwardingEnableValue"	
	
	
}
