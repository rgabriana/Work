#!/usr/bin/env python
import paramiko
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('192.168.1.58', username='enlighted', password='save-energy')
ftp = ssh.open_sftp()
ftp.put('em_cloud_instance.war', 'deploy/em_cloud_instance.war')
ftp.put('replicascript.sh', 'deploy/replicascript.sh')
ftp.close()
stdin, stdout, stderr = ssh.exec_command("echo 'save-energy' | sudo -S chmod +x deploy/replicascript.sh")
stdin, stdout, stderr = ssh.exec_command("deploy/replicascript.sh")
y = stdout.readlines()
print y
for line in y:
    print line
ssh.close()
