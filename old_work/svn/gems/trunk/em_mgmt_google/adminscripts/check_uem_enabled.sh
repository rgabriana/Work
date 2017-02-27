#!/bin/bash
{


	uemEnableValue=$(/usr/bin/psql -x -U postgres ems -c "select value from system_configuration where name ='uem.enable'" | grep value | cut -d " " -f3)
	echo "$uemEnableValue"	
	
	
}