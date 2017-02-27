#!/bin/sh
sleep 2
rm em_cloud_instance.war
wget '--no-check-certificate' https://192.168.1.101/jenkins/job/eCloud_And_Replica_Automation_Server_Release_2.3.x/lastSuccessfulBuild/artifact/EM/em_cloud_instance/target/em_cloud_instance.war
sleep 5
python replicapython.py
sleep 2

