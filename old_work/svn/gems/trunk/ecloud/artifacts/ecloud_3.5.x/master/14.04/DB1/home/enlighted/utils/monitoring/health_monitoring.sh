#!/bin/bash
subject="Health parameters from cloud"
reciepient=cloudgazers@enlightedinc.com
cd /home/enlighted/utils/monitoring
python monitor_servers.py
zip  HealthData.zip HealthData.html
uuencode HealthData.html HealthData.html | mailx -s "$subject" $reciepient
