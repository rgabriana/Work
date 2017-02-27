import subprocess
from django.utils.translation import ugettext as _
from django.conf import settings
import csv
import hashlib
import os

def runProcessWithErr(exe):    
    p = subprocess.Popen(exe, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    while(True):
        retcode = p.poll()
        line = p.stdout.readline()
        yield line
        if((line is None or line == '')  and retcode is not None):
            break

def runProcess(exe):    
    p = subprocess.Popen(exe, stdout=subprocess.PIPE)
    while(True):
        retcode = p.poll()
        line = p.stdout.readline()
        yield line
        if((line is None or line == '')  and retcode is not None):
            break

def translate(name, values):
    return _(name) % values

#def saveEvent(severity, event_type, description):
#    for line in runProcess(["psql", "-U",  "postgres" , "ems", "-c", "insert into events_and_fault(id, event_time,severity, event_type, description,active) values (nextval('events_seq'), current_timestamp, '" + severity + "', '" + event_type + "', '" + description + "', true)"]):
#        if(line.strip() is not None and line.strip() == "INSERT 0 1" ):
#            break


def saveAuditLog(action_type, description, ip_address):
    for line in runProcess([settings.PROJECT_ROOT + "/../adminscripts/auditlogs.sh", "add", action_type, description, ip_address]):
        if(line.strip() is not None and line.strip() == "INSERT 0 1" ):
            break

def getMountUSBSticks():
    out = []
    for line in runProcess(["ls", "-tr1", "/media"]):
        if(line is not None and line.strip() != "" and line.strip() != "usb"):
            out.append(line.strip())
    return out

def getDBBackupsFromUSBList(usbs):
    out = []
    for usb in usbs:
        fullpath = '/media/' + usb + '/'
        for line in runProcess(["/bin/bash", settings.PROJECT_ROOT + "/../adminscripts/checkandgetbackupfiles.sh", fullpath]):
            if(line is not None and line.strip() != ""):
                out.append(line.strip() + "#" + fullpath)
        fullpath = fullpath + 'dbbackup/'
        for line in runProcess(["/bin/bash", settings.PROJECT_ROOT + "/../adminscripts/checkandgetbackupfiles.sh", fullpath]):
            if(line is not None and line.strip() != ""):
                out.append(line.strip() + "#" + fullpath)
    return out

def getDBBackupsFromSFTPList():
    out = []
    fullpath = '/SFTP/'
    for line in runProcess(["/bin/bash", settings.PROJECT_ROOT + "/../adminscripts/listsftpfiles.sh"]):
        if(line is not None and line.strip() != ""):
            out.append(line.strip() + "#" + fullpath)
    return out

def getAlreadyRunning():
    out = "N"
    for line in runProcess(["head", "-n", "1", os.environ["ENL_APP_HOME"]+"/Enlighted/emsmode"]):
        if(line is not None and line.strip() != ""):
            out = line.strip()
    if out == "NORMAL" or out == "TOMCAT_SHUTDOWN":
        out = "N"
    else:
        out = "Y"
    return out

def getCurrentRunning():
    out = "N"
    for line in runProcess(["head", "-n", "1", os.environ["ENL_APP_HOME"]+"/Enlighted/emsmode"]):
        if(line is not None and line.strip() != ""):
            out = line.strip()
    if out == "NORMAL" or out == "TOMCAT_SHUTDOWN":
        out = "N"
    return out

def setRunning(state):
    for line in runProcess(["checkandsetemmode.sh", state]):
        if(line is not None and line.strip() != ""):
            out = line.strip()
    return out

def getOngoingProc():
    out = "NORMAL"
    for line in runProcess(["head", "-n", "1", os.environ["ENL_APP_HOME"]+"/Enlighted/emsmode"]):
        if(line is not None and line.strip() != ""):
            out = line.strip()
    if out.startswith("NORMAL:BACKUP"):
        out = "BACKUP"
    elif out.startswith("NORMAL:IMAGE_UPGRADE"):
        out = "IMAGE_UPGRADE"
    elif out.startswith("UPGRADE_RESTORE") and out.endswith("deb"):
        out = "EM_UPGRADE"
    elif out.startswith("UPGRADE_RESTORE") and out.endswith("tar.gz"):
        out = "RESTORE"
    return out

def getBackupRestoreLog():
    out = ""
    newline = ""
    isStatus = False
    for line in runProcess(["cat", os.environ["ENL_APP_HOME"]+"/Enlighted/adminlogs/backuprestore_error.log", os.environ["ENL_APP_HOME"]+"/Enlighted/adminlogs/backuprestore.log" ]):
        if(isStatus or line.strip() == "EMS_BACKUP_RESTORE_STARTED"):
            isStatus = True
            newline = line.strip("\n").replace('\"', "&quot;").replace("\'", "&quot;") + "\\"
            out = out + newline
        else:
            out = out + line.strip("\n").replace('\"', "&quot;").replace("\'", "&quot;") + "\\" + "<br>"    
    return out

def getUpgradeLog():
    out = ""
    newline = ""
    isStatus = False
    for line in runProcess(["cat", os.environ["ENL_APP_HOME"]+"/Enlighted/adminlogs/upgradegems_error.log", os.environ["ENL_APP_HOME"]+"/Enlighted/adminlogs/upgradegems.log" ]):
        if(isStatus or line.strip() == "EMS_UPGRADE_STARTED"): 
            isStatus = True
            newline = line.strip("\n").replace('\"', "&quot;").replace("\'", "&quot;") + "\\"
            out = out + newline
        else:
            out = out + line.strip("\n").replace('\"', "&quot;").replace("\'", "&quot;") + "\\" + "<br>"
    return out            

#def getSaltString():
#    salt = ""
#    reader = csv.reader(open(os.environ["ENL_APP_HOME"]+'/Enlighted/adminpasswd'),delimiter=';')
#    for row in reader:
#        salt = row[1]
#    return salt
def getPassString():
    f = open(os.environ["ENL_APP_HOME"]+'/Enlighted/adminpasswd', 'r')
    lines = f.readlines()
    f.close()
    passString = lines[0]
    return passString

def getSaltString():
    f = open(os.environ["ENL_APP_HOME"]+'/Enlighted/adminpasswd', 'r')
    lines = f.readlines()
    f.close()
    passString = lines[0]
    if ';' in passString:
        indexOfcolon = getPassString().index(";")
        saltString = getPassString()[indexOfcolon+1:]
        return saltString
    else :
        return None


def getAdminUserSaltString(username):
    try:
        f = open(os.environ["ENL_APP_HOME"]+'/Enlighted/adminusercredentials', 'r')
    except IOError:
        print 'cannot open', os.environ["ENL_APP_HOME"]+'/Enlighted/adminusercredentials'
    else:
        for line in f:
            if '=' in line:
                indexOfcolon = line.index(";")
                indexOfequal= line.index("=")
                if username == line[:indexOfequal]:
                    saltString = line[indexOfcolon+1:]
                    return saltString
        f.close()
    return None

        
def getPasswordDigest(username, password):
	if username == "admin":
		if getSaltString() is not None:
			m = hashlib.sha1()
			m.update(password)
			m.update('{'+getSaltString().strip()+'}')    
			return m.hexdigest()
		else:
			m = hashlib.md5()
			m.update(password)
			return m.hexdigest()
	else:
		if getAdminUserSaltString(username) is not None:
			m = hashlib.sha1()
			m.update(password)
			m.update('{'+getAdminUserSaltString(username).strip()+'}')    
			return m.hexdigest()
		else:
			m = hashlib.md5()
			m.update(password)
			return m.hexdigest()
	        
	        
def updateSystemConfig(value, name):
    for line in runProcess([settings.PROJECT_ROOT + "/../adminscripts/update_system_config.sh", "update", value, name]):
        if(line.strip() is not None and line.strip() == "UPDATE 1" ):
            break
        
def get_ConnexusFeatureState():
     isConnexusFeatureEnabled=0
     for line in runProcess([settings.PROJECT_ROOT + "/../adminscripts/check_connexusfeature_enabled.sh"]):
        if(line is not None and line.strip() != ""):
            isConnexusFeatureEnabled = line.strip()
     return isConnexusFeatureEnabled

def saveSecurityKey(securityKey):
    status="1"
    for line in runProcess([settings.PROJECT_ROOT + "/../adminscripts/generatekeyfile.sh", "save", securityKey]):
        if(line.strip() is not None and line.strip() != "" ):
             status = line.strip()
    return status
    
def encryptString(stringtoencrypt):
    encryptedString=""
    for line in runProcess([settings.PROJECT_ROOT + "/../adminscripts/encryptdecryptstring.sh", "encrypt", stringtoencrypt]):
        if(line.strip() is not None and line.strip() != "" ):
            encryptedString = line.strip()
    return encryptedString
    
def decryptString(stringtodecrypt):
    decryptedString=""
    for line in runProcess([settings.PROJECT_ROOT + "/../adminscripts/encryptdecryptstring.sh", "decrypt", stringtodecrypt]):
        if(line.strip() is not None and line.strip() != "" ):
            decryptedString = line.strip()
    return decryptedString
    
def getUbuntuVersion():
    ubuntuVersion = ""
    for line in runProcess(["lsb_release", "-sr"]):
        if(line is not None and line.strip() != ""):
            ubuntuVersion = line.strip()
    return ubuntuVersion
    
def getUbuntuType():
    ubuntuType = ""
    for line in runProcess(["uname", "-i"]):
        if(line is not None and line.strip() != ""):
            ubuntuType = line.strip()
    return ubuntuType
    