#!/bin/bash
{


	cloudCommunicateTypeValue=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='cloud.communicate.type'" | grep value | cut -d " " -f3)
	echo "$cloudCommunicateTypeValue"
	
}