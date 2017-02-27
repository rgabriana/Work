#!/usr/bin/env python
import paramiko
import sys
REPLICA_CLOUD_IP= sys.argv[1]
REPLICA_CLOUD_USERNAME= sys.argv[2]
REPLICA_CLOUD_PASSWORD= sys.argv[3]
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(REPLICA_CLOUD_IP, username=REPLICA_CLOUD_USERNAME, password=REPLICA_CLOUD_PASSWORD)
stdin, stdout, stderr = ssh.exec_command("echo "+REPLICA_CLOUD_PASSWORD+" | sudo -S mkdir -p /home/enlighted/deploy")
stdin, stdout, stderr = ssh.exec_command("echo "+REPLICA_CLOUD_PASSWORD+" | sudo -S chown enlighted:enlighted /home/enlighted/deploy")
ftp = ssh.open_sftp()
ftp.put('em_cloud_instance.war', 'deploy/em_cloud_instance.war')
ftp.put('replicascript.sh', 'deploy/replicascript.sh')
ftp.close()
stdin, stdout, stderr = ssh.exec_command("echo "+REPLICA_CLOUD_PASSWORD+" | sudo -S chmod +x deploy/replicascript.sh")
stdin, stdout, stderr = ssh.exec_command("echo "+REPLICA_CLOUD_PASSWORD+" | sudo -S sh deploy/replicascript.sh "+REPLICA_CLOUD_PASSWORD)
y = stdout.readlines()
print y
for line in y:
    print line
ssh.close()
