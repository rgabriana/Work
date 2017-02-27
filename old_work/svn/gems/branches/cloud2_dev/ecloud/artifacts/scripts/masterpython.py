#!/usr/bin/env python
import paramiko
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('192.168.1.58', username='enlighted', password='save-energy')
ftp = ssh.open_sftp()
ftp.put('ecloud.war', 'deploy/ecloud.war')
ftp.put('ecloud_upgrade.sql', 'deploy/ecloud_upgrade.sql')
ftp.put('masterscript.sh', 'deploy/masterscript.sh')
ftp.close()
stdin, stdout, stderr = ssh.exec_command("echo 'save-energy' | sudo -S chmod +x deploy/masterscript.sh")
stdin, stdout, stderr = ssh.exec_command("echo 'save-energy' | sudo -S chmod +x deploy/ecloud_upgrade.sql")
stdin, stdout, stderr = ssh.exec_command("deploy/masterscript.sh")
y = stdout.readlines()
print y
for line in y:
    print line
ssh.close()
