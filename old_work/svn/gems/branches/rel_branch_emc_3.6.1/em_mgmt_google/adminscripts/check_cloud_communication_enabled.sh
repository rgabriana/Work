#!/bin/bash
{


	cloudCommunicateTypeValue=$(/usr/bin/psql -x -U postgres ems -c "select value from system_configuration where name ='enable.cloud.communication'" | grep value | cut -d " " -f3)
	echo "$cloudCommunicateTypeValue"
	
}