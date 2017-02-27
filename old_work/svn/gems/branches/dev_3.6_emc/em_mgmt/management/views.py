from django.http import HttpResponse, HttpResponseRedirect
from django.template import Context, loader, RequestContext
from django.shortcuts import render_to_response,render
from django.conf import settings
import os, time
from datetime import datetime
from django.core.files import File
from management.models import UpgradeFile
from management.models import BackupFile
from management.models import LogFile
from management import util
from django.utils.encoding import smart_str
from django.core.servers.basehttp import FileWrapper
import hashlib
import xml.dom.minidom
from xml.dom.minidom import Node

def login(request):
    request.session["connexusFeature"] = util.get_ConnexusFeatureState()
    if request.GET.get('errorType') != None:
        return render_to_response('pages/login.html', {request.GET.get('errorType') : "true", 'forward': request.GET.get('forward')}, context_instance=RequestContext(request))
    else:
        return render_to_response('pages/login.html', {'forward': request.GET.get('forward')}, context_instance=RequestContext(request))

def maintenance(request):
    return render_to_response('pages/maintenance.html', context_instance=RequestContext(request))


def em_maintenance(request, url):
    return render_to_response('pages/maintenance.html', context_instance=RequestContext(request))

def home(request):
    return render_to_response('pages/home.html', context_instance=RequestContext(request, {'proc': util.getOngoingProc(), }))

def ensure_dir(path):
   try:
        os.makedirs(path)
        return
   except OSError:
        if not os.path.isdir(path):
                raise             
 
def history(request):
    path = settings.HISTORY_FILE_PATH
    ensure_dir(path)
    os.chdir(path)
    d = []
    for tarfile in os.listdir("."):
        if tarfile.endswith(".tar.gz"):
            logfile = LogFile()
            logfile.filename = tarfile
            logfile.lastModifiedDate = datetime.fromtimestamp(os.path.getmtime(path + tarfile)).strftime("%Y-%m-%d %H:%M:%S")
            d.append(logfile)
    return render_to_response('pages/history.html', context_instance=RequestContext(request, {'filelist': d, 'proc': util.getOngoingProc(), }))

def upgrade(request):
    path = os.environ.get('ENL_APP_HOME')+'/Enlighted/UpgradeImages/'
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
    log_msg = ''
    ubuntuVersion = util.getUbuntuVersion()
    ubuntuType = util.getUbuntuType()
    
    if(state.find("UPGRADE_RESTORE") != -1 and state.endswith(".deb")):
        runningFile = state.strip()[16:]
        existingProcess = True
        log_msg = util.getUpgradeLog()
    if request.GET.get('uploadStatus') != None:
        return render_to_response('pages/upgrade.html', {'uploadStatus' : request.GET.get('uploadStatus'), 'fileuploadconfirmation': util.translate('file.upload.successful', {"filename": request.GET.get('filename')})}, context_instance=RequestContext(request, {'filelist': d, 'existingProcess': existingProcess, 'filename': runningFile,'proc': util.getOngoingProc(),'logMessage': log_msg,'ubuntuVersion': ubuntuVersion,'ubuntuType': ubuntuType,  }))
    else:
        return render_to_response('pages/upgrade.html', context_instance=RequestContext(request, {'filelist': d, 'existingProcess': existingProcess, 'filename': runningFile,'proc': util.getOngoingProc(),'logMessage': log_msg,'ubuntuVersion': ubuntuVersion,'ubuntuType': ubuntuType,  }))
    
def getText(nodelist):
    rc = []
    for node in nodelist:
        if node.nodeType == node.TEXT_NODE:
            rc.append(node.data)
    return ''.join(rc)
    
def uemsettings(request):	
    path = settings.CLOUD_SERVER_INFO_FILEPATH
    uememMacValue = "Understand this"    
    isUemEnabled = "0"
    pktForwardingEnabled = "0"
    uemCommunicateTypeValue = ""     
    doc = xml.dom.minidom.parse(path)     
    servers = doc.getElementsByTagName("server")
    for server in servers:
        serverIpTag = server.firstChild
        serverIpValue = getText(serverIpTag.childNodes)
        emMacTag = server.getElementsByTagName("emMac")[0]
        uememMacValue = getText(emMacTag.childNodes)
    uemServerIpValue = ""
    for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/check_uem_ip.sh"]):
		if(line is not None and line.strip() != ""):
    			uemServerIpValue = line.strip()      
    for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/check_uem_enabled.sh"]):
		if(line is not None and line.strip() != ""):
    			isUemEnabled = line.strip()
    for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/pkt_forwarding_enabled.sh"]):
		    if(line is not None and line.strip() != ""):
        			pktForwardingEnabled = line.strip()    
    return render_to_response('pages/uemsettings.html', {'savestatus':request.GET.get('savestatus')},context_instance=RequestContext(request, {'uemServerIpValue':uemServerIpValue,'uememMacValue':uememMacValue,'uemCommunicateTypeValue':uemCommunicateTypeValue,'isUemEnabled':isUemEnabled, 'pktForwardingEnabled': pktForwardingEnabled}))

def cloudsettings(request):
    path = settings.CLOUD_SERVER_INFO_FILEPATH
    doc = xml.dom.minidom.parse(path)
    servers = doc.getElementsByTagName("server")
    for server in servers:
    	serverIpTag = server.firstChild
    	serverIpValue = getText(serverIpTag.childNodes)
    	emMacTag = server.getElementsByTagName("emMac")[0]
    	emMacValue = getText(emMacTag.childNodes)
    cloudCommunicateTypeValue=1
    isCloudEnabled=1
    for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/check_cloud_communicate_type.sh"]):
        if(line is not None and line.strip() != ""):
            cloudCommunicateTypeValue = line.strip()
    for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/check_cloud_communication_enabled.sh"]):
        if(line is not None and line.strip() != ""):
            isCloudEnabled = line.strip()
            
    if request.GET.get('savestatus') != None:
    	return render_to_response('pages/cloudsettings.html', {'savestatus':request.GET.get('savestatus')},context_instance=RequestContext(request, {'serverIpValue':serverIpValue,'emMacValue':emMacValue,'cloudCommunicateTypeValue':cloudCommunicateTypeValue,'isCloudEnabled':isCloudEnabled,}))
    elif request.GET.get('uploadStatus') != None:
        return render_to_response('pages/cloudsettings.html', {'uploadStatus':request.GET.get('uploadStatus'),'fileuploadconfirmation': util.translate('file.upload.successful', {"filename": request.GET.get('filename')})},context_instance=RequestContext(request, {'serverIpValue':serverIpValue,'emMacValue':emMacValue,'cloudCommunicateTypeValue':cloudCommunicateTypeValue,'isCloudEnabled':isCloudEnabled,}))
    else:
    	return render_to_response('pages/cloudsettings.html', context_instance=RequestContext(request, {'serverIpValue':serverIpValue,'emMacValue':emMacValue,'cloudCommunicateTypeValue':cloudCommunicateTypeValue,'isCloudEnabled':isCloudEnabled}))

def clouduemsettings(request):
    path = settings.CLOUD_SERVER_INFO_FILEPATH
    doc = xml.dom.minidom.parse(path)
    servers = doc.getElementsByTagName("server")
    for server in servers:
    	serverIpTag = server.firstChild
    	serverIpValue = getText(serverIpTag.childNodes)
    	emMacTag = server.getElementsByTagName("emMac")[0]
    	emMacValue = getText(emMacTag.childNodes)
    cloudCommunicateTypeValue=1
    isCloudEnabled=1
    for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/check_cloud_communicate_type.sh"]):
        if(line is not None and line.strip() != ""):
            cloudCommunicateTypeValue = line.strip()
    for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/check_cloud_communication_enabled.sh"]):
        if(line is not None and line.strip() != ""):
            isCloudEnabled = line.strip()

    isUemEnabled = "0"
    pktForwardingEnabled = "0"

    uemServerIpValue = ""
    for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/check_uem_ip.sh"]):
		if(line is not None and line.strip() != ""):
    			uemServerIpValue = line.strip()      
    for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/check_uem_enabled.sh"]):
		if(line is not None and line.strip() != ""):
    			isUemEnabled = line.strip()
    for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/pkt_forwarding_enabled.sh"]):
	       if(line is not None and line.strip() != ""):
        		pktForwardingEnabled = line.strip()
            
    if request.GET.get('savestatus') != None:
    	return render_to_response('pages/clouduemsettings.html', {'savestatus':request.GET.get('savestatus')},context_instance=RequestContext(request, {'serverIpValue':serverIpValue,'uemServerIpValue':uemServerIpValue,'emMacValue':emMacValue,'cloudCommunicateTypeValue':cloudCommunicateTypeValue,'isCloudEnabled':isCloudEnabled,'isUemEnabled':isUemEnabled}))
    elif request.GET.get('savestatusuem') != None:
        return render_to_response('pages/clouduemsettings.html', {'savestatusuem':request.GET.get('savestatusuem')},context_instance=RequestContext(request, {'serverIpValue':serverIpValue,'uemServerIpValue':uemServerIpValue,'emMacValue':emMacValue,'cloudCommunicateTypeValue':cloudCommunicateTypeValue,'isCloudEnabled':isCloudEnabled,'isUemEnabled':isUemEnabled}))
    elif request.GET.get('uploadStatus') != None:
        return render_to_response('pages/clouduemsettings.html', {'uploadStatus':request.GET.get('uploadStatus'),'fileuploadconfirmation': util.translate('file.upload.successful', {"filename": request.GET.get('filename')})},context_instance=RequestContext(request, {'serverIpValue':serverIpValue,'uemServerIpValue':uemServerIpValue,'emMacValue':emMacValue,'cloudCommunicateTypeValue':cloudCommunicateTypeValue,'isCloudEnabled':isCloudEnabled,'isUemEnabled':isUemEnabled}))
    else:
    	return render_to_response('pages/clouduemsettings.html', context_instance=RequestContext(request,{'serverIpValue':serverIpValue,'uemServerIpValue':uemServerIpValue,'emMacValue':emMacValue,'cloudCommunicateTypeValue':cloudCommunicateTypeValue,'isCloudEnabled':isCloudEnabled,'isUemEnabled':isUemEnabled}))    

def replaceText(nodelist,newText):
    path2 = settings.CLOUD_SERVER_INFO_FILEPATH
    doc2 = xml.dom.minidom.parse(path2)
    for node in nodelist:
        if node.nodeType == node.TEXT_NODE:
            node.data = newText

def saveUemsettings(request):
    serverIp1 = request.POST.get('serverIp').strip()
    checkbox = request.POST.get('ueCC', False) 
    if checkbox:
        util.updateSystemConfig('1', 'uem.enable')
	    #util.updateSystemConfig('false', 'enable.profilefeature')
        util.updateSystemConfig('1', 'uem.pkt.forwarding.enable')
    else:
        util.updateSystemConfig('0', 'uem.enable') 
	    #util.updateSystemConfig('true', 'enable.profilefeature')
        util.updateSystemConfig('0', 'uem.pkt.forwarding.enable')
    
    util.updateSystemConfig(serverIp1, 'uem.ip')    
    savestatus = 'S'
    return HttpResponseRedirect('/em_mgmt/clouduemsettings?savestatusuem='+ savestatus)
    
def saveCloudsettings(request):
    serverIp1 = request.POST.get('serverIp')
    checkbox = request.POST.get('eCC', False) 
    if checkbox:
        util.updateSystemConfig('1', 'enable.cloud.communication')        
    else:
        util.updateSystemConfig('0', 'enable.cloud.communication')        
        util.updateSystemConfig('true', 'enable.profilefeature')
        
    path1 = settings.CLOUD_SERVER_INFO_FILEPATH
    doc1 = xml.dom.minidom.parse(path1)
    servers1 = doc1.getElementsByTagName("server")
    savestatus = ''
    for server1 in servers1:
    	serverIpTag1 = server1.firstChild
    	replaceText(serverIpTag1.childNodes,serverIp1)
        try:
            f = open(path1, 'w')
            doc1.writexml(f)
            f.close()
            savestatus = 'S'
        except:
            savestatus = 'F'
        
    return HttpResponseRedirect('/em_mgmt/clouduemsettings?savestatus='+ savestatus)

def uploadCertificateFile(request):
    status = 'F'
    try:
        if 'file' in request.FILES:
            file = request.FILES['file']
            if not os.path.exists(os.environ["ENL_APP_HOME"]+'/Enlighted/certs/'):
                os.makedirs(os.environ["ENL_APP_HOME"]+'/Enlighted/certs/')
            with open(os.environ["ENL_APP_HOME"]+'/Enlighted/certs/' + file.name, 'wb+') as destination:
                for chunk in file.chunks():
                    destination.write(chunk)
            util.saveAuditLog('EM Management', 'Uploaded certificate file ' + file.name, request.META['REMOTE_ADDR'])
            status = 'S'
    except:
        status = 'F'
    return HttpResponseRedirect('/em_mgmt/clouduemsettings?uploadStatus=' + status + '&filename=' + request.FILES['file'].name)

     
def backuprestore(request):
    backupOptionSelectedValue = 'usb'
    backupUsbPathValue = '/media/usb1'
    backupSftpUsernameValue = ''
    backupSftpPasswordValue = ''
    backupSftpIpValue = ''
    backupSftpDirectoryValue = ''
    backupUsbList = []
    for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/backup_option_selected.sh"]):
        if(line is not None and line.strip() != ""):
            backupOptionSelectedValue = line.strip()
    for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/backup_usb_path.sh"]):
        if(line is not None and line.strip() != ""):
            backupUsbPathValue = line.strip()
    for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/getbackupusblist.sh"]):
        if(line is not None and line.strip() != ""):
            backupUsbList.append(line.strip())
    d = []
    existingProcess = False
    runningFile = ''
    processType = ''
    
    if backupOptionSelectedValue == "sftp":
        for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/backup_sftp_ip.sh"]):
            if(line is not None and line.strip() != ""):
                backupSftpIpValue = line.strip()
        for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/backup_sftp_directory.sh"]):
            if(line is not None and line.strip() != ""):
                backupSftpDirectoryValue = line.strip()
        for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/backup_sftp_username.sh"]):
            if(line is not None and line.strip() != ""):
                backupSftpUsernameValue = line.strip()
        for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/backup_sftp_password.sh"]):
            if(line is not None and line.strip() != ""):
                backupSftpPasswordValue = util.decryptString(line.strip())
    
    state = util.getCurrentRunning()
    log_msg = ''
    if(state.find("UPGRADE_RESTORE") != -1 and state.endswith(".tar.gz")):
        runningFile = state.strip()[16:]
        existingProcess = True
        processType = 'restore'
        log_msg = util.getBackupRestoreLog()
    if(state.find("NORMAL") != -1 and state.find("BACKUP") != -1):
        runningFile = state.strip()[14:]
        existingProcess = True
        processType = 'backup'
        log_msg = util.getBackupRestoreLog()
    if request.GET.get('savestatususb') != None:
        return render_to_response('pages/backuprestore.html', {'savestatususb' : request.GET.get('savestatususb')}, context_instance=RequestContext(request, {'existingProcess': existingProcess, 'filename': runningFile, 'processType': processType,'proc': util.getOngoingProc(),'logMessage': log_msg,'backupOptionSelectedValue': backupOptionSelectedValue,'backupUsbPathValue': backupUsbPathValue,'backupSftpIpValue': backupSftpIpValue,'backupSftpDirectoryValue': backupSftpDirectoryValue,'backupSftpUsernameValue': backupSftpUsernameValue,'backupSftpPasswordValue': backupSftpPasswordValue,'backupUsbList': backupUsbList, }))
    elif request.GET.get('savestatussftp') != None:
        return render_to_response('pages/backuprestore.html', {'savestatussftp' : request.GET.get('savestatussftp')}, context_instance=RequestContext(request, {'existingProcess': existingProcess, 'filename': runningFile, 'processType': processType,'proc': util.getOngoingProc(),'logMessage': log_msg,'backupOptionSelectedValue': backupOptionSelectedValue,'backupUsbPathValue': backupUsbPathValue,'backupSftpIpValue': backupSftpIpValue,'backupSftpDirectoryValue': backupSftpDirectoryValue,'backupSftpUsernameValue': backupSftpUsernameValue,'backupSftpPasswordValue': backupSftpPasswordValue,'backupUsbList': backupUsbList, }))
    elif request.GET.get('uploadStatus') != None:
        return render_to_response('pages/backuprestore.html', {'uploadStatus' : request.GET.get('uploadStatus'), 'fileuploadconfirmation': util.translate('file.upload.successful', {"filename": request.GET.get('filename')})}, context_instance=RequestContext(request, {'existingProcess': existingProcess, 'filename': runningFile, 'processType': processType,'proc': util.getOngoingProc(),'logMessage': log_msg,'backupOptionSelectedValue': backupOptionSelectedValue,'backupUsbPathValue': backupUsbPathValue,'backupSftpIpValue': backupSftpIpValue,'backupSftpDirectoryValue': backupSftpDirectoryValue,'backupSftpUsernameValue': backupSftpUsernameValue,'backupSftpPasswordValue': backupSftpPasswordValue,'backupUsbList': backupUsbList, }))
    else:
        return render_to_response('pages/backuprestore.html', context_instance=RequestContext(request, {'existingProcess': existingProcess, 'filename': runningFile, 'processType': processType,'proc': util.getOngoingProc(),'logMessage': log_msg,'backupOptionSelectedValue': backupOptionSelectedValue,'backupUsbPathValue': backupUsbPathValue,'backupSftpIpValue': backupSftpIpValue,'backupSftpDirectoryValue': backupSftpDirectoryValue,'backupSftpUsernameValue': backupSftpUsernameValue,'backupSftpPasswordValue': backupSftpPasswordValue,'backupUsbList': backupUsbList, }))


def saveUsbsettings(request):
    if request.POST.get('usbpathsselect') != None:
        backupUsbPath = request.POST.get('usbpathsselect').strip()
    else:
        backupUsbPath = '/media/usb1'
    util.updateSystemConfig('usb', 'backup.option.selected')
    util.updateSystemConfig(backupUsbPath, 'backup.usb.path')    
    saveusbstatus = 'S'
    return HttpResponseRedirect('/em_mgmt/backuprestore?savestatususb='+ saveusbstatus)

def saveSftpsettings(request):
    sftpserverip = request.POST.get('sftpserverip').strip()
    sftpdirectory = request.POST.get('sftpdirectory').strip()
    sftpusername = request.POST.get('sftpusername').strip()
    sftppassword = request.POST.get('sftppassword').strip()
    util.updateSystemConfig('sftp', 'backup.option.selected')
    util.updateSystemConfig(sftpserverip, 'backup.sftp.ip')
    util.updateSystemConfig(sftpdirectory, 'backup.sftp.directory')
    util.updateSystemConfig(sftpusername, 'backup.sftp.username')
    util.updateSystemConfig(util.encryptString(sftppassword), 'backup.sftp.password')
    savesftpstatus = 'S'
    return HttpResponseRedirect('/em_mgmt/backuprestore?savestatussftp='+ savesftpstatus)    

def authenticate(request):
    forwardURL = request.POST.get('forwardURL')
    username = request.POST.get('username')
    passwordString = request.POST.get('password')    
    passwd = util.getPasswordDigest(username,passwordString)
    request.session["username"] = username
    if username == "admin":
		for line in util.runProcess(["authadmin.sh", "auth", username, passwd]):
		    if(line.strip() is not None and line.strip() != ""):
		        authStatus = line.strip()
		        break
		if authStatus == 'S':
		    request.session['isAuthenticated'] = True
		    util.saveAuditLog('EM Management Validation', 'User '+username+' logged on the EM Management', request.META['REMOTE_ADDR'])
		    if forwardURL != 'None' and forwardURL != settings.STATIC_URL and forwardURL != (settings.STATIC_URL + 'login/'):        
		        return HttpResponseRedirect(forwardURL)
		    else:
		        return HttpResponseRedirect('/em_mgmt/home/')    
		else:
		    util.saveAuditLog('EM Management Validation', 'User '+username+' authentication failed on EM Management', request.META['REMOTE_ADDR'])
		    return HttpResponseRedirect('/em_mgmt/login?errorType=error&forward=' + forwardURL)
    else:
		for line in util.runProcess(["authadmin.sh", "auth", username, passwd]):
		    if(line.strip() is not None and line.strip() != ""):
		        authStatus = line.strip()
		        break
		if authStatus == 'S':
		    request.session['isAuthenticated'] = True
		    util.saveAuditLog('EM Management Validation', 'User '+username+' logged on the EM Management', request.META['REMOTE_ADDR'])
		    if forwardURL != 'None' and forwardURL != settings.STATIC_URL and forwardURL != (settings.STATIC_URL + 'login/'):        
		        return HttpResponseRedirect(forwardURL)
		    else:
		        return HttpResponseRedirect('/em_mgmt/home/')    
		else:
		    util.saveAuditLog('EM Management Validation', 'User '+username+' authentication failed on EM Management', request.META['REMOTE_ADDR'])
		    return HttpResponseRedirect('/em_mgmt/login?errorType=error&forward=' + forwardURL)

def uploadUpgradeFile(request):
    status = 'F'
    try:
        if 'file' in request.FILES:
            file = request.FILES['file']
            with open(os.environ["ENL_APP_HOME"]+'/Enlighted/UpgradeImages/' + file.name, 'wb+') as destination:
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
    if directoryname == "/SFTP":
        directoryname = "/tmp"
        for line in util.runProcess([settings.PROJECT_ROOT + "/../adminscripts/getsftpfile.sh", os.path.basename(request.GET.get("file"))]):
            if(line.strip() is not None and line.strip() != ""):
                downloadStatus = line.strip()
                break
    if (directoryname.startswith("/media/") or directoryname.startswith(settings.BACKUP_FILE_PATH) or directoryname.startswith("/tmp") and os.path.exists(directoryname + "/" + os.path.basename(request.GET.get("file"))) and os.path.basename(request.GET.get("file")).endswith("tar.gz")):
        path = directoryname + "/" + os.path.basename(request.GET.get("file"))
        wrapper = FileWrapper( open( path, "r" ) )
        response = HttpResponse(wrapper, content_type = "application/x-tar")
        response['Content-Length'] = os.path.getsize( path )
        response['Content-Disposition'] = 'attachment; filename=%s' % smart_str(os.path.basename( path ))
        util.saveAuditLog('EM Management', 'Download requested for database backup ' + os.path.basename(request.GET.get("file")), request.META['REMOTE_ADDR'])
        if (directoryname.startswith("/tmp")):
            for line in util.runProcess(["rm", path]):
                if(line is not None and line.strip() != ""):
                    break
        return response
    else:
        return HttpResponse(util.translate("error.invalid.file", {}))

def logs_download(request):
    path = settings.HISTORY_FILE_PATH + request.GET.get("file")
    if (os.path.exists(path) and path.endswith("tar.gz")):
        wrapper = FileWrapper( open( path, "r" ) )
        response = HttpResponse(wrapper, content_type = "application/x-tar")
        response['Content-Length'] = os.path.getsize( path )
        response['Content-Disposition'] = 'attachment; filename=%s' % smart_str(os.path.basename( path ))
        return response
    else:
        path = request.GET.get("file")
        if (path == "tomcat" or path == "dailybackup"):
            if (path == "tomcat"):
                path = os.environ["ENL_APP_HOME"]+"/logs/catalina.out"
            else:
                path = os.environ["ENL_APP_HOME"]+"/Enlighted/adminlogs/dailybackup.log"
            wrapper = FileWrapper( open( path, "r" ) )
            response = HttpResponse(wrapper, content_type = "application/x-html")
            response['Content-Length'] = os.path.getsize( path )
            response['Content-Disposition'] = 'attachment; filename=%s' % smart_str(os.path.basename( path ))
            return response
        else:        
            return HttpResponse(util.translate("error.invalid.file", {}))
        
def recoverylockout(request):
    usersecuritykey=""
    emMacValue=""
    downloadCertEnable=1
    try:
        f = open(os.environ["ENL_APP_HOME"]+'/Enlighted/recoverykey.key')
        f.close()
        downloadCertEnable =0
    except IOError as e:
        downloadCertEnable =1
    if request.GET.get('savestatus') != None:
        return render_to_response('pages/recoverylockout.html', {'savestatus':request.GET.get('savestatus')}, context_instance=RequestContext(request, {'usersecuritykey':usersecuritykey,'emMacValue':emMacValue,'downloadCertEnable':downloadCertEnable}))
    elif request.GET.get('uploadStatus') != None:
        return render_to_response('pages/recoverylockout.html', {'uploadStatus':request.GET.get('uploadStatus'),'filemismatch':request.GET.get('filemismatch'),'fileuploadconfirmation': util.translate('file.upload.successful', {"filename": request.GET.get('filename')})},context_instance=RequestContext(request, {'usersecuritykey':usersecuritykey,'emMacValue':emMacValue,'downloadCertEnable':downloadCertEnable}))
    else:
        return render_to_response('pages/recoverylockout.html', context_instance=RequestContext(request, {'usersecuritykey':usersecuritykey,'emMacValue':emMacValue,'downloadCertEnable':downloadCertEnable}))
   
def generaterecoverykey(request):
    usersecuritykey = request.POST.get('usersecuritykey')
    path = settings.CLOUD_SERVER_INFO_FILEPATH
    doc = xml.dom.minidom.parse(path)
    servers = doc.getElementsByTagName("server")
    for server in servers:
        serverIpTag = server.firstChild
        serverIpValue = getText(serverIpTag.childNodes)
        emMacTag = server.getElementsByTagName("emMac")[0]
        emMacValue = getText(emMacTag.childNodes)
    m = hashlib.sha1()
    m.update(emMacValue)
    m.update('{'+usersecuritykey.strip()+'}')    
    generatedsecuritykey = m.hexdigest()
    savestatus = util.saveSecurityKey(generatedsecuritykey)
    return HttpResponseRedirect('/em_mgmt/recoverylockout?savestatus='+ savestatus)

def download_recoverykey(request):
        path = os.environ["ENL_APP_HOME"]+'/Enlighted/recoverykey.key'
        wrapper = FileWrapper( open( path, "r" ) )
        response = HttpResponse(wrapper, content_type = "application/text")
        response['Content-Length'] = os.path.getsize( path )
        response['Content-Disposition'] = 'attachment; filename=%s' % smart_str(os.path.basename( path ))
        return response
    
def uploadRecoveryFile(request):
    # upload Enlighted Provided Security Key to os.environ["ENL_APP_HOME"]+'/Enlighted/uploadedkey/' path
    status = 'F'
    try:
        if 'file' in request.FILES:
            file = request.FILES['file']
            if not os.path.exists(os.environ["ENL_APP_HOME"]+'/Enlighted/uploadedkey/'):
                os.makedirs(os.environ["ENL_APP_HOME"]+'/Enlighted/uploadedkey/')
            with open(os.environ["ENL_APP_HOME"]+'/Enlighted/uploadedkey/' + file.name, 'wb+') as destination:
                for chunk in file.chunks():
                    destination.write(chunk)
            status = 'S'
    except:
        status = 'F'
    
    # Calculate sha1sum for the confirm security string    
    confirmSecurityString = request.POST['confirmsecuritykey']
    path = settings.CLOUD_SERVER_INFO_FILEPATH
    doc = xml.dom.minidom.parse(path)
    servers = doc.getElementsByTagName("server")
    for server in servers:
        serverIpTag = server.firstChild
        serverIpValue = getText(serverIpTag.childNodes)
        emMacTag = server.getElementsByTagName("emMac")[0]
        emMacValue = getText(emMacTag.childNodes)
    m = hashlib.sha1()
    m.update(emMacValue)
    m.update('{'+confirmSecurityString.strip()+'}')    
    regeneratedsecuritykey = m.hexdigest()
    
    # read sha1sum of enlighted provided key 
    f = open(os.environ["ENL_APP_HOME"]+'/Enlighted/uploadedkey/recoverykey.key', 'r')
    lines = f.readlines()
    f.close()
    originalSecurityString = lines[0]
    
    # compare the regenerated key with enlighted provided security key, if they matches then navigate to change password workflow 
    filemismatch='F'
    if (regeneratedsecuritykey == originalSecurityString):
       filemismatch = 'T'
       newpassword=''
       confirmpassword=''
       return render_to_response('pages/changepassword.html', context_instance=RequestContext(request, {'newpassword':newpassword,'confirmpassword':confirmpassword}))
    else:
       filemismatch = 'F'
       return HttpResponseRedirect('/em_mgmt/recoverylockout?uploadStatus=' + status + '&filename=' + request.FILES['file'].name  + '&filemismatch=' + filemismatch)

def changePassword(request):
    savestatus ='F'
    saltString = util.getSaltString()
    newpassword = request.POST.get('newpassword')
    confirmpassword = request.POST.get('confirmpassword')
    
    m = hashlib.sha1()
    m.update(newpassword)
    m.update('{'+saltString.strip()+'}')    
    encryptedpassword = m.hexdigest()
    
    for line in util.runProcess(["authadmin.sh", "changeadminpwd", "admin", encryptedpassword,encryptedpassword,saltString]):
            if(line.strip() is not None and line.strip() != ""):
                savestatus = line.strip()
                break
    if savestatus == 'S':
        try:
            os.remove(os.environ["ENL_APP_HOME"]+'/Enlighted/uploadedkey/recoverykey.key');
            os.rmdir(os.environ["ENL_APP_HOME"]+'/Enlighted/uploadedkey');
            os.remove(os.environ["ENL_APP_HOME"]+'/Enlighted/recoverykey.key');
        except OSError:
            pass
    return render_to_response('pages/changepassword.html', context_instance=RequestContext(request, {'newpassword':newpassword,'confirmpassword':confirmpassword,'savestatus':savestatus}))
