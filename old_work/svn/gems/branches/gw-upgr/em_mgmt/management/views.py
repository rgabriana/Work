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
    log_msg = ''
    
    if(state.find("UPGRADE_RESTORE") != -1 and state.endswith(".deb")):
        runningFile = state.strip()[16:]
        existingProcess = True
        log_msg = util.getUpgradeLog()
    if request.GET.get('uploadStatus') != None:
        return render_to_response('pages/upgrade.html', {'uploadStatus' : request.GET.get('uploadStatus'), 'fileuploadconfirmation': util.translate('file.upload.successful', {"filename": request.GET.get('filename')})}, context_instance=RequestContext(request, {'filelist': d, 'existingProcess': existingProcess, 'filename': runningFile,'proc': util.getOngoingProc(),'logMessage': log_msg, }))
    else:
        return render_to_response('pages/upgrade.html', context_instance=RequestContext(request, {'filelist': d, 'existingProcess': existingProcess, 'filename': runningFile,'proc': util.getOngoingProc(),'logMessage': log_msg,  }))
    
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
            if not os.path.exists('/var/lib/tomcat6/Enlighted/certs/'):
                os.makedirs('/var/lib/tomcat6/Enlighted/certs/')
            with open('/var/lib/tomcat6/Enlighted/certs/' + file.name, 'wb+') as destination:
                for chunk in file.chunks():
                    destination.write(chunk)
            util.saveAuditLog('EM Management', 'Uploaded certificate file ' + file.name, request.META['REMOTE_ADDR'])
            status = 'S'
    except:
        status = 'F'
    return HttpResponseRedirect('/em_mgmt/clouduemsettings?uploadStatus=' + status + '&filename=' + request.FILES['file'].name)

     
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
    if request.GET.get('uploadStatus') != None:
        return render_to_response('pages/backuprestore.html', {'uploadStatus' : request.GET.get('uploadStatus'), 'fileuploadconfirmation': util.translate('file.upload.successful', {"filename": request.GET.get('filename')})}, context_instance=RequestContext(request, {'filelist': d, 'existingProcess': existingProcess, 'filename': runningFile, 'processType': processType,'proc': util.getOngoingProc(),'logMessage': log_msg, }))
    else:
        return render_to_response('pages/backuprestore.html', context_instance=RequestContext(request, {'filelist': d, 'existingProcess': existingProcess, 'filename': runningFile, 'processType': processType,'proc': util.getOngoingProc(),'logMessage': log_msg, }))
    

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
        path = directoryname + "/" + os.path.basename(request.GET.get("file"))
        wrapper = FileWrapper( open( path, "r" ) )
        response = HttpResponse(wrapper, content_type = "application/x-tar")
        response['Content-Length'] = os.path.getsize( path )
        response['Content-Disposition'] = 'attachment; filename=%s' % smart_str(os.path.basename( path ))
        util.saveAuditLog('EM Management', 'Download requested for database backup ' + os.path.basename(request.GET.get("file")), request.META['REMOTE_ADDR'])
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
                path = "/var/lib/tomcat6/logs/catalina.out"
            else:
                path = "/var/lib/tomcat6/Enlighted/adminlogs/dailybackup.log"
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
        f = open('/var/lib/tomcat6/Enlighted/recoverykey.key')
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
        path = '/var/lib/tomcat6/Enlighted/recoverykey.key'
        wrapper = FileWrapper( open( path, "r" ) )
        response = HttpResponse(wrapper, content_type = "application/text")
        response['Content-Length'] = os.path.getsize( path )
        response['Content-Disposition'] = 'attachment; filename=%s' % smart_str(os.path.basename( path ))
        return response
    
def uploadRecoveryFile(request):
    # upload Enlighted Provided Security Key to '/var/lib/tomcat6/Enlighted/uploadedkey/' path
    status = 'F'
    try:
        if 'file' in request.FILES:
            file = request.FILES['file']
            if not os.path.exists('/var/lib/tomcat6/Enlighted/uploadedkey/'):
                os.makedirs('/var/lib/tomcat6/Enlighted/uploadedkey/')
            with open('/var/lib/tomcat6/Enlighted/uploadedkey/' + file.name, 'wb+') as destination:
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
    f = open('/var/lib/tomcat6/Enlighted/uploadedkey/recoverykey.key', 'r')
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
            os.remove('/var/lib/tomcat6/Enlighted/uploadedkey/recoverykey.key');
            os.rmdir('/var/lib/tomcat6/Enlighted/uploadedkey');
            os.remove('/var/lib/tomcat6/Enlighted/recoverykey.key');
        except OSError:
            pass
    return render_to_response('pages/changepassword.html', context_instance=RequestContext(request, {'newpassword':newpassword,'confirmpassword':confirmpassword,'savestatus':savestatus}))