#!/usr/bin/python
# -*- coding: utf-8 -*-

import smtplib

from email.MIMEMultipart import MIMEMultipart
from email.MIMEBase import MIMEBase
from email.MIMEText import MIMEText
from email.Utils import COMMASPACE, formatdate
from email import Encoders
import os

def send_mail(dashboardFileName, filename):
    login= 'en_monitoring'
    password=''
    sender= 'no-reply@enlightedinc.com'
    receivers = ['lalit.bhatt@enlightedinc.com','sharad.mahajan@enlightedinc.com','Sreedhar.Kamishetty@enlightedinc.com', 'Quentin.Finck@enlightedinc.com']
    server = 'port80.smtpcorp.com'
    mailPort = 443
    
    message = MIMEMultipart('alternative');
    message['Subject'] = "Health parameters from cloud";
    message['From'] = sender;
    message['To'] = ",".join(receivers);
    
    fo= open(dashboardFileName,"rb");
    html = fo.read();
    part1 = MIMEText(html, 'html');    
    fo.close();
    
    message.attach(part1);
    
    part2 = MIMEBase('application', "octet-stream")
    part2.set_payload(open(filename,"rb").read())
    Encoders.encode_base64(part2);
    part2.add_header('Content-Disposition', 'attachment; filename="%s"' % os.path.basename(filename))
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
