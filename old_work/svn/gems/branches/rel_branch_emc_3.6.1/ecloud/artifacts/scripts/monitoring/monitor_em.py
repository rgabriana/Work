#!/usr/bin/python
# -*- coding: utf-8 -*-

import psycopg2
import sys
import datetime
from dateutil import tz


def monitor_em(hostname,portNumber,database_name,username, passString,emId, dataSynchConnectivity,healthDetailsFile, emName):
    con = None
    gatewayTotal=0;
    gatewayUnderObservation= 0;
    gatewayCritical = 0;
    sensorTotal= 0;
    sensorUnderObservation=0;
    sensorCritical = 0;
    mca = 0;
    currentTime = datetime.datetime.now();

    try:     
        con = psycopg2.connect(host=hostname, port = portNumber, database=database_name, user=username,  password=passString) 
        cur = con.cursor()
        
        # Get the timezone of this em
        cur.execute("select time_zone from company");
        rows = cur.fetchall();
        time_zone = "UTC";
        
        for row in rows:
            time_zone = row[0];

        #print(str(time_zone));
        if time_zone == 'CST' :
            time_zone = 'America/Chicago';
        #print(str(time_zone));
        emTimeZone = tz.gettz(time_zone);
        #print(str(emTimeZone));
        #print(database_name);
        utc_zone = tz.gettz('UTC');
        #print(emTimeZone);
        #dataSynchConnectivity = datetime.datetime.strptime(str(dataSynchConnectivity), '%Y-%m-%d %H:%M:%S')
        #print(str(dataSynchConnectivity));
        #dataSynchConnectivity = dataSynchConnectivity.replace(tzinfo=utc_zone);
        #dataSynchConnectivity = dataSynchConnectivity.astimezone(emTimeZone);
        #dataSynchConnectivity = dataSynchConnectivity.replace(tzinfo=None);
        
        #Gateways
        cur.execute("select d.id, d.mac_address, d.name, g.app1_version, cast (g.last_connectivity_at as timestamp(0)), " + 
                    "g.no_of_sensors , d.location from gateway g, device d where g.id=d.id and g.commissioned= 't'")          

        rows = cur.fetchall()
        
        healthDetailsFile.write("<a name='" + str(emId) + "'><b> "+ emName + "->Gateways:</b></a>");
        healthDetailsFile.write("<table><tr>" + "<th>Id</th>" + "<th>Name</th>" + "<th>Mac Address</th>" +"<th>Version</th>"+ 
                                "<th>No. of Sensors</th>"+ "<th>Last Connectivity</th>" + "<th>Location</th>" + "</tr>");  
        
        gatewayTotal = len(rows);
        for row in rows:
            gatewayId = row[0];
            gatewayMacAddress = row[1];
            gatewayName = row[2];
            gatewayVersion = row[3];
            gatewayConnectivity = row[4];
            gatewayNoOfSensors = row[5];
            gatewayLocation = row[6];
          
             
            #print(str(gatewayId));
            #print(str(gatewayName));
            #print(str(gatewayConnectivity)); 
            #print(str(emTimeZone));
            #print(str(utc_zone));
            #print(str(database_name));
            #print(str(emId));
            gatewayConnectivity  = datetime.datetime.strptime(str(gatewayConnectivity), '%Y-%m-%d %H:%M:%S')
            #print 'Gateway Connectivity = %', gatewayConnectivity 
            gatewayConnectivity = gatewayConnectivity.replace(tzinfo=emTimeZone)
            gatewayConnectivity = gatewayConnectivity.astimezone(utc_zone);
            gatewayConnectivity = gatewayConnectivity.replace(tzinfo=None);
			
            if dataSynchConnectivity is not None:
                gatewayConnectivityHealthTimeDelta = dataSynchConnectivity - gatewayConnectivity;
            else:
                gatewayConnectivityHealthTimeDelta = currentTime - gatewayConnectivity;

            gatewayConnectivityHealthTimeLapse = (gatewayConnectivityHealthTimeDelta.days*24*3600 + gatewayConnectivityHealthTimeDelta.seconds)/60;
            gatewayConnectivityHealthStyle = 'black';
            if(gatewayConnectivityHealthTimeLapse > 15 and gatewayConnectivityHealthTimeLapse <= 10080):
                gatewayConnectivityHealthStyle = 'brown';
                gatewayUnderObservation += 1;
            elif (gatewayConnectivityHealthTimeLapse > 10080):
                gatewayConnectivityHealthStyle = 'red'
                gatewayCritical += 1;            
              
            if gatewayVersion is None:
                gatewayVersion="NA";
                
            if gatewayConnectivityHealthTimeLapse > 15: 
                healthDetailsFile.writelines("<tr>" + "<td>" + str(gatewayId) +"</td><td>"+gatewayName +"</td><td>" + gatewayMacAddress+ 
                                          "</td><td>" + gatewayVersion + "</td><td>" + str(gatewayNoOfSensors) +
                                        "</td><td style='color:" + gatewayConnectivityHealthStyle +"'>"+str(gatewayConnectivity)+ 
                                          " (" + str(gatewayConnectivityHealthTimeLapse) +" mnts.)" +
                                        "</td><td>" + str(gatewayLocation) + "</td></tr>");
            
        healthDetailsFile.write("</table>");
        healthDetailsFile.write("----------------------------------------------------------------------<br/>");
         
        # Sensors 
        cur.execute("select d.id, d.mac_address, d.name, cast(f.last_connectivity_at as timestamp(0)), d.version ,d.location " + 
                    " from fixture f, device d where f.id=d.id and f.state='COMMISSIONED'");
        rows = cur.fetchall()
        
        healthDetailsFile.write("<b> "+ emName + "->Sensors:</b>");
        healthDetailsFile.write("<table><tr>" + "<th>Id</th>" + "<th>Name</th>" + "<th>Mac Address</th>" +"<th>Version</th>"+ 
                                "<th>Last Connectivity</th>"  + "<th>Location</th>" +"</tr>");         
        
        sensorTotal = len(rows);
        for row in rows:
            sensorId = row[0];
            sensorMacAddress = row[1];
            sensorName = row[2];
            sensorLastConnectivity = row[3];
            sensorVersion = row[4];
            sensorLocation = row[5];

            sensorLastConnectivity  = datetime.datetime.strptime(str(sensorLastConnectivity), '%Y-%m-%d %H:%M:%S')
            sensorLastConnectivity = sensorLastConnectivity.replace(tzinfo=emTimeZone)
            sensorLastConnectivity = sensorLastConnectivity.astimezone(utc_zone);
            sensorLastConnectivity = sensorLastConnectivity.replace(tzinfo=None);
            
            if dataSynchConnectivity is not None:
                sensorConnectivityHealthTimeDelta = dataSynchConnectivity - sensorLastConnectivity;
            else:
                sensorConnectivityHealthTimeDelta = currentTime - sensorLastConnectivity;
				
            sensorConnectivityHealthTimeLapse = (sensorConnectivityHealthTimeDelta.days*24*3600 + sensorConnectivityHealthTimeDelta.seconds)/60;
            sensorConnectivityHealthStyle = 'black';
            if(sensorConnectivityHealthTimeLapse > 15 and sensorConnectivityHealthTimeLapse <= 10080):
                sensorConnectivityHealthStyle = 'brown';
                sensorUnderObservation += 1;
            elif (sensorConnectivityHealthTimeLapse > 10080):
                sensorConnectivityHealthStyle = 'red';
                sensorCritical += 1;
            
            if sensorConnectivityHealthTimeLapse > 15:      
                    healthDetailsFile.writelines("<tr>" + "<td>" + str(sensorId) +"</td><td>"+ sensorName+"</td><td>" + sensorMacAddress+ 
                                          "</td><td>" + sensorVersion +
                                        "</td><td style='color:" + sensorConnectivityHealthStyle +"'>"+str(sensorLastConnectivity) +
                                        " (" + str(sensorConnectivityHealthTimeLapse) + " mnts.)" +  
                                        "</td><td>"+ sensorLocation +"</td></tr>");    
         
        healthDetailsFile.write("</table>");     
        healthDetailsFile.write("----------------------------------------------------------------------<br/>");


        cur.execute("select to_char(max(capture_at), 'YYYY-MM-DD HH24:MI:SS') from energy_consumption")          
        cat = cur.fetchone()
	for c in cat:
		if (c is None) or (len(c) == 0):
			mca = 0 
		else:
			mca = c 
        		mca =  datetime.datetime.strptime(mca, '%Y-%m-%d %H:%M:%S')
		        mca = mca.replace(tzinfo=emTimeZone)
		        mca = mca.astimezone(utc_zone)
		        mca = mca.replace(tzinfo=None)

        return {'gatewayTotal' : gatewayTotal, 'gatewayUnderObservation' : gatewayUnderObservation ,  'gatewayCritical' : gatewayCritical,
                'sensorTotal' : sensorTotal,  'sensorUnderObservation' : sensorUnderObservation,  'sensorCritical' : sensorCritical,
		'maxCaptureAt' : mca}
        
    except psycopg2.DatabaseError, e:
        print 'Error %s' % e    
        
    finally:
        
        if con:
            con.close()
