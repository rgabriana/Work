from django.http import HttpResponse, HttpResponseRedirect
from django.template import Context, loader, RequestContext
from django.shortcuts import render_to_response
from django.conf import settings
import os, time
from datetime import datetime
from django.core.files import File
from management.models import UpgradeFile
from management.models import BackupFile
from management import util
from django.utils.encoding import smart_str
from django.core.servers.basehttp import FileWrapper

def login(request):
    if request.GET.get('errorType') != None:
        return render_to_response('pages/login.html', {request.GET.get('errorType') : "true", 'forward': request.GET.get('forward')}, context_instance=RequestContext(request))
    else:
        return render_to_response('pages/login.html', {'forward': request.GET.get('forward')}, context_instance=RequestContext(request))

def maintenance(request):
    return render_to_response('pages/maintenance.html', context_instance=RequestContext(request))

def home(request):
    return render_to_response('pages/home.html', context_instance=RequestContext(request, {'proc': util.getOngoingProc(), }))

def upgrade(request):
    path = '/var/lib/tomcat6/Enlighted/UpgradeImages/'
    os.chdir(path)
    d = []
    existingProcess = False
    runningFile = ''
    for debfile in os.listdir("."):
        if debfile.endswith(".deb"):
            f = open(path + debfile, 'r')
            df = File(f)
            upgradefile = UpgradeFile()
            upgradefile.filepath = f.name
            upgradefile.filename = debfile
            upgradefile.filesize = df.size/1024
            upgradefile.version = '0'
            for line in util.runProcess(["dpkg-deb", "-f",  path + debfile , "Version"]):
                if(line.strip() is not None and line.strip() != ""):
                    upgradefile.version = line
            upgradefile.creationDate = datetime.fromtimestamp(os.path.getmtime(path + debfile)).strftime("%Y-%m-%d %H:%M:%S")
            df.closed
            f.closed
            d.append(upgradefile)
    state = util.getCurrentRunning()
    if(state.find("UPGRADE_RESTORE") != -1 and state.endswith(".deb")):
        runningFile = state.strip()[16:]
        existingProcess = True
    if request.GET.get('uploadStatus') != None:
        return render_to_response('pages/upgrade.html', {'uploadStatus' : request.GET.get('uploadStatus'), 'fileuploadconfirmation': util.translate('file.upload.successful', {"filename": request.GET.get('filename')})}, context_instance=RequestContext(request, {'filelist': d, 'existingProcess': existingProcess, 'filename': runningFile,'proc': util.getOngoingProc(), }))
    else:
        return render_to_response('pages/upgrade.html', context_instance=RequestContext(request, {'filelist': d, 'existingProcess': existingProcess, 'filename': runningFile,'proc': util.getOngoingProc(), }))
    

def backuprestore(request):
    usbs = util.getMountUSBSticks()
    dbfiles = util.getDBBackupsFromUSBList(usbs)
    d = []
    existingProcess = False
    runningFile = ''
    processType = ''
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

    state = util.getCurrentRunning()
    if(state.find("UPGRADE_RESTORE") != -1 and state.endswith(".tar.gz")):
        runningFile = state.strip()[16:]
        existingProcess = True
        processType = 'restore'
    if(state.find("NORMAL") != -1 and state.find("BACKUP") != -1):
        runningFile = state.strip()[14:]
        existingProcess = True
        processType = 'backup'
    if request.GET.get('uploadStatus') != None:
        return render_to_response('pages/backuprestore.html', {'uploadStatus' : request.GET.get('uploadStatus'), 'fileuploadconfirmation': util.translate('file.upload.successful', {"filename": request.GET.get('filename')})}, context_instance=RequestContext(request, {'filelist': d, 'existingProcess': existingProcess, 'filename': runningFile, 'processType': processType,'proc': util.getOngoingProc(), }))
    else:
        return render_to_response('pages/backuprestore.html', context_instance=RequestContext(request, {'filelist': d, 'existingProcess': existingProcess, 'filename': runningFile, 'processType': processType,'proc': util.getOngoingProc(), }))
    

def authenticate(request):
    forwardURL = request.POST.get('forwardURL')
    if request.POST.get('username') == 'admin' and request.POST.get('password') == 'admin':
        request.session['isAuthenticated'] = True
        util.saveAuditLog('EM Management Validation', 'User admin logged on the EM Management', request.META['REMOTE_ADDR'])
        if forwardURL != 'None' and forwardURL != settings.STATIC_URL and forwardURL != (settings.STATIC_URL + 'login/'):        
            return HttpResponseRedirect(forwardURL)
        else:
            return HttpResponseRedirect('/em_mgmt/home/')    
    else:
        util.saveAuditLog('EM Management Validation', 'User admin authentication failed on EM Management', request.META['REMOTE_ADDR'])
        return HttpResponseRedirect('/em_mgmt/login?errorType=error&forward=' + forwardURL)

def uploadUpgradeFile(request):
    status = 'F'
    try:
        if 'file' in request.FILES:
            file = request.FILES['file']
            with open('/var/lib/tomcat6/Enlighted/UpgradeImages/' + file.name, 'wb+') as destination:
                for chunk in file.chunks():
                    destination.write(chunk)
            util.saveAuditLog('EM Management', 'Uploaded upgrade debian ' + file.name, request.META['REMOTE_ADDR'])
            status = 'S'
    except:
        status = 'F'
    return HttpResponseRedirect('/em_mgmt/upgrade?uploadStatus=' + status + '&filename=' + request.FILES['file'].name)

def uploadBackupFile(request):
    status = 'F'
    try:
        if 'file' in request.FILES:
            file = request.FILES['file']
            with open(settings.BACKUP_FILE_PATH + "/" + file.name, 'wb+') as destination:
                for chunk in file.chunks():
                    destination.write(chunk)
            util.saveAuditLog('EM Management', 'Uploaded database backup ' + file.name, request.META['REMOTE_ADDR'])
            status = 'S'
    except:
        status = 'F'
    return HttpResponseRedirect('/em_mgmt/backuprestore?uploadStatus=' + status + '&filename=' + request.FILES['file'].name)

def download_file(request):
    directoryname = os.path.dirname(request.GET.get('file'))
    if directoryname == "/BACKUP":
        directoryname = settings.BACKUP_FILE_PATH
    if (directoryname.startswith("/media/") or directoryname.startswith(settings.BACKUP_FILE_PATH) and os.path.exists(directoryname + "/" + os.path.basename(request.GET.get("file"))) and os.path.basename(request.GET.get("file")).endswith("tar.gz")):
        print "exists"
        path = directoryname + "/" + os.path.basename(request.GET.get("file"))
        wrapper = FileWrapper( open( path, "r" ) )
        response = HttpResponse(wrapper, content_type = "application/x-tar")
        response['Content-Length'] = os.path.getsize( path )
        response['Content-Disposition'] = 'attachment; filename=%s' % smart_str(os.path.basename( path ))
        util.saveAuditLog('EM Management', 'Download requested for database backup ' + os.path.basename(request.GET.get("file")), request.META['REMOTE_ADDR'])
        return response
    else:
        return HttpResponse(util.translate("error.invalid.file", {}))