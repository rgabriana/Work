#!/usr/bin/python
# -*- coding: utf-8 -*-

import smtplib

from email.MIMEMultipart import MIMEMultipart
from email.MIMEBase import MIMEBase
from email.MIMEText import MIMEText
from email.Utils import COMMASPACE, formatdate
from email import Encoders
import os
import zipfile
import tempfile

def send_mail(dashboardFileName, filename):
    login= 'en_monitoring'
    password='enlighted@123#'
    sender= 'no-reply@enlightedinc.com'
    #receivers = ['Sanjeev.Patel@enlightedinc.com','Sreedhar.Kamishetty@enlightedinc.com']
    receivers = ['cloudgazers@enlightedinc.com'] 
    server = 'port80.smtpcorp.com'
    mailPort = 443
    filenamewthtextn = os.path.splitext(os.path.basename(filename))[0]
    
    message = MIMEMultipart('alternative');
    message['Subject'] = "Health parameters from cloud";
    message['From'] = sender;
    message['To'] = ",".join(receivers);
    
    fo= open(dashboardFileName,"rb");
    html = fo.read();
    part1 = MIMEText(html, 'html');    
    fo.close();
    
    message.attach(part1);
	
    zf = tempfile.TemporaryFile(prefix='mail', suffix='.zip')
    zip = zipfile.ZipFile(zf, 'w', zipfile.ZIP_DEFLATED)
    zip.write(filename)
    zip.close()
    zf.seek(0)      
    
    part2 = MIMEBase('application', "zip")
    part2.set_payload(zf.read())
    Encoders.encode_base64(part2);
    part2.add_header('Content-Disposition', 'attachment', filename=filenamewthtextn + '.zip')
    message.attach(part2);
    
    smtpObj= None
    
    try:
        smtpObj = smtplib.SMTP_SSL(server,mailPort) 
        #smtpObj.ehlo()
        #smtpObj.set_debuglevel(1);
        #smtpObj.starttls()
        #smtpObj.ehlo

        smtpObj.login(login,password);
        smtpObj.sendmail(sender,receivers,message.as_string())
        smtpObj.close()
    except smtplib.SMTPException, e:
        print 'Error %s' % e
        print "Error: unable to send email"
    finally:    
        if smtpObj:
            smtpObj.close()
