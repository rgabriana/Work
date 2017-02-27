#!/bin/bash
{


	uemIpValue=$(/usr/bin/psql -x -U postgres ems -c "select value from system_configuration where name ='uem.ip'" | grep value | cut -d " " -f3)
	echo "$uemIpValue"	
	
	
}