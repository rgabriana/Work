#!/bin/bash
{


	backupOptionSelected=$(/usr/bin/psql -x -U postgres ems -h localhost -c "select value from system_configuration where name ='backup.option.selected'" | grep value | cut -d " " -f3)
	echo "$backupOptionSelected"
	
}