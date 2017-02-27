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
healthFile.write("<html><body><style type='text/css'>table,th,tr,td{border:1px solid black;}"+ 
                 "div{background-image: url(http://tablesorter.com/themes/blue/bg.gif);"+ 
				 "background-repeat: no-repeat;background-position: right;cursor: pointer;padding: 4px 18px 4px 4px;}</style>")
healthFile.write("<h3>Enlighted Cloud Monitoring</h3>");
healthFile.write("<span><b>Report Time: </b>"+ str(currentTime) +" (All timestamps are in UTC+0)</span><br/>");
healthFile.write("<span><b>Color Code: </b><label style:'color:red;'>Red: Not connected for &gt; 7 days,  </label>" +
                 "<label style:'color:brown;'>Brown: Not connected for &gt; 15 mnts and &lt; 7 days,  </label>" +
                 "<label style:'color;black;'>Black: All is well</label></span><br/>"	)
healthFile.write("<span><b>Device Status:</b><label> Critical/Under Observation/Total</label></span><br/><br/><label style:'color;black;'>Commissioned</label></span><br/><br/> ");
healthFile.write("<table id='commTable' class='tablesorter'>")

healthDetailsFile = open('HealthDataDetails.html', 'wb')

healthDashboardFile = open('HealthDashboard.html', 'wb')
    
try:
    con = psycopg2.connect(host='localhost', port = cloudPort, database='emscloud', user='postgres',  password='postgres') 
    cur = con.cursor()

    cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name='em_instance' and column_name='last_successful_sync_time'")
    colExists = cur.fetchone()
    	
    cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name='em_instance' and column_name='pause_sync'")
    pauseSyncColExists = cur.fetchone()
    
    healthFile.write("<thead><tr>" + "<th><div>EM ID</div></th>" + "<th><div>Customer</div></th>" + "<th><div>EM Name</div></th>" + "<th><div>Call Home Connectivity</div></th>");
    healthFile.write("<th><div>Data Synch Connectivity</div></th>") if colExists is not None else ""
    healthFile.write("<th><div>Gateway Status</div></th>" + "<th><div>Sensor Status</div></th>" + "<th><div>Max Capture At</div></th>" +
				 "<th><div>Replica Id</div></th>" + "<th><div>Database</div></th>");
    healthFile.write("<th><div>Sync Paused</div></th>") if pauseSyncColExists is not None else ""
    healthFile.write("<th><div>Data Sync Status</div></th>" + "</tr></thead>");	
    healthFile.write("<tbody>")
    unCommTable ="</br></br></br><label style:'color;black;'>Not Commissioned</label></span><br/><br/><table id='unCommTable' class='tablesorter'><thead><tr>" + "<th><div>EM ID</div></th>" + "<th><div>Customer</div></th>" + "<th><div>EM Name</div></th>" + "<th><div>Call Home Connectivity</div></th>";
    unCommTable += "<th><div>Data Synch Connectivity</div></th>" if colExists is not None else ""
    unCommTable += "<th><div>Gateway Status</div></th>" + "<th><div>Sensor Status</div></th>" + "<th><div>Max Capture At</div></th>" + "<th><div>Replica Id</div></th>" + "<th><div>Database</div></th>";
    unCommTable += "<th><div>Sync Paused</div></th>" if pauseSyncColExists is not None else ""
    unCommTable += "<th><div>Data Sync Status</div></th>" + "</tr></thead><tbody>" ;

    if colExists is not None and pauseSyncColExists is not None:	   	    
       cur.execute("select em.id as emId,c.name, em.name, em.replica_server_id,em.database_name as datbaseName, rs.internal_ip as internalIp, " +
                " cast(em.last_connectivity_at as timestamp(0)), cast (em.last_successful_sync_time as timestamp(0)) as dataSynchConnectivity , es.database_status, es.log , " + 
                " em.mac_id , cast(em.em_commissioned_date as timestamp(0)) , em.pause_sync as pauseSync " + 
                " from customer c, em_instance em, replica_server rs, em_state es " +
                " where active = true and em.customer_id = c.id and em.replica_server_id = rs.id and em.latest_em_state_id = es.id and em.sppa_enabled='t'" +
                " order by dataSynchConnectivity ")
    else:	    
		cur.execute("select em.id as emId,c.name, em.name, em.replica_server_id,em.database_name as datbaseName,rs.internal_ip as internalIp, " +
                " cast(em.last_connectivity_at as timestamp(0)) as callHomeConnectivity , es.database_status, es.log , " + 
                " em.mac_id , cast(em.em_commissioned_date as timestamp(0)) " + 
                " from customer c, em_instance em, replica_server rs, em_state es " +
                " where active = true and em.customer_id = c.id and em.replica_server_id = rs.id and em.latest_em_state_id = es.id and em.sppa_enabled='t'" +
                " order by callHomeConnectivity ")
    rows = cur.fetchall()
    for row in rows:
        emId= row[0];
        customerName = row[1];
        emName = row[2];
        replicaId = row[3];
        databaseName = row[4];
        hostName = row[5];
        callHomeConnectivity = row[6];
        if colExists is not None and pauseSyncColExists is not None:
           dataSynchConnectivity = row[7]; 
           dataSynchStatus = row[8];
           dataSynchLog = row[9];
           mac = row[10] ;
           commissionedDate = row[11];
	   pauseSync = row[12];
        else:
           dataSynchConnectivity = None; 
           dataSynchStatus = row[7];
           dataSynchLog = row[8];
           mac = row[9] ;
           commissionedDate = row[10];
	   pauseSync = None;
	
        #print 'For Em ' + emName;	
        
	maxCaptureAt = "0"
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

        if dataSynchStatus != 'NOT_MIGRATED':  
            if dataSynchConnectivity is not None:
	        dataSynchHealthTimeDelta = currentTime - dataSynchConnectivity;
                dataSynchHealthTimeLapse = (dataSynchHealthTimeDelta.days*24*3600 + dataSynchHealthTimeDelta.seconds)/60;
                if(dataSynchHealthTimeLapse > 15 and dataSynchHealthTimeLapse <= 10080):
                    dataSynchHealthStyle = 'brown'
                elif (dataSynchHealthTimeLapse > 10080):
                    dataSynchHealthStyle = 'red'
            results = monitor_em(hostName,replicaPort,databaseName,'postgres', 'postgres',emId,dataSynchConnectivity,healthDetailsFile, customerName+"/" + emName);
        
        if results != None:       
            gatewayStatus = str(results['gatewayCritical'])+"/"+ str(results['gatewayUnderObservation'])+"/" +str(results['gatewayTotal']);
            sensorStatus =  str(results['sensorCritical'])+"/"+ str(results['sensorUnderObservation'])+"/" +str(results['sensorTotal']);
	    maxCaptureAt = str(results['maxCaptureAt'])

        if pauseSync == True:
            syncPaused = 'Yes'
        else:
	    syncPaused = 'No'
	
        if  commissionedDate == None or datetime.datetime.now() <  commissionedDate: 
            unCommTable +=("<tr>" + "<td>" + str(emId) +"</td><td>"+ customerName+"</td><td><a href='#"+ 
                              str(emId) +"'>" + emName+ "</a>" + 
                              "</td><td style='color:" + callHomeHealthStyle +"'>"+
                              str(callHomeConnectivity) +" (" + str(callHealthTimeLapse) +" mnts.)" +
                              "</td>");
	    unCommTable += ("<td style='color:" + dataSynchHealthStyle +"'>"+str(dataSynchConnectivity)+
                              " (" + str(dataSynchHealthTimeLapse) + " mnts.)") if colExists is not None else "" 
            unCommTable += ("</td><td>"+ gatewayStatus + "</td><td>" + sensorStatus + "</td><td>" + maxCaptureAt + 
                              "</td><td>" + str(replicaId) + "</td><td>"+ databaseName + "</td>");
	    unCommTable += ("<td>"+ syncPaused + "</td>") if pauseSyncColExists is not None else ""
	    unCommTable += ("<td>"+ dataSynchStatus + "</td></tr>") ;             
        elif datetime.datetime.now() >=  commissionedDate:
            healthFile.writelines("<tr>" + "<td>" + str(emId) +"</td><td>"+ customerName+"</td><td><a href='#"+ 
                                  str(emId) +"'>" + emName+ "</a>"   +
                                  "</td><td style='color:" + callHomeHealthStyle +"'>"+
                                  str(callHomeConnectivity) +" (" + str(callHealthTimeLapse) +" mnts.)");
	    healthFile.writelines("</td><td style='color:" + dataSynchHealthStyle +"'>"+str(dataSynchConnectivity)+
                                  " (" + str(dataSynchHealthTimeLapse) + " mnts.)") if colExists is not None else ""
	    healthFile.writelines("</td><td>"+ gatewayStatus + "</td><td>" + sensorStatus +"</td><td>" + maxCaptureAt +
                                  "</td><td>" + str(replicaId) + "</td><td>"+ databaseName + "</td>");
            healthFile.writelines("<td>"+ syncPaused + "</td>") if pauseSyncColExists is not None else ""
	    healthFile.writelines("<td>"+ dataSynchStatus + "</td></tr>");
        
 
    healthDetailsFile.close();
            
    healthFile.write("</tbody>")
    healthFile.write("</table>");
    healthFile.write(unCommTable);
    healthFile.write("</tbody>")
    healthFile.write("</table>");
    healthFile.write("<script src='http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js'></script>");    
    healthFile.write("<script type='text/javascript' src='http://tablesorter.com/__jquery.tablesorter.min.js'></script>");
    healthFile.write("<script>$(document).ready(function() {$('#commTable').tablesorter(); $('#unCommTable').tablesorter(); });</script>");
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
        
