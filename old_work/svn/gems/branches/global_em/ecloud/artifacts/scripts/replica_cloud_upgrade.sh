#!/bin/sh
sleep 2
rm em_cloud_instance.war
export REPLICA_CLOUD_IP="192.168.1.58"
export REPLICA_CLOUD_USERNAME="enlighted"
export REPLICA_CLOUD_PASSWORD="save-energy"
export JENKINS_ECLOUDINSTANCE_WAR_URL="https://192.168.1.101/jenkins/job/eCloud_And_Replica_Automation_Server_Release_2.3.x/lastSuccessfulBuild/artifact/EM/em_cloud_instance/target/em_cloud_instance.war"
wget '--no-check-certificate' "$JENKINS_ECLOUDINSTANCE_WAR_URL"
sleep 5
python replicapython.py "$REPLICA_CLOUD_IP" "$REPLICA_CLOUD_USERNAME" "$REPLICA_CLOUD_PASSWORD"
sleep 2
