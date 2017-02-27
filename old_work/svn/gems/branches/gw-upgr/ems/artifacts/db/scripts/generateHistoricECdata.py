#!usr/bin/python
#-*- coding: utf-8 -*-

import sys, psycopg2, datetime, os
import random
import csv
con=None
# hours of data to be inserted into db at a time
hour = 6

fromDate = raw_input("Enter From Date (YYYY/MM/DD) :")
fd = datetime.datetime.strptime(fromDate, "%Y/%m/%d")
toDate = raw_input("Enter To Date (YYYY/MM/DD) :")
td = datetime.datetime.strptime(toDate, "%Y/%m/%d")

def writeMinuteRowData(minuteRowData, mark, con):
    minute_csv_out = open('minuteCSV.csv', 'w+')
    mywriter = csv.DictWriter(minute_csv_out, delimiter=',', fieldnames=minfieldnames)                                                    
    for row in minuteRowData:                
        mywriter.writerow(row)
        minute_csv_out.flush()   
    f = open('minuteCSV.csv','r')
    mark.copy_from(f, 'energy_consumption', sep=',')
    con.commit()
    f.close()
    os.remove('minuteCSV.csv')
    return

def writeHourRowData(hourRowData, mark, con):
    hour_csv_out = open('hourCSV.csv', 'w+') 
    hwriter = csv.DictWriter(hour_csv_out, delimiter=',', fieldnames=hourfieldnames)                      
    for hrow in hourRowData:                
        hwriter.writerow(hrow)
        hour_csv_out.flush()
    f = open('hourCSV.csv','r')       
    mark.copy_from(f, 'energy_consumption_hourly', sep=',')
    con.commit()
    f.close()  
    os.remove('hourCSV.csv')
    return

def writeDailyRowData(dailyRowData, mark, con):
    daily_csv_out = open('dailyCSV.csv', 'w+')
    dwriter = csv.DictWriter(daily_csv_out, delimiter=',', fieldnames=dayfieldnames)            
    for drow in dailyRowData:                
        dwriter.writerow(drow)
        daily_csv_out.flush()     
    f = open('dailyCSV.csv','r')       
    mark.copy_from(f, 'energy_consumption_daily', sep=',')
    con.commit()
    f.close()
    os.remove('dailyCSV.csv')
    return

try:

    con = psycopg2.connect("port=5433 dbname='ems' user='postgres' password='postgres'")
    mark = con.cursor() 
     
    mark.execute("SELECT id FROM fixture where state='COMMISSIONED'") 
    records = mark.fetchall()
    
    minuteRowData = []
    hourRowData   = []
    dailyRowData  = []
    minfieldnames = ['id','min_temperature','max_temperature','avg_temperature',
                 'light_on_seconds','light_min_level','light_max_level','light_avg_level',
		 'light_on','light_off','power_used','occ_in','occ_out','occ_count',
		 'dim_percentage','dim_offset','bright_percentage', 'bright_offset',
		 'capture_at','fixture_id','price','cost','base_power_used','base_cost',		 			                       			'saved_power_used','saved_cost','occ_saving','tuneup_saving',
		 'ambient_saving','manual_saving','zero_bucket','avg_volts','curr_state',
		 'motion_bits','power_calc','energy_cum','energy_calib','min_volts','max_volts',
		 'energy_ticks','last_volts','saving_type','cu_status','last_temperature','sys_uptime']
    hourfieldnames = ['id','min_temperature','max_temperature','avg_temperature',
                 'light_on_seconds','light_min_level','light_max_level','light_avg_level',
		 'light_on','light_off','power_used','occ_in','occ_out','occ_count',
		 'dim_percentage','dim_offset','bright_percentage', 'bright_offset',
		 'capture_at','fixture_id','price','cost','base_power_used','base_cost',		 			                       			'saved_power_used','saved_cost','occ_saving','tuneup_saving',
		 'ambient_saving','manual_saving','peak_load','min_load','min_price','max_price','avg_load']
    dayfieldnames = ['id','min_temperature','max_temperature','avg_temperature',
                 'light_on_seconds','light_min_level','light_max_level','light_avg_level',
		 'light_on','light_off','power_used','occ_in','occ_out','occ_count',
		 'dim_percentage','dim_offset','bright_percentage', 'bright_offset',
		 'capture_at','fixture_id','price','cost','base_power_used','base_cost',		 			                       			'saved_power_used','saved_cost','occ_saving','tuneup_saving',
		 'ambient_saving','manual_saving','power_used2','power_used3','power_used4', 			'power_used5','price2','price3','price4','price5','peak_load','min_load','min_price','max_price']
    
    print 'start : data creation'
    ecId = 1
    counter=1 
    print datetime.datetime.now()
    fd1 = fd
    while fd1 < td:           
      for record in records:                    
           minuteRowData.append({'id':ecId,'min_temperature':random.randint(25, 35),'max_temperature':random.randint(25, 35),'avg_temperature':'78.0', 			'light_on_seconds':random.randrange(0, 300),'light_min_level':random.randrange	(200, 8000),'light_max_level':random.randrange(200, 8000), 			'light_avg_level':random.randrange(200, 8000),'light_on':random.randrange(0, 200), 'light_off':random.randrange(0, 200),
		'power_used':random.uniform(0, 77),'occ_in':'0','occ_out':'0','occ_count':'0','dim_percentage':'0',
		'dim_offset':random.randrange(0, 200),'bright_percentage':random.randrange(0, 100),'bright_offset':random.randrange(0, 200),
		'capture_at':fd1.strftime("%Y-%m-%d %H:%M:%S"),'fixture_id':str('%d' %record),'price':'0.11', 'cost':random.uniform(0, 0.5), 			'base_power_used':random.uniform(25, 80),'base_cost':random.uniform(0, 0.5),'saved_power_used':random.uniform(0, 75),
		'saved_cost':random.uniform(-0.5, 0.5),'occ_saving':random.uniform(0, 80),'tuneup_saving':random.uniform(0, 75), 			'ambient_saving':random.uniform(0, 45),'manual_saving':random.uniform(0, 75),'zero_bucket':random.randrange(0, 2),
		'avg_volts':random.randrange(0, 100),'curr_state':random.randint(2, 13),'motion_bits':random.getrandbits(21), 			'power_calc':random.randint(1, 2),'energy_cum':random.getrandbits(21),'energy_calib':random.randrange(0, 40000),
		'min_volts':random.randint(0, 100),'max_volts':random.randint(0, 100),'energy_ticks':random.randint(0, 65535), 			'last_volts':random.randint(0, 100),'saving_type':random.randint(0, 3),'cu_status':'0', 'last_temperature':random.randint(25, 35), 			'sys_uptime':random.getrandbits(10)});           
	   if counter % 12 == 0 :
		hourRowData.append({'id':ecId,'min_temperature':random.randint(25, 35),'max_temperature':random.randint(25, 35),'avg_temperature':'78.0', 			'light_on_seconds':random.randrange(0, 300),'light_min_level':random.randrange	(200, 8000),'light_max_level':random.randrange(200, 8000), 			'light_avg_level':random.randrange(200, 8000),'light_on':random.randrange(0, 200), 'light_off':random.randrange(0, 200),
		'power_used':random.uniform(0, 77),'occ_in':'0','occ_out':'0','occ_count':'0','dim_percentage':'0',
		'dim_offset':random.randrange(0, 200),'bright_percentage':random.randrange(0, 100),'bright_offset':random.randrange(0, 200),
		'capture_at':fd1.strftime("%Y-%m-%d %H:%M:%S"),'fixture_id':str('%d' %record),'price':'0.11', 'cost':random.uniform(0, 0.5), 			'base_power_used':random.uniform(25, 80),'base_cost':random.uniform(0, 0.5),'saved_power_used':random.uniform(0, 75),
		'saved_cost':random.uniform(-0.5, 0.5),'occ_saving':random.uniform(0, 80),'tuneup_saving':random.uniform(0, 75), 			'ambient_saving':random.uniform(0, 45),'manual_saving':random.uniform(0, 75),'peak_load':random.uniform(0, 104), 		  			'min_load':random.uniform(0, 100),'min_price':random.uniform(0, 0.15),'max_price':random.uniform(0, 0.25),'avg_load':random.uniform(0, 100)});
           if counter % 288 == 0 :
		dailyRowData.append({'id':ecId,'min_temperature':random.randint(25, 35),'max_temperature':random.randint(25, 35),'avg_temperature':'78.0', 			'light_on_seconds':random.randrange(0, 300),'light_min_level':random.randrange	(200, 8000),'light_max_level':random.randrange(200, 8000), 			'light_avg_level':random.randrange(200, 8000),'light_on':random.randrange(0, 200), 'light_off':random.randrange(0, 200),
		'power_used':random.uniform(0, 77),'occ_in':'0','occ_out':'0','occ_count':'0','dim_percentage':'0',
		'dim_offset':random.randrange(0, 200),'bright_percentage':random.randrange(0, 100),'bright_offset':random.randrange(0, 200),
		'capture_at':fd1.strftime("%Y-%m-%d %H:%M:%S"),'fixture_id':str('%d' %record),'price':'0.11', 'cost':random.uniform(0, 0.5), 			'base_power_used':random.uniform(25, 80),'base_cost':random.uniform(0, 0.5),'saved_power_used':random.uniform(0, 75),
		'saved_cost':random.uniform(-0.5, 0.5),'occ_saving':random.uniform(0, 80),'tuneup_saving':random.uniform(0, 75), 			'ambient_saving':random.uniform(0, 45),'manual_saving':random.uniform(0, 75),'power_used2':'0.00','power_used3':'0.00','power_used4':'0.00', 			'power_used5':'0.00','price2':'0','price3':'0','price4':'0','price5':'0','peak_load':random.uniform(0, 104), 		  			'min_load':random.uniform(0, 80),'min_price':random.uniform(0, 0.15),'max_price':random.uniform(0, 0.25)});
           
           ecId += 1             

      if counter % (hour*12) == 0 :
            print '6 hour data creation complete'
            print datetime.datetime.now()  
            print '======================'                        
            writeMinuteRowData(minuteRowData, mark, con)
            minuteRowData = []
            print '6 hr DB Insert complete - ec table'
            print datetime.datetime.now()  
            print '======================'
            writeHourRowData(hourRowData, mark, con) 
            hourRowData   = []           
            print '6 hr DB Insert complete - ec hourly table'
            print datetime.datetime.now()  
            print '======================'
            writeDailyRowData(dailyRowData, mark, con)
            dailyRowData  = []            
            print '6 hr DB Insert complete - ec daily table'
            print datetime.datetime.now()  
            print '======================'
      
      counter += 1
      fd1 += datetime.timedelta(minutes=5)     

    print 'everything complete'
    print datetime.datetime.now()
      
except psycopg2.DatabaseError, e:
	if con:
	  con.rollback()
	print 'Error %s' %e
	sys.exit(1)
except IOError, ie:
        print 'IOError %s' %ie

finally:
	if con:
	  con.close()	

sys.exit(0)
