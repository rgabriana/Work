from piston.handler import BaseHandler
from django.views.decorators.csrf import csrf_exempt, csrf_protect
from django.core.files import File
import os
from management import util
from django.conf import settings
from management.templatetags import customtemplatetags
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
            if not (directoryname == "/var/lib/tomcat6/Enlighted/UpgradeImages" and os.path.basename(data['file']).endswith("deb")):
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
            subprocess.Popen(["nohup", "/bin/bash", settings.PROJECT_ROOT + "/../adminscripts/debian_upgrade.sh", request.POST.get("file"), "/var/lib/tomcat6/Enlighted/UpgradeImages/", customtemplatetags.revision(), settings.PROJECT_ROOT, request.META['REMOTE_ADDR']])
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
        for line in util.runProcess(["cat", "/var/lib/tomcat6/Enlighted/adminlogs/upgradegems_error.log", "/var/lib/tomcat6/Enlighted/adminlogs/upgradegems.log" ]):
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
                msg = 'FAILURE'
        return {'msg': msg}
            
class BackupRestoreLogsHandler(BaseHandler):
    allowed_methods = ('POST',)
    def create(self, request):
        msg = ''
        isStatus = False
        for line in util.runProcess(["cat", "/var/lib/tomcat6/Enlighted/adminlogs/backuprestore_error.log", "/var/lib/tomcat6/Enlighted/adminlogs/backuprestore.log" ]):
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
        
