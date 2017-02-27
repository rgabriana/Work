eCloud Server Software upgrade
--------------------------------
You need to install the python module 'paramiko' in your linux machine before running the below scripts.

Run the command 'sudo apt-get install python-paramiko' in terminal to install the python module 'paramiko'.

Master server:
-------------
1.Copy the 3 files master_cloud_upgrade.sh,masterpython.py,masterscript.sh in a folder on your linux machine
2.change JENKINS_ECLOUD_WAR_URL,JENKINS_ECLOUD_UPGRADE_SQL_URL in the file 'master_cloud_upgrade.sh' with the appropriate jenkins url from where you want to download ecloud.war and ecloud_upgrade.sql
3.change the MASTER_CLOUD_IP,MASTER_CLOUD_USERNAME,MASTER_CLOUD_PASSWORD values in the file 'master_cloud_upgrade.sh' with the Master Cloud Server values.
4.Run the command 'sudo sh master_cloud_upgrade.sh' in terminal.

Replica Server:
---------------
1.Copy the 3 files replica_cloud_upgrade.sh,replicapython.py,replicascript.sh in a folder on your linux machine.
2.change JENKINS_ECLOUDINSTANCE_WAR_URL in the file 'replica_cloud_upgrade.sh' with the appropriate jenkins url from where you want to download em_cloud_instance.war.
3.change the REPLICA_CLOUD_IP,REPLICA_CLOUD_USERNAME,REPLICA_CLOUD_PASSWORD values in the file 'replica_cloud_upgrade.sh' with the Replica Cloud Server values.
4.Run the command 'sudo sh replica_cloud_upgrade.sh' in terminal.

