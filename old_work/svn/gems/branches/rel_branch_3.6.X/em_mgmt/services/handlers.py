from piston.handler import BaseHandler
from django.views.decorators.csrf import csrf_exempt, csrf_protect
from django.core.files import File
import os
from management import util
from management.models import BackupFile
from django.conf import settings
from management.templatetags import customtemplatetags
from datetime import datetime
import subprocess

class UploadSizeHandler(BaseHandler):
    allowed_methods = ('POST',)
    def create(self, request):
        for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/checkfilesize.sh"]):
            if(line.strip() is not None and line.strip() != ""):
                return {'msg': util.translate('upload.size.indicator', {"size": line.strip()})}

class UpgradeDeleteHandler(BaseHandler):
    allowed_methods = ('POST',)
    def create(self, request):
        try:
            data = request.data
            directoryname = os.path.dirname(data['file'])
            if not (directoryname == os.environ["ENL_APP_HOME"]+"/Enlighted/UpgradeImages" and os.path.basename(data['file']).endswith("deb")):
                return {'msg': 'I'}
            os.remove(data['file'])
            util.saveAuditLog('EM Management', 'Deleted upgrade debian ' + os.path.basename(data['file']), request.META['REMOTE_ADDR'])
            return {'msg': 'S'}
        except: 
            return {'msg': 'I'}
                
class BackupDeleteHandler(BaseHandler):
    allowed_methods = ('POST',)
    def create(self, request):
        try:
            data = request.data
            directoryname = os.path.dirname(data['file'])
            if directoryname == "/BACKUP":
                directoryname = settings.BACKUP_FILE_PATH
            if not ((directoryname.startswith("/media/") or directoryname.startswith(settings.BACKUP_FILE_PATH)) and os.path.basename(data['file']).endswith("tar.gz")):
                return {'msg': 'I'}
            os.remove(directoryname + "/" + os.path.basename(data['file']))
            util.saveAuditLog('EM Management', 'Deleted database backup ' + os.path.basename(data['file']), request.META['REMOTE_ADDR'])
            return {'msg': 'S'}
        except: 
            return {'msg': 'I'}
                

class UpgradeHandler(BaseHandler):
    allowed_methods = ('POST',)
    def create(self, request):
        msg = 'SUCCESS'
        try:
            subprocess.Popen(["nohup", "/bin/bash", settings.PROJECT_ROOT + "/../adminscripts/debian_upgrade.sh", request.POST.get("file"), os.environ["ENL_APP_HOME"]+"/Enlighted/UpgradeImages/", customtemplatetags.revision(), settings.PROJECT_ROOT, request.META['REMOTE_ADDR']])
            msg = 'SUCCESS'
        except BaseException as err:
            if(str(err).find('Interrupted') == -1):
                msg = 'FAILURE'
        return {'msg': msg}
                

class UpgradeLogsHandler(BaseHandler):
    allowed_methods = ('POST',)
    def create(self, request):
        msg = ''
        isStatus = False
        for line in util.runProcess(["cat", os.environ["ENL_APP_HOME"]+"/Enlighted/adminlogs/upgradegems_error.log", os.environ["ENL_APP_HOME"]+"/Enlighted/adminlogs/upgradegems.log" ]):
            if(isStatus or line.strip() == "EMS_UPGRADE_STARTED"):
                isStatus = True
                msg = msg + line
            else:
                msg = msg + line + "<br />"
        return {'msg': msg}
            

class BackupHandler(BaseHandler):
    allowed_methods = ('POST',)
    def create(self, request):
        msg = 'SUCCESS'
        try:
            p1 = subprocess.Popen(["ls", "-l", settings.BACKUP_FILE_PATH], stdout=subprocess.PIPE)
            p2 = subprocess.Popen(["grep", "tar.gz"], stdin=p1.stdout, stdout=subprocess.PIPE)
            p1.stdout.close()
            p3 = subprocess.Popen(["awk", "{tot +=$5} END {tot=tot/1024; tot=tot/1024; tot=tot/1024; printf(\"%4.2f\", tot)}"], stdin=p2.stdout, stdout=subprocess.PIPE)
            p2.stdout.close()
            total_backup_size = float(p3.communicate()[0])
            if total_backup_size > 20.0:
                return {'msg': 'NOSPACE'}
            for line in util.runProcess(["/bin/bash", settings.PROJECT_ROOT + "/../adminscripts/backuprestoreguiaction.sh", "backup", request.POST.get("file"), settings.BACKUP_FILE_PATH, settings.PROJECT_ROOT, request.META['REMOTE_ADDR']]):
                line = line.strip()
            msg = 'SUCCESS'
        except BaseException as err:
            if(str(err).find('Interrupted') == -1):
                msg = 'FAILURE'
        return {'msg': msg}
            
class RestoreHandler(BaseHandler):
    allowed_methods = ('POST',)
    def create(self, request):
        msg = 'SUCCESS'
        try:
            directoryname = os.path.dirname(request.POST.get("file"))
            if directoryname == "/BACKUP":
                directoryname = settings.BACKUP_FILE_PATH
            if not ((directoryname.startswith("/media/") or directoryname.startswith(settings.BACKUP_FILE_PATH)) and os.path.exists(directoryname + "/" + os.path.basename(request.POST.get("file"))) and os.path.basename(request.POST.get("file")).endswith("tar.gz")):
                return {'msg': 'FAILURE'}
            for line in util.runProcess(["/bin/bash", settings.PROJECT_ROOT + "/../adminscripts/backuprestoreguiaction.sh", "restore", os.path.basename(request.POST.get("file")), directoryname, settings.PROJECT_ROOT, request.META['REMOTE_ADDR'] ]):
                line = line.strip()
        except BaseException as err:
            if(str(err).find('Interrupted') == -1):
                msg = 'FAILURE'
        return {'msg': msg}
            
class BackupRestoreLogsHandler(BaseHandler):
    allowed_methods = ('POST',)
    def create(self, request):
        msg = ''
        isStatus = False
        for line in util.runProcess(["cat", os.environ["ENL_APP_HOME"]+"/Enlighted/adminlogs/backuprestore_error.log", os.environ["ENL_APP_HOME"]+"/Enlighted/adminlogs/backuprestore.log" ]):
            if(isStatus or line.strip() == "EMS_BACKUP_RESTORE_STARTED"):
                isStatus = True
                msg = msg + line
            else:
                msg = msg + line + "<br />"
        return {'msg': msg}
            

class ReleaseLockHandler(BaseHandler):
    allowed_methods = ('POST',)
    def create(self, request):
        isset = util.getCurrentRunning()
        count = 0
        msg = "S"
        while isset != "N" and count < 100:
            msg = util.setRunning("NORMAL")
            isset = util.getCurrentRunning()
            count = count + 1
        return {'msg': msg}
        

class BackupListHandler(BaseHandler):
    allowed_methods = ('POST',)
    def create(self, request):
        backupOptionSelectedValue = 'usb'
        backupUsbPathValue = '/media/usb1'
        backupSftpUsernameValue = ''
        backupSftpPasswordValue = ''
        backupSftpIpValue = ''
        backupSftpDirectoryValue = ''
        backupUsbList = []
        d = []
        for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/backup_option_selected.sh"]):
            if(line is not None and line.strip() != ""):
                backupOptionSelectedValue = line.strip()
        if backupOptionSelectedValue == "sftp":
            sftpfiles = util.getDBBackupsFromSFTPList()
            for sftpfile in sftpfiles:
                fileinfo = sftpfile.split('#')
                backupfile = BackupFile()
                backupfile.filepath = fileinfo[3] + fileinfo[2]
                backupfile.filename = fileinfo[2]
                backupfile.filesize = int(fileinfo[0])/1024
                backupfile.creationDate = fileinfo[1]
                d.append(backupfile)
        
        if backupOptionSelectedValue == "usb":
            usbs = util.getMountUSBSticks()
            dbfiles = util.getDBBackupsFromUSBList(usbs)
            for dbfile in dbfiles:
                fileinfo = dbfile.split('#')
                backupfile = BackupFile()
                backupfile.filepath = fileinfo[3] + fileinfo[2]
                backupfile.filename = fileinfo[2]
                backupfile.filesize = int(fileinfo[0])/1024
                backupfile.creationDate = fileinfo[1]
                d.append(backupfile)
        
        backup_system_path = settings.BACKUP_FILE_PATH + "/"
        os.chdir(backup_system_path)
        for dbfile in os.listdir("."):
            if dbfile.endswith(".tar.gz"):
                f = open(backup_system_path + dbfile, 'r')
                df = File(f)
                backupfile = BackupFile()
                backupfile.filepath = "/BACKUP/" + dbfile
                backupfile.filename = dbfile
                backupfile.filesize = df.size/1024
                backupfile.creationDate = datetime.fromtimestamp(os.path.getmtime(backup_system_path + dbfile)).strftime("%Y-%m-%d %H:%M:%S")
                df.closed
                f.closed
                d.append(backupfile)
        
        return {'filelist': d}
        