#!/bin/sh
sleep 2
rm ecloud.war
rm ecloud_upgrade.sql
export MASTER_CLOUD_IP="192.168.1.58"
export MASTER_CLOUD_USERNAME="enlighted"
export MASTER_CLOUD_PASSWORD="save-energy"
export JENKINS_ECLOUD_WAR_URL="https://192.168.1.101/jenkins/job/eCloud_And_Replica_Automation_Server_Release_2.3.x/lastSuccessfulBuild/artifact/EM/ecloud/target/ecloud.war"
export JENKINS_ECLOUD_UPGRADE_SQL_URL="https://192.168.1.101/jenkins/job/eCloud_And_Replica_Automation_Server_Release_2.3.x/lastSuccessfulBuild/artifact/EM/ecloud/artifacts/sql/ecloud_upgrade.sql"
wget '--no-check-certificate' "$JENKINS_ECLOUD_WAR_URL"
sleep 5
wget '--no-check-certificate' "$JENKINS_ECLOUD_UPGRADE_SQL_URL"
sleep 5
python masterpython.py "$MASTER_CLOUD_IP" "$MASTER_CLOUD_USERNAME" "$MASTER_CLOUD_PASSWORD"
sleep 2
