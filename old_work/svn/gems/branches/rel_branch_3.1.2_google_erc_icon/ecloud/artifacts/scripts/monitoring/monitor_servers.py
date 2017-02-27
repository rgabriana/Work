#!/usr/bin/python
# -*- coding: utf-8 -*-

import psycopg2
import sys
from monitor_em import monitor_em
from send_mail import send_mail
import os
import datetime
import fileinput


con = None
cloudPort = '5432'
replicaPort ='5432'

try:
    os.remove("HealthData.html")
except:
    print "No file found"
    
try:
    os.remove("HealthDataDetails.html")
except:
    print "No file found"

try:
    os.remove("HealthDashboard.html")
except:
    print 'Health Dashboard file not found'   

currentTime = datetime.datetime.now();
healthFile = open('HealthData.html', 'wb')
healthFile.write("<html><body><style type='text/css'>table,th,tr,td{border:1px solid black;}</style>")
healthFile.write("<h3>Enlighted Cloud Monitoring</h3>");
healthFile.write("<span><b>Report Time: </b>"+ str(currentTime) +" (All timestamps are in UTC+0)</span><br/>");
healthFile.write("<span><b>Color Code: </b><label style:'color:red;'>Red: Not connected for &gt; 7 days  </label>" +
                 "<label style:'color:brown;'>Brown: Not connected for &gt; 15 mnts and &lt; 7 days  </label>" +
                 "<label style:'color;black;'>Black: All is well</label></span><br/>"	)
healthFile.write("<span><b>Device Status:</b><label> Critical/Under Observation/Total</label></span><br/><br/><label style:'color;black;'>Commissioned</label></span><br/><br/> ");
healthFile.write("<table>")
healthFile.write("<tr>" + "<th>EM ID</th>" + "<th>Customer</th>" + "<th>EM Name</th>" +"<th>Call Home Connectivity</th>"+ "<th>Data Synch Connectivity</th>"
                  "<th>Gateway Status</th>" + "<th>Sensor Status</th>" +"</tr>");
unCommTable ="</br></br></br><label style:'color;black;'>Not Commissioned</label></span><br/><br/><table><tr>" + "<th>EM ID</th>" + "<th>Customer</th>" + "<th>EM Name</th>" +"<th>Call Home Connectivity</th>"+ "<th>Data Synch Connectivity</th>" + "<th>Gateway Status</th>" + "<th>Sensor Status</th>" +"</tr>" ;

internalEmTable ="</br></br></br><label style:'color;black;'>Internal</label></span><br/><br/><table><tr>" + "<th>EM ID</th>" + "<th>Customer</th>" + "<th>EM Name</th>" +"<th>Call Home Connectivity</th>"+ "<th>Data Synch Connectivity</th>" + "<th>Gateway Status</th>" + "<th>Sensor Status</th>" +"</tr>" ;

otherEmTable ="</br></br></br><label style:'color;black;'>Other</label></span><br/><br/><table><tr>" + "<th>EM ID</th>" + "<th>Customer</th>" + "<th>EM Name</th>" +"<th>Call Home Connectivity</th>"+ "<th>Data Synch Connectivity</th>" + "<th>Gateway Status</th>" + "<th>Sensor Status</th>" +"</tr>" ;


healthDetailsFile = open('HealthDataDetails.html', 'wb')

healthDashboardFile = open('HealthDashboard.html', 'wb')
    
try:
    with open('unCommissionedEm.txt', 'r') as f:
    	unCommEmList = [line.strip() for line in f]	
    with open('otherEm.txt', 'r') as f:
        otherEmList = [line.strip() for line in f]    
    with open('internalEm.txt', 'r') as f:
        internalEmList = [line.strip() for line in f]
    con = psycopg2.connect(host='localhost', port = cloudPort, database='emscloud', user='postgres',  password='postgres') 
    cur = con.cursor()
    cur.execute("select em.id as emId,c.name, em.name, em.database_name as datbaseName, rs.internal_ip as internalIp, " +
                " cast(em.last_connectivity_at as timestamp(0)), cast (es.set_time as timestamp(0)) , es.database_status, es.log , em.mac_id " + 
                " from customer c, em_instance em, replica_server rs, em_state es " +
                " where em.customer_id = c.id and em.replica_server_id = rs.id and em.latest_em_state_id = es.id and em.sppa_enabled='t'" +
                " order by c.name, em.name, em.last_connectivity_at ")          
    rows = cur.fetchall()
    for row in rows:
        emId= row[0];
        customerName = row[1];
        emName = row[2];
        databaseName = row[3];
        hostName = row[4];
        callHomeConnectivity = row[5];
        dataSynchConnectivity = row[6]; 
        dataSynchStatus = row[7];
        dataSynchLog = row[8];
	mac = row[9] ;
        
        if callHomeConnectivity is None:
            callHomeConnectivity = currentTime
 
        callHealthTimeDelta =   currentTime - callHomeConnectivity;
        callHealthTimeLapse = (callHealthTimeDelta.days*24*3600 + callHealthTimeDelta.seconds)/60;
        callHomeHealthStyle = 'black';
        if(callHealthTimeLapse > 15 and callHealthTimeLapse <= 10080):
            callHomeHealthStyle = 'brown'
        elif (callHealthTimeLapse > 10080):
            callHomeHealthStyle = 'red'
           
  
        dataSynchHealthStyle = 'black';
        dataSynchHealthTimeLapse= '?';
        results = None;
        gatewayStatus = "NA"
        sensorStatus  = "NA";
        if dataSynchStatus == 'SYNC_READY':  
            try:
                if dataSynchLog != None:
                    dataSynchLogSplit  = dataSynchLog.split("@");
                    dataSynchConnectivity = datetime.datetime.strptime(dataSynchLogSplit[1],'%Y-%m-%d %H:%M:%S');
            except:
                print 'Logs do not have parsable date' 
            dataSynchHealthTimeDelta = currentTime - dataSynchConnectivity;
            dataSynchHealthTimeLapse = (dataSynchHealthTimeDelta.days*24*3600 + dataSynchHealthTimeDelta.seconds)/60;
            if(dataSynchHealthTimeLapse > 15 and dataSynchHealthTimeLapse <= 10080):
                dataSynchHealthStyle = 'brown'
            elif (dataSynchHealthTimeLapse > 10080):
                dataSynchHealthStyle = 'red'
            results = monitor_em(hostName,replicaPort,databaseName,'postgres', 'postgres',emId,dataSynchConnectivity,healthDetailsFile, customerName+"/" + emName);
        else:
            dataSynchConnectivity = dataSynchStatus;


        if results != None:       
            gatewayStatus = str(results['gatewayCritical'])+"/"+ str(results['gatewayUnderObservation'])+"/" +str(results['gatewayTotal']);
            sensorStatus =  str(results['sensorCritical'])+"/"+ str(results['sensorUnderObservation'])+"/" +str(results['sensorTotal']);

        if mac in unCommEmList: 
	           unCommTable +=("<tr>" + "<td>" + str(emId) +"</td><td>"+ customerName+"</td><td><a href='#"+ str(emId) +"'>" + emName+ "</a>" +
                              "</td><td style='color:" + callHomeHealthStyle +"'>"+
                              str(callHomeConnectivity) +" (" + str(callHealthTimeLapse) +" mnts.)" +
                              "</td><td style='color:" + dataSynchHealthStyle +"'>"+str(dataSynchConnectivity)+
                              " (" + str(dataSynchHealthTimeLapse) + " mnts.)" +
                              "</td><td>"+ gatewayStatus + "</td><td>" + sensorStatus +"</td></tr>") ; 
        elif mac in internalEmList: 
            internalEmTable +=("<tr>" + "<td>" + str(emId) +"</td><td>"+ customerName+"</td><td><a href='#"+ str(emId) +"'>" + emName+ "</a>" +
                              "</td><td style='color:" + callHomeHealthStyle +"'>"+
                              str(callHomeConnectivity) +" (" + str(callHealthTimeLapse) +" mnts.)" +
                              "</td><td style='color:" + dataSynchHealthStyle +"'>"+str(dataSynchConnectivity)+
                              " (" + str(dataSynchHealthTimeLapse) + " mnts.)" +
                              "</td><td>"+ gatewayStatus + "</td><td>" + sensorStatus +"</td></tr>") ; 
        elif mac in otherEmList: 
            otherEmTable +=("<tr>" + "<td>" + str(emId) +"</td><td>"+ customerName+"</td><td><a href='#"+ str(emId) +"'>" + emName+ "</a>" +
                              "</td><td style='color:" + callHomeHealthStyle +"'>"+
                              str(callHomeConnectivity) +" (" + str(callHealthTimeLapse) +" mnts.)" +
                              "</td><td style='color:" + dataSynchHealthStyle +"'>"+str(dataSynchConnectivity)+
                              " (" + str(dataSynchHealthTimeLapse) + " mnts.)" +
                              "</td><td>"+ gatewayStatus + "</td><td>" + sensorStatus +"</td></tr>") ; 
        else:
            healthFile.writelines("<tr>" + "<td>" + str(emId) +"</td><td>"+ customerName+"</td><td><a href='#"+ str(emId) +"'>" + emName+ "</a>" + 
                                  "</td><td style='color:" + callHomeHealthStyle +"'>"+
                                  str(callHomeConnectivity) +" (" + str(callHealthTimeLapse) +" mnts.)" +
                                  "</td><td style='color:" + dataSynchHealthStyle +"'>"+str(dataSynchConnectivity)+
                                  " (" + str(dataSynchHealthTimeLapse) + " mnts.)" +
                                  "</td><td>"+ gatewayStatus + "</td><td>" + sensorStatus +"</td></tr>");
        
 
    healthDetailsFile.close();
            
    healthFile.write("</table>");
    healthFile.write(unCommTable);
    healthFile.write("</table>");
    healthFile.write(internalEmTable);
    healthFile.write("</table>");
    healthFile.write(otherEmTable);
    healthFile.write("</table>");
    healthFile.flush();

    #Let's write dashboard file 
    for line in fileinput.input("HealthData.html"):
        healthDashboardFile.write(line); 
    healthDashboardFile.write("</body></html>")
    healthDashboardFile.close()

    healthFile.write("<br/><br/>")
    healthFile.write("***********************<b>Details</b>***************************************<br/>");

    for line in fileinput.input("HealthDataDetails.html"):
        healthFile.write(line);

    healthFile.write("</body></html>")
    healthFile.close()

    send_mail('HealthDashboard.html','HealthData.html')

except psycopg2.DatabaseError, e:
    print 'Error %s' % e    
    sys.exit(1)
    
finally:    
    if con:
        con.close()
        
