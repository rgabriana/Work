#!/bin/sh
sleep 2
rm ecloud.war
rm ecloud_upgrade.sql
echo "save-energy" | sudo -S rm ecloud_upgrade.sql
wget '--no-check-certificate' https://192.168.1.101/jenkins/job/eCloud_And_Replica_Automation_Server_Release_2.3.x/lastSuccessfulBuild/artifact/EM/ecloud/target/ecloud.war
sleep 5
wget '--no-check-certificate' https://192.168.1.101/jenkins/job/eCloud_And_Replica_Automation_Server_Release_2.3.x/lastSuccessfulBuild/artifact/EM/ecloud/artifacts/sql/ecloud_upgrade.sql
sleep 5
python masterpython.py
sleep 2

