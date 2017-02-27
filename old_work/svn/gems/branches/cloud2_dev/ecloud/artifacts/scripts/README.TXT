eCloud Server Software upgrade
--------------------------------
You need to install the python module 'paramiko' in your linux machine before running the below scripts.

Master server:
-------------
1.Copy the 3 files master_cloud_upgrade.sh,masterpython.py,masterscript.sh in a folder on your linux machine
2.change the url's in the file 'master_cloud_upgrade.sh' with the appropriate jenkins url from where you want to download ecloud.war and ecloud_upgrade.sql
3.change the hostip,username,password values in masterpython.py with the Master Server values.
4.Run the command 'sh master_cloud_upgrade.sh' in terminal.

Replica Server:
---------------
1.Copy the 3 files replica_cloud_upgrade.sh,replicapython.py,replicascript.sh in a folder on your linux machine.
2.change the url's in the file 'replica_cloud_upgrade.sh' with the appropriate jenkins url from where you want to download em_cloud_instance.war.
3.change the hostip,username,password values in replicapython.py with the Replica Server values.
4.Run the command 'sh replica_cloud_upgrade.sh' in terminal.

