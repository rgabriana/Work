#!/usr/bin/env python
import paramiko
import sys
MASTER_CLOUD_IP= sys.argv[1]
MASTER_CLOUD_USERNAME= sys.argv[2]
MASTER_CLOUD_PASSWORD= sys.argv[3]
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect(MASTER_CLOUD_IP, username=MASTER_CLOUD_USERNAME, password=MASTER_CLOUD_PASSWORD)
stdin, stdout, stderr = ssh.exec_command("echo "+MASTER_CLOUD_PASSWORD+" | sudo -S mkdir -p /home/enlighted/deploy")
stdin, stdout, stderr = ssh.exec_command("echo "+MASTER_CLOUD_PASSWORD+" | sudo -S chown enlighted:enlighted /home/enlighted/deploy")
ftp = ssh.open_sftp()
ftp.put('ecloud.war', 'deploy/ecloud.war')
ftp.put('ecloud_upgrade.sql', 'deploy/ecloud_upgrade.sql')
ftp.put('masterscript.sh', 'deploy/masterscript.sh')
ftp.close()
stdin, stdout, stderr = ssh.exec_command("echo "+MASTER_CLOUD_PASSWORD+" | sudo -S chmod +x deploy/masterscript.sh")
stdin, stdout, stderr = ssh.exec_command("echo "+MASTER_CLOUD_PASSWORD+" | sudo -S chmod +x deploy/ecloud_upgrade.sql")
stdin, stdout, stderr = ssh.exec_command("echo "+MASTER_CLOUD_PASSWORD+" | sudo -S sh deploy/masterscript.sh "+MASTER_CLOUD_PASSWORD)
y = stdout.readlines()
print y
for line in y:
    print line
ssh.close()
