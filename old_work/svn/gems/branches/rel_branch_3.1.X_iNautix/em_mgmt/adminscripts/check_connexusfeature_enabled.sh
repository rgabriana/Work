#!/bin/bash
{


	connexusFeatureValue=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='enable.connexusfeature'" | grep value | cut -d " " -f3)
	echo "$connexusFeatureValue"
	
}